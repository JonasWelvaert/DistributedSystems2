package registrar;

public class UserNotRegisteredException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserNotRegisteredException() {
		super();
	}
	
	public UserNotRegisteredException(String phoneNumber) {
		super("User with number " + phoneNumber + " was not found in the database.");
	}
}
