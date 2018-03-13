package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.ArrayOps;
import edu.cmu.cs.varex.V;
import model.Contexts;

/**
 * Performance reasons.
 *
 * Also because we are lifting Comparator
 *
 * @author chupanw
 */
public class Arrays {
    public static Object[] copyOf(Object[] original, int newLength) {
        return java.util.Arrays.copyOf(original, newLength);
    }
    public static V<?> copyOf__Array_Ljava_lang_Object_I__Array_Ljava_lang_Object(V<V[]> original, V<Integer> newLength, FeatureExpr ctx) {
        return original.sflatMap(ctx, (fe, va) -> newLength.smap(fe, integer -> {
            V[] copied = java.util.Arrays.copyOf(va, integer);
            if (integer > va.length)
                java.util.Arrays.fill(copied, va.length, integer, V.one(fe, null)); return copied;
        }));
    }

    public static char[] copyOf(char[] original, int newLength) {
        return java.util.Arrays.copyOf(original, newLength);
    }
    public static V<?> copyOf__Array_C_I__Array_C(V<V[]> original, V<Integer> newLength, FeatureExpr ctx) {
        return original.sflatMap(ctx, (fe, va) -> newLength.smap(fe, integer -> {
                V[] copied = java.util.Arrays.copyOf(va, integer);
                if (integer > va.length)
                    java.util.Arrays.fill(copied, va.length, integer, V.one(fe, 0));
                return copied;
        }));
    }

    public static void fill(int[] a, int val) {
        java.util.Arrays.fill(a, val);
    }

    public static void fill(char[] a, char val) {
        java.util.Arrays.fill(a, val);
    }

    public static V<?> fill__Array_I_I__V(V<V[]> varray, V<Integer> vvalue, FeatureExpr ctx) {
        vvalue.sforeach(ctx, value -> varray.sforeach(ctx, array -> {
            for (int i = 0; i < array.length; i++) {
                array[i] = V.choice(ctx, V.one(ctx, value), array[i]);
            }
        }));
        return null;    // dummy return value
    }

    public static <T> void sort(T[] array, model.java.util.Comparator comparator) {
        java.util.Arrays.sort(array, comparator::compare);
    }
    public static <T> V<?> sort__Array_Ljava_lang_Object_Lmodel_java_util_Comparator__V(V<V<T>[]> vArray, V<Comparator> vComparator, FeatureExpr ctx) {
        vArray.sforeach(ctx, (FeatureExpr fe, V<T>[] array) -> {
            vComparator.sforeach(fe, (FeatureExpr fe2, Comparator comparator) -> {
                V<T[]> expanded = ArrayOps.expandArray(array, Object[].class, fe2);
                Contexts.model_java_util_Comparator_compare = fe2;
                expanded.sforeach(fe2, (T[] expandedArray) -> java.util.Arrays.sort(expandedArray, comparator::compare));
                V[] compressed = ArrayOps.compressArray(expanded);
                ArrayOps.copyVArray(compressed, array);
            });
        });
        return null;    // dummy return value
    }

    public static long[] copyOf(long[] original, int newLength) {
        return java.util.Arrays.copyOf(original, newLength);
    }
    public static byte[] copyOf(byte[] original, int newLength) {
        return java.util.Arrays.copyOf(original, newLength);
    }

    public static void sort(int[] a) {
        java.util.Arrays.sort(a);
    }

    public static V<?> sort__Array_I__V(V<V<Integer>[]> vArray, FeatureExpr ctx) {
        vArray.sforeach(ctx, (FeatureExpr fe, V<Integer>[] array) -> {
            V<Integer[]> expanded = ArrayOps.expandArray(array, Integer[].class, fe);
            expanded.sforeach(fe, expandedArray -> java.util.Arrays.sort(expandedArray));
            V[] compressed = ArrayOps.compressArray(expanded);
            ArrayOps.copyVArray(compressed, array);
        });
        return null;    // dummy return value
    }

    public static int binarySearch(int[] array, int key) {
        return java.util.Arrays.binarySearch(array, key);
    }

    public static int binarySearch(double[] a, double key) {
        return java.util.Arrays.binarySearch(a, key);
    }

    public static V<?> binarySearch__Array_I_I__I(V<V<Integer>[]> vIntegerArray, V<Integer> key, FeatureExpr ctx) {
        return key.sflatMap(ctx, (fe, k) -> vIntegerArray.sflatMap(fe, (FeatureExpr fe2, V<Integer>[] vArray) -> {
            V<Integer[]> expanded = ArrayOps.expandArray(vArray, Integer[].class, fe2);
            return (V) expanded.smap(fe2, array -> java.util.Arrays.binarySearch(array, k));
        }));
    }

    public static V<?> binarySearch__Array_D_D__I(V<V<Double>[]> vDoubleArray, V<Double> key, FeatureExpr ctx) {
        return key.sflatMap(ctx, (fe, k) -> vDoubleArray.sflatMap(fe, (FeatureExpr fe2, V<Double>[] vArray) -> {
            V<Double[]> expanded = ArrayOps.expandArray(vArray, Double[].class, fe2);
            return (V) expanded.smap(fe2, array -> java.util.Arrays.binarySearch(array, k));
        }));
    }

    public static V<?> equals__Array_I_Array_I__Z(V<V<Integer>[]> vIntArray1, V<V<Integer>[]> vIntArray2, FeatureExpr ctx) {
        return vIntArray1.sflatMap(ctx, (fe, a1) -> {
            return vIntArray2.smap(fe, (fe2, a2) -> compareVArrays(a1, a2, fe2));
        });
    }

    private static Boolean compareVArrays(V<Integer>[] a1, V<Integer>[] a2, FeatureExpr ctx) {
        if (a1 == a2)
            return true;
        if (a1 == null || a2 == null)
            return false;
        if (a1.length != a2.length)
            return false;
        for (int i = 0; i < a1.length; i++) {
            if (!a1[i].select(ctx).equals(a2[i].select(ctx)))
                return false;
        }
        return true;
    }

    public static List asList(Object[] array) {
        ArrayList list = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    public static V<? extends List> asList__Array_Ljava_lang_Object__Lmodel_java_util_List(V<Object[]> vObjectArrays, FeatureExpr ctx) {
        return vObjectArrays.smap(ctx, Arrays::asList);
    }

    public static int[] copyOfRange(int[] original, int from, int to) {
        return java.util.Arrays.copyOfRange(original, from, to);
    }

    public static void fill(Object[] a, Object value) {
        java.util.Arrays.fill(a, value);
    }
}
