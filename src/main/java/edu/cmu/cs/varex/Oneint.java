package edu.cmu.cs.varex;

import java.util.function.*;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 6/5/17.
 */
public class Oneint implements Vint {
    final int value;
    final FeatureExpr configSpace;
    private final int SOME_PRIME = 31;
    
    public Oneint(FeatureExpr ctx, int val){
        configSpace = ctx;
        value = val;
    }

    @Override
    public String toString() {
        String condition = "";
        if (!configSpace.isTautology()) {
            condition = configSpace.toString() + ":";
        }
        return "One(" + condition + value + ")";
    }

    @Override
    public int getOne() {
        return value;
    }

    // int -> int
    @Override
    public Vint map(@Nonnull IntUnaryOperator f) {
        assert f != null;
        return new Oneint(configSpace, f.applyAsInt(value));
    }

    // FeatureExpr int -> int
    @Override
    public Vint map(@Nonnull ToIntBiFunction<FeatureExpr, Integer> f) {
        assert f != null;
        return new Oneint(configSpace, f.applyAsInt(configSpace, value));
    }

    // int -> Vint
    @Override
    public Vint flatMap(@Nonnull IntFunction<Vint> f) {
        assert f != null;
        Vint result = f.apply(value);
        assert result != null;
        return result.reduce(configSpace);
    }
    // FeatureExpr int -> Vint
    @Override
    public Vint flatMap(@Nonnull BiFunction<FeatureExpr, Integer, Vint> f) {
        assert f != null;
        Vint result = f.apply(configSpace, value);
        assert result != null;
        return result.reduce(configSpace);
    }
    // int -> V
    // FeatureExpr int -> V
    // To perform these maps, do oneint.toV().flatMap(...)
    @Override
    public V<Integer> toV() {
        return V.one(configSpace, value);
    }

    @Override
    public void foreach(@Nonnull IntConsumer f) {
        assert f != null;
        f.accept(value);
    }

    @Override
    public void foreach(@Nonnull ObjIntConsumer<FeatureExpr> f) {
        assert f != null;
        f.accept(configSpace, value);
    }

    @Override
    public FeatureExpr when(@Nonnull IntPredicate condition) {
        assert condition != null;
        return condition.test(value) ? FeatureExprFactory.True()
            : FeatureExprFactory.False();
    }

    @Override
    public Vint select(@Nonnull FeatureExpr selectConfigSpace) {
        assert selectConfigSpace != null;
        assert selectConfigSpace.implies(configSpace).isTautology() :
                "selecting under broader condition (" + selectConfigSpace + ") than the configuration space described by One (" + configSpace + ")";

        return reduce(selectConfigSpace);
    }

    @Override
    public Vint reduce(@Nonnull FeatureExpr reducedConfigSpace) {
        assert reducedConfigSpace != null;
        FeatureExpr newCondition = configSpace.and(reducedConfigSpace);
        if (newCondition.isSatisfiable()) {
            return new Oneint(newCondition, value);
        } else   {
            return VintEmpty.instance();
        }
    }

    @Override
    public FeatureExpr getConfigSpace() {
        return configSpace;
    }

    @Override
    public int hashCode() {
        return value*SOME_PRIME + configSpace.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Oneint) {
            Oneint objAsOneint = (Oneint)obj;
            return objAsOneint.value == value && objAsOneint.configSpace.equivalentTo(configSpace);
        }
        return super.equals(obj);
    }

    @Override
    public boolean equalValue(Object obj) {
        if (obj instanceof Oneint) {
            return ((Oneint) obj).value == value;
        }
        return super.equals(obj);
    }
}
