package com.benlinus92.dskvideocatalog.parsers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.benlinus92.dskvideocatalog.model.BrowserVideoItem;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.SimpleVideoItem;
import com.benlinus92.dskvideocatalog.model.VideoLink;

public interface Parser {
	public String getHtmlContent(String url) throws IOException;
	public List<SimpleVideoItem> getVideoItemsByCategory(int category, int page);
	public BrowserVideoItem getVideoItemByUrl(String url);
	public SimpleVideoItem createCatalogVideoItemFromHtml(Element el);
	public BrowserVideoItem createBrowserVideoItemFromHtml(Element el);
	public Map<String, String> getVideoStreamMap(VideoLink video, MediaStream type);
	public List<MediaStream> getMediaStreamsList();
}
