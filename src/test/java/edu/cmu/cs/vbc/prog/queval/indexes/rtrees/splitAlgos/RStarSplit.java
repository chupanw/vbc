package edu.cmu.cs.vbc.prog.queval.indexes.rtrees.splitAlgos;

import edu.cmu.cs.vbc.prog.queval.indexes.RVariant;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;

import java.util.Arrays;
import java.util.Comparator;

public class RStarSplit extends SplitAlgorithm{
	private static final Comparator<MyContainer> compLower, compUpper, compLowerUpper;
	
	static{
		compLower = new Comparator<RStarSplit.MyContainer>() {
			@Override
			public int compare(MyContainer o1, MyContainer o2) {
				return o1.lower-o2.lower;
			}
		};
		compUpper = new Comparator<RStarSplit.MyContainer>() {	
			@Override
			public int compare(MyContainer o1, MyContainer o2) {
				return o1.upper-o2.upper;
			}
		};
		compLowerUpper = new Comparator<RStarSplit.MyContainer>() {	
			@Override
			public int compare(MyContainer o1, MyContainer o2) {
				int lower = compLower.compare(o1, o2);
				if (lower == 0) {
					return compUpper.compare(o1, o2);
				}
				return lower;
			}
		};
	}
		
	@Override
	public void split(MBR toSplit) {
		RVariant tree = toSplit.tree;
		MBR first = new MBR(tree, toSplit);
		MBR second = new MBR(tree, toSplit);
		
		//S1: Get the dimension with the smallest S
		SplitHandler[] axises = new SplitHandler[tree.DIMENSIONS];
		long Smin = Long.MAX_VALUE;
		int indexSmin = -1;
		for(int axis=0; axis<tree.DIMENSIONS; axis++){//axis == dim -> synonym
			axises[axis] = new SplitHandler(toSplit, axis);
			if(axises[axis].S<Smin){
				Smin = axises[axis].S;
				indexSmin = axis;
			}
		}
		
		//S2: 
		SplitHandler.Distribution bestDistribution = axises[indexSmin].bestDistribution();
		MBR[] firstGroup = bestDistribution.getFirst();
		MBR[] secondGroup = bestDistribution.getSecond();
		
		for(int mbr=0;mbr<firstGroup.length;mbr++){
			MBR toInsert = firstGroup[mbr];
			toInsert.parent = first;
			first.insert(toInsert);
		}
		
		for(int mbr=0;mbr<secondGroup.length;mbr++){
			MBR toInsert = secondGroup[mbr];
			toInsert.parent = second;
			second.insert(toInsert);
		}
		
		super.handleLinkingMBRs(toSplit, first, second, tree);
	}
	
	/**
	 * Handles one axis.
	 * @author amartin
	 *
	 */
	public final class SplitHandler{
		MyContainer[] sortedAxis;
		final int dimension;
		final RVariant tree;
		final int NUM_DISTRIBUTIONS;
		Distribution[] distributions;
		/** SUM of margins of all distributions*/
		long S ;
		
		long[] margins, areas = null, overlaps = null;
		
		public SplitHandler(MBR toSplit, int _dim){
			this.dimension = _dim;
			sortedAxis = new MyContainer[toSplit.size()];
			tree = toSplit.tree;
			NUM_DISTRIBUTIONS = toSplit.size()-(2*tree.MIN_POINTS_MBR)+1;//p.5 R*-Tree
			
			for(int mbr=0;mbr<toSplit.size();mbr++){
				//copy the original child array, because need multiple copies and don't want to modify original array
				MBR temp = toSplit.children[mbr];
				sortedAxis[mbr]=new MyContainer(
						temp.minBorder[dimension]
						, temp.maxBorder[dimension]
                        , temp);
			}
//			Arrays.sort(sortedAxis, compLowerUpper);
			Arrays.sort(sortedAxis, compLower);
			Arrays.sort(sortedAxis, compUpper);
			getMarginValues();
		}
		
		/**
		 * Calculate S, the SUM of distribution's margins
		 */
		private final void getMarginValues(){ 
			//remember that at least |MIN_POINTS_MBR| shall remain in each of the splitted MBR
			
			this.distributions = new Distribution[NUM_DISTRIBUTIONS];
			for(int splitHere = tree.MIN_POINTS_MBR; splitHere<sortedAxis.length-tree.MIN_POINTS_MBR+1;splitHere++){
				Distribution temp = new Distribution(splitHere);
				distributions[splitHere-tree.MIN_POINTS_MBR] = temp;
				S+= temp.Margin();
			}
		}

		
		public final Distribution bestDistribution(){
			for(int dist=0; dist<distributions.length;dist++){
				distributions[dist].computeArea();
				distributions[dist].computeOverlap();
				Arrays.sort(distributions, new Comparator<Distribution>() {
					@Override
					public int compare(Distribution o1, Distribution o2) {
						return (int) ((o1.overlap!=o2.overlap) ? o1.overlap-o2.overlap  : o1.area-o2.area);
					}
				});
			}
			return distributions[0];
		}
		
		public final class Distribution{
			private final int SPLIT_INDEX;
			private final long MARGIN_UPPER, MARGIN_LOWER;//the only one that definetly is computed
			public long area, overlap;
			private final int[] 
			      bbFirstLower = new int[tree.DIMENSIONS]
				, bbFirstUpper = new int[tree.DIMENSIONS]
				, bbSecondLower= new int[tree.DIMENSIONS]
				, bbSecondUpper= new int[tree.DIMENSIONS]
			;
		
			private Distribution(int _splitIndex){
				this.SPLIT_INDEX = _splitIndex;
				
				//calculate bounding boxes & margins
				long lowerMargin = 0, upperMargin = 0;
				for(int dim=0; dim<tree.DIMENSIONS;dim++){
					{//nur f�r mich
						//First bounding box
						int min = Integer.MAX_VALUE, max=Integer.MIN_VALUE;
						for(int mbr=0;mbr<SPLIT_INDEX;mbr++){
							final int MBRmin = sortedAxis[mbr].mbr.minBorder[dim];
							final int MBRmax = sortedAxis[mbr].mbr.maxBorder[dim];
							min = (min<MBRmin) ? min : MBRmin;
							max = (max>MBRmax) ? max : MBRmax;
						}
						bbFirstLower[dim] = min;
						bbFirstUpper[dim] = max;
						lowerMargin += max-min;
					}
					{//nur f�r mich
						//Second bounding box
						int min = Integer.MAX_VALUE, max=Integer.MIN_VALUE;
						for(int mbr=SPLIT_INDEX;mbr<sortedAxis.length;mbr++){
							final int MBRmin = sortedAxis[mbr].mbr.minBorder[dim];
							final int MBRmax = sortedAxis[mbr].mbr.maxBorder[dim];
							min = (min<MBRmin) ? min : MBRmin;
							max = (max>MBRmax) ? max : MBRmax;
						}
						bbSecondLower[dim] = min;
						bbSecondUpper[dim] = max;
						upperMargin += max-min;		
					}
				}
				MARGIN_LOWER = lowerMargin;
				MARGIN_UPPER = upperMargin;
			}
			
			public final long Margin(){
				return MARGIN_LOWER + MARGIN_UPPER;
			}
			
			final void computeOverlap(){
				//TODO
				overlap = 1;
				for(int dim=0;dim<tree.DIMENSIONS;dim++){
					int minBorder = Math.max(bbFirstLower[dim], bbSecondLower[dim]);
					int maxBorder = Math.min(bbFirstUpper[dim], bbSecondUpper[dim]);
					if(minBorder>maxBorder){
						overlap = 0;
						return;
					}
					else
						overlap*=(maxBorder-minBorder);
				}
			}
			
			final void computeArea(){
				long areaFirst =1, areaSecond=1;
				for(int dim=0;dim<tree.DIMENSIONS;dim++){
					areaFirst  *= bbFirstUpper[dim]-bbFirstLower[dim];
					areaSecond *= bbSecondUpper[dim]-bbSecondLower[dim];
				}
				this.area = areaFirst+areaSecond;
			}
			
			public MBR[] getFirst(){
				MBR[] firstGroup = new MBR[SPLIT_INDEX];
				for(int mbr=0;mbr<SPLIT_INDEX;mbr++){
					firstGroup[mbr] = sortedAxis[mbr].mbr;
				}
				return firstGroup;
			}
			public MBR[] getSecond(){
				MBR[] secondGroup = new MBR[sortedAxis.length-SPLIT_INDEX];
				for(int mbr=SPLIT_INDEX;mbr<sortedAxis.length;mbr++){
					secondGroup[mbr-SPLIT_INDEX] = sortedAxis[mbr].mbr;
				}
				return secondGroup;
			}
		}
	}
	
	public final class MyContainer{
		public final int lower;
		public final int upper;
		public final MBR mbr;
		
		public MyContainer(int _lower, int _upper, MBR _mbr){
			this.lower 	= _lower;
			this.upper 	= _upper;
			this.mbr 	= _mbr;
		}
	}
	
	public final String toString(){
		return "R* split";
	}
}
