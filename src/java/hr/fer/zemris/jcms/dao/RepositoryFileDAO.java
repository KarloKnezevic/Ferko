package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.RepositoryCategory;
import hr.fer.zemris.jcms.model.RepositoryCourse;
import hr.fer.zemris.jcms.model.RepositoryFile;

import javax.persistence.EntityManager;

public interface RepositoryFileDAO {

	public RepositoryFile getFile(EntityManager em, Long id);
	public void save(EntityManager em, RepositoryFile repositoryFile);
	public void remove(EntityManager em, RepositoryFile repositoryFile);

	public RepositoryCourse getRepositoryCourse(EntityManager em, Long id);
	public void save(EntityManager em, RepositoryCourse repositoryCourse);
	public void remove(EntityManager em, RepositoryCourse repositoryCourse); 

	public RepositoryCategory getRepositoryCategory(EntityManager em, Long id);
	public void save(EntityManager em, RepositoryCategory repositoryCategory);
	public void remove(EntityManager em, RepositoryCategory repositoryCategory);
	
}
