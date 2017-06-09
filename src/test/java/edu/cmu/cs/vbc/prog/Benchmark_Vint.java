package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

/**
 * Created by lukas on 6/2/17.
 */
public class Benchmark_Vint {
    @VConditional
    boolean use30 = true;

    @VConditional
    boolean doubleLimit = true;
    
    public static void main(String[] args){
        Benchmark_Vint self = new Benchmark_Vint();
        int c1, c2, c3, c4;
        if (self.use30) {
            // x^3 + 1850 x + 75 x^2 + 15000 = (x + 20)(x + 25)(x + 30)
            c1 = 1;
            c2 = 75;
            c3 = 1850;
            c4 = 15000;
        } else {
            // x^3 + 1805 x + 74 x^2 + 14500 = (x + 20)(x + 25)(x + 29)
            c1 = 1;
            c2 = 74;
            c3 = 1805;
            c4 = 14500;
        }
        self.findRoots(c1, c2, c3, c4);
    }

    public int maxCoefficient(int c1, int c2, int c3, int c4){
        if (c1 > c2 && c1 > c3 && c1 > c4) {
            return c1;
        } else if (c2 > c1 && c2 > c3 && c2 > c4) {
            return c2;
        } else if (c3 > c1 && c3 > c2 && c3 > c4) {
            return c3;
        } else {
            return c4;
        }
    }


    public int pow_int(int base, int expt){
        return pow_int(base, expt, 1);
    }
    public int pow_int(int base, int expt, int valueSoFar){
        if (expt < 1){
            return valueSoFar;
        } else {
            return pow_int(base, expt - 1, valueSoFar*base);
        }
    }


    public boolean isRoot(int x, int c1, int c2, int c3, int c4){
        return (c1*pow_int(x, 3) + c2*pow_int(x, 2) + c3*x + c4) == 0;
    }


    // Assumes POLYNOMIAL's first term is the first non-zero term
    // Also assumes POLYNOMIAL's length >= 1
    public void findRoots(int c1, int c2, int c3, int c4){
        int multiplier = 4;
        if (doubleLimit) {
            multiplier *= 2;
        }
        int limit = multiplier*maxCoefficient(c1, c2, c3, c4)/c1;
        System.out.print("[ ");
        for (int root = -limit; root <= limit; root++) {
            if (isRoot(root, c1, c2, c3, c4)){
                System.out.print(root + " ");
            }
        }
        System.out.println("]");
    }
}
