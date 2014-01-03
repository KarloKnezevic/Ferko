package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.AssessmentTag;

import java.util.List;

import javax.persistence.EntityManager;

public interface AssessmentTagDAO {
	public List<AssessmentTag> list(EntityManager em);
	public AssessmentTag get(EntityManager em, Long id);
	public AssessmentTag getByShortName(EntityManager em, String shortName);
	public void save(EntityManager em, AssessmentTag assessmentTag);
	public void remove(EntityManager em, AssessmentTag assessmentTag);
}
