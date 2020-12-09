package mixingproxy;

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
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import javafx.application.Platform;
import matchingservice.MatchingServiceInterface;
import registrar.RegistrarInterface;
import sharedclasses.Capsule;
import sharedclasses.Token;
import values.Values;

public class MixingProxyImplementation extends UnicastRemoteObject implements MixingProxyInterface {

	private static final long serialVersionUID = 1L;
	private PublicKey registrarPubK;
	private List<Capsule> capsules = new ArrayList<>();
	private KeyPair keyPair;
	
	private transient static MixingProxyController controller;
	private transient static MixingProxyImplementation impl;

	protected MixingProxyImplementation() throws RemoteException {
		MixingProxyImplementation.impl = this;
		try {
			File file = new File(Values.FILE_DIR + "mixingproxy.csv");
			Scanner scanner = new Scanner(file);

			// eigen KeyPair
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
			PrivateKey privkey = keyFactory.generatePrivate(privSpec);
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
			PublicKey pubkey = keyFactory.generatePublic(pubSpec);
			keyPair = new KeyPair(pubkey, privkey);

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
			Type cListType = new TypeToken<List<Capsule>>() {
			}.getType();
			capsules = gson.fromJson(scanner.nextLine(), cListType);
			// TODO info inlezen.

			scanner.close();
		} catch (FileNotFoundException e) {
			try {
				Registry myRegistry;
				myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
				RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
				registrarPubK = registrar.getPublicKey();

				// generate KeyPair for signing
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				keyPair = kpg.generateKeyPair();
				//
				capsules = new ArrayList<Capsule>();
				// TODO info init.
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			} catch (NotBoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		// Registrar public key
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			registrarPubK = registrar.getPublicKey();
		} catch (NotBoundException e1) {
			e1.printStackTrace();
		}

		// flusing capsules to matchingservice, everyday 3AM:
		//disabled for presentation
		/*
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 3);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendCapsulesToMatchingService();
			}
		}, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		*/
	}

	private void updateFile() {
		try {
			File dir = new File(Values.FILE_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(Values.FILE_DIR + "mixingproxy.csv");
			file.createNewFile();
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(file));
			// KeyPair
			bw.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) + System.lineSeparator());
			bw.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) + System.lineSeparator());

			// capsules
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
			bw.write(gson.toJson(capsules) + System.lineSeparator());

			// TODO: info wegschrijven naar file. vergeet lineseperator niet.

			bw.flush();
			bw.close();
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
			    	controller.updateInfo(capsules.size());
			    }
			});
		} catch (IOException e) {
			System.err.println("error in updateFile()-method.");
			e.printStackTrace();
		}
	}

	@Override
	public byte[] registerVisit(Capsule capsule) {
		byte[] sign = null;
		if (!capsule.getUserToken().checkSignature(registrarPubK)) {
			System.out.println("MixingProxy || registervisit | signature error.");
			return sign;
		}
		// Jonas: Hier gebruik ik de MixingProxy time zodat visitor Eve niet kan liegen
		// over de datum.
		// LocalDate date = capsule.getCurrentTime().toLocalDate();
		LocalDate date = LocalDate.now();
		if (!capsule.getUserToken().checkIssuedDate(date)) {
			System.out.println("MixingProxy || registervisit | date-check failed.");
			return sign;
		}
		for (Capsule c : capsules) {
			if (c.getUserToken().equals(capsule.getUserToken())) {
				System.out.println("MixingProxy || registervisit | Capsule duplication.");
				return sign;
			}
		}
		sign = sign(capsule.getHash());
		if (sign != null) {
			capsules.add(capsule);
			updateFile();
			addLog(LocalDateTime.now().toLocalTime() + ": capsule received to mark visit; location:");
			addLog("\t\t" + capsule.getHash());
			return sign;
		}
		System.out.println("MixingProxy || registervisit | Signing failed.");
		return null;
	}

	@Override
	public void acknowledge(List<Token> tokens) {
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.MATCHINGSERVICE_HOSTNAME, Values.MATCHINGSERVICE_PORT);
			MatchingServiceInterface matchingService = (MatchingServiceInterface) myRegistry.lookup(Values.MATCHINGSERVICE_SERVICE);	
			matchingService.submitAcknowledgements(tokens);
			addLog(LocalDateTime.now().toLocalTime() + ": " + tokens.size() + " tokens passed through for acknowledgement.");
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	private byte[] sign(byte[] toBeSigned) {
		byte[] sign = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(keyPair.getPrivate());
			sig.update(toBeSigned);
			sign = sig.sign();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			System.err.println("Sign bytes will be null: if this appears: Fix it!");
		}
		return sign;

	}

	public synchronized void sendCapsulesToMatchingService() {
		try {
			Registry myRegistry;
			myRegistry = LocateRegistry.getRegistry(Values.MATCHINGSERVICE_HOSTNAME, Values.MATCHINGSERVICE_PORT);
			MatchingServiceInterface matchingService = (MatchingServiceInterface) myRegistry
					.lookup(Values.MATCHINGSERVICE_SERVICE);
			matchingService.submitCapsules(capsules);
			capsules.clear();
			updateFile();
			addLog(LocalDateTime.now().toLocalTime() + ": queue flushed to Matching Server");
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	private static void addLog(String log) {
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	MixingProxyImplementation.controller.addLog(log);
		    }
		});
	}
	
	public static void setController(MixingProxyController controller) {
		MixingProxyImplementation.controller = controller;
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	controller.updateInfo(MixingProxyImplementation.impl.capsules.size());
		    }
		});
	}
	
	public static MixingProxyImplementation getImpl() {
		return MixingProxyImplementation.impl;
	}
}
