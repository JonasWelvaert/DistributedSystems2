package matchingservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.SignedObject;
import java.time.LocalDateTime;
import java.util.List;

import sharedclasses.Capsule;
import sharedclasses.Log;
import sharedclasses.Token;
import sharedclasses.Tuple;

public interface MatchingServiceInterface extends Remote {
	public void submitCapsules(List<Capsule> capsules) throws RemoteException;

	public void submitAcknowledgements(List<Token> tokens) throws RemoteException;

	public List<Tuple> requestCriticalIntervals() throws RemoteException;

	// temp
	public void submitLogs(List<SignedObject> medicalLogs) throws RemoteException;
}

class Interval {
	LocalDateTime from;
	LocalDateTime to;
}
