package com.benlinus92.dskvideocatalog.parsers;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.benlinus92.dskvideocatalog.model.BrowserVideoItem;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.SimpleVideoItem;

public interface Parser {
	public String getHtmlContent(String url) throws IOException;
	public List<SimpleVideoItem> getVideoItemsByCategory(int category, int page);
	public BrowserVideoItem getVideoItemByUrl(String url);
	public SimpleVideoItem createCatalogVideoItemFromHtml(Element el);
	public BrowserVideoItem createBrowserVideoItemFromHtml(Element el);
	public List<MediaStream> getMediaStreamsList();
}
