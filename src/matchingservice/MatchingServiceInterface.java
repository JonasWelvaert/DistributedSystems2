package matchingservice;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchingServiceInterface {
	public void submitCapsules(List<Capsule> capsules);
	
	public void submitAcknowledgements(List<Capsule> capsules);
	
	public List<Tuple> requestInfectedCapsules();
	
	//temp
	public void submitLogs(List<byte[]> medicalLogs);
}

class Capsule{
	
}

class Tuple{
	Capsule capsule;
	Interval interval;
}

class Interval{
	LocalDateTime from;
	LocalDateTime to;
}
