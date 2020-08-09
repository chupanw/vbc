package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.varex.UnimplementedModelClassMethodException;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.varex.VOps;
import edu.cmu.cs.vbc.utils.Profiler;
import model.Contexts;

import java.lang.reflect.Field;
import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author chupanw
 */
public class ArrayList implements List {

    private V<? extends MyArrayList> vActual;
    private MyArrayList actual;
    private boolean asLifted;    // mark whether this class is being used as lifted or not

    /**
     * Split vActual LinkedLists according to current ctx
     */
    private void split(FeatureExpr ctx) {
        V<? extends MyArrayList> selected = vActual.smap(ctx, (Function<MyArrayList, MyArrayList>) t -> new MyArrayList(t));
        vActual = V.choice(ctx, selected, vActual);
    }

    @Override
    public V<? extends MyArrayList> getVCopies(FeatureExpr ctx) {
        return vActual.smap(ctx, l -> new MyArrayList(l));
    }

    public V<?> getVOfArrays(Class c, FeatureExpr ctx) {
        return vActual.smap(ctx, l -> {
            Object[] o = l.toArray();
            return java.util.Arrays.copyOf(o, l.size(), c);
        });
    }

    //////////////////////////////////////////////////
    // Non-V methods
    //////////////////////////////////////////////////
    public ArrayList() {
        actual = new MyArrayList();
        asLifted = false;
    }

    public boolean add(Object o) {
        return actual.add(o);
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean addAll(java.util.Collection c) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean retainAll(java.util.Collection c) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean removeAll(java.util.Collection c) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean containsAll(java.util.Collection c) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public java.util.Iterator iterator() {
        return actual.iterator();
    }

    @Override
    public int size() {
        return actual.size();
    }

    @Override
    public Object get(int index) {
        return actual.get(index);
    }

    @Override
    public Object remove(int index) {
        return actual.remove(index);
    }

    @Override
    public Object[] toArray(Object[] a) {
        return actual.toArray(a);
    }

    @Override
    public Object[] toArray() {
        return actual.toArray();
    }

    @Override
    public void clear() {
        actual.clear();
    }

    @Override
    public boolean contains(Object o) {
        return actual.contains(o);
    }

    @Override
    public Object set(int index, Object element) {
        return actual.set(index, element);
    }

    @Override
    public boolean isEmpty() {
        return actual.isEmpty();
    }

    //////////////////////////////////////////////////
    // V methods
    //////////////////////////////////////////////////

    public ArrayList(FeatureExpr ctx) {
        asLifted = true;
        vActual = V.one(ctx, new MyArrayList());
    }

    public ArrayList(V<Integer> size, FeatureExpr ctx, int dummy) {
        vActual = size.smap(ctx, i -> new MyArrayList(i));
        asLifted = true;
    }

    public ArrayList(V<Collection> vc, FeatureExpr ctx, Collection dummy) {
        vActual = V.one(ctx, new MyArrayList());
        addAll__Lmodel_java_util_Collection__Z(vc, ctx);
    }

    /**
     * In case we don't lift java.util.Collection
     */
    public ArrayList(V<java.util.Collection> vc, FeatureExpr ctx, java.util.Collection dummy) {
        vActual = vc.smap(ctx, MyArrayList::new);
        asLifted = true;
    }

    public V<?> add__Ljava_lang_Object__Z(V<?> elem, FeatureExpr ctx) {
        String id = "ArrayList#add#";
        Profiler.startTimer(id);
        V res = elem.sflatMap(ctx, (fe, e) -> {
            split(fe);
            return vActual.smap(fe, (featureExpr, l) -> l.add(e) ? 1 : 0);
        });
        Profiler.stopTimer(id);
        return res;
    }

    public V ensureCapacity__I__V(V<java.lang.Integer> vI, FeatureExpr ctx) {
        vI.sforeach(ctx, (fe, i) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, l) -> l.ensureCapacity(i));
        });
        return null;    // dummy return
    }

    public V add__I_Ljava_lang_Object__V(V<? extends Integer> vIndex, V<?> vElement, FeatureExpr ctx) {
        vIndex.sforeach(ctx, (fe, index) -> vElement.sforeach(fe, (fe2, element) -> {
            split(fe2);
            vActual.sforeach(fe2, l -> l.add(index, element));
        }));
        return null;    // dummy value, never used
    }

    public V<?> size____I(FeatureExpr ctx) {
        String id = "ArrayList#size#";
        Profiler.startTimer(id);
        V res = vActual.smap(ctx, list -> list.size());
        Profiler.stopTimer(id);
        return res;
    }

    @Override
    public V<?> toArray____Array_Ljava_lang_Object(FeatureExpr ctx) {
        return vActual.smap(ctx, (fe, l) -> {
            V[] res = new V[l.size()];
            for (int i = 0; i < l.size(); i++) {
                res[i] = V.one(fe, l.get(i));
            }
            return res;
        });
    }

    public V<?> get__I__Ljava_lang_Object(V<? extends Integer> index, FeatureExpr ctx) {
        String id = "ArrayList#get#";
        Profiler.startTimer(id);
        V res = vActual.sflatMap(ctx, (fe, list) -> index.smap(fe, i -> list.get(i.intValue())));
        Profiler.stopTimer(id);
        return res;
    }

    public V<?> sort__Lmodel_java_util_Comparator__V(V<Comparator> vComparator, FeatureExpr ctx) {
        String id = "ArrayList#sort#";
        Profiler.startTimer(id);
        vComparator.sforeach(ctx, (fe, c) -> {
            split(fe);
            vActual.sforeach(fe, (fe2, l) -> {
                Contexts.model_java_util_Comparator_compare = fe2;
                if (c != null) {
                    l.sort(c::compare);
                }
                else {
                    l.sort(null);
                }
            });
        });
        Profiler.stopTimer(id);
        return null;
    }

    public V<?> isEmpty____Z(FeatureExpr ctx) {
        String id = "ArrayList#isEmpty#";
        Profiler.startTimer(id);
        V res = vActual.smap(ctx, list -> list.isEmpty());
        Profiler.stopTimer(id);
        return res;
    }

    public V<? extends java.util.Iterator> iterator____Ljava_util_Iterator(FeatureExpr ctx) {
        String id = "ArrayList#iterator#";
        Profiler.startTimer(id);
        V res = vActual.smap(ctx, l -> l.iterator());
        Profiler.stopTimer(id);
        return res;
    }

    @Override
    public V<?> listIterator____Lmodel_java_util_ListIterator(FeatureExpr ctx) {
        V<MyArrayList.MyListItr> vI = (V<MyArrayList.MyListItr>) vActual.smap(ctx, l -> l.listIterator());
        return V.one(ctx, new ArrayListListIteratorImpl(vI));
    }

    @Override
    public V<?> listIterator__I__Lmodel_java_util_ListIterator(V<? extends Integer> vI, FeatureExpr ctx) {
        V<MyArrayList.MyListItr> vItr = (V<MyArrayList.MyListItr>) vI.sflatMap(ctx, (fe, i) -> vActual.smap(fe, l -> l.listIterator(i)));
        return V.one(ctx, new ArrayListListIteratorImpl(vItr));
    }

    @Override
    public V<?> containsAll__Lmodel_java_util_Collection__Z(V<?> vObjects, FeatureExpr ctx) {
        return vObjects.sflatMap(ctx, (fe, object) -> {
            Collection collection = (Collection) object;
            V<Iterator> iterator = (V<Iterator>) collection.iterator____Lmodel_java_util_Iterator(fe);
            return iterator.sflatMap(fe, (fe2, itr) -> {
                V<Integer> ret = V.one(FeatureExprFactory.True(), 1);
                while (true) {
                    V<?> hasNext = itr.hasNext____Z(fe2);
                    FeatureExpr fe3 = hasNext.when(x -> {
                        if (x instanceof Boolean) return x.equals(true);
                        return x.equals(1);
                    }, true);
                    if (fe3.equivalentTo(FeatureExprFactory.False())) {
                        break;
                    }
                    V element = itr.next____Ljava_lang_Object(fe3);
                    V<Integer> contained = (V<Integer>) contains__Ljava_lang_Object__Z(element, fe3);
                    ret = (V<Integer>) VOps.iand(ret, contained, fe3);
                }
                return ret;
            });
        });
    }

    @Override
    public V<?> iterator____Lmodel_java_util_Iterator(FeatureExpr ctx) {
        V<MyArrayList.MyItr> vI = (V<MyArrayList.MyItr>) vActual.smap(ctx, l -> l.iterator());
        return V.one(ctx, new ArrayListIteratorImpl(vI));
    }

    public V<? extends Boolean> remove__Ljava_lang_Object__Z(V<?> vo, FeatureExpr ctx) {
        String id = "ArrayList#remove#";
        Profiler.startTimer(id);
        V res = vo.sflatMap(ctx, (fe, o) -> {
            split(fe);
            return vActual.smap(fe, l -> l.remove(o));
        });
        Profiler.stopTimer(id);
        return res;
    }

    public V<?> remove__I__Ljava_lang_Object(V<? extends Integer> vIndex, FeatureExpr ctx) {
        return vIndex.sflatMap(ctx, (fe, i) -> {
            split(fe);
            return vActual.smap(fe, list -> list.remove(i.intValue()));
        });
    }

    @Override
    public V<?> subList__I_I__Lmodel_java_util_List(V<? extends Integer> vBegin, V<? extends Integer> vEnd, FeatureExpr ctx) {
        return vBegin.sflatMap(ctx, (fe, begin) -> vEnd.sflatMap(fe, (fe2, end) -> (V<?>) vActual.smap(fe2, (fe3, actual) -> actual.subList(begin, end))));
    }

    @Override
    public V<?> addAll__Ljava_util_Collection__Z(V<? extends java.util.Collection> vCollection, FeatureExpr ctx) {
        return vCollection.sflatMap(ctx, (fe, collection) -> {
            split(fe);
            return vActual.smap(fe, l -> l.addAll(collection));
        });
    }

    @Override
    public V<?> addAll__Lmodel_java_util_Collection__Z(V<?> vCollection, FeatureExpr ctx) {
        return vCollection.sflatMap(ctx, (fe, collection) -> {
            if (collection instanceof model.java.util.Collection) {
                Collection vcollection = (model.java.util.Collection) collection;
                V<V[]> varray = (V<V[]>) vcollection.toArray____Array_Ljava_lang_Object(fe);
                return varray.sflatMap(fe, (fe2, array) -> {
                    V<?> ret = V.one(FeatureExprFactory.True(), 0);
                    for (int i = 0; i < array.length; i++) {
                        V<?> changed = add__Ljava_lang_Object__Z(array[i], fe2);
                        ret = VOps.ior(ret, changed, fe2);
                    }
                    return ret;
                });
            }
            else {
                split(fe);
                return vActual.smap(fe, (fe2, l) -> l.addAll((java.util.Collection) collection));
            }
        });
    }

    @Override
    public V<?> removeAll__Lmodel_java_util_Collection__Z(V<?> vCollection, FeatureExpr ctx) {
        return vCollection.sflatMap(ctx, (fe, collection) -> {
            Collection vcollection = (model.java.util.Collection) collection;
            V<V[]> varray = (V<V[]>) vcollection.toArray____Array_Ljava_lang_Object(fe);
            return varray.sflatMap(fe, (fe2, array) -> {
                V<?> ret = V.one(FeatureExprFactory.True(), 0);
                for (int i = 0; i < array.length; i++) {
                    V<?> changed = remove__Ljava_lang_Object__Z(array[i], fe2);
                    ret = VOps.ior(ret, changed, fe2);
                }
                return ret;
            });
        });
    }

    @Override
    public V<?> indexOf__Ljava_lang_Object__I(V<?> vObject, FeatureExpr ctx) {
        return vObject.sflatMap(ctx, (fe, o) -> vActual.smap(fe, l -> l.indexOf(o)));
    }

    public V<?> clear____V(FeatureExpr ctx) {
        String id = "ArrayList#clear#";
        Profiler.startTimer(id);
        split(ctx);
        vActual.sforeach(ctx, l -> l.clear());
        Profiler.stopTimer(id);
        return null;
    }

    @Override
    public V<?> hashCode____I(FeatureExpr ctx) {
        throw new UnimplementedModelClassMethodException("hashCode");
    }

    @Override
    public V<?> contains__Ljava_lang_Object__Z(V<?> vO, FeatureExpr ctx) {
        String id = "ArrayList#contains#";
        Profiler.startTimer(id);
        V res = vActual.sflatMap(ctx, (fe, l) -> vO.smap(fe, o -> l.contains(o)));
        Profiler.stopTimer(id);
        return res;
    }

    public V<?> set__I_Ljava_lang_Object__Ljava_lang_Object(V<? extends Integer> vI, V<?> vO, FeatureExpr ctx) {
        String id = "ArrayList#set#";
        Profiler.startTimer(id);
        V res = vI.sflatMap(ctx, (fe, i) -> vO.sflatMap(fe, (fe2, o) -> {
            split(fe2);
            return (V) vActual.smap(fe2, l -> l.set(i, o));
        }));
        Profiler.stopTimer(id);
        return res;
    }

    /**
     * Probably not efficient, but we need to wrap elements into Vs.
     *
     * perf: maybe use System.arrayCopy for larger arrays?
     */
    public V toArray__Array_Ljava_lang_Object__Array_Ljava_lang_Object(V<V[]> a, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> vActual.smap(fe, (fe2, l) -> {
            Object[] elements = l.toArray();
            V[] destArray = aa;
            if (aa.length < elements.length) {
                destArray = new V[elements.length];
            }
            for (int i = 0; i < elements.length; i++) {
                destArray[i] = V.one(fe2, elements[i]);
            }
            return destArray;
        }));
    }
}

/**
 * Override equals() for splitting
 */
class MyArrayList extends java.util.ArrayList {
    MyArrayList(int size) {
        super(size);
    }
    MyArrayList() {
        super();
    }
    MyArrayList(MyArrayList origin) {
        super(origin);
    }
    MyArrayList(java.util.Collection c) { super(c); }

    transient Object[] elementData;

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public java.util.Iterator iterator() {
        return new MyItr();
    }

    @Override
    public ListIterator listIterator() {
        return new MyListItr(0);
    }

    @Override
    public ListIterator listIterator(int i) {
        return new MyListItr(i);
    }

    /**
     * An optimized version of AbstractList.Itr
     */
    class MyItr implements java.util.Iterator {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        MyItr() {}

        public MyItr(int cursor, int lastRet, int expectedModCount) {
            this.cursor = cursor;
            this.lastRet = lastRet;
            this.expectedModCount = expectedModCount;
        }

        public MyItr clone() {
            return new MyItr(cursor, lastRet, expectedModCount);
        }

        public boolean hasNext() {
            return cursor != size();
        }

        @SuppressWarnings("unchecked")
        public Object next() {
            checkForComodification();
            int i = cursor;
            if (i >= size())
                throw new NoSuchElementException();
            try {
                Field fElementData = MyArrayList.this.getClass().getSuperclass().getDeclaredField("elementData");
                fElementData.setAccessible(true);
                Object[] elementData = (Object[]) fElementData.get(MyArrayList.this);
                if (i >= elementData.length)
                    throw new ConcurrentModificationException();
                cursor = i + 1;
                return elementData[lastRet = i];
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                MyArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer consumer) {
            Objects.requireNonNull(consumer);
            final int size = MyArrayList.this.size();
            int i = cursor;
            if (i >= size) {
                return;
            }
            try {
                Field fElementData = MyArrayList.this.getClass().getSuperclass().getDeclaredField("elementData");
                fElementData.setAccessible(true);
                final Object[] elementData = (Object[]) fElementData.get(MyArrayList.this);
                if (i >= elementData.length) {
                    throw new ConcurrentModificationException();
                }
                while (i != size && modCount == expectedModCount) {
                    consumer.accept(elementData[i++]);
                }
                // update once at end of iteration to reduce heap write traffic
                cursor = i;
                lastRet = i - 1;
                checkForComodification();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    class MyListItr extends MyItr implements java.util.ListIterator {
        MyListItr(int index) {
            super();
            cursor = index;
        }

        private MyListItr(int cursor, int lastRet, int expectedModCount) {
            this.cursor = cursor;
            this.lastRet = lastRet;
            this.expectedModCount = expectedModCount;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public MyListItr clone() {
            return new MyListItr(cursor, lastRet, expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public Object previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            try {
                Field fElementData = MyArrayList.this.getClass().getSuperclass().getDeclaredField("elementData");
                fElementData.setAccessible(true);
                Object[] elementData = (Object[]) fElementData.get(MyArrayList.this);
                if (i >= elementData.length)
                    throw new ConcurrentModificationException();
                cursor = i;
                return elementData[lastRet = i];
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        public void set(Object e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                MyArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(Object e) {
            checkForComodification();

            try {
                int i = cursor;
                MyArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }
}

class ArrayListIteratorImpl implements model.java.util.Iterator {

    protected V<? extends MyArrayList.MyItr> vActual;
    protected MyArrayList.MyItr actual;

    ArrayListIteratorImpl(V<?> vI) {
        vActual = (V<MyArrayList.MyItr>) vI;
    }

    //////////////////////////////////////////////////
    // V methods
    //////////////////////////////////////////////////

    protected void split(FeatureExpr ctx) {
        V<MyArrayList.MyItr> selected = (V<MyArrayList.MyItr>) vActual.smap(ctx, (fe, t) -> t.clone());
        vActual = V.choice(ctx, selected, vActual);
    }

    @Override
    public V<?> hasNext____Z(FeatureExpr ctx) {
        return vActual.smap(ctx, (fe, i) -> i.hasNext());
    }

    @Override
    public V<?> next____Ljava_lang_Object(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, (fe, i) -> i.next());
    }

    @Override
    public V<?> remove____V(FeatureExpr ctx) {
        split(ctx);
        vActual.sforeach(ctx, (fe, i) -> i.remove());
        return null; //dummy
    }
}

class ArrayListListIteratorImpl extends ArrayListIteratorImpl implements model.java.util.ListIterator {

    ArrayListListIteratorImpl(V<?> vI) {
        super(vI);
    }

    @Override
    public V<?> hasPrevious____Z(FeatureExpr ctx) {
        return vActual.smap(ctx, (fe, i) -> ((MyArrayList.MyListItr) i).hasPrevious());
    }

    @Override
    public V<?> previous____Ljava_lang_Object(FeatureExpr ctx) {
        split(ctx);
        return vActual.smap(ctx, (fe, itr) -> ((MyArrayList.MyListItr) itr).previous());
    }
}
