package doctor;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import sharedclasses.Log;

public class DoctorUIController {

	@FXML
	private Label labelDoctorNr;

	@FXML
	private Label labelNrOfPatients;

	@FXML
	private Button buttonSendInformation;

	@FXML
	private Button buttonReceiveLogs;

	@FXML
	public void initialize() {
		labelDoctorNr.setText(Integer.toString(Doctor.getNrOfDoctor()));
		ImageView imageView = new ImageView(new Image("/sharedclasses/clipboard.png", 20, 20, true, false));
		buttonReceiveLogs.setGraphic(imageView);
		buttonReceiveLogs.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					Object clipboardContent = Toolkit.getDefaultToolkit().getSystemClipboard()
							.getData(DataFlavor.javaFileListFlavor);
					if (clipboardContent instanceof List) {
						List clipboardList = (List) clipboardContent;
						if (clipboardList.size() == 1) {
							if (clipboardList.get(0) instanceof File) {
								File receivedFile = (File) clipboardList.get(0);
								Scanner scanner = new Scanner(receivedFile);
								Gson gson = new GsonBuilder()
										.registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
											@Override
											public void write(JsonWriter jsonWriter, LocalDate localDate)
													throws IOException {
												jsonWriter.value(localDate.toString());
											}

											@Override
											public LocalDate read(JsonReader jsonReader) throws IOException {
												return LocalDate.parse(jsonReader.nextString());
											}

										}).create();
								Type lListType = new TypeToken<List<Log>>() {
								}.getType();
								List<Log> logs = gson.fromJson(scanner.nextLine(), lListType);
								scanner.close();
								Doctor.receiveLogs(logs);
								updateLayout();
							}
						}
					}
				} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
					//TODO auto generated ofzo
				}
			}
		});
		buttonSendInformation.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Doctor.submitLogs();
				updateLayout();
			}
		});
		updateLayout();

	}

	public void updateLayout() {
		labelNrOfPatients.setText(Integer.toString(Doctor.getNrOfUnsendPatients()));
	}
}
