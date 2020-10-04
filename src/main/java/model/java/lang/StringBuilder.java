package model.java.lang;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.ArrayOps;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.varex.VImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * StringBuilder might be used in two scenarios.
 *
 * If it is used in lifted bytecode, great.
 *
 * If it is used in unlifted bytecode, we need to be careful because Strings from conflicting sub-contexts
 * might get appended into one StringBuilder and cause unexpected behaviors. This scenario could happen if
 * StringBuilder is used in classes that we don't lift.
 *
 * @author chupanw
 */
public class StringBuilder implements Appendable {

    V<? extends java.lang.StringBuilder> vActual;
    java.lang.StringBuilder actual;

    public StringBuilder(V<? extends String> vS, FeatureExpr ctx, String dummy) {
        vActual = vS.smap(ctx, s -> new java.lang.StringBuilder(s));
    }

    public StringBuilder(FeatureExpr ctx) {
        vActual = V.one(ctx, new java.lang.StringBuilder());
    }

    public StringBuilder(V<? extends java.lang.Integer> vCapacity, FeatureExpr ctx, int dummy) {
        vActual = vCapacity.smap(ctx, c -> new java.lang.StringBuilder(c.intValue()));
    }

    // perf: very slow for extensive string manipulation such as introclass digits
    public V<?> append__Ljava_lang_String__Lmodel_java_lang_StringBuilder(V<? extends String> vS, FeatureExpr ctx) {
        vS.sforeach(ctx, (fe, s) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, actual) -> actual.append(s));
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> append__Z__Lmodel_java_lang_StringBuilder(V<?> vI, FeatureExpr ctx) {
        vI.sforeach(ctx, (fe, i) -> {
            split(fe);
            if (i instanceof java.lang.Integer) {
                vActual.sforeach(fe, (fe2, actual) -> actual.append((java.lang.Integer) i != 0));
            }
            else if (i instanceof java.lang.Boolean) {
                vActual.sforeach(fe, (fe2, actual) -> actual.append((java.lang.Boolean) i));
            }
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> append__I__Lmodel_java_lang_StringBuilder(V<? extends java.lang.Integer> vI, FeatureExpr ctx) {
        vI.sforeach(ctx, (fe, i) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, actual) -> actual.append(i.intValue()));
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> append__Ljava_lang_Object__Lmodel_java_lang_StringBuilder(V<?> vO, FeatureExpr ctx) {
        vO.sforeach(ctx, (fe, o) -> {
            try {
                Method m = o.getClass().getMethod("toString____Ljava_lang_String", FeatureExpr.class);
                V<?> strings = (V<?>) m.invoke(o, fe);
                strings.sforeach(fe, (fe2, s) -> {
                    split(fe2);
                    vActual.sforeach(fe2, (fe3, sb) -> sb.append(s));
                });
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//                java.lang.System.err.println("Falling back to unlifted toString()");
                split(fe);
                vActual.sforeach(fe, (fe2, sb) -> sb.append(o));
            }
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> append__C__Lmodel_java_lang_StringBuilder(V<? extends java.lang.Integer> vI, FeatureExpr ctx) {
        vI.sforeach(ctx, (fe, i) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, actual) -> actual.append((char)i.intValue()));
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> append__D__Lmodel_java_lang_StringBuilder(V<? extends java.lang.Double> vD, FeatureExpr ctx) {
        vD.sforeach(ctx, (fe, d) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, actual) -> actual.append(d.doubleValue()));
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> append__J__Lmodel_java_lang_StringBuilder(V<? extends java.lang.Long> vJ, FeatureExpr ctx) {
        vJ.sforeach(ctx, (fe, j) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, actual) -> actual.append(j.longValue()));
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> append__F__Lmodel_java_lang_StringBuilder(V<? extends java.lang.Float> vF, FeatureExpr ctx) {
        vF.sforeach(ctx, (fe, f) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, actual) -> actual.append(f.floatValue()));
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> insert__I_Ljava_lang_String__Lmodel_java_lang_StringBuilder(V<? extends java.lang.Integer> vI, V<? extends String> vS, FeatureExpr ctx) {
        vI.sforeach(ctx, (fe, i) -> {
            vS.sforeach(fe, (fe2, s) -> {
                split(fe2);
                vActual.sforeach(fe2, (fe3, sb) -> {
                    sb.insert(i, s);
                });
            });
        });
        merge();
        return V.one(ctx, this);
    }

    public V<?> setLength__I__V(V<? extends java.lang.Integer> vNewLength, FeatureExpr ctx) {
        vNewLength.sforeach(ctx, (fe, newLength) -> {
            split(fe);
            vActual.sforeach(fe, sb -> sb.setLength(newLength));
        });
        merge();
        return null;    // dummy value
    }

    public V<?> toString____Ljava_lang_String(FeatureExpr ctx) {
        return vActual.smap(ctx, sb -> sb.toString());
    }

    public V<?> append__Array_C_I_I__Lmodel_java_lang_StringBuilder(V<V<java.lang.Integer>[]> vCArray,
                                                                    V<java.lang.Integer> vOffset,
                                                                    V<java.lang.Integer> vLen,
                                                                    FeatureExpr ctx) {
        vOffset.sforeach(ctx, (fe1, offset) -> vLen.sforeach(fe1, (fe2, len) -> vCArray.sforeach(fe2, (fe3, cArray) -> {
            V array = ArrayOps.expandCArray(cArray, fe3);
            array.sforeach(fe3, (fe4, a) -> {
                split((FeatureExpr) fe4);
                vActual.sforeach((FeatureExpr) fe4, sb -> {
                    sb.append((char[]) a, offset, len);
                });
            });
        })));
        merge();
        return V.one(ctx, this);
    }

    public V<?> append__Array_C__Lmodel_java_lang_StringBuilder(V<V<java.lang.Integer>[]> vCArray, FeatureExpr ctx) {
        vCArray.sforeach(ctx, (fe, cArray) -> {
            V array = ArrayOps.expandCArray(cArray, fe);
            array.sforeach(fe, (fe2, a) -> {
                split((FeatureExpr) fe2);
                vActual.sforeach((FeatureExpr) fe2, sb -> {
                    sb.append((char[]) a);
                });
            });
        });
        merge();
        return V.one(ctx, this);
    }

    public V<? extends java.lang.Integer> length____I(FeatureExpr ctx) {
        return vActual.smap(ctx, sb -> sb.length());
    }

    public V<? extends java.lang.Integer> charAt__I__C(V<? extends java.lang.Integer> vI, FeatureExpr ctx) {
        return vI.sflatMap(ctx, (fe, i) -> vActual.smap(fe, sb -> (int) sb.charAt(i)));
    }

    public V<? extends String> substring__I__Ljava_lang_String(V<? extends java.lang.Integer> vI, FeatureExpr ctx) {
        return vI.sflatMap(ctx, (fe, i) -> vActual.smap(fe, sb -> sb.substring(i)));
    }

    /**
     * Split vActual LinkedLists according to current ctx
     */
    private void split(FeatureExpr ctx) {
        V<? extends java.lang.StringBuilder> selected = vActual.smap(ctx, sb -> new java.lang.StringBuilder(sb));
        vActual = V.choice(ctx, selected, vActual);
    }

    private void merge() {
        if (vActual instanceof VImpl) {
            vActual = ((VImpl) vActual).merge(x -> x.toString());
        }
    }

    //////////////////////////////////////////////////
    // non-V part
    //////////////////////////////////////////////////
    public StringBuilder() {
        actual = new java.lang.StringBuilder();
    }

    public StringBuilder(int size) {
        actual = new java.lang.StringBuilder(size);
    }

    public StringBuilder(String s) {
        actual = new java.lang.StringBuilder(s);
    }

    public StringBuilder append(String s) {
        actual.append(s);
        return this;    // not creating new instances, following JDK style
    }

    public StringBuilder append(int i) {
        actual.append(i);
        return this;    // not creating new instances, following JDK style
    }

    public StringBuilder append(Object o) {
        actual.append(o);
        return this;
    }

    public StringBuilder append(char c) {
        actual.append(c);
        return this;
    }

    public char charAt(int index) {
        return actual.charAt(index);
    }

    public String substring(int start) {
        return actual.substring(start);
    }

    public void setLength(int newLength) {
        actual.setLength(newLength);
    }

    public String toString() {
        return actual.toString();
    }

    @Override
    public V<?> append__C__Lmodel_java_lang_Appendable(V<? extends java.lang.Integer> vC, FeatureExpr ctx) {
        return append__C__Lmodel_java_lang_StringBuilder(vC, ctx);
    }

    @Override
    public V<?> append__Ljava_lang_CharSequence__Lmodel_java_lang_Appendable(V<? extends CharSequence> vCharSequence, FeatureExpr ctx) {
        vCharSequence.sforeach(ctx, (fe, cs) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, actual) -> actual.append(cs));
        });
        merge();
        return V.one(ctx, this);
    }
}
