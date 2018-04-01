package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

/**
 * @author chupanw
 */
public class ExceptionExample {
    @VConditional
    boolean A = false;

    public static void main(String[] args) throws Exception {
        ExceptionExample example = new ExceptionExample();
        example.noThrow();
        example.noThrow2();
//        example.simpleThrow();    // partial exception is not supported yet
    }

    // in fact we are returning a One(null) to indicate no exception
    private void noThrow() {
        System.out.println("no exception");
    }

    private void noThrow2() {
        if (A) {
            System.out.println("A is true");
            return;
        }
        System.out.println("A is false");
    }

    private void simpleThrow() throws Exception {
        if (A) {
            throw new Exception("A is true");
        }
        System.out.println("A is false");
    }
}
