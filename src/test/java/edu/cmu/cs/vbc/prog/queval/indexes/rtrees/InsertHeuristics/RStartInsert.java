package edu.cmu.cs.vbc.prog.queval.indexes.rtrees.InsertHeuristics;

import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.Point;

import java.util.Collections;
import java.util.LinkedList;

public class RStartInsert implements Heuristic {

	@Override
	public InsertHeuristic getInsertNode(MBR current, Point toInsert) {
		InsertHeuristic ret;
		
		// (1) if leaf, compute punishment for this
		if (current.isLeaf()){
			ret = new InsertHeuristic(0, current);
		}
		// CS2: This is the main difference to R-Tree insert
		else if(current.children[0].isLeaf()) {
			LinkedList<MyContainer> punishements = new LinkedList<MyContainer>();
			for(int mbr=0;mbr<current.size();mbr++){
				long overlap = overlap(current, mbr, toInsert);
				long volume = current.children[mbr].deltaVolume(toInsert);
				punishements.add(new MyContainer(overlap, volume, current.children[mbr]));
			}
			//sort so that MBR with least overlap is first, resolve ties by min deltaVolume
			Collections.sort(punishements);
			ret = getInsertNode(punishements.getFirst().leafToInsert, toInsert);
		}else{
			//for inner nodes do the same as for the R-Tree
			long[] punishmentsOfSubtrees = new long[current.size()];
			for(int mbr=0;mbr<current.size();mbr++){
				//TODO ak�rzen wenn null
				punishmentsOfSubtrees[mbr] = current.children[mbr].deltaVolume(toInsert);
			}
			// get the best MBR (subtree) to hand forward
			int minMBRIndex=0;
			for(int mbr=1;mbr<current.size();mbr++){
				if (punishmentsOfSubtrees[mbr]<punishmentsOfSubtrees[minMBRIndex])
					minMBRIndex=mbr;
			}
			//hand forward
			ret = getInsertNode(current.children[minMBRIndex], toInsert);
		}
		return ret;
	}

	public long overlap(MBR current, int mbrToExtend, Point toInsert){
		long orgOverlapVolume = 0;
		long newOverlapVolume = 0;
		
		//determine new border if inserting into this leaf
		final int dimensions = current.tree.DIMENSIONS;
		int[] newMinBorder = new int[dimensions];
		int[] newMaxBorder = new int[dimensions];
		
		int[] thisMin 	= current.children[mbrToExtend].minBorder;
		int[] thisMax 	= current.children[mbrToExtend].maxBorder;
		int[] point		= toInsert.coordinates;
		
		for(int dim=0;dim<dimensions;dim++){
			newMinBorder[dim] = (thisMin[dim]<point[dim]) ? thisMin[dim] : point[dim];
			newMaxBorder[dim] = (thisMax[dim]>point[dim]) ? thisMax[dim] : point[dim];
		}
		
		for(int mbr=0;mbr<current.size();mbr++){
			if(mbrToExtend==mbr)
				continue;//XXX so lala
			//orgOverlapVolume
			orgOverlapVolume += overlap(current.children[mbr], current.children[mbrToExtend].minBorder, current.children[mbrToExtend].maxBorder);
			//newOverlapVolume
			orgOverlapVolume += overlap(current.children[mbr], newMinBorder, newMaxBorder);
		}
			
		return newOverlapVolume-orgOverlapVolume;
	}
	
	public long overlap(MBR mbr, int[] minBorder, int[] maxBorder){
		//try to prove intersection in one dimension them return 0L else compute intersection
		long overlap = 1;
		for(int dim=0;dim<mbr.tree.DIMENSIONS;dim++){
			/* 6 Cases R(x1,x2), R(o1,2)
			 * Cases	| MIN | MAX |
			 * x1x2o1o2 | o1  | x2  | MIN > MAX -> no intersect
			 * x1o1x2o2 | o1  | x2  | 
			 * x1o1o2x1 | o1  | o2  | 
			 * o1x1o2x2 | x1  | o2  | 
			 * o1x1x2o2 | x1  | x2  |
			 * o1o2x1x2 | x1  | o2  | MIN > MAX -> no intersect
			 */
			int intersectMin = Math.max(mbr.minBorder[dim], minBorder[dim]);
			int intersectMax = Math.min(mbr.maxBorder[dim], maxBorder[dim]);
			if(intersectMin>intersectMax){//no intersect see above
				return 0;//I like this line, no overlap at all
			}else
				overlap *= (intersectMax-intersectMin);
		}
		
		return overlap;
	}
	
	private final class MyContainer implements Comparable<MyContainer>{
		public final long overlap;
		public final long deltaVolume;
		public final MBR leafToInsert;
		
		public MyContainer(long _overlap, long _deltaVolume, MBR _leaf){
			this.overlap 		= _overlap;
			this.deltaVolume 	= _deltaVolume;
			this.leafToInsert 	= _leaf;
		}
		
		public final int compareTo(MyContainer compare){
			return (int)( (this.overlap!=compare.overlap) ? this.overlap-compare.overlap : this.deltaVolume-compare.deltaVolume);
		}
	}
}
