package hr.fer.zemris.tests;

import hr.fer.zemris.jcms.beans.cached.CourseScoreTable;
import hr.fer.zemris.jcms.beans.cached.STEScore;
import hr.fer.zemris.jcms.beans.cached.STEStudent;
import hr.fer.zemris.jcms.beans.cached.ScoreTableEntry;
import hr.fer.zemris.jcms.caching.JCMSCacheFactory;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.service.AssessmentService;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CourseScoreTableTest {

	private EntityManagerFactory emf;

	@Before
	public void setUp() throws Exception {
		//emf = Persistence.createEntityManagerFactory("jcmstestdb");
		emf = Persistence.createEntityManagerFactory("jcmsdb");
		PersistenceUtil.initSingleTon(emf);
		JCMSCacheFactory.init();
	}

	@After
	public void tearDown() throws Exception {
		PersistenceUtil.clearSingleTon();
		emf.close();
	}

	@Test
	public void testInsertAuthType() {
		System.out.println(System.getProperty("java.io.tmpdir"));
		IMessageLogger messageLogger = MessageLoggerFactory.createDummyMessageLogger();
		AssessmentService.updateAllAssessments(messageLogger, Long.valueOf(2), "2007L/34354");
		
		CourseScoreTable table = JCMSCacheFactory.getCache().getCourseScoreTable("2007L/34354");
		int[] index = table.getIndexes().get(0);
		for(int i = 0; i < index.length; i++) {
			ScoreTableEntry[] entries = table.getTableItems().get(index[i]);
			STEStudent u = (STEStudent)entries[0];
			STEScore s = (STEScore)entries[1];
			System.out.println(u.getJmbag()+" "+u.getLastName()+" "+u.getFirstName()+": "+s.getScore()+" "+s.getRank());
		}
		
	}
}
