package edu.cmu.cs.vbc.prog.iteration;

public class looptype2 extends iterationBench {
    public static void main(String[] args){
        new looptype2().run();
    }

    // modifying a variable outside of the loop each iteration
    public void run() {
        Integer sum = 0;
        for (Integer el : FEList.list) {
            System.out.println(el);
            sum += el;
        }
    }
}
