package hr.fer.zemris.jcms.web.actions.forum;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.forum.Post;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Topic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.interceptor.ParameterNameAware;
import com.opensymphony.xwork2.validator.annotations.VisitorFieldValidator;

/**
 * Omogućuje uređivanje tema.
 * 
 * @author Hrvoje Ban
 */
@SuppressWarnings("serial")
public class TopicAction extends AbstractAction
		implements ModelDriven<Topic>, ParameterNameAware {
	
	private static Set<String> acceptableParameters;
	
	/** ID teme */
	private Long mid;
	
	/** Poruke koje treba prikazati na trenutnoj stranici */
	private List<Post> pagePosts;
	
	/** Redni broj poruke od koje bi trebalo početi prikazivanje */
	private int ordinal;
	
	/** Treba li dohvati i podatke za prikazivanje (ili samo za uređivanje) */
	private boolean prepareView;
	
	private String status;
	private Topic topic;
	private boolean canViewTopic;
	private boolean canEditTopic;
	private boolean canCreatePost;
	private boolean canEditOwnPost;
	private boolean canEditOthersPost;
	
	static {
		acceptableParameters = new HashSet<String>();
		acceptableParameters.add("courseInstanceID");
		acceptableParameters.add("mid");
		acceptableParameters.add("name");
		acceptableParameters.add("pinned");
		acceptableParameters.add("status");
		acceptableParameters.add("ordinal");
	}
	
	public String getTitle() {
		if (!prepareView)
			return getText("Forum.editTopic");
		else
			return topic.getName();
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
			topic = dh.getForumDAO().acquireTopic(em, mid);
			if (prepareView) {
				int postsPerPage = 10; //getCurrentUser().getPostsPerPage();
				ordinal = Math.min(Math.max(1, ordinal), topic.getPostCount());
				int start = (ordinal - 1) / postsPerPage * postsPerPage + 1;
				pagePosts = dh.getForumDAO().getPosts(em, topic, start, start + postsPerPage - 1);
			}
			prepare(em, topic.getSubforum().getCategory());
			CourseInstance ci = getData().getCourseInstance();
			canViewTopic = getSecurityManager().canViewTopic(topic, ci);
			canEditTopic = getSecurityManager().canEditTopic(topic.getSubforum(), ci);
			canCreatePost = getSecurityManager().canCreatePost(topic, ci);
			canEditOwnPost = canCreatePost;
			canEditOthersPost = getSecurityManager().canEditOthersPost(topic, ci);
			
			if (topic.isClosed()) {
				if (topic.isHidden())
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
		if (!canViewTopic)
			return NO_PERMISSION;

		return SUCCESS;
	}
	
	@Override
	public String input() throws Exception {
		if (!canEditTopic || (mid == null && topic.getSubforum().isClosed()))
			return NO_PERMISSION;
		else
			return INPUT;
	}
	
	public String save() throws Exception {
		if (input().equals(NO_PERMISSION))
			return NO_PERMISSION;
		
		topic.setClosed(false);
		topic.setHidden(false);
		if ("closed".equals(status))
			topic.setClosed(true);
		else if ("hidden".equals(status)) {
			topic.setClosed(true);
			topic.setHidden(true);
		}
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		@Override
		public Void executeOperation(EntityManager em) {
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			Subforum subforum = topic.getSubforum();
			Topic firstTopic = subforum.getFirstTopic();
			Post firstPost = dh.getForumDAO().findPostByOrdinal(em, topic, 1);
			firstPost.setName(topic.getName());
			dh.getForumDAO().save(em, topic);
			if (topic.isHidden() && topic.equals(subforum.getFirstTopic()))
				dh.getForumDAO().updateFirstTopic(em, topic.getSubforum());
			else if (firstTopic == null ||
					firstTopic.getModificationDate().compareTo(topic.getModificationDate()) < 0)
				subforum.setFirstTopic(topic);
			
			em.merge(topic.getSubforum());
			return null;
		}});		
		return UPDATE;
	}
	
	public void setMid(Long mid) {
		this.mid = mid;
	}
	
	public List<Post> getPagePosts() {
		return pagePosts;
	}
	
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	@VisitorFieldValidator(appendPrefix = false, message = "")
	public Topic getModel() {
		return topic;
	}
	
	public boolean isCanEditTopic() {
		return canEditTopic;
	}
	
	public boolean isCanCreatePost() {
		return canCreatePost;
	}
	
	public boolean isCanEditOwnPost() {
		return canEditOwnPost;
	}
	
	public boolean isCanEditOthersPost() {
		return canEditOthersPost;
	}

}
