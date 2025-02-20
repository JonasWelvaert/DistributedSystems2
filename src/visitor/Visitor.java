package visitor;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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
import matchingservice.MatchingServiceInterface;
import mixingproxy.MixingProxyInterface;
import registrar.RegistrarInterface;
import registrar.TokensAlreadyIssuedException;
import registrar.UserAlreadyRegisteredException;
import registrar.UserNotRegisteredException;
import sharedclasses.Capsule;
import sharedclasses.Log;
import sharedclasses.Token;
import sharedclasses.Tuple;
import values.Values;

public class Visitor extends Application {
	private static String fileName;
	private static visitor.User user;
	private static Map<LocalDate, List<Token>> issuedTokens = new HashMap<LocalDate, List<Token>>();
	private static List<Log> logs = new ArrayList<>();
	private static Log lastLog = null;
	
	private static List<String> pastAlerts = new ArrayList<>();

	private static Stage primaryStage;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			Visitor.primaryStage = primaryStage;
			Parent root = FXMLLoader.load(getClass().getResource("/visitor/VisitorRegister.fxml"));
			Scene scene = new Scene(root);

			primaryStage.setTitle("Visitor's application");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void openVisitorHomeGUI() {
		try {
			Parent root = FXMLLoader.load(Visitor.class.getResource("/visitor/VisitorHome.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setTitle(Visitor.user.getPhoneNr());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean enrollUser(String phoneNumber) throws UserAlreadyRegisteredException {
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			registrar.enrollUser(phoneNumber);
			return true;
		} catch (RemoteException | NotBoundException re) {
			re.printStackTrace();
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Registration failed!");
			alert.setHeaderText("Registration failed.");
			alert.setContentText("Please contact the app developers, Internal Server Error detected.");
			alert.showAndWait();
			return false;
		}
	}

	public static Map<LocalDate, List<Token>> getTokenAllocation(String phoneNumber)
			throws TokensAlreadyIssuedException {
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			try {
				return registrar.retrieveTokens(phoneNumber);
			} catch (UserNotRegisteredException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("User identification error.");
				alert.setHeaderText("Server failed to identify user.");
				alert.setContentText(
						"A server issue was encountered identifying the user. Please contact a server admin. System will exit...");
				alert.showAndWait();
				return null;
			}
		} catch (RemoteException | NotBoundException e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Token allocation failed!");
			alert.setHeaderText("Token allocation failed.");
			alert.setContentText("Please contact the app developers, Internal Server Error detected.");
			alert.showAndWait();
			System.err.println("error connecting to the registrar");
			e.printStackTrace();
			return null;
		}
	}

	public static boolean register(String name, String phoneNumber, String password) {
		try {
			if (enrollUser(phoneNumber)) {
				// if this is succesfull, it's a newly registered account and a .csv should be
				// constructed; return true so GUI knows that a new window should be opened.
				// if this returns false, server is not available: do not notify failure to
				// report: an alert is already fired
				// --> just reset the UI: return false?
				// if user is already registered, this will throw an error.
				// -- hindsight: this might've been better the other way around?
				Visitor.fileName = Values.FILE_DIR + "Visitor_" + phoneNumber + ".csv";
				Visitor.user = new User(name, password, phoneNumber);
				updateFile();
				return true;
			} else {
				// in this case return false: error is handled, we just need to let UI know to
				// reset fields
				return false;
			}
		} catch (UserAlreadyRegisteredException e) {
			// reset fields == return false
			System.out.println("Registration not succesful: user already registered.");
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Registration Info");
			alert.setHeaderText("Duplicate Registration attempt.");
			alert.setContentText("A user with this phone number is already registered. Try logging in instead.");
			alert.showAndWait();
			return false;
		}
	}

	public static boolean login(String phoneNumber, String password) {
		try {
			fileName = Values.FILE_DIR + "Visitor_" + phoneNumber + ".csv";
			File file = new File(fileName);

			Scanner sc = new Scanner(file);
			Visitor.user = new User(sc.nextLine(), sc.nextLine(), sc.nextLine());

			// return false if the user exists but the password is wrong
			if (!Visitor.user.getPassw().equals(password)) {
				sc.close();
				System.out.println("attempted pass: " + password);
				System.out.println("logged pass: " + Visitor.user.getPassw());
				return false;
			}

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
			Type tokenMapType = new TypeToken<Map<LocalDate, List<Token>>>() {
			}.getType();
			Visitor.issuedTokens = gson.fromJson(sc.nextLine(), tokenMapType);

			// capsules opslaan
			Type lListType = new TypeToken<List<Log>>() {
			}.getType();
			logs = gson.fromJson(sc.nextLine(), lListType);
			
			//alert record
			Type alertListType = new TypeToken<List<String>>() {
			}.getType();
			Visitor.pastAlerts = gson.fromJson(sc.nextLine(), alertListType);
			
			sc.close();
			return true;
		} catch (FileNotFoundException e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Login failed!");
			alert.setHeaderText("Login failed.");
			alert.setContentText("You first have to register your phonenumber before logging in.");
			alert.showAndWait();
			System.out.println("file not found...");
			e.printStackTrace();
			return false;
		}
	}

	public static void updateFile() {
		try {
			File dir = new File(Values.FILE_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(fileName);
			file.createNewFile();
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(file));

			bw.write(Visitor.user.getUsn() + System.lineSeparator());
			bw.write(Visitor.user.getPassw() + System.lineSeparator());
			bw.write(Visitor.user.getPhoneNr() + System.lineSeparator());

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

			bw.write(gson.toJson(Visitor.issuedTokens) + System.lineSeparator());
			bw.write(gson.toJson(Visitor.logs) + System.lineSeparator());
			bw.write(gson.toJson(Visitor.pastAlerts) + System.lineSeparator());

			bw.flush();
			bw.close();
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Data storing failed!");
			alert.setHeaderText("Data storing failed.");
			alert.setContentText("Please contact the app developers, Internal Server Error detected.");
			alert.showAndWait();
			e.printStackTrace();
		}
	}

	public static User getUser() {
		return Visitor.user;
	}

	public static Map<LocalDate, List<Token>> getIssuedTokens() {
		return Visitor.issuedTokens;
	}

	public static byte[] registerVisit(String random, String barname, String hash, LocalDateTime entryTime) {
		try {
			Registry myRegistry = LocateRegistry.getRegistry(Values.MIXINGPROXY_HOSTNAME,
					Values.MIXINGPROXY_PORT/*
											 * , new SslRMIClientSocketFactory()
											 */);
			MixingProxyInterface mixingProxy = (MixingProxyInterface) myRegistry.lookup(Values.MIXINGPROXY_SERVICE);

			List<Token> tokens = issuedTokens.get(entryTime.toLocalDate());
			if (tokens == null || tokens.size() == 0) {
				try {
					issuedTokens.putAll(Visitor.getTokenAllocation(user.getPhoneNr()));
				} catch (TokensAlreadyIssuedException e) {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Info");
					alert.setHeaderText("You can't register again at a horeca.");
					alert.setContentText("You've already visited to much horeca today. Try again tomorrow!");
					alert.showAndWait();
					return null;
				}
			}
			tokens = issuedTokens.get(entryTime.toLocalDate());
			Token token = Token.getFirstUnused(tokens);

			Capsule capsule = new Capsule();
			capsule.setCurrentTime(entryTime);
			capsule.setHash(Base64.getDecoder().decode(hash));
			capsule.setUserToken(token);
			capsule.setRandom(Integer.parseInt(random));
			byte[] signedHash = mixingProxy.registerVisit(capsule);
			if (signedHash != null) {
				capsule.setSign(signedHash);
				token.setUsed(true);
				Log log = new Log();
				log.setRandom(Integer.parseInt(random));
				log.setHash(Base64.getDecoder().decode(hash));
				log.setToken(token);
				log.setBarname(barname);
				log.setStartTime(capsule.getCurrentTime());
				log.setEndTime(capsule.getCurrentTime().plusMinutes(30));
				logs.add(log);
				lastLog = log;

				// elke 30 min nieuwe token sturen totdat endVisit geklikt.
				Date date = Date.from(lastLog.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (lastLog == log) {
							registerVisit(random, barname, hash, log.getEndTime());
						}
						this.cancel();
					}
				}, date, TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES));

			} else {
				System.out.println("Visitor || Mixing Proxy did not sign: null received.");
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Registration failed!");
				alert.setHeaderText("Could not register visit.");
				alert.setContentText("Failed to connect to the server. Please try again");
				alert.showAndWait();
				return null;
			}
			updateFile();
			return signedHash;
		} catch (RemoteException | NotBoundException e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Registrar failed!");
			alert.setHeaderText("Registrar failed.");
			alert.setContentText("Please contact the app developers, Internal Server Error detected.");
			alert.showAndWait();
			e.printStackTrace();
			return null;
		}
	}

	public static void endVisit() {
		LocalDateTime now = LocalDateTime.now();
		lastLog.setEndTime(now);
		lastLog = null;
		updateFile();
	}

	public static List<Log> getLogs() {
		List<Log> ret = new ArrayList<>();
		LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(Values.CRITICAL_PERIOD_IN_DAYS);
		for (Log l : logs) {
			if (l.getStartTime().isAfter(fourteenDaysAgo)) {
				ret.add(l);
			}
		}
		return ret;
	}

	public static void fetchCriticalIntervalsAndCompare() {
		try {
			//MatchingService Registry
			Registry matchingRegistry = LocateRegistry.getRegistry(Values.MATCHINGSERVICE_HOSTNAME, Values.MATCHINGSERVICE_PORT);
			MatchingServiceInterface matchingService = (MatchingServiceInterface) matchingRegistry.lookup(Values.MATCHINGSERVICE_SERVICE);
			//MixingProxy Registry
			Registry mixingRegistry = LocateRegistry.getRegistry(Values.MIXINGPROXY_HOSTNAME,Values.MIXINGPROXY_PORT);
			MixingProxyInterface mixingProxy = (MixingProxyInterface) mixingRegistry.lookup(Values.MIXINGPROXY_SERVICE);
			
			List<Tuple> criticalIntervals = matchingService.requestCriticalIntervals();			
			//fetch own logs to compare [within critical interval]
			List<Log> ownLogs = getLogs();
			if(criticalIntervals == null || ownLogs == null) {
				System.out.println("A null-variable was discovered. No reason to continue method...");
				return;
			}
			
			//For each critical interval received:
				//find all logs that match the interval
				//if so, add token to list & display alert
				//if any matches were found, notify the mixingproxy with all tokens.
			List<Token> tokensToSend = new ArrayList<>();
			for(Tuple criticalInterval: criticalIntervals) {
				boolean match = false;
				String cateringName = null;
				for(Log log: ownLogs) {
					if(criticalInterval.contains(log)) {
						if(!tokensToSend.contains(log.getToken())) {
							tokensToSend.add(log.getToken());
							cateringName = log.getBarname();
							match = true;
						}
					}
				}
				if(match) {
					//display an alert & notify mixingproxy
					StringBuilder sb = new StringBuilder();
					sb.append("Possible infection on ");
					sb.append(criticalInterval.dateToString());
					sb.append(" at catering facility ");
					sb.append(cateringName);
					sb.append("\n");
					
					if (!pastAlerts.contains(sb.toString())) {
						pastAlerts.add(sb.toString());
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Risk detected!");
						alert.setHeaderText("Possible infection detected!");
						alert.setContentText(sb.toString());
						alert.showAndWait();
					}
				}
			}
			
			if (!tokensToSend.isEmpty()) {
				//only send if any matches were found
				mixingProxy.acknowledge(tokensToSend);
			}
			
			updateFile();
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public static List<String> getAlerts() {
		return Visitor.pastAlerts;
	}
}

class User {
	private String usn;
	private String passw;
	private String phoneNr;

	public String getUsn() {
		return usn;
	}

	public void setUsn(String usn) {
		this.usn = usn;
	}

	public String getPassw() {
		return passw;
	}

	public void setPassw(String passw) {
		this.passw = passw;
	}

	public String getPhoneNr() {
		return phoneNr;
	}

	public void setPhoneNr(String phoneNr) {
		this.phoneNr = phoneNr;
	}

	public User(String usn, String passw, String phoneNr) {
		super();
		this.usn = usn;
		this.passw = passw;
		this.phoneNr = phoneNr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((phoneNr == null) ? 0 : phoneNr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (phoneNr == null) {
			if (other.phoneNr != null)
				return false;
		} else if (!phoneNr.equals(other.phoneNr))
			return false;
		return true;
	}

}
