package edu.cmu.cs.vbc.prog.iteration;


public class simpleIteration extends iterationBench {
    public static void main(String[] args){
        new simpleIteration().run();
    }

    public void run() {
        for (Integer el : FEList.list) {
            System.out.println(el);
        }
    }
}
