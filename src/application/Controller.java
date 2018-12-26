package application;

import java.awt.Dimension;
import java.util.List;

import org.pdfsam.ui.RingProgressIndicator;

import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import math.Display;
import math.Matrix4f;
import math.Vector3f;

public class Controller extends J4KSDK {

	public static final float SENSITIVITY = 0.1f;
	public static final double MILLISECONDS_BEFORE_CLICK = 1500;
	public static final int UPDATE_SPEED = 10; // millis

	private Scene scene;
	private int mouseX;
	private int mouseY;
	
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
		
		SensorTransforms.sensorElevationAngleToUse = this.getElevationAngle();
		start(J4KSDK.COLOR | J4KSDK.DEPTH | J4KSDK.SKELETON);
	}

	private void initTimer() {
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(UPDATE_SPEED), (e) -> {
			indicator.setProgress((int) (milliseconds / MILLISECONDS_BEFORE_CLICK * 100));
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

		boolean shouldShowLeftIndicator = (root.getCountCousin() > 0 && oneOrFewerChildren)
				|| (root.getChildrenFileDisplays().size() > totalCells && pageIndex != 0);
		leftImage.getParent().setVisible(shouldShowLeftIndicator);

		boolean shouldShowBack = root != rootDisplay;
		showBack(shouldShowBack);

		grid.getChildren().clear();
		if (root.getChildrenFileDisplays().size() <= 1) {
			FileDisplay display = root;

			// Opening a folder with just one image? Go ahead and open that
			// image!
			if (root.getChildrenFileDisplays().size() == 1) {
				display = root.getChildrenFileDisplays().get(0);
				leftImage.getParent().setVisible(false);
				rightImage.getParent().setVisible(false);
			}

			// Center image.
			GridPane.setColumnSpan(display, GridPaneSize.getGridCols(grid));
			GridPane.setRowSpan(display, GridPaneSize.getGridRows(grid));

			// Enlarge image.
			display.fitWidthProperty().bind(grid.widthProperty().divide(1.15));
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

		if (children.size() == 1) {
			startCol = 1;
		}

		for (int row = startRow; row < rows; row++) {
			for (int col = startCol; col < cols; col++) {
				StackPane node = new StackPane();
				if (childIndex < children.size()) {
					FileDisplay display = children.get(childIndex);
					display.setMouseTransparent(true);
					node.getChildren().add(display);

					if (display.getFile().isFile()) {
						display.setPreserveRatio(true);
					} else {
						if (display.isRegularFolder()) {
							Label dirLabel = new Label(display.getFile().getName());
							dirLabel.setMouseTransparent(true);
							node.getChildren().add(dirLabel);
						}
					}

					// Scale the image to fit the grid cell.
					display.fitHeightProperty()
							.bind(grid.heightProperty().subtract(grid.getVgap() * (rows + 1)).divide(rows));
					display.fitWidthProperty()
							.bind(grid.widthProperty().subtract(grid.getHgap() * (cols + 1)).divide(cols));

					node.setOnMouseEntered(e -> {
						selectedFileDisplay = display;
						ScaleTransition scaleUp = new ScaleTransition(Duration.millis(350), node);
						scaleUp.setToX(1.1);
						scaleUp.setToY(1.1);
						scaleUp.playFromStart();
						timeline.play();
					});

					node.setOnMouseExited(e -> {
						resetTimer();
						ScaleTransition scaleDown = new ScaleTransition(Duration.millis(500), node);
						scaleDown.setToX(1);
						scaleDown.setToY(1);
						scaleDown.playFromStart();
					});
				}

				grid.add(node, col, row);
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
			fadeCursor();
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
			fadeCursor();
		});

		rightImage.setOnMouseExited(e -> {
			resetTimer();
		});

		// Left
		leftImage.setOnMouseEntered(e -> {
			selectedFileDisplay = left;
			timeline.play();
			fadeCursor();
		});

		leftImage.setOnMouseExited(e -> {
			resetTimer();
		});
	}

	void fadeCursor() {
		Image img = new Image("handHidden.png");
		ImageCursor cursor = new ImageCursor(img, img.getWidth() / 2, img.getHeight() / 2);
		scene.setCursor(cursor);
	}

	void showCursor() {
		Image img = new Image("hand.png");
		ImageCursor cursor = new ImageCursor(img, img.getWidth() / 2, img.getHeight() / 2);
		scene.setCursor(cursor);
	}

	private void resetTimer() {
		timeline.pause();
		milliseconds = 0;
		indicator.setProgress(0);
		selectedFileDisplay = null;
		showCursor();
	}

	public void setScene(Scene scene) {
		this.scene = scene;

		double center = indicator.getInnerCircleRadius() + indicator.getRingWidth();

		scene.addEventFilter(MouseEvent.ANY, e -> {
			mouseX = (int)e.getSceneX();
			mouseY = (int)e.getSceneY();
			indicator.setTranslateX(e.getSceneX() - center - gridStackPane.getLayoutX());
			indicator.setTranslateY(e.getSceneY() - center - gridStackPane.getLayoutY());
		});

		showCursor();
	}

	@Override
	public void onColorFrameEvent(byte[] arg0) {
		
	}

	@Override
	public void onDepthFrameEvent(short[] arg0, byte[] arg1, float[] arg2, float[] arg3) {
	}

	@Override
	public void onSkeletonFrameEvent(boolean[] flags, float[] positions, float[] orientations, byte[] states) {
		for (int i = 0; i < getSkeletonCountLimit(); i++) {
			
			Skeleton s = Skeleton.getSkeleton(i, flags, positions, orientations, states, this);
			
			
			
			if (s.isTracked() && s.isJointTracked(Skeleton.HAND_RIGHT)) {
				Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
				

				Matrix4f transform = SensorTransforms.sensorToScreenCoordinatesTransform(
						SensorTransforms.sensorOffsetFromScreenCenter,
						SensorTransforms.sensorElevationAngleToUse,
						Display.getScreenWidthMeters(),
						Display.getScreenHeightMeters(),
						Display.getScreenWidth(),
						Display.getScreenHeight());
				
				Vector3f pos = SensorTransforms.transformSkeletonPoint(transform,
						new Vector3f(
								s.get3DJointX(Skeleton.HAND_RIGHT),
								s.get3DJointY(Skeleton.HAND_RIGHT),
								s.get3DJointZ(Skeleton.HAND_RIGHT)));
				
				float headX = pos.x * screenSize.width * 0;
				float headY = pos.y * screenSize.height * 0;
				
//				float x = (pos.x * screenSize.width * 2.1f) + headX;
//				float y = (pos.y * -screenSize.height * 2.5f) + headY;
				
				float x = pos.x;
				float y = pos.y;
					
				System.out.println(x + ", " + y);
				
				try {
					java.awt.Robot robot = new java.awt.Robot();

				    robot.mouseMove((int)lerp(mouseX, x, SENSITIVITY), (int)lerp(mouseY, y, SENSITIVITY));
				   
				} catch (java.awt.AWTException e) {
				    e.printStackTrace();
				}
			}
			
		}
	}
	
	float lerp(float a, float b, float f)
	{
	    return a + f * (b - a);
	}

	public void onClose() {
		this.stop();
	}
	
}
