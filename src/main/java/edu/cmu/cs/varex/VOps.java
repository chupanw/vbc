package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import org.apache.commons.beanutils.BeanUtilsBean;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.PrintStream;
import java.lang.reflect.*;

/**
 * Created by ckaestne on 1/16/2016.
 */
public class VOps {

    public static V<? extends Integer> IADD(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        System.out.print("Adding sum: ");
        System.out.println(a.toString());
        System.out.print("with el: ");
        System.out.println(b.toString());
        System.out.print("In ctx: ");
        System.out.println(ctx.toString() + "\n");
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() + bb.intValue()));
    }

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

    public static FeatureExpr whenIEQ(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
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

    public static FeatureExpr whenILE(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
        return whenLE(sub);
    }

    public static FeatureExpr whenINE(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
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
                        if (aa.intValue() > 0 && bb.intValue() < 0)
                           return 1;
                        else if (aa.intValue() < 0 && bb.intValue() > 0)
                            return -1;
                        else
                            return aa.intValue() - bb.intValue();
                    }
                });
        });
    }

    public static V<? extends Integer> ISUB(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() - bb.intValue()));
    }


    public static V<? extends Integer> IMUL(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() * bb.intValue()));
    }

    public static V<? extends Integer> IDIV(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() / bb.intValue()));
    }

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

    public static V<? extends Integer> iushr(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 >>> v2));
    }

    public static V<? extends Integer> irem(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.intValue() % v2.intValue()));
    }

    public static V<? extends Integer> ior(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.intValue() | v2.intValue()));
    }

    public static V<? extends Integer> iand(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.intValue() & v2.intValue()));
    }

    public static V<? extends Integer> ixor(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 ^ v2));
    }

    public static V<? extends Long> ldiv(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() / v2.longValue()));
    }

    public static V<? extends Integer> l2i(V<? extends Long> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) x.longValue());
    }

    public static V<? extends Integer> i2b(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) (byte) x.intValue());
    }

    public static V<? extends Integer> i2s(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) (short) x.intValue());
    }

    public static V<? extends Long> i2l(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (long) x.intValue());
    }

    public static V<? extends Long> ladd(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() + v2.longValue()));
    }

    public static V<? extends Long> land(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() & v2.longValue()));
    }

    public static V<? extends Long> lushr(V<? extends Long> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() >>> v2.intValue()));
    }

    public static V<? extends Long> lsub(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() - v2.longValue()));
    }

    public static V<? extends Integer> ineg(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, v -> -v.intValue());
    }

    public static V<? extends Double> dmul(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() * v2.doubleValue()));
    }

    public static V<? extends Integer> dcmpl(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> {
            if (v1.isNaN() || v2.isNaN()) return -1;
            else if (v1.doubleValue() > v2.doubleValue()) return 1;
            else if (v1.doubleValue() == v2.doubleValue()) return 0;
            else return -1;
        }));
    }

    public static V<? extends Long> lmul(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() * v2.longValue()));
    }

    public static V<? extends Long> lor(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() | v2.longValue()));
    }

    public static V<? extends Long> lshl(V<? extends Long> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() << v2.intValue()));
    }

    public static V<? extends Long> lxor(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() ^ v2.longValue()));
    }

    public static V<? extends Double> l2d(V<? extends Long> value1, FeatureExpr ctx) {
        return value1.smap(ctx, l -> (double) l.longValue());
    }

    public static V<? extends Double> i2d(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, i -> (double) i.intValue());
    }

    public static V<? extends Float> d2f(V<? extends Double> value1, FeatureExpr ctx) {
        return value1.smap(ctx, d -> (float) d.doubleValue());
    }

    public static V<? extends Double> f2d(V<? extends Float> value1, FeatureExpr ctx) {
        return value1.smap(ctx, f -> (double) f.floatValue());
    }

    public static V<? extends Integer> dcmpg(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> {
            if (v1.isNaN() || v2.isNaN()) return 1;
            else if (v1.doubleValue() > v2.doubleValue()) return 1;
            else if (v1.doubleValue() == v2.doubleValue()) return 0;
            else return -1;
        }));
    }

    public static V<? extends Double> ddiv(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() / v2.doubleValue()));
    }

    public static V<? extends Float> i2f(V<? extends Integer> value, FeatureExpr ctx) {
        return value.smap(ctx, i -> (float) i.intValue());
    }

    public static V<? extends Float> fdiv(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.floatValue() / v2.floatValue()));
    }

    public static V<? extends Integer> f2i(V<? extends Float> value, FeatureExpr ctx) {
        return value.smap(ctx, f -> (int) f.floatValue());
    }

    public static V<? extends Integer> fcmpg(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> {
            if (v1.isNaN() || v2.isNaN()) return 1;
            else if (v1.floatValue() > v2.floatValue()) return 1;
            else if (v1.floatValue() == v2.floatValue()) return 0;
            else return -1;
        }));
    }

    public static V<? extends Integer> fcmpl(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> {
            if (v1.isNaN() || v2.isNaN()) return -1;
            else if (v1.floatValue() > v2.floatValue()) return 1;
            else if (v1.floatValue() == v2.floatValue()) return 0;
            else return -1;
        }));
    }

    public static V<? extends Float> fmul(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.floatValue() * v2.floatValue()));
    }

    public static V<? extends Long> d2l(V<? extends Double> value, FeatureExpr ctx) {
        return value.smap(ctx, v -> (long) v.doubleValue());
    }

    public static V<? extends Double> dadd(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() + v2.doubleValue()));
    }

    public static V<? extends Long> lrem(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() % v2.longValue()));
    }

    public static V<? extends Float> fadd(V<? extends Float> value1, V<? extends Float> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.floatValue() + v2.floatValue()));
    }

    public static V<? extends Long> lshr(V<? extends Long> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() >> v2.intValue()));
    }

    public static V<? extends Double> dsub(V<? extends Double> value1, V<? extends Double> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.doubleValue() - v2.doubleValue()));
    }

    public static V<? extends Integer> d2i(V<? extends Double> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) x.doubleValue());
    }

    //////////////////////////////////////////////////
    // Special println that prints configuration as well
    //////////////////////////////////////////////////
    public static void println(PrintStream out, String s, FeatureExpr ctx) {
        out.println(s + " [" + ctx + "]");
    }
    public static void println(PrintStream out, int i, FeatureExpr ctx) {
        out.println(i + " [" + ctx + "]");
    }
    public static void println(PrintStream out, Object o, FeatureExpr ctx) {
        out.println(o + " [" + ctx + "]");
    }
    public static void println(PrintStream out, char c, FeatureExpr ctx) {
        out.println(c + " [" + ctx + "]");
    }
    public static void println(PrintStream out, boolean b, FeatureExpr ctx) {
        out.println(b + " [" + ctx + "]");
    }
    public static void println(PrintStream out, FeatureExpr ctx) {
        out.println(" [" + ctx + "]");
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

    public static V<Object> verifyAndThrowException(V<Object> e, FeatureExpr methodCtx) throws Throwable {
        V<Object> simplifiedV = e.simplified();
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
            sb.append("L" + parameters[i].getCanonicalName().replace('.', '_') + "_");
        }
        String prefix = sb.toString();
        Method[] allMethods = clazz.getMethods();
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

    public static Object invoke(Method m, Object obj, Object[] parameters, FeatureExpr ctx) {
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
            e.printStackTrace();
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
}
