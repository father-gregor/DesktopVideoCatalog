package com.benlinus92.dskvideocatalog.viewcontroller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.VideoLink;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MediaPlayerController {

	private final static String TOOLBAR_COLOR = "#ced5e0";
	private MainApp mainApp;
	private MediaPlayer player = null;
	private Slider timeSlider;
	private Label timeTextLabel;
	private EventHandler<MouseEvent> enteredButton;
	private EventHandler<MouseEvent> exitedButton;
	private ChangeListener<Duration> timeListener;
	private volatile Map<String, String> availableStreams = new LinkedHashMap<>(); 
	private String videoName;
	private String streamName;
	private Thread backgroundThread = null;
	@FXML
	private MediaView playerView;
	@FXML
	private VBox toolbarBox;
	@FXML
	public void initialize() {
		backgroundThread = new Thread();
		playerView.setOnMouseClicked(me -> {
			if(player != null && me.getButton().toString() == MouseButton.PRIMARY.toString()) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if(player.getStatus() == MediaPlayer.Status.PLAYING)
							player.pause();
						else if(player.getStatus() == MediaPlayer.Status.PAUSED)
							player.play();
					}
				});
			}
		});
		enteredButton = (me) -> {
			((Button)me.getSource()).setStyle("-fx-background-color:#a4adbc");
		};
		exitedButton = (me) -> {
			((Button)me.getSource()).setStyle("-fx-background-color:" + TOOLBAR_COLOR);
		};
		toolbarBox.setStyle("-fx-background-color:" + TOOLBAR_COLOR);
		timeSlider = createTimeSlider();
		timeListener = (observable, oldV, newV) -> {
			timeTextLabel.setText(formatTime(player.getCurrentTime(), player.getTotalDuration()));
			if(!timeSlider.isValueChanging())
				timeSlider.setValue(newV.toSeconds());
		};
		toolbarBox.getChildren().add(timeSlider);
		createToolbar();
	}
	public void initializeMediaPlayer(VideoLink video, MediaStream streamType) {
		videoName = video.getVideoName();
		streamName = streamType.toString();
		Runnable task = new Runnable() {		
			@Override
			public void run() {
				availableStreams = mainApp.getCurrentParser().getVideoStreamMap(video, streamType);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Media media = new Media(availableStreams.get("480"));
						player = new MediaPlayer(media);
						player.setAutoPlay(true);
						player.setMute(true);
						player.currentTimeProperty().addListener(timeListener);
						player.setOnReady(() -> {
							timeSlider.setValue(0);
							timeSlider.setMax(player.getTotalDuration().toSeconds());
							player.play();
						});
						playerView.setMediaPlayer(player);
						playerView.setFitHeight(media.getHeight());
						playerView.setFitWidth(media.getWidth());
						mainApp.getPlayerStage().setTitle(videoName + " " + streamName);
						//((BorderPane)playerView.getParent()).setPrefWidth(1000);
						//((BorderPane)playerView.getParent()).setPrefHeight(media.getHeight());
					}
				});
			}
		};
		if(!backgroundThread.isAlive()) {
			backgroundThread = new Thread(task);
			backgroundThread.setDaemon(true);
			backgroundThread.start();
		}
		//if(streamType == MediaStream.HLS) { }
	}
	public void createToolbar() {
		HBox buttonsBox = new HBox();
		buttonsBox.setStyle("-fx-background-color:" + TOOLBAR_COLOR);
		Image buttonImage = new Image(this.getClass().getClassLoader().getResourceAsStream("img/toolbar/player_play.png"), 35.0, 35.0, true, true);
		Button newButton = createToolbarButton(buttonImage);
		newButton.setOnAction(ae -> {
			if(player.getStatus() == Status.UNKNOWN || player.getStatus() == Status.HALTED)
				return ;
			if(player != null && player.getStatus() != MediaPlayer.Status.PLAYING)
				player.play();
		});
		buttonsBox.getChildren().add(newButton);
		
		buttonImage = new Image(this.getClass().getClassLoader().getResourceAsStream("img/toolbar/player_pause.png"), 35.0, 35.0, true, true);
		newButton = createToolbarButton(buttonImage);
		newButton.setOnAction(ae -> {
			if(player.getStatus() == Status.UNKNOWN || player.getStatus() == Status.HALTED)
				return ;
			if(player != null && player.getStatus() != Status.PAUSED)
				player.pause();
		});
		buttonsBox.getChildren().add(newButton);
		
		buttonImage = new Image(this.getClass().getClassLoader().getResourceAsStream("img/toolbar/player_stop.png"), 35.0, 35.0, true, true);
		newButton = createToolbarButton(buttonImage);
		newButton.setOnAction(ae -> {
			if(player.getStatus() == Status.UNKNOWN || player.getStatus() == Status.HALTED)
				return ;
			if(player != null && player.getStatus() != Status.STOPPED)
				player.stop();
		});
		buttonsBox.getChildren().add(newButton);
		
		buttonImage = new Image(this.getClass().getClassLoader().getResourceAsStream("img/toolbar/player_fullscreen.png"), 35.0, 35.0, true, true);
		newButton = createToolbarButton(buttonImage);
		newButton.setOnAction(ae -> {
			if(player.getStatus() == Status.UNKNOWN || player.getStatus() == Status.HALTED)
				return ;
			if(player != null && player.getStatus() != Status.STOPPED)
				player.stop();
		});
		newButton.setOnAction(ae -> {
			if(mainApp.getPlayerStage().isFullScreen())
				mainApp.getPlayerStage().setFullScreen(false);
			else
				mainApp.getPlayerStage().setFullScreen(true);
		});
		buttonsBox.getChildren().add(newButton);
		
		timeTextLabel = new Label();
		buttonsBox.getChildren().add(timeTextLabel);
		toolbarBox.getChildren().add(buttonsBox);
	}
	
	private Button createToolbarButton(Image buttonImage) {
		Button newButton = new Button();
		newButton.setStyle("-fx-background-color:" + TOOLBAR_COLOR);
		newButton.setGraphic(new ImageView(buttonImage));
		newButton.addEventHandler(MouseEvent.MOUSE_ENTERED, enteredButton);
		newButton.addEventHandler(MouseEvent.MOUSE_EXITED, exitedButton);
		return newButton;
	}
	private Slider createTimeSlider() {
		Slider slider = new Slider(0, 100, 0);
		slider.valueProperty().addListener((observable, oldV, newV) -> {
            if (!timeSlider.isValueChanging()) {
                double currentTime = player.getCurrentTime().toSeconds();
                if (Math.abs(currentTime - newV.doubleValue()) > 10) {
                    player.seek(Duration.seconds(newV.doubleValue()));
                    //timeTextLabel.setText(formatTime(player.getCurrentTime(), player.getTotalDuration()));
                }
            }
		});
		slider.valueChangingProperty().addListener((observable, wasChanging, changingNow) -> {
			if(!changingNow)
				player.seek(Duration.seconds(slider.getValue()));
		});
		return slider;
	}
	private static String formatTime(Duration elapsed, Duration duration) {
		int elapsedInt = (int)Math.floor(elapsed.toSeconds());
		int elapsedHours = elapsedInt / (60 * 60);
		if(elapsedHours > 0)
			elapsedInt -= elapsedHours * 60 * 60;
		int elapsedMinutes = elapsedInt / 60;
		int elapsedSeconds = elapsedInt - elapsedHours * 60 * 60 - elapsedMinutes * 60;
		if(duration.greaterThan(Duration.ZERO)) {
			int durationInt = (int) Math.floor(duration.toSeconds());
			int durationHours = durationInt / (60 * 60);
			if(durationHours > 0)
				durationInt -= durationHours * 60 * 60;
			int durationMinutes = durationInt / 60;
			int durationSeconds = durationInt - durationHours * 60 * 60 
					- durationMinutes * 60;
			if(durationHours > 0) {
		         return String.format("%d:%02d:%02d/%d:%02d:%02d", 
		        		 elapsedHours, elapsedMinutes, elapsedSeconds,
		        		 durationHours, durationMinutes, durationSeconds);
			} else {
		          return String.format("%02d:%02d/%02d:%02d",
		                  elapsedMinutes, elapsedSeconds,durationMinutes, 
		                      durationSeconds);
			}
		} else {
			if(elapsedHours > 0) 
				return String.format("%d:%02d:%02d", elapsedHours, 
	                     elapsedMinutes, elapsedSeconds);
			else
				return String.format("%02d:%02d",elapsedMinutes, 
	                    elapsedSeconds);
		}
	}
	
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
