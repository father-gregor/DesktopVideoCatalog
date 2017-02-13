package com.benlinus92.dskvideocatalog.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
		request.addHeader("User-Agent", AppConstants.MOBILE_USER_AGENT); 
		HttpResponse response = client.execute(request);
		System.out.println(EXFS_BASIC_URL + " -  status " + response.getStatusLine().getStatusCode());
		for(Header header: Arrays.asList(response.getHeaders("Set-Cookie"))) {
			if(header.getValue().contains("__cfduid=") && !cookieStr.contains("__cfduid=")) {
				cookieStr = cookieStr + header.getValue().split(";")[0] + "; ";
			} else if(header.getValue().contains("PHPSESSID=") && !cookieStr.contains("PHPSESSID=")) {
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
			for(Element elem: content.select("div#rightholder iframe")) {
				if(!elem.attr("src").isEmpty() && !elem.attr("src").contains("serial") && item != null)
					item.setVideoTransTypeList(getVideoTranslationList(url, elem.attr("src"))); //videolist can only be retrieved by different link
			}
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
		return item;
	}
	
	private List<VideoTranslationType> getVideoTranslationList(String originalUrl, String iframeLink) {
		List<VideoTranslationType> videoList = new ArrayList<>();
		System.out.println(iframeLink);
		try {
			Header userAgent = new BasicHeader("User-Agent", AppConstants.MOBILE_USER_AGENT);
			Header cookie = new BasicHeader("Cookie", cookieStr);
			Header referer = new BasicHeader("Referer", originalUrl);
			int timeout = 6;
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(1000 * timeout)
					.setConnectionRequestTimeout(1000 * timeout).build();
			HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			HttpGet getRequest = new HttpGet(iframeLink);
			getRequest.addHeader(userAgent);
			getRequest.addHeader(cookie);
			getRequest.addHeader(referer);
			HttpResponse response = client.execute(getRequest);
			for(Header header: Arrays.asList(response.getHeaders("Set-Cookie"))) {
				if(header.getValue().contains("_moon_session=") && !cookieStr.contains("_moon_session=")) {
					cookieStr = cookieStr + header.getValue().split(";")[0] + "; ";
					break;
				}
			}
			cookie = new BasicHeader("Cookie", cookieStr);
			referer = new BasicHeader("Referer", iframeLink);
			String webcontent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
			String videolistUrl = parseStringByTemplate("(http:\\/\\/[a-zA-Z0-9\\W]+?)\\/", iframeLink) + "/sessions/new_session";
			System.out.println("URL - " + videolistUrl);
			HttpPost postRequest = new HttpPost(videolistUrl);
			postRequest.setEntity(new UrlEncodedFormEntity(getFormDataForPostRequest(webcontent)));
			postRequest.addHeader(userAgent);
			postRequest.addHeader(cookie);
			postRequest.addHeader(referer);
			postRequest.addHeader("X-Condition-Safe", "Normal");
			postRequest.addHeader("X-Requested-With", "XMLHttpRequest");
			response = client.execute(postRequest);
			webcontent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
			JsonParser jsonP = new JsonParser();
			JsonObject jsonObj = jsonP.parse(webcontent).getAsJsonObject();
			//videoList.add(new VideoTranslationType().se)
			System.out.println(jsonObj.getAsJsonObject("mans").getAsJsonPrimitive("manifest_m3u8").getAsString());
			
		} catch(IOException | UnsupportedOperationException e) {
			e.printStackTrace();
		}
		return videoList;
	}
	private List<BasicNameValuePair> getFormDataForPostRequest(String webcontent) {
		List<BasicNameValuePair> list = new ArrayList<>();
		list.add(new BasicNameValuePair("video_token", parseStringByTemplate("video_token:\\W'([a-zA-Z0-9]+)'", webcontent)));
		list.add(new BasicNameValuePair("content_type", parseStringByTemplate("content_type:\\W'([a-zA-Z0-9]+)'", webcontent)));
		list.add(new BasicNameValuePair("mw_key", parseStringByTemplate("mw_key:\\W'([a-zA-Z0-9]+)'", webcontent)));
		list.add(new BasicNameValuePair("mw_pid", parseStringByTemplate("mw_pid:\\W([a-zA-Z0-9]+)", webcontent)));
		list.add(new BasicNameValuePair("p_domain_id", parseStringByTemplate("p_domain_id:\\W([a-zA-Z0-9]+)", webcontent)));
		list.add(new BasicNameValuePair("debug", parseStringByTemplate("debug:\\W([a-zA-Z0-9]+)", webcontent)));
		list.add(new BasicNameValuePair("condition_safe", parseStringByTemplate("condition_safe\\W=\\W'([a-zA-Z0-9]+)", webcontent)));
		list.add(new BasicNameValuePair("ad_attr:", "0"));
		return list;
	}
	private String parseStringByTemplate(String template, String content) {
		Pattern regex = Pattern.compile(template);
		Matcher m = regex.matcher(content);
		if(m.find()) {
			System.out.println(m.group(1));
			return m.group(1);
		}
		return "";
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
