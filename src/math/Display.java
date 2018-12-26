package math;

import java.awt.Dimension;

public class Display {

	static Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	static int pixelPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();

	public static float getScreenHeight() {
		return screen.height;
	}
	
	public static float getScreenWidth() {
		return screen.width;
	}
	
	public static float getScreenHeightMeters() {
		float height = (float) screen.getHeight() / pixelPerInch;
		return inchesToMeters(height);
	}

	public static float getScreenWidthMeters() {
		float width = (float) screen.getWidth() / pixelPerInch;
		return inchesToMeters(width);
	}

	public static float inchesToMeters(float inches) {
		return inches * 0.0254f;
	}

}
