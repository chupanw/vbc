package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

/**
 * @author chupanw
 */
public class TernaryOperatorExample {
    static boolean A;
    @VConditional
    static boolean B;
    static boolean C;
    @VConditional
    static boolean D;

    public static void main(String[] args) {
        TernaryOperatorExample example = new TernaryOperatorExample();
        example.testConstructor();
        example.testVConstructor();
        example.testMultiParameters();
        example.testMultiVParameters();
        example.testMultiConstructors();
        example.testMultiVConstructors();
        example.testNestedConstructors();
        example.testNestedVConstructors();
    }

    private void testConstructor() {
        Bar bar = new Bar(A ? 1 : 2);
        System.out.println(bar.toString());
    }

    private void testVConstructor() {
        Bar bar = new Bar(B ? 1 : 2);
        System.out.println(bar.toString());
    }

    private void testMultiParameters() {
        Foo foo = new Foo(A ? 0 : 1, C ? 2 : 3);
        System.out.println(foo.toString());
    }

    private void testMultiVParameters() {
        Foo foo = new Foo(B ? 0 : 1, D ? 2 : 3);
        System.out.println(foo.toString());
    }

    private void testMultiConstructors() {
        Foo foo = new Foo(A ? 0 : 1, C ? 2 : 3);
        Bar bar = new Bar(A ? 1 : 2);
        System.out.println(foo.toString());
        System.out.println(bar.toString());
    }

    private void testMultiVConstructors() {
        Foo foo = new Foo(B ? 0 : 1, D ? 2 : 3);
        Bar bar = new Bar(B ? 1 : 2);
        System.out.println(foo.toString());
        System.out.println(bar.toString());
    }

    private void testNestedConstructors() {
        FooBar foobar = new FooBar(A ? 0 : 1, new Bar(C ? 2 : 3));
        System.out.println(foobar.toString());
    }

    private void testNestedVConstructors() {
        FooBar foobar = new FooBar(B ? 0 : 1, new Bar(D ? 2 : 3));
        System.out.println(foobar.toString());
    }
}

class Foo {
    private int v1;
    private int v2;
    Foo(int a, int b) {
        v1 = a;
        v2 = b;
    }
    @Override
    public String toString() {
        return v1 + ":" + v2;
    }
}

class Bar {
    int v;
    Bar(int a) {
        v = a;
    }
    @Override
    public String toString() {
        return Integer.valueOf(v).toString();
    }
}

class FooBar {
    private int v1;
    private Bar v2;
    FooBar(int a, Bar b) {
        v1 = a;
        v2 = b;
    }
    @Override
    public String toString() {
        return "FooBar{" +
                "v1=" + v1 +
                ", v2=" + v2.toString() +
                '}';
    }
}