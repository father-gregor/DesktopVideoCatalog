package com.benlinus92.dskvideocatalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.VideoLink;
import com.benlinus92.dskvideocatalog.model.VideoTranslationType;
import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;
import com.benlinus92.dskvideocatalog.viewcontroller.CatalogController;
import com.benlinus92.dskvideocatalog.viewcontroller.ChooseMediaMenuController;
import com.benlinus92.dskvideocatalog.viewcontroller.ItemBrowserController;
import com.benlinus92.dskvideocatalog.viewcontroller.MediaPlayerController;
import com.benlinus92.dskvideocatalog.viewcontroller.RootWindowController;
import com.benlinus92.dskvideocatalog.viewcontroller.SettingsController;
import com.benlinus92.dskvideocatalog.viewcontroller.VideoListController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sun.applet.Main;

/*
 * Main class which instantiate and start the application. It extends {@link Application}
 * to bootstrap application via start() method. Contains object of all main Stage in app.
 * Instance of this class is send to different controller classes to enable communication
 * between them and primaryStage. MainApp contains a number of methods for initializing new 
 * interface nodes and connect them to primaryStage
 */
public class MainApp extends Application {

	private static final Logger logger = LogManager.getLogger();
	
	private Stage primaryStage;
	private Stage playerStage;
	private Stage settingsStage;
	private Pane mainLayout;
	private Pane catalogLayout;
	private Pane itemBrowserLayout;
	private RootWindowController root;
	private CatalogController catalog;
	private ItemBrowserController itemBrowser;
	private VideoListController videoList;
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
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getAppProperty("view.rootwindow")));
			mainLayout = (AnchorPane)fxml.load();
			root = fxml.getController();
			root.setMainApp(this);
			primaryStage.setScene(new Scene(mainLayout));
			primaryStage.setMaxHeight(1300.0);
			//primaryStage.setMaxWidth(1500.0);
			primaryStage.setMinHeight(600.0);
			primaryStage.setMinWidth(750.0);
			primaryStage.setOnCloseRequest(e -> Platform.exit());
			logger.info("Base window loaded");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	private void initCatalogLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getAppProperty("view.catalog")));
			catalogLayout = (AnchorPane)fxml.load();
			catalog = fxml.getController();
			catalog.setMainApp(this);
			catalog.startUpdateCatalogThread();
			logger.info("Catalog panel loaded");
		} catch(IOException e) {
			logger.error("Exception occured - {}", e.getMessage());
		}
	}
	private void initItemBrowserLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getAppProperty("view.itembrowser")));
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
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getAppProperty("view.imageviewerwindow")));
			AnchorPane imageWindow = (AnchorPane) fxml.load();
			imageWindow.getChildren().add(new ImageView(image));
			imageWindow.setOnScroll(se -> {
                double zoomFactor = 1.15;
                double deltaY = se.getDeltaY();
                if (deltaY < 0){
                  zoomFactor = 2.0 - zoomFactor;
                }
                AnchorPane pane = (AnchorPane)se.getSource();
                pane.setScaleX(pane.getScaleX() * zoomFactor);
                pane.setScaleY(pane.getScaleY() * zoomFactor);
                se.consume();
			});
			Stage imageStage = new Stage();
			imageStage.setScene(new Scene(imageWindow));
			imageStage.setTitle("Image");
			imageStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void initVideoListLayout(VideoTranslationType videoItem, MediaStream streamType) {
		try {
			//Desktop.getDesktop().open(new File("E:/Media/se-12-01_lostfilm.flv"));
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getAppProperty("view.videolist")));
			ScrollPane videoListPane = (ScrollPane)fxml.load();
			videoList = fxml.getController();
			videoList.setMainApp(this);
			videoList.initializeVideoList(videoItem, streamType);
			itemBrowser.setLinksTabContent(videoListPane);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void initMediaPlayerLayout(VideoLink video, MediaStream streamType) {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getAppProperty("view.mediaplayer")));
			BorderPane mediaPlayerPane = (BorderPane)fxml.load();
			MediaPlayerController mediaPlayer = fxml.getController();
			playerStage = new Stage();
			//playerStage.initStyle(StageStyle.TRANSPARENT);
			mediaPlayer.setMainApp(this);
			mediaPlayer.initializeMediaPlayer(video, streamType);
			mediaPlayerPane.setStyle("-fx-background-color: Black");
			//mediaPlayerPane.setMinSize(840, 580);
			//mediaPlayerPane.setPrefSize(840, 580);
			//mediaPlayerPane.setMaxSize(840, 580);
			Scene videoScene = new Scene(mediaPlayerPane);
			videoScene.setFill(Color.BLACK);
			playerStage.setScene(videoScene);
			playerStage.setTitle("Player");
			playerStage.setMinHeight(580);
			playerStage.setMinWidth(840);
			playerStage.setOnCloseRequest(e -> {
				mediaPlayer.disposeMediaPlayer();
				mediaPlayerPane.getChildren().clear();
				playerStage.setScene(null);
			});
			playerStage.show();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void initSettingsWindow() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getAppProperty("view.settings")));
			BorderPane settingsPane = (BorderPane) fxml.load();
			SettingsController settings = fxml.getController();
			settings.setMainApp(this);
			settingsStage = new Stage();
			settingsStage.initModality(Modality.APPLICATION_MODAL);
			settingsStage.setTitle("Settings");
			settingsStage.setScene(new Scene(settingsPane));
			settingsStage.setOnCloseRequest(e -> {
				settingsPane.getChildren().clear();
				settingsStage.setScene(null);
			});
			settingsStage.show();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void openMainWindow() {
		primaryStage.setScene(mainLayout.getScene());
	}
	public void changeCategory(int category) {
		catalog.updateCatalogWithNewCategory(category);
		catalog.setCurrentCategory(category);
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
	public Stage getPlayerStage() {
		return this.playerStage;
	}
	public Stage getSettingsStage() {
		return this.settingsStage;
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
