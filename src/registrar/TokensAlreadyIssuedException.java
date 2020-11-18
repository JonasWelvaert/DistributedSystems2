package registrar;

public class TokensAlreadyIssuedException extends Exception{
	public TokensAlreadyIssuedException() {
		super();
	}
	
	public TokensAlreadyIssuedException(String phoneNumber) {
		super("tokens for user with phone number " + phoneNumber + " were already issued today.");
	}
}
