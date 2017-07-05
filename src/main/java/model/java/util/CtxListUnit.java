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
        run("Add V", () -> test_add_V());
        run("Length", () -> test_length());
        run("Simplify", () -> test_simplify());
        run("Remove", () -> test_remove());
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
        list.add(T, 1);
        assertEq(list.get(T, 0), 1);
        assertEq(list.get(A, 0), 1);
        assertEq(list.get(B, 0), 1);
        list.add(T, 2);
        assertEq(list.get(T, 1), 2);
        assertEq(list.get(A, 1), 2);
        assertEq(list.get(B, 1), 2);

        list.add(A, 3);
        list.add(A.not(), 4);
        assertEq(list.get(A, 2), 3);
        assertEq(list.get(A.not(), 2), 4);

        list.add(A.or(B), 5);
        assertEq(list.get(A, 3), 5);
        assertEq(list.get(A.not(), 3), 5);
    }

    public static void test_add_V() {
        CtxList<Integer> list = new CtxList<>();
        list.add(V.choice(A, 1, 2));
        assertEq(list.length(), 2);
        assertEq(list.get(A, 0), 1);
        assertEq(list.get(A.not(), 0), 2);
    }

    public static void test_length() {
        CtxList<Integer> list = new CtxList<>();
        list.add(T, 1);
        assertEq(list.length(), 1);
        list.add(T, 2);
        assertEq(list.length(), 2);
        list.add(T, 3);
        assertEq(list.length(), 3);
    }

    public static void test_simplify() {
        CtxList<Integer> list = new CtxList<>();
        list.add(T, 1);
        list.add(A, 2);
        list.add(A.not(), 2);
        list.add(B, 3);
        assertEq(list.length(), 4);
        list.simplify();
        assertEq(list.length(), 3);

        list.add(F, 4);
        assertEq(list.length(), 4);
        list.simplify();
        assertEq(list.length(), 3);
    }

    public static void test_remove() {
        CtxList<Integer> list = new CtxList<>();
        list.add(T, 1);
        list.remove(T, 1);
        list.simplify();
        assertEq(list.length(), 0);
        list.add(T, 1);
        list.remove(A, 1);
        list.simplify();
        assertEq(list.length(), 1);
        list.remove(A.not(), 1);
        list.simplify();
        assertEq(list.length(), 0);
    }
}
