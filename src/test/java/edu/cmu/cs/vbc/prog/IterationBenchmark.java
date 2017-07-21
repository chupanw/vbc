package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.LinkedList;

public class IterationBenchmark {
    @VConditional
    boolean A;
    @VConditional
    boolean B;
    @VConditional
    boolean C;
    @VConditional
    boolean D;

    public static void main(String[] args) {
        IterationBenchmark ie = new IterationBenchmark();
        ie.runSimple();
        System.out.println("\n~~~\n");
        ie.run();
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
}
