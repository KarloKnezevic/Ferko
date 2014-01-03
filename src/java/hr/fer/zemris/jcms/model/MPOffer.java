package hr.fer.zemris.jcms.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@NamedQueries({
    @NamedQuery(name="MPOffer.listForMPUser",query="select mpo from MPOffer as mpo where mpo.marketPlace=:marketPlace and (mpo.fromUser=:user or mpo.toUser=:user)"),
    @NamedQuery(name="MPOffer.listForMPGroup",query="select mpo from MPOffer as mpo where mpo.marketPlace=:marketPlace and mpo.toGroup.id IN (select ug.group.id from UserGroup as ug where ug.group.parent=:parent and ug.user=:user) and mpo.toUser IS NULL and mpo.replyTo IS NULL"),
    @NamedQuery(name="MPOffer.listForMPUser2",query="select mpo from MPOffer as mpo where mpo.marketPlace=:marketPlace and ((mpo.fromUser=:user and mpo.fromGroup=:group) or (mpo.toUser=:user and mpo.toGroup=:group))"),
    @NamedQuery(name="MPOffer.listForMPGroup2",query="select mpo from MPOffer as mpo where mpo.marketPlace=:marketPlace and mpo.toGroup=:group and mpo.toUser IS NULL and mpo.replyTo IS NULL"),
    @NamedQuery(name="MPOffer.deletePhase1",query="delete from MPOffer where marketPlace=:marketPlace and ((toUser=:user and toGroup=:group) or (fromUser=:user and fromGroup=:group)) and replyTo IS NOT NULL"),
    @NamedQuery(name="MPOffer.deletePhase2",query="delete from MPOffer where marketPlace=:marketPlace and ((toUser=:user and toGroup=:group) or (fromUser=:user and fromGroup=:group)) and replyTo IS NULL"),
    @NamedQuery(name="MPOffer.deleteGPhase1",query="delete from MPOffer where marketPlace=:marketPlace and (toGroup=:group or fromGroup=:group) and replyTo IS NOT NULL"),
    @NamedQuery(name="MPOffer.deleteGPhase2",query="delete from MPOffer where marketPlace=:marketPlace and (toGroup=:group or fromGroup=:group) and replyTo IS NULL"),
    @NamedQuery(name="MPOffer.deleteReplysTo",query="delete from MPOffer where marketPlace=:marketPlace and replyTo=:offer")
})
@Entity
@Table(name="mpoffers",uniqueConstraints={
		@UniqueConstraint(columnNames={"marketPlace_id","fromUser_id","fromGroup_id","toUser_id","toGroup_id"})
})
public class MPOffer {

	private Long id;
	private MarketPlace marketPlace;
	private User fromUser;
	private Group fromGroup;
	private User toUser;
	private Group toGroup;
	private MPOffer replyTo;
	private boolean needsAck;
	private Date validUntil;
	private String reason;
	private boolean rejected;
	private boolean expired = false;
	private String fromTag;
	
	public MPOffer() {
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(length=10,nullable=true)
	public String getFromTag() {
		return fromTag;
	}
	public void setFromTag(String fromTag) {
		this.fromTag = fromTag;
	}

	public boolean getRejected() {
		return rejected;
	}
	public void setRejected(boolean rejected) {
		this.rejected = rejected;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public MarketPlace getMarketPlace() {
		return marketPlace;
	}
	public void setMarketPlace(MarketPlace marketPlace) {
		this.marketPlace = marketPlace;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public User getFromUser() {
		return fromUser;
	}
	public void setFromUser(User fromUser) {
		this.fromUser = fromUser;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public Group getFromGroup() {
		return fromGroup;
	}
	public void setFromGroup(Group fromGroup) {
		this.fromGroup = fromGroup;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	public User getToUser() {
		return toUser;
	}
	public void setToUser(User toUser) {
		this.toUser = toUser;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public Group getToGroup() {
		return toGroup;
	}
	public void setToGroup(Group toGroup) {
		this.toGroup = toGroup;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	public MPOffer getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(MPOffer replyTo) {
		this.replyTo = replyTo;
	}

	public boolean getNeedsAck() {
		return needsAck;
	}
	public void setNeedsAck(boolean needsAck) {
		this.needsAck = needsAck;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getValidUntil() {
		return validUntil;
	}
	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	@Column(length=100)
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getFromGroup() == null) ? 0 : getFromGroup().hashCode());
		result = prime * result
				+ ((getFromUser() == null) ? 0 : getFromUser().hashCode());
		result = prime * result
				+ ((getMarketPlace() == null) ? 0 : getMarketPlace().hashCode());
		result = prime * result + ((getToGroup() == null) ? 0 : getToGroup().hashCode());
		result = prime * result + ((getToUser() == null) ? 0 : getToUser().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MPOffer))
			return false;
		MPOffer other = (MPOffer) obj;
		if (getFromGroup() == null) {
			if (other.getFromGroup() != null)
				return false;
		} else if (!getFromGroup().equals(other.getFromGroup()))
			return false;
		if (getFromUser() == null) {
			if (other.getFromUser() != null)
				return false;
		} else if (!getFromUser().equals(other.getFromUser()))
			return false;
		if (getMarketPlace() == null) {
			if (other.getMarketPlace() != null)
				return false;
		} else if (!getMarketPlace().equals(other.getMarketPlace()))
			return false;
		if (getToGroup() == null) {
			if (other.getToGroup() != null)
				return false;
		} else if (!getToGroup().equals(other.getToGroup()))
			return false;
		if (getToUser() == null) {
			if (other.getToUser() != null)
				return false;
		} else if (!getToUser().equals(other.getToUser()))
			return false;
		return true;
	}
	
	@Transient
	public boolean getExpired() {
		return expired;
	}
	public void setExpired(boolean expired) {
		this.expired = expired;
	}
}
