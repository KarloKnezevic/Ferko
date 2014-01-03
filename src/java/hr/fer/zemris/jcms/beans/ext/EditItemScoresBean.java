package hr.fer.zemris.jcms.beans.ext;

import java.util.ArrayList;
import java.util.List;

public class EditItemScoresBean {
	
	private String letter;
	private List<ComponentDefBean> scoreTypeList = new ArrayList<ComponentDefBean>();
	private List<ItemScoreBean> scoreList = new ArrayList<ItemScoreBean>();
	
	public EditItemScoresBean() {
	}

	public String getLetter() {
		return letter;
	}

	public void setLetter(String letter) {
		this.letter = letter;
	}

	public List<ItemScoreBean> getScoreList() {
		return scoreList;
	}

	public void setScoreList(List<ItemScoreBean> scoreList) {
		this.scoreList = scoreList;
	}

	public List<ComponentDefBean> getScoreTypeList() {
		return scoreTypeList;
	}

	public void setScoreTypeList(List<ComponentDefBean> scoreTypeList) {
		this.scoreTypeList = scoreTypeList;
	}
}
