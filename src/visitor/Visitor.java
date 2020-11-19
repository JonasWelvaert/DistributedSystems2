package visitor;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import registrar.RegistrarInterface;
import registrar.TokensAlreadyIssuedException;
import registrar.UserNotRegisteredException;
import sharedclasses.Token;
import values.Values;

public class Visitor extends Application {
	private static String name;
	private static String phoneNumber;
	private static Map<LocalDate, List<Token>> allTokens = new HashMap<LocalDate, List<Token>>();
	private static Map<LocalDate, Stack<Token>> tokenCache = new HashMap<LocalDate, Stack<Token>>();

	private static Stage primaryStage;

	public static void main(String[] args) {
		
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
	
	public static List<Token> getTokenAllocation(String phoneNumber) {
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			try {
				return registrar.retrieveTokens(phoneNumber);
			} catch (UserNotRegisteredException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("User identification error.");
				alert.setHeaderText("Server failed to identify user.");
				alert.setContentText("A server issue was encountered identifying the user. Please contact a server admin.");
				alert.showAndWait();
				return null;
			} catch (TokensAlreadyIssuedException taie) {
				taie.printStackTrace();
				return null;
			}
		} catch (RemoteException | NotBoundException e) {
			System.err.println("error connecting to the registrar");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void register(String name, String phoneNumber, String password) {
		if(enrollUser(phoneNumber)) {
			//if this is succesfull, it's a newly registered account and a .csv should be constructed
		} else {
			//in this case we just need to make an alert to check server availability or this number might already be registered.
		}
	}
	
	private static void noGuiTest() {
		String number = "0472/07 77 74";
		if(enrollUser(number)) {
			System.out.println("Register: Success!");
		} else {
			System.out.println("Register: Already registered!");
		}
		List<Token> todaysTokens = getTokenAllocation(number);
		if(todaysTokens == null) {
			System.out.println("gettingTokens: Failure.");
			System.exit(0);
		} else {
			System.out.println("gettingTokens: Success.");
			allTokens.put(LocalDate.now(), todaysTokens);
			Stack tokens = new Stack();
			tokens.addAll(todaysTokens);
			tokenCache.put(LocalDate.now(), tokens);
		}
		List<Token> duplicateTokens = getTokenAllocation(number);
		if(duplicateTokens != null) {
			System.out.println("uh oh...");
		} else {
			System.out.println("Second Batch not received :)");
		}
		int count = 0;
		try {
			while(count < 55) {
				tokenCache.get(LocalDate.now()).pop();
				count++;
			}
		} catch (EmptyStackException ese) {
			System.out.println("expected amount: 48");
			System.out.println("amount of tokens received today: " + count);
			System.out.println(allTokens.get(LocalDate.now()).size());
		} finally {
			System.exit(0);
		}
	}
}
