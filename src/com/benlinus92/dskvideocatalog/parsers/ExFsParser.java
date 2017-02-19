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
	private final static String EXFS_PLAYLIST_URL = 
			"http://cdn.ex-fs.net/video/VIDEOTOKEN/index.m3u8?cd=0&mw_pid=PIDTOKEN&man_type=reorder2&man_arg=2";
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
					item.setVideoTransTypeList(getVideoTranslationList(url, elem.attr("src"), item.getTitle() + " (" + item.getYear() + ")"));
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
		if(infoElem.size() > 5)
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
	
	private List<VideoTranslationType> getVideoTranslationList(String originalUrl, String iframeLink, String videoItemName) {
		List<VideoTranslationType> availablePlaylist = new ArrayList<>();
		try {
			int timeout = 6;
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(1000 * timeout)
					.setConnectionRequestTimeout(1000 * timeout).build();
			HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			HttpGet getRequest = new HttpGet(iframeLink);
			getRequest.addHeader(new BasicHeader("User-Agent", AppConstants.MOBILE_USER_AGENT));
			getRequest.addHeader(new BasicHeader("Cookie", cookieStr));
			getRequest.addHeader(new BasicHeader("Referer", originalUrl));
			HttpResponse response = client.execute(getRequest);
			String respContent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
			VideoLink video = new VideoLink();
			video.setLink(getPlaylistURL(respContent));
			video.setName(videoItemName + "- index.m3u8");
			VideoTranslationType videoList = new VideoTranslationType();
			videoList.addVideoLink(video);
			videoList.setTranslationName(videoItemName);
			availablePlaylist.add(videoList);
			
		} catch(IOException | UnsupportedOperationException e) {
			e.printStackTrace();
		}
		return availablePlaylist;
	}
	private String getPlaylistURL(String webcontent) {
		String mwPid = parseStringByTemplate("mw_pid:\\W([a-zA-Z0-9]+)", webcontent);
		String videoToken = parseStringByTemplate("video_token:\\W'([a-zA-Z0-9]+)'", webcontent);
		return EXFS_PLAYLIST_URL.replaceFirst("VIDEOTOKEN", videoToken).replaceFirst("PIDTOKEN", mwPid);
	}
	private String parseStringByTemplate(String template, String content) {
		Pattern regex = Pattern.compile(template);
		Matcher m = regex.matcher(content);
		if(m.find()) {
			return m.group(1);
		}
		return "";
	}
	private Map<String, String> getHlsStreamMap(String link) {
		Map<String, String> availablePlaylist = new LinkedHashMap<>();
		try {
			int timeout = 6;
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(1000 * timeout)
					.setConnectionRequestTimeout(1000 * timeout).build();
			HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			HttpGet request = new HttpGet(link);
			request.addHeader("User-Agent", AppConstants.MOBILE_USER_AGENT);
			HttpResponse response = client.execute(request);
			String respContent = EntityUtils.toString(response.getEntity());
			System.out.println("Check - " + respContent);
			for(String line: respContent.split("#EXT-X-STREAM-INF:")) {
				Pattern regex = Pattern.compile("RESOLUTION=[0-9]+?x([0-9]+)");
				Matcher m = regex.matcher(line);
				System.out.println(line + " size - " + availablePlaylist.size());
				if(m.find()) {
					availablePlaylist.put(m.group(1), line.substring(line.indexOf("http://")).trim());
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return availablePlaylist;
	}
	@Override
	public Map<String, String> getVideoStreamMap(VideoLink video, MediaStream type) {
		return getHlsStreamMap(video.getLink());
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
