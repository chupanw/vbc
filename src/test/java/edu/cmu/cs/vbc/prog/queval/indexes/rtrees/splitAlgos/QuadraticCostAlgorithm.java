/**
 * @author martin
 * Node split algorithm: R-Trees (1984) by A. Guttman pp. 52
 * Names of Sub-steps as in the paper  
 */

package edu.cmu.cs.vbc.prog.queval.indexes.rtrees.splitAlgos;

import edu.cmu.cs.vbc.prog.queval.indexes.RVariant;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;

import java.util.Iterator;
import java.util.LinkedList;

public final class QuadraticCostAlgorithm extends SplitAlgorithm {

	@Override
	public void split(MBR toSplit) {
		LinkedList<MBR> toDistribute = new LinkedList<MBR>();
		RVariant tree = toSplit.tree;
		
		MBR first = new MBR(tree, toSplit);
		MBR second = new MBR(tree, toSplit);
		
		for(int mbr=0;mbr<toSplit.size();mbr++){
			toDistribute.add(toSplit.children[mbr]);
		}
		//QS1
		PickSeeds(toDistribute,first,second);
		
		//for all remaining MBRs choose the minimal expansion
		int stillToDistribute;
		while(!toDistribute.isEmpty()){
			//QS2: if less than ship all remaining MBRs
			stillToDistribute = toDistribute.size();
			if(first.size()+tree.MIN_POINTS_MBR<stillToDistribute){
				shipAll(first, toDistribute);
				break;
			}else if(second.size()+tree.MIN_POINTS_MBR<stillToDistribute){
				shipAll(second, toDistribute);
				break;
			}
			//QS3: 
			PickNext(toDistribute,first,second);
		}
		
		super.handleLinkingMBRs(toSplit, first, second, tree);
	}
	
	/**
	 * Implementation of Algorithm QS1
	 * @param toDistribute
	 * Picks two MBRs of toDistribute, removes them, and adds them to first and second
	 */
	private final void PickSeeds(LinkedList<MBR> toDistribute, MBR first, MBR second){
//		System.err.println("Stupid implementation.");
		MBR insertInFirst 	= null;
		MBR insertInSecond 	= null;
		long maxDeltaVolume = Long.MIN_VALUE;
		Iterator<MBR> iterLeft = toDistribute.iterator(); 
		
		//XXX this is stupid
		while(iterLeft.hasNext()){
			MBR currentLeft = iterLeft.next();
			Iterator<MBR> iterRight = toDistribute.iterator();
			
			while(iterRight.hasNext()){
				MBR currentRight = iterRight.next();
				long curDeltaVolume = currentLeft.deltaVolume(currentRight);
				if (maxDeltaVolume<curDeltaVolume){
					insertInFirst = currentLeft;
					insertInSecond = currentRight;
					maxDeltaVolume = curDeltaVolume;
				}
			}	
		}

		first.insert(insertInFirst);
		second.insert(insertInSecond);
	}
	
	private final void shipAll(MBR insertHere, LinkedList<MBR> toInsert){
		Iterator<MBR> iter = toInsert.iterator();
		while(iter.hasNext()){
			insertHere.insert(iter.next());
		}
	}
	
	private final void PickNext(LinkedList<MBR> toDistribute, MBR first, MBR second){
		Iterator<MBR> iter = toDistribute.iterator();
		MBR current = iter.next();
		
		MBR pickThis = current;
		long currentMinDeltaVolume;
		long MinDeltaVolume;
		boolean insertInFirst;
		
		long deltaFirst = first.deltaVolume(current);
		long deltaSecond = second.deltaVolume(current);
		
		if(deltaFirst<deltaSecond){
			currentMinDeltaVolume = deltaFirst;
			insertInFirst = true;
		}else{
			currentMinDeltaVolume = deltaSecond;
			insertInFirst = false;
		}
		
		while(iter.hasNext()){
			current = iter.next();
			deltaFirst = first.deltaVolume(current);
			deltaSecond = second.deltaVolume(current);
			MinDeltaVolume = (deltaFirst<deltaSecond) ? deltaFirst : deltaSecond;
			if(MinDeltaVolume<currentMinDeltaVolume){
				currentMinDeltaVolume = MinDeltaVolume;
				pickThis = current;
				insertInFirst = (deltaFirst<deltaSecond) ? true : false;
			}
		}
		if(insertInFirst)
			first.insert(pickThis);
		else
			second.insert(pickThis);
	}
	

	@Override
	public String toString() {
		return "QuadraticCostAlgorithm";
	}

}
