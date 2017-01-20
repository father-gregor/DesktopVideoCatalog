package com.benlinus92.dskvideocatalog.viewcontroller;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

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
		
	}
	public void updateItemBrowser() {
		BrowserVideoItem item = mainApp.getCurrentParser().getVideoItemByUrl(currentItemUrl);
		try {
			String temp = "";
			int ind = 0;
			for(String genre: item.getGenre()) {
				temp = temp + genre;
				if(ind < item.getGenre().size() - 1)
					temp = temp + ", ";
				ind++;
			}
			genreLabel.setText(temp);
			temp = "";
			ind = 0;
			for(String actor: item.getCast()) {
				temp = temp + actor;
				if(ind < item.getCast().size() - 1)
					temp = temp + ", ";
				ind++;
			}
			castLabel.setText(temp);
			Image image = new Image(item.getPrevImg());
			if(image.isError())
				image = downloadImageWithHttpClient(item.getPrevImg());
			posterImage.setImage(image);
			titleLabel.setText(item.getTitle());
			yearLabel.setText(item.getYear());
			countryLabel.setText(item.getCountry());
			directorLabel.setText(item.getDirector());
			translationLabel.setText(item.getTranslation());
			durationLabel.setText(item.getDuration());
			plotLabel.setText(item.getPlot());
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	public void loadNewItemInBrowser(String url) {
		setCurrentItemUrl(url);
		updateItemBrowser();
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
	public void setCurrentItemUrl(String url) {
		this.currentItemUrl = url;
	}
	public void setMainApp(MainApp app) {
		this.mainApp = app;
	}
}
