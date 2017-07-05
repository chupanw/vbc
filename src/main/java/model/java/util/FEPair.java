package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;

/**
 * Created by lukas on 6/30/17.
 */
public class FEPair<B> {
    public FeatureExpr ctx;
    public B v;

    public FEPair(FeatureExpr a, B b) {
        ctx = a;
        v = b;
    }

    public String toString() {
        return "(" + v.toString() + ": " + ctx.toString() + ")";
    }
}
