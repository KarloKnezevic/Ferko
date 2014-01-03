package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.service.assessments.AssessmentStatus;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OptimisticLockType;

/**
 * Id je string oblika "courseInstanceID/userID/assessmentID" i nije GeneratedValue.
 * NE. Odustajemo od ovoga - nije potrebno; dovoljno se efikasno moze dohvatiti i po provjera.kolegijid-u.
 * score ne smije biti null. Ako student nije pisao provjeru, ne smije
 * postojati ovakav zapis, ili moze ali tada {@link #present} mora biti false! 
 * Ovaj razred predstavlja ukupan broj bodova studenta na
 * navedenoj provjeri, nakon svih rezanja, skaliranja i svega što se toj provjeri radi.
 * Pri tome {@linkplain #score} predstavlja konačan broj bodova (rezanje, skaliranje i sl)
 * a {@linkplain #rawScore} predstavlja direktan broj bodova dobiven na temelju načina kako
 * se provjera interno boduje, i to NIJE konačan broj bodova.
 * 
 * Assigner je korisnik koji je unio ove bodove. Treba vidjeti što
 * je sa slučajevima gdje više ljudi definira ukupni score.
 *
 * Ovaj razred modelira korisnikov konačni broj bodova na ovoj provjeri,
 * ma kako da se je do njega došlo.
 * 
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="AssessmentScore.listForCourseInstance",query="select a from AssessmentScore as a where a.assessment.courseInstance.id=:courseInstanceID"),
    @NamedQuery(name="AssessmentScore.listForAssessment",query="select a from AssessmentScore as a where a.assessment=:assessment"),
    @NamedQuery(name="AssessmentScore.getForAssessmentAndUser",query="select a from AssessmentScore as a where a.assessment=:assessment and a.user=:user"),
    @NamedQuery(name="AssessmentScore.listForCourseInstanceAndUser",query="select a from AssessmentScore as a join fetch a.assessment where a.assessment.courseInstance=:courseInstance and a.user=:user")
})
@Entity
@Table(name="assessment_score", uniqueConstraints={
	@UniqueConstraint(columnNames={"assessment_id","user_id"})
})
@Cache(usage=CacheConcurrencyStrategy.NONE)
@org.hibernate.annotations.Entity(dynamicInsert=false,dynamicUpdate=true,optimisticLock=OptimisticLockType.VERSION)
public class AssessmentScore implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Assessment assessment;
	private User user;
	private double score;
	private double rawScore;
	private User assigner;
	private boolean error;
	private boolean present;
	private boolean rawPresent;
	private AssessmentStatus status;
	private short rank;
	private short effectiveRank;
	private double effectiveScore;
	private boolean effectivePresent;
	private AssessmentStatus effectiveStatus;
	private boolean scoreLocked;
	private long version;
	
	public AssessmentScore() {
	}

	public AssessmentScore(Assessment assessment, User user) {
		super();
		this.assessment = assessment;
		this.user = user;
	}

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
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	public Assessment getAssessment() {
		return assessment;
	}

	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getRawScore() {
		return rawScore;
	}

	public void setRawScore(double rawScore) {
		this.rawScore = rawScore;
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

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public boolean getPresent() {
		return present;
	}
	
	public void setPresent(boolean present) {
		this.present = present;
	}

	public boolean getRawPresent() {
		return rawPresent;
	}
	
	public void setRawPresent(boolean rawPresent) {
		this.rawPresent = rawPresent;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable=false)
	public AssessmentStatus getStatus() {
		return status;
	}
	public void setStatus(AssessmentStatus status) {
		this.status = status;
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
		if (!(obj instanceof AssessmentScore))
			return false;
		AssessmentScore other = (AssessmentScore) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	public short getRank() {
		return rank;
	}
	public void setRank(short rank) {
		this.rank = rank;
	}

	public double getEffectiveScore() {
		return effectiveScore;
	}
	public void setEffectiveScore(double effectiveScore) {
		this.effectiveScore = effectiveScore;
	}

	public boolean getEffectivePresent() {
		return effectivePresent;
	}
	public void setEffectivePresent(boolean effectivePresent) {
		this.effectivePresent = effectivePresent;
	}

	public AssessmentStatus getEffectiveStatus() {
		return effectiveStatus;
	}
	public void setEffectiveStatus(AssessmentStatus effectiveStatus) {
		this.effectiveStatus = effectiveStatus;
	}

	public short getEffectiveRank() {
		return effectiveRank;
	}
	public void setEffectiveRank(short effectiveRank) {
		this.effectiveRank = effectiveRank;
	}
	
	public boolean getScoreLocked() {
		return scoreLocked;
	}
	public void setScoreLocked(boolean scoreLocked) {
		this.scoreLocked = scoreLocked;
	}
}
