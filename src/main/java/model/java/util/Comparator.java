package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import model.Contexts;
import model.java.lang.Integer;

/**
 * We create model class for Comparator because it has INVOKEDYNAMIC
 *
 * @author chupanw
 */
public interface Comparator {
    default int compare(Object a, Object b) {
        FeatureExpr ctx = Contexts.model_java_util_Comparator_compare;
        V<?> compareResult = compare__Ljava_lang_Object_Ljava_lang_Object__I(V.one(ctx, a), V.one(ctx, b), ctx);
        V<?> selected = compareResult.select(ctx);
        assert selected.getOne() instanceof Integer : "compare returns non-Integer";
        return ((java.lang.Integer) selected.getOne());
    }
    V<?> compare__Ljava_lang_Object_Ljava_lang_Object__I(V a, V b, FeatureExpr fe);
}
