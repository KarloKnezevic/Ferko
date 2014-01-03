package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MPOffer;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MPUserState {

	private List<MPOffer> allOffers;
	private Map<Long,MPUserGroupState> mapByUsersGroups = new HashMap<Long, MPUserGroupState>();
	private List<MPUserGroupState> allStates;
	
	public List<MPOffer> getAllOffers() {
		return allOffers;
	}
	public void setAllOffers(List<MPOffer> allOffers) {
		this.allOffers = allOffers;
	}
	public Map<Long, MPUserGroupState> getMapByUsersGroups() {
		return mapByUsersGroups;
	}
	public void setMapByUsersGroups(Map<Long, MPUserGroupState> mapByUsersGroups) {
		this.mapByUsersGroups = mapByUsersGroups;
	}
	
	public MPUserGroupState getForGroup(Group group) {
		return mapByUsersGroups.get(group.getId());
	}
	
	public MPUserGroupState getForGroup(Long groupID) {
		return mapByUsersGroups.get(groupID);
	}
	
	public MPUserGroupState getOrCreateForGroup(UserGroup userGroup) {
		MPUserGroupState s = mapByUsersGroups.get(userGroup.getGroup().getId());
		if(s==null) {
			s = new MPUserGroupState(userGroup);
			mapByUsersGroups.put(userGroup.getGroup().getId(), s);
			if(allStates==null) allStates = new ArrayList<MPUserGroupState>();
			allStates.add(s);
		}
		return s;
	}

	public List<MPUserGroupState> getAllStates() {
		if(allStates==null || allStates.size()!=mapByUsersGroups.size()) {
			allStates = new ArrayList<MPUserGroupState>(mapByUsersGroups.values());
		}
		return allStates;
	}
	
	public static MPUserState buildFrom(List<MPOffer> allOffers, User user, Date now, MPSecurityConstraints scons, Map<Group, UserGroup> myUserGroupMap) {
		MPUserState st = new MPUserState();
		for(MPOffer offer : allOffers) {
			if(offer.getFromUser().equals(user)) {
				// Ako je ovo ponuda od mene, idemo vidjeti o čemu se radi:
				MPUserGroupState gs = st.getOrCreateForGroup(myUserGroupMap.get(offer.getFromGroup()));
				if(offer.getReplyTo()==null) {
					offer.setExpired(isOfferExpired(offer.getValidUntil(), now));
					// Ovo nije odgovor, vec prava ponuda... Ajmo vidjeti kome:
					if(offer.getToUser()!=null) {
						// To je direktna ponuda korisniku...
						gs.getMyDirectOffers().add(offer);
					} else {
						// To je moja grupna ponuda nekoj grupi:
						// Kakva god da je, ta mi je grupa dalje blokirana za slanje novih ponuda...
						gs.getBlockedGroups().add(offer.getToGroup());
						// Da vidimo sada tocno kakva je to grupna ponuda:
						if(offer.getNeedsAck()) {
							// Stovise, to je moja ponuda koja trazi moju potvrdu:
							gs.getMyGroupOffersWithAck().add(offer);
						} else {
							// To je moja grupna ponuda koja ne trazi moju potvrdu:
							gs.getMyGroupOffersNoAck().add(offer);
						}
					}
				} else {
					// To je moj odgovor na ponudu nekog korisnika...
					offer.setExpired(isOfferExpired(offer.getReplyTo().getValidUntil(), now) || isOfferExpired(offer.getValidUntil(), now));
					gs.getMyAckReqForGroupOffers().add(offer);
				}
			} else {
				// Ovo je od nekog drugog korisnika, pa mora biti ZA mene:
				MPUserGroupState gs = st.getOrCreateForGroup(myUserGroupMap.get(offer.getToGroup()));
				if(offer.getReplyTo()==null) {
					// Ako se ovo ne može provesti (jer npr. "sam ja student a ponuda je od demosa"), ignoriraj:
					if(scons!=null && !scons.canExchange(offer.getFromGroup().getMpSecurityTag(), offer.getFromTag(), offer.getToGroup().getMpSecurityTag(), myUserGroupMap.get(offer.getToGroup()).getTag())) {
						continue;
					}
					// Ako to nije odgovor, onda je ili ponuda za mene, ili za moju grupu:
					// Pogledajmo je li to isteklo, i ako je, preskocimo obradu...
					offer.setExpired(isOfferExpired(offer.getValidUntil(), now));
					if(offer.getExpired()) continue;
					// Ako nije isteklo, pogledajmo o cemu se radi:
					if(offer.getToUser()!=null) {
						// To je ponuda bas za mene:
						gs.getDirectOffersForMe().add(offer);
					} else {
						// Inace je ponuda za moju grupu
						// Izvorisnu grupu dodat cemo u blokirane grupe samo ako se ne trazi potvrda
						// jer to znaci da se ta zamjena moze odmah prihvatiti...
						if(offer.getNeedsAck()) {
							// To je ponuda za moju grupu koja trazi potvrdu
							gs.getGroupOffersWithAckForMe().add(offer);
						} else {
							// To je ponuda za moju grupu koja ne trazi potvrdu
							gs.getGroupOffersNoAckForMe().add(offer);
							gs.getBlockedGroups().add(offer.getFromGroup());
						}
					}
				} else {
					// To je odgovor na neku moju ponudu...
					offer.setExpired(isOfferExpired(offer.getReplyTo().getValidUntil(), now) || isOfferExpired(offer.getValidUntil(), now));
					gs.getGroupOfferAcksForMe().add(offer);
				}
			}
		}
		return st;
	}
	
	public static boolean isOfferExpired(Date mpOfferValidUntil, Date now) {
		if(mpOfferValidUntil==null) return false;
		return mpOfferValidUntil.before(now);
	}
}
