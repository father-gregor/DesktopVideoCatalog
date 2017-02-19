package com.benlinus92.dskvideocatalog;

import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;

import javafx.scene.layout.Pane;

/*
 * This class responsible for holding objects of current using parser,
 * pane in catalog and category number such as film, series etc.
 */
public class CatalogStateHandler {
	private int categoryState;
	private Parser parserState;
	private Pane paneState;
	public CatalogStateHandler() {
		categoryState = 1;
		parserState = new TreeTvParser();
		paneState = null;
	}
	public int getCategoryState() {
		return categoryState;
	}
	public void setCatalogState(int categoryState, Parser parserState, Pane paneState) {
		this.categoryState = categoryState;
		this.parserState = parserState;
		this.paneState = paneState;
	}
	public void setPaneState(Pane paneState) {
		this.paneState = paneState;
	}
	public Parser getParserState() {
		return parserState;
	}
	public Pane getPaneState() {
		return paneState;
	}
}
