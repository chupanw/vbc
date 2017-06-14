package edu.cmu.cs.vbc.prog;

/**
 * Created by lukas on 6/13/17.
 */
public class Simple {
    public static void main(String[] args){
        Simple s = new Simple();
        int x = 1;
        int y = x + 1;
        s.foo(y);
    }

    public int foo(int x){
        return x*2;
    }
}
