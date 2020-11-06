package barowner;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class BarOwnerRegisterController {

	@FXML
	private Button registerButton;
	
	@FXML
	private TextField horecaName;
	
	@FXML
	private TextField horecaNumber;
	
	@FXML
	private TextField phoneNumber;
	
	@FXML
	private void initialize() {
		registerButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//TODO register to registrar
				//check if fields are OK.
				BarOwner.setBarName(horecaName.getText());
				BarOwner.setSceneInfo();
			}
		});
	}
	
}
