package registrar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Stack;

public interface RegistrarInterface {
	/** info over functie
	 * @param details
	 * @return 
	 */
	public List<byte[]> enrollHORECA(BarOwnerDetails details);
	
	/** functie die user in het systeem opneemt: eenmalig op te roepen
	 * @param telefoonNummer
	 * @return success = null; failure: Exception with report
	 */
	public Exception enrollUser(String phoneNumber);
	
	public Stack<byte[]> retrieveTokens(String phoneNumber);
	
	public void addUnacknowledgedLogs(List<byte[]> unacknowledgedTokens);
	
	public List<String> getUnacknowledgedPhoneNumbers();
}

class BarOwnerDetails {
	
}