package com.benlinus92.dskvideocatalog;

import java.io.IOException;

import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;
import com.benlinus92.dskvideocatalog.viewcontroller.CatalogController;
import com.benlinus92.dskvideocatalog.viewcontroller.ItemBrowserController;
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
	private Pane itemBrowserLayout;
	private RootWindowController root;
	private CatalogController catalog;
	private ItemBrowserController itemBrowser;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(PropertiesHandler.getInstance().getAppTitleProp());
		initRootWindowLayout();
		initCatalogLayout();
		initItemBrowserLayout();
		root.setRightSidePane(catalogLayout);
		this.primaryStage.show();
	}
	private void initRootWindowLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getRootWindowViewProp()));
			mainLayout = (AnchorPane)fxml.load();
			root = fxml.getController();
			root.setMainApp(this);
			primaryStage.setScene(new Scene(mainLayout));
			primaryStage.setMaxHeight(1300.0);
			primaryStage.setMaxWidth(1500.0);
			primaryStage.setMinHeight(600.0);
			primaryStage.setMinWidth(750.0);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	private void initCatalogLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getCatalogViewProp()));
			catalogLayout = (AnchorPane)fxml.load();
			catalog = fxml.getController();
			catalog.setMainApp(this);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	private void initItemBrowserLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getItembrowserViewProp()));
			itemBrowserLayout = (AnchorPane)fxml.load();
			itemBrowser = fxml.getController();
			itemBrowser.setMainApp(this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void changeCategory(int category) {
		catalog.updateCatalogWithNewCategory(category);
	}
	public void openItemBrowser(String url) {
		itemBrowser.loadNewItemInBrowser(url);
		root.setRightSidePane(itemBrowserLayout);
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public Stage getPrimaryStage() {
		return this.primaryStage;
	}
	public CatalogController getCatalogController() {
		return this.catalog;
	}
}
