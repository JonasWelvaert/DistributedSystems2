package mixingproxy;

import java.time.LocalTime;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class MixingProxyController {
	private MixingProxyImplementation mixingProxy;
	private ObservableList<String> logStrings;

    @FXML
    private Label capsulesLabel;

    @FXML
    private ListView<String> logsList = new ListView<String>();

    @FXML
    void flushToMatchingService(ActionEvent event) {
    	mixingProxy.sendCapsulesToMatchingService();
    }
    
    @FXML
    public void initialize() {
    	this.mixingProxy = MixingProxyImplementation.getImpl();
    	MixingProxyImplementation.setController(this);
    	logStrings = FXCollections.observableList(new ArrayList<>());
    	logsList.setItems(logStrings);
    	addLog(LocalTime.now() + ": MixingProxy initialised.");
    }

    public void updateInfo(int size) {
    	this.capsulesLabel.setText(Integer.toString(size));
    }
    
    public void addLog(String log) {
    	logStrings.add(log);
    }
}
