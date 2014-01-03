package hr.fer.zemris.jcms.dao;

import java.util.List;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.SeminarInfo;
import hr.fer.zemris.jcms.model.SeminarRoot;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;

public interface SeminarDAO {

	// -------------------------------------------
	// Metode za rad s vršnom kategorijom seminara
	// -------------------------------------------
	public void saveSeminarRoot(EntityManager em, SeminarRoot seminarRoot);
	public void removeSeminarRoot(EntityManager em, SeminarRoot seminarRoot);
	public SeminarRoot getSeminarRoot(EntityManager em, Long id);
	public List<SeminarRoot> findSeminarRootsFor(EntityManager em, YearSemester yearSemester);
	public List<SeminarRoot> findActiveSeminarRoots(EntityManager em);
	public List<SeminarRoot> listSeminarRoots(EntityManager em);
	
	// ----------------------------------------------
	// Metode za rad s pojedinim seminarima studenata
	// ----------------------------------------------
	public void saveSeminarInfo(EntityManager em, SeminarInfo seminarInfo);
	public void removeSeminarInfo(EntityManager em, SeminarInfo seminarInfo);
	public SeminarInfo getSeminarInfo(EntityManager em, Long id);
	public List<SeminarInfo> listSeminarInfosFor(EntityManager em, SeminarRoot seminarRoot);
	public List<SeminarInfo> findSeminarInfosFor(EntityManager em, SeminarRoot seminarRoot, User student);
	public List<SeminarInfo> findSeminarInfosFor(EntityManager em, Group group);
}
