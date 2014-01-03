package hr.fer.zemris.jcms.web.actions.forum;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.forum.Post;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Topic;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import com.opensymphony.xwork2.interceptor.ParameterNameAware;

/**
 * Omogućuje brisanje kategorija, podforuma, tema i poruka.
 * 
 * @author Hrvoje Ban
 */
@SuppressWarnings("serial")
public class DeleteAction extends AbstractAction implements ParameterNameAware {
	
	private static Set<String> acceptableParameters;
	
	/** ID entita kojeg treba prikazati nakon uspješnog brisanja */
	private Long id;
	
	/** ID entiteta kojeg treba izbrisati */
	private Long mid;
	
	static {
		acceptableParameters = new HashSet<String>();
		acceptableParameters.add("courseInstanceID");
		acceptableParameters.add("mid");
	}
	
	@Override
	public boolean acceptableParameterName(String parameterName) {
		return acceptableParameters.contains(parameterName);
	}
	
	public String topic() throws Exception {
		return PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<String>() {
			@Override
			public String executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				Topic topic = dh.getForumDAO().acquireTopic(em, mid);
				Subforum subforum = topic.getSubforum();
				prepare(em, topic.getSubforum().getCategory());
				if (!topic.isHidden() || !getSecurityManager().canEditTopic(subforum, getData().getCourseInstance()))
					return NO_PERMISSION;
				
				id = topic.getSubforum().getId();
				subforum.decreasePostCount(topic.getPostCount());
				subforum.decreaseTopicCount();
				if (topic.equals(subforum.getFirstTopic()))
					dh.getForumDAO().updateFirstTopic(em, topic.getSubforum());
				
				topic.setLastPost(null);
				em.remove(topic);
				return "update-topic";
			}
		});
	}
	
	public String post() throws Exception {
		return PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<String>() {
			@Override
			public String executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				Post post = dh.getForumDAO().acquirePost(em, mid);
				Topic topic = post.getTopic();
				prepare(em, topic.getSubforum().getCategory());
				if (post.getOrdinal() == 1 || getLoggedUser() != post.getAuthor() &&
						!getSecurityManager().canEditOthersPost(topic, getData().getCourseInstance()))
					return NO_PERMISSION;
				
				id = post.getTopic().getId();
				if (post.getOrdinal() != post.getTopic().getPostCount()) {
					dh.getForumDAO().pushPosts(em, topic, post.getReplyTo(), post.getOrdinal());
				} else
					topic.setLastPost(dh.getForumDAO().findPostByOrdinal(em, topic, topic.getPostCount() - 1));
				
				post.getTopic().decreasePostCount(1);
				dh.getForumDAO().save(em, post.getTopic());
				em.remove(post);
				return "update-post";
			}
		});
	}
	
	public Long getId() {
		return id;
	}
	
	public void setMid(Long mid) {
		this.mid = mid;
	}

}
