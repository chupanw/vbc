package edu.cmu.cs.vbc.prog;

public class ArrayCloneExample {
    public static void main(String[] args) {
        ArrayCloneExample foo = new ArrayCloneExample();
        foo.testInt();
        foo.testIntTwoDim();
        foo.testDouble();
        foo.testDoubleTwoDim();
    }

    void testInt() {
        int[] a = new int[]{1, 2, 3};
        int[] b = a.clone();
        a[0] = 0;
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i] + " " + b[i]);
        }
    }

    void testIntTwoDim() {
        int[][] a = new int[][]{{1, 2}, {3, 4}};
        int[][] b = a.clone();
        a[0][0] = -1;
        a[0] = new int[]{-11, -2};
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println(a[i][j] + " " + b[i][j]);
            }
        }
    }

    void testDouble() {
        double[] a = new double[]{1.0, 2.0, 3.0};
        double[] b = a.clone();
        a[0] = 0.0;
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i] + " " + b[i]);
        }
    }

    void testDoubleTwoDim() {
        double[][] a = new double[][]{{1.0, 2.0}, {3.0, 4.0}};
        double[][] b = a.clone();
        a[0][0] = -1.0;
        a[0] = new double[]{-11.0, -2.0};
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println(a[i][j] + " " + b[i][j]);
            }
        }
    }
}
