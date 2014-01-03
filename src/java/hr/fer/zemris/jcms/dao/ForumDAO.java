package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.forum.AbstractEntity;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Post;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Subscription;
import hr.fer.zemris.jcms.model.forum.Topic;

import java.util.List;

import javax.persistence.EntityManager;

public interface ForumDAO {
	
	public <T extends AbstractEntity> void save(EntityManager em, T entity);
	
	public List<Subscription> getUserSubscriptions(EntityManager em, User user, boolean getHidden);
	public Subscription findUserSubscription(EntityManager em, User user, Category category);
	public void removeUserSubscriptions(EntityManager em, User user, Long[] subscriptionsIds);
	
	public Category acquireCategory(EntityManager em, Long id);
	public List<Category> getNonCourseCategories(EntityManager em, boolean getHidden);
	public List<Category> getAllCategories(EntityManager em);
	
	public Subforum acquireSubforum(EntityManager em, Long id);
	public List<Subforum> getCategorySubforums(EntityManager em, Category category, boolean getHidden);
	public List<Subforum> getOpenCategorySubforums(EntityManager em, Category category);
	public void updateFirstTopic(EntityManager em, Subforum subforum);
	
	public Topic acquireTopic(EntityManager em, Long id);
	public List<Topic> getTopicsRangePinnedFirst(EntityManager em, Subforum subforum, int start, int count, boolean getHidden);
	public List<Topic> getTopicsRange(EntityManager em, Subforum subforum, int start, int count);
	
	public Post acquirePost(EntityManager em, Long id);
	public Post findPostByOrdinal(EntityManager em, Topic topic, int ordinal);
	public List<Post> getPosts(EntityManager em, Topic topic, int start, int end);
	public void pushPosts(EntityManager em, Topic topic, Post reply, Integer fromOrdinal);

}
