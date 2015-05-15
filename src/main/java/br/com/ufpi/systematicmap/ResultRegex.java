package br.com.ufpi.systematicmap;

public class ResultRegex {
	private int count;
	private String comments = "";
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public void appendComment(String comment){
		this.setComments(this.getComments()+", "+comment);
	}
}
