package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public interface Iterator {
    V<?> hasNext____Z(FeatureExpr ctx);

    V<?> next____Ljava_lang_Object(FeatureExpr ctx);

    V<?> remove____V(FeatureExpr ctx);
}
