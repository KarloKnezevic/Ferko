package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.web.actions.data.StudentScheduleAnalyzerData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.StringUtil;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.DateStampCache;
import hr.fer.zemris.util.time.TemporalList;
import hr.fer.zemris.util.time.TemporalNode;
import hr.fer.zemris.util.time.TimeSpanCache;
import hr.fer.zemris.util.time.TimeStamp;
import hr.fer.zemris.util.time.TimeStampCache;
import hr.fer.zemris.util.time.TemporalList.TL;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ScheduleAnalyzerService {

	public static void getStudentScheduleAnalyzerData(final StudentScheduleAnalyzerData data, final Long userID, final String semesterID, final String dateFrom, final String dateTo, final String jmbagsList, final boolean createOccupancyMap, final String courseInstanceID, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = data.getCurrentUser();

				CourseInstance courseInstance = null;
				boolean local = false;
				if(!StringUtil.isStringBlank(courseInstanceID)) {
					local = true;
					courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
					if(courseInstance==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				if(!local) {
					if(!JCMSSecurityManagerFactory.getManager().canAnalizeGlobalSchedule()) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				} else {
					if(!task.equals("inputSemesterAndUsers") && !task.equals("viewForSemesterAndUsers")) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					if(!JCMSSecurityManagerFactory.getManager().canAnalizeCourseSchedule(courseInstance)) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				YearSemester ysem = null;
				if(local) {
					ysem = courseInstance.getYearSemester();
				} else {
					String semID = semesterID==null || semesterID.equals("") ? BasicBrowsing.getCurrentSemesterID(em) : semesterID;
					if(semID!=null && !semID.equals("")) ysem = dh.getYearSemesterDAO().get(em, semID);
					if(ysem==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				data.setYearSemester(ysem);
				if(task.equals("inputSemester") || task.equals("inputSemesterAndUsers")) {
					data.setAllSemesters(dh.getYearSemesterDAO().list(em));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				Date dFrom = null;
				Date dTo = null;
				boolean err = false;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if(!StringUtil.isStringBlank(dateFrom)) {
					if(!StringUtil.checkStandardDateFormat(dateFrom)) {
						data.getMessageLogger().addErrorMessage("Početni datum je pogrešnog formata.");
						err = true;
					} else {
						try { 
							dFrom = sdf.parse(dateFrom+" 00:00:00");
						} catch(Exception ex) {
							data.getMessageLogger().addErrorMessage("Početni datum je pogrešnog formata.");
							err = true;
						}
					}
				} else {
					data.getMessageLogger().addErrorMessage("Početni datum mora biti zadan.");
					err = true;
				}
				if(!StringUtil.isStringBlank(dateTo)) {
					if(!StringUtil.checkStandardDateFormat(dateTo)) {
						data.getMessageLogger().addErrorMessage("Završni datum je pogrešnog formata.");
						err = true;
					} else {
						try { 
							dTo = sdf.parse(dateTo+" 23:59:00"); 
						} catch(Exception ex) {
							data.getMessageLogger().addErrorMessage("Završni datum je pogrešnog formata.");
							err = true;
						}
					}
				} else {
					data.getMessageLogger().addErrorMessage("Završni datum mora biti zadan.");
					err = true;
				}
				if(!err && dFrom.after(dTo)) {
					data.getMessageLogger().addErrorMessage("Završni datum je prije početnog.");
					err = true;
				}
				if( !err && local && (dTo.getTime()-dFrom.getTime()) > 7*24*60*60*1000 ) {
					data.getMessageLogger().addErrorMessage("Analiza može obuhvaćati period od najviše tjedan dana.");
					err = true;
				}
				if(err) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					data.setAllSemesters(dh.getYearSemesterDAO().list(em));
					return null;
				}
				if(!task.equals("viewForSemester") && !task.equals("viewForSemesterAndUsers")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				BasicResult res = new BasicResult(); 
				Set<DateStamp> set = new HashSet<DateStamp>(200);
				Calendar c = Calendar.getInstance();
				c.setTime(dFrom);
				int cnt = 0;
				while(true) {
					cnt++;
					if(cnt>183) {
						// beskonacna petlja?
						break;
					}
					Date d = c.getTime();
					if(d.after(dTo)) break;
					int day = c.get(Calendar.DAY_OF_WEEK);
					if(day==Calendar.SATURDAY || day==Calendar.SUNDAY) {
						c.add(Calendar.DATE, 1);
						continue;
					}
					set.add(res.dateStampCache.get(res.sdf.format(d)));
					c.add(Calendar.DATE, 1);
				}

				
				List<User> users;
				if(task.equals("viewForSemesterAndUsers")) {
					List<String> jmbags = null;
					try {
						jmbags = TextService.readerToStringList(new StringReader(jmbagsList));
						List<String> tmpJmbags = new ArrayList<String>(jmbags.size());
						for(String s : jmbags) {
							s = s.trim();
							int l = s.length();
							if(l < 10) {
								l = 10-l; // toliko nula nedostaje
								StringBuilder sb = new StringBuilder(10);
								for(int i = 0; i < l; i++) {
									sb.append('0');
								}
								sb.append(s);
								s = sb.toString();
							}
							tmpJmbags.add(s);
						}
						jmbags = tmpJmbags;
					} catch (IOException e) {
						data.getMessageLogger().addErrorMessage("Pogreška kod čitanja jmbagova.");
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					if(local) {
						List<User> allUsers = dh.getUserDAO().listUsersOnCourseInstance(em, courseInstanceID);
						if(jmbags.isEmpty()) {
							users = allUsers;
						} else {
							Map<String,User> umap = UserUtil.mapUserByJmbag(allUsers);
							List<String> notFound = new ArrayList<String>(jmbags.size());
							users = new ArrayList<User>(jmbags.size());
							for(String jmbag : jmbags) {
								User u = umap.get(jmbag);
								if(u==null) {
									notFound.add(jmbag);
								} else {
									users.add(u);
								}
							}
							if(!notFound.isEmpty()) {
								data.getMessageLogger().addErrorMessage(notFound+" studenata su nepoznati: "+notFound.toString());
								data.setAllSemesters(dh.getYearSemesterDAO().list(em));
								data.setResult(AbstractActionData.RESULT_INPUT);
								return null;
							}
						}
					} else {
						users = dh.getUserDAO().getForJmbagSublistBatching(em, jmbags);
						HashSet<String> foundJmbags = new HashSet<String>(users.size());
						for(User u : users) {
							foundJmbags.add(u.getJmbag());
						}
						List<String> notFound = new ArrayList<String>(jmbags.size());
						for(String jmbag : jmbags) {
							if(foundJmbags.contains(jmbag)) continue;
							notFound.add(jmbag);
						}
						if(!notFound.isEmpty()) {
							data.getMessageLogger().addErrorMessage(notFound+" studenata su nepoznati: "+notFound.toString());
							data.setAllSemesters(dh.getYearSemesterDAO().list(em));
							data.setResult(AbstractActionData.RESULT_INPUT);
							return null;
						}
					}
				} else {
					users = dh.getYearSemesterDAO().findUsersInSemester(em, ysem);
				}
				analyzeFaster(em, res, users, dFrom, dTo, ysem);
				Comparator<Entry<DateStamp,TemporalList.TL>> dsComparator = new Comparator<Entry<DateStamp,TemporalList.TL>>() {
					@Override
					public int compare(Entry<DateStamp, TL> o1, Entry<DateStamp, TL> o2) {
						return o1.getKey().compareTo(o2.getKey());
					}
				};
				TemporalList tlOccMap = null;
				if(createOccupancyMap) tlOccMap = new TemporalList(res.timeSpanCache);
				ZipOutputStream zip = null;
				File tmpFile = null;
				try {
					tmpFile = File.createTempFile("SCH", null);
					zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)));
					zip.putNextEntry(new ZipEntry("zauzetost.csv"));
					Writer w = new OutputStreamWriter(zip, "UTF-8");
					for(Map.Entry<User, TemporalList> en : res.busyMap.entrySet()) {
						User user = en.getKey();
						TemporalList tl = en.getValue();
						Map<DateStamp,TemporalList.TL> byDays = tl.getMap();
						List<Entry<DateStamp,TemporalList.TL>> podaci = new ArrayList<Entry<DateStamp,TemporalList.TL>>(byDays.entrySet());
						Collections.sort(podaci, dsComparator);
						for(Map.Entry<DateStamp,TemporalList.TL> en2 : podaci) {
							TemporalNode n = en2.getValue().first;
							while(n != null) {
								if(tlOccMap!=null) {
									tlOccMap.addInterval(n.getDateStamp(), n.getTimeSpan(), user.getJmbag());
								}
								w.write(user.getJmbag());
								w.write(';');
								w.write(en2.getKey().getStamp());
								w.write(';');
								w.write(n.getTimeSpan().getStart().toString());
								w.write(';');
								w.write(n.getTimeSpan().getEnd().toString());
								w.write(';');
								w.write(Integer.toString(n.getDescriptors().size()));
								for(String d : n.getDescriptors()) {
									w.write('|');
									w.write(d);
								}
								w.write('\n');
								n = n.getNext();
							}
						}						
					}
					w.flush();
					zip.closeEntry();
					zip.putNextEntry(new ZipEntry("slobodno.csv"));
					w = new OutputStreamWriter(zip, "UTF-8");
					for(Map.Entry<User, TemporalList> en : res.busyMap.entrySet()) {
						User user = en.getKey();
						TemporalList tl = en.getValue().createInversionList(set, new TimeStamp(8,0), new TimeStamp(20,0));
						Map<DateStamp,TemporalList.TL> byDays = tl.getMap();
						List<Entry<DateStamp,TemporalList.TL>> podaci = new ArrayList<Entry<DateStamp,TemporalList.TL>>(byDays.entrySet());
						Collections.sort(podaci, dsComparator);
						for(Map.Entry<DateStamp,TemporalList.TL> en2 : podaci) {
							TemporalNode n = en2.getValue().first;
							while(n != null) {
								w.write(user.getJmbag());
								w.write(';');
								w.write(en2.getKey().getStamp());
								w.write(';');
								w.write(n.getTimeSpan().getStart().toString());
								w.write(';');
								w.write(n.getTimeSpan().getEnd().toString());
								w.write('\n');
								n = n.getNext();
							}
						}						
					}
					w.flush();
					zip.closeEntry();
					if(tlOccMap!=null) {
						zip.putNextEntry(new ZipEntry("mapaZauzetosti.csv"));
						w = new OutputStreamWriter(zip, "UTF-8");
						Map<DateStamp,TemporalList.TL> byDays = tlOccMap.getMap();
						List<Entry<DateStamp,TemporalList.TL>> podaci = new ArrayList<Entry<DateStamp,TemporalList.TL>>(byDays.entrySet());
						Collections.sort(podaci, dsComparator);
						for(Map.Entry<DateStamp,TemporalList.TL> en2 : podaci) {
							TemporalNode n = en2.getValue().first;
							while(n != null) {
								w.write(en2.getKey().getStamp());
								w.write(';');
								w.write(n.getTimeSpan().getStart().toString());
								w.write(';');
								w.write(n.getTimeSpan().getEnd().toString());
								w.write(';');
								w.write(Integer.toString(n.getDescriptors().size()));
								for(String d : n.getDescriptors()) {
									w.write('|');
									w.write(d);
								}
								w.write('\n');
								n = n.getNext();
							}
						}						
						w.flush();
						zip.closeEntry();
					}
					zip.close();
					DeleteOnCloseFileInputStream docis = new DeleteOnCloseFileInputStream(tmpFile);
					docis.setFileName("podaci.zip");
					docis.setMimeType("application/zip");
					data.setStream(docis);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
				} catch (IOException e) {
					e.printStackTrace();
					data.getMessageLogger().addErrorMessage("Greška prilikom izrade ZIP datoteke.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				} finally {
					if(zip!=null) {
						try { zip.close(); } catch(Exception ignorable) {}
					}
				}
				//data.setEvents(EventsService.listForUser(em, currentUser, dateFrom, dateTo));
				return null;
			}
		});
	}

	public static void getStudentScheduleAnalyzerAppletData(final StudentScheduleAnalyzerData data, final Long userID, final String semesterID, final String dateFrom, final String dateTo, final String jmbagsList, final boolean createOccupancyMap, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = data.getCurrentUser();

				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				
				CourseInstance courseInstance = null;
				boolean local = false;
				if(!StringUtil.isStringBlank(courseInstanceID)) {
					local = true;
					courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
					if(courseInstance==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					if(!JCMSSecurityManagerFactory.getManager().canAnalizeCourseSchedule(courseInstance)) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				} else {
					if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				data.setCourseInstance(courseInstance);
				
				YearSemester ysem = null;
				if(local) {
					ysem = courseInstance.getYearSemester();
				} else {
					String semID = semesterID==null || semesterID.equals("") ? BasicBrowsing.getCurrentSemesterID(em) : semesterID;
					if(semID!=null && !semID.equals("")) ysem = dh.getYearSemesterDAO().get(em, semID);
					if(ysem==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				data.setYearSemester(ysem);
				Date dFrom = null;
				Date dTo = null;
				boolean err = false;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if(!StringUtil.isStringBlank(dateFrom)) {
					if(!StringUtil.checkStandardDateFormat(dateFrom)) {
						data.getMessageLogger().addErrorMessage("Početni datum je pogrešnog formata.");
						err = true;
					} else {
						try { 
							dFrom = sdf.parse(dateFrom+" 00:00:00");
						} catch(Exception ex) {
							data.getMessageLogger().addErrorMessage("Početni datum je pogrešnog formata.");
							err = true;
						}
					}
				} else {
					data.getMessageLogger().addErrorMessage("Početni datum mora biti zadan.");
					err = true;
				}
				if(!StringUtil.isStringBlank(dateTo)) {
					if(!StringUtil.checkStandardDateFormat(dateTo)) {
						data.getMessageLogger().addErrorMessage("Završni datum je pogrešnog formata.");
						err = true;
					} else {
						try { 
							dTo = sdf.parse(dateTo+" 23:59:00"); 
						} catch(Exception ex) {
							data.getMessageLogger().addErrorMessage("Završni datum je pogrešnog formata.");
							err = true;
						}
					}
				} else {
					data.getMessageLogger().addErrorMessage("Završni datum mora biti zadan.");
					err = true;
				}
				if(!err && dFrom.after(dTo)) {
					data.getMessageLogger().addErrorMessage("Završni datum je prije početnog.");
					err = true;
				}
				if( !err && local && (dTo.getTime()-dFrom.getTime()) > 7*24*60*60*1000 ) {
					data.getMessageLogger().addErrorMessage("Analiza može obuhvaćati period od najviše tjedan dana.");
					err = true;
				}
				char[] chs = jmbagsList!=null ? jmbagsList.toCharArray() : new char[0];
				StringBuilder sb = new StringBuilder(chs.length);
				for(char c : chs) {
					switch(c) {
					case '\r':
					case '\n':
					case '\t':
						sb.append(' ');
						break;
					default: 
						sb.append(c);
						break;
					}
				}
				data.setJmbagsSingleLine(sb.toString());
				sb = null; chs = null;
				if(err) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					data.setAllSemesters(dh.getYearSemesterDAO().list(em));
					return null;
				}
				data.setDateFrom(dateFrom);
				data.setDateTo(dateTo);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	protected static void analyze(EntityManager em, BasicResult res, List<User> users, Date dateFrom, Date dateTo) {
		Calendar cal = Calendar.getInstance();
		res.busyMap = new HashMap<User, TemporalList>(users.size());
		int counter = 0;
		System.out.println("[ScheduleAnalyzerService] Starting analysis. User count: "+users.size());
		System.out.println("[ScheduleAnalyzerService] Analysis in progress...");
		for(User currentUser : users) {
			counter++;
			//System.out.println("Korisnik broj: "+counter);
			TemporalList tlist = new TemporalList(res.timeSpanCache);
			List<AbstractEvent> events = EventsService.listForUser(em, currentUser, dateFrom, dateTo);
			for(AbstractEvent event : events) {
				if(event.isDeadline()) continue; // ovo nema trajanja pa ignoriram
				if(event.isHidden()) continue;   // ako je ovaj skriven, onda postoji njegov "shadow" koji nije skriven
				if(event.getStrength()==EventStrength.WEAK) continue;  // ako je slab događaj, također ignoriram
				String date = res.sdf.format(event.getStart());
				cal.setTime(event.getStart());
				TimeStamp start = res.timeStampCache.get(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
				cal.add(Calendar.MINUTE, event.getDuration());
				TimeStamp end = res.timeStampCache.get(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
				tlist.addInterval(
					res.dateStampCache.get(date),
					res.timeSpanCache.get(start, end),
					event.getTitle()==null ? "?" : event.getTitle()
				);
			}
			res.busyMap.put(currentUser, tlist);
		}
		System.out.println("[ScheduleAnalyzerService] Analysis completed.");
	}

	/**
	 * Ova metoda je za barem dva reda velicine brza od gornje ako se pokrene za citav semestar (dakle na cca 3000 studenata).
	 * Tu se najbolje vidi kako treba iskoristiti cacheve!
	 * 
	 * @param em
	 * @param res
	 * @param users
	 * @param dateFrom
	 * @param dateTo
	 * @param ysem
	 */
	@SuppressWarnings("unchecked")
	protected static void analyzeFaster(EntityManager em, BasicResult res, List<User> users, Date dateFrom, Date dateTo, YearSemester ysem) {
		Calendar cal = Calendar.getInstance();
		res.busyMap = new HashMap<User, TemporalList>(users.size());
		for(User currentUser : users) {
			res.busyMap.put(currentUser, new TemporalList(res.timeSpanCache));
		}
		Map<Long,User> userMap = UserUtil.mapUserById(users);
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		// Faza 1: dohvat svih grupnih evenata
		Iterator<User> it = users.iterator();
		while(true) {
			int c;
			StringBuilder sb = new StringBuilder(4000);
			sb.append("select gwe.id, usergr.user.id from GroupWideEvent as gwe, IN(gwe.groups) ggg, IN(ggg.users) usergr WHERE gwe.hidden=false AND gwe.start >= :fromDate AND gwe.start <= :toDate AND usergr.user.id in (");
			for(c=0; c<100 && it.hasNext(); c++) {
				if(c!=0) {
					sb.append(", ");
				}
				sb.append(it.next().getId());
			}
			sb.append(')');
			if(c==0) break; // gotovi smo!
			Query q = em.createQuery(sb.toString());
			q.setParameter("fromDate", dateFrom);
			q.setParameter("toDate", dateTo);
			List<Object[]> list = (List<Object[]>)q.getResultList();
			System.out.println("Faza 1: vraceno "+list.size());
			for(Object[] o : list) {
				User currentUser = userMap.get((Long)o[1]);
				AbstractEvent event = dh.getEventDAO().get(em, (Long)o[0]);
				addToSchedule(cal, res, currentUser, event);
			}
		}
		// Faza 2: dohvat svih evenata kolegija
		it = users.iterator();
		while(true) {
			int c;
			StringBuilder sb = new StringBuilder(4000);
			sb.append("select cwe.id, usergr.user.id from CourseWideEvent as cwe, UserGroup as usergr WHERE cwe.hidden=false AND usergr.group.relativePath LIKE '0/%' AND cwe.courseInstance.id=usergr.group.compositeCourseID AND cwe.start >= :fromDate AND cwe.start <= :toDate and usergr.user.id in (");
			for(c=0; c<100 && it.hasNext(); c++) {
				if(c!=0) {
					sb.append(", ");
				}
				sb.append(it.next().getId());
			}
			sb.append(')');
			if(c==0) break; // gotovi smo!
			Query q = em.createQuery(sb.toString());
			q.setParameter("fromDate", dateFrom);
			q.setParameter("toDate", dateTo);
			List<Object[]> list = (List<Object[]>)q.getResultList();
			System.out.println("Faza 2: vraceno "+list.size());
			for(Object[] o : list) {
				User currentUser = userMap.get((Long)o[1]);
				AbstractEvent event = dh.getEventDAO().get(em, (Long)o[0]);
				addToSchedule(cal, res, currentUser, event);
			}
		}
	}

	private static void addToSchedule(Calendar cal, BasicResult res, User currentUser, AbstractEvent event) {
		TemporalList tlist = res.busyMap.get(currentUser);
		if(event.isDeadline()) return; // ovo nema trajanja pa ignoriram
		if(event.isHidden()) return;   // ako je ovaj skriven, onda postoji njegov "shadow" koji nije skriven
		if(event.getStrength()==EventStrength.WEAK) return;  // ako je slab događaj, također ignoriram
		String date = res.sdf.format(event.getStart());
		cal.setTime(event.getStart());
		TimeStamp start = res.timeStampCache.get(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		cal.add(Calendar.MINUTE, event.getDuration());
		TimeStamp end = res.timeStampCache.get(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		tlist.addInterval(
			res.dateStampCache.get(date),
			res.timeSpanCache.get(start, end),
			event.getTitle()==null ? "?" : event.getTitle()
		);
	}

	static class BasicResult {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		TimeSpanCache timeSpanCache = new TimeSpanCache();
		TimeStampCache timeStampCache = new TimeStampCache();
		DateStampCache dateStampCache = new DateStampCache();
		Map<User, TemporalList> busyMap;
	}
}
