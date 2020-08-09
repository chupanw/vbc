package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public interface ListIterator extends Iterator {
    V<?> hasPrevious____Z(FeatureExpr ctx);
    V<?> previous____Ljava_lang_Object(FeatureExpr ctx);
}
