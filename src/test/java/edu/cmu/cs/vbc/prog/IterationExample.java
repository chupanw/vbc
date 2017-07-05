package edu.cmu.cs.vbc.prog;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import edu.cmu.cs.varex.V;
import edu.cmu.cs.varex.VHelper;
import edu.cmu.cs.varex.annotation.VConditional;
import model.java.util.CtxList;
import model.java.util.FEPair;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * Created by lukas on 6/29/17.
 */
public class IterationExample {
    @VConditional
    static boolean A = true;
    @VConditional
    static boolean B = false;

    public static void main(String[] args) {
        automatic();
//        manual();
    }

    // Put everything in a method because all contexts in main coerced to True
    public static void automatic() {
        ArrayList<Integer> l = new ArrayList<>();
        if (A) {
            l.add(1);
        }
        if (B) {
            l.add(2);
        }
        else {
            l.add(2);
        }
        if (A) {
            l.add(3);
        }

        l.add(4);
        l.add(5);

        if (B) {
            l.add(6);
        }

        for (int i = 0; i < l.size(); i++) {
            System.out.println(l.get(i));
        }
   }

    public static void manual() {
//        V<ArrayList<Integer>> l = V.one(FeatureExprFactory.True(), new ArrayList<>());
        FeatureExpr a = FeatureExprFactory.createDefinedExternal("A");
        FeatureExpr b = FeatureExprFactory.createDefinedExternal("B");
//        l = (V<ArrayList<Integer>>) V.choice(a, l.smap(a, (ArrayList<Integer> list) -> {
//            ArrayList<Integer> newlist = new ArrayList<>(list);
//            newlist.add(1);
//            return newlist;
//        }), l.select(a.not()));
//        l = (V<ArrayList<Integer>>) V.choice(b, l.smap(b, (ArrayList<Integer> list) -> {
//            ArrayList<Integer> newlist = new ArrayList<>(list);
//            newlist.add(2);
//            return newlist;
//        }), l.select(b.not()));
//        l = (V<ArrayList<Integer>>) V.choice(b.not(), l.smap(b.not(), (ArrayList<Integer> list) -> {
//            ArrayList<Integer> newlist = new ArrayList<>(list);
//            newlist.add(2);
//            return newlist;
//        }), l.select(b));
//        l = (V<ArrayList<Integer>>) V.choice(a, l.smap(a, (ArrayList<Integer> list) -> {
//            ArrayList<Integer> newlist = new ArrayList<>(list);
//            newlist.add(3);
//            return newlist;
//        }), l.select(a.not()));
//
//        l.foreach(list -> list.add(4));
//        l.foreach(list -> list.add(5));
//
//        l = (V<ArrayList<Integer>>) V.choice(b, l.smap(b, (ArrayList<Integer> list) -> {
//            ArrayList<Integer> newlist = new ArrayList<>(list);
//            newlist.add(6);
//            return newlist;
//        }), l.select(b.not()));
//
//        System.out.println(l);
//
//        l.foreach(list -> {
//            for (int i = 0; i < list.size(); i++) {
//                System.out.println(list.get(i));
//                //                                     A^B          A^!B            !A^B       !A^!B
//            } // should print something like        1 2 3 4 5 6   1 2 3 4 5        2 4 5 6     2 4 5
//        });
//
//        System.out.println("------------------");
//
//        BiFunction<String, Integer, V<Optional<Integer>>> newChoice = (s, i) ->
//                (V<Optional<Integer>>)V.choice(FeatureExprFactory.createDefinedExternal(s), Optional.of(i), Optional.empty());
//        BiFunction<String, Integer, V<Optional<Integer>>> newChoiceNot = (s, i) ->
//                (V<Optional<Integer>>)V.choice(FeatureExprFactory.createDefinedExternal(s).not(), Optional.of(i), Optional.empty());
//        Function<Integer, V<Optional<Integer>>> newOne = i ->
//                (V<Optional<Integer>>)V.one(FeatureExprFactory.True(), Optional.of(i));
//
//        ArrayList<V<Optional<Integer>>> l2 = new ArrayList<>();
//        l2.add(newChoice.apply("A", 1));
//        l2.add(newChoice.apply("B", 2));
//        l2.add(newChoiceNot.apply("B", 2));
//        l2.add(newChoice.apply("A", 3));
//        l2.add(newOne.apply(4));
//        l2.add(newOne.apply(5));
//        l2.add(newChoice.apply("B", 6));
//
////        l2 = l2.simplify(); // remove e.g. element 2 redundance
//        for (V<Optional<Integer>> el : l2) {
//            FeatureExpr thisCtx = optionalConfigSpace(el);
//            el.sforeach(thisCtx, oi -> System.out.println(oi.get() + ": " + thisCtx));
//        }
//
//
//        System.out.println("------------------");
//
//        ArrayList<Pair<FeatureExpr, Integer>> l3 = new ArrayList<>();
//        BiFunction<String, Integer, Pair<FeatureExpr, Integer>> newPair = (s, i) -> new Pair(FeatureExprFactory.createDefinedExternal(s), i);
//        BiFunction<String, Integer, Pair<FeatureExpr, Integer>> newPairNot = (s, i) -> new Pair(FeatureExprFactory.createDefinedExternal(s).not(), i);
//        Function<Integer, Pair<FeatureExpr, Integer>> newPairTrue = (i) -> new Pair(FeatureExprFactory.True(), i);
//        l3.add(newPair.apply("A", 1));
//        l3.add(newPair.apply("B", 2));
//        l3.add(newPairNot.apply("B", 2));
//        l3.add(newPair.apply("A", 3));
//        l3.add(newPairTrue.apply(4));
//        l3.add(newPairTrue.apply(5));
//        l3.add(newPair.apply("B", 6));
//
//        FeatureExpr currentCtx = FeatureExprFactory.True();
//        for (Pair<FeatureExpr, Integer> el : l3) {
//            FeatureExpr thisCtx = el.getFirst().and(currentCtx);
//            Integer value = el.getSecond();
//            System.out.println(value + ": " + thisCtx);
//        }
//
//        System.out.println("------------------");

        CtxList<Integer> l4 = new CtxList<>();
        l4.add(a, 1);
        l4.add(b, 2);
        l4.add(b.not(), 2);
        l4.add(a, 3);
        l4.add(4);
        l4.add(5);
        l4.add(b, 6);
//        l4.add(b.and(b.not()), 7);
//        l4.add(7);
//        l4.add(b, 7);

        l4.simplify();
        for (FEPair<Integer> p : l4) {
            System.out.println(p);
        }
    }


    static FeatureExpr optionalConfigSpace(V<Optional<Integer>> v) {
        FeatureExpr theSpace = FeatureExprFactory.False();
        V<FeatureExpr> ctxs = (V<FeatureExpr>)v.map((FeatureExpr ctx, Optional<Integer> val) -> val.isPresent() ? ctx : FeatureExprFactory.False());
        Map<FeatureExpr, FeatureExpr> ctxMap = VHelper.explode(FeatureExprFactory.True(), ctxs);
        for (FeatureExpr ctx : ctxMap.values()) {
            theSpace = theSpace.or(ctx);
        }
        return theSpace;
    }
}
