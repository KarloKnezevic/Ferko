package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.Venue;

import java.util.List;

import javax.persistence.EntityManager;

public interface VenueDAO {
	public Venue get(EntityManager em, String shortName);
	public void remove(EntityManager em, Venue venue);
	public void save(EntityManager em, Venue venue);
	public List<Venue> list(EntityManager em);
}
