package matchingservice;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import sharedclasses.Capsule;

public class MatchingServiceController {
	private MatchingServiceImplementation matchingService;
	private ObservableList<String> toObserve;

    @FXML
    private Label capsulesLabel;

    @FXML
    private Label criticalLabel;

    @FXML
    private Label acknowledgedLabel;
    
    @FXML
    private ListView<String> logsList = new ListView<>();

    @FXML
    void flushToRegistrar(ActionEvent event) {
    	matchingService.flushUnackedCapsules();
    }

    @FXML
    void initialize() {
    	matchingService = MatchingServiceImplementation.getImpl();
    	MatchingServiceImplementation.setController(this);
    	toObserve = FXCollections.observableList(new ArrayList<>());
    	logsList.setItems(toObserve);
    	toObserve.add(LocalTime.now() + ": MatchingService initialised.");
    }
    
    void setMatchingService(MatchingServiceImplementation matchingService) {
    	this.matchingService = matchingService;
    }
    
    void updateInfo(List<Capsule> capsules) {
    	int all = 0;
    	int critical = 0;
    	int ack = 0;
    	
    	for(Capsule caps: capsules) {
    		all++;
    		if(caps.isCritical()) {
    			critical++;
    			if(caps.isInformed()) {
    				ack++;
    			}
    		}
    	}
    	this.capsulesLabel.setText(Integer.toString(all));
    	this.criticalLabel.setText(Integer.toString(critical));
    	this.acknowledgedLabel.setText(Integer.toString(ack));
    }
    
    public void addLog(String log) {
    	toObserve.add(log);
    }
}
