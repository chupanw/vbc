package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.ArrayOps;
import edu.cmu.cs.varex.V;
import model.java.lang.StringBuffer;

public class MessageFormat extends Format {

    public MessageFormat(V<String> vString, FeatureExpr ctx, String dummy) {
        vActual = vString.smap(ctx, (fe, string) -> new java.text.MessageFormat(string));
    }

    //////////////////////////////////////////////////
    // Lifted methods
    //////////////////////////////////////////////////

    @Override
    public V<? extends String> format__Ljava_lang_Object__Ljava_lang_String(V<?> vObject, FeatureExpr ctx) {
        return vObject.sflatMap(ctx, (fe, object) -> {
            if (object instanceof V[]) {
                V<Object[]> expanded = ArrayOps.expandArray((V[]) object, Object[].class, fe);
                return expanded.sflatMap(fe, (fe2, array) -> {
                    split(fe2);
                    return (V<String>) vActual.smap(fe2, (fe3, actual) -> ((java.text.MessageFormat) actual).format(array));
                });
            }
            else {
                split(fe);
                return vActual.smap(fe, (fe2, actual) -> ((java.text.MessageFormat) actual).format(object));
            }
        });
    }

    @Override
    public V<? extends StringBuffer> format__Ljava_lang_Object_Lmodel_java_lang_StringBuffer_Lmodel_java_text_FieldPosition__Lmodel_java_lang_StringBuffer(V<?> vObject, V<? extends StringBuffer> vToAppendTo, V<? extends FieldPosition> vPos, FeatureExpr ctx) {
        return null;
    }

    @Override
    public V<?> parseObject__Ljava_lang_String_Lmodel_java_text_ParsePosition__Ljava_lang_Object(V<String> vString, V<? extends ParsePosition> vPP, FeatureExpr ctx) {
        return null;
    }

    public static V<String> format__Ljava_lang_String_Array_Ljava_lang_Object__Ljava_lang_String(
            V<String> vString,
            V<V<Object>[]> vArray,
            FeatureExpr ctx
    ) {
        return (V<String>) vString.sflatMap(ctx, (fe, string) -> {
            return vArray.sflatMap(fe, (fe2, array) -> {
                V<Object[]> expanded = ArrayOps.expandArray(array, Object[].class, fe2);
                return (V<String>) expanded.smap(fe2, (fe3, arr) -> {
                    return java.text.MessageFormat.format(string, arr);
                });
            });
        });
    }

}
