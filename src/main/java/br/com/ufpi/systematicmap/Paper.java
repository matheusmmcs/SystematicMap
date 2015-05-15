package br.com.ufpi.systematicmap;

import java.util.HashSet;
import java.util.Set;

import br.com.ufpi.systematicmap.enums.ClassificationEnum;

public class Paper {
	
	private double id;
	private String title;
	private String abs;
	private String authors;
	private String keys;
	
	private ClassificationEnum classification;
	private String comments;
	private Set<String> setOfComments; 
	
	//regex scores
	private Integer regexTitle = 0;
	private Integer regexAbs = 0;
	private Integer regexKeys = 0;
	
	//min
	private Integer minLevenshteinDistance;
	private Paper paperMinLevenshteinDistance;
	
	
	public Paper(){
		super();
		this.setSetOfComments(new HashSet<String>());
	}
	
	public Paper(double id, String title, String abs, String authors, String keys) {
		this();
		
		this.id = id;
		this.title = title;
		this.abs = abs;
		this.authors = authors;
		this.keys = keys;
		
		this.setComments("");
	}

	public double getId() {
		return id;
	}
	public void setId(double id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAbs() {
		return abs;
	}
	public void setAbs(String abs) {
		this.abs = abs;
	}
	public String getAuthors() {
		return authors;
	}
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	public String getKeys() {
		return keys;
	}
	public void setKeys(String keys) {
		this.keys = keys;
	}

	public ClassificationEnum getClassification() {
		return classification;
	}

	public void setClassification(ClassificationEnum classification) {
		this.classification = classification;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public Set<String> getSetOfComments() {
		return setOfComments;
	}

	public void setSetOfComments(Set<String> setOfComments) {
		this.setOfComments = setOfComments;
	}

	public void appendComment(String comment) {
		this.setComments(this.getComments()+"\""+comment+"\", ");
	}
	
	public Integer getMinLevenshteinDistance() {
		return minLevenshteinDistance;
	}

	public void setMinLevenshteinDistance(Integer minLevenshteinDistance) {
		this.minLevenshteinDistance = minLevenshteinDistance;
	}

	public Paper getPaperMinLevenshteinDistance() {
		return paperMinLevenshteinDistance;
	}

	public void setPaperMinLevenshteinDistance(
			Paper paperMinLevenshteinDistance) {
		this.paperMinLevenshteinDistance = paperMinLevenshteinDistance;
	}

	public Integer getRegexTitle() {
		return regexTitle;
	}

	public void setRegexTitle(int regexTitle) {
		this.regexTitle = regexTitle;
	}

	public Integer getRegexAbs() {
		return regexAbs;
	}

	public void setRegexAbs(int regexAbs) {
		this.regexAbs = regexAbs;
	}

	public Integer getRegexKeys() {
		return regexKeys;
	}

	public void setRegexKeys(int regexKeys) {
		this.regexKeys = regexKeys;
	}

	public Integer getRegexTotal() {
		return this.getRegexTitle() + this.getRegexAbs() + this.getRegexKeys();
	}
	
	public Object[] toTitleReport() {
		return new Object[] {
				"ID", 
				"Classification", 
				"Comments", 
				"RegexTitle",
				"RegexAbs",
				"RegexKeys",
				"RegexTotal",
				"Title",
				"MinLevenshtein",
				"MinId",
				"MinLevenshteinTitle"
		};
	}
	
	public Object[] toReport() {
		return new Object[] {
				this.getId(), 
				this.getClassification(), 
				this.getComments(), 
				this.getRegexTitle(),
				this.getRegexAbs(),
				this.getRegexKeys(),
				this.getRegexTotal(),
				this.getTitle(),
				this.getMinLevenshteinDistance(),
				(this.getPaperMinLevenshteinDistance() != null ? this.getPaperMinLevenshteinDistance().getId() : null),
				(this.getPaperMinLevenshteinDistance() != null ? this.getPaperMinLevenshteinDistance().getTitle() : null)
				
		};
	}
	
	public String getStringSetOfComments() {
		String ret = "";
		
		for (String s : this.getSetOfComments()){
			ret += s + ", ";
		}
		
		return ret;
	}
	
}
