package edu.cmu.cs.vbc.prog.queval.queval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public final class Util {
	public static String out(int[] array) {
		if (array == null)
			return "Null array";

		String ret = "[";
		for (int index = 0; index < array.length - 1; index++) {
			ret += array[index] + ", ";
		}
		ret += array[array.length - 1] + "]";

		return ret;
	}

	public static int[][] getData() {
		final int UPPER_BOUND = 200;
		final int[][] data = new int[UPPER_BOUND * UPPER_BOUND][2];
		System.out.println("Util.getData(): Size of dataset " + UPPER_BOUND
				* UPPER_BOUND);
		int counter = 0;
		for (int x = 0; x < UPPER_BOUND; x++) {
			for (int y = 0; y < UPPER_BOUND; y++) {
				data[counter][0] = x;
				data[counter][1] = UPPER_BOUND - y;
				counter++;
			}
		}

		return data;
	}

	public static int[][] getData(int size, int dim) {

		final int[][] data = new int[size][dim];
		System.out.println("Util.getData(): Size of dataset " + size);

		for (int point = 0; point < size; point++) {
			for (int value = 0; value < dim; value++) {
				data[point][value] = point + value;
			}
		}

		return data;
	}
	
	static final int[] randData = new int[]{345,523,23457,123,45,1,23,46,12,97,4,2134,124};

	public static int[][] getData(int size, int dim, int dimMax, long seed) {
		Random rand = new Random(seed);

		final int[][] data = new int[size][dim];
		System.out.println("Util.getData(): Size of dataset " + size);
		for (int point = 0; point < size; point++) {
			for (int value = 0; value < dim; value++) {
				data[point][value] = rand.nextInt(dimMax);
			}
		}

		return data;
	}

	/**
	 * Stops on first inequal dimension
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static boolean equals(final int[] p1, final int[] p2) {
		for (int dim = 0; dim < p1.length; dim++) {
			if (p1[dim] != p2[dim])
				return false;
		}
		return true;
	}

	// Implementing Fisher�Yates shuffle
	public static void shuffleArray(int[] ar) {
		Random rnd = new Random(123456789);
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	public static boolean isIn(int[] point, int[] lowerBoundQuery, int[] upperBoundQuery) {
		
		for(int dim=0;dim<point.length;dim++){
			if(lowerBoundQuery[dim]>point[dim] || point[dim]>upperBoundQuery[dim])
				return false;
		}
		
		return true;
	}

	public static String outSorted(ArrayList<Integer> resultTIDs) {
		Collections.sort(resultTIDs);
		StringBuilder s = new StringBuilder("[");
		for(int i=0;i<resultTIDs.size();i++){
			s.append(resultTIDs.get(i));
			s.append('|');
		}
		s.append(']');
		return s.toString();
	}
}
