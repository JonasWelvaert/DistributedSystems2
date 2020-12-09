package mixingproxy;

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

public class MixingProxy extends Application{

	public static void main(String[] args) {
		launch(args);
	}
	
	private void startServer() {
		
			Registry registry = null;
			try {
				registry = LocateRegistry.createRegistry(Values.MIXINGPROXY_PORT);
			} catch (RemoteException e) {
				try {
					registry = LocateRegistry.getRegistry(Values.MIXINGPROXY_PORT);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
			try {
				registry.rebind(Values.MIXINGPROXY_SERVICE, new MixingProxyImplementation());
			} catch (AccessException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		/*
		try {
			new MixingProxyImplementation();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		System.out.println("| Mixing Proxy listening at port " + Values.MIXINGPROXY_PORT);
	}

	@Override
	public void start(Stage primaryStage) {
		MixingProxy mixingProxy = new MixingProxy();
		mixingProxy.startServer();
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/mixingproxy/MixingProxy.fxml"));
			Scene scene = new Scene(root);

			primaryStage.setTitle("MixingProxy Server UI");
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
