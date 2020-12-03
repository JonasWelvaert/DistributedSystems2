package doctor;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import matchingservice.MatchingServiceInterface;
import sharedclasses.Log;
import values.Values;

public class Doctor extends Application {

	private static Stage primaryStage;
	private static int nrOfDoctor = 123;
	private static int nrOfUnsendPatients = 0;
	private static List<Log> logs;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			Doctor.primaryStage = primaryStage;
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

	public static int getNrOfUnsendPatients() {
		return nrOfUnsendPatients;
	}

	public static int getNrOfDoctor() {
		return nrOfDoctor;
	}
	
	public static void receiveLogs(List<Log> logs) {
		Doctor.logs.addAll(logs);
		nrOfUnsendPatients++;
	}

	public static void submitLogs() {
		try {
			Registry registry;
			registry = LocateRegistry.getRegistry(Values.MATCHINGSERVICE_HOSTNAME, Values.MATCHINGSERVICE_PORT);
			MatchingServiceInterface matchingService = (MatchingServiceInterface) registry.lookup(Values.MATCHINGSERVICE_SERVICE);
			matchingService.submitLogs(logs);
			// TODO logs submitten
			
			nrOfUnsendPatients = 0;
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		
	}
}
