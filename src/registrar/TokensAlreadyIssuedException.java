package registrar;

public class TokensAlreadyIssuedException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TokensAlreadyIssuedException() {
		super();
	}
	
	public TokensAlreadyIssuedException(String phoneNumber) {
		super("tokens for user with phone number " + phoneNumber + " were already issued today.");
	}
}
