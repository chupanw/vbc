package edu.cmu.cs.vbc.prog.queval.defects;

import edu.cmu.cs.vbc.prog.queval.queval.Configuration;
import edu.cmu.cs.vbc.prog.queval.queval.MainClass;

public class GetArrayIndexOutOfBounds {

	public static void main(String[] args) {
//		if !EXACT_MATCH_QUERY & KNN_QUERY & !GiSTII & VA_SSA & !BPD4 & BPD6 | !EXACT_MATCH_QUERY & KNN_QUERY & !GiSTII & VA_SSA & BPD4:
//			java.lang.ArrayIndexOutOfBoundsException: 2
//				at indexes.VA_SSA.getLowerBound(VA_SSA.java:201)
//				at indexes.VA_SSA.searchKNN(VA_SSA.java:89)
//				at query_processing.QueryPlan.run(QueryPlan.java:157)
//				at queval.MainClass.run(MainClass.java:151)
//				at queval.MainClass.main(MainClass.java:75)

		Configuration.KNN_QUERY = true;
		Configuration.VA_SSA = true;
//		Configuration.BPD6 = true;
		Configuration.BPD7 = true;
		Configuration.EucleadeanSqrd = true;
		
		MainClass.run();
	}

}
