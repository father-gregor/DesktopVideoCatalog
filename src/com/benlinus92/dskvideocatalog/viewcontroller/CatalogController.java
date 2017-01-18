package com.benlinus92.dskvideocatalog.viewcontroller;

import java.util.ArrayList;
import java.util.List;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.SimpleVideoItem;
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
		scrollCatalogPane.vvalueProperty().addListener((ObservableValue<? extends Number> ov, Number oldV, Number newV) -> {
			if(newV.doubleValue() >= (scrollCatalogPane.getVmax() - 0.05)) {
				System.out.println(scrollCatalogPane.getVmax());
				updateCatalog();
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
		updateCatalog();
	}
	
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
	public void updateCatalog() {
		List<SimpleVideoItem> items = parser.getVideoItemsByCategory(currentCategory, currentPage);
		List<GridPane> gridItemsList = new ArrayList<>();
		for(SimpleVideoItem itemObj: items) {
			gridItemsList.add(createGridForVideoItem(itemObj));
		}
		currentPage++;
		mainTilePane.getChildren().addAll(gridItemsList);
	}
	public void updateCatalogWithNewCategory(int category) {
		setCurrentCategory(category);
		mainTilePane.getChildren().clear();
		updateCatalog();
	}
	private GridPane createGridForVideoItem(SimpleVideoItem itemObj) {
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
	public void setCurrentCategory(int category) {
		this.currentPage = 1;
		this.currentCategory = category;
	}
	private class VideoItemClickedEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent me) {
			System.out.println("Clicked");
			System.out.println( ((SimpleVideoItem)((GridPane)me.getSource()).getUserData()).getTitle() );
		}
	}
}
