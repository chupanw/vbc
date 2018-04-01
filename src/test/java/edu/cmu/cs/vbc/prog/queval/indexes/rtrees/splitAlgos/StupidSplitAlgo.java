package edu.cmu.cs.vbc.prog.queval.indexes.rtrees.splitAlgos;

import edu.cmu.cs.vbc.prog.queval.indexes.RVariant;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;

public final class StupidSplitAlgo extends SplitAlgorithm {
		@Override
		public final void split(MBR toSplit) {
			int numFirstMBR = toSplit.size()/2;
			RVariant tree = toSplit.tree;
			
			//first one
			MBR first = new MBR(tree, toSplit);
			for (int i=0;i< numFirstMBR;i++){
				MBR toInsert = toSplit.children[i];
				toInsert.parent = first;
				first.insert(toInsert);
			}
			//second one, start with i = numFirstMBR
			MBR second = new MBR(tree, toSplit);
			for (int i=numFirstMBR;i< toSplit.children.length;i++){
				MBR toInsert = toSplit.children[i];
				toInsert.parent = second;
				second.insert(toInsert);
			}
			
			super.handleLinkingMBRs(toSplit, first, second, tree);
		}
		
		public final String toString(){return "Simple split Algo";}

	}

