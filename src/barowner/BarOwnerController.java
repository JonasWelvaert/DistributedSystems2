package barowner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class BarOwnerController {

	@FXML
	private TextField horecaName;

	@FXML
	private Label bitstream_label;

	@FXML
	private ImageView qrCode;

	@FXML
	private Button copyToClipboard;

	@FXML
	private Label proofOfRegistration_label;

	@FXML
	private Canvas proofOfRegistration;

	@FXML
	private void initialize() {
		horecaName.setEditable(false);
		horecaName.setText(BarOwner.getBarName());
		ByteArrayOutputStream out = QRCode.from(BarOwner.getBarName()).to(ImageType.GIF).withSize(200, 200).stream();//TODO veranderen
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Image image = new Image(in);
		qrCode.setImage(image);;
		// TODO change this figure.
		proofOfRegistration.getGraphicsContext2D().fillOval(10, 60, 30, 30);
		copyToClipboard.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();
				content.putString(BarOwner.getBarName()); // TODO: veranderen
				clipboard.setContent(content);
			}
		});
	}
}
