package visitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Visitor extends Application {

	private static Stage primaryStage;

	public static void main(String[] args) {
		launch();

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Visitor.primaryStage = primaryStage;
		Parent root = FXMLLoader.load(getClass().getResource("/visitor/VisitorRegister.fxml"));
		Scene scene = new Scene(root);

		primaryStage.setTitle("Visitor's application");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

}
