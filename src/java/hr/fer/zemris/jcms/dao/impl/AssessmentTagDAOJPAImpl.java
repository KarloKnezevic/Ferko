package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.AssessmentTagDAO;
import hr.fer.zemris.jcms.model.AssessmentTag;

import java.util.List;

import javax.persistence.EntityManager;

public class AssessmentTagDAOJPAImpl implements AssessmentTagDAO {

	@Override
	public AssessmentTag get(EntityManager em, Long id) {
		return em.find(AssessmentTag.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AssessmentTag getByShortName(EntityManager em, String shortName) {
		List<AssessmentTag> list = (List<AssessmentTag>)em.createNamedQuery("AssessmentTag.findByShortName")
			.setParameter("shortName", shortName).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentTag> list(EntityManager em) {
		return (List<AssessmentTag>)em.createNamedQuery("AssessmentTag.list").getResultList();
	}

	@Override
	public void remove(EntityManager em, AssessmentTag assessmentTag) {
		em.remove(assessmentTag);
	}

	@Override
	public void save(EntityManager em, AssessmentTag assessmentTag) {
		em.persist(assessmentTag);
	}
}
