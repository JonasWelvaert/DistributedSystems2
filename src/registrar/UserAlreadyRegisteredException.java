package registrar;

public class UserAlreadyRegisteredException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserAlreadyRegisteredException() {
		super("User is already registered!");
	}
	
	public UserAlreadyRegisteredException(String phoneNumber) {
		super("User with phone number " + phoneNumber + " is already registered.");
	}
	
	public UserAlreadyRegisteredException(User user) {
		this(user.getPhoneNumber());
	}
}
