package hr.fer.zemris.jcms.web.actions.forum;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Subscription;

import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;

/**
 * Omogućuje prikaz i uređivanje pretplata na kategorije.
 * 
 * @author Hrvoje Ban
 */
@SuppressWarnings("serial")
public class ForumIndexAction extends AbstractAction {

	private List<Category> nonCourseCategories;
	private List<Subscription> subscriptions;
	private Long[] canceledSubscriptions;
	private Long categoryId;
	private boolean prepareView;
	private boolean canCreateCategory;
	
	public void prepareExecute() throws Exception {
		prepareView = true;
	}
	
	@Override
	public void prepare() throws Exception {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		@Override
		public Void executeOperation(EntityManager em) {
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			prepare(em, null);
			boolean canViewHidden = getSecurityManager().canViewHiddenForum(null);
			nonCourseCategories = dh.getForumDAO().getNonCourseCategories(em, canViewHidden);
			subscriptions = dh.getForumDAO().getUserSubscriptions(em, getLoggedUser(), !prepareView || canViewHidden);
			for (Subscription sub : subscriptions) {
				nonCourseCategories.remove(sub.getCategory());
				if (prepareView) {
					sub.getCategory().setSubforums(new HashSet<Subforum>(dh.getForumDAO().getCategorySubforums(
						em, sub.getCategory(), canViewHidden)));
				}
			}
			canCreateCategory = getSecurityManager().canCreateNonCourseCategory();
			return null;
		}});
	}
	
	public String save() throws Exception {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		@Override
		public Void executeOperation(EntityManager em) {
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			if (canceledSubscriptions != null)
				dh.getForumDAO().removeUserSubscriptions(em, getLoggedUser(), canceledSubscriptions);
			
			return null;
		}});
		return UPDATE;
	}
	
	public String add() throws Exception {
		if (categoryId != null) {
			PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				Category category = dh.getForumDAO().acquireCategory(em, categoryId);
				if (dh.getForumDAO().findUserSubscription(em, getLoggedUser(), category) != null)
					return null;
				
				CourseInstance ci = null;
				if (getCourseInstanceID() != null && category.getCourse() != null) {
					ci = dh.getCourseInstanceDAO().get(em, getCourseInstanceID());
					if (ci == null || !category.getCourse().equals(ci.getCourse()))
						throw new IllegalParameterException();
				}				
				Subscription subscription = new Subscription();
				subscription.setUser(getLoggedUser());
				subscription.setCategory(category);
				subscription.setCourseInstance(ci);
				em.persist(subscription);
				return null;
			}});		
		}
		return UPDATE;	
	}
	
	public List<Category> getNonCourseCategories() {
		return nonCourseCategories;
	}
	
	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}
	
	public void setCanceledSubscriptions(Long[] canceledSubscriptions) {
		this.canceledSubscriptions = canceledSubscriptions;
	}
	
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	
	public boolean isCanCreateCategory() {
		return canCreateCategory;
	}

}
