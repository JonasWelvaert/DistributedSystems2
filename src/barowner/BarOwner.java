package barowner;

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

	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/barowner/BarOwner.fxml"));

		Scene scene = new Scene(root, 380, 360);

		primaryStage.setTitle("Bar owner's application");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static void main(String[] args) {
		String barName = "";
		if (args.length == 0) {
			Scanner scanner = new Scanner(System.in);
			while (barName.equals("")) {
				System.out.println("Give a name for your business: ");
				barName = scanner.nextLine();
			}
			scanner.close();
		} else {
			barName = String.join(" ", args);
			System.out.println(barName);
			if (barName.equals("")) {
				System.out.println("Fill in a good business name.");
				System.exit(1);
			}
		}
		fileName = System.getProperty("user.home") + "/.contacttracing/" + barName + ".csv";
		try {
			Scanner scanner = new Scanner(new File(fileName));
			while (scanner.hasNextLine()) {
				BarOwner.barName = scanner.nextLine();
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException in BarOwner.main()");
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
			System.out.println("IOException in BarOwner.setBarName():second try");
		}
	}
}
