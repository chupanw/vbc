package edu.cmu.cs.vbc.prog.queval.indexes.rtrees;

public final class Point {
	public final int[] coordinates;
	
	public Point(int[] _coordinates){
		coordinates = _coordinates;
	}
	
	public final String toString(){
		String ret = "[";
		for (int dim=0;dim<coordinates.length;dim++){
			ret += coordinates[dim]+",";
		}
		return ret+"]";
	}
}

/**
 * Version for disk access
 *
public final class Point {
	public final TID;
	
	public Point(int _tid){
		TID = _tid;
	}
	
	public final int[] getCoordinates(){
		return AIndexStructure.getPointByTID(TID);
	}
	
	public final String toString(){
		final int[] coordinates = getCoordinates();
		String ret = "[";
		for (int dim=0;dim<coordinates.length;dim++){
			ret += coordinates[dim]+",";
		}
		return ret+"]";
	}
}
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */