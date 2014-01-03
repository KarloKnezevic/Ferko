package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.RepositoryFilePageDAO;
import hr.fer.zemris.jcms.model.RepositoryFilePage;
import hr.fer.zemris.jcms.model.RepositoryFilePageComment;

import javax.persistence.EntityManager;


public class RepositoryFilePageDAOJPAImpl implements RepositoryFilePageDAO {

	@Override
	public RepositoryFilePage getFilePage(EntityManager em, Long id) {
		return em.find(RepositoryFilePage.class, id);
	}
	@Override
	public void remove(EntityManager em, RepositoryFilePage repositoryFilePage) {
		em.remove(repositoryFilePage);
	}
	@Override
	public void save(EntityManager em, RepositoryFilePage repositoryFilePage) {
		em.persist(repositoryFilePage);
	}
	
	
	@Override
	public RepositoryFilePageComment getFilePageComment(EntityManager em, Long id) {
		return em.find(RepositoryFilePageComment.class, id);
	}
	@Override
	public void remove(EntityManager em, RepositoryFilePageComment repositoryFilePageComment) {
		em.remove(repositoryFilePageComment);
	}	
	@Override
	public void save(EntityManager em, RepositoryFilePageComment repositoryFilePageComment) {
		em.persist(repositoryFilePageComment);
	}
	
}
