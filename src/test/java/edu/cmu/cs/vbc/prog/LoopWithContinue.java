package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

/**
 * A concrete example for the optimality discussion.
 *
 * Use Oracle JDK 1.8.0_161 to observe the effects. Use with VBlock logging
 * and {@link edu.cmu.cs.vbc.utils.TraceExpander} to observe optimality.
 */
public class LoopWithContinue {
    @VConditional
    static boolean A = true;
    @VConditional
    static boolean B = true;
    public static void main(String[] args) {
        int a, b;
        if (A) {
            a = 2; b = 2;
        } else {
            a = 1; b = 1;
        }
        foo(a, b);
        if (B) {
            a = 1; b = 2;
        } else {
            a = 2; b = 1;
        }
        foo(a, b);
    }


    /**
     * This is an example where our VBlock ordering can be optimal or
     * sub-optimal, depending on inputs.
     *
     * Not optimal:
     *  a = 2, b = 3
     *  a = 1, b = 1
     * Optimal:
     *  a = 1, b = 2
     *  a = 2, b = 1
     */
    public static void foo(int a, int b) {
        int i = 0;
        while (i < b) {
            i++;
            if (i != a)
                continue;
            System.out.println("hello");
        }
    }
}
