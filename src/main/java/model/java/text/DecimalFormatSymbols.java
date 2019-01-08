package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

import java.util.Locale;

public class DecimalFormatSymbols {
    V<? extends MyDecimalFormatSymbols> vActual;

    public void split(FeatureExpr ctx) {
        V<? extends MyDecimalFormatSymbols> selected = vActual.smap(ctx, t -> (MyDecimalFormatSymbols) t.clone());
        vActual = V.choice(ctx, selected, vActual);
    }

    public V<? extends MyDecimalFormatSymbols> raw() {
        return vActual;
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public DecimalFormatSymbols(V<? extends java.util.Locale> vL, FeatureExpr ctx, java.util.Locale dummy) {
        vActual = vL.smap(ctx, (fe, l) -> new MyDecimalFormatSymbols(l));
    }

    public V<?> getCurrencySymbol____Ljava_lang_String(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, dfs -> dfs.getCurrencySymbol());
    }
}

class MyDecimalFormatSymbols extends java.text.DecimalFormatSymbols {
    public MyDecimalFormatSymbols(Locale locale) {
        super(locale);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
