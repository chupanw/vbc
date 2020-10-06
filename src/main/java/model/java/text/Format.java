package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.vbc.VException;
import model.java.lang.StringBuffer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Format {

    V<?> vActual;

    Format(V<?> vA) {
        vActual = vA;
    }
    // in case SUT classes extend the Format class
    public Format(FeatureExpr ctx) {}

    Format(){}

    void split(FeatureExpr ctx) {
        V<?> selected = vActual.smap(ctx, (fe, t) -> {
            try {
                Method m = t.getClass().getMethod("clone");
                return m.invoke(t);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new VException(e, fe);
            }
        });
        vActual = V.choice(ctx, selected, vActual);
    }

    public abstract V<? extends String> format__Ljava_lang_Object__Ljava_lang_String(V<?> vObject, FeatureExpr ctx);

    public abstract V<? extends StringBuffer> format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
            V<?> vObject,
            V<? extends StringBuffer> vToAppendTo,
            V<? extends FieldPosition> vPos,
            FeatureExpr ctx
    );

    public abstract V<?> parseObject__Ljava_lang_String_Lmodel_java_text_ParsePosition__Ljava_lang_Object(
            V<String> vString,
            V<? extends ParsePosition> vPP,
            FeatureExpr ctx
    );
}

