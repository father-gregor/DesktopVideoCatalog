package com.benlinus92.dskvideocatalog.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
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

public class KinogoClubParser implements Parser {
	private final static String KINOGO_BASIC_URL = "http://kinogo.club";
	private final static String KINOGO_FILMS_URL = "http://kinogo.club/page/";
	private final static String KINOGO_SERIES_URL = "http://kinogo.club/zarubezhnye_serialy/page/";
	private final static String KINOGO_CARTOONS_URL = "http://kinogo.club/multfilmy/page/";
	private List<MediaStream> mediaStreamsList;
	private Map<String, Integer> parserCategoryMap;

	public KinogoClubParser() {
		mediaStreamsList = Arrays.asList(MediaStream.FLV);
		parserCategoryMap = new LinkedHashMap<>();
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitFilmsName(), AppConstants.CATEGORY_FILMS);
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitSeriesName(), AppConstants.CATEGORY_SERIES);
		parserCategoryMap.put(PropertiesHandler.getInstance().getUnitCartoonsName(), AppConstants.CATEGORY_CARTOONS);
	}
	@Override
	public String getHtmlContent(String url) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
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
			String html = getHtmlContent(url + Integer.toString(page));
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
			//System.out.println("SIBLING:" + e.text());
			if(e.text().contains("Страна")) {
				//System.out.println("SIBLING:" + e.nextElementSibling().outerHtml());
			}
			//System.out.println("SIBLING: " + e.nextElementSibling().text());
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
		Pattern regex = Pattern.compile("decode[(]'([\\S]+)'[)]");
		Matcher m = regex.matcher(el.select("div.box.visible script").html());
		if(m.find()) {
			decodeBase64VideoUrlList(m.group(1));
		}
		
		return item;
	}
	private String decodeBase64VideoUrlList(String strBase64) {
		String decodedStr = new String(Base64.getDecoder().decode(strBase64), StandardCharsets.UTF_8);
		System.out.println(decodedStr);
		return null;
	}

	@Override
	public Map<String, String> getVideoStreamMap(VideoLink video, MediaStream type) {
		// TODO Auto-generated method stub
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
		return "Kinogo.Club";
	}

}
