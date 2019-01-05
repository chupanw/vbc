package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public class ParsePosition {

    private V<? extends MyParsePosition> vActual;

    public void split(FeatureExpr ctx) {
        V<? extends MyParsePosition> selected = vActual.smap(ctx, pp -> {
            MyParsePosition newPP = new MyParsePosition(pp.getIndex());
            newPP.setErrorIndex(pp.getErrorIndex());
            return newPP;
        });
        vActual = V.choice(ctx, selected, vActual);
    }

    public V<? extends MyParsePosition> raw() {
        return vActual;
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public ParsePosition(V<? extends Integer> vI, FeatureExpr ctx, int dummy) {
        vActual = vI.smap(ctx, i -> new MyParsePosition(i));
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

/**
 * Replace equals with reference comparison
 */
class MyParsePosition extends java.text.ParsePosition {
    public MyParsePosition(int index) {
        super(index);
    }
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
