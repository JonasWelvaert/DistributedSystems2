package registrar;

public class NotInitialisedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotInitialisedException() {
		super();
	}
	
	public NotInitialisedException(String s) {
		super(s + " was not correctly initialised.");
	}
}
