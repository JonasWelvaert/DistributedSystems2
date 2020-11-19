package visitor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    }

    @FXML
    //A user exists already if he has a csv-file; this file is made when confirmation of registration is sent by the registrar.
    void registerButtonClick(ActionEvent event) {
    	
    }
    
    @FXML
    private void initialize() {
    	
    }

}