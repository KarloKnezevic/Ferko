package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Ovo je osnovni razred za unos bodova svih vrsta provjera. Veza između 
 * {@link Assessment} i {@link AssessmentConfiguration} izvedena je kao 
 * one-to-one zato da se omogući dinamička promjena vrste provjere, odnosno
 * odvoji definiranje pojma provjere (koji uključuje vrijeme provođenja,
 * raspored studenata i sl) od definiranja vrste provjere (je li to provjera
 * sa sumarnim rezultatom ili provjera na skeniranje obrazaca ili nešto treće).
 * Zahvaljujući ovakvoj organizaciji vrsta se naknadno može i mijenjati, a bez
 * da to utječe na samu provjeru.
 *   
 * @author marcupic
 *
 */
@Entity
@Table(name="assessment_configurations")
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="dtype",discriminatorType=DiscriminatorType.CHAR)
@DiscriminatorValue("*")
public class AssessmentConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Long id;
	protected Assessment assessment;
	
	public AssessmentConfiguration() {
	}

	/**
	 * Identifikator vrste provjere.
	 * 
	 * @return
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Provjera koju ovo opisuje.
	 * @return
	 */
	@OneToOne(optional=false,fetch=FetchType.LAZY)
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getAssessment() == null) ? 0 : getAssessment().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AssessmentConfiguration))
			return false;
		AssessmentConfiguration other = (AssessmentConfiguration) obj;
		if (getAssessment() == null) {
			if (other.getAssessment() != null)
				return false;
		} else if (!getAssessment().equals(other.getAssessment()))
			return false;
		return true;
	}
	
	/**
	 * Zadatak ove metode jest postavljanje svih parametara provjere
	 * na defaultne vrijednosti. Metoda ce biti pozvana svaki puta
	 * kada se stvori novi primjerak vrste provjere. 
	 */
	public void setDefaults() {
	}
}
