package hr.fer.zemris.jcms.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("S")
public class UserSpecificEvent2 extends AbstractEvent {
	
	private static final long serialVersionUID = 1L;
	
	private Set<USE2Element> users = new HashSet<USE2Element>();
	
	public UserSpecificEvent2() {
	}

	@OneToMany(fetch=FetchType.LAZY,mappedBy="event",cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	public Set<USE2Element> getUsers() {
		return users;
	}
	public void setUsers(Set<USE2Element> users) {
		this.users = users;
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
		if (!(obj instanceof UserSpecificEvent))
			return false;
		final UserSpecificEvent other = (UserSpecificEvent) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
