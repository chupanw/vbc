package edu.cmu.cs.vbc.prog.triangle;

public class SchemataLibMethods {

    //AOR
//plus
    public static int AOR_plus(int left, int right, boolean... mutants) {

        return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left - right) : (left + right)))));
    }

    public static double AOR_plus(double left, double right, boolean... mutants) {

        return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left - right) : (left + right)))));
    }

    //minus
    public static int AOR_minus(int left, int right, boolean... mutants) {

        return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left + right) : (left - right)))));
    }

    public static double AOR_minus(double left, double right, boolean... mutants) {

        return (mutants[3] ? (left % right) : (mutants[2] ? (left / right) : (mutants[1] ? (left * right) : (mutants[0] ? (left + right) : (left - right)))));
    }

//multiply

//divide

//remainder


    //ROR
    public static boolean ROR_equals(int left, int right, boolean... mutants) {

        return (mutants[4] ? (left <= right) : (mutants[3] ? (left >= right) : (mutants[2] ? (left < right) : (mutants[1] ? (left > right) : (mutants[0] ? (left != right) : (left == right))))));
    }

    public static boolean ROR_not_equals(int left, int right, boolean... mutants) {

        return (mutants[4] ? (left <= right) : (mutants[3] ? (left >= right) : (mutants[2] ? (left < right) : (mutants[1] ? (left > right) : (mutants[0] ? (left == right) : (left != right))))));
    }

    public static boolean ROR_greater(int left, int right, boolean... mutants) {

        return (mutants[4] ? (left <= right) : (mutants[3] ? (left >= right) : (mutants[2] ? (left < right) : (mutants[1] ? (left == right) : (mutants[0] ? (left != right) : (left > right))))));
    }

    public static boolean ROR_less(int left, int right, boolean... mutants) {

        return (mutants[4] ? (left <= right) : (mutants[3] ? (left >= right) : (mutants[2] ? (left == right) : (mutants[1] ? (left > right) : (mutants[0] ? (left != right) : (left < right))))));
    }

    public static boolean ROR_greater_equals(int left, int right, boolean... mutants) {

        return (mutants[4] ? (left <= right) : (mutants[3] ? (left == right) : (mutants[2] ? (left < right) : (mutants[1] ? (left > right) : (mutants[0] ? (left != right) : (left >= right))))));
    }

    public static boolean ROR_less_equals(int left, int right, boolean... mutants) {

        return (mutants[4] ? (left == right) : (mutants[3] ? (left >= right) : (mutants[2] ? (left < right) : (mutants[1] ? (left > right) : (mutants[0] ? (left != right) : (left <= right))))));
    }


    //LCR
    public static boolean LCR_or(boolean left, boolean right, boolean... mutants) {

        return mutants[0] ? (left && right) : (left || right);
    }

    public static boolean LCR_and(boolean left, boolean right, boolean... mutants) {

        return mutants[0] ? (left || right) : (left && right);
    }
}

