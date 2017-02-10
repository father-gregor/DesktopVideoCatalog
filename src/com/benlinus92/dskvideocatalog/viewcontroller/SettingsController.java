package com.benlinus92.dskvideocatalog.viewcontroller;

import java.io.File;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.PropertiesHandler;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class SettingsController {
	private MainApp mainApp;
	@FXML
	private Button fileChooseButton;
	@FXML
	private TextField pathToSystemPlayer;
	@FXML
	private void initialize() {
		String path = PropertiesHandler.getInstance().getUserProperty("userplayer.path");
		if(path == null)
			path = "";
		pathToSystemPlayer.setText(path);
		Image buttonImage = new Image(this.getClass().getClassLoader().getResourceAsStream("img/misc/folder.png"));
		fileChooseButton.setGraphic(new ImageView(buttonImage));
		fileChooseButton.setStyle("-fx-background-color: transparent");
		fileChooseButton.setOnMouseClicked(e -> {
			if(e.getButton() == MouseButton.PRIMARY) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.getExtensionFilters().add(new ExtensionFilter("Application(*.exe)", "*.exe"));
				fileChooser.setTitle("Путь к проигрывателю");
				File userPlayer = fileChooser.showOpenDialog(mainApp.getSettingsStage());
				if(userPlayer != null) {
					System.out.println(userPlayer.getName());
					PropertiesHandler.getInstance().setUserProperty("userplayer.path", userPlayer.getAbsolutePath());
					PropertiesHandler.getInstance().setUserProperty("userplayer.name", userPlayer.getName());
					pathToSystemPlayer.setText(userPlayer.getAbsolutePath());
				}
			}
		});
		fileChooseButton.setOnMouseEntered(e -> {
			fileChooseButton.setStyle("-fx-background-color: #d4e2f9");
			fileChooseButton.setOpacity(0.7);
		});
		fileChooseButton.setOnMouseExited(e -> {
			fileChooseButton.setStyle("-fx-background-color: transparent");
			fileChooseButton.setOpacity(1.0);
		});
	}
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
