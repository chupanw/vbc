package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

/**
 * @author chupanw
 */
public interface List extends Iterable {
    int size();
    V<?> size____I(FeatureExpr ctx);

    Object get(int index);
    V<?> get__I__Ljava_lang_Object(V<? extends Integer> index, FeatureExpr ctx);

    void sort(Comparator comparator);
    V<?> sort__Lmodel_java_util_Comparator__V(V<Comparator> vComparator, FeatureExpr ctx);
}
