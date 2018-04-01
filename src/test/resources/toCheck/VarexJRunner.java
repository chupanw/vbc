import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class VarexJRunner {

	public static void main(String[] args) {
		System.out.println("VarexJRunner.main()");
		new VarexJRunner();
	}

	public VarexJRunner() {
		List<String> commands = new LinkedList<>();
		commands.add("C:/Program Files/Java/jdk1.7.0_75/bin/java");
//		commands.add("-Xns2000m");
		commands.add("-Xms2g");
		commands.add("-Xmx16g");
		commands.add("-XX:+UseConcMarkSweepGC");
		commands.add("-XX:+UseParNewGC");
		commands.add("-XX:-UseParallelGC");
//		commands.add("-XX:NewSize=2000m");
//		commands.add("–XX:NewSize=2g");
//		commands.add("–XX:MaxNewSize=2000m");
		
//		commands.add("-Xgc: parallel");
//		commands.add("-XXaggressive");
		commands.add("-jar");
		commands.add("C:/Users/meinicke/git/VarexJ/VarexJ/build/libs/RunJPF.jar");
		commands.add("+nhandler.delegateUnhandledNative");
		commands.add("+native_classpath=C:/Users/meinicke/git/VarexJ/VarexJ/lib/*");
		commands.add("+search.class=.search.RandomSearch");
		commands.add("+classpath=C:\\Users\\meinicke\\workspace\\FindBugsRunner\\bin\\;"
				+ "C:/Users/meinicke/git/VarexJ/VarexJ/build/libs/jpf-classes.jar;"
				+ "C:/Users/meinicke/git/VarexJ/VarexJ/build/libs/jpf.jar;"
				+ "E:/downloads/checkstyle-6.4.1-src/checkstyle-6.4.1/target/classes;"
				+ "C:/Users/meinicke/.m2/repository/com/google/guava/guava/18.0/guava-18.0.jar;"
				+ "C:/Users/meinicke/.m2/repository/com/google/code/findbugs/jsr305/2.0.1/jsr305-2.0.1.jar;"
				+ "C:/Users/meinicke/git/VarexJ/lib/annotations.jar;"
				+ "C:/Users/meinicke/.m2/repository/commons-beanutils/commons-beanutils-core/1.8.3/commons-beanutils-core-1.8.3.jar;"
				+ "C:/Users/meinicke/.m2/repository/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar;"
				+ "C:/Users/meinicke/.m2/repository/log4j/log4j/1.2.12/log4j-1.2.12.jar;"
				+ "C:/Users/meinicke/.m2/repository/antlr/antlr/2.7.7/antlr-2.7.7.jar;"
				+ "C:/Users/meinicke/.m2/repository/org/antlr/antlr4-runtime/4.3/antlr4-runtime-4.3.jar;"
				+ "C:/Users/meinicke/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar");
//		commands.add("+sample=true");
//		commands.add("+featuremodel=C:/Users/meinicke/workspace/ElevatorChanged/model.dimacs");
//		commands.add("+interaction=interaction");
//		commands.add("+interaction=composedContext");
//		commands.add("+interaction=stack");
//		commands.add("+interaction=local");
//		commands.add("+interaction=stackType");
		 commands.add("+choice=MapChoice");
		 commands.add("+stack=HybridStackHandler");
//		commands.add("+minInteraction=1");
		commands.add("com.puppycrawl.tools.checkstyle.Main");
		commands.add("-c");
//		commands.add("checks.xml");
		commands.add("Copyofchecks.xml");
//		commands.add("-o");
//		commands.add("results.xml");
		commands.add("C:/Users/meinicke/git/VarexJ/src/main/gov/nasa/jpf/vm/ThreadInfo.java");
		commands.add("E:/ZeugVon145/Programs/Elevator/src/ElevatorSystem/Elevator.java");
		commands.add("C:/Users/meinicke/workspace/CheckStyleRunner/src/Main.java");
//		for (int i = 0; i < 1; i++) {
//			long start = System.nanoTime();
			process(commands);
//			long end = System.nanoTime();
//			long time = (end - start) / 1_000_000;
//			System.out.println(time);
//			createOutput(time);
//			
//		}
}

private static void createOutput(long time) {
	File results = new File("CheckStyleVarexJVATime.csv");
	System.out.println("write results to " + results + " " + time);
	try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(results, true)))) {
		out.print(time);
		out.println();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

	private void process(List<String> commands) {
		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		BufferedReader input = null;
		BufferedReader error = null;
		try {
			Process process = processBuilder.start();
			input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			while (true) {
				try {
					String line;
					while ((line = input.readLine()) != null) {
						System.out.println(line);
					}
					while ((line = error.readLine()) != null) {
						System.err.println(line);
					}

					try {
						process.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					int exitValue = process.exitValue();
					if (exitValue != 0) {
						throw new IOException("The process doesn't finish normally (exit=" + exitValue + ")!");
					}
					break;
				} catch (IllegalThreadStateException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (error != null) {
					try {
						error.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
