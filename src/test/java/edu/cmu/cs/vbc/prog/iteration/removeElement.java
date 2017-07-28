package edu.cmu.cs.vbc.prog.iteration;


public class removeElement extends iterationBench {
    public static void main(String[] args){
        new removeElement().run();
    }

    public void run() {
        FEList.list.remove(Integer.valueOf(6));
    }

    public static void generate(Integer numFeatures) {
        FEList.generate(numFeatures);
    }
}
