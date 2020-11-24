package visitor;

import java.util.Random;

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

public class VisitorHomeController {
	@FXML
	private AnchorPane apHorecaInformation;

	@FXML
	private Button buttonRegisterAtHoreca;

	@FXML
	private TextField tfQrCodeContent;

	@FXML
	private Label labelHorecaName;

	@FXML
	private Canvas proofOfRegistration;

	@FXML
	private void initialize() {
		apHorecaInformation.setVisible(false);

		buttonRegisterAtHoreca.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String qrCodeContent = tfQrCodeContent.getText();
				String s1 = qrCodeContent.substring(1, qrCodeContent.length()-1);
				String[] s2 = s1.split(", ");
				System.out.println(s2);
				if (qrCodeContent != null && !qrCodeContent.equals("")) {
					// TODO inputverwerken, registeren bij mixing proxy

					// TODO change this figure.
					labelHorecaName.setText(s2[1]);
					setProofOfRegistration(qrCodeContent);
					apHorecaInformation.setVisible(true);
				}

			}
		});

	}

	private void setProofOfRegistration(String text) {
		GraphicsContext gc = proofOfRegistration.getGraphicsContext2D();
		gc.clearRect(0, 0, proofOfRegistration.getWidth(), proofOfRegistration.getHeight());
		Random random = new Random(text.hashCode());
		if (random.nextInt(2) == 0) {
			// using Fill
			gc.setFill(Color.rgb(random.nextInt(200),random.nextInt(200),random.nextInt(200)));
			int i = random.nextInt(3);
			if(i==0) {
				gc.fillOval(40, 40, 200, 200);
			}
			else if(i==1) {
				int i1 = random.nextInt(360);
				gc.fillArc(40, 40, 200, 200, i1, Math.floorMod(i1+250, 360), ArcType.ROUND);
			}else if(i==2) {
				gc.fillRect(40, 40, 200, 200);
			}
		} else {
			gc.setStroke(Color.rgb(random.nextInt(200),random.nextInt(200),random.nextInt(200)));
	        gc.setLineWidth(40);
	        int i = random.nextInt(3);
			if(i==0) {
				gc.strokeOval(40, 40, 200, 200);
			}
			else if(i==1) {
				int i1 = random.nextInt(360);
				gc.strokeArc(40, 40, 200, 200, i1, Math.floorMod(i1+250, 360), ArcType.ROUND);
			}else if(i==2) {
				gc.strokeRect(40, 40, 200, 200);
			}
		}

	}

}
