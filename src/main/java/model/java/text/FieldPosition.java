package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public class FieldPosition {

    private V<? extends java.text.FieldPosition> vActual;

    private void split(FeatureExpr ctx) {
        V<? extends java.text.FieldPosition> selected = vActual.smap(ctx, f -> {
            java.text.FieldPosition n = new java.text.FieldPosition(f.getFieldAttribute(), f.getField());
            n.setEndIndex(f.getEndIndex());
            n.setBeginIndex(f.getBeginIndex());
            return n;
        });
        vActual = V.choice(ctx, selected, vActual);
    }

    public V<? extends java.text.FieldPosition> raw() {
        return vActual;
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public FieldPosition(V<Integer> vI, FeatureExpr ctx, int dummy) {
        vActual = vI.smap(ctx, (fe, i) -> new java.text.FieldPosition(i));
    }

    public V<?> setBeginIndex__I__V(V<Integer> vI, FeatureExpr ctx) {
        split(ctx);
        vI.sforeach(ctx, (fe, i) -> vActual.sforeach(fe, f -> f.setBeginIndex(i)));
        return null;    // void
    }


    public V<?> setEndIndex__I__V(V<Integer> vI, FeatureExpr ctx) {
        split(ctx);
        vI.sforeach(ctx, (fe, i) -> vActual.sforeach(fe, f -> f.setEndIndex(i)));
        return null;    // void
    }
}
