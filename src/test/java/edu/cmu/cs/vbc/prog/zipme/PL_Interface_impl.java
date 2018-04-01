package edu.cmu.cs.vbc.prog.zipme;

import java.util.ArrayList;
import java.util.List;
//import gov.nasa.jpf.symbc.Symbolic;

public class PL_Interface_impl implements PL_Interface {

    public FeatureSwitches fs = new FeatureSwitches();

    //	@FilterField
    //@Symbolic("false")
    public static boolean executedUnimplementedAction = false;


    //	@FilterField
    //@Symbolic("false")
    public static List<String> actionHistory = new ArrayList<String>();


    public static boolean verbose = false;


    //	@FilterField
    //@Symbolic("false")
    private static boolean isAbortedRun = false;


    public static void main(String[] args) {
        try {
            PL_Interface_impl impl = new PL_Interface_impl();
//			args = new String[1];
//			verbose = true;
            if (FeatureSwitches.valid()) {
                impl.start(1, 1);
                PL_Interface_impl.println("no Exception");
            }
        } catch (Throwable e) {
            PL_Interface_impl.println("Caught Exception: " + e.getClass() + " "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int getIntegerMinMax(int min, int max) {
        return min;//Verify.getInt(min, max);
    }

    public static boolean getBoolean() {
        return true;//Verify.getBoolean();// verify true first
    }

    static String listToString(List<String> list) {
        String ret = "";
        for (String s : list) {
            ret = ret + " " + s;
        }
        return ret;
    }

    public static void println(String s) {
        if (PL_Interface_impl.verbose) {
            System.out.println(s);
        }
    }

    public static void println() {
        if (PL_Interface_impl.verbose) {
            System.out.println();
        }
    }

    public static void print(String s) {
        if (PL_Interface_impl.verbose) {
            System.out.print(s);
        }
    }

    public void start(int specification, int variation) throws Throwable {
        try {
            if (verbose)
                PL_Interface_impl.print("Started with Specification " + specification + ", Variation: " + variation);
            test(specification, variation);
        } catch (Throwable e) {
            throw e;
        } finally {
        }
    }

    public void checkOnlySpecification(int specID) {
        PL_Interface_impl.println("Specifications not implemented");
    }

    public List<String> getExecutedActions() {
        return actionHistory;
    }

    public boolean isAbortedRun() {
        return isAbortedRun;
    }

    // this method is used as hook for the liveness properties.
    public void test(int specification, int variation) throws Throwable {
        String[] args = new String[1];
        if (variation == 1) {
            args[0] = "Hallo\n";
        } else if (variation == 2) {
            // text from wikipedia
            args[0] = "Bears are mammals of the family Ursidae. Bears are classified as caniforms, or doglike carnivorans, with the pinnipeds being their closest living relatives. Although there are only eight living species of bear, they are widespread, appearing in a wide variety of habitats throughout the Northern Hemisphere and partially in the Southern Hemisphere. Bears are found in the continents of North America, South America, Europe, and Asia.\n" +
                    "Common characteristics of modern bears include a large body with stocky legs, a long snout, shaggy hair, plantigrade paws with five nonretractile claws, and a short tail. While the polar bear is mostly carnivorous and the giant panda feeds almost entirely on bamboo, the remaining six species are omnivorous, with largely varied diets including both plants and animals.\n" +
                    "With the exceptions of courting individuals and mothers with their young, bears are typically solitary animals. They are generally diurnal, but may be active during the night (nocturnal) or twilight (crepuscular), particularly around humans. Bears are aided by an excellent sense of smell, and despite their heavy build and awkward gait, they can run quickly and are adept climbers and swimmers. In autumn some bear species forage large amounts of fermented fruits which affects their behaviour.[1] Bears use shelters such as caves and burrows as their dens, which are occupied by most species during the winter for a long period of sleep similar to hibernation.";
        } else {
            System.out.println("unknown variation : " + variation);
            throw new Exception("unknown variation : " + variation);
        }
        try {
            ZipTest.main(args);
        } catch (Throwable e) {
            PL_Interface_impl.println("Caught Exception: " + e.getClass() + " "
                    + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


}
