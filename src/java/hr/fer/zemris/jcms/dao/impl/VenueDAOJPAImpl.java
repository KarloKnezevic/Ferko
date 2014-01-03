package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.VenueDAO;
import hr.fer.zemris.jcms.model.Venue;

import java.util.List;

import javax.persistence.EntityManager;

public class VenueDAOJPAImpl implements VenueDAO {

	@Override
	public Venue get(EntityManager em, String shortName) {
		return em.find(Venue.class, shortName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Venue> list(EntityManager em) {
		return (List<Venue>)em.createNamedQuery("Venue.list").getResultList();
	}

	@Override
	public void remove(EntityManager em, Venue venue) {
		em.remove(venue);
	}

	@Override
	public void save(EntityManager em, Venue venue) {
		em.persist(venue);
	}

}
