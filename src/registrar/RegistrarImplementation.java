package registrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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

import sharedclasses.Token;
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
			try {
				KeyFactory keyFactory = KeyFactory.getInstance("DSA");
				PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
				PrivateKey privkey = keyFactory.generatePrivate(privSpec);
				
				X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
				PublicKey pubkey = keyFactory.generatePublic(pubSpec);
				
				keyPair = new KeyPair(pubkey, privkey);
			} catch (InvalidKeySpecException e) {
				System.err.println("invalid keyspec for keyPair reading.");
				e.printStackTrace();
			}
			
			//info over de visitors
			Type userListType = new TypeToken<List<User>>() {
			}.getType();
			users = gson.fromJson(scanner.nextLine(), userListType);
			User.initialiseUserSystem(Values.CRITICAL_PERIOD_IN_DAYS, users);
			User.getUserList().forEach(user -> {
				System.out.println(user);
			});
			// TODO: info uit file.

			scanner.close();
		} catch (FileNotFoundException e) {
			//generate secretKey
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			secretKey = keyGen.generateKey().getEncoded();
			//generate KeyPair for signing
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
			keyPair = kpg.generateKeyPair();
			
			//initialise lists
			cateringFacilitys = new ArrayList<>();
			users = new ArrayList<>();
			User.initialiseUserSystem(Values.CRITICAL_PERIOD_IN_DAYS, users);
			// TODO: lege info
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
			fw.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) + System.lineSeparator());
			fw.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())  + System.lineSeparator());
			
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
			updateFile();
		} catch (NotInitialisedException e) {
			System.err.println("arrived in illegal state with 'enrollUser(String)-method', should never throw this error.");
			e.printStackTrace();
			throw new RemoteException("Problem with system initialisation... -- call User.initialise(int) to fix.");
		} catch (UserAlreadyRegisteredException uare) {
			System.out.println("User already registered...");
			return false;
		}
		return true;
	}

	@Override
	public synchronized List<Token> retrieveTokens(String phoneNumber) throws TokensAlreadyIssuedException, UserNotRegisteredException {
		User user = User.findUser(phoneNumber);
		if(user == null) {
			throw new UserNotRegisteredException(phoneNumber);
		}
		List<Token> tokens= new ArrayList<Token>(48);
		SecureRandom rng = User.getRNG();
		if(rng == null) {
			System.out.println("System not initialised correctly -- returning null tokens.");
		}
		for(int i=0; i<48; i++) {
			tokens.add(Token.createToken(this.keyPair.getPrivate(), rng));
		}
		//nu hebben we een lijst tokens aangemaakt & ondertekend. Deze moeten geregistreerd worden bij de user.
		if(!User.addTokens(user, tokens)) {
			System.out.println("Something went wrong finding the user in the User.addTokens-method. Called by retrieveTokens RMI method.");
		}
		updateFile();
		return tokens;
	}

	@Override
	public synchronized void addUnacknowledgedLogs(List<Token> unacknowledgedTokens) {
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
