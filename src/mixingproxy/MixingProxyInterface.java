package mixingproxy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import sharedclasses.Capsule;
import sharedclasses.Token;

public interface MixingProxyInterface extends Remote {
	/**
	 * @param capsule
	 * @return ontvangen hash van de locatie, maar dan signed door de mixingproxy
	 */
	public byte[] registerVisit(Capsule capsule) throws RemoteException;
	
	public void acknowledge(List<Token> tokens) throws RemoteException;

}