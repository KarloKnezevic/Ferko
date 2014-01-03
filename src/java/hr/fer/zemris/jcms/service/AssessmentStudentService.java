package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.beans.ext.UserRoomBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentAssistantSchedule;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.UserSpecificEvent;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.web.actions.data.BaseAssessment;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

/**
 * Service koji ima sve usluge vezane za raspored studenata
 * @author TOMISLAV
 *
 */

@Deprecated
public class AssessmentStudentService {
	
	@Deprecated
	public static void getStudents(final BaseAssessment data, final String assessmentID, final String sure,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				boolean doit = false;
				try {
					doit = Boolean.valueOf(sure);
				} catch (Exception ignorable) {}
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				//napunimo s podacima
				if (!BasicBrowsing.fillAssessment(em, data, assessmentID))
					return null;
				data.setCourseInstance(data.getAssessment().getCourseInstance());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Assessment a = data.getAssessment();
				AssessmentFlag flag = a.getAssessmentFlag();
				
				//dohvacamo sve korisnike koji smiju na provjeru
				Collection<User> users = null;
				if (flag==null)
					users = dh.getUserDAO().listUsersOnCourseInstance(em, a.getCourseInstance().getId());
				else
					users = dh.getAssessmentDAO().listUsersWithFlagUp(em, flag);
				
				//dohvacamo grupu assessmenta, ako je nema stvaramo je
				if (a.getGroup() == null)
					createNewRootGroup(dh,em,a);
				
				//radimo sinkronizaciju kolekcije i trenutnog stanja u bazi
				if (!synchronizeGroupUsers(dh,em,a,users,doit)) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.studentsSuccessfullySynchronized"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				
				return null;
			}
		
		});
		
	}
	
	@Deprecated
	public static void makeStudentSchedule(final BaseAssessment data, final String assessmentID, 
			final String type, final String sure, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				boolean arranged = false;				
				
				if (!BasicBrowsing.fillAssessment(em, data, assessmentID))
					return null;
				data.setCourseInstance(data.getAssessment().getCourseInstance());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Assessment a = data.getAssessment();
				
				int userNum = getUserNum(dh, em, a);
				
				if (userNum==0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noStudents"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				if (userNum>getRoomCapacity(a)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.notEnoughCapacity"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				if (userNum != a.getGroup().getUsers().size()) 
					arranged = true;
				
				boolean doit = false;
				try {
					doit = Boolean.valueOf(sure);
				} catch (Exception ignorable) {}
				
				if (arranged && !doit) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				else if (arranged)
					clearAssessmentSchedule(dh, em, a);
				
				List<AssessmentRoom> roomList  = new ArrayList<AssessmentRoom>(a.getRooms());
				//sortiramo sobe po imenu
				Collections.sort(roomList, new Comparator<AssessmentRoom>() {
					@Override
					public int compare(AssessmentRoom o1, AssessmentRoom o2) {
						return o1.getRoom().getName().compareTo(o2.getRoom().getName());
					}
				});
				
				//studenti koje rasporedjujemo
				List<UserGroup> userList = new ArrayList<UserGroup>(a.getGroup().getUsers());
				
				//provjeravamo kakav raspored studenata korisnik zeli i pripremamo sortiranje
				
				final Collator myCollator = Collator.getInstance(new Locale("hr"));
				Comparator<UserGroup> myComparator = new Comparator<UserGroup>() {
					@Override
					public int compare(UserGroup o1, UserGroup o2) {
						int r = myCollator.compare(o1.getUser().getLastName(),o2.getUser().getLastName());
						if (r == 0)
							return myCollator.compare(o1.getUser().getFirstName(), o2.getUser().getFirstName());
						return r;
					}
				};
				
				if ("random".equals(type)) {
					//radimo random raspored
					Collections.shuffle(userList);
				}
				else {
					//abecedno sortiranje usera po prezimenu, pa po imenu
					Collections.sort(userList, myComparator);
				}
				
				//idemo napunit sobe
				Iterator<UserGroup> it = userList.iterator();
				for (AssessmentRoom ar : roomList) {
					if (ar.isTaken()) {
						if (ar.getGroup() == null)
							createNewRoomGroup(dh,em,ar,a.getGroup());
						Group g = ar.getGroup();
						int i=0, capacity = ar.getCapacity();
						
						//stavljamo prvo sve u tmpListu koju cemo sortirat
						List<UserGroup> tmpList = new ArrayList<UserGroup>(capacity);
						while (i<capacity && it.hasNext()) {
							tmpList.add(it.next());
							++i;
						}
						//sortiramo po prezimenu i dodajemo poziciju unutar grupe
						Collections.sort(tmpList, myComparator);
						i = 0;
						for (UserGroup ug : tmpList) {
							++i;
							a.getGroup().getUsers().remove(ug);
							ug.setGroup(g);
							g.getUsers().add(ug);
							ug.setPosition(i);
						}
						
						if (!it.hasNext())
							break;
					}
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.studentsScheduled"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	@Deprecated
	public static void broadcastEvents(final BaseAssessment data, final String assessmentID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				//napunimo s podacima
				if (!BasicBrowsing.fillAssessment(em, data, assessmentID))
					return null;
				data.setCourseInstance(data.getAssessment().getCourseInstance());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Assessment a = data.getAssessment();
				
				int userNum = getUserNum(dh, em, a);
				
				if (userNum == a.getGroup().getUsers().size() || userNum == 0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noStudentSchedule"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
								
				if (a.getEvent() == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRootEvent"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				for (AssessmentRoom ar : a.getRooms()) {
					if (ar.isTaken() && ar.getGroup()!=null) {
						if (ar.getGroup().getEvents() == null || ar.getGroup().getEvents().size()==0) {
							GroupWideEvent gwe = new GroupWideEvent();
							
							gwe.setDuration(a.getEvent().getDuration());
							gwe.setStart(a.getEvent().getStart());
							gwe.getGroups().add(ar.getGroup());
							gwe.setRoom(ar.getRoom());
							gwe.setTitle(a.getEvent().getTitle());
							gwe.setStrength(a.getEvent().getStrength());
							gwe.setIssuer(data.getCurrentUser());
							gwe.setContext("a:"+a.getId());
							ar.getGroup().getEvents().add(gwe);
							dh.getEventDAO().save(em, gwe);
						}
					}
				}
				
				//radimo evente koji ce obavijestiti asistente
				for (AssessmentAssistantSchedule aas : a.getAssistantSchedule()) {
					if (aas.getRoom() != null) {
						UserSpecificEvent use = aas.getRoom().getUserEvent();
						if (use == null) {
							use = new UserSpecificEvent();
							
							use.setDuration(a.getEvent().getDuration());
							use.setStart(a.getEvent().getStart());
							use.setRoom(aas.getRoom().getRoom());
							use.setTitle(a.getEvent().getTitle());
							use.setStrength(a.getEvent().getStrength());
							use.setIssuer(data.getCurrentUser());
							
							aas.getRoom().setUserEvent(use);
							dh.getEventDAO().save(em, use);
						}
						
						use.getUsers().add(aas.getUser());
					}
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.eventsBroadcastSuccessful"));
				return null;
			}
		});
	}
	
	@Deprecated
	public static void importSchedule(final BaseAssessment data, final List<UserRoomBean> beanList, 
			final String assessmentID, final String sure, final Long userID, final String venueShortName) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				//napunimo s podacima
				if (!BasicBrowsing.fillAssessment(em, data, assessmentID))
					return null;
				data.setCourseInstance(data.getAssessment().getCourseInstance());
				Assessment a = data.getAssessment();
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//ako je edit samo smo napunili podatke u data objekt i vracamo se natrag
				if ("edit".equals(sure)) {
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				boolean doit = false;
				try {
					doit = Boolean.valueOf(sure);
				} catch (Exception ignorable) {}
				
				
				if (a.getRooms() == null || a.getRooms().size() == 0) {
					if (!AssessmentRoomService.synchronizeRooms(dh, em, a, venueShortName)) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRooms"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				
				//punimo mapu usera na predmetu
				Map<String, User> userMap = UserUtil.mapUserByJmbag(
						dh.getUserDAO().listUsersOnCourseInstance(em, a.getCourseInstance().getId())
					);
				
				//punimo mapu roomova
				Map<String, AssessmentRoom> roomMap = new HashMap<String, AssessmentRoom>(a.getRooms().size());
				for (AssessmentRoom ar : a.getRooms()) {
					roomMap.put(ar.getRoom().getShortName(), ar);
				}
				
				//set u kojeg cemo spremiti sve dobivene usere
				Set<User> userSet = new HashSet<User>();
				
				//provjera podataka
				for (UserRoomBean urb : beanList) {
					if (userMap.get(urb.getJmbag())==null) {
						String[] param = new String[1];
						param[0] = urb.getJmbag();
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchStudent",param));
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					userSet.add(userMap.get(urb.getJmbag()));
					
					if (roomMap.get(urb.getShortRoomName())==null) {
						String[] param = new String[1];
						param[0] = urb.getShortRoomName();
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchRoom",param));
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
				}
				
				//dohvacamo grupu assessmenta, ako je nema stvaramo je
				if (a.getGroup() == null)
					createNewRootGroup(dh,em,a);
				 
				//idemo sinkronizirati popis korisnika koji smo dobili
				if (!synchronizeGroupUsers(dh,em,a,userSet,doit)) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				//idemo napraviti raspored po sobama koje smo dobili
				
				//TODO: sto s rezervacijom
				//odrezerviramo stare sobe i pripremamo mapu pozicija
				Map<String, Integer> roomPositionMap = new HashMap<String, Integer>(a.getRooms().size());
				for (AssessmentRoom ar : a.getRooms()) {
					ar.setTaken(false);
					roomPositionMap.put(ar.getRoom().getShortName(), new Integer(0));
				}
				
				//onda radimo raspored
				Map<String, UserGroup> userGroupbyJmbagMap = UserUtil.mapUserGroupByJmbag(a.getGroup().getUsers());
				
				for (UserRoomBean urb : beanList) {
					
					AssessmentRoom ar = roomMap.get(urb.getShortRoomName());
					if (ar.getGroup() == null)
						createNewRoomGroup(dh, em, ar, a.getGroup());
					if (!ar.isTaken())
						ar.setTaken(true);
					
					int x = urb.getPosition();
					if (x==-1) {
						x = roomPositionMap.get(urb.getShortRoomName())+1;
						roomPositionMap.put(urb.getShortRoomName(), x);
					}
					
					UserGroup ug = userGroupbyJmbagMap.get(urb.getJmbag());
					ug.getGroup().getUsers().remove(ug);
					ug.setPosition(x);
					ug.setGroup(ar.getGroup());
					ar.getGroup().getUsers().add(ug);
				}
				
								
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.importSuccessful"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	///////////////////////////
	//Pomocne privatne metode//
	///////////////////////////
	
	/**
	 * Metoda koja sinkronizira predanu kolekciju studenata s vrsnom grupom studenata nekog Assessmenta.
	 * Kolekcija studenata predstavlja studente koji smiju pristupiti Assessmentu.
	 * Ukoliko raspored studenata vec postoji metoda ce ga ponistiti (vratiti ce sve studente koji smiju
	 * pristupiti assessmentu u vrsnu grupu assessmenta)
	 * @param dh
	 * @param em
	 * @param g
	 * @param users
	 * @param doit
	 */
	@Deprecated
	private static boolean synchronizeGroupUsers(DAOHelper dh, EntityManager em, Assessment a, Collection<User> users, boolean doit) {
		
		// zastavica da li postoje vec rasporedjeni studenti
		boolean arranged = false;
		Group g = a.getGroup();
		
		//dohvatimo broj korisnika vrsne grupe
		Set<UserGroup> ugSet = g.getUsers();
		
		//dohvacamo sve korisnike
		List<UserGroup> currentUsers = dh.getUserDAO().findForGroupAndSubGroups(em,
				g.getCompositeCourseID(), g.getRelativePath()+"/%", g.getRelativePath());
		
		//postavljamo zastavicu
		if (currentUsers.size() > ugSet.size())
			arranged = true;
		
		//ako vec postoji raspored i zastavica doit nije podignuta izlazimo van
		if (arranged && !doit)
			return false;
		
		//skup koji na pocetku sadrzi sve studente u trenutnom assessmentu
		//na kraju ce sadrzavati samo one koji ne mogu izaci na assessment
		Set<UserGroup> dismissedUserSet = new HashSet<UserGroup>(currentUsers);
		Map<String, UserGroup> userGroupbyJmbagMap = UserUtil.mapUserGroupByJmbag(currentUsers);
	
		//dodajemo nove korisnike i pripremamo dismissedUserSet za brisanje viska
		for (User u : users) {
			UserGroup ug = userGroupbyJmbagMap.get(u.getJmbag());
			if (ug == null) {
				ug = new UserGroup();
				ug.setUser(u);
				ug.setGroup(g);
				ugSet.add(ug);
				dh.getUserGroupDAO().save(em, ug);
			}
			else  {
				dismissedUserSet.remove(ug);
				if (ug.getGroup() != g) {
					ug.getGroup().getUsers().remove(ug);
					ug.setGroup(g);
					g.getUsers().add(ug);
				}
			}
		}
		for (UserGroup ug : dismissedUserSet) {
			ug.getGroup().getUsers().remove(ug);
			dh.getUserGroupDAO().remove(em, ug);
		}
		if (arranged)
			clearAssessmentSchedule(dh,em,a);
		
		return true;
	}
	
	/**
	 * Metoda koja stvara jednu vrsnu grupu Assessmenta
	 * @param dh
	 * @param em
	 * @param a
	 */
	@Deprecated
	private static void createNewRootGroup(DAOHelper dh, EntityManager em, Assessment a) {
		
		a.getCourseInstance().getPrimaryGroup();
		Group primaryGroup = dh.getGroupDAO().get(em, a.getCourseInstance().getId(), "4");
		
		if (primaryGroup == null) {
			primaryGroup = new Group();
			primaryGroup.setCompositeCourseID(a.getCourseInstance().getId());
			primaryGroup.setEnteringAllowed(false);
			primaryGroup.setLeavingAllowed(false);
			primaryGroup.setManagedRoot(false);
			primaryGroup.setName("Grupe za ispite");
			primaryGroup.setParent(a.getCourseInstance().getPrimaryGroup());
			primaryGroup.setRelativePath("4");
			DAOHelperFactory.getDAOHelper().getGroupDAO().save(em, primaryGroup);
			a.getCourseInstance().getPrimaryGroup().getSubgroups().add(primaryGroup);
		}
		
		Group g = new Group();
		
		g.setCapacity(-1);
		g.setCompositeCourseID(a.getCourseInstance().getId());
		g.setEnteringAllowed(false);
		g.setLeavingAllowed(false);
		g.setManagedRoot(false);
		g.setName("Provjera "+a.getShortName());
		g.setRelativePath("4/"+findNextGroupId(dh,em,a.getCourseInstance().getId(),"4/%"));
		g.setParent(primaryGroup);
		
		a.setGroup(g);
		dh.getGroupDAO().save(em, g);
	}
	
	/**
	 * Metoda koja stvara jednu grupu za odredjeni AssessmentRoom 
	 * @param dh
	 * @param em
	 * @param ar
	 */
	@Deprecated
	private static void createNewRoomGroup(DAOHelper dh, EntityManager em, AssessmentRoom ar, Group parent) {
		Group g = new Group();
		
		g.setCapacity(-1);
		g.setCompositeCourseID(parent.getCompositeCourseID());
		g.setEnteringAllowed(false);
		g.setLeavingAllowed(false);
		g.setManagedRoot(false);
		g.setName(ar.getRoom().getShortName());
		g.setParent(parent);
		
		String tmp = parent.getRelativePath()+"/";
		g.setRelativePath(tmp+findNextGroupId(dh, em, parent.getCompositeCourseID(), tmp+"%"));
		ar.setGroup(g);
		dh.getGroupDAO().save(em, g);
	}
	/**
	 * Metoda koja brise sve groupe, sve groupWideEvente i userSpecificEvente iz AssessmentRoomova 
	 * @param dh
	 * @param em
	 * @param a
	 */
	@Deprecated
	public static void clearAssessmentSchedule(DAOHelper dh, EntityManager em, Assessment a) {
		
		//TODO: otkazati rezervaciju na FERWebu
		if (a!=null) {
			//iteriramo kroz sobe i brisemo grupe, u ovom trenutku bi sobe trebale biti prazne
			Group rootGroup = a.getGroup();
			for (AssessmentRoom ar : a.getRooms()) {
				Group g = ar.getGroup();
				//ako grupa postoji
				if (g != null) {
					if (g.getEvents() != null) {
						for (GroupWideEvent e : g.getEvents()) {
							e.getGroups().remove(g);
							dh.getEventDAO().remove(em, e);
						}
						g.getEvents().clear();
					}
					//premjestamo sve usere u glavnu grupu assessmenta
					if (g.getUsers() != null) {
						for (UserGroup ug : g.getUsers()) {
							ug.setGroup(rootGroup);
							rootGroup.getUsers().add(ug);
						}
						g.getUsers().clear();
					}
					//brisemo samu grupu
					g.getParent().getSubgroups().remove(g);
					g.setParent(null);
					ar.setGroup(null);
					dh.getGroupDAO().remove(em, g);
				}
				//brisemo user specific evente
				UserSpecificEvent use = ar.getUserEvent();
				if (use != null) {
					ar.setUserEvent(null);
					dh.getEventDAO().remove(em, use);
				}
			}
			
			//iteriramo kroz asistente i brisemo ih
			for (AssessmentAssistantSchedule aas : a.getAssistantSchedule()) {
				aas.setRoom(null);
				aas.setUser(null);
				aas.setAssessment(null);
				dh.getAssessmentDAO().remove(em, aas);
			}
			a.getAssistantSchedule().clear();
		}
	}
	
	/**
	 * Metoda koja vraca id prve sljedece slobodne grupe unutar grupa za Assessmente
	 * 
	 * @param dh
	 * @param em
	 * @param compositeCourseID id predmeta
	 * @param relativePath path unutar kojeg se trazi prvi slobodni id (oblika "4/%", 4/1/%", ... uvijek pocinje s "4/") 
	 * @return
	 */
	@Deprecated
	private static String findNextGroupId(DAOHelper dh, EntityManager em, String compositeCourseID, String relativePath) {
		
		List<Group> groups = dh.getGroupDAO().findSubgroups(em, compositeCourseID, relativePath);
		//ako nismo nasli nista
		if (groups == null || groups.size()==0)
			return "0";
		
		int min = 0, slashes;
		slashes = relativePath.split("/").length-1;
		
		for (Group g : groups) {
			String[] tmp = g.getRelativePath().split("/");
			if (tmp.length-1 == slashes) {
				int x = Integer.valueOf(tmp[slashes]);
				if (min<x) min = x;
			}
		}
		
		return String.valueOf(min+1);
	}

	@Deprecated
	private static int getRoomCapacity(Assessment a) {
		
		int currCapacity = 0;
		for (AssessmentRoom ar : a.getRooms()) {
			if (ar.isTaken()) currCapacity += ar.getCapacity();
		}
		return currCapacity;
	}

	@Deprecated
	private static int getUserNum(DAOHelper dh, EntityManager em, Assessment a) {
		
		int userNumber = 0;
		if (a.getGroup() != null) {
			Number num = dh.getUserDAO().getUserNumber(em, a.getCourseInstance().getId(),
					a.getGroup().getRelativePath()+"/%", a.getGroup().getRelativePath());
			userNumber = num.intValue();
		}
		return userNumber;
	}
}
