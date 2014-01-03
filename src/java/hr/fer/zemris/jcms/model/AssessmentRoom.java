package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.model.extra.AssessmentRoomStatus;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomTag;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name="assessment_rooms", uniqueConstraints={
	@UniqueConstraint(columnNames={"assessment_id","room_id"})
})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AssessmentRoom implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Assessment assessment;
	private Room room;
	private int capacity;
	private int requiredAssistants;
	private boolean available = true; // po defaultu, prostorija je dostupna
	private AssessmentRoomTag roomTag = AssessmentRoomTag.MANDATORY;
	private boolean taken = false;
	private boolean reserved = false;
	private AssessmentRoomStatus roomStatus = AssessmentRoomStatus.UNCHECKED;
	private String reservationID;
	private UserSpecificEvent userEvent;
	private Group group;
	
	public AssessmentRoom() {
	}
	
	public AssessmentRoom(Long id) {
		this.id = id;
	}

	/**
	 * Identifikator.
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

	/**
	 * Provjera.
	 * 
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
	 * Prostorija.
	 * @return prostorija
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	/**
	 * Broj studenata koji se mogu smjestiti u ovu prostoriju.
	 * @return broj studenata
	 */
	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Broj asistenata koji su potrebni kako bi se ova dvorana čuvala.
	 * @return potreban proj asistenata
	 */
	public int getRequiredAssistants() {
		return requiredAssistants;
	}
	
	public void setRequiredAssistants(int requiredAssistants) {
		this.requiredAssistants = requiredAssistants;
	}
	
	/**
	 * Je li dvorana dostupna? Ovu zastavicu će postavljati provjerivač (primjerice,
	 * upitom u FERWeb).
	 * @return true ako je dvorana slobodna; false inače
	 */
	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	/**
	 * Je li dvorana rezervirana putem FERWeba
	 * @return
	 */
	public boolean isReserved() {
		return reserved;
	}

	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	/**
	 * Id rezervacije ukoliko je dvorana rezervirana putem FERWeba
	 * @return
	 */
	@Column(nullable=true)
	public String getReservationID() {
		return reservationID;
	}

	public void setReservationID(String reservationID) {
		this.reservationID = reservationID;
	}
	
	/**
	 * Oznaka poželjnosti dvorane.
	 * @return poželjnost dvorane
	 */
	@Enumerated(EnumType.ORDINAL)
	public AssessmentRoomTag getRoomTag() {
		return roomTag;
	}

	public void setRoomTag(AssessmentRoomTag roomTag) {
		this.roomTag = roomTag;
	}

	/**
	 * Podatak o statusu rezervacija dvorane.
	 * @return status dvorane
	 */
	@Enumerated(EnumType.ORDINAL)
	public AssessmentRoomStatus getRoomStatus() {
		return roomStatus;
	}
	
	public void setRoomStatus(AssessmentRoomStatus roomStatus) {
		this.roomStatus = roomStatus;
	}
	
	/**
	 * Je li dvorana odabrana za ovu provjeru znanja.
	 * @return true ako je; false inače
	 */
	public boolean isTaken() {
		return taken;
	}

	public void setTaken(boolean taken) {
		this.taken = taken;
	}

	/**
	 * Događaj kojim će se asistenti obavijestiti da čuvaju provjeru u ovoj
	 * dvorani.
	 * 
	 * @return događaj za asistente
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=true)
	public UserSpecificEvent getUserEvent() {
		return userEvent;
	}

	public void setUserEvent(UserSpecificEvent userEvent) {
		this.userEvent = userEvent;
	}

	/**
	 * Grupa koja čuva studente koji idu u ovu prostoriju. Sustav treba pripaziti
	 * da grupe (podgrupe od grupe u provjeri) sinkronizira s ovim. Poseban je problem
	 * što grupe ne vode redne brojeve korisnika, pa se pamćenje poretka vodi u dodatnoj
	 * kolekciji (što je možda glupo; kako ovo riješiti pametnije?). Grupa kasnije ima
	 * vezu prema grupnom eventu pa na taj način dalje slijedi objava evenata.
	 *  
	 * @return grupa
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=true)
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getAssessment() == null) ? 0 : getAssessment().hashCode());
		result = prime * result + ((getRoom() == null) ? 0 : getRoom().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AssessmentRoom))
			return false;
		AssessmentRoom other = (AssessmentRoom) obj;
		if (getAssessment() == null) {
			if (other.getAssessment() != null)
				return false;
		} else if (!getAssessment().equals(other.getAssessment()))
			return false;
		if (getRoom() == null) {
			if (other.getRoom() != null)
				return false;
		} else if (!getRoom().equals(other.getRoom()))
			return false;
		return true;
	}
	
}
