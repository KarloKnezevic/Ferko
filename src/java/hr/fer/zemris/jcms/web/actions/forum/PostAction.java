package hr.fer.zemris.jcms.web.actions.forum;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.model.forum.Post;
import hr.fer.zemris.jcms.model.forum.Topic;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.interceptor.ParameterNameAware;
import com.opensymphony.xwork2.validator.annotations.Validation;
import com.opensymphony.xwork2.validator.annotations.VisitorFieldValidator;

/**
 * Omogućuje stvanje i uređivanje poruka, te stvanje tema.
 * 
 * @author Hrvoje Ban
 */
@SuppressWarnings("serial")
@Validation
public class PostAction extends AbstractAction
		implements ModelDriven<Post>, ParameterNameAware {
	
	private static Set<String> acceptableParameters;
	
	/** ID podforuma unutar kojeg treba otvoriti temu */
	private Long sid;
	
	/** ID teme unutar koje treba stvoriti poruku */
	private Long tid;
	
	/** ID poruke */
	private Long mid;
	
	/**	HTML prikaz poruke */
	private String preview;
	
	/** ID poruke na koju je ova replika */
	private Long reply;
	
	private Post post;
	private boolean canCreateTopic;
	private boolean canCreatePost;
	private boolean canEditPost;
	
	static {
		acceptableParameters = new HashSet<String>();
		acceptableParameters.add("courseInstanceID");
		acceptableParameters.add("sid");
		acceptableParameters.add("tid");
		acceptableParameters.add("mid");
		acceptableParameters.add("reply");
		acceptableParameters.add("name");
		acceptableParameters.add("message");
	}
	
	public String getTitle() {
		if (mid == null) {
			if (tid == null)
				return getText("Forum.newTopic");
			else
				return getText("Forum.newPost");
		} else
			return getText("Forum.editPost");
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
			if (mid == null) {
				post = new Post();
				if (tid == null) {
					Topic topic = new Topic();
					topic.setSubforum(dh.getForumDAO().acquireSubforum(em, sid));
					post.setTopic(topic);
					post.setOrdinal(1);
				} else {
					post.setTopic(dh.getForumDAO().acquireTopic(em, tid));
					post.setOrdinal(post.getTopic().getPostCount() + 1);
					Post replyTo;
					if (reply == null) {
						replyTo = dh.getForumDAO().findPostByOrdinal(em, post.getTopic(), 1);
					} else {
						replyTo = dh.getForumDAO().acquirePost(em, reply);
						if (!replyTo.getTopic().equals(post.getTopic()))
							throw new IllegalParameterException();
					}
					post.setReplyTo(replyTo);
					String name = replyTo.getName();
					if (name.startsWith("Re: "))
						post.setName(name);
					else
						post.setName("Re: " + name);
				}
			} else
				post = dh.getForumDAO().acquirePost(em, mid);
			
			prepare(em, post.getTopic().getSubforum().getCategory());
			canCreateTopic = getSecurityManager().canCreateTopic(post.getTopic().getSubforum(), getData().getCourseInstance());
			canCreatePost = getSecurityManager().canCreatePost(post.getTopic(), getData().getCourseInstance());
			canEditPost = getSecurityManager().canEditOthersPost(post.getTopic(), getData().getCourseInstance()) ||
					(mid != null && post.getAuthor().equals(getLoggedUser()));
			
			return null;
		}});
	}
	
	@Override
	public void validate() {
		if (post.getOrdinal() != 1 && post.getName().trim().equals(""))
			post.setName("Re: " + post.getReplyTo().getName());
	}
	
	@Override
	public String input() throws Exception {	
		if (tid == null && !canCreateTopic)
			return NO_PERMISSION;
		else if (mid != null && !canEditPost)
			return NO_PERMISSION;
		else if (mid == null && !canCreatePost)
			return NO_PERMISSION;
		else
			return INPUT;
	}
	
	public String preview() throws Exception {
		preview = post.getMessage();
		return input();
	}
	
	public String save() throws Exception {
		if (input().equals(NO_PERMISSION))
			return NO_PERMISSION;
		
		Topic topic = post.getTopic();
		if (mid == null) {
			post.setAuthor(getLoggedUser());
			post.setCreationDate(new Date());
			if (tid == null) {
				topic.setAuthor(getLoggedUser());
				topic.getSubforum().increaseTopicCount();
			}
			topic.setModificationDate(post.getCreationDate());
			topic.increasePostCount(1);
		} else {
			post.setEditor(getLoggedUser());
			post.setModificationDate(new Date());
		}
		if (post.getOrdinal() == 1)
			topic.setName(post.getName());
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if (tid == null)
					dh.getForumDAO().save(em, post.getTopic());
				
				dh.getForumDAO().save(em, post);
				if (mid == null) {
					post.getTopic().getSubforum().setFirstTopic(post.getTopic());
					post.getTopic().setLastPost(post);
					dh.getForumDAO().save(em, post.getTopic().getSubforum());
					dh.getForumDAO().save(em, post.getTopic());
				}
				return null;
			}
		});
		return UPDATE;
	}
	
	public void setSid(Long sid) {
		this.sid = sid;
	}
	
	public void setTid(Long tid) {
		this.tid = tid;
	}
	
	public void setMid(Long mid) {
		this.mid = mid;
	}
	
	public String getPreview() {
		return preview;
	}
	
	public Long getReply() {
		return reply;
	}
	
	public void setReply(Long reply) {
		this.reply = reply;
	}
	
	@Override
	@VisitorFieldValidator(appendPrefix = false, message = "")
	public Post getModel() {
		return post;
	}

}
