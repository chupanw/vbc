package edu.cmu.cs.vbc.prog.queval.queval;

public class KnnResult {
	final int[] TIDs;
	final long[] DISTANCES;
	
	public KnnResult(int[] tids, long[] dist){
		this.TIDs = tids;
		this.DISTANCES = dist;
	}
	
	public String toString(){
		StringBuilder str = new StringBuilder();
		for(int i=0;i<this.TIDs.length;i++){
			str.append("TID: "+TIDs[i]);
			str.append(" Dist: "+DISTANCES[i]);
			str.append('|');
		}
		str.append('\n');
		return str.toString();
	}
	/**
	 * TODO
	 */
	public void sort(){
		//TODO
	}

	public int getTID(int index) {
		return TIDs[index];
	}
}
