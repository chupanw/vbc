package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.LinkedList;

/**
 * Created by lukas on 6/29/17.
 */
public class IterationExample {
    @VConditional
    static boolean A;
    @VConditional
    static boolean B;

    public static void main(String[] args) {
        IterationExample ie = new IterationExample();
        ie.iterator();
        System.out.println("\n~~~~~\n");
        ie.indexing();
    }

    public void indexing() {
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
        if (A)
            l.add(6);

        for (int i = 0; i < l.size(); i++) {
            System.out.println(l.get(i));
        }
   }

    private void iterator() {
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

}