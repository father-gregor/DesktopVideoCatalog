package com.benlinus92.dskvideocatalog.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.model.VideoItem;

public class TreeTvParser implements Parser {
	private final static String TREE_TV_FILMS_URL = "http://tree.tv/films/sortType/new/page/";
	private final static String TREE_TV_SERIES_URL = "http://tree.tv/serials/sortType/new/page/";
	private final static String TREE_TV_MULTFILMS = "http://tree.tv/multfilms/sortType/new/page/";
	private final static String TREE_TV_BASIC_URL = "http://tree.tv";
	private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final static String ID_SAMPLE = "TREE_TV_";
	
	@Override
	public String getHtmlContent(String url) throws IOException, ClientProtocolException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.addHeader("User-Agent", AppConstants.USER_AGENT); 
		HttpResponse response = client.execute(request);
		System.out.println(response.getStatusLine().getStatusCode());
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while((line = br.readLine()) != null)
			sb.append(line);
		return sb.toString();
	}
	@Override
	public List<VideoItem> getVideoItemsByCategory(int category, int page) {
		List<VideoItem> itemsList = new ArrayList<>();
		try {
			String url = "";
			if(category == AppConstants.CATEGORY_FILMS)
				url = TREE_TV_FILMS_URL;
			else if(category == AppConstants.CATEGORY_SERIES)
				url = TREE_TV_SERIES_URL;
			else if(category == AppConstants.CATEGORY_MULTFILMS)
				url = TREE_TV_MULTFILMS;
			else
				url = TREE_TV_FILMS_URL;
			String html = getHtmlContent(url + Integer.toString(page));
			Document content = Jsoup.parse(html.toString());
			Elements elems = content.select("div.item");
			for(Element elem: elems) {
				itemsList.add(createVideoItemFromHtml(elem));
			}
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} 
		return itemsList;
	}
	@Override
	public VideoItem createVideoItemFromHtml(Element el) {
		VideoItem item = new VideoItem();
		Element tempElem = el.select("h2").first();
		item.setTitle(tempElem.text());
		item.setUrl(TREE_TV_BASIC_URL + tempElem.select("a").attr("href"));
		for(Element elem: el.select("img")) {
			if(elem.attr("title").length() > 0) {
				item.setPrevImg(elem.attr("src"));
				break;
			}
		}
		item.setYear(el.select("div.smoll_year").text());
		item.setAddedDate(LocalDate.parse(el.select("div.date_create span").text(), DATE_FORMAT));
		item.setId(ID_SAMPLE);
		return item;
	}

}
