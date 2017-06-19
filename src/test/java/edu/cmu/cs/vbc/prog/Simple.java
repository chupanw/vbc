package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.Vint;

/**
 * Created by lukas on 6/13/17.
 */
public class Simple {
    public static void main(String[] args){
        Simple s = new Simple();
        int x = 1;
        int y = x + 1;
        int z  = s.foo(y);
        z = z + 1;
        boolean a = s.even(1);
        nop(z);
        int b = s.toN(a);
    }

    public int foo(int x) {
        return bar(x)*2;
    }

    private int bar(int x) {
        return x*2;
    }

    public boolean even(int x) {
        return x%2 == 0;
    }

    public static void nop(int x) {
        assert true;
    }


    public int toN(boolean b) {
        return 1;
    }
}
