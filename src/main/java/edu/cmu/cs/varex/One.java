package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.function.*;

/**
 * Created by ckaestne on 11/27/2015.
 */
public class One<T> implements V<T>, Serializable {
    final FeatureExpr configSpace;
    final T value;

    public One(FeatureExpr configSpace, T v) {
        this.configSpace = configSpace;
        this.value = v;
    }

    @Override
    public String toString() {
        String condition = "";
        if (!configSpace.isTautology())
            condition = configSpace.toString() + ":";
        return "One(" + condition + value + ")";
    }

    @Override
    public T getOne() {
        return value;
    }

    @Override
    public <U> V<? extends U> map(@Nonnull Function<? super T, ? extends U> fun) {
        assert fun != null;
        return new One(configSpace, fun.apply(value));
    }

    @Override
    public <U> V<? extends U> map(@Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun) {
        assert fun != null;
        return new One(configSpace, fun.apply(configSpace, value));
    }

    @Override
    public <U> V<? extends U> flatMap(@Nonnull Function<? super T, V<? extends U>> fun) {
        assert fun != null;
        V<? extends U> result = fun.apply(value);
        assert result != null;
        return result.reduce(configSpace);
    }

    @Override
    public <U> V<? extends U> flatMap(@Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun) {
        assert fun != null;
        V<? extends U> result = fun.apply(configSpace, value);
        assert result != null;
        return result.reduce(configSpace);
    }

    @Override
    public void foreach(@Nonnull Consumer<T> fun) {
        assert fun != null;
        fun.accept(value);
    }

    @Override
    public void foreach(@Nonnull BiConsumer<FeatureExpr, T> fun) {
        assert fun != null;
        fun.accept(configSpace, value);
    }

    @Override
    public FeatureExpr when(@Nonnull Predicate<T> condition, boolean filterNull) {
        assert condition != null;
        if (filterNull && value == null) return FeatureExprFactory.False();
        return condition.test(value) ? FeatureExprFactory.True() : FeatureExprFactory.False();
    }

    @Override
    public V<T> select(@Nonnull FeatureExpr selectConfigSpace) {
        assert selectConfigSpace != null;
        assert selectConfigSpace.implies(configSpace).isTautology() :
                "selecting under broader condition (" + selectConfigSpace + ") than the configuration space described by One (" + configSpace + ")";

        return reduce(selectConfigSpace);
    }

    @Override
    public V<T> reduce(@Nonnull FeatureExpr reducedConfigSpace) {
        assert reducedConfigSpace != null;
        FeatureExpr newCondition = configSpace.and(reducedConfigSpace);
        if (newCondition.isSatisfiable())
            return new One(newCondition, value);
        else return VEmpty.instance();
    }

    @Override
    public FeatureExpr getConfigSpace() {
        return configSpace;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof One) {
            if (((One) obj).value == null) return value == null;
            return ((One) obj).value.equals(value) && ((One) obj).configSpace.equivalentTo(configSpace);
        }
        return super.equals(obj);
    }

    @Override
    public boolean equalValue(Object o) {
        if (o instanceof One) {
            if (((One) o).value == null) return value == null;
            return ((One) o).value.equals(value);
        }
        return super.equals(o);
    }

    @Override
    public boolean hasThrowable() {
        if (value instanceof Throwable)
            return true;
        else
            return false;
    }

    /**
     * Return Empty if the value has False context
     */
    @Override
    public V<T> simplified() {
        if (configSpace.isSatisfiable()) {
            return this;
        } else {
            return new VEmpty();
        }
    }
}
