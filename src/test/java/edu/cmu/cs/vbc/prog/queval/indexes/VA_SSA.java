package edu.cmu.cs.vbc.prog.queval.indexes;

import edu.cmu.cs.vbc.prog.queval.queval.*;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.ArrayList;

public final class VA_SSA extends Index {

	//XXX @Jens alternative 4,6,7
	private int bitsPerDimension = Configuration.BPD4 ? 4 : Configuration.BPD6 ? 6 : Configuration.BPD7 ? 7 : -1;

	private int NUM_DIM;
	private int[][] VA_FILE;
	private int RANGE_OF_CELL;
	private int INTs_PER_APPROXIMATION;
	int SIZE;
	
	public VA_SSA(Store s) {
		super(s);
	}

	@Override
	protected void buildIndex() {
		NUM_DIM = numDim();
		INTs_PER_APPROXIMATION = intsPerApproximation();
		VA_FILE = new int[size()][INTs_PER_APPROXIMATION];//TODO doppelt gemoppelt
		RANGE_OF_CELL = (STORE.MAX_VALUE- STORE.MIN_VALUE + 1) / power(2, bitsPerDimension) + 1;
		SIZE = size();
		
		int[] toInsert;
		int[] bitVector;
		
		for(int tid=firstTID();tid<size();tid++){
			toInsert 	 = this.STORE.getPoint(tid);
			bitVector    = encodeApprox(approximation((toInsert)));
			VA_FILE[tid] = bitVector;//TODO doppelt gemoppelt
		}
	}
	
	@Override
	public int search(int[] query) {
		int[] approxQuery = encodeApprox(approximation(query));
		int[] pointToCompare;
		
		//Check approximations
		for (int tid = 0; tid < SIZE; tid++) {
			//do we need to compare the real points?
			if (Util.equals(VA_FILE[tid], approxQuery)) {
				pointToCompare = STORE.getPoint(tid);
				if(Util.equals(query, pointToCompare))
					return tid;
			}
		}
		return NOT_FOUND;
	}
	
	@Override
	public ArrayList<Integer> windowQuery(int[] lowerBoundQuery,
			int[] upperBoundQuery) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		int[] point;
		//TODO consider approximation
		for (int tid = 0; tid < SIZE; tid++) {
			point = STORE.getPoint(tid);
			if(Util.isIn(point, lowerBoundQuery, upperBoundQuery)){
				result.add(tid);
			}
		}
		
		return result;
	}
	
	@Override
	public KnnResult searchKNN(final int[] query, final int k) {
		final KnnCollectionIntDistances col; 
		long minDistApprox;
		final int[] approxQuery = encodeApprox(approximation(query));
		
		//init with first k points
		col = KnnCollectionIntDistances.create_With_First_K_Elements(k, STORE, query);
		
		for(int tid=firstTID()+k;tid<size();tid++){
			minDistApprox = getLowerBound(approxQuery, query, decodeApprox(VA_FILE[tid]));
			
			if(minDistApprox<col.maxDist){
				col.tryUpdate(tid);
			}
		}
		return col.getResult();
	}
	

	@Override boolean canKNNSearch() {	return true; }
	@Override void clear() { VA_FILE = null; }
	@Override String getInfo() { return getName()+" " + "bits per dimension:"+ bitsPerDimension; }
	@Override public String getName() {	return "VA File (SSA)";	}	
	private int intsPerApproximation() { return (NUM_DIM * bitsPerDimension + 31) / 32; }

	private int power(int base, int exp) {
		int result = 1; 
		
		for(int i=0;i<exp;i++){
			result *= base;
		}
		return result;
		/*
		if (exp == 1) {
			return base;
		}
		int tmp = power(base, exp / 2);
		if (exp % 2 == 0) {
			return tmp * tmp;
		} else {
			return tmp * tmp * base;
		}*/
	}
	
	private int[] encodeApprox(int[] point) {
		int[] approx = new int[INTs_PER_APPROXIMATION];
		int shortIndex = 0;
		int bitsAvailable = 32;//bits per integer
		
		for (int dim = 0; dim < point.length; dim++) {
			int bitsToStore = bitsPerDimension;
			int bits = point[dim];
			while (bitsToStore > 0) {
				int storeBits = Math.min(bitsToStore, bitsAvailable);
				approx[shortIndex] <<= storeBits;
				int tBits = bits >> (bitsPerDimension - storeBits);
				approx[shortIndex] |= tBits;
				bits -= tBits;

				bitsToStore -= storeBits;
				bitsAvailable -= storeBits;
				if (bitsAvailable == 0) {
					shortIndex++;
					bitsAvailable = 32;
				}
			}
		}
		approx[approx.length - 1] <<= bitsAvailable;
		return approx;
	}

	private int[] approximation(int[] vector) {
		int[] approx = new int[vector.length];
		for (int c0 = 0; c0 < vector.length; c0++) {
			approx[c0] = vector[c0] / RANGE_OF_CELL;
		}
		return approx;
	}

	private int[] decodeApprox(int[] approx) {
		int[] res = new int[numDim()];

		int approxPointer = approx.length - 1;
		int stuffedBits = 32 - ((numDim() * bitsPerDimension) % 32);

		int currentApprox = approx[approxPointer];
		
		int bitsInApproxLeft = 32;

		if(	stuffedBits != 32){
			approxPointer--;
			currentApprox >>= stuffedBits;
			bitsInApproxLeft = 32 - stuffedBits;	
		}
		
		for (int c0 = res.length - 1; c0 >= 0; c0--) {
			int bitsToGetLeft = bitsPerDimension;
			while (bitsToGetLeft > 0) {
				if (bitsInApproxLeft == 0) {
					currentApprox = approx[approxPointer];
					approxPointer--;
					bitsInApproxLeft = 32;
				}
				
				int getBits = Math.min(bitsToGetLeft, bitsInApproxLeft);
				res[c0] = (currentApprox << (32 - getBits)) >>> (32 - getBits);
				res[c0] <<= bitsToGetLeft - getBits;
				currentApprox >>>= getBits;
				bitsToGetLeft -= getBits;
				bitsInApproxLeft -= getBits;
			}
		}
		return res;
	}

	private long getLowerBound(int[] qApprox, int[] qData, int[] pApprox) {
		int[] t = new int[pApprox.length];
		int[] t2 = new int[pApprox.length]; 

		for (int dimension = 0; dimension < pApprox.length; dimension++) {
			
			if (qApprox[dimension] < pApprox[dimension]) {
				t[dimension] = pApprox[dimension] * RANGE_OF_CELL - qData[dimension];
			} else if (qApprox[dimension] > pApprox[dimension]) {
				t[dimension] = qData[dimension] - ((pApprox[dimension] + 1) * RANGE_OF_CELL);
			}	
		}
		return Metrics.EucleadeanSqrd(t, t2);
	}
}
