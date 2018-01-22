package edu.cmu.cs.vbc.prog.queval.indexes;

import edu.cmu.cs.vbc.prog.queval.indexes.myKDTree.HyperRectangle;
import edu.cmu.cs.vbc.prog.queval.indexes.myKDTree.MyKDNode;
import edu.cmu.cs.vbc.prog.queval.queval.KnnCollectionIntDistances;
import edu.cmu.cs.vbc.prog.queval.queval.KnnResult;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.ArrayList;
import java.util.Arrays;

public final class MyKDTree extends Index {
    public static MyKDTree tree; 
    MyKDNode root=null;
        
    public MyKDTree(Store s) {
    	super(s);
    	tree = this;
	}
    
    public final void buildIndex() {
    	int[] dataset = this.STORE.getPoint(firstTID());
    	createRoot(dataset);
    	
    	//create remaining nodes
    	
    	for(int tid = firstTID()+1;tid<size();tid++){
    		dataset = this.STORE.getPoint(tid);
    		add(dataset, tid);
    		//root.insert(dataset, 0, tid);
    	}
    }
    

    MyKDNode add(final int[] toAdd, final int TID){
    	MyKDNode parent = root;
    	MyKDNode nextNode = root;
    	
    	int dimForCompare = -1;//increment makes 0 for first loop
    	
    	do{
    		parent = nextNode;
    		dimForCompare = (dimForCompare!=toAdd.length-1) ? dimForCompare+1 : 0;
    		nextNode = (toAdd[dimForCompare]<=parent.compValue) ? parent.left : parent.right;
    	}while(nextNode!=null);
    	
    	MyKDNode add = new MyKDNode(TID, toAdd, (dimForCompare!=toAdd.length-1) ? dimForCompare+1 : 0, parent);
    	
    	if(toAdd[dimForCompare]<=parent.compValue){
			parent.left = add;
		}else{
			parent.right = add;;
		}
    
    	return parent;
    }
    
    private void createRoot(int[] dataset){
    	root = new MyKDNode(0, dataset, 0, null);
    }

    /**
     * @deprecated may result in stackoverflows.
     */
    public final int search2(int[] query) {
    	return root.exactMatch(query, 0);
    }
    
    public final int search(final int[] query) {
    	MyKDNode node = root;
    	int dimForCompare = 0;
    	int[] point;
    	
    	do{
    		//value = node.compValue;
    		//queryDim = query[dimForCompare];
    		
    		if(query[dimForCompare]==node.compValue){//match in this dimension so point possibly found
    			point = STORE.getPoint(node.TID);
    			if(Arrays.equals(query, point)){
    				return node.TID;
    			}
    		}
    		node = (query[dimForCompare]<=node.compValue) ? node.left : node.right;
    		dimForCompare = (dimForCompare!=query.length-1) ? dimForCompare+1 : 0;
    	}while(node!=null);
    	
    	return NOT_FOUND;
    }
    
    public final KnnResult searchKNN(final int[] query, int k) {
    	KnnCollectionIntDistances col = KnnCollectionIntDistances.create_With_Empty_Collection(k, this.STORE, query);
  
    	HyperRectangle rect = new HyperRectangle(query, STORE.MIN_VALUE, STORE.MAX_VALUE);
    	root.knn(col,rect,0,query);

    	return col.getResult();
    }

    public boolean canKNNSearch() {
        return true;
    }

    public String getInfo() {
        return "My kd-Tree";
    }

    public void clear() {
        root = null;
    }

	public String getName() {
		return "My kd-Tree";
	}
	
	public String toString(){
		return getName();
	}

	public interface KDTreeFeature{
		boolean
		IS_UNIQUE 	= false,
		VERBOSE 	= false;
	}

	@Override
	public ArrayList<Integer> windowQuery(int[] lowerBoundQuery, int[] upperBoundQuery) {
		ArrayList<Integer> resultTIDS = new ArrayList<Integer>(16);
		
		root.windowQuery(lowerBoundQuery, upperBoundQuery, resultTIDS, 0);
		
		return resultTIDS;
	}
}