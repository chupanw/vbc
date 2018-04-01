package edu.cmu.cs.vbc.prog.queval.indexes;

import edu.cmu.cs.vbc.prog.queval.queval.KnnCollectionIntDistances;
import edu.cmu.cs.vbc.prog.queval.queval.KnnResult;
import edu.cmu.cs.vbc.prog.queval.queval.Util;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.ArrayList;

public final class SeqScan extends Index {
	final int MY_SIZE;

	public SeqScan(Store s) {
		super(s);
		MY_SIZE = s.size();
	}

	@Override
	public void buildIndex() {
		return; //nothing todo
	}

	@Override
	public int search(int[] query) {
		int[] currentPoint;
		
		for(int tid=0;tid<MY_SIZE;tid++){
			currentPoint = STORE.getPoint(tid);
			if(Util.equals(query,currentPoint))
				return tid;
		}
		return NOT_FOUND;
	}

	@Override
	public KnnResult searchKNN(int[] query, int k) {
		KnnCollectionIntDistances col = KnnCollectionIntDistances.create_With_First_K_Elements(k, this.STORE, query);

		for(int tid = k; tid<MY_SIZE;tid++){
			col.tryUpdate(tid);
		}

		return col.getResult();
	}
	
	@Override
	public ArrayList<Integer> windowQuery(final int[] lowerBoundQuery, final int[] upperBoundQuery){
		ArrayList<Integer> resultTIDS = new ArrayList<Integer>(16);
		int[] point;
		
		for(int tid = 0; tid<MY_SIZE;tid++){
			point = STORE.getPoint(tid);
			if (Util.isIn(point, lowerBoundQuery, upperBoundQuery))
				resultTIDS.add(tid);
		}
		
		return resultTIDS;
	}

	@Override boolean canKNNSearch() { return true;	}
	@Override void clear() { return; } //nothing todo
	@Override String getInfo() { return "SeqScan with no optimizations"; }
	@Override public String getName() { return "SeqScan"; }
}
