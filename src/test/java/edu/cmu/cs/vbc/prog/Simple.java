package edu.cmu.cs.vbc.prog;

/**
 * Created by lukas on 6/13/17.
 */
public class Simple {
    int f1 = 5;
    boolean f2 = false;
//    char f3 = 'a';
//    byte f4 = 1;

//    static int sf1;
    public static void main(String[] args){
        Simple s = new Simple();
        // arithmetic
        int x = 1;
        int y = x + 1;

        // methods, int -> int
        int z  = s.foo(y);
        z = z + 1;

        // static methods
        nop(z);

        // methods, U -> int or int -> U
        boolean a = s.even(1);
        int b = s.toN(a);
        String g = s.toS(1);
        int c = s.toI("");
//        char p = s.toC(1);
//        int q = s.toI(p);
//        byte r = s.toB(1);
//        int t = s.toI(r);

        // fields
        int o = s.f1;
        s.f1 = 1;
        boolean l = s.f2;
        s.f2 = true;
//        char m = s.f3;
//        s.f3 = 'a';
//        byte n = s.f4;
//        s.f4 = 5;
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

    public String toS(int x) {
        return "";
    }

    public int toI(String s) {
        return 1;
    }
//
//    public char toC(int x) {
//        return 'a';
//    }
//
//    public int toI(char c) {
//        return 1;
//    }
//
//    public byte toB(int x) {
//        return 1;
//    }
//
//    public int toI(byte b) {
//        return 1;
//    }
}
