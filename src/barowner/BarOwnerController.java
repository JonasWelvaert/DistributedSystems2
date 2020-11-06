package barowner;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class BarOwnerController {

	@FXML
	private TextField horecaName;

	@FXML
	private Label bitstream_label;

	@FXML
	private Label bitstream;

	@FXML
	private Button copyToClipboard;

	@FXML
	private Label proofOfRegistration_label;

	@FXML
	private Canvas proofOfRegistration;

	@FXML
	private void initialize() {
		setEnrolledScene();
		copyToClipboard.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();
				content.putString(bitstream.getText());
				clipboard.setContent(content);
			}
		});
	}

	private void setEnrolledScene() {
		horecaName.setEditable(false);
		horecaName.setText(BarOwner.getBarName());
		bitstream_label.setVisible(true);
		// TODO change this bitstream
		bitstream.setVisible(true);
		copyToClipboard.setVisible(true);
		proofOfRegistration_label.setVisible(true);
		proofOfRegistration.setVisible(true);
		// TODO change this figure.
		proofOfRegistration.getGraphicsContext2D().fillOval(10, 60, 30, 30);
	}
}
