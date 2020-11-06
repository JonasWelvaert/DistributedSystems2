package mixingproxy;

import java.util.List;

public interface MixingProxyInterface {
	/**
	 * @param capsule
	 * @return ontvangen hash van de locatie, maar dan signed door de mixingproxy
	 */
	public byte[] registerVisit(Capsule capsule);
	
	public void acknowledge(List<byte[]> tokens);

}

class Capsule{
	
}