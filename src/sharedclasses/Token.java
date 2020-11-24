package sharedclasses;

import java.io.Serializable;
import java.security.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Token implements Serializable {
	private static final long serialVersionUID = 1L;
	private byte[] token;
	private byte[] signature;
	//this is for visitors, servers will check their local db!
	private boolean used;
	
	public Token(byte[] token, byte[] signature) {
		this.token = token;
		this.signature = signature;
		this.used = false;
	}
	
	/** use the PublicKey of the Registrar to check whether it was a validly issued token.
	 * @param pubkey
	 * @return
	 */
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
	
	/** Check whether or not a Token was issued on a certain date. No RMI necessary. To be called by mixing proxy
	 * @param toCheck
	 * @return
	 */
	public boolean checkIssuedDate(LocalDate toCheck) {
		String toParse = new String(this.token);
		int year = Integer.parseInt(toParse.substring(0, 4));
		int month = Integer.parseInt(toParse.substring(5, 7));
		int day = Integer.parseInt(toParse.substring(8, 10));
		
		LocalDate issuedAt = LocalDate.of(year, month, day);
		return issuedAt.equals(toCheck);
	}
	
	/** create a signed token, 32 bytes of information ( = 256 bits = 1.15*10^77 possibilities), with 22 bytes randomised and 10 bytes day-bound.
	 * 	This leaves 2^(22*8) possibilities for each day = 9.6*10^55 possibilities.
	 * @param privkey: private key of the registrar
	 * @param sr: SecureRandom as created in the User-class (registrar-side)
	 * @return
	 */
	public static Token createToken(PrivateKey privkey, SecureRandom sr, LocalDate date) {
		try {
			byte[] ld = date.toString().getBytes();
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
	
	/** gets the first unused token in the list for use (this will mark the returned token as used)
	 * @param tokenList
	 * @return
	 */
	public static Token getFirstUnused(List<Token> tokenList) {
		Iterator<Token> it = tokenList.iterator();
		Token token;
		while(it.hasNext()) {
			token = it.next();
			if(!token.used) {
				return token;
			}
		}
		return null;
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
	
	/** Print a bit-representation of a token to the screen. Take note that since LocalDate is used first, the first part of each day is the same.
	 * 
	 */
	public void printTokenBitRepresentation() {
		System.out.println("token length: " + 8*this.token.length + " bits.");
		for(int i=0; i<this.token.length; i++) {
			System.out.print(String.format("%8s", Integer.toBinaryString(this.token[i] & 0xff)).replace(" ", "0"));
			if(i != (this.token.length - 1)) {
				System.out.print("|");
			}
		}
		System.out.println();
	}
}
