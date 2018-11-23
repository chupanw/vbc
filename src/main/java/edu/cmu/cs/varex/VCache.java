package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDException;

import java.util.HashMap;

/**
 * Cache common FeatureExprs and the operations on them
 *
 * @author chupanw
 */
public class VCache {

    private static HashMap<BDD, Boolean> isSatCache = new HashMap<>();
    private static HashMap<BDD, HashMap<BDD, FeatureExpr>> andCache = new HashMap<>();
    private static HashMap<BDD, FeatureExpr> notCache = new HashMap<>();

    /**
     * An attempt to clear bdd cache in the bdd library between test cases
     *
     * TODO: try to clear the bddnodes array as well, that's the memory bottleneck
     */
    public static void clearAll() {
        for (BDD bdd : isSatCache.keySet()) {
            try {
                if (bdd.nodeCount() > 2) bdd.free();
            } catch (BDDException ignored) {}
        }
        for (BDD bdd : notCache.keySet()) {
            try {
                if (bdd.nodeCount() > 2) bdd.free();
            } catch (BDDException ignored) {}
        }
        for (BDD bdd : andCache.keySet()) {
            try {
                if (bdd.nodeCount() > 2) bdd.free();
            } catch (BDDException ignored) {}
        }
        isSatCache.clear();
        andCache.clear();
        notCache.clear();
        Runtime.getRuntime().gc();
    }

    public static boolean isSatisfiable(FeatureExpr fe) {
        BDD bdd = ((BDDFeatureExpr) fe).bdd();
        return isSatCache.computeIfAbsent(bdd, x -> {
            if (!fe.isSatisfiable())
                return false;
            else if (V.isDegreeTooHigh(fe))
                return false;
            else
                return true;
        });
    }

    public static boolean isContradiction(FeatureExpr fe) {
        return !isSatisfiable(fe);
    }

    public static FeatureExpr and(FeatureExpr a, FeatureExpr b) {
        BDD aa = ((BDDFeatureExpr) a).bdd();
        BDD bb = ((BDDFeatureExpr) b).bdd();
        if (!andCache.containsKey(aa))
            andCache.put(aa, new HashMap<>());
        return andCache.get(aa).computeIfAbsent(bb, x -> a.and(b));
    }

    public static FeatureExpr not(FeatureExpr fe) {
        BDD bdd = ((BDDFeatureExpr) fe).bdd();
        return notCache.computeIfAbsent(bdd, x -> fe.not());
    }
}
