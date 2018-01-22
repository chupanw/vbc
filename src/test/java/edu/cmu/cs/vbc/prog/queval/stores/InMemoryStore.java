package edu.cmu.cs.vbc.prog.queval.stores;

import edu.cmu.cs.vbc.prog.queval.queval.Util;

public final class InMemoryStore extends Store{
	private final int NUM_POINTS;
	private final int[][] STORE;//linearized
	//private final int[] RETURN_BUFFER;
	static private final int INVALID_TID = -1;
	
	private int currentTID = INVALID_TID;
	
	
	public InMemoryStore(int dim, int size, int dimMin, int dimMax){
		super(dimMin, dimMax, dim);
		this.NUM_POINTS = size;
		
		this.STORE = new int[size][];
		//this.RETURN_BUFFER = new int[dim];
	}
	
	/**
	 * 
	 * @param toInsert
	 * @return assigned TID
	 */
	int insert(int[] toInsert){
		final int TID 	 = getNextTID();
		//Check length point
		
		//TODO check max NUM Points
		this.STORE[TID] = toInsert;
		
		return TID;
	}
	
	private int getNextTID() {
		return currentTID++;
	}

	public int[] getPoint(final int TID){
		return this.STORE[TID];
	}
	
	public String toString() {
		String ret = "InMemoryStore "+NUM_POINTS+" "+NUM_DIM+" "+currentTID;
		for(int tid=0;tid<5;tid++){
			ret+= Util.out(getPoint(tid));
			ret+="\n";
		}
		return ret ;
	}
	
	public void bulkInsertWithoutCheck(final int[][] DATA){
		System.out.println("bulkInsertWithoutCheck(): start");
		for(currentTID=0;currentTID<this.NUM_POINTS;currentTID++){
			this.STORE[currentTID]=DATA[currentTID];
		}
		System.out.println("bulkInsertWithoutCheck(): stop");
	}
	
	public void out() {
		System.out.println("InMemoryStore "+NUM_POINTS+" "+NUM_DIM+" "+currentTID);
		for(int tid=0;tid<NUM_POINTS;tid++){
			System.out.println(Util.out(getPoint(tid)));
		}
	}

	@Override
	public int size() {
		return this.NUM_POINTS;
	}
}
