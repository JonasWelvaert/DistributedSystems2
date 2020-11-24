package mixingproxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import registrar.RegistrarInterface;
import rmissl.RMISSLClientSocketFactory;
import rmissl.RMISSLServerSocketFactory;
import values.Values;

public class MixingProxyImplementation extends UnicastRemoteObject implements MixingProxyInterface {

	private static final long serialVersionUID = 1L;
	private PublicKey registrarPubK;
	private List<Capsule> capsules;

	protected MixingProxyImplementation() throws Exception {
		super(Values.MIXINGPROXY_PORT, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory());
		try {
			File file = new File(Values.FILE_DIR + "mixingproxy.csv");
			Scanner scanner = new Scanner(file);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA");
			X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(scanner.nextLine()));
			registrarPubK = keyFactory.generatePublic(pubSpec);
			// TODO info inlezen.

			scanner.close();
		} catch (FileNotFoundException e) {
			Registry myRegistry = LocateRegistry.getRegistry(Values.REGISTRAR_HOSTNAME, Values.REGISTRAR_PORT,
					new RMISSLClientSocketFactory());
			RegistrarInterface registrar = (RegistrarInterface) myRegistry.lookup(Values.REGISTRAR_SERVICE);
			registrarPubK = registrar.getPublicKey();
			// TODO info init.

		}
	}

	private void updateFile() {
		try {
			File file = new File(Values.FILE_DIR + "registrar.csv");
			file.createNewFile();
			FileWriter fw;
			fw = new FileWriter(file);
			fw.write(Base64.getEncoder().encodeToString(registrarPubK.getEncoded()) + System.lineSeparator());
			// TODO: info wegschrijven naar file. vergeet lineseperator niet.

			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("error in updateFile()-method.");
			e.printStackTrace();
		}
	}

	@Override
	public byte[] registerVisit(Capsule capsule) {
		if (!capsule.getUserToken().checkSignature(registrarPubK)) {
			return null;
		}
		if(!capsule.getUserToken().checkIssuedDate(capsule.getCurrentTime().toLocalDate())) {
			return null;
		}
		for(Capsule c: capsules) {
			if(c.getUserToken().equals(capsule.getUserToken())){
				return null;
			}
		}
		
		// TODO sign hash & send back to user as confirmation.
		// log the capsule

		// in other method: pushing capsules randomly to matching service.
		return null;
	}

	@Override
	public void acknowledge(List<byte[]> tokens) {
		// TODO Auto-generated method stub

	}

}
