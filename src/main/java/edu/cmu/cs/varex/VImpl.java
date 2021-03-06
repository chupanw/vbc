package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.vbc.VException;
import edu.cmu.cs.vbc.config.VERuntime;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;

/**
 * internal implementation of V
 */
public class VImpl<T> implements V<T>, Serializable {

    static int expensiveWarningThreshold = 1024;

    static <U> V<? extends U> choice(FeatureExpr condition, U a, U b) {
        Map<U, FeatureExpr> result = new HashMap<>(2);
        if (VCache.isSatisfiable(condition))
            put(result, a, condition);
        else return V.one(VCache.not(condition), b);
        if (VCache.isSatisfiable(VCache.not(condition)))
            put(result, b, VCache.not(condition));
        else return V.one(condition, a);

        return createV(result);
    }

    static <U> V<? extends U> choice(FeatureExpr condition, V<? extends U> a, V<? extends U> b) {
        Map<U, FeatureExpr> result = new HashMap<>(2);
        if (VCache.isSatisfiable(condition))
            addVToMap(result, condition, a);
        else return b;
        if (VCache.isSatisfiable(VCache.not(condition)))
            addVToMap(result, VCache.not(condition), b);
        else return a;

        return createV(result);
    }

    private VImpl(Map<T, FeatureExpr> values) {
        this.values = values;
        if (values.size() > expensiveWarningThreshold) {
            System.err.println("Too many alternatives, potentially expensive: " + this.values.size());
            expensiveWarningThreshold = values.size();
        }
        assert checkInvariant() : "invariants violated";
    }


    @Override
    public V<T> restrictInteractionDegree() {
        Map<T, FeatureExpr> results = new HashMap<>();
        for (Map.Entry<T, FeatureExpr> entry : values.entrySet()) {
            if (entry.getValue().isSatisfiable() && !V.isDegreeTooHigh(entry.getValue())) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return createV(results);
    }

    //invariant: nonempty, all FeatureExpr together yield true
    private final Map<T, FeatureExpr> values;

    private boolean checkInvariant() {
        if (values.isEmpty()) return false;// : "empty V";
        if (values.size() < 2) return false;// : "singleton VImpl?";
        FeatureExpr conditions = FeatureExprFactory.False();
        for (FeatureExpr cond : values.values()) {
            if (!(VCache.isContradiction(VCache.and(conditions, cond)))) return false;// : "condition overlaps with previous condition";
            conditions = conditions.or(cond);
        }
        //"conditions together not a tautology" is no longer required, it just expresses a smaller config space

        return true;
    }


    @Override
    public T getOne() {
        assert false : "getOne called on Choice: " + this;
        return values.keySet().iterator().next();
    }

//    @Override
//    public T getOne(FeatureExpr ctx) {
//        T result = null;
//        boolean foundResult = false;
//        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet())
//            if (ctx.and(e.getValue()).isSatisfiable()) {
//                assert !foundResult : "getOne(" + ctx + ") called on Choice with multiple matching values: " + this;
//                result = e.getKey();
//                foundResult=true;
//            }
//        assert foundResult : "getOne(" + ctx + ") called but no result found: " + this;  //this should always hold if the context is satisfiable
//
//        return result;
//    }

    private void interceptThrowable(Throwable t, FeatureExpr ctx) {
        if (!VERuntime.shouldPostpone(ctx)) {
            VERuntime.throwExceptionCtx(ctx);
            if (t instanceof VException) {
                throw (VException) t;
            }
            else {
                throw new VException(t, ctx);
            }
        } else {
            VERuntime.postponeException(t, ctx);
        }
    }

    @Override
    public <U> V<? extends U> map(Function<? super T, ? extends U> fun) {
        Map<U, FeatureExpr> result = new HashMap<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            try {
                put(result, fun.apply(e.getKey()), e.getValue());
            } catch (Throwable t) {
                interceptThrowable(t, e.getValue());
            }
        }
        return createV(result);
    }

    @Override
    public <U> V<? extends U> map(@Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun) {
        Map<U, FeatureExpr> result = new HashMap<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            try {
                put(result, fun.apply(e.getValue(), e.getKey()), e.getValue());
            } catch (Throwable t) {
                interceptThrowable(t, e.getValue());
            }
        }
        return createV(result);
    }

    @Override
    public <U> V<? extends U> flatMap(Function<? super T, V<? extends U>> fun) {
        assert fun != null;
        Map<U, FeatureExpr> result = new HashMap<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            try {
                V<? extends U> u = fun.apply(e.getKey());
                assert u != null;
                addVToMap(result, e.getValue(), u);
            } catch (Throwable t) {
                interceptThrowable(t, e.getValue());
            }
        }
        return createV(result).restrictInteractionDegree();
    }

    @Override
    public <U> V<? extends U> flatMap(@Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun) {
        Map<U, FeatureExpr> result = new HashMap<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            try {
                V<? extends U> u = fun.apply(e.getValue(), e.getKey());
                assert u != null;
                addVToMap(result, e.getValue(), u);
            } catch (Throwable t) {
                interceptThrowable(t, e.getValue());
            }
        }
        return createV(result).restrictInteractionDegree();
    }


    private static <U> void addVToMap(Map<U, FeatureExpr> result, FeatureExpr ctx, @Nonnull V<? extends U> u) {
        assert u != null;
        if (u instanceof VEmpty)
            return;
        else if (u instanceof One)
            put(result, ((One<U>) u).value, VCache.and(ctx, ((One<U>) u).configSpace));
        else
            for (HashMap.Entry<U, FeatureExpr> ee : ((VImpl<U>) u).values.entrySet()) {
                FeatureExpr cond = VCache.and(ctx, ee.getValue());
                if (VCache.isSatisfiable(cond))
                    put(result, ee.getKey(), cond);
            }
    }

    private static <U> void put(Map<U, FeatureExpr> map, U value, FeatureExpr condition) {
        if (map.containsKey(value))
            condition = condition.or(map.get(value));
        map.put(value, condition);
    }

    private static <U> V<U> createV(Map<U, FeatureExpr> map) {
        if (map.size() == 0)
            return VEmpty.instance();
        if (map.size() == 1) {
            Map.Entry<U, FeatureExpr> entry = map.entrySet().iterator().next();
            return new One(entry.getValue(), entry.getKey());
        }
        return new VImpl<>(map);
    }

    @Override
    public void foreach(@Nonnull Consumer<T> fun) {
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            try {
                fun.accept(e.getKey());
            } catch (Throwable t) {
                interceptThrowable(t, e.getValue());
            }
        }
    }

    @Override
    public void foreach(@Nonnull BiConsumer<FeatureExpr, T> fun) {
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            try {
                fun.accept(e.getValue(), e.getKey());
            } catch (Throwable t) {
                interceptThrowable(t, e.getValue());
            }
        }
    }


    @Override
    public FeatureExpr when(@Nonnull Predicate<T> condition, boolean filterNull) {
        assert condition != null;
        FeatureExpr result = FeatureExprFactory.False();
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            if (filterNull && e.getKey() == null) continue;
            if (VCache.isSatisfiable(e.getValue()) && condition.test(e.getKey()))
                result = result.or(e.getValue());
        }
        return result;
    }

    @Override
    public V<T> select(@Nonnull FeatureExpr configSpace) {
        assert configSpace != null;
        assert configSpace.and(VERuntime.postponedExceptionContext().not()).implies(getConfigSpace()).isTautology() :
                "selecting under broader condition (" + configSpace + ") than the configuration space described by One (" + getConfigSpace() + ")";

        return reduce(configSpace);
    }

    @Override
    public V<T> reduce(@Nonnull FeatureExpr reducedConfigSpace) {
        assert reducedConfigSpace != null;

        Map<T, FeatureExpr> result = new HashMap<>(values.size());
        Iterator<Map.Entry<T, FeatureExpr>> it = values.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<T, FeatureExpr> entry = it.next();
            FeatureExpr newCondition = VCache.and(entry.getValue(), reducedConfigSpace);
            if (VCache.isSatisfiable(newCondition)) {
                if (result.containsKey(entry.getKey())) {
                    // duplicate values, merge conditions
                    FeatureExpr existingCond = result.get(entry.getKey());
                    FeatureExpr combinedCond = existingCond.or(newCondition);
                    result.put(entry.getKey(), combinedCond);
                } else {
                    result.put(entry.getKey(), newCondition);
                }
            }
        }
        return createV(result);
    }

    @Override
    public FeatureExpr getConfigSpace() {
        FeatureExpr result = FeatureExprFactory.False();
        for (FeatureExpr f : values.values())
            result = result.or(f);
        return result;
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        List<String> entries = new ArrayList<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet())
            entries.add(e.getKey() + "<-" + e.getValue().toTextExpr());
        Collections.sort(entries);
        out.append("CHOICE(");
        for (String e : entries) {
            out.append(e);
            out.append("; ");
        }
        out.delete(out.length() - 2, out.length());
        out.append(")");


        return out.toString();
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VImpl) {
            return ((VImpl) obj).values.equals(values);
        }
        return super.equals(obj);
    }

    @Override
    public boolean equalValue(Object o) {
        return equals(o);
    }

    @Override
    public boolean hasThrowable() {
        for (Object o : values.keySet()) {
            if (o instanceof Throwable)
                return true;
        }
        return false;
    }

    /**
     * Remove values that have False context
     * @return
     */
    @Override
    public V<T> simplified() {
        HashMap<T, FeatureExpr> simplified = new HashMap<>();
        for (HashMap.Entry<T, FeatureExpr> entry : values.entrySet()) {
            if (VCache.isSatisfiable(entry.getValue())) {
                simplified.put(entry.getKey(), entry.getValue());
            }
        }
        if (simplified.size() == 1) {
            HashMap.Entry<T, FeatureExpr> entry = simplified.entrySet().iterator().next();
            return V.one(entry.getValue(), entry.getKey());
        } else {
            return new VImpl<T>(simplified);
        }
    }

    public <U> V<T> merge(Function<T, U> conversion) {
        HashMap<U, T> cache = new HashMap<>();
        HashMap<T, FeatureExpr> merged = new HashMap<>();
        for (Map.Entry<T, FeatureExpr> entry : values.entrySet()) {
            U equivalence = conversion.apply(entry.getKey());
            if (cache.containsKey(equivalence)) {
                T cached = cache.get(equivalence);
                FeatureExpr existing = merged.get(cached);
                merged.put(cached, existing.or(entry.getValue()));
            }
            else {
                cache.put(equivalence, entry.getKey());
                merged.put(entry.getKey(), entry.getValue());
            }
        }
//        System.err.println("shrinking value map from " + values.size() + " to " + merged.size());
        if (merged.size() < 2) {
            HashMap.Entry<T, FeatureExpr> entry = merged.entrySet().iterator().next();
            return V.one(entry.getValue(), entry.getKey());
        } else {
            return new VImpl<>(merged);
        }
    }

    /**
     * Used in cases where we only need one of the values, such as
     * {@link VOps#extractThrowableAndThrow(V, FeatureExpr)}}.
     */
    public One<T> getOneValue() {
        T one = values.keySet().iterator().next();
        return new One<>(values.get(one), one);
    }

}


