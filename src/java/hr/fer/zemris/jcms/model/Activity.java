package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Aktivnost. Ovo je obavijest studentu da se je u sustavu dogodilo nešto što je
 * njemu zanimljivo.
 * 
 * @author marcupic
 */
@NamedQueries({
	@NamedQuery(name="Activity.forUser1",query="select a from Activity as a where a.user=:user and archived=false order by date desc"),
	@NamedQuery(name="Activity.forUser2",query="select a from Activity as a where a.user=:user and a.date>=:date and archived=false order by date desc"),
	@NamedQuery(name="Activity.forUser3",query="select a from Activity as a where a.user=:user and a.context=:context and archived=false order by date desc"),
	@NamedQuery(name="Activity.forUser4",query="select a from Activity as a where a.user=:user and a.date>=:date and a.context=:context and archived=false order by date desc"),
	@NamedQuery(name="Activity.forUser5",query="select a from Activity as a where a.user=:user order by date desc"),
	@NamedQuery(name="Activity.forUser6",query="select a from Activity as a where a.user=:user and a.date>=:date order by date desc"),
	@NamedQuery(name="Activity.forUser7",query="select a from Activity as a where a.user=:user and a.context=:context order by date desc"),
	@NamedQuery(name="Activity.forUser8",query="select a from Activity as a where a.user=:user and a.date>=:date and a.context=:context order by date desc")
})
@Entity
@Table(name="activities")
public class Activity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Identifikator aktivnosti.
	 */
	private Long id;
	
	/**
	 * Korisnik o kojem se radi.
	 */
	private User user;
	/**
	 * Datum aktivnosti.
	 */
	private Date date;
	/**
	 * Troslovna oznaka vrste aktivnosti.
	 */
	private String kind;

	/**
	 * Nullabilni property; ako je aktivnost vezana uz kolegij, ovdje
	 * će pisati cid=ID_PRIMJERKA_KOLEGIJA. Ako nije, bit će <code>null</code>.
	 * Eventualno, ako je vezan uz nešto drugo, bit će korišten prikladan format;
	 * međutim, uvijek će biti samo jedna informacija (ili primjerak kolegija, ili 
	 * nešto drugo) tako da ovo polje može ući u indeks i pomoći brzom pretraživanju.
	 */
	private String context;
	
	/**
	 * Svi potrebni podatci kako bi se mogla rekonstruirati poruka: primjerice, broj bodova, ocjena, itd, itd.
	 */
	private String data;
	/**
	 * Je li aktivnost arhivirana? Po defaultu ovo je <code>false</code>.
	 */
	private boolean archived;
	/**
	 * Je li aktivnost već viđena? Po defaultu ovo je <code>false</code>.
	 */
	private boolean viewed;
	
	/**
	 * Primarni ključ.
	 * 
	 * @return identifikator
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	@Column(length=3,nullable=false)
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}

	@Column(length=25)
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}

	@Column(length=2048)
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	public boolean getArchived() {
		return archived;
	}
	public void setArchived(boolean archived) {
		this.archived = archived;
	}
	
	public boolean getViewed() {
		return viewed;
	}
	public void setViewed(boolean viewed) {
		this.viewed = viewed;
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
		Activity other = (Activity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
