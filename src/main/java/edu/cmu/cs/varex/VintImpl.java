package edu.cmu.cs.varex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.*;
import java.util.HashMap;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import javax.annotation.Nonnull;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Created by lukas on 6/5/17.
 */
public class VintImpl implements Vint {
    public TIntObjectHashMap<FeatureExpr> values;

    public VintImpl(TIntObjectHashMap<FeatureExpr> map){
        this.values = map;
        assert checkInvariant() : "invariants violated";
    }

    private boolean checkInvariant(){
        if (values.isEmpty()) return false;
        if (values.size() < 2) return false; // singleton VImpl should be impossible
        FeatureExpr conditions = FeatureExprFactory.False();
        for (FeatureExpr cond : values.valueCollection()){
            if (!conditions.and(cond).isContradiction()){
                return false; // condition overlap - not a partitioning
            }
            conditions = conditions.or(cond); // Add condition to checked space
        }
        return true;
    }

    static Vint choice(FeatureExpr condition, int ifTrue, int ifFalse){
        TIntObjectHashMap<FeatureExpr> result = new TIntObjectHashMap<FeatureExpr>(2);
        if (condition.isSatisfiable()){
            put(result, ifTrue, condition);
        } else {
            return new Oneint(condition.not(), ifFalse);
        }
        if (condition.not().isSatisfiable()){
            put(result, ifFalse, condition.not());
        } else {
            return new Oneint(condition, ifTrue);
        }

        return createV(result);
    }

    static Vint choice(FeatureExpr condition, Vint ifTrue, Vint ifFalse){
        TIntObjectHashMap<FeatureExpr> result = new TIntObjectHashMap<FeatureExpr>(2);
        if (condition.isSatisfiable()){
            addVToMap(result, condition, ifTrue);
        } else {
            return ifFalse;
        }
        if (condition.not().isSatisfiable()){
            addVToMap(result, condition.not(), ifFalse);
        } else {
            return ifTrue;
        }

        return createV(result);
    }

    @Override
    public int getOne() {
        assert false : "getOne called on Choice: " + this;
        return values.keySet().iterator().next();
    }

    // int -> int
    @Override
    public Vint map(IntUnaryOperator f){
        assert f != null;
        TIntObjectHashMap<FeatureExpr> result = new TIntObjectHashMap<FeatureExpr>(values.size());
        for (int value : values.keys()){
            FeatureExpr expr = values.get(value);
            put(result, f.applyAsInt(value), expr);
        }
        return createV(result);
    }
    @Override
    public Vint map(ToIntBiFunction<FeatureExpr, Integer> f){
        assert f != null;
        TIntObjectHashMap<FeatureExpr> result = new TIntObjectHashMap<FeatureExpr>(values.size());
        for (int value : values.keys()){
            FeatureExpr expr = values.get(value);
            put(result, f.applyAsInt(expr, value), expr);
        }
        return createV(result);
    }

    // int -> Vint
    @Override
    public Vint flatMap(IntFunction<Vint> f){
        assert f != null;
        TIntObjectHashMap<FeatureExpr> result = new TIntObjectHashMap<FeatureExpr>(values.size());
        for (int value : values.keys()){
            FeatureExpr expr = values.get(value);
            Vint res = f.apply(value);
            assert res != null;
            addVToMap(result, expr, res);
        }
        return createV(result);
    }
    // FeatureExpr int -> Vint
    @Override
    public Vint flatMap(BiFunction<FeatureExpr, Integer, Vint> f) {
        assert f != null;
        TIntObjectHashMap<FeatureExpr> result = new TIntObjectHashMap<FeatureExpr>(values.size());
        for (int value : values.keys()) {
            FeatureExpr expr = values.get(value);
            Vint res = f.apply(expr, value);
            assert res != null;
            addVToMap(result, expr, res);
        }
        return createV(result);
    }
    // int -> V<U>
    // FeatureExpr int -> V<U>
    // To perform these maps, do vint.toV().flatMap(...)
    @Override
    public V<Integer> toV() {
        HashMap<Integer, FeatureExpr> convertedValues = new HashMap<>();
        for (int value : values.keys()) {
            convertedValues.put(value, values.get(value));
        }
        return VImpl.createV(convertedValues);
    }

    private static void addVToMap(TIntObjectHashMap<FeatureExpr> result, FeatureExpr ctx, Vint v){
        assert v != null;
        assert (v instanceof Oneint) || (v instanceof VintImpl) : "unexpected V value: " + v;
        if (v instanceof Oneint){
            Oneint vAsOne = (Oneint)v;
            put(result, vAsOne.value, ctx.and(vAsOne.configSpace));
        } else {
            VintImpl vAsImpl = (VintImpl)v;
            for (int value : vAsImpl.values.keys()){
                FeatureExpr cond = ctx.and(vAsImpl.values.get(value));
                if (cond.isSatisfiable()){
                    put(result, value, cond);
                }
            }
        }
    }

    private static void put(TIntObjectHashMap<FeatureExpr> map, int value, FeatureExpr condition){
        if (map.containsKey(value)) {
            condition = condition.or(map.get(value));
        }
        map.put(value, condition);
    }

    protected static Vint createV(TIntObjectHashMap<FeatureExpr> map) {
        if (map.size() == 0) {
            return VintEmpty.instance();
        }
        if (map.size() == 1) {
            TIntObjectIterator<FeatureExpr> it = map.iterator();
            it.advance();
            return new Oneint(it.value(), it.key());
        }
        return new VintImpl(map);
    }

    @Override
    public void foreach(@Nonnull IntConsumer f) {
        assert f != null;
        for (int value : values.keys()){
            f.accept(value);
        }
    }

    @Override
    public void foreach(@Nonnull ObjIntConsumer<FeatureExpr> f) {
        assert f != null;
        for (int value : values.keys()) {
            f.accept(values.get(value), value);
        }
    }

    @Override
    public FeatureExpr when(@Nonnull IntPredicate condition) {
        assert condition != null;
        FeatureExpr result = FeatureExprFactory.False();
        for (int value : values.keys()) {
            if (condition.test(value)) {
                result = result.or(values.get(value));
            }
        }
        return result;
    }

    @Override
    public Vint select(@Nonnull FeatureExpr configSpace) {
        assert configSpace != null;
        assert configSpace.implies(getConfigSpace()).isTautology() : "selecting under broader condition ("
            + configSpace + ") than the configuration space described by One (" + getConfigSpace() + ")";

        return reduce(configSpace);
    }

    @Override
    public Vint reduce(@Nonnull FeatureExpr reducedConfigSpace) {
        assert reducedConfigSpace != null;

        TIntObjectHashMap<FeatureExpr> result = new TIntObjectHashMap<>(values.size());

        for (int value : values.keys()) {
            FeatureExpr newCondition = values.get(value).and(reducedConfigSpace);
            if (newCondition.isSatisfiable()) {
                result.put(value, newCondition);
            }
        }
        return createV(result); 
    }

    @Override
    public FeatureExpr getConfigSpace() {
        FeatureExpr result = FeatureExprFactory.False();
        for (int value : values.keys()) {
            result = result.or(values.get(value));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();
        List<String> entries = new ArrayList<>(values.size());
        for (int value : values.keys()) {
            entries.add(value + "<-" + values.get(value).toTextExpr());
        }
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
        if (obj instanceof VintImpl) {
            return ((VintImpl) obj).values.equals(values);
        }
        return super.equals(obj);
    }

    @Override
    public boolean equalValue(Object obj) {
        return equals(obj);
    }
}
