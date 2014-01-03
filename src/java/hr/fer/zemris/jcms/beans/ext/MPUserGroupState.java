package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MPOffer;
import hr.fer.zemris.jcms.model.UserGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MPUserGroupState {

	private boolean active;
	private UserGroup myUserGroup;
	private List<MPOffer> myGroupOffersNoAck = new ArrayList<MPOffer>();
	private List<MPOffer> myGroupOffersWithAck = new ArrayList<MPOffer>();
	private List<MPOffer> myDirectOffers = new ArrayList<MPOffer>();
	private List<MPOffer> myAckReqForGroupOffers = new ArrayList<MPOffer>();
	private List<MPOffer> groupOffersNoAckForMe = new ArrayList<MPOffer>();
	private List<MPOffer> groupOffersWithAckForMe = new ArrayList<MPOffer>();
	private List<MPOffer> directOffersForMe = new ArrayList<MPOffer>();
	private List<MPOffer> groupOfferAcksForMe = new ArrayList<MPOffer>();
	private Set<Group> blockedGroups = new HashSet<Group>();
	private Set<Group> directBlockedGroups = new HashSet<Group>();
	private List<Group> availForGroupOffers = new ArrayList<Group>(16);
	private List<Group> availForDirectOffers = new ArrayList<Group>(16);
	private List<Group> availForMove = new ArrayList<Group>(16);
	
	public MPUserGroupState(UserGroup myUserGroup) {
		super();
		this.myUserGroup = myUserGroup;
	}
	
	public UserGroup getMyUserGroup() {
		return myUserGroup;
	}
	public void setMyUserGroup(UserGroup myUserGroup) {
		this.myUserGroup = myUserGroup;
	}
	
	public List<MPOffer> getGroupOfferAcksForMe() {
		return groupOfferAcksForMe;
	}
	public void setGroupOfferAcksForMe(List<MPOffer> groupOfferAcksForMe) {
		this.groupOfferAcksForMe = groupOfferAcksForMe;
	}
	
	public List<MPOffer> getMyGroupOffersNoAck() {
		return myGroupOffersNoAck;
	}
	public void setMyGroupOffersNoAck(List<MPOffer> myGroupOffersNoAck) {
		this.myGroupOffersNoAck = myGroupOffersNoAck;
	}
	public List<MPOffer> getMyGroupOffersWithAck() {
		return myGroupOffersWithAck;
	}
	public void setMyGroupOffersWithAck(List<MPOffer> myGroupOffersWithAck) {
		this.myGroupOffersWithAck = myGroupOffersWithAck;
	}
	public List<MPOffer> getMyDirectOffers() {
		return myDirectOffers;
	}
	public void setMyDirectOffers(List<MPOffer> myDirectOffers) {
		this.myDirectOffers = myDirectOffers;
	}
	public List<MPOffer> getMyAckReqForGroupOffers() {
		return myAckReqForGroupOffers;
	}
	public void setMyAckReqForGroupOffers(List<MPOffer> myAckReqForGroupOffers) {
		this.myAckReqForGroupOffers = myAckReqForGroupOffers;
	}
	public List<MPOffer> getGroupOffersNoAckForMe() {
		return groupOffersNoAckForMe;
	}
	public void setGroupOffersNoAckForMe(List<MPOffer> groupOffersNoAckForMe) {
		this.groupOffersNoAckForMe = groupOffersNoAckForMe;
	}
	public List<MPOffer> getGroupOffersWithAckForMe() {
		return groupOffersWithAckForMe;
	}
	public void setGroupOffersWithAckForMe(List<MPOffer> groupOffersWithAckForMe) {
		this.groupOffersWithAckForMe = groupOffersWithAckForMe;
	}
	public List<MPOffer> getDirectOffersForMe() {
		return directOffersForMe;
	}
	public void setDirectOffersForMe(List<MPOffer> directOffersForMe) {
		this.directOffersForMe = directOffersForMe;
	}
	public Set<Group> getBlockedGroups() {
		return blockedGroups;
	}
	public void setBlockedGroups(Set<Group> blockedGroups) {
		this.blockedGroups = blockedGroups;
	}
	public Set<Group> getDirectBlockedGroups() {
		return directBlockedGroups;
	}
	public void setDirectBlockedGroups(Set<Group> directBlockedGroups) {
		this.directBlockedGroups = directBlockedGroups;
	}
	public List<Group> getAvailForGroupOffers() {
		return availForGroupOffers;
	}
	public void setAvailForGroupOffers(List<Group> availForGroupOffers) {
		this.availForGroupOffers = availForGroupOffers;
	}
	public List<Group> getAvailForDirectOffers() {
		return availForDirectOffers;
	}
	public void setAvailForDirectOffers(List<Group> availForDirectOffers) {
		this.availForDirectOffers = availForDirectOffers;
	}
	public List<Group> getAvailForMove() {
		return availForMove;
	}
	public void setAvailForMove(List<Group> availForMove) {
		this.availForMove = availForMove;
	}
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
