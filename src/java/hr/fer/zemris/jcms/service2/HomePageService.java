package hr.fer.zemris.jcms.service2;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.beans.ActivityBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Activity;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.EventsService;
import hr.fer.zemris.jcms.service.ToDoService;
import hr.fer.zemris.jcms.web.actions.data.FerkoCalendarJSONFetcherData;
import hr.fer.zemris.jcms.web.actions.data.MainData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

/**
 * Sloj usluge koji se bavi naslovnicom.
 * 
 * @author marcupic
 *
 */
public class HomePageService {

	/**
	 * Metoda priprema podatke za iscrtavanje naslovnice.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void prepareHomePage(EntityManager em, MainData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		String semID = StringUtil.isStringBlank(data.getCurrentYearSemesterID()) ? BasicServiceSupport.getCurrentSemesterID(em) : data.getCurrentYearSemesterID();
		YearSemester ysem = null;
		if(semID!=null && !semID.equals("")) ysem = dh.getYearSemesterDAO().get(em, semID);
		// Aktivnosti prikazuj iz trenutnog semestra. Ako taj nema postavljen datom pocetka,
		// uzmi trenutni datum i odbij mjesec dana
		Date activitySince = ysem!=null && ysem.getStartsAt()!=null ? ysem.getStartsAt() : null;
		if(activitySince==null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			activitySince = cal.getTime();
		}
		List<Activity> activities = dh.getActivityDAO().listLastNForUser(em, activitySince, data.getCurrentUser(), 5);
		List<ActivityBean> activityBeans = ActivityService.generateActivityBeans(activities, data.getMessageLogger());
		data.setActivityBeans(activityBeans);
		boolean activityChanged = false;
		for(Activity a : activities) {
			if(!a.getViewed()) {
				activityChanged=true;
				a.setViewed(true);
			}
		}
		if(activityChanged) {
			em.flush();
		}
		List<CourseInstance> allCourses = Collections.emptyList(); 
		data.setAllCourseInstances(allCourses);
		data.setAllSemesters(dh.getYearSemesterDAO().list(em));
		if(ysem==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setSelectedSemester(ysem);
		data.setCurrentYearSemesterID(ysem.getId());
		User currentUser = data.getCurrentUser();
		if(data.getCalendarType()==1 && ysem.getStartsAt()!=null && ysem.getEndsAt()!=null) {
			data.setEvents(EventsService.listForUser(em, currentUser, ysem.getStartsAt(), ysem.getEndsAt()));
		} else {
			data.setEvents(EventsService.listForUser(em, currentUser, data.getDateFrom(), data.getDateTo()));
		}
		data.setCourseInstanceWithGroups(DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findForUserAndSemester(em, ysem.getId(), currentUser));

		data.setRenderCourseAdministration(
				JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration()
		);

		if(data.getRenderCourseAdministration()) {
			data.setAllCourseInstances(
				JCMSSecurityManagerFactory.getManager().getCourseAdministrationList(ysem)
			);
		}
		
		data.setRenderSystemAdministration(JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration());

		if(data.getAllCourseInstances()!=null) {
			Collections.sort(data.getAllCourseInstances(),StringUtil.COURSEINSTANCE_COMPARATOR);
		}
		if(data.getAllSemesters()!=null) {
			Collections.sort(data.getAllSemesters(),StringUtil.YEARSEMESTER_COMPARATOR);
		}
		
		//List<Poll> polls = dh.getPollDAO().getPollsForUser(em, userID);
		data.setPollsForUser(new LinkedList<Poll>());
		
		//List<Poll> pollsOwn = dh.getPollDAO().getPollsForOwner(em, userID);
		data.setPollsForOwner(new LinkedList<Poll>());
		
		data.setUserKey(Long.toString(currentUser.getId(), 16)+":"+currentUser.getUserDescriptor().getExternalID());
		
		data.setPorukaAdmina(BasicServiceSupport.getKeyValue(em, "AdminMessage"));
		
		data.setOwnToDoList(ToDoService.getOwnList(dh,em,currentUser.getId()));
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda priprema podatke za jednostavan (stari) kalendar.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void prepareSimpleCalndar(EntityManager em, MainData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		String semID = StringUtil.isStringBlank(data.getCurrentYearSemesterID()) ? BasicServiceSupport.getCurrentSemesterID(em) : data.getCurrentYearSemesterID();
		YearSemester ysem = null;
		if(semID!=null && !semID.equals("")) ysem = dh.getYearSemesterDAO().get(em, semID);
		if(ysem==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setSelectedSemester(ysem);
		data.setCurrentYearSemesterID(ysem.getId());
		User currentUser = data.getCurrentUser();
		if(data.getCalendarType()==1 && ysem.getStartsAt()!=null && ysem.getEndsAt()!=null) {
			data.setEvents(EventsService.listForUser(em, currentUser, ysem.getStartsAt(), ysem.getEndsAt()));
		} else {
			data.setEvents(EventsService.listForUser(em, currentUser, data.getDateFrom(), data.getDateTo()));
		}

		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void getFerkoWeekCalendar(EntityManager em, FerkoCalendarJSONFetcherData data) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if("fetchWeek".equals(data.getCommand())) {
				if(data.getDateFrom()==null && data.getDateTo()==null) {
					resolveCurrentWeek(data, sdf, sdf2);
				} else if(data.getDateFrom()==null || data.getDateTo()==null) {
					data.setFatalMessage("Pogreška u datumima.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				} else {
					Calendar cal = Calendar.getInstance();
					cal.setTime(data.getDateFrom());
					cal.set(Calendar.HOUR_OF_DAY, 12);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.add(Calendar.DAY_OF_YEAR, 6);
					String end = sdf.format(cal.getTime());
					String origEnd = sdf.format(data.getDateTo());
					if(!end.equals(origEnd)) {
						data.setFatalMessage("Pogreška u datumima.");
						data.setResult(AbstractActionData.RESULT_FATAL);
						return;
					}
					String start = sdf.format(data.getDateFrom());
					Date startDate = sdf2.parse(start+" 00:00:00");
					Date endDate = sdf2.parse(end+" 23:59:59");
					data.setDateFrom(startDate);
					data.setDateTo(endDate);
					data.setSDateFrom(sdf2.format(data.getDateFrom()));
					data.setSDateTo(sdf2.format(data.getDateTo()));
				}
				fillDaysMap(data.getDateFrom(), data.getDateTo(), sdf, data);
				User currentUser = data.getCurrentUser();
				data.setEvents(EventsService.listForUser(em, currentUser, data.getDateFrom(), data.getDateTo()));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			if("fetchCurrentWeek".equals(data.getCommand())) {
				resolveCurrentWeek(data, sdf, sdf2);
				fillDaysMap(data.getDateFrom(), data.getDateTo(), sdf, data);
				User currentUser = data.getCurrentUser();
				data.setEvents(EventsService.listForUser(em, currentUser, data.getDateFrom(), data.getDateTo()));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			if("fetchNextWeek".equals(data.getCommand())) {
				if(data.getDateFrom()==null || data.getDateTo()==null) {
					data.setFatalMessage("Pogreška u datumima.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				} else {
					Calendar cal = Calendar.getInstance();
					cal.setTime(data.getDateFrom());
					cal.set(Calendar.HOUR_OF_DAY, 12);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.add(Calendar.DAY_OF_YEAR, 6);
					String end = sdf.format(cal.getTime());
					String origEnd = sdf.format(data.getDateTo());
					if(!end.equals(origEnd)) {
						data.setFatalMessage("Pogreška u datumima.");
						data.setResult(AbstractActionData.RESULT_FATAL);
						return;
					}
					cal.add(Calendar.DAY_OF_YEAR, 1);
					String start = sdf.format(cal.getTime());
					cal.add(Calendar.DAY_OF_YEAR, 6);
					end = sdf.format(cal.getTime());
					data.setSDateFrom(start+" 00:00:00");
					data.setSDateTo(end+" 23:59:59");
					Date startDate = sdf2.parse(data.getSDateFrom());
					Date endDate = sdf2.parse(data.getSDateTo());
					data.setDateFrom(startDate);
					data.setDateTo(endDate);
				}
				fillDaysMap(data.getDateFrom(), data.getDateTo(), sdf, data);
				User currentUser = data.getCurrentUser();
				data.setEvents(EventsService.listForUser(em, currentUser, data.getDateFrom(), data.getDateTo()));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			if("fetchPreviousWeek".equals(data.getCommand())) {
				if(data.getDateFrom()==null || data.getDateTo()==null) {
					data.setFatalMessage("Pogreška u datumima.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				} else {
					Calendar cal = Calendar.getInstance();
					cal.setTime(data.getDateFrom());
					cal.set(Calendar.HOUR_OF_DAY, 12);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.add(Calendar.DAY_OF_YEAR, 6);
					String end = sdf.format(cal.getTime());
					String origEnd = sdf.format(data.getDateTo());
					if(!end.equals(origEnd)) {
						data.setFatalMessage("Pogreška u datumima.");
						data.setResult(AbstractActionData.RESULT_FATAL);
						return;
					}
					cal.add(Calendar.DAY_OF_YEAR, -13);
					String start = sdf.format(cal.getTime());
					cal.add(Calendar.DAY_OF_YEAR, 6);
					end = sdf.format(cal.getTime());
					data.setSDateFrom(start+" 00:00:00");
					data.setSDateTo(end+" 23:59:59");
					Date startDate = sdf2.parse(data.getSDateFrom());
					Date endDate = sdf2.parse(data.getSDateTo());
					data.setDateFrom(startDate);
					data.setDateTo(endDate);
				}
				fillDaysMap(data.getDateFrom(), data.getDateTo(), sdf, data);
				User currentUser = data.getCurrentUser();
				data.setEvents(EventsService.listForUser(em, currentUser, data.getDateFrom(), data.getDateTo()));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			data.setFatalMessage("Nepoznata naredba.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		} catch(Exception ex) {
			data.setFatalMessage(ex.getMessage());
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
	}

	private static void fillDaysMap(Date dateFrom, Date dateTo, SimpleDateFormat sdf, FerkoCalendarJSONFetcherData data) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFrom);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		Map<String,Integer> map = new LinkedHashMap<String, Integer>();
		List<DayDescriptor> list = new ArrayList<DayDescriptor>();
		for(int i = 0; i < 7; i++) {
			String key = sdf.format(cal.getTime());
			map.put(key, Integer.valueOf(i));
			cal.add(Calendar.DAY_OF_YEAR, 1);
			list.add(new DayDescriptor(i, dayName(i,data.getMessageLogger()), key));
		}
		data.setDaysMap(map);
		data.setDayDescriptors(list);
	}

	private static String dayName(int i, IMessageLogger messageLogger) {
		return messageLogger.getText("dayTitle.sh."+i);
	}

	public static class DayDescriptor {
		private int index;
		private String title;
		private String date;
		
		public DayDescriptor(int index, String title, String date) {
			super();
			this.index = index;
			this.title = title;
			this.date = date;
		}
		
		public String getDate() {
			return date;
		}
		public int getIndex() {
			return index;
		}
		public String getTitle() {
			return title;
		}
	}
	
	private static void resolveCurrentWeek(FerkoCalendarJSONFetcherData data, SimpleDateFormat sdf, SimpleDateFormat sdf2) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		int dow = cal.get(Calendar.DAY_OF_WEEK);
		switch(dow) {
		case Calendar.MONDAY: break;
		case Calendar.TUESDAY: 
			cal.add(Calendar.DAY_OF_YEAR, -1);
			break;
		case Calendar.WEDNESDAY: 
			cal.add(Calendar.DAY_OF_YEAR, -2);
			break;
		case Calendar.THURSDAY: 
			cal.add(Calendar.DAY_OF_YEAR, -3);
			break;
		case Calendar.FRIDAY: 
			cal.add(Calendar.DAY_OF_YEAR, -4);
			break;
		case Calendar.SATURDAY: 
			cal.add(Calendar.DAY_OF_YEAR, -5);
			break;
		case Calendar.SUNDAY: 
			cal.add(Calendar.DAY_OF_YEAR, -6);
			break;
		}
		String start = sdf.format(cal.getTime());
		cal.add(Calendar.DAY_OF_YEAR, 6);
		String end = sdf.format(cal.getTime());
		data.setDateFrom(sdf2.parse(start+" 00:00:00"));
		data.setDateTo(sdf2.parse(end+" 23:59:59"));
		data.setSDateFrom(sdf2.format(data.getDateFrom()));
		data.setSDateTo(sdf2.format(data.getDateTo()));
	}
}
