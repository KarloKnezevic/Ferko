package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MPOffer;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.User;

import java.util.List;

import javax.persistence.EntityManager;

public interface MarketPlaceDAO {
	public MarketPlace getMarketPlace(EntityManager em, Long id);
	public MPOffer getMPOffer(EntityManager em, Long id);
	public void save(EntityManager em, MarketPlace marketPlace);
	public void save(EntityManager em, MPOffer offer);
	public void remove(EntityManager em, MarketPlace marketPlace);
	public void remove(EntityManager em, MPOffer offer);
	public List<MPOffer> listOffersRegardingUser(EntityManager em, MarketPlace marketPlace, User user);
	public List<MPOffer> listOffersRegardingUser(EntityManager em, MarketPlace marketPlace, User user, Group group);
	public void clearAllOffersForUser(EntityManager em, MarketPlace marketPlace, User user, Group group);
	public void clearAllOffersInvolvingGroup(EntityManager em, MarketPlace marketPlace, Group group);
	public void clearAllOffersForUsers(EntityManager em, MarketPlace marketPlace, User user1, Group group1, User user2, Group group2);
	public void deleteReplysTo(EntityManager em, MarketPlace marketPlace, MPOffer offer);
	public List<MPOffer> listOffers(EntityManager em, String likeCompositeCourseID);
}
