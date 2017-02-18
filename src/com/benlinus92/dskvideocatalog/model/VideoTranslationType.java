package com.benlinus92.dskvideocatalog.model;

import java.util.ArrayList;
import java.util.List;

public class VideoTranslationType {
	private String translation = "";
	private List<VideoLink> videosList = null;
	public String getType() {
		return translation;
	}
	public void setType(String type) {
		this.translation = type;
	}
	public List<VideoLink> getVideosList() {
		if(videosList == null)
			videosList = new ArrayList<>();
		return videosList;
	}
	public void setVideosList(List<VideoLink> videosList) {
		this.videosList = new ArrayList<>(videosList);
	}
	public void addVideoLink(VideoLink link) {
		if(videosList == null)
			videosList = new ArrayList<>();
		this.videosList.add(link);
	}
}
