package barowner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class BarOwnerInfoController {

	@FXML
	private Label horecaName;

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
		horecaName.setText(BarOwner.getHorecaName());
		String[] qrCodeText = BarOwner.getQRCode(LocalDate.now());
		ByteArrayOutputStream out = QRCode.from(Arrays.toString(qrCodeText)).to(ImageType.GIF).withSize(200, 200)
				.stream();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		Image image = new Image(in);
		qrCode.setImage(image);
		copyToClipboard.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();
				content.putString(Arrays.toString(qrCodeText));
				clipboard.setContent(content);
			}
		});

		// TODO change this figure.
		proofOfRegistration.getGraphicsContext2D().fillOval(10, 60, 30, 30);

	}
}
