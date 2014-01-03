package hr.fer.zemris.jcms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Model jedne prostorije koja može biti korištena za predavanja, ispite te labose
 * (ovisno o definiranim kapacitetima).
 *
 * Obzirom da je ovdje u priču upao i {@linkplain Venue}, kako to sada povezati s
 * FERWeb rezervacijama, tj. kako znati što se može rezervirati a što ne?
 * 
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="Room.list",query="select r from Room as r"),
    @NamedQuery(name="Room.findByShortName",query="select r from Room as r where r.venue.shortName=:venueShortName and r.shortName=:roomShortName"),
    @NamedQuery(name="Room.listByVenue",query="select r from Room as r where r.venue.shortName=:venueShortName")
})
@Entity
@Table(name="rooms")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Room {
	private String id;
	private String name;
	private String shortName;
	private String locator;
	private int lecturePlaces;
	private int exercisePlaces;
	private int assessmentPlaces;
	private int assessmentAssistants;
	private boolean publicRoom = true;
	private Venue venue;
	
	public Room() {

	}

	/**
	 * Identifikator. Identifikator je oblika "VenueShortName/RoomShortName",
	 * npr. "FER/D339A".
	 * 
	 * @return identifikator
	 */
	@Id
	@Column(length=21)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Kratko ime prostorije; primjerice: "D339A".
	 * 
	 * @return kratko ime prostorije
	 */
	@Column(length=10,nullable=false)
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	/**
	 * Ime prostorije; primjerice: "Laboratorij za tehnologije znanja".
	 * 
	 * @return ime prostorije
	 */
	@Column(length=100,nullable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Lokator pomoću kojega se može doći do mape ili drugog načina određivanja
	 * pozicije navedene prostorije.
	 * 
	 * @return lokator
	 */
	@Column(length=100,nullable=true)
	public String getLocator() {
		return locator;
	}

	public void setLocator(String locator) {
		this.locator = locator;
	}

	/**
	 * Broj mjesta za predavanja.
	 * @return broj mjesta
	 */
	public int getLecturePlaces() {
		return lecturePlaces;
	}

	public void setLecturePlaces(int lecturePlaces) {
		this.lecturePlaces = lecturePlaces;
	}

	/**
	 * Broj mjesta za laboratorijske vježbe.
	 * @return broj mjesta
	 */
	public int getExercisePlaces() {
		return exercisePlaces;
	}

	public void setExercisePlaces(int exercisePlaces) {
		this.exercisePlaces = exercisePlaces;
	}

	/**
	 * Broj mjesta za ispite.
	 * @return broj mjesta
	 */
	public int getAssessmentPlaces() {
		return assessmentPlaces;
	}

	public void setAssessmentPlaces(int assessmentPlaces) {
		this.assessmentPlaces = assessmentPlaces;
	}

	/**
	 * Broj asistenata koji su potrebni za čuvanje ispita u ovoj dvorani.
	 * 
	 * @return broj asistenata
	 */
	public int getAssessmentAssistants() {
		return assessmentAssistants;
	}
	
	public void setAssessmentAssistants(int assessmentAssistants) {
		this.assessmentAssistants = assessmentAssistants;
	}
	/**
	 * Je li ovo javna prostorija, ili se nalazi na nekom zavodu i nije
	 * dostupna svima?
	 * @return true ako je javna, false inače 
	 */
	public boolean getPublicRoom() {
		return publicRoom;
	}

	public void setPublicRoom(boolean publicRoom) {
		this.publicRoom = publicRoom;
	}
	
	/**
	 * Lokacija na kojoj se nalazi ova prostorija.
	 * @return lokacija
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public Venue getVenue() {
		return venue;
	}
	
	public void setVenue(Venue venue) {
		this.venue = venue;
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
		if (!(obj instanceof Room))
			return false;
		Room other = (Room) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
}
