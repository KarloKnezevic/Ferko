package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Razred predstavlja primjerak kolegija, tj. kolegij koji se održava točno definirane
 * akademske godine u točno definiranom semestru. Uz ovaj razred dalje se vežu studenti
 * koji ga tada slušaju, njihove grupe i sl.
 * 
 * @author marcupic
 *
 */
@NamedQueries({
    @NamedQuery(name="CourseInstance.listForYearSemesterStaff",query="select distinct ci from CourseInstance as ci, UserGroup as ug where ci.yearSemester=:yearSemester and ug.user=:user and ug.group.compositeCourseID LIKE :compositeCourseID and ug.group.relativePath LIKE '3/%' and ci.id=ug.group.compositeCourseID"),
    @NamedQuery(name="CourseInstance.listForYearSemester",query="select ci from CourseInstance as ci where ci.yearSemester=:yearSemester"),
    @NamedQuery(name="CourseInstance.listForYearSemesterKey",query="select ci from CourseInstance as ci where ci.yearSemester.id=:yearSemesterID"),
    @NamedQuery(name="CourseInstance.find",query="select ci from CourseInstance as ci where ci.yearSemester=:yearSemester and ci.course.isvuCode=:isvuCode"),
    @NamedQuery(name="CourseInstance.listForCourse",query="select ci from CourseInstance as ci where ci.course.isvuCode=:courseIsvuCode order by ci.yearSemester.academicYear desc")
})
@Entity
@Table(name="course_instances")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CourseInstance implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String id;
	private YearSemester yearSemester;
	private Course course;
	private Group primaryGroup;
	private Set<Assessment> assessments = new HashSet<Assessment>();
	private Set<AssessmentFlag> flags = new HashSet<AssessmentFlag>();
	private Set<IssueTopic> issueTopics = new HashSet<IssueTopic>();
	private Set<CourseComponent> courseComponents = new HashSet<CourseComponent>();
	private CourseInstanceIsvuData isvuData;
	private GradingPolicy gradingPolicy;
	
	public CourseInstance() {
	}
	
	/**
	 * Identifikator primjerka kolegija. Identifikator je oblika "2007Z/19674", odnosno
	 * odgovara spoju akademske godine, semestra i isvu sifre predmeta. Na ovaj način bi
	 * se trebalo olakšati pretraživanje i dohvaćanje primjeraka kolegija.
	 * 
	 * @return
	 */
	@Id
	@Column(length=16)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Kolegij kojem ovaj primjerak pripada.
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	
	/**
	 * Akademska godina i semestar u kojem se kolegij održava.
	 * 
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public YearSemester getYearSemester() {
		return yearSemester;
	}
	public void setYearSemester(YearSemester yearSemester) {
		this.yearSemester = yearSemester;
	}

	@OneToOne(fetch=FetchType.LAZY,cascade=CascadeType.ALL,optional=true)
	@JoinColumn(nullable=true)
	@Fetch(FetchMode.SELECT)
	public GradingPolicy getGradingPolicy() {
		return gradingPolicy;
	}
	public void setGradingPolicy(GradingPolicy gradingPolicy) {
		this.gradingPolicy = gradingPolicy;
	}
	
	/**
	 * Ulaz u hijerarhiju grupa ovog kolegija.
	 * 
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	@Fetch(FetchMode.SELECT)
	public Group getPrimaryGroup() {
		return primaryGroup;
	}
	public void setPrimaryGroup(Group primaryGroup) {
		this.primaryGroup = primaryGroup;
	}
	
	/**
	 * Sve provjere definirane na ovom primjerku kolegija. U ovom skupu
	 * hijerarhija je izravnata! Pripaziti na to.
	 * 
	 * @return skup provjera
	 */
	@OneToMany(mappedBy="courseInstance",fetch=FetchType.LAZY)
	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Assessment> getAssessments() {
		return assessments;
	}
	public void setAssessments(Set<Assessment> assessments) {
		this.assessments = assessments;
	}
	
	/**
	 * Sve zastavice definirane na ovom primjerku kolegija.
	 * 
	 * @return skup zastavica
	 */
	@OneToMany(mappedBy="courseInstance",fetch=FetchType.LAZY)
	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<AssessmentFlag> getFlags() {
		return flags;
	}
	public void setFlags(Set<AssessmentFlag> flags) {
		this.flags = flags;
	}
	
	/**
	 * Sve teme poruka, definirane na ovom primjerku kolegija.
	 * 
	 * @return skup tema
	 */
	@OneToMany(mappedBy="courseInstance",fetch=FetchType.LAZY)
	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<IssueTopic> getIssueTopics() {
		return issueTopics;
	}
	public void setIssueTopics(Set<IssueTopic> issueTopics) {
		this.issueTopics = issueTopics;
	}
	public void addIssueTopic(IssueTopic issueTopic) {
		this.getIssueTopics().add(issueTopic);
	}
	
	/**
	 * Sve komponente definirane na ovom primjerku kolegija
	 * @return
	 */
	@OneToMany(mappedBy="courseInstance",fetch=FetchType.LAZY)
	public Set<CourseComponent> getCourseComponents() {
		return courseComponents;
	}
	public void setCourseComponents(Set<CourseComponent> courseComponents) {
		this.courseComponents = courseComponents;
	}

	@OneToOne(fetch=FetchType.EAGER,cascade=CascadeType.ALL)
	@JoinColumn(nullable=true)
	@Fetch(FetchMode.SELECT)
	public CourseInstanceIsvuData getIsvuData() {
		return isvuData;
	}
	public void setIsvuData(CourseInstanceIsvuData isvuData) {
		this.isvuData = isvuData;
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
		if (!(obj instanceof CourseInstance))
			return false;
		final CourseInstance other = (CourseInstance) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
