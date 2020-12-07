package registrar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class CateringFacility {
	private Map<LocalDate, byte[]> secretKeys;
	private String horecaName;
	private String horecaNumber;
	private String address;
	private String password;
	
	private Map<LocalDate, byte[]> pseudonyms;

	public CateringFacility(String horecaName, String horecaNumber, String address, String password) {
		secretKeys = new HashMap<>();
		this.horecaName = horecaName;
		this.horecaNumber = horecaNumber;
		this.address = address;
		this.password = password;
		this.pseudonyms = new HashMap<>();
	}

	private byte[] getSecretKey(LocalDate date, byte[] s) {
		if (secretKeys.containsKey(date)) {
			return secretKeys.get(date);
		} else {
			SecretKey sCF = null;
			try {
				byte[] salt = new byte[32];
				new SecureRandom().nextBytes(salt);
				String input = Base64.getEncoder().encodeToString(s) + horecaNumber + date.toString();
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				PBEKeySpec keySpec = new PBEKeySpec(input.toCharArray(), salt, 1024, 32 * 8);

				sCF = keyFactory.generateSecret(keySpec);
			} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			secretKeys.put(date, sCF.getEncoded());
			return sCF.getEncoded();
		}
	}

	public byte[] getPseudonym(LocalDate date, byte[] s) {
		byte[] pseudonym = null;
		try {
			byte[] sCF = getSecretKey(date, s);
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-256");
			String input = Base64.getEncoder().encodeToString(sCF) + address + date.toString();
			pseudonym = md.digest(input.getBytes());
			
			this.pseudonyms.put(date, pseudonym);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return pseudonym;
	}

	public boolean hasHorecaNumber(String horecaNumber) {
		return this.horecaNumber.equals(horecaNumber);
	}

	public boolean hasHorecaName(String horecaName) {
		return this.horecaName.equals(horecaName);
	}

	public boolean isCorrectPassword(String password) {
		return this.password.equals(password);
	}
	
	public String getHorecaName() {
		return horecaName;
	}

	public byte[] getPseudonymForDate(LocalDate date) {
		return this.pseudonyms.get(date);
	}
}
