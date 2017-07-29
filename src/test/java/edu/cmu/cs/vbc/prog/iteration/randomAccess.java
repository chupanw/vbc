package edu.cmu.cs.vbc.prog.iteration;


public class randomAccess extends iterationBench {
    public static void main(String[] args){
        new randomAccess().run();
    }

    public void run() {
        System.out.println(FEList.list.get(6));
    }
}
