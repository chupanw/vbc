package edu.cmu.cs.vbc.prog.queval.indexes.myKDTree;

/**
 * For knn, similar to MBRs in T-Trees. Characterizes the space covered by a KDNode
 * @author amartin
 *
 */
public final class HyperRectangle{
	final int[] LOWER, UPPER, THEORETIC_CLOSEST_POINT;
	public long MIN_DIST;
	
	/**
	 * For initialization with root
	 * @param query
	 * @param domMin
	 * @param domMax
	 */
	public HyperRectangle(int[] query, int domMin, int domMax){
		final int SIZE = query.length;
		LOWER = new int[SIZE];
		UPPER = new int[SIZE];
		THEORETIC_CLOSEST_POINT = new int[SIZE];
		long sum=0;
		
		for(int dim=0; dim<SIZE;dim++){
			LOWER[dim] = domMin;
			UPPER[dim] = domMax;
			THEORETIC_CLOSEST_POINT[dim] = (query[dim]<LOWER[dim]) ? LOWER[dim] :(query[dim]>UPPER[dim]) ? UPPER[dim] : query[dim];
			sum += (query[dim]-THEORETIC_CLOSEST_POINT[dim]) * (query[dim]-THEORETIC_CLOSEST_POINT[dim]);
		}
		MIN_DIST = sum;//cast only once
		//MIN_DIST=MartinsKDTree.tree.metrik.calculateDistance(query,THEORETIC_CLOSEST_POINT);
	}
	
	/**
	 * Produce new HyperRectangle with smaller borders.
	 * @param query
	 * @param parent
	 * @param smallerValue
	 * @param dimSmallerValue
	 * @param isLeftChild
	 */
	public HyperRectangle(int[] query, HyperRectangle parent, int smallerValue, int dimSmallerValue, boolean isLeftChild){
		final int SIZE = query.length;
		LOWER = new int[SIZE];
		UPPER = new int[SIZE];
		THEORETIC_CLOSEST_POINT = new int[SIZE];
		
		for(int dim=0; dim<SIZE;dim++){		
			LOWER[dim] = parent.LOWER[dim];
			UPPER[dim] = parent.UPPER[dim];
			THEORETIC_CLOSEST_POINT[dim] = parent.THEORETIC_CLOSEST_POINT[dim];
		}
		MIN_DIST = parent.MIN_DIST;
		//propagate new border of this HyperRectanagle
		slice(query[dimSmallerValue], smallerValue, dimSmallerValue, isLeftChild);
	}
	
	public final void slice(int queryDim, int smallerValue, int dimSmallerValue, boolean isLeftChild){
		//propagate new border of this HyperRectanagle
		int subtractionValue = (queryDim-THEORETIC_CLOSEST_POINT[dimSmallerValue]) * (queryDim-THEORETIC_CLOSEST_POINT[dimSmallerValue]);
		
		if(isLeftChild)
			UPPER[dimSmallerValue] = smallerValue;
		else
			LOWER[dimSmallerValue] = smallerValue;
		
		THEORETIC_CLOSEST_POINT[dimSmallerValue] = 
			(queryDim<LOWER[dimSmallerValue]) ? LOWER[dimSmallerValue] 
			      :(queryDim>UPPER[dimSmallerValue]) ? UPPER[dimSmallerValue] 
			      : queryDim
		;
		//distance adjustment
		MIN_DIST -= (double)subtractionValue;
		MIN_DIST += (queryDim-THEORETIC_CLOSEST_POINT[dimSmallerValue]) * (queryDim-THEORETIC_CLOSEST_POINT[dimSmallerValue]);
			      
		//MIN_DIST=MartinsKDTree.tree.metrik.calculateDistance(query,THEORETIC_CLOSEST_POINT);
	}
}
