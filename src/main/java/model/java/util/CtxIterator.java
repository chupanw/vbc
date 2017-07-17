package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

import java.util.Iterator;

/**
 * Created by lukas on 7/14/17.
 */
public interface CtxIterator<T> extends Iterator {
    V<FEPair<T>> next____Ljava_lang_Object(FeatureExpr ctx);
//    FEPair<T> next____Ljava_lang_Object(FeatureExpr ctx);
    FEPair<T> next();
    boolean hasNext();
    V<Boolean> hasNext____Z(FeatureExpr ctx);
}
