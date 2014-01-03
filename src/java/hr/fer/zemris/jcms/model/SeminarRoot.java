package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.periodicals.impl.SeminarImporter;

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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@NamedQueries({
    @NamedQuery(name="SeminarRoot.findSeminarRootsForSemester",query="select sr from SeminarRoot as sr where sr.semester=:semester"),
    @NamedQuery(name="SeminarRoot.findActiveSeminarRoots",query="select sr from SeminarRoot as sr where sr.active=true"),
    @NamedQuery(name="SeminarRoot.listSeminarRoots",query="select sr from SeminarRoot as sr")
})
@Entity
@Table(name="seminar_roots")
@Cache(usage=CacheConcurrencyStrategy.NONE)
public class SeminarRoot {

	private Long id;
	private YearSemester semester;
	private boolean active;
	private String source;
	private Group rootGroup;
	
	public SeminarRoot() {
		// TODO Auto-generated constructor stub
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
	public YearSemester getSemester() {
		return semester;
	}

	public void setSemester(YearSemester semester) {
		this.semester = semester;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Source opisuje kako doći do izvornih podataka s drugih sustava. 
	 * Opći oblik je "protokol", pa razmak, pa parametri. Trenutno je podržan
	 * protokol ferweb_v1, koji ima dva parametra: url za dohvat grupa te url 
	 * za dohvat termina prezentacija (separator je takoder razmak). Ovo trenutno
	 * koristi podsustav {@linkplain SeminarImporter} pa se tamo mogu pogledati
	 * detalji oko formata navedenih datoteka kao i koji se parseri koriste.
	 * 
	 * @return izvor podataka
	 */
	@Column(length=1000,nullable=false)
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public Group getRootGroup() {
		return rootGroup;
	}

	public void setRootGroup(Group rootGroup) {
		this.rootGroup = rootGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		SeminarRoot other = (SeminarRoot) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
