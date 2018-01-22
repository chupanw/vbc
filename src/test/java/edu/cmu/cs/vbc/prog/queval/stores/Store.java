package edu.cmu.cs.vbc.prog.queval.stores;

public abstract class Store {
	public final int MIN_VALUE, MAX_VALUE, NUM_DIM;
	
	Store(int dimMin, int dimMax, int numDim){
		this.MIN_VALUE = dimMin;
		this.MAX_VALUE = dimMax;
		this.NUM_DIM   = numDim;
	}
	
	abstract int insert(int[] toInsert);
	public abstract int[] getPoint(int TID);
	
	public abstract int size();
}
