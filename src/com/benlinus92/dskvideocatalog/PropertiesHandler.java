package com.benlinus92.dskvideocatalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropertiesHandler {
	private static PropertiesHandler instance = null;
	private String fileAppProp = "application.properties";
	private String fileLocale = "locale/MenuUnits";
	private String rootWindowViewProp = "";
	private String catalogViewProp = "";
	private String itembrowserViewProp = "";
	private String imageViewerWindowProp = "";
	private String videoListViewProp = "";
	private String mediaPlayerViewProp = "";
	private ResourceBundle appUnitsBundle;
	
	public static PropertiesHandler getInstance() {
		if(instance == null)
			instance = new PropertiesHandler();
		return instance;
	}
	private PropertiesHandler() { 
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream(fileAppProp);
			if(input != null) {
				prop.load(input);
				rootWindowViewProp = prop.getProperty("view.rootwindow");
				catalogViewProp = prop.getProperty("view.catalog");
				itembrowserViewProp = prop.getProperty("view.itembrowser");
				imageViewerWindowProp = prop.getProperty("view.imageviewerwindow");
				videoListViewProp = prop.getProperty("view.videolist");
				mediaPlayerViewProp = prop.getProperty("view.mediaplayer");
				Locale locale = new Locale(prop.getProperty("locale.lang"), prop.getProperty("locale.country"));
				appUnitsBundle = ResourceBundle.getBundle(fileLocale, locale);
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		} catch(MissingResourceException e) {
			e.printStackTrace();
		} finally {
			try {
				if(input != null)
					input.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	public String getAppTitleProp() {
		return appUnitsBundle.getString("apptitle");
	}
	public String getRootWindowViewProp() {
		return rootWindowViewProp;
	}
	public String getCatalogViewProp() {
		return catalogViewProp;
	}
	public String getItembrowserViewProp() {
		return itembrowserViewProp;
	}
	public String getVideoListViewProp() {
		return videoListViewProp;
	}
	public String getMediaPlayerViewProp() {
		return mediaPlayerViewProp;
	}
	public String getImageViewerWindowProp() {
		return imageViewerWindowProp;
	}
	public String getUnitFilmsName() {
		return appUnitsBundle.getString("unit.films");
	}
	public String getUnitSeriesName() {
		return appUnitsBundle.getString("unit.series");
	}
	public String getUnitCartoonsName() {
		return appUnitsBundle.getString("unit.cartoons");
	}
}
