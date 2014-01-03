package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;

import java.util.List;

import javax.persistence.EntityManager;

public interface YearSemesterDAO {

	public List<YearSemester> list(EntityManager em);
	public YearSemester get(EntityManager em, String id);
	public void save(EntityManager em, YearSemester y);
	public void remove(EntityManager em, YearSemester y);
	public List<User> findUsersInSemester(EntityManager em, YearSemester y);
}
