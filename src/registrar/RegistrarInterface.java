package registrar;

import java.security.*;

import sharedclasses.Token;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
	public void enrollUser(String phoneNumber) throws RemoteException, UserAlreadyRegisteredException;

	/**
	 * @return PublicKey pubkey: wordt gebruikt voor controle van signed tokens
	 * @throws RemoteException
	 */
	public PublicKey getPublicKey() throws RemoteException;
	
	/** get a list of Token objects, signed by the registrar. Also, for users, this should be put on a stack/in a queue to make sure they aren't used already...
	 * @param phoneNumber
	 * @return
	 * @throws RemoteException
	 * @throws UserNotRegisteredException
	 */
	public Map<LocalDate, List<Token>> retrieveTokens(String phoneNumber) throws RemoteException, UserNotRegisteredException, TokensAlreadyIssuedException;

	public void addUnacknowledgedLogs(List<Token> unacknowledgedTokens) throws RemoteException;

	public List<String> getUnacknowledgedPhoneNumbers() throws RemoteException;

	public Map<LocalDate, byte[]> getPseudonyms(String horecaNumber, String password, LocalDate ld)
			throws RemoteException;
}