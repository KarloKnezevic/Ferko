package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseInstanceIsvuData;
import hr.fer.zemris.jcms.model.Grade;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.CourseInstanceWithGroup;

import java.util.List;

import javax.persistence.EntityManager;

public interface CourseInstanceDAO {

	public CourseInstance get(EntityManager em, String id);
	public void save(EntityManager em, CourseInstance ci);
	public void save(EntityManager em, CourseInstanceIsvuData isvuData);
	public void remove(EntityManager em, CourseInstance ci);
	/**
	 * Returns a list of course instances in given semester.
	 * @param em
	 * @param yearSemesterID
	 * @return
	 */
	public List<CourseInstance> findForSemester(EntityManager em, String yearSemesterID);
	/**
	 * Vraca listu kolegija koju u predanom semestru slusa korisnik.
	 * @param em
	 * @param yearSemesterID
	 * @param user
	 * @return
	 */
	public List<CourseInstanceWithGroup> findForUserAndSemester(EntityManager em, String yearSemesterID, User user);
	/**
	 * Provjerava je li navedeni korisnik clan "osoblja" kolegija (dakle, asistent, nastavnik, ...)
	 * @param em
	 * @param courseInstance
	 * @param user
	 * @return
	 */
	public boolean isCourseStaffMember(EntityManager em, CourseInstance courseInstance, User user);
	/**
	 * Pronalazi sve kolegije u zadanom semestru na kojima je korisnik član osoblja.
	 * @param em
	 * @param yearSemester
	 * @param user
	 * @return
	 */
	public List<CourseInstance> findForCourseStaff(EntityManager em, YearSemester yearSemester, User user);
	/**
	 * Pronalazi sve korisnike koji su na kolegiju (koji su u grupi za predavanja)
	 * @param em
	 * @param courseCompositeID
	 * @return
	 */
	public List<User> findCourseUsers(EntityManager em, String courseCompositeID);
	
	/**
	 * @return Zadnji primjerak kolegija.
	 */
	public CourseInstance findLastForCourse(EntityManager em, Course course);
	
	/**
	 * Dohvaća sve podijeljene ocjene na kolegiju.
	 * 
	 * @param em entity manager
	 * @param courseInstance primjerak kolegija
	 * @return lista ocjena
	 */
	public List<Grade> listGradesFor(EntityManager em, CourseInstance courseInstance);
	
	/**
	 * Dohvaća ocjenu studenta na kolegiju.
	 * 
	 * @param em entity manager
	 * @param courseInstance primjerak kolegija
	 * @param user student
	 * @return ocjena ili <code>null</code> ako nema ocjene
	 */
	public Grade findGradeForCIAndUser(EntityManager em, CourseInstance courseInstance, User user);
	
	/**
	 * Snima novu ocjenu.
	 * 
	 * @param em entity manager
	 * @param g ocjena
	 */
	public void save(EntityManager em, Grade g);
	
	/**
	 * Briše predanu ocjenu.
	 * 
	 * @param em entity manager
	 * @param g ocjena
	 */
	public void remove(EntityManager em, Grade g);
}
