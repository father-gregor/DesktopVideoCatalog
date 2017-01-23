package com.benlinus92.dskvideocatalog.model;

import java.util.ArrayList;
import java.util.List;

public class VideoTranslationType {
	private String type = "";
	private List<VideoLink> videosList = new ArrayList<>();
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<VideoLink> getVideosList() {
		return videosList;
	}
	public void setVideosList(List<VideoLink> videosList) {
		this.videosList = videosList;
	}
	public void addVideoLink(VideoLink link) {
		this.videosList.add(link);
	}
}
