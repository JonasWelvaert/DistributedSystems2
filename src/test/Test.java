package test;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import matchingservice.MatchingService;
import mixingproxy.MixingProxy;
import registrar.Registrar;
import sharedclasses.FileTransferable;

public class Test {

	public static void main(String[] args) throws Exception {
		// testSignature();
		new Thread(() -> Registrar.main(args)).run();
		new Thread(() -> MatchingService.main(args)).run();
		new Thread(() -> MixingProxy.main(args)).run();

		//testCopyToClipboard();

	}

	private static void testCopyToClipboard() throws IOException, HeadlessException, UnsupportedFlavorException {
		File file = new File("helloWorld.txt");
		file.createNewFile();
		FileWriter fw = new FileWriter(file);
		fw.write("Hello world!");
		fw.flush();
		fw.close();
		List<File> listOfFiles = new ArrayList<>();
		listOfFiles.add(file);

		FileTransferable ft = new FileTransferable(listOfFiles);

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ft, (clipboard, contents)->{/*executed when your content is overwritten*/});

		Object clipboardContent = Toolkit.getDefaultToolkit().getSystemClipboard()
				.getData(DataFlavor.javaFileListFlavor);
		if (clipboardContent instanceof List) {
			List clipboardList = (List) clipboardContent;
			if (clipboardList.size() == 1) {
				if (clipboardList.get(0) instanceof File) {
					List<File> listOfReceivedFiles = (List<File>) clipboardList;
					File receivedFile = (File) listOfReceivedFiles.get(0);
					Scanner scanner = new Scanner(receivedFile);
					while (scanner.hasNextLine()) {
						System.out.println(scanner.nextLine());
					}
					scanner.close();
				}
			}
		}
	}

	private static void testSignature() {
		try {
			// use of RSA because of DSA generates an different signature each time.
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			KeyPair keyPair = kpg.generateKeyPair();

			byte[] toBeSigned = "Hello World!".getBytes();

			Signature sig = Signature.getInstance("SHA256WithRSA");
			sig.initSign(keyPair.getPrivate());
			sig.update(toBeSigned);
			byte[] sign1 = sig.sign();

			sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(keyPair.getPrivate());
			sig.update(toBeSigned);
			byte[] sign2 = sig.sign();

			System.out.println(Base64.getEncoder().encodeToString(sign1));
			System.out.println(Base64.getEncoder().encodeToString(sign2));

		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			System.err.println("Sign bytes will be null: if this appears: Fix it!");
		}
	}
}
