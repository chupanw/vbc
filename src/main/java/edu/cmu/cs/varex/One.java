package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.vbc.VException;
import edu.cmu.cs.vbc.config.VERuntime;

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

    private <U> V<? extends U> interceptThrowable(Throwable t) {
        if (VERuntime.shouldPostpone(configSpace)) {
            VERuntime.postponeException(t, configSpace);
            return VEmpty.instance();
        }
        else {
            VERuntime.throwExceptionCtx(configSpace);
            if (t instanceof VException) {
                throw (VException) t;
            }
            else {
                throw new VException(t, configSpace);
            }
        }
    }

    @Override
    public <U> V<? extends U> map(@Nonnull Function<? super T, ? extends U> fun) {
        try {
            return V.one(configSpace, fun.apply(value));
        } catch (Throwable t) {
            return interceptThrowable(t);
        }
    }

    @Override
    public <U> V<? extends U> map(@Nonnull BiFunction<FeatureExpr, ? super T, ? extends U> fun) {
        try {
            return V.one(configSpace, fun.apply(configSpace, value));
        } catch (Throwable t) {
            return interceptThrowable(t);
        }
    }

    @Override
    public <U> V<? extends U> flatMap(@Nonnull Function<? super T, V<? extends U>> fun) {
        try {
            V<? extends U> result = fun.apply(value);
            assert result != null;
            return result.reduce(configSpace);
        } catch (Throwable t) {
            return interceptThrowable(t);
        }
    }

    @Override
    public <U> V<? extends U> flatMap(@Nonnull BiFunction<FeatureExpr, ? super T, V<? extends U>> fun) {
        try {
            V<? extends U> result = fun.apply(configSpace, value);
            assert result != null;
            return result.reduce(configSpace);
        } catch (Throwable t) {
            return interceptThrowable(t);
        }
    }

    @Override
    public void foreach(@Nonnull Consumer<T> fun) {
        try {
            fun.accept(value);
        } catch (Throwable t) {
            interceptThrowable(t);
        }
    }

    @Override
    public void foreach(@Nonnull BiConsumer<FeatureExpr, T> fun) {
        try {
            fun.accept(configSpace, value);
        } catch (Throwable t) {
            interceptThrowable(t);
        }
    }

    @Override
    public FeatureExpr when(@Nonnull Predicate<T> condition, boolean filterNull) {
        assert condition != null;
        if (filterNull && value == null) return FeatureExprFactory.False();
        return condition.test(value) ? configSpace : FeatureExprFactory.False();
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
        FeatureExpr newCondition = VCache.and(configSpace, reducedConfigSpace);
        if (VCache.isSatisfiable(newCondition))
            return V.one(newCondition, value);
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
        if (VCache.isSatisfiable(configSpace)) {
            return this;
        } else {
            return new VEmpty();
        }
    }

    static One oneNull = new One(FeatureExprFactory.True(), null);
    public static V<?> getOneNull() {
        return oneNull;
    }

    @Override
    public V<T> restrictInteractionDegree() {
        if (V.isDegreeTooHigh(configSpace))
            return VEmpty.instance();
        else
            return this;
    }
}
