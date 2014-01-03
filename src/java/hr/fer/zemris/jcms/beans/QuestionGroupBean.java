package hr.fer.zemris.jcms.beans;

public class QuestionGroupBean {
	
	private Long id;
	private String isvuCode;
	private String name;
	private String tags;
	
	public QuestionGroupBean() {
		super();
	}

	public QuestionGroupBean(String isvuCode, String name, String tags) {
		super();
		this.isvuCode = isvuCode;
		this.name = name;
		this.tags = tags;
	}
	
	public String getIsvuCode() {
		return isvuCode;
	}
	
	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTags() {
		return tags;
	}
	
	public void setTags(String tags) {
		this.tags = tags;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
}
