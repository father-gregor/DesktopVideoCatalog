package com.benlinus92.dskvideocatalog.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.model.BrowserVideoItem;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.SimpleVideoItem;
import com.benlinus92.dskvideocatalog.model.VideoLink;
import com.benlinus92.dskvideocatalog.model.VideoTranslationType;

public class TreeTvParser implements Parser {
	private final static String TREE_TV_FILMS_URL = "http://tree.tv/films/sortType/new/page/";
	private final static String TREE_TV_SERIES_URL = "http://tree.tv/serials/sortType/new/page/";
	private final static String TREE_TV_CARTOONS = "http://tree.tv/multfilms/sortType/new/page/";
	private final static String TREE_TV_BASIC_URL = "http://tree.tv";
	private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final static String ID_SAMPLE = "TREE_TV_";
	private List<MediaStream> mediaStreamsList;
	
	public TreeTvParser() {
		mediaStreamsList = new ArrayList<>();
		mediaStreamsList.add(MediaStream.MP4);
		mediaStreamsList.add(MediaStream.HLS);
	}
	
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
	public List<SimpleVideoItem> getVideoItemsByCategory(int category, int page) {
		List<SimpleVideoItem> itemsList = new ArrayList<>();
		try {
			String url = TREE_TV_FILMS_URL;
			if(category == AppConstants.CATEGORY_FILMS)
				url = TREE_TV_FILMS_URL;
			else if(category == AppConstants.CATEGORY_SERIES)
				url = TREE_TV_SERIES_URL;
			else if(category == AppConstants.CATEGORY_CARTOONS)
				url = TREE_TV_CARTOONS;
			String html = getHtmlContent(url + Integer.toString(page));
			Document content = Jsoup.parse(html);
			for(Element elem: content.select("div.item")) {
				itemsList.add(createCatalogVideoItemFromHtml(elem));
			}
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} 
		return itemsList;
	}
	@Override
	public SimpleVideoItem createCatalogVideoItemFromHtml(Element el) {
		SimpleVideoItem item = new SimpleVideoItem();
		item.setTitle(el.select("h2").first().text());
		item.setUrl(TREE_TV_BASIC_URL + el.select("h2").first().select("a").attr("href"));
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
	private void testLinkParser() {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(TREE_TV_BASIC_URL + "/film/index/link");
			request.addHeader("Referer", "http://tree.tv/film/23050-fantasticheskie-tvari-i-gde-oni-obitayut");
			request.addHeader("User-agent", AppConstants.USER_AGENT);
			request.addHeader("X-Requested-With:", "XMLHttpRequest");
			request.addHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
			request.addHeader("Host", "tree.tv");
			request.addHeader("Origin", "http://tree.tv");
			//request.addHeader("Cookie", "");
			List<BasicNameValuePair> list = new ArrayList<>();
			list.add(new BasicNameValuePair("quality", "480"));
			list.add(new BasicNameValuePair("file", "172171"));
			request.setEntity(new UrlEncodedFormEntity(list));
			HttpResponse response = client.execute(request);
			System.out.println("GET LINK: " + response.getStatusLine().getStatusCode());
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String line = null;
			while((line = br.readLine()) != null)
				System.out.println(line);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public BrowserVideoItem getVideoItemByUrl(String url) {
		BrowserVideoItem item = null;
		try {
			String html = getHtmlContent(url);
			Document content = Jsoup.parse(html);
			testLinkParser();
			item = createBrowserVideoItemFromHtml(content.select("div.main_bg").first());
		} catch(ClientProtocolException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return item;
	}
	@Override
	public BrowserVideoItem createBrowserVideoItemFromHtml(Element el) {
		BrowserVideoItem item = new BrowserVideoItem();
		item.setTitle(el.select("h1#film_object_name").text());
		item.setPrevImg(el.select("img#preview_img").attr("src"));
		List<String> list = new ArrayList<>();
		for(Element elem: el.select("a[data-rel='janrs[]']"))
			list.add(elem.text());
		item.setGenre(list);
		item.setYear(el.select("div.list_year a").text());
		item.setCountry(el.select("span.item").text());
		item.setDirector(el.select("span.regiser_item").text());
		item.setTranslation(el.select("div.section_item:contains(Перевод:)").select("span:not(.main)").text());
		item.setDuration(el.select("div.section_item:contains(Длительность:)").select("span:not(.main)").text());
		list = new ArrayList<>();
		for(Element elem: el.select("div.actors_content"))
			list.add(elem.text());
		item.setCast(list);
		item.setPlot(el.select("div.description").text());
		
		int transItemsSize = el.select("div.accordion div.accordion_item").size();
		List<VideoTranslationType> typeList = new ArrayList<>();
		for(int i = 0; i < transItemsSize; i++) {
			VideoTranslationType newItem = new VideoTranslationType();
			newItem.setType(el.select("div.accordion_head").get(i).select("a").attr("title"));
			for(Element e: el.select("div.accordion_content").get(i).select("div.accordion_content_item")) {
				VideoLink newLink = new VideoLink();
				newLink.setName(e.select("div.film_title a").text());
				newLink.setLink(TREE_TV_BASIC_URL + e.select("div.film_title a").attr("data-href"));
				newLink.setId(Jsoup.parse(e.outerHtml()).select("div.accordion_content_item").attr("data-file_id"));
				newItem.addVideoLink(newLink);
			}
			typeList.add(newItem);
		}
		item.setVideoTransTypeList(typeList);
		return item;
	}

	@Override
	public List<MediaStream> getMediaStreamsList() {
		return mediaStreamsList;
	}

}
