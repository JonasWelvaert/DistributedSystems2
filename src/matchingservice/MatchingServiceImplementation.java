package matchingservice;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javafx.application.Platform;
import registrar.RegistrarInterface;
import sharedclasses.Capsule;
import sharedclasses.Log;
import sharedclasses.SystemException;
import sharedclasses.Token;
import sharedclasses.Tuple;
import values.Values;

public class MatchingServiceImplementation extends UnicastRemoteObject implements MatchingServiceInterface {

	private static final long serialVersionUID = 1L;
	private List<Capsule> capsules;
	private Map<LocalDate, List<byte[]>> pseudonyms; // Voorlopig mag CF name weggelaten worden voor privacy concerns.
	private Map<LocalDate, List<Tuple>> criticalIntervals;
	
	private transient static MatchingServiceController controller;
	private transient static MatchingServiceImplementation impl;

	protected MatchingServiceImplementation() throws RemoteException {
		try {
			MatchingServiceImplementation.impl = this;
			File file = new File(Values.FILE_DIR + "matchingservice.csv");
			Scanner scanner = new Scanner(file);

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

			Type mapType = new TypeToken<Map<LocalDate, List<byte[]>>>() {
			}.getType();

			pseudonyms = gson.fromJson(scanner.nextLine(), mapType);
			// TODO: info uit file.

			Type critType = new TypeToken<Map<LocalDate, List<Tuple>>>(){
			}.getType();
			criticalIntervals = gson.fromJson(scanner.nextLine(), critType);
			
			scanner.close();
		} catch (FileNotFoundException e) {
			capsules = new ArrayList<Capsule>();
			pseudonyms = new HashMap<LocalDate, List<byte[]>>();
			criticalIntervals = new HashMap<LocalDate, List<Tuple>>();
			// TODO: lege info
		}
	}

	private void updateFile() {
		try {
			File dir = new File(Values.FILE_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(Values.FILE_DIR + "matchingservice.csv");
			file.createNewFile();
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(file));

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
			bw.write(gson.toJson(pseudonyms) + System.lineSeparator());
			bw.write(gson.toJson(criticalIntervals) + System.lineSeparator());
			// TODO: info wegschrijven naar file.

			bw.flush();
			bw.close();
			
			Platform.runLater(new Runnable() {
			    @Override
			    public void run() {
			    	controller.updateInfo(capsules);
			    }
			});
		} catch (IOException e) {
			System.err.println("error in updateFile()-method.");
			e.printStackTrace();
		}
	}

	@Override
	public void submitCapsules(List<Capsule> capsules) {
		this.capsules.addAll(capsules);
		updateFile();
		addLog(LocalTime.now() + ": capsules submitted by Mixing Proxy.");
	}

	@Override
	public void submitAcknowledgements(List<Token> tokens) {
		for(Capsule caps: this.capsules) {
			if(tokens.contains(caps.getUserToken())) {
				caps.setInformed(true);
			}
		}
		updateFile();
		addLog(LocalTime.now() + ": acknowledgements received.");
	}
	
	//also reset the capsules-list to a new ArrayList?
	public void flushUnackedCapsules() {
		List<Capsule> toFlush = new ArrayList<>();
		for(Capsule caps: capsules) {
			if(caps.isCritical() && !caps.isInformed()) {
				toFlush.add(caps);
				caps.setInformed(true);
			}
		}
		try {
			Registry registry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) registry.lookup(Values.REGISTRAR_SERVICE);
			
			registrar.flushUnacknowledgedInfo(toFlush);
			toFlush.forEach(caps -> {
				caps.setInformed(true);
			});
			updateFile();
			this.criticalIntervals = new HashMap<LocalDate, List<Tuple>>();
			addLog(LocalTime.now() + ": flushing to registrar...");
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			System.err.println("Error with system detected. Critical bug with identification.");
			System.err.println("");
			e.printStackTrace();
		}
	}

	@Override
	public List<Tuple> requestCriticalIntervals() {
		List<Tuple> result = new ArrayList<>();
		for(int i=0; i<Values.CRITICAL_PERIOD_IN_DAYS; i++) {
			LocalDate date = LocalDate.now().minusDays(i);
			List<Tuple> criticalOfDay = this.criticalIntervals.get(date);
			if(criticalOfDay == null) {
				continue;
			}
			criticalOfDay.forEach(element -> {
				if(!result.contains(element)) {
					result.add(element);
				}
			});
		}
		addLog(LocalTime.now() + ": Critical intervals request acknowledged.");
		return result;
	}

	@Override
	public void submitLogs(List<SignedObject> medicalLogs) {
		// TODO check if signed by doctor.
		try {
			for (SignedObject so : medicalLogs) {
				//check signature
				Signature signature = Signature.getInstance("SHA256withRSA");
				if(!so.verify(getArtsPublicKey(), signature)) {
					//"blacklist de spammer" & doe niets verder
					System.out.println("Signature verification failed -- doctor imposter found.");
					addLog(LocalTime.now() + ": false logs received from possible imposter.");
					return;
				}
				//do stuff
				Log l = (Log) so.getObject();
				if (!pseudonyms.containsKey(l.getStartTime().toLocalDate())) {
					Registry registry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
					RegistrarInterface registrar = (RegistrarInterface) registry.lookup(Values.REGISTRAR_SERVICE);
					List<byte[]> nyms = registrar.getPseudonymsForDay(l.getStartTime().toLocalDate());
					pseudonyms.put(l.getEndTime().toLocalDate(), nyms);
				}
				byte[] hash = l.getHash();
				int random = l.getRandom();
				LocalDateTime startTime = l.getStartTime();
				LocalDateTime endTime = l.getEndTime();
				LocalDate date = startTime.toLocalDate();
				Token token = l.getToken();
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				boolean validHash = false;
				for (byte[] pseudEntry : pseudonyms.get(date)) {
					String input = random + Base64.getEncoder().encodeToString(pseudEntry);
					byte[] gehashed = md.digest(input.getBytes());
					if (Arrays.equals(gehashed, hash)) {
						validHash = true;
						break;
					}
				}
				System.out.println("Logs received from doctor.");
				//construct the Tuple to put in criticalIntervals-list
				if(this.criticalIntervals.get(LocalDate.now()) == null) {
					this.criticalIntervals.put(LocalDate.now(), new ArrayList<>());
				}
				List<Tuple> criticalLogs = this.criticalIntervals.get(LocalDate.now());
				Tuple tup = new Tuple(l.getHash(), l.getStartTime(), l.getEndTime());
				//check if there is an overlap with a previously constructed Tuple, if so fix the list
				addTuple(tup, criticalLogs);
				
				// begintijd [begin; eind[
				if (validHash) {
					for (Capsule c : this.capsules) {
						if ((c.getCurrentTime().isAfter(startTime) && c.getCurrentTime().isBefore(endTime)) || c.getCurrentTime().isEqual(startTime)) {
							if (Arrays.equals(c.getHash(),hash)) {
								c.setCritical(true);
								c.setInformed(false);
								if (c.getUserToken().equals(token)) {
									c.setInformed(true);
								}
							}
						}
					}
				}
			}
			updateFile();
			addLog(LocalTime.now() + ": logs received from doctor.");
		} catch (NotBoundException | NoSuchAlgorithmException | ClassNotFoundException | IOException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}

	}

	//fixes the entire tuple list so all overlapping with the new tuple are unified.
	private static void addTuple(Tuple tup, List<Tuple> tupList) {
		Tuple toMatch = tup;
		for(int i=0; i<tupList.size(); i++) {
			boolean usefulIteration = false;
			Iterator<Tuple> listit = tupList.iterator();
			while(listit.hasNext()) {
				Tuple thisTup = listit.next();
				if(Tuple.haveOverlap(tup, thisTup)) {
					listit.remove();
					toMatch = Tuple.fixOverlap(tup, thisTup);
					usefulIteration = true;
					break;
				}
			}
			if(!usefulIteration) {
				break;
			}
		}
		tupList.add(toMatch);
	}
	
	private PublicKey getArtsPublicKey() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(Values.FILE_DIR, "artsPublicKey.txt")));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(reader.readLine()));
			reader.close();
			return keyFactory.generatePublic(pubSpec);
		} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setController(MatchingServiceController controller) {
		MatchingServiceImplementation.controller = controller;	
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	controller.updateInfo(MatchingServiceImplementation.impl.capsules);
		    }
		});
	}
	
	public static MatchingServiceImplementation getImpl() {
		return MatchingServiceImplementation.impl;
	}
	
	public static void addLog(String log) {
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	MatchingServiceImplementation.controller.addLog(log);
		    }
		});
	}
}
