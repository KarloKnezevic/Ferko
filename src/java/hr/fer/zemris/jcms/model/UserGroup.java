package hr.fer.zemris.jcms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@NamedQueries({
    @NamedQuery(name="UserGroup.findForUserGroup",query="select ug from UserGroup as ug where ug.group=:group and ug.user=:user"),
    @NamedQuery(name="UserGroup.findForSemester",query="select distinct u.user from Group as g JOIN g.users as u where g.compositeCourseID LIKE :compositeCourseID AND relativePath LIKE '0/%'"),
    @NamedQuery(name="UserGroup.findAllLectureUGForSemester",query="select u from UserGroup as u, Group as g where u.group=g and g.compositeCourseID LIKE :compositeCourseID AND g.relativePath LIKE '0/%'"),
    @NamedQuery(name="UserGroup.findForGroupAndSubGroups",query="select u from UserGroup as u where u.group.compositeCourseID=:compositeCourseID AND (u.group.relativePath LIKE :likeRelativePath OR u.group.relativePath=:eqRelativePath)"),
    @NamedQuery(name="UserGroup.find",query="select u from UserGroup as u join fetch u.user usr where u.group.compositeCourseID LIKE :compositeCourseID AND u.group.relativePath LIKE :likeRelativePath"),
    @NamedQuery(name="UserGroup.search",query="SELECT u FROM UserGroup AS u JOIN FETCH u.user usr WHERE usr.username LIKE :term OR usr.firstName LIKE :term OR usr.lastName LIKE :term"),
    @NamedQuery(name="UserGroup.getUserNumber",query="select count(u) from UserGroup as u where u.group.compositeCourseID=:compositeCourseID AND (u.group.relativePath LIKE :likeRelativePath OR u.group.relativePath=:eqRelativePath)")
})
@Entity
@Table(name="user_groups",uniqueConstraints=@UniqueConstraint(columnNames={"group_id","user_id"}))
public class UserGroup {
	private Long id;
	private Group group;
	private User user;
	private int position;
	private String tag;
	private long version;
	
	public UserGroup() {
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
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Polje tag može se koristiti kako bi se unutar grupe
	 * napravila distinkcija između različitih korisnika
	 * (tipa: DEMOS i slično).  
	 * Ne smije sadržavati znakove: '?', '#', ',', '/', ':'.
	 * 
	 * @return
	 */
	@Column(length=10,nullable=true)
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@Version
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getGroup() == null) ? 0 : getGroup().hashCode());
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserGroup))
			return false;
		UserGroup other = (UserGroup) obj;
		if (getGroup() == null) {
			if (other.getGroup() != null)
				return false;
		} else if (!getGroup().equals(other.getGroup()))
			return false;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}
}
