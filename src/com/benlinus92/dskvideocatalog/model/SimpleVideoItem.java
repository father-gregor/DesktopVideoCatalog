package com.benlinus92.dskvideocatalog.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SimpleVideoItem {
	private StringProperty title = new SimpleStringProperty("");
	private StringProperty year = new SimpleStringProperty("");
	private StringProperty url = new SimpleStringProperty("");
	private StringProperty prevImg = new SimpleStringProperty("");
	private ObjectProperty<LocalDate> addedDate = new SimpleObjectProperty<LocalDate>(LocalDate.MIN);
	
	public String getTitle() {
		return title.get();
	}
	public StringProperty titleProperty() {
		return title;
	}
	public void setTitle(String title) {
		this.title.set(title);
	}
	public String getYear() {
		return year.get();
	}
	public StringProperty yearProperty() {
		return year;
	}
	public void setYear(String year) {
		this.year.set(year);
	}
	public String getUrl() {
		return url.get();
	}
	public StringProperty urlProperty() {
		return url;
	}
	public void setUrl(String url) {
		this.url.set(url);
	}
	public String getPrevImg() {
		return prevImg.get();
	}
	public StringProperty prevImgProperty() {
		return prevImg;
	}
	public void setPrevImg(String prevImg) {
		this.prevImg.set(prevImg);
	}
	public LocalDate getAddedDate() {
		return addedDate.get();
	}
	public ObjectProperty<LocalDate> addedDateProperty() {
		return addedDate;
	}
	public void setAddedDate(LocalDate addedDate) {
		this.addedDate.set(LocalDate.ofEpochDay(addedDate.toEpochDay()));
	}
	@Override
	public String toString() {
		return String.format("%s\n%s\n%s\n%s\n%s", getTitle(), getUrl(), getYear(), getPrevImg(), getAddedDate());
	}
}
