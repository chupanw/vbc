/**
 * 
 */
package edu.cmu.cs.vbc.prog.queval.indexes.rtrees.splitAlgos;

import edu.cmu.cs.vbc.prog.queval.indexes.RVariant;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;
import edu.cmu.cs.vbc.prog.queval.queval.Metrics;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * @author amartin
 * This is an adaption of the linear split algorithm of Guttman�s R-Tree linear split algorithm.
 * In contrast to the original algo, we use the currently applied distance metric to distribute the points.
 */
public final class LinearSplit extends SplitAlgorithm {

	@Override
	public void split(MBR toSplit) {
		RVariant tree = toSplit.tree;
		MBR first = new MBR(tree, toSplit);
		MBR second = new MBR(tree, toSplit);
		boolean[] alreadyDistributed = new boolean[tree.SPLIT_SIZE]; 
		
		PickSeed(first, second, toSplit.children, alreadyDistributed, tree);
		DistributeRemainingMBRs(first, second, toSplit.children, alreadyDistributed);
		
		super.handleLinkingMBRs(toSplit, first, second, tree);
	}
	
	/**
	 * The seeds are the points located closest to the lowerLeft (probably {0,0,...}) and upperRight bounds of the space 
	 * @param first
	 * @param second
	 * @param toDistribute
	 * @param alreadyUsed
	 * @param tree
	 */
	private void PickSeed(MBR first, MBR second, MBR[] toDistribute, boolean[] alreadyUsed, RVariant tree){
		DistanceContainer[] lowerDistances = new DistanceContainer[toDistribute.length];
		/** e.g., {0,0,0} for three Dim with tree Min value = 0*/
		int[] lowerLeft  = new int[tree.DIMENSIONS];
		
		//init lower left & upper right corner, for distance computation, also works if not [0-255]
		final int minValue = tree.getDimMinValue();
		for(int dim=0;dim<tree.DIMENSIONS;dim++){
			lowerLeft[dim]  = minValue;
		}
		
		//compute distanced and store the min MBRs which will be the seeds
		for(int mbr=0;mbr<toDistribute.length;mbr++){
			//distance to lowerLeft {0,0,...}
			double dist = Metrics.Eucleadean(lowerLeft, toDistribute[mbr].minBorder);
			lowerDistances[mbr] = new DistanceContainer(dist, toDistribute[mbr], mbr);
		}
		Comparator<DistanceContainer> comp = new Comparator<LinearSplit.DistanceContainer>() {			
			@Override
			public final int compare(DistanceContainer o1, DistanceContainer o2) {
				return (int)(o1.distance-o2.distance);
			}
		};
		Arrays.sort(lowerDistances, comp);
		
		//get the first elems and compare them
		first.insert(lowerDistances[0].mbr);
		alreadyUsed[lowerDistances[0].index] = true;
		
		second.insert(lowerDistances[lowerDistances.length-1].mbr);
		alreadyUsed[lowerDistances[lowerDistances.length-1].index] = true;
		
	}
	
	private void DistributeRemainingMBRs(MBR first, MBR second, MBR[] toDistribute, boolean[] alreadyUsed){
		LinkedList<DistanceContainer> distToFirst  = new LinkedList<DistanceContainer>();
		int[] low  = first.minBorder;
		
		for(int mbr=0;mbr<toDistribute.length;mbr++){
			if(alreadyUsed[mbr])
				continue;//Sch�nheit h�lt sich in Grenzen
			double distLow = Metrics.Eucleadean(low, toDistribute[mbr].maxBorder);
			distToFirst.add(new DistanceContainer(distLow, toDistribute[mbr], mbr));
		}
		Comparator<DistanceContainer> comp = new Comparator<LinearSplit.DistanceContainer>() {			
			@Override
			public final int compare(DistanceContainer o1, DistanceContainer o2) {
				return (int)(o1.distance-o2.distance);
			}
		};
		java.util.Collections.sort(distToFirst, comp);
		
		while(!distToFirst.isEmpty()){
			DistanceContainer forFirst = distToFirst.removeFirst();
			first.insert(forFirst.mbr);
			
			if(distToFirst.isEmpty())
				return;
			DistanceContainer forSecond = distToFirst.removeLast();
			second.insert(forSecond.mbr);
		}
	}

	@Override
	public String toString() {
		return "Linear split algo using the Distance Metrik of the R-Tree";
	}
	
	private final class DistanceContainer{
		public final int index;//for marking which one are already used
		public final double distance;
		public final MBR mbr;
		
		public DistanceContainer(double _distance, MBR _mbr, int _index){
			this.distance 	= _distance;
			this.mbr 		= _mbr;
			this.index 		= _index;
		}
		
		public final String toString(){
			return "MBR@"+index+" with dist "+distance+'\n'+mbr.toString();
		}
	}

}
