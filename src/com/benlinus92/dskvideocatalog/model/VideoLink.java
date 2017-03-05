package com.benlinus92.dskvideocatalog.model;

/*
 * Store link to actual videofile or playlist media of item.
 */
public class VideoLink {
	private String id = "";
	private String videoName = "";
	private String link = "";
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVideoName() {
		return videoName;
	}
	public void setName(String videoName) {
		this.videoName = videoName;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
}
