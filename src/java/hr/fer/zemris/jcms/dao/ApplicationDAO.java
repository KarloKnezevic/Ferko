package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * @author Nikola
 *
 */
public interface ApplicationDAO {
	public void save(EntityManager em, StudentApplication application);
	public void remove(EntityManager em, StudentApplication application);
	
	public void save(EntityManager em, ApplicationDefinition definition);
	public void remove(EntityManager em, ApplicationDefinition definition);
	
	public StudentApplication get(EntityManager em, Long id);
	public ApplicationDefinition getDefinition(EntityManager em, Long id);

	/**
	 *	Vraća listu svih definicija prijave
	 * @param em
	 * @return
	 */
	public List<ApplicationDefinition> listDefinitions (EntityManager em, String courseId);
	/**
	 *  Vraća listu svih prijava studenata
	 * @param em
	 * @return
	 */
	public List<StudentApplication> listStudentApplications (EntityManager em, String courseId);
		
	/**
	 *  Vraća listu svih prijava koje je ispunio određeni student
	 * @param em
	 * @param currentUserID
	 * @return
	 */
	public List<StudentApplication> listForUser(EntityManager em, User user, String courseId);
	
	public StudentApplication getApplicationForUser(EntityManager em, User user,
			Long appId);
	public List<StudentApplication> listForDefinition(EntityManager em, String courseId, Long defId);
	
	public List<StudentApplicationShortBean> listShortBeansFor(EntityManager em, CourseInstance courseInstance, String shortName);
	public ApplicationDefinition getForShortName(EntityManager em, CourseInstance courseInstance, String shortName);
}
