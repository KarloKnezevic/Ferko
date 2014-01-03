package hr.fer.zemris.jcms.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.dao.QuestionsDAO;
import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.questions.QuestionGroup;

public class QuestionsDAOJPAImpl implements QuestionsDAO {
	
	@Override
	public QuestionGroup getQuestionGroup(EntityManager em, Long id) {
		return em.find(QuestionGroup.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<QuestionGroup> listQuestionGroupsForCourse(EntityManager em,
			Course course) {
		return (List<QuestionGroup>)em.createNamedQuery("QuestionGroup.byCourse").setParameter("course", course).getResultList();
	}

	@Override
	public void remove(EntityManager em, QuestionGroup questionGroup) {
		em.remove(questionGroup);
	}

	@Override
	public void save(EntityManager em, QuestionGroup questionGroup) {
		em.persist(questionGroup);
	}

}
