package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.MarketPlaceDAO;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MPOffer;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.User;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

public class MarketPlaceDAOJPAImpl implements MarketPlaceDAO {

	@Override
	public MPOffer getMPOffer(EntityManager em, Long id) {
		return em.find(MPOffer.class, id);
	}

	@Override
	public MarketPlace getMarketPlace(EntityManager em, Long id) {
		return em.find(MarketPlace.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MPOffer> listOffersRegardingUser(EntityManager em,
			MarketPlace marketPlace, User user) {
		List<MPOffer> l1 = (List<MPOffer>)em.createNamedQuery("MPOffer.listForMPUser")
				.setParameter("marketPlace", marketPlace)
				.setParameter("user", user)
				.getResultList();
		List<MPOffer> l2 = (List<MPOffer>)em.createNamedQuery("MPOffer.listForMPGroup")
				.setParameter("marketPlace", marketPlace)
				.setParameter("user", user)
				.setParameter("parent", marketPlace.getGroup())
				.getResultList();
		List<MPOffer> allOffers = new ArrayList<MPOffer>(l1.size() + l2.size());
		allOffers.addAll(l1);
		allOffers.addAll(l2);
		return allOffers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MPOffer> listOffersRegardingUser(EntityManager em,
			MarketPlace marketPlace, User user, Group group) {
		List<MPOffer> l1 = (List<MPOffer>)em.createNamedQuery("MPOffer.listForMPUser2")
				.setParameter("marketPlace", marketPlace)
				.setParameter("user", user)
				.setParameter("group", group)
				.getResultList();
		List<MPOffer> l2 = (List<MPOffer>)em.createNamedQuery("MPOffer.listForMPGroup2")
				.setParameter("marketPlace", marketPlace)
				.setParameter("group", group)
				.getResultList();
		List<MPOffer> allOffers = new ArrayList<MPOffer>(l1.size() + l2.size());
		allOffers.addAll(l1);
		allOffers.addAll(l2);
		return allOffers;
	}
	
	@Override
	public void remove(EntityManager em, MarketPlace marketPlace) {
		em.remove(marketPlace);
	}

	@Override
	public void remove(EntityManager em, MPOffer offer) {
		em.remove(offer);
	}

	@Override
	public void save(EntityManager em, MarketPlace marketPlace) {
		em.persist(marketPlace);
	}

	@Override
	public void save(EntityManager em, MPOffer offer) {
		em.persist(offer);
	}

	@Override
	public void clearAllOffersForUser(EntityManager em, MarketPlace marketPlace, User user, Group group) {
		if(marketPlace==null) return;
		em.createNamedQuery("MPOffer.deletePhase1")
		.setParameter("marketPlace", marketPlace)
		.setParameter("user", user)
		.setParameter("group", group)
		.executeUpdate();
		em.flush();
		em.createNamedQuery("MPOffer.deletePhase2")
		.setParameter("marketPlace", marketPlace)
		.setParameter("user", user)
		.setParameter("group", group)
		.executeUpdate();
		em.flush();
	}

	@Override
	public void clearAllOffersInvolvingGroup(EntityManager em, MarketPlace marketPlace, Group group) {
		if(marketPlace==null) return;
		em.createNamedQuery("MPOffer.deleteGPhase1")
		.setParameter("marketPlace", marketPlace)
		.setParameter("group", group)
		.executeUpdate();
		em.flush();
		em.createNamedQuery("MPOffer.deleteGPhase2")
		.setParameter("marketPlace", marketPlace)
		.setParameter("group", group)
		.executeUpdate();
		em.flush();
	}

	@Override
	public void clearAllOffersForUsers(EntityManager em, MarketPlace marketPlace, User user1, Group group1, User user2, Group group2) {
		if(marketPlace==null) return;
		em.createNamedQuery("MPOffer.deletePhase1")
		.setParameter("marketPlace", marketPlace)
		.setParameter("user", user1)
		.setParameter("group", group1)
		.executeUpdate();
		em.createNamedQuery("MPOffer.deletePhase1")
		.setParameter("marketPlace", marketPlace)
		.setParameter("user", user2)
		.setParameter("group", group2)
		.executeUpdate();
		em.flush();
		em.createNamedQuery("MPOffer.deletePhase2")
		.setParameter("marketPlace", marketPlace)
		.setParameter("user", user1)
		.setParameter("group", group1)
		.executeUpdate();
		em.createNamedQuery("MPOffer.deletePhase2")
		.setParameter("marketPlace", marketPlace)
		.setParameter("user", user2)
		.setParameter("group", group2)
		.executeUpdate();
		em.flush();
	}

	@Override
	public void deleteReplysTo(EntityManager em, MarketPlace marketPlace, MPOffer offer) {
		if(marketPlace==null) return;
		em.createNamedQuery("MPOffer.deleteReplysTo")
		.setParameter("marketPlace", marketPlace)
		.setParameter("offer", offer)
		.executeUpdate();
		em.flush();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MPOffer> listOffers(EntityManager em, String likeCompositeCourseID) {
		List<MPOffer> l1 = (List<MPOffer>)em.createNamedQuery("MarketPlace.listOffers")
		.setParameter("compositeCourseID", likeCompositeCourseID)
		.getResultList();
		return l1;
	}
}
