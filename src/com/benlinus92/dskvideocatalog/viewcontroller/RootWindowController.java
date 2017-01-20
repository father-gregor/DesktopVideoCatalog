package com.benlinus92.dskvideocatalog.viewcontroller;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.PropertiesHandler;
import com.benlinus92.dskvideocatalog.parsers.ExFsParser;
import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;

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
	private TitledPane treeTvPane;
	@FXML
	private TitledPane exfsPane;
	@FXML
	private void initialize() {
		PropertiesHandler menuUnits = PropertiesHandler.getInstance();
		treeTvPane.setUserData(new TreeTvParser());
		exfsPane.setUserData(new ExFsParser());
		EventHandler<MouseEvent> categoryClickedEvent = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				Label currLabel = (Label)me.getSource();
				if(mainApp.getCatalogPane().getScene() == null) //show catalog if current visible scene is item browser
					setRightSidePane(mainApp.getCatalogPane());
				mainApp.changeCategory(Integer.parseInt(currLabel.getId()));
			}
		};
		for(TitledPane pane: webSitesSectionPane.getPanes()) {
			VBox vb = new VBox();
			Label label = new Label(menuUnits.getFilmsUnitName());
			label.setId(Integer.toString(AppConstants.CATEGORY_FILMS));
			label.setOnMouseClicked(categoryClickedEvent);
			vb.getChildren().add(label);
			label = new Label(menuUnits.getSeriesUnitName());
			label.setId(Integer.toString(AppConstants.CATEGORY_SERIES));
			label.setOnMouseClicked(categoryClickedEvent);
			vb.getChildren().add(label);
			label = new Label(menuUnits.getCartoonsUnitName());
			label.setId(Integer.toString(AppConstants.CATEGORY_CARTOONS));
			label.setOnMouseClicked(categoryClickedEvent);
			vb.getChildren().add(label);
			pane.setOnMouseReleased(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent me) {
					Parser parser = (Parser)((TitledPane)me.getSource()).getUserData();
					if(!parser.equals(mainApp.getCurrentParser()))
						mainApp.setCurrentParser(parser);
				}
			});
			pane.setContent(vb);
		}
		webSitesSectionPane.setExpandedPane(webSitesSectionPane.getPanes().get(0));
	}
	
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
	
	public void setRightSidePane(Pane rightSide) {
		//menuSplitPane.getItems().get(1).
		menuSplitPane.getItems().set(1, rightSide);
	}
}
