package mixingproxy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MixingProxyInterface extends Remote {
	/**
	 * @param capsule
	 * @return ontvangen hash van de locatie, maar dan signed door de mixingproxy
	 */
	public byte[] registerVisit(Capsule capsule) throws RemoteException;
	
	public void acknowledge(List<byte[]> tokens) throws RemoteException;

}