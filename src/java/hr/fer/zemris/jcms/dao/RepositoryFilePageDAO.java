package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.RepositoryFilePage;
import hr.fer.zemris.jcms.model.RepositoryFilePageComment;

import javax.persistence.EntityManager;
	
public interface RepositoryFilePageDAO {

	public RepositoryFilePage getFilePage(EntityManager em, Long id);
	public void save(EntityManager em, RepositoryFilePage repositoryFilePage);
	public void remove(EntityManager em, RepositoryFilePage repositoryFilePage);

	public RepositoryFilePageComment getFilePageComment(EntityManager em, Long id);
	public void save(EntityManager em, RepositoryFilePageComment repositoryFilePageComment);
	public void remove(EntityManager em, RepositoryFilePageComment repositoryFilePageComment); 

		
}

