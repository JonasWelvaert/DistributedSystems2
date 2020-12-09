package doctor;

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
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
import javafx.stage.Stage;
import matchingservice.MatchingServiceInterface;
import sharedclasses.Log;
import values.Values;

public class Doctor extends Application {

	private static int nrOfDoctor = 1;
	private static int nrOfUnsendPatients;
	private static List<SignedObject> logs;
	private static KeyPair keyPair;

	public static void main(String[] args) {
		try {
			File file = new File(Values.FILE_DIR + "Doctor_" + nrOfDoctor + ".csv");
			Scanner scanner = new Scanner(file);
			nrOfDoctor = Integer.parseInt(scanner.nextLine());
			nrOfUnsendPatients = Integer.parseInt(scanner.nextLine());
			// capsules opslaan
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
			Type lListType = new TypeToken<List<SignedObject>>() {
			}.getType();
			logs = gson.fromJson(scanner.nextLine(), lListType);

			// eigen KeyPair
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
			PrivateKey privkey = keyFactory.generatePrivate(privSpec);
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
			PublicKey pubkey = keyFactory.generatePublic(pubSpec);
			keyPair = new KeyPair(pubkey, privkey);

			// TODO info inlezen.

			scanner.close();
		} catch (FileNotFoundException e) {
			try {
				nrOfDoctor = 1;
				nrOfUnsendPatients = 0;
				logs = new ArrayList<SignedObject>();
				KeyPairGenerator kpg;
				kpg = KeyPairGenerator.getInstance("RSA");
				keyPair = kpg.generateKeyPair();
				//publish public key
				PublicKey pubkey = keyPair.getPublic();
				String s1 = Base64.getEncoder().encodeToString(pubkey.getEncoded());
				
				File file = new File(Values.FILE_DIR, "artsPublicKey.txt");
				FileWriter fw = new FileWriter(file);
				BufferedWriter writer = new BufferedWriter(fw);
				
				writer.write(s1);
				
				writer.flush();
				writer.close();
			} catch (NoSuchAlgorithmException | IOException e1) {
				e1.printStackTrace();
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			primaryStage.setTitle("Doctor's application");
			Parent root = FXMLLoader.load(getClass().getResource("/doctor/DoctorUI.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private static void updateFile() {
		try {
			File dir = new File(Values.FILE_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			String fileName = Values.FILE_DIR + "Doctor_" + nrOfDoctor + ".csv";
			File file = new File(fileName);
			file.createNewFile();
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(nrOfDoctor + System.lineSeparator());
			bw.write(nrOfUnsendPatients + System.lineSeparator());
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
			bw.write(gson.toJson(logs) + System.lineSeparator());

			// KeyPair
			bw.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) + System.lineSeparator());
			bw.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) + System.lineSeparator());

			// TODO: info wegschrijven
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static int getNrOfUnsendPatients() {
		return nrOfUnsendPatients;
	}

	public static int getNrOfDoctor() {
		return nrOfDoctor;
	}

	public static void receiveLogs(List<Log> logs) {
		try {
			 
			for (Log l : logs) {
				Signature signature = Signature.getInstance("SHA256withRSA");
				SignedObject signedObject = new SignedObject(l,keyPair.getPrivate(),signature);
				Doctor.logs.add(signedObject);
			}
			nrOfUnsendPatients++;
			updateFile();
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void submitLogs() {
		try {
			Registry registry;
			registry = LocateRegistry.getRegistry(Values.MATCHINGSERVICE_HOSTNAME, Values.MATCHINGSERVICE_PORT);
			MatchingServiceInterface matchingService = (MatchingServiceInterface) registry
					.lookup(Values.MATCHINGSERVICE_SERVICE);
			matchingService.submitLogs(logs);
			logs.clear();
			nrOfUnsendPatients = 0;
			updateFile();
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

	}

}
