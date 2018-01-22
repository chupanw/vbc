package edu.cmu.cs.vbc.prog.queval.indexes.myKDTree;

import edu.cmu.cs.vbc.prog.queval.indexes.Index;
import edu.cmu.cs.vbc.prog.queval.indexes.MyKDTree;
import edu.cmu.cs.vbc.prog.queval.queval.Configuration;
import edu.cmu.cs.vbc.prog.queval.queval.KnnCollectionIntDistances;
import edu.cmu.cs.vbc.prog.queval.queval.Metrics;
import edu.cmu.cs.vbc.prog.queval.queval.Util;

import java.util.ArrayList;
import java.util.Arrays;

public final class MyKDNode {
	public final int TID, compValue;
	final MyKDNode parent;
	public MyKDNode left;
	public MyKDNode right;


	public MyKDNode(int TID, int[] point, int dimForCompare, MyKDNode parent){
		this.TID = TID;
		this.compValue = point[dimForCompare];
		this.parent = parent;
	}
	
	/**
	 * @deprecated Recursive variant may result in stackoverflows.
	 * @param query
	 * @param dimForCompare
	 * @return
	 */
	public final int exactMatch(int[] query, int dimForCompare){
		if(query[dimForCompare]==compValue){//match in this dimension so point possibly found
			final int[] point = MyKDTree.tree.STORE.getPoint(TID);
			if(Arrays.equals(query, point)){
				return TID;
			}
		}
		int nextDimForCompare = (dimForCompare!=query.length-1) ? dimForCompare+1 : 0;
		if(query[dimForCompare]<=compValue){
			return (left!=null) ? left.exactMatch(query, nextDimForCompare) : Index.NOT_FOUND;
		}
		else{
			return (right!=null) ? right.exactMatch(query, nextDimForCompare) : Index.NOT_FOUND;
		}
	}
	
	public final boolean insert(int[] toAdd, int dimForCompare, int tid){
		//do we have to consider left or right subtree
		final boolean nextNodeLeft;
		final MyKDNode nextNode;
		final int nextDimForCompare = (dimForCompare!=toAdd.length-1) ? dimForCompare+1 : 0;
		
		if(toAdd[dimForCompare]<=compValue){
			nextNodeLeft = true;
			nextNode = this.left;
		}else{
			nextNodeLeft = false;
			nextNode = this.right;
		}
		
		if (nextNode!=null)//hand forward
			return nextNode.insert(toAdd, nextDimForCompare, tid);
		
		//else we found the Node where we have to add the point
		MyKDNode add = new MyKDNode(tid, toAdd, nextDimForCompare, this);
		
		if(nextNodeLeft)
			this.left = add;
		else
			this.right = add;
		return true;
	}
	
	public final String toString(){
		String ret="";
		if (parent==null)
			ret+="Root, ";
		ret+="Compare @Dim #??? Value: "+compValue;
		ret+="\n[";
		final int[] point = MyKDTree.tree.STORE.getPoint(TID);
		for(int dim=0;dim<point.length;dim++){
			ret+=" "+point[dim];
		}
		ret+="]";
		return ret;
	}

	/**
	 * Some kind of depth-first search
	 * @param nearest
	 * @param rect
	 * @param dimForCompare
	 * @param query
	 */
	public void knn(KnnCollectionIntDistances nearest, HyperRectangle rect, int dimForCompare, int[] query) {
		int[] point = MyKDTree.tree.STORE.getPoint(this.TID);
		
		long distance = 0;//queval.Metrics.EucleadeanSqrd(query, point);
		// TODO can this be different to KnnCollectionIntDistances
		if (Configuration.EucleadeanSqrd) {
			distance = Metrics.EucleadeanSqrd(query, point);
		} else if (Configuration.Manhatten) {
			distance = Metrics.Manhatten(query, point);
		} else {
			throw new RuntimeException("Metric not selected");
		}
		
		if(distance<nearest.maxDist)
			nearest.update(distance, this.TID);
		
		//Do we have to consider sub-trees
		final int nextDimForCompare = (dimForCompare!=query.length-1) ? dimForCompare+1 : 0;
		
		HyperRectangle rightHyperRect = (right==null) ? null : (left==null) ? rect : new HyperRectangle(query, rect, point[dimForCompare], dimForCompare, false);
		
		//Left sub-tree
		if(left!=null){
			//re-use parent rectangle
			HyperRectangle leftHyperRect = rect;
			leftHyperRect.slice(query[dimForCompare], point[dimForCompare], dimForCompare, true);//new HyperRectangle(query, rect, point[dimForCompare], dimForCompare, true);
			if(leftHyperRect.MIN_DIST<(nearest.maxDist)){//could there be any points closer than current farest nearestNeighbor?
				left.knn(nearest, leftHyperRect, nextDimForCompare, query);
			}
		}
		//right sub-tree
		if(right!=null){
			if(left==null)//avoid double slicing
				rightHyperRect.slice(query[dimForCompare], point[dimForCompare], dimForCompare, false);
			if(rightHyperRect.MIN_DIST<(nearest.maxDist)){
				right.knn(nearest, rightHyperRect, nextDimForCompare, query);
			}
		}
	}
	
	public final class KDContainer{
		public final MyKDNode node;
		public final HyperRectangle rect;
		public final int nextDimForCompare;
		
		public KDContainer(MyKDNode node, HyperRectangle rect, int nextDimForCompare){
			this.node = node;
			this.rect = rect;
			this.nextDimForCompare = nextDimForCompare;
		}
	}

	public void windowQuery(final int[] lowerBoundQuery, final int[] upperBoundQuery
			, final ArrayList<Integer> resultTIDS, int dimForCompare) {
		
		//XXX erst mit dem compValue, damit ich den Punkt nicht holen muss?
		final int[] point = MyKDTree.tree.STORE.getPoint(TID);
		if(Util.isIn(point, lowerBoundQuery, upperBoundQuery))
			resultTIDS.add(TID);
		
		int nextDimForCompare = (dimForCompare!=lowerBoundQuery.length-1) ? dimForCompare+1 : 0;
		
		//left branch FIXME
		if(lowerBoundQuery[dimForCompare]<=this.compValue){//dim lowerBoundQuery links oder gleich von comp value
			if(left != null)
				left.windowQuery(lowerBoundQuery, upperBoundQuery, resultTIDS, nextDimForCompare);
		}
		if(upperBoundQuery[dimForCompare]>=this.compValue){//right branch
			if(right != null)
				right.windowQuery(lowerBoundQuery, upperBoundQuery, resultTIDS, nextDimForCompare);
		}	
	}
}
