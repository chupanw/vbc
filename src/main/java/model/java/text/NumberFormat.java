package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import model.java.lang.StringBuffer;

public class NumberFormat extends Format {

    private V<? extends java.text.NumberFormat> vActual;

    private NumberFormat(V<? extends java.text.NumberFormat> v) {
        vActual = v;
    }

    private void split(FeatureExpr ctx) {
        V<? extends java.text.NumberFormat> selected = vActual.smap(ctx, t -> (java.text.NumberFormat) t.clone());
        vActual = V.choice(ctx, selected, vActual);
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public NumberFormat(FeatureExpr ctx) {
        // empty originally
    }

    public static V<?> getNumberInstance__Ljava_util_Locale__Lmodel_java_text_NumberFormat(
            V<? extends java.util.Locale> vLocale,
            FeatureExpr ctx
    ) {
        NumberFormat ret = new NumberFormat(vLocale.smap(ctx, (fe, l) -> java.text.NumberFormat.getNumberInstance(l)));
        return V.one(ctx, ret);
    }

    public static V<?> getInstance____Lmodel_java_text_NumberFormat(FeatureExpr ctx) {
        return V.one(ctx, new NumberFormat(V.one(ctx, java.text.NumberFormat.getInstance())));
    }

    public V<?> setMaximumFractionDigits__I__V(V<Integer> vNewValue, FeatureExpr ctx) {
        split(ctx);
        vNewValue.sforeach(ctx, (fe, i) -> vActual.sforeach(fe, l -> l.setMaximumFractionDigits(i)));
        return null;    // void
    }

    public V<?> setParseIntegerOnly__Z__V(V<Integer> vValue, FeatureExpr ctx) {
        split(ctx);
        vValue.sforeach(ctx, (fe, v) -> vActual.sforeach(fe, l -> l.setParseIntegerOnly(v != 0)));
        return null;    // void
    }

    public V<?> clone____Ljava_lang_Object(FeatureExpr ctx) {
        V<?> cloned = vActual.smap(ctx, l -> l.clone());
        return V.one(ctx, new NumberFormat((V<? extends java.text.NumberFormat>)cloned));
    }

    public V<? extends String> format__D__Ljava_lang_String(V<Double> vNumber, FeatureExpr ctx) {
        if (vActual != null) {
            return vNumber.sflatMap(ctx, (fe, number) -> vActual.smap(fe, n -> n.format(number.doubleValue())));
        } else {
            // used as an abstract class
            return format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
                    vNumber,
                    V.one(ctx, new StringBuffer(ctx)),
                    V.one(ctx, new DontCareFieldPosition(ctx)),
                    ctx
            ).sflatMap(ctx, (fe, x) -> (V<? extends String>) x.toString____Ljava_lang_String(fe));
        }
    }

    public V<? extends String> format__J__Ljava_lang_String(V<Long> vNumber, FeatureExpr ctx) {
        if (vActual != null) {
            return vNumber.sflatMap(ctx, (fe, number) -> vActual.smap(fe, n -> n.format(number.longValue())));
        } else {
            // used as an abstract class
            return format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
                    vNumber,
                    V.one(ctx, new StringBuffer(ctx)),
                    V.one(ctx, new DontCareFieldPosition(ctx)),
                    ctx
            ).sflatMap(ctx, (fe, x) -> (V<? extends String>) x.toString____Ljava_lang_String(fe));
        }
    }

    public V format__J_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
            V<? extends java.lang.Long> vNumber,
            V<? extends StringBuffer> vToAppendTo,
            V<? extends FieldPosition> vPos,
            FeatureExpr ctx
    ) {
        if (vActual == null) {
            throw new RuntimeException("Should be overridden by subclasses");
        } else {
            V ret = vNumber.sflatMap(ctx, (fe, n) -> {
                return vToAppendTo.sflatMap(fe, (fe1, toAppendTo) -> {
                    return (V) toAppendTo.raw().sflatMap(fe1, (fe2, rawToAppendTo) -> {
                        return (V) vPos.sflatMap(fe2, (fe3, pos) -> {
                            return (V) pos.raw().sflatMap(fe3, (fe4, rawPos) -> {
                                return (V) vActual.smap(fe4, nf -> nf.format(n.longValue(), rawToAppendTo, rawPos));
                            });
                        });
                    });
                });
            });
            return V.one(ctx, new StringBuffer(ret));
        }
    }

    @Override
    public V<? extends StringBuffer> format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(V<?> vObject, V<? extends StringBuffer> vToAppendTo, V<? extends FieldPosition> vPos, FeatureExpr ctx) {
        V<? extends java.lang.StringBuffer> ret =
                (V<? extends java.lang.StringBuffer>) vObject.sflatMap(ctx, (fe1, o) -> {
                    return vToAppendTo.sflatMap(fe1, (fe2, sb) -> {
                        return (V<?>) sb.raw().sflatMap(fe2, (fe3, rsb) -> {
                            return (V<?>) vPos.sflatMap(fe3, (fe4, p) -> {
                                return (V<?>) p.raw().sflatMap(fe4, (fe5, rp) -> {
                                    return (V<?>) vActual.smap(fe5, nf -> {
                                        return nf.format(o, (java.lang.StringBuffer) rsb, (java.text.FieldPosition) rp);
                                    });
                                });
                            });
                        });
                    });
                });
        return V.one(ctx, new StringBuffer(ret));
    }

    public V parse__Ljava_lang_String_Lmodel_java_text_ParsePosition__Ljava_lang_Number(V<? extends String> vS, V<? extends ParsePosition> vP, FeatureExpr ctx) {
        if (vActual == null) {
            throw new RuntimeException("Should get overridden by: " + this.getClass());
        } else {
            return vS.sflatMap(ctx, (fe1, s) -> vP.sflatMap(fe1, (fe2, p) -> (V) p.raw().sflatMap(fe2, (fe3, rawp) -> (V) vActual.smap(fe2, nf -> nf.parse(s, rawp)))));
        }
    }
}
