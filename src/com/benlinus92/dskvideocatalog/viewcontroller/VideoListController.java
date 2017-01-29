package com.benlinus92.dskvideocatalog.viewcontroller;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.VideoLink;
import com.benlinus92.dskvideocatalog.model.VideoTranslationType;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class VideoListController {
	private MainApp mainApp;
	private VideoTranslationType videoItem; 
	private MediaStream streamType;
	private VBox videoListBox;
	@FXML
	private ScrollPane listScrollPane;
	@FXML
	private Button backButton;
	@FXML
	private AnchorPane listPane;
	@FXML
	private void initialize() {
		videoListBox = new VBox();
		backButton.setOnMouseClicked(me -> {
			videoListBox.getChildren().clear();
			mainApp.openMainWindow();
		});
	}
	public void initializeVideoList(VideoTranslationType videoItem, MediaStream streamType) {
		this.videoItem = videoItem;
		this.streamType = streamType;
		VBox videoListBox = new VBox();
		for(VideoLink video: videoItem.getVideosList()) {
			BorderPane videoElemPane = new BorderPane();
			videoElemPane.setUserData(video);
			videoElemPane.setLeft(new Label(video.getVideoName()));
			videoElemPane.setOnMouseClicked(me -> {
				openVideoStream((VideoLink)((BorderPane)me.getSource()).getUserData());
			});
			videoListBox.getChildren().add(videoElemPane);
		}
		listScrollPane.setContent(videoListBox);
	}
	private void openVideoStream(VideoLink video) {
		String streamUrl = mainApp.getCurrentParser().getVideoStreamUrl(video, this.streamType);
		System.out.println(streamUrl);
		switch(this.streamType) {
			case MP4:
				break;
			case HLS:
				break;
			case M3U8:
				break;
			default:
				break;
		}
	}
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
