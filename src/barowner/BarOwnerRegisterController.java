package barowner;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class BarOwnerRegisterController {

	@FXML
	private Button registerButton;
	@FXML
	private TextField horecaNameR;
	@FXML
	private TextField horecaNumberR;
	@FXML
	private TextField addressR;
	@FXML
	private PasswordField passwordR;
	@FXML
	private Button loginButton;
	@FXML
	private TextField horecaNameL;
	@FXML
	private PasswordField passwordL;

	@FXML
	private void initialize() {
		registerButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				boolean valid = true;
				if (horecaNameR.getText() == null || horecaNameR.getText().equals("")) {
					valid = false;
				}
				if (horecaNumberR.getText() == null || horecaNumberR.getText().equals("")) {
					valid = false;
				}
				if (addressR.getText() == null || addressR.getText().equals("")) {
					valid = false;
				}
				if (passwordR.getText() == null || passwordR.getText().equals("")) {
					valid = false;
				}
				if (valid) {
					BarOwner.register(horecaNameR.getText(), horecaNumberR.getText(), addressR.getText(),
							passwordR.getText());
				}
			}
		});
		loginButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				boolean valid = true;
				if (horecaNameL.getText() == null || horecaNameL.getText().equals("")) {
					valid = false;
				}
				if (passwordL.getText() == null || passwordL.getText().equals("")) {
					valid = false;
				}
				if (valid) {
					BarOwner.login(horecaNameL.getText(), passwordL.getText());
				}
			}
		});
	}

}
