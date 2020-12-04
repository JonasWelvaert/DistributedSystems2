package inspector;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class InspectorController implements Initializable {

	@FXML
	private TextField tfQRCode;
	
	@FXML
	private Button buttonCheckValidity;
	
	@FXML
	private Label labelValidity;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		labelValidity.setText("");
		
		buttonCheckValidity.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				String qrCode = tfQRCode.getText();
				tfQRCode.setText("");
				
				boolean valid = Inspector.validateQRCode(qrCode);
				
				if(valid) {
					labelValidity.setText("This QR code is valid!");
					labelValidity.setTextFill(Color.BLACK);
				}else {
					labelValidity.setText("This QR code is invalid!");
					labelValidity.setTextFill(Color.RED);
				}
				
			}
		});
		
	}

}
