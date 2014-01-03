package hr.fer.zemris.jcms.beans.ext;

import java.util.Date;

/**
 * Bean koji sluzi za unos rasporeda medjuispita
 * @author TOMISLAV
 *
 */
public class AssessmentScheduleBean {
	String courseISVUCode;
	Date start;
	int duration;
	
	public String getCourseISVUCode() {
		return courseISVUCode;
	}
	public void setCourseISVUCode(String courseISVUCode) {
		this.courseISVUCode = courseISVUCode;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
}
