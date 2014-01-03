package hr.fer.zemris.jcms.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.dao.WikiPageDAO;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.WikiPage;

public class WikiPageDAOJPAImpl implements WikiPageDAO {

	@Override
	public WikiPage get(EntityManager em, Long id) {
		return em.find(WikiPage.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public WikiPage getByPath(EntityManager em, CourseInstance courseInstance,
			String path) {
		List<WikiPage> list = em.createNamedQuery("WikiPage.find").setParameter("course", courseInstance.getCourse()).setParameter("path", path).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}
	
	@Override
	public void remove(EntityManager em, WikiPage wikiPage) {
		em.remove(wikiPage);
	}

	@Override
	public void save(EntityManager em, WikiPage wikiPage) {
		em.persist(wikiPage);
	}

}
