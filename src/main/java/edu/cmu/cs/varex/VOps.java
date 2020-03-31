package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.vbc.config.Settings;
import edu.cmu.cs.vbc.config.VERuntime;
import edu.cmu.cs.vbc.VException;
import edu.cmu.cs.vbc.vbytecode.Owner;
import model.java.lang.StringBuilder;
import org.apache.commons.beanutils.BeanUtilsBean;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.*;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

/**
 * Created by ckaestne on 1/16/2016.
 */
public class VOps {

    // no runtime exception
    public static V<? extends Integer> IADD(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() + bb.intValue()));
    }

    // no runtime exception
    public static V<? extends Integer> IINC(V<? extends Integer> a, int increment, FeatureExpr ctx) {
        return a.smap(ctx, aa -> aa.intValue() + increment);
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
                throw new RuntimeException("Unsupported whenEQ type: " + v.getClass());
        }, true);
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

    public static FeatureExpr whenGT(V<? extends Integer> a) {
        return a.when(v -> v > 0, true);
    }

    public static FeatureExpr whenGE(V<? extends Integer> a) {
        return a.when(v -> v >= 0, true);
    }

    public static FeatureExpr whenLT(V<? extends Integer> a) {
        return a.when(v -> v < 0, true);
    }

    public static FeatureExpr whenLE(V<? extends Integer> a) {
        return a.when(v -> v <= 0, true);
    }

    public static FeatureExpr whenNONNULL(V<? extends Object> a) {
        return a.when(v -> v != null, false);
    }

    public static FeatureExpr whenNULL(V<? extends Object> a) {
        return a.when(v -> v == null, false);
    }

    public static FeatureExpr whenIEQ(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        V<? extends Integer> sub = compareInt(a.select(ctx), b.select(ctx));
        return whenEQ(sub);
    }

    public static FeatureExpr whenIGE(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        V<? extends Integer> sub = compareInt(a.select(ctx), b.select(ctx));
        return whenGE(sub);
    }

    public static FeatureExpr whenILT(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        V<? extends Integer> sub = compareInt(a.select(ctx), b.select(ctx));
        return whenLT(sub);
    }

    public static FeatureExpr whenILE(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        V<? extends Integer> sub = compareInt(a.select(ctx), b.select(ctx));
        return whenLE(sub);
    }

    public static FeatureExpr whenINE(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        V<? extends Integer> sub = compareInt(a.select(ctx), b.select(ctx));
        return whenNE(sub);
    }

    public static FeatureExpr whenIGT(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        V<? extends Integer> sub = compareInt(a.select(ctx), b.select(ctx));
        return whenGT(sub);
    }

    private static V<? extends Integer> compareInt(V<? extends Integer> a, V<? extends Integer> b) {
        return a.flatMap(aa -> {
            if (aa == null)
                return V.one(null);
            else
                return b.map(bb -> {
                    if (bb == null)
                        return null;
                    else {
                        // avoid Integer overflow
                        if (aa.intValue() >= 0 && bb.intValue() < 0)
                           return 1;
                        else if (aa.intValue() < 0 && bb.intValue() > 0)
                            return -1;
                        else
                            return aa.intValue() - bb.intValue();
                    }
                });
        });
    }

    // no runtime exception
    public static V<? extends Integer> ISUB(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() - bb.intValue()));
    }

    // no runtime exception
    public static V<? extends Float> FSUB(V<? extends Float> a, V<? extends Float> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.floatValue() - bb.floatValue()));
    }


    // no runtime exception
    public static V<? extends Integer> IMUL(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() * bb.intValue()));
    }

    public static V<? extends Integer> IDIV(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, (fe2, bb) -> {
            return aa.intValue() / bb.intValue();
        }));
    }

    // no runtime exception
    public static V<? extends Integer> i2c(V<? extends Integer> a, FeatureExpr ctx) {
        return a.smap((v -> {
            int i = v.intValue();
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

    public static FeatureExpr whenANE(V<?> a, V<?> b) {
        V<? extends Boolean> compare = a.flatMap(aa -> {
            return b.map(bb -> {
                return aa != bb;
            });
        });
        return compare.when(c -> c, true);
    }

    // no runtime exception
    public static V<? extends Integer> iushr(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 >>> v2));
    }

    // arithmetic exception
    public static V<? extends Integer> irem(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, (fe2, v2) -> {
            return v1.intValue() % v2.intValue();
        }));
    }

    // no runtime exception
    public static V<? extends Double> drem(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() % v2.doubleValue()));
    }

    // no runtime exception
    public static V<? extends Integer> ior(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.intValue() | v2.intValue()));
    }

    // no runtime exception
    public static V<? extends Integer> iand(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.intValue() & v2.intValue()));
    }

    // no runtime exception
    public static V<? extends Integer> ixor(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 ^ v2));
    }

    // arithmetic exception
    public static V<? extends Long> ldiv(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, (fe2, v2) -> {
            return v1.longValue() / v2.longValue();
        }));
    }

    // no runtime exception
    public static V<? extends Integer> l2i(V<? extends Long> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) x.longValue());
    }

    // no runtime exception
    public static V<? extends Integer> i2b(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) (byte) x.intValue());
    }

    // no runtime exception
    public static V<? extends Integer> i2s(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) (short) x.intValue());
    }

    // no runtime exception
    public static V<? extends Long> i2l(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (long) x.intValue());
    }

    // no runtime exception
    public static V<? extends Long> ladd(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() + v2.longValue()));
    }

    // no runtime exception
    public static V<? extends Long> land(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() & v2.longValue()));
    }

    // no runtime exception
    public static V<? extends Long> lushr(V<? extends Long> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() >>> v2.intValue()));
    }

    // no runtime exception
    public static V<? extends Long> lsub(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() - v2.longValue()));
    }

    // no runtime exception
    public static V<? extends Integer> ineg(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, v -> -v.intValue());
    }

    // no runtime exception
    public static V<? extends Float> fneg(V<? extends Float> value1, FeatureExpr ctx) {
        return value1.smap(ctx, v -> -v.floatValue());
    }

    // no runtime exception
    public static V<? extends Double> dmul(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() * v2.doubleValue()));
    }

    // no runtime exception
    public static V<? extends Integer> dcmpl(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> {
            if (v1.isNaN() || v2.isNaN()) return -1;
            else if (v1.doubleValue() > v2.doubleValue()) return 1;
            else if (v1.doubleValue() == v2.doubleValue()) return 0;
            else return -1;
        }));
    }

    // no runtime exception
    public static V<? extends Long> lmul(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() * v2.longValue()));
    }

    // no runtime exception
    public static V<? extends Long> lor(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() | v2.longValue()));
    }

    // no runtime exception
    public static V<? extends Long> lshl(V<? extends Long> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() << v2.intValue()));
    }

    // no runtime exception
    public static V<? extends Long> lxor(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() ^ v2.longValue()));
    }

    // no runtime exception
    public static V<? extends Double> l2d(V<? extends Long> value1, FeatureExpr ctx) {
        return value1.smap(ctx, l -> (double) l.longValue());
    }

    // no runtime exception
    public static V<? extends Double> i2d(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, i -> (double) i.intValue());
    }

    // no runtime exception
    public static V<? extends Float> d2f(V<? extends Double> value1, FeatureExpr ctx) {
        return value1.smap(ctx, d -> (float) d.doubleValue());
    }

    // no runtime exception
    public static V<? extends Float> l2f(V<? extends Long> value1, FeatureExpr ctx) {
        return value1.smap(ctx, l -> (float) l.longValue());
    }

    // no runtime exception
    public static V<? extends Double> f2d(V<? extends Float> value1, FeatureExpr ctx) {
        return value1.smap(ctx, f -> (double) f.floatValue());
    }

    // no runtime exception
    public static V<? extends Integer> dcmpg(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> {
            if (v1.isNaN() || v2.isNaN()) return 1;
            else if (v1.doubleValue() > v2.doubleValue()) return 1;
            else if (v1.doubleValue() == v2.doubleValue()) return 0;
            else return -1;
        }));
    }

    // no runtime exception
    public static V<? extends Double> ddiv(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() / v2.doubleValue()));
    }

    // no runtime exception
    public static V<? extends Float> i2f(V<? extends Integer> value, FeatureExpr ctx) {
        return value.smap(ctx, i -> (float) i.intValue());
    }

    // no runtime exception
    public static V<? extends Float> fdiv(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.floatValue() / v2.floatValue()));
    }

    // no runtime exception
    public static V<? extends Integer> f2i(V<? extends Float> value, FeatureExpr ctx) {
        return value.smap(ctx, f -> (int) f.floatValue());
    }

    // no runtime exception
    public static V<? extends Integer> fcmpg(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> {
            if (v1.isNaN() || v2.isNaN()) return 1;
            else if (v1.floatValue() > v2.floatValue()) return 1;
            else if (v1.floatValue() == v2.floatValue()) return 0;
            else return -1;
        }));
    }

    // no runtime exception
    public static V<? extends Integer> fcmpl(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> {
            if (v1.isNaN() || v2.isNaN()) return -1;
            else if (v1.floatValue() > v2.floatValue()) return 1;
            else if (v1.floatValue() == v2.floatValue()) return 0;
            else return -1;
        }));
    }

    // no runtime exception
    public static V<? extends Float> fmul(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.floatValue() * v2.floatValue()));
    }

    // no runtime exception
    public static V<? extends Long> d2l(V<? extends Double> value, FeatureExpr ctx) {
        return value.smap(ctx, v -> (long) v.doubleValue());
    }

    // no runtime exception
    public static V<? extends Double> dadd(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() + v2.doubleValue()));
    }

    // arithmetic exception
    public static V<? extends Long> lrem(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, (fe2, v2) -> {
            return v1.longValue() % v2.longValue();
        }));
    }

    // no runtime exception
    public static V<? extends Float> fadd(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.floatValue() + v2.floatValue()));
    }

    // no runtime exception
    public static V<? extends Long> lshr(V<? extends Long> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() >> v2.intValue()));
    }

    // no runtime exception
    public static V<? extends Double> dsub(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() - v2.doubleValue()));
    }

    // no runtime exception
    public static V<? extends Integer> d2i(V<? extends Double> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) x.doubleValue());
    }

    //////////////////////////////////////////////////
    // Special println that prints configuration as well
    //////////////////////////////////////////////////
    public static void println(PrintStream out, String s, FeatureExpr ctx) {
        if (ctx.isSatisfiable()) {
            if (Settings.printContext())
                out.println(s + " [" + ctx + "]");
            else
                out.println(s + " [hidden context]");
        }
    }
    public static void println(PrintStream out, int i, FeatureExpr ctx) {
        if (ctx.isSatisfiable()) {
            if (Settings.printContext())
                out.println(i + " [" + ctx + "]");
            else
                out.println(i + " [hidden context]");
        }
    }
    public static void println(PrintStream out, long l, FeatureExpr ctx) {
        if (ctx.isSatisfiable()) {
            if (Settings.printContext())
                out.println(l + " [" + ctx + "]");
            else
                out.println(l + " [hidden context]");
        }
    }
    public static void println(PrintStream out, double d, FeatureExpr ctx) {
        if (ctx.isSatisfiable()) {
            if (Settings.printContext())
                out.println(d + " [" + ctx + "]");
            else
                out.println(d + " [hidden context]");
        }
    }
    public static void println(PrintStream out, Object o, FeatureExpr ctx) {
        if (ctx.isSatisfiable()) {
            if (Settings.printContext())
                out.println(o + " [" + ctx + "]");
            else
                out.println(o + " [hidden context]");
        }
    }
    public static void println(PrintStream out, char c, FeatureExpr ctx) {
        if (ctx.isSatisfiable()) {
            if (Settings.printContext())
                out.println(c + " [" + ctx + "]");
            else
                out.println(c + " [hidden context]");
        }
    }
    public static void println(PrintStream out, boolean b, FeatureExpr ctx) {
        if (ctx.isSatisfiable()) {
            if (Settings.printContext())
                out.println(b + " [" + ctx + "]");
            else
                out.println(b + " [hidden context]");
        }
    }
    public static void println(PrintStream out, FeatureExpr ctx) {
        if (ctx.isSatisfiable()) {
            if (Settings.printContext())
                out.println(" [" + ctx + "]");
            else
                out.println(" [hidden context]");
        }
    }

    //////////////////////////////////////////////////
    // Truncating primitive types to int
    //////////////////////////////////////////////////
    public static Integer truncB(Integer o) {
        return (int) (byte) o.intValue();
    }
    public static Integer truncC(Integer o) {
        return (int) (char) o.intValue();
    }
    public static Integer truncZ(Integer o) {
        return (int) (byte) o.intValue();   // same as byte according to the spec of BASTORE
    }
    public static Integer truncS(Integer o) {
        return (int) (short) o.intValue();
    }

    /**
     * Called as part of lifting ATHROW to throw exceptions under method contexts.
     */
    public static V<?> extractThrowableAndThrow(V<? extends Throwable> vT, FeatureExpr ctx) throws Throwable {
        V<? extends Throwable> selected = vT.select(ctx);
        FeatureExpr configSpace = selected.getConfigSpace();
        if (!VERuntime.shouldPostpone(configSpace)) {
            VERuntime.throwExceptionCtx(configSpace);
            if (selected instanceof One) {
                throw new VException(selected.getOne(), configSpace);
            } else {
                One oneValue = ((VImpl) selected).getOneValue();
                Object v = oneValue.value;  // HashMap VImpl
                if (v instanceof AssertionError) {
                    throw new VException(new RuntimeException("Multiple assertion errors"), selected.getConfigSpace());
                } else {
                    throw new VException((Throwable) v, oneValue.getConfigSpace());
                }
            }
        } else {
            VERuntime.postponeExceptionCtx(ctx);
        }
        // ATHROW should be the end of a block anyway (compiler-enforced), so doing nothing should not affect the rest of the execution
        return VEmpty.instance();
    }

    /**
     * Used in original catch blocks to extract real exceptions from VExceptions and handle them,
     * in case exceptions are handled within the same method or in outer methods.
     */
    public static V<? extends Throwable> extractVExceptionIfHandled(V<? extends Throwable> vT, String handledExceptions, FeatureExpr ctx) throws Throwable {
        String[] exps = handledExceptions.split(";");
        HashSet<String> expSet = new HashSet<>();
        for (int i = 0; i < exps.length; i++) {
            if (exps[i] != "null")
                expSet.add(exps[i]);
            else
                expSet.add("java/lang/Throwable");
        }
        FeatureExpr ctxOfVException = vT.select(ctx).when(x -> {return x instanceof VException;}, false);
        if (ctxOfVException.isContradiction())
            return vT;
        V<? extends Throwable> selected = vT.select(ctx.and(ctxOfVException));
        assert selected instanceof One : "Should have only one VException"; // cpwTODO: can we generalize this to handle multiple exceptions at a time?
        VException ve = (VException) selected.getOne();
        for (String s : expSet) {
            try {
                Class c = Class.forName(s.replace('/', '.'), false, VERuntime.classloader().get());
                if (c.isInstance(ve.e())) {
                    return V.one(ctxOfVException, ve.e());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw e;
            }
        }
        throw ve;
    }

    public static boolean isTypeOf(VException ve, String t) throws ClassNotFoundException {
        Class c = Class.forName(t.replace('/', '.'), false, VERuntime.classloader().get());
        return c.isInstance(ve.e());
    }

    public static FeatureExpr extractCtxFromVException(Throwable t) {
        if (t instanceof VException)
            return ((VException) t).ctx();
        else
            return FeatureExprFactory.True();
    }

    /**
     * Used in our try-catch to detect conditional exceptions.
     *
     * If a conditional exception is detected, we wrap it into a VException with its context and throw it.
     *
     * @cpwTODO: remove the redundant mCtx parameter (why is it redundant?)
     */
    public static void checkAndThrow(V<? extends Throwable> vT, FeatureExpr ctx, FeatureExpr mCtx) {
        V<? extends Throwable> selected = vT.select(ctx);
        assert selected instanceof One : "Is a CHOICE possible here?";
        FeatureExpr expCtx = selected.getConfigSpace();
        Throwable x = selected.getOne();
        if (x instanceof VException)
            throw (VException) x;
        else if (x instanceof ExceptionInInitializerError) {
            // exceptions thrown inside clinit are converted to ExceptionInInitializerError internally in JVM
            System.err.println("Error in <clinit>, re-execution likely to be affected");
            if (x.getCause() instanceof VException) {
                throw (VException) x.getCause();
            }
            else {
                throw new VException(x, expCtx);
            }
        }
        else
            throw new VException(x, expCtx);
    }

    public static FeatureExpr updateCurrentContextFromGlobal(FeatureExpr ctx) {
        return ctx.and(VERuntime.postponedExceptionContext().not());
    }
    /**
     * Not really used anymore.
     *
     * We keep this method in case we want to disable handling conditional exceptions later.
     */
    public static V<Object> verifyAndThrowException(V<Object> e, FeatureExpr methodCtx) throws Throwable {
        V<Object> simplifiedV = e.select(methodCtx);
        if (simplifiedV.hasThrowable()) {
            if (simplifiedV instanceof One) {
                Throwable t = (Throwable) ((One) simplifiedV).getOne();
                FeatureExpr ctx = simplifiedV.getConfigSpace();
                if (!ctx.equivalentTo(methodCtx))
                    throw new RuntimeException("An exception/error was thrown under subcontext of method");
                throw t;
            } else {
                // must be a Choice
                throw new RuntimeException("An exception/error was thrown under subcontext of method");
            }
        } else {
            return e;
        }
    }

    /**
     * TODO: Needs to check whether we are lifting this class
     */
    public static Object newInstance(Class clazz, FeatureExpr ctx) throws Throwable {
        try {
            Constructor c = clazz.getConstructor(FeatureExpr.class);
            return c.newInstance(new Object[]{ctx});
        } catch (NoSuchMethodException e) {
            System.out.println("Could not find constructor with ctx");
            Constructor cc = clazz.getConstructor();
            return cc.newInstance(new Object[]{});
        } catch (IllegalAccessException e) {
            System.out.println("Error initializing " + clazz.getName());
            throw e;
        } catch (InstantiationException e) {
            System.out.println("Error initializing " + clazz.getName());
            throw e;
        } catch (InvocationTargetException e) {
            System.out.println("Error initializing " + clazz.getName());
            throw e;
        }
    }

    public static Method getDeclaredMethod(Class clazz, String name, Class[] parameters) {
        StringBuilder sb = new StringBuilder(name);
        sb.append("__");
        for (int i = 0; i < parameters.length; i++) {
            sb.append(encodeParameter(parameters[i]) + "_");
        }
        String prefix = sb.toString();
        Method[] allMethods = clazz.getDeclaredMethods();
        Method res = null;
        for (int i = 0; i < allMethods.length; i++) {
            if (allMethods[i].getName().startsWith(prefix)) {
                if (res == null)
                    res = allMethods[i];
                else
                    throw new RuntimeException("Error in getDeclaredMethod, more than one match");
            }
        }
        if (res == null) {
            throw new RuntimeException("Error in getDeclaredMethod, method not found");
        } else {
            return res;
        }
    }

    static String encodeParameter(Class p) {
        switch(p.getCanonicalName()) {
            case "int": return "I";
            case "byte": return "B";
            case "short": return "S";
            case "long": return "J";
            case "char": return "C";
            case "float": return "F";
            case "double": return "D";
            case "boolean": return "Z";
            default:
                return "L" + p.getCanonicalName().replace('.', '_');
        }
    }

    public static Object invoke(Method m, Object obj, Object[] parameters, FeatureExpr ctx) throws Throwable {
        Object[] newParameters = new Object[parameters.length + 1];
        for (int i = 0; i < parameters.length; i++) {
            newParameters[i] = V.one(ctx, parameters[i]);
        }
        newParameters[parameters.length] = ctx;
        try {
            return m.invoke(obj, newParameters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof VException)
                throw new VException(new InvocationTargetException(((VException) e.getCause()).e()), ((VException) e.getCause()).ctx());
            else {
                System.err.println("Error in VOps.invoke:");
                e.printStackTrace();
                throw new VException(e, ctx);
            }
        } catch (IllegalArgumentException e) {
            throw new VException(e, ctx);
        }
        throw new RuntimeException("Error in invoke");
    }

    /**
     * Replacement for BeanUtilsBean.copyProperty()
     *
     * Use reflection to find the corresponding set method and then invoke it.
     */
    public static void copyProperty(BeanUtilsBean bean, Object target, String key, Object value, FeatureExpr ctx) {
        Method[] methods = target.getClass().getMethods();
        String setterName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            // Lifted classes have parameter type embedded in method names, but it shouldn't matter
            // because standard JavaBean prohibits setters with the same names but different types
            if (m.getName().startsWith(setterName)) {
                // TODO: convert value to appropriate types if necessary
                V vValue;
                if (setterName.equals("setTabWidth")) {
                    Integer iValue = Integer.valueOf((String) value);
                    vValue = V.one(ctx, iValue);
                } else {
                    vValue = V.one(ctx, value);
                }
                try {
                    m.invoke(target, vValue, ctx);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Called from the digester library
     *
     * NOTE: we assume that the digester library code is executed under the global entry context.
     */
    public static void populate(Object bean, Map<String, Object> properties) {
        FeatureExpr ctx = VERuntime.boundaryCtx();
        for (String key : properties.keySet()) {
            String methodNamePrefix = "set" + key.toUpperCase().charAt(0) + key.substring(1) + "__";
            Object value = properties.get(key);
            if (!(value instanceof String)) {
                throw new RuntimeException("Unsupported property value type: " + value.getClass());
            }
            try {
                Method[] methods = bean.getClass().getDeclaredMethods();
                boolean found = false;
                for (Method m : methods) {
                    if (m.getName().startsWith(methodNamePrefix)) {
                        found = true;
                        if (m.getName().startsWith(methodNamePrefix + "Z")) {
                            int bValue = 0;
                            if (value.equals("true")) bValue = 1;
                            m.invoke(bean, V.one(ctx, bValue), ctx);
                        } else if (m.getName().startsWith(methodNamePrefix + "I")) {
                            int iValue = Integer.valueOf((String) value);
                            m.invoke(bean, V.one(ctx, iValue), ctx);
                        } else {
                            m.invoke(bean, V.one(ctx, value), ctx);
                        }
                    }
                }
                if (!found)
                    throw new RuntimeException("No setter method for: " + key);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException("Error in VOps.populate");
            }
        }
    }

    public static Object getProperty(Object bean, String property, FeatureExpr ctx) {
        String methodPrefix = "get" + property.toUpperCase().charAt(0) + property.substring(1);
        try {
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().startsWith(methodPrefix)) {
                    return m.invoke(bean, ctx);
                }
            }
            throw new VException(new RuntimeException("no getter method for " + property), ctx);
        } catch (IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
            throw new VException(e, ctx);
        }
    }

    public static Method getMethod(Class clazz, String mName, Class[] paramTypes, FeatureExpr ctx) {
        StringBuffer mdb = new StringBuffer();
        mdb.append(mName + "__");
        for (Class c : paramTypes) {
            String model = new Owner(c.getName().replace(".", "/")).toModel().toString();
            mdb.append("L" + model.replace("/", "_") + "_");
        }
        mdb.append("_");
        String liftedNamePrefix = mdb.toString();
        Method[] allMethods = clazz.getDeclaredMethods();
        for (Method m : allMethods) {
            if (m.getName().startsWith(liftedNamePrefix)) {
                return m;
            }
        }
        throw new VException(new RuntimeException("could not find required lifted method"), ctx);
    }

    /**
     * Get the argument of parameterized type instead of V
     */
    public static Class getType(Field f) {
        Type t = f.getGenericType();
        assert t instanceof ParameterizedTypeImpl : "Not a parameterized type";
        assert ((ParameterizedTypeImpl) t).getActualTypeArguments().length == 1 :
                "Parameterized type with more than one argument";
        return (Class) ((ParameterizedTypeImpl) t).getActualTypeArguments()[0];
    }

    public static int getInt(Field f, Object obj) throws IllegalAccessException {
        try {
            Object value = f.get(obj);
            assert value instanceof One :
                    "Value of field " + f.getName() + " in " + obj + " is not V.One";
            Object actualValue = ((One) value).getOne();
            assert actualValue instanceof Integer :
                    "Actual value of field " + f.getName() + "in " + obj + " is not Integer";
            return (Integer) actualValue;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Assume we are getting constructor from a LIFTED class
     *
     * TODO: check that we are actually lifting this class
     */
    public static Constructor getConstructor(Class c, Class[] typeArgs) throws NoSuchMethodException {
        int numArgs = typeArgs.length;
        Class[] newTypeArgs = new Class[numArgs * 2 + 1];
        for (int i = 0; i < numArgs; i++) {
            newTypeArgs[i] = V.class;
            newTypeArgs[i + numArgs + 1] = typeArgs[i];
        }
        newTypeArgs[numArgs] = FeatureExpr.class;
        try {
            return c.getConstructor(newTypeArgs);
        } catch (NoSuchMethodException e) {
            System.err.println("Exception in VOps");
            throw e;
        }
    }

    public static Object newInstance(Constructor c, Object[] args, FeatureExpr ctx) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        int argsCount = args.length;
        Object[] newArgs = new Object[argsCount * 2 + 1];
        for (int i = 0; i < argsCount; i++) {
            newArgs[i] = V.one(ctx, args[i]);
            newArgs[i + args.length + 1] = null;
        }
        newArgs[argsCount] = ctx;
        try {
            return c.newInstance(newArgs);
        } catch (InstantiationException e) {
            System.err.println("Exception in VOps");
            throw e;
        } catch (IllegalAccessException e) {
            System.err.println("Exception in VOps");
            throw e;
        } catch (InvocationTargetException e) {
            System.err.println("Exception in VOps");
            throw e;
        }
    }

    public static Object monitorVerify(V<?> syncRef, FeatureExpr ctx) {
        V selected = syncRef.select(ctx);
        if (selected instanceof One) {
            Object ref = ((One) selected).getOne();
            return ref;
        } else {
            throw new RuntimeException("MONITORENTER or MONITOREXIT on a choice: " + selected.toString());
        }
    }

    public static V<? extends Integer> hashCode(Object o1, FeatureExpr ctx) {
        try {
            Method liftedHashCode = o1.getClass().getMethod("hashCode____I", FeatureExpr.class);
            liftedHashCode.setAccessible(true);
            return (V<? extends Integer>) liftedHashCode.invoke(o1, ctx);
        } catch (NoSuchMethodException e) {
            return V.one(ctx, o1.hashCode());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error in calling hashCode()");
    }

    public static V<? extends Integer> equals(Object o1, V o2, FeatureExpr ctx) {
        try {
            Method liftedEquals = o1.getClass().getMethod("equals__Ljava_lang_Object__Z", V.class, FeatureExpr.class);
            liftedEquals.setAccessible(true);
            return (V<? extends Integer>) liftedEquals.invoke(o1, o2, ctx);
        } catch (NoSuchMethodException e) {
            return o2.smap(ctx, o -> o1.equals(o));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error in calling equals()");
    }

    public static V<? extends String> toString(Object o, FeatureExpr ctx) {
        try {
            Method liftedToString = o.getClass().getMethod("toString____Ljava_lang_String", FeatureExpr.class);
            liftedToString.setAccessible(true);
            return (V<? extends String>) liftedToString.invoke(o, ctx);
        } catch (NoSuchMethodException e) {
            return V.one(ctx, o.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error in calling toString()");
    }

    public static V<?> initVStrings__Array_C__V(V<V<java.lang.Integer>[]> vA, FeatureExpr ctx) {
        return vA.sflatMap(ctx, (fe, a) -> {
            StringBuilder sb = new StringBuilder(ctx);
            for (int i = 0; i < a.length; i++) {
                sb.append__C__Lmodel_java_lang_StringBuilder(a[i], fe);
            }
            return sb.toString____Ljava_lang_String(fe);
        });
    }

    public static V<?> initVStrings__Array_C_I_I__V(V<V<java.lang.Integer>[]> vA, V<java.lang.Integer> vOffset, V<java.lang.Integer> vCount, FeatureExpr ctx) {
        return vA.sflatMap(ctx, (fe, a) -> {
            return vOffset.sflatMap(fe, (fe2, offset) -> {
                return (V) vCount.sflatMap(fe2, (fe3, count) -> {
                    StringBuilder sb = new StringBuilder(fe3);
                    for (int i = 0; i < count; i++) {
                        sb.append__C__Lmodel_java_lang_StringBuilder(a[offset + i], fe3);
                    }
                    return sb.toString____Ljava_lang_String(fe3);
                });
            });
        });
    }

    public static V[] getDeclaredFields(Class c, FeatureExpr fe) {
        Field[] fields = c.getDeclaredFields();
        V[] result = new V[fields.length];
        for (int i = 0; i < fields.length; i++) {
            result[i] = V.one(fe, fields[i]);
        }
        return result;
    }

    public static Field getDeclaredField(Class c, String fn, FeatureExpr ctx) {
        try {
            return c.getDeclaredField(fn);
        } catch (NoSuchFieldException e) {
            throw new VException(e, ctx);
        }
    }

    public static InputStream getResourceAsStream(Class c, String s, FeatureExpr ctx) {
        return c.getResourceAsStream(s);
    }

    public static void checkBlockCount(FeatureExpr ctx) throws Throwable {
        VERuntime.incrementBlockCount();
        // We throw Error to avoid exceptions being caught, such as the catchers in Monopoli
        if (VERuntime.curBlockCount() > Settings.maxBlockCount()) {
            if (VERuntime.shouldPostpone(ctx)) {
                VERuntime.postponeExceptionCtx(ctx);
            }
            else {
                VERuntime.throwExceptionCtx(ctx);
                throw new VException(new Error("Max block exceeded, potential infinite loop"), ctx);
            }
        }
    }

    public static void debug_PrintCtx(FeatureExpr ctx) {
        System.out.println(ctx);
    }

    public static HashMap<String, Stack<java.lang.StringBuilder>> traces = new HashMap<>();

    public static void log(String s) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("vtrace.txt", true));
            writer.write(s);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logStart(String methodName) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        sb.append("\n" + methodName + "\n");
        if (traces.containsKey(methodName)) {
            Stack<java.lang.StringBuilder> existing = traces.get(methodName);
            existing.push(sb);
        } else {
            Stack<java.lang.StringBuilder> s = new Stack<>();
            s.push(sb);
            traces.put(methodName, s);
        }
//        log("\n" + methodName + "\n");
    }

    public static void logBlock(String b, FeatureExpr fe, String methodName) {
        Stack<java.lang.StringBuilder> s = traces.get(methodName);
        java.lang.StringBuilder sb = s.peek();
        sb.append(b + " " + fe.toString() + "\n");
//        log(b + " " + fe.toString() + "\n");
    }

    public static void logEnd(String methodName) {
        Stack<java.lang.StringBuilder> s = traces.get(methodName);
        java.lang.StringBuilder sb = s.pop();
        log(sb.toString());
    }

    public static long nSimpleInvocations = 0;
    public static void logSimple() {
        nSimpleInvocations += 1;
    }

    public static final java.util.List<Class> unliftedCompareToList = java.util.Arrays.asList(
            Long.class,
            Integer.class,
            Short.class,
            Byte.class,
            Boolean.class,
            Character.class,
            Double.class,
            Float.class,
            String.class
    );
    public static V<?> compareTo(V<?> a, V<?> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> {
            if (unliftedCompareToList.contains((aa.getClass()))) {
                return b.smap(fe, bb -> ((Comparable) aa).compareTo((Comparable) bb));
            } else {
                try {
                    Method m = aa.getClass().getMethod("compareTo__Ljava_lang_Object__I", V.class, FeatureExpr.class);
                    m.setAccessible(true);
                    return (V) m.invoke(aa, b, fe);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error in compareTo");
                }
            }
        });
    }
}
