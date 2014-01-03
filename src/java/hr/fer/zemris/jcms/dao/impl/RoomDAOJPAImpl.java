package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.RoomDAO;
import hr.fer.zemris.jcms.model.Room;

import java.util.List;

import javax.persistence.EntityManager;

public class RoomDAOJPAImpl implements RoomDAO {

	@Override
	public Room get(EntityManager em, String id) {
		return em.find(Room.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Room> list(EntityManager em) {
		return (List<Room>)em.createNamedQuery("Room.list").getResultList();
	}

	@Override
	public void remove(EntityManager em, Room room) {
		em.remove(room);
	}

	@Override
	public void save(EntityManager em, Room room) {
		em.persist(room);
	}

	@Override
	public Room get(EntityManager em, String venueShortName,
			String roomShortName) {
		return em.find(Room.class, venueShortName+"/"+roomShortName);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Room> listByVenue(EntityManager em, String venueShortName) {
		return (List<Room>)em.createNamedQuery("Room.listByVenue").setParameter("venueShortName", venueShortName).getResultList();
	}
}
