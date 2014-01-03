package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Razred predstavlja jedan semestar u jednoj akademskoj godini.
 * 
 * @author marcupic
 *
 */
@NamedQueries({
    @NamedQuery(name="YearSemester.find",query="select ys from YearSemester as ys where ys.academicYear=:year and ys.semester=:semester"),
    @NamedQuery(name="YearSemester.findByYear",query="select ys from YearSemester as ys where ys.academicYear=:year"),
    @NamedQuery(name="YearSemester.list",query="select ys from YearSemester as ys")
})
@Entity
@Table(name="year_semesters")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class YearSemester implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String id;
	private String academicYear;
	private String semester;
	private Date startsAt;
	private Date endsAt;
	
	public YearSemester() {
	}
	
	public YearSemester(String id, String academicYear, String semester) {
		super();
		this.id = id;
		this.academicYear = academicYear;
		this.semester = semester;
	}

	public YearSemester(String academicYear, String semester) {
		super();
		this.academicYear = academicYear;
		this.semester = semester;
		calculateId();
	}

	/**
	 * Identifikator.
	 * @return
	 */
	@Id
	@Column(length=5)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Trenutna akademska godina. Podatak je oblika "2007/2008".
	 * 
	 * @return
	 */
	@Column(nullable=false,length=9,unique=false)
	public String getAcademicYear() {
		return academicYear;
	}
	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}
	
	/**
	 * Trenutni semestar. Može biti "zimski", "ljetni" ili po potrebi nešto treće.
	 *  
	 * @return
	 */
	@Column(nullable=false,length=10,unique=false)
	public String getSemester() {
		return semester;
	}
	public void setSemester(String semester) {
		this.semester = semester;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof YearSemester))
			return false;
		final YearSemester other = (YearSemester) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	public void calculateId() {
		id = academicYear.substring(0,4)+semester.substring(0,1).toUpperCase();
	}
	
	@Transient
	public String getFullTitle() {
		return academicYear + " - " + semester;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(Date startsAt) {
		this.startsAt = startsAt;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(Date endsAt) {
		this.endsAt = endsAt;
	}
	
}
