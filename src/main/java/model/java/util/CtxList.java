package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.varex.VHelper;

import java.util.*;
import java.util.LinkedList;


/**
 * Created by lukas on 6/30/17.
 */
public class CtxList<T extends Comparable<T>> implements List {
    LinkedList<FEPair<T>> list = new LinkedList<>();
    V<Integer> size = V.one(FeatureExprFactory.True(), 0);


    public CtxList(FeatureExpr ctx) {}
    public CtxList(){}

    public V<Integer> index____I(FeatureExpr ctx) {
        System.out.println("Warning: Variational index invoked");
        return (V<Integer>)size.select(ctx).map(i -> 0);
    }

//    public V<Integer> size____I() {
//        return V.one(FeatureExprFactory.True(), list.size());
//    }
    public int size() {
        return list.size();
//        return VHelper.explode(VHelper.True(), size____I(VHelper.True())).values().stream().mapToInt(i -> i).sum();
        // I suspect that the size is being obtained in a true ctx,
        // but then it is iterating in the restricted ctx !A
        // because One(T, 5).select(!A) -> One(!A, 5)

        // do I need to return size as a V (below), where each ctx has a size of its own?
    }
    public V<Integer> size____I(FeatureExpr ctx) {
        V<Integer> selected = size.select(ctx);
        return selected;
    }

    public V<Boolean> add(T v, FeatureExpr ctx) {
        list.add(new FEPair<>(ctx, v));
        size = (V<Integer>)size.flatMap((vCtx, s) -> (V<Integer>)V.choice(vCtx.and(ctx), s + 1, s));
        return V.one(ctx, true); // list.add always returns true
    }
    public V<Boolean> add(T v) {
        return add(v, FeatureExprFactory.True());
    }
    public V<Boolean> add__Ljava_lang_Object__Z(V<? extends T> v, FeatureExpr vCtx) {
        return (V<Boolean>)v.sflatMap(vCtx, (ctx, val) -> add(val, ctx));
    }
    public V<Boolean> add(CtxList<T> l) {
        Iterator<FEPair<T>> it = l.iterator();
        while(it.hasNext()) {
            FEPair<T> fePair = it.next();
            add(fePair.v, fePair.ctx);
        }
        return V.one(FeatureExprFactory.True(), true); // list.add always returns true
    }

    public void remove(T v, FeatureExpr ctx) {
        Iterator<FEPair<T>> it = this.iterator();
        while(it.hasNext()) {
            FEPair<T> el = it.next();
            if (el.ctx.and(ctx).isSatisfiable() && el.v == v) {
                el.ctx = el.ctx.andNot(ctx);
                size = (V<Integer>)size.flatMap((vCtx, s) -> (V<Integer>)V.choice(vCtx.and(ctx), s - 1, s));
                break;
            }
        }
    }
    public void removeIndex(int index, FeatureExpr ctx) {
        int i = 0;
        Iterator<FEPair<T>> it = this.iterator();
        while(it.hasNext()) {
            FEPair<T> el = it.next();
            if (el.ctx.and(ctx).isSatisfiable()) {
                if (i == index) {
                    el.ctx = el.ctx.andNot(ctx);
                    size = (V<Integer>)size.flatMap((vCtx, s) -> (V<Integer>)V.choice(vCtx.and(ctx), s - 1, s));
                    break;
                }
                i++;
            }
        }
    }
    public void remove__Ljava_lang_Object__V(V<? extends T> v, FeatureExpr vCtx) {
        v.sforeach(vCtx, (ctx, value) -> remove(value, ctx));
    }
    public void removeIndex__Ljava_lang_Object__V(V<Integer> vIndex, FeatureExpr vCtx) {
        vIndex.sforeach(vCtx, (ctx, index) -> removeIndex(index, ctx));
    }

    private CtxList<T> select(FeatureExpr ctx) {
        CtxList<T> filtered = new CtxList<>();
        for (FEPair<T> p : list) {
            FeatureExpr newCtx = p.ctx.and(ctx);
            if (newCtx.isSatisfiable()) {
                filtered.add(p.v, newCtx);
            }
        }
        return filtered;
    }

    public T get(int index) {
        assert index < list.size(); // "Attempt to access index beyond end of list for selected context"
        return list.get(index).v;
    }

    public T get(int index, FeatureExpr ctx) {
        CtxList<T> filtered = select(ctx);
        int filteredSize = filtered.size();
        if (index < filteredSize) {
            return filtered.get(index);
        }
        else {
            return list.getFirst().v;
        }
    }
    public V<T> get__I__Ljava_lang_Object(V<? extends Integer> vIndex, FeatureExpr indexCtx) {
        V<T> result = (V<T>) vIndex.smap(indexCtx, (ctx, index) -> get(index, ctx));
        return result;
    }

    public CtxIterator ctxIterator(FeatureExpr ctx) {
        CtxList<T> ctxList = select(ctx);
        return new CtxListIterator(ctxList.iterator());
    }
    public Iterator<FEPair<T>> iterator() {
        return list.iterator();
    }
    public V<CtxIterator<T>> iterator____Lmodel_java_util_CtxIterator(FeatureExpr ctx) {
        return V.one(VHelper.True(), new CtxListIterator<>(iterator()));
    }

    /**
     * simplify: Remove duplicate elements and elements with unsatisfiable contexts.
     */
    public void simplify____V() {
        LinkedList<FEPair<T>> simplified = new LinkedList<>();
        Iterator<FEPair<T>> it = list.iterator();
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

    public void sort(Comparator comparator) {
        list.sort((a, b) -> comparator.compare(a.v, b.v));
    }
    public V<?> sort__Lmodel_java_util_Comparator__V(V<Comparator> vComparator, FeatureExpr ctx) {
        CtxList<T> newList = new CtxList<>();
        vComparator.sforeach(ctx, (vCtx, comparator) -> {
            CtxList<T> l = select(vCtx);
            l.sort(comparator);
            newList.add(l);
        });
        list = newList.list;
        return null;
    }
}
