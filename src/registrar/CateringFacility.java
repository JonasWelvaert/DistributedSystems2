package registrar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class CateringFacility {
	private Map<LocalDate, SecretKey> secretKeys;
	private String horecaName;
	private String horecaNumber;
	private String address;
	private String password;

	public CateringFacility(String horecaName, String horecaNumber, String address, String password) {
		secretKeys = new HashMap<>();
		this.horecaName = horecaName;
		this.horecaNumber = horecaNumber;
		this.address = address;
		this.password = password;
	}

	public byte[] getPseudonym(LocalDate date, SecretKey s) throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKey sCF;
		if (secretKeys.containsKey(date)) {
			sCF = secretKeys.get(date);
		} else {
			String input = Base64.getEncoder().encodeToString(s.getEncoded()) + horecaNumber + date.toString();
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec keySpec = new PBEKeySpec(input.toCharArray());
			sCF = keyFactory.generateSecret(keySpec);
			secretKeys.put(date, sCF);
		}
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String input = Base64.getEncoder().encodeToString(s.getEncoded()) + address + date.toString();
		byte[] pseudonym = md.digest(input.getBytes());
		return pseudonym;
	}

}
