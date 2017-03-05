package com.benlinus92.dskvideocatalog.viewcontroller;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.parsers.ExFsParser;
import com.benlinus92.dskvideocatalog.parsers.KinogoClubParser;
import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/*
 * Class controlling application basic window, which contains website chooser on
 * the left and {@link SplitPane} on the right. SplitPane content is vary 
 * depending on application current state.
 */
public class RootWindowController {
	
	private MainApp mainApp;
	@FXML
	private Accordion webSitesSectionPane;
	@FXML
	private SplitPane menuSplitPane;
	@FXML
	private Button backButton;
	@FXML
	private Button settingsButton;
	@FXML
	private void initialize() {
		List<Parser> parsersList = Arrays.asList(new TreeTvParser(), new KinogoClubParser(), new ExFsParser());//add all parsers there
		Image buttonImage = new Image(this.getClass().getClassLoader().getResourceAsStream("img/misc/gear.png"));
		settingsButton.setGraphic(new ImageView(buttonImage));
		settingsButton.setStyle("-fx-background-color: transparent");
		settingsButton.setOnMouseClicked(e -> {
			if(e.getButton() == MouseButton.PRIMARY)
				mainApp.initSettingsWindow();
		});
		buttonImage = new Image(this.getClass().getClassLoader().getResourceAsStream("img/misc/back.png"));
		backButton.setGraphic(new ImageView(buttonImage));
		backButton.setStyle("-fx-background-color: transparent");
		backButton.setOnMouseClicked(e -> {
			if(e.getButton() == MouseButton.PRIMARY && mainApp.getCurrentState().getPaneState() != null) {
				setRightSidePane(mainApp.getCurrentState().getPaneState());
				if(mainApp.getCurrentState().getPaneState() == mainApp.getCatalogPane())
					mainApp.getCurrentState().setPaneState(null);;
				mainApp.setCurrentParser(mainApp.getCurrentState().getParserState());
				mainApp.getCatalogController().setCurrentCategory(mainApp.getCurrentState().getCategoryState());
			}
		});
		EventHandler<MouseEvent> categoryClickedEvent = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				Label currLabel = (Label)me.getSource();
				if(mainApp.getCatalogPane().getScene() == null) //show catalog if current visible scene is item browser
					setRightSidePane(mainApp.getCatalogPane());
				mainApp.changeCategory(Integer.parseInt(currLabel.getId()));
			}
		};
		for(Parser parser: parsersList) {
			VBox categoryBox = new VBox();
			for(Entry<String, Integer> categoryEntry: parser.getParserCategoryMap().entrySet()) {
				Label label = new Label(categoryEntry.getKey());
				label.setId(Integer.toString(categoryEntry.getValue()));
				label.setOnMouseClicked(categoryClickedEvent);
				categoryBox.getChildren().add(label);
			}
			categoryBox.setUserData(parser);
			categoryBox.setOnMouseReleased(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent me) {
					mainApp.setCurrentParser((Parser)((VBox)me.getSource()).getUserData());
				}
			});
			TitledPane webSitePane = new TitledPane(parser.getWebSiteName(), categoryBox);
			webSitesSectionPane.getPanes().add(webSitePane);
		}
		webSitesSectionPane.setExpandedPane(webSitesSectionPane.getPanes().get(0));
	}
	
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
	
	public void initRightSidePane(Pane rightSide) {
		menuSplitPane.getItems().set(1, rightSide);
	}
	public void setRightSidePane(Pane rightSide) {
		menuSplitPane.getItems().set(1, rightSide);
	}
}
