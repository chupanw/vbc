package edu.cmu.cs.vbc.prog.queval.indexes.rtrees.InsertHeuristics;

import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.Point;

public final class GuttmanInsert implements Heuristic {

	@Override
	public final InsertHeuristic getInsertNode(MBR current, Point toInsert) {
		InsertHeuristic ret;
		
		// (1) if leaf, compute punishment for this
		if (current.isLeaf()){
			ret = new InsertHeuristic(0, current);
		}
		// (2) else MIN(children)->handle forward to the min sub-tree
		else {
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

}
