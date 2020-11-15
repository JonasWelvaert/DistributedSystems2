package registrar;

public class NotInitialisedException extends Exception {
	public NotInitialisedException() {
		super();
	}
	
	public NotInitialisedException(String s) {
		super(s + " was not correctly initialised.");
	}
}
