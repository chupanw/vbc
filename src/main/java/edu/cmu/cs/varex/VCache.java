package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import net.sf.javabdd.BDD;

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

    static void clearAll() {
        isSatCache.clear();
        andCache.clear();
        notCache.clear();
    }

    public static boolean isSatisfiable(FeatureExpr fe) {
        BDD bdd = ((BDDFeatureExpr) fe).bdd();
        return isSatCache.computeIfAbsent(bdd, x -> fe.isSatisfiable());
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
