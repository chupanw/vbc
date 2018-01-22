package edu.cmu.cs.vbc.prog.queval.indexes;

import edu.cmu.cs.vbc.prog.queval.queval.KnnResult;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.ArrayList;

public final class Dwarf_Linearized_Small extends Index{
	static final int ROOT_OFFSET = 0;
	byte[] DWARF;
	int[] POINTER;
	static final int FIRST_DATA = 1;
	int NUM_DIM;
	
//	static final int OFFSET_COMPARE_VALUE = 0;
//	static final int OFFSET_POINTER_NEXT_LIST = 1;
	
	public Dwarf_Linearized_Small(Store s){
		super(s);
	}
	
	public int search(int[] query) {
		int pointer = ROOT_OFFSET;//start of ROOT
		int compareValue;
		
		for(int dim=0;dim<NUM_DIM;dim++){
			compareValue = query[dim];
			pointer = listContains(compareValue, pointer);
			if(pointer == NOT_FOUND)
				return NOT_FOUND;
		}

		return pointer;
	}
	
	public int listContains(final int VALUE, final int START){
		final int MY_SIZE = DWARF[START];
		int toCompare;
		
		for(int i=0;i<MY_SIZE;i++){
			toCompare=DWARF[START+FIRST_DATA+i];
			if(VALUE==toCompare)
				return POINTER[START+FIRST_DATA+i];
			else if(VALUE<toCompare)
				return NOT_FOUND;
		}
		
		return NOT_FOUND;
	}
	
	void out(){
		System.out.println("Linearized Dwarf:out()");
		outList(0,0);
	}
	
	void outList(final int START, final int DIM_INDEX){
		final int MY_SIZE = DWARF[START];
		 
		for(int i=0;i<MY_SIZE;i++){
			System.out.print(DWARF[START+FIRST_DATA+i]+"->");
			System.out.print("@"+POINTER[START+FIRST_DATA+i]+" | ");
			if(DIM_INDEX<NUM_DIM-2)//not the final node
				outList(POINTER[START+FIRST_DATA+i], DIM_INDEX+1);
			else
				outFinalList(POINTER[START+FIRST_DATA+i]);
		}
	}

	private void outFinalList(int START) {
		final int MY_SIZE = DWARF[START];
		for(int i=0;i<MY_SIZE;i++){
			System.out.print(" "+DWARF[START+FIRST_DATA+i]+"->");
			System.out.print("TID:"+POINTER[START+FIRST_DATA+i]);
		}
		System.out.println();
	}

	@Override
	protected void buildIndex() {
		Dwarf d = new Dwarf(this.STORE);
		d.build();
		
		int requiredInts = d.requiredInts();
		
		DWARF   = new byte[requiredInts];
		POINTER = new int[requiredInts];
		NUM_DIM = d.numDim();
		
		int writePointer = 0;
		d.linearize(DWARF, POINTER, writePointer);
		d = null;//dear garbage collector remove the Dwarf
	}

	@Override
	public KnnResult searchKNN(int[] query, int k) {
		return null;
	}

	@Override
	boolean canKNNSearch() {
		return false;
	}

	@Override
	void clear() {	
	}

	@Override
	String getInfo() {
		return "List-Linearized Dwarf";
	}

	@Override
	public String getName() {
		return "Linearized Dwarf";
	}

	private void evluateWindowOnList(final int DIMENSION, final int START_LIST, final ArrayList<Integer> RESULTS, int[] lowerBoundQuery, int[] upperBoundQuery){
		final int MY_SIZE = DWARF[START_LIST];
		int toCompare;
			
		if(DIMENSION<NUM_DIM-1){
			for(int i=0;i<MY_SIZE;i++){
				toCompare=DWARF[START_LIST+FIRST_DATA+i];
				if(isIn(lowerBoundQuery[DIMENSION], upperBoundQuery[DIMENSION], toCompare))
					evluateWindowOnList(DIMENSION+1, POINTER[START_LIST+FIRST_DATA+i], RESULTS, lowerBoundQuery, upperBoundQuery);
			}
		}else{// in final dimension add TIDs
			for(int i=0;i<MY_SIZE;i++){
				toCompare=DWARF[START_LIST+FIRST_DATA+i];
				if(isIn(lowerBoundQuery[DIMENSION], upperBoundQuery[DIMENSION], toCompare))
					RESULTS.add(POINTER[START_LIST+FIRST_DATA+i]);
			}
		}
	}
	
	boolean isIn(final int min, final int max, final int value){
		return ((value>=min)&&(value<=max)) ? true : false;
	}
	
	@Override
	public ArrayList<Integer> windowQuery(int[] lowerBoundQuery, int[] upperBoundQuery) {
		ArrayList<Integer> resultTIDS = new ArrayList<Integer>(16);
		evluateWindowOnList(0, ROOT_OFFSET, resultTIDS, lowerBoundQuery, upperBoundQuery);
		return resultTIDS;
	}
}
