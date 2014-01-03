package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.AssessmentFlagTagDAO;
import hr.fer.zemris.jcms.model.AssessmentFlagTag;

import java.util.List;

import javax.persistence.EntityManager;

public class AssessmentFlagTagDAOJPAImpl implements AssessmentFlagTagDAO {

	@Override
	public AssessmentFlagTag get(EntityManager em, Long id) {
		return em.find(AssessmentFlagTag.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AssessmentFlagTag getByShortName(EntityManager em, String shortName) {
		List<AssessmentFlagTag> list = (List<AssessmentFlagTag>)em.createNamedQuery("AssessmentFlagTag.findByShortName")
			.setParameter("shortName", shortName).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFlagTag> list(EntityManager em) {
		return (List<AssessmentFlagTag>)em.createNamedQuery("AssessmentFlagTag.list").getResultList();
	}

	@Override
	public void remove(EntityManager em, AssessmentFlagTag assessmentFlagTag) {
		em.remove(assessmentFlagTag);
	}

	@Override
	public void save(EntityManager em, AssessmentFlagTag assessmentFlagTag) {
		em.persist(assessmentFlagTag);
	}
}
