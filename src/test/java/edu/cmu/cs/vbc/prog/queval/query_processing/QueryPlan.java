package edu.cmu.cs.vbc.prog.queval.query_processing;

import edu.cmu.cs.vbc.prog.queval.indexes.Index;
import edu.cmu.cs.vbc.prog.queval.queval.KnnResult;
import edu.cmu.cs.vbc.prog.queval.queval.Util;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.ArrayList;
import java.util.Collections;

public class QueryPlan {

	public static final int EXACT_MATCH_QUERY 	= 0; // param
	public static final int KNN_QUERY 			= 1; // param
	public static final int EPSILON_NN_QUERY 	= 2; // param -> noch nicht umgesetzt
	public static final int RANGE_QUERY 		= 3; // param

	final boolean[] QUERY_TYPES;
	
	public Index[] indexes;
	QueryResult[] results;
	final Store STORE;
	int runs;
	int k;
	boolean checkPrecisions;
	double start, stop;
	public double building, exactmatch, range;

	
	int[] TIDQueryPointsInStore;
	int[][] queryPointsExactNotInStore;

	/**
	 * Default Constructor assuming that only exact-match queries are performed using only one Index with one repitition.
	 * @param s : Indexed store.
	 * @param index
	 * @param numPointsToQuery form the store.
	 */
	public QueryPlan(Store s,Index index, int numPointsToQuery){
		indexes = new Index[1];
		indexes[0] = index;
		results = new QueryResult[1];
		results[0] = new QueryResult();
		STORE = s;
		runs = 1;
		k=10;
		checkPrecisions= false;
		TIDQueryPointsInStore = getTIDs(numPointsToQuery, s);
		if (Features.SHUFFLE)
			Util.shuffleArray(TIDQueryPointsInStore);
		QUERY_TYPES = new boolean[RANGE_QUERY+1];
		QUERY_TYPES[EXACT_MATCH_QUERY] = true; // enable exact matches only.	
		building = -1;
		exactmatch = -1;
		range = -1;
	}
	
	public QueryPlan(Store s,Index index, int numPointsToQuery, boolean[] queryTypes){
		this(s, index, numPointsToQuery);
		for(int i=0;i<QUERY_TYPES.length;i++){
			QUERY_TYPES[i] = queryTypes[i];//assuming length is correct
		}
	}
	
	private static int[] getTIDs(int numPointsToQuery, Store s) {
		int[] tids = new int[numPointsToQuery];
		int delta = s.size()/numPointsToQuery;
		int currentTID = 0;
		
		for(int i=0;i<numPointsToQuery;i++){
			tids[i] = currentTID;
			currentTID+= delta;
		}
		return tids;
	}
	
	static final int[] randData = new int[]{345,523,23457,123,45,1,23,46,12,97,4,2134,124};

	public void run(){
		Index index;
		QueryResult result;
		
		//create queries for exact match
		int[][] queries = new int[TIDQueryPointsInStore.length][];
		
		for(int point=0;point<TIDQueryPointsInStore.length;point++){
			queries[point] = STORE.getPoint(TIDQueryPointsInStore[point]);
		}
		
		int[][] lowerBound = null, upperBound = null;
		if(QUERY_TYPES[RANGE_QUERY]){
			long seed = 123456;
			
			upperBound = new int[TIDQueryPointsInStore.length][STORE.NUM_DIM];
			lowerBound = new int[TIDQueryPointsInStore.length][STORE.NUM_DIM];
			int[] randPoint1, randPoint2;
//			Random rand = new Random(seed);
			int mi = 0;
			int mul = 7;
			for(int query=0;query<TIDQueryPointsInStore.length;query++){
				mul = (mul * randData[mi++]) * 31;
				mi = mi % randData.length;
				randPoint1 = STORE.getPoint(mi % STORE.size());
				mul = (mul * randData[mi++]) * 31;
				mi = mi % randData.length;
				randPoint2 = STORE.getPoint(mi % STORE.size());
				
//				randPoint1 = STORE.getPoint(rand.nextInt(STORE.size()));
//				randPoint2 = STORE.getPoint(rand.nextInt(STORE.size()));
				for(int dim=0;dim<STORE.NUM_DIM;dim++){
					if(randPoint1[dim]<randPoint2[dim]){
						lowerBound[query][dim] = randPoint1[dim];
						upperBound[query][dim] = randPoint2[dim];
					}else{
						lowerBound[query][dim] = randPoint2[dim];
						upperBound[query][dim] = randPoint1[dim];
					}
				}
			}
			
		}
		
		for(int i=0;i<indexes.length;i++){
			index = indexes[i];
			result = results[i];
			
			System.out.println(index.getName());
			start = System.currentTimeMillis();
			index.build();
			stop = System.currentTimeMillis();
			building = stop-start;
			System.out.println(index.getName()+ " build "+(stop-start));
			
			if(QUERY_TYPES[EXACT_MATCH_QUERY]){
				//perform exact match queries with points in data store
				start = System.currentTimeMillis();
				for(int query=0;query<TIDQueryPointsInStore.length;query++){
					int tid;
					tid = index.search(queries[query]);
					result.exact(query, tid);
					if(Features.VERBOSE)
						if(query%10000==0)
							System.out.print(query+":"+tid+" ");
				}
				stop = System.currentTimeMillis();
				exactmatch = (stop - start);
				result.exact(stop-start);
				System.out.println(index.getName()+ " exact match "+(stop-start));
			}
			
			if(QUERY_TYPES[KNN_QUERY]){
				KnnResult resultTIDs;
				
				start = System.currentTimeMillis();
				for(int query=0;query<TIDQueryPointsInStore.length;query++){
					resultTIDs = index.searchKNN(queries[query], k);
					result.knn(query, resultTIDs);
					if(Features.VERBOSE)
						if(query%10000==0)
							System.out.print(query+":"+resultTIDs+" ");
				}
				stop = System.currentTimeMillis();
				result.exact(stop-start);
				System.out.println(index.getName()+ " KNN_QUERY "+(stop-start));
			}
			/*
			if(QUERY_TYPES[EPSILON_NN_QUERY]){
				ArrayList<Integer> resultTIDs = new ArrayList<Integer>();
				
				start = System.currentTimeMillis();
				for(int query=0;query<TIDQueryPointsInStore.length;query++){
					resultTIDs = index.(queries[query], k);
					result.knn(query, resultTIDs);
					if(Features.VERBOSE)
						if(query%10000==0)
							System.out.print(query+":"+resultTIDs+" ");
				}
				stop = System.currentTimeMillis();
				result.exact(stop-start);
				System.out.println(index.getName()+ " KNN_QUERY "+(stop-start));
			}*/
			
			if(QUERY_TYPES[RANGE_QUERY]){
				ArrayList<Integer> resultTIDs = new ArrayList<Integer>();
				
				start = System.currentTimeMillis();
				for(int query=0;query<TIDQueryPointsInStore.length;query++){
					resultTIDs = index.windowQuery(lowerBound[query], upperBound[query]);
					//result.knn(query, resultTIDs); TODO
					if(Features.VERBOSE)
						if(query%10000==0){
							Collections.sort(resultTIDs);
							System.out.print(query+":"+resultTIDs+" ");
						}
				}
				stop = System.currentTimeMillis();
				range = stop-start;
				result.exact(stop-start);
				System.out.println(index.getName()+ " RANGE_QUERY "+(stop-start));
			}
		}
	}
	
	public void enableQueryType(int queryType){
		this.QUERY_TYPES[queryType] = true;
	}
	
	public void disableQueryType(int queryType){
		this.QUERY_TYPES[queryType] = false;
	}
	
	interface Features{
		boolean SHUFFLE = false;
		boolean VERBOSE = true;
	}
}
