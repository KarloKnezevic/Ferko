package hr.fer.zemris.jcms.web.actions.forum;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Topic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.interceptor.ParameterNameAware;
import com.opensymphony.xwork2.validator.annotations.Validation;
import com.opensymphony.xwork2.validator.annotations.VisitorFieldValidator;

/**
 * Omogućuje stvaranje i uređivanje podforuma.
 * 
 * @author Hrvoje Ban
 */
@SuppressWarnings("serial")
@Validation
public class SubforumAction extends AbstractAction
		implements ModelDriven<Subforum>, ParameterNameAware {
	
	private static Set<String> acceptableParameters;
	
	/** ID katetogije kojoj pripada podforum */
	private Long cid;
	
	/** ID podforuma */
	private Long mid;
	
	/** Teme koje treba prikazati na trenutnoj stranici */
	private List<Topic> pageTopics;

	/** Stranica sa temama koje treba prikazati */
	private int page;
	
	/** Treba li dohvati i podatke za prikazivanje (ili samo za uređivanje) */
	private boolean prepareView;
	
	private String status;
	private Subforum subforum;
	private boolean canViewSubforum;
	private boolean canEditSubforum;
	private boolean canCreateTopic;
	
	static {
		acceptableParameters = new HashSet<String>();
		acceptableParameters.add("courseInstanceID");
		acceptableParameters.add("cid");
		acceptableParameters.add("mid");
		acceptableParameters.add("name");
		acceptableParameters.add("description");
		acceptableParameters.add("status");
		acceptableParameters.add("page");
	}
	
	public String getTitle() {
		if (mid == null)
			return getText("Forum.newSubforum");
		else if (!prepareView)
			return getText("Forum.editSubforum");
		else
			return subforum.getName();
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
			if (mid == null) {
				subforum = new Subforum();
				subforum.setCategory(dh.getForumDAO().acquireCategory(em, cid));
			} else
				subforum = dh.getForumDAO().acquireSubforum(em, mid);
			
			prepare(em, subforum.getCategory());
			CourseInstance ci = getData().getCourseInstance();
			if (prepareView) {
				int topicsPerPage = 20; //getCurrentUser().getTopicsPerPage();
				page = Math.min(Math.max(1, page), (subforum.getTopicCount() - 1) / topicsPerPage + 1);
				int start = (page - 1) * topicsPerPage;
				pageTopics = dh.getForumDAO().getTopicsRangePinnedFirst(em, subforum, start, topicsPerPage,
						getSecurityManager().canViewHiddenForum(ci));
			}
			canViewSubforum = getSecurityManager().canViewSubforum(subforum, ci);
			canEditSubforum = getSecurityManager().canEditSubforum(subforum.getCategory(), ci);
			canCreateTopic = getSecurityManager().canCreateTopic(subforum, ci);
			
			if (subforum.isClosed()) {
				if (subforum.isHidden())
					status = "hidden";
				else
					status = "closed";
			} else
				status = "open";
			
			return null;
		}});
	}
	
	@Override
	public String execute() throws Exception {
		if (!canViewSubforum)
			return NO_PERMISSION;
		else
			return SUCCESS;
	}
	
	@Override
	public String input() throws Exception {
		if (!canEditSubforum)
			return NO_PERMISSION;
		else
			return INPUT;
	}
	
	public String save() throws Exception {
		if (input().equals(NO_PERMISSION))
			return NO_PERMISSION;
		
		subforum.setClosed(false);
		subforum.setHidden(false);
		if ("closed".equals(status))
			subforum.setClosed(true);
		else if ("hidden".equals(status)) {
			subforum.setClosed(true);
			subforum.setHidden(true);
		}		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				dh.getForumDAO().save(em, subforum);
				return null;
			}
		});
		return UPDATE;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}
	
	public void setMid(Long mid) {
		this.mid = mid;
	}
	
	public List<Topic> getPageTopics() {
		return pageTopics;
	}
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	@VisitorFieldValidator(appendPrefix = false, message = "")
	public Subforum getModel() {
		return subforum;
	}
	
	public boolean isCanEditSubforum() {
		return canEditSubforum;
	}
	
	public boolean isCanCreateTopic() {
		return canCreateTopic;
	}

}
