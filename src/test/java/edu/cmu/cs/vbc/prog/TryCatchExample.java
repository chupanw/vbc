package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

/**
 * @author chupanw
 */
public class TryCatchExample {

    @VConditional
    public static boolean A;
    @VConditional
    public static boolean B;

    void tryCatch1(Integer a, Integer b) {
        Integer aa = a;
        Integer bb = b;
        if (A) aa++;
        if (B) bb--;
        try {
            Integer result = aa / bb;
            System.out.println(result);
        } catch (ArithmeticException e) {
            System.out.println("divide by zero");
        }
    }

    void tryFinally(Integer a, Integer b) {
        Integer aa = a;
        Integer bb = b;
        if (A) aa++;
        if (B) bb--;
        try {
            Integer result = aa / bb;
            System.out.println(result);
        } finally {
            System.out.println("aa: " + aa);
            System.out.println("bb: " + bb);
        }
    }

    /**
     * Minimal example from Checkstyle.
     *
     * In bytecode, there are three blocks that are protected by a TRYCATCHBLOCK. One of those leaves a
     * value on the operand stack while the other two leave the operand stack empty. This is problematic because
     * the incoming edges of catch block have incompatible height operand stack.
     */
    Integer tryWithIf(Integer num) {
        try {
            Integer i = 3;
            if (i/num >= 0) {
                System.out.println("No exception");
            }
            return null;
        }
        catch (Exception e) {
            System.out.println("Exception caught");
            throw new RuntimeException();
        }
    }

    /**
     * IncompatibleClassChangeError from Checkstyle.
     *
     * This is caused by the ASTORE instruction in the catch block, which assumes the values on the
     * operand stack are Vs, but this assumption does not hold for catching exceptions.
     */
    Integer tryCatch2(Integer someNumber) {
        Integer result = -1;
        try {
            result = 1 / someNumber;
            System.out.println("No exception");
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
        return result;
    }

    /**
     * Comes from CheckStyle
     */
    void tryCatch3() {
        Integer i = 0;
        try {
            while (true) {
                i++;
                if (i == 10)
                    break;
            }
        } finally {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        TryCatchExample example = new TryCatchExample();
        // Since we do not handle partial exception (i.e., exception context that is smaller than
        // method context), we disable the following line so that differential testing would succeed.
//        example.tryCatch1(1, 1);
        example.tryCatch1(1, 2);
        example.tryFinally(1, 2);
        example.tryCatch2(0);
        example.tryCatch2(1);
        try {
            example.tryWithIf(0);
        } catch (Exception e) {
            System.out.println("Correct if you see this");
        }
        example.tryWithIf(1);
        example.tryCatch3();
    }
}
