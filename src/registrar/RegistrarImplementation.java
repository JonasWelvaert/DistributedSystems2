package registrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import javax.crypto.KeyGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import values.Values;
import visitor.Visitor;

public class RegistrarImplementation extends UnicastRemoteObject implements RegistrarInterface {

	private static final long serialVersionUID = 1L;
	private List<CateringFacility> cateringFacilitys;
	private List<User> users;
	private byte[] secretKey;
	private KeyPair keyPair;

	public RegistrarImplementation() throws RemoteException, NoSuchAlgorithmException {
		try {
			File file = new File(Values.FILE_DIR + "registrar.csv");
			Scanner scanner = new Scanner(file);

			secretKey = Base64.getDecoder().decode(scanner.nextLine());

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
			Type cfListType = new TypeToken<List<CateringFacility>>() {
			}.getType();
			cateringFacilitys = gson.fromJson(scanner.nextLine(), cfListType);
			
			//KeyPair
			keyPair = gson.fromJson(scanner.nextLine(), KeyPair.class);
			
			//info over de visitors
			Type userListType = new TypeToken<List<User>>() {
			}.getType();
			users = gson.fromJson(scanner.nextLine(), userListType);
			
			// TODO: info uit file.

			scanner.close();
		} catch (FileNotFoundException e) {
			//generate secretKey
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			secretKey = keyGen.generateKey().getEncoded();
			//generate KeyPair for signing
			// TODO: decide algorithm used: await email Naessens
			
			//initialise lists
			cateringFacilitys = new ArrayList<>();
			users = new ArrayList<>();
			// TODO: lege info
		} finally {
			User.initialiseUserSystem(Values.CRITICAL_PERIOD_IN_DAYS);
			try {
				users = User.getUserList();
			} catch (NotInitialisedException e) {
				System.err.println("Startup of registrar error: this should never happen.");
				e.printStackTrace();
			}
		}

	}

	private void updateFile() {
		try {
			File file = new File(Values.FILE_DIR + "registrar.csv");
			file.createNewFile();
			FileWriter fw;
			fw = new FileWriter(file);

			fw.write(Base64.getEncoder().encodeToString(secretKey) + System.lineSeparator());

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
			fw.write(gson.toJson(cateringFacilitys) + System.lineSeparator());
			
			//KeyPair
			fw.write(gson.toJson(keyPair) + System.lineSeparator());
			
			//visitors
			fw.write(gson.toJson(users) + System.lineSeparator());

			// TODO: info wegschrijven naar file.

			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("error in updateFile()-method.");
			e.printStackTrace();
		}
	}

	@Override
	public synchronized Map<LocalDate, byte[]> enrollHORECA(String horecaName, String horecaNumber, String address,
			String password) throws HorecaNumberAlreadyEnrolledException {
		for (CateringFacility cf : cateringFacilitys) {
			if (cf.hasHorecaNumber(horecaNumber)) {
				throw new HorecaNumberAlreadyEnrolledException();
			}
		}
		CateringFacility cf = new CateringFacility(horecaName, horecaNumber, address, password);
		cateringFacilitys.add(cf);
		Map<LocalDate, byte[]> map = new HashMap<>();
		// TODO: upgrade towards week/month keys
		LocalDate d = LocalDate.now();
		map.put(d, cf.getPseudonym(d, secretKey));
		updateFile();
		return map;
	}

	public Map<LocalDate, byte[]> getPseudonyms(String horecaNumber, String password, LocalDate ld) {
		CateringFacility cateringfacility = null;
		for (CateringFacility cf : cateringFacilitys) {
			if (cf.hasHorecaNumber(horecaNumber) && cf.isCorrectPassword(password)) {
				cateringfacility = cf;
				break;
			}
		}
		Map<LocalDate, byte[]> map = new HashMap<>();
		// TODO: upgrade towards week/month keys
		map.put(ld, cateringfacility.getPseudonym(ld, secretKey));
		updateFile();
		return map;
	}

	@Override
	public synchronized boolean enrollUser(String phoneNumber) throws RemoteException{
		try {
			User user = new User(phoneNumber);
		} catch (NotInitialisedException e) {
			System.err.println("arrived in illegal state with 'enrollUser(String)-method', should never throw this error.");
			e.printStackTrace();
			throw new RemoteException("Problem with system initialisation... -- call User.initialise(int) to fix.");
		} catch (UserAlreadyRegisteredException uare) {
			return false;
		}
		return true;
	}

	@Override
	public synchronized Stack<byte[]> retrieveTokens(String phoneNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized void addUnacknowledgedLogs(List<byte[]> unacknowledgedTokens) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized List<String> getUnacknowledgedPhoneNumbers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized PublicKey getPublicKey() throws RemoteException {
		return keyPair.getPublic();
	}

}
