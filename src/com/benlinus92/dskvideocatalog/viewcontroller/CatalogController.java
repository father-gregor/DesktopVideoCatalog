package com.benlinus92.dskvideocatalog.viewcontroller;

import java.util.ArrayList;
import java.util.List;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.VideoItem;
import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;

public class CatalogController {
	
	private MainApp app;
	@FXML
	private TilePane mainTilePane;
	@FXML 
	private GridPane gridSample;
	public CatalogController() {
		
	}
	@FXML
	private void initialize() {
		System.out.println("Initialize");
		List<VideoItem> initialItems = loadDataForCatalog();
		List<GridPane> gridItemsList = new ArrayList<>();
		for(VideoItem itemObj: initialItems) {
			GridPane samplePane = new GridPane();
			ImageView itemImage = new ImageView(itemObj.getPrevImg());
			itemImage.setFitWidth(135.0);
			itemImage.setFitHeight(180.0);
			Label itemTitle = new Label(itemObj.getTitle());
			itemTitle.setMaxWidth(Double.MAX_VALUE);
			itemTitle.setAlignment(Pos.CENTER);
			samplePane.setAlignment(Pos.CENTER);
			samplePane.add(itemImage, 0, 0);
			samplePane.add(itemTitle, 0, 1);
			samplePane.setStyle("-fx-border-color: blue;");
			samplePane.setStyle("-fx-border-style: solid inside;");
			samplePane.setStyle("-fx-border-radius: 5;");
			samplePane.setStyle("-fx-border-width: 2;");
			samplePane.setPrefWidth(150.0);
			samplePane.setPrefHeight(200.0);
			gridItemsList.add(samplePane);
		}
		mainTilePane.setPadding(new Insets(10, 10, 10, 10));
		mainTilePane.setPrefColumns(3);
		mainTilePane.getChildren().addAll(gridItemsList);
	}
	
	public void setMainApp(MainApp app) {
		this.app = app;
	}
	private List<VideoItem> loadDataForCatalog() {
		Parser parser = new TreeTvParser();
		return parser.parseHtml();
	}
}
