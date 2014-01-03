package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Assessment je jedna konkretna provjera (moze biti i
 * virtualna). Virtualne provjere su provjere koje imaju
 * djecu na temelju koje računaju bodove. To su zapravo
 * hijerarhijske provjere.
 *
 * Ovdje definirati jos:
 * <ul>
 * <li>prag za prolaz (dopustiti? unos oblika 5.5 bodova ili pak 33.3333%)</li>
 * <li>faktor skaliranja</li>
 * <li>rezanje broja bodova na neki maksimum</li>
 * </ul>
 *  
 * Provjera MOŽE imati (a i ne mora) zastavicu koja govori tko može ići na tu provjeru,
 * te MOŽE imati generiran raspored studenata (što će se manifestirati stvaranjem
 * odgovarajuće grupe i njenih podgrupa kroz utility za generiranje rasporeda). Ako 
 * se želi raditi raspored, i ako nema zastavice koja govori tko može izaći na provjeru,
 * tada na provjeru mogu izaći svi upisani studenti. Alternativa je da se korisnika natjera
 * da kaže tko može na provjeru tako kroz wizard stvori odgovarajuću zastavicu (tipa SVI ili
 * uploada tko može ili upiše program).
 * 
 * Provjera MOŽE imati i raspored asistenata (koji asistent ide u koju dvoranu).
 * 
 * Ovo mora biti ili nadrazred ili na neki drugi način povezan razred koji je
 * osnova za izvođenje svih vrsta provjera.
 * 
 * Da li ovdje dodati još neke zastavice? Tipa:
 * <ul>
 * <li>Treba asistente</li>
 * <li>Treba raspored studenata</li>
 * </ul>
 * kako bi se to nudilo samo na onim provjerama koje to doista trebaju?
 *
 * Ovdje definirati jos:
 * <ul>
 * <li>prag za prolaz (dopustiti? unos oblika 5.5 bodova ili pak 33.3333%)</li>
 * <li>faktor skaliranja</li>
 * <li>rezanje broja bodova na neki maksimum</li>
 * </ul>
 * 
 * @author marcupic
 */
@Entity
@Table(name="assessments", uniqueConstraints={
	@UniqueConstraint(columnNames={"courseInstance_id","shortName"})
})
@NamedQueries({
    @NamedQuery(name="Assessment.findForChainedParent",query="select a from Assessment as a where a.chainedParent=:chainedParent"),
    @NamedQuery(name="Assessment.listForCourseInstance",query="select a from Assessment as a where a.courseInstance.id=:courseInstanceID"),
    @NamedQuery(name="Assessment.findTaggedOnSemester",query="select a from Assessment as a where a.courseInstance.yearSemester=:yearSemester and a.assessmentTag=:assessmentTag"),
    @NamedQuery(name="Assessment.findShortNamesOnSemester",query="select a.courseInstance.course.isvuCode, a.shortName from Assessment as a where a.courseInstance.yearSemester=:yearSemester")
})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Assessment implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String shortName;
	private AssessmentTag assessmentTag;
	private CourseInstance courseInstance;
	private CourseWideEvent event;
	private Assessment chainedChild;
	private Assessment chainedParent;
	private Assessment parent;
	private Set<Assessment> children = new HashSet<Assessment>();
	private AssessmentFlag assessmentFlag;
	private Group group;
	private Set<AssessmentRoom> rooms = new HashSet<AssessmentRoom>();
	private AssessmentConfiguration assessmentConfiguration;
	private String programType;
	private String program;
	private int programVersion;
	private Set<AssessmentScore> score = new HashSet<AssessmentScore>();
	private Set<AssessmentAssistantSchedule> assistantSchedule = new HashSet<AssessmentAssistantSchedule>();
	private Double maxScore;
	private char visibility;
	private boolean assessmentLocked;
	private int sortIndex;

	public Assessment() {
	}
	
	public Assessment(Long id) {
		this.id = id;
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Indeks koji će se koristiti kod sortiranja, prije no što se obavi
	 * usporedba direktno po imenima.
	 * 
	 * @return vrijednost za sortiranje
	 */
	public int getSortIndex() {
		return sortIndex;
	}
	
	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}
	
	@OneToMany(mappedBy="assessment",fetch=FetchType.LAZY,cascade=CascadeType.REMOVE)
	public Set<AssessmentAssistantSchedule> getAssistantSchedule() {
		return assistantSchedule;
	}
	
	public void setAssistantSchedule(
			Set<AssessmentAssistantSchedule> assistantSchedule) {
		this.assistantSchedule = assistantSchedule;
	}
	
	@OneToOne(mappedBy="assessment",fetch=FetchType.EAGER,cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	@Fetch(FetchMode.SELECT)
	public AssessmentConfiguration getAssessmentConfiguration() {
		return assessmentConfiguration;
	}

	public void setAssessmentConfiguration(
			AssessmentConfiguration assessmentConfiguration) {
		this.assessmentConfiguration = assessmentConfiguration;
	}

	@Column(length=100,nullable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length=10,nullable=false)
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public AssessmentTag getAssessmentTag() {
		return assessmentTag;
	}

	public void setAssessmentTag(AssessmentTag assessmentTag) {
		this.assessmentTag = assessmentTag;
	}

	@OneToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	@JoinColumn(name="event_id",nullable=true)
	@Fetch(FetchMode.SELECT)
	public CourseWideEvent getEvent() {
		return event;
	}
	
	public void setEvent(CourseWideEvent event) {
		this.event = event;
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

	@OneToOne(mappedBy="chainedParent", fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public Assessment getChainedChild() {
		return chainedChild;
	}
	
	public void setChainedChild(Assessment chainedChild) {
		this.chainedChild = chainedChild;
	}
	
	@OneToOne(fetch=FetchType.LAZY)
	@Fetch(FetchMode.SELECT)
	public Assessment getChainedParent() {
		return chainedParent;
	}
	
	public void setChainedParent(Assessment chainedParent) {
		this.chainedParent = chainedParent;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public Assessment getParent() {
		return parent;
	}
	
	public void setParent(Assessment parent) {
		this.parent = parent;
	}
	
	@OneToMany(mappedBy="parent",fetch=FetchType.LAZY)
	public Set<Assessment> getChildren() {
		return children;
	}

	public void setChildren(Set<Assessment> children) {
		this.children = children;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public AssessmentFlag getAssessmentFlag() {
		return assessmentFlag;
	}

	public void setAssessmentFlag(AssessmentFlag assessmentFlag) {
		this.assessmentFlag = assessmentFlag;
	}

	@ManyToOne(fetch=FetchType.LAZY,cascade=CascadeType.ALL)
	@Fetch(FetchMode.SELECT)
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@OneToMany(mappedBy="assessment", fetch=FetchType.LAZY)
	public Set<AssessmentRoom> getRooms() {
		return rooms;
	}

	public void setRooms(Set<AssessmentRoom> rooms) {
		this.rooms = rooms;
	}

	/**
	 * Vrsta programa koji određuje konačnu vrijednost bodova,
	 * te je li student prošao ili nije zadanu provjeru. Ako je
	 * "java" onda je to direktno napisan java kod.
	 * @return vrsta programa
	 */
	@Column(length=10)
	public String getProgramType() {
		return programType;
	}

	public void setProgramType(String programType) {
		this.programType = programType;
	}

	/**
	 * Program koji određuje konačnu vrijednost bodova,
	 * te je li student prošao ili nije zadanu provjeru. Ovo je uvijek čisti java kod.
	 * Ako je kao vrsta postavljeno nešto različito od "java", tada to znači samo
	 * da se u komentarima na početku koda nalazi nešto na temelju čega je sam kod
	 * generiran, i na temelju čega se wizard može inicijalizirati.
	 *  
	 * @return program
	 */
	@Column(length=16000)
	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	/**
	 * Verzija programa. Prilikom svake izmjene programa ovo polje OBAVEZNO
	 * uvećati za jedan. Ovo polje služi optimizaciji kompajliranja programa.
	 * 
	 * @return verzija programa
	 */
	public int getProgramVersion() {
		return programVersion;
	}
	
	public void setProgramVersion(int programVersion) {
		this.programVersion = programVersion;
	}
	
	@OneToMany(mappedBy="assessment",fetch=FetchType.LAZY)
	public Set<AssessmentScore> getScore() {
		return score;
	}
	
	public void setScore(Set<AssessmentScore> score) {
		this.score = score;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getCourseInstance() == null) ? 0 : getCourseInstance().hashCode());
		result = prime * result
				+ ((getShortName() == null) ? 0 : getShortName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Assessment))
			return false;
		Assessment other = (Assessment) obj;
		if (getCourseInstance() == null) {
			if (other.getCourseInstance() != null)
				return false;
		} else if (!getCourseInstance().equals(other.getCourseInstance()))
			return false;
		if (getShortName() == null) {
			if (other.getShortName() != null)
				return false;
		} else if (!getShortName().equals(other.getShortName()))
			return false;
		return true;
	}
	
	/**
	 * Maksimalni broj bodova koji se može ostvariti na ovoj provjeri.
	 * 
	 * @return maksimalni broj bodova; može biti null.
	 */
	public Double getMaxScore() {
		return maxScore;
	}
	
	public void setMaxScore(Double maxScore) {
		this.maxScore = maxScore;
	}

	/**
	 * Utvrđuje status vidljivosti ove provjere studentu. Moguće vrijednosti su:
	 * <ul>
	 * <li><strong>H</strong> (hidden) - student ovo ne vidi</li>
	 * <li><strong>E</strong> (hidden when empty) - student ovo vidi samo je bio na toj provjeri, inače ne vidi</li>
	 * <li><strong>V</strong> (visible) - student ovo vidi</li>
	 * </ul>
	 * @return status vidljivosti
	 */
	public char getVisibility() {
		return visibility;
	}

	public void setVisibility(char visibility) {
		this.visibility = visibility;
	}

	/**
	 * Vraća je li provjera zaključana. Ako je, program za izračun vrijednosti provjere
	 * neće mijenjati podatke u provjeri, čak i ako se raw podaci promijene.
	 * @return true ako je provjera zaključana, false inače
	 */
	public boolean getAssessmentLocked() {
		return assessmentLocked;
	}

	public void setAssessmentLocked(boolean assessmentLocked) {
		this.assessmentLocked = assessmentLocked;
	}
}