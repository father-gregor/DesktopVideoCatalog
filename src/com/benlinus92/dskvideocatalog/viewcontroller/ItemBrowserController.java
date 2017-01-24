package com.benlinus92.dskvideocatalog.viewcontroller;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.benlinus92.dskvideocatalog.MainApp;
import com.benlinus92.dskvideocatalog.model.BrowserVideoItem;
import com.benlinus92.dskvideocatalog.model.VideoLink;
import com.benlinus92.dskvideocatalog.model.VideoTranslationType;
import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ItemBrowserController {

	private MainApp mainApp;
	private String currentItemUrl;
	@FXML private TabPane tabPane;
	@FXML private ScrollPane linksPane;
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
	private void updateItemBrowser() {
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
			posterImage.setOnMouseClicked(e -> {
				mainApp.initImageViewerWindow(((ImageView)e.getSource()).getImage());
			});
			titleLabel.setText(item.getTitle());
			yearLabel.setText(item.getYear());
			countryLabel.setText(item.getCountry());
			directorLabel.setText(item.getDirector());
			translationLabel.setText(item.getTranslation());
			durationLabel.setText(item.getDuration());
			plotLabel.setText(item.getPlot());
			VBox vbox = new VBox();
			for(VideoTranslationType typeItem: item.getVideoTransTypeList()) {
				BorderPane typePane = new BorderPane();
				typePane.setLeft(new Label(typeItem.getType()));
				typePane.setUserData(typeItem.getVideosList());
				typePane.setOnMouseClicked(new VideoTypeLinksOpenedEventHandler());
				vbox.getChildren().add(typePane);
			}
			linksPane.setContent(vbox);
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	public void loadNewItemInBrowser(String url) {
		tabPane.getSelectionModel().select(0);//select first tab always
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
	private class VideoTypeLinksOpenedEventHandler implements EventHandler<MouseEvent> {
		@SuppressWarnings("unchecked")
		@Override
		public void handle(MouseEvent me) {
			BorderPane videoLink = (BorderPane)me.getSource();
			videoLink.removeEventHandler(MouseEvent.MOUSE_CLICKED, videoLink.getOnMouseClicked());
			int videoLinkIndex = ((VBox)videoLink.getParent()).getChildren().indexOf(videoLink);
			VBox linksVBox = new VBox();
			for(VideoLink link: (List<VideoLink>)videoLink.getUserData()) {
				BorderPane linkPane = new BorderPane();
				linkPane.setLeft(new Label(link.getVideoName()));
				linkPane.setRight(new Hyperlink(link.getLink()));
				linksVBox.getChildren().add(linkPane);
			}
			videoLink.setOnMouseClicked(new VideoTypeLinksClosedEventHandler());
			((VBox)videoLink.getParent()).getChildren().add(videoLinkIndex + 1, linksVBox);
		}
	}
	private class VideoTypeLinksClosedEventHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent me) {
			BorderPane videoLink = (BorderPane)me.getSource();
			videoLink.removeEventHandler(MouseEvent.MOUSE_CLICKED, videoLink.getOnMouseClicked());
			int videoLinkIndex = ((VBox)videoLink.getParent()).getChildren().indexOf(videoLink);
			((VBox)videoLink.getParent()).getChildren().remove(videoLinkIndex + 1); //remove next element in VBox, which is expanded 'links list'
			videoLink.setOnMouseClicked(new VideoTypeLinksOpenedEventHandler());
		}
	}
}
