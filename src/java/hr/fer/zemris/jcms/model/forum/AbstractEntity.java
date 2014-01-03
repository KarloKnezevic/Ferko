package hr.fer.zemris.jcms.model.forum;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * Apstraktni razred kojeg nasljeđuju svi entiteti foruma.
 * 
 * @author Hrvoje Ban
 */
@MappedSuperclass
public abstract class AbstractEntity {

	protected Long id;
	protected int version;

	/**
	 * @return Jedinstveni indentifikacijki broj entiteta.
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return Verzija entiteta. Koristi se kod optimističnog zaključavanja.
	 */
	@Version
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return Hash kod entiteta.
	 * @throws IllegalStateException Ako entitet nije spremljen u bazi podataka.
	 */
	@Override
	public int hashCode() {
		return (id == null) ? 0 : id.hashCode();
	}

	/**
	 * @return Jeli predani object jednak ovomu.
	 * @throws IllegalStateException Ako i predani i ovaj entitet nisu spremljeni
	 * 	u bazi podataka.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractEntity other = (AbstractEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
