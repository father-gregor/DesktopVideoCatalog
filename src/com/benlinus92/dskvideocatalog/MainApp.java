package com.benlinus92.dskvideocatalog;

import java.io.IOException;

import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;
import com.benlinus92.dskvideocatalog.viewcontroller.CatalogController;
import com.benlinus92.dskvideocatalog.viewcontroller.RootWindowController;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private Pane mainLayout;
	private Pane catalogLayout;
	private RootWindowController root;
	private CatalogController catalog;
	public PropertiesHandler appProperties = PropertiesHandler.getInstance(); 
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(appProperties.getAppTitleProp());
		initRootWindowLayout();
		initCatalogLayout();
		root.setRightSidePane(catalogLayout);
		this.primaryStage.show();
	}
	private void initRootWindowLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(appProperties.getRootWindowViewProp()));
			mainLayout = (AnchorPane)fxml.load();
			root = fxml.getController();
			root.setMainApp(this);
			primaryStage.setScene(new Scene(mainLayout));
			primaryStage.setMaxHeight(800.0);
			primaryStage.setMaxWidth(800.0);
			primaryStage.setMinHeight(600.0);
			primaryStage.setMinWidth(700.0);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	private void initCatalogLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(appProperties.getCatalogviewProp()));
			catalogLayout = (AnchorPane)fxml.load();
			//rootLayout.setCenter(catalogAnchor);
			catalog = fxml.getController();
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
