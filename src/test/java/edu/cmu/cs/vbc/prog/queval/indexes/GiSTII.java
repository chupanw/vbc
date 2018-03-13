package edu.cmu.cs.vbc.prog.queval.indexes;

import edu.cmu.cs.vbc.prog.queval.queval.Configuration;
import edu.cmu.cs.vbc.prog.queval.queval.KnnResult;
import edu.cmu.cs.vbc.prog.queval.queval.Util;
import edu.cmu.cs.vbc.prog.queval.stores.InMemoryStore;
import edu.cmu.cs.vbc.prog.queval.stores.Store;

import java.util.*;

/**
 * 
 * @author amartin
 * 
 */
public class GiSTII extends Index {
	
	/**
	 * Tree linearized as array. Avoids stupid trick that points are MBRs themselves.
	 * For dynamic variants implement growing strategy.
	 * */
	public int[] TREE;
	int insertPointer = 0;
	// TODO @Jens Numerischer Parameter: 5 11 17
	public final int SPLITSIZE = Configuration.SS11 ? 11 : Configuration.SS17 ? 17 : 5/*unused -  does not work*/;
	public final int MIN_POINTS_MBR = (SPLITSIZE - 1) / 2;
	final int ROOT_INDEX = 0;
	public final int NUM_DIM;

	SplitAlgorithm SPLIT_ALGO;
	InsertAlgo insertAlgo;

	static final int LEVEL_OFFSET = 0;
	static final int PARENT_OFFSET = LEVEL_OFFSET + 1;
	static final int SIZE_OFFSET = PARENT_OFFSET + 1;

	final int LOWER_BORDER_OFFSET = SIZE_OFFSET + 1;
	final int UPPER_BORDER_OFFSET;
	/** in leafs TIDs */
	final int CHLID_POINTER;
	final int SIZE_IN_INT;

	static final int IS_LEAF = 1;
	// -1 reserved for Index.NOT_FOUND
	static final int HAS_NO_PARENT = -2;
	static final int MY_NULL = -3;

	public GiSTII(Store s) {
		super(s);
		NUM_DIM = s.NUM_DIM;

		UPPER_BORDER_OFFSET = LOWER_BORDER_OFFSET + NUM_DIM;
		CHLID_POINTER = UPPER_BORDER_OFFSET + NUM_DIM;
		SIZE_IN_INT = CHLID_POINTER + SPLITSIZE;
		TREE = new int[s.size() * SIZE_IN_INT];
	}

//	public static final boolean USE_RSTAR_SPLIT = true;
//	public static final boolean USE_RSTAR_INSERT = true;

	@Override
	protected void buildIndex() {
		//Can't be in constructor as parameter may be modified afterwards...
		//XXX @Jens Params
		if(Configuration.RStarSplit){
			SPLIT_ALGO = new RStarSplitAlgorithm(this);
		} else if (Configuration.StupidSplitAlgo) {
			SPLIT_ALGO = new StupidSplit(this);
		} else {
			throw new RuntimeException("Split algo not selected"); 
		}
		if(Configuration.RStartInsert){
			insertAlgo = new RStarInsert();
		} else if (Configuration.GuttmanInsert) {
			insertAlgo = new GuttmanInsert();
		} else {
			throw new RuntimeException("Insert algo not selected"); 
		}
		
		//create root
		insertPointer = 0;
		newMBRPointer();
		setLevel(ROOT_INDEX, 1);//by definition, makes the root a leaf.

		int[] dataset;
		/** Is expected to be a leaf. */
		int insertHereLeaf;
		
		for(int tid=0;tid<STORE.size();tid++){
			dataset = STORE.getPoint(tid);
			insertHereLeaf = insertAlgo.getInsertNode(ROOT_INDEX, dataset);
			//expand(insertHereLeaf, dataset);
			add(insertHereLeaf,dataset,tid);
		}
		trimToSize();
	}

	void trimToSize() {
		int[] trimmedTree = new int[insertPointer];
		System.arraycopy(TREE, ROOT_INDEX, trimmedTree, ROOT_INDEX, (insertPointer));
		this.TREE = trimmedTree;
	}

	@Override
	boolean canKNNSearch() {
		return false;// TODO
	}

	@Override
	void clear() {

	}

	@Override
	String getInfo() {
		return getName() + " with " + SPLITSIZE;
	}

	@Override
	public String getName() {
		return "GiST II";
	}

	/**
	 * Contains access methods for better readability.
	 * 
	 * @author amartin
	 * 
	 */
	// public final class MBR{

	final int getLevel(final int POINTER) {
		return TREE[POINTER + LEVEL_OFFSET];
	}

	void incLevel(final int POINTER) {
		TREE[POINTER + LEVEL_OFFSET] += 1;
	}

	void setLevel(final int POINTER, final int LEVEL) {
		TREE[POINTER + LEVEL_OFFSET] = LEVEL;
	}

	public final int getSize(final int POINTER) {
		return TREE[POINTER + SIZE_OFFSET];
	}

	void incSize(final int POINTER) {
		TREE[POINTER + SIZE_OFFSET] += 1;
	}

	void setSize(final int POINTER, final int SIZE) {
		TREE[POINTER + SIZE_OFFSET] = SIZE;
	}

	int getParentIndex(final int POINTER) {
		return TREE[POINTER + PARENT_OFFSET];
	}

	void setParent(final int POINTER, final int PARENT_INDEX) {
		TREE[POINTER + PARENT_OFFSET] = PARENT_INDEX;
	}

	public int getLowerBorderAtDim(final int POINTER, final int DIM) {
		return TREE[POINTER + LOWER_BORDER_OFFSET + DIM];
	}

	public int getUpperBorderAtDim(final int POINTER, final int DIM) {
		return TREE[POINTER + UPPER_BORDER_OFFSET + DIM];
	}

	void setLowerBorderAtDim(final int POINTER, final int DIM, final int VALUE) {
		TREE[POINTER + LOWER_BORDER_OFFSET + DIM] = VALUE;
	}

	void setUpperBorderAtDim(final int POINTER, final int DIM, final int VALUE) {
		TREE[POINTER + UPPER_BORDER_OFFSET + DIM] = VALUE;
	}

	public int getChildIndex(final int POINTER, final int INDEX) {
		return TREE[POINTER + CHLID_POINTER + INDEX];
	}

	public int getTID(final int POINTER, final int INDEX) {
		return TREE[POINTER + CHLID_POINTER + INDEX];
	}

	void setChild(final int POINTER, final int INDEX, final int CHILD_POINTER) {
		TREE[POINTER + CHLID_POINTER + INDEX] = CHILD_POINTER;
	}

	/** Clone by purpose - calls setChild(int,int,int) */
	void setTID(final int POINTER, final int INDEX, final int TID) {
		setChild(POINTER, INDEX, TID);
	}

	public boolean isLeaf(final int POINTER) {
		return getLevel(POINTER) == IS_LEAF;
	}

	boolean isRoot(final int POINTER) {
		return (getParentIndex(POINTER) == HAS_NO_PARENT);
		// return getLevel(MBR) == 1;
	}

	// helper Functions for insert etc.
	public long deltaVolume(final int POINTER, final int[] TO_INSERT) {
		long volumeOld = 1, volumeNew = 1;
		for (int dim = 0; dim < NUM_DIM; dim++) {
			int min = getLowerBorderAtDim(POINTER, dim);
			int max = getUpperBorderAtDim(POINTER, dim);;
			int pointDim = TO_INSERT[dim];

			volumeOld *= max - min;
			max = (max > pointDim) ? max : pointDim;
			min = (min < pointDim) ? min : pointDim;
			volumeNew *= max - min;
		}
		return volumeNew - volumeOld;
	}

	public void add(final int LEAF_POINTER, final int[] TO_INSERT, final int TID) {
		expand(LEAF_POINTER, TO_INSERT);
		int size = getSize(LEAF_POINTER);
		setTID(LEAF_POINTER, size, TID);
		incSize(LEAF_POINTER);

		if (getSize(LEAF_POINTER) == SPLITSIZE)
			split(LEAF_POINTER);
	}

	private final void expand(final int TO_EXPAND, int[] TO_INSERT) {
		boolean expanded = false;

		// default
		if (getSize(TO_EXPAND) != 0) {
			for (int dim = 0; dim < NUM_DIM; dim++) {
				if (getLowerBorderAtDim(TO_EXPAND, dim) > TO_INSERT[dim]) {
					setLowerBorderAtDim(TO_EXPAND, dim, TO_INSERT[dim]);
					expanded = true;
				}
				if (getUpperBorderAtDim(TO_EXPAND, dim) < TO_INSERT[dim]) {
					setUpperBorderAtDim(TO_EXPAND, dim, TO_INSERT[dim]);
					expanded = true;
				}
			}
		}
		if ((expanded) && (!isRoot(TO_EXPAND)))// stop reaching the root
			expand(getParentIndex(TO_EXPAND), TO_INSERT);
	}

	void split(final int MBR_TO_SPLIT) {

		if (isRoot(MBR_TO_SPLIT)) {
			// is the evil case as level grows
			// (1) Init stuff
			final int POINTER_FIRST = newMBRPointer();
			final int POINTER_SECOND = newMBRPointer();

			final int ROOT_LEVEL = getLevel(MBR_TO_SPLIT);
			setLevel(POINTER_FIRST, ROOT_LEVEL);
			setLevel(POINTER_SECOND, ROOT_LEVEL);

			// (2) Distribute children of MBR_TO_SPLIT
			ArrayList<Integer> distributeMBRPointerLeft = new ArrayList<Integer>();
			ArrayList<Integer> distributeMBRPointerRight = new ArrayList<Integer>();
			// That's the trick of a GiST
			SPLIT_ALGO.distributeNodes(MBR_TO_SPLIT, distributeMBRPointerLeft, distributeMBRPointerRight);

			for (int child = 0; child < distributeMBRPointerLeft.size(); child++) {
				setChild(POINTER_FIRST, child, distributeMBRPointerLeft.get(child));
				incSize(POINTER_FIRST);
			}
			for (int child = 0; child < distributeMBRPointerRight.size(); child++) {
				setChild(POINTER_SECOND, child, distributeMBRPointerRight.get(child));
				incSize(POINTER_SECOND);
			}

			// (3) Handle linking
			setParent(POINTER_FIRST, ROOT_INDEX);
			setParent(POINTER_SECOND, ROOT_INDEX);

			setChild(MBR_TO_SPLIT, 0, POINTER_FIRST);
			setChild(MBR_TO_SPLIT, 1, POINTER_SECOND);

			// Propagate new parents to former root child MBRs
			if (!isLeaf(POINTER_FIRST)) {
				for (int i = 0; i < getSize(POINTER_FIRST); i++) {
					int child = getChildIndex(POINTER_FIRST, i);
					setParent(child, POINTER_FIRST);
				}
				for (int i = 0; i < getSize(POINTER_SECOND); i++) {
					int child = getChildIndex(POINTER_SECOND, i);
					setParent(child, POINTER_SECOND);
				}
			}

			// (3) clear root
			setSize(MBR_TO_SPLIT, 2);
			for (int child = 2; child < SPLITSIZE; child++) {
				setChild(MBR_TO_SPLIT, child, MY_NULL);
			}
			setLevel(MBR_TO_SPLIT, ROOT_LEVEL + 1);
			recomputeBorders(POINTER_FIRST);
			recomputeBorders(POINTER_SECOND);

			return;// No further splitting
		} else {
			// requires no re-je-pointere of children
			int newRight = newMBRPointer();
			setParent(newRight, getParentIndex(MBR_TO_SPLIT));
			setLevel(newRight, getLevel(MBR_TO_SPLIT));

			// (2) Copy TIDs or child MBRs
			ArrayList<Integer> distributeMBRPointerLeft = new ArrayList<Integer>();
			ArrayList<Integer> distributeMBRPointerRight = new ArrayList<Integer>();
			// That's the trick of a GiST
			SPLIT_ALGO.distributeNodes(MBR_TO_SPLIT, distributeMBRPointerLeft, distributeMBRPointerRight);

			// clear(MBR_TO_SPLIT);
			for (int i = 0; i < SPLITSIZE; i++) {
				setChild(MBR_TO_SPLIT, i, MY_NULL);
			}
			setSize(MBR_TO_SPLIT, 0);

			for (int i = 0; i < distributeMBRPointerLeft.size(); i++) {
				setChild(MBR_TO_SPLIT, i, distributeMBRPointerLeft.get(i));
				incSize(MBR_TO_SPLIT);
			}

			int childIndex;
			for (int i = 0; i < distributeMBRPointerRight.size(); i++) {
				childIndex = distributeMBRPointerRight.get(i);
				setChild(newRight, i, childIndex);
				incSize(newRight);
				if (!isLeaf(MBR_TO_SPLIT))
					setParent(childIndex, newRight);
			}

			int parent = getParentIndex(MBR_TO_SPLIT);
			int parentSize = getSize(parent);
			setChild(parent, parentSize, newRight);
			setSize(parent, parentSize + 1);

			recomputeBorders(MBR_TO_SPLIT);
			recomputeBorders(newRight);

			if (getSize(parent) == SPLITSIZE) {
				split(parent);
			}
		}
	}

	private void recomputeBorders(final int MBR) {
		if (isLeaf(MBR)) {
			// Need to fetch points from Store using their TIDs.
			int tid = getTID(MBR, 0);
			int[] point = STORE.getPoint(tid);

			// Create first border with first child only.
			for (int dim = 0; dim < NUM_DIM; dim++) {
				setLowerBorderAtDim(MBR, dim, point[dim]);
				setUpperBorderAtDim(MBR, dim, point[dim]);
			}

			// Test whether we need to adjust borders.
			for (int child = 1; child < getSize(MBR); child++) {
				point = STORE.getPoint(getTID(MBR, child));
				for (int dim = 0; dim < NUM_DIM; dim++) {
					if (point[dim] < getLowerBorderAtDim(MBR, dim)) {
						setLowerBorderAtDim(MBR, dim, point[dim]);
					}
					if (getUpperBorderAtDim(MBR, dim) < point[dim]) {
						setUpperBorderAtDim(MBR, dim, point[dim]);
					}
				}
			}
		} else {
			int childMBR = getChildIndex(MBR, 0);
			for (int dim = 0; dim < NUM_DIM; dim++) {
				setLowerBorderAtDim(MBR, dim, getLowerBorderAtDim(childMBR, dim));
				setUpperBorderAtDim(MBR, dim, getUpperBorderAtDim(childMBR, dim));
			}

			// Adjust borders if necessary.
			for (int child = 1; child < getSize(MBR); child++) {
				childMBR = getChildIndex(MBR, child);

				for (int dim = 0; dim < NUM_DIM; dim++) {
					if (getLowerBorderAtDim(childMBR, dim) < getLowerBorderAtDim(MBR, dim)) {
						setLowerBorderAtDim(MBR, dim, getLowerBorderAtDim(childMBR, dim));
					}
					if (getUpperBorderAtDim(MBR, dim) < getUpperBorderAtDim(childMBR, dim)) {
						setUpperBorderAtDim(MBR, dim, getUpperBorderAtDim(childMBR, dim));
					}
				}
			}
		}
	}

	/**
	 * Pseudo Constructor
	 * 
	 * @return new MBR of correct size as int[].
	 *         ensures Borders are set to extreme values & children are intialized with MY_NULL
	 */
	int newMBRPointer() {
		int pointer = insertPointer;
		insertPointer += SIZE_IN_INT;
		initWithDummyData(pointer);
		return pointer;
	}

	private void initWithDummyData(final int POINTER) {
		setSize(POINTER, 0);
		setLevel(POINTER, 0);
		setParent(POINTER, HAS_NO_PARENT);

		// Dummy values for borders
		for (int dim = 0; dim < NUM_DIM; dim++) {
			setLowerBorderAtDim(POINTER, dim, Integer.MAX_VALUE);
			setUpperBorderAtDim(POINTER, dim, Integer.MIN_VALUE);
		}
		for (int child = 0; child < SPLITSIZE; child++) {
			setChild(POINTER, child, MY_NULL);
		}
	}

	/************************************
	 * Query types *
	 ************************************/

	int exactMatch(final int MBR, final int[] QUERY) {
		if (isLeaf(MBR)) {
			int[] point;
			int tid;

			for (int child = 0; child < getSize(MBR); child++) {
				tid = getTID(MBR, child);
				point = STORE.getPoint(tid);
				if (Util.equals(point, QUERY)) {
					return tid;
				}
			}
			return Index.NOT_FOUND;
		} else {
			// if inner node or root -> determine subtrees
			for (int mbr = 0; mbr < getSize(MBR); mbr++) {
				int currMBR = getChildIndex(MBR, mbr);
				// does this sub-tree possibly contains the point
				if (FULL_OVERLAP == intersects(currMBR, QUERY)) {// inlining intersects slows down performance
					int tid = exactMatch(currMBR, QUERY);
					if (tid != Index.NOT_FOUND)// return first result where point found
						return tid;
				}
			}
		}
		// Could not find the point
		return Index.NOT_FOUND;
	}

	private final int intersects(final int MBR, final int[] QUERY) {
		// try to prove that in one dimension there is NO overlap
		for (int dim = 0; dim < NUM_DIM; dim++) {
			if ((getLowerBorderAtDim(MBR, dim) > QUERY[dim])// left of minBorder
					|| (QUERY[dim] > getUpperBorderAtDim(MBR, dim)))// right of maxBorder
				return NO_OVERLAPP;// could prove there is no overlap
		}
		return FULL_OVERLAP;
	}

	/** For intersects(final int[] MBR, final int[] QUERY) */
	public static final int NO_OVERLAPP = 1, PARTIAL_OVERLAP = 2, FULL_OVERLAP = 3;

	// }

	@Override
	public int search(final int[] QUERY) {
		return exactMatch(ROOT_INDEX, QUERY);
	}

	@Override
	public ArrayList<Integer> windowQuery(int[] lowerBoundQuery, int[] upperBoundQuery) {
		ArrayList<Integer> resultTIDs = new ArrayList<Integer>(16);
		windowQuery(ROOT_INDEX, lowerBoundQuery, upperBoundQuery, resultTIDs);
		return resultTIDs;
	}

	private final void windowQuery(final int MBR, final int[] lowerBoundQuery, final int[] upperBoundQuery, final ArrayList<Integer> resultTIDs) {

		if (isLeaf(MBR)) {//
			int[] point;
			int tid;

			for (int child = 0; child < getSize(MBR); child++) {
				tid = getTID(MBR, child);
				point = STORE.getPoint(tid);
				// inlined Util.isIn() -> a little bit faster
				boolean isIn = true;
				for (int dim = 0; dim < point.length; dim++) {
					if (lowerBoundQuery[dim] > point[dim] || point[dim] > upperBoundQuery[dim]) {
						isIn = false;
						break;
					}
				}
				if (isIn) {
					resultTIDs.add(tid);
				}
			}
		} else {
			int temp;
			int intersection;
			for (int child = 0; child < getSize(MBR); child++) {
				temp = getChildIndex(MBR, child);
				intersection = intersects(temp, lowerBoundQuery, upperBoundQuery);

				if (intersection == FULL_OVERLAP) {
					addAll(temp, resultTIDs);
				} else if (intersection == PARTIAL_OVERLAP) {
					windowQuery(temp, lowerBoundQuery, upperBoundQuery, resultTIDs);
				}
				// else prune
			}
		}
	}

	private final int intersects(final int MBR, final int[] lBorder, final int[] uBorder) {
		/*
		 * 6 Cases MBR(x1,x2), Q(o1,2)
		 * Cases | MIN | MAX |
		 * (1) x1x2o1o2 | o1 | x2 | NO_OVERLAPP
		 * (2) x1o1x2o2 | o1 | x2 | PARTIAL_OVERLAP
		 * (3) x1o1o2x1 | o1 | o2 | PARTIAL_OVERLAP
		 * (4) o1x1o2x2 | x1 | o2 | PARTIAL_OVERLAP
		 * (5) o1x1x2o2 | x1 | x2 | FULL_OVERLAP
		 * (6) o1o2x1x2 | x1 | o2 | NO_OVERLAPP
		 */
		int intersection = FULL_OVERLAP;
		int lower, upper;

		for (int dim = 0; dim < NUM_DIM; dim++) {
			lower = getLowerBorderAtDim(MBR, dim);
			upper = getUpperBorderAtDim(MBR, dim);

			if ((upper < lBorder[dim]) || // on the left case 1
					(lower > uBorder[dim])) // on the right case 6
			{
				return NO_OVERLAPP;
			}// easier to test case 5 only, if not --> it is partial overlap
			else if (!((lBorder[dim] <= lower) && (upper <= uBorder[dim]))) {
				intersection = PARTIAL_OVERLAP;
			}// else remains FULL_OVERLAP
		}
		return intersection;
	}

	void addAll(final int MBR, ArrayList<Integer> resultTIDs) {
		if (isLeaf(MBR)) {
			for (int child = 0; child < getSize(MBR); child++) {
				resultTIDs.add(getChildIndex(MBR, child));
			}
		} else {
			int temp;
			for (int child = 0; child < getSize(MBR); child++) {
				temp = getChildIndex(MBR, child);
				addAll(temp, resultTIDs);
			}
		}
	}

	@Override
	public KnnResult searchKNN(int[] query, int k) {
		// TODO Auto-generated method stub
		return null;
	}

	void checkTree() {
		// int numNodes = (insertPointer/SIZE_IN_INT);
		for (int node = 0; node < insertPointer; node += insertPointer) {
			checkMBBR(node);
		}
	}

	// TEST stuff
	public static void main(String[] args) {
		int dim = 2;
		int size = 500;
		int dimMaxValue = 255;

		final int[][] DATA = Util.getData(size, dim, dimMaxValue, 1234567);
		InMemoryStore store = new InMemoryStore(DATA[0].length, DATA.length, 0, dimMaxValue);
		store.bulkInsertWithoutCheck(DATA);

		GiSTII tree = new GiSTII(store);
		tree.build();

		for (int tid = 0; tid < size; tid++) {
			System.out.println(tid + ":" + tree.search(store.getPoint(tid)));
		}

		tree.checkTree();
		// tree.out();

		System.out.println("Ready.");
	}

	/************************************************
	 * Error detection stuff *
	 ************************************************/

	static final int IS_CORRECT = 0;
	static final int NO_TEST_ROOT_IS_LEAF = 1;

	int checkMBBR(final int MBR_POINTER) {
		if (isLeaf(ROOT_INDEX)) {
			return NO_TEST_ROOT_IS_LEAF;
		}
		// System.err.println("Start check: "+MBR_INDEX);
		int size = getSize(MBR_POINTER);
		int myChild;
		int errorCode = IS_CORRECT;

		for (int child = 0; child < size; child++) {
			myChild = getChildIndex(MBR_POINTER, child);
			if (myChild < 0) {
				System.err.println(myChild + " should contain valid pointer or tid.");
			}
		}
		for (int child = size; child < SPLITSIZE; child++) {
			myChild = getChildIndex(MBR_POINTER, child);
			if (myChild != GiSTII.MY_NULL) {
				System.err.println(myChild + " Pointer exceeds size and should contain MY_NULL (" + GiSTII.MY_NULL + ").");
			}
		}

		if (isLeaf(MBR_POINTER)) {
			// check borders
			int tid;
			int[] point;
			for (int i = 0; i < getSize(MBR_POINTER); i++) {
				tid = getTID(MBR_POINTER, i);
				point = STORE.getPoint(tid);
				for (int dim = 0; dim < NUM_DIM; dim++) {
					if (point[dim] < getLowerBorderAtDim(MBR_POINTER, dim)) {
						System.err.println("Lower border is to small.");
					}
					if (getUpperBorderAtDim(MBR_POINTER, dim) < point[dim]) {
						System.err.println("Upper border is to small.");
					}
				}
			}
			// check if parent is correctly linked
			int parent = getParentIndex(MBR_POINTER);
			boolean isLinked = false;
			int pointer;
			for (int i = 0; i < getSize(parent); i++) {
				pointer = getChildIndex(parent, i);
				if (MBR_POINTER == pointer) {
					isLinked = true;
					break;
				}
			}
			if (!isLinked) {
				System.err.println("Not registered in parent: " + MBR_POINTER);
			}
		} else {
			// TODO
		}
		return errorCode;
	}

	/************************************************
	 * Insert algorithms *
	 ************************************************/

	public abstract class InsertAlgo {
		abstract int getInsertNode(final int CURRENT, final int[] TO_INSERT);
	}

	public final class GuttmanInsert extends InsertAlgo {
		int getInsertNode(final int CURRENT, final int[] TO_INSERT) {
			if (isLeaf(CURRENT)) {
				return CURRENT;
			}
			// MIN(children)->handle forward to the min sub-tree
			else {
				int size = getSize(CURRENT);
				int tempMBR;

				long[] punishmentsOfSubtrees = new long[size];
				for (int mbr = 0; mbr < size; mbr++) {
					tempMBR = getChildIndex(CURRENT, mbr);
					punishmentsOfSubtrees[mbr] = deltaVolume(tempMBR, TO_INSERT);
				}
				// get the best MBR (subtree) to hand forward
				int minMBRIndex = 0;
				for (int mbr = 1; mbr < size; mbr++) {
					if (punishmentsOfSubtrees[mbr] < punishmentsOfSubtrees[minMBRIndex])
						minMBRIndex = mbr;
				}
				tempMBR = getChildIndex(CURRENT, minMBRIndex);

				// hand forward
				return getInsertNode(tempMBR, TO_INSERT);
			}
		}
	}

	public final class RStarInsert extends InsertAlgo {
		int getInsertNode(final int CURRENT, final int[] TO_INSERT) {
			// (1) if leaf, compute punishment for this
			if (isLeaf(CURRENT)) {
				return CURRENT;
			}
			// CS2: This is the main difference to R-Tree insert
			else if (isLeaf(getChildIndex(CURRENT, 0))) {
				LinkedList<MyContainer> punishements = new LinkedList<MyContainer>();
				for (int childIndex = 0; childIndex < getSize(CURRENT); childIndex++) {
					long overlap = overlap(CURRENT, childIndex, TO_INSERT);
					long volume = deltaVolume(getChildIndex(CURRENT, childIndex), TO_INSERT);
					punishements.add(new MyContainer(overlap, volume, getChildIndex(CURRENT, childIndex)));
				}
				// sort so that MBR with least overlap is first, resolve ties by min deltaVolume
				Collections.sort(punishements, new Comparator<MyContainer>() {
					@Override
					public int compare(MyContainer o1, MyContainer o2) {
						return o1.compareTo(o2);
					}
				});
				return getInsertNode(punishements.getFirst().leafToInsertPointer, TO_INSERT);
			} else {
				// for inner nodes do the same as for the R-Tree
				int size = getSize(CURRENT);
				long[] punishmentsOfSubtrees = new long[size];
				int tempMBR;

				for (int mbr = 0; mbr < size; mbr++) {
					tempMBR = getChildIndex(CURRENT, mbr);
					punishmentsOfSubtrees[mbr] = deltaVolume(tempMBR, TO_INSERT);
				}
				// get the best MBR (subtree) to hand forward
				int minMBRIndex = 0;
				for (int mbr = 1; mbr < size; mbr++) {
					if (punishmentsOfSubtrees[mbr] < punishmentsOfSubtrees[minMBRIndex])
						minMBRIndex = mbr;
				}
				// hand forward
				tempMBR = getChildIndex(CURRENT, minMBRIndex);
				return getInsertNode(tempMBR, TO_INSERT);
			}
		}

		public long overlap(final int current, int childIndex, int[] toInsert) {
			long orgOverlapVolume = 0;
			long newOverlapVolume = 0;

			// determine new border if inserting into this leaf
			final int dimensions = NUM_DIM;
			int[] newMinBorder = new int[dimensions];
			int[] newMaxBorder = new int[dimensions];

			int[] lBorder = new int[NUM_DIM], uBorder = new int[NUM_DIM];
			int toExtend = getChildIndex(current, childIndex);
			for (int dim = 0; dim < NUM_DIM; dim++) {
				lBorder[dim] = getLowerBorderAtDim(toExtend, dim);
				uBorder[dim] = getUpperBorderAtDim(toExtend, dim);
			}

			for (int dim = 0; dim < dimensions; dim++) {
				newMinBorder[dim] = (lBorder[dim] < toInsert[dim]) ? lBorder[dim] : toInsert[dim];
				newMaxBorder[dim] = (uBorder[dim] > toInsert[dim]) ? uBorder[dim] : toInsert[dim];
			}

			for (int mbr = 0; mbr < getSize(current); mbr++) {
				if (childIndex == mbr)
					continue;
				// orgOverlapVolume
				orgOverlapVolume += overlap(getChildIndex(current, mbr), lBorder, uBorder);
				// newOverlapVolume
				newOverlapVolume += overlap(getChildIndex(current, mbr), newMinBorder, newMaxBorder);
			}

			return newOverlapVolume - orgOverlapVolume;
		}

		public long overlap(final int mbr, int[] minBorder, int[] maxBorder) {
			// try to prove intersection in one dimension them return 0L else compute intersection
			long overlap = 1;
			for (int dim = 0; dim < NUM_DIM; dim++) {
				/*
				 * 6 Cases R(x1,x2), R(o1,2)
				 * Cases | MIN | MAX |
				 * x1x2o1o2 | o1 | x2 | MIN > MAX -> no intersect
				 * x1o1x2o2 | o1 | x2 |
				 * x1o1o2x1 | o1 | o2 |
				 * o1x1o2x2 | x1 | o2 |
				 * o1x1x2o2 | x1 | x2 |
				 * o1o2x1x2 | x1 | o2 | MIN > MAX -> no intersect
				 */
				int intersectMin = Math.max(getLowerBorderAtDim(mbr, dim), minBorder[dim]);
				int intersectMax = Math.min(getLowerBorderAtDim(mbr, dim), maxBorder[dim]);
				if (intersectMin > intersectMax) {// no intersect see above
					return 0;// I like this line, no overlap at all
				} else
					overlap *= (intersectMax - intersectMin);
			}

			return overlap;
		}

		private final class MyContainer implements Comparable<MyContainer> {
			public final long overlap;
			public final long deltaVolume;
			public final int leafToInsertPointer;

			public MyContainer(long _overlap, long _deltaVolume, int _leaf) {
				this.overlap = _overlap;
				this.deltaVolume = _deltaVolume;
				this.leafToInsertPointer = _leaf;
			}

			public final int compareTo(MyContainer compare) {
				return (int) ((this.overlap != compare.overlap) ? this.overlap - compare.overlap : this.deltaVolume - compare.deltaVolume);
			}
		}
	}

	public abstract class SplitAlgorithm {
		final GiSTII TREE;

		public SplitAlgorithm(GiSTII TREE) {
			this.TREE = TREE;
		}

		public abstract void distributeNodes(int MBR_TO_SPLIT, ArrayList<Integer> distributeMBRPointerLeft, ArrayList<Integer> distributeMBRPointerRight);

		public abstract String toString();
	}

	public final class StupidSplit extends SplitAlgorithm {
		public StupidSplit(GiSTII TREE) {
			super(TREE);
		}

		@Override
		public void distributeNodes(final int MBR_TO_SPLIT, final ArrayList<Integer> distributeMBRPointerLeft, final ArrayList<Integer> distributeMBRPointerRight) {
			int numFirstMBR = TREE.getSize(MBR_TO_SPLIT) / 2;

			for (int child = 0; child < numFirstMBR; child++) {
				distributeMBRPointerLeft.add(TREE.getChildIndex(MBR_TO_SPLIT, child));
			}
			for (int child = numFirstMBR; child < TREE.SPLITSIZE; child++) {
				distributeMBRPointerRight.add(TREE.getChildIndex(MBR_TO_SPLIT, child));
			}
		}

		@Override
		public String toString() {
			return "Stupid split algo for testing only - or evaluation of effects of split algos.";
		}
	}

	public final class RStarSplitAlgorithm extends SplitAlgorithm {
		private final Comparator<SplitContainer> compLower, compUpper;
		ArrayList<Integer> distributeMBRPointerLeft;
		ArrayList<Integer> distributeMBRPointerRight;
		int MBR_TO_SPLIT;

		public RStarSplitAlgorithm(GiSTII TREE) {
			super(TREE);
			compLower = new Comparator<SplitContainer>() {
				@Override
				public int compare(SplitContainer o1, SplitContainer o2) {
					return o1.lower - o2.lower;
				}
			};
			compUpper = new Comparator<SplitContainer>() {
				@Override
				public int compare(SplitContainer o1, SplitContainer o2) {
					return o1.upper - o2.upper;
				}
			};
		}

		@Override
		public void distributeNodes(final int MBR_TO_SPLIT, final ArrayList<Integer> distributeMBRPointerLeft, final ArrayList<Integer> distributeMBRPointerRight) {
			this.distributeMBRPointerLeft = distributeMBRPointerLeft;
			this.distributeMBRPointerRight = distributeMBRPointerRight;
			this.MBR_TO_SPLIT = MBR_TO_SPLIT;

			// S1: Get the dimension with the smallest S
			SplitHandler[] axises = new SplitHandler[TREE.NUM_DIM];
			long Smin = Long.MAX_VALUE;
			int indexSmin = -1;
			for (int axis = 0; axis < TREE.NUM_DIM; axis++) {// axis == dim -> synonym
				axises[axis] = new SplitHandler(axis);
				if (axises[axis].S < Smin) {
					Smin = axises[axis].S;
					indexSmin = axis;
				}
			}

			// S2:
			SplitHandler.Distribution bestDistribution = axises[indexSmin].bestDistribution();
			// fills the ArrayLists
			bestDistribution.getFirst();
			bestDistribution.getSecond();
		}

		@Override
		public String toString() {
			return "R*-tree split";
		}

		/**
		 * Handles one axis.
		 * 
		 * @author amartin
		 * 
		 */
		public final class SplitHandler {
			SplitContainer[] sortedAxis;
			final int dimension;
			final int NUM_DISTRIBUTIONS;
			Distribution[] distributions;
			/** SUM of margins of all distributions */
			long S;

			long[] margins, areas = null, overlaps = null;

			public SplitHandler(int _dim) {
				int size = TREE.getSize(MBR_TO_SPLIT);
				this.dimension = _dim;
				sortedAxis = new SplitContainer[size];
				NUM_DISTRIBUTIONS = size - (2 * TREE.MIN_POINTS_MBR) + 1;// p.5 R*-Tree

				for (int child = 0; child < size; child++) {
					if (!TREE.isLeaf(MBR_TO_SPLIT)) {
						int tempMBRPointer = TREE.getChildIndex(MBR_TO_SPLIT, child);
						sortedAxis[child] = new SplitContainer(TREE.getLowerBorderAtDim(tempMBRPointer, dimension), TREE.getUpperBorderAtDim(tempMBRPointer, dimension), null,
								tempMBRPointer);
					} else {
						int tid = TREE.getTID(MBR_TO_SPLIT, child);
						int[] point = TREE.STORE.getPoint(tid);
						sortedAxis[child] = new SplitContainer(point[dimension], point[dimension], point, tid);
					}
				}
				Arrays.sort(sortedAxis, compLower);
				Arrays.sort(sortedAxis, compUpper);
				getMarginValues();
			}

			/**
			 * Calculate S, the SUM of distribution's margins
			 */
			private final void getMarginValues() {
				// remember that at least |MIN_POINTS_MBR| shall remain in each of the splitted MBR

				this.distributions = new Distribution[NUM_DISTRIBUTIONS];
				for (int splitHere = TREE.MIN_POINTS_MBR; splitHere < sortedAxis.length - TREE.MIN_POINTS_MBR + 1; splitHere++) {
					Distribution temp = new Distribution(splitHere);
					distributions[splitHere - TREE.MIN_POINTS_MBR] = temp;
					S += temp.Margin();
				}
			}

			public final Distribution bestDistribution() {
				for (int dist = 0; dist < distributions.length; dist++) {
					distributions[dist].computeArea();
					distributions[dist].computeOverlap();
					Arrays.sort(distributions, new Comparator<Distribution>() {
						@Override
						public int compare(Distribution o1, Distribution o2) {
							return (int) ((o1.overlap != o2.overlap) ? o1.overlap - o2.overlap : o1.area - o2.area);
						}
					});
				}
				return distributions[0];
			}

			public final class Distribution {
				private final int SPLIT_INDEX;
				private final long MARGIN_UPPER, MARGIN_LOWER;// the only one that definetly is computed
				public long area, overlap;
				private final int[] bbFirstLower = new int[TREE.NUM_DIM], bbFirstUpper = new int[TREE.NUM_DIM], bbSecondLower = new int[TREE.NUM_DIM],
						bbSecondUpper = new int[TREE.NUM_DIM];

				private Distribution(int _splitIndex) {
					this.SPLIT_INDEX = _splitIndex;

					// calculate bounding boxes & margins
					long lowerMargin = 0, upperMargin = 0;
					for (int dim = 0; dim < TREE.NUM_DIM; dim++) {
						{// nur f�r mich - lesbarkeit
							// First bounding box
							int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
							int[] point;
							int MBRmin, MBRmax;

							for (int mbr = 0; mbr < SPLIT_INDEX; mbr++) {
								if (!TREE.isLeaf(MBR_TO_SPLIT)) {
									MBRmin = TREE.getLowerBorderAtDim(sortedAxis[mbr].POINTER, dim);
									MBRmax = TREE.getUpperBorderAtDim(sortedAxis[mbr].POINTER, dim);
								} else {
									point = sortedAxis[mbr].point;
									MBRmin = point[dim];
									MBRmax = point[dim];
								}
								min = (min < MBRmin) ? min : MBRmin;
								max = (max > MBRmax) ? max : MBRmax;
							}
							bbFirstLower[dim] = min;
							bbFirstUpper[dim] = max;
							lowerMargin += max - min;
						}
						{// nur f�r mich - lesbarkeit
							// Second bounding box
							int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
							int[] point;
							int MBRmin, MBRmax;

							for (int mbr = SPLIT_INDEX; mbr < sortedAxis.length; mbr++) {
								if (!TREE.isLeaf(MBR_TO_SPLIT)) {
									MBRmin = TREE.getLowerBorderAtDim(sortedAxis[mbr].POINTER, dim);
									MBRmax = TREE.getUpperBorderAtDim(sortedAxis[mbr].POINTER, dim);
								} else {
									point = sortedAxis[mbr].point;
									MBRmin = point[dim];
									MBRmax = point[dim];
								}
								min = (min < MBRmin) ? min : MBRmin;
								max = (max > MBRmax) ? max : MBRmax;
							}
							bbSecondLower[dim] = min;
							bbSecondUpper[dim] = max;
							upperMargin += max - min;
						}
					}
					MARGIN_LOWER = lowerMargin;
					MARGIN_UPPER = upperMargin;
				}

				public final long Margin() {
					return MARGIN_LOWER + MARGIN_UPPER;
				}

				final void computeOverlap() {
					overlap = 1;
					for (int dim = 0; dim < TREE.NUM_DIM; dim++) {
						int minBorder = Math.max(bbFirstLower[dim], bbSecondLower[dim]);
						int maxBorder = Math.min(bbFirstUpper[dim], bbSecondUpper[dim]);
						if (minBorder > maxBorder) {
							overlap = 0;
							return;
						} else
							overlap *= (maxBorder - minBorder);
					}
				}

				final void computeArea() {
					long areaFirst = 1, areaSecond = 1;
					for (int dim = 0; dim < TREE.NUM_DIM; dim++) {
						areaFirst *= bbFirstUpper[dim] - bbFirstLower[dim];
						areaSecond *= bbSecondUpper[dim] - bbSecondLower[dim];
					}
					this.area = areaFirst + areaSecond;
				}

				public void getFirst() {
					for (int mbr = 0; mbr < SPLIT_INDEX; mbr++) {
						distributeMBRPointerLeft.add(sortedAxis[mbr].POINTER);
					}
				}

				public void getSecond() {
					for (int mbr = SPLIT_INDEX; mbr < sortedAxis.length; mbr++) {
						distributeMBRPointerRight.add(sortedAxis[mbr].POINTER);
					}
				}
			}
		}

		public final class SplitContainer {
			public final int lower;
			public final int upper;
			/** XXX - In case of a leaf this the point else null */
			public final int[] point;
			public final int POINTER;

			public SplitContainer(int _lower, int _upper, int[] _point, int pointer) {
				this.lower = _lower;
				this.upper = _upper;
				this.point = _point;
				this.POINTER = pointer;
			}
		}
	}

	void out() {
		for (int pointer = 0; pointer < insertPointer; pointer += SIZE_IN_INT) {
			System.out.print(pointer + ": [");
			for (int i = 0; i < SIZE_IN_INT; i++) {
				System.out.print(TREE[pointer + i] + ", ");
			}
			System.out.println("]");
		}
	}
}
