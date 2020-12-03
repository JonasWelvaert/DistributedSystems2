package matchingservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;

import mixingproxy.Capsule;
import sharedclasses.Log;

public interface MatchingServiceInterface extends Remote {
	public void submitCapsules(List<Capsule> capsules) throws RemoteException;

	public void submitAcknowledgements(List<Capsule> capsules) throws RemoteException;

	public List<Tuple> requestInfectedCapsules() throws RemoteException;

	// temp
	public void submitLogs(List<Log> medicalLogs) throws RemoteException;
}


class Tuple {
	Capsule capsule;
	Interval interval;
}

class Interval {
	LocalDateTime from;
	LocalDateTime to;
}
