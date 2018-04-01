package edu.cmu.cs.vbc.prog;

/**
 * A collection of methods that have caused VBlockAnalysis to fail
 * @author chupanw
 */
public class VBlockAnalysisTest {

    /**
     * Extract from elevator.PL_Interface_impl.start()
     *
     * The finally part introduces a loop among Block 5 and Block 16, where Block 16 is
     * the handler block of Block 5.
     */
    public void start(int specification, int variation) throws Throwable {
        try {
            if (specification == 0)
                System.out.println("Started Elevator PL with Specification " + String.valueOf(specification) +  ", Variation: " +String.valueOf(variation));
        } catch (Throwable e) {
            throw e;
        } finally {
        }
    }

    /**
     * Extract from jetty.AbstractConnector.doStop()
     *
     * This is a variant of the previous method. The try-catch structure forms a loop
     * between Block 6 and Block 18, where Block 18 is the handler block of Block 6.
     * While updating the VBlock index of Block 6, the process is distracted because
     * Block 18 is also a predecessor of Block 6.
     */
    protected void doStop() throws Exception {
        try {
            System.out.println("dummy 1");
        } catch (Exception var7) {
            System.out.printf("dummy 2");
        }

        System.out.println("dummy 3");
        Integer acceptors;
        synchronized(this) {
            acceptors = 1;
        }

        if (acceptors != 0) {
            Integer var2 = acceptors;
            int var3 = acceptors + 1;

            for(int var4 = 0; var4 < var3; ++var4) {
                Integer thread = var2 * 2;
                if (thread != 0) {
                    System.out.println("dummy 4");
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("If you are seeing this, it means this test passed");
    }

}
