package hr.fer.zemris.jcms.beans.ext;

public class UserText {
	private String lastName;
	private String firstName;
	private String jmbag;
	private Long id;
	
	public UserText() {
	}

	public UserText(Long id, String jmbag, String lastName, String firstName) {
		super();
		this.id = id;
		this.jmbag = jmbag;
		this.lastName = lastName;
		this.firstName = firstName;
	}

	public static UserText parse(String text) {
		UserText userText = new UserText();
		if(text==null) return userText;
		text = text.trim();
		if(text.length()==0) return userText;
		int pos = text.indexOf(',');
		if(pos==-1) {
			userText.setLastName(text);
			return userText;
		} else {
			userText.setLastName(text.substring(0, pos).trim());
			text = text.substring(pos+1).trim();
		}
		if(text.length()==0) return userText;
		pos = text.indexOf('(');
		if(pos==-1) {
			userText.setFirstName(text);
			return userText;
		} else {
			userText.setFirstName(text.substring(0, pos).trim());
			text = text.substring(pos+1).trim();
		}		
		if(text.length()==0) return userText;
		pos = text.indexOf(')');
		if(pos!=-1) {
			userText.setJmbag(text.substring(0, pos).trim());
		}		
		return userText;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(lastName!=null) sb.append(lastName).append(", ");
		if(firstName!=null) sb.append(firstName).append(" ");
		if(jmbag!=null) sb.append('(').append(jmbag).append(')');
		return sb.toString();
	}
	
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ \"").append(toString()).append("\", \"").append(id).append("\" ]");
		return sb.toString();
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getJmbag() {
		return jmbag;
	}

	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
