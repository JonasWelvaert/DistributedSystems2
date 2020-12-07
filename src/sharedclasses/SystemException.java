package sharedclasses;

public class SystemException extends Exception {
	public SystemException() {
		super("fault with system detected.");
	}
	
	public SystemException(String message) {
		super("fault with system detected: " + message);
	}
}
