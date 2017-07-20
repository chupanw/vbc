package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.varex.VHelper;

import java.util.Iterator;

/**
 * Created by lukas on 7/14/17.
 */
public class CtxListIterator<T> implements CtxIterator<T> {
    Iterator<FEPair<T>> feIt;
    public CtxListIterator(Iterator<FEPair<T>> it) {
            feIt = it;
        }

    public V<FEPair<T>> next____Ljava_lang_Object(FeatureExpr ctx) {
        return V.one(VHelper.True(), feIt.next());
    }
    public T next() {
        return feIt.next().v;
    }

    public boolean hasNext() {
        return feIt.hasNext();
    }
    public V<Boolean> hasNext____Z(FeatureExpr ctx) {
            return V.one(VHelper.True(), hasNext());
        }
}