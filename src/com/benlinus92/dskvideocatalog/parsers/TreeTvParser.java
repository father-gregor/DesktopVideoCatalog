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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class TreeTvParser implements Parser {
	private final static String TREE_TV_FILMS_URL = "http://tree.tv/films/sortType/new/page/";
	private final static String TREE_TV_SERIES_URL = "http://tree.tv/serials/sortType/new/page/";
	private final static String TREE_TV_CARTOONS = "http://tree.tv/multfilms/sortType/new/page/";
	private final static String TREE_TV_BASIC_URL = "http://tree.tv";
	private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private Map<String, Integer> parserCategoryMap;
	private List<MediaStream> mediaStreamsList;
	private List<String> qualityList = Arrays.asList("360", "480", "720", "1080");
	private int sessionUserId = 203; //basic session id (minimal working number)
	private String sessionKey = "";
	private String sessionPHPSessid = "";
	
	public TreeTvParser() {
		sessionUserId = generateUserId(); 
		mediaStreamsList = Arrays.asList(MediaStream.MP4, MediaStream.HLS);
		parserCategoryMap = new LinkedHashMap<>();
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitFilmsName(), AppConstants.CATEGORY_FILMS);
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitSeriesName(), AppConstants.CATEGORY_SERIES);
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitCartoonsName(), AppConstants.CATEGORY_CARTOONS);
	}
	
	@Override
	public String getHtmlContent(String url) throws IOException, ClientProtocolException {
		int timeout = 6;
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(1000 * timeout)
				.setConnectionRequestTimeout(1000 * timeout).build();
		HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		HttpGet request = new HttpGet(url);
		request.addHeader("User-Agent", AppConstants.USER_AGENT); 
		HttpResponse response = client.execute(request);
		System.out.println(TREE_TV_BASIC_URL + " -  status " + response.getStatusLine().getStatusCode());
		for(Header header: Arrays.asList(response.getHeaders("Set-Cookie"))) {
			if(header.getValue().contains("key=")) {
				sessionKey = header.getValue().split(";")[0];
			} else if(header.getValue().contains("PHPSESSID=")) {
				sessionPHPSessid = header.getValue().split(";")[0];
			}
		}
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
		return item;
	}
	private String getMp4StreamUrl(String videoId, String quality) {
		String fileLink = "";
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(TREE_TV_BASIC_URL + "/film/index/link");
			request.addHeader("User-agent", AppConstants.USER_AGENT);
			request.addHeader("Cookie", "user_id=" + sessionUserId);
			List<BasicNameValuePair> list = new ArrayList<>();
			list.add(new BasicNameValuePair("quality", quality));
			list.add(new BasicNameValuePair("file", videoId));
			request.setEntity(new UrlEncodedFormEntity(list));
			HttpResponse response = client.execute(request);
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = null;
			while((line = br.readLine()) != null) {
				if(line.contains("http://")) {
					fileLink = line;
					break;
				}
			}
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
			sessionUserId = generateUserId(); //in case of exception generate new user id which is not compromised
		}
		return fileLink;
	}
	/**
	 * Method implements exactly the same logic as correspondent
	 * method in website's app.js to get URL for M3U8-playlist
	 **/
	private Map<String, String> getHlsStreamMap(String videoId) {
		long begin = System.currentTimeMillis();
		String playlistLink = "";
		Map<String, String> availablePlaylist = new LinkedHashMap<>();
		try {
			Header userAgent = new BasicHeader("User-Agent", AppConstants.USER_AGENT);
			Header cookie = new BasicHeader("Cookie", sessionPHPSessid + "; " + sessionKey);
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost postRequest = new HttpPost("http://player.tree.tv/guard");
			postRequest.addHeader(userAgent);
			postRequest.addHeader(cookie);
			List<BasicNameValuePair> list = new ArrayList<>();
			list.add(new BasicNameValuePair("key", "55"));
			postRequest.setEntity(new UrlEncodedFormEntity(list));
			HttpResponse response = client.execute(postRequest);
			JsonParser jsonP = new JsonParser();
			JsonObject jsonObj = jsonP.parse(EntityUtils.toString(response.getEntity())).getAsJsonObject();
			String gParam = jsonObj.get("g").getAsString();
			String pParam = jsonObj.get("p").getAsString();
			int paramKey = (int)Math.random()*6 + 1;
			int clientKey = ((int)Math.pow(Double.parseDouble(gParam), (double)paramKey)) % Integer.parseInt(pParam);
			
			postRequest = new HttpPost("http://player.tree.tv/guard");
			postRequest.addHeader(userAgent);
			postRequest.addHeader(cookie);
			list.clear();
			list.add(new BasicNameValuePair("key", String.valueOf(clientKey)));
			postRequest.setEntity(new UrlEncodedFormEntity(list));
			response = client.execute(postRequest);
			jsonObj = jsonP.parse(EntityUtils.toString(response.getEntity())).getAsJsonObject();
			String s_keyParam = jsonObj.get("s_key").getAsString();
			System.out.println("S_KEY PARAM: " + s_keyParam);
			s_keyParam = String.valueOf(((int)Math.pow(Double.parseDouble(s_keyParam), (double)paramKey)) % Integer.parseInt(pParam));
			
			postRequest = new HttpPost("http://player.tree.tv/guard/guard/");
			postRequest.addHeader(userAgent);
			postRequest.addHeader(cookie);
			list.clear();
			list.add(new BasicNameValuePair("file", videoId));
			list.add(new BasicNameValuePair("source", "1"));
			list.add(new BasicNameValuePair("skc", s_keyParam));
			postRequest.setEntity(new UrlEncodedFormEntity(list));
			response = client.execute(postRequest);
			String resp = EntityUtils.toString(response.getEntity());
			//System.out.println("RESPONSE " + resp);
			JsonArray jsonArr = jsonP.parse(resp).getAsJsonArray();
			for (JsonElement jsonElem : jsonArr) {
				playlistLink = searchLinkInJsonArray(jsonElem.getAsJsonObject().get("sources").getAsJsonArray(), videoId);
				if(playlistLink.length() > 0)
					break;
			}
			System.out.println("LINK " + playlistLink);
			
			HttpGet getRequest = new HttpGet(playlistLink);
			getRequest.addHeader(userAgent);
			getRequest.addHeader(cookie);
			response = client.execute(getRequest);
			playlistLink = EntityUtils.toString(response.getEntity());
			for(String line: playlistLink.split("#EXT-X-STREAM-INF:")) {
				Pattern regex = Pattern.compile("RESOLUTION=[0-9]+?x([0-9]+)");
				Matcher m = regex.matcher(line);
				if(m.find()) {
					availablePlaylist.put(m.group(1), line.substring(line.indexOf("http://")).trim());
				}
			}
			
		} catch(IOException | JsonSyntaxException | IllegalStateException e) {
			e.printStackTrace();
			sessionUserId = generateUserId(); 
		} 
		long end = System.currentTimeMillis();
		System.out.println((end - begin)/1000.0);
		return availablePlaylist;
	}
	private String searchLinkInJsonArray(JsonArray jsonArr, String videoId) {
		for(JsonElement sourceElem: jsonArr) {
			if(videoId.equals(sourceElem.getAsJsonObject().get("point").getAsString()))
				return sourceElem.getAsJsonObject().get("src").getAsString();
		}
		return "";
	}
	@Override
	public BrowserVideoItem getVideoItemByUrl(String url) {
		BrowserVideoItem item = null;
		try {
			String html = getHtmlContent(url);
			Document content = Jsoup.parse(html);
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
	public Map<String, String> getVideoStreamMap(VideoLink video, MediaStream type) {
		Map<String, String> availableVideoMap = new LinkedHashMap<>();
		try {
			if(type == MediaStream.MP4) {
				for(String quality: qualityList) {
					String resUrl = getMp4StreamUrl(video.getId(), quality);
					if(resUrl.length() > 0)
						availableVideoMap.put(quality, resUrl);
				}
			} else if(type == MediaStream.HLS) {
				availableVideoMap = getHlsStreamMap(video.getId());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return availableVideoMap;
	}
	private int generateUserId() {
		return (int)Math.random() * 10000 + 203; 
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
		return "Tree.Tv";
	}
}
