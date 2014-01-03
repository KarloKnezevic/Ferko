package hr.fer.zemris.jcms.beans.cached;

public class STEStudent extends ScoreTableEntry {

	private static final long serialVersionUID = 1L;
	
	private String lastName;
	private String firstName;
	private String jmbag;
	
	public STEStudent(Long id, String jmbag, String firstName, String lastName) {
		super(id);
		this.firstName = firstName;
		this.jmbag = jmbag;
		this.lastName = lastName;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getJmbag() {
		return jmbag;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	@Override
	public String toString() {
		return lastName+" "+firstName+" ("+jmbag+")";
	}
	
	@Override
	public byte getType() {
		return (byte)0;
	}
}
