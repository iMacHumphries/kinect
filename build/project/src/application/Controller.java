package application;

import java.awt.Dimension;
import java.util.List;

import org.pdfsam.ui.RingProgressIndicator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Controller {

	public static final double MILLISECONDS_BEFORE_CLICK = 1500;
	public static final int UPDATE_SPEED = 10; // millis
	
	@FXML
	BorderPane borderPane;
	@FXML
	StackPane gridStackPane;
	@FXML
	GridPane grid;
	@FXML
	ImageView leftImage;
	@FXML
	ImageView rightImage;
	@FXML
	ImageView back;
	
	Timeline timeline;
	long milliseconds;

	RingProgressIndicator indicator = new RingProgressIndicator();
	private int pageIndex;
	private FileDisplay rootDisplay;
	private FileDisplay selectedFileDisplay;
	private FileDisplay currentFileDisplay;
	private FileDisplay left, right;
	
	@FXML
	public void initialize() {
		left = new FileDisplay();
		right = new FileDisplay();
		
		timeline = new Timeline();
		milliseconds = 0;
		
		initTimer();
				
		indicator.setMouseTransparent(true);
		indicator.setProgress(0);
		gridStackPane.getChildren().add(indicator);
		
		// Resize grid.
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		borderPane.setPrefSize(screenSize.getWidth(), screenSize.getHeight());
		borderPane.layout();

		pageIndex = 0;
		leftImage.fitHeightProperty().bind(grid.heightProperty());
		rightImage.fitHeightProperty().bind(grid.heightProperty());
		
		// Display first level of tree.
		rootDisplay = FileFinder.getFileDisplayTree();
		display(rootDisplay);
		
		addBackMouseListener();
		addSideImageMouseListeners();
	}
	
	private void initTimer() {
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(UPDATE_SPEED), (e) -> {
			indicator.setProgress((int) (milliseconds / MILLISECONDS_BEFORE_CLICK * 100 ));
			milliseconds += UPDATE_SPEED;
			
			if (milliseconds > MILLISECONDS_BEFORE_CLICK) {
				milliseconds = 0;
				
				if (selectedFileDisplay != null) {
					
					if (selectedFileDisplay == left) {
						if (currentFileDisplay.getChildrenFileDisplays().isEmpty()) {
							pageIndex = 0;
							display(currentFileDisplay.getPrevious());
						} else {
							pageIndex = Math.max(0, pageIndex - 1);
							display(currentFileDisplay);
						}
					} else if (selectedFileDisplay == right) {
						if (currentFileDisplay.getChildrenFileDisplays().isEmpty()) {
							pageIndex = 0;
							display(currentFileDisplay.getNextChild());
						} else {
							pageIndex++;
							display(currentFileDisplay);
						}
					} else {
						if (selectedFileDisplay == currentFileDisplay) {
							selectedFileDisplay = selectedFileDisplay.getParentFileDisplay();
						}
						pageIndex = 0;
						display(selectedFileDisplay);
					}
				}	
			}
		}));
	}

	private void display(FileDisplay root) {		

		boolean oneOrFewerChildren = root.getChildrenFileDisplays().size() <= 1;
		int totalCells = GridPaneSize.cellCount(grid);
		
		boolean shouldShowRightIndicator = (root.getCountCousin() > 0 && oneOrFewerChildren)
				|| root.getChildrenFileDisplays().size() - (totalCells * pageIndex) > totalCells;
		rightImage.getParent().setVisible(shouldShowRightIndicator);
		
		boolean shouldShowLeftIndicator = (root.getCountCousin() > 0 && oneOrFewerChildren) ||
				(root.getChildrenFileDisplays().size() > totalCells && pageIndex != 0);
		leftImage.getParent().setVisible(shouldShowLeftIndicator);
		
		boolean shouldShowBack = root != rootDisplay;
		showBack(shouldShowBack);
		
		grid.getChildren().clear();
		if (root.getChildrenFileDisplays().size() <= 1) {
			FileDisplay display = root;
			
			// Opening a folder with just one image? Go ahead and open that image!
			if (root.getChildrenFileDisplays().size() == 1) {
				display = root.getChildrenFileDisplays().get(0);
				leftImage.getParent().setVisible(false);
				rightImage.getParent().setVisible(false);
			}
			
			// Center image.
			GridPane.setColumnSpan(display, GridPaneSize.getGridCols(grid));
			GridPane.setRowSpan(display, GridPaneSize.getGridRows(grid));
						
			// Enlarge image.
			display.fitWidthProperty().bind(grid.widthProperty());
			display.fitHeightProperty().bind(grid.heightProperty().divide(1.15));
			
			grid.getChildren().add(display);
		} else {
			GridPane.setColumnSpan(root, 1);
			GridPane.setRowSpan(root, 1);
			root.fitHeightProperty().unbind();
			root.fitWidthProperty().unbind();
			displayChildren(root);
		}
		
		currentFileDisplay = root;
	}
	
	private void displayChildren(FileDisplay root) {

		List<FileDisplay> children = root.getChildrenFileDisplays();

		int childIndex = pageIndex * GridPaneSize.cellCount(grid);
		int cols = GridPaneSize.getGridCols(grid);
		int rows = GridPaneSize.getGridRows(grid);

		int startRow = 0;
		int startCol = 0;
		
		if (children.size() <= cols) {
			startCol = 1;
			
			if (children.size() == 1) {
				startRow = rows / 2;
			}
		}
			
		for (int col = startCol; col < cols; col++) {
			for (int row = startRow; row < rows; row++) {

				StackPane node = new StackPane();
				if (childIndex < children.size()) {
					FileDisplay display = children.get(childIndex);
					display.setMouseTransparent(true);
					node.getChildren().add(display);

					if (display.getFile().isFile()) {
						display.setPreserveRatio(true);
					} else {
						Label dirLabel = new Label(display.getFile().getName());
						dirLabel.setMouseTransparent(true);
						node.getChildren().add(dirLabel);
					}

					// Scale the image to fit the grid cell.
					display.fitHeightProperty()
							.bind(grid.heightProperty().subtract(grid.getVgap() * (rows + 1)).divide(rows));
					display.fitWidthProperty()
							.bind(grid.widthProperty().subtract(grid.getHgap() * (cols + 1)).divide(cols));
					
					node.setOnMouseEntered(e -> {
						selectedFileDisplay = display;
						timeline.play();
					});
					
					node.setOnMouseExited(e -> {					
						resetTimer();
					});
				}
				
				grid.add(node, row, col);
				childIndex++;
			}
		}
	}
	
	void showBack(boolean shouldShowBack) {
		back.setVisible(shouldShowBack);
	}
	
	void addBackMouseListener() {
		back.setOnMouseEntered(e -> {
			selectedFileDisplay = currentFileDisplay.getParentFileDisplay();
			timeline.play();
		});
		
		back.setOnMouseExited(e -> {
			resetTimer();
		});
	}
	
	void addSideImageMouseListeners() {
		// Right
		rightImage.setOnMouseEntered(e -> {
			selectedFileDisplay = right;
			timeline.play();
		});
		
		rightImage.setOnMouseExited(e -> {
			resetTimer();
		});
		
		// Left 
		leftImage.setOnMouseEntered(e -> {
			selectedFileDisplay = left;
			timeline.play();
		});
		
		leftImage.setOnMouseExited(e -> {
			resetTimer();
		});
	}
	
	private void resetTimer() {
		timeline.pause();
		milliseconds = 0;
		indicator.setProgress(0);
		selectedFileDisplay = null;
	}

	public void setScene(Scene scene) {

		double center = indicator.getInnerCircleRadius() + indicator.getRingWidth();
		
		scene.addEventFilter(MouseEvent.ANY, e -> {
			indicator.setTranslateX(e.getSceneX() - center - gridStackPane.getLayoutX());
			indicator.setTranslateY(e.getSceneY() - center - gridStackPane.getLayoutY());
		});
	}
	
}
