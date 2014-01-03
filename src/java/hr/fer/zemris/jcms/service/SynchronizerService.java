package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.CourseUserRoleBean;
import hr.fer.zemris.jcms.beans.RoomBean;
import hr.fer.zemris.jcms.beans.ext.ConstraintsImportBean;
import hr.fer.zemris.jcms.beans.ext.CourseInstanceBeanExt;
import hr.fer.zemris.jcms.beans.ext.GroupFlat;
import hr.fer.zemris.jcms.beans.ext.GroupScheduleBean;
import hr.fer.zemris.jcms.beans.ext.ISVUFileItemBean;
import hr.fer.zemris.jcms.beans.ext.LabScheduleBean;
import hr.fer.zemris.jcms.beans.ext.MPFormulaConstraints;
import hr.fer.zemris.jcms.beans.ext.UserGroupFlat;
import hr.fer.zemris.jcms.beans.ext.UserPartialBean;
import hr.fer.zemris.jcms.beans.ext.LabScheduleBean.CategoryStudents;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.CourseComponentDescriptor;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseInstanceIsvuData;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.MPOffer;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserDescriptor;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.Venue;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.parsers.CourseUserRoleParser;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.CourseInstanceUtil;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.web.actions.data.ImportCourseMPConstraintsData;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseIsvuData;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseLectureScheduleData;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseStudentsData;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeLabScheduleData;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeRoomsData;
import hr.fer.zemris.jcms.web.actions.data.UpdateCourseInstanceRolesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

public class SynchronizerService {

	public static final Logger logger = Logger.getLogger(SynchronizerService.class.getCanonicalName());
	
	public static void synchronizeCourseInstances(final String yearSemesterID, final Iterable<CourseInstanceBeanExt> courseInstances) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				synchronizeCourseInstances(em, yearSemesterID, courseInstances);
				return null;
			}
		
		});
	}

	public static void synchronizeISVUFile(final String yearSemesterID, final Long authTypeID, final List<ISVUFileItemBean> isvuItems) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				List<CourseInstanceBeanExt> courseInstanceBeans = extractCourseInstanceBeans(isvuItems);
				List<CourseInstance> courseInstances = synchronizeCourseInstances(em, yearSemesterID, courseInstanceBeans);
				List<UserPartialBean> userBeans = extractUserBeans(isvuItems);
				List<User> users = synchronizeUsers(em, yearSemesterID, userBeans, authTypeID);
				Map<String,Map<String,GroupFlat>> groupBeans = extractGroupBeans(isvuItems);
				Map<String,Map<String,UserGroupFlat>> userGroupBeans = extractUserGroupBeans(isvuItems);
				synchronizeGroups(em, yearSemesterID, users, courseInstances, groupBeans, userGroupBeans);
				return null;
			}
		
		});
	}

	public static void synchronizeISVUFile(EntityManager em, YearSemester ysem, AuthType authType, List<ISVUFileItemBean> isvuItems) {
		List<CourseInstanceBeanExt> courseInstanceBeans = extractCourseInstanceBeans(isvuItems);
		List<CourseInstance> courseInstances = synchronizeCourseInstances(em, ysem.getId(), courseInstanceBeans);
		List<UserPartialBean> userBeans = extractUserBeans(isvuItems);
		List<User> users = synchronizeUsers(em, ysem.getId(), userBeans, authType.getId());
		Map<String,Map<String,GroupFlat>> groupBeans = extractGroupBeans(isvuItems);
		Map<String,Map<String,UserGroupFlat>> userGroupBeans = extractUserGroupBeans(isvuItems);
		synchronizeGroups(em, ysem.getId(), users, courseInstances, groupBeans, userGroupBeans);
	}

	public static void synchronizeCourseLectureSchedule(final String yearSemesterID, final List<GroupScheduleBean> scheduleItems) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				synchronizeCourseLectureSchedule(null, em, yearSemesterID, scheduleItems);
				return null;
			}
		
		});
	}

	protected static void synchronizeCourseLectureSchedule(IMessageLogger logger, EntityManager em, String yearSemesterID, List<GroupScheduleBean> scheduleItems) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		// Najprije dohvati sve kolegije na semestru, i mapiraj ih po isvuSifri
		YearSemester yearSemester = dh.getYearSemesterDAO().get(em, yearSemesterID);
		if(yearSemester==null) return;
		List<CourseInstance> instances = dh.getCourseInstanceDAO().findForSemester(em, yearSemesterID);
		Map<String, CourseInstance> courseByIsvuCodeMap = new HashMap<String, CourseInstance>(instances.size());
		for(CourseInstance ci : instances) {
			String isvuCode = ci.getCourse().getIsvuCode();
			courseByIsvuCodeMap.put(isvuCode, ci);
		}
		
		// Izračunaj predmete za koje si sada dobio satnicu i mapiraj predane evente po predmetima. Ostale predmete NE diramo.
		Set<String> givenCourses = new HashSet<String>(50);
		Map<String,List<GroupScheduleBean>> givenCourseSchedule = new HashMap<String, List<GroupScheduleBean>>(50);
		for(GroupScheduleBean b : scheduleItems) {
			givenCourses.add(b.getIsvuCode());
			List<GroupScheduleBean> list = givenCourseSchedule.get(b.getIsvuCode());
			if(list==null) {
				list = new ArrayList<GroupScheduleBean>(128);
				givenCourseSchedule.put(b.getIsvuCode(), list);
			}
			list.add(b);
		}
		
		// Napravi batch preload svih grupa za predavanja, i potom dohvati sve evente satnice za aktualni semestar
		Map<String, Map<String, Group>> groups = loadGroupsForrest(em, dh, yearSemesterID+"/%", "0");
		List<GroupWideEvent> currentEvents = dh.getEventDAO().listSemesterLectureEvents(em, yearSemesterID);

		// Podijeli sve evente koje si procitao po kolegijima
		Map<String,List<GroupWideEvent>> courseEventsMap = new HashMap<String, List<GroupWideEvent>>(50);
		for(GroupWideEvent gwe : currentEvents) {
			if(gwe.getGroups().isEmpty()) continue; // Ovo se zapravo ne smije dogoditi!
			Group g = gwe.getGroups().iterator().next();
			int pos = g.getCompositeCourseID().indexOf('/');
			String isvuCode = g.getCompositeCourseID().substring(pos+1);
			List<GroupWideEvent> gweList = courseEventsMap.get(isvuCode);
			if(gweList == null) {
				gweList = new ArrayList<GroupWideEvent>(128);
				courseEventsMap.put(isvuCode, gweList);
			}
			gweList.add(gwe);
		}

		for(String isvuCode : givenCourses) {
			CourseInstance ci = courseByIsvuCodeMap.get(isvuCode);
			if(ci==null) {
				// Ovog kolegija uopce nema u semestru! Preskoci!
				if(logger!=null) logger.addWarningMessage("Preskačem satnicu za kolegij "+isvuCode+" jer ga nemam u bazi.");
				System.out.println("Preskacem satnicu za kolegij "+isvuCode+" jer ga nemam u bazi.");
				continue;
			}
			List<GroupScheduleBean> givenList = givenCourseSchedule.get(isvuCode);
			List<GroupWideEvent> dbList = courseEventsMap.get(isvuCode);
			Map<String, Group> groupsByNameMap = groups.get(isvuCode); 
			synchronizeSingleCourseLectureSchedule(logger, em, yearSemesterID, ci, isvuCode, givenList, dbList, groupsByNameMap);
		}
	}

	private static void synchronizeSingleCourseLectureSchedule(
			IMessageLogger logger, EntityManager em, String yearSemesterID, CourseInstance ci, String isvuCode, List<GroupScheduleBean> givenList,
			List<GroupWideEvent> dbList, Map<String, Group> groupsByNameMap) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(dbList==null) dbList = new ArrayList<GroupWideEvent>();
		Collections.sort(givenList, new Comparator<GroupScheduleBean>() {
			@Override
			public int compare(GroupScheduleBean o1, GroupScheduleBean o2) {
				int r = o1.getDate().compareTo(o2.getDate());
				if(r!=0) return r;
				r = o1.getStart().compareTo(o2.getStart());
				if(r!=0) return r;
				r = o1.getRoom().compareTo(o2.getRoom());
				return r;
			}
		});
		Collections.sort(dbList, new Comparator<GroupWideEvent>() {
			@Override
			public int compare(GroupWideEvent o1, GroupWideEvent o2) {
				int r = o1.getStart().compareTo(o2.getStart());
				if(r!=0) return r;
				r = o1.getRoom().getShortName().compareTo(o2.getRoom().getShortName());
				return r;
			}
		});
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String givenStart = null;
		String dbStart = null;
		GroupScheduleBean givenEvent = null;
		GroupWideEvent dbEvent = null;
		Iterator<GroupScheduleBean> givenIt = givenList.iterator();
		Iterator<GroupWideEvent> dbIt = dbList.iterator();
		
		if(givenIt.hasNext()) {
			givenEvent = givenIt.next();
			givenStart = givenEvent.getDate()+" "+givenEvent.getStart()+":00";
		} else {
			givenEvent = null;
			givenStart = null;
		}
		if(dbIt.hasNext()) {
			dbEvent = dbIt.next();
			dbStart = sdf.format(dbEvent.getStart());
		} else {
			dbEvent = null;
			dbStart = null;
		}
		while(givenEvent!=null && dbEvent!=null) {
			int r = compareEvents(givenStart, givenEvent.getRoom(), dbStart, dbEvent.getRoom().getShortName());
			boolean nextGiven = false;
			boolean nextDb = false;
			if(r<0) {
				// Tada imamo jedan novi ubaceni event; treba ga dodati u bazu.
				createGroupEventFromBean(logger, em, dh, yearSemesterID, ci, isvuCode, givenStart, givenEvent, groupsByNameMap, sdf);
				nextGiven = true;
			} else if(r>0) {
				// Sada očito imam u bazi groupEvent nastao satnicom, međutim, tog termina u satnici više nema.
				// Idemo to obrisati!
				for(Group g : dbEvent.getGroups()) {
					g.getEvents().remove(dbEvent);
				}
				dbEvent.getGroups().clear();
				dh.getEventDAO().remove(em, dbEvent);
				nextDb = true;
			} else {
				// Inace sam naletio na isti termin i u satnici, i u bazi
				// Provjeri slazu li se grupe!
				updateEventGroupsWithBean(isvuCode, givenEvent, dbEvent,
						groupsByNameMap);
				nextDb= true;
				nextGiven = true;
			}
			if(nextGiven) {
				if(givenIt.hasNext()) {
					givenEvent = givenIt.next();
					givenStart = givenEvent.getDate()+" "+givenEvent.getStart()+":00";
				} else {
					givenEvent = null;
					givenStart = null;
				}
			}
			if(nextDb) {
				if(dbIt.hasNext()) {
					dbEvent = dbIt.next();
					dbStart = sdf.format(dbEvent.getStart());
				} else {
					dbEvent = null;
					dbStart = null;
				}
			}
		}
		// Ako je ovo dolje razlicito od null, tada u satnici ima novih evenata kojih u bazi nema; dodaj ih sve...
		while(givenEvent != null) {
			createGroupEventFromBean(logger, em, dh, yearSemesterID, ci, isvuCode, givenStart, givenEvent, groupsByNameMap, sdf);
			if(givenIt.hasNext()) {
				givenEvent = givenIt.next();
				givenStart = givenEvent.getDate()+" "+givenEvent.getStart()+":00";
			} else {
				givenEvent = null;
				givenStart = null;
			}
		}
		// Ako je ovo dolje razlicito od null, tada u bazi ima viska evenata kojih u satnici nema; izbrisi ih sve...
		while(dbEvent != null) {
			for(Group g : dbEvent.getGroups()) {
				g.getEvents().remove(dbEvent);
			}
			dbEvent.getGroups().clear();
			dh.getEventDAO().remove(em, dbEvent);
			if(dbIt.hasNext()) {
				dbEvent = dbIt.next();
				dbStart = sdf.format(dbEvent.getStart());
			} else {
				dbEvent = null;
				dbStart = null;
			}
		}
	}

	private static void updateEventGroupsWithBean(String isvuCode,
			GroupScheduleBean givenEvent, GroupWideEvent dbEvent,
			Map<String, Group> groupsByNameMap) {
		Set<Group> givenGroups = new HashSet<Group>();
		for(String gname : givenEvent.getGroups()) {
			Group g = groupsByNameMap.get(gname);
			if(g==null) {
				System.out.println("Schedule contains group "+gname+" which is not found in database on course "+isvuCode);
			} else {
				givenGroups.add(g);
			}
		}
		Set<Group> newGroups = new HashSet<Group>(givenGroups);
		newGroups.removeAll(dbEvent.getGroups());
		if(!newGroups.isEmpty()) {
			// Imam novih grupa za ubaciti u event!
			for(Group g : newGroups) {
				dbEvent.getGroups().add(g);
				g.getEvents().add(dbEvent);
			}
		}
		Set<Group> abandonedGroups = new HashSet<Group>(dbEvent.getGroups());
		abandonedGroups.remove(givenGroups);
		if(!abandonedGroups.isEmpty()) {
			// U bazi imam na eventu grupa koje vise nisu u satnici; maknimo ih!
			for(Group g : abandonedGroups) {
				dbEvent.getGroups().remove(g);
				g.getEvents().remove(dbEvent);
			}
		}
	}

	private static void createGroupEventFromBean(IMessageLogger logger, EntityManager em,
			DAOHelper dh, String yearSemesterID, CourseInstance ci, String isvuCode,
			String givenStart, GroupScheduleBean givenEvent,
			Map<String, Group> groupsByNameMap, SimpleDateFormat sdf) {
		Set<Group> grupe = new HashSet<Group>();
		for(String gname : givenEvent.getGroups()) {
			Group g = groupsByNameMap.get(gname);
			if(g==null) { //Ove grupe nema!!!
				System.out.println("Missing group "+gname+" on course "+isvuCode);
				if(logger!=null) logger.addWarningMessage("Missing group "+gname+" on course "+isvuCode);
			} else {
				grupe.add(g);
			}
		}
		Date d = null;
		try {
			d = sdf.parse(givenStart);
		} catch(Exception ignorable) {
		}
		Room room = dh.getRoomDAO().get(em, givenEvent.getVenue(), givenEvent.getRoom());
		if(grupe.isEmpty()) {
			System.out.println("No group left for event creation on course "+isvuCode);
			if(logger!=null) logger.addWarningMessage("No group left for event creation on course "+isvuCode);
		} else if(room == null) {
			System.out.println("Room "+givenEvent.getRoom()+" not found, so no group event created on course "+isvuCode);
			if(logger!=null) logger.addWarningMessage("Room "+givenEvent.getRoom()+" not found, so no group event created on course "+isvuCode);
		} else if(d == null) {
			System.out.println("Date "+givenStart+" is misformatted, so no group event created on course "+isvuCode);
			if(logger!=null) logger.addWarningMessage("Date "+givenStart+" is misformatted, so no group event created on course "+isvuCode);
		} else {
			GroupWideEvent gwe = new GroupWideEvent();
			gwe.setDuration(givenEvent.getDuration());
			gwe.getGroups().addAll(grupe);
			for(Group g : grupe) {
				g.getEvents().add(gwe);
			}
			gwe.setRoom(room);
			gwe.setSpecifier(yearSemesterID+"/satnica/P");
			gwe.setStart(d);
			gwe.setStrength(EventStrength.STRONG);
			gwe.setTitle(ci.getCourse().getName());
			gwe.setContext("l:"+ci.getId());
			dh.getEventDAO().save(em, gwe);
		}
	}

	private static int compareEvents(String givenStart, String givenRoomName,
			String dbStart, String dbRoomName) {
		int r = givenStart.compareTo(dbStart);
		if(r!=0) return r;
		return givenRoomName.compareTo(dbStart);
	}

	/**
	 * Vraća mapu Map<isvuCode, Map<jmbag, UserGroupFlat>>.
	 * @param isvuItems isvu stavke
	 * @return mapa veza grupa-student.
	 */
	protected static Map<String, Map<String, UserGroupFlat>> extractUserGroupBeans(List<ISVUFileItemBean> isvuItems) {
		Map<String, Map<String, UserGroupFlat>> userGroupBeans = new HashMap<String, Map<String,UserGroupFlat>>(100);
		for(ISVUFileItemBean b : isvuItems) {
			Map<String, UserGroupFlat> m = userGroupBeans.get(b.getIsvuCode());
			if(m==null) {
				m = new HashMap<String, UserGroupFlat>(200);
				userGroupBeans.put(b.getIsvuCode(), m);
			}
			UserGroupFlat g = m.get(b.getJmbag());
			if(g==null) {
				g = new UserGroupFlat(b.getIsvuCode(),b.getJmbag(),b.getGroup());
				m.put(b.getJmbag(), g);
			}
		}
		return userGroupBeans;
	}

	/**
	 * Stvara mapu Map<isvuSifra, Map<imeGrupeNaKolegiju, GroupFlat>>. pri tome su u objektu GroupFlat popunjeni samo isvuCode i groupName.
	 * @param isvuItems isvu stavke
	 * @return mapa grupa iz datoteke
	 */
	protected static Map<String, Map<String, GroupFlat>> extractGroupBeans(List<ISVUFileItemBean> isvuItems) {
		Map<String, Map<String, GroupFlat>> groupBeans = new HashMap<String, Map<String,GroupFlat>>(100);
		for(ISVUFileItemBean b : isvuItems) {
			Map<String, GroupFlat> m = groupBeans.get(b.getIsvuCode());
			if(m==null) {
				m = new HashMap<String, GroupFlat>();
				groupBeans.put(b.getIsvuCode(), m);
			}
			GroupFlat g = m.get(b.getGroup());
			if(g==null) {
				g = new GroupFlat();
				g.setIsvuCode(b.getIsvuCode());
				g.setGroupName(b.getGroup());
				m.put(b.getGroup(), g);
			}
		}
		return groupBeans;
	}

	protected static void synchronizeGroups(EntityManager em, String yearSemesterID, List<User> users, List<CourseInstance> courseInstances,
			Map<String,Map<String,GroupFlat>> groupBeans, Map<String,Map<String,UserGroupFlat>> userGroupBeans) {
		// Dodaj sve grupe za predavanja koje postoje u ISVU datoteci; nakon ovoga ću ih sigurno imati sve (možda i previše)
		// no višak ću micati zadnji, kada se pobrinem da unutra više nema korisnika
		Map<String, Map<String, Group>> groups = addMissingLectureGroups(em, yearSemesterID, users, courseInstances, groupBeans, userGroupBeans);
		// Dohvati iz baze sve grupe za predavanja koje postoje
		Map<String,Map<String,UserGroup>> userGroups = loadCourseUserGroups(em, yearSemesterID);
		// Izgradi mapu korisnika
		Map<String,User> userMap = createUserMap(users);
		// Stvori popis korisnika s ponudama
		Map<String, Set<Long>> usersWithOffersMap = createUsersWithOffers(em, yearSemesterID);
		// Dodaj sve nove korisnike u grupe te ažuriraj grupe postojećim korisnicima
		addOrUpdateExistingUserGroups(em, userGroupBeans, userGroups, userMap, groups, usersWithOffersMap);
		// Tek sada provjeri ima li u bazi korisnika viška, kojih više nema u ISVU, i njih makni iz grupa
		removeExtraUserGroups(em, userGroupBeans, userGroups, userMap, groups);
		// I konačno, izbriši sve grupe iz baze kojih više nema u ISVU
		removeExtraGroups(em, groupBeans, groups);
	}

	private static Map<String, Set<Long>> createUsersWithOffers(EntityManager em, String yearSemesterID) {
		List<MPOffer> offers = DAOHelperFactory.getDAOHelper().getMarketPlaceDAO().listOffers(em, yearSemesterID+"/%");
		Map<String, Set<Long>> map = new HashMap<String, Set<Long>>(256);
		for(MPOffer o : offers) {
			String ccid = o.getFromGroup().getCompositeCourseID();
			int pos = ccid.indexOf('/');
			String isvu = pos!=-1 ? ccid.substring(pos+1) : "";
			if(isvu.length()==0) continue;
			Set<Long> set = map.get(isvu);
			if(set==null) {
				set = new HashSet<Long>(32);
				map.put(isvu, set);
			}
			set.add(o.getFromUser().getId());
			if(o.getToUser()!=null) {
				set.add(o.getToUser().getId());
			}
		}
		return map;
	}

	private static void removeExtraGroups(EntityManager em,
			Map<String, Map<String, GroupFlat>> groupBeans,
			Map<String, Map<String, Group>> groups) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Set<Group> groupsToRemove = new HashSet<Group>(500);
		Set<EntrySetItem> entriesToRemove = new HashSet<EntrySetItem>(500);
		Set<GroupWideEvent> eventsToRemove = new HashSet<GroupWideEvent>(500);
		for(Map.Entry<String, Map<String, Group>> isvuCourseGroups : groups.entrySet()) {
			String isvuCode = isvuCourseGroups.getKey();
			Map<String, Group> groupMapping = isvuCourseGroups.getValue();
			Map<String, GroupFlat> groupBeanMapping = groupBeans.get(isvuCode);
			if(groupBeanMapping==null) {
				// Ovog kolegija više nema u ISVU datoteci; preskoči
				// Naime, kolegij je već odavno trebao biti izbrisan, a ako to nije, onda su mu barem korisnici
				// već morali biti maknuti
				continue;
			}
			Iterator<Map.Entry<String, Group>> it = groupMapping.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, Group> e = it.next();
				String groupName = e.getKey();
				Group g = e.getValue();
				// Ako je to zaštićena prazna grupa, poštedi je brisanja
				if(groupName.equals("")) {
					continue;
				}
				if(!groupBeanMapping.containsKey(groupName)) {
					// U ISVU datoteci ova grupa više ne postoji. Obriši je.
					// Grupa više također ne bi smjela imati niti korisnike!
					groupsToRemove.add(g);
					entriesToRemove.add(new EntrySetItem(e,groupMapping.entrySet()));
					//g.getParent().getSubgroups().remove(g);
					//dh.getGroupDAO().remove(em, g);
					//it.remove();
					// obrisi evente
					for(GroupWideEvent gwe : g.getEvents()) {
						gwe.getGroups().remove(g);
						if(gwe.getGroups().isEmpty()) {
							eventsToRemove.add(gwe);
						}
					}
					g.getEvents().clear();
				}
			}
		}
		if(!eventsToRemove.isEmpty()) {
			em.flush();
			for(GroupWideEvent gwe : eventsToRemove) {
				dh.getEventDAO().remove(em, gwe);
			}
		}
		if(!groupsToRemove.isEmpty()) {
			for(Group g : groupsToRemove) {
				g.getParent().getSubgroups().remove(g);
				dh.getGroupDAO().remove(em, g);
			}
			em.flush();
		}
		for(EntrySetItem e : entriesToRemove) {
			e.entrySet.remove(e.entry);
		}
	}

	static class EntrySetItem {
		Map.Entry<String, Group> entry;
		Set<Map.Entry<String, Group>> entrySet;
		public EntrySetItem(Entry<String, Group> entry,
				Set<Entry<String, Group>> entrySet) {
			super();
			this.entry = entry;
			this.entrySet = entrySet;
		}
	}
	
	private static Map<String, User> createUserMap(List<User> users) {
		Map<String, User> map = new HashMap<String, User>(users.size());
		for(User u : users) {
			map.put(u.getJmbag(), u);
		}
		return map;
	}

	private static void removeExtraUserGroups(EntityManager em,
			Map<String, Map<String, UserGroupFlat>> userGroupBeans,
			Map<String, Map<String, UserGroup>> allUserGroups,
			Map<String, User> userMap, Map<String, Map<String, Group>> groups) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		boolean anyRemoved = false;
		for(Map.Entry<String, Map<String, UserGroup>> courses : allUserGroups.entrySet()) {
			String isvuCode = courses.getKey();
			Map<String, UserGroup> userGroups = courses.getValue();
			Map<String, UserGroupFlat> userGroupFlats = userGroupBeans.get(isvuCode);
			Iterator<Map.Entry<String, UserGroup>> it = userGroups.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, UserGroup> e = it.next();
				String jmbag = e.getKey();
				UserGroup ug = e.getValue();
				UserGroupFlat mappedGroup = userGroupFlats==null ? null : userGroupFlats.get(jmbag);
				if(mappedGroup==null) {
					// Ovaj korisnik vise nije na kolegiju, ili više nema niti kolegija!!! Makni ga!
					it.remove();
					ug.getGroup().getUsers().remove(ug);
					dh.getUserGroupDAO().remove(em, ug);
					anyRemoved = true;
				}
			}
		}
		if(anyRemoved) {
			em.flush();
		}
	}

	private static void addOrUpdateExistingUserGroups(EntityManager em,
			Map<String, Map<String, UserGroupFlat>> userGroupBeans,
			Map<String, Map<String, UserGroup>> userGroups, Map<String, User> userMap, Map<String, Map<String, Group>> groups, Map<String, Set<Long>> usersWithOffersMap) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		for(Map.Entry<String, Map<String, UserGroupFlat>> courses : userGroupBeans.entrySet()) {
			String isvuCode = courses.getKey();
			Map<String, UserGroupFlat> courseUserGroups = courses.getValue();
			Map<String, UserGroup> existingUserGroups = userGroups.get(isvuCode);
			if(existingUserGroups==null) {
				existingUserGroups = new HashMap<String, UserGroup>(200);
				userGroups.put(isvuCode, existingUserGroups);
			}
			Map<String, Group> courseGroups = groups.get(isvuCode);
			for(UserGroupFlat ugf : courseUserGroups.values()) {
				UserGroup ug = existingUserGroups.get(ugf.getJmbag());
				// Ako ovaj korisnik uopće nije još na kolegiju:
				if(ug==null) {
					ug = new UserGroup();
					ug.setUser(userMap.get(ugf.getJmbag()));
					ug.setGroup(courseGroups.get(ugf.getGroupName()));
					ug.getGroup().getUsers().add(ug);
					dh.getUserGroupDAO().save(em, ug);
					existingUserGroups.put(ugf.getJmbag(), ug);
				} else if(!ug.getGroup().getName().equals(ugf.getGroupName())) {
					// Inace, ako je u nekoj grupi, ali nije u dobroj, premjesti ga...
					// 1. vidi ima li kakvih ponuda:
					Set<Long> set = usersWithOffersMap.get(isvuCode);
					if(set!=null && set.contains(ug.getUser().getId())) {
						// Ovaj ima ponuda koje se ticu njega... Rijesi te ponude!
						MarketPlace mp = ug.getGroup().getParent().getMarketPlace();
						if(mp==null) {
							System.err.println("Greska! Nemam marketplace za grupu "+ug.getGroup().getId()+" preko roditelja.");
						} else {
							dh.getMarketPlaceDAO().clearAllOffersForUser(em, mp, ug.getUser(), ug.getGroup());
							System.out.println("Obrisao sam ponude od/za korisnika "+ug.getUser().getId()+" i grupu "+ug.getGroup().getId()+" na kolegiju "+isvuCode+".");
						}
					}
					ug.getGroup().getUsers().remove(ug);
					ug.setGroup(courseGroups.get(ugf.getGroupName()));
					ug.getGroup().getUsers().add(ug);
				}
			}
		}
	}

	private static Map<String, Map<String, UserGroup>> loadCourseUserGroups(
			EntityManager em, String yearSemesterID) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<UserGroup> uglist = dh.getGroupDAO().findAllLectureUserGroups(em, yearSemesterID+"/%");
		Map<String, Map<String, UserGroup>> userGroups = new HashMap<String, Map<String,UserGroup>>(100);
		for(UserGroup ug : uglist) {
			String ccid = ug.getGroup().getCompositeCourseID();
			int pos = ccid.indexOf('/');
			String isvuCode = ccid.substring(pos+1);
			Map<String, UserGroup> m = userGroups.get(isvuCode);
			if(m==null) {
				m = new HashMap<String, UserGroup>(200);
				userGroups.put(isvuCode, m);
			}
			m.put(ug.getUser().getJmbag(), ug);
		}
		return userGroups;
	}

	protected static Map<String, Map<String, Group>> addMissingLectureGroups(EntityManager em, String yearSemesterID, List<User> users, List<CourseInstance> courseInstances,
			Map<String,Map<String,GroupFlat>> groupBeans, Map<String,Map<String,UserGroupFlat>> userGroupBeans) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Map<String, Map<String, Group>> groups = loadGroupsForrest(em, dh, yearSemesterID+"/%", "0");
		// Korak 1. Napravi grupe kojih jos nema
		for(Map.Entry<String,Map<String,GroupFlat>> courseGroups : groupBeans.entrySet()) {
			String isvuCode = courseGroups.getKey();
			Map<String,GroupFlat> groupFlats = courseGroups.getValue();
			Map<String, Group> dbGroups = groups.get(isvuCode);
			// Ovdje se ne smije dogoditi da je dbGroups null, jer bi to značilo da postoji predmet koji NEMA grupu
			// neraspoređeni a to je neprihvatljivo!
			List<GroupFlat> mustAddGroups = new ArrayList<GroupFlat>();
			for(GroupFlat groupFlat : groupFlats.values()) {
				if(dbGroups.get(groupFlat.getGroupName())==null) {
					mustAddGroups.add(groupFlat);
				}
			}
			Group lectureGroup = null;
			List<Integer> indexes = new ArrayList<Integer>();
			for(Group g : dbGroups.values()) {
				if(g.getRelativePath().equals("0/0")) {
					lectureGroup = g;
				}
				int pos = g.getRelativePath().indexOf('/');
				indexes.add(Integer.valueOf(g.getRelativePath().substring(pos+1)));
			}
			Collections.sort(indexes);
			for(GroupFlat groupFlat : mustAddGroups) {
				int freeIndex = 0;
				for(int i = 0; i < indexes.size(); i++) {
					if(indexes.get(i).intValue()!=freeIndex) {
						break;
					}
					freeIndex++;
				}
				Integer newInt = Integer.valueOf(freeIndex);
				indexes.add(newInt);
				for(int i = indexes.size()-1; i >= 1; i--) {
					Integer old = indexes.get(i-1);
					if(old.intValue()>freeIndex) {
						indexes.set(i, old);
						indexes.set(i-1, newInt);
					} else break;
				}
				Group newGroup = new Group();
				newGroup.setCompositeCourseID(lectureGroup.getCompositeCourseID());
				newGroup.setRelativePath("0/"+freeIndex);
				newGroup.setName(groupFlat.getGroupName());
				newGroup.setCapacity(-1);
				newGroup.setEnteringAllowed(true);
				newGroup.setLeavingAllowed(true);
				newGroup.setManagedRoot(false);
				newGroup.setParent(lectureGroup.getParent());
				dh.getGroupDAO().save(em, newGroup);
				lectureGroup.getSubgroups().add(newGroup);
				dbGroups.put(newGroup.getName(), newGroup);
			}
		}
		return groups;
	}

	/**
	 * Vraca mapu <isvuCode,Map<groupName,Group>>.
	 * @param em
	 * @param dh
	 * @param likeCompositeCourseID
	 * @param rootGroupName
	 * @return
	 */
	private static Map<String, Map<String, Group>> loadGroupsForrest(EntityManager em, DAOHelper dh, String likeCompositeCourseID, String rootGroupName) {
		List<Group> groupsList = dh.getGroupDAO().findSubgroupsLLE(em, likeCompositeCourseID, rootGroupName, rootGroupName+"/%");
		Map<String, Map<String, Group>> groups = new HashMap<String, Map<String,Group>>(100);
		for(Group g : groupsList) {
			// Ako sam na vrsnoj grupi predavanja, preskoci je...
			if(g.getRelativePath().equals(rootGroupName)) continue;
			int pos = g.getCompositeCourseID().indexOf('/');
			String key = g.getCompositeCourseID().substring(pos+1);
			Map<String, Group> m = groups.get(key);
			if(m==null) {
				m = new HashMap<String, Group>();
				groups.put(key, m);
			}
			m.put(g.getName(), g);
		}
		return groups;
	}

	protected static List<User> synchronizeUsers(EntityManager em,
			String yearSemesterID, List<UserPartialBean> userBeans, Long authTypeID) {
		logger.debug("Pozvan synchronizeUsers: 1");
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		YearSemester yearSemester = dh.getYearSemesterDAO().get(em, yearSemesterID);
		logger.debug("Pozvan synchronizeUsers: 2");
		if(yearSemester==null) return new ArrayList<User>();

		// Korak 1. Stvori skup jmbagova koji bi trebali postojati po zavrsetku rada ove metode
		Set<String> jmbags = new HashSet<String>(userBeans.size());
		for(UserPartialBean upb : userBeans) {
			jmbags.add(upb.getJmbag());
		}
		
		// Korak 2. Dohvati sve korisnike koji postoje u trenutnoj akademskoj godini
		List<User> existingUsers = dh.getYearSemesterDAO().findUsersInSemester(em, yearSemester);
		logger.debug("Pozvan synchronizeUsers: 3");
		
		// Korak 3. Otkrij kojih korisnika možda nema:
		Set<String> foundJmbags = new HashSet<String>(existingUsers.size());
		for(User u : existingUsers) {
			foundJmbags.add(u.getJmbag());
		}
		Set<String> maybeMissingJmbags = new HashSet<String>(jmbags);
		maybeMissingJmbags.removeAll(foundJmbags);

		// Korak 4. Pokušaj ih eksplicitno dohvatiti po jmbag-u...
		List<String> maybeMissingJmbagsList = new ArrayList<String>(maybeMissingJmbags);
		List<User> foundUsers = dh.getUserDAO().getForJmbagSublistBatching(em, maybeMissingJmbagsList);
		for(User u : foundUsers) {
			maybeMissingJmbags.remove(u.getJmbag());
		}

		// Korak 5. Sada maybeMissingJmbags sadrži jmbagove koji doista ne postoje u bazi. Napravi ih!
		List<User> createdUsers = new ArrayList<User>(maybeMissingJmbags.size());
		AuthType type = dh.getAuthTypeDAO().get(em, authTypeID);
		Role studentRole = dh.getRoleDAO().get(em, "student");
		Random r = new Random();
		char[] slova = new char[12];
		for(UserPartialBean upb : userBeans) {
			if(!maybeMissingJmbags.contains(upb.getJmbag())) continue;
			User u = new User();
			u.setFirstName(upb.getFirstName());
			u.setLastName(upb.getLastName());
			u.setJmbag(upb.getJmbag());
			u.setUsername(upb.getJmbag());
			UserDescriptor udes = new UserDescriptor();
			udes.setAuthType(type);
			udes.setAuthUsername(upb.getJmbag());
			if(JCMSSettings.getSettings().isDebugMode()) {
				udes.setDataValid(true);
			} else {
				udes.setDataValid(false);
			}
			udes.setLocked(false);
			udes.setEmail(upb.getJmbag()+"@fer.hr");
			for(int i = 0; i < slova.length; i++) {
				slova[i] = (char)(r.nextInt('Z'-'A')+'A');
			}
			if(JCMSSettings.getSettings().isDebugMode()) {
				// Ovo je privremeni override za potrebe testiranja
				for(int i = 0; i < slova.length; i++) {
					slova[i] = 'A';
				}
			}
			udes.setPassword(StringUtil.encodePassword(new String(slova), "SHA"));
			udes.getRoles().add(studentRole);
			u.setUserDescriptor(udes);
			udes.setExternalID(createExternalID(r));
			dh.getUserDAO().save(em, u);
			createdUsers.add(u);
		}
		
		List<User> allUsers = new ArrayList<User>(existingUsers.size() + foundUsers.size() + createdUsers.size());
		allUsers.addAll(existingUsers);
		allUsers.addAll(foundUsers);
		allUsers.addAll(createdUsers);
		
		return allUsers;
	}

	public static final char[] externalIDChars = new char[] {
		'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
		'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
		'0','1','2','3','4','5','6','7','8','9','=','+','-','.'
	};
	
	public static String createExternalID(Random r) {
		char[] c = new char[64];
		for(int i = 0; i < c.length; i++) {
			c[i] = externalIDChars[r.nextInt(externalIDChars.length)];
		}
		return new String(c);
	}

	/**
	 * Prolazi kroz listu ISVU stavki i stvara listu pronađenih studenata (svaki student
	 * bit će vraćen samo jednom).
	 * 
	 * @param isvuItems isvu stavke
	 * @return lista studenata
	 */
	protected static List<UserPartialBean> extractUserBeans(
			List<ISVUFileItemBean> isvuItems) {
		Set<String> jmbags = new HashSet<String>(5000);
		List<UserPartialBean> userBeans = new ArrayList<UserPartialBean>();
		for(ISVUFileItemBean bean : isvuItems) {
			if(jmbags.add(bean.getJmbag())) {
				UserPartialBean upb = new UserPartialBean();
				upb.setFirstName(bean.getFirstName());
				upb.setLastName(bean.getLastName());
				upb.setJmbag(bean.getJmbag());
				userBeans.add(upb);
			}
		}
		return userBeans;
	}

	/**
	 * Prolazi kroz listu ISVU stavki i stvara listu pronađenih kolegija (svaki kolegij
	 * samo jednom).
	 * 
	 * @param isvuItems isvu stavke
	 * @return lista pronađenih kolegija
	 */
	protected static List<CourseInstanceBeanExt> extractCourseInstanceBeans(
			List<ISVUFileItemBean> isvuItems) {
		Set<String> isvuCodes = new HashSet<String>(100);
		List<CourseInstanceBeanExt> courseInstanceBeans = new ArrayList<CourseInstanceBeanExt>();
		for(ISVUFileItemBean bean : isvuItems) {
			if(isvuCodes.add(bean.getIsvuCode())) {
				CourseInstanceBeanExt cibex = new CourseInstanceBeanExt();
				cibex.setIsvuCode(bean.getIsvuCode());
				cibex.setName(bean.getCourseName());
				courseInstanceBeans.add(cibex);
			}
		}
		return courseInstanceBeans;
	}

	/**
	 * Sinkronizira u bazi popis kolegija za aktualni semestar. Kolegije koji nisu na popisu a jesu u bazi briše!
	 * 
	 * VAŽNO: brisanje kolegija trenutno NE radi.
	 * 
	 * @param em entity manager
	 * @param yearSemesterID oznaka semestra
	 * @param courseInstances lista kolegija
	 */
	public static List<CourseInstance> synchronizeCourseInstances(EntityManager em, String yearSemesterID, Iterable<CourseInstanceBeanExt> courseInstances) {
		return synchronizeCourseInstances(em, yearSemesterID, courseInstances, true);
	}
	
	/**
	 * Sinkronizira u bazi popis kolegija za aktualni semestar.
	 * 
	 * VAŽNO: brisanje kolegija trenutno NE radi.
	 * 
	 * @param em entity manager
	 * @param yearSemesterID oznaka semestra
	 * @param courseInstances lista kolegija
	 * @param fullSynchronization ako je <code>true</code>, kolegiji koji su u bazi a nisu na popisu bit će obrisani; ako je <code>false</code>, samo će nadodati kolegije koji nisu u bazi
	 */
	public static List<CourseInstance> synchronizeCourseInstances(EntityManager em, String yearSemesterID, Iterable<CourseInstanceBeanExt> courseInstances, boolean fullSynchronization) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		YearSemester yearSemester = dh.getYearSemesterDAO().get(em, yearSemesterID);
		if(yearSemester==null) return new ArrayList<CourseInstance>();
		// Dohvati kolegije koji trenutno postoje...
		List<CourseInstance> instances = dh.getCourseInstanceDAO().findForSemester(em, yearSemesterID);
		List<CourseInstance> allCourseInstances = new ArrayList<CourseInstance>();
		// Pogledaj što smo dobili izvana...
		Set<String> newState = new HashSet<String>();
		for(CourseInstanceBeanExt cib : courseInstances) {
			newState.add(cib.getIsvuCode());
		}
		// Pogledaj što trenutno imamo u bazi. 
		// Istovremeno obriši one koji trenutno postoje a nismo ih dobili izvana.
		Set<String> state = new HashSet<String>(instances.size());
		for(CourseInstance ci : instances) {
			allCourseInstances.add(ci);
			if(newState.contains(ci.getCourse().getIsvuCode())) {
				state.add(ci.getCourse().getIsvuCode());
				continue;
			}
			if(!fullSynchronization) continue;
			// TODO: implementirati brisanje primjerka kolegija 
			// brisanje kolegija ćemo za sada izbaciti
			// dh.getCourseInstanceDAO().remove(em, ci);
		}
		// Pogledaj što nemamo u bazi i to stvori:
		for(CourseInstanceBeanExt cib : courseInstances) {
			if(state.contains(cib.getIsvuCode())) {
				continue;
			}
			// Postoji li uopće taj kolegij?
			Course c = dh.getCourseDAO().get(em, cib.getIsvuCode());
			if(c==null) {
				c = new Course();
				c.setIsvuCode(cib.getIsvuCode());
				c.setName(cib.getName());
				dh.getCourseDAO().save(em, c);
				createDefaultCategory(c, em);
				// OVO DOLJE SAM ZAKOMENTIRAO JER BACA EXCEPTION!!! Marine - sredite to
				//..> ovo ne bi smjelo bacati exception!
				//RepositoryService.checkRepositoryRootCourse(c);    
			}

			CourseInstance ci = new CourseInstance();
			ci.setId(yearSemester.getId()+"/"+c.getIsvuCode());

			CourseInstanceIsvuData ciisvud = new CourseInstanceIsvuData();
			ciisvud.setId(ci.getId());
			ci.setIsvuData(ciisvud);
			
			Group primaryGroup = new Group();
			primaryGroup.setCompositeCourseID(ci.getId());
			primaryGroup.setRelativePath("");
			primaryGroup.setName("Primarna grupa");
			primaryGroup.setCapacity(-1);
			primaryGroup.setEnteringAllowed(false);
			primaryGroup.setLeavingAllowed(false);
			primaryGroup.setManagedRoot(false);

			dh.getGroupDAO().save(em, primaryGroup);

			Group lectureGroup = new Group();
			lectureGroup.setCompositeCourseID(ci.getId());
			lectureGroup.setRelativePath("0");
			lectureGroup.setName("Grupe za predavanja");
			lectureGroup.setCapacity(-1);
			lectureGroup.setEnteringAllowed(false);
			lectureGroup.setLeavingAllowed(false);
			lectureGroup.setManagedRoot(true);
			lectureGroup.setParent(primaryGroup);

			// lectureGroup je Managed root; napravimo mu odmah dodijeljenu burzu
			
			MarketPlace mp = new MarketPlace();
			mp.setGroup(lectureGroup);
			mp.setTimeBuffer(-1);
			lectureGroup.setMarketPlace(mp);
			dh.getMarketPlaceDAO().save(em, mp);
			
			dh.getGroupDAO().save(em, lectureGroup);
			
			// Ovo ce osigurati da postoji podgrupa "Neraspoređeni" u svakom
			// kolegiju
			Group nonAssignedLectureGroup = new Group();
			nonAssignedLectureGroup.setCompositeCourseID(ci.getId());
			nonAssignedLectureGroup.setRelativePath("0/0");
			nonAssignedLectureGroup.setName("");
			nonAssignedLectureGroup.setCapacity(-1);
			nonAssignedLectureGroup.setEnteringAllowed(false);
			nonAssignedLectureGroup.setLeavingAllowed(true);
			nonAssignedLectureGroup.setManagedRoot(false);
			nonAssignedLectureGroup.setParent(lectureGroup);

			dh.getGroupDAO().save(em, nonAssignedLectureGroup);
			
			lectureGroup.getSubgroups().add(nonAssignedLectureGroup);

			Group labGroup = new Group();
			labGroup.setCompositeCourseID(ci.getId());
			labGroup.setRelativePath("1");
			labGroup.setName("Grupe za laboratorijske vježbe");
			labGroup.setCapacity(-1);
			labGroup.setEnteringAllowed(false);
			labGroup.setLeavingAllowed(false);
			labGroup.setManagedRoot(false);
			labGroup.setParent(primaryGroup);
			
			dh.getGroupDAO().save(em, labGroup);

			primaryGroup.getSubgroups().add(labGroup);

			ci.setCourse(c);
			ci.setYearSemester(yearSemester);
			ci.setPrimaryGroup(primaryGroup);

			dh.getCourseInstanceDAO().save(em, ci);

			allCourseInstances.add(ci);
		}
		return allCourseInstances;
	}
	
	public static void createDefaultCategory(Course course, EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Category category = new Category();
		course.setCategory(category);
		category.setCourse(course);
		Subforum subforum = new Subforum();
		subforum.setCategory(category);
		subforum.setName("Generalna rasprava");
		subforum.setDescription("Pitanja za kolegij " + course.getName());
		dh.getForumDAO().save(em, category);
		dh.getForumDAO().save(em, subforum);
	}

	@Deprecated
	public static void synchronizeCourseIsvuData(final SynchronizeCourseIsvuData data, final Long userID, final String ayear, final File f, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!task.equals("upload")) {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setCurrentSemesterID(BasicBrowsing.getCurrentSemesterID(em));
					return null;
				}
				if(f==null || !f.canRead()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noFileAttached"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(synchronizeCourseIsvuData(em, ayear, f)) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
				} else {
					data.getMessageLogger().addErrorMessage("Neuspjeh.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
				}
				return null;
			}
		});
		
	}

	public static void synchronizeCourseIsvuDataUnsecure(final String ayear, final File f) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(synchronizeCourseIsvuData(em, ayear, f)) {
					System.out.println("CourseIsvuData synchronized.");
				} else {
					System.out.println("CourseIsvuData not synchronized.");
				}
				return null;
			}
		});
		
	}

	@Deprecated
	public static boolean synchronizeCourseIsvuData(EntityManager em, String ayear, File f) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(f);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		try {
			List<CourseInstance>  ciList = dh.getCourseInstanceDAO().findForSemester(em, ayear);
			Map<String,CourseInstance> ciMap = CourseInstanceUtil.mapCourseInstanceByISVUCode(ciList);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry ze = entries.nextElement();
				if(ze.isDirectory()) continue;
				String name = ze.getName();
				String id = ayear+"/"+name;
				CourseInstance ci = ciMap.get(name);
				if(ci==null) continue;
				String data = null;
				try {
					InputStream is = new BufferedInputStream(zipFile.getInputStream(ze));
					data = TextService.inputStreamToString(is, "UTF-8");
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				if(ci.getIsvuData()==null) {
					CourseInstanceIsvuData isvuData = new CourseInstanceIsvuData();
					isvuData.setId(id);
					isvuData.setData(data);
					ci.setIsvuData(isvuData);
					dh.getCourseInstanceDAO().save(em, isvuData);
				} else {
					ci.getIsvuData().setData(data);
				}
			}
		} catch(Exception ex) {
			return false;
		} finally {
			try { zipFile.close(); } catch(Exception ignorable) {}
		}
		return true;
	}

	@Deprecated
	public static void SynchronizeCourseStudentsData( final SynchronizeCourseStudentsData data, final Long userID, final String semester, final List<ISVUFileItemBean> items, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!task.equals("upload")) {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setCurrentSemesterID(BasicBrowsing.getCurrentSemesterID(em));
					return null;
				}
				if(StringUtil.isStringBlank(semester)) {
					data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, semester);
				if(ys==null) {
					data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				AuthType authType = DAOHelperFactory.getDAOHelper().getAuthTypeDAO().getByName(em, "ferweb://https://www.fer.hr/xmlrpc/xr_auth.php");
				if(authType==null) {
					data.getMessageLogger().addErrorMessage("Ne postoji trazeni autentifikacijski tip!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				SynchronizerService.synchronizeISVUFile(em, ys, authType, items);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void SynchronizeCourseLectureScheduleData(
			final SynchronizeCourseLectureScheduleData data, final Long userID,
			final String semester, final List<GroupScheduleBean> items,
			final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!task.equals("upload")) {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setCurrentSemesterID(BasicBrowsing.getCurrentSemesterID(em));
					return null;
				}
				if(StringUtil.isStringBlank(semester)) {
					data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, semester);
				if(ys==null) {
					data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				SynchronizerService.synchronizeCourseLectureSchedule(data.getMessageLogger(), em, ys.getId(), items);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getImportCourseMPConstraintsData(
			final ImportCourseMPConstraintsData data, final Long userID, final String semester,
			final List<ConstraintsImportBean> items, final String parentGroupRelativePath, final boolean resetCapacities,
			final boolean resetConstraints, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!task.equals("upload")) {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setCurrentSemesterID(BasicBrowsing.getCurrentSemesterID(em));
					return null;
				}
				if(StringUtil.isStringBlank(semester)) {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setCurrentSemesterID(BasicBrowsing.getCurrentSemesterID(em));
					data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(StringUtil.isStringBlank(parentGroupRelativePath)) {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setCurrentSemesterID(BasicBrowsing.getCurrentSemesterID(em));
					data.getMessageLogger().addErrorMessage("Nije odabrana roditeljska grupa!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, semester);
				if(ys==null) {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(SynchronizerService.getImportCourseMPConstraintsData(em, data.getMessageLogger(), data.getCurrentUser(), ys, items, parentGroupRelativePath, resetCapacities, resetConstraints)) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
				} else {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setCurrentSemesterID(ys.getId());
					data.setResult(AbstractActionData.RESULT_INPUT);
				}
				return null;
			}
		});
	}

	protected static boolean getImportCourseMPConstraintsData(EntityManager em, IMessageLogger messageLogger, User currentUser, YearSemester ys,
			List<ConstraintsImportBean> items, String parentGroupRelativePath, boolean resetCapacities, boolean resetConstraints) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<Group> allGroups = dh.getGroupDAO().findSubgroupsLLE(em, ys.getId()+"/%", parentGroupRelativePath, parentGroupRelativePath+"/%");
		Map<String, List<Group>> groupsByCourses = GroupUtil.mapGroupByCompositeCourseID(allGroups);
		Map<String, Group> courseMarketPlaces = GroupUtil.mapMarketPlacesByCompositeCourseID(allGroups);
		Map<String, List<ConstraintsImportBean>> constraintsMap = mapConstraintsImportBeanByCIID(ys.getId(), items);
		// Dohvati sve kolegije nad koje postavljamo ogranicenje
		Set<String> constrainedCourses = new HashSet<String>(constraintsMap.keySet());
		// Ukloni sve za koje imamo grupe
		constrainedCourses.removeAll(groupsByCourses.keySet());
		// Ako time nismo uklonili sve, to znaci da stavljamo ogranicenje na kolegij kojeg nema, ili za koji nemamo grupa
		if(!constrainedCourses.isEmpty()) {
			for(String key : constrainedCourses) {
				messageLogger.addErrorMessage("Za kolegij "+key+" je definirano ograničenje, no on ili ne postoji, ili nema niti jednu grupu.");
			}
			return false;
		}
		// Iteriraj po svim kolegijima za koje smo naveli ogranicenja:
		constrainedCourses = new HashSet<String>(constraintsMap.keySet());
		boolean error = false;
		for(String key : constrainedCourses) {
			error = !checkImportCourseMPConstraints(em, messageLogger, key, groupsByCourses.get(key), courseMarketPlaces.get(key), constraintsMap.get(key)) || error;
		}
		if(error) {
			return false;
		}
		for(String key : constrainedCourses) {
			doImportCourseMPConstraints(em, messageLogger, key, groupsByCourses.get(key), courseMarketPlaces.get(key), constraintsMap.get(key),resetCapacities, resetConstraints);
		}
		return true;
	}
	
	private static void doImportCourseMPConstraints(EntityManager em, IMessageLogger messageLogger, String courseInstanceID, List<Group> allGroups, Group parentGroup,
			List<ConstraintsImportBean> items, boolean resetCapacities, boolean resetConstraints) {
		if(allGroups==null || allGroups.isEmpty()) {
			messageLogger.addErrorMessage("Kolegij "+courseInstanceID+" nema grupa.");
			return;
		}
		if(parentGroup==null) {
			messageLogger.addErrorMessage("Kolegij "+courseInstanceID+" nema roditeljsku grupu.");
			return;
		}
		List<Group> childrenGroups = new ArrayList<Group>(allGroups.size());
		for(Group g : allGroups) {
			if(!g.isManagedRoot()) {
				childrenGroups.add(g);
			}
		}
		if(childrenGroups.isEmpty()) {
			messageLogger.addErrorMessage("Kolegij "+courseInstanceID+" nema djecu-grupe.");
			return;
		}
		Map<String, Group> mapByName = GroupUtil.mapGroupByName(childrenGroups);
		Set<String> capacitySetGroups = new HashSet<String>(allGroups.size());
		boolean constraintSet = false;
		if(!items.isEmpty()) {
			StringBuilder sb = new StringBuilder(5000);
			for(ConstraintsImportBean bean : items) {
				if(bean.getType()==1) {
					Group g = mapByName.get(bean.getConstraint());
					if(g==null) {
						// Ovo je nemoguce. No ipak, kad se dogodi:
						messageLogger.addErrorMessage("Grupa "+bean.getConstraint()+" nije pronađena, predmet "+bean.getIsvuCode()+". Prekidam obradu ovog kolegija.");
						return;
					}
					if(g.getCapacity()!=bean.getCount()) {
						g.setCapacity(bean.getCount());
					}
					capacitySetGroups.add(g.getName());
				} else if(bean.getType()==2) {
					sb.append(bean.getConstraint()).append("\r\n");
				} else {
					// I ovo je nemoguce. Kad se dogodi:
					messageLogger.addErrorMessage("Pronađeno ograničenje tipa "+bean.getType()+", predmet "+bean.getIsvuCode()+", što još nije podržano. Prekidam obradu ovog kolegija.");
					return;
				}
			}
			if(sb.length() > 0) {
				constraintSet = true;
				parentGroup.getMarketPlace().setFormulaConstraints(sb.toString());
			}
		}
		if(resetCapacities) {
			for(Group g : childrenGroups) {
				if(capacitySetGroups.contains(g.getName())) {
					continue;
				}
				g.setCapacity(-1);
			}
		}
		if(resetConstraints) {
			if(!constraintSet) {
				parentGroup.getMarketPlace().setFormulaConstraints(null);
			}
		}
	}
	
	private static boolean checkImportCourseMPConstraints(EntityManager em, IMessageLogger messageLogger, String courseInstanceID, List<Group> allGroups, Group parentGroup,
			List<ConstraintsImportBean> items) {
		if(items==null || items.isEmpty()) return true;
		StringBuilder sb = new StringBuilder(5000);
		for(ConstraintsImportBean bean : items) {
			if(bean.getType()==1) {
				sb.append('"').append(bean.getConstraint()).append('"').append(" <= ").append(bean.getCount()).append("\r\n");
			} else if(bean.getType()==2) {
				sb.append(bean.getConstraint()).append("\r\n");
			} else {
				messageLogger.addErrorMessage("Pronađeno ograničenje tipa "+bean.getType()+", predmet "+bean.getIsvuCode()+", što još nije podržano.");
				return false;
			}
		}
		boolean err = false;
		Map<String, Group> mapByName = GroupUtil.mapGroupByName(allGroups);
		try {
			MPFormulaConstraints cons = new MPFormulaConstraints(sb.toString());
			Set<String> groupNames = new HashSet<String>(16);
			cons.extractGroupNames(groupNames);
			for(String s : groupNames) {
				if(!mapByName.containsKey(s)) {
					messageLogger.addErrorMessage("Predmet "+items.get(0).getIsvuCode()+": nepostojeća grupa: "+s+".");
					err = true;
				}
			}
		} catch (ParseException e) {
			messageLogger.addErrorMessage("Predmet "+items.get(0).getIsvuCode()+": pogreška u tumačenju ograničenja: "+e.getMessage()+".");
			e.printStackTrace();
			return false;
		}
		return !err;
	}

	private static Map<String, List<ConstraintsImportBean>> mapConstraintsImportBeanByCIID(String semester, List<ConstraintsImportBean> items) {
		Map<String, List<ConstraintsImportBean>> map = new HashMap<String, List<ConstraintsImportBean>>(100);
		for(ConstraintsImportBean bean : items) {
			String key = semester + "/" + bean.getIsvuCode();
			List<ConstraintsImportBean> l = map.get(key);
			if(l==null) {
				l = new ArrayList<ConstraintsImportBean>();
				map.put(key, l);
			}
			l.add(bean);
		}
		return map;
	}

	public static void getSynchronizeRoomsData(final SynchronizeRoomsData data, final Long userID, final List<RoomBean> items) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(items==null || items.isEmpty()) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				for(RoomBean roomBean : items) {
					Room room = dh.getRoomDAO().get(em, roomBean.getId());
					if(room==null) {
						Venue venue = dh.getVenueDAO().get(em, roomBean.getVenueShortName());
						if(venue==null) {
							data.getMessageLogger().addWarningMessage("Venue "+roomBean.getVenueShortName()+" ne postoji. Preskačem sobu "+roomBean.getShortName()+".");
							continue;
						}
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
						room.setVenue(venue);
						dh.getRoomDAO().save(em, room);
					}
				}
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getSynchronizeLabScheduleData(
			final SynchronizeLabScheduleData data, final Long userID, final String semester,
			final List<LabScheduleBean> items, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!task.equals("upload")) {
					List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setCurrentSemesterID(BasicBrowsing.getCurrentSemesterID(em));
					return null;
				}
				if(StringUtil.isStringBlank(semester)) {
					data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, semester);
				if(ys==null) {
					data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				SynchronizerService.synchronizeLabSchedule(data.getMessageLogger(), em, ys.getId(), items);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	protected static void synchronizeLabSchedule(IMessageLogger messageLogger, EntityManager em, String yearSemesterID, List<LabScheduleBean> items) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		List<CourseComponentDescriptor> descriptors = dh.getCourseComponentDAO().listDescriptors(em);
		CourseComponentDescriptor labDescriptor = null;
		for(CourseComponentDescriptor d : descriptors) {
			if(d.getGroupRoot().equals("1")) {
				labDescriptor = d;
				break;
			}
		}
		YearSemester yearSemester = dh.getYearSemesterDAO().get(em, yearSemesterID);
		if(yearSemester==null) return;
		List<CourseInstance> instances = dh.getCourseInstanceDAO().findForSemester(em, yearSemesterID);
		Map<String, CourseInstance> courseByIsvuCodeMap = new HashMap<String, CourseInstance>(instances.size());
		for(CourseInstance ci : instances) {
			String isvuCode = ci.getCourse().getIsvuCode();
			courseByIsvuCodeMap.put(isvuCode, ci);
		}
		
		boolean fail = false;
		Set<String> knownRooms = new HashSet<String>();
		Map<String,Room> roomMap = new HashMap<String, Room>();
		Set<String> haveCourses = new HashSet<String>(100);
		for(LabScheduleBean bean : items) {
			haveCourses.add(bean.getIsvuCode());
			String key = bean.getVenue() + "|" + bean.getRoom();
			if(knownRooms.contains(key)) continue;
			Room room = dh.getRoomDAO().get(em, bean.getVenue(), bean.getRoom());
			if(room==null) {
				messageLogger.addInfoMessage("Soba " + key + " ne postoji u sustavu! Raspored NECE biti ucitan.");
				fail = true;
			} else {
				knownRooms.add(key);
				roomMap.put(key, room);
			}
		}
		
		if(labDescriptor==null) {
			messageLogger.addErrorMessage("Opisnik laboratorija nije pronaden. Raspored NECE biti ucitan.");
			fail = true;
		}
		if(fail) return;
		
		for(String isvuCode : haveCourses) {
			if(!courseByIsvuCodeMap.containsKey(isvuCode)) {
				messageLogger.addInfoMessage("Kolegij " + isvuCode + " ne postoji u sustavu! Raspored NECE biti ucitan.");
				fail = true;
			}
		}
		if(fail) return;

		List<User> existingUsers = dh.getYearSemesterDAO().findUsersInSemester(em, yearSemester);

		Map<String, Group> masterLabGroups = new HashMap<String, Group>(1000);
		for(String isvuCode : haveCourses) {
			String groupKey = yearSemesterID+"/"+isvuCode+"_"+"1";
			Group masterLabGroup = dh.getGroupDAO().get(em, yearSemesterID+"/"+isvuCode, "1");
			if(masterLabGroup==null) {
				masterLabGroup = new Group();
				masterLabGroup.setCompositeCourseID(courseByIsvuCodeMap.get(isvuCode).getId());
				masterLabGroup.setRelativePath("1");
				masterLabGroup.setName("Grupe za laboratorijske vježbe");
				masterLabGroup.setCapacity(-1);
				masterLabGroup.setEnteringAllowed(false);
				masterLabGroup.setLeavingAllowed(false);
				masterLabGroup.setManagedRoot(false);
				masterLabGroup.setParent(courseByIsvuCodeMap.get(isvuCode).getPrimaryGroup());
				dh.getGroupDAO().save(em, masterLabGroup);
				courseByIsvuCodeMap.get(isvuCode).getPrimaryGroup().getSubgroups().add(masterLabGroup);
			}
			masterLabGroups.put(groupKey, masterLabGroup);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Map<Group, Set<Group>> visitedGroups = new HashMap<Group, Set<Group>>();
		Set<Group> topGroups = new HashSet<Group>();

		Map<String, User> allUsers = UserUtil.mapUserByJmbag(existingUsers);

		// Sada sinkroniziraj raspored
		for(LabScheduleBean bean : items) {
			CourseInstance ci = courseByIsvuCodeMap.get(bean.getIsvuCode());
			String groupKey = yearSemesterID+"/"+bean.getIsvuCode()+"_"+"1";
			String topGroupKey = yearSemesterID+"/"+bean.getIsvuCode()+"_"+"1/"+bean.getLabNo();
			Group masterLabGroup = masterLabGroups.get(groupKey);
			Group topLabGroup = masterLabGroups.get(topGroupKey);
			if(topLabGroup==null) {
				topLabGroup = dh.getGroupDAO().get(em, yearSemesterID+"/"+bean.getIsvuCode(), "1/"+bean.getLabNo());
				if(topLabGroup==null) {
					topLabGroup = new Group();
					topLabGroup.setCompositeCourseID(courseByIsvuCodeMap.get(bean.getIsvuCode()).getId());
					topLabGroup.setRelativePath("1/"+bean.getLabNo());
					topLabGroup.setName("Lab "+bean.getLabNo());
					topLabGroup.setCapacity(-1);
					topLabGroup.setEnteringAllowed(false);
					topLabGroup.setLeavingAllowed(false);
					topLabGroup.setManagedRoot(true);
					topLabGroup.setParent(masterLabGroup);
					dh.getGroupDAO().save(em, topLabGroup);
					masterLabGroup.getSubgroups().add(topLabGroup);
				}
				masterLabGroups.put(topGroupKey, topLabGroup);
				topGroups.add(topLabGroup);
			}
			/*
			 * Vazno:
			 * 
			 * morao sam prosiriti naziv grupe s krajem, zato sto na popisu osnova elektrotehnike ima studenata koji u isto vrijeme
			 * pocinju labos u istoj dvorani, ali zatim neki ostaju jedan termin, a neki vise njih, pa to treba tretirati kao
			 * razlicite grupe. Pametnije bi to bilo zabraniti i traziti da se to popuni po normalnim terminima, ali sad sta je tu je...
			 */
			String groupName = bean.getDate()+" "+bean.getStart()+" "+calculateEnd(bean.getStart(), bean.getDuration())+" "+bean.getRoom();
			Group g = null;
			int max = 0;
			for(Group g2 : topLabGroup.getSubgroups()) {
				if(g2.getName().equals(groupName)) {
					g = g2;
					break;
				}
				int br = Integer.parseInt(g2.getRelativePath().substring(g2.getRelativePath().lastIndexOf('/')+1));
				if(br>max) max = br;
			}
			if(g==null) {
				g = new Group();
				g.setCompositeCourseID(courseByIsvuCodeMap.get(bean.getIsvuCode()).getId());
				g.setRelativePath("1/"+bean.getLabNo()+"/"+(max+1));
				g.setName(groupName);
				g.setCapacity(-1);
				g.setEnteringAllowed(false);
				g.setLeavingAllowed(false);
				g.setManagedRoot(false);
				g.setParent(topLabGroup);
				dh.getGroupDAO().save(em, g);
				em.flush();
				topLabGroup.getSubgroups().add(g);
			}
			Set<Group> visited = visitedGroups.get(topLabGroup);
			if(visited == null) {
				visited = new HashSet<Group>();
				visitedGroups.put(topLabGroup, visited);
			}
			visited.add(g);

			Set<String> foundJMBAGsInBean = new HashSet<String>(100);
			for(CategoryStudents cs : bean.getStudents()) {
				for(String jmbag : cs.getJmbags()) {
					foundJMBAGsInBean.add(jmbag);
				}
			}
			Map<String,UserGroup> ugMap = new HashMap<String, UserGroup>(g.getUsers().size());
			boolean anyRemoved = false;
			List<Object[]> toRemoveList = null;
			for(UserGroup ug : g.getUsers()) {
				if(foundJMBAGsInBean.contains(ug.getUser().getJmbag())) {
					ugMap.put(ug.getUser().getJmbag(), ug);
					continue;
				}
				System.out.println("kolegij "+bean.getIsvuCode()+", lab: "+bean.getLabNo()+", grupa: "+groupName+", micem "+ug.getUser().getJmbag());
				if(toRemoveList==null) {
					toRemoveList = new ArrayList<Object[]>();
				}
				toRemoveList.add(new Object[] {ug.getGroup().getUsers(), ug});
				//ug.getGroup().getUsers().remove(ug);
				anyRemoved = true;
			}
			if(anyRemoved) {
				for(Object[] o : toRemoveList) {
					Collection<?> set = (Collection<?>)o[0];
					Object obj = o[1];
					set.remove(obj);
				}
				toRemoveList = null;
				em.flush();
			}

			for(CategoryStudents cs : bean.getStudents()) {
				for(String jmbag : cs.getJmbags()) {
					UserGroup ug = ugMap.get(jmbag);
					if(ug!=null) {
						if(StringUtil.stringEquals(ug.getTag(), cs.getCategory())) {
							continue;
						}
						ug.setTag(cs.getCategory());
						continue;
					}
					User user = allUsers.get(jmbag);
					if(user==null) {
						messageLogger.addInfoMessage("Labos " + bean.getIsvuCode() + "/"+bean.getLabNo()+"/"+ bean.getDate()+"/"+bean.getStart()+"/"+bean.getDuration()+"/"+bean.getRoom()+": student ne postoji u bazi: "+jmbag);
						continue;
					}
					ug = new UserGroup();
					ug.setUser(user);
					ug.setGroup(g);
					ug.setTag(cs.getCategory());
					dh.getUserGroupDAO().save(em, ug);
					ug.getGroup().getUsers().add(ug);
					ugMap.put(user.getJmbag(), ug);
				}
			}
			
			if(g.getEvents().isEmpty()) {
				GroupWideEvent gwe = new GroupWideEvent();
				gwe.setDuration(bean.getDuration());
				gwe.setIssuer(null);
				gwe.setRoom(roomMap.get(bean.getVenue() + "|" + bean.getRoom()));
				gwe.setSpecifier(yearSemesterID+"/satnica/L");
				try {
					gwe.setStart(sdf.parse(bean.getDate()+" "+bean.getStart()+":00"));
				} catch (ParseException e) {
					e.printStackTrace();
					messageLogger.addInfoMessage("Labos " + bean.getIsvuCode() + "/"+bean.getLabNo()+"/"+ bean.getDate()+"/"+bean.getStart()+"/"+bean.getDuration()+"/"+bean.getRoom()+". Dogadaj nije objavljen. Date parse exception. "+e.getMessage());
					continue;
				}
				gwe.setStrength(EventStrength.STRONG);
				gwe.setTitle(ci.getCourse().getName()+" - lab. vježba "+bean.getLabNo());
				gwe.setContext("c_"+labDescriptor.getShortName()+":"+ci.getId()+":"+topLabGroup.getRelativePath());
				dh.getEventDAO().save(em, gwe);
				gwe.getGroups().add(g);
				g.getEvents().add(gwe);
			}
		}
		
		for(Group topGroup : topGroups) {
			Set<Group> visited = visitedGroups.get(topGroup);
			if(visited==null) {
				messageLogger.addInfoMessage("Grupa " + topGroup.getCompositeCourseID() + ", "+topGroup.getRelativePath()+" nema posjecene djece. Nista necu brisati u toj grupi.");
				continue;
			}
			Iterator<Group> it = topGroup.getSubgroups().iterator();
			while(it.hasNext()) {
				Group g = it.next();
				// Ako sam obradio tu grupu
				if(visited.contains(g)) {
					continue;
				}
				// Inace o toj grupi ne znam nista? Obrisi je!
				List<UserGroup> uglist = new ArrayList<UserGroup>(g.getUsers());
				g.getUsers().clear();
				em.flush();
				for(UserGroup ug : uglist) {
					dh.getUserGroupDAO().remove(em, ug);
				}
				it.remove();
				em.flush();
				List<GroupWideEvent> gweList = new ArrayList<GroupWideEvent>(g.getEvents());
				for(GroupWideEvent gwe : gweList) {
					for(Group gweg : gwe.getGroups()) {
						gweg.getEvents().remove(gwe);
					}
					gwe.getGroups().clear();
					em.flush();
					dh.getEventDAO().remove(em, gwe);
				}
				dh.getGroupDAO().remove(em, g);
				em.flush();
			}
		}
	}

	@Deprecated
	private static String calculateEnd(String start, int duration) {
		int h = Integer.parseInt(start.substring(0,2));
		int m = Integer.parseInt(start.substring(3));
		int n = h*60+m+duration;
		h = n / 60;
		m = n - h*60;
		StringBuilder sb = new StringBuilder(5);
		if(h<10) sb.append('0');
		sb.append(h);
		sb.append(':');
		if(m<10) sb.append('0');
		sb.append(m);
		return sb.toString();
	}
	
	public static void getUpdateCourseInstanceRolesData(
			final UpdateCourseInstanceRolesData data, final Long userID, final String semester,
			final String text, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
				data.setAllYearSemesters(list);
				data.setCurrentSemesterID(BasicBrowsing.getCurrentSemesterID(em));
				
				if(task.equals("input")) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}

				if(!task.equals("syncCIRoles")) {
					data.getMessageLogger().addErrorMessage("Nepoznat zadatak!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				List<CourseUserRoleBean> items = null;
				try {
					items = CourseUserRoleParser.parseTabbedFormat(new StringReader(text==null ? "" : text));
				} catch(IOException ex) {
					data.getMessageLogger().addErrorMessage("Format podataka je neispravan: "+ex.getMessage());
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}

				if(StringUtil.isStringBlank(semester)) {
					data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, semester);
				if(ys==null) {
					data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(SynchronizerService.synchronizeCourseUserRole(data.getMessageLogger(), em, ys.getId(), items)) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
				} else {
					data.setResult(AbstractActionData.RESULT_INPUT);
				}
				return null;
			}
		});
	}

	protected static boolean synchronizeCourseUserRole(IMessageLogger messageLogger, EntityManager em, String semesterID, List<CourseUserRoleBean> items) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		// Mapa kolegij, lista
		Map<String,List<CourseUserRoleBean>> mapa = new HashMap<String, List<CourseUserRoleBean>>();
		Set<String> validRoles = new HashSet<String>();
		validRoles.add("L"); // - asistent
		validRoles.add("P"); // - nastavnik
		validRoles.add("N"); // - nositelj
		validRoles.add("A"); // - asistent
		validRoles.add("-S"); // - asistent organizator
		Set<String> invalidRoles = new HashSet<String>();
		
		for(CourseUserRoleBean r : items) {
			if(!validRoles.contains(r.getRole())) {
				if(!invalidRoles.contains(r.getRole())) {
					invalidRoles.add(r.getRole());
					messageLogger.addErrorMessage("Pronadena nepoznata uloga: "+r.getRole());
				}
				continue;
			}
			List<CourseUserRoleBean> list = mapa.get(r.getCourseInstanceID());
			if(list==null) {
				list = new ArrayList<CourseUserRoleBean>();
				mapa.put(r.getCourseInstanceID(), list);
			}
		}
		if(!invalidRoles.isEmpty()) {
			messageLogger.addInfoMessage("Sinkronizacija nije obavljena.");
			return false;
		}
		
		Set<String> invalidCourses = new HashSet<String>(200);
		CourseInstance falseCI = new CourseInstance();

		List<String[]> secGroups = new ArrayList<String[]>();
		for(int i = 0; i < JCMSSecurityConstants.getSecurityCourseRolesCount(); i++) {
			secGroups.add(new String[] {JCMSSecurityConstants.getSecurityCourseRole(i),JCMSSecurityConstants.getSecurityCourseRoleName(i)});
		}
		Map<String, CourseInstance> ciMap = new HashMap<String, CourseInstance>(200);
		Map<String, User> userMap = new HashMap<String, User>(500);
		Map<String, Map<String,Group>> courseGroupMap = new HashMap<String, Map<String,Group>>(200);
		User falseUser = new User();
		for(CourseUserRoleBean r : items) {
			User u = userMap.get(r.getUserJMBAG());
			if(u==null) {
				u = dh.getUserDAO().getUserByJMBAG(em, r.getUserJMBAG());
				if(u==null) {
					u = falseUser;
					userMap.put(r.getUserJMBAG(), u);
					messageLogger.addErrorMessage("Pronaden nepoznat korisnik: "+r.getUserJMBAG());
					continue;
				}
				userMap.put(r.getUserJMBAG(), u);
			}
			if(u==falseUser) {
				continue;
			}
			CourseInstance ci = ciMap.get(r.getCourseInstanceID());
			if(ci==null) {
				ci = dh.getCourseInstanceDAO().get(em, semesterID+"/"+r.getCourseInstanceID());
				if(ci==null) {
					ci = falseCI;
					ciMap.put(r.getCourseInstanceID(), ci);
					if(!invalidCourses.contains(r.getCourseInstanceID())) {
						invalidCourses.add(r.getCourseInstanceID());
						messageLogger.addErrorMessage("Pronaden nepoznat kolegij: "+r.getCourseInstanceID());
					}
					continue;
				}
				Map<String,Group> courseGroups = new HashMap<String, Group>(10);
				courseGroupMap.put(r.getCourseInstanceID(), courseGroups);
				Group mainGroup = null;
				for(Group g : ci.getPrimaryGroup().getSubgroups()) {
					if(g.getRelativePath().equals(JCMSSecurityConstants.SEC_ROLE_GROUP)) {
						mainGroup = g;
						break;
					}
				}
				if(mainGroup==null) {
					// Nema glavne grupe!
					mainGroup = new Group();
					mainGroup.setName(JCMSSecurityConstants.SEC_ROLE_GROUP_NAME);
					mainGroup.setCapacity(-1);
					mainGroup.setCompositeCourseID(ci.getId());
					mainGroup.setEnteringAllowed(false);
					mainGroup.setLeavingAllowed(false);
					mainGroup.setParent(ci.getPrimaryGroup());
					mainGroup.setRelativePath(JCMSSecurityConstants.SEC_ROLE_GROUP);
					dh.getGroupDAO().save(em, mainGroup);
					ci.getPrimaryGroup().getSubgroups().add(mainGroup);
				}
				Set<String> foundGroups = new HashSet<String>();
				for(Group g : mainGroup.getSubgroups()) {
					courseGroups.put(g.getRelativePath(), g);
					foundGroups.add(g.getRelativePath());
				}
				for(String[] gv : secGroups) {
					if(foundGroups.contains(gv[0])) continue;
					Group  gr = new Group();
					gr.setName(gv[1]);
					gr.setCapacity(-1);
					gr.setCompositeCourseID(ci.getId());
					gr.setEnteringAllowed(false);
					gr.setLeavingAllowed(false);
					gr.setParent(mainGroup);
					gr.setRelativePath(gv[0]);
					dh.getGroupDAO().save(em, gr);
					mainGroup.getSubgroups().add(gr);
					courseGroups.put(gr.getRelativePath(), gr);
				}
			} else if(ci==falseCI) {
				continue;
			}
			// Inace imam pravi kolegij:
			String groupName = null;
			if(r.getRole().equals("N")) {
				groupName = JCMSSecurityConstants.NOSITELJ;
			}
			if(r.getRole().equals("A")) {
				groupName = JCMSSecurityConstants.ASISTENT;
			}
			if(r.getRole().equals("L")) {
				groupName = JCMSSecurityConstants.ASISTENT;
			}
			if(r.getRole().equals("P")) {
				groupName = JCMSSecurityConstants.NASTAVNIK;
			}
			if(r.getRole().equals("-S")) {
				groupName = JCMSSecurityConstants.ASISTENT_ORG;
			}
			if(groupName==null) {
				System.out.println("groupName==null; ovo se nije smjelo dogoditi!");
				continue;
			}
			
			Map<String, Group> courseGroups = courseGroupMap.get(r.getCourseInstanceID());
			if(courseGroups==null) {
				System.out.println("courseGroups==null; ovo se nije smjelo dogoditi!");
				continue;
			}
			Group group = courseGroups.get(groupName);
			if(group==null) {
				System.out.println("group==null; ovo se nije smjelo dogoditi!");
				continue;
			}
//			
//			
//			Group mainGroup = null;
//			for(Group g : ci.getPrimaryGroup().getSubgroups()) {
//				if(g.getRelativePath().equals(JCMSSecurityConstants.SEC_ROLE_GROUP)) {
//					mainGroup = g;
//					break;
//				}
//			}
//			if(mainGroup==null) {
//				System.out.println("mainGroup==null; ovo se nije smjelo dogoditi!");
//				continue;
//			}
//			Group group = null;
//			for(Group g : mainGroup.getSubgroups()) {
//				if(g.getRelativePath().equals(groupName)) {
//					group = g;
//					break;
//				}
//			}
//			if(group==null) {
//				System.out.println("group==null; ovo se nije smjelo dogoditi!");
//				continue;
//			}
			UserGroup ug = null;
			for(UserGroup ug2 : group.getUsers()) {
				if(ug2.getUser().equals(u)) {
					ug = ug2;
					continue;
				}
			}
			if(ug!=null) continue;
			ug = new UserGroup();
			ug.setGroup(group);
			ug.setUser(u);
			dh.getUserGroupDAO().save(em, ug);
			group.getUsers().add(ug);
		}		
		return true;
	}

}
