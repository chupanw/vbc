package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.Scanner;

public class ScannerExample {
    @VConditional public static boolean A;
    @VConditional public static boolean B;
    @VConditional public static boolean C;

    public static void main(String[] args) {
        ScannerExample example = new ScannerExample();
        example.foo();
    }

    /**
     * 1 [A]
     * 1 [!A & B]
     * 2 [A & B]
     * 1 [!A & !B & C]
     * 2 [(!A & B & C) | (A & !B & C)]
     * 3 [A & B & C]
     */
    void foo() {
        Scanner s = new Scanner("1 2 3");
        if (A)
            System.out.println(s.nextInt());
        if (B)
            System.out.println(s.nextInt());
        if (C)
            System.out.println(s.nextInt());
    }
}
