package sharedclasses;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Capsule implements Serializable {

	private static final long serialVersionUID = 1L;
	private LocalDateTime currentTime = null;
	private Token userToken = null;
	private byte[] hash = null;
	private byte[] sign = null;
	private boolean critical = false;
	private boolean informed = false;
	private Integer random = null;

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

	public boolean isCritical() {
		return critical;
	}
	
	public boolean isInformed() {
		return informed;
	}
	
	public void setCritical(boolean critical) {
		this.critical = critical;
	}
	
	public void setInformed(boolean informed) {
		this.informed = informed;
	}

	public Integer getRandom() {
		return random;
	}

	public void setRandom(Integer random) {
		this.random = random;
	}
}
