package edu.cmu.cs.vbc.prog.iteration;


public class findElement extends iterationBench {
    public static void main(String[] args){
        new findElement().run();
    }

    public void run() {
        FEList.list.indexOf(6);
    }

    public static void generate(Integer numFeatures) {
        FEList.generate(numFeatures);
    }
}
