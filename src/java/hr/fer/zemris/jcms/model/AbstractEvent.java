package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.model.extra.EventStrength;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

/**
 * Razred koji predstavlja događaj. Događaj je definiran početkom i trajanjem,
 * a može imati pridruženo i mjesto. Postoji nekoliko vrsta događaja. Ovaj razred
 * je apstraktan.
 * 
 * @author marcupic
 */
@Entity
@Table(name="events")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="dtype",discriminatorType=DiscriminatorType.CHAR)
@DiscriminatorValue("*")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class AbstractEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private String title;
	private Date start;
	private int duration;
	private Room room;
	private EventStrength strength = EventStrength.STRONG;
	private String specifier;
	private User issuer;
	private boolean deadline;
	private boolean hidden;
	private String context;
	
	public AbstractEvent() {
	}
	
	/**
	 * Identifikator događaja.
	 * @return
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(length=250, nullable=true)
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	
	/**
	 * Ako ovaj event zapravo predstavlja deadline, tada ovdje treba biti true, i trajanje 0.
	 * Inače ovdje treba biti false i trajanje veće od 0. 
	 * @return
	 */
	public boolean isDeadline() {
		return deadline;
	}
	public void setDeadline(boolean deadline) {
		this.deadline = deadline;
	}
	
	/**
	 * Zastavica koja kaze da li je ovaj event skriven ili nije, tj treba li ga prikazati ili ne
	 * @return
	 */
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	/**
	 * Snaga događaja. Za detalje pogledati {@linkplain EventStrength}.
	 * @return
	 */
	@Enumerated
	public EventStrength getStrength() {
		return strength;
	}
	public void setStrength(EventStrength strength) {
		this.strength = strength;
	}
	
	/**
	 * Proizvoljan opis događaja. Trebao bi uključivati ime.
	 * @return
	 */
	@Column(nullable=false,length=100,unique=false)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Početak događaja. Mora biti zadan.
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	@Index(name="events_date_index")
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}

	/**
	 * Trajanje događaja u minutama. Mora biti zadano. Default je 0. 
	 * @return
	 */
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * Mjesto odvijanja događaja. Ne mora biti zadano.
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=true)
	public Room getRoom() {
		return room;
	}
	public void setRoom(Room room) {
		this.room = room;
	}

	/**
	 * Polje koje služi za označavanje semantički povezanih događaja. Primjerice,
	 * očekuje se da se prilikom importa satnice na početku semestra stvaraju
	 * događaji čiji će Specifier biti oblika semestar/satnica (primjerice, "2007L/satnica")
	 * kako bi se kod nove verzije satnice svi prethodno stvoreni eventi mogli jednostavno
	 * izbrisati (delete ... where specifier="2007L/satnica") i potom nanovo učitati.
	 * Specifier bi se mogao iskoristiti i za oznacavanje vrste dogadaja: npr.
	 * <ul>
	 * <li>2007L/satnica/P - predavanje</li>
	 * <li>2007L/satnica/L - laboratorijske vježbe</li>
	 * </ul>
	 * @return
	 */
	@Column(nullable=true,length=20,unique=false)
	public String getSpecifier() {
		return specifier;
	}
	public void setSpecifier(String specifier) {
		this.specifier = specifier;
	}

	/**
	 * Osoba koja je definirala/stvorila ovaj događaj. Može biti null, za događaje
	 * koje je stvorio sustav.
	 * 
	 * @return
	 */
	@ManyToOne
	@JoinColumn(nullable=true)
	public User getIssuer() {
		return issuer;
	}
	public void setIssuer(User issuer) {
		this.issuer = issuer;
	}
	
	@Transient
	public String getStartAsText() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(getStart());
	}
	
	public boolean setStartFromText(String text) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			setStart(sdf.parse(text));
		} catch (ParseException e) {
			return false;
		}
		return true;
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
		if (!(obj instanceof AbstractEvent))
			return false;
		final AbstractEvent other = (AbstractEvent) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
