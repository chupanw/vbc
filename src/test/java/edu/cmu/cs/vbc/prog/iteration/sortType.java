package edu.cmu.cs.vbc.prog.iteration;

import java.util.Comparator;

public class sortType extends iterationBench {
    public static void main(String[] args){
        new sortType().run();
    }

    public void run() {
        Comparator<Integer> c;
        if (FEList.E1 || FEList.E50)
            c = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Integer i1 = (Integer) o1;
                Integer i2 = (Integer) o2;
                return i1 - i2;
            }
        };
        else
            c = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Integer i1 = (Integer) o1;
                Integer i2 = (Integer) o2;
                return i2 - i1;
            }
        };

        FEList.list.sort(c);
    }
}
