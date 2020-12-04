package matchingservice;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignedObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import registrar.RegistrarInterface;
import sharedclasses.Capsule;
import sharedclasses.Log;
import sharedclasses.Token;
import values.Values;

public class MatchingServiceImplementation extends UnicastRemoteObject implements MatchingServiceInterface {

	private static final long serialVersionUID = 1L;
	private List<Capsule> capsules;
	private Map<LocalDate, List<byte[]>> pseudonyms; // Voorlopig mag CF name weggelaten worden voor privacy concerns.

	protected MatchingServiceImplementation() throws RemoteException {
		try {
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

			scanner.close();
		} catch (FileNotFoundException e) {
			capsules = new ArrayList<Capsule>();
			pseudonyms = new HashMap<LocalDate, List<byte[]>>();
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
			// TODO: info wegschrijven naar file.

			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.err.println("error in updateFile()-method.");
			e.printStackTrace();
		}

		// TODO plannen om capsules na X(=?14) dagen te verwijderen.
	}

	@Override
	public void submitCapsules(List<Capsule> capsules) {
		this.capsules.addAll(capsules);
		updateFile();
	}

	@Override
	public void submitAcknowledgements(List<Capsule> capsules) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Tuple> requestInfectedCapsules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void submitLogs(List<SignedObject> medicalLogs) {
		// TODO check if signed by doctor.
		try {
			for (SignedObject so : medicalLogs) {
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
					System.out.println(Base64.getEncoder().encodeToString(gehashed));
					System.out.println(Base64.getEncoder().encodeToString(hash));
					if (Arrays.equals(gehashed, hash)) {
						validHash = true;
						break;
					}
				}
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
		} catch (NotBoundException | NoSuchAlgorithmException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

	}

}
