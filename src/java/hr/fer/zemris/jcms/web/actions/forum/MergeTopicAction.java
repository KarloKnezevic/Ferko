package hr.fer.zemris.jcms.web.actions.forum;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.forum.Post;
import hr.fer.zemris.jcms.model.forum.Topic;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.interceptor.ParameterNameAware;

@SuppressWarnings("serial")
public class MergeTopicAction extends AbstractAction
		implements ModelDriven<Topic>, ParameterNameAware {
	
	private static Set<String> acceptableParameters;
	
	private Long mid;
	private Topic topic;
	private String url;
	private boolean canEditTopic;
	private Topic destination;
	
	static {
		acceptableParameters = new HashSet<String>();
		acceptableParameters.add("courseInstanceID");
		acceptableParameters.add("mid");
		acceptableParameters.add("url");
	}
	
	@Override
	public String getTitle() {
		return getText("Forum.mergeTopic");
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
		if (input().equals(NO_PERMISSION))
			return NO_PERMISSION;
		
		return PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<String>() {
		@Override
		public String executeOperation(EntityManager em) {
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			Matcher matcher;
			try {
				matcher = Pattern.compile("Topic.*?mid=(\\d+)").matcher(url);
				matcher.find();
				Long id = Long.parseLong(matcher.group(1));
				destination = dh.getForumDAO().acquireTopic(em, id);
			} catch (Exception e) {
				addFieldError("url", getText("Forum.wrongURL"));
				return INPUT;
			}
			
			topic = dh.getForumDAO().acquireTopic(em, mid);
			int ordinal = destination.getPostCount() + 1;
			for (Post post : topic.getPosts()) {
				post.setTopic(destination);
				post.setOrdinal(ordinal++);
				em.merge(post);
			}
			Post first = dh.getForumDAO().findPostByOrdinal(em, destination, 1);
			topic.getPosts().iterator().next().setReplyTo(first);
			destination.increasePostCount(topic.getPostCount());
			return UPDATE;
		}});
	}
	
	public void setMid(Long mid) {
		this.mid = mid;
	}
	
	@Override
	public Topic getModel() {
		return topic;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Topic getDestination() {
		return destination;
	}

}
