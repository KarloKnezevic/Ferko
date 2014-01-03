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
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * AssessmentFlag je jedna konkretna zastavica.
 * Vrijednost zastavice može se definirati ili
 * putem uploada (datoteka oblika JMBAG#vrijednost,
 * ili samo JMBAG uz mogučnost da se definira da tim
 * studentima zastavicu treba dodatno podignuti ili
 * spustiti što je inkrementalni upload, ili reći da
 * samo ti studenti imaju zastavicu dignutu ili spuštenu
 * i da se staro stanje treba pregaziti), ili izračunski
 * putem nekog programa koji korisnik definira.
 * 
 * @author marcupic
 *
 */
@NamedQueries({
    @NamedQuery(name="AssessmentFlag.listForCourseInstance",query="select a from AssessmentFlag as a where a.courseInstance.id=:courseInstanceID"),
    @NamedQuery(name="AssessmentFlag.listUsersWithFlagUp", query=" select afv.user from AssessmentFlagValue as afv where afv.assessmentFlag=:assessmentFlag AND afv.value=true ")
})
@Entity
@Table(name="assessment_flags")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AssessmentFlag implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String shortName;
	private AssessmentFlagTag assesmentFlagTag;
	private CourseInstance courseInstance;
	private Set<AssessmentFlagValue> values = new HashSet<AssessmentFlagValue>();
	private String programType;
	private String program;
	private int programVersion;
	private char visibility;
	private int sortIndex;

	public AssessmentFlag() {
	}

	public AssessmentFlag(Long id) {
		this.id = id;
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

	/**
	 * Naziv provjere.
	 * @return naziv
	 */
	@Column(length=100,nullable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Kratki naziv provjere (do 10 znakova).
	 * @return kratki naziv
	 */
	@Column(length=10,nullable=false)
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * Pridružena oznaka zastavice provjere.
	 * @return oznaka
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public AssessmentFlagTag getAssesmentFlagTag() {
		return assesmentFlagTag;
	}

	public void setAssesmentFlagTag(AssessmentFlagTag assesmentFlagTag) {
		this.assesmentFlagTag = assesmentFlagTag;
	}

	/**
	 * Primjerak kolegija uz koji je zastavica vezana.
	 * @return primjerak kolegija
	 */
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
	 * Skup vrijednosti zastavica za korisnike ovog kolegija.
	 * Ako za nekog korisnika vrijednost ne postoji, treba je
	 * tretirati kao false.
	 * 
	 * @return skup vrijednosti zastavica
	 */
	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="assessmentFlag")
	public Set<AssessmentFlagValue> getValues() {
		return values;
	}
	public void setValues(Set<AssessmentFlagValue> values) {
		this.values = values;
	}

	/**
	 * Vrsta programa koji određuje vrijednost zastavice. Ako je
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
	 * Program koji određuje vrijednost zastavice. Ovo je uvijek čisti java kod.
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
	 * Verzija programa. Obavezno uvećati kod svake izmjene programa.
	 * 
	 * @return verzija programa
	 */
	public int getProgramVersion() {
		return programVersion;
	}
	
	public void setProgramVersion(int programVersion) {
		this.programVersion = programVersion;
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
		if (!(obj instanceof AssessmentFlag))
			return false;
		AssessmentFlag other = (AssessmentFlag) obj;
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
	 * Utvrđuje status vidljivosti ove zastavice studentu. Moguće vrijednosti su:
	 * <ul>
	 * <li><strong>H</strong> (hidden) - student ovo ne vidi</li>
	 * <li><strong>E</strong> (hidden when empty) - student ovo vidi samo je zastavica postavljena na true, inače ne vidi</li>
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
}
