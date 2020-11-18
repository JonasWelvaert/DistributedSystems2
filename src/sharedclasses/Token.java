package sharedclasses;

import java.io.Serializable;
import java.security.*;
import java.time.LocalDate;
import java.util.Arrays;

import javax.security.*;

public class Token implements Serializable {
	private byte[] token;
	private byte[] signature;
	
	public Token(byte[] token, byte[] signature) {
		this.token = token;
		this.signature = signature;
	}
	
	public boolean checkSignature(PublicKey pubkey) {
		try {
			Signature sig = Signature.getInstance("SHA256withDSA");
			sig.initVerify(pubkey);
			sig.update(token);
			return sig.verify(signature);
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			System.out.println("error with checking signature -- Token.token.checkSignature(PublicKey)");
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean checkIssuedDate(LocalDate toCheck) {
		String toParse = new String(this.token);
		int year = Integer.parseInt(toParse.substring(0, 4));
		int month = Integer.parseInt(toParse.substring(5, 7));
		int day = Integer.parseInt(toParse.substring(8, 10));
		
		LocalDate issuedAt = LocalDate.of(year, month, day);
		return issuedAt.equals(toCheck);
	}
	
	public static Token createToken(PrivateKey privkey, SecureRandom sr) {
		try {
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
			Signature sig = Signature.getInstance("SHA256withDSA");
			sig.initSign(privkey);
			sig.update(preToken);
			return new Token(preToken, sig.sign());
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			System.err.println("Error with Token.createToken(PrivateKey, SecureRandom) -- nullToken was returned");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(token);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (!Arrays.equals(token, other.token))
			return false;
		return true;
	}
	
	
}
