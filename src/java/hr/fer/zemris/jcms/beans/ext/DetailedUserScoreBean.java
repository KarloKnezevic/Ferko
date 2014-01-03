package hr.fer.zemris.jcms.beans.ext;

public class DetailedUserScoreBean {
	private String jmbag;
	private String[] listOfValues;
	private String group;
	
	public DetailedUserScoreBean(String jmbag, String group, int listSize) {
		super();
		this.jmbag = jmbag;
		this.listOfValues = new String[listSize];
		this.group = group;
	}
	
	public DetailedUserScoreBean() {
	}
	
	public void setValue(String doubleValue, int index) {
		if (this.listOfValues == null) {
			throw new IllegalArgumentException(
					"Lista rezultata nije inicijalizirana!");
		}
		
		this.listOfValues[index] = doubleValue;
	}
	
	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String[] getListOfValues() {
		return this.listOfValues;
	}

	public void setListOfValues(String[] listOfValues) {
		this.listOfValues = listOfValues;
	}
	
	
}
