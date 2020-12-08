package matchingservice;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import values.Values;
import visitor.Visitor;

public class MatchingService extends Application{

	public static void main(String[] args) {
		launch(args);	
	}

	private void startServer() {

		Registry registry = null;
		try {
			registry = LocateRegistry.createRegistry(Values.MATCHINGSERVICE_PORT);
		} catch (RemoteException e) {
			try {
				registry = LocateRegistry.getRegistry(Values.MATCHINGSERVICE_PORT);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
		try {
			registry.rebind(Values.MATCHINGSERVICE_SERVICE, new MatchingServiceImplementation());
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * try { new MixingProxyImplementation(); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */
		System.out.println("| Matching Service listening at port " + Values.MATCHINGSERVICE_PORT);
	}

	@Override
	public void start(Stage primaryStage) {
		MatchingService matchingService = new MatchingService();
		matchingService.startServer();
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/matchingservice/MatchingService.fxml"));
			Scene scene = new Scene(root);

			primaryStage.setTitle("MatchingService Server UI");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
