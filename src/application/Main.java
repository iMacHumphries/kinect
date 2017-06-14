package application;

import java.net.URL;

import javax.swing.ImageIcon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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
						
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setFullScreen(true);
			stage.setScene(scene);
			stage.setTitle(APP_NAME);
			stage.setAlwaysOnTop(true);
			
			Image icon = new Image("icon.png");
			stage.getIcons().add(icon);
			
			try {
				URL iconURL = Main.class.getResource("./../icon.png");
				java.awt.Image macIcon = new ImageIcon(iconURL).getImage();
				//com.apple.eawt.Application.getApplication().setDockIconImage(macIcon);
			} catch (Exception e) {
			}
			
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
