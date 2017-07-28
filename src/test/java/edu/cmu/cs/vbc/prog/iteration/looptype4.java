package edu.cmu.cs.vbc.prog.iteration;


public class looptype4 extends iterationBench {
    public static void main(String[] args){
        new looptype4().run();
    }

    // reading a variable outside of the loop each iteration
    public void run() {
        Integer n = 42;
        for (Integer el : FEList.list) {
            System.out.println(el + n);
        }
    }
}
