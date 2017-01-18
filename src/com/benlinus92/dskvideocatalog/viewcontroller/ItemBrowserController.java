package com.benlinus92.dskvideocatalog.viewcontroller;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.BrowserVideoItem;
import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ItemBrowserController {

	private MainApp mainApp;
	private Parser parser;
	private String currentItemUrl;
	@FXML private ImageView posterImage;
	@FXML private Label titleLabel;
	@FXML private Label genreLabel;
	@FXML private Label yearLabel;
	@FXML private Label countryLabel;
	@FXML private Label directorLabel;
	@FXML private Label translationLabel;
	@FXML private Label durationLabel;
	@FXML private Label castLabel;
	@FXML private Label plotLabel;
	
	@FXML public void initialize() {
		parser = new TreeTvParser();
	}
	public void updateItemBrowser() {
		BrowserVideoItem item = parser.getVideoItemByUrl(currentItemUrl);
		try {
			String temp = "";
			int ind = 0;
			for(String genre: item.getGenre()) {
				temp = temp + genre;
				if(ind < item.getGenre().size() - 1)
					temp = temp + ", ";
				ind++;
			}
			temp = "";
			ind = 0;
			for(String actor: item.getCast()) {
				temp = temp + actor;
				if(ind < item.getCast().size() - 1)
					temp = temp + ", ";
				ind++;
			}
			System.out.println(item.getPrevImg());
			posterImage.setImage(new Image(item.getPrevImg()));
			titleLabel.setText(item.getTitle());
			genreLabel.setText(temp);
			yearLabel.setText(item.getYear());
			countryLabel.setText(item.getCountry());
			directorLabel.setText(item.getDirector());
			translationLabel.setText(item.getTranslation());
			durationLabel.setText(item.getDuration());
			castLabel.setText(temp);
			plotLabel.setText(item.getPlot());
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	public void loadNewItemInBrowser(String url) {
		setCurrentItemUrl(url);
		updateItemBrowser();
	}
	public void setCurrentItemUrl(String url) {
		this.currentItemUrl = url;
	}
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
}
