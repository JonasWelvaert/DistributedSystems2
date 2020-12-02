package test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import mixingproxy.MixingProxy;
import registrar.Registrar;

public class Test {

	public static void main(String[] args) {
		//testSignature();
		new Thread(()-> Registrar.main(args)).run();
		new Thread(()-> MixingProxy.main(args)).run();
		
		
	}
	
	private static void testSignature() {
		try {
			//use of RSA because of DSA generates an different signature each time.
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			KeyPair keyPair = kpg.generateKeyPair();
			
			byte[] toBeSigned = "Hello World!".getBytes();
			
			Signature sig = Signature.getInstance("SHA256WithRSA");
			sig.initSign(keyPair.getPrivate());
			sig.update(toBeSigned);
			byte [] sign1 = sig.sign();
			
			sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(keyPair.getPrivate());
			sig.update(toBeSigned);
			byte [] sign2 = sig.sign();
			
			System.out.println(Base64.getEncoder().encodeToString(sign1));
			System.out.println(Base64.getEncoder().encodeToString(sign2));
			
			
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			System.err.println("Sign bytes will be null: if this appears: Fix it!");
		}
	}
}
