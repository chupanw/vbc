package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.HashSet;

/**
 * @author chupanw
 */
public class HashSetExample {

    @VConditional
    public static boolean A;
    @VConditional
    public static boolean B;

    public static void main(String[] args) {
        HashSetExample example = new HashSetExample();
        example.testInit();
        example.testAdd();
        example.testAddConflict();
        example.testRemove();
        example.testIterator();
    }

    private void testInit() {
        System.out.println("------");
        HashSet<Integer> s;
        if (A) {
            s = new HashSet<>();
            s.add(1);
        }
        else {
            s = new HashSet<Integer>();
            s.add(1);
            s.add(2);
        }
        System.out.println(s.size());
    }

    private void testAdd() {
        System.out.println("------");
        HashSet<Integer> s = new HashSet<>();
        if (A)
            s.add(1);
        else
            s.add(2);
        System.out.println(s.size());
        System.out.println(s.contains(1));
    }

    private void testAddConflict() {
        System.out.println("------");
        HashSet<Integer> s = new HashSet<>();
        if (A)
            s.add(1);
        else
            s.add(1);
        System.out.println(s.size());
        System.out.println(s.contains(1));
    }

    private void testRemove() {
        System.out.println("------");
        HashSet<Integer> s = new HashSet<>();
        if (A)
            s.add(1);
        else
            s.add(2);
        s.remove(2);
        System.out.println(s.size());
        System.out.println(s.contains(1));
    }

    private void testIterator() {
        System.out.println("------");
        HashSet<Integer> s = new HashSet<Integer>();
        if (A) {
            s.add(1);
            s.add(2);
            s.add(3);
        } else {
            s.add(4);
            s.add(5);
            s.add(6);
            s.add(7);
        }
        for (Object value : s) {
            System.out.println(value);
        }
    }
}
