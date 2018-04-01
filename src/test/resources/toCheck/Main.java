

public class Main {

	public static void main(String[] args) {
		try {
			com.puppycrawl.tools.checkstyle.Main.main(new String[]{
					"-c", "C:\\Users\\Jens Meinicke\\workspaceVarexJ\\CheckStyleRunner\\Copyofchecks.xml",
					"C:\\Users\\Jens Meinicke\\workspaceVarexJ\\CheckStyleRunner\\src\\"
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
