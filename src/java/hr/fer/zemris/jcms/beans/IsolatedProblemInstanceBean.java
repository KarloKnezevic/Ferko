package hr.fer.zemris.jcms.beans;

import java.util.Date;

public class IsolatedProblemInstanceBean {
	private String id;
	private Date createdOn;
	private Date finishedOn;
	private Double correctnessMeasure;
	private boolean solved;
	private IsolatedProblemInstanceStatus status;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	public Date getFinishedOn() {
		return finishedOn;
	}
	public void setFinishedOn(Date finishedOn) {
		this.finishedOn = finishedOn;
	}
	public Double getCorrectnessMeasure() {
		return correctnessMeasure;
	}
	public void setCorrectnessMeasure(Double correctnessMeasure) {
		this.correctnessMeasure = correctnessMeasure;
	}
	public boolean isSolved() {
		return solved;
	}
	public void setSolved(boolean solved) {
		this.solved = solved;
	}
	public IsolatedProblemInstanceStatus getStatus() {
		return status;
	}
	public void setStatus(IsolatedProblemInstanceStatus status) {
		this.status = status;
	}
}
