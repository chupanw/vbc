package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.LinkedList;

public class IterationBenchmark {
    @VConditional
    boolean A;
    @VConditional
    boolean B;

    public static void main(String[] args) {
        IterationBenchmark ie = new IterationBenchmark();
        ie.runSimple();
//        System.out.println("\n~~~\n");
//        ie.run();
//        System.out.println("\n~~~\n");
//        ie.checkStyle();
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

//    @VConditional
//    boolean C;
//    @VConditional
//    boolean D;
//    public void run() {
//        LinkedList<Integer> l = new LinkedList<>();
//        l.add(1);
//        if (A) {
//            l.add(2);
//            l.add(3);
//        }
//        else
//            l.add(3);
//
//        if (D)
//            l.add(4);
//
//        if (B)
//            l.add(4);
//
//        l.add(5);
//
//        if (!B)
//            l.add(76);
//        else if (C)
//            l.add(77);
//        else
//            l.add(60);
//
//        if (A)
//            if (B)
//                l.add(20);
//            else if (D)
//                l.add(21);
//
//        if (C)
//            l.add(22);
//        else
//            l.add(22);
//
//        l.add(30);
//        l.add(30);
//
//        if (D)
//            if (A)
//                if (!C)
//                    l.add(7);
//                else if (B)
//                    l.add(7);
//                else
//                    l.add(7);
//            else
//                l.add(7);
//        else
//            l.add(7);
//
//        int res = 0;
//        for (Integer el : l) {
//            System.out.println(el);
//            res = res + el;
//        }
//        System.out.println("=== sum: ===");
//        System.out.println(res);
//        res = res / l.size();
//        System.out.println("=== len: ===");
//        System.out.println(l.size());
//        System.out.println("=== avg: ===");
//        System.out.println(res);
//    }
//
//
//    @VConditional
//    static boolean E1;
//    @VConditional
//    static boolean E2;
//    @VConditional
//    static boolean E3;
//    @VConditional
//    static boolean E4;
//    @VConditional
//    static boolean E5;
//    @VConditional
//    static boolean E6;
//    @VConditional
//    static boolean E7;
//    @VConditional
//    static boolean E8;
//    @VConditional
//    static boolean E9;
//    @VConditional
//    static boolean E10;
//    @VConditional
//    static boolean E11;
//    @VConditional
//    static boolean E13;
//    @VConditional
//    static boolean E12;
//    @VConditional
//    static boolean E14;
//    @VConditional
//    static boolean E15;
//    @VConditional
//    static boolean E16;
//    @VConditional
//    static boolean E17;
//    @VConditional
//    static boolean E18;
//    @VConditional
//    static boolean E19;
//    @VConditional
//    static boolean E20;
//    @VConditional
//    static boolean E21;
//    @VConditional
//    static boolean E22;
//    @VConditional
//    static boolean E23;
//    @VConditional
//    static boolean E24;
//    @VConditional
//    static boolean E25;
//    @VConditional
//    static boolean E26;
//    @VConditional
//    static boolean E27;
//    @VConditional
//    static boolean E28;
//    @VConditional
//    static boolean E29;
//    @VConditional
//    static boolean E30;
//    @VConditional
//    static boolean E31;
//    @VConditional
//    static boolean E32;
//    @VConditional
//    static boolean E33;
//    @VConditional
//    static boolean E34;
//    @VConditional
//    static boolean E35;
//    @VConditional
//    static boolean E36;
//    @VConditional
//    static boolean E37;
//    @VConditional
//    static boolean E38;
//    @VConditional
//    static boolean E39;
//    @VConditional
//    static boolean E40;
//    @VConditional
//    static boolean E41;
//    @VConditional
//    static boolean E42;
//    @VConditional
//    static boolean E43;
//    @VConditional
//    static boolean E44;
//    @VConditional
//    static boolean E45;
//    @VConditional
//    static boolean E46;
//    @VConditional
//    static boolean E47;
//    @VConditional
//    static boolean E48;
//    @VConditional
//    static boolean E49;
//    @VConditional
//    static boolean E50;
//    @VConditional
//    static boolean E51;
//    @VConditional
//    static boolean E52;
//    @VConditional
//    static boolean E53;
//    @VConditional
//    static boolean E54;
//    @VConditional
//    static boolean E55;
//    @VConditional
//    static boolean E56;
//    @VConditional
//    static boolean E57;
//    @VConditional
//    static boolean E58;
//    @VConditional
//    static boolean E59;
//    @VConditional
//    static boolean E60;
//    @VConditional
//    static boolean E61;
//    @VConditional
//    static boolean E62;
//    @VConditional
//    static boolean E63;
//    @VConditional
//    static boolean E64;
//    @VConditional
//    static boolean E65;
//    @VConditional
//    static boolean E66;
//    @VConditional
//    static boolean E67;
//    @VConditional
//    static boolean E68;
//    @VConditional
//    static boolean E69;
//    @VConditional
//    static boolean E70;
//    @VConditional
//    static boolean E71;
//    @VConditional
//    static boolean E72;
//    @VConditional
//    static boolean E73;
//    @VConditional
//    static boolean E74;
//    @VConditional
//    static boolean E75;
//    @VConditional
//    static boolean E76;
//    @VConditional
//    static boolean E77;
//    @VConditional
//    static boolean E78;
//    @VConditional
//    static boolean E79;
//    @VConditional
//    static boolean E80;
//    @VConditional
//    static boolean E81;
//    @VConditional
//    static boolean E82;
//    @VConditional
//    static boolean E83;
//    @VConditional
//    static boolean E84;
//    @VConditional
//    static boolean E85;
//    @VConditional
//    static boolean E86;
//    @VConditional
//    static boolean E87;
//    @VConditional
//    static boolean E88;
//    @VConditional
//    static boolean E89;
//    @VConditional
//    static boolean E90;
//    @VConditional
//    static boolean E91;
//    @VConditional
//    static boolean E92;
//    @VConditional
//    static boolean E93;
//    @VConditional
//    static boolean E94;
//    @VConditional
//    static boolean E95;
//    @VConditional
//    static boolean E96;
//    @VConditional
//    static boolean E97;
//    @VConditional
//    static boolean E98;
//    @VConditional
//    static boolean E99;
//    @VConditional
//    static boolean E100;
//
//
//    public void checkStyle() {
//        LinkedList<Integer> l = new LinkedList<>();
//
//        if (E1)
//            addThisMany(6, l);
//        if (E2)
//            addThisMany(13, l);
//        if (E3)
//            addThisMany(33, l);
//        if (E4)
//            addThisMany(22, l);
//        if (E5)
//            addThisMany(2, l);
//        if (E6)
//            addThisMany(10, l);
//        if (E7)
//            addThisMany(5, l);
//        if (E8)
//            addThisMany(22, l);
//        if (E9)
//            addThisMany(10, l);
//        if (E10)
//            addThisMany(7, l);
//        if (E11)
//            addThisMany(8, l);
//        if (E12)
//            addThisMany(34, l);
//        if (E13)
//            addThisMany(8, l);
//        if (E14)
//            addThisMany(8, l);
//        if (E15)
//            addThisMany(44, l);
//        if (E16)
//            addThisMany(6, l);
//        if (E17)
//            addThisMany(10, l);
//        if (E18)
//            addThisMany(26, l);
//        if (E19)
//            addThisMany(21, l);
//        if (E20)
//            addThisMany(36, l);
//        if (E21)
//            addThisMany(29, l);
//        if (E22)
//            addThisMany(30, l);
//        if (E23)
//            addThisMany(38, l);
//        if (E24)
//            addThisMany(20, l);
//        if (E25)
//            addThisMany(13, l);
//        if (E26)
//            addThisMany(16, l);
//        if (E27)
//            addThisMany(30, l);
//        if (E28)
//            addThisMany(45, l);
//        if (E29)
//            addThisMany(12, l);
//        if (E30)
//            addThisMany(1, l);
//        if (E31)
//            addThisMany(44, l);
//        if (E32)
//            addThisMany(1, l);
//        if (E33)
//            addThisMany(27, l);
//        if (E34)
//            addThisMany(39, l);
//        if (E35)
//            addThisMany(19, l);
//        if (E36)
//            addThisMany(3, l);
//        if (E37)
//            addThisMany(40, l);
//        if (E38)
//            addThisMany(31, l);
//        if (E39)
//            addThisMany(4, l);
//        if (E40)
//            addThisMany(44, l);
//        if (E41)
//            addThisMany(29, l);
//        if (E42)
//            addThisMany(48, l);
//        if (E43)
//            addThisMany(40, l);
//        if (E44)
//            addThisMany(23, l);
//        if (E45)
//            addThisMany(2, l);
//        if (E46)
//            addThisMany(1, l);
//        if (E47)
//            addThisMany(11, l);
//        if (E48)
//            addThisMany(2, l);
//        if (E49)
//            addThisMany(16, l);
//        if (E50)
//            addThisMany(44, l);
//        if (E51)
//            addThisMany(31, l);
//        if (E52)
//            addThisMany(40, l);
//        if (E53)
//            addThisMany(1, l);
//        if (E54)
//            addThisMany(1, l);
//        if (E55)
//            addThisMany(3, l);
//        if (E56)
//            addThisMany(48, l);
//        if (E57)
//            addThisMany(47, l);
//        if (E58)
//            addThisMany(1, l);
//        if (E59)
//            addThisMany(39, l);
//        if (E60)
//            addThisMany(28, l);
//        if (E61)
//            addThisMany(25, l);
//        if (E62)
//            addThisMany(28, l);
//        if (E63)
//            addThisMany(28, l);
//        if (E64)
//            addThisMany(21, l);
//        if (E65)
//            addThisMany(32, l);
//        if (E66)
//            addThisMany(48, l);
//        if (E67)
//            addThisMany(42, l);
//        if (E68)
//            addThisMany(24, l);
//        if (E69)
//            addThisMany(29, l);
//        if (E70)
//            addThisMany(10, l);
//        if (E71)
//            addThisMany(36, l);
//        if (E72)
//            addThisMany(27, l);
//        if (E73)
//            addThisMany(9, l);
//        if (E74)
//            addThisMany(38, l);
//        if (E75)
//            addThisMany(5, l);
//        if (E76)
//            addThisMany(14, l);
//        if (E77)
//            addThisMany(23, l);
//        if (E78)
//            addThisMany(27, l);
//        if (E79)
//            addThisMany(48, l);
//        if (E80)
//            addThisMany(30, l);
//        if (E81)
//            addThisMany(13, l);
//        if (E82)
//            addThisMany(42, l);
//        if (E83)
//            addThisMany(3, l);
//        if (E84)
//            addThisMany(40, l);
//        if (E85)
//            addThisMany(17, l);
//        if (E86)
//            addThisMany(34, l);
//        if (E87)
//            addThisMany(40, l);
//        if (E88)
//            addThisMany(4, l);
//        if (E89)
//            addThisMany(2, l);
//        if (E90)
//            addThisMany(9, l);
//        if (E91)
//            addThisMany(17, l);
//        if (E92)
//            addThisMany(48, l);
//        if (E93)
//            addThisMany(33, l);
//        if (E94)
//            addThisMany(4, l);
//        if (E95)
//            addThisMany(6, l);
//        if (E96)
//            addThisMany(28, l);
//        if (E97)
//            addThisMany(22, l);
//        if (E98)
//            addThisMany(7, l);
//        if (E99)
//            addThisMany(23, l);
//        if (E100)
//            addThisMany(41, l);
//
//        int sum = 0;
//        for (Integer el : l) {
//            System.out.println(el);
//            sum += el;
//        }
//        System.out.println("=== sum: ===");
//        System.out.println(sum);
//    }
//
//    public void addThisMany(int n, LinkedList<Integer> l) {
//        for (int i = 0; i < n; i++)
//            l.add(1);
//    }
}
