package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public class DecimalFormat extends NumberFormat {
    public DecimalFormat(java.text.NumberFormat nf, FeatureExpr ctx) {
        super(nf, ctx);
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public DecimalFormat(V<? extends String> vS, FeatureExpr ctx, String dummy) {
        super(ctx);
        vActual = vS.smap(ctx, s -> new java.text.DecimalFormat(s));
    }

    public DecimalFormat(V<? extends String> vS, V<? extends DecimalFormatSymbols> vDFS, FeatureExpr ctx, String dummy1, DecimalFormatSymbols dummy2) {
        super(ctx);
        vActual = vS.sflatMap(ctx, (fe, s) -> {
            return vDFS.sflatMap(fe, (fe2, mdfs) -> {
                mdfs.split(fe2);
                return (V) mdfs.raw().smap(fe2, (fe3, rdfs) -> {
                    return new java.text.DecimalFormat(s, rdfs);
                });
            });
        });
    }

    public V<?> getMultiplier____I(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, df -> ((java.text.DecimalFormat) df).getMultiplier());
    }

    public V<?> toPattern____Ljava_lang_String(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, df -> ((java.text.DecimalFormat) df).toPattern());
    }

    public V<?> applyPattern__Ljava_lang_String__V(V<? extends String> vS, FeatureExpr ctx) {
        vS.sforeach(ctx, (fe, s) -> {
            split(fe);
            vActual.sforeach(fe, df -> ((java.text.DecimalFormat)df).applyPattern(s));
        });
        return null;    // void
    }
}
