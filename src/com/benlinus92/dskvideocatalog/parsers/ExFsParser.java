package com.benlinus92.dskvideocatalog.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.PropertiesHandler;
import com.benlinus92.dskvideocatalog.model.BrowserVideoItem;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.SimpleVideoItem;
import com.benlinus92.dskvideocatalog.model.VideoLink;
import com.benlinus92.dskvideocatalog.model.VideoTranslationType;

public class ExFsParser implements Parser {
	private final static String EXFS_FILMS_URL = "http://ex-fs.net/films/page/";
	private final static String EXFS_SERIES_URL = "http://ex-fs.net/series/page/";
	private final static String EXFS_CARTOONS_URL = "http://ex-fs.net/cartoon/page/";
	private final static String EXFS_BASIC_URL = "http://ex-fs.net";
	private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d.MM.yyyy");
	private Map<String, Integer> parserCategoryMap;
	private List<MediaStream> mediaStreamsList;
	private String cookieStr = "";
	
	public ExFsParser() {
		mediaStreamsList = new ArrayList<>();
		mediaStreamsList.add(MediaStream.HLS);
		parserCategoryMap = new LinkedHashMap<>();
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitFilmsName(), AppConstants.CATEGORY_FILMS);
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitSeriesName(), AppConstants.CATEGORY_SERIES);
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitCartoonsName(), AppConstants.CATEGORY_CARTOONS);
	}

	@Override
	public String getHtmlContent(String url) throws IOException {
		int timeout = 6;
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(1000 * timeout)
				.setConnectionRequestTimeout(1000 * timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		HttpGet request = new HttpGet(url);
		request.addHeader("User-Agent", AppConstants.USER_AGENT); 
		HttpResponse response = client.execute(request);
		System.out.println(EXFS_BASIC_URL + " -  status " + response.getStatusLine().getStatusCode());
		for(Header header: Arrays.asList(response.getHeaders("Set-Cookie"))) {
			if(header.getValue().contains("__cfduid=")) {
				cookieStr = cookieStr + header.getValue().split(";")[0] + "; ";
			} else if(header.getValue().contains("PHPSESSID=")) {
				cookieStr = cookieStr + header.getValue().split(";")[0] + "; ";
			}
		}
		System.out.println(cookieStr);
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
			String url = EXFS_FILMS_URL;
			if(category == AppConstants.CATEGORY_FILMS)
				url = EXFS_FILMS_URL;
			else if(category == AppConstants.CATEGORY_SERIES)
				url = EXFS_SERIES_URL;
			else if(category == AppConstants.CATEGORY_CARTOONS)
				url = EXFS_CARTOONS_URL;
			String html = getHtmlContent(url + Integer.toString(page) + "/");
			Document content = Jsoup.parse(html);
			for(Element elem: content.select("div.MiniPostAllForm")) {
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
		item.setTitle(el.select("div.MiniPostName").text());
		item.setUrl(el.select("div.MiniPostName a").attr("href"));
		item.setPrevImg(EXFS_BASIC_URL + el.select("a.MiniPostPoster img").attr("src"));
		item.setYear(el.select("div.MiniPostInfo a").attr("title"));
		String date = el.select("div.customInfo span").text().trim();
		if(date.contains("Сегодня"))
			item.setAddedDate(LocalDate.now());
		else if(date.contains("Вчера"))
			item.setAddedDate(LocalDate.now().minusDays(1));
		else
			item.setAddedDate(LocalDate.parse(date, DATE_FORMAT));
		return item;
	}

	@Override
	public BrowserVideoItem getVideoItemByUrl(String url) {
		BrowserVideoItem item = null;
		try {
			String html = getHtmlContent(url);
			Document content = Jsoup.parse(html);
			item = createBrowserVideoItemFromHtml(content.select("div#dle-content").first());
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
		item.setTitle(el.select("h1.view-caption").text());
		item.setPrevImg(EXFS_BASIC_URL + el.select("div.FullstoryFormLeft img").attr("src"));
		Elements infoElem = el.select("div.FullstoryInfo p");
		item.setYear(infoElem.get(0).text());
		item.setCountry(infoElem.get(1).text());
		List<String> list = new ArrayList<>();
		for(Element elem: infoElem.get(2).select("a"))
			list.add(elem.attr("title"));
		item.setGenre(list);
		item.setTranslation(infoElem.get(3).text());
		item.setDuration(infoElem.get(5).text());
		item.setPlot(el.select("div.FullstorySubFormText").text());
		list = new ArrayList<>();
		for(Element elem: el.select("div.FullstoryKadrFormImgAc img"))
			list.add(elem.attr("title"));
		item.setCast(list);
		for(Element elem: el.select("div.TabDopInfoBlockOne"))
			if(elem.html().contains("Режиссер")) {
				item.setDirector(elem.select("a").text());
				break;
			}
		item.setVideoTransTypeList(getVideoTranslationList(el.select("div#rightholder iframe").attr("src")));
		return item;
	}
	
	private List<VideoTranslationType> getVideoTranslationList(String iframeLink) {
		List<VideoTranslationType> videoList = new ArrayList<>();
		System.out.println(iframeLink);
		try {
			Header userAgent = new BasicHeader("User-Agent", AppConstants.MOBILE_USER_AGENT);
			Header cookie = new BasicHeader("Cookie", cookieStr);
			int timeout = 6;
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(1000 * timeout)
					.setConnectionRequestTimeout(1000 * timeout).build();
			HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			HttpGet request = new HttpGet(iframeLink);
			request.addHeader(userAgent);
			request.addHeader(cookie);
			HttpResponse response = client.execute(request);
			String webcontent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
			System.out.println(webcontent);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return videoList;
	}
	private Map<String, String> getHlsStreamMap(String link) {
		Map<String, String> videoMap = new LinkedHashMap<>();
		return videoMap;
	}
	@Override
	public Map<String, String> getVideoStreamMap(VideoLink video, MediaStream type) {
		Map<String, String> availableVideoMap = new LinkedHashMap<>();
		getHlsStreamMap(video.getLink());
		return null;
	}

	@Override
	public List<MediaStream> getMediaStreamsList() {
		return mediaStreamsList;
	}

	@Override
	public Map<String, Integer> getParserCategoryMap() {
		return parserCategoryMap;
	}

	@Override
	public String getWebSiteName() {
		return "EX-FS.net";
	}

}
