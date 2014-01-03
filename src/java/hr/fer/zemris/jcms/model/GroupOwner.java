package hr.fer.zemris.jcms.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@NamedQueries({
    @NamedQuery(name="GroupOwner.findForSubgroupsLLE",query="select go from GroupOwner as go join go.group g where g.compositeCourseID LIKE :compositeCourseID AND (g.relativePath LIKE :likeRelativePath OR g.relativePath=:eqRelativePath)"),
    @NamedQuery(name="GroupOwner.findForSubgroupsELE",query="select go from GroupOwner as go join go.group g where g.compositeCourseID=:compositeCourseID AND (g.relativePath LIKE :likeRelativePath OR g.relativePath=:eqRelativePath)"),
    @NamedQuery(name="GroupOwner.findForSubgroups",query="select go from GroupOwner as go join go.group g where g.compositeCourseID=:compositeCourseID AND g.relativePath LIKE :relativePath"),
    @NamedQuery(name="GroupOwner.findForSubgroupsAndUser",query="select go from GroupOwner as go join go.group g where g.compositeCourseID=:compositeCourseID AND g.relativePath LIKE :relativePath and go.user=:user"),
    @NamedQuery(name="GroupOwner.findAllCourseGOs",query="select go from GroupOwner as go where go.group.compositeCourseID=:compositeCourseID and go.user=:user"),
    @NamedQuery(name="GroupOwner.findAllCourseGroups",query="select go.group from GroupOwner as go where go.group.compositeCourseID=:compositeCourseID and go.user=:user"),
    @NamedQuery(name="GroupOwner.findForGroup",query="select go from GroupOwner as go where go.group=:group"),
    @NamedQuery(name="GroupOwner.getGroupOwner",query="select go from GroupOwner as go where go.group=:group AND go.user=:user")
})
@Entity
@Table(name="group_owners",uniqueConstraints=@UniqueConstraint(columnNames={"group_id","user_id"}))
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class GroupOwner {
	private Long id;
	private User user;
	private Group group;
	
	public GroupOwner() {
	}

	public GroupOwner(Group group, User user) {
		super();
		this.group = group;
		this.user = user;
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
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
		if (!(obj instanceof GroupOwner))
			return false;
		GroupOwner other = (GroupOwner) obj;
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
