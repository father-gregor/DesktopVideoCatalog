package com.benlinus92.dskvideocatalog.viewcontroller;

import com.benlinus92.dskvideocatalog.MainApp;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;

public class RootWindowController {
	
	private MainApp mainApp;
	@FXML
	private SplitPane menuSplitPane;
	@FXML
	private void initialize() {
		
	}
	
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
	
	public void setRightSidePane(Pane rightSide) {
		menuSplitPane.getItems().set(1, rightSide);
	}
}
