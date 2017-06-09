package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntConsumer;

/**
 * Created by lukas on 6/2/17.
 */
public class Benchmark_Vint {
    @VConditional
    static boolean use30 = true;

    @VConditional
    static boolean doubleLimit = true;
    
    public static void main(String[] args){
        int[] polynomial_int;
        if (use30) {
            // x^3 + 1850 x + 75 x^2 + 15000 = (x + 20)(x + 25)(x + 30)
            int[] poly= {1, 75, 1850, 15000};
            polynomial_int = poly;
        } else {
            // x^3 + 1805 x + 74 x^2 + 14500 = (x + 20)(x + 25)(x + 29)
            int[] poly = {1, 74, 1805, 14500};
            polynomial_int = poly;
        }
        System.out.println(findRoots(polynomial_int));
    }

    public static int maxCoefficient(int[] nums){
        int maxSoFar = nums[0];
        for (int i = 1; i < nums.length; i++) {
            maxSoFar = nums[i] > maxSoFar ? nums[i] : maxSoFar;
        }
        return maxSoFar;
    }


    public static int pow_int(int base, int expt){
        return pow_int(base, expt, 1);
    }
    public static int pow_int(int base, int expt, int valueSoFar){
        if (expt < 1){
            return valueSoFar;
        } else {
            return pow_int(base, expt - 1, valueSoFar*base);
        }
    }


    public static boolean isRoot(int x, int[] polynomial){
        int sum = 0;
        for (int i = 0; i < polynomial.length; i++) {
            sum += polynomial[polynomial.length - i - 1]*pow_int(x, i);
        }
        return sum == 0;
    }


    // Assumes POLYNOMIAL's first term is the first non-zero term
    // Also assumes POLYNOMIAL's length >= 1
    public static List<Integer> findRoots(int[] polynomial){
        assert(polynomial.length > 0);
	int multiplier = polynomial.length;
	if (doubleLimit) {
	    multiplier *= 2;
	}
        int limit = multiplier*maxCoefficient(polynomial)/polynomial[0];
        List<Integer> roots = new LinkedList<>();
        for (int root = -limit; root <= limit; root++) {
            if (isRoot(root, polynomial)){
                roots.add(root);
            }
        }
        return roots;
    }
}
