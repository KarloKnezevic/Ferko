package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * AssessmentFlagValue je jedna konkretna vrijednost zastavice
 * za jednog konkretnog studenta.
 *  
 * Id je string oblika "courseInstanceID/userID/assessmentFlagID" i nije GeneratedValue.
 * NE. I tu odustajemo u korist obicnog longa.
 * 
 * @author marcupic
 *
 */
@NamedQueries({
    @NamedQuery(name="AssessmentFlagValue.listForCourseInstance",query="select a from AssessmentFlagValue as a where a.assessmentFlag.courseInstance.id=:courseInstanceID"),
    @NamedQuery(name="AssessmentFlagValue.getForAssessmentFlagAndUser",query="select a from AssessmentFlagValue as a where a.assessmentFlag=:assessmentFlag and a.user=:user"),
    @NamedQuery(name="AssessmentFlagValue.listForAssessmentFlag",query="select a from AssessmentFlagValue as a where a.assessmentFlag=:assessmentFlag"),
    @NamedQuery(name="AssessmentFlagValue.listForCourseInstanceAndUser",query="select a from AssessmentFlagValue as a join fetch a.assessmentFlag where a.assessmentFlag.courseInstance=:courseInstance and a.user=:user")
})
@Entity
@Table(name="assessment_flag_values")
@Cache(usage=CacheConcurrencyStrategy.NONE)
public class AssessmentFlagValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private AssessmentFlag assessmentFlag;
	private User user;
	private User assigner;
	private boolean value;
	private boolean manuallySet;
	private boolean manualValue;
	private boolean error;
	private long version;
	
	public AssessmentFlagValue() {
	}

	/**
	 * Identifikator.
	 * @return identifikator
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Version
	@Column(name="OPTLOCK")
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}

	/**
	 * Zastavica čija je ovo vrijednost.
	 * 
	 * @return zastavica
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public AssessmentFlag getAssessmentFlag() {
		return assessmentFlag;
	}

	public void setAssessmentFlag(AssessmentFlag assessmentFlag) {
		this.assessmentFlag = assessmentFlag;
	}

	/**
	 * Student čija je ovo vrijednost.
	 * @return student
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=true)
	@Fetch(FetchMode.SELECT)
	public User getAssigner() {
		return assigner;
	}

	public void setAssigner(User assigner) {
		this.assigner = assigner;
	}
	
	/**
	 * Vrijednost zastavice.
	 * 
	 * @return vrijednost
	 */
	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	/**
	 * Je li vrijednost zastavice postavljena ručno. Ako je, tada 
	 * se kao konačna vrijednost zastavice mora uzeti {@linkplain #manualValue};
	 * ako nije, tada se uzima ona vrijednost koja se automatski izračuna.
	 * 
	 * @return je li vrijednost zadana ručno?
	 */
	public boolean getManuallySet() {
		return manuallySet;
	}

	public void setManuallySet(boolean manuallySet) {
		this.manuallySet = manuallySet;
	}

	/**
	 * Vrijednost koja je zadana ručno. Ovo polje ignorirati ako je {@linkplain #getManuallySet()}
	 * po vrijednosti false.
	 * 
	 * @return ručno zadana vrijednost
	 */
	public boolean getManualValue() {
		return manualValue;
	}

	public void setManualValue(boolean manualValue) {
		this.manualValue = manualValue;
	}

	/**
	 * Zastavica koja se postavlja ako je prilikom izračuna vrijednosti došlo do greške.
	 * @return true za pogrešku, false inače
	 */
	public boolean getError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getAssessmentFlag() == null) ? 0 : getAssessmentFlag().hashCode());
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AssessmentFlagValue))
			return false;
		AssessmentFlagValue other = (AssessmentFlagValue) obj;
		if (getAssessmentFlag() == null) {
			if (other.getAssessmentFlag() != null)
				return false;
		} else if (!getAssessmentFlag().equals(other.getAssessmentFlag()))
			return false;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}
	
}
