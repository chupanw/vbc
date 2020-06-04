package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public class IteratorImpl implements Iterator {

    private V<? extends java.util.Iterator> vActual;
    private java.util.Iterator actual;

    IteratorImpl(V<? extends java.util.Iterator> vI) {
        vActual = vI;
    }

    //////////////////////////////////////////////////
    // V methods
    //////////////////////////////////////////////////
    @Override
    public V<?> hasNext____Z(FeatureExpr ctx) {
        return vActual.smap(ctx, (fe, i) -> i.hasNext());
    }

    @Override
    public V<?> next____Ljava_lang_Object(FeatureExpr ctx) {
        return vActual.smap(ctx, (fe, i) -> i.next());
    }

}
