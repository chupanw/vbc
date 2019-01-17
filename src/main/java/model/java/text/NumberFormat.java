package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.vbc.VException;
import model.java.lang.StringBuffer;

/**
 * We don't need internal class for this because it's originally abstract
 */
public class NumberFormat extends Format {

    NumberFormat(V<?> v) {
        super(v);
    }

    NumberFormat(){
        super();
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    public static V<?> getNumberInstance__Ljava_util_Locale__Lmodel_java_text_NumberFormat(
            V<? extends java.util.Locale> vLocale,
            FeatureExpr ctx
    ) {
        NumberFormat ret = new NumberFormat(vLocale.smap(ctx, (fe, l) -> {
            java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(l);
            if (nf instanceof java.text.DecimalFormat) {
                return new DecimalFormat(new DecimalFormatWrapper((java.text.DecimalFormat) nf), fe);
            } else {
                throw new VException(new RuntimeException("Unsupported subclass of NumberFormat"), ctx);
            }
        }));
        return V.one(ctx, ret);
    }

    public static V<?> getInstance____Lmodel_java_text_NumberFormat(FeatureExpr ctx) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        if (nf instanceof java.text.DecimalFormat) {
            return V.one(ctx, new NumberFormat(V.one(ctx, new DecimalFormatWrapper((java.text.DecimalFormat) nf))));
        } else {
            throw new VException(new RuntimeException("Unsupported subclass of NumberFormat"), ctx);
        }
    }

    public static V<?> getInstance__Ljava_util_Locale__Lmodel_java_text_NumberFormat(V<? extends java.util.Locale> vL, FeatureExpr ctx) {
        V<?> nfs = vL.smap(ctx, (fe, l) -> {
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(l);
            if (nf instanceof java.text.DecimalFormat) {
                return new DecimalFormat(new DecimalFormatWrapper((java.text.DecimalFormat) nf), fe);
            } else {
                throw new VException(new RuntimeException("Unsupported subclass of NumberFormat"), ctx);
            }
        });
        return nfs;
    }

    public static V<?> getCurrencyInstance____Lmodel_java_text_NumberFormat(FeatureExpr ctx) {
        java.text.NumberFormat rnf = java.text.NumberFormat.getCurrencyInstance();
        if (rnf instanceof java.text.DecimalFormat) {
            return V.one(ctx, new DecimalFormat(new DecimalFormatWrapper((java.text.DecimalFormat) rnf), ctx));
        } else {
            throw new VException(new RuntimeException("Unsupported subclass of NumberFormat"), ctx);
        }
    }

    public static V<?> getCurrencyInstance__Ljava_util_Locale__Lmodel_java_text_NumberFormat(V<? extends java.util.Locale> vL, FeatureExpr ctx) {
        return vL.smap(ctx, (fe, l) -> {
            java.text.NumberFormat rnf = java.text.NumberFormat.getCurrencyInstance(l);
            if (rnf instanceof java.text.DecimalFormat) {
                return new DecimalFormat(new DecimalFormatWrapper((java.text.DecimalFormat) rnf), fe);
            } else {
                throw new VException(new RuntimeException("Unsupported subclass of NumberFormat"), ctx);
            }
        });
    }

    public static V<?> getPercentInstance____Lmodel_java_text_NumberFormat(FeatureExpr ctx) {
        java.text.NumberFormat rnf = java.text.NumberFormat.getPercentInstance();
        if (rnf instanceof java.text.DecimalFormat) {
            return V.one(ctx, new DecimalFormat(new DecimalFormatWrapper((java.text.DecimalFormat) rnf), ctx));
        } else {
            throw new VException(new RuntimeException("Unsupported subclass of NumberFormat"), ctx);
        }
    }

    public static V<?> getPercentInstance__Ljava_util_Locale__Lmodel_java_text_NumberFormat(V<? extends java.util.Locale> vL, FeatureExpr ctx) {
        return vL.smap(ctx, (fe, l) -> {
            java.text.NumberFormat rnf = java.text.NumberFormat.getPercentInstance(l);
            if (rnf instanceof java.text.DecimalFormat) {
                return new DecimalFormat(new DecimalFormatWrapper((java.text.DecimalFormat) rnf), fe);
            } else {
                throw new VException(new RuntimeException("Unsupported subclass of NumberFormat"), ctx);
            }
        });
    }

    public V<?> setMaximumFractionDigits__I__V(V<Integer> vNewValue, FeatureExpr ctx) {
        vNewValue.sforeach(ctx, (fe, i) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, l) -> {
                if (l instanceof DecimalFormatWrapper) {
                    ((DecimalFormatWrapper)l).actual.setMaximumFractionDigits(i);
                } else {
                    throw new VException(new RuntimeException("Unsupported wrapper type"), fe2);
                }
            });
        });
        return null;    // void
    }

    public V<?> setParseIntegerOnly__Z__V(V<Integer> vValue, FeatureExpr ctx) {
        vValue.sforeach(ctx, (fe, v) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, l) -> {
                if (l instanceof DecimalFormatWrapper) {
                    ((DecimalFormatWrapper)l).actual.setParseIntegerOnly(v != 0);
                } else {
                    throw new VException(new RuntimeException("Unsupported wrapper type"), fe2);
                }
            });
        });
        return null;    // void
    }

    public V<?> setGroupingUsed__Z__V(V<? extends Integer> vNewValue, FeatureExpr ctx) {
        vNewValue.sforeach(ctx, (fe, v) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, nf) -> {
                if (nf instanceof DecimalFormatWrapper) {
                    ((DecimalFormatWrapper)nf).actual.setGroupingUsed(v.intValue() != 0);
                } else {
                    throw new VException(new RuntimeException("Unsupported wrapper type"), fe2);
                }
            });
        });
        return null;    // void
    }

    public V<?> setMinimumFractionDigits__I__V(V<? extends Integer> vNewValue, FeatureExpr ctx) {
        vNewValue.sforeach(ctx, (fe, v) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, nf) -> {
                if (nf instanceof DecimalFormatWrapper) {
                    ((DecimalFormatWrapper)nf).actual.setMinimumFractionDigits(v.intValue());
                } else {
                    throw new VException(new RuntimeException("Unsupported wrapper type"), fe2);
                }
            });
        });
        return null;    // void
    }

    public V<?> clone____Ljava_lang_Object(FeatureExpr ctx) {
        V<?> cloned = vActual.smap(ctx, (fe, l) -> {
            if (l instanceof DecimalFormatWrapper) {
                return ((DecimalFormatWrapper)l).clone();
            } else {
                throw new VException(new RuntimeException("Unsupported wrapper type"), fe);
            }
        });
        return V.one(ctx, new NumberFormat(cloned));
    }

    public V<? extends String> format__D__Ljava_lang_String(V<Double> vNumber, FeatureExpr ctx) {
        return vNumber.sflatMap(ctx, (fe, number) -> {
            split(fe);
            return vActual.smap(fe, (fe2, n) -> {
                if (n instanceof DecimalFormatWrapper) {
                    return ((DecimalFormatWrapper)n).actual.format(number);
                } else {
                    throw new VException(new RuntimeException("Unsupported wrapper type"), fe2);
                }
            });
        });
    }

    public V<? extends String> format__J__Ljava_lang_String(V<Long> vNumber, FeatureExpr ctx) {
        return vNumber.sflatMap(ctx, (fe, number) -> {
            split(fe);
            return vActual.smap(fe, (fe2, n) -> {
                if (n instanceof DecimalFormatWrapper) {
                    return ((DecimalFormatWrapper)n).actual.format(number);
                } else {
                    throw new VException(new RuntimeException("Unsupported wrapper type"), fe2);
                }
            });
        });
    }

    public V format__J_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
            V<? extends java.lang.Long> vNumber,
            V<? extends StringBuffer> vToAppendTo,
            V<? extends FieldPosition> vPos,
            FeatureExpr ctx
    ) {
        V ret = vNumber.sflatMap(ctx, (fe, n) -> {
            split(fe);
            return vActual.sflatMap(fe, (fe1, nf) -> {
                return (V) vToAppendTo.sflatMap(fe1, (fe2, toAppendTo) -> {
                    toAppendTo.split(fe2);
                    return (V) toAppendTo.raw().sflatMap(fe2, (fe3, rawToAppendTo) -> {
                        return (V) vPos.sflatMap(fe3, (fe4, pos) -> {
                            pos.split(fe4);
                            return (V) pos.raw().smap(fe4, (fe5, rawPos) -> {
                                if (nf instanceof DecimalFormatWrapper) {
                                    return ((DecimalFormatWrapper)nf).actual.format(n.longValue(), rawToAppendTo, rawPos);
                                } else {
                                    throw new VException(new RuntimeException("Unsupported wrapper type"), fe5);
                                }
                            });
                        });
                    });
                });
            });
        });
        return V.one(ctx, new StringBuffer(ret));
    }

    public V format__D_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
            V<? extends java.lang.Double> vNumber,
            V<? extends StringBuffer> vToAppendTo,
            V<? extends FieldPosition> vPos,
            FeatureExpr ctx
    ) {
        V ret = vNumber.sflatMap(ctx, (fe, n) -> {
            split(fe);
            return vActual.sflatMap(fe, (fe1, nf) -> {
                return (V) vToAppendTo.sflatMap(fe1, (fe2, toAppendTo) -> {
                    toAppendTo.split(fe2);
                    return (V) toAppendTo.raw().sflatMap(fe2, (fe3, rawToAppendTo) -> {
                        return (V) vPos.sflatMap(fe3, (fe4, pos) -> {
                            pos.split(fe4);
                            return (V) pos.raw().smap(fe4, (fe5, rawPos) -> {
                                if (nf instanceof DecimalFormatWrapper) {
                                    return ((DecimalFormatWrapper)nf).actual.format(n.doubleValue(), rawToAppendTo, rawPos);
                                } else {
                                    throw new VException(new RuntimeException("Unsupported wrapper type"), fe5);
                                }
                            });
                        });
                    });
                });
            });
        });
        return V.one(ctx, new StringBuffer(ret));
    }

    @Override
    public V<? extends String> format__Ljava_lang_Object__Ljava_lang_String(V<?> vObject, FeatureExpr ctx) {
        return vObject.sflatMap(ctx, (fe, o) -> {
            split(fe);
            return vActual.smap(fe, (fe2, f) -> {
                if (f instanceof DecimalFormatWrapper) {
                    return ((DecimalFormatWrapper) f).actual.format(o);
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
                                        if (nf instanceof DecimalFormatWrapper) {
                                            return ((DecimalFormatWrapper)nf).actual.format(o, (java.lang.StringBuffer) rsb, (java.text.FieldPosition) rp);
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

    @Override
    public V<?> parseObject__Ljava_lang_String_Lmodel_java_text_ParsePosition__Ljava_lang_Object(V<String> vString, V<? extends ParsePosition> vPP, FeatureExpr ctx) {
        return vString.sflatMap(ctx, (fe, s) -> {
            split(fe);
            return vActual.sflatMap(fe, (fe2, nf) -> {
                return (V) vPP.sflatMap(fe2, (fe3, mpp) -> {
                    mpp.split(fe3);
                    return (V) mpp.raw().smap(fe3, (fe4, pp) -> {
                        if (nf instanceof DecimalFormatWrapper) {
                            Object o = ((DecimalFormatWrapper) nf).actual.parseObject(s, pp);
                            return o;
                        } else {
                            throw new VException(new RuntimeException("Unsupported wrapper type") , fe4);
                        }
                    });
                });
            });
        });
    }

    public V parse__Ljava_lang_String_Lmodel_java_text_ParsePosition__Ljava_lang_Number(V<? extends String> vS, V<? extends ParsePosition> vP, FeatureExpr ctx) {
        return vS.sflatMap(ctx, (fe1, s) -> {
            split(fe1);
            return vActual.sflatMap(fe1, (fe2, nf) -> {
                return (V) vP.sflatMap(fe2, (fe3, p) -> {
                    p.split(fe3);
                    return (V) p.raw().smap(fe3, (fe4, rawp) -> {
                        if (nf instanceof DecimalFormatWrapper) {
                            return ((DecimalFormatWrapper)nf).actual.parse(s, rawp);
                        } else {
                            throw new VException(new RuntimeException("Unsupported wrapper type") , fe4);
                        }
                    });
                });
            });
        });
    }

    public V<?> isParseIntegerOnly____Z(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, (fe, nf) -> {
            if (nf instanceof DecimalFormatWrapper)
                return ((DecimalFormatWrapper) nf).actual.isParseIntegerOnly();
            else {
                throw new VException(new RuntimeException("Unsupported wrapper type"), fe);
            }
        });
    }

    public V<?> getMinimumFractionDigits____I(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, (fe, nf) -> {
            if (nf instanceof DecimalFormatWrapper)
                return ((DecimalFormatWrapper) nf).actual.getMinimumFractionDigits();
            else {
                throw new VException(new RuntimeException("Unsupported wrapper type"), fe);
            }
        });
    }

    public V<?> getMaximumFractionDigits____I(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, (fe, nf) -> {
            if (nf instanceof DecimalFormatWrapper)
                return ((DecimalFormatWrapper) nf).actual.getMaximumFractionDigits();
            else {
                throw new VException(new RuntimeException("Unsupported wrapper type"), fe);
            }
        });
    }

}
