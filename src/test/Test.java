package test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Test {
	private Map<LocalDate, SecretKey> secretKeys;
	private String horecaNumber;
	private String address;

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
		Test test = new Test();
		test.secretKeys = new HashMap<>();
		test.horecaNumber = "0697601";
		test.address = "Bassevelde";
		Test test2 = new Test();
		test2.secretKeys = new HashMap<>();
		test.horecaNumber = "0697601";
		test.address = "Bassevelde";

		LocalDate d1 = LocalDate.now();
		System.out.println(d1);
		LocalDate d5 = LocalDate.of(2020, 11, 15);
		System.out.println(d5);
		LocalDate d6 = LocalDate.of(1999, 5, 10);
		System.out.println(d6);
		System.out.println(d5.equals(d1));
		System.out.println(d5 == d1);
		System.out.println(d1.equals(d6));
		
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		SecretKey s = keyGen.generateKey();
		byte[] sk = s.getEncoded();
		
		
		String text = Base64.getEncoder().encodeToString(sk);
		System.out.println(text);

		byte[] sk2 = Base64.getDecoder().decode(text);
		
		System.out.println(Base64.getEncoder().encodeToString(sk));
		System.out.println(Base64.getEncoder().encodeToString(sk2));
		
		
		for (int i = 0; i < 2; i++) {
			byte[] pseudonym = test.getPseudonym(d5, s);
			System.out.println(Base64.getEncoder().encodeToString(pseudonym));
		}
		for (int i = 0; i < 2; i++) {
			byte[] pseudonym = test.getPseudonym(d6, s);
			System.out.println(Base64.getEncoder().encodeToString(pseudonym));
		}
		System.out.println(test.secretKeys.size());

	}

	private SecretKey getSecretKey(LocalDate date, SecretKey s)
			throws InvalidKeySpecException, NoSuchAlgorithmException {

		if (secretKeys.containsKey(date)) {
			return secretKeys.get(date);
		} else {
			byte[] salt = new byte[32];
			new SecureRandom().nextBytes(salt);
			String input = Base64.getEncoder().encodeToString(s.getEncoded()) + horecaNumber + date.toString();
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec keySpec = new PBEKeySpec(input.toCharArray(), salt, 1024, 32 * 8);
			SecretKey sCF = keyFactory.generateSecret(keySpec);
			secretKeys.put(date, sCF);
			return sCF;
		}
	}

	public byte[] getPseudonym(LocalDate date, SecretKey s) throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKey sCF = getSecretKey(date, s);
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String input = Base64.getEncoder().encodeToString(sCF.getEncoded()) + address + date.toString();
		byte[] pseudonym = md.digest(input.getBytes());
		return pseudonym;
	}
}
