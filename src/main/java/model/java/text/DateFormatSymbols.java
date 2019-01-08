package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

import java.util.Locale;

public class DateFormatSymbols {
    V<? extends MyDateFormatSymbols> vActual;

    public void split(FeatureExpr ctx) {
        V<? extends MyDateFormatSymbols> selected = vActual.smap(ctx, t -> (MyDateFormatSymbols) t.clone());
        vActual = V.choice(ctx, selected, vActual);
    }

    public V<? extends MyDateFormatSymbols> raw() {
        return vActual;
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public DateFormatSymbols(V<? extends java.util.Locale> vL, FeatureExpr ctx, java.util.Locale dummy) {
        vActual = vL.smap(ctx, l -> new MyDateFormatSymbols(l));
    }
}


class MyDateFormatSymbols extends java.text.DateFormatSymbols {
    public MyDateFormatSymbols(Locale locale) {
        super(locale);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
