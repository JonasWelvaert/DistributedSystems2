package mixingproxy;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import values.Values;

public class MixingProxy {

	public static void main(String[] args) {
		MixingProxy mixingProxy = new MixingProxy();
		mixingProxy.startServer();
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
		System.out.println("| Mixing Proxy is ready.");
	}

}
