package doctor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DoctorInterface  extends Remote{
	//stuur al de geregistreerde logs
	public void releaseInfectedLogs(List<Capsule> capsules) throws RemoteException;
	
}

class Capsule{
	
}