package edu.cmu.cs.vbc.prog.iteration;

public class looptype3 extends iterationBench {
    public static void main(String[] args){
        new looptype3().run();
    }

    // have a feature condition in body
    public void run() {
        for (Integer el : FEList.list) {
            if (FEList.E1)
                System.out.println(el);
        }
    }
}
