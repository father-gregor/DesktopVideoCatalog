package com.benlinus92.dskvideocatalog.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import com.benlinus92.dskvideocatalog.AppConstants;
import com.benlinus92.dskvideocatalog.PropertiesHandler;
import com.benlinus92.dskvideocatalog.model.BrowserVideoItem;
import com.benlinus92.dskvideocatalog.model.MediaStream;
import com.benlinus92.dskvideocatalog.model.SimpleVideoItem;
import com.benlinus92.dskvideocatalog.model.VideoLink;
import com.benlinus92.dskvideocatalog.model.VideoTranslationType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class KinogoClubParser extends Parser {
	private final static String KINOGO_BASIC_URL = "http://kinogo.club";
	private final static String KINOGO_FILMS_URL = "http://kinogo.club/page/";
	private final static String KINOGO_SERIES_URL = "http://kinogo.club/zarubezhnye_serialy/page/";
	private final static String KINOGO_CARTOONS_URL = "http://kinogo.club/multfilmy/page/";

	public KinogoClubParser() {
		mediaStreamsList = Arrays.asList(MediaStream.FLV);
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
		System.out.println(KINOGO_BASIC_URL + " -  status " + response.getStatusLine().getStatusCode());
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
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
			String url = KINOGO_FILMS_URL;
			if(category == AppConstants.CATEGORY_FILMS)
				url = KINOGO_FILMS_URL;
			else if(category == AppConstants.CATEGORY_SERIES)
				url = KINOGO_SERIES_URL;
			else if(category == AppConstants.CATEGORY_CARTOONS)
				url = KINOGO_CARTOONS_URL;
			String html = getHtmlContent(url + Integer.toString(page) + "/");
			Document content = Jsoup.parse(html);
			for(Element elem: content.select("div.shortstory")) {
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
		item.setTitle(el.select("h2.zagolovki").text());
		item.setUrl(el.select("h2.zagolovki a").attr("href"));
		item.setPrevImg(KINOGO_BASIC_URL + el.select("div.shortimg img").attr("src"));
		for(Element elem: el.select("div.shortimg a")) {
			if(elem.text().matches("[0-9]+")) {
				item.setYear(elem.text());
				break;
			}
		}
		String text= "";
		for(TextNode node: el.select("div.shortimg br").get(0).textNodes()) {
			text += node.text();
		}
		for(Element e: el.select("div.shortimg b")) {
			text = e.nextSibling().toString().trim();
		}
		//System.out.println("SIBLING: " + el.select("div.shortimg b").first().nextSibling().toString());
		//item.setAddedDate(LocalDate.parse("2017-02-02"));
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
		item.setTitle(el.select("div.fullstory h1").text());
		item.setPrevImg(KINOGO_BASIC_URL + el.select("div.fullimg img").attr("src").replace("thumbs/", ""));
		String temp =  el.select("div.fullimg > div[id^=news-id-]").text();
		item.setPlot(temp.substring(0, temp.indexOf("Год выпуска")));
		for(Element e: el.select("div.fullimg b")) {
			temp = e.text();
			if(temp.contains("Год")) {
				item.setYear(e.nextElementSibling().text());;
			} else if(temp.contains("Страна")) {
				if(!e.nextElementSibling().text().isEmpty())
					item.setCountry(e.nextElementSibling().text());
				else
					item.setCountry(e.nextSibling().toString());
			} else if(temp.contains("Жанр")) {
				List<String> list = new ArrayList<>();
				Element currElem = e.nextElementSibling();
				while(currElem.tagName() == "a") {
					list.add(currElem.text());
					currElem = currElem.nextElementSibling();
				}
				item.setGenre(list);
			} else if(temp.contains("Перевод")) {
				item.setTranslation(e.nextSibling().toString());
			} else if(temp.contains("Продолжительность")) {
				item.setDuration(e.nextSibling().toString());
			} else if(temp.contains("Режиссер")) {
				item.setDirector(e.nextSibling().toString());
			} else if(temp.contains("В ролях")) {
				List<String> list = new ArrayList<>();
				String[] castArr = e.nextSibling().toString().split(",");
				for(int i = 0; i < castArr.length; i++) {
					list.add(castArr[i]);
				}
				item.setCast(list);
			}
		}
		try{
			Pattern regex = Pattern.compile("decode[(]'([\\S]+)'[)]");
			Matcher m = regex.matcher(el.select("div.box.visible script").html());
			if(m.find()) {
				VideoTranslationType videoTr = new VideoTranslationType();
				videoTr.setVideosList(decodeBase64VideoUrlList(m.group(1)));
				videoTr.setTranslationName(item.getTitle());
				List<VideoTranslationType> videoList = new ArrayList<>();
				videoList.add(videoTr);
				item.setVideoTransTypeList(videoList);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return item;
	}
	private List<VideoLink> decodeBase64VideoUrlList(String strBase64) throws IOException {
		String decodedStr = new String(Base64.getDecoder().decode(strBase64), StandardCharsets.UTF_8);
		List<VideoLink> urlList = new ArrayList<>();
		Pattern regex = Pattern.compile("file=([\\S]+)&amp");
		Matcher m = regex.matcher(decodedStr);
		if(m.find()) {
			decodedStr = decodeTextViaHash(m.group(1));
			VideoLink video = new VideoLink();
			video.setLink(decodedStr);
			regex = Pattern.compile("hq[\\d]+/([\\S]+?\\.flv)");
			m = regex.matcher(decodedStr); m.find();
			video.setName(m.group(1));
			urlList.add(video);
		} else { //file= params not found, that mean there is a link to the txt file in decodedStr
			regex = Pattern.compile("pl=([\\S]+.txt)");
			m = regex.matcher(decodedStr);
			if(m.find()) {
				HttpClient client = HttpClientBuilder.create().build();
				HttpGet request = new HttpGet(m.group(1));
				request.addHeader("User-Agent", AppConstants.USER_AGENT); 
				HttpResponse response = client.execute(request);
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
				String line = "";
				String fileContent = "";
				while((line = br.readLine()) != null)
					fileContent = fileContent + line;
				JsonParser jParser = new JsonParser();
				System.out.println("CONTENT " + fileContent);
				try {
					JsonObject jsobObj = jParser.parse(fileContent).getAsJsonObject();
					for(JsonElement elem: jsobObj.get("playlist").getAsJsonArray()) {
						VideoLink video = new VideoLink();
						video.setLink(elem.getAsJsonObject().get("file").getAsString());
						regex = Pattern.compile("\\/([a-zA-Z0-9_-]+?\\.flv)$");
						m = regex.matcher(video.getLink()); m.find();
						video.setName(m.group(1));
						urlList.add(video);
					}
				} catch(JsonSyntaxException e) {
					e.printStackTrace();
				}
			}
			
		}
		System.out.println(decodedStr);
		return urlList;
	}
	private String decodeTextViaHash(String data) {
		String hash = "0123456789WGXMHRUZID=NQVBLihbzaclmepsJxdftioYkngryTwuvihv7ec41D6GpBtXx3QJRiN5WwMf=ihngU08IuldVHosTmZz9kYL2bayE";
		data = data.replace("tQ3N", "");//remove "garbage part", which interfere decoding
		data = decodeUppodData(data, "r", "A");
		data = data.replace("\n", "");
		String[] hashArr = hash.split("ih");
		String a = "";
		String b = "";
		if(data.substring(data.length() - 1) == "!") {
			data = data.substring(0, data.length() - 1);
			a = hashArr[3];
			b = hashArr[2];
		} else {
			a = hashArr[1];
			b = hashArr[0];
		}
		for(int i = 0; i < a.length(); i++) {
			data = data.replaceAll(b.substring(i, i + 1), "__");
			data = data.replaceAll(a.substring(i, i + 1), b.substring(i, i + 1));
			data = data.replaceAll("__", a.substring(i, i + 1));
		}
		return new String(Base64.getDecoder().decode(data));
	}
	private String decodeUppodData(String data, String ch1, String ch2) { //flash player's name
		if((data.substring(data.length() - 2, data.length() - 1) == ch1) && (data.substring(2, 3) == ch2)) {
			String srev = new StringBuilder(data).reverse().toString();
			int loc3 = Integer.parseInt(srev.substring(srev.length() - 2)) / 2;
			srev = srev.substring(2, srev.length() - 5);
			if(loc3 < srev.length()) {
				int ind = loc3;
				while(ind < srev.length()) {
					srev = srev.substring(0, ind) + srev.substring(ind + 1);
					ind += loc3;
				}
			}
			data = srev + "!";
		}
		return data;
	}
	@Override
	public Map<String, String> getVideoStreamMap(VideoLink video, MediaStream type) {
		Map<String, String> availableVideoMap = new LinkedHashMap<>();
		availableVideoMap.put("480", video.getLink());
		return availableVideoMap;
	}

	@Override
	public String getWebSiteName() {
		return "Kinogo.Club";
	}

}
