package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.Scanner;

/**
 * Adapted from the IntroClassJava dataset
 */
public class Median {

    @VConditional public static boolean c56;
    @VConditional public static boolean c57;
    @VConditional public static boolean c58;
    @VConditional public static boolean c61;

    public static void main (String[]args) throws Exception {
        Median x = new Median();
        x.v_0cea42f9_Black_t1();
//        x.v_0cea42f9_Black_t2();
//        x.v_0cea42f9_Black_t3();
//        x.v_0cea42f9_Black_t5();
//        x.v_0cea42f9_Black_t6();
//        x.v_0cea42f9_Black_t7();
        System.out.println("To pass: ");
    }

    void v_0cea42f9_Black_Template(String expected, String input) {
        String out = v_0cea42f9(new Scanner(input)).replace ("\n", " ").trim();
        System.out.println(out);
        assertEquals(expected.replace (" ", ""), out.replace (" ", ""));
    }

    void v_0cea42f9_Black_t1() {
        v_0cea42f9_Black_Template("Please enter 3 numbers separated by spaces > 6 is the median", "2 6 8");
    }
    void v_0cea42f9_Black_t2() {
        v_0cea42f9_Black_Template("Please enter 3 numbers separated by spaces > 6 is the median", "2 8 6");
    }
    void v_0cea42f9_Black_t3() {
        v_0cea42f9_Black_Template("Please enter 3 numbers separated by spaces > 6 is the median", "6 2 8");
    }
    void v_0cea42f9_Black_t4() {
        v_0cea42f9_Black_Template("Please enter 3 numbers separated by spaces > 6 is the median", "6 8 2");
    }
    void v_0cea42f9_Black_t5() {
        v_0cea42f9_Black_Template("Please enter 3 numbers separated by spaces > 6 is the median", "8 2 6");
    }
    void v_0cea42f9_Black_t6() {
        v_0cea42f9_Black_Template("Please enter 3 numbers separated by spaces > 6 is the median", "8 6 2");
    }
    void v_0cea42f9_Black_t7() {
        v_0cea42f9_Black_Template("Please enter 3 numbers separated by spaces > 9 is the median", "9 9 9");
    }


    public String v_0cea42f9(Scanner scanner) {
        Integer a = 0, b = 0, c = 0;
        String output = "";
        output += (String.format("Please enter 3 numbers separated by spaces > "));
        a = scanner.nextInt();
        b = scanner.nextInt();
        c = scanner.nextInt();
        if (((a > b) && (a < c))
                || ((a < b) && (a > c))) {
            output += (String.format ("%d is the median\n", a));
        } else if (((b > a) && (b < c))
                || ((b < a) && (b > c))) {
            output += (String.format ("%d is the median\n", b));
        } else if (((c > a) && (c < b))
                || ((c < a) && (c > b))) {
            output += (String.format ("%d is the median\n", c));
        }
        return output;
    }

    void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            if (expected instanceof String && actual instanceof String)
                throw new RuntimeException("Comparison failed...");
            else
                throw new AssertionError();
        }
    }
}
