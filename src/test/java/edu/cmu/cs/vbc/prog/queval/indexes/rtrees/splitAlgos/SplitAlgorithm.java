package edu.cmu.cs.vbc.prog.queval.indexes.rtrees.splitAlgos;

import edu.cmu.cs.vbc.prog.queval.indexes.RVariant;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;

public abstract class SplitAlgorithm {
	public abstract void split(MBR toSplit);
	public abstract String toString();
	
	final void handleLinkingMBRs(MBR toSplit, MBR first, MBR second, RVariant tree){
		if(toSplit.isRoot()||toSplit.parent.size()==tree.SPLIT_SIZE){
			toSplit.resetSize();
			MBR[] newChildren = new MBR[tree.SPLIT_SIZE];
			newChildren[toSplit.size()] = first;
			toSplit.incSize();
			newChildren[toSplit.size()] = second;
			toSplit.incSize();
			toSplit.children = newChildren;
		}else{
			//get index of this, in parent's children
			int i=0;
			for (;i<toSplit.parent.size();i++){
				if (toSplit.parent.children[i]==toSplit)
					break;
			}
			toSplit.parent.children[i] = first;
			first.parent = toSplit.parent;
			first.level = toSplit.parent.level-1;
			//XXX unn�tiger test auf resize -> can speed up insert
			toSplit.parent.insert(second);
		}	
	}
}
