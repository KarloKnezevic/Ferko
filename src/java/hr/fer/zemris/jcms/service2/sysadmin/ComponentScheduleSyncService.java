package hr.fer.zemris.jcms.service2.sysadmin;

import java.io.IOException;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.activities.types.ComponentGroupActivity;
import hr.fer.zemris.jcms.beans.ext.GroupScheduleBean;
import hr.fer.zemris.jcms.beans.ext.LabScheduleBean;
import hr.fer.zemris.jcms.beans.ext.LabScheduleBean.CategoryStudents;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.CourseComponentDescriptor;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.parsers.LabScheduleTextListParser;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeLabScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.DummyMessageLoggerImpl;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

/**
 * Usluga koja obavlja sinkronizaciju rasporeda studenata za komponente kolegija; primjerice, rasporeda labosa.
 * 
 * @author marcupic
 *
 */
public class ComponentScheduleSyncService {

	public static void prepareComponentScheduleSync(EntityManager em, SynchronizeLabScheduleData data) {
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(BasicServiceSupport.getCurrentSemesterID(em));
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void componentScheduleSync(EntityManager em, SynchronizeLabScheduleData data) {
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(data.getSemester());

		if(StringUtil.isStringBlank(data.getSemester())) {
			data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, data.getSemester());
		if(ys==null) {
			data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		List<LabScheduleBean> items = null;
		try {
			items = LabScheduleTextListParser.parseTabbedFormat(new StringReader(data.getText()==null ? "" : data.getText()));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Format podataka je neispravan!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		synchronizeLabSchedule(data.getMessageLogger(), em, ys.getId(), items);
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Wrapper metode kako bi se omogućilo pozivanje na stari nacin (primjerice, za potrebe inicijalnog stvaranja baze kod
	 * razvoja, gdje bazu punimo unaprijed pripremljenim demo podatcima).
	 * 
	 * @param yearSemesterID
	 * @param scheduleItems
	 */
	public static void synchronizeLabSchedule(final String yearSemesterID, final List<LabScheduleBean> scheduleItems) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				synchronizeLabSchedule(JCMSSettings.getSettings().getI18nLogger(), em, yearSemesterID, scheduleItems);
				return null;
			}
		});
	}

	public static class Helper {
		private Map<String, Group> masterGroups = new HashMap<String, Group>(1000);
		private String yearSemesterID;
		private Map<String,CourseComponentDescriptor> descriptorByShortName;
		private DAOHelper dh;
		private EntityManager em;
		private Map<String, CourseInstance> courseByIsvuCodeMap;
		private Set<Group> requestedTopGroups = new HashSet<Group>(1000);
		
		public Helper(DAOHelper dh, EntityManager em, String yearSemesterID, Map<String, CourseComponentDescriptor> descriptorByShortName, Map<String, CourseInstance> courseByIsvuCodeMap) {
			super();
			this.dh = dh;
			this.em = em;
			this.yearSemesterID = yearSemesterID;
			this.descriptorByShortName = descriptorByShortName;
			this.courseByIsvuCodeMap = courseByIsvuCodeMap;
		}
		
		public String groupKey(String kind, String isvuCode) {
			CourseComponentDescriptor ccd = descriptorByShortName.get(kind);
			return yearSemesterID+"/"+isvuCode+"_"+ccd.getGroupRoot();
		}
		
		public String groupKey(String kind, String isvuCode, int position) {
			CourseComponentDescriptor ccd = descriptorByShortName.get(kind);
			if(ccd==null) {
				System.out.println("descriptor for kind '"+kind+"' is null!");
			}
			return yearSemesterID+"/"+isvuCode+"_"+ccd.getGroupRoot()+"/"+position;
		}
		
		public Group getComponentRootGroup(String kind, String isvuCode) {
			String groupKey = groupKey(kind, isvuCode);
			Group masterLabGroup = masterGroups.get(groupKey);
			if(masterLabGroup!=null) return masterLabGroup;
			CourseComponentDescriptor ccd = descriptorByShortName.get(kind);
			masterLabGroup = dh.getGroupDAO().get(em, yearSemesterID+"/"+isvuCode, ccd.getGroupRoot());
			if(masterLabGroup==null) {
				masterLabGroup = new Group();
				masterLabGroup.setCompositeCourseID(courseByIsvuCodeMap.get(isvuCode).getId());
				masterLabGroup.setRelativePath(ccd.getGroupRoot());
				masterLabGroup.setName(getComponentRootGroupName(ccd));
				masterLabGroup.setCapacity(-1);
				masterLabGroup.setEnteringAllowed(false);
				masterLabGroup.setLeavingAllowed(false);
				masterLabGroup.setManagedRoot(false);
				masterLabGroup.setParent(courseByIsvuCodeMap.get(isvuCode).getPrimaryGroup());
				dh.getGroupDAO().save(em, masterLabGroup);
				courseByIsvuCodeMap.get(isvuCode).getPrimaryGroup().getSubgroups().add(masterLabGroup);
			}
			masterGroups.put(groupKey, masterLabGroup);
			return masterLabGroup;
		}
		
		public static String getComponentRootGroupName(CourseComponentDescriptor ccd) {
			if(ccd.getShortName().equals("LAB")) return "Grupe za laboratorijske vježbe";
			if(ccd.getShortName().equals("ZAD")) return "Grupe za domaće zadaće";
			if(ccd.getShortName().equals("SEM")) return "Grupe za seminarske radove";
			return "Grupe za komponentu "+ccd.getShortName();
		}

		public Group getComponentTopGroup(String kind, String isvuCode, int position) {
			String topGroupKey = groupKey(kind, isvuCode,position);
			Group topLabGroup = masterGroups.get(topGroupKey);
			if(topLabGroup==null) {
				Group masterLabGroup = getComponentRootGroup(kind, isvuCode);
				CourseComponentDescriptor ccd = descriptorByShortName.get(kind);
				topLabGroup = dh.getGroupDAO().get(em, yearSemesterID+"/"+isvuCode, ccd.getGroupRoot()+"/"+position);
				if(topLabGroup==null) {
					topLabGroup = new Group();
					topLabGroup.setCompositeCourseID(courseByIsvuCodeMap.get(isvuCode).getId());
					topLabGroup.setRelativePath(ccd.getGroupRoot()+"/"+position);
					topLabGroup.setName(position+". "+ccd.getPositionalName());
					topLabGroup.setCapacity(-1);
					topLabGroup.setEnteringAllowed(false);
					topLabGroup.setLeavingAllowed(false);
					topLabGroup.setManagedRoot(true);
					topLabGroup.setParent(masterLabGroup);
					dh.getGroupDAO().save(em, topLabGroup);
					masterLabGroup.getSubgroups().add(topLabGroup);
				}
				masterGroups.put(topGroupKey, topLabGroup);
				requestedTopGroups.add(topLabGroup);
			}
			return topLabGroup;
		}

		public String getShortName(CourseComponentDescriptor ccd) {
			if(ccd.getShortName().equals("LAB")) return "lab. vježba";
			if(ccd.getShortName().equals("ZAD")) return "dom. zadaća";
			if(ccd.getShortName().equals("SEM")) return "sem. rad";
			return ccd.getShortName();
		}
		
		public Set<Group> getRequestedTopGroups() {
			return requestedTopGroups;
		}

		public String getSpecifierPart(CourseComponentDescriptor ccd) {
			if(ccd.getShortName().equals("LAB")) return "L";
			if(ccd.getShortName().equals("ZAD")) return "Z";
			if(ccd.getShortName().equals("SEM")) return "S";
			return "?";
		}
	}
	
	private static void synchronizeLabSchedule(IMessageLogger messageLogger, EntityManager em, String yearSemesterID, List<LabScheduleBean> items) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		List<CourseComponentDescriptor> descriptors = dh.getCourseComponentDAO().listDescriptors(em);
		Map<String,CourseComponentDescriptor> descriptorByShortName = new HashMap<String, CourseComponentDescriptor>();
		for(CourseComponentDescriptor d : descriptors) {
			descriptorByShortName.put(d.getShortName(), d);
		}
		// Skup komponenti koje ova metoda podržava - zbog nazivlja!
		// Ako se ovo mijenja, obavezno promijeniti i Helper.getComponentRootGroup
		Set<String> supportedComponents = new HashSet<String>();
		supportedComponents.add("LAB");
		supportedComponents.add("ZAD");
		supportedComponents.add("SEM");
		
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
			if(!descriptorByShortName.keySet().contains(bean.getKind())) {
				messageLogger.addInfoMessage("Vrsta komponente " + bean.getKind() + " ne postoji u sustavu! Raspored NECE biti ucitan.");
				fail = true;
			} else if(!supportedComponents.contains(bean.getKind())) {
				messageLogger.addInfoMessage("Vrsta komponente " + bean.getKind() + " postoji u sustavu ali nije podržana od metode za unos rasporeda! Raspored NECE biti ucitan.");
				fail = true;
			}
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
		Helper helper = new Helper(dh, em, yearSemesterID, descriptorByShortName, courseByIsvuCodeMap);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Map<Group, Set<Group>> visitedGroups = new HashMap<Group, Set<Group>>();

		Map<String, User> allUsers = UserUtil.mapUserByJmbag(existingUsers);

		Date now = new Date();

		Map<Long, Object[]> courseInstanceByTopID = new HashMap<Long, Object[]>(100);
		
		// Sada sinkroniziraj raspored
		for(LabScheduleBean bean : items) {
			CourseComponentDescriptor ccd = descriptorByShortName.get(bean.getKind());
			CourseInstance ci = courseByIsvuCodeMap.get(bean.getIsvuCode());
			Group topLabGroup = helper.getComponentTopGroup(bean.getKind(), bean.getIsvuCode(), bean.getLabNo());
			Object[] o2 = courseInstanceByTopID.get(topLabGroup.getId());
			if(o2==null) {
				courseInstanceByTopID.put(topLabGroup.getId(), new Object[] {ci, ccd, Integer.valueOf(bean.getLabNo())});
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
				g.setRelativePath(topLabGroup.getRelativePath()+"/"+(max+1));
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
				System.out.println("kolegij "+bean.getIsvuCode()+", "+bean.getKind()+": "+bean.getLabNo()+", grupa: "+groupName+", micem "+ug.getUser().getJmbag());
				if(toRemoveList==null) {
					toRemoveList = new ArrayList<Object[]>();
				}
				toRemoveList.add(new Object[] {ug.getGroup().getUsers(), ug});
				//ug.getGroup().getUsers().remove(ug);
				anyRemoved = true;
			}
			if(anyRemoved) {
				for(Object[] o : toRemoveList) {
					UserGroup ug = (UserGroup)o[1];
					MarketPlace mp = ug.getGroup().getParent().getMarketPlace();
					if(mp!=null) {
						dh.getMarketPlaceDAO().clearAllOffersForUser(em, mp, ug.getUser(), ug.getGroup());
					}
					Collection<?> set = (Collection<?>)o[0];
					Object obj = o[1];
					set.remove(obj);
					JCMSSettings.getSettings().getActivityReporter().addActivity(new ComponentGroupActivity(now, ci.getId(), ug.getUser().getId(), 2, ug.getGroup().getName(), ug.getGroup().getParent().getName(), ccd.getGroupRoot(), bean.getLabNo()));
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
						messageLogger.addInfoMessage(bean.getKind() + " " + bean.getIsvuCode() + "/"+bean.getLabNo()+"/"+ bean.getDate()+"/"+bean.getStart()+"/"+bean.getDuration()+"/"+bean.getRoom()+": student ne postoji u bazi: "+jmbag);
						continue;
					}
					ug = new UserGroup();
					ug.setUser(user);
					ug.setGroup(g);
					ug.setTag(cs.getCategory());
					dh.getUserGroupDAO().save(em, ug);
					ug.getGroup().getUsers().add(ug);
					ugMap.put(user.getJmbag(), ug);
					JCMSSettings.getSettings().getActivityReporter().addActivity(new ComponentGroupActivity(now, ci.getId(), ug.getUser().getId(), 1, ug.getGroup().getName(), ug.getGroup().getParent().getName(), ccd.getGroupRoot(), bean.getLabNo()));
				}
			}
			
			if(g.getEvents().isEmpty()) {
				GroupWideEvent gwe = new GroupWideEvent();
				gwe.setDuration(bean.getDuration());
				gwe.setIssuer(null);
				gwe.setRoom(roomMap.get(bean.getVenue() + "|" + bean.getRoom()));
				gwe.setSpecifier(yearSemesterID+"/satnica/"+helper.getSpecifierPart(ccd));
				try {
					gwe.setStart(sdf.parse(bean.getDate()+" "+bean.getStart()+":00"));
				} catch (ParseException e) {
					e.printStackTrace();
					messageLogger.addInfoMessage("Labos " + bean.getIsvuCode() + "/"+bean.getLabNo()+"/"+ bean.getDate()+"/"+bean.getStart()+"/"+bean.getDuration()+"/"+bean.getRoom()+". Dogadaj nije objavljen. Date parse exception. "+e.getMessage());
					continue;
				}
				gwe.setStrength(EventStrength.STRONG);
				gwe.setTitle(ci.getCourse().getName()+" - "+helper.getShortName(ccd)+" "+bean.getLabNo());
				gwe.setContext("c_"+ccd.getShortName()+":"+ci.getId()+":"+topLabGroup.getRelativePath());
				dh.getEventDAO().save(em, gwe);
				gwe.getGroups().add(g);
				g.getEvents().add(gwe);
			}
		}
		
		for(Group topGroup : helper.getRequestedTopGroups()) {
			Set<Group> visited = visitedGroups.get(topGroup);
			if(visited==null) {
				messageLogger.addInfoMessage("Grupa " + topGroup.getCompositeCourseID() + ", "+topGroup.getRelativePath()+" nema posjecene djece. Nista necu brisati u toj grupi.");
				continue;
			}
			Object[] o = courseInstanceByTopID.get(topGroup.getId());
			Iterator<Group> it = topGroup.getSubgroups().iterator();
			while(it.hasNext()) {
				Group g = it.next();
				// Ako sam obradio tu grupu
				if(visited.contains(g)) {
					continue;
				}
				CourseInstance ci = (CourseInstance)o[0];
				CourseComponentDescriptor ccd = (CourseComponentDescriptor)o[1];
				Integer labNo = (Integer)o[2];
				// Inace o toj grupi ne znam nista? Obrisi je!
				List<UserGroup> uglist = new ArrayList<UserGroup>(g.getUsers());
				MarketPlace mp = g.getParent().getMarketPlace();
				for(UserGroup ug : uglist) {
					if(mp!=null) {
						dh.getMarketPlaceDAO().clearAllOffersForUser(em, mp, ug.getUser(), ug.getGroup());
					}
					JCMSSettings.getSettings().getActivityReporter().addActivity(new ComponentGroupActivity(now, ci.getId(), ug.getUser().getId(), 2, ug.getGroup().getName(), ug.getGroup().getParent().getName(), ccd.getGroupRoot(), labNo.intValue()));
				}
				
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

}
