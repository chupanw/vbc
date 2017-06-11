package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;

/**
 * Created by ckaestne on 1/16/2016.
 */
public class VOps {

    public static V<? extends Integer> IADD(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() + bb.intValue()));
    }
    public static Vint IADD(Vint a, Vint b, FeatureExpr ctx){
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() + bb));
    }

    public static V<? extends Integer> IINC(V<? extends Integer> a, int increment, FeatureExpr ctx) {
        return a.smap(ctx, aa -> aa.intValue() + increment);
    }
    public static Vint IINC(Vint a, int increment, FeatureExpr ctx) {
        return a.smap(ctx, aa -> aa + increment);
    }

    /**
     * Called by lifted bytecode, compare with 0
     *
     * @param a
     * @return
     */
    public static FeatureExpr whenEQ(V<?> a) {
        return a.when(v -> {
            if (v instanceof Boolean)
                return !(Boolean) v ;
            else if (v instanceof Integer)
                return (Integer) v == 0;
            else
                throw new RuntimeException("Unsupported whenEQ type");
        }, true);
    }
    public static FeatureExpr whenEQ(Vint a) {
        return a.when(v -> v == 0);
    }

    /**
     * Called by lifted bytecode, compare with 0
     *
     * @param a
     * @return
     */
    public static FeatureExpr whenNE(V<?> a) {
        return a.when(v -> {
            if (v instanceof Boolean)
                return (Boolean) v;
            else if (v instanceof Integer)
                return (Integer) v != 0;
            else
                throw new RuntimeException("Unsupported whenNE type");
        }, true);
    }
    public static FeatureExpr whenNE(Vint a) {
        return a.when(v -> v != 0);
    }

    public static FeatureExpr whenGT(V<? extends Integer> a) {
        return a.when(v -> v > 0, true);
    }
    public static FeatureExpr whenGT(Vint a) {
        return a.when(v -> v > 0);
    }

    public static FeatureExpr whenGE(V<? extends Integer> a) {
        return a.when(v -> v >= 0, true);
    }
    public static FeatureExpr whenGE(Vint a) {
        return a.when(v -> v >= 0);
    }

    public static FeatureExpr whenLT(V<? extends Integer> a) {
        return a.when(v -> v < 0, true);
    }
    public static FeatureExpr whenLT(Vint a) {
        return a.when(v -> v < 0);
    }

    public static FeatureExpr whenLE(V<? extends Integer> a) {
        return a.when(v -> v <= 0, true);
    }
    public static FeatureExpr whenLE(Vint a) {
        return a.when(v -> v <= 0);
    }

    public static FeatureExpr whenNONNULL(V<? extends Object> a) {
        return a.when(v -> v != null, false);
    }
    public static FeatureExpr whenNONNULL(Vint a) {
        return a.when(v -> true);
    }

    public static FeatureExpr whenNULL(V<? extends Object> a) {
        return a.when(v -> v == null, false);
    }
    public static FeatureExpr whenNULL(Vint a) {
        return a.when(v -> true);
    }

    public static FeatureExpr whenIEQ(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = subtract(a, b);
        return whenEQ(sub);
    }
    public static FeatureExpr whenIEQ(Vint a, Vint b) {
        Vint sub = subtract(a, b);
        return whenEQ(sub);
    }

    public static FeatureExpr whenIGE(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = subtract(a, b);
        return whenGE(sub);
    }
    public static FeatureExpr whenIGE(Vint a, Vint b) {
        Vint sub = subtract(a, b);
        return whenGE(sub);
    }

    public static FeatureExpr whenILT(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = subtract(a, b);
        return whenLT(sub);
    }
    public static FeatureExpr whenILT(Vint a, Vint b) {
        Vint sub = subtract(a, b);
        return whenLT(sub);
    }

    public static FeatureExpr whenILE(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = subtract(a, b);
        return whenLE(sub);
    }
    public static FeatureExpr whenILE(Vint a, Vint b) {
        Vint sub = subtract(a, b);
        return whenLE(sub);
    }

    public static FeatureExpr whenINE(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = subtract(a, b);
        return whenNE(sub);
    }
    public static FeatureExpr whenINE(Vint a, Vint b) {
        Vint sub = subtract(a, b);
        return whenNE(sub);
    }

    public static FeatureExpr whenIGT(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = subtract(a, b);
        return whenGT(sub);
    }
    public static FeatureExpr whenIGT(Vint a, Vint b) {
        Vint sub = subtract(a, b);
        return whenGT(sub);
    }

    private static V<? extends Integer> subtract(V<? extends Integer> a, V<? extends Integer> b) {
        return a.flatMap(aa -> {
            if (aa == null)
                return V.one(null);
            else
                return b.map(bb -> {
                    if (bb == null)
                        return null;
                    else
                        return aa.intValue() - bb.intValue();
                });
        });
    }
    private static Vint subtract(Vint a, Vint b) {
        return a.flatMap(aa -> b.map(bb -> aa - bb));
    }

    public static V<? extends Integer> ISUB(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() - bb.intValue()));
    }
    public static Vint ISUB(Vint a, Vint b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() - bb));
    }


    public static V<? extends Integer> IMUL(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() * bb.intValue()));
    }
    public static Vint IMUL(Vint a, Vint b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() * bb));
    }

    public static V<? extends Integer> IDIV(V<? extends Integer> a, V<? extends Integer> b) {
        return a.flatMap(aa -> b.map(bb -> aa.intValue() / bb.intValue()));
    }
    public static Vint IDIV(Vint a, Vint b) {
        return a.flatMap(aa -> b.map(bb -> aa / bb));
    }

    public static V<? extends Integer> i2c(V<? extends Integer> a, FeatureExpr ctx) {
        return a.smap((v -> {
            int i = v.intValue();
            char c = (char)i;
            return (int) c;
        }), ctx);
    }
    public static Vint i2c(Vint a, FeatureExpr ctx) {
        return a.smap((v -> {
            int i = v;
            char c = (char)i;
            return (int) c;
        }), ctx);
    }

    public static FeatureExpr whenAEQ(V<?> a, V<?> b) {
        V<? extends Boolean> compare = a.flatMap(aa -> {
            return b.map(bb -> {
                return aa == bb;
            });
        });
        return compare.when(c -> c, true);
    }
    public static FeatureExpr whenAEQ(Vint a, Vint b) {
        V<? extends Boolean> compare = a.toV().flatMap(aa -> {
            return b.toV().map(bb -> {
                return aa == bb;
            });
        });
        return compare.when(c -> c, true);
    }

    public static FeatureExpr whenANE(V<?> a, V<?> b) {
        V<? extends Boolean> compare = a.flatMap(aa -> {
            return b.map(bb -> {
                return aa != bb;
            });
        });
        return compare.when(c -> c, true);
    }
    public static FeatureExpr whenANE(Vint a, Vint b) {
        V<? extends Boolean> compare = a.toV().flatMap(aa -> {
            return b.toV().map(bb -> {
                return aa != bb;
            });
        });
        return compare.when(c -> c, true);
    }

    public static V<? extends Integer> iushr(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 >>> v2));
    }
    public static Vint iushr(Vint value1, Vint value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 >>> v2));
    }

    public static V<? extends Integer> irem(V<? extends Integer> value1, V<? extends Integer> value2) {
        return value1.flatMap(v1 -> value2.map(v2 -> v1 % v2));
    }
    public static Vint irem(Vint value1, Vint value2) {
        return value1.flatMap(v1 -> value2.map(v2 -> v1 % v2));
    }

    public static V<? extends Integer> ior(V<? extends Integer> value1, V<? extends Integer> value2) {
        return value1.flatMap(v1 -> value2.map(v2 -> v1 | v2));
    }
    public static Vint ior(Vint value1, Vint value2) {
        return value1.flatMap(v1 -> value2.map(v2 -> v1 | v2));
    }
}
