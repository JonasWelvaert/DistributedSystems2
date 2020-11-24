package visitor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class VisitorRegisterController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField PassField;

    @FXML
    private TextField PhoneNrField;

    @FXML
    private TextField loginPhoneNrField;

    @FXML
    private PasswordField LoginPassField;

    @FXML
    //look if the file corresponding to this user already exists. If it does, read in the user temporarily and check password.
    //may need to return errors here :) 
    void loginButtonClick(ActionEvent event) {
    	String phoneNumber = this.loginPhoneNrField.getText().trim();
    	String password = this.LoginPassField.getText().trim();
    	if(Visitor.login(phoneNumber, password)) {
    		//close this window & open a new one.
    		System.out.println("registration succesful, file read & password validated.");
    		Visitor.openVisitorHomeGUI();
    	} else {
    		System.out.println("Login unsuccesful.");
    		resetGUI();
    	}
    }

    @FXML
    //A user exists already if he has a csv-file; this file is made when confirmation of registration is sent by the registrar.
    void registerButtonClick(ActionEvent event) {
    	String name = this.firstNameField.getText().trim() + " " + this.lastNameField.getText().trim();
    	String phoneNumber = this.PhoneNrField.getText().trim();
    	String password = this.PassField.getText().trim();
    	if(!validateRegister(name, phoneNumber, password)) {
    		resetGUI();
    		return;
    	}
    	if(Visitor.register(name, phoneNumber, password)) {
    		//close this window & open a new one.
    		System.out.println("registration succesful, file created and user logged.");
    		Visitor.openVisitorHomeGUI();
    	} else {
    		System.out.println("registration unsuccesful.");
    		resetGUI();
    	}
    }
    
    @FXML
    private void initialize() {
    	System.out.println("Visitor || Initialized the register/login-form ");
    }
    
    private void resetGUI() {
    	firstNameField.setText("");
    	lastNameField.setText("");
    	PassField.setText("");
    	PhoneNrField.setText("");
    	loginPhoneNrField.setText("");
    	LoginPassField.setText("");
    }
    
    private boolean validateRegister(String name, String nr, String pwd) {
    	if(name.equals("") || name.equals("null") || name.equals("\\s+")) {
    		Alert alert = new Alert(AlertType.WARNING);
    		alert.setHeaderText("naming issue detected.");
    		alert.setTitle("Name validation issue.");
    		alert.setContentText("Please enter a valid name.");
    		alert.showAndWait();
    		return false;
    	}
    	if(nr.equals("") || nr.equals("null") || nr.equals("\\s+")) {
    		Alert alert = new Alert(AlertType.WARNING);
    		alert.setHeaderText("number issue detected.");
    		alert.setTitle("Phone number validation issue.");
    		alert.setContentText("Please enter a valid phone number.");
    		alert.showAndWait();
    		return false;
    	}
    	if(pwd.equals("") || pwd.equals("null") || pwd.equals("\\s+") || pwd.length() <= 3 || pwd.length() >= 30) {
    		Alert alert = new Alert(AlertType.WARNING);
    		alert.setHeaderText("Password issue detected.");
    		alert.setTitle("Password validation issue.");
    		alert.setContentText("Please enter a valid password.");
    		alert.showAndWait();
    		return false;
    	}
    	return true;
    }

}