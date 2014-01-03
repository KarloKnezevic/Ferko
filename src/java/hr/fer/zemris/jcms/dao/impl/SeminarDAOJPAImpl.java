package hr.fer.zemris.jcms.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.dao.SeminarDAO;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.SeminarInfo;
import hr.fer.zemris.jcms.model.SeminarRoot;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;

public class SeminarDAOJPAImpl implements SeminarDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<SeminarInfo> findSeminarInfosFor(EntityManager em,
			SeminarRoot seminarRoot, User student) {
		return (List<SeminarInfo>)em.createNamedQuery("SeminarInfo.findSeminarInfosForRootStudent")
		.setParameter("seminarRoot", seminarRoot)
		.setParameter("student", student)
		.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SeminarInfo> findSeminarInfosFor(EntityManager em,
			Group group) {
		return (List<SeminarInfo>)em.createNamedQuery("SeminarInfo.findSeminarInfosForGroup")
		.setParameter("group", group)
		.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SeminarRoot> findActiveSeminarRoots(EntityManager em) {
		return (List<SeminarRoot>)em.createNamedQuery("SeminarRoot.findActiveSeminarRoots")
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SeminarRoot> listSeminarRoots(EntityManager em) {
		return (List<SeminarRoot>)em.createNamedQuery("SeminarRoot.listSeminarRoots")
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SeminarRoot> findSeminarRootsFor(EntityManager em,
			YearSemester yearSemester) {
		return (List<SeminarRoot>)em.createNamedQuery("SeminarRoot.findSeminarRootsForSemester")
		.setParameter("semester", yearSemester)
		.getResultList();
	}

	@Override
	public SeminarInfo getSeminarInfo(EntityManager em, Long id) {
		return em.find(SeminarInfo.class, id);
	}

	@Override
	public SeminarRoot getSeminarRoot(EntityManager em, Long id) {
		return em.find(SeminarRoot.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SeminarInfo> listSeminarInfosFor(EntityManager em,
			SeminarRoot seminarRoot) {
		return (List<SeminarInfo>)em.createNamedQuery("SeminarInfo.findSeminarInfosForRoot")
		.setParameter("seminarRoot", seminarRoot)
		.getResultList();
	}

	@Override
	public void removeSeminarInfo(EntityManager em, SeminarInfo seminarInfo) {
		em.remove(seminarInfo);
	}

	@Override
	public void removeSeminarRoot(EntityManager em, SeminarRoot seminarRoot) {
		em.remove(seminarRoot);
	}

	@Override
	public void saveSeminarInfo(EntityManager em, SeminarInfo seminarInfo) {
		em.persist(seminarInfo);
	}

	@Override
	public void saveSeminarRoot(EntityManager em, SeminarRoot seminarRoot) {
		em.persist(seminarRoot);
	}
}
