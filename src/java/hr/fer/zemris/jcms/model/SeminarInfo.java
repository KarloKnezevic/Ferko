package hr.fer.zemris.jcms.model;

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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@NamedQueries({
    @NamedQuery(name="SeminarInfo.findSeminarInfosForRootStudent",query="select si from SeminarInfo as si where si.seminarRoot=:seminarRoot and si.student=:student"),
    @NamedQuery(name="SeminarInfo.findSeminarInfosForRoot",query="select si from SeminarInfo as si where si.seminarRoot=:seminarRoot"),
    @NamedQuery(name="SeminarInfo.findSeminarInfosForGroup",query="select si from SeminarInfo as si where si.group=:group")
})
@Entity
@Table(name="seminar_infos", uniqueConstraints=@UniqueConstraint(columnNames={"seminarRoot_id","student_id","mentor_id"}))
@Cache(usage=CacheConcurrencyStrategy.NONE)
public class SeminarInfo {
	private Long id;
	private SeminarRoot seminarRoot;
	private User student;
	private User mentor;
	private Group group;
	private GroupWideEvent event;
	private String title;
	private CourseInstance courseInstance;
	private String roomText;
	
	public SeminarInfo() {
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public SeminarRoot getSeminarRoot() {
		return seminarRoot;
	}

	public void setSeminarRoot(SeminarRoot seminarRoot) {
		this.seminarRoot = seminarRoot;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public User getStudent() {
		return student;
	}

	public void setStudent(User student) {
		this.student = student;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=true)
	@Fetch(FetchMode.SELECT)
	public User getMentor() {
		return mentor;
	}

	public void setMentor(User mentor) {
		this.mentor = mentor;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=true)
	@Fetch(FetchMode.SELECT)
	public GroupWideEvent getEvent() {
		return event;
	}

	public void setEvent(GroupWideEvent event) {
		this.event = event;
	}

	@Column(length=500, nullable=false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	/**
	 * Obzirom da nastavnici kao prostoriju mogu upisati bilo što, ovo polje će
	 * preslikavati tekst koji su oni upisali, ako se to ne može preslikati u neku
	 * postojeću i poznatu prostoriju u sustavu.
	 * 
	 * @return opis mjesta odvijanja seminara
	 */
	@Column(nullable=false, length=100)
	public String getRoomText() {
		return roomText;
	}
	
	public void setRoomText(String roomText) {
		this.roomText = roomText;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((mentor == null) ? 0 : mentor.hashCode());
		result = prime * result
				+ ((seminarRoot == null) ? 0 : seminarRoot.hashCode());
		result = prime * result + ((student == null) ? 0 : student.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeminarInfo other = (SeminarInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (mentor == null) {
			if (other.mentor != null)
				return false;
		} else if (!mentor.equals(other.mentor))
			return false;
		if (seminarRoot == null) {
			if (other.seminarRoot != null)
				return false;
		} else if (!seminarRoot.equals(other.seminarRoot))
			return false;
		if (student == null) {
			if (other.student != null)
				return false;
		} else if (!student.equals(other.student))
			return false;
		return true;
	}

}
