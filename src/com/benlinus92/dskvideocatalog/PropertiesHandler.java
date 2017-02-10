package com.benlinus92.dskvideocatalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
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
	private String localeFilename = "locale/MenuUnits";
	private Map<String, String> appPropertiesBundle;
	private Map<String, String> userPropertiesBundle;
	private ResourceBundle appUnitsBundle;
	
	public static PropertiesHandler getInstance() {
		if(instance == null)
			instance = new PropertiesHandler();
		return instance;
	}
	private PropertiesHandler() { 
		Properties prop = new Properties();
		appPropertiesBundle = new LinkedHashMap<>();
		userPropertiesBundle = new LinkedHashMap<>();
		InputStream input = null;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream(appPropFilename);
			if(input != null) {
				prop.load(input);
				appPropertiesBundle = propertiesToMap(prop);
				Locale locale = new Locale(prop.getProperty("locale.lang"), prop.getProperty("locale.country"));
				appUnitsBundle = ResourceBundle.getBundle(localeFilename, locale);
				prop.clear();
				input.close();
				input = null;
			}
			prop = new Properties();
			File userProperties = new File(userAppPropFilename);
			if(userProperties.createNewFile()) {
				System.out.println("User properties file created");
			}
			input = new FileInputStream(userProperties);
			if(input != null) {
				prop.load(input);
				userPropertiesBundle = propertiesToMap(prop);
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
	public Map<String, String> propertiesToMap(Properties prop) {
		Map<String, String> map = new LinkedHashMap<>();
		Enumeration<Object> e = prop.keys();
		while(e.hasMoreElements()) {
			String str = (String) e.nextElement();
			map.put(str, prop.getProperty(str));
		}
		return map;
	}
	public String getAppProperty(String propName) {
		return appPropertiesBundle.get(propName);
	}
	public String getUserProperty(String propName) {
		return userPropertiesBundle.get(propName);
	}
	public void setUserProperty(String propName, String value) {
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
			prop.store(output, "User Properties");
			userPropertiesBundle = propertiesToMap(prop);
			
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
		System.out.println(appUnitsBundle.getString("label.internalplayer"));
		return appUnitsBundle.getString("label.internalplayer");
	}
	public String getWebPlayerLabel() {
		System.out.println(appUnitsBundle.getString("label.webplayer"));
		return appUnitsBundle.getString("label.webplayer");
	}
	public String getDefaultPlayerLabel() {
		System.out.println(appUnitsBundle.getString("label.defaultplayer"));
		return appUnitsBundle.getString("label.defaultplayer");
	}
	public String getDownloadLinkLabel() {
		System.out.println();
		return appUnitsBundle.getString("label.downloadlink");
	}
	public String getCopyLinkLabel() {
		return appUnitsBundle.getString("label.copylink");
	}
}
