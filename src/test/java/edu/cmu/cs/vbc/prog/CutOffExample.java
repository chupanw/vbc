package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

public class CutOffExample {
    @VConditional public static boolean A;
    @VConditional public static boolean B;
    @VConditional public static boolean C;
    @VConditional public static boolean D;
    @VConditional public static boolean E;

    public static void main(String[] args) {
        CutOffExample example = new CutOffExample();
        example.foo();
//        example.foo2();
    }

    void foo() {
        int i = 0;
        if (A)
            i += 1;
        if (B)
            i += 10;
        if (C) {
            i += 100;
            if (D)
                i += 1000;
        }
        if (E)
            i += 10000;
        System.out.println(i);
    }

    void foo2() {
        int i = 0;
        if (A)
            i += 1;
        if (B)
            i += 10;
        if (C) {
            i += 100;
            if (D)
                i += 1000;
        }
        if (E)
            i += 10000;
        if (i > 100) throw new RuntimeException("exceeded 100");
        System.out.println(i);
    }
}
