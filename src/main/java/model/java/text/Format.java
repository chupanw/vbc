package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import model.java.lang.StringBuffer;

public abstract class Format {

    V<? extends java.text.Format> vActual;

    void split(FeatureExpr ctx) {
        V<? extends java.text.Format> selected = vActual.smap(ctx, t -> (java.text.Format) t.clone());
        vActual = V.choice(ctx, selected, vActual);
    }

    public final V<? extends String> format__Ljava_lang_Object__Ljava_lang_String(
            V<?> vObject,
            FeatureExpr ctx
    ) {
//        return format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
//                vObject,
//                V.one(ctx, new StringBuffer(ctx)),
//                V.one(ctx, new FieldPosition(V.one(ctx, 0), ctx, 0)),
//                ctx
//        ).sflatMap(ctx, (fe, x) -> (V<? extends String>) x.toString____Ljava_lang_String(fe));
        return vObject.sflatMap(ctx, (fe, o) -> {
            split(fe);
            return vActual.smap(fe, (fe2, f) -> {
                return f.format(o);
            });
        });
    }

    public Format(FeatureExpr ctx){}
    Format(V<? extends java.text.Format> vA) {
        vActual = vA;
    }

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

