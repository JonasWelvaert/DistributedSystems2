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
	private static String horecaName;
	private static String horecaNumber;
	private static String phoneNumber;
	private static String password;
	private static Stage primaryStage;

	public static void main(String[] args) {
		launch(args);
	}

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

	public static void openInfoScene() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(BarOwner.class.getResource("/barowner/BarOwnerInfo.fxml"));
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

	public static void register(String horecaName, String horecaNumber, String phoneNumber, String password) {

		// TODO: register bij Registrar...
		boolean isRegistered = true;

		if (isRegistered) {
			File dir = new File(System.getProperty("user.home") + "\\.contacttracing\\");
			if (!dir.exists()) {
				dir.mkdir();
			}
			fileName = System.getProperty("user.home") + "\\.contacttracing\\barOwner_" + horecaName + ".csv";
			File file = new File(fileName);
			FileWriter fw;
			try {
				file.createNewFile();
				fw = new FileWriter(file);
				fw.write(horecaName + System.lineSeparator());
				fw.write(horecaNumber + System.lineSeparator());
				fw.write(phoneNumber + System.lineSeparator());
				fw.write(password + System.lineSeparator()); // TODO: password/file encrypteren
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			BarOwner.horecaName = horecaName;
			BarOwner.horecaNumber = horecaNumber;
			BarOwner.phoneNumber = phoneNumber;
			BarOwner.password = password;

			BarOwner.openInfoScene();
		} else {
			// TODO: not registered because of errors
		}
	}

	public static void login(String horecaName, String password) {
		fileName = System.getProperty("user.home") + "\\.contacttracing\\barOwner_" + horecaName + ".csv";
		System.out.println(fileName);
		File file = new File(fileName);
		String[] inhoud = new String[4];
		if (!file.exists()) {
			// TODO: vraag aan registrar
			inhoud = null;
		} else {
			try {
				Scanner scanner = new Scanner(file);
				for (int i = 0; i < 4; i++) {
					inhoud[i] = scanner.nextLine();
				}
				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		if (inhoud != null && horecaName.equals(inhoud[0]) && password.equals(inhoud[3])) {
			BarOwner.horecaName = inhoud[0];
			BarOwner.horecaNumber = inhoud[1];
			BarOwner.phoneNumber = inhoud[2];
			BarOwner.password = inhoud[3];
			BarOwner.openInfoScene();
		} else {
			// TODO: foute inlogGegevens.
		}
	}

	public static String getHorecaName() {
		return horecaName;
	}

}
