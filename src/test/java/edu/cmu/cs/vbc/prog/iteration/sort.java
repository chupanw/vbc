package edu.cmu.cs.vbc.prog.iteration;

import java.util.Comparator;

public class sort extends iterationBench {
    public static void main(String[] args){
        new sort().run();
    }

    public void run() {
        FEList.list.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Integer i1 = (Integer) o1;
                Integer i2 = (Integer) o2;
                return i1 - i2;
            }
        });
    }
}
