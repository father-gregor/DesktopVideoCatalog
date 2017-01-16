package com.benlinus92.dskvideocatalog.parsers;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Element;

import com.benlinus92.dskvideocatalog.model.VideoItem;

public interface Parser {
	public String getHtmlContent() throws IOException;
	public void parseHtml();
	public VideoItem createVideoItemFromHtml(Element el);
}
