package barowner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import registrar.RegistrarInterface;
import values.Values;

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
		BarOwner.primaryStage = primaryStage;
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
		boolean isRegistered = false;
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			List<byte[]> ret = registrar.enrollHORECA(horecaName, horecaNumber, phoneNumber, password);
			if (ret != null) {
				// TODO verwerken van ret;
				isRegistered = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (isRegistered) {
			File dir = new File(Values.FILE_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			fileName = Values.FILE_DIR + "barOwner_" + horecaName + ".csv";
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
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Something went wrong!");
			alert.setHeaderText("Registration failed");
			alert.setContentText("Plaese check the inputfields for errors!");
			alert.showAndWait();
		}
	}

	public static void login(String horecaName, String password) {
		fileName = Values.FILE_DIR + "barOwner_" + horecaName + ".csv";
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
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Something went wrong!");
			alert.setHeaderText("Login failed");
			alert.setContentText("Plaese check the inputfields for errors!");
			alert.showAndWait();
		}
	}

	public static String getHorecaName() {
		return horecaName;
	}

	public static String getHorecaNumber() {
		return horecaNumber;
	}

}
