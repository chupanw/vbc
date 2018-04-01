package edu.cmu.cs.vbc.prog.queval.defects;

import edu.cmu.cs.vbc.prog.queval.queval.Configuration;
import edu.cmu.cs.vbc.prog.queval.queval.MainClass;

public class GetStackOverflow {

	public static void main(String[] args) {
//		if RVariant & !GiSTII & !VA_SSA & !RStarSplit & !LinearSplit & QuadraticCostAlgorithm & SS11:
//		if RVariant & !GiSTII & !VA_SSA & !RStarSplit & !LinearSplit & QuadraticCostAlgorithm & !SS11:
		
		Configuration.RVariant = true;
		Configuration.QuadraticCostAlgorithm = true;
		Configuration.SS17 = true;
//		Configuration.SS11 = true;
		Configuration.EPSILON_NN_QUERY = true;
		Configuration.GuttmanInsert = true;
		
		MainClass.run();
	}
	
}