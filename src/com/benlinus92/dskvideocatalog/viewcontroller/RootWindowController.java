package com.benlinus92.dskvideocatalog.viewcontroller;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.PropertiesHandler;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class RootWindowController {
	
	private MainApp mainApp;
	@FXML
	private Accordion webSitesSectionPane;
	@FXML
	private SplitPane menuSplitPane;
	@FXML
	private void initialize() {
		PropertiesHandler menuUnits = PropertiesHandler.getInstance();
		EventHandler<MouseEvent> mouseEv = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				int id = Integer.parseInt(((Label)me.getSource()).getId());
				mainApp.changeCategory(id);
			}
		};
		for(TitledPane pane: webSitesSectionPane.getPanes()) {
			VBox vb = new VBox();
			Label label = new Label(menuUnits.getFilmsUnitName());
			label.setId(Integer.toString(AppConstants.CATEGORY_FILMS));
			label.setOnMouseClicked(mouseEv);
			vb.getChildren().add(label);
			label = new Label(menuUnits.getSeriesUnitName());
			label.setId(Integer.toString(AppConstants.CATEGORY_SERIES));
			label.setOnMouseClicked(mouseEv);
			vb.getChildren().add(label);
			label = new Label(menuUnits.getCartoonsUnitName());
			label.setId(Integer.toString(AppConstants.CATEGORY_CARTOONS));
			label.setOnMouseClicked(mouseEv);
			vb.getChildren().add(label);
			pane.setContent(vb);
		}
	}
	
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
	
	public void setRightSidePane(Pane rightSide) {
		menuSplitPane.getItems().set(1, rightSide);
	}
}
