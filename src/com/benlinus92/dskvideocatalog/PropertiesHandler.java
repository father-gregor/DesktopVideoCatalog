package com.benlinus92.dskvideocatalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropertiesHandler {
	private static PropertiesHandler instance = null;
	private String appPropFilename = "application.properties";
	private String userAppPropFilename = "./user.properties";
	private String fileLocale = "locale/MenuUnits";
	private Map<String, String> appPropertiesBundle;
	private ResourceBundle appUnitsBundle;
	
	public static PropertiesHandler getInstance() {
		if(instance == null)
			instance = new PropertiesHandler();
		return instance;
	}
	private PropertiesHandler() { 
		Properties prop = new Properties();
		appPropertiesBundle = new LinkedHashMap<>();
		InputStream input = null;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream(appPropFilename);
			if(input != null) {
				prop.load(input);
				appPropertiesBundle.put("view.rootwindow", prop.getProperty("view.rootwindow"));
				appPropertiesBundle.put("view.catalog", prop.getProperty("view.catalog"));
				appPropertiesBundle.put("view.itembrowser", prop.getProperty("view.itembrowser"));
				appPropertiesBundle.put("view.imageviewerwindow", prop.getProperty("view.imageviewerwindow"));
				appPropertiesBundle.put("view.videolist", prop.getProperty("view.videolist"));
				appPropertiesBundle.put("view.mediaplayer", prop.getProperty("view.mediaplayer"));
				appPropertiesBundle.put("view.choosemediamenu", prop.getProperty("view.choosemediamenu"));
				appPropertiesBundle.put("view.settings", prop.getProperty("view.settings"));
				appPropertiesBundle.put("html.webplayer", prop.getProperty("html.webplayer"));
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
	public String getAppProperty(String name) {
		return appPropertiesBundle.get(name);
	}
	public void setAppProperty(String propName, String value) {
		InputStream input = null;
		OutputStream output = null;
		try {
			Properties prop = new Properties();
			File userProperties = new File(userAppPropFilename);
			if(userProperties.createNewFile()) {
				System.out.println("User properties file created");
			}
			input = new FileInputStream(userProperties);
			prop.load(input);
			if(prop.setProperty(propName, value) == null)
				System.out.println("New property " + propName + " is added and set");
			if(input != null) {
				input.close();
				input = null;
			}
			output = new FileOutputStream(userProperties);
			prop.store(output, "User properties");
			
		} catch(IOException e) {
			e.printStackTrace();
		} catch(MissingResourceException e) {
			e.printStackTrace();
		} finally {
			try {
				if(input != null)
					input.close();
				if(output != null)
					output.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	public String getAppTitleProp() {
		return appUnitsBundle.getString("apptitle");
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
	public String getInternalPlayerLabel() {
		return appUnitsBundle.getString("label.internalplayer");
	}
	public String getWebPlayerLabel() {
		return appUnitsBundle.getString("label.webplayer");
	}
	public String getDefaultPlayerLabel() {
		return appUnitsBundle.getString("label.defaultplayer");
	}
	public String getDownloadLinkLabel() {
		return appUnitsBundle.getString("label.downloadlink");
	}
	public String getCopyLinkLabel() {
		return appUnitsBundle.getString("label.copylink");
	}
}
