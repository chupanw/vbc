package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.io.IOException;

/**
 * @author chupanw
 */
public class ExceptionExample {
    @VConditional
    boolean A;
    @VConditional
    boolean B;
    @VConditional
    boolean C;
    @VConditional
    boolean numZero;

    int num;

    public ExceptionExample() {
        if (numZero)
            num = 0;
        else
            num = 10;
    }

    /**
     * Due to re-execution under smaller contexts, methods in the beginning
     * might mask the behaviors of latter methods.
     *
     * Uncomment any specific *SINGLE* method for debugging
     *
     * @param args
     */
    public static void main(String[] args) {
        ExceptionExample example = new ExceptionExample();
//        example.noThrow();
//        example.noThrow2();
        example.throwInMethod();
//        example.throwInMethod2();
//        example.implicitThrow();
//        example.shouldCatchSameMethod();
//        example.shouldCatchInMethod();
//        example.shouldCatchMultiExceptionSameMethod();
//        example.shouldCatchMultiExceptionInMethod();
//        example.shouldCatchMultiExceptionSameMethodSameCatch();
//        example.shouldCatchMultiExceptionInMethodSameCatch();
//        example.shouldNotCatchSameMethod();
//        example.shouldNotCatchInMethod();
//        example.shouldCatchSameMethodWithFinally();
//        example.shouldCatchInMethodWithFinally();
//        example.exceptionWithField();
    }

    // in fact we are returning a One(null) to indicate no exception
    public void noThrow() {
        System.out.println("no exception");
    }

    public void noThrow2() {
        if (A) {
            System.out.println("A is true");
            return;
        }
        System.out.println("A is false");
    }

    public void throwInMethod() {
        if (B)
            conditionallyThrow();
        else
            simplyThrow();
        System.out.println("end of throwInMethod");
    }

    public void throwInMethod2() {
        if (B)
            conditionallyThrow();
        else
            implicitThrow();
        System.out.println("no exception");
    }

    public void exceptionWithField() {
        try {
            if (A)
                throw new RuntimeException("exception under A");
            else {
                implicitThrow2();
            }
        } catch (ArithmeticException e) {
            System.out.println("ArithmeticException caught under A");
        }
        System.out.println("end of method");
    }

    public void shouldCatchSameMethod() {
        try {
            if (A)
                throw new RuntimeException("exception under A");
            else {
                System.out.println("no exception if !A");
            }
        } catch (RuntimeException e) {
            System.out.println("exception caught under A");
        }
    }

    public void shouldCatchInMethod() {
        if (A) {
            try {
                implicitThrow();
            } catch (ArithmeticException e) {
                System.out.println("caught " + e);
            }
        } else {
            implicitThrow();
        }
        System.out.println("no exception");
    }


    public void shouldCatchMultiExceptionSameMethod() {
        if (A) {
            try {
                if (B)
                    throw new ArithmeticException("/ by zero");
                else if (C)
                    throw new ArrayIndexOutOfBoundsException("-1");
                else
                    throw new IOException("file not exist");
            } catch (ArithmeticException e) {
                System.out.println("caught ArithmeticException: " + e);
            } catch (IOException e) {
                System.out.println("caught IOException: " + e);
            }
        } else {
            System.out.println("no exception");
        }
    }

    public void shouldCatchMultiExceptionSameMethodSameCatch() {
        if (A) {
            try {
                if (B)
                    throw new ArithmeticException("/ by zero");
                else if (C)
                    throw new ArrayIndexOutOfBoundsException("-1");
                else
                    throw new IOException("file not exist");
            } catch (ArithmeticException | IOException e) {
                System.out.println("caught ArithmeticException or IOException: " + e);
            }
        } else {
            System.out.println("no exception");
        }
    }

    public void shouldCatchMultiExceptionInMethod() {
        if (A) {
            try {
                if (B)
                    throwArithmeticException();
                else if (C)
                    throwArrayIndexOutOfBoundsException();
                else
                    throwIOException();
            } catch (ArithmeticException e) {
                System.out.println("caught ArithmeticException: " + e);
            } catch (IOException e) {
                System.out.println("caught IOException: " + e);
            }
        } else {
            System.out.println("no exception");
        }
    }

    public void shouldCatchMultiExceptionInMethodSameCatch() {
        if (A) {
            try {
                if (B)
                    throwArithmeticException();
                else if (C)
                    throwArrayIndexOutOfBoundsException();
                else
                    throwIOException();
            } catch (ArithmeticException | IOException e) {
                System.out.println("caught ArithmeticException or IOException: " + e);
            }
        } else {
            System.out.println("no exception");
        }
    }

    public void shouldNotCatchSameMethod() {
        try {
            if (A)
                throw new RuntimeException("exception under A");
            else {
                System.out.println("no exception if !A");
            }
        } catch (ArithmeticException e) {
            System.out.println("exception caught under A");
        }
    }

    public void shouldNotCatchInMethod() {
        if (A) {
            try {
                implicitThrow();
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("caught " + e);
            }
        } else {
            implicitThrow();
        }
        System.out.println("no exception");
    }

    public void shouldCatchSameMethodWithFinally() {
        try {
            if (A)
                throw new RuntimeException("exception under A");
            else {
                System.out.println("no exception if !A");
            }
        } catch (RuntimeException e) {
            System.out.println("exception caught under A");
        } finally {
            System.out.println("finally");
        }
    }

    public void shouldCatchInMethodWithFinally() {
        if (A) {
            try {
                implicitThrow();
            } catch (ArithmeticException e) {
                System.out.println("caught " + e);
            } finally {
                System.out.println("finally");
            }
        } else {
            implicitThrow();
        }
        System.out.println("no exception");
    }

    //////////////////////////////////////////////////
    // Helper methods
    //////////////////////////////////////////////////
    private void throwArithmeticException() { throw new ArithmeticException("/ by zero"); }
    private void throwArrayIndexOutOfBoundsException() { throw new ArrayIndexOutOfBoundsException("-1"); }
    private void throwIOException() throws IOException { throw new IOException("file not exist"); }
    private int implicitThrow() {
        int a = 10;
        if (C)
            return a / 0;
        else
            return a / 2;
    }
    private int implicitThrow2() {
        if (C)
            return 1 / num;
        else
            return 1 + num;
    }
    private void conditionallyThrow() {
        if (A) {
            throw new RuntimeException("exception if A");
        }
        System.out.println("no exception if !A");
    }
    private void simplyThrow() {
        throw new RuntimeException("exception");
    }
}
