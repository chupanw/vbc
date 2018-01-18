package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author chupanw
 */
public class InvokeDynamicExample {
    @VConditional
    static boolean A;

    public static void main(String[] args) {
        InvokeDynamicExample example = new InvokeDynamicExample();
//        example.lambdaFunction();
//        example.lambdaFunction2();
        example.lambdaComparator();
    }

    private void lambdaComparator() {
        ArrayList<Integer> l = new ArrayList<>();
        l.add(3);l.add(2);l.add(1);
        l.sort((a, b) -> a - b);
        for (Integer i : l)
            System.out.println(i);
    }

    private void lambdaFunction() {
        LinkedList<String> l = new LinkedList<>();
        l.add("hello");
        l.add("world");
        l.stream().map(String::toUpperCase).forEach(System.out::println);
    }

    private void lambdaFunction2() {
        LinkedList<String> l = new LinkedList<>();
        l.add("hello");
        l.add("world");
        l.stream().map(s -> s + "s").forEach(System.out::println);
    }
}
