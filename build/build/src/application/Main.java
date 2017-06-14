package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String APP_NAME = "Highlands Realty Kinect";

	@Override
	public void start(Stage stage) {
		try {			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			
			Controller controller = loader.getController();
			controller.setScene(scene);
			
			System.out.println("1");
			// Cursor Image
			Image img = new Image("hand.png");
			ImageCursor cursor = new ImageCursor(img, img.getWidth() / 2, img.getHeight() / 2);
			scene.setCursor(cursor);
			System.out.println("2");
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setFullScreen(true);
			stage.setScene(scene);
			stage.setTitle(APP_NAME);
			stage.setAlwaysOnTop(true);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
