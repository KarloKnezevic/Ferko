package hr.fer.zemris.jcms.dao.impl;


import hr.fer.zemris.jcms.dao.RepositoryFileDAO;
import hr.fer.zemris.jcms.model.RepositoryCategory;
import hr.fer.zemris.jcms.model.RepositoryCourse;
import hr.fer.zemris.jcms.model.RepositoryFile;

import javax.persistence.EntityManager;


public class RepositoryFileDAOJPAImpl implements RepositoryFileDAO {

	@Override
	public RepositoryFile getFile(EntityManager em, Long id) {
		return em.find(RepositoryFile.class, id);
	}
	@Override
	public void remove(EntityManager em, RepositoryFile repositoryFile) {
		em.remove(repositoryFile);
	}
	@Override
	public void save(EntityManager em, RepositoryFile repositoryFile) {
		em.persist(repositoryFile);
	}
	
	
	@Override
	public RepositoryCategory getRepositoryCategory(EntityManager em, Long id) {
		return em.find(RepositoryCategory.class, id);
	}
	@Override
	public void remove(EntityManager em, RepositoryCategory repositoryCategory) {
		em.remove(repositoryCategory);
	}	
	@Override
	public void save(EntityManager em, RepositoryCategory repositoryCategory) {
		em.persist(repositoryCategory);
		}
	
	
	@Override
	public RepositoryCourse getRepositoryCourse(EntityManager em, Long id) {
		return em.find(RepositoryCourse.class, id);
		
	}
	@Override
	public void remove(EntityManager em, RepositoryCourse repositoryCourse) {
		em.remove(repositoryCourse);
	}
	@Override
	public void save(EntityManager em, RepositoryCourse repositoryCourse) {
		em.persist(repositoryCourse);
	}

}
