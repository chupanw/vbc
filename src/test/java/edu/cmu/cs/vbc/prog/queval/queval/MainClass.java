package edu.cmu.cs.vbc.prog.queval.queval;

import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.queval.indexes.*;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.MBR;
import edu.cmu.cs.vbc.prog.queval.indexes.rtrees.splitAlgos.RStarSplit;
import edu.cmu.cs.vbc.prog.queval.query_processing.QueryPlan;
import edu.cmu.cs.vbc.prog.queval.stores.InMemoryStore;

import java.util.ArrayList;

public class MainClass {

	static long start = 0;

	public static void main(String[] args) {
		// CONFIG_RVariant & !CONFIG_GiSTII & !CONFIG_VA_SSA &
		// !CONFIG_RStarSplit & !CONFIG_LinearSplit &
		// CONFIG_QuadraticCostAlgorithm & CONFIG_SS11

		// XXX configuration for java.lang.StackOverflowError @ RVariant
		// Configuration.RVariant = true;
		// Configuration.QuadraticCostAlgorithm = true;
		// Configuration.SS11 = true;
		// Configuration.EXACT_MATCH_QUERY = true;
		// Configuration.RStartInsert = true;

		// int size = 250; // param 500000
		// int dim = 10; // param 10
		// int DIM_MAX = 10; // param 10
		// int queryPoints = size / 50; // param 10000
		// Configuration.VA_SSA = true;
		// Configuration.BPD4 = true;
		// Configuration.EXACT_MATCH_QUERY = true;
		// if !Dwarf & !GiSTII & VA_SSA:
		// java.lang.ArithmeticException: division by zero
		// at indexes.VA_SSA.approximation(VA_SSA.java:154)
		// at indexes.VA_SSA.buildIndex(VA_SSA.java:40)
		// at indexes.Index.build(Index.java:17)
		// at query_processing.QueryPlan.run(QueryPlan.java:130)
		// at queval.MainClass.run(MainClass.java:137)
		// at queval.MainClass.main(MainClass.java:68)

		try {
			// System.out.println("Args:");
			// for (String a : args) {
			// System.out.println(a);
			// }
			// System.out.println("------------");
//			if (args.length != 0) {
//				Configuration.initFeatures(args);
//
//			}

			if (!Configuration.valid() ||
					Configuration.knownBug1() ||
					Configuration.knownBug2() ||
					Configuration.knownBug3() ||
					Configuration.knownBug4()) {
				// VarexC do not support variational exceptions
//				throw new RuntimeException("Something with feature selection went wrong!");
				System.out.println("Wrong config...");
                return;
			}

			initStaticFields();

			start = System.nanoTime();

			if (Configuration.VA_SSA && !Configuration.BPD4 && !Configuration.BPD6 && !Configuration.BPD7) {
				System.out.println("WHAT?");
				return;
			}
//			if (Configuration.BPD7) {
//				return;
//			}

			new MainClass();

		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		} finally {
		}
		long end = System.nanoTime();
		System.out.println("TIME: " + ((end - start) / 1000000) + "ms");
	}

	@SuppressWarnings("rawtypes")
	private static void initStaticFields() {
		// init static fields
		ArrayList l = new ArrayList<Object>();
//		l.toArray();
//		l.add("A");
//		l.add("B");
//		Arrays.sort(l.toArray());
//		Collections.sort(l);
		new Dwarf_Linearized_Small(null);
		new RStarSplit();
		final int[][] DATA = Util.getData(10, 10, 10, 87654321);
		// if (!Configuration.BPD7 && !Configuration.BPD6 &&
		// !Configuration.BPD4) {
		// return;
		// }
		InMemoryStore store = new InMemoryStore(DATA[0].length, DATA.length, 0, (10 - 1));
		new GiSTII(store);
		try {
			java.lang.Class.forName("");
			MainClass.class.desiredAssertionStatus();
		} catch (ClassNotFoundException e) {
		}
		new MBR();

	}

	// @Conditional
	// static boolean QUERY = true;
	// @Conditional
	// static boolean SIZE = true;
	//
	 @VConditional
	 static boolean MDIM = true;

	public MainClass() {
		run();
	}

	public static void run() {
		System.out.println("RUN");
		int dim = 10; // param 10
		int size = 20;// SIZE ? 10 : 20; // param 500000
		int DIM_MAX = 10;// MDIM ? 10 : 100; // param 10
		int queryPoints = 10;// QUERY ? 10 : 100; // param 10000

		final int[][] DATA = Util.getData(size, dim, DIM_MAX, 87654321);
		InMemoryStore store = new InMemoryStore(DATA[0].length, DATA.length, 0, (DIM_MAX - 1));
		store.bulkInsertWithoutCheck(DATA);
		if (queryPoints == -1) {
			queryPoints = DATA.length;
		}
		// store.out();

		// select index
		Index index = null;
		if (Configuration.Dwarf) {
			index = new Dwarf(store);
		}
		if (Configuration.Dwarf_Linearized_Small) {
			index = new Dwarf_Linearized_Small(store);
		}
		if (Configuration.MyKDTree) {
			index = new MyKDTree(store);
		}
		if (Configuration.SeqScan) {
			index = new SeqScan(store);
		}
		if (Configuration.RVariant) {
			index = new RVariant(store);
		}
		if (Configuration.VA_SSA) {
			index = new VA_SSA(store);
		}
		if (Configuration.GiSTII) {
			index = new GiSTII(store);
		}
		if (index == null) {
			throw new RuntimeException("Index not defined");
		}

		// select query plan
		final QueryPlan q = new QueryPlan(store, index, queryPoints);
		q.disableQueryType(QueryPlan.EXACT_MATCH_QUERY);
		if (Configuration.EXACT_MATCH_QUERY) {
			q.enableQueryType(QueryPlan.EXACT_MATCH_QUERY);
		} else if (Configuration.KNN_QUERY) {
			q.enableQueryType(QueryPlan.KNN_QUERY);
		} else if (Configuration.EPSILON_NN_QUERY) {
			q.enableQueryType(QueryPlan.EPSILON_NN_QUERY);
		} else if (Configuration.RANGE_QUERY) {
			q.enableQueryType(QueryPlan.RANGE_QUERY);
		}

		// run queval
		q.run();
	}

	// public static void writeCSV(String outputFile, QueryPlan q) {
	// // before we open the file check to see if it already exists
	// boolean alreadyExists = new File(outputFile).exists();
	//
	// try {
	// // use FileWriter constructor that specifies open for appending
	// CSVWriter csvOutput = new CSVWriter(new FileWriter(outputFile, true),
	// ';');
	//
	// // if the file didn't already exist then we need to write out the
	// // header line
	// if (!alreadyExists) {
	// csvOutput.write("Indexname");
	// csvOutput.write("Buildtime");
	// csvOutput.write("Exactmatchtime");
	// csvOutput.write("Rangematchtime");
	// csvOutput.endRecord();
	// }
	// // else assume that the file already has the correct header line
	//
	// csvOutput.write(q.indexes[0].getName());
	// csvOutput.write(String.valueOf(q.building));
	// csvOutput.write(String.valueOf(q.exactmatch));
	// csvOutput.write(String.valueOf(q.range));
	// csvOutput.endRecord();
	// csvOutput.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// }
}
