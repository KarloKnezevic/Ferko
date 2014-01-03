package hr.fer.zemris.jcms.beans;

import java.util.Date;

public class ActivityBean {

	private Long id;
	private Date date;
	private String message;
	private boolean archived;
	private boolean viewed;

	public ActivityBean(Long id, Date date, String message, boolean archived, boolean viewed) {
		super();
		this.id = id;
		this.date = date;
		this.message = message;
		this.archived = archived;
		this.viewed = viewed;
	}
	
	public Long getId() {
		return id;
	}
	public String getMessage() {
		return message;
	}
	
	public boolean getArchived() {
		return archived;
	}

	public boolean getViewed() {
		return viewed;
	}
	
	public Date getDate() {
		return date;
	}
}
