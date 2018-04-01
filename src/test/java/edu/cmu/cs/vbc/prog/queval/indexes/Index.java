package edu.cmu.cs.vbc.prog.queval.indexes;

import edu.cmu.cs.vbc.prog.queval.queval.KnnResult;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.ArrayList;

public abstract class Index {
	public final Store STORE;
	public static final int NOT_FOUND = -1;
	
	public Index(Store s) {
		this.STORE = s;
	}

	public void build(){
		buildIndex();
	}
	
	/**
	 * use the Store to build the Index
	 */
	protected abstract void buildIndex();
	/**
	 * Exact match
	 * @param query
	 * @return TID of found point
	 */
	public abstract int search(final int[] query);
	/**
	 * 
	 * @param query
	 * @param k
	 * @return TIDs & distances of neighbors
	 */
	public abstract KnnResult searchKNN(final int[] query, final int k);
	
	public abstract ArrayList<Integer> windowQuery(final int[] lowerBoundQuery, final int[] upperBoundQuery);
	
	abstract boolean canKNNSearch();
	abstract void clear();
	abstract String getInfo();
	public abstract String getName();
	int numDim(){ return STORE.NUM_DIM;}
	
	//void initParameter(java.util.HashMap<java.lang.String,java.lang.String> _map);
	
	int firstTID(){ return 0;}
	int size(){ return this.STORE.size();}
}
