package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;

/**
 * internal implementation of V
 */
class VImpl<T> implements V<T>, Serializable {

    static <U> V<? extends U> choice(FeatureExpr condition, U a, U b) {
        Map<U, FeatureExpr> result = new HashMap<>(2);
        if (VCache.isSatisfiable(condition))
            put(result, a, condition);
        else return V.one(condition.not(), b);
        if (VCache.isSatisfiable(condition.not()))
            put(result, b, condition.not());
        else return V.one(condition, a);

        return createV(result);
    }

    static <U> V<? extends U> choice(FeatureExpr condition, V<? extends U> a, V<? extends U> b) {
        Map<U, FeatureExpr> result = new HashMap<>(2);
        if (VCache.isSatisfiable(condition))
            addVToMap(result, condition, a);
        else return b;
        if (VCache.isSatisfiable(condition.not()))
            addVToMap(result, condition.not(), b);
        else return a;

        return createV(result);
    }

    private VImpl(Map<T, FeatureExpr> values) {
        this.values = values;
        assert checkInvariant() : "invariants violated";
    }

    //invariant: nonempty, all FeatureExpr together yield true
    private final Map<T, FeatureExpr> values;

    private boolean checkInvariant() {
        if (values.isEmpty()) return false;// : "empty V";
        if (values.size() < 2) return false;// : "singleton VImpl?";
        FeatureExpr conditions = FeatureExprFactory.False();
        for (FeatureExpr cond : values.values()) {
            if (!(VCache.isContradiction(conditions.and(cond)))) return false;// : "condition overlaps with previous condition";
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


    @Override
    public <U> V<? extends U> map(Function<? super T, ? extends U> fun) {
        Map<U, FeatureExpr> result = new HashMap<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet())
            put(result, fun.apply(e.getKey()), e.getValue());
        return createV(result);
    }

    @Override
    public <U> V<? extends U> map(@Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun) {
        assert fun != null;
        Map<U, FeatureExpr> result = new HashMap<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet())
            put(result, fun.apply(e.getValue(), e.getKey()), e.getValue());
        return createV(result);
    }

    @Override
    public <U> V<? extends U> flatMap(Function<? super T, V<? extends U>> fun) {
        assert fun != null;
        Map<U, FeatureExpr> result = new HashMap<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            V<? extends U> u = fun.apply(e.getKey());
            assert u != null;
            addVToMap(result, e.getValue(), u);
        }
        return createV(result);
    }

    @Override
    public <U> V<? extends U> flatMap(@Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun) {
        assert fun != null;
        Map<U, FeatureExpr> result = new HashMap<>(values.size());
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet()) {
            V<? extends U> u = fun.apply(e.getValue(), e.getKey());
            assert u != null;
            addVToMap(result, e.getValue(), u);
        }
        return createV(result);
    }


    private static <U> void addVToMap(Map<U, FeatureExpr> result, FeatureExpr ctx, @Nonnull V<? extends U> u) {
        assert u != null;
        assert (u instanceof One) || (u instanceof VImpl) : "unexpected V value: " + u;
        if (u instanceof One)
            put(result, ((One<U>) u).value, ctx.and(((One<U>) u).configSpace));
        else
            for (HashMap.Entry<U, FeatureExpr> ee : ((VImpl<U>) u).values.entrySet()) {
                FeatureExpr cond = ctx.and(ee.getValue());
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
        assert fun != null;
        values.keySet().forEach(fun::accept);
    }

    @Override
    public void foreach(@Nonnull BiConsumer<FeatureExpr, T> fun) {
        assert fun != null;
        for (HashMap.Entry<T, FeatureExpr> e : values.entrySet())
            fun.accept(e.getValue(), e.getKey());
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
        assert configSpace.implies(getConfigSpace()).isTautology() :
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
            FeatureExpr newCondition = entry.getValue().and(reducedConfigSpace);
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

}


