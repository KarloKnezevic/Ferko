package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.questions.QuestionGroup;

import java.util.List;

import javax.persistence.EntityManager;

public interface QuestionsDAO {

	public QuestionGroup getQuestionGroup(EntityManager em, Long id);
	public void save(EntityManager em, QuestionGroup questionGroup);
	public void remove(EntityManager em, QuestionGroup questionGroup);

	public List<QuestionGroup> listQuestionGroupsForCourse(EntityManager em, Course course);
}
