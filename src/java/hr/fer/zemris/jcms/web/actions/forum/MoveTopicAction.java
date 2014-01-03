package hr.fer.zemris.jcms.web.actions.forum;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Topic;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.interceptor.ParameterNameAware;

@SuppressWarnings("serial")
public class MoveTopicAction extends AbstractAction
		implements ModelDriven<Topic>, ParameterNameAware {
	
	private static Set<String> acceptableParameters;
	
	private Long mid;
	private Long destination;
	private Topic topic;
	private List<Category> categories;
	private boolean canEditTopic;
	
	static {
		acceptableParameters = new HashSet<String>();
		acceptableParameters.add("courseInstanceID");
		acceptableParameters.add("mid");
		acceptableParameters.add("destination");
	}

	@Override
	public String getTitle() {
		return getText("Forum.moveTopic");
	}
	
	@Override
	public boolean acceptableParameterName(String parameterName) {
		return acceptableParameters.contains(parameterName);
	}
	
	@Override
	public void prepare() throws Exception {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		@Override
		public Void executeOperation(EntityManager em) {
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			topic = dh.getForumDAO().acquireTopic(em, mid);
			prepare(em, topic.getSubforum().getCategory());
			categories = dh.getForumDAO().getAllCategories(em);
			
			Iterator<Category> iter = categories.iterator();
			while (iter.hasNext()) { 
				Category category = iter.next();
				if (category.isClosed())
					iter.remove();
				else {
					List<Subforum> subforums = dh.getForumDAO().getOpenCategorySubforums(em, category);
					if (subforums.size() == 0)
						iter.remove();
					else
						category.setSubforums(new HashSet<Subforum>(subforums));					
				}
			}			
			canEditTopic = getSecurityManager().canEditTopic(topic.getSubforum(), getData().getCourseInstance());
			
			return null;
		}});
	}
	
	@Override
	public String input() throws Exception {
		if (!canEditTopic)
			return NO_PERMISSION;
		else
			return INPUT;
	}
	
	@Override
	public String execute() throws Exception {
		if (input() == NO_PERMISSION)
			return NO_PERMISSION;
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		@Override
		public Void executeOperation(EntityManager em) {
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			Subforum subforum = dh.getForumDAO().acquireSubforum(em, destination);
			if (subforum.isClosed() || subforum.getCategory().isClosed())
				throw new IllegalParameterException();
			
			dh.getForumDAO().updateFirstTopic(em, topic.getSubforum());
			if (subforum.getFirstTopic() == null ||
					subforum.getFirstTopic().getModificationDate().compareTo(topic.getModificationDate()) < 0)
				subforum.setFirstTopic(topic);
			
			topic.getSubforum().decreasePostCount(topic.getPostCount());
			topic.getSubforum().decreaseTopicCount();
			em.merge(topic.getSubforum());
			topic.setSubforum(subforum);
			subforum.increaseTopicCount();
			subforum.increasePostCount(topic.getPostCount());
			em.merge(topic);
			return null;
		}});
		return UPDATE;
	}
	
	public void setMid(Long mid) {
		this.mid = mid;
	}
	
	public Long getDestination() {
		return destination;
	}
	
	public void setDestination(Long destination) {
		this.destination = destination;
	}
	
	@Override
	public Topic getModel() {
		return topic;
	}
	
	public List<Category> getCategories() {
		return categories;
	}	

}
