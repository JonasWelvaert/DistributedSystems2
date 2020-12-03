package matchingservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import mixingproxy.Capsule;
import sharedclasses.Log;
import values.Values;

public class MatchingServiceImplementation extends UnicastRemoteObject implements MatchingServiceInterface {

	private static final long serialVersionUID = 1L;
	private List<Capsule> capsules;

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

			// TODO: info uit file.

			scanner.close();
		} catch (FileNotFoundException e) {
			capsules = new ArrayList<Capsule>();
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

			// TODO: info wegschrijven naar file.

			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.err.println("error in updateFile()-method.");
			e.printStackTrace();
		}
		
		//TODO plannen om capsules na X(=?14) dagen te verwijderen.
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
	public void submitLogs(List<Log> medicalLogs) {
		// TODO Auto-generated method stub

	}

}
