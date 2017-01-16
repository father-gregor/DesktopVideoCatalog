package com.benlinus92.dskvideocatalog;

import java.io.IOException;

import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;
import com.benlinus92.dskvideocatalog.viewcontroller.CatalogController;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private Pane rootLayout;
	public PropertiesHandler appProperties = PropertiesHandler.getInstance(); 
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(appProperties.getAppTitleProp());
		initRootLayout();
		initCatalogLayout();
		//Parser parser = new TreeTvParser();
		//parser.parseHtml();
	}
	private void initRootLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(appProperties.getCatalogviewProp()));
			rootLayout = (AnchorPane)fxml.load();
			primaryStage.setScene(new Scene(rootLayout));
			primaryStage.setMaxHeight(800.0);
			primaryStage.setMaxWidth(800.0);
			primaryStage.setMinHeight(600.0);
			primaryStage.setMinWidth(700.0);
			primaryStage.show();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	private void initCatalogLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(appProperties.getCatalogviewProp()));
			AnchorPane catalogAnchor = (AnchorPane)fxml.load();
			//rootLayout.setCenter(catalogAnchor);
			CatalogController catalog = fxml.getController();
			catalog.setMainApp(this);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public Stage getPrimaryStage() {
		return this.primaryStage;
	}
}
