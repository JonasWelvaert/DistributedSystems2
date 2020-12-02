package matchingservice;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import values.Values;

public class MatchingService {

	public static void main(String[] args) {
		MatchingService matchingService = new MatchingService();
		matchingService.startServer();
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
		System.out.println("| Matching Servide is ready.");

	}
}
