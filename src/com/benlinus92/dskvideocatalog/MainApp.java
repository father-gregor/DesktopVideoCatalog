package com.benlinus92.dskvideocatalog;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	public PropertiesHandler appProperties = PropertiesHandler.getInstance(); 
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(appProperties.getAppTitleProp());
		initRootLayout();
		initCatalogLayout();
		
	}
	private void initRootLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(appProperties.getRootviewProp()));
			rootLayout = (BorderPane)fxml.load();
			primaryStage.setScene(new Scene(rootLayout));
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
			rootLayout.setCenter(catalogAnchor);
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
