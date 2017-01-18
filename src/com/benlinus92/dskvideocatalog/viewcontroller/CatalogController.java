package com.benlinus92.dskvideocatalog.viewcontroller;

import java.util.ArrayList;
import java.util.List;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.VideoItem;
import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;

public class CatalogController {
	
	private MainApp mainApp;
	@FXML
	private TilePane mainTilePane;
	@FXML 
	private GridPane gridSample;
	@FXML
	private ScrollPane scrollCatalogPane;
	private Parser parser = new TreeTvParser();
	private int currentCategory = AppConstants.CATEGORY_FILMS;
	private int currentPage = 1;
	
	public CatalogController() {
		
	}
	
	@FXML
	private void initialize() {
		List<VideoItem> initialItems = parser.getVideoItemsByCategory(currentCategory, currentPage);
		List<GridPane> gridItemsList = new ArrayList<>();
		for(VideoItem itemObj: initialItems) {
			gridItemsList.add(createGridForVideoItem(itemObj));
		}
		scrollCatalogPane.vvalueProperty().addListener((ObservableValue<? extends Number> ov, Number oldV, Number newV) -> {
			if(newV.doubleValue() >= (scrollCatalogPane.getVmax() - 0.05)) {
				System.out.println(scrollCatalogPane.getVmax());
				List<VideoItem> items = parser.getVideoItemsByCategory(currentCategory, currentPage);
				List<GridPane> innerGridItemsList = new ArrayList<>();
				for(VideoItem itemObj: items) {
					innerGridItemsList.add(createGridForVideoItem(itemObj));
				}
				currentPage++;
				mainTilePane.getChildren().addAll(innerGridItemsList);
			}
		});
		/*scrollCatalogPane.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent se) {
				if(scrollCatalogPane.getVvalue() >= scrollCatalogPane.getVmax())
					System.out.println("MAX VALUE");
			}
		});*/
		mainTilePane.setPadding(new Insets(10, 10, 10, 10));
		mainTilePane.setHgap(10.0);
		mainTilePane.setVgap(10.0);
		currentPage++;
		mainTilePane.getChildren().addAll(gridItemsList);
	}
	
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
	private GridPane createGridForVideoItem(VideoItem itemObj) {
		GridPane videoItemPane = new GridPane();
		ImageView itemImage = new ImageView(itemObj.getPrevImg());
		itemImage.setFitWidth(135.0);
		itemImage.setFitHeight(180.0);
		Label itemTitle = new Label(itemObj.getTitle());
		itemTitle.setMaxWidth(Double.MAX_VALUE);
		itemTitle.setAlignment(Pos.CENTER);
		itemTitle.setWrapText(true);
		itemTitle.setPrefWidth(100);
		itemTitle.setPrefHeight(200);
		videoItemPane.setAlignment(Pos.CENTER);
		videoItemPane.add(itemImage, 0, 0);
		videoItemPane.add(itemTitle, 0, 1);
		videoItemPane.setPrefWidth(150.0);
		videoItemPane.setPrefHeight(200.0);
		videoItemPane.setUserData(itemObj);
		videoItemPane.setOnMouseClicked(new VideoItemClickedEventHandler());
		return videoItemPane;
	}
	private class VideoItemClickedEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent me) {
			System.out.println("Clicked");
			System.out.println( ((VideoItem)((GridPane)me.getSource()).getUserData()).getTitle() );
		}
	}
}
