package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

import java.lang.reflect.Field;

public class Scanner {
    private V<? extends String> vActual;

    public Scanner(V<String> vSources, FeatureExpr ctx, String dummy) {
        vActual = vSources.select(ctx);
    }

    public V<? extends Integer> nextInt____I(FeatureExpr ctx) {
        V<? extends MyPair> pairs = vActual.smap(ctx, (fe, actual) -> {
            java.util.Scanner scanner = new java.util.Scanner(actual);
            String ret = String.valueOf(scanner.nextInt());
            int index = readPositionField(scanner);
            String res = actual.substring(index);
            return new MyPair(ret, res);
        });
        V<? extends String> res = pairs.smap(ctx, (fe, pair) -> pair.res);
        vActual = V.choice(ctx, res, vActual);
        V<? extends Integer> ret = pairs.smap(ctx, (fe, pair) -> Integer.valueOf(pair.ret));
        return ret;
    }

    public V<? extends Double> nextDouble____D(FeatureExpr ctx) {
        V<? extends MyPair> pairs = vActual.smap(ctx, (fe, actual) -> {
            java.util.Scanner scanner = new java.util.Scanner(actual);
            String ret = String.valueOf(scanner.nextDouble());
            int index = readPositionField(scanner);
            String res = actual.substring(index);
            return new MyPair(ret, res);
        });
        V<? extends String> res = pairs.smap(ctx, (fe, pair) -> pair.res);
        vActual = V.choice(ctx, res, vActual);
        V<? extends Double> ret = pairs.smap(ctx, (fe, pair) -> Double.valueOf(pair.ret));
        return ret;
    }

    public V<? extends Float> nextFloat____F(FeatureExpr ctx) {
        V<? extends MyPair> pairs = vActual.smap(ctx, (fe, actual) -> {
            java.util.Scanner scanner = new java.util.Scanner(actual);
            String ret = String.valueOf(scanner.nextFloat());
            int index = readPositionField(scanner);
            String res = actual.substring(index);
            return new MyPair(ret, res);
        });
        V<? extends String> res = pairs.smap(ctx, (fe, pair) -> pair.res);
        vActual = V.choice(ctx, res, vActual);
        V<? extends Float> ret = pairs.smap(ctx, (fe, pair) -> Float.valueOf(pair.ret));
        return ret;
    }

    public V<? extends String> next____Ljava_lang_String(FeatureExpr ctx) {
        V<? extends MyPair> pairs = vActual.smap(ctx, (fe, actual) -> {
            java.util.Scanner scanner = new java.util.Scanner(actual);
            String ret = scanner.next();
            int index = readPositionField(scanner);
            String res = actual.substring(index);
            return new MyPair(ret, res);
        });
        V<? extends String> res = pairs.smap(ctx, (fe, pair) -> pair.res);
        vActual = V.choice(ctx, res, vActual);
        V<? extends String> ret = pairs.smap(ctx, (fe, pair) -> pair.ret);
        return ret;
    }

    public V<? extends String> findInLine__Ljava_lang_String__Ljava_lang_String(V<? extends String> vS, FeatureExpr ctx) {
        V<? extends MyPair> pairs = vActual.sflatMap(ctx, (fe, actual) -> {
            return vS.smap(fe, (fe2, s) -> {
                java.util.Scanner scanner = new java.util.Scanner(actual);
                String ret = scanner.findInLine(s);
                int index = readPositionField(scanner);
                String res = actual.substring(index);
                return new MyPair(ret, res);
            });
        });
        V<? extends String> res = pairs.smap(ctx, (fe, pair) -> pair.res);
        vActual = V.choice(ctx, res, vActual);
        V<? extends String> ret = pairs.smap(ctx, (fe, pair) -> pair.ret);
        return ret;
    }

    private int readPositionField(java.util.Scanner scanner) {
        try {
            Field field = scanner.getClass().getDeclaredField("position");
            field.setAccessible(true);
            return (int) field.get(scanner);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void split(FeatureExpr ctx) {
        V<? extends String> selected = vActual.smap(ctx, x -> {
            return new String(x);
        });
        vActual = V.choice(ctx, selected, vActual);
    }
}

class MyPair {
    String ret;
    String res;
    MyPair(String a, String b) {
        ret = a;
        res = b;
    }
}