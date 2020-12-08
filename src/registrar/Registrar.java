package registrar;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import values.Values;

public class Registrar extends Application{

	public static void main(String[] args) {
		launch(args);
	}

	private void startServer() {
		try {			
			Registry registry = LocateRegistry.createRegistry(Values.REGISTRAR_PORT);
			registry.rebind(Values.REGISTRAR_SERVICE, new RegistrarImplementation());
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		try {
			new RegistrarImplementation();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}*/
		System.out.println("| Registrar listening at port " + Values.REGISTRAR_PORT);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Registrar registrar = new Registrar();
		registrar.startServer();
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/registrar/Registrar.fxml"));
			Scene scene = new Scene(root);

			primaryStage.setTitle("Registrar Server UI");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
