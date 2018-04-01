package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

/**
 * @author chupanw
 */
public class MultiArrayExample {
    @VConditional
    public static boolean A = true;

    public static void main(String[] args) {
        MultiArrayExample foo = new MultiArrayExample();
        foo.create2DArray();
        foo.create3DArray();
        foo.access3DArray();
        foo.access3DArray2();
    }

    void create2DArray() {
        System.out.println("------ create2DArray ------");
        int[][] a;
        if (A)
            a = new int[1][2];
        else
            a = new int[3][4];
        System.out.println(a[0][0]);
        System.out.println(a[0].length);
    }

    void create3DArray() {
        System.out.println("------ create3DArray ------");
        int[][][] a;
        if (A)
            a = new int[1][2][3];
        else
            a = new int[4][5][6];
        System.out.println(a[0][0][0]);
        System.out.println(a.length);
        System.out.println(a[0].length);
        System.out.println(a[0][0].length);

        String[][][] b = new String[1][2][3];
        System.out.println(b[0][0][0]);
    }

    void access3DArray() {
        System.out.println("------ access3DArray ------");
        int[][][] a = new int[1][2][3];
        if (A)
            a[0][0][1] = 1;
        else
            a[0][0][1] = 2;
        System.out.println(a[0][0][1]);
        System.out.println(a[0][0][2]);
    }

    void access3DArray2() {
        System.out.println("------ access3DArray2 ------");
        int[][][] a = new int[1][2][3];
        if (A)
            a[0][0] = new int[]{0, 1, 2};
        else
            a[0][1] = new int[]{0, 1};
        System.out.println(a[0][0].length);
        System.out.println(a[0][1].length);

        System.out.println(a[0][0][0]);
        System.out.println(a[0][0][1]);
        System.out.println(a[0][0][2]);

        System.out.println(a[0][1][0]);
        System.out.println(a[0][1][1]);
    }
}
