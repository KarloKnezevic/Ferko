package hr.fer.zemris.jcms.service;

/*
 * VAZNO: ovaj razred vise ne mijenjati u debug svrhe. Napravljen je razred PrepareServiceDebug koji se od sada poziva
 *        iz testova i tamo stavite kod za inicijalno punjenje podataka u debug svrhe.
 *        Ovaj razred sadrzi sve sto je potrebno da se pokrene inicijalna cista verzija sustava i poziva se iskljucivo preko weba
 *        iz akcije Prepare.
 * */

import hr.fer.zemris.jcms.beans.RoomBean;
import hr.fer.zemris.jcms.beans.VenueBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AssessmentFlagTag;
import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.CourseComponentDescriptor;
import hr.fer.zemris.jcms.model.KeyValue;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserDescriptor;
import hr.fer.zemris.jcms.model.Venue;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.parsers.RoomParser;
import hr.fer.zemris.jcms.parsers.VenueParser;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;

public class PrepareService {

	public static void prepare() {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {	
			@Override
			public Void executeOperation(EntityManager em) {
				KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "systemInstalled");
				if(kv!=null && kv.getValue()!=null && "true".equals(kv.getValue())) {
					// Sustav je vec instaliran. Akcija ne radi nista!
					return null;
				}
				prepareKeys(em);
				prepareYearSemester(em);
				prepareAuthTypes(em);
				prepareRoles(em);
				prepareUsers(em);
				prepareVenues(em);
				prepareRooms(em);
				prepareAssessmentTags(em);
				prepareAssessmentFlagTags(em);
				prepareCourseComponentDescriptors(em);
				prepare(em);
				kv = new KeyValue("systemInstalled", "true");
				DAOHelperFactory.getDAOHelper().getKeyValueDAO().save(em, kv);
				return null;
			}
		});
	}

	protected static void prepareKeys(EntityManager em) {
		KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "currentSemester");
		if(kv==null) {
			kv = new KeyValue("currentSemester", "2008Z");
			DAOHelperFactory.getDAOHelper().getKeyValueDAO().save(em, kv);
		}
		kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "academicYear");
		if(kv==null) {
			kv = new KeyValue("academicYear", "2008/2009");
			DAOHelperFactory.getDAOHelper().getKeyValueDAO().save(em, kv);
		}
		kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "marketPlace");
		if(kv==null) {
			kv = new KeyValue("marketPlace", "yes");
			DAOHelperFactory.getDAOHelper().getKeyValueDAO().save(em, kv);
		}
		kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "AdminMessage");
		if(kv==null) {
			kv = new KeyValue("AdminMessage", "Sustav je uspješno instaliran.");
			DAOHelperFactory.getDAOHelper().getKeyValueDAO().save(em, kv);
		}
	}

	private static void prepareYearSemester(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		String[][] yearSemesters = new String[][] {
				{"2007Z", "2007/2008", "zimski"},
				{"2007L", "2007/2008", "ljetni"},
				{"2008Z", "2008/2009", "zimski"},
				{"2008L", "2008/2009", "ljetni"}
		};
		for(String[] yearSemester : yearSemesters) {
			YearSemester ys = dh.getYearSemesterDAO().get(em, yearSemester[0]);
			if(ys == null) {
				ys = new YearSemester();
				ys.setId(yearSemester[0]);
				ys.setAcademicYear(yearSemester[1]);
				ys.setSemester(yearSemester[2]);
				dh.getYearSemesterDAO().save(em, ys);
			}
		}
	}
	
	private static void prepareAssessmentTags(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper(); 
		String[][] aTags = new String[][] {
				{"MI1", "Prvi međuispit"},
				{"MI2", "Drugi međuispit"},
				{"MI1N", "Nadoknada prvog međuispita"},
				{"MI2N", "Nadoknada drugog međuispita"},
				{"ZI", "Završni ispit"},
				{"PZI", "Ponovljeni završni ispit"}
			};
		for(String[] aTag : aTags) {
			AssessmentTag tag = dh.getAssessmentTagDAO().getByShortName(em, aTag[0]);
			if(tag==null) {
				tag = new AssessmentTag();
				tag.setActive(true);
				tag.setShortName(aTag[0]);
				tag.setName(aTag[1]);
				dh.getAssessmentTagDAO().save(em, tag);
			}
		}
	}
	
	private static void prepareAssessmentFlagTags(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper(); 
		String[][] aTags = new String[][] {
				{"PROLAZ", "Ostvaren prolaz na kolegiju"}
			};
		for(String[] aTag : aTags) {
			AssessmentFlagTag tag = dh.getAssessmentFlagTagDAO().getByShortName(em, aTag[0]);
			if(tag==null) {
				tag = new AssessmentFlagTag();
				tag.setActive(true);
				tag.setShortName(aTag[0]);
				tag.setName(aTag[1]);
				dh.getAssessmentFlagTagDAO().save(em, tag);
			}
		}
	}
	
	private static void prepareAuthTypes(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper(); 
		String[][] authTypes = new String[][] {
				{"local:mysql", "Lokalno iz baze"},
				{"pop3://pinus.cc.fer.hr", "POP3 protokolom preko pinus.cc.fer.hr"},
				{"ferweb://https://www.fer.hr/xmlrpc/xr_auth.php", "XML-RPC-om i SSL-om preko FERWeb-a"}
			};
		for(String[] authType : authTypes) {
			AuthType type = dh.getAuthTypeDAO().getByName(em, authType[0]);
			if(type==null) {
				type = new AuthType(authType[0], authType[1]);
				dh.getAuthTypeDAO().save(em, type);
			}
		}
	}
	
	private static void prepareRoles(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper(); 
		String[][] roles = new String[][] {
			{JCMSSecurityConstants.ROLE_ADMIN, "Administrator"},
			{JCMSSecurityConstants.ROLE_STUDENT, "Student"},
			{JCMSSecurityConstants.ROLE_ASISTENT, "Assistant"},
			{JCMSSecurityConstants.ROLE_LECTURER, "Lecturer"},
			{JCMSSecurityConstants.ROLE_COURSE_STAFF, "Course staff"}
		};
		for(String[] role : roles) {
			Role r = dh.getRoleDAO().get(em, role[0]);
			if(r==null) {
				r = new Role();
				r.setName(role[0]);
				r.setDescription(role[1]);
				dh.getRoleDAO().save(em, r);
			}
 		}
	}
	
	private static void prepareUsers(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		Role adminRole = dh.getRoleDAO().get(em, JCMSSecurityConstants.ROLE_ADMIN);
		AuthType type = dh.getAuthTypeDAO().getByName(em, "local:mysql");
		
		Random rnd = new Random();
		User u = dh.getUserDAO().getUserByUsername(em, "admin");
		if(u==null) {
			u = new User();
			u.setFirstName("Administrator");
			u.setLastName("Administrator");
			u.setJmbag("A0000000000");
			u.setUsername("admin");
			UserDescriptor udes = new UserDescriptor();
			udes.setAuthType(type);
			udes.setAuthUsername("admin");
			udes.setDataValid(true);
			udes.setLocked(false);
			udes.setEmail("admin@jcms.zemris.fer.hr");
			udes.setPassword(StringUtil.encodePassword("adminPass", "SHA"));
			udes.getRoles().add(adminRole);
			u.setUserDescriptor(udes);
			udes.setExternalID(SynchronizerService.createExternalID(rnd));
			dh.getUserDAO().save(em, u);
		}
	}

	protected static void prepareVenues(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		InputStream is = PrepareService.class.getClassLoader().getResourceAsStream("initial-data/venues.txt");
		if(is==null) {
			System.out.println("Ne mogu otvoriti initial-data/venues.txt");
		} else {
			try {
				List<VenueBean> venues = VenueParser.parseTabbedFormat(is);
				for(VenueBean venueBean : venues) {
					Venue venue = dh.getVenueDAO().get(em, venueBean.getShortName());
					if(venue==null) {
						venue = new Venue();
						venue.setShortName(venueBean.getShortName());
						venue.setName(venueBean.getName());
						venue.setAddress(venueBean.getAddress());
						venue.setLocator(venueBean.getLocator());
						dh.getVenueDAO().save(em, venue);
					}
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	protected static void prepareRooms(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		InputStream is = PrepareService.class.getClassLoader().getResourceAsStream("initial-data/rooms.txt");
		if(is==null) {
			System.out.println("Ne mogu otvoriti initial-data/rooms.txt");
		} else {
			try {
				List<RoomBean> rooms = RoomParser.parseTabbedFormat(is);
				for(RoomBean roomBean : rooms) {
					Room room = dh.getRoomDAO().get(em, roomBean.getId());
					if(room==null) {
						room = new Room();
						room.setId(roomBean.getId());
						room.setShortName(roomBean.getShortName());
						room.setName(roomBean.getName());
						room.setLocator(roomBean.getLocator());
						room.setLecturePlaces(roomBean.getLecturePlaces());
						room.setExercisePlaces(roomBean.getExercisePlaces());
						room.setAssessmentPlaces(roomBean.getAssessmentPlaces());
						room.setAssessmentAssistants(roomBean.getAssessmentAssistants());
						room.setPublicRoom(roomBean.getPublicRoom());
						room.setVenue(dh.getVenueDAO().get(em, roomBean.getVenueShortName()));
						dh.getRoomDAO().save(em, room);
					}
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void prepareCourseComponentDescriptors(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		List<CourseComponentDescriptor> list = dh.getCourseComponentDAO().listDescriptors(em);
		Set<String> dbDescriptors = new HashSet<String>();
		for (CourseComponentDescriptor ccd : list) {
			dbDescriptors.add(ccd.getShortName());
		}
		
		if (!dbDescriptors.contains("LAB")) {
			CourseComponentDescriptor ccd = new CourseComponentDescriptor();
			ccd.setGroupRoot("1");
			ccd.setName("Laboratorijske vježbe");
			ccd.setPositionalName("laboratorijska vježba");
			ccd.setShortName("LAB");
			dh.getCourseComponentDAO().save(em, ccd);
		}
		
		if (!dbDescriptors.contains("ZAD")) {
			CourseComponentDescriptor ccd = new CourseComponentDescriptor();
			ccd.setGroupRoot("2");
			ccd.setName("Domaće zadaće");
			ccd.setPositionalName("domaća zadaća");
			ccd.setShortName("ZAD");
			dh.getCourseComponentDAO().save(em, ccd);
		}
		
		if (!dbDescriptors.contains("SEM")) {
			CourseComponentDescriptor ccd = new CourseComponentDescriptor();
			ccd.setGroupRoot("5");
			ccd.setName("Seminarski radovi");
			ccd.setPositionalName("seminarski rad");
			ccd.setShortName("SEM");
			dh.getCourseComponentDAO().save(em, ccd);
		}
	}

	private static void prepare(EntityManager em) {
		// DAOHelper dh = DAOHelperFactory.getDAOHelper(); 
	}
}
