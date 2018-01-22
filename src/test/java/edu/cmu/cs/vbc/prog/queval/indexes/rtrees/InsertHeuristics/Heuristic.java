package edu.cmu.cs.vbc.prog.queval.indexes.rtrees.InsertHeuristics;

import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.Point;


public interface Heuristic {
	/**
	 * Get the best node to insert this point. 
	 * @param shallBeInserted
	 * @return Some punishment value, if large inserting here is not good, if zero = no expansion
	 */
	public InsertHeuristic getInsertNode(MBR current, Point toInsert);
}
