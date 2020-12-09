package sharedclasses;

public class SystemException extends Exception {
	private static final long serialVersionUID = 1L;

	public SystemException() {
		super("fault with system detected.");
	}
	
	public SystemException(String message) {
		super("fault with system detected: " + message);
	}
}
