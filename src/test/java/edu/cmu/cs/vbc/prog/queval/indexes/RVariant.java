package edu.cmu.cs.vbc.prog.queval.indexes;

import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.InsertHeuristics.GuttmanInsert;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.InsertHeuristics.Heuristic;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.InsertHeuristics.InsertHeuristic;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.InsertHeuristics.RStartInsert;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.Point;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.splitAlgos.*;
import edu.cmu.cs.vbc.prog.queval.queval.Configuration;
import edu.cmu.cs.vbc.prog.queval.queval.KnnCollectionIntDistances;
import edu.cmu.cs.vbc.prog.queval.queval.KnnResult;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.ArrayList;
import java.util.LinkedList;

public class RVariant extends Index{
	// param -> Split Size
	public final int MIN_POINTS_MBR = 2, SPLIT_SIZE, DIMENSIONS, MAX_DIM;
	public final MBR root;
	//public final SplitAlgorithm splitAlgo = new StupidSplitAlgo();
	public final SplitAlgorithm splitAlgo;  // param -> 4- alternativen
	public final Heuristic insertHeuristic;  // param -> 3-alternativen
	//public final Heuristic insertHeuristic = new index.MartinsRtree.InsertHeuristics.GuttmanInsert();
	
	public RVariant(Store s) {
		super(s); 
		if (Configuration.RStarSplit) {
			splitAlgo = new RStarSplit();
		} else if (Configuration.LinearSplit) {
			splitAlgo = new LinearSplit();
		} else if (Configuration.QuadraticCostAlgorithm) {// XXX Throws java.lang.StackOverflowError
			splitAlgo = new QuadraticCostAlgorithm();
		} else if (Configuration.StupidSplitAlgo) {
			splitAlgo = new StupidSplitAlgo();
		} else {
			throw new RuntimeException("Split algo not selected");
		}
		if (Configuration.RStartInsert) {
			insertHeuristic = new RStartInsert();
		} else if (Configuration.GuttmanInsert) {
			insertHeuristic = new GuttmanInsert();
		} else {
			throw new RuntimeException("Split algo not selected");
		}
		
		DIMENSIONS = numDim();
		
		if (Configuration.SS11) {
			SPLIT_SIZE = 11;
		} else if (Configuration.SS17) {
			SPLIT_SIZE = 17;
		} else {
			throw new RuntimeException("Split size not selected");
		}
		
		
		
		MAX_DIM = STORE.MAX_VALUE;
		root = MBR.getInitialRoot(this);
	}
	
	public void buildIndex(){

		int[] dataset;
		
		for(int tid=0;tid<STORE.size();tid++){
			dataset = STORE.getPoint(tid);
			insert(dataset, tid);
		}
	}

	public boolean insert(int[] dataset, final int TID) {
		Point toInsert = new Point(dataset);
		if(!root.isLeaf()){
			//the root node searches for best node, this is done to be extendable somehow ...
			InsertHeuristic heuristics = this.insertHeuristic.getInsertNode(root,toInsert);
			//now really do the insert and if necessary the splitting
			heuristics.mbr.insert(TID,toInsert);
		}else{//only for first points
			root.insert(TID,toInsert);
		}
		return true;
	}

	@Override
	public int search(int[] query) {
		return root.getExact(new Point(query));
	}

	@Override
	public KnnResult searchKNN(int[] query, int k) {
		KnnCollectionIntDistances col = KnnCollectionIntDistances.create_With_Empty_Collection(k, this.STORE, query);
		root.getNearestNeighbor(col);
		return col.getResult();
	}

	

	@Override
	public final String getInfo() {
		return "This is an R-Tree variant with "+ splitAlgo.toString() +'\n'
			+ insertHeuristic.toString() + '\n'
			+ "Min points " + MIN_POINTS_MBR + "\n"
			+ "Max points " + (SPLIT_SIZE-1)
		;
	}
	
	@Override void clear() 			 {			 				}
	@Override public String getName(){return "R-Tree Variant";	}
	@Override boolean canKNNSearch() {return true;				}
	public final int getDimMinValue(){return STORE.MIN_VALUE;	}
	public final int getDimMaxValue(){return STORE.MAX_VALUE;	}
	
	public String toString(){
		if (root==null)
			return "Empty Tree";
		StringBuilder sb = new StringBuilder();
		LinkedList<MBR> queue = new LinkedList<MBR>();
		queue.add(root);
		int lastLevel = -1;
		while(!queue.isEmpty()){
			MBR current = queue.removeFirst();
			int thisLevel = current.level;
			//sb.append(current.toString());
			if (!current.isLeaf()){
				for(int child =0; child<current.size();child++){
					queue.add(current.children[child]);
				}
			}
			//reached new level
			if (lastLevel!=thisLevel)
				sb.append("----- new level \n");
			//sb.append(current.hashCode());
			if (current==root)
				sb.append("Root ");
			if(current.isLeaf())
				sb.append("Leaf ");
			if(current.isPoint())
				sb.append("Point ");
			sb.append("@ lvl "+current.level);
			sb.append("@ size "+current.size());
			sb.append('\t');
			lastLevel = current.level;
		}
		
		return sb.toString();
	}

	@Override
	public ArrayList<Integer> windowQuery(int[] lowerBoundQuery, int[] upperBoundQuery) {
		ArrayList<Integer> resultTIDs = new ArrayList<Integer>(16);
		root.windowQuery(lowerBoundQuery,upperBoundQuery, resultTIDs);
		return resultTIDs;
	}
}
