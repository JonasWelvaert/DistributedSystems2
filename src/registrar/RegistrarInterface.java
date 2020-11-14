package registrar;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Stack;

public interface RegistrarInterface extends Remote {
	/**
	 * info over functie
	 * 
	 * @param details
	 * @return
	 */
	public List<byte[]> enrollHORECA(String horecaName, String horecaNumber, String phoneNumber, String password)
			throws RemoteException;

	/**
	 * functie die user in het systeem opneemt: eenmalig op te roepen
	 * 
	 * @param telefoonNummer
	 * @return success = null; failure: Exception with report
	 */
	public Exception enrollUser(String phoneNumber) throws RemoteException;

	public Stack<byte[]> retrieveTokens(String phoneNumber) throws RemoteException;

	public void addUnacknowledgedLogs(List<byte[]> unacknowledgedTokens) throws RemoteException;

	public List<String> getUnacknowledgedPhoneNumbers() throws RemoteException;
}