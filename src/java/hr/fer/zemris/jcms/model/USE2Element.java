package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="USE2_elements",uniqueConstraints=@UniqueConstraint(columnNames={"eventUser_id","event_id"}))
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="dtype",discriminatorType=DiscriminatorType.CHAR)
@DiscriminatorValue("*")
public class USE2Element implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private User eventUser;
	private UserSpecificEvent2 event;
	boolean visible = true;
	
	public USE2Element() {
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getEventUser() {
		return eventUser;
	}

	public void setEventUser(User user) {
		this.eventUser = user;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public UserSpecificEvent2 getEvent() {
		return event;
	}

	public void setEvent(UserSpecificEvent2 event) {
		this.event = event;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getEvent() == null) ? 0 : getEvent().hashCode());
		result = prime * result + ((getEventUser() == null) ? 0 : getEventUser().hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof USE2Element))
			return false;
		final USE2Element other = (USE2Element) obj;
		if (getEvent() == null) {
			if (other.getEvent() != null)
				return false;
		} else if (!getEvent().equals(other.getEvent()))
			return false;
		if (getEventUser() == null) {
			if (other.getEventUser() != null)
				return false;
		} else if (!getEventUser().equals(other.getEventUser()))
			return false;
		return true;
	}
	
	
}
