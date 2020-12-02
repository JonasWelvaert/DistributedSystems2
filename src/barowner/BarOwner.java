package barowner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import registrar.HorecaNumberAlreadyEnrolledException;
import registrar.RegistrarInterface;
import values.Values;

public class BarOwner extends Application {
	private static String fileName;
	private static String horecaName;
	private static String horecaNumber;
	private static String address;
	private static String password;
	private static Stage primaryStage;
	private static Map<LocalDate, byte[]> pseudonyms = new HashMap<LocalDate, byte[]>();
	private static Map<LocalDate, Integer> randomNumbers = new HashMap<LocalDate, Integer>();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			BarOwner.primaryStage = primaryStage;
			primaryStage.setTitle("Bar owner's application");
			Parent root = FXMLLoader.load(getClass().getResource("/barowner/BarOwnerRegister.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void openInfoScene() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(BarOwner.class.getResource("/barowner/BarOwnerInfo.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root, 300, 300);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static boolean register(String horecaName, String horecaNumber, String address, String password) {
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			Map<LocalDate, byte[]> ret = registrar.enrollHORECA(horecaName, horecaNumber, address, password);
			if (ret != null) {
				pseudonyms.putAll(ret);
				BarOwner.horecaName = horecaName;
				BarOwner.horecaNumber = horecaNumber;
				BarOwner.address = address;
				BarOwner.password = password;
				updateFile();
				return true;
			} else {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Something went wrong!");
				alert.setHeaderText("Registration failed");
				alert.setContentText("Plaese check the inputfields for errors!");
				alert.showAndWait();
				return false;
			}
		} catch (RemoteException | NotBoundException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Something went wrong!");
			alert.setHeaderText("Registration failed");
			alert.setContentText("Something is wrong with the registrar server, plaese try again later.");
			alert.showAndWait();
			return false;
		} catch (HorecaNumberAlreadyEnrolledException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Something went wrong!");
			alert.setHeaderText("Registration failed");
			alert.setContentText("The given horecanumber is already in use!");
			alert.showAndWait();
			return false;
		}

	}

	public static boolean login(String horecaName, String password) {
		try {
			fileName = Values.FILE_DIR + "barOwner_" + horecaName + ".csv";
			File file = new File(fileName);
			Scanner scanner = new Scanner(file);

			BarOwner.horecaName = scanner.nextLine();
			BarOwner.password = scanner.nextLine();

			if (!BarOwner.password.equals(password)) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Something went wrong!");
				alert.setHeaderText("Login failed");
				alert.setContentText("The name and password doesn't match!");
				alert.showAndWait();
				scanner.close();
				return false;
			}

			BarOwner.horecaNumber = scanner.nextLine();
			BarOwner.address = scanner.nextLine();

			Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
				@Override
				public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
					jsonWriter.value(localDate.toString());
				}

				@Override
				public LocalDate read(JsonReader jsonReader) throws IOException {
					return LocalDate.parse(jsonReader.nextString());
				}

			}).create();
			Type pseuMapType = new TypeToken<Map<LocalDate, byte[]>>() {
			}.getType();
			BarOwner.pseudonyms = gson.fromJson(scanner.nextLine(), pseuMapType);
			Type rndMapType = new TypeToken<HashMap<LocalDate, Integer>>() {
			}.getType();
			BarOwner.randomNumbers = gson.fromJson(scanner.nextLine(), rndMapType);

			scanner.close();
			return true;

		} catch (FileNotFoundException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Something went wrong!");
			alert.setHeaderText("Login failed");
			alert.setContentText("Plaese check the inputfields for errors!");
			alert.showAndWait();
			return false;
		}
	}

	public static String getHorecaName() {
		return horecaName;
	}

	public static String getHorecaNumber() {
		return horecaNumber;
	}

	public static byte[] getPseudonym(LocalDate ld) {
		if (!pseudonyms.containsKey(ld)) {
			try {
				Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
				RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
				Map<LocalDate, byte[]> ret = registrar.getPseudonyms(horecaNumber, password, ld);
				if (ret != null) {
					pseudonyms.putAll(ret);
					updateFile();
				}
			} catch (RemoteException | NotBoundException e) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Something went wrong!");
				alert.setHeaderText("Getting data failed!");
				alert.setContentText("Something is wrong with the registrar server, plaese try again later.");
				alert.showAndWait();
				System.exit(1);
			}
		}
		return pseudonyms.get(ld);
	}

	public static int getRandom(LocalDate ld) {
		if (!randomNumbers.containsKey(ld)) {
			randomNumbers.put(ld, new Random().nextInt());
			updateFile();
		}
		return randomNumbers.get(ld);
	}

	public static String[] getQRCode(LocalDate ld) { 
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			String random = Integer.toString(getRandom(ld));
			String pseudonym = Base64.getEncoder().encodeToString(getPseudonym(ld));
			String input = random + pseudonym;
			byte[] gehashed = md.digest(input.getBytes());
			String hash = Base64.getEncoder().encodeToString(gehashed);
			return new String[] { random, horecaName, hash };
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;

	}

	protected static void updateFile() {
		try {
			File dir = new File(Values.FILE_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			fileName = Values.FILE_DIR + "barOwner_" + horecaName + ".csv";
			File file = new File(fileName);
			file.createNewFile();
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(horecaName + System.lineSeparator());
			bw.write(password + System.lineSeparator()); // TODO: password/file encrypteren
			bw.write(horecaNumber + System.lineSeparator());
			bw.write(address + System.lineSeparator());
			Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
				@Override
				public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
					jsonWriter.value(localDate.toString());
				}

				@Override
				public LocalDate read(JsonReader jsonReader) throws IOException {
					return LocalDate.parse(jsonReader.nextString());
				}

			}).create();
			bw.write(gson.toJson(pseudonyms) + System.lineSeparator());
			bw.write(gson.toJson(randomNumbers) + System.lineSeparator());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
