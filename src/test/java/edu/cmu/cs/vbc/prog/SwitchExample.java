package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

/**
 * Example used for lifting switch statements.
 * @author chupanw
 */
public class SwitchExample {

    @VConditional
    boolean A;
    @VConditional
    boolean B;

    public static void main(String args[]) {
        SwitchExample foo = new SwitchExample();
        if (foo.A)
            foo.switch0(2);
        else
            foo.switch0(3);
        if (foo.A) {
            if (foo.B)
                foo.switch1(0);
            else
                foo.switch1(1);
        } else {
            if (foo.B)
                foo.switch1(2);
            else
                foo.switch1(3);
        }
        if (foo.A) {
            if (foo.B)
                foo.switch2(0);
            else
                foo.switch2(1);
        } else {
            if (foo.B)
                foo.switch2(2);
            else
                foo.switch2(3);
        }
        if (foo.A) {
            if (foo.B)
                foo.switch3(0);
            else
                foo.switch3(1);
        } else {
            if (foo.B)
                foo.switch3(2);
            else
                foo.switch3(3);
        }
        int a = 0;
        if (foo.A) {
            a = 1;
        }
        foo.testSwitch(a);
    }

    /**
     * Single clause
     */
    void switch0(int i) {
        switch (i) {
            case 3:
                System.out.println("3");
        }
    }

    /**
     * Fall through
     */
    void switch1(int i) {
        switch (i) {
            case 3:
                System.out.println("3");
            case 2:
                System.out.println("2");
            case 1:
                System.out.println("1");
        }
    }

    /**
     * Not fall through
     */
    void switch2(int i) {
        switch (i) {
            case 3:
                System.out.println("3");
                break;
            case 2:
                System.out.println("2");
                break;
            case 1:
                System.out.println("1");
                break;
            default:
                System.out.println("default");
        }
    }

    /**
     * Mixing fall through and not fall through
     */
    void switch3(int i) {
        switch (i) {
            case 3:
                System.out.println("3");
            case 2:
                System.out.println("2");
            case 1:
                System.out.println("1");
                break;
            default:
                System.out.println("default");
        }
    }

    /**
     * Switch on String, yield LookupSwitch rather than TableSwitch
     */
    void switch4(String s) {
        switch(s) {
            case "hello":
                System.out.println("hello");
            case "world":
                System.out.println("world");
            default:
                System.out.println("Not recognized");
        }
    }

    void testSwitch(int i) {
        switch (i) {
            case 1:
                System.out.println("1");
                return;
            default:
                System.out.println("default");
                if (i == 0) {
                    System.out.println(0);
                }
                else {
                    System.out.println("not zero");
                }
        }
    }
}
