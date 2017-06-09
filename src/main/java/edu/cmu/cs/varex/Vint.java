package edu.cmu.cs.varex;

import java.util.function.*;

import de.fosd.typechef.featureexpr.FeatureExpr;

import javax.annotation.Nonnull;

public interface Vint {

    @Deprecated
    int getOne();

    V<Integer> toV();

    /**
     * map and flatMap support (int -> int) and (int -> Vint) operations
     * only.
     * To convert the contained type, do
     * vint.toV().map(... Integer -> U ...)
     * vint.toV().flatMap(... Integer -> V<U> ...)
     */
    Vint map(IntUnaryOperator f);
    Vint map(ToIntBiFunction<FeatureExpr, Integer> f);

    default Vint smap(@Nonnull FeatureExpr ctx, @Nonnull IntUnaryOperator f){
        assert ctx != null;
        assert f != null;
        return this.select(ctx).map(f);
    }
    default Vint smap(@Nonnull IntUnaryOperator f, @Nonnull FeatureExpr ctx) {
        return smap(ctx, f);
    }

    default Vint smap(@Nonnull FeatureExpr ctx, @Nonnull ToIntBiFunction<FeatureExpr, Integer> f){
        assert ctx != null;
        assert f != null;
        return this.select(ctx).map(f);
    }
    default Vint smap(@Nonnull ToIntBiFunction<FeatureExpr, Integer> f, @Nonnull FeatureExpr ctx) {
        return smap(ctx, f);
    }


    default Vint pmap(@Nonnull FeatureExpr ctx, @Nonnull IntUnaryOperator f, @Nonnull IntUnaryOperator altF) {
        assert ctx != null;
        assert f != null;
        assert altF != null;
        return Vint.choice(ctx, this.select(ctx).map(f), this.select(ctx.not()).map(altF));
    }

    default Vint pmap(@Nonnull FeatureExpr ctx, @Nonnull ToIntBiFunction<FeatureExpr, Integer> f, @Nonnull ToIntBiFunction<FeatureExpr, Integer> altF) {
        assert ctx != null;
        assert f != null;
        assert altF != null;
        return Vint.choice(ctx, this.select(ctx).map(f), this.select(ctx.not()).map(altF));
    }

    default Vint pmap(@Nonnull FeatureExpr ctx, @Nonnull ToIntBiFunction<FeatureExpr, Integer> f, @Nonnull IntUnaryOperator altF) {
        assert ctx != null;
        assert f != null;
        assert altF != null;
        return Vint.choice(ctx, this.select(ctx).map(f), this.select(ctx.not()).map(altF));
    }


    Vint flatMap(IntFunction<Vint> f);
    Vint flatMap(BiFunction<FeatureExpr, Integer, Vint> f);

    default Vint sflatMap(@Nonnull FeatureExpr ctx, @Nonnull IntFunction<Vint> f) {
        assert ctx != null;
        assert f != null;
        return this.select(ctx).flatMap(f);
    }
    default Vint sflatMap(@Nonnull IntFunction<Vint> f, @Nonnull FeatureExpr ctx) {
        return sflatMap(ctx, f);
    }

    default Vint sflatMap(@Nonnull FeatureExpr ctx, @Nonnull BiFunction<FeatureExpr, Integer, Vint> f) {
        assert ctx != null;
        assert f != null;
        return this.select(ctx).flatMap(f);
    }
    default Vint sflatMap(@Nonnull BiFunction<FeatureExpr, Integer, Vint> f, @Nonnull FeatureExpr ctx) {
        return sflatMap(ctx, f);
    }


    default Vint pflatMap(@Nonnull FeatureExpr ctx, @Nonnull IntFunction<Vint> f, @Nonnull IntFunction<Vint> altF) {
        assert ctx != null;
        assert f != null;
        assert altF != null;
        return Vint.choice(ctx, this.select(ctx).flatMap(f), this.select(ctx.not()).flatMap(altF));
    }

    default Vint pflatMap(@Nonnull FeatureExpr ctx, @Nonnull BiFunction<FeatureExpr, Integer, Vint> f, @Nonnull BiFunction<FeatureExpr, Integer, Vint> altF) {
        assert ctx != null;
        assert f != null;
        assert altF != null;
        return Vint.choice(ctx, this.select(ctx).flatMap(f), this.select(ctx.not()).flatMap(altF));
    }

    void foreach(IntConsumer f);
    void foreach(ObjIntConsumer<FeatureExpr> f);

    default void sforeach(@Nonnull FeatureExpr ctx, @Nonnull IntConsumer f) {
        assert ctx != null;
        assert f != null;
        this.select(ctx).foreach(f);
    }
    default void sforeach(@Nonnull IntConsumer f, @Nonnull FeatureExpr ctx) {
        sforeach(ctx, f);
    }

    default void sforeach(@Nonnull FeatureExpr ctx, @Nonnull ObjIntConsumer<FeatureExpr> f) {
        assert ctx != null;
        assert f != null;
        this.select(ctx).foreach(f);
    }
    default void sforeach(@Nonnull ObjIntConsumer<FeatureExpr> f, @Nonnull FeatureExpr ctx) {
        sforeach(ctx, f);
    }

    FeatureExpr when(IntPredicate condition);

    Vint select(FeatureExpr configSpace);

    Vint reduce(FeatureExpr reducedConfigSpace);

    FeatureExpr getConfigSpace();

    @Deprecated
    static Vint one(int v) {
        return one(VHelper.True(), v);
    }

    static Vint one(FeatureExpr configSpace, int v) {
        return new Oneint(configSpace, v);
    }

    static Vint choice(@Nonnull FeatureExpr condition, int a, int b) {
        assert condition != null;
        if (condition.isContradiction()) {
            return one(b);
        } else if (condition.isTautology()) {
            return one(a);
        } else {
            return VintImpl.choice(condition, a, b);
        }
    }

    static Vint choice(@Nonnull FeatureExpr condition, IntSupplier a, IntSupplier b) {
        assert condition != null;
        if (condition.isContradiction()) {
            return one(b.getAsInt());
        } else if (condition.isTautology()) {
            return one(a.getAsInt());
        } else {
            return VintImpl.choice(condition, a.getAsInt(), b.getAsInt());
        }
    }
    static Vint choice(@Nonnull FeatureExpr condition, @Nonnull Vint a, @Nonnull Vint b) {
        assert a != null;
        assert b != null;
        assert condition != null;
        if (condition.isContradiction()) {
            return b;
        } else if (condition.isTautology()) {
            return a;
        } else {
            return VintImpl.choice(condition, a , b);
        }
    }


    boolean equalValue(Object obj);
}
