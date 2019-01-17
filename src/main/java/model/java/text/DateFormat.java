package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.vbc.VException;
import model.java.lang.StringBuffer;

import java.text.ParseException;

public class DateFormat extends Format {

    protected DateFormat(FeatureExpr ctx) {
        super();
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
                    return new SimpleDateFormat(new SimpleDateFormatWrapper((java.text.SimpleDateFormat) rdf), fe2);
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
                return new SimpleDateFormat(new SimpleDateFormatWrapper((java.text.SimpleDateFormat) rdf), fe);
            } else {
                throw new RuntimeException("Unsupported subclass of DateFormat");
            }
        });
    }

    public static V<?> getDateInstance__I__Lmodel_java_text_DateFormat(V<? extends java.lang.Integer> vI, FeatureExpr ctx) {
        return vI.smap(ctx, (fe, i) -> {
            java.text.DateFormat rdf = java.text.DateFormat.getDateInstance(i);
            if (rdf instanceof java.text.SimpleDateFormat) {
                return new SimpleDateFormat(new SimpleDateFormatWrapper((java.text.SimpleDateFormat) rdf), fe);
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
                    return new SimpleDateFormat(new SimpleDateFormatWrapper((java.text.SimpleDateFormat) rdf), fe2);
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
                    return new SimpleDateFormat(new SimpleDateFormatWrapper((java.text.SimpleDateFormat) rdf), fe2);
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
                        return new SimpleDateFormat(new SimpleDateFormatWrapper((java.text.SimpleDateFormat) rdf), fe3);
                    } else {
                        throw new RuntimeException("Unsupported subclass of DateFormat");
                    }
                });
            });
        });
    }

    @Override
    public V<? extends String> format__Ljava_lang_Object__Ljava_lang_String(V<?> vObject, FeatureExpr ctx) {
        return vObject.sflatMap(ctx, (fe, o) -> {
            split(fe);
            return vActual.smap(fe, (fe2, f) -> {
                if (f instanceof SimpleDateFormatWrapper) {
                    return ((SimpleDateFormatWrapper) f).actual.format(o);
                } else {
                    throw new VException(new RuntimeException("Unsupported format type"), fe);
                }
            });
        });
    }

    @Override
    public V<? extends StringBuffer> format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(V<?> vObject, V<? extends StringBuffer> vToAppendTo, V<? extends FieldPosition> vPos, FeatureExpr ctx) {
        V<? extends java.lang.StringBuffer> ret =
                (V<? extends java.lang.StringBuffer>) vObject.sflatMap(ctx, (fe1, o) -> {
                    split(fe1);
                    return vActual.sflatMap(fe1, (fe2, nf) -> {
                        return (V) vToAppendTo.sflatMap(fe2, (fe3, sb) -> {
                            sb.split(fe3);
                            return (V<?>) sb.raw().sflatMap(fe3, (fe4, rsb) -> {
                                return (V<?>) vPos.sflatMap(fe4, (fe5, p) -> {
                                    p.split(fe5);
                                    return (V<?>) p.raw().smap(fe5, (fe6, rp) -> {
                                        if (nf instanceof SimpleDateFormatWrapper) {
                                            return ((SimpleDateFormatWrapper)nf).actual.format(o, (java.lang.StringBuffer) rsb, (java.text.FieldPosition) rp);
                                        } else {
                                            throw new VException(new RuntimeException("Unsupported format type"), fe6);
                                        }
                                    });
                                });
                            });
                        });
                    });
                });
        return V.one(ctx, new StringBuffer(ret));
    }

    public V<?> setLenient__Z__V(V<? extends java.lang.Integer> vB, FeatureExpr ctx) {
        vB.sforeach(ctx, (fe, b) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, df) -> {
                if (df instanceof SimpleDateFormatWrapper) {
                    ((SimpleDateFormatWrapper) df).actual.setLenient(b != 0);
                } else {
                    throw new VException(new RuntimeException("Unsupported format type"), fe);
                }
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
            split(fe);
            return vActual.sflatMap(fe, (fe2, df) -> {
                return (V) vPP.sflatMap(fe2, (fe3, mpp) -> {
                    mpp.split(fe3);
                    return (V) mpp.raw().smap(fe3, (fe4, pp) -> {
                        if (df instanceof SimpleDateFormatWrapper) {
                            return ((SimpleDateFormatWrapper) df).actual.parseObject(s, pp);
                        } else {
                            throw new VException(new RuntimeException("Unsupported format type"), fe);
                        }
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
                    if (df instanceof SimpleDateFormatWrapper) {
                        return ((SimpleDateFormatWrapper) df).actual.parse(s);
                    } else {
                        throw new VException(new RuntimeException("Unsupported format type"), fe);
                    }
                } catch (ParseException e) {
//                    e.printStackTrace();
                    throw new VException(e, fe2);
                }
            });
        });
    }

    public V<?> getCalendar____Ljava_util_Calendar(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, (fe, df) -> {
            if (df instanceof SimpleDateFormatWrapper) {
                return ((SimpleDateFormatWrapper) df).actual.getCalendar();
            } else {
                throw new VException(new RuntimeException("Unsupported format type"), fe);
            }
        });
    }


    public V<?> setTimeZone__Ljava_util_TimeZone__V(V<? extends java.util.TimeZone> vT, FeatureExpr ctx) {
        vT.sforeach(ctx, (fe, t) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, df) -> {
                if (df instanceof SimpleDateFormatWrapper) {
                    ((SimpleDateFormatWrapper) df).actual.setTimeZone(t);
                } else {
                    throw new VException(new RuntimeException("Unsupported format type"), fe);
                }
            });
        });
        return null;    // null
    }
}
