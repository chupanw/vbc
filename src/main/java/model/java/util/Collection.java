package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import model.java.lang.Iterable;

/**
 * @author chupanw
 */
public interface Collection extends java.util.Collection, Iterable {
    V<?> getVCopies(FeatureExpr ctx);

    V<?> iterator____Ljava_util_Iterator(FeatureExpr ctx);
    V<?> iterator____Lmodel_java_util_Iterator(FeatureExpr ctx);
    V<?> size____I(FeatureExpr ctx);
    V<?> toArray____Array_Ljava_lang_Object(FeatureExpr ctx);
    V<?> toArray__Array_Ljava_lang_Object__Array_Ljava_lang_Object(V<V[]> vArray, FeatureExpr ctx);
    V<?> add__Ljava_lang_Object__Z(V<?> vObject, FeatureExpr ctx);
    V<?> addAll__Lmodel_java_util_Collection__Z(V<?> vCollection, FeatureExpr ctx);
    V<?> isEmpty____Z(FeatureExpr ctx);
    V<?> clear____V(FeatureExpr ctx);
    V<?> hashCode____I(FeatureExpr ctx);
    V<?> contains__Ljava_lang_Object__Z(V<?> vObject, FeatureExpr ctx);
    V<?> remove__Ljava_lang_Object__Z(V<?> vObject, FeatureExpr ctx);
}
