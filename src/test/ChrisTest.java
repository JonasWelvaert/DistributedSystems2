package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.*;
import javax.xml.bind.DatatypeConverter;

import sharedclasses.Token;

public class ChrisTest {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		//intToByteArrayTest();
		//SecureRandom sr = new SecureRandom();
		//createTokenTest(sr);
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(LocalDate.now().toString() + ".txt"));
			writer.write("testing 1 2 3");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void intToByteArrayTest() {
		SecureRandom sr;
		try {
			sr = new SecureRandom();
			
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			KeyPair keyPair = kpg.generateKeyPair();
			
			byte[] token = ChrisTest.generateUnsigned32ByteToken(sr);
			
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(keyPair.getPrivate());
			sig.update(token);
			byte[] signedToken = sig.sign();
			
			sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(keyPair.getPublic());
			//de originele token moet gekend zijn, anders is het niet mogelijk een signature op locatie te controleren.
			//dit betekent dat de mixingproxy effectief
			sig.update(token);
			System.out.println(sig.verify(signedToken));
			
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] generateUnsigned32ByteToken(SecureRandom sr) {
		byte[] ld = LocalDate.now().toString().getBytes();
		byte[] random = new byte[22];
		sr.nextBytes(random);
		byte[] preToken = new byte[32];
		for(int i=0; i<10; i++) {
			preToken[i] = ld[i];
		}
		for(int i=10; i<32; i++) {
			preToken[i] = random[i-10];
		}
		//---> new String(byte[]) stelt ons in staat om de datum te lezen!
		System.out.println(new String(preToken));
		//--> handig om interne elementen te vergelijken
		System.out.println(Arrays.toString(preToken));
		//geen vreemde tekens, ook best handig.
		System.out.println(DatatypeConverter.printHexBinary(preToken));
		return preToken;
	}
	
	public static void tokenMethodTest(SecureRandom sr) {
		try {
			byte[] unsignedToken = ChrisTest.generateUnsigned32ByteToken(sr);
			
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
			KeyPair keyPair = kpg.generateKeyPair();
			
			Signature sig = Signature.getInstance("SHA256withDSA");
			sig.initSign(keyPair.getPrivate());
			sig.update(unsignedToken);
			byte[] signature = sig.sign();
			
			sig = Signature.getInstance("SHA256withDSA");
			sig.initVerify(keyPair.getPublic());
			sig.update(unsignedToken);
			System.out.println(sig.verify(signature));
			
			Token token = new Token(unsignedToken, signature);
			
			System.out.println("checking signature: "+token.checkSignature(keyPair.getPublic()));
			System.out.println("checking correct date: " + token.checkIssuedDate(LocalDate.now()));
			System.out.println("checking false date: " + token.checkIssuedDate(LocalDate.of(2019, 1, 1)));
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
		}
	}
	
	public static void createTokenTest(SecureRandom sr) {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
			KeyPair keyPair = kpg.generateKeyPair();
			
			Token token = Token.createToken(keyPair.getPrivate(), sr, LocalDate.now());
			System.out.println("checking signature: "+token.checkSignature(keyPair.getPublic()));
			System.out.println("checking correct date: " + token.checkIssuedDate(LocalDate.now()));
			System.out.println("checking false date: " + token.checkIssuedDate(LocalDate.of(2019, 1, 1)));
			
			token.printTokenBitRepresentation();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}
