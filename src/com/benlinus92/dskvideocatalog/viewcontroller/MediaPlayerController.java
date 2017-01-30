package com.benlinus92.dskvideocatalog.viewcontroller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.VideoLink;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class MediaPlayerController {

	private MainApp mainApp;
	private volatile Map<String, String> availableStreams = new LinkedHashMap<>(); 
	private Thread backgroundThread = null;
	@FXML
	private MediaView playerView;
	@FXML
	public void initialize() {
		backgroundThread = new Thread();
	}
	public void initializeMediaPlayer(VideoLink video, MediaStream streamType) {
		Runnable task = new Runnable() {		
			@Override
			public void run() {
				availableStreams = mainApp.getCurrentParser().getVideoStreamMap(video, streamType);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Media media = new Media(availableStreams.get("480"));
						MediaPlayer player = new MediaPlayer(media);
						player.setAutoPlay(true);
						playerView.setMediaPlayer(player);
					}
				});
			}
		};
		if(!backgroundThread.isAlive()) {
			backgroundThread = new Thread(task);
			backgroundThread.setDaemon(true);
			backgroundThread.start();
		}
		if(streamType == MediaStream.HLS) {
			
		}
		
	}
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
