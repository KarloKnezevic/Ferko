package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.beans.RoomBean;
import hr.fer.zemris.jcms.beans.ToDoTaskBean;
import hr.fer.zemris.jcms.beans.VenueBean;
import hr.fer.zemris.jcms.beans.ext.CourseInstanceBeanExt;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AssessmentFlagTag;
import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.CourseComponentDescriptor;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.KeyValue;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.ToDoTask;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserDescriptor;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.Venue;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.poll.MultiChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.Option;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.model.poll.PollUser;
import hr.fer.zemris.jcms.model.poll.Question;
import hr.fer.zemris.jcms.model.poll.SingleChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.TextQuestion;
import hr.fer.zemris.jcms.parsers.CourseInstanceParser;
import hr.fer.zemris.jcms.parsers.RoomParser;
import hr.fer.zemris.jcms.parsers.ToDoParser;
import hr.fer.zemris.jcms.parsers.VenueParser;
import hr.fer.zemris.jcms.parsers.json.JSONArray;
import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.parsers.json.JSONObject;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.util.StringUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;

public class PrepareServiceDebug {

	public static void prepare() {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {	
			@Override
			public Void executeOperation(EntityManager em) {
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
				prepareToDoTasks(em);
				prepare(em);
				return null;
			}
		});
		
		prepareCourses();

		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				preparePolls(em);
				return null;
			}
		});
	}

	protected static void prepareKeys(EntityManager em) {
		KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "currentSemester");
		if(kv==null) {
			kv = new KeyValue("currentSemester", "2009Z");
			DAOHelperFactory.getDAOHelper().getKeyValueDAO().save(em, kv);
		}
		kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "academicYear");
		if(kv==null) {
			kv = new KeyValue("academicYear", "2009/2010");
			DAOHelperFactory.getDAOHelper().getKeyValueDAO().save(em, kv);
		}
		kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "marketPlace");
		if(kv==null) {
			kv = new KeyValue("marketPlace", "yes");
			DAOHelperFactory.getDAOHelper().getKeyValueDAO().save(em, kv);
		}
	}

	private static void prepareCourses() {
		InputStream is = PrepareServiceDebug.class.getClassLoader().getResourceAsStream("initial-data/courseInstances.txt");
		if(is==null) {
			System.out.println("Ne mogu otvoriti initial-data/courseInstances.txt");
		} else {
			try {
				List<CourseInstanceBeanExt> courseInstances = CourseInstanceParser.parseTabbedFormat(is);
				SynchronizerService.synchronizeCourseInstances("2007L", courseInstances);
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void prepareYearSemester(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		String[][] yearSemesters = new String[][] {
				{"2007Z", "2007/2008", "zimski"},
				{"2007L", "2007/2008", "ljetni"},
				{"2008Z", "2008/2009", "zimski"},
				{"2008L", "2008/2009", "ljetni"},
				{"2009Z", "2009/2010", "zimski"},
				{"2009L", "2009/2010", "ljetni"},
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

		Role studentRole = dh.getRoleDAO().get(em, JCMSSecurityConstants.ROLE_STUDENT);
		Role adminRole = dh.getRoleDAO().get(em, JCMSSecurityConstants.ROLE_ADMIN);
		Role cStaffRole = dh.getRoleDAO().get(em, JCMSSecurityConstants.ROLE_COURSE_STAFF);
		Role asistantRole = dh.getRoleDAO().get(em, JCMSSecurityConstants.ROLE_ASISTENT);
		Role lecturerRole = dh.getRoleDAO().get(em, JCMSSecurityConstants.ROLE_LECTURER);
		AuthType type = dh.getAuthTypeDAO().getByName(em, "local:mysql");
		
		User u = dh.getUserDAO().getUserByUsername(em, "pperic");
		if(u==null) {
			u = new User();
			u.setFirstName("Perić");
			u.setLastName("Pero");
			u.setJmbag("0012345678");
			u.setUsername("pperic");
			UserDescriptor udes = new UserDescriptor();
			udes.setAuthType(type);
			udes.setAuthUsername("pperic");
			udes.setDataValid(true);
			udes.setLocked(false);
			udes.setEmail("pperic@fer.hr");
			udes.setPassword(StringUtil.encodePassword("ppericPass", "SHA"));
			udes.getRoles().add(studentRole);
			udes.setExternalID("1111");
			u.setUserDescriptor(udes);
			dh.getUserDAO().save(em, u);
		}

		u = dh.getUserDAO().getUserByUsername(em, "admin");
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
			udes.setExternalID("2222");
			u.setUserDescriptor(udes);
			dh.getUserDAO().save(em, u);
		}
		
		for(int i = 0; i < 20; i++) {
			String userName = "asistent"+(i+1);
			String lastName = "Asistent"+(i+1);
			String firstName = "Asistent"+(i+1);
			u = dh.getUserDAO().getUserByUsername(em, userName);
			if(u==null) {
				u = new User();
				u.setFirstName(firstName);
				u.setLastName(lastName);
				u.setJmbag(userName);
				u.setUsername(userName);
				UserDescriptor udes = new UserDescriptor();
				udes.setAuthType(type);
				udes.setAuthUsername(userName);
				udes.setDataValid(true);
				udes.setLocked(false);
				udes.setEmail(userName+"@jcms.zemris.fer.hr");
				udes.setPassword(StringUtil.encodePassword(userName+"Pass", "SHA"));
				udes.getRoles().add(cStaffRole);
				udes.getRoles().add(asistantRole);
				u.setUserDescriptor(udes);
				udes.setExternalID("3333"+i);
				dh.getUserDAO().save(em, u);
			}
		}
		
		for(int i = 0; i < 20; i++) {
			String userName = "profa"+(i+1);
			String lastName = "Profa"+(i+1);
			String firstName = "Profa"+(i+1);
			u = dh.getUserDAO().getUserByUsername(em, userName);
			if(u==null) {
				u = new User();
				u.setFirstName(firstName);
				u.setLastName(lastName);
				u.setJmbag(userName);
				u.setUsername(userName);
				UserDescriptor udes = new UserDescriptor();
				udes.setAuthType(type);
				udes.setAuthUsername(userName);
				udes.setDataValid(true);
				udes.setLocked(false);
				udes.setEmail(userName+"@jcms.zemris.fer.hr");
				udes.setPassword(StringUtil.encodePassword(userName+"Pass", "SHA"));
				udes.getRoles().add(cStaffRole);
				udes.getRoles().add(lecturerRole);
				udes.setExternalID("4444"+i);
				u.setUserDescriptor(udes);
				dh.getUserDAO().save(em, u);
			}
		}
	}

	protected static void prepareVenues(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		InputStream is = PrepareServiceDebug.class.getClassLoader().getResourceAsStream("initial-data/venues.txt");
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
		InputStream is = PrepareServiceDebug.class.getClassLoader().getResourceAsStream("initial-data/rooms.txt");
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
	
	protected static void prepareToDoTasks(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		InputStream is = PrepareServiceDebug.class.getClassLoader().getResourceAsStream("initial-data/todoTasks.txt");
		if(is==null) {
			System.out.println("Ne mogu otvoriti initial-data/todoTasks.txt");
		} else {
			try {
				List<ToDoTaskBean> todoTasks = ToDoParser.parseTabbedFormat(is);
//				//Popunjavanje mape prema virtualnim ID-evima
				
				Map<String, ToDoTaskBean> map = new HashMap<String, ToDoTaskBean>();
				for(ToDoTaskBean b : todoTasks){
					map.put(b.getVirtualID(), b);
				}
				
				Map<String, ToDoTask> map2 = new HashMap<String, ToDoTask>();
				for(ToDoTaskBean b : todoTasks){
					ToDoTask task = new ToDoTask();
					task.setDeadline(b.getDeadline());
					task.setDescription(b.getDescription());
					task.setGarbageCollectable(b.getGarbageCollectable());
					User owner = dh.getUserDAO().getUserByUsername(em, b.getOwnerUserName());
					task.setOwner(owner);
					User realizer = dh.getUserDAO().getUserByUsername(em, b.getRealizerUserName());
					task.setRealizer(realizer);
					task.setPriority(b.getPriority());
					task.setStatus(b.getStatus());
					task.setTitle(b.getTitle());
					ToDoTask p = map2.get(b.getParentTask());
					task.setParentTask(p);
					em.persist(task);
					map2.put(b.getVirtualID(), task);
				}
//				#
//				#Map<String,ToDo> map2 = ...
//				#for(ToDoBean b : listaBeanova) {
//				#  Todo t = new ToDo();
//				#  // popunite ga iz beana
//				#  // recimo da je parent u beanu "T2"
//				#  ToDo p = map2.get(b.getParent());
//				#  t.setParent(p);
//				#  em.persist(t);
//				#  map2.put(b.getKljuc(),t);
//				#}
				
				

			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void preparePolls(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		InputStream is = PrepareServiceDebug.class.getClassLoader().getResourceAsStream("initial-data/ankete.json");
		if(is==null) {
			System.out.println("Ne mogu otvoriti initial-data/ankete.json");
		} else {
			StringBuilder json = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			try {
				while((line = reader.readLine()) != null) {
					json.append(line).append("\n");
				}
			} catch (IOException e) {
				System.out.println("Greška u čitanju initial-data/ankete.json!");
				return;
			}
			try {
				reader.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			JSONObject json_polls=null;
			try {
				json_polls = new JSONObject(json.toString());
				JSONArray json_polls_array = json_polls.getJSONArray("polls");
				for(int i = 0; i<json_polls_array.length(); i++) {
					JSONObject json_poll = (JSONObject)json_polls_array.get(i);
					String title = json_poll.getString("title");
					String description = json_poll.getString("description");
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy.");
					Date startDate = sdf.parse(json_poll.getString("startDate"));
					Date endDate = sdf.parse(json_poll.getString("endDate"));
					Boolean viewablePublic = json_poll.getBoolean("public");
					if(dh.getPollDAO().getPollsWithName(em, title).size()!=0) continue;
					Poll poll = new Poll(title, description, startDate, endDate, viewablePublic);
					
					JSONArray json_questions = (JSONArray)json_poll.get("questions");
					for(int k = 0; k<json_questions.length(); k++) {
						JSONObject json_question = json_questions.getJSONObject(k);
						String questionText = json_question.getString("question");
						String validation = json_question.optString("validation");
						String type = json_question.getString("type");
						Question question = null;
						if("text".equals(type)) {
							question = new TextQuestion(questionText, k);
						} else {
							if("singlechoice".equals(type)) question = new SingleChoiceQuestion(questionText, k);
							if("multichoice".equals(type)) question = new MultiChoiceQuestion(questionText, k);
							JSONArray json_options = json_question.getJSONArray("options");
							for(int j = 0; j<json_options.length(); j++) {
								Option option = new Option(json_options.getString(j), j);
								if("singlechoice".equals(type)) ((SingleChoiceQuestion)question).getOptions().add(option);
								if("multichoice".equals(type)) ((MultiChoiceQuestion)question).getOptions().add(option);
								option.setQuestion(question);
							}
						}
						if(validation != null) question.setValidation(validation);
						poll.getQuestions().add(question);
						question.setPoll(poll);
					}
					dh.getPollDAO().save(em, poll);	
					
					List<Group> groups = dh.getGroupDAO().findLectureSubgroups(em, "2007L/34285");
					for(Group group : groups) {
						Set<UserGroup> users = group.getUsers();
						for(UserGroup user : users) {
							PollUser pollUser = new PollUser();
							pollUser.setUser(user.getUser());
							pollUser.setGroup(group);
							pollUser.setPoll(poll);
							pollUser.setAnswered(false);
							dh.getPollDAO().savePollUser(em, pollUser);
						}
					}
				}
			} catch (JSONException e) {
				System.out.println("Greška u parsiranju ankete.json");
				e.printStackTrace();
			} catch (ParseException e) {
				System.out.println("Nepravilan format datuma u ankete.json");
				e.printStackTrace();
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
