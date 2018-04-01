package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.varex.VHelper;

import java.util.*;
import java.util.LinkedList;
import java.util.stream.Collectors;


/**
 * Created by lukas on 6/30/17.
 */
public class CtxList<T> implements List {
    LinkedList<FEPair<T>> list = new LinkedList<>();


    public CtxList(FeatureExpr ctx) {}
    public CtxList(){}

    public int size() {
        return list.size();
    }
    public V<Integer> size____I(FeatureExpr ctx) {
        V<Integer> size = V.one(ctx, 0);
        CtxIterator<T> it = select(ctx).iterator();
        while (it.hasNext()) {
            FEPair<T> el = it.next____Ljava_lang_Object(FeatureExprFactory.True()).getOne();
            size = (V<Integer>)size.flatMap((vCtx, s) -> (V<Integer>)V.choice(vCtx.and(el.ctx), s + 1, s));
        }
        return size;
    }

    public V<Boolean> add(T v, FeatureExpr ctx) {
        list.add(new FEPair<>(ctx, v));
        return V.one(ctx, true); // list.add always returns true
    }
    public void add(int i, Object v) {
        list.add(i, new FEPair(FeatureExprFactory.True(), v));
    }
    public boolean add(Object v) {
        return list.add(new FEPair(FeatureExprFactory.True(), v));
    }
    public V<Boolean> add__Ljava_lang_Object__Z(V<? extends T> v, FeatureExpr vCtx) {
        return (V<Boolean>)v.sflatMap(vCtx, (ctx, val) -> add(val, ctx));
    }
    public V<Boolean> add(CtxList<T> l) {
        Iterator<FEPair<T>> it = l.list.iterator();
        while(it.hasNext()) {
            FEPair<T> fePair = it.next();
            add(fePair.v, fePair.ctx);
        }
        return V.one(FeatureExprFactory.True(), true); // list.add always returns true
    }

    public boolean remove(Object v) {
        Iterator<FEPair<T>> it = list.iterator();
        while(it.hasNext()) {
            FEPair<T> el = it.next();
            if (el.v == v) {
                el.ctx = FeatureExprFactory.False();
                return true;
            }
        }
        return false;
    }
    public V<Boolean> remove(T v, FeatureExpr ctx) {
        Iterator<FEPair<T>> it = list.iterator();
        V<Boolean> result = V.one(ctx, false);
        FeatureExpr removeCtx = ctx;
        while(it.hasNext()) {
            FEPair<T> el = it.next();
            FeatureExpr combinedCtx = el.ctx.and(removeCtx);
            if (combinedCtx.isSatisfiable()) {
                result = (V<Boolean>)result.flatMap((FeatureExpr resCtx, Boolean resVal) -> {
                    if (el.v == v) {
                        el.ctx = el.ctx.andNot(ctx);
                        return V.choice(combinedCtx, true, resVal);
                    } else {
                        return V.one(resCtx, resVal);
                    }
                });
                removeCtx = result.when(t -> !t, false);
            }
        }
        return result;
    }

    public T remove(int index) {
        if (list.size() <= index) {
            return null;
        }
        FEPair<T> el = list.get(index);
        el.ctx = FeatureExprFactory.False();
        return el.v;
    }
    public V<T> removeIndex(int index, FeatureExpr ctx) {
        V<Integer> i = V.one(ctx, 0);
        V<T> returnValue = V.one(ctx, null);
        Iterator<FEPair<T>> it = list.iterator();
        FeatureExpr removeIndexCtx = ctx;
        while(it.hasNext()) {
            FEPair<T> el = it.next();
            FeatureExpr combinedCtx = el.ctx.and(removeIndexCtx);
            if (combinedCtx.isSatisfiable()) {
                final V<Integer> finalI = i;
                returnValue = (V<T>)returnValue.flatMap((FeatureExpr retCtx, T retVal) -> finalI.sflatMap(retCtx, (FeatureExpr iCtx, Integer iVal) -> {
                    if (iVal == index) {
                        el.ctx = el.ctx.andNot(combinedCtx);
                        return (V<T>)V.choice(iCtx.and(combinedCtx), el.v, retVal);
                    } else {
                        return V.one(retCtx, retVal);
                    }
                }));
                removeIndexCtx = returnValue.when(t -> t == null, false);
                i = (V<Integer>)i.flatMap((FeatureExpr vCtx, Integer iVal) ->
                        V.choice(vCtx.and(combinedCtx), iVal + 1, iVal));
            }
        }
        return returnValue;
    }
    public V<Boolean> remove__Ljava_lang_Object__Z(V<? extends T> v, FeatureExpr vCtx) {
        return (V<Boolean>)v.sflatMap(vCtx, (ctx, value) -> remove(value, ctx));
    }
    public V<T> remove__I__Ljava_lang_Object(V<Integer> vIndex, FeatureExpr vCtx) {
        return (V<T>)vIndex.sflatMap(vCtx, (ctx, index) -> removeIndex(index, ctx));
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

    public V<T> get(int index, FeatureExpr ctx) {
        V<Integer> i = V.one(ctx, 0);
        V<T> returnValue = V.one(ctx, null);
        Iterator<FEPair<T>> it = list.iterator();
        FeatureExpr getIndexCtx = ctx;
        while(it.hasNext()) {
            FEPair<T> el = it.next();
            FeatureExpr combinedCtx = el.ctx.and(getIndexCtx);
            if (combinedCtx.isSatisfiable()) {
                final V<Integer> finalI = i;
                returnValue = (V<T>)returnValue.flatMap((FeatureExpr retCtx, T retVal) -> finalI.sflatMap(retCtx, (FeatureExpr iCtx, Integer iVal) -> {
                    if (iVal == index) {
                        return (V<T>)V.choice(iCtx.and(combinedCtx), el.v, retVal);
                    } else {
                        return V.one(retCtx, retVal);
                    }
                }));
                getIndexCtx = returnValue.when(t -> t == null, false);
                i = (V<Integer>)i.flatMap((FeatureExpr vCtx, Integer iVal) ->
                        V.choice(vCtx.and(combinedCtx), iVal + 1, iVal));
            }
        }
        return returnValue;
    }
    public V<T> get__I__Ljava_lang_Object(V<? extends Integer> vIndex, FeatureExpr indexCtx) {
        return (V<T>) vIndex.sflatMap(indexCtx, (ctx, index) -> get(index, ctx));
    }

    public CtxIterator ctxIterator(FeatureExpr ctx) {
        CtxList<T> ctxList = select(ctx);
        return new CtxListIterator(ctxList.iterator());
    }
    public CtxIterator<T> iterator() {
        return new CtxListIterator<>(list.iterator());
    }
    public V<CtxIterator<T>> iterator____Lmodel_java_util_CtxIterator(FeatureExpr ctx) {
        return V.one(ctx, new CtxListIterator<>(list.iterator()));
    }
    public ListIterator<T> listIterator(int i) {
        // todo: Implement this
        // This is dummy code to make the types match
        List<T> x = list.stream().map(fepair -> fepair.v).collect(Collectors.toList());
        return x.listIterator();
    }
    public ListIterator<T> listIterator() {
        // todo: Implement this
        return listIterator(0);
    }

     // simplify: Remove duplicate elements and elements with unsatisfiable contexts.
    public void simplify____V() {
        if (list.size() == 0)
            return;

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

    public int indexOf(Object o) {
        int i = 0;
        CtxIterator<T> it = this.iterator();
        while (it.hasNext()) {
            T el = it.next();
            if (o.equals(el))
                return i;
            i++;
        }
        return -1;
    }
    public V<Integer> indexOf(Object o, FeatureExpr ctx) {
        V<Integer> i = V.one(ctx, 0);
        V<Integer> returnValue = V.one(ctx, -1);
        Iterator<FEPair<T>> it = list.iterator();
        FeatureExpr getIndexCtx = ctx;
        while(it.hasNext()) {
            FEPair<T> el = it.next();
            FeatureExpr combinedCtx = el.ctx.and(getIndexCtx);
            if (combinedCtx.isSatisfiable()) {
                final V<Integer> finalI = i;
                returnValue = (V<Integer>)returnValue.flatMap((FeatureExpr retCtx, Integer retVal) -> finalI.sflatMap(retCtx, (FeatureExpr iCtx, Integer iVal) -> {
                    if (o.equals(el.v)) {
                        return (V<Integer>)V.choice(iCtx.and(combinedCtx), iVal, retVal);
                    } else {
                        return V.one(retCtx, retVal);
                    }
                }));
                getIndexCtx = returnValue.when(t -> t == -1, false);
                i = (V<Integer>)i.flatMap((FeatureExpr vCtx, Integer iVal) ->
                        V.choice(vCtx.and(combinedCtx), iVal + 1, iVal));
            }
        }
        return returnValue;
    }
    public V<Integer> indexOf__Ljava_lang_Object__I(V<Object> vObj, FeatureExpr ctx) {
        return (V<Integer>)vObj.sflatMap(ctx, (FeatureExpr objCtx, Object obj) -> indexOf(obj, objCtx));
    }

    public int lastIndexOf(Object o) {
        int i = 0;
        int index = -1;
        CtxIterator<T> it = this.iterator();
        while (it.hasNext()) {
            T el = it.next();
            if (o.equals(el))
                index = i;
            i++;
        }
        return index;
    }

    public CtxList<T> subList(int start, int end) {
        assert false;
        return new CtxList<>();
    }

    public T set(int i, Object o) {
        assert false;
        return list.set(i, (FEPair<T>)o).v;
    }

    public void clear() {
        assert false;
        list.clear();
    }

    public boolean retainAll(Collection c) {
        assert false;
        return list.retainAll(c);
    }

    public boolean removeAll(Collection c) {
        assert false;
        return list.removeAll(c);
    }

    public boolean addAll(Collection c) {
        assert false;
        return list.addAll(c);
    }
    public boolean addAll(int i, Collection c) {
        assert false;
        return list.addAll(i, c);
    }

    public boolean containsAll(Collection c) {
        assert false;
        return list.containsAll(c);
    }

    public Object[] toArray(Object[] a) {
        assert false;
        return a;
    }
    public Object[] toArray() {
        assert false;
        Object a[] = new Object[]{};
        return a;
    }

    public boolean contains(Object c) {
        assert false;
        return list.contains(c);
    }
    public boolean isEmpty() {
        assert false;
        return list.isEmpty();
    }
}
