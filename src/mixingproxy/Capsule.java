package mixingproxy;

import java.time.LocalDateTime;

import sharedclasses.Token;

public class Capsule {

	private LocalDateTime currentTime;
	private Token userToken;
	private String hash;

	public void setCurrentTime(LocalDateTime currentTime) {
		this.currentTime = currentTime;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setUserToken(Token userToken) {
		this.userToken = userToken;
	}

	public LocalDateTime getCurrentTime() {
		return currentTime;
	}

	public String getHash() {
		return hash;
	}

	public Token getUserToken() {
		return userToken;
	}

}
