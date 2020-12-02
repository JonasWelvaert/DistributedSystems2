package mixingproxy;

import java.io.Serializable;
import java.time.LocalDateTime;

import sharedclasses.Token;

public class Capsule implements Serializable {

	private static final long serialVersionUID = 1L;
	private LocalDateTime currentTime = null;
	private Token userToken = null;
	private byte[] hash = null;
	private byte[] sign = null;

	public void setCurrentTime(LocalDateTime currentTime) {
		this.currentTime = currentTime;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public void setUserToken(Token userToken) {
		this.userToken = userToken;
	}

	public LocalDateTime getCurrentTime() {
		return currentTime;
	}

	public byte[] getHash() {
		return hash;
	}

	public Token getUserToken() {
		return userToken;
	}
	
	public byte[] getSign() {
		return sign;
	}
	
	public void setSign(byte[] sign) {
		this.sign = sign;
	}

}
