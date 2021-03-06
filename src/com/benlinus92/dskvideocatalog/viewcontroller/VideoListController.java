package com.benlinus92.dskvideocatalog.viewcontroller;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.benlinus92.dskvideocatalog.JavaToJavascriptBridge;
import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.PropertiesHandler;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.VideoLink;
import com.benlinus92.dskvideocatalog.model.VideoTranslationType;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import netscape.javascript.JSObject;

/*
 * Class for displaying list of video files associated with chosen item.
 * Based on the outcome of {@link ChooseMediaMenuController} window, class
 * initialize one of the available streaming options: internal player, web player,
 * user-defined player, copy link and open in browser. Each option is defined
 * in its own method. 
 */
public class VideoListController {
	private MainApp mainApp;
	private VideoLink selectedVideo; 
	private MediaStream streamType;
	private WebView webPlayer;
	private volatile Map<String, String> availableStreams;
	private AtomicBoolean isHtmlLoaded = new AtomicBoolean(false);
	private Stage mediaMenuStage;
	@FXML
	private ScrollPane listScrollPane;
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
			fxml.setLocation(MainApp.class.getResource(PropertiesHandler.getInstance().getAppProperty("view.choosemediamenu")));
			Pane mediaMenuPane = (Pane)fxml.load();
			ChooseMediaMenuController menuCont = fxml.getController();
			menuCont.setMainCaller(this);
			menuCont.initializeMenuItems();
			mediaMenuPane.setStyle("-fx-background-color:#cbd8ed");
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
	public void openWebPlayer() {
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		mediaMenuStage.fireEvent(new WindowEvent(mediaMenuStage, WindowEvent.WINDOW_CLOSE_REQUEST));
		try {
			Stage webStage = new Stage();
			webPlayer = new WebView();
			AtomicInteger webstatusCount = new AtomicInteger(0);
			//webPlayer.setMinWidth(700.0);
			//webPlayer.setMinHeight(400.0);
			webPlayer.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
				if(!isHtmlLoaded.get() && newState.equals(State.SUCCEEDED)) {
					isHtmlLoaded.set(true);
					startInitWebSourceThread();
				}
			});
			webPlayer.getEngine().setOnStatusChanged(e -> {
				if(isHtmlLoaded.get() && webstatusCount.get() > 0) {
					startInitWebSourceThread();
				}
				webstatusCount.incrementAndGet();
			});
			String content = streamToString(getClass().getClassLoader()
					.getResourceAsStream(PropertiesHandler.getInstance().getAppProperty("html.webplayer")));
			webPlayer.getEngine().loadContent(content);
			webPlayer.getEngine().getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
			    @Override
			    public void changed(ObservableValue<? extends Throwable> ov, Throwable t, Throwable t1) {
			        System.out.println("Received exception: "+t1.getMessage());
			    }
			});
			Scene webScene = new Scene(webPlayer, 640, 400);
			webStage.setScene(webScene);
			webStage.setOnCloseRequest(e -> {
				webPlayer.getEngine().executeScript("disposePlayer()");
				webPlayer = null;
				webStage.setScene(null);
			});
			webStage.show();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	private void startInitWebSourceThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				availableStreams = mainApp.getCurrentParser().getVideoStreamMap(selectedVideo, streamType);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						System.out.println("TTTTT");
						JSObject window = (JSObject) webPlayer.getEngine().executeScript("window");
						window.setMember("java", new JavaToJavascriptBridge());
		            	window.call("setSource", (Object)availableStreams.keySet().toArray(new String[availableStreams.size()]),
		            			availableStreams.values().toArray(), getStreamTypeForWeb(streamType));
					}
				});
				
			}
		}).start();
	}
	public void downloadFile() {
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
	public void openUserDefaultPlayer() {
		String pathToPlayer = PropertiesHandler.getInstance().getUserProperty("userplayer.path");
		if(pathToPlayer != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						availableStreams = mainApp.getCurrentParser().getVideoStreamMap(selectedVideo, streamType);
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								mediaMenuStage.fireEvent(new WindowEvent(mediaMenuStage, WindowEvent.WINDOW_CLOSE_REQUEST));
							}
						});
						ProcessBuilder pb = new ProcessBuilder(pathToPlayer, availableStreams.get("480"));
						pb.start();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
	public void copyLinkToClipboard() {
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
	public String streamToString(InputStream stream) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while((length = stream.read(buffer)) != -1)
			str.write(buffer, 0, length);
		return str.toString("UTF-8");
		
	}
	private String getStreamTypeForWeb(MediaStream type) {
		if(type == MediaStream.MP4)
			return "video/mp4";
		else if(type == MediaStream.HLS)
			return "application/x-mpegURL";
		else if(type == MediaStream.FLV)
			return "video/x-flv";
		return null;
	}
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
