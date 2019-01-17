package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public class DecimalFormat extends NumberFormat {
    public DecimalFormat(DecimalFormatWrapper nf, FeatureExpr ctx) {
        super(V.one(ctx, nf));
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public DecimalFormat(V<? extends String> vS, FeatureExpr ctx, String dummy) {
        super();
        vActual = vS.smap(ctx, s -> new DecimalFormatWrapper(new java.text.DecimalFormat((String) s)));
    }

    public DecimalFormat(V<? extends String> vS, V<? extends DecimalFormatSymbols> vDFS, FeatureExpr ctx, String dummy1, DecimalFormatSymbols dummy2) {
        super();
        vActual = vS.sflatMap(ctx, (fe, s) -> {
            return vDFS.sflatMap(fe, (fe2, mdfs) -> {
                mdfs.split(fe2);
                return (V) mdfs.raw().smap(fe2, (fe3, rdfs) -> {
                    return new DecimalFormatWrapper(new java.text.DecimalFormat(s, rdfs));
                });
            });
        });
    }

    public V<?> getMultiplier____I(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, df -> ((DecimalFormatWrapper) df).actual.getMultiplier());
    }

    public V<?> toPattern____Ljava_lang_String(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, df -> ((DecimalFormatWrapper) df).actual.toPattern());
    }

    public V<?> applyPattern__Ljava_lang_String__V(V<? extends String> vS, FeatureExpr ctx) {
        vS.sforeach(ctx, (fe, s) -> {
            split(fe);
            vActual.sforeach(fe, df -> ((DecimalFormatWrapper)df).actual.applyPattern(s));
        });
        return null;    // void
    }
}

/**
 * It's okay to extend Format because it's does not have any modifiable fields
 */
class DecimalFormatWrapper {
    java.text.DecimalFormat actual;
    private DecimalFormatWrapper(){}
    DecimalFormatWrapper(java.text.DecimalFormat d) {
        actual = d;
    }
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public Object clone() {
        return new DecimalFormatWrapper((java.text.DecimalFormat)actual.clone());
    }

    // Wrapper methods
    public String format(Object o) {
        return actual.format(o);
    }
}
