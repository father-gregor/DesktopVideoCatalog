package com.benlinus92.dskvideocatalog.parsers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.benlinus92.dskvideocatalog.model.BrowserVideoItem;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.SimpleVideoItem;
import com.benlinus92.dskvideocatalog.model.VideoLink;

public abstract class Parser {
	protected Map<String, Integer> parserCategoryMap;
	protected List<MediaStream> mediaStreamsList;
	public abstract String getHtmlContent(String url) throws IOException;
	public abstract List<SimpleVideoItem> getVideoItemsByCategory(int category, int page);
	public abstract BrowserVideoItem getVideoItemByUrl(String url);
	public abstract SimpleVideoItem createCatalogVideoItemFromHtml(Element el);
	public abstract BrowserVideoItem createBrowserVideoItemFromHtml(Element el);
	public abstract Map<String, String> getVideoStreamMap(VideoLink video, MediaStream type);
	public abstract String getWebSiteName();
	
	protected HttpClient getHttpClient(int timeout) {
		return HttpClientBuilder.create().setDefaultRequestConfig(
				RequestConfig.custom()
				.setConnectTimeout(1000 * timeout)
				.setConnectionRequestTimeout(1000 * timeout).build())
				.build();
	}
	public List<MediaStream> getMediaStreamsList() {
		return mediaStreamsList;
	}
	
	public Map<String, Integer> getParserCategoryMap() {
		return parserCategoryMap;
	}
}
