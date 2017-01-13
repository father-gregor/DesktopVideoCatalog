package com.benlinus92.dskvideocatalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHandler {
	private static PropertiesHandler instance = null;
	private String filename = "application.properties";
	private String appTitleProp = "";
	private String rootviewProp = "";
	private String catalogviewProp = "";
	
	public static PropertiesHandler getInstance() {
		if(instance == null)
			instance = new PropertiesHandler();
		return instance;
	}
	private PropertiesHandler() { 
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream(filename);
			if(input != null) {
				prop.load(input);
				appTitleProp = prop.getProperty("apptitle");
				rootviewProp = prop.getProperty("rootview");
				catalogviewProp = prop.getProperty("catalogview");
			}
			
		} catch(IOException e) {
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
		return appTitleProp;
	}
	public String getRootviewProp() {
		return rootviewProp;
	}
	public String getCatalogviewProp() {
		return catalogviewProp;
	}
}
