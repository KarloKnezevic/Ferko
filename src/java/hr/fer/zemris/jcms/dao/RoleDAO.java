package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.User;

import java.util.List;

import javax.persistence.EntityManager;

public interface RoleDAO {

	public Role get(EntityManager em, String name);
	public void save(EntityManager em, Role role);
	public void remove(EntityManager em, Role role);
	public List<Role> list(EntityManager em);
	
	/**
	 * Vraca sve korisnike koji imaju zadanu ulogu
	 * 
	 * @param em
	 * @param roleName
	 * @return
	 */
	public List<User> listWithRole(EntityManager em, String roleName);
	/**
	 * Pronalazi sve korisnike sa zadanom ulogom koji zadovoljavaju zadane kriterije.
	 * @param em
	 * @param roleName
	 * @param eqLastName
	 * @param eqFirstName
	 * @param likeJmbag
	 * @return
	 */
	public List<User> listWithRole(EntityManager em, String roleName, String eqLastName, String eqFirstName, String likeJmbag);
	/**
	 * Pronalazi sve korisnike sa zadanom ulogom koji zadovoljavaju zadane kriterije.
	 * @param em
	 * @param roleName
	 * @param eqLastName
	 * @param likeFirstName
	 * @return
	 */
	public List<User> listWithRole(EntityManager em, String roleName, String eqLastName, String likeFirstName);
	/**
	 * Pronalazi sve korisnike sa zadanom ulogom koji zadovoljavaju zadane kriterije.
	 * @param em
	 * @param roleName
	 * @param likeLastName
	 * @return
	 */
	public List<User> listWithRole(EntityManager em, String roleName, String likeLastName);
	
}
