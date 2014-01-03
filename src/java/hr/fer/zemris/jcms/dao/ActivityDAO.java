package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.Activity;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

public interface ActivityDAO {

	public Activity get(EntityManager em, Long id);
	public void save(EntityManager em, Activity activity);
	public void remove(EntityManager em, Activity activity);

	/**
	 * Dohvaća zadnjih N nearhiviranih aktivnosti za korisnika.
	 * 
	 * @param em entity manager
	 * @param since od kojeg datuma na dalje
	 * @param user korisnik
	 * @param n koliko aktivnosti
	 * @return listu aktivnosti
	 */
	public List<Activity> listLastNForUser(EntityManager em, Date since, User user, int n);
	/**
	 * Dohvaća zadnjih N nearhiviranih aktivnosti za korisnika i zadani kolegij.
	 * 
	 * @param em entity manager
	 * @param since od kojeg datuma na dalje
	 * @param user korisnik
	 * @param ci primjerak kolegija
	 * @param n koliko aktivnosti
	 * @return listu aktivnosti
	 */
	public List<Activity> listLastNForUserAndCourse(EntityManager em, Date since, User user, CourseInstance ci, int n);
	/**
	 * Dohvaća sve aktivnosti za korisnika.
	 * 
	 * @param em entity manager
	 * @param since od kojeg datuma na dalje
	 * @param user korisnik
	 * @return listu aktivnosti
	 */
	public List<Activity> listForUser(EntityManager em, Date since, User user);
	/**
	 * Dohvaća sve aktivnosti za korisnika i kolegij.
	 * 
	 * @param em entity manager
	 * @param since od kojeg datuma na dalje
	 * @param user korisnik
	 * @param ci primjerak kolegija
	 * @return listu aktivnosti
	 */
	public List<Activity> listForUserAndCourse(EntityManager em, Date since, User user, CourseInstance ci);
}
