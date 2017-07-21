package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.LinkedList;

public class IterationBenchmark {
    @VConditional
    static boolean A;
    @VConditional
    static boolean B;
    @VConditional
    static boolean C;
    @VConditional
    static boolean D;

    public static void main(String[] args) {
        IterationBenchmark ie = new IterationBenchmark();
        ie.runSimple();
        System.out.println("\n~~~\n");
        ie.run();
        System.out.println("\n~~~\n");
        ie.checkStyle();
    }

    public void run() {
        LinkedList<Integer> l = new LinkedList<>();
        l.add(1);
        if (A) {
            l.add(2);
            l.add(3);
        }
        else
            l.add(3);

        if (D)
            l.add(4);

        if (B)
            l.add(4);

        l.add(5);

        if (!B)
            l.add(76);
        else if (C)
            l.add(77);
        else
            l.add(60);

        if (A)
            if (B)
                l.add(20);
            else if (D)
                l.add(21);

        if (C)
            l.add(22);
        else
            l.add(22);

        l.add(30);
        l.add(30);

        if (D)
            if (A)
                if (!C)
                    l.add(7);
                else if (B)
                    l.add(7);
                else
                    l.add(7);
            else
                l.add(7);
        else
            l.add(7);

        int res = 0;
        for (Integer el : l) {
            System.out.println(el);
            res = res + el;
        }
        System.out.println("=== sum: ===");
        System.out.println(res);
        res = res / l.size();
        System.out.println("=== len: ===");
        System.out.println(l.size());
        System.out.println("=== avg: ===");
        System.out.println(res);
    }

    public void runSimple() {
        LinkedList<Integer> l = new LinkedList<>();
        l.add(1);
        if (A) {
            l.add(2);
            l.add(3);
        }
        else
            l.add(3);

        if (B)
            l.add(4);

        l.add(5);

        if (!B)
            l.add(76);

        int res = 0;
        for (Integer el : l) {
            System.out.println(el);
            res = res + el;
        }
        System.out.println("=== sum: ===");
        System.out.println(res);
    }

    @VConditional
    static boolean E1;
    @VConditional
    static boolean E2;
    @VConditional
    static boolean E3;
    @VConditional
    static boolean E4;
    @VConditional
    static boolean E5;
    @VConditional
    static boolean E6;
    @VConditional
    static boolean E7;
    @VConditional
    static boolean E8;
    @VConditional
    static boolean E9;
    @VConditional
    static boolean E10;

    public void checkStyle() {
        LinkedList<Integer> l = new LinkedList<>();

        if (E1)
            l.add(1);
        if (E2)
            l.add(1);
        if (E3)
            l.add(1);
        if (E4)
            l.add(1);
        if (E5)
            l.add(1);
        if (E6)
            l.add(1);
        if (E7)
            l.add(1);
        if (E8)
            l.add(1);
        if (E9)
            l.add(1);
        if (E10)
            l.add(1);

        int sum = 0;
        for (Integer el : l) {
            System.out.println(el);
            sum += el;
        }
        System.out.println("=== sum: ===");
        System.out.println(sum);
    }
}
