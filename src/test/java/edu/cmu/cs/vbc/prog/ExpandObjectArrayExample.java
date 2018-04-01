package edu.cmu.cs.vbc.prog;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.varex.annotation.VConditional;
import org.apache.commons.cli.*;

/**
 * Test {@link edu.cmu.cs.varex.ArrayOps#expandArray(V[], Class, FeatureExpr)}
 *
 * This example is based on {@link edu.cmu.cs.vbc.prog.checkstyle.Main}
 *
 * @author chupanw
 */
public class ExpandObjectArrayExample {
    @VConditional
    boolean A;
    @VConditional
    boolean B;

    private static final Options OPTS = new Options();
    static {
        OPTS.addOption("c", true, "foo option");
        OPTS.addOption("o", true, "bar option");
    }

    public static void main(String[] args) {
        ExpandObjectArrayExample foo = new ExpandObjectArrayExample();
        String[] myargs = new String[4];
        myargs[0] = "-c";
        myargs[2] = "-o";
        if (foo.A)
            myargs[1] = "hello";
        else
            myargs[1] = "hola";

        if (foo.B)
            myargs[3] = "world";
        else
            myargs[3] = "mundo";

        final CommandLineParser clp = new PosixParser();
        CommandLine line = null;
        try {
            line = clp.parse(OPTS, myargs);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(line.getOptionValue("c"));
        System.out.println(line.getOptionValue("o"));
    }
}
