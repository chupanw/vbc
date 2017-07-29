package edu.cmu.cs.vbc.prog.iteration;

import java.util.LinkedList;
import java.util.Comparator;

public class binarySearch extends iterationBench {
    public static void main(String[] args){
        new binarySearch().run();
    }

    public void run() {
        binarySearch(610, FEList.list);
    }

    public Integer binarySearch(Integer what, LinkedList<Integer> list) {
        int size = list.size();
        int index = size/2;
        int el = list.get(index);
        while (el != what) {
            int nextIndex;
            if (el < what) {
                nextIndex = index - index / 2;
                if (nextIndex == index) nextIndex -= 1;
                if (nextIndex < 0) break;
            } else {
                nextIndex = index + index / 2;
                if (nextIndex == index) nextIndex += 1;
                if (nextIndex >= size) break;
            }
            index = nextIndex;
            el = list.get(index);
        }
        return index;
    }


    public static void generate(Integer numFeatures) {
        FEList.generate(numFeatures);
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
