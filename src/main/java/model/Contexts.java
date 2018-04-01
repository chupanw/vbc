package model;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

/**
 * Store context when going into unlifted code.
 *
 * When going from variational world to non-variational world, we cannot carry context anymore.
 * This should be fine for most cases, but there are some corner cases where unlifted code needs
 * to call into lifted code and thus we need to provide the contexts.
 *
 * @author chupanw
 */
public class Contexts {
    public static FeatureExpr model_java_util_Comparator_compare = FeatureExprFactory.True();
}
