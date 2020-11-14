package registrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import values.Values;

public class RegistrarImplementation extends UnicastRemoteObject implements RegistrarInterface {

	private static final long serialVersionUID = 1L;
	private List<CateringFacility> cateringFacilitys;
	private SecretKey s;

	public RegistrarImplementation() throws RemoteException, NoSuchAlgorithmException {
		File file = new File(Values.FILE_DIR + "registrar.csv");
		if (file.exists()) {
			try {
				Scanner scanner = new Scanner(file);
				// TODO: info uit file halen.

				scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			s = keyGen.generateKey();
			cateringFacilitys = new ArrayList<>();
			// TODO: lege info
		}
	}

	@Override
	protected void finalize() throws Throwable { // wordt opgeroepen door garbagecollector
		File file = new File(Values.FILE_DIR + "registrar.csv");
		FileWriter fw = new FileWriter(file);
		// TODO: info wegschrijven naar file.

		fw.flush();
		fw.close();
		super.finalize();
	}

	@Override
	public synchronized List<byte[]> enrollHORECA(String horecaName, String horecaNumber, String address,
			String password) {
		CateringFacility cf = new CateringFacility(horecaName, horecaNumber, address, password);
		
		return new ArrayList<byte[]>();
	}

	@Override
	public synchronized Exception enrollUser(String phoneNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized Stack<byte[]> retrieveTokens(String phoneNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized void addUnacknowledgedLogs(List<byte[]> unacknowledgedTokens) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized List<String> getUnacknowledgedPhoneNumbers() {
		// TODO Auto-generated method stub
		return null;
	}

}
