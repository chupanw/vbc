import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class VarexJRunnerBF {

	public static void main(String[] args) {
		new VarexJRunnerBF();
	}

	public VarexJRunnerBF() {
		LinkedList<String> commands = new LinkedList<>();
		commands.add("java");
//		commands.add("-Xns2000m");
		commands.add("-Xms6g");
		commands.add("-Xmx6g");
		commands.add("-XX:+UseConcMarkSweepGC");
		commands.add("-XX:+UseParNewGC");
		commands.add("-XX:-UseParallelGC");
//		commands.add("-XX:NewSize=2000m");
//		commands.add("–XX:NewSize=2g");
//		commands.add("–XX:MaxNewSize=2000m");
		
//		commands.add("-Xgc: parallel");
//		commands.add("-XXaggressive");
		
		commands.add("-jar");
		commands.add("C:\\Users\\meinicke\\git\\VarexJ\\build\\RunJPF.jar");
		commands.add("+nhandler.delegateUnhandledNative");
		commands.add("+native_classpath=C:\\Users\\meinicke.WI-01\\VarexJ\\lib\\*");
		commands.add("+search.class=.search.RandomSearch");
		commands.add("+classpath=C:\\Users\\meinicke\\workspace\\FindBugsRunner\\bin\\;"
				+ "C:\\Users\\meinicke\\git\\VarexJ\\build\\jpf.jar;"
				+ "E:\\downloads\\checkstyle-6.4.1-src\\checkstyle-6.4.1\\target\\classes;"
				+ "C:\\Users\\meinicke\\.m2\\repository\\com\\google\\guava\\guava\\18.0\\guava-18.0.jar;"
				+ "C:\\Users\\meinicke\\git\\VarexJ\\lib\\annotations.jar;"
				+ "C:\\Users\\meinicke\\.m2\\repository\\commons-beanutils\\commons-beanutils-core\\1.8.3\\commons-beanutils-core-1.8.3.jar;"
				+ "C:\\Users\\meinicke\\.m2\\repository\\commons-logging\\commons-logging\\1.1.1\\commons-logging-1.1.1.jar;"
				+ "C:\\Users\\meinicke\\.m2\\repository\\log4j\\log4j\\1.2.12\\log4j-1.2.12.jar;"
				+ "C:\\Users\\meinicke\\.m2\\repository\\antlr\\antlr\\2.7.7\\antlr-2.7.7.jar;"
				+ "C:\\Users\\meinicke\\.m2\\repository\\org\\antlr\\antlr4-runtime\\4.3\\antlr4-runtime-4.3.jar;"
				+ "C:\\Users\\meinicke\\.m2\\repository\\commons-cli\\commons-cli\\1.2\\commons-cli-1.2.jar");
		
//		commands.add("+featuremodel=C:\\Users\\meinicke\\workspace\\ElevatorChanged\\model.dimacs");
//		commands.add("+interaction=local2");
//		commands.add("+interaction=composedContext");
//		commands.add("+interaction=interaction");
//		commands.add("+interaction=local");
//		commands.add("+interaction=feature");
		 commands.add("+choice=MapChoice");
//		commands.add("+minInteraction=1");
		commands.add("com.puppycrawl.tools.checkstyle.Main");
		commands.add("-c");
		commands.add("");
		commands.add("");
		for (int i = 22; i < 23; i++) {
			commands.removeLast();
			commands.removeLast();
			String counter = (i < 10 ? "0" : "") + i;
			System.out.println(counter + "-------------------------------");
			commands.add("checks\\checks" + counter + ".xml");
	//		commands.add("-o");
	//		commands.add("results.xml");
	//		commands.add("C:\\Users\\meinicke\\git\\VarexJ\\src\\main\\gov\\nasa\\jpf\\vm\\ThreadInfo.java");
			commands.add("E:\\ZeugVon145\\Programs\\Elevator\\src\\ElevatorSystem\\Elevator.java");
	//		commands.add("C:\\Users\\meinicke\\workspace\\CheckStyleRunner\\src\\Main.java");
			
			for (int k = 0; k < 1; k++) {
				long start = System.nanoTime();
				process(commands);
				long end = System.nanoTime();
				long time = (end - start) / 1_000_000;
				System.out.println(time);
				createOutput(i , time);
				
			}
		}
	}

		private static void createOutput(int k, long time) {
			File results = new File("CheckStyleVarexJSample22.csv");
			System.out.println("write results to " + results + " " + time);
			try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(results, true)))) {
				out.print(k);
				out.print(';');
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
						System.out.println(line);
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
				if (input != null)
					input.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (error != null)
					try {
						error.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}
}
