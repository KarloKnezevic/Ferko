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
 * Dogadaj na razini jedne ili vise grupa. Primjerice, predavanja iz Umjetne inteligencije
 * datuma tog-i-tog u toliko i toliko sati u toj-i-toj dvorani. Predavanje moze prema
 * rasporedu slusati vise grupa. Zato je ovdje kolekcija. Kada se dodaje nova grupa u neki
 * dogadaj, kako znati smije li se postojeci reuse-ati ili treba raditi novi? Primjerice,
 * ako nisam dovoljno specifican, ja mogu grupi studenata zakazati ispit na ZEMRIS-u,
 * i netko drugi moze nekoj drugoj grupi (a kasnije cemo to obaviti u nekoj u tom trenutku
 * slobodnoj zavodskoj prostoriji) - ja i taj netko ne smijemo dijeliti isti dogadaj iako je
 * on istovremeno na istom "mjestu", jer ako ja kasnije hoci to pomaknuti, moram to moci
 * napraviti bez da pomicem i njega. Stoga ono sto bi trebalo biti jedinstveno i po cemu cu
 * prepoznati svoju grupu je: courseInstance + start + place (valjda, nemam pojma je li ovo dobro).
 * 
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="GroupWideEvent.findAllSemLecture",query="select gwe from GroupWideEvent as gwe where gwe.specifier LIKE :specifier"),
    @NamedQuery(name="GroupWideEvent.findForUser",query="select distinct gwe from GroupWideEvent as gwe, IN(gwe.groups) ggg WHERE gwe.hidden=false AND (ggg.id in (select ug2.group.id from UserGroup as ug2 where :user=ug2.user) or ggg.id in (select go2.group.id from GroupOwner as go2 where :user=go2.user))"),
    @NamedQuery(name="GroupWideEvent.findForUser2",query="select distinct gwe from GroupWideEvent as gwe, IN(gwe.groups) ggg WHERE (ggg.id in (select ug2.group.id from UserGroup as ug2 where gwe.hidden=false AND :user=ug2.user) or ggg.id in (select go2.group.id from GroupOwner as go2 where :user=go2.user)) AND gwe.start >= :fromDate AND gwe.start <= :toDate"),
    @NamedQuery(name="GroupWideEvent.findForCourseInstance",query="select distinct gwe from GroupWideEvent as gwe, IN(gwe.groups) ggg WHERE gwe.hidden=false AND ggg.compositeCourseID=:courseInstanceID"),
    @NamedQuery(name="GroupWideEvent.findForCourseInstance2",query="select distinct gwe from GroupWideEvent as gwe, IN(gwe.groups) ggg WHERE gwe.hidden=false AND ggg.compositeCourseID=:courseInstanceID AND gwe.start >= :fromDate AND gwe.start <= :toDate")
})
@Entity
@DiscriminatorValue("G")
public class GroupWideEvent extends AbstractEvent {

	private static final long serialVersionUID = 1L;

	private Set<Group> groups = new HashSet<Group>();
	
	public GroupWideEvent() {
	}

	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name="group_to_events",
			joinColumns=@JoinColumn(name="event_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="group_id",referencedColumnName="id")
	)
	public Set<Group> getGroups() {
		return groups;
	}
	public void setGroups(Set<Group> groups) {
		this.groups = groups;
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
		if (!(obj instanceof GroupWideEvent))
			return false;
		final GroupWideEvent other = (GroupWideEvent) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	
}
