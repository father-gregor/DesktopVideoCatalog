package com.benlinus92.dskvideocatalog;

import com.benlinus92.dskvideocatalog.parsers.Parser;
import com.benlinus92.dskvideocatalog.parsers.TreeTvParser;

import javafx.scene.layout.Pane;

public class CatalogStateHandler {
	private int categoryState;
	private Parser parserState;
	private Pane paneState;
	public CatalogStateHandler() {
		categoryState = 1;
		parserState = new TreeTvParser();
		paneState = new Pane();
	}
	public int getCategoryState() {
		return categoryState;
	}
	public void setCatalogState(int categoryState, Parser parserState, Pane paneState) {
		this.categoryState = categoryState;
		this.parserState = parserState;
		this.paneState = paneState;
	}
	public Parser getParserState() {
		return parserState;
	}
	public Pane getPaneState() {
		return paneState;
	}
}
