package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Model asistenta raspoređenog za čuvanje provjere znanja. Ovdje se zasebno vode
 * assessment i room (umjesto objekta AssessmentRoom) zato što prilikom dodavanja
 * asistenata na provjeru oni ne moraju odmah biti i raspoređeni (što na ovaj drugi
 * način ne bi bilo moguće).
 * 
 * @author marcupic
 *
 */
@Entity
@Table(name="assessment_assistant_schedules", uniqueConstraints={
	@UniqueConstraint(columnNames={"assessment_id","user_id"})
})
public class AssessmentAssistantSchedule implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Assessment assessment;
	private User user;
	private AssessmentRoom room;
	private int position;
	
	public AssessmentAssistantSchedule() {
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

	/**
	 * Provjera znanja.
	 * @return provjera
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public Assessment getAssessment() {
		return assessment;
	}

	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}

	/**
	 * Asistent.
	 * @return asistent
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Dodijeljena prostorija.
	 * @return prostorija
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=true)
	public AssessmentRoom getRoom() {
		return room;
	}

	public void setRoom(AssessmentRoom room) {
		this.room = room;
	}

	/**
	 * Redni broj u prostoriji.
	 * @return redni broj
	 */
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getAssessment() == null) ? 0 : getAssessment().hashCode());
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AssessmentAssistantSchedule))
			return false;
		AssessmentAssistantSchedule other = (AssessmentAssistantSchedule) obj;
		if (getAssessment() == null) {
			if (other.getAssessment() != null)
				return false;
		} else if (!getAssessment().equals(other.getAssessment()))
			return false;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}
	
}
