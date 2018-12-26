package application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javafx.scene.layout.GridPane;

public class GridPaneSize {

	public static int cellCount(GridPane grid) {
		return getGridRows(grid) * getGridCols(grid);
	}
	
	public static int getGridRows(GridPane grid) {
		return invokeIntMethod(grid, "getNumberOfRows");
	}

	public static int getGridCols(GridPane grid) {
		return invokeIntMethod(grid, "getNumberOfColumns");
	}

	/// HACK: Why can JFX not give me grid rows and cols?????
	private static int invokeIntMethod(GridPane grid, String methodName) {
		try {
			Method method = grid.getClass().getDeclaredMethod(methodName);
			method.setAccessible(true);
			return (Integer) method.invoke(grid);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return 0;
	}

}
