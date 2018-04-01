package edu.cmu.cs.vbc.prog;

/**
 * Try to reduce the number of VBlocks
 */
public class VBlockExample {
    public static void main(String[] args) {
        VBlockExample foo = new VBlockExample();
        System.out.println(foo.factorial());
        System.out.println(foo.factorialN(10));
        foo.factorialN2(10);
    }

    /**
     * Not related to variation at all, should have only one VBlock.
     *
     * Unfortunately, we don't have a automatic way of checking number of VBlocks
     * with existing testing infrastructure.
     *
     * Should have 1 VBlock
     */
    int factorial() {
        int result = 1;
        for (int i = 1; i <= 10; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Not related to variation, but the parameter is a V
     *
     * Should have 4 VBlocks
     */
    int factorialN(int n) {
        int result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Mixing the two. However, Java compiler uses the same local variable for
     * i and j, so the first for loop is affected by the second for loop.
     *
     * Should have 4 VBlocks
     */
    void factorialN2(int n) {
        int result1 = 1;
        int result2 = 1;
        for (int i = 1; i <= 10; i++)
            result1 *= i;
        for (int j = 1; j <= n; j++)
            result2 *= j;
    }

    /**
     * Mixing the two.
     *
     * Should have 1 VBlock
     */
    void factorialN3(int n) {
        int result1 = 1;
        int result2 = n + 1;
        for (int i = 1; i <= 10; i++)
            result1 *= i;
        System.out.println(result1);
    }
}
