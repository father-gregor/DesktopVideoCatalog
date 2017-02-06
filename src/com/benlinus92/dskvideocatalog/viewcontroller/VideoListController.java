package com.benlinus92.dskvideocatalog.viewcontroller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.PropertiesHandler;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.VideoLink;
import com.benlinus92.dskvideocatalog.model.VideoTranslationType;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class VideoListController {
	private MainApp mainApp;
	private VideoLink selectedVideo; 
	private MediaStream streamType;
	private volatile Map<String, String> availableStreams;
	private Stage mediaMenuStage;
	@FXML
	private ScrollPane listScrollPane;
	@FXML
	private AnchorPane listPane;
	@FXML
	private void initialize() {
		
	}
	public void initializeVideoList(VideoTranslationType videoItem, MediaStream streamType) {
		this.streamType = streamType;
		VBox videoListBox = new VBox();
		for(VideoLink video: videoItem.getVideosList()) {
			BorderPane videoElemPane = new BorderPane();
			videoElemPane.setUserData(video);
			videoElemPane.setLeft(new Label(video.getVideoName()));
			videoElemPane.setOnMouseClicked(me -> {
				selectedVideo = (VideoLink)((BorderPane)me.getSource()).getUserData();
				initChooseMediaMenuLayout();
			});
			videoListBox.getChildren().add(videoElemPane);
		}
		listScrollPane.setContent(videoListBox);
	}
	public void initChooseMediaMenuLayout() {
		try {
			FXMLLoader fxml = new FXMLLoader();
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getMediaMenuViewProp()));
			Pane mediaMenuPane = (Pane)fxml.load();
			ChooseMediaMenuController menuCont = fxml.getController();
			menuCont.setMainCaller(this);
			menuCont.initializeMenuItems();
			mediaMenuPane.setStyle("-fx-background-color:#c2c6ce");
			mediaMenuStage = new Stage(StageStyle.TRANSPARENT);
			mediaMenuStage.setOnCloseRequest(e -> {
				mediaMenuPane.getChildren().clear();
				((Stage)e.getSource()).setScene(null);
			});
			mediaMenuStage.setScene(new Scene(mediaMenuPane));
			mediaMenuStage.initOwner(mainApp.getPrimaryStage());
			mediaMenuStage.focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
				if(!isNowFocused)
					mediaMenuStage.fireEvent(new WindowEvent(mediaMenuStage, WindowEvent.WINDOW_CLOSE_REQUEST));
			});
			mediaMenuStage.show();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void openInternalVideoStreamPlayer() {
		mediaMenuStage.fireEvent(new WindowEvent(mediaMenuStage, WindowEvent.WINDOW_CLOSE_REQUEST));
		mainApp.initMediaPlayerLayout(selectedVideo, streamType);
	}
	public void openUserDefaultPlayer() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				availableStreams = mainApp.getCurrentParser().getVideoStreamMap(selectedVideo, streamType);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							Desktop.getDesktop().browse(new URI(availableStreams.get("480")));
						} catch(IOException | URISyntaxException | UnsupportedOperationException e) {
							e.printStackTrace();
						} finally {
							mediaMenuStage.fireEvent(new WindowEvent(mediaMenuStage, WindowEvent.WINDOW_CLOSE_REQUEST));
						}
					}
				});
			}
		};
		Thread backgroundThread = new Thread(task);
		backgroundThread.start();
	}
	public void copyLinkToClipBoard() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				availableStreams = mainApp.getCurrentParser().getVideoStreamMap(selectedVideo, streamType);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Clipboard clipboard = Clipboard.getSystemClipboard();
				        ClipboardContent content = new ClipboardContent();
				        content.putString(availableStreams.get("480"));
				        clipboard.setContent(content);
				        mediaMenuStage.fireEvent(new WindowEvent(mediaMenuStage, WindowEvent.WINDOW_CLOSE_REQUEST));	
					}
				});
			}
		};
		Thread backgroundThread = new Thread(task);
		backgroundThread.start();
	}
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
