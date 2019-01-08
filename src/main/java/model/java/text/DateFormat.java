package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.vbc.VException;
import model.java.lang.StringBuffer;

import java.text.ParseException;

public class DateFormat extends Format {

    protected DateFormat(FeatureExpr ctx) {
        super(ctx);
    }

    DateFormat(V<? extends java.text.Format> vA) {
        super(vA);
    }

    DateFormat(java.text.DateFormat df, FeatureExpr ctx) {
        super(V.one(ctx, df));
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public static V<?> getTimeInstance__I_Ljava_util_Locale__Lmodel_java_text_DateFormat(V<? extends java.lang.Integer> vI, V<? extends java.util.Locale> vL, FeatureExpr ctx) {
        return vI.sflatMap(ctx, (fe, i) -> {
            return vL.smap(fe, (fe2, l) -> {
                java.text.DateFormat rdf = java.text.DateFormat.getTimeInstance(i, l);
                if (rdf instanceof java.text.SimpleDateFormat) {
                    return new SimpleDateFormat(rdf, fe2);
                } else {
                    throw new RuntimeException("Unsupported subclass of DateFormat");
                }
            });
        });
    }

    public static V<?> getTimeInstance__I__Lmodel_java_text_DateFormat(V<? extends java.lang.Integer> vI, FeatureExpr ctx) {
        return vI.smap(ctx, (fe, i) -> {
            java.text.DateFormat rdf = java.text.DateFormat.getTimeInstance(i);
            if (rdf instanceof java.text.SimpleDateFormat) {
                return new SimpleDateFormat(rdf, fe);
            } else {
                throw new RuntimeException("Unsupported subclass of DateFormat");
            }
        });
    }

    public static V<?> getDateInstance__I__Lmodel_java_text_DateFormat(V<? extends java.lang.Integer> vI, FeatureExpr ctx) {
        return vI.smap(ctx, (fe, i) -> {
            java.text.DateFormat rdf = java.text.DateFormat.getDateInstance(i);
            if (rdf instanceof java.text.SimpleDateFormat) {
                return new SimpleDateFormat(rdf, fe);
            } else {
                throw new RuntimeException("Unsupported subclass of DateFormat");
            }
        });
    }

    public static V<?> getDateInstance__I_Ljava_util_Locale__Lmodel_java_text_DateFormat(V<? extends java.lang.Integer> vI, V<? extends java.util.Locale> vL, FeatureExpr ctx) {
        return vI.sflatMap(ctx, (fe, i) -> {
            return vL.smap(fe, (fe2, l) -> {
                java.text.DateFormat rdf = java.text.DateFormat.getDateInstance(i, l);
                if (rdf instanceof java.text.SimpleDateFormat) {
                    return new SimpleDateFormat(rdf, fe2);
                } else {
                    throw new RuntimeException("Unsupported subclass of DateFormat");
                }
            });
        });
    }

    public static V<?> getDateTimeInstance__I_I__Lmodel_java_text_DateFormat(V<? extends java.lang.Integer> vI, V<? extends java.lang.Integer> vI2, FeatureExpr ctx) {
        return vI.sflatMap(ctx, (fe, i) -> {
            return vI.smap(fe, (fe2, i2) -> {
                java.text.DateFormat rdf = java.text.DateFormat.getDateTimeInstance(i, i2);
                if (rdf instanceof java.text.SimpleDateFormat) {
                    return new SimpleDateFormat(rdf, fe2);
                } else {
                    throw new RuntimeException("Unsupported subclass of DateFormat");
                }
            });
        });
    }

    public static V<?> getDateTimeInstance__I_I_Ljava_util_Locale__Lmodel_java_text_DateFormat(V<? extends java.lang.Integer> vI, V<? extends java.lang.Integer> vI2, V<? extends java.util.Locale> vL, FeatureExpr ctx) {
        return vI.sflatMap(ctx, (fe, i) -> {
            return vI.sflatMap(fe, (fe2, i2) -> {
                return (V) vL.smap(fe2, (fe3, l) -> {
                    java.text.DateFormat rdf = java.text.DateFormat.getDateTimeInstance(i, i2, l);
                    if (rdf instanceof java.text.SimpleDateFormat) {
                        return new SimpleDateFormat(rdf, fe2);
                    } else {
                        throw new RuntimeException("Unsupported subclass of DateFormat");
                    }
                });
            });
        });
    }

    @Override
    public V<? extends StringBuffer> format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(V<?> vObject, V<? extends StringBuffer> vToAppendTo, V<? extends FieldPosition> vPos, FeatureExpr ctx) {
        return null;
    }

    public V<?> setLenient__Z__V(V<? extends java.lang.Integer> vB, FeatureExpr ctx) {
        vB.sforeach(ctx, (fe, b) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, df) -> {
                ((java.text.DateFormat) df).setLenient(b != 0);
            });
        });
        return null;    // void
    }

    public V<?> parseObject__Ljava_lang_String_Lmodel_java_text_ParsePosition__Ljava_lang_Object(
            V<String> vString,
            V<? extends ParsePosition> vPP,
            FeatureExpr ctx
    ) {
        return vString.sflatMap(ctx, (fe, s) -> {
            return vPP.sflatMap(fe, (fe2, mpp) -> {
                mpp.split(fe2);
                return (V) mpp.raw().sflatMap(fe2, (fe3, pp) -> {
                    split(fe3);
                    return (V) vActual.smap(fe3, (fe4, df) -> {
                        return ((java.text.DateFormat) df).parseObject(s, pp);
                    });
                });
            });
        });
    }

    public V<?> parse__Ljava_lang_String__Ljava_util_Date(V<? extends String> vS, FeatureExpr ctx) {
        return vS.sflatMap(ctx, (fe, s) -> {
            split(fe);
            return vActual.smap(fe, (fe2, df) -> {
                try {
                    return ((java.text.DateFormat)df).parse(s);
                } catch (ParseException e) {
//                    e.printStackTrace();
                    throw new VException(e, fe2);
                }
            });
        });
    }

    public V<?> getCalendar____Ljava_util_Calendar(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, df -> ((java.text.DateFormat) df).getCalendar());
    }


    public V<?> setTimeZone__Ljava_util_TimeZone__V(V<? extends java.util.TimeZone> vT, FeatureExpr ctx) {
        vT.sforeach(ctx, (fe, t) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, df) -> {
                ((java.text.DateFormat)df).setTimeZone(t);
            });
        });
        return null;    // null
    }
}
