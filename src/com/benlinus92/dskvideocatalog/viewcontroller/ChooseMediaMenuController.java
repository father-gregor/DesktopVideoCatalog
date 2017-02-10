package com.benlinus92.dskvideocatalog.viewcontroller;

import com.benlinus92.dskvideocatalog.PropertiesHandler;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;

public class ChooseMediaMenuController {

	private VideoListController mainCaller;
	@FXML
	private Label internalPlayerLabel;
	@FXML
	private Label webPlayerLabel;
	@FXML
	private Label userDefaultPlayerLabel;
	@FXML
	private Label copyLinkLabel;
	@FXML
	public void initialize() {
		internalPlayerLabel.setText(PropertiesHandler.getInstance().getInternalPlayerLabel());
		webPlayerLabel.setText(PropertiesHandler.getInstance().getWebPlayerLabel());
		userDefaultPlayerLabel.setText(PropertiesHandler.getInstance().getDownloadLinkLabel());
		copyLinkLabel.setText(PropertiesHandler.getInstance().getCopyLinkLabel());
	}
	public void initializeMenuItems() {
		internalPlayerLabel.setOnMouseClicked(me -> {
			if(me.getButton().toString() == MouseButton.PRIMARY.toString()) {
				System.out.println("INTERNAL PLAYER");
				mainCaller.openInternalVideoStreamPlayer();
			}
		});
		webPlayerLabel.setOnMouseClicked(me -> {
			if(me.getButton().toString() == MouseButton.PRIMARY.toString()) {
				System.out.println("WEB PLAYER");
				mainCaller.openWebPlayer();
			}
		});
		userDefaultPlayerLabel.setOnMouseClicked(me -> {
			if(me.getButton().toString() == MouseButton.PRIMARY.toString()) {
				System.out.println("DEFAULT PLAYER");
				mainCaller.openUserDefaultPlayer();
			}
		});
		copyLinkLabel.setOnMouseClicked(me -> {
			if(me.getButton().toString() == MouseButton.PRIMARY.toString()) {
				System.out.println("COPY LINK");
				mainCaller.copyLinkToClipboard();
			}
		});
	}
	public void setMainCaller(VideoListController mainCaller) {
		this.mainCaller = mainCaller;
	}
}
