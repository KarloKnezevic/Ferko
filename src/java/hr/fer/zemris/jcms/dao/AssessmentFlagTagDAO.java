package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.AssessmentFlagTag;

import java.util.List;

import javax.persistence.EntityManager;

public interface AssessmentFlagTagDAO {
	public List<AssessmentFlagTag> list(EntityManager em);
	public AssessmentFlagTag get(EntityManager em, Long id);
	public AssessmentFlagTag getByShortName(EntityManager em, String shortName);
	public void save(EntityManager em, AssessmentFlagTag assessmentFlagTag);
	public void remove(EntityManager em, AssessmentFlagTag assessmentFlagTag);
}
