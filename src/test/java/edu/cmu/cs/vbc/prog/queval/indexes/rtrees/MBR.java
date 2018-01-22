package edu.cmu.cs.vbc.prog.queval.indexes.rtrees;

import edu.cmu.cs.vbc.prog.queval.indexes.Index;
import edu.cmu.cs.vbc.prog.queval.indexes.RVariant;
import edu.cmu.cs.vbc.prog.queval.queval.KnnCollectionIntDistances;
import edu.cmu.cs.vbc.prog.queval.queval.Util;

import java.util.ArrayList;
import java.util.Collections;

public final class MBR {
	/** Imagine an MBR with [0,1] and [2,3] than minBorder[0]=0, minBorder[1]=1, maxBorder[0]=2 etc. */
	public int[] minBorder, maxBorder;
	//XXX so semi-sch�n der tid exitiert nur bei Puntken
	final int tid;

	public final RVariant tree;
	public MBR parent;
	public MBR[] children;
	int size = 0;
	/** 0 = Point, 1 = Leaf, ..*/
	public int level;
	
	/**
	 * Point Constructor, I do not like this but a point is an MBR with for each i minBorder[i] == maxBorder[i] (but the array pointer are not the same, tried this -> stupid idea)
	 * @param p
	 * @param _tree
	 */
	public MBR(int tid, Point p, RVariant _tree, MBR _parent){
		this.tree = _tree;
		this.parent = _parent;
		this.tid = tid;

		int[] clone = new int[p.coordinates.length];
		System.arraycopy(p.coordinates, 0, clone, 0, p.coordinates.length);
		this.minBorder = clone;
		
		int[] clone2 = new int[p.coordinates.length];
		System.arraycopy(p.coordinates, 0, clone2, 0, p.coordinates.length);
		this.maxBorder = clone2;

		level = 0;
	}
	
	/**
	 * Empty constructor for initialization purposes
	 */
	public MBR() {
		this.tree = null;
		tid = -1;
	}
	
	/**
	 * For root node, has no parent
	 */
	private MBR(RVariant _tree){
		this.tree = _tree;
		minBorder = null;
		maxBorder = null;
		tid = -1;
		//allocate the storage for the pointers
		children = new MBR[tree.SPLIT_SIZE];
		level = 1;
	}
	
	public MBR(RVariant _tree, MBR _parent) {
		this.tree = _tree;
		minBorder = null;
		maxBorder = null;
		tid = -1;
		//allocate the storage for the pointers
		children = new MBR[tree.SPLIT_SIZE];

		level = _parent.level;
		parent = _parent;
	}
	
	/**
	 * Call this from outside the MBR class to create a new root node, do want the root node constructor to be callable ...
	 * @param _tree
	 * @return
	 */
	public static final MBR getInitialRoot(RVariant _tree){return new MBR(_tree);};
	
	/**
	 * MBR contains space where *query* is located
	 * @param query
	 * @return
	 */
	public final int getExact(Point query){
		//IF LEAF -> over all points
		if (isLeaf()){
			//for each point in this leaf
			for (int point=0;point<size();point++){
				//re-consider MIN_BORDER == MAX_BORDER
				MBR temp = this.children[point];
				int[] toCompare = tree.STORE.getPoint(temp.tid);
				//for every coordinate (dimension)
				//try to prove they are not the same and stop than
				if(Util.equals(toCompare, query.coordinates))
					return temp.tid;
			}
		}else{//if inner node or root -> determine subtrees
			for(int mbr=0;mbr<size();mbr++){
				MBR currMBR = children[mbr];
				//does this sub-tree possibly contains the point
				if(FULL_OVERLAP==currMBR.intersects(query)){
					int tid = currMBR.getExact(query);
					if (tid!=Index.NOT_FOUND)//return first result point is contained
						return tid;
				}
			}
		}
		//Could not find the point
		return Index.NOT_FOUND;
	}
	
	private final int intersects(Point query) {
		//try to prove that in one dimension there is NO overlap
		for(int dim=0;dim<query.coordinates.length;dim++){
			if((this.minBorder[dim]>query.coordinates[dim])//left of minBorder
				||(query.coordinates[dim]>maxBorder[dim]))//right of maxBorder
				return NO_OVERLAPP;//could prove there is no overlap
		}
		return FULL_OVERLAP;
	}
	
	/** For intersects(Point[] query) */
	private static final int NO_OVERLAPP = 1, PARTIAL_OVERLAP = 2, FULL_OVERLAP = 3;  
	
	public final boolean isPoint()	{	return level == 0;		}
	public final boolean isLeaf()	{	return level == 1;		}
	public final boolean isRoot()	{	return parent == null;	}//has no parent
	
	///////////////////////////////////////////BEGIN INSERT STUFF//////////////////////////
	
	/**
	 * Really do the insert
	 * @param _point
	 * @return
	 * 
	 * @Assumption Dim_point = Dim_Index -> no check
	 */
	public final void insert(int tid, Point p){
		//(1) expand borders if necessary, recall (0,..,0) is in the UPPER_LEFT
		int[] clone = new int[p.coordinates.length];
		System.arraycopy(p.coordinates, 0, clone, 0, p.coordinates.length);
		expand(p.coordinates, clone);
		//(2) add to children
		MBR toInsert = new MBR(tid, p, tree, this);
		children[size()]=toInsert;
		incSize();
		
		//(3) if full -> split
		if (this.size() == tree.SPLIT_SIZE)
			splitMBR();
	}
	
	/**
	 * For insert, choose the node/sub-tree with smallest additional volume
	 * @param shallBeInserted
	 * @return
	 */
	public final long deltaVolume(Point shallBeInserted){
		long volumeOld = 1, volumeNew = 1;
		for (int dim=0;dim<this.tree.DIMENSIONS;dim++){
			int min = minBorder[dim];
			int max = maxBorder[dim];
			int pointDim = shallBeInserted.coordinates[dim];
			
			volumeOld *= max-min;
			max = (max>pointDim)? max : pointDim;
			min = (min<pointDim)? min : pointDim;
			volumeNew *= max-min;
		}
		return volumeNew-volumeOld;
	}
	
	public final long deltaVolume(MBR toInsert){
		//TODO
		return -1;
	}
	
	///////////////////////////////////////////END INSERT STUFF//////////////////////////
	
	/**
	 * For Debugging only
	 * @see java.lang.Object#toString()
	 */
	public final String toString(){
		String ret ="";
		if (isRoot())
			ret += "Root ";
		if (isLeaf())
			ret +="Leaf ";
		if (isPoint())
			ret+="Point ";
		ret+="size: "+size()+" ";
		ret+="level: "+level+" ";
		ret+="\n[";
		for(int dim=0;dim<tree.DIMENSIONS;dim++){
			ret+=minBorder[dim]+",";
		}
		ret+="]\n[";
		for(int dim=0;dim<tree.DIMENSIONS;dim++){
			ret+=maxBorder[dim]+",";
		}
		ret+="]\n";
		return ret;
	}
	
	/**
	 * This Method specifies the tree type! Default is simple SplitAlgo
	 * TODO no real r-tree right now
	 */
	public final void splitMBR(){
		//somehow distribute the points
		tree.splitAlgo.split(this);
		level++;//for root node only, other nodes aren't linked anymore ...
	}
	
	/**
	 * Similar to insert point
	 * @param toInsert
	 */
	public final void insert(MBR toInsert) {
		expand(toInsert.minBorder, toInsert.maxBorder);
		children[size()]=toInsert;
		toInsert.parent = this;
		incSize();
		
		//if full split
		if (this.size() == tree.SPLIT_SIZE)
			splitMBR();
	}

	/** Just to capsule the size field, perhaps I want to change this ...*/
	public final int size(){ return this.size;}
	public final void incSize(){this.size++;}
	public final void resetSize() {this.size=0;};
	
	private static int numExpands = 0;
	private final void expand(int[] minNew, int[] maxNew){
		numExpands++;
		boolean expanded = false;
		
		//default
		if(size()!=0){
			int[] thisMin = minBorder;
			int[] thisMax = maxBorder;
			if(numExpands==262){
				//let me dbug you
				int d=1;
			}
			for(int dim=0;dim<tree.DIMENSIONS;dim++){
				if(thisMin[dim]>minNew[dim]){
					thisMin[dim] = minNew[dim];
					expanded = true;
				}
				if(thisMax[dim]<maxNew[dim]){
					thisMax[dim] = maxNew[dim];
					expanded = true;
				}
			}	
		}else{//empty mbr
			int[] clone = new int[minNew.length];
			System.arraycopy(minNew, 0, clone, 0, minNew.length);
			minBorder = clone;
			int[] clone2 = new int[minNew.length];
			System.arraycopy(minNew, 0, clone2, 0, minNew.length);
			maxBorder = clone2;
		}
		//FIXME error when expanding upper border
		if(true){
			//check whether all
			//for each child
			for(int child=0;child<this.children.length;child++){
				if (this.children[child]==null)
					continue;
				//for each dimension
				for(int MyDim=0; MyDim<tree.DIMENSIONS;MyDim++){
					if(this.maxBorder[MyDim]<this.children[child].maxBorder[MyDim]){
						//let me debug you
						int debug = 0;
						//der ist manchmal nur mit der border der ersten punktes initialisiert
						this.maxBorder[MyDim]=this.children[child].maxBorder[MyDim];
					}
				}
			}	
		}
		if ((expanded)&&(parent!=null))//stop id reaching the root
			this.parent.expand(minBorder, maxBorder);
	}

	public KnnCollectionIntDistances getNearestNeighbor(KnnCollectionIntDistances narestNeighbor) {
		if(isLeaf()){
			//try to update (performs update only if dist is smaller)
			for(int point =0; point<this.size();point++){
				narestNeighbor.tryUpdate(this.children[point].tid);
			}
		}else{
			//try to exclude subtrees, order w.r.t. to their distance (to consider those having less dist first)
			ArrayList<MBRdist> subTreeList = new ArrayList<MBRdist>(size());
			for (int subTree=0;subTree<size();subTree++){
				long dist = this.children[subTree].getDist(narestNeighbor.QUERY);
				subTreeList.add(new MBRdist(subTree, dist));
			}
			Collections.sort(subTreeList);
			
			//handle forward to subtrees following the order of dist (asc.)
			for (int subTree=0;subTree<subTreeList.size();subTree++){
				MBRdist current = subTreeList.get(subTree);
				//if we cannot exclude this MBR
				if(current.minDist<narestNeighbor.maxDist){
					this.children[current.index].getNearestNeighbor(narestNeighbor);
				}
			}
		}
		return narestNeighbor;
	}
	
	//XXX erstmal ganz simpel Euklid ohne Wurzel
	public final long getDist(int[] QUERY){
		long sum = 0;
		long distThisDim;
		
		for(int dim=0;dim<tree.DIMENSIONS;dim++){
			int lower = this.minBorder[dim];
			int upper = this.maxBorder[dim];
			int point = QUERY[dim];
			distThisDim = (point<lower) ? lower-point :(point>upper) ? point-upper : 0;
			sum += distThisDim*distThisDim;
		}
		return sum;
	}
	
	public final class MBRdist implements Comparable<MBRdist>{
		final int index;
		final long minDist;
		
		public MBRdist(int _index, long _dist){
			index 	= _index;
			minDist	= _dist;
		}

		@Override
		public int compareTo(MBRdist toCompare) {
			return (int)(this.minDist-toCompare.minDist);
		}
	}

	
	public void windowQuery(int[] lowerBoundQuery, int[] upperBoundQuery, ArrayList<Integer> resultTIDs) {
		MBR temp;
		
		if(isLeaf()){
			int[] point;
			
			for(int child=0;child<this.size;child++){
				temp = this.children[child];
				point = tree.STORE.getPoint(temp.tid);
				if(Util.isIn(point, lowerBoundQuery, upperBoundQuery)){
					resultTIDs.add(temp.tid);
				}
			}
		}else{
			int intersection;
			for(int child=0;child<this.size;child++){
				temp = this.children[child];
				intersection = temp.intersects(lowerBoundQuery, upperBoundQuery);
				
				if(intersection == FULL_OVERLAP){
					temp.addAll(resultTIDs);
				}else if(intersection == PARTIAL_OVERLAP){
					temp.windowQuery(lowerBoundQuery, upperBoundQuery, resultTIDs);
				}
				//else prune
			}
		}
	}

	private void addAll(ArrayList<Integer> resultTIDs) {
		MBR temp;
		
		if(isLeaf()){
			for(int child=0;child<this.size;child++){
				temp = this.children[child];
				resultTIDs.add(temp.tid);
			}
		}else{
			for(int child=0;child<this.size;child++){
				temp = this.children[child];
				temp.addAll(resultTIDs);
			}
		}
	}

	private int intersects(int[] lowerBoundQuery, int[] upperBoundQuery) {
		/* 6 Cases MBR(x1,x2), Q(o1,2)
		 * Cases	    | MIN | MAX |
		 * (1) x1x2o1o2 | o1  | x2  | NO_OVERLAPP
		 * (2) x1o1x2o2 | o1  | x2  | PARTIAL_OVERLAP
		 * (3) x1o1o2x1 | o1  | o2  | PARTIAL_OVERLAP
		 * (4) o1x1o2x2 | x1  | o2  | PARTIAL_OVERLAP
		 * (5) o1x1x2o2 | x1  | x2  | FULL_OVERLAP
		 * (6) o1o2x1x2 | x1  | o2  | NO_OVERLAPP
		 */
		int intersection = FULL_OVERLAP;
		for(int dim=0;dim<this.tree.DIMENSIONS;dim++){
			if((this.maxBorder[dim]<lowerBoundQuery[dim])	||//on the left case 1
				(this.minBorder[dim]>upperBoundQuery[dim])) // on the right case 6
			{
				return NO_OVERLAPP;
			}//easier to test case 5 only, if not --> it is partial overlap
			else if(!
					((lowerBoundQuery[dim]<=this.minBorder[dim]) &&
					(this.maxBorder[dim]<=upperBoundQuery[dim])
			)){
				intersection = PARTIAL_OVERLAP;
			}//else remains FULL_OVERLAP
		}
		return intersection;
	}
}
