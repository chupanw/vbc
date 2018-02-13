package edu.cmu.cs.vbc.prog.queval.queval;

import edu.cmu.cs.varex.annotation.VConditional;

public class Configuration {

//	final static boolean True = true;
//	final static boolean False = false;

	public static boolean valid() {
		return Root  &&  (!Root  ||  quert_type)  &&  (!Root  ||  index)  &&  (!quert_type  ||  Root)  &&  (!index  ||  Root)  &&  (!SplitSize  ||  Root)  &&  (!splitAlgos  ||  Root)  &&  (!InsertHeuristics  ||  Root)  &&  (!quert_type  ||  EXACT_MATCH_QUERY  ||  KNN_QUERY  ||  EPSILON_NN_QUERY  ||  RANGE_QUERY)  &&  (!EXACT_MATCH_QUERY  ||  quert_type)  &&  (!KNN_QUERY  ||  quert_type)  &&  (!EPSILON_NN_QUERY  ||  quert_type)  &&  (!RANGE_QUERY  ||  quert_type)  &&  (!EXACT_MATCH_QUERY  ||  !KNN_QUERY)  &&  (!EXACT_MATCH_QUERY  ||  !EPSILON_NN_QUERY)  &&  (!EXACT_MATCH_QUERY  ||  !RANGE_QUERY)  &&  (!KNN_QUERY  ||  !EPSILON_NN_QUERY)  &&  (!KNN_QUERY  ||  !RANGE_QUERY)  &&  (!EPSILON_NN_QUERY  ||  !RANGE_QUERY)  &&  (!KNN_QUERY  ||  EucleadeanSqrd  ||  Manhatten)  &&  (!EucleadeanSqrd  ||  KNN_QUERY)  &&  (!Manhatten  ||  KNN_QUERY)  &&  (!EucleadeanSqrd  ||  !Manhatten)  &&  (!index  ||  RVariant  ||  Dwarf  ||  Dwarf_Linearized_Small  ||  MyKDTree  ||  SeqScan  ||  VA_SSA  ||  GiSTII)  &&  (!RVariant  ||  index)  &&  (!Dwarf  ||  index)  &&  (!Dwarf_Linearized_Small  ||  index)  &&  (!MyKDTree  ||  index)  &&  (!SeqScan  ||  index)  &&  (!VA_SSA  ||  index)  &&  (!GiSTII  ||  index)  &&  (!RVariant  ||  !Dwarf)  &&  (!RVariant  ||  !Dwarf_Linearized_Small)  &&  (!RVariant  ||  !MyKDTree)  &&  (!RVariant  ||  !SeqScan)  &&  (!RVariant  ||  !VA_SSA)  &&  (!RVariant  ||  !GiSTII)  &&  (!Dwarf  ||  !Dwarf_Linearized_Small)  &&  (!Dwarf  ||  !MyKDTree)  &&  (!Dwarf  ||  !SeqScan)  &&  (!Dwarf  ||  !VA_SSA)  &&  (!Dwarf  ||  !GiSTII)  &&  (!Dwarf_Linearized_Small  ||  !MyKDTree)  &&  (!Dwarf_Linearized_Small  ||  !SeqScan)  &&  (!Dwarf_Linearized_Small  ||  !VA_SSA)  &&  (!Dwarf_Linearized_Small  ||  !GiSTII)  &&  (!MyKDTree  ||  !SeqScan)  &&  (!MyKDTree  ||  !VA_SSA)  &&  (!MyKDTree  ||  !GiSTII)  &&  (!SeqScan  ||  !VA_SSA)  &&  (!SeqScan  ||  !GiSTII)  &&  (!VA_SSA  ||  !GiSTII)  &&  (!VA_SSA  ||  BPD4  ||  BPD6  ||  BPD7)  &&  (!BPD4  ||  VA_SSA)  &&  (!BPD6  ||  VA_SSA)  &&  (!BPD7  ||  VA_SSA)  &&  (!BPD4  ||  !BPD6)  &&  (!BPD4  ||  !BPD7)  &&  (!BPD6  ||  !BPD7)  &&  (!SplitSize  ||  SS11  ||  SS17)  &&  (!SS11  ||  SplitSize)  &&  (!SS17  ||  SplitSize)  &&  (!SS11  ||  !SS17)  &&  (!splitAlgos  ||  RStarSplit  ||  LinearSplit  ||  QuadraticCostAlgorithm  ||  StupidSplitAlgo)  &&  (!RStarSplit  ||  splitAlgos)  &&  (!LinearSplit  ||  splitAlgos)  &&  (!QuadraticCostAlgorithm  ||  splitAlgos)  &&  (!StupidSplitAlgo  ||  splitAlgos)  &&  (!RStarSplit  ||  !LinearSplit)  &&  (!RStarSplit  ||  !QuadraticCostAlgorithm)  &&  (!RStarSplit  ||  !StupidSplitAlgo)  &&  (!LinearSplit  ||  !QuadraticCostAlgorithm)  &&  (!LinearSplit  ||  !StupidSplitAlgo)  &&  (!QuadraticCostAlgorithm  ||  !StupidSplitAlgo)  &&  (!InsertHeuristics  ||  GuttmanInsert  ||  RStartInsert)  &&  (!GuttmanInsert  ||  InsertHeuristics)  &&  (!RStartInsert  ||  InsertHeuristics)  &&  (!GuttmanInsert  ||  !RStartInsert)  &&  (!RVariant  ||  SplitSize)  &&  (!RVariant  ||  splitAlgos)  &&  (!RVariant  ||  InsertHeuristics)  &&  (!GiSTII  ||  SplitSize)  &&  (!GiSTII  ||  splitAlgos)  &&  (!GiSTII  ||  InsertHeuristics)  &&  (!SplitSize  ||  !splitAlgos  ||  !InsertHeuristics  ||  RVariant  ||  GiSTII)  &&  (!GiSTII  ||  StupidSplitAlgo  ||  RStarSplit);	
	}

	public static boolean validWithoutExceptions() {
		return valid() && !(VA_SSA && KNN_QUERY) && !(QuadraticCostAlgorithm && RVariant);
	}

//	public static boolean bug1() {
//		return InsertHeuristics && SplitSize && RVariant && QuadraticCostAlgorithm && splitAlgos && SS11 && !Dwarf && !SS17 && ((GuttmanInsert && !RStartInsert) || (RStartInsert && !GuttmanInsert));
//	}

//	public static boolean bug2() {
//		return InsertHeuristics && SplitSize && RVariant && QuadraticCostAlgorithm && splitAlgos && !SS11 && !Dwarf && SS17 && ((GuttmanInsert && !RStartInsert) || (RStartInsert && !GuttmanInsert));
//	}

//	public static boolean bug3() {
//		return ((!(SeqScan)&&!(MyKDTree)&&!(EPSILON_NN_QUERY)&&!(RANGE_QUERY)&&!(GiSTII)&&(EXACT_MATCH_QUERY)&&(VA_SSA)&&(((InsertHeuristics)&&((!(SplitSize)&&((!(LinearSplit)&&!(StupidSplitAlgo)&&(RStarSplit)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm))||(!(RStarSplit)&&((!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm)&&(LinearSplit))||(!(LinearSplit)&&((!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&(QuadraticCostAlgorithm))||((StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm)))))))&&(splitAlgos))||(!(splitAlgos)&&(((SplitSize)&&!(LinearSplit)&&!(StupidSplitAlgo)&&((!(RStartInsert)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&(GuttmanInsert))||((RStartInsert)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&!(GuttmanInsert)))&&!(RStarSplit)&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(RStarSplit)&&!(SplitSize)&&!(QuadraticCostAlgorithm))))))||(!(InsertHeuristics)&&((!(SplitSize)&&((!(Manhatten)&&!(LinearSplit)&&!(StupidSplitAlgo)&&(RStarSplit)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)&&!(QuadraticCostAlgorithm))||(!(RStarSplit)&&((!(Manhatten)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(LinearSplit)&&!(GuttmanInsert)&&!(SS17)&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&((!(Manhatten)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(QuadraticCostAlgorithm)&&!(GuttmanInsert)&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)&&(StupidSplitAlgo)&&!(QuadraticCostAlgorithm)))))))&&(splitAlgos))||(!(splitAlgos)&&(((SplitSize)&&!(LinearSplit)&&!(StupidSplitAlgo)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&!(RStartInsert)&&!(RStarSplit)&&!(GuttmanInsert)&&!(QuadraticCostAlgorithm))||(!(Manhatten)&&!(LinearSplit)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&!(RStarSplit)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SplitSize)&&!(SS17)&&!(QuadraticCostAlgorithm)))))))&&!(Dwarf)&&!(RVariant)&&!(KNN_QUERY)&&!(Dwarf_Linearized_Small))) || bug5_2();
//	}

//	public static boolean bug3_2() {
//		return (!(EXACT_MATCH_QUERY)&&((!(SeqScan)&&!(MyKDTree)&&!(EPSILON_NN_QUERY)&&!(RANGE_QUERY)&&(KNN_QUERY)&&!(GiSTII)&&(VA_SSA)&&!(Dwarf)&&(((InsertHeuristics)&&((!(SplitSize)&&((!(LinearSplit)&&!(StupidSplitAlgo)&&((!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(SS17))||((RStartInsert)&&!(SS11)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(GuttmanInsert)&&!(SS17)))&&(RStarSplit)&&!(QuadraticCostAlgorithm))||(!(RStarSplit)&&((!(StupidSplitAlgo)&&((!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(SS17))||((RStartInsert)&&!(SS11)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm)&&(LinearSplit))||(!(LinearSplit)&&((!(StupidSplitAlgo)&&((!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(SS17))||((RStartInsert)&&!(SS11)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(GuttmanInsert)&&!(SS17)))&&(QuadraticCostAlgorithm))||((StupidSplitAlgo)&&((!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(SS17))||((RStartInsert)&&!(SS11)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm)))))))&&(splitAlgos))||(!(splitAlgos)&&(((SplitSize)&&!(LinearSplit)&&!(StupidSplitAlgo)&&((!(RStartInsert)&&((!(SS17)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&(SS11))||((SS17)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(SS11)))&&(GuttmanInsert))||((RStartInsert)&&((!(SS17)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&(SS11))||((SS17)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(SS11)))&&!(GuttmanInsert)))&&!(RStarSplit)&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&!(StupidSplitAlgo)&&((!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(SS17))||((RStartInsert)&&!(SS11)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(GuttmanInsert)&&!(SS17)))&&!(RStarSplit)&&!(SplitSize)&&!(QuadraticCostAlgorithm))))))||(!(InsertHeuristics)&&((!(SplitSize)&&((!(LinearSplit)&&!(StupidSplitAlgo)&&(RStarSplit)&&!(SS11)&&!(RStartInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(GuttmanInsert)&&!(SS17)&&!(QuadraticCostAlgorithm))||(!(RStarSplit)&&((!(StupidSplitAlgo)&&!(SS11)&&!(RStartInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&(LinearSplit)&&!(GuttmanInsert)&&!(SS17)&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&((!(StupidSplitAlgo)&&!(SS11)&&!(RStartInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&(QuadraticCostAlgorithm)&&!(GuttmanInsert)&&!(SS17))||(!(SS11)&&!(RStartInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(GuttmanInsert)&&!(SS17)&&(StupidSplitAlgo)&&!(QuadraticCostAlgorithm)))))))&&(splitAlgos))||(!(splitAlgos)&&(((SplitSize)&&!(LinearSplit)&&!(StupidSplitAlgo)&&!(RStartInsert)&&!(RStarSplit)&&!(GuttmanInsert)&&((!(SS17)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&(SS11))||((SS17)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(SS11)))&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&!(StupidSplitAlgo)&&!(SS11)&&!(RStartInsert)&&((!(Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(EucleadeanSqrd))||((Manhatten)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(EucleadeanSqrd)))&&!(RStarSplit)&&!(GuttmanInsert)&&!(SplitSize)&&!(SS17)&&!(QuadraticCostAlgorithm)))))))&&!(RVariant)&&!(Dwarf_Linearized_Small))|| bug5_3()
//		));
//	}

//	public static boolean bug3_3() {
//		return (!(KNN_QUERY)&&((!(SeqScan)&&!(MyKDTree)&&(EPSILON_NN_QUERY)&&!(RANGE_QUERY)&&!(GiSTII)&&(VA_SSA)&&(((InsertHeuristics)&&((!(SplitSize)&&((!(LinearSplit)&&!(StupidSplitAlgo)&&(RStarSplit)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm))||(!(RStarSplit)&&((!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm)&&(LinearSplit))||(!(LinearSplit)&&((!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&(QuadraticCostAlgorithm))||((StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm)))))))&&(splitAlgos))||(!(splitAlgos)&&(((SplitSize)&&!(LinearSplit)&&!(StupidSplitAlgo)&&((!(RStartInsert)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&(GuttmanInsert))||((RStartInsert)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&!(GuttmanInsert)))&&!(RStarSplit)&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(RStarSplit)&&!(SplitSize)&&!(QuadraticCostAlgorithm))))))||(!(InsertHeuristics)&&((!(SplitSize)&&((!(Manhatten)&&!(LinearSplit)&&!(StupidSplitAlgo)&&(RStarSplit)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)&&!(QuadraticCostAlgorithm))||(!(RStarSplit)&&((!(Manhatten)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(LinearSplit)&&!(GuttmanInsert)&&!(SS17)&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&((!(Manhatten)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(QuadraticCostAlgorithm)&&!(GuttmanInsert)&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)&&(StupidSplitAlgo)&&!(QuadraticCostAlgorithm)))))))&&(splitAlgos))||(!(splitAlgos)&&(((SplitSize)&&!(LinearSplit)&&!(StupidSplitAlgo)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&!(RStartInsert)&&!(RStarSplit)&&!(GuttmanInsert)&&!(QuadraticCostAlgorithm))||(!(Manhatten)&&!(LinearSplit)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&!(RStarSplit)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SplitSize)&&!(SS17)&&!(QuadraticCostAlgorithm)))))))&&!(Dwarf)&&!(RVariant)&&!(Dwarf_Linearized_Small))||(!(SeqScan)&&!(MyKDTree)&&!(EPSILON_NN_QUERY)&&!(GiSTII)&&(RANGE_QUERY)&&(VA_SSA)&&(((InsertHeuristics)&&((!(SplitSize)&&((!(LinearSplit)&&!(StupidSplitAlgo)&&(RStarSplit)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm))||(!(RStarSplit)&&((!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm)&&(LinearSplit))||(!(LinearSplit)&&((!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&(QuadraticCostAlgorithm))||((StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(QuadraticCostAlgorithm)))))))&&(splitAlgos))||(!(splitAlgos)&&(((SplitSize)&&!(LinearSplit)&&!(StupidSplitAlgo)&&((!(RStartInsert)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&(GuttmanInsert))||((RStartInsert)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&!(GuttmanInsert)))&&!(RStarSplit)&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&!(StupidSplitAlgo)&&((!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&(GuttmanInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&(RStartInsert)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)))&&!(RStarSplit)&&!(SplitSize)&&!(QuadraticCostAlgorithm))))))||(!(InsertHeuristics)&&((!(SplitSize)&&((!(Manhatten)&&!(LinearSplit)&&!(StupidSplitAlgo)&&(RStarSplit)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)&&!(QuadraticCostAlgorithm))||(!(RStarSplit)&&((!(Manhatten)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(LinearSplit)&&!(GuttmanInsert)&&!(SS17)&&!(QuadraticCostAlgorithm))||(!(LinearSplit)&&((!(Manhatten)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(QuadraticCostAlgorithm)&&!(GuttmanInsert)&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SS17)&&(StupidSplitAlgo)&&!(QuadraticCostAlgorithm)))))))&&(splitAlgos))||(!(splitAlgos)&&(((SplitSize)&&!(LinearSplit)&&!(StupidSplitAlgo)&&((!(Manhatten)&&(SS11)&&!(EucleadeanSqrd)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(SS17))||(!(Manhatten)&&!(EucleadeanSqrd)&&!(SS11)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&(SS17)))&&!(RStartInsert)&&!(RStarSplit)&&!(GuttmanInsert)&&!(QuadraticCostAlgorithm))||(!(Manhatten)&&!(LinearSplit)&&!(StupidSplitAlgo)&&!(EucleadeanSqrd)&&!(SS11)&&!(RStartInsert)&&!(RStarSplit)&&((!(BPD6)&&!(BPD7)&&(BPD4))||(!(BPD4)&&(((BPD6)&&!(BPD7))||(!(BPD6)&&(BPD7)))))&&!(GuttmanInsert)&&!(SplitSize)&&!(SS17)&&!(QuadraticCostAlgorithm)))))))&&!(Dwarf)&&!(RVariant)&&!(Dwarf_Linearized_Small))));
//	}

//	public static boolean BPDs() {
//		return (BPD4 && !BPD6 && !BPD7) || (BPD6 && !BPD4 && !BPD7) || (BPD7 && !BPD4 && !BPD6);
//	}

	public static void initFeatures(String[] args) {
		int index = 0;
		BPD4 = Boolean.valueOf(args[index++]);
		BPD6 = Boolean.valueOf(args[index++]);
		BPD7 = Boolean.valueOf(args[index++]);
		Dwarf = Boolean.valueOf(args[index++]);
		Dwarf_Linearized_Small = Boolean.valueOf(args[index++]);
		EPSILON_NN_QUERY = Boolean.valueOf(args[index++]);
		EucleadeanSqrd = Boolean.valueOf(args[index++]);
		EXACT_MATCH_QUERY = Boolean.valueOf(args[index++]);
		GiSTII = Boolean.valueOf(args[index++]);
		GuttmanInsert = Boolean.valueOf(args[index++]);
		InsertHeuristics = Boolean.valueOf(args[index++]);
		KNN_QUERY = Boolean.valueOf(args[index++]);
		LinearSplit = Boolean.valueOf(args[index++]);
		Manhatten = Boolean.valueOf(args[index++]);
		MyKDTree = Boolean.valueOf(args[index++]);
		QuadraticCostAlgorithm = Boolean.valueOf(args[index++]);
		RANGE_QUERY = Boolean.valueOf(args[index++]);
		RStarSplit = Boolean.valueOf(args[index++]);
		RStartInsert = Boolean.valueOf(args[index++]);
		RVariant = Boolean.valueOf(args[index++]);
		SeqScan = Boolean.valueOf(args[index++]);
		splitAlgos = Boolean.valueOf(args[index++]);
		SplitSize = Boolean.valueOf(args[index++]);
		SS11 = Boolean.valueOf(args[index++]);
		SS17 = Boolean.valueOf(args[index++]);
		StupidSplitAlgo = Boolean.valueOf(args[index++]);
		VA_SSA = Boolean.valueOf(args[index++]);
	}

	// abstract features
	private final static boolean Root = true;
	private final static boolean quert_type = true;
	private final static boolean index = true;

	// query plans
	@VConditional
	public static boolean EXACT_MATCH_QUERY = false;
	@VConditional
	public static boolean KNN_QUERY = false;
	@VConditional
	public static boolean EPSILON_NN_QUERY = false;
	@VConditional
	public static boolean RANGE_QUERY = true;

	// index types
	@VConditional
	public static boolean Dwarf = false;
	@VConditional
	public static boolean Dwarf_Linearized_Small = false;
	@VConditional
	public static boolean MyKDTree = false;
	@VConditional
	public static boolean SeqScan = false;
	@VConditional
	public static boolean RVariant = false;
	@VConditional
	public static boolean GiSTII = false;
	@VConditional
	public static boolean VA_SSA = true;

	// select implied abstract features for variability-aware
	@VConditional
	public static boolean InsertHeuristics = false;
	@VConditional
	public static boolean splitAlgos = true;
	@VConditional
	public static boolean SplitSize = false;

	// split algorithmen 4 Rtree
	@VConditional
	public static boolean RStarSplit = true;
	@VConditional
	public static boolean LinearSplit = false;
	@VConditional
	public static boolean QuadraticCostAlgorithm = false;
	@VConditional
	public static boolean StupidSplitAlgo = false;

	// Insert Heuristics 4 Rtree
	@VConditional
	public static boolean GuttmanInsert = false;
	@VConditional
	public static boolean RStartInsert = false;

	// Split Size for Rtree
	@VConditional
	public static boolean SS11 = false;
	@VConditional
	public static boolean SS17 = false;
	
	@VConditional
	public static boolean EucleadeanSqrd = false;
	@VConditional
	public static boolean Manhatten = false;
	
	// bit per dimension 4 VAFile
	@VConditional
	public static boolean BPD4 = true;
	@VConditional
	public static boolean BPD6 = false;
	@VConditional
	public static boolean BPD7 = false;
	
	
}
