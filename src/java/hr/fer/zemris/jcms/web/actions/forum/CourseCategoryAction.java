package hr.fer.zemris.jcms.web.actions.forum;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.forum.Category;

import javax.persistence.EntityManager;

/**
 * Omogućuje otvaranje predmetne kategorije ili preusmjerava na nju ako je
 * već stvorena.
 * 
 * @author Hrvoje Ban
 */
@SuppressWarnings("serial")
public class CourseCategoryAction extends AbstractAction {
	
	private CourseInstance ci;
	private Category category;
	private boolean canCreateCategory;
	
	public String getTitle() {
		return getText("Forum.nonexistentCategory");
	}
	
	@Override
	public void prepare() throws Exception {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<String>() {
		@Override
		public String executeOperation(EntityManager em) {
			prepare(em, null);
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			ci = dh.getCourseInstanceDAO().get(em, getCourseInstanceID());
			if (ci == null)
				throw new IllegalParameterException();
			
			category = ci.getCourse().getCategory();
			canCreateCategory = getSecurityManager().canCreateCourseCategory(ci);
			return null;
		}});
	}
	
	@Override
	public String execute() throws Exception {
		if (category == null)
			return INPUT;
		else
			return SUCCESS;
	}
	
	public String save() throws Exception {
		if (category != null || !canCreateCategory)
			return NO_PERMISSION;
	
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		@Override
		public Void executeOperation(EntityManager em) {
			category = new Category();
			category.setCourse(ci.getCourse());
			ci.getCourse().setCategory(category);	
			em.persist(category);
			return null;
		}});	
		return SUCCESS;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public boolean isCanCreateCategory() {
		return canCreateCategory;
	}

}
