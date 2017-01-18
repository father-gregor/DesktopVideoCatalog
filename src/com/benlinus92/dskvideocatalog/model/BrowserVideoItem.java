package com.benlinus92.dskvideocatalog.model;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BrowserVideoItem extends SimpleVideoItem {
	private ObjectProperty<List<String>> genre = new SimpleObjectProperty<>();
	private StringProperty country = new SimpleStringProperty("");
	private StringProperty director = new SimpleStringProperty("");
	private StringProperty translation = new SimpleStringProperty("");
	private StringProperty duration = new SimpleStringProperty("");
	private ObjectProperty<List<String>> cast = new SimpleObjectProperty<>();
	private StringProperty plot = new SimpleStringProperty("");
	
	public List<String> getGenre() {
		return genre.get();
	}
	public ObjectProperty<List<String>> genreProperty() {
		return genre;
	}
	public void setGenre(List<String> genre) {
		this.genre.set(genre);
	}
	public String getCountry() {
		return country.get();
	}
	public StringProperty countryProperty() {
		return country;
	}
	public void setCountry(String country) {
		this.country.set(country);
	}
	public String getDirector() {
		return director.get();
	}
	public StringProperty directorProperty() {
		return director;
	}
	public void setDirector(String director) {
		this.director.set(director);
	}
	public String getTranslation() {
		return translation.get();
	}
	public StringProperty translationProperty() {
		return translation;
	}
	public void setTranslation(String translation) {
		this.translation.set(translation);
	}
	public String getDuration() {
		return duration.get();
	}
	public StringProperty durationProperty() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration.set(duration);
	}
	public List<String> getCast() {
		return cast.get();
	}
	public ObjectProperty<List<String>> castProperty() {
		return cast;
	}
	public void setCast(List<String> cast) {
		this.cast.set(cast);
	}
	public String getPlot() {
		return plot.get();
	}
	public StringProperty plotProperty() {
		return plot;
	}
	public void setPlot(String plot) {
		this.plot.set(plot);
	}
}
