public class JVMRunnerSample2 {

	public static void main(String[] _) {
		com.puppycrawl.tools.checkstyle.Main.main(new String[]{
				"-c",
				"Copyofchecks.xml",
		"E:\\ZeugVon145\\Programs\\Elevator\\src\\ElevatorSystem\\Elevator.java"});
		for (int i = 0; i < 30; i++) {
			String counter = (i < 10 ? "0" : "") + i;
			com.puppycrawl.tools.checkstyle.Main.main(new String[]{
					"-c",
					"checks\\checks" + counter + ".xml",
			"E:\\ZeugVon145\\Programs\\Elevator\\src\\ElevatorSystem\\Elevator.java"});
		}
	}
}
