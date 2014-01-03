package hr.fer.zemris.jcms.beans.ext;

/**
 * Veza kolegij-korisnik-grupa. Podrazumijeva se scenarij gdje je akademska godina
 * fiksirana, te se misli na primjerak kolegija tada (to je određeno negdje izvana).
 * Također, dubina hijerarhije grupa očekuje se da je 1 (dakle, korisnik se nalazi 
 * u jednoj od n grupa), pa ovdje piše točno u kojoj (i to piše po imenu grupe koje
 * u toj nadgrupi mora biti jedinstveno).
 * 
 * @author marcupic
 *
 */
public class UserGroupFlat {
	private String isvuCode;
	private String jmbag;
	private String groupName;
	
	public UserGroupFlat(String isvuCode, String jmbag, String groupName) {
		super();
		this.isvuCode = isvuCode;
		this.jmbag = jmbag;
		this.groupName = groupName;
	}

	public String getIsvuCode() {
		return isvuCode;
	}
	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}

	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((isvuCode == null) ? 0 : isvuCode.hashCode());
		result = prime * result + ((jmbag == null) ? 0 : jmbag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserGroupFlat))
			return false;
		final UserGroupFlat other = (UserGroupFlat) obj;
		if (isvuCode == null) {
			if (other.isvuCode != null)
				return false;
		} else if (!isvuCode.equals(other.isvuCode))
			return false;
		if (jmbag == null) {
			if (other.jmbag != null)
				return false;
		} else if (!jmbag.equals(other.jmbag))
			return false;
		return true;
	}
}
