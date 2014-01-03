package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.WikiPage;

import javax.persistence.EntityManager;

public interface WikiPageDAO {
	public WikiPage get(EntityManager em, Long id);
	public WikiPage getByPath(EntityManager em, CourseInstance courseInstance, String path);
	public void save(EntityManager em, WikiPage wikiPage);
	public void remove(EntityManager em, WikiPage wikiPage);
}
