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
        new IterationBenchmark().runSimple();
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
