package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

import javax.annotation.Nonnull;
import java.util.function.*;


/**
 * represents a V for an empty configuration space without any values
 */
public class VintEmpty implements Vint {

    public static VintEmpty instance() {
        return new VintEmpty();
    }

    @Override
    public String toString() {
        return "VintEmpty()";
    }

    @Override
    public int getOne() {
        assert false : "getOne() on empty Vint";
        return 0;
    }

    @Override
    public V<Integer> toV(){
        return new VEmpty<>();
    }

    @Override
    public Vint map(IntUnaryOperator f) {
        return instance();
    }

    @Override
    public Vint map(@Nonnull ToIntBiFunction<FeatureExpr, Integer> f) {
        return instance();
    }

    @Override
    public Vint flatMap(IntFunction<Vint> f) {
        return instance();
    }

    @Override
    public Vint flatMap(BiFunction<FeatureExpr, Integer, Vint> f) {
        return instance();
    }

    @Override
    public void foreach(IntConsumer f) {
    }

    @Override
    public void foreach(ObjIntConsumer<FeatureExpr> f) {
    }

    @Override
    public FeatureExpr when(IntPredicate condition) {
        return FeatureExprFactory.False();
    }

    @Override
    public Vint select(FeatureExpr selectConfigSpace) {
        return this;
    }

    @Override
    public Vint reduce(@Nonnull FeatureExpr reducedConfigSpace) {
        return this;
    }

    @Override
    public FeatureExpr getConfigSpace() {
        return FeatureExprFactory.False();
    }

    @Override
    public boolean equalValue(Object o) {
        return equals(o);
    }

    @Override
    public int hashCode() {
        return 17;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof VintEmpty) || super.equals(obj);
    }
}

