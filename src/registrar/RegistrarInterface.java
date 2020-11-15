package registrar;

import java.security.*;
import javax.security.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public interface RegistrarInterface extends Remote {
	/**
	 * info over functie
	 * 
	 * @param details
	 * @return
	 * @throws HorecaNumberAlreadyEnrolledException
	 */
	public Map<LocalDate, byte[]> enrollHORECA(String horecaName, String horecaNumber, String phoneNumber,
			String password) throws RemoteException, HorecaNumberAlreadyEnrolledException;

	/**
	 * functie die user in het systeem opneemt: eenmalig op te roepen
	 * 
	 * @param telefoonNummer
	 * @return success = true, false if already registered
	 */
	public boolean enrollUser(String phoneNumber) throws RemoteException;

	public PublicKey getPublicKey() throws RemoteException;
	
	public Stack<byte[]> retrieveTokens(String phoneNumber) throws RemoteException, UserNotRegisteredException;

	public void addUnacknowledgedLogs(List<byte[]> unacknowledgedTokens) throws RemoteException;

	public List<String> getUnacknowledgedPhoneNumbers() throws RemoteException;

	public Map<LocalDate, byte[]> getPseudonyms(String horecaNumber, String password, LocalDate ld)
			throws RemoteException;
}