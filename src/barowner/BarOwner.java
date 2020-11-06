package barowner;

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BarOwner extends Application {
	private static String fileName;
	private static String barName;
	private static Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
		Parent root = FXMLLoader.load(getClass().getResource("/barowner/BarOwnerRegister.fxml"));
		Scene scene = new Scene(root);

		primaryStage.setTitle("Bar owner's application");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static void setSceneInfo() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(BarOwner.class.getResource("/barowner/BarOwner.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root, 300, 540);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		String barName = String.join(" ", args);
		fileName = System.getProperty("user.home") + "/.contacttracing/" + barName + ".csv";
		try {
			Scanner scanner = new Scanner(new File(fileName));
			while (scanner.hasNextLine()) {
				BarOwner.barName = scanner.nextLine();
				// TODO other information like QR bitstream ...
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO enroll to registrar
			BarOwner.setBarName(barName);
		}

		launch(args);
	}

	public static String getBarName() {
		return barName;
	}

	public static void setBarName(String barName) {
		File dir = new File(System.getProperty("user.home") + "/.contacttracing/");
		if (!dir.exists()) {
			dir.mkdir();
		}
		File file = new File(fileName);
		FileWriter fw;
		try {
			file.createNewFile();
			fw = new FileWriter(file);
			fw.write(barName);
			fw.close();
			BarOwner.barName = barName;
		} catch (IOException e) {
			System.out.println("IOException in BarOwner.setBarName()");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
