package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.varex.V;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;


/**
 * Created by lukas on 6/30/17.
 */
public class CtxList<T> implements Iterable<FEPair<T>> {
    class CtxIterator<T> implements Iterator<T> {
        Iterator<FEPair<T>> pairIt;
        public CtxIterator(Iterator<FEPair<T>> it){
            pairIt = it;
        }

        public T next() {
            return pairIt.next().v;
        }

        public boolean hasNext() {
            return pairIt.hasNext();
        }
    }


    ArrayList<FEPair<T>> list = new ArrayList<>();


    public int length() {
        return list.size();
    }

    public boolean add(FeatureExpr ctx, T v) {
        return list.add(new FEPair<>(ctx, v));
    }
    public boolean add(T v) {
        return add(FeatureExprFactory.True(), v);
    }
    public void add(V<? extends T> v) {
        v.foreach((ctx, val) -> list.add(new FEPair<>(ctx, val)));
    }

    public void remove(FeatureExpr ctx, T v) {
        for (FEPair<T> el : this) {
            if (el.ctx.and(ctx).isSatisfiable() && el.v == v) {
                el.ctx = el.ctx.andNot(ctx);
                break;
            }
        }
    }
    public void removeIndex(FeatureExpr ctx, int index) {
        int i = 0;
        for (FEPair<T> el : this) {
            if (el.ctx.and(ctx).isSatisfiable()) {
                if (i == index) {
                    el.ctx = el.ctx.andNot(ctx);
                    break;
                }
                i++;
            }
        }
    }

    private CtxList<T> select(FeatureExpr ctx) {
        CtxList<T> filtered = new CtxList<>();
        for (FEPair<T> p : list) {
            FeatureExpr newCtx = p.ctx.and(ctx);
            if (newCtx.isSatisfiable()) {
                filtered.add(newCtx, p.v);
            }
        }
        return filtered;
    }

    private T getValue(int index) {
        assert index < list.size(); // "Attempt to access index beyond end of list for selected context"
        return list.get(index).v;
    }

    public T get(FeatureExpr ctx, int index) {
        CtxList<T> filtered = select(ctx);
        return filtered.getValue(index);
    }

    public CtxIterator ctxIterator() {
        return new CtxIterator(list.iterator());
    }
    public Iterator<FEPair<T>> iterator() {
        return list.iterator();
    }

    /**
     * simplify: Remove duplicate elements and elements with unsatisfiable contexts.
     */
    public void simplify() {
        ArrayList<FEPair<T>> simplified = new ArrayList<>();
        ListIterator<FEPair<T>> it = list.listIterator();
        FEPair<T> first = it.next();
        while(it.hasNext()) {
            final FEPair<T> second = it.next();
            if (!first.ctx.isSatisfiable()) {
                // discard unsatisfiable element
                first = second;
                continue;
            }
            if (first.v == second.v && !first.ctx.and(second.ctx).isSatisfiable()) {
                // can merge equal elements with mutually exclusive contexts
                first = new FEPair(first.ctx.or(second.ctx), first.v);
            }
            else {
                simplified.add(first);
                first = second;
            }
        }
        if (first.ctx.isSatisfiable()) {
            simplified.add(first); // last element
        }

        list = simplified;
    }
}
