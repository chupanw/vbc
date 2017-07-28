package edu.cmu.cs.vbc.prog.iteration;


public class size extends iterationBench {
    public static void main(String[] args){
        new size().run();
    }

    public void run() {
        FEList.list.size();
    }
}
