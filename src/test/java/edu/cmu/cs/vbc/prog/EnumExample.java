package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

public class EnumExample {
    @VConditional
    public boolean A;

    @VConditional
    public boolean B;

    @VConditional
    public boolean C;

    Day day;

    public static void main(String[] args) {
        EnumExample example = new EnumExample();
        example.test();
    }

    public EnumExample() {
        day = Day.MONDAY;
        if (A && B && C)
            day = Day.TUESDAY;
        else if (A && B && !C)
            day = Day.WEDNESDAY;
        else if (A && !B && C)
            day = Day.THURSDAY;
        else if (A && !B && !C)
            day = Day.FRIDAY;
        else if (!A && B && C)
            day = Day.SATURDAY;
        else if (!A && B && !C)
            day = Day.SUNDAY;
    }

    public void test() {
        switch (day) {
            case MONDAY:
                System.out.println("1"); break;
            case TUESDAY:
                System.out.println("2"); break;
            case WEDNESDAY:
                System.out.println("3"); break;
            case THURSDAY:
                System.out.println("4"); break;
            case FRIDAY:
                System.out.println("5"); break;
            case SATURDAY:
                System.out.println("6"); break;
            case SUNDAY:
                System.out.println("7"); break;
        }
    }
}

enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}