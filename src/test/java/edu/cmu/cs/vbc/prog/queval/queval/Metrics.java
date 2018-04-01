package edu.cmu.cs.vbc.prog.queval.queval;

public final class Metrics {
	/**
	 * Eucleadean dist sqrd
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static long EucleadeanSqrd(final int[] p1, final int[] p2) {
		long dist = 0;
		for(int dim=0;dim<p1.length;dim++){
			dist += (p1[dim]-p2[dim])*(p1[dim]-p2[dim]);
		}
		return dist;
	}
	
	public static double Eucleadean(final int[] p1, final int[] p2) {
		long dist = 0;
		for(int dim=0;dim<p1.length;dim++){
			dist += (p1[dim]-p2[dim])*(p1[dim]-p2[dim]);
		}
		return Math.sqrt((double)dist);
	}
	
	public static long Manhatten(final int[] p1, final int[] p2) {
		long dist = 0;
		int temp;
		
		for(int dim=0;dim<p1.length;dim++){
			temp = p1[dim]-p2[dim];
			dist += (temp<0) ? -temp : temp;
		}
		return dist;
	}
}
