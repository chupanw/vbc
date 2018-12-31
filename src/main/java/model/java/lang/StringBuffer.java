package model.java.lang;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StringBuffer {

    private V<? extends java.lang.StringBuffer> vActual;

    public V<? extends java.lang.StringBuffer> raw() {
        return vActual;
    }

    public StringBuffer(V<? extends java.lang.StringBuffer> v) {
        vActual = v;
    }

    private void split(FeatureExpr ctx) {
        V<? extends java.lang.StringBuffer> selected = vActual.smap(ctx, t -> new java.lang.StringBuffer(t.toString()));
        vActual = V.choice(ctx, selected, vActual);
    }


    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////
    public StringBuffer(FeatureExpr ctx) {
        vActual = V.one(ctx, new java.lang.StringBuffer());
    }

    public StringBuffer(V<java.lang.Integer> vCapacity, FeatureExpr ctx, int dummy) {
        vActual = vCapacity.smap(ctx, c -> new java.lang.StringBuffer(c));
    }

    public StringBuffer(V<java.lang.String> vS, FeatureExpr ctx, String dummy) {
        vActual = vS.smap(ctx, s -> new java.lang.StringBuffer(s));
    }

    public V<?> append__Ljava_lang_String__Lmodel_java_lang_StringBuffer(V<? extends String> vS, FeatureExpr ctx) {
        vS.sforeach(ctx, (fe, s) -> {
            split(fe);
            vActual.sforeach(fe, sb -> sb.append(s));
        });
        return V.one(ctx, this);
    }

    public V<?> append__C__Lmodel_java_lang_StringBuffer(V<java.lang.Integer> vC, FeatureExpr ctx) {
        vC.sforeach(ctx, (fe, c) -> {
            split(fe);
            vActual.sforeach(fe, sb -> sb.append((char)c.intValue()));
        });
        return V.one(ctx, this);
    }

    public V<?> append__D__Lmodel_java_lang_StringBuffer(V<java.lang.Double> vD, FeatureExpr ctx) {
        vD.sforeach(ctx, (fe, d) -> {
            split(fe);
            vActual.sforeach(fe, sb -> sb.append(d.doubleValue()));
        });
        return V.one(ctx, this);
    }

    public V<?> append__J__Lmodel_java_lang_StringBuffer(V<java.lang.Long> vL, FeatureExpr ctx) {
        vL.sforeach(ctx, (fe, l) -> {
            split(fe);
            vActual.sforeach(fe, sb -> sb.append(l.longValue()));
        });
        return V.one(ctx, this);
    }

    public V<?> append__Ljava_lang_Object__Lmodel_java_lang_StringBuffer(V<?> vO, FeatureExpr ctx) {
        vO.sforeach(ctx, (fe, o) -> {
            split(fe);
            vActual.sforeach(fe, sb -> {
                try {
                    Method vToString = o.getClass().getMethod("toString____Ljava_lang_String", FeatureExpr.class);
                    V<? extends String> res = (V<? extends String>) vToString.invoke(o, fe);
                    res.sforeach(fe, s -> sb.append(s));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    sb.append(o);
                }
            });
        });
        return V.one(ctx, this);
    }

    public V<?> append__Lmodel_java_lang_StringBuffer__Lmodel_java_lang_StringBuffer(V<? extends StringBuffer> vSb, FeatureExpr ctx) {
        vSb.sforeach(ctx, (fe, osb) -> {
            osb.raw().sforeach(fe, (fe2, rosb) -> {
                split(fe2);
                vActual.sforeach(fe2, tsb -> tsb.append(rosb));
            });
        });
        return V.one(ctx, this);
    }

    public V<?> setLength__I__V(V<? extends java.lang.Integer> vI, FeatureExpr ctx) {
        vI.sforeach(ctx, (fe, i) -> {
            split(fe);
            vActual.sforeach(fe, sb -> sb.setLength(i));
        });
        return null;    // void
    }

    public V<?> toString____Ljava_lang_String(FeatureExpr ctx) {
        return vActual.smap(ctx, sb -> sb.toString());
    }

    public V<?> length____I(FeatureExpr ctx) {
        return vActual.smap(ctx, sb -> sb.length());
    }


}
