package hr.fer.zemris.jcms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Rezultati studenta na provjeri znanja.
 * 
 * @author Ivan Krišto
 */
@NamedQueries({
    @NamedQuery(name="AssessmentConfChoiceAnswers.listAssessmentConfChoiceAnswersForAssessement",query="select acca from AssessmentConfChoiceAnswers as acca where acca.assessmentConfChoice=:assessmentConfChoice"),
    @NamedQuery(name="AssessmentConfChoiceAnswers.listAssessmentConfChoiceAnswersForAssessementAndGroup",query="select acca from AssessmentConfChoiceAnswers as acca where acca.assessmentConfChoice=:assessmentConfChoice and acca.group=:group"),
    @NamedQuery(name="AssessmentConfChoiceAnswers.getAssessmentConfChoiceAnswersForAssessementAndStudent",query="select acca from AssessmentConfChoiceAnswers as acca where acca.assessmentConfChoice=:assessmentConfChoice and acca.user=:user")
})
@Entity
@Table(name="assessmentConfChoiceAnswers", uniqueConstraints={
	@UniqueConstraint(columnNames={"assessmentConfChoice_id", "user_id"})
})
public class AssessmentConfChoiceAnswers {
	
	/** Identifikator. */
	private Long id;
	
	/** Korisnik čiji su podatci ovdje. */
	private User user;
	
	/** Korisnik koji je postavio podatke ovdje. */
	private User assigner;
	
	/** Opisnik provjere uz koju su ovi podatci vezani. */
	private AssessmentConfChoice assessmentConfChoice;
	
	/** Grupa na provjeri. */
	private String group;
	
	/** Studentovi odgovori na provjeri. */
	private String answers;
	
	/** Točnost odgovora. Za svaki odgovor je zapisana ispravnost T/N. */
	private String answersStatus;
	
	/** Je li student pristupio ovoj provjeri. */
	private boolean present;
	
	/**
	 * Konstruktor.
	 */
	public AssessmentConfChoiceAnswers() {
	}
	
	/**
	 * @return Id.
	 */
	@Id @GeneratedValue
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * @return Opisnik provjere uz koju su ovi podatci vezani.
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public AssessmentConfChoice getAssessmentConfChoice() {
		return this.assessmentConfChoice;
	}

	public void setAssessmentConfChoice(AssessmentConfChoice assessmentConfChoice) {
		this.assessmentConfChoice = assessmentConfChoice;
	}
	
	/**
	 * @return Grupa studenta ("putanja").
	 */
	@Column(name="assessmentGroup", length=15, nullable=true)
	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	/**
	 * @return Je li student pristupio ovoj provjeri.
	 */
	@Column(nullable=false)
	public boolean getPresent() {
		return this.present;
	}

	public void setPresent(boolean present) {
		this.present = present;
	}
	
	/**
	 * @return Korisnik čiji su podatci ovdje.
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return Korisnik koji je postavio podatke ovdje.
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getAssigner() {
		return this.assigner;
	}

	public void setAssigner(User assigner) {
		this.assigner = assigner;
	}

	/**
	 * Studentovi odgovori. Format:
	 * <table border="1">
	 * 	<tr>
	 * 		<td>JMBAG</td>
	 * 		<td>Grupa</td>
	 * 		<td>Odgovor1</td>
	 * 		<td>Odgovor2</td>
	 * 		<td>Odgovor3</td>
	 * 		<td>...</td>
	 * 		<td>OdgovorN</td>
	 * 	</tr>
	 * </table>
	 * Elementi su međusobno odvojeni tabovima.<br />
	 * Grupa može biti BLANK (nije označena).<br />
	 * Odgovor može biti BLANK (neodgovoreno).
	 * @return the answers
	 */
	@Column(length=250, nullable=true)
	public String getAnswers() {
		return this.answers;
	}

	/**
	 * @param answers the answers to set
	 */
	public void setAnswers(String answers) {
		this.answers = answers;
	}

	/**
	 * @return the answersStatus
	 */
	@Column(length=100, nullable=true)
	public String getAnswersStatus() {
		return this.answersStatus;
	}

	/**
	 * @param answersStatus the answersStatus to set
	 */
	public void setAnswersStatus(String answersStatus) {
		this.answersStatus = answersStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((getAssessmentConfChoice() == null) ? 0 : getAssessmentConfChoice().hashCode());
		result = PRIME * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AssessmentConfChoiceAnswers other = (AssessmentConfChoiceAnswers) obj;
		if (getAssessmentConfChoice() == null) {
			if (other.getAssessmentConfChoice() != null)
				return false;
		} else if (!getAssessmentConfChoice().equals(other.getAssessmentConfChoice()))
			return false;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}
	
}
