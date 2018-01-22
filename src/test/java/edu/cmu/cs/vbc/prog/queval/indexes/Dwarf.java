package edu.cmu.cs.vbc.prog.queval.indexes;

import edu.cmu.cs.vbc.prog.queval.queval.KnnResult;
import edu.cmu.cs.vbc.prog.queval.queval.Util;
import edu.cmu.cs.vbc.prog.queval.stores.InMemoryStore;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.ArrayList;
import java.util.Collections;

public final class Dwarf extends Index {

	final DwarfDimList root;  // param -> welche dimension ist root?
	
	public Dwarf(Store s) {
		super(s);
		this.root = new DwarfDimList(0);
	}

	@Override
	protected void buildIndex() {
		int[] dataset;
		
		//insert each point
		for(int tid = firstTID();tid<size();tid++){
			dataset = this.STORE.getPoint(tid);
			insert(dataset, tid);
		}

	}

	private void insert(int[] dataset, final int TID) {
		DwarfDimList currentList = root;
		DwarfDimList thisList = null;
		DwarfElement temp = null;
		int dim;
		
		for(dim=0;dim<numDim();dim++){
			thisList=currentList;
			temp = currentList.get(dataset[dim]);
			if(temp!=NO_SUCH_DWARF){
				currentList = temp.pointerToNextDimList;
			}else{
				//currentList = currentList.add(new DwarfElement(dataset[dim]));
				temp = creteaRemainingLists(dim, currentList, dataset);
				temp.TID = TID;
				return;
			}
		}
		
		// Now it's getting nasty -> handle Double entries
		// param -> erlaubt dublikate..?
		temp = new DwarfElement(dataset[dim-1], null);
		temp.TID = TID;	
		thisList.add(temp);
	}

	private DwarfElement creteaRemainingLists(final int FIRST_DIM_WITHOUT_ENTRY, final DwarfDimList lastFoundEntry, final int[] point) {
		DwarfDimList newList, currentList = lastFoundEntry;
		DwarfElement e;
		int compareValue;
		
		for(int dim=FIRST_DIM_WITHOUT_ENTRY;dim<(numDim()-1);dim++){//except the last -> last need TID and null pointer
			compareValue = point[dim];
			newList = new DwarfDimList(dim+1);
			e = new DwarfElement(compareValue, newList);
			currentList.add(e);
			
			currentList = newList;
		}
		
		//for last dimension
		e = new DwarfElement(point[numDim()-1], null);
		currentList.add(e);
		
		return e;
	}

	@Override
	public int search(int[] query) {
		int compValue;
		DwarfDimList currentList = root;
		DwarfElement temp = null;
		
		for(int dim=0;dim<numDim();dim++){
			compValue = query[dim];
			temp=currentList.get(compValue);
			if(temp!=NO_SUCH_DWARF){
				currentList = temp.pointerToNextDimList;
			}else{
				return NOT_FOUND;
			}
		}
		return temp.TID;
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
		// TODO Auto-generated method stub

	}

	@Override
	String getInfo() {
		return "No info";
	}

	@Override
	public String getName() {
		return "Veit's Dwarf";
	}
	
	final class DwarfElement implements Comparable<DwarfElement>{
		final int DIM_VALUE;
		DwarfDimList pointerToNextDimList;
		int TID;//XXX semisch�n, speicher immer vorgehalten aber nur bei letzter dim gebraucht
	
		DwarfElement(final int value, DwarfDimList next){
			this.DIM_VALUE = value;
			this.pointerToNextDimList = next;
			this.TID = -1;
		}
		
		@Override
		public int compareTo(DwarfElement o) {
			return this.DIM_VALUE-o.DIM_VALUE;
		} 
		
		public String toString(){
			return DIM_VALUE+" "+ ((pointerToNextDimList!=null) ? pointerToNextDimList.hashCode() : "null") + ((TID!=-1) ? " TID->"+TID : "");
		}
	}
	
	static final DwarfElement NO_SUCH_DWARF = null;
	final class DwarfDimList{
		final ArrayList<DwarfElement> list;
		private final int DIMENSION;
		
		DwarfDimList(int dim){
			this.list = new ArrayList<Dwarf.DwarfElement>();
			this.DIMENSION = dim;
		}
		
		public void add(DwarfElement toAdd) {
			list.add(toAdd);
			Collections.sort(list);
		}

		DwarfElement get(final int TO_COMPARE){
			DwarfElement temp;
			
			for(int i=0;i<list.size();i++){
				temp=list.get(i);
				if(temp.DIM_VALUE==TO_COMPARE)
					return temp; 
				else if(temp.DIM_VALUE>TO_COMPARE)
					return NO_SUCH_DWARF;
			}
			return NO_SUCH_DWARF;
		}
		public String toString(){
			StringBuilder s = new StringBuilder("Dim "+DIMENSION+" "+list.size()+": ");
			DwarfElement e;
			
			for(int i=0;i<this.list.size();i++){
				e = list.get(i);
				s.append(e.toString()+"; ");
			}
			return s.toString();
		}

		public void out(StringBuilder s) {
			DwarfElement e;
			
			for(int i=0;i<list.size();i++){
				e = list.get(i);
				s.append(e.toString()+'|');
				if(e.pointerToNextDimList!=null)//reached end
					e.pointerToNextDimList.out(s);
				else
					s.append('\n');
			}
		}

		public int requiredInts() {
			int result = 1;//size
			result+=list.size();
			DwarfElement e;
			
			for(int i=0;i<list.size();i++){
				e = this.list.get(i);
				if(e.pointerToNextDimList!=null)
					result += e.pointerToNextDimList.requiredInts();
			}
			
			return result;
		}

		public int linearize(final byte[] DWARF, final  int[] POINTER, int writePointer) {
			DwarfElement e;
			final int UNUSED = -1;
			final int MY_SIZE = this.list.size();
			final int[] nextListPositions = new int[MY_SIZE];
			
			DWARF[writePointer] = (byte)this.list.size();
			POINTER[writePointer] = UNUSED;
			writePointer++;        
			
			//write own compare values and pointers
			for(int i=0;i<MY_SIZE;i++){
				e = this.list.get(i);
				DWARF[writePointer] = (byte) e.DIM_VALUE;
				/* Nasty thing -> i donno right now where the next list starts.
				 * However, if it is the last list *e.pointerToNextDimList==null* I know i have to use the TID
				 * else I store the current position of the writePointer for later
				 */
				if(e.pointerToNextDimList==null)
					POINTER[writePointer] = e.TID;
				else
					nextListPositions[i] = writePointer;
				writePointer++;
			}
			
			//call remaining lists to linearize themselves
			for(int i=0;i<MY_SIZE;i++){
				e = this.list.get(i);
				if(e.pointerToNextDimList!=null){
					//now i know where this list starts
					POINTER[nextListPositions[i]] = writePointer;
					writePointer = e.pointerToNextDimList.linearize(DWARF, POINTER, writePointer);
				}
			}
			return writePointer;
		}

		public void windowQuery(int[] lowerBoundQuery, int[] upperBoundQuery, ArrayList<Integer> resultTIDS) {
			DwarfElement e;
			int min, max;
			min = lowerBoundQuery[DIMENSION];
			max = upperBoundQuery[DIMENSION];
			
			for(int i=0;i<this.list.size();i++){
				e = this.list.get(i);
				if(isFinalDimension(e) && isIn(min, max, e.DIM_VALUE)){//e.TID != -1 && DIMEBNSION Attribute
					resultTIDS.add(e.TID);
				}else{
					if(isIn(min, max, e.DIM_VALUE)){
						e.pointerToNextDimList.windowQuery(lowerBoundQuery, upperBoundQuery, resultTIDS);
					}
					//else prune :)
				}
			}
		}

		private boolean isFinalDimension(DwarfElement e) {
			return (e.TID!=-1);
		}
	}
	
	public static void main(String[] args){
		int size = 2000;
		int dim = 25;
		
		int[][] data = {{1,2,3},{3,4,3},{1,3,3}};
		//int[][] data = {{1,2,3},{3,4,3},{1,2,3}};//double keys
		data = Util.getData(size,dim);
		data = Util.getData(size,dim,255,1234567);
		
		InMemoryStore s = new InMemoryStore(data[0].length, data.length,0,4);
		
		s.bulkInsertWithoutCheck(data);
		
		Index gunnar = new Dwarf(s);
		gunnar.build();
		//((Dwarf)gunnar).out();
		/*
		for(int i=0;i<data.length;i++){
			System.out.println("TID:"+gunnar.search(data[i]));
		}*/
		
		//System.out.println(((Dwarf)gunnar).requiredInts());
		
		Index loki = new Dwarf_Linearized_Small(s);
		loki.build();
		//((Dwarf_Linearized)loki).out();
		
		for(int i=0;i<data.length;i++){
			System.out.println("TID:"+loki.search(data[i]));
		}
		
		System.out.println("df");
	}
	
	public void out(){
		StringBuilder s = new StringBuilder();
		s.append("root\n");
		root.out(s);	
		System.out.println(s.toString());
	}
	
	int requiredInts(){
		return root.requiredInts();
	}

	public void linearize(byte[] DWARF, int[] POINTER, int writePointer) {
		root.linearize(DWARF, POINTER, writePointer);
	}

	@Override
	public ArrayList<Integer> windowQuery(int[] lowerBoundQuery, int[] upperBoundQuery) {
		ArrayList<Integer> resultTIDS = new ArrayList<Integer>(16);
		root.windowQuery(lowerBoundQuery, upperBoundQuery,resultTIDS);
		return resultTIDS;
	}
	
	boolean isIn(final int min, final int max, final int value){
		return ((value>=min)&&(value<=max)) ? true : false;
	}
}
