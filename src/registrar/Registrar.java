package registrar;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import values.Values;

public class Registrar {

	public static void main(String[] args) {
		Registrar registrar = new Registrar();
		registrar.startServer();
	}

	private void startServer() {
		try {			
			Registry registry = LocateRegistry.createRegistry(Values.REGISTRAR_PORT);
			registry.rebind(Values.REGISTRAR_SERVICE, new RegistrarImplementation());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("| Registrar is ready.");
	}

}
