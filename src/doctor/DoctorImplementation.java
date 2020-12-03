package doctor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class DoctorImplementation extends UnicastRemoteObject implements DoctorInterface {

	protected DoctorImplementation() throws RemoteException {
		//files lezen
		
		
		
		
	}

	private static final long serialVersionUID = 4400389729600207097L;

	@Override
	public void releaseInfectedLogs(List<Capsule> capsules) throws RemoteException {
		// TODO Auto-generated method stub

	}

}
