package edu.cmu.cs.vbc.prog.iteration;

public class expensiveIteration extends iterationBench {
    public static void main(String[] args){
        new expensiveIteration().run();
    }

    public void run() {
        for (someObj el : FEList.objList) {
            el.longOperation();
        }
    }

    public static void generate(Integer numFeatures) {
        FEList.generateObj(numFeatures);
    }
}
