package edu.cmu.cs.vbc.prog.queval.defects;

import edu.cmu.cs.vbc.prog.queval.queval.Configuration;
import edu.cmu.cs.vbc.prog.queval.queval.MainClass;

public class GetDivByZero {
	
	public static void main(String[] args) {
		Configuration.VA_SSA = true;
		Configuration.BPD4 = true;
		Configuration.EXACT_MATCH_QUERY = true;
		
		MainClass.run();
	}

}
