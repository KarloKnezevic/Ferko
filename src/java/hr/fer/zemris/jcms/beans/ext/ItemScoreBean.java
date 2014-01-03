package hr.fer.zemris.jcms.beans.ext;

import java.util.ArrayList;
import java.util.List;

public class ItemScoreBean extends BaseUserBean {
	
	public String id;
	/**
	 * Ovaj property cuva listu originalno ocitanih vrijednosti iz baze
	 */
	public List<String> oScores = new ArrayList<String>();
	/**
	 * Ovaj property cuva listu ocitanih vrijednosti iz baze koju ce korisnik (mozda) promijeniti.
	 * Kasnije usporedbom sa oScores mogu znati koje je elemente mijenjao a koje nije.
	 */
	public List<String> scores = new ArrayList<String>();
	/**
	 * Ovaj property cuva listu verzija ocitanih vrijednosti iz baze
	 */
	public List<Long> versions = new ArrayList<Long>();
	public String assignedBy;
	private String tag;
	private boolean error = false;
	
	public ItemScoreBean() {
	}

	public List<Long> getVersions() {
		return versions;
	}
	public void setVersions(List<Long> versions) {
		this.versions = versions;
	}
	
	public List<String> getScores() {
		return scores;
	}

	public void setScores(List<String> scores) {
		this.scores = scores;
	}

	public List<String> getOScores() {
		return oScores;
	}
	
	public void setOScores(List<String> oScores) {
		this.oScores = oScores;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}
}
