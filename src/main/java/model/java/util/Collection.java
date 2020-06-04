package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

/**
 * @author chupanw
 */
public interface Collection extends java.util.Collection {
    V<?> getVCopies(FeatureExpr ctx);

    V<?> iterator____Ljava_util_Iterator(FeatureExpr ctx);
    V<?> iterator____Lmodel_java_util_Iterator(FeatureExpr ctx);
    V<?> size____I(FeatureExpr ctx);
}
