package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.ForumDAO;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.forum.AbstractEntity;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Post;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Subscription;
import hr.fer.zemris.jcms.model.forum.Topic;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

public class ForumDAOJPAImpl implements ForumDAO {
	
	private <T> T acquire(EntityManager em, Class<T> entityClass, Object id) {
		if (id == null)
			throw new NoResultException();
		
		T entity = em.find(entityClass, id);
		if (entity == null)
			throw new NoResultException();
		
		return entity;
	}
	
	@Override
	public <T extends AbstractEntity> void save(EntityManager em, T entity) {
		if (entity.getId() == null)
			em.persist(entity);
		else
			em.merge(entity);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Subscription> getUserSubscriptions(EntityManager em, User user, boolean getHidden) {
		return em.createNamedQuery("Subscription.byUser" + (getHidden ? "" : "NonHidden"))
		.setParameter("userId", user.getId()).getResultList();
	}
	
	@Override
	public Subscription findUserSubscription(EntityManager em, User user, Category category) {
		try {
			return (Subscription)em.createNamedQuery("Subscription.byUserAndCategory")
			.setParameter("userId", user.getId())
			.setParameter("categoryId", category.getId())
			.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public void removeUserSubscriptions(EntityManager em, User user, Long[] subscriptionsIds) {
		if (subscriptionsIds == null || subscriptionsIds.length == 0)
			return;
		
		StringBuilder sbQuery = new StringBuilder(128);
		sbQuery.append("DELETE FROM Subscription s WHERE s.user.id=").append(user.getId())
		.append(" AND s.category.id IN (").append(subscriptionsIds[0]);
		
		for (int i = 1; i < subscriptionsIds.length; ++i)
			sbQuery.append(", ").append(subscriptionsIds[i]);
		
		sbQuery.append(")");
		em.createQuery(sbQuery.toString()).executeUpdate();
	}
	
	@Override
	public Category acquireCategory(EntityManager em, Long id) {
		return acquire(em, Category.class, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Category> getNonCourseCategories(EntityManager em, boolean getHidden) {
		return em.createNamedQuery("Category.nonCourse" + (getHidden ? "" : "NonHidden"))
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Category> getAllCategories(EntityManager em) {
		return em.createNamedQuery("Category.all")
		.getResultList();
	}
	
	@Override
	public Subforum acquireSubforum(EntityManager em, Long id) {
		return (Subforum)em.createNamedQuery("Subforum.find")
		.setParameter("id", id)
		.getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<Subforum> getCategorySubforums(EntityManager em, Category category, boolean getHidden) {
		return em.createNamedQuery("Subforum.list" + (getHidden ? "" : "NonHidden"))
		.setParameter("categoryId", category.getId()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Subforum> getOpenCategorySubforums(EntityManager em, Category category) {
		return em.createNamedQuery("Subforum.listOpen")
		.setParameter("categoryId", category.getId())
		.getResultList();
	}
	
	@Override
	public void updateFirstTopic(EntityManager em, Subforum subforum) {
		List<Topic> second = getTopicsRange(em, subforum, 1, 1);
		if (second.size() != 0)
			subforum.setFirstTopic(second.get(0));
		else
			subforum.setFirstTopic(null);
	}
	
	@Override
	public Topic acquireTopic(EntityManager em, Long id) {
		return (Topic)em.createNamedQuery("Topic.find")
		.setParameter("id", id)
		.getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Topic> getTopicsRangePinnedFirst(EntityManager em, Subforum subforum, int start, int count, boolean getHidden) {
		return em.createNamedQuery("Topic.listPinnedFirst" + (getHidden ? "" : "NonHidden"))
		.setParameter("subforumId", subforum.getId())
		.setFirstResult(start)
		.setMaxResults(count)
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Topic> getTopicsRange(EntityManager em, Subforum subforum, int start, int count) {
		return em.createNamedQuery("Topic.list")
		.setParameter("subforumId", subforum.getId())
		.setFirstResult(start)
		.setMaxResults(count)
		.getResultList();
	}
	
	@Override
	public Post acquirePost(EntityManager em, Long id) {
		return (Post)em.createNamedQuery("Post.find")
		.setParameter("id", id)
		.getSingleResult();
	}
	
	@Override
	public Post findPostByOrdinal(EntityManager em, Topic topic, int ordinal) {
		return (Post)em.createNamedQuery("Post.byOrdinal")
		.setParameter("topicId", topic.getId())
		.setParameter("ordinal", ordinal)
		.getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Post> getPosts(EntityManager em, Topic topic, int start, int end) {
		return em.createNamedQuery("Post.list")
		.setParameter("topicId", topic.getId())
		.setParameter("start", start)
		.setParameter("end", end)
		.getResultList();
	}
	
	@Override
	public void pushPosts(EntityManager em, Topic topic, Post reply, Integer fromOrdinal) {
		em.createNamedQuery("Post.push")
		.setParameter("topicId", topic.getId())
		.setParameter("replyId", reply.getId())
		.setParameter("fromOrdinal", fromOrdinal)
		.executeUpdate();
	}

}
