package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Helper class for array handling while lifting bytecode.
 *
 * perf: expanding arrays and compressing them are really slow
 *
 * @author chupanw
 */
public class ArrayOps {

    private static HashMap<FeatureExpr, HashMap<V[], V>> cached = new HashMap<>();

    //////////////////////////////////////////////////
    // long
    //////////////////////////////////////////////////

    public static V<Long>[] initJArray(Integer length, FeatureExpr ctx) {
        V<?>[] array = new V<?>[length];
        ArrayList<V<Long>> arrayList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            arrayList.add(i, V.one(ctx, 0L));
        }
        return arrayList.toArray((V<Long>[])array);
    }

    public static V<Long>[] initJArray(int length, FeatureExpr ctx) {
        return initJArray(Integer.valueOf(length), ctx);
    }

    public static V<?> expandJArray(V<Long>[] array, FeatureExpr ctx) {
        return expandJArrayElements(array, ctx, 0, new ArrayList<>());
    }

    private static V<?> expandJArrayElements(V<Long>[] array, FeatureExpr ctx, Integer index, ArrayList<Long> soFar) {
        model.java.vutil.ArrayList list = new model.java.vutil.ArrayList(ctx);
        for (int i = 0; i < array.length; i++) {
            list.add__Ljava_lang_Object__Z(array[i], ctx);
        }
        V<Long[]> vList = (V<Long[]>) list.getVOfArrays(Long[].class, ctx);
        return vList.map(l -> {
            long[] ll = new long[l.length];
            for (int i = 0; i < ll.length; i++) {
                ll[i] = l[i];
            }
            return ll;
        });
    }

    public static V<?>[] compressJArray(V<long[]> arrays) {
        V<?> sizes = arrays.map(t -> t.length);
        Integer size = (Integer) sizes.getOne(); // if results in a choice, exceptions will be thrown.
        ArrayList<V<?>> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(compressJArrayElement(arrays, i));
        }
        V<?>[] result = new V<?>[size];
        array.toArray(result);
        return result;
    }

    private static V<?> compressJArrayElement(V<long[]> arrays, Integer index) {
        return arrays.map(ts -> ts[index]);
    }

    //////////////////////////////////////////////////
    // double
    //////////////////////////////////////////////////

    public static V<Double>[] initDArray(Integer length, FeatureExpr ctx) {
        V<?>[] array = new V<?>[length];
        ArrayList<V<Double>> arrayList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            arrayList.add(i, V.one(ctx, 0.0));
        }
        return arrayList.toArray((V<Double>[])array);
    }

    public static V<Double>[] initDArray(int length, FeatureExpr ctx) {
        return initDArray(Integer.valueOf(length), ctx);
    }

    //////////////////////////////////////////////////
    // short
    //////////////////////////////////////////////////

    public static V<Integer>[] initSArray(Integer length, FeatureExpr ctx) {
        return initIArray(length, ctx);
    }

    public static V<Integer>[] initSArray(int length, FeatureExpr ctx) {
        return initIArray(Integer.valueOf(length), ctx);
    }

    public static V<?> expandSArray(V<Integer>[] array, FeatureExpr ctx) {
        return expandSArrayElements(array, ctx, 0, new ArrayList<>());
    }

    private static V<?> expandSArrayElements(V<Integer>[] array, FeatureExpr ctx, Integer index, ArrayList<Short> soFar) {
        return array[index].sflatMap(ctx, new BiFunction<FeatureExpr, Integer, V<?>>() {
            @Override
            public V<?> apply(FeatureExpr featureExpr, Integer t) {
                ArrayList<Short> newArray = new ArrayList<Short>(soFar);
                newArray.add((short)t.intValue());
                if (index == array.length - 1) {
                    short[] result = new short[array.length];
                    for (int i = 0; i < array.length; i++) {
                        result[i] = newArray.get(i);
                    }
                    return V.one(featureExpr, result);
                } else {
                    return expandSArrayElements(array, ctx, index + 1, newArray);
                }
            }
        });
    }

    public static V<?>[] compressSArray(V<short[]> arrays) {
        V<?> sizes = arrays.map(new Function<short[], Integer>() {
            @Override
            public Integer apply(short[] t) {
                return t.length;
            }
        });
        Integer size = (Integer) sizes.getOne(); // if results in a choice, exceptions will be thrown.
        ArrayList<V<?>> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(compressSArrayElement(arrays, i));
        }
        V<?>[] result = new V<?>[size];
        array.toArray(result);
        return result;
    }

    private static V<?> compressSArrayElement(V<short[]> arrays, Integer index) {
        return arrays.map(new Function<short[], Integer>() {
            @Override
            public Integer apply(short[] ts) {
                return (int)ts[index];
            }
        });
    }

    //////////////////////////////////////////////////
    // byte
    //////////////////////////////////////////////////

    public static V<Integer>[] initBArray(Integer length, FeatureExpr ctx) {
        return initIArray(length, ctx);
    }

    public static V<Integer>[] initBArray(int length, FeatureExpr ctx) {
        return initIArray(Integer.valueOf(length), ctx);
    }

    public static V<?> expandBArray(V<Integer>[] array, FeatureExpr ctx) {
        return expandBArrayElements(array, ctx, 0, new ArrayList<>());
    }

    private static V<?> expandBArrayElements(V<Integer>[] array, FeatureExpr ctx, Integer index, ArrayList<Byte> soFar) {
        model.java.vutil.ArrayList list = new model.java.vutil.ArrayList(ctx);
        for (int i = 0; i < array.length; i++) {
            list.add__Ljava_lang_Object__Z(array[i], ctx);
        }
        V<Integer[]> vList = (V<Integer[]>) list.getVOfArrays(Integer[].class, ctx);
        return vList.map(l -> {
            byte[] ll = new byte[l.length];
            for (int i = 0; i < ll.length; i++) {
                ll[i] = l[i].byteValue();
            }
            return ll;
        });
    }

    public static V<?>[] compressBArray(V<byte[]> arrays) {
        V<?> sizes = arrays.map(new Function<byte[], Integer>() {
            @Override
            public Integer apply(byte[] t) {
                return t.length;
            }
        });
        Integer size = (Integer) sizes.getOne(); // if results in a choice, exceptions will be thrown.
        ArrayList<V<?>> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(compressBArrayElement(arrays, i));
        }
        V<?>[] result = new V<?>[size];
        array.toArray(result);
        return result;
    }

    private static V<?> compressBArrayElement(V<byte[]> arrays, Integer index) {
        return arrays.map(new Function<byte[], Integer>() {
            @Override
            public Integer apply(byte[] ts) {
                return (int)ts[index];
            }
        });
    }

    //////////////////////////////////////////////////
    // int
    //////////////////////////////////////////////////

    public static V<Integer>[] initIArray(Integer length, FeatureExpr ctx) {
        V<?>[] array = new V<?>[length];
        ArrayList<V<Integer>> arrayList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            arrayList.add(i, V.one(ctx, 0));
        }
        return arrayList.toArray((V<Integer>[])array);
    }

    public static V<Integer>[] initIArray(int length, FeatureExpr ctx) {
        return initIArray(Integer.valueOf(length), ctx);
    }

    public static V<?> expandIArray(V<Integer>[] array, FeatureExpr ctx) {
        return expandIArrayElements(array, ctx, 0, new ArrayList<>());
    }

    private static V<?> expandIArrayElements(V<Integer>[] array, FeatureExpr ctx, Integer index, ArrayList<Integer> soFar) {
        return array[index].sflatMap(ctx, new BiFunction<FeatureExpr, Integer, V<?>>() {
            @Override
            public V<?> apply(FeatureExpr featureExpr, Integer t) {
                ArrayList<Integer> newArray = new ArrayList<Integer>(soFar);
                newArray.add(t);
                if (index == array.length - 1) {
                    int[] result = new int[array.length];
                    for (int i = 0; i < array.length; i++) {
                        result[i] = newArray.get(i);
                    }
                    return V.one(featureExpr, result);
                } else {
                    return expandIArrayElements(array, ctx, index + 1, newArray);
                }
            }
        });
    }

    public static V<?>[] compressIArray(V<int[]> arrays) {
        V<?> sizes = arrays.map(new Function<int[], Integer>() {
            @Override
            public Integer apply(int[] t) {
                return t.length;
            }
        });
        Integer size = (Integer) sizes.getOne(); // if results in a choice, exceptions will be thrown.
        ArrayList<V<?>> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(compressIArrayElement(arrays, i));
        }
        V<?>[] result = new V<?>[size];
        array.toArray(result);
        return result;
    }

    private static V<?> compressIArrayElement(V<int[]> arrays, Integer index) {
        return arrays.map(new Function<int[], Integer>() {
            @Override
            public Integer apply(int[] ts) {
                return ts[index];
            }
        });
    }
    //////////////////////////////////////////////////
    // char
    //////////////////////////////////////////////////

    public static V<Integer>[] initCArray(Integer length, FeatureExpr ctx) {
        return initIArray(length, ctx);
    }

    public static V<Integer>[] initCArray(int length, FeatureExpr ctx) {
        return initIArray(Integer.valueOf(length), ctx);
    }

    public static V<?> expandCArray(V<Integer>[] array, FeatureExpr ctx) {
        return expandCArrayElements(array, ctx, 0, new ArrayList<>());
    }

    private static V<?> expandCArrayElements(V<Integer>[] array, FeatureExpr ctx, Integer index, ArrayList<Character> soFar) {
        return array[index].sflatMap(ctx, new BiFunction<FeatureExpr, Integer, V<?>>() {
            @Override
            public V<?> apply(FeatureExpr featureExpr, Integer t) {
                ArrayList<Character> newArray = new ArrayList<Character>(soFar);
                newArray.add((char)t.intValue());
                if (index == array.length - 1) {
                    char[] result = new char[array.length];
                    for (int i = 0; i < array.length; i++) {
                        result[i] = newArray.get(i);
                    }
                    return V.one(featureExpr, result);
                } else {
                    return expandCArrayElements(array, ctx, index + 1, newArray);
                }
            }
        });
    }

    /**
     * Transform V<Character[]> to V<Character>[]
     */
    public static V<?>[] compressCArray(V<char[]> arrays) {
        V<?> sizes = arrays.map(new Function<char[], Integer>() {
            @Override
            public Integer apply(char[] t) {
                return t.length;
            }
        });
        Integer size = (Integer) sizes.getOne(); // if results in a choice, exceptions will be thrown.
        ArrayList<V<?>> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(compressCArrayElement(arrays, i));
        }
        V<?>[] result = new V<?>[size];
        array.toArray(result);
        return result;
    }

    /**
     * Helper function for {@link #compressCArray(V)}
     */
    private static V<?> compressCArrayElement(V<char[]> arrays, Integer index) {
        return arrays.map(new Function<char[], Integer>() {
            @Override
            public Integer apply(char[] ts) {
                return (int)ts[index];
            }
        });
    }

    //////////////////////////////////////////////////
    // Object
    //////////////////////////////////////////////////

    public static V<?>[] initArray(Integer length, FeatureExpr ctx) {
        V<?>[] array = new V<?>[length];
        ArrayList<V> arrayList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            arrayList.add(i, V.one(ctx, null));
        }
        return arrayList.toArray(array);
    }

    public static V<?>[] initArray(int length, FeatureExpr ctx) {
        return initArray(Integer.valueOf(length), ctx);
    }

    /**
     * Transform V<T>[] to V<T[]>
     *
     * We need the cache because the array being expaneded might be used multiple times
     * in one function call, e.g., System.arrayCopy
     */
    public static <T> V<T[]> expandArray(V<T>[] array, Class c, FeatureExpr ctx) {
        if (cached.containsKey(ctx)) {
            HashMap<V[], V> subMap = cached.get(ctx);
            if (subMap.containsKey(array))
                return subMap.get(array);
        }
        V result;
        if (array.length == 0) {
            result = V.one(ctx, Array.newInstance(c, 0));
        }
        else {
            result = expandArrayElements(array, c, ctx);
        }
        if (!cached.containsKey(ctx)) {
            HashMap<V[], V> subMap = new HashMap<>();
            subMap.put(array, result);
            cached.put(ctx, subMap);
        } else {
            cached.get(ctx).put(array, result);
        }
        return result;
    }

    public static void clearCache() {
        cached.clear();
    }

    /**
     * Helper function for {@link #expandArray(V[], Class, FeatureExpr)}
     */
    private static <T> V<?> expandArrayElements(V<T>[] array, Class c, FeatureExpr ctx) {
        model.java.vutil.ArrayList list = new model.java.vutil.ArrayList(ctx);
        for (int i = 0; i < array.length; i++) {
            list.add__Ljava_lang_Object__Z(array[i], ctx);
        }
        return list.getVOfArrays(c, ctx);
    }

    /**
     * Transform V<T[]> to V<T>[]
     */
    public static <T> V<?>[] compressArray(V<T[]> arrays) {
        V<?> sizes = arrays.map(new Function<T[], Integer>() {
            @Override
            public Integer apply(T[] t) {
                return t.length;
            }
        });
        Integer size = (Integer) sizes.getOne(); // if results in a choice, exceptions will be thrown.
        ArrayList<V<?>> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(compressArrayElement(arrays, i));
        }
        V<?>[] results = new V<?>[size];
        array.toArray(results);
        return results;
    }

    /**
     * Helper function for {@link #compressArray(V)}
     */
    private static <T> V<?> compressArrayElement(V<T[]> arrays, Integer index) {
        return arrays.map(new Function<T[], Object>() {
            @Override
            public Object apply(T[] ts) {
                return ts[index];
            }
        });
    }


    public static void copyVArray(V<?>[] from, V<?>[] to) {
        assert from.length == to.length;
        for (int i = 0; i < from.length; i++) {
            to[i] = V.choice(from[i].getConfigSpace(), from[i], to[i]);
        }
    }

    //////////////////////////////////////////////////
    // Others
    //////////////////////////////////////////////////

    public static void aastore(V[] arrayref, int index, V newValue, FeatureExpr ctx) {
        V oldValue = arrayref[index];
        V choice = V.choice(ctx, newValue, oldValue);
        arrayref[index] = choice;
    }

    public static void aastore(V[] arrayref, V<Integer> index, V newValue, FeatureExpr ctx) {
        index.sforeach(ctx, (fe, i) -> {
            V oldValue = arrayref[i];
            V choice = V.choice(ctx, newValue, oldValue);
            arrayref[i] = choice;
        });
    }

    //////////////////////////////////////////////////
    // Transform a primitive array to an array of Vs
    //////////////////////////////////////////////////

    public static V<?>[] BArray2VArray(byte[] bytes, FeatureExpr ctx) {
        V<?>[] vs = new V<?>[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            vs[i] = V.one(ctx, (int) bytes[i]);
        }
        return vs;
    }

    public static V<?>[] ObjectArray2VArray(Object[] objects, FeatureExpr ctx) {
        V<?>[] vs = new V<?>[objects.length];
        for (int i = 0; i < objects.length; i++) {
            vs[i] = V.one(ctx, objects[i]);
        }
        return vs;
    }

    public static V<?>[] CArray2VArray(char[] chars, FeatureExpr ctx) {
        V<?>[] vs = new V<?>[chars.length];
        for (int i = 0; i < chars.length; i++) {
            vs[i] = V.one(ctx, (int) chars[i]);
        }
        return vs;
    }
}
