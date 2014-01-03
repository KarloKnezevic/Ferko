package hr.fer.zemris.jcms.periodicals.impl;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import hr.fer.zemris.jcms.beans.SeminarGroupsInfoBean;
import hr.fer.zemris.jcms.beans.SeminarScheduleInfoBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.SeminarInfo;
import hr.fer.zemris.jcms.model.SeminarRoot;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.parsers.SeminarGroupsInfoParser;
import hr.fer.zemris.jcms.parsers.SeminarScheduleInfoParser;
import hr.fer.zemris.jcms.periodicals.IPeriodicalService;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.util.StringUtil;

public class SeminarImporter implements IPeriodicalService {

	private static final Logger logger = Logger.getLogger(SeminarImporter.class.getCanonicalName());
	private boolean inProgress = false;
	
	@Override
	public void destroy() {
		logger.info("SeminarImporter destroyed.");
	}

	@Override
	public void init() {
		logger.info("SeminarImporter started.");
	}

	@Override
	public void passMessage(String key, String value) {
		logger.info("SeminarImporter got message key="+key+", value="+value+".");
		if("run".equals(key)) {
			periodicalExecute();
		}
	}

	@Override
	public void periodicalExecute() {
		synchronized(this) {
			if(inProgress) {
				logger.warn("SeminarImporter periodicalExecute skipped, since inProgrss flag is still set!");
				return;
			}
			inProgress = true;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = new Date();
		logger.info("SeminarImporter periodicalExecute called at "+sdf.format(startDate)+".");
		try {
			synchronize();
		} catch(Exception ex) {
			logger.error("SeminarImporter periodicalExecute exception.", ex);
		} finally {
			Date endDate = new Date();
			logger.info("SeminarImporter periodicalExecute finished at "+sdf.format(endDate)+". Duration was "+((double)(endDate.getTime()-startDate.getTime())/1000.0)+" seconds.");
			synchronized(this) {
				inProgress = false;
			}
		}
	}

	private void synchronize() {
		
		List<SeminarRoot> rootList = PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<List<SeminarRoot>>() {
			@Override
			public List<SeminarRoot> executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				List<SeminarRoot> roots = dh.getSeminarDAO().findActiveSeminarRoots(em);
				Iterator<SeminarRoot> it = roots.iterator();
				while(it.hasNext()) {
					SeminarRoot root = it.next();
					if(!root.isActive()) {
						it.remove();
					}
				}
				return roots;
			}
		});
		
		if(rootList==null) return;
		
		for(SeminarRoot root : rootList) {
			String source = root.getSource();
			String[] parts = source.split(" ");
			if(parts[0].equals("ferweb_v1")) {
				String grupeURL = parts[1];
				String terminiURL = parts[2];
				List<SeminarGroupsInfoBean> groupsInfoBeans = readGroupsInfoBeans(grupeURL);
				List<SeminarScheduleInfoBean> scheduleInfoBeans = readScheduleInfoBeans(terminiURL);
				if(groupsInfoBeans==null || scheduleInfoBeans==null) {
					continue;
				}
				logger.info("SeminarImporter found "+groupsInfoBeans.size()+" group records and "+scheduleInfoBeans.size()+" schedule records.");
				syncSeminarRoot(root.getId(), groupsInfoBeans,scheduleInfoBeans);
			} else {
				logger.warn("SeminarImporter unsupported source: "+parts[0]+". Skipping.");
				continue;
			}
		}
	}

	private void syncSeminarRoot(final Long rootID, final List<SeminarGroupsInfoBean> groupsInfoBeans,final List<SeminarScheduleInfoBean> scheduleInfoBeans) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				SeminarRoot root = dh.getSeminarDAO().getSeminarRoot(em, rootID);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				List<Room> roomList = dh.getRoomDAO().listByVenue(em, "FER");
				Map<String,Room> roomsMapByName = new HashMap<String, Room>(roomList.size());
				for(Room r : roomList) {
					roomsMapByName.put(r.getShortName(), r);
				}
				
				// ----------- Izracunaj koje grupe treba dodati / ukloniti -----------------------
				
				// Mapa<NazivGrupe,Grupa>
				Map<String,Group> subGroupsMapByName = new HashMap<String, Group>(100);
				for(Group g : root.getRootGroup().getSubgroups()) {
					subGroupsMapByName.put(g.getName(), g);
				}
				
				// Mapa JMBAG,SeminarInfo
				Map<String,SeminarInfo> seminarInfoMapByJMBAG = new HashMap<String, SeminarInfo>(2000);
				for(SeminarInfo info : dh.getSeminarDAO().listSeminarInfosFor(em, root)) {
					seminarInfoMapByJMBAG.put(info.getStudent().getJmbag(), info);
				}

				// Mapa<Mentor.ID, Set<Grupa.ID>>
				Map<Long, Set<Long>> grupeMentoraMap = new HashMap<Long, Set<Long>>(200);
				
				Set<String> foundGroups = new HashSet<String>(100);
				Set<String> foundInfos = new HashSet<String>(2000);
				for(SeminarGroupsInfoBean groupBean : groupsInfoBeans) {
					String groupName = groupBean.getKind()+"::"+groupBean.getGroupName();
					Group g = subGroupsMapByName.get(groupName);
					if(g==null) {
						// dodaj grupu
						g = new Group();
						g.setCompositeCourseID(root.getRootGroup().getCompositeCourseID());
						g.setManagedRoot(false);
						g.setName(groupName);
						g.setParent(root.getRootGroup());
						g.setRelativePath(GroupUtil.findNextRelativePath(root.getRootGroup()));
						root.getRootGroup().getSubgroups().add(g);
						dh.getGroupDAO().save(em, g);
						subGroupsMapByName.put(groupName, g);
					}
					foundGroups.add(g.getName());
					
					// Nadi info tog korisnika
					SeminarInfo info = seminarInfoMapByJMBAG.get(groupBean.getJmbag());
					if(info == null) {
						User student = dh.getUserDAO().getUserByJMBAG(em, groupBean.getJmbag());
						if(student==null) {
							logger.warn("[SeminarImporter] Nepoznat student: "+groupBean.getJmbag());
							continue;
						}
						CourseInstance ci = dh.getCourseInstanceDAO().get(em, root.getSemester().getId()+"/"+groupBean.getIsvuCode());
						if(ci==null) {
							logger.warn("[SeminarImporter] Nepoznat kolegij: "+groupBean.getIsvuCode());
							continue;
						}
						info = new SeminarInfo();
						info.setCourseInstance(ci);
						info.setGroup(g);
						User mentor = dh.getUserDAO().getUserByJMBAG(em, groupBean.getMentorID());
						if(mentor==null) {
							logger.warn("[SeminarImporter] Nepoznat mentor: "+groupBean.getMentorID());
						}
						info.setMentor(mentor);
						info.setRoomText("");
						info.setSeminarRoot(root);
						info.setStudent(student);
						info.setTitle(groupBean.getTitle());
						dh.getSeminarDAO().saveSeminarInfo(em, info);
						seminarInfoMapByJMBAG.put(groupBean.getJmbag(), info);
						UserGroup ug = new UserGroup();
						ug.setGroup(g);
						ug.setUser(student);
						dh.getUserGroupDAO().save(em, ug);
						g.getUsers().add(ug);
					} else {
						info.setTitle(groupBean.getTitle());
						if(info.getMentor()==null || !info.getMentor().getJmbag().equals(groupBean.getMentorID())) {
							User mentor = dh.getUserDAO().getUserByJMBAG(em, groupBean.getMentorID());
							info.setMentor(mentor);
							if(mentor==null) {
								logger.warn("[SeminarImporter] Nepoznat mentor: "+groupBean.getMentorID());
							}
						}
					}
					foundInfos.add(groupBean.getJmbag());
					
					// Ako je u krivoj grupi, promijeni mu grupu
					Group oldGroup = info.getGroup();
					if(!info.getGroup().equals(g)) {
						UserGroup userGroup = null;
						for(UserGroup ug : info.getGroup().getUsers()) {
							if(ug.getUser().equals(info.getStudent())) {
								userGroup = ug;
								break;
							}
						}
						if(userGroup!=null) {
							info.getGroup().getUsers().remove(userGroup);
							em.flush();
							dh.getUserGroupDAO().remove(em, userGroup);
						}
						userGroup = new UserGroup();
						userGroup.setGroup(g);
						userGroup.setUser(info.getStudent());
						dh.getUserGroupDAO().save(em, userGroup);
						g.getUsers().add(userGroup);

						info.setGroup(g);
						
						if(info.getEvent()!=null && info.getEvent().getGroups().contains(oldGroup)) {
							info.getEvent().getGroups().remove(oldGroup);
							oldGroup.getEvents().remove(info.getEvent());
							info.getEvent().getGroups().add(g);
						}
						
						if(info.getEvent()!=null && !info.getEvent().getGroups().contains(g)) {
							info.getEvent().getGroups().add(g);
							g.getEvents().add(info.getEvent());
						}
					}
					if(info.getMentor()!=null) {
						GroupOwner go = dh.getGroupDAO().getGroupOwner(em, info.getGroup(), info.getMentor());
						if(go==null) {
							go = new GroupOwner();
							go.setGroup(info.getGroup());
							go.setUser(info.getMentor());
							dh.getGroupDAO().save(em, go);
						}
						Set<Long> grupe = grupeMentoraMap.get(info.getMentor().getId());
						if(grupe==null) {
							grupe = new HashSet<Long>();
							grupeMentoraMap.put(info.getMentor().getId(), grupe);
						}
						grupe.add(info.getGroup().getId());
					}
				}
				
				// Izracunaj grupe koje treba ukloniti
				Set<String> groupsToRemove = new HashSet<String>(subGroupsMapByName.keySet());
				groupsToRemove.removeAll(foundGroups);

				Set<String> infosToRemove = new HashSet<String>(seminarInfoMapByJMBAG.keySet());
				infosToRemove.removeAll(foundInfos);
				
				for(SeminarScheduleInfoBean b : scheduleInfoBeans) {
					SeminarInfo info = seminarInfoMapByJMBAG.get(b.getJmbag());
					if(info==null) {
						System.out.println("[SeminarImporter] Nemam grupu za studenta: "+b.getJmbag());
						continue;
					}
					info.setRoomText(b.getRoomText()==null ? "" : b.getRoomText());
					if(StringUtil.isStringBlank(b.getDateTime())) {
						if(info.getEvent()!=null) {
							Group g = new ArrayList<Group>(info.getEvent().getGroups()).get(0);
							g.getEvents().remove(info.getEvent());
							info.getEvent().getGroups().remove(g);
							AbstractEvent e = info.getEvent();
							info.setEvent(null);
							em.flush();
							dh.getEventDAO().remove(em, e);
						}
					} else {
						Date d = null;
						try {
							d = sdf.parse(b.getDateTime());
						} catch(ParseException ex) {
							logger.error("[SeminarImporter] Datum je pogrešnog formata: "+d);
						}
						if(d!=null) {
							if(info.getEvent()!=null) {
								if(!info.getEvent().getStart().equals(d)) {
									info.getEvent().setStart(d);
								}
							} else {
								GroupWideEvent gwe = new GroupWideEvent();
								gwe.setStart(d);
								gwe.setDuration(30);
								gwe.setStrength(EventStrength.STRONG);
								gwe.setTitle("Prezentacija seminara");
								gwe.setContext("sem:"+info.getId());
								dh.getEventDAO().save(em, gwe);
								gwe.getGroups().add(info.getGroup());
								info.getGroup().getEvents().add(gwe);
								info.setEvent(gwe);
							}
							Room room = roomsMapByName.get(info.getRoomText().toUpperCase());
							if(room==null) {
								if(info.getEvent().getRoom()!=null) {
									info.getEvent().setRoom(null);
								}
							} else {
								if(info.getEvent().getRoom()==null || !info.getEvent().getRoom().equals(room)) {
									info.getEvent().setRoom(room);
								}
							}
						}
					}
				}
				em.flush();
				
				List<GroupOwner> goToRemove = new ArrayList<GroupOwner>();
				for(Group g : root.getRootGroup().getSubgroups()) {
					List<GroupOwner> list = dh.getGroupDAO().findForGroup(em, g);
					for(GroupOwner go : list) {
						Set<Long> shouldOwn = grupeMentoraMap.get(go.getUser().getId());
						if(shouldOwn==null || !shouldOwn.contains(go.getGroup().getId())) {
							goToRemove.add(go);
						}
					}
				}
				for(GroupOwner go : goToRemove) {
					dh.getGroupDAO().remove(em, go);
				}
				em.flush();
				
				return null;
			}
		});
	}

	private List<SeminarGroupsInfoBean> readGroupsInfoBeans(String grupeURL) {
		InputStream is = null;
		try {
			URL url = new URL(grupeURL);
			URLConnection conn = url.openConnection();
			conn.setAllowUserInteraction(false);
			conn.setUseCaches(false);
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(60000);
			conn.connect();
			is = conn.getInputStream();
			return SeminarGroupsInfoParser.parseCSVFormat(is);
		} catch(Exception ex) {
			logger.error("SeminarImporter exception.", ex);
			return null;
		} finally {
			if(is!=null) try { is.close(); } catch(Exception ignorable) {}
		}
	}

	private List<SeminarScheduleInfoBean> readScheduleInfoBeans(String terminiURL) {
		InputStream is = null;
		try {
			URL url = new URL(terminiURL);
			URLConnection conn = url.openConnection();
			conn.setAllowUserInteraction(false);
			conn.setUseCaches(false);
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(60000);
			conn.connect();
			is = conn.getInputStream();
			return SeminarScheduleInfoParser.parseCSVFormat(is);
		} catch(Exception ex) {
			logger.error("SeminarImporter exception.", ex);
			return null;
		} finally {
			if(is!=null) try { is.close(); } catch(Exception ignorable) {}
		}
	}

}
