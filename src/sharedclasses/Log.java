package sharedclasses;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Log implements Serializable {
	private static final long serialVersionUID = 4206033057993352885L;
	private LocalDateTime startTime= null;
	private LocalDateTime endTime = null;
	private Token token = null;
	private byte[] hash = null;
	private int random;
	private byte[] doctorSign = null;
	private transient String barname = null;

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public int getRandom() {
		return random;
	}

	public void setRandom(int random) {
		this.random = random;
	}
	
	public byte[] getDoctorSign() {
		return doctorSign;
	}
	
	public void setDoctorSign(byte[] doctorSign) {
		this.doctorSign = doctorSign;
	}
	
	public String getBarname() {
		return barname;
	}
	
	public void setBarname(String barname) {
		this.barname = barname;
	}
	
	

}
