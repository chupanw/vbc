package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.*;

/**
 * central abstraction for conditional/variational values
 *
 * @param <T>
 */
@Nonnull
public interface V<T> {

    @Deprecated
    T getOne();

    @Deprecated
    default T getOne(@Nonnull FeatureExpr ctx) {
        assert ctx != null;
        return select(ctx).getOne();
    }


    /**
     * maps over a V describing a (possibly partial) configuration space.
     * all entries have satisfiable conditions.
     * result is another V describing the same (partial) configuration space
     * <p>
     * overloaded to access the condition of each entry if desired
     * <p>
     * fun may return null.
     */
    <U> V<? extends U> map(@Nonnull Function<? super T, ? extends U> fun);

    <U> V<? extends U> map(@Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun);

    /**
     * select map: shorthand for x.select(ctx).map(fun)
     * <p>
     * restricts the configuration space by ctx before applying map. removes all
     * entries that are not valid within ctx. result is at most defined in
     * configuration space ctx.
     * <p>
     * fun may return null.
     */
    default <U> V<? extends U> smap(@Nonnull FeatureExpr ctx, @Nonnull Function<? super T, ? extends U> fun) {
        assert ctx != null;
        assert fun != null;
        return this.select(ctx).map(fun);
    }

    default <U> V<? extends U> smap(@Nonnull FeatureExpr ctx, @Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun) {
        assert ctx != null;
        assert fun != null;
        return this.select(ctx).map(fun);
    }

    default <U> V<? extends U> smap(@Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun, @Nonnull FeatureExpr ctx) {
        return smap(ctx, fun);
    }

    default <U> V<? extends U> smap(@Nonnull Function<? super T, ? extends U> fun, @Nonnull FeatureExpr ctx) {
        return smap(ctx, fun);
    }

    /**
     * partially map: apply function fun to all values inside a restricted configuration space and apply function altFun
     * to all values outside the restricted configuration space. Overloaded for the common case where
     * altFun is the identify function.
     */
    default <U> V<? extends U> pmap(@Nonnull FeatureExpr ctx, @Nonnull Function<? super T, ? extends U> fun, @Nonnull Function<? super T, ? extends U> altFun) {
        assert ctx != null;
        assert fun != null;
        assert altFun != null;
        return V.choice(ctx, this.select(ctx).map(fun), this.select(ctx.not()).map(altFun));
    }

    default <U> V<? extends U> pmap(@Nonnull FeatureExpr ctx, @Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun, @Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> altFun) {
        assert ctx != null;
        assert fun != null;
        assert altFun != null;
        return V.choice(ctx, this.select(ctx).map(fun), this.select(ctx.not()).map(altFun));
    }

    default <U> V<? extends U> pmap(@Nonnull FeatureExpr ctx, @Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun, @Nonnull Function<? super T, ? extends U> altFun) {
        assert ctx != null;
        assert fun != null;
        assert altFun != null;
        return V.choice(ctx, this.select(ctx).map(fun), this.select(ctx.not()).map(altFun));
    }

    /**
     * @param fun may not return null, but One(null)
     */
    <U> V<? extends U> flatMap(@Nonnull Function<? super T, V<? extends U>> fun);

    <U> V<? extends U> flatMap(@Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun);

    /**
     * see smap
     */
    default <U> V<? extends U> sflatMap(@Nonnull FeatureExpr ctx, @Nonnull Function<? super T, V<? extends U>> fun) {
        assert ctx != null;
        assert fun != null;
        return this.select(ctx).flatMap(fun);
    }

    default <U> V<? extends U> sflatMap(@Nonnull FeatureExpr ctx, @Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun) {
        assert ctx != null;
        assert fun != null;
        return this.select(ctx).flatMap(fun);
    }

    /**
     * alternative parameter order to simplify lifting
     */
    default <U> V<? extends U> sflatMap(@Nonnull Function<? super T, V<? extends U>> fun, @Nonnull FeatureExpr ctx) {
        return sflatMap(ctx, fun);
    }

    default <U> V<? extends U> sflatMap(@Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun, @Nonnull FeatureExpr ctx) {
        return sflatMap(ctx, fun);
    }

    /**
     * see pmap
     */
    default <U> V<? extends U> pflatMap(@Nonnull FeatureExpr ctx, @Nonnull Function<? super T, V<? extends U>> fun, @Nonnull Function<? super T, V<? extends U>> altFun) {
        assert ctx != null;
        assert fun != null;
        assert altFun != null;
        return V.choice(ctx, this.select(ctx).flatMap(fun), this.select(ctx.not()).flatMap(altFun));
    }

    default <U> V<? extends U> pflatMap(@Nonnull FeatureExpr ctx, @Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun, @Nonnull Function<? super T, ? extends U> altFun) {
        assert ctx != null;
        assert fun != null;
        assert altFun != null;
        return V.choice(ctx, this.select(ctx).flatMap(fun), this.select(ctx.not()).map(altFun));
    }

    default <U> V<? extends U> pflatMap(@Nonnull FeatureExpr ctx, @Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun, @Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> altFun) {
        assert ctx != null;
        assert fun != null;
        assert altFun != null;
        return V.choice(ctx, this.select(ctx).flatMap(fun), this.select(ctx.not()).flatMap(altFun));
    }

    void foreach(@Nonnull Consumer<T> fun);

    void foreach(@Nonnull BiConsumer<FeatureExpr, T> fun);

    default void sforeach(@Nonnull FeatureExpr ctx, @Nonnull Consumer<T> fun) {
        assert ctx != null;
        assert fun != null;
        this.select(ctx).foreach(fun);
    }

    default void sforeach(@Nonnull FeatureExpr ctx, @Nonnull BiConsumer<FeatureExpr, T> fun) {
        assert ctx != null;
        assert fun != null;
        this.select(ctx).foreach(fun);
    }

    /**
     * alternative parameter order to simplify lifting
     */
    default void sforeach(@Nonnull Consumer<T> fun, @Nonnull FeatureExpr ctx) {
        sforeach(ctx, fun);
    }

    default void sforeach(@Nonnull BiConsumer<FeatureExpr, T> fun, @Nonnull FeatureExpr ctx) {
        sforeach(ctx, fun);
    }

    FeatureExpr when(@Nonnull Predicate<T> condition, boolean filterNull);

    /**
     * select a subconfiguration space of V
     *
     * @param configSpace must be the same or smaller than the configuration
     *                    space provided by this V
     */
    V<T> select(@Nonnull FeatureExpr configSpace);

    /**
     * reduces the configuration space of V. Resulting V covers at most the
     * provided configuration space. If it was original configuration space
     * was smaller, the smaller space remains
     */
    V<T> reduce(@Nonnull FeatureExpr reducedConfigSpace);

    FeatureExpr getConfigSpace();

    @Deprecated
    static <U> V<U> one(@Nullable U v) {
        return one(VHelper.True(), v);
    }

    static <U> V<U> one(FeatureExpr configSpace, @Nullable U v) {
        if (v instanceof V) {
            return (V<U>) v;
        } else {
            return new One(configSpace, v);
        }
    }

    static <U> V<? extends U> choice(@Nonnull FeatureExpr condition, @Nullable U a, @Nullable U b) {
        assert condition != null;
        if (condition.isContradiction())
            return one(b);
        else if (condition.isTautology())
            return one(a);
        else
            return VImpl.choice(condition, a, b);
    }

    static <U> V<? extends U> choice(@Nonnull FeatureExpr condition, Supplier<U> a, Supplier<U> b) {
        assert condition != null;
        if (condition.isContradiction())
            return one(b.get());
        else if (condition.isTautology())
            return one(a.get());
        else
            return VImpl.choice(condition, a.get(), b.get());
    }

    static <U> V<? extends U> choice(@Nonnull FeatureExpr condition, @Nonnull V<? extends U> a, @Nonnull V<? extends U> b) {
        assert a != null;
        //TODO should not accept null values here. requires clean initialization of variational variables with One(null) instead of null
        if (b == null)
            b = V.one(null);
        assert condition != null;
        if (condition.isContradiction())
            return b;
        else if (condition.isTautology())
            return a;
        else
            return VImpl.choice(condition, a, b);
    }

    /**
     * Compares equality of the wrapped value.
     *
     * @param o V to be compared.
     * @return If the wrapped values are equal.
     */
    boolean equalValue(Object o);

    boolean hasThrowable();

    V<T> simplified();
}
