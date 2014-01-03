package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.Room;

import java.util.List;

import javax.persistence.EntityManager;

public interface RoomDAO {
	public Room get(EntityManager em, String id);
	public void remove(EntityManager em, Room room);
	public void save(EntityManager em, Room room);
	public List<Room> list(EntityManager em);
	public Room get(EntityManager em, String venueShortName, String roomShortName);
	public List<Room> listByVenue(EntityManager em, String venueShortName);
}
