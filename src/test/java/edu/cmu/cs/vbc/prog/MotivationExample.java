package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.annotation.VConditional;

public class MotivationExample {
	@VConditional
	static boolean SMILEY = false;
	@VConditional
	static boolean WEATHER = true;
	@VConditional
	static boolean FAHRENHEIT = false;

	public static void main(String[] args) {
		System.out.println(toHTML());
	}

	public static String toHTML() {
		String h = getHTMLHeader();
		String c = getContent();
		if (SMILEY) 
			c = c.replace(":]", "<img>...</img>");
		if (WEATHER) {
			String w = getWeather();
			c = c.replace("[:w:]", w);
		}
		String f = getHTMLFooter();
		return h + c + f;
	}

	public static String getWeather() {
		float t = getCelsius();
		if (FAHRENHEIT)
			return (t * 1.8f + 32f) + "F";
		else 
			return t + "C";
	}

	private static String getHTMLHeader() {
		return "<html>";
	}

	private static String getHTMLFooter() {
		return "</html>";
	}

	private static String getContent() {
		return "It's [:w:].";
	}

	private static float getCelsius() {
		return 30.0f;
	}
}
