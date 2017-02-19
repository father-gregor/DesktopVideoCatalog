package com.benlinus92.dskvideocatalog;

/*
 * Miniature class for implementing mapping of functions from Javascript to Java
 */
public class JavaToJavascriptBridge {
	public void log(String text) {
		System.out.println("console.log(): " + text);
	}
}
