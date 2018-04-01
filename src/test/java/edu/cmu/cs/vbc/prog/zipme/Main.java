package edu.cmu.cs.vbc.prog.zipme;

import sun.util.locale.BaseLocale;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

public class Main {

    public Main() {
        try {
//			PL_Interface_impl.main(null);

            String arg = "Bears are mammals of the family Ursidae. Bears are classified as caniforms, or doglike carnivorans, with the pinnipeds being their closest living relatives. Although there are only eight living species of bear, they are widespread, appearing in a wide variety of habitats throughout the Northern Hemisphere and partially in the Southern Hemisphere. Bears are found in the continents of North America, South America, Europe, and Asia.\n" +
                    "Common characteristics of modern bears include a large body with stocky legs, a long snout, shaggy hair, plantigrade paws with five nonretractile claws, and a short tail. While the polar bear is mostly carnivorous and the giant panda feeds almost entirely on bamboo, the remaining six species are omnivorous, with largely varied diets including both plants and animals.\n" +
                    "With the exceptions of courting individuals and mothers with their young, bears are typically solitary animals. They are generally diurnal, but may be active during the night (nocturnal) or twilight (crepuscular), particularly around humans. Bears are aided by an excellent sense of smell, and despite their heavy build and awkward gait, they can run quickly and are adept climbers and swimmers. In autumn some bear species forage large amounts of fermented fruits which affects their behaviour.[1] Bears use shelters such as caves and burrows as their dens, which are occupied by most species during the winter for a long period of sleep similar to hibernation.";

            ZipTest.main(new String[]{arg});
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            int i = 0;
            FeatureSwitches.__SELECTED_FEATURE_Adler32Checksum = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_ArchiveCheck = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_Base = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_Compress = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_CRC = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressAdler32Checksum = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressCRC = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressGZIP = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_DerivativeCompressGZIPCRC = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_DerivativeExtractCRC = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_DerivativeGZIPCRC = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_Extract = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_GZIP = Boolean.valueOf(args[i++]);
            FeatureSwitches.__SELECTED_FEATURE_ZipMeTest = Boolean.valueOf(args[i++]);
        }

        // initialize interfaces / static fields
//		initialize();
//		FeatureSwitches.__GUIDSL_ROOT_PRODUCTION = true;

        if (!FeatureSwitches.valid()) {
//			throw new RuntimeException("Wrong Selection");
            return;
        }

        new Main();
    }

    private static void initialize() {
        int c = ZipOutputStream.DEFLATED;
        new DeflaterHuffman(null);
        boolean debugging = DeflaterConstants.DEBUGGING;
        new DeflaterEngine(null);
        new CRC32();
        int[] goodLength = DeflaterConstants.GOOD_LENGTH;
        new Inflater(true);
        InflaterHuffmanTree x = InflaterHuffmanTree.defDistTree;
        String z = BaseLocale.SEP;
        ZipEntry.getCalendar();

        new ZipEntry("").setTime(System.currentTimeMillis());
    }
}
