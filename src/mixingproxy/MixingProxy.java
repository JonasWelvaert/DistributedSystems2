package mixingproxy;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import values.Values;

public class MixingProxy {

	public static void main(String[] args) {
		MixingProxy mixingProxy = new MixingProxy();
		mixingProxy.startServer();
	}
	
	private void startServer() {
		try {			
			Registry registry = LocateRegistry.createRegistry(Values.MIXINGPROXY_PORT);
			registry.rebind(Values.MIXINGPROXY_SERVICE, new MixingProxyImplementation());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("| Mixing Proxy is ready.");
	}

}
