package visitor;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import registrar.RegistrarInterface;
import values.Values;

public class Visitor extends Application {
	private static String name;
	private static String phoneNumber;
	private static Map<LocalDate, List<byte[]>> tokencache = new HashMap<LocalDate, List<byte[]>>();

	private static Stage primaryStage;

	public static void main(String[] args) {
		if(enrollUser("0472/07 77 74")) {
			System.out.println("Success!");
		} else {
			System.out.println("Failure :(");
		}
		System.exit(0);
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
	
	public static boolean enrollUser(String phoneNumber) {
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			return registrar.enrollUser(phoneNumber);
		} catch (RemoteException | NotBoundException re) {
			re.printStackTrace();
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Registration failed!");
			alert.setHeaderText("Registration failed");
			alert.setContentText("This phone number is already in use! If this is your first registration, please contact the app developers.");
			alert.showAndWait();
			return false;
		}
	}
	
	public static PublicKey getRegistrarPublicKey() {
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			return registrar.getPublicKey();
		} catch (RemoteException | NotBoundException e) {
			System.err.println("error fetching public key from registrar");
			e.printStackTrace();
			return null;
		}
	}
}
