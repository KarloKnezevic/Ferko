package hr.fer.zemris.jcms.beans;

public class CCIAMatrixRow {
	private String lastName;
	private String firstName;
	private String jmbag;
	private Long userID;
	private CCIAMatrixColumn[] columns;
	
	public CCIAMatrixRow(Long userID, String jmbag, String firstName,
			String lastName, CCIAMatrixColumn[] columns) {
		super();
		this.userID = userID;
		this.jmbag = jmbag;
		this.firstName = firstName;
		this.lastName = lastName;
		this.columns = columns;
	}
	
	public String getLastName() {
		return lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getJmbag() {
		return jmbag;
	}
	public Long getUserID() {
		return userID;
	}
	public CCIAMatrixColumn[] getColumns() {
		return columns;
	}
	
	
}
