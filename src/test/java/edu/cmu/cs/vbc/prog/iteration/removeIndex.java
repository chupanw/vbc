package edu.cmu.cs.vbc.prog.iteration;


public class removeIndex extends iterationBench {
    public static void main(String[] args){
        new removeIndex().run();
    }

    public void run() {
        System.out.println(FEList.list.remove(6));
    }
}
