package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

/**
 * @author chupanw
 */
public class LongExample {

    @VConditional
    boolean A;

    public static void main(String[] args) {
        LongExample foo = new LongExample();
        long skipNum = 0L;
        if (foo.A)
            skipNum = 100L;
        else
            skipNum = 1000L;
        System.out.println(foo.skip(skipNum));
    }

    /**
     * Excerpt from ZipMe
     *
     * The problem was caused by long parameter type.
     */
    public long skip(long n) {
        if (n == 0)
            return 0;
        long skipped = 0L;
        skipped += 60;
        return skipped;
    }
}
