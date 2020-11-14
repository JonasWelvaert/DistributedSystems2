package registrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import values.Values;

public class RegistrarImplementation extends UnicastRemoteObject implements RegistrarInterface {

	private static final long serialVersionUID = 1L;

	public RegistrarImplementation() throws RemoteException {
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
	public synchronized List<byte[]> enrollHORECA(String horecaName, String horecaNumber, String phoneNumber,
			String password) {
		// TODO Auto-generated method stub
		
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
