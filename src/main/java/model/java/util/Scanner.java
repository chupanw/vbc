package model.java.util;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public class Scanner {
    private V<? extends String> vActual;

    public Scanner(V<String> vSources, FeatureExpr ctx, String dummy) {
        vActual = vSources.select(ctx);
    }

    public V<? extends Integer> nextInt____I(FeatureExpr ctx) {
        V<? extends MyPair> pairs = vActual.smap(ctx, (fe, actual) -> {
            java.util.Scanner scanner = new java.util.Scanner(actual);
            String ret = String.valueOf(scanner.nextInt());
            int index = actual.indexOf(ret);
            String res = actual.substring(index + ret.length());
            return new MyPair(ret, res);
        });
        V<? extends String> res = pairs.smap(ctx, (fe, pair) -> pair.res);
        vActual = V.choice(ctx, res, vActual);
        V<? extends Integer> ret = pairs.smap(ctx, (fe, pair) -> Integer.valueOf(pair.ret));
        return ret;
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