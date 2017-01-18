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
	private String catalogviewProp = "";
	private String itembrowserProp = "";
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
				catalogviewProp = prop.getProperty("view.catalog");
				itembrowserProp = prop.getProperty("view.itembrowser");
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
	public String getCatalogviewProp() {
		return catalogviewProp;
	}
	public String getItemBrowserProp() {
		return itembrowserProp;
	}
	public String getFilmsUnitName() {
		return appUnitsBundle.getString("unit.films");
	}
	public String getSeriesUnitName() {
		return appUnitsBundle.getString("unit.series");
	}
	public String getCartoonsUnitName() {
		return appUnitsBundle.getString("unit.cartoons");
	}
}
