package com.benlinus92.dskvideocatalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;
import com.benlinus92.dskvideocatalog.viewcontroller.CatalogController;
import com.benlinus92.dskvideocatalog.viewcontroller.ItemBrowserController;
import com.benlinus92.dskvideocatalog.viewcontroller.RootWindowController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	private Runtime runtime = Runtime.getRuntime();
	private Parser currentParser;
	private List<Pane> savedPanesList = new ArrayList<>();
	private CatalogStateHandler currentState = new CatalogStateHandler();
	
	@Override
	public void start(Stage primaryStage) {
		currentParser = new TreeTvParser();
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle(PropertiesHandler.getInstance().getAppTitleProp());
		initRootWindowLayout();
		initCatalogLayout();
		initItemBrowserLayout();
		root.initRightSidePane(catalogLayout);
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
			primaryStage.setOnCloseRequest(e -> Platform.exit());
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
			catalog.startUpdateCatalogThread();
			//catalog.updateCatalog();
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
	public void initImageViewerWindow(Image image) {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getImageViewerWindowProp()));
			AnchorPane imageWindow = (AnchorPane) fxml.load();
			imageWindow.getChildren().add(new ImageView(image));
			imageWindow.setOnScroll(e -> {
                double zoomFactor = 1.15;
                double deltaY = e.getDeltaY();
                if (deltaY < 0){
                  zoomFactor = 2.0 - zoomFactor;
                }
                AnchorPane pane = (AnchorPane)e.getSource();
                pane.setScaleX(pane.getScaleX() * zoomFactor);
                pane.setScaleY(pane.getScaleY() * zoomFactor);
                e.consume();
			});
			Stage secondStage = new Stage();
			secondStage.setScene(new Scene(imageWindow));
			secondStage.setTitle("Image");
			secondStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void changeCategory(int category) {
		catalog.updateCatalogWithNewCategory(category);
	}
	public void openItemBrowser(String url) {
		currentState.setCatalogState(catalog.getCurrentCategory(), currentParser, catalogLayout);
		itemBrowser.loadNewItemInBrowser(url);
		root.setRightSidePane(itemBrowserLayout);
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public Stage getPrimaryStage() {
		return this.primaryStage;
	}
	public Pane getCatalogPane() {
		return this.catalogLayout;
	}
	public CatalogController getCatalogController() {
		return this.catalog;
	}
	public Runtime getRuntime() {
		return this.runtime;
	}
	public Parser getCurrentParser() {
		return this.currentParser;
	}
	public void setCurrentParser(Parser parser) {
		this.currentParser = parser;
	}
	public void addPaneToSavedStateList(Pane state) {
		this.savedPanesList.add(state);
	}
	public void removePaneFromSavedStateList() {
		this.savedPanesList.remove(this.savedPanesList.size() - 1);
	}
	public Pane getLastSavedPane() {
		return this.savedPanesList.get(this.savedPanesList.size() - 1);
	}
	public int getSavedStateListSize() {
		return this.savedPanesList.size();
	}
	public CatalogStateHandler getCurrentState() {
		return currentState;
	}
}
