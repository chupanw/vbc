package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.varex.V;

/**
 * Created by lukas on 6/30/17.
 */
public class CtxListUnit {
    private static FeatureExpr T = FeatureExprFactory.True();
    private static FeatureExpr F = FeatureExprFactory.False();
    private static FeatureExpr A = FeatureExprFactory.createDefinedExternal("A");
    private static FeatureExpr B = FeatureExprFactory.createDefinedExternal("B");

    public static void main(String[] args) {
        run("Add and get", () -> test_add_get());
        run("Add and get V", () -> test_add_get_V());
        run("Size", () -> test_size());
        run("Size V", () -> test_size_V());
        run("Simplify", () -> test_simplify());
        run("Simplify V", () -> test_simplify_V());
        run("Remove", () -> test_remove());
        run("Remove V", () -> test_remove_V());
        run("RemoveIndex V", () -> test_removeIndex_V());
        run("Get V", () -> test_get_V());
        run("IndexOf V", () -> test_indexOf_V());
    }

    public static void run(String name, Runnable f) {
        System.out.println("=== Running {" + name + "} ===");
        try {
            f.run();
            System.out.println("Success");
        }
        catch (AssertionError e) {
            e.printStackTrace(System.out);
            System.out.println("\n\nFailure in {" + name + "}\n");
        }
    }
    public static void assertEq(Object a, Object b) {
        if (!a.equals(b)) {
            System.out.println("AssertionError: " + a + " != " + b);
            assert false;
        }
    }

    public static void test_add_get() {
        CtxList<Integer> list = new CtxList<>();
        list.add(1, T);
        assertEq(list.get(0, T), V.one(T, 1));
        assertEq(list.get(0, A), V.one(A, 1));
        assertEq(list.get(0, B), V.one(B, 1));
        list.add(2, T);
        assertEq(list.get(1, T), V.one(T, 2));
        assertEq(list.get(1, A), V.one(A, 2));
        assertEq(list.get(1, B), V.one(B, 2));

        list.add(3, A);
        list.add(4, A.not());
        assertEq(list.get(2, A), V.one(A, 3));
        assertEq(list.get(2, A.not()), V.one(A.not(), 4));

        list.add(5, A.or(B));
        assertEq(list.get(3, A), V.one(A, 5));
        assertEq(list.get(3, A.not()), V.choice(B, V.one(A.not(), 5), V.one(A.not(), null)));
    }

    public static void test_add_get_V() {
        CtxList<Integer> list = new CtxList<>();
        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        assertEq(list.get__I__Ljava_lang_Object(V.one(T, 0), T), V.one(T, 1));
        assertEq(list.get__I__Ljava_lang_Object(V.one(A, 0), A), V.one(A, 1));
        assertEq(list.get__I__Ljava_lang_Object(V.one(B, 0), B), V.one(B, 1));
        list.add__Ljava_lang_Object__Z(V.one(T, 2), T);
        assertEq(list.get__I__Ljava_lang_Object(V.one(T, 1), T), V.one(T, 2));
        assertEq(list.get__I__Ljava_lang_Object(V.one(A, 1), A), V.one(A, 2));
        assertEq(list.get__I__Ljava_lang_Object(V.one(B, 1), B), V.one(B, 2));

        list.add__Ljava_lang_Object__Z(V.one(A, 3), A);
        list.add__Ljava_lang_Object__Z(V.one(A.not(), 4), A.not());
        assertEq(list.get__I__Ljava_lang_Object(V.one(A, 2), A), V.one(A, 3));
        assertEq(list.get__I__Ljava_lang_Object(V.one(A.not(), 2), A.not()), V.one(A.not(), 4));

        list.add__Ljava_lang_Object__Z(V.one(A.or(B), 5), A.or(B));
        assertEq(list.get__I__Ljava_lang_Object(V.one(A, 3), A), V.one(A, 5));
        assertEq(list.get__I__Ljava_lang_Object(V.one(A.not(), 3), A.not()),
                V.choice(B, V.one(A.not(), 5), V.one(A.not(), null)));
    }

    public static void test_size() {
        CtxList<Integer> list = new CtxList<>();
        list.add(1, T);
        assertEq(list.size(), 1);
        list.add(2, T);
        assertEq(list.size(), 2);
        list.add(3, T);
        assertEq(list.size(), 3);
    }
    public static void test_size_V() {
        CtxList<Integer> list = new CtxList<>();
        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        assertEq(list.size____I(T), V.one(T, 1));

        list.add__Ljava_lang_Object__Z(V.one(T, 2), T);
        assertEq(list.size____I(T), V.one(T, 2));

        list.add__Ljava_lang_Object__Z(V.one(A, 3), A);
        assertEq(list.size____I(T), V.choice(A, 3, 2));

        list.add__Ljava_lang_Object__Z(V.one(B, 4), B);
        assertEq(list.size____I(T), V.choice(A, V.choice(B, 4, 3), V.choice(B, 3, 2)));
    }

    public static void test_simplify() {
        CtxList<Integer> list = new CtxList<>();
        list.add(1, T);
        list.add(2, A);
        list.add(2, A.not());
        list.add(3, B);
        assertEq(list.size(), 4);
        list.simplify____V();
        assertEq(list.size(), 3);

        list.add(4, F);
        assertEq(list.size(), 4);
        list.simplify____V();
        assertEq(list.size(), 3);
    }
    public static void test_simplify_V() {
        CtxList<Integer> list = new CtxList<>();
        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        list.add__Ljava_lang_Object__Z(V.one(A, 2), A);
        list.add__Ljava_lang_Object__Z(V.one(A.not(), 2), A.not());
        list.add__Ljava_lang_Object__Z(V.one(B, 3), B);
        assertEq(list.size____I(T), V.choice(B, 3, 2));
        assertEq(list.size(), 4);
        list.simplify____V();
        assertEq(list.size____I(T), V.choice(B, 3, 2));
        assertEq(list.size(), 3);

        list.add__Ljava_lang_Object__Z(V.one(F, 4), F);
        assertEq(list.size____I(T), V.choice(B, 3, 2));
        assertEq(list.size(), 3);
        list.simplify____V();
        assertEq(list.size____I(T), V.choice(B, 3, 2));
        assertEq(list.size(), 3);
    }

    public static void test_get_V() {
        CtxList<Integer> list = new CtxList<>();
        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        list.add__Ljava_lang_Object__Z(V.one(A, 2), A);
        list.add__Ljava_lang_Object__Z(V.one(A.or(B), 3), A.or(B));
        list.add__Ljava_lang_Object__Z(V.one(B, 4), B);
        assertEq(list.get__I__Ljava_lang_Object(V.one(T, 0), T), V.one(T, 1));
        assertEq(list.get__I__Ljava_lang_Object(V.one(T, 1), T),
                V.choice(A, V.one(A, 2), V.choice(B, 3, null)));
        // fails with equivalent ctxs? Seems like the expression isn't being simplified properly.
        // !A ^ B ^ (A v B) <=> (!A ^ B ^ A) v (!A ^ B ^ B) <=> 0 v (!A ^ B) <=> !A ^ B
        assertEq(list.get__I__Ljava_lang_Object(V.one(B, 1), B),
                V.choice(A, V.one(B, 2), V.one(B, 3)));
    }

    public static void test_indexOf_V() {
        CtxList<Integer> list = new CtxList<>();
        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        list.add__Ljava_lang_Object__Z(V.one(A, 2), A);
        list.add__Ljava_lang_Object__Z(V.one(A.or(B), 3), A.or(B));
        list.add__Ljava_lang_Object__Z(V.one(B, 4), B);
        list.add__Ljava_lang_Object__Z(V.one(B, 2), B);

        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 1), T),
                V.one(T, 0));
        // fails with equivalent ctxs? Seems like the expression isn't being simplified properly.
        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 2), T),
                V.choice(A, V.one(A, 1), V.choice(B, 3, -1)));
        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 3), T),
                V.choice(A, V.one(A, 2), V.choice(B, 1, -1)));
    }


    public static void test_remove() {
        CtxList<Integer> list = new CtxList<>();
        list.add(1, T);
        list.remove(1, T);
        list.simplify____V();
        assertEq(list.size(), 0);
        list.add(1, T);
        list.remove(1, A);
        list.simplify____V();
        assertEq(list.size(), 1);
        list.remove(1, A.not());
        list.simplify____V();
        assertEq(list.size(), 0);
    }
    public static void test_remove_V() {
        CtxList<Integer> list = new CtxList<>();
        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        list.remove__Ljava_lang_Object__Z(V.one(T, 1), T);
        list.simplify____V();
        assertEq(list.size____I(T), V.one(T, 0));

        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        list.remove__Ljava_lang_Object__Z(V.one(A, 1), A);
        list.simplify____V();
        assertEq(list.size____I(T), V.choice(A, 0, 1));
        assertEq(list.get__I__Ljava_lang_Object(V.one(T, 0), T),
                V.choice(A, null, 1));
        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 1), T),
                V.choice(A, -1, 0));

        list.remove__Ljava_lang_Object__Z(V.one(B, 1), B);
        assertEq(list.size____I(T), V.choice(A.or(B), 0, 1));
        assertEq(list.get__I__Ljava_lang_Object(V.one(T, 0), T),
                V.choice(A.or(B), null, 1));
        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 1), T),
                V.choice(A.or(B), -1, 0));

        list.remove__Ljava_lang_Object__Z(V.one(A.not(), 1), A.not());
        list.simplify____V();
        assertEq(list.size____I(T), V.one(T, 0));
        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 1), T),
                V.one(T, -1));
    }
    public static void test_removeIndex_V() {
        CtxList<Integer> list = new CtxList<>();
        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        list.remove__I__Ljava_lang_Object(V.one(T, 0), T);
        list.simplify____V();
        assertEq(list.size____I(T), V.one(T, 0));

        list.add__Ljava_lang_Object__Z(V.one(T, 1), T);
        list.remove__I__Ljava_lang_Object(V.one(A, 0), A);
        list.simplify____V();
        assertEq(list.size____I(T), V.choice(A, 0, 1));
        assertEq(list.get__I__Ljava_lang_Object(V.one(T, 0), T),
                V.choice(A, null, 1));
        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 1), T),
                V.choice(A, -1, 0));

        list.remove__I__Ljava_lang_Object(V.one(B, 0), B);
        assertEq(list.size____I(T), V.choice(A.or(B), 0, 1));
        assertEq(list.get__I__Ljava_lang_Object(V.one(T, 0), T),
                V.choice(A.or(B), null, 1));
        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 1), T),
                V.choice(A.or(B), -1, 0));

        list.remove__I__Ljava_lang_Object(V.one(A.not(), 0), A.not());
        list.simplify____V();
        assertEq(list.size____I(T), V.one(T, 0));
        assertEq(list.indexOf__Ljava_lang_Object__I(V.one(T, 1), T),
                V.one(T, -1));
    }
}
