package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.UserBrowsing;

public class UserGroupSearch extends ExtendedActionSupport {

	private static final long serialVersionUID = 1L;
	
	private String type = "JSON";
	private String term = null;
	private UserGroupSearchData data = null;
	
	public String execute() {
		if(type.equals("JSON")) return get_json();
		return SUCCESS;
	}
	
	public String get_json() {
		if(term == null) return SUCCESS;
		term.trim();
		if(term.length()<3) return SUCCESS;
		data = UserBrowsing.getUserGroupSearchData(term);
		return SUCCESS;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public UserGroupSearchData getData() {
		return data;
	}

	public void setData(UserGroupSearchData data) {
		this.data = data;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}
	
	

}
