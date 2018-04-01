package edu.cmu.cs.vbc.prog.queval.queval;

import edu.cmu.cs.vbc.prog.queval.stores.Store;

public final class KnnCollectionIntDistances {
	final int[] TIDs;
	final long[] DISTANCES;
	public final int[] QUERY;
	public long maxDist = Long.MAX_VALUE;
	int indexMaxDistance = -1;
	final Store DATA;
	final int k;
	
	private KnnCollectionIntDistances(final int k, Store data, final int[] query){
		this.k=k;
		this.DATA=data;
		this.QUERY = query;
		
		this.TIDs = new int[k];
		this.DISTANCES = new long[k];
	}
	
	/**
	 * update only if distance smaller
	 * @param TID
	 * @return
	 */
	public boolean tryUpdate(final int TID){
		int[] point = DATA.getPoint(TID);
		 // param -> Typ der Metrik kann noch variiert werden...
		long distance = 0;//Metrics.EucleadeanSqrd(QUERY, point);
//		TODO can this be different to KnnCollectionIntDistances
		if (Configuration.EucleadeanSqrd) {
			distance = Metrics.EucleadeanSqrd(QUERY, point);
		} else if (Configuration.Manhatten) {
			distance = Metrics.Manhatten(QUERY, point);
		} else {
			throw new RuntimeException("Metric not selected");
		}
		if(distance<this.maxDist){
			update(distance, TID);		
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * No check - safe variant: tryUpdate(final int TID)  
	 * @param distance
	 * @param TID
	 * @return
	 */
	public void update(final long distance, final int TID){
		DISTANCES[indexMaxDistance]	= distance;
		TIDs[indexMaxDistance]		= TID;
		
		//determine new *maxDistance*
		maxDist=Long.MIN_VALUE;
		for(int j=0;j<k; j++){
			if (maxDist<DISTANCES[j]){
				maxDist=DISTANCES[j];
				indexMaxDistance=j;
			}
		}
	}
	
	public static KnnCollectionIntDistances create_With_First_K_Elements(final int k, Store data, final int[] query){
		KnnCollectionIntDistances col = new KnnCollectionIntDistances(k, data, query);
		int[] point;
		long distance;
		
		col.maxDist = Long.MIN_VALUE;
		col.indexMaxDistance = 0;
		
		//insert data & distaces
		for(int tid=0;tid<k;tid++){
			col.TIDs[tid] = tid;
			point = col.DATA.getPoint(tid);
			// param -> Typ der Metrik kann noch variiert werden...
			if (Configuration.EucleadeanSqrd) {
				distance = Metrics.EucleadeanSqrd(col.QUERY, point);
			} else if (Configuration.Manhatten) {
				distance = Metrics.Manhatten(col.QUERY, point);
			} else {
				throw new RuntimeException("Metric not selected");
			}
			col.DISTANCES[tid] = distance;
			
			if(distance>col.maxDist){
				col.maxDist 		 = distance;
				col.indexMaxDistance = tid;
			}
		}
		
		return col;
	}
	public static KnnCollectionIntDistances create_With_Empty_Collection(final int k, Store data, final int[] query){
		KnnCollectionIntDistances col = new KnnCollectionIntDistances(k, data, query);
		
		col.maxDist = Long.MAX_VALUE;
		col.indexMaxDistance = 0;
		
		for(int i=0;i<k;i++){
			col.DISTANCES[i] = Long.MAX_VALUE;
			col.TIDs[i] = -1;
		}
		
		return col;
	}
	
	public KnnResult getResult(){
		return new KnnResult(this.TIDs, this.DISTANCES);
	}
}
