package registrar;

import java.io.BufferedWriter;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.KeyGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import barowner.BarOwner;
import javafx.application.Platform;
import sharedclasses.Capsule;
import sharedclasses.SystemException;
import sharedclasses.Token;
import values.Values;

public class RegistrarImplementation extends UnicastRemoteObject implements RegistrarInterface {

	private static final long serialVersionUID = 1L;
	private List<CateringFacility> cateringFacilitys;
	private List<User> users;
	private byte[] secretKey;
	private KeyPair keyPair;
	
	private transient static RegistrarImplementation impl;
	private transient static RegistrarController controller;

	public RegistrarImplementation() throws RemoteException, NoSuchAlgorithmException {
		RegistrarImplementation.impl = this;
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

			// KeyPair
			KeyFactory keyFactory = KeyFactory.getInstance("DSA");
			PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
			PrivateKey privkey = keyFactory.generatePrivate(privSpec);

			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
			PublicKey pubkey = keyFactory.generatePublic(pubSpec);

			keyPair = new KeyPair(pubkey, privkey);

			// info over de visitors
			Type userListType = new TypeToken<List<User>>() {
			}.getType();
			users = gson.fromJson(scanner.nextLine(), userListType);
			User.initialiseUserSystem(Values.CRITICAL_PERIOD_IN_DAYS, users);

			scanner.close();
		} catch (FileNotFoundException e) {
			// generate secretKey
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			secretKey = keyGen.generateKey().getEncoded();
			// generate KeyPair for signing
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
			keyPair = kpg.generateKeyPair();

			// initialise lists
			cateringFacilitys = new ArrayList<>();
			users = new ArrayList<>();
			User.initialiseUserSystem(Values.CRITICAL_PERIOD_IN_DAYS, users);
			// TODO: lege info
		} catch (InvalidKeySpecException e) {
			System.err.println("invalid keyspec for keyPair reading.");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private void updateFile() {
		try {
			File dir = new File(Values.FILE_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(Values.FILE_DIR + "registrar.csv");
			file.createNewFile();
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(file));

			bw.write(Base64.getEncoder().encodeToString(secretKey) + System.lineSeparator());

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
			bw.write(gson.toJson(cateringFacilitys) + System.lineSeparator());
			// KeyPair
			bw.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) + System.lineSeparator());
			bw.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) + System.lineSeparator());

			// visitors
			bw.write(gson.toJson(users) + System.lineSeparator());

			// TODO: info wegschrijven naar file.

			bw.flush();
			bw.close();
			
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
			    	controller.updateInfo(users.size(), cateringFacilitys.size());
			    }
			});
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
				addLog(LocalTime.now() + ": duplicate horeca registration rejected.");
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
		addLog(LocalTime.now() + ": " + horecaName + " enrolled with number " + horecaNumber + ".");
		return map;
	}

	public synchronized Map<LocalDate, byte[]> getPseudonyms(String horecaNumber, String password, LocalDate ld) {
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
		addLog(LocalTime.now() + ": catering " + horecaNumber + " got daily pseudonym.");
		return map;
	}

	@Override
	public synchronized void enrollUser(String phoneNumber) throws RemoteException, UserAlreadyRegisteredException {
		try {
			new User(phoneNumber); // Constructor does already
			updateFile();
			addLog(LocalTime.now() + ": user added with number " + phoneNumber);
		} catch (NotInitialisedException e) {
			System.err.println(
					"arrived in illegal state with 'enrollUser(String)-method', should never throw this error.");
			e.printStackTrace();
			throw new RemoteException(
					"Problem with Server System initialisation... -- call User.initialise(int) to fix.");
		} catch (UserAlreadyRegisteredException uare) {
			System.out.println("User already registered...");
			throw uare;
		}
	}

	@Override
	public synchronized Map<LocalDate, List<Token>> retrieveTokens(String phoneNumber)
			throws TokensAlreadyIssuedException, UserNotRegisteredException {
		User user = User.findUser(phoneNumber);
		if (user == null) {
			addLog(LocalTime.now() + ": unregistered user asked for tokens.");
			throw new UserNotRegisteredException(phoneNumber);
		}
		Map<LocalDate, List<Token>> tokensMap = new HashMap<>();
		List<Token> tokens = new ArrayList<Token>(48);
		SecureRandom rng = User.getRNG();
		if (rng == null) {
			System.out.println("System not initialised correctly -- returning null tokens.");
		}
		LocalDate date = LocalDate.now();
		for (int i = 0; i < 48; i++) {
			tokens.add(Token.createToken(this.keyPair.getPrivate(), rng, date));
		}
		
		tokensMap.put(date, tokens);
		// nu hebben we een lijst tokens aangemaakt & ondertekend. Deze moeten
		// geregistreerd worden bij de user.
		try {
			if (!User.addTokens(user, tokensMap)) {
				System.out.println(
						"Something went wrong finding the user in the User.addTokens-method. Called by retrieveTokens RMI method.");
			}
		} catch (TokensAlreadyIssuedException e) {
			addLog(LocalTime.now() + ": tokens for " + phoneNumber + " were already issued today.");
			throw e;
		}
		addLog(LocalTime.now() + ": daily allowance for " + phoneNumber + " added.");
		updateFile();
		return tokensMap;
	}

	@Override
	public synchronized void flushUnacknowledgedInfo(List<Capsule> unackList) throws SystemException {
		List<String> result = new ArrayList<>();
		for(Capsule caps: unackList) {
			Token toFind = caps.getUserToken();
			User user = User.identifyUser(toFind);
			//if null: error in system.
			if(user == null) {
				throw new SystemException("Unable to identify user.");
			}
			//identify cateringfacility
			byte[] cateringHash = caps.getHash();
			LocalDate ld = caps.getCurrentTime().toLocalDate();			

			CateringFacility catf = null;
			for(CateringFacility cf : this.cateringFacilitys) {
				String pseudonym = Base64.getEncoder().encodeToString(cf.getPseudonymForDate(ld));
				try {
					MessageDigest md = MessageDigest.getInstance("SHA-256");
					String random = Integer.toString(caps.getRandom());
					String input = random + pseudonym;
					byte[] gehashed = md.digest(input.getBytes());
					if(Arrays.equals(cateringHash, gehashed)) {
						catf = cf;
						break;
					}
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
			if(catf == null) {
				throw new SystemException("Unable to identify Catering Facility.");
			}

			
			//construct info-string
			StringBuilder sb = new StringBuilder();
			sb.append("User with phoneNumber ");
			sb.append(user.getPhoneNumber());
			sb.append(" was put at risk of infection on ");
			sb.append(caps.getCurrentTime().toLocalDate().toString());
			sb.append(" in catering facility ");
			sb.append(catf.getHorecaName());
			sb.append(".\n");
			result.add(sb.toString());
			
		}
		//create a file for this date
		try {
			File dir = new File(Values.FILE_DIR + "toNotify/");
			dir.mkdir();
			File file = new File(Values.FILE_DIR, "toNotify/" + LocalDate.now().toString() + ".txt");
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fw);
			
			for(String s: result) {
				writer.write(s);
			}
			writer.flush();
			writer.close();
			addLog(LocalTime.now() + ": unacknowledged capsules logged, location: ");
			addLog("\t\t" + Values.FILE_DIR + "toNotify/" + LocalDate.now() + ".txt");
		} catch (IOException e) {
			addLog(LocalTime.now() + ": failure to log unacknowledged capsules");
			e.printStackTrace();
		}
	}

	@Override
	public synchronized PublicKey getPublicKey() throws RemoteException {
		return keyPair.getPublic();
	}

	@Override
	public List<byte[]> getPseudonymsForDay(LocalDate date) throws RemoteException {
		List<byte[]> ret = new ArrayList<>();
		for (CateringFacility cf : cateringFacilitys) {
			ret.add(cf.getPseudonym(date, secretKey));
		}
		addLog(LocalTime.now() + ": pseudonym-list sent for day " + LocalDate.now() + ".");
		return ret;
	}

	@Override
	public byte[] getPseudonymAsInspector(String CF, LocalDate date) {
		for (CateringFacility cf : cateringFacilitys) {
			if (cf.getHorecaName().equals(CF)) {
				return cf.getPseudonym(date, secretKey);
			}
		}
		addLog(LocalTime.now() + ": inspector-request received.");
		return null;
	}
	
	public int getRandom(LocalDate ld) {
		Map<LocalDate, Integer> randomNumbers = BarOwner.getRandoms();
		if (!randomNumbers.containsKey(ld)) {
			randomNumbers.put(ld, new Random().nextInt());
			updateFile();
		}
		return randomNumbers.get(ld);
	}
	
	public static RegistrarImplementation getImpl() {
		return RegistrarImplementation.impl;
	}
	
	public static void setController(RegistrarController controller) {
		RegistrarImplementation.controller = controller;
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	controller.updateInfo(RegistrarImplementation.impl.users.size(), RegistrarImplementation.impl.cateringFacilitys.size());
		    }
		});
	}
	
	private void addLog(String log) {
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	RegistrarImplementation.controller.addLog(log);
		    }
		});
	}
}
