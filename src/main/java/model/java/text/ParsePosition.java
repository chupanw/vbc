package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public class ParsePosition {

    private V<? extends java.text.ParsePosition> vActual;

    private void split(FeatureExpr ctx) {
        V<? extends java.text.ParsePosition> selected = vActual.smap(ctx, pp -> {
            java.text.ParsePosition newPP = new java.text.ParsePosition(pp.getIndex());
            newPP.setErrorIndex(pp.getErrorIndex());
            return newPP;
        });
        vActual = V.choice(ctx, selected, vActual);
    }

    public V<? extends java.text.ParsePosition> raw() {
        return vActual;
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public ParsePosition(V<? extends Integer> vI, FeatureExpr ctx, int dummy) {
        vActual = vI.smap(ctx, i -> new java.text.ParsePosition(i));
    }

    public V getIndex____I(FeatureExpr ctx) {
        return vActual.smap(ctx, pp -> pp.getIndex());
    }

    public V setIndex__I__V(V<? extends Integer> vI, FeatureExpr ctx) {
        split(ctx);
        vI.sforeach(ctx, (fe1, i) -> vActual.sforeach(fe1, pp -> pp.setIndex(i)));
        return null;    // void
    }

    public V getErrorIndex____I(FeatureExpr ctx) {
        return vActual.smap(ctx, pp -> pp.getErrorIndex());
    }

    public V setErrorIndex__I__V(V<? extends Integer> vI, FeatureExpr ctx) {
        split(ctx);
        vI.sforeach(ctx, (fe, i) -> vActual.sforeach(fe, pp -> pp.setErrorIndex(i)));
        return null;    // void
    }
}
