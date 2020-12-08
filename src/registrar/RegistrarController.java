package registrar;

import java.time.LocalTime;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class RegistrarController {
	private RegistrarImplementation registrar;
	private ObservableList<String> toObserve;
	
    @FXML
    private Label userLabel;

    @FXML
    private Label cateringLabel;

    @FXML
    private ListView<String> logsList = new ListView<>();

    @FXML
    void initialize() {
    	registrar = RegistrarImplementation.getImpl();
    	RegistrarImplementation.setController(this);
    	toObserve = FXCollections.observableList(new ArrayList<>());
    	logsList.setItems(toObserve);
    	addLog(LocalTime.now() + "Registrar initialised.");
    }
    
    public void addLog(String log) {
    	this.toObserve.add(log);
    }
    
    public void updateInfo(int userAmount, int cateringAmount) {
    	this.userLabel.setText(Integer.toString(userAmount));
    	this.cateringLabel.setText(Integer.toString(cateringAmount));
    }
}
