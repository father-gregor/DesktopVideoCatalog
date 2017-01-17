package com.benlinus92.dskvideocatalog.parsers;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Element;

import com.benlinus92.dskvideocatalog.model.VideoItem;

public interface Parser {
	public String getHtmlContent(String url) throws IOException;
	public List<VideoItem> getVideoItemsByCategory(int category, int page);
	public VideoItem createVideoItemFromHtml(Element el);
}
