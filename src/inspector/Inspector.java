package inspector;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;

import barowner.BarOwner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import registrar.RegistrarInterface;
import values.Values;

public class Inspector extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			primaryStage.setTitle("Inspector's application");
			Parent root = FXMLLoader.load(getClass().getResource("/inspector/Inspector.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static boolean validateQRCode(String qrCode) {
		try {
			Registry registry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT);
			RegistrarInterface registrar = (RegistrarInterface) registry.lookup(Values.REGISTRAR_SERVICE);
			LocalDate date = LocalDate.now();
			if(qrCode.length()<3) {
				return false;
			}
			String s1 = qrCode.substring(1, qrCode.length() - 1);// qrcode without [ and ]
			String[] s2 = s1.split(", "); // 0: random, 1: name, 2: hash
			if(s2.length!=3) {
				return false;
			}
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			String random = s2[0];
			byte[] pseudonym = registrar.getPseudonymAsInspector(s2[1], date);
			if (pseudonym != null) {
				String input = random + Base64.getEncoder().encodeToString(pseudonym);
				byte[] gehashed = md.digest(input.getBytes());
				System.out.println(s2[2]);
				System.out.println(Base64.getEncoder().encodeToString(gehashed));
				if (Arrays.equals(gehashed, Base64.getDecoder().decode(s2[2]))) {
					return true;
				}
			} 
		} catch (RemoteException | NotBoundException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(ArrayIndexOutOfBoundsException aioobe) {
			return false;
		}
		return false;
	}

}
