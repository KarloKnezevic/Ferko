package hr.fer.zemris.tests;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.ext.AssessmentScheduleBean;
import hr.fer.zemris.jcms.beans.ext.GroupScheduleBean;
import hr.fer.zemris.jcms.beans.ext.ISVUFileItemBean;
import hr.fer.zemris.jcms.beans.ext.LabScheduleBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.Venue;
import hr.fer.zemris.jcms.parsers.AssessmentScheduleParser;
import hr.fer.zemris.jcms.parsers.GroupScheduleParser;
import hr.fer.zemris.jcms.parsers.ISVUFileParser;
import hr.fer.zemris.jcms.parsers.LabScheduleTextListParser;
import hr.fer.zemris.jcms.service.AssessmentEventService;
import hr.fer.zemris.jcms.service.PrepareServiceDebug;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.service2.sysadmin.ComponentScheduleSyncService;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BasicDBTesting {

	private EntityManagerFactory emf;
	
	@Before
	public void setUp() throws Exception {
//		emf = Persistence.createEntityManagerFactory("jcmstestdb");
		emf = Persistence.createEntityManagerFactory("jcmsdb");
		PersistenceUtil.initSingleTon(emf);
	}

	@After
	public void tearDown() throws Exception {
		PersistenceUtil.clearSingleTon();
		emf.close();
	}

	@Test
	public void testInsertAuthType() {
		final String[][] authTypes = new String[][] {
				{"local:mysql", "Lokalno iz baze"},
				{"pop3://pinus.cc.fer.hr", "POP3 protokolom preko pinus.cc.fer.hr"},
				{"ferweb://https://www.fer.hr/xmlrpc/xr_auth.php", "XML-RPC-om i SSL-om preko FERWeb-a"}
			};
		final DAOHelper dh = DAOHelperFactory.getDAOHelper();

		JCMSSettings.init();
		PrepareServiceDebug.prepare();
		
		final AuthType[] elems = new AuthType[authTypes.length];
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				int i = 0;
				for(String[] authType : authTypes) {
					elems[i++] = dh.getAuthTypeDAO().getByName(em, authType[0]);
				}
				return null;
			}
		});
		for(int i = 0; i < elems.length; i++) {
			assertNotNull("AuthType "+elems[i].getName()+" nije dohvaćen.", elems[i]);
			assertEquals("Provjera naziva.", authTypes[i][0], elems[i].getName());
			assertEquals("Provjera opisa.", authTypes[i][1], elems[i].getDescription());
		}
		
		final Venue[] venues = new Venue[1];
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				venues[0] = dh.getVenueDAO().get(em, "FER");
				return null;
			}
		});
		assertNotNull("Venue FER nije dohvaćen.", venues[0]);
		assertEquals("Provjera naziva.", "FER", venues[0].getShortName());

		final Room[] rooms = new Room[2];
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				rooms[0] = dh.getRoomDAO().get(em, "FER/D346");
				rooms[1] = dh.getRoomDAO().get(em, "FER", "D1");
				return null;
			}
		});
		assertNotNull("Room FER/D346 nije dohvaćen.", rooms[0]);
		assertEquals("Provjera identifikatora.", "FER/D346", rooms[0].getId());
		assertEquals("Provjera kratkog imena.", "D346", rooms[0].getShortName());
		assertNotNull("Room FER/D1 nije dohvaćen.", rooms[1]);
		assertEquals("Provjera identifikatora.", "FER/D1", rooms[1].getId());
		assertEquals("Provjera kratkog imena.", "D1", rooms[1].getShortName());
		
		InputStream is = null;
		try {
			//is = new BufferedInputStream(new FileInputStream("various-files/isvu-sol-rand-lossy.txt"));
			is = new BufferedInputStream(new FileInputStream("various-files/noviPodatci/isvuUTF8.txt"));
		} catch(Exception ignorable) {
		}
		if(is==null) {
			System.out.println("Ne mogu otvoriti various-files/noviPodatci/isvuUTF8.txt");
		} else {
			System.out.println("*************************************************************************************");
			System.out.println("Zapocinje sinkronizacija kolegija i studenata: various-files/noviPodatci/isvuUTF8.txt");
			System.out.println("*************************************************************************************");
			try {
				List<ISVUFileItemBean> items = ISVUFileParser.parseTabbedFormat(is);
				SynchronizerService.synchronizeISVUFile("2009Z", elems[0].getId(), items);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		is = null;
		try {
			is = new BufferedInputStream(new FileInputStream("various-files/noviPodatci/satnica.txt"));
		} catch(Exception ignorable) {
		}
		if(is==null) {
			System.out.println("Ne mogu otvoriti various-files/noviPodatci/satnica.txt");
		} else {
			System.out.println("*************************************************************************************");
			System.out.println("Zapocinje sinkronizacija satnice kolegija: various-files/noviPodatci/satnica.txt");
			System.out.println("*************************************************************************************");
			try {
				List<GroupScheduleBean> items = GroupScheduleParser.parseTabbedFormat(is);
				SynchronizerService.synchronizeCourseLectureSchedule("2009Z", items);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		
		String[] imena = new String[] {"03","04","05","06","07","08","09","10","11","12","13"};
		int brojac = 0;
		for(String ime : imena) {
			brojac++;
			is = null;
			try {
				is = new BufferedInputStream(new FileInputStream("various-files/noviPodatci/rasporedC"+ime+"_tl.txt"));
			} catch(Exception ignorable) {
			}
			if(is==null) {
				System.out.println("Ne mogu otvoriti various-files/noviPodatci/rasporedC"+ime+"_tl.txt");
			} else {
				System.out.println("*********************************************************************************************");
				System.out.println("["+brojac+"/"+imena.length+"] Zapocinje sinkronizacija rasporeda labosa: various-files/noviPodatci/rasporedC"+ime+"_tl.txt");
				System.out.println("*********************************************************************************************");
				try {
					List<LabScheduleBean> items = LabScheduleTextListParser.parseTabbedFormat(is);
					ComponentScheduleSyncService.synchronizeLabSchedule("2009Z", items);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		imena = new String[] {"raspored-final-mi1.txt","raspored-final-mi2.txt","raspored-final-zi.txt","raspored-final-pzi.txt"};
		String[] tagovi = new String[] {"MI1", "MI2", "ZI", "PZI"};
		brojac = 0;
		for(String ime : imena) {
			brojac++;
			is = null;
			try {
				is = new BufferedInputStream(new FileInputStream("various-files/noviPodatci/"+ime));
			} catch(Exception ignorable) {
			}
			if(is==null) {
				System.out.println("Ne mogu otvoriti various-files/noviPodatci/"+ime);
			} else {
				System.out.println("*********************************************************************************************");
				System.out.println("["+brojac+"/"+imena.length+"] Zapocinje sinkronizacija rasporeda ispita: various-files/noviPodatci/"+ime);
				System.out.println("*********************************************************************************************");
				try {
					List<AssessmentScheduleBean> beanList = AssessmentScheduleParser.parseTabbedFormat(new BufferedReader(new InputStreamReader(new BufferedInputStream(is),"UTF-8")));
					AssessmentEventService.addAssessmentEvents(JCMSSettings.getSettings().getI18nLogger(),beanList,"2009Z",tagovi[brojac-1]);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		File f = new File("various-files/course-isvu-data.zip");
		if(!f.exists()) {
			System.out.println("Ne mogu otvoriti "+f.getAbsolutePath());
		} else {
			SynchronizerService.synchronizeCourseIsvuDataUnsecure("2007L", f);
		}
	}
	
	public static void main(String[] args) throws Exception {
		BasicDBTesting b = new BasicDBTesting();
		b.setUp();
		b.testInsertAuthType();
		b.tearDown();
	}
}
