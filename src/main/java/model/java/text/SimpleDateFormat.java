package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

public class SimpleDateFormat extends DateFormat {

    public SimpleDateFormat(SimpleDateFormatWrapper sdf, FeatureExpr ctx) {
        super(ctx);
        vActual = V.one(ctx, sdf);
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////
    public SimpleDateFormat(V<? extends String> vS, FeatureExpr ctx, String dummy) {
        super(ctx); // useless
        vActual = vS.smap(ctx, (fe, s) -> new SimpleDateFormatWrapper(new java.text.SimpleDateFormat(s)));
    }

    public SimpleDateFormat(V<? extends String> vS, V<? extends DateFormatSymbols> vDFS, FeatureExpr ctx, String dummy1, DateFormatSymbols dummy2) {
        super(ctx);
        vActual = vS.sflatMap(ctx, (fe, s) -> {
            return vDFS.sflatMap(fe, (fe2, dfs) -> {
                dfs.split(fe2);
                return (V) dfs.raw().smap(fe2, (fe3, rdfs) -> {
                    return new SimpleDateFormatWrapper(new java.text.SimpleDateFormat(s, rdfs));
                });
            });
        });
    }
}

class SimpleDateFormatWrapper {
    java.text.SimpleDateFormat actual;
    private SimpleDateFormatWrapper(){}
    SimpleDateFormatWrapper(java.text.SimpleDateFormat d) {
        actual = d;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public Object clone() {
        return new SimpleDateFormatWrapper((java.text.SimpleDateFormat)actual.clone());
    }
}