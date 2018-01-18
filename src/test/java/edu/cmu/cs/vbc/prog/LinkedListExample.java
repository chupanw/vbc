package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Test variational LinkedList
 *
 * @author chupanw
 */
public class LinkedListExample {
    @VConditional
    public static boolean A;
    @VConditional
    public static boolean B;

    private LinkedList<Integer> list;

    public LinkedListExample() {
        list = new LinkedList<>();
        if (A) {
            list.add(2);
            list.add(1);
        } else {
            list.add(5);
            list.add(4);
            list.add(3);
        }
    }

    private void testAddAll() {
        LinkedList<Integer> l = new LinkedList<>();
        if (A)
            l.add(3);
        if (B)
            l.add(9);
        else
            l.add(99);
        list.addAll(l);
        for (Integer i : list)
            System.out.println(i);
    }

    public static void main(String[] args) {
        LinkedListExample test = new LinkedListExample();
        test.printSize();
        test.printElements();
        test.binarySearch();
        test.sort();
        test.printSize();
        test.printElements();
        test.binarySearch();
        test.testAddAll();
    }

    void printElements() {
        System.out.print("[");
        for (int i = 0; i < list.size(); i++) {
            System.out.print(list.get(i));
            if (i != list.size() - 1)
                System.out.print(",");
        }
        System.out.println("]");
    }

    void printSize() {
        System.out.println("size: " + list.size());
    }

    void sort() {
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Integer i1 = (Integer) o1;
                Integer i2 = (Integer) o2;
                return i1 - i2;
            }
        });
    }

    void binarySearch() {
        int key;
        if (A)
            key = 2;
        else
            key = 4;
        System.out.println(Collections.binarySearch(list, key, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Integer i1 = (Integer) o1;
                Integer i2 = (Integer) o2;
                return i1 - i2;
            }
        }));
    }

}
