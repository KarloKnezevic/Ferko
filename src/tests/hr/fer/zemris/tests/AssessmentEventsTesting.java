package hr.fer.zemris.tests;

import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.service.PrepareService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AssessmentEventsTesting {

	private EntityManagerFactory emf;
	
	@Before
	public void setUp() throws Exception {
		//emf = Persistence.createEntityManagerFactory("jcmstestdb");
		emf = Persistence.createEntityManagerFactory("jcmsdb");
		PersistenceUtil.initSingleTon(emf);
	}

	@After
	public void tearDown() throws Exception {
		PersistenceUtil.clearSingleTon();
		emf.close();
	}

	@Test
	public void testAssessmentInserting() {

		PrepareService.prepare();
		
		Reader is = null;
		try {
			is = new BufferedReader(new InputStreamReader(new FileInputStream("various-files/raspored_zi.txt"),"UTF-8"));
		}
		catch (Exception ignorable) {
		}
		if (is == null)
			System.out.println("Greska");
		
//		try {
//			final List <AssessmentScheduleBean> items = AssessmentScheduleParser.parseTabbedFormat(is);
//			PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
//				
//				@Override
//				public Void executeOperation(EntityManager em) {
//					AssessmentEventService.addAssessmentEvents(em,items, "2007L", "MI2", "admin");
//					return null;
//				}
//			
//			});
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
	}
}
