package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import model.java.lang.StringBuffer;

public abstract class Format {

    public final V<? extends String> format__Ljava_lang_Object__Ljava_lang_String(
            V<?> vObject,
            FeatureExpr ctx
    ) {
        return format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
                vObject,
                V.one(ctx, new StringBuffer(ctx)),
                V.one(ctx, new FieldPosition(V.one(ctx, 0), ctx, 0)),
                ctx
        ).sflatMap(ctx, (fe, x) -> (V<? extends String>) x.toString____Ljava_lang_String(fe));
    }

    public Format(FeatureExpr ctx){}

    public abstract V<? extends StringBuffer> format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(
            V<?> vObject,
            V<? extends StringBuffer> vToAppendTo,
            V<? extends FieldPosition> vPos,
            FeatureExpr ctx
    );
}

