package visitor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.Random;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import registrar.TokensAlreadyIssuedException;
import sharedclasses.Token;

public class VisitorHomeController {
	@FXML
	private AnchorPane apHorecaInformation;

	@FXML
	private AnchorPane apHorecaForm;

	@FXML
	private Button buttonRegisterAtHoreca;

	@FXML
	private Button buttonLeaveHoreca;

	@FXML
	private TextField tfQrCodeContent;

	@FXML
	private Label labelHorecaName;

	@FXML
	private Label labelTimeOfRegistration;

	@FXML
	private Canvas proofOfRegistration;

	@FXML
	private void initialize() {
		//try to fetch daily tokens
		try {
			//if this IS null, a server error occured with registration and system should exit for retry.
			Map <LocalDate, List<Token>> todaysTokens = Visitor.getTokenAllocation(Visitor.getUser().getPhoneNr());
			if( todaysTokens == null) {
				System.exit(1);
			}
				//make sure to add the correct date (the one that was fetched from the server
			Set<LocalDate> keys = todaysTokens.keySet();
			for(LocalDate date : keys) {
				System.out.println("Adding tokens to user " + Visitor.getUser().getPhoneNr() + " for date " + date);
				Visitor.getIssuedTokens().put(date, todaysTokens.get(date));
			}
		} catch (TokensAlreadyIssuedException e) {
			System.out.println("Tokens were already fetched today for user " + Visitor.getUser().getPhoneNr() +", no duplication allowed!");
		}
		//do some initialisation
		apHorecaInformation.setVisible(false);

		apHorecaForm.setVisible(true);
		buttonRegisterAtHoreca.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String qrCodeContent = tfQrCodeContent.getText();
				if (qrCodeContent != null && !qrCodeContent.equals("")) {
					String s1 = qrCodeContent.substring(1, qrCodeContent.length() - 1);// qrcode without [ and ]
					String[] s2 = s1.split(", "); // array of strings from qrcode
					if (s2.length == 3) {
						byte[] signedhash = Visitor.registerVisit(s2[0], s2[1], s2[2]);
						if (signedhash != null) {
							String signedHashAsString = Base64.getEncoder().encodeToString(signedhash);
							labelHorecaName.setText(s2[1]);
							labelTimeOfRegistration.setText("12:00"/* TODO */);
							setProofOfRegistration(signedHashAsString);
							apHorecaInformation.setVisible(true);
							apHorecaForm.setVisible(false);
						} else {
							apHorecaInformation.setVisible(false);
							apHorecaForm.setVisible(true);
						}
					} else {
						apHorecaInformation.setVisible(false);
						apHorecaForm.setVisible(true);
					}
				} else {
					apHorecaInformation.setVisible(false);
					apHorecaForm.setVisible(true);
				}
			}
		});

		buttonLeaveHoreca.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				//TODO alles
				apHorecaInformation.setVisible(false);
				apHorecaForm.setVisible(true);
			}
		});

	}

	private void setProofOfRegistration(String text) {
		GraphicsContext gc = proofOfRegistration.getGraphicsContext2D();
		gc.clearRect(0, 0, proofOfRegistration.getWidth(), proofOfRegistration.getHeight());
		Random random = new Random(text.hashCode());
		if (random.nextInt(2) == 0) {
			// using Fill
			gc.setFill(Color.rgb(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
			int i = random.nextInt(3);
			if (i == 0) {
				gc.fillOval(40, 40, 200, 200);
			} else if (i == 1) {
				int i1 = random.nextInt(360);
				gc.fillArc(40, 40, 200, 200, i1, Math.floorMod(i1 + 250, 360), ArcType.ROUND);
			} else if (i == 2) {
				gc.fillRect(40, 40, 200, 200);
			}
		} else {
			gc.setStroke(Color.rgb(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
			gc.setLineWidth(40);
			int i = random.nextInt(3);
			if (i == 0) {
				gc.strokeOval(40, 40, 200, 200);
			} else if (i == 1) {
				int i1 = random.nextInt(360);
				gc.strokeArc(40, 40, 200, 200, i1, Math.floorMod(i1 + 250, 360), ArcType.ROUND);
			} else if (i == 2) {
				gc.strokeRect(40, 40, 200, 200);
			}
		}

	}

}
