package hr.fer.zemris.jcms.beans;

public class CCTAMatrixRow {
	private String lastName;
	private String firstName;
	private String jmbag;
	private Long userID;
	private CCTAMatrixColumn[] columns;
	
	public CCTAMatrixRow(Long userID, String jmbag, String firstName,
			String lastName, CCTAMatrixColumn[] columns) {
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
	public CCTAMatrixColumn[] getColumns() {
		return columns;
	}
	
	
}
