package hr.fer.zemris.jcms.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Ovo je dogadaj na razini svih korisnika.
 * 
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="GlobalEvent.findForUser",query="select gev from GlobalEvent as gev"),
    @NamedQuery(name="GlobalEvent.findForUser2",query="select gev from GlobalEvent as gev WHERE gev.start >= :fromDate AND gev.start <= :toDate")
})
@Entity
@DiscriminatorValue("E")
public class GlobalEvent extends AbstractEvent {
	
	private static final long serialVersionUID = 1L;

	public GlobalEvent() {
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
		if (!(obj instanceof GlobalEvent))
			return false;
		final GlobalEvent other = (GlobalEvent) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	
}
