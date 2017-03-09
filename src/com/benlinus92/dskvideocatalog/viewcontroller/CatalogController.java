package com.benlinus92.dskvideocatalog.viewcontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.SimpleVideoItem;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;

/*
 * Class controls layout and functionality of main catalog pane. It calls
 * current parser's methods to get list of video items from current website.
 * Items from list then added to {@link TilePane} instance as layout objects.
 */
public class CatalogController {
	
	private final Logger logger = LogManager.getLogger();
	private MainApp mainApp;
	@FXML
	private TilePane mainTilePane;
	@FXML 
	private GridPane gridSample;
	@FXML
	private ScrollPane scrollCatalogPane;
	private ChangeListener<Number> scrollMaxEvent;
	private int currentCategory = AppConstants.CATEGORY_FILMS;
	private int currentPage = 1;
	private Thread backgroundThread = null;
	private List<ImageView> imageHolder = new CopyOnWriteArrayList<>();
	
	public CatalogController() {
		
	}
	
	@FXML
	private void initialize() {
		backgroundThread = new Thread();
		scrollMaxEvent = (ObservableValue<? extends Number> ov, Number oldV, Number newV) -> {
			if(newV.doubleValue() >= (scrollCatalogPane.getVmax() - 0.2)) {
				scrollCatalogPane.vvalueProperty().removeListener(scrollMaxEvent);
				startUpdateCatalogThread();
			}
		};
		scrollCatalogPane.vvalueProperty().addListener(scrollMaxEvent);
		mainTilePane.setPadding(new Insets(10, 10, 10, 10));
		mainTilePane.setHgap(10.0);
		mainTilePane.setVgap(10.0);
	}
	
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
	public void startUpdateCatalogThread() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				List<GridPane> gridItemsList = new ArrayList<>();
				for(SimpleVideoItem itemObj: mainApp.getCurrentParser().getVideoItemsByCategory(currentCategory, currentPage)) {
					gridItemsList.add(createGridForVideoItem(itemObj));
				}
				currentPage++;
				loadImagesAsync();
				Platform.runLater(new Runnable() {	
					@Override
					public void run() {
						mainTilePane.getChildren().addAll(gridItemsList);
						scrollCatalogPane.vvalueProperty().addListener(scrollMaxEvent);
						logger.info("Items loaded");
					}
				});
			}
		};
		if(!backgroundThread.isAlive()) {
			clearImageHolder();
			backgroundThread = new Thread(task);
			backgroundThread.setDaemon(true);
			backgroundThread.start();
		}
	}
	public void updateCatalogWithNewCategory(int category) {
		setCurrentCategory(category);
		mainTilePane.getChildren().clear();
		startUpdateCatalogThread();
	}
	private GridPane createGridForVideoItem(SimpleVideoItem itemObj) {
		GridPane videoItemPane = new GridPane();
		ImageView itemImage = new ImageView();//image
		itemImage.setFitWidth(135.0);
		itemImage.setFitHeight(180.0);
		itemImage.setUserData(itemObj.getPrevImg());
		addImageToHolder(itemImage);
		Label itemTitle = new Label(itemObj.getTitle());
		itemTitle.setMaxWidth(Double.MAX_VALUE);
		itemTitle.setAlignment(Pos.CENTER);
		itemTitle.setWrapText(true);
		itemTitle.setPrefWidth(100);
		itemTitle.setPrefHeight(50);
		itemTitle.setMaxHeight(100);
		videoItemPane.setAlignment(Pos.CENTER);
		videoItemPane.add(itemImage, 0, 0);
		videoItemPane.add(itemTitle, 0, 1);
		videoItemPane.setPrefWidth(150.0);
		//videoItemPane.setPrefHeight(200.0);
		videoItemPane.setUserData(itemObj);
		videoItemPane.setOnMouseClicked(new VideoItemClickedEventHandler());
		return videoItemPane;
	}
	private void loadImagesAsync() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for(ImageView imageV: imageHolder) {
					String imgLink = (String)imageV.getUserData();
					if(imgLink != null) {
						Image image = new Image(imgLink);
						if(image.isError())
							image = downloadImageWithHttpClient(imgLink);
						imageV.setImage(image);
					}
				}
			}
		}).start();
	}
	public void setCurrentCategory(int category) {
		this.currentPage = 1;
		this.currentCategory = category;
	}
	public int getCurrentCategory() {
		return currentCategory;
	}
	private synchronized void addImageToHolder(ImageView image) {
		imageHolder.add(image);
	}
	private synchronized void clearImageHolder() {
		imageHolder.clear();
	}
	private Image downloadImageWithHttpClient(String url) {
		Image image = null;
		try {
			HttpEntity entity = HttpClientBuilder.create().build().execute(new HttpGet(url)).getEntity();
			if(entity != null) {
				image = new Image(entity.getContent());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	private class VideoItemClickedEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent me) {
			logger.info("Item clicked: {}", ((SimpleVideoItem)((GridPane)me.getSource()).getUserData()).getTitle() );
			mainApp.openItemBrowser(((SimpleVideoItem)((GridPane)me.getSource()).getUserData()).getUrl());
		}
	}
}
