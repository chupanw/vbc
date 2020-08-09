package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

/**
 * @author chupanw
 */
public interface List extends Collection {
    V<?> add__Ljava_lang_Object__Z(V<?> elem, FeatureExpr ctx);
    V<?> add__I_Ljava_lang_Object__V(V<? extends Integer> vI, V<?> vElem, FeatureExpr ctx);
    V<?> set__I_Ljava_lang_Object__Ljava_lang_Object(V<? extends Integer> vI, V<?> vObject, FeatureExpr ctx);

    V<?> size____I(FeatureExpr ctx);

    V<?> get__I__Ljava_lang_Object(V<? extends Integer> index, FeatureExpr ctx);

    V<?> sort__Lmodel_java_util_Comparator__V(V<Comparator> vComparator, FeatureExpr ctx);

    V<?> isEmpty____Z(FeatureExpr ctx);

    V<?> iterator____Ljava_util_Iterator(FeatureExpr ctx);

    V<?> listIterator____Lmodel_java_util_ListIterator(FeatureExpr ctx);
    V<?> listIterator__I__Lmodel_java_util_ListIterator(V<? extends Integer> vI, FeatureExpr ctx);

    V<?> containsAll__Lmodel_java_util_Collection__Z(V<?> vObjects, FeatureExpr ctx);

    V<?> clear____V(FeatureExpr ctx);

    V<?> contains__Ljava_lang_Object__Z(V<?> vO, FeatureExpr ctx);

    V<?> remove__Ljava_lang_Object__Z(V<?> vO, FeatureExpr ctx);

    V<?> addAll__Ljava_util_Collection__Z(V<? extends java.util.Collection> vCollection, FeatureExpr ctx);

    V<?> indexOf__Ljava_lang_Object__I(V<?> vObject, FeatureExpr ctx);

    V<?> toArray__Array_Ljava_lang_Object__Array_Ljava_lang_Object(V<V[]> vObject, FeatureExpr ctx);

    V<?> addAll__Lmodel_java_util_Collection__Z(V<?> vCollection, FeatureExpr ctx);

    V<?> removeAll__Lmodel_java_util_Collection__Z(V<?> vCollection, FeatureExpr ctx);

    V<?> remove__I__Ljava_lang_Object(V<? extends Integer> vI, FeatureExpr ctx);

    V<?> subList__I_I__Lmodel_java_util_List(V<? extends Integer> vBegin, V<? extends Integer> vEnd, FeatureExpr ctx);

    boolean isEmpty();
    boolean add(Object o);
    java.util.Iterator iterator();
    int size();
    Object get(int index);
    Object remove(int index);
    Object[] toArray(Object[] a);
    Object[] toArray();
    void clear();
    boolean contains(Object o);
    Object set(int index, Object element);
    default void sort(Comparator c) {
        Object[] a = this.toArray();
        Arrays.sort(a, c);
        for (int i = 0; i < this.size(); i++) {
            this.set(i, a[i]);
        }
    }
}
