package hr.fer.zemris.jcms.web.actions.forum;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Subforum;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.interceptor.ParameterNameAware;
import com.opensymphony.xwork2.validator.annotations.Validation;
import com.opensymphony.xwork2.validator.annotations.VisitorFieldValidator;

/**
 * Omogućuje uređivanje postojećih kategorija te otvaranje nepredmetnih.
 * 
 * @author Hrvoje Ban
 */
@SuppressWarnings("serial")
@Validation
public class CategoryAction extends AbstractAction
		implements ModelDriven<Category>, ParameterNameAware {
	
	private static Set<String> acceptableParameters;
	
	/** ID kategorije */
	private Long mid;
	
	/** Svi podforumi kategorije */
	private List<Subforum> pageSubforums;	
	
	/** Treba li dohvati i podatke za prikazivanje (ili samo za uređivanje) */
	private boolean prepareView;
	
	private String status;
	private Category category;
	private boolean canViewCategory;
	private boolean canEditCategory;
	private boolean canCreateSubforum;
	
	static {
		acceptableParameters = new HashSet<String>();
		acceptableParameters.add("courseInstanceID");
		acceptableParameters.add("mid");
		acceptableParameters.add("name");
		acceptableParameters.add("status");
		acceptableParameters.add("restrictedGroupsIds");
	}
	
	public String getTitle() {
		if (mid == null)
			return getText("Forum.newCategory");
		else if (!prepareView)
			return getText("Forum.editCategory");
		else
			return category.getDisplayName();
	}
	
	@Override
	public boolean acceptableParameterName(String parameterName) {
		return acceptableParameters.contains(parameterName);
	}
	
	public void prepareExecute() throws Exception {
		prepareView = true;
	}
	
	@Override
	public void prepare() throws Exception {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if (mid == null && !prepareView)
					category = new Category();
				else
					category = dh.getForumDAO().acquireCategory(em, mid);
				
				prepare(em, category);
				CourseInstance ci = getData().getCourseInstance();
				canViewCategory = getSecurityManager().canViewCategory(category, ci);
				canEditCategory = getSecurityManager().canEditCategory(category) ||
						getSecurityManager().canCreateCourseCategory(ci);
				canCreateSubforum = getSecurityManager().canEditSubforum(category, ci);
				if (prepareView)
					pageSubforums = dh.getForumDAO().getCategorySubforums(em, category,
							getSecurityManager().canViewHiddenForum(ci));
				
				if (category.isClosed()) {
					if (category.isHidden())
						status = "hidden";
					else
						status = "closed";
				} else
					status = "open";

				return null;
			}
		});
	}
	
	@Override
	public String execute() throws Exception {
		if (!canViewCategory)
			return NO_PERMISSION;
		else
			return SUCCESS;
	}
	
	@Override
	public String input() throws Exception {
		if (!canEditCategory)
			return NO_PERMISSION;
		else
			return INPUT;
	}
	
	public String save() throws Exception {		
		if (input().equals(NO_PERMISSION))
			return NO_PERMISSION;
		
		category.setClosed(false);
		category.setHidden(false);
		if ("closed".equals(status))
			category.setClosed(true);
		else if ("hidden".equals(status)) {
			category.setClosed(true);
			category.setHidden(true);
		}
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				dh.getForumDAO().save(em, category);
				return null;
			}
		});
		return UPDATE;
	}
	
	public void setMid(Long mid) {
		this.mid = mid;
	}
	
	public List<Subforum> getPageSubforums() {
		return pageSubforums;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	@VisitorFieldValidator(appendPrefix = false, message = "")
	public Category getModel() {
		return category;
	}
	
	public boolean isCanEditCategory() {
		return canEditCategory;
	}
	
	public boolean isCanCreateSubforum() {
		return canCreateSubforum;
	}

}
