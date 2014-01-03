package hr.fer.zemris.jcms.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Direktno zakazivanje sastanaka odredenim studentima. Vidi raspravu za
 * jedinstvenost događaja kod {@linkplain GroupWideEvent}. Ono što je ovdje
 * jedinstveno je start + place + issuer.
 *  
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="UserSpecificEvent.findForUser",query="select usev from UserSpecificEvent as usev WHERE :user MEMBER OF usev.users"),
    @NamedQuery(name="UserSpecificEvent.findForUser2",query="select usev from UserSpecificEvent as usev WHERE :user MEMBER OF usev.users AND usev.start >= :fromDate AND usev.start <= :toDate")
})
@Entity
@DiscriminatorValue("U")
public class UserSpecificEvent extends AbstractEvent {
	
	private static final long serialVersionUID = 1L;

	private Set<User> users = new HashSet<User>();
	
	public UserSpecificEvent() {
	}
	
	/**
	 * Kojim je korisnicima ovaj dogadaj zakazan?
	 * 
	 * @return
	 */
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name="users_to_events",
			joinColumns=@JoinColumn(name="event_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="user_id",referencedColumnName="id")
	)
	public Set<User> getUsers() {
		return users;
	}
	public void setUsers(Set<User> users) {
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
