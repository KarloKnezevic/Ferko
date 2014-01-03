package hr.fer.zemris.jcms.service2;

import hr.fer.zemris.jcms.activities.types.ActivityEventKind;
import hr.fer.zemris.jcms.beans.ActivityBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Activity;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.ActivityGoData;
import hr.fer.zemris.jcms.web.actions.data.ShowActivitiesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

public class ActivityService {

	// ---------------------------------------------------------------------------------------------------------------------- //
	// MAPA POZNATIH VRSTA OBAVIJESTI
	// ---------------------------------------------------------------------------------------------------------------------- //
	private static final Map<String, IActivityBeanFiller> map = new HashMap<String, IActivityBeanFiller>();
	static {
		map.put("A", new ApplicationActivityBeanFiller());
		map.put("G", new GradeActivityBeanFiller());
		map.put("S", new ScoreActivityBeanFiller());
		map.put("R", new GroupActivityBeanFiller());
		map.put("M", new MarketActivityBeanFiller());
		map.put("I", new IssueTrackingActivityBeanFiller());
	}

	public static List<ActivityBean> generateActivityBeans(List<Activity> activities, IMessageLogger messageLogger) {
		List<ActivityBean> list = new ArrayList<ActivityBean>(activities.size());
		for(Activity a : activities) {
			IActivityBeanFiller f = map.get(a.getKind());
			ActivityBean ab;
			if(f==null) {
				ab = new ActivityBean(a.getId(), a.getDate(), messageLogger.getText("activity.unknownKind"), a.getArchived(), a.getViewed());
			} else {
				ab = f.fill(a, messageLogger);
			}
			list.add(ab);
			continue;
		}
		return list;
	}

	public static ActivityBean generateActivityBean(Activity activity, IMessageLogger messageLogger) {
		IActivityBeanFiller f = map.get(activity.getKind());
		ActivityBean ab;
		if(f==null) {
			ab = new ActivityBean(activity.getId(), activity.getDate(), messageLogger.getText("activity.unknownKind"), activity.getArchived(), activity.getViewed());
		} else {
			ab = f.fill(activity, messageLogger);
		}
		return ab;
	}

	public static void dispatch(EntityManager em, ActivityGoData data) {
		if(data.getAid()==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Activity a = DAOHelperFactory.getDAOHelper().getActivityDAO().get(em, data.getAid());
		if(a==null || !a.getUser().equals(data.getCurrentUser())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if(a.getKind().equals("A")) {
			data.setCourseInstanceID(a.getContext().substring(4));
			String[] elems = StringUtil.split(a.getData(),'\t');
			data.setStudentApplicationID(Long.valueOf(elems[0]));
			data.setResult("RES_STUDENT_APPLICATION_VIEW");
			return;
		}
		if(a.getKind().equals("G")) {
			data.setCourseInstanceID(a.getContext().substring(4));
			data.setResult("RES_A_SUMMARYVIEW");
			return;
		}
		if(a.getKind().equals("S")) {
			data.setCourseInstanceID(a.getContext().substring(4));
			data.setResult("RES_A_SUMMARYVIEW");
			return;
		}
		if(a.getKind().equals("M")) {
			data.setCourseInstanceID(a.getContext().substring(4));
			String[] elems = StringUtil.split(a.getData(),'\t');
			data.setParentID(elems[1]);
			data.setResult("RES_MP_VIEW");
			return;
		}
		if(a.getKind().equals("R")) {
			String ciid = a.getContext().substring(4);
			data.setCourseInstanceID(ciid);
			// {kind, parentGroup, groupName, isvucode, coursename, podvrsta, ...}
			String[] elems = StringUtil.split(a.getData(),'\t');
			if(elems[5].equals("C")) {
				CourseComponentItem cci = DAOHelperFactory.getDAOHelper().getCourseComponentDAO().findItem(em, ciid, elems[6], Integer.parseInt(elems[7]));
				if(cci==null) {
					// Dakle, ako takvog itema nema, vodi na kolegij
					data.setResult("RES_COURSEINSTANCE");
					return;
				} else {
					data.setItemID(cci.getId());
					data.setResult("RES_ITEM_VIEW");
					return;
				}
			} else {
				data.setResult("RES_COURSEINSTANCE");
				return;
			}
		}
		if(a.getKind().equals("I")) {
			data.setCourseInstanceID(a.getContext().substring(4));
			String[] elems = StringUtil.split(a.getData(),'\t');
			data.setIssueID(Long.parseLong(elems[2]));
			data.setResult("RES_ISSUE_VIEW");
			return;
		}
		data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return;
	}

	public static void fetchForCurrentSemestar(EntityManager em, ShowActivitiesData data) {
		data.setActivityBeans(fetchForCurrentSemestar(em, data.getCurrentUser(), data.getMessageLogger()));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static List<ActivityBean> fetchForCurrentSemestar(EntityManager em, User user, IMessageLogger messageLogger) {
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		String semID = BasicServiceSupport.getCurrentSemesterID(em);
		
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
		List<Activity> activities = dh.getActivityDAO().listForUser(em, activitySince, user);
		List<ActivityBean> activityBeans = ActivityService.generateActivityBeans(activities, messageLogger);
		return activityBeans;
	}


	private static interface IActivityBeanFiller {
		ActivityBean fill(Activity act, IMessageLogger logger);
	}

	// ---------------------------------------------------------------------------------------------------------------------- //
	// OVDJE SLIJEDE IMPLEMENTACIJE LOKALIZIRANOG PRIKAZA POJEDINIH OBAVIJESTI
	// ---------------------------------------------------------------------------------------------------------------------- //
	
	private static class ApplicationActivityBeanFiller implements IActivityBeanFiller {
		@Override
		public ActivityBean fill(Activity act, IMessageLogger logger) {
			String msg = null;
			String[] elems = StringUtil.split(act.getData(), '\t');
			msg = logger.getText("activity.msg.application", new String[] {elems[2],elems[4]});
			return new ActivityBean(act.getId(), act.getDate(), msg, act.getArchived(), act.getViewed());
		}
	}
	
	private static class GradeActivityBeanFiller implements IActivityBeanFiller {
		@Override
		public ActivityBean fill(Activity act, IMessageLogger logger) {
			String msg = null;
			String[] elems = StringUtil.split(act.getData(), '\t');
			String kind = elems[2];
			if(kind.equals(ActivityEventKind.CREATED.name())) {
				msg = logger.getText("activity.msg.grade.c", new String[] {elems[4],elems[0]});
			} else if(kind.equals(ActivityEventKind.DELETED.name())) {
				msg = logger.getText("activity.msg.grade.d", new String[] {elems[4],elems[0]});
			} else if(kind.equals(ActivityEventKind.MODIFIED.name())) {
				msg = logger.getText("activity.msg.grade.m", new String[] {elems[4],elems[0]});
			} else if(kind.equals(ActivityEventKind.PUBLISH.name())) {
				msg = logger.getText("activity.msg.grade", new String[] {elems[4],elems[0]});
			} else {
				msg = logger.getText("activity.msg.grade", new String[] {elems[4],elems[0]});
			}
			return new ActivityBean(act.getId(), act.getDate(), msg, act.getArchived(), act.getViewed());
		}
	}

	private static class MarketActivityBeanFiller implements IActivityBeanFiller {
		@Override
		public ActivityBean fill(Activity act, IMessageLogger logger) {
			String msg = null;
			String[] elems = StringUtil.split(act.getData(), '\t');
			// {kind, parentGroupID, username, groupname, isvucode, coursename, parentGroupName}
			if(elems[0].equals("1")) {
				msg = logger.getText("activity.msg.market.k1", new String[] {elems[2],elems[3], elems[5], elems[6]});
			} else {
				msg = logger.getText("activity.msg.market.k2", new String[] {elems[3],elems[5], elems[6]});
			}
			return new ActivityBean(act.getId(), act.getDate(), msg, act.getArchived(), act.getViewed());
		}
	}

	private static class GroupActivityBeanFiller implements IActivityBeanFiller {
		@Override
		public ActivityBean fill(Activity act, IMessageLogger logger) {
			String msg = null;
			String[] elems = StringUtil.split(act.getData(), '\t');
			// {kind, parentGroup, groupName, isvucode, coursename, podvrsta, ...}
			if(elems[0].equals("1")) {
				msg = logger.getText("activity.msg.group.k1", new String[] {elems[4], elems[1], elems[2]});
			} else {
				msg = logger.getText("activity.msg.group.k2", new String[] {elems[4], elems[1], elems[2]});
			}
			return new ActivityBean(act.getId(), act.getDate(), msg, act.getArchived(), act.getViewed());
		}
	}

	private static class ScoreActivityBeanFiller implements IActivityBeanFiller {
		@Override
		public ActivityBean fill(Activity act, IMessageLogger logger) {
			String msg = null;
			String[] elems = StringUtil.split(act.getData(), '\t');
			// {isvu, courseinstance, component1, component2, ...}
			String sufix = "";
			if(elems[elems.length-1].equals(".")) {
				// Imam varijantu sa "i drugo"
				sufix = ".m";
			}
			StringBuilder sb = new StringBuilder();
			int what = 0; // na kraju će biti 1, 2 ili 3
			for(int i = 2; i < elems.length; i++) {
				char c = elems[i].charAt(0);
				if(c=='a') {
					what = what | 1;
					String[] e = StringUtil.split(elems[i], '#');
					if(sb.length()!=0) sb.append(", ");
					sb.append(e[2]).append(" (").append(e[1]).append(")");
					continue;
				}
				if(c=='f') {
					what = what | 2;
					String[] e = StringUtil.split(elems[i], '#');
					if(sb.length()!=0) sb.append(", ");
					sb.append(e[2]).append(" (").append(e[1]).append(")");
					continue;
				}
			}
			msg = logger.getText("activity.msg.score."+what+sufix, new String[] {elems[1],sb.toString()});
			return new ActivityBean(act.getId(), act.getDate(), msg, act.getArchived(), act.getViewed());
		}
	}
	
	private static class IssueTrackingActivityBeanFiller implements IActivityBeanFiller {
		@Override
		public ActivityBean fill(Activity act, IMessageLogger logger) {
			String msg = null;
			String[] elems = StringUtil.split(act.getData(), '\t');
			// a.setData(iActivity.getUsername()+"\t"+iActivity.getKind()+"\t"+iActivity.getIssueID()+"\t"+iActivity.getUserID()+"\t"+courseInstance.getCourse().getIsvuCode()+"\t"+courseInstance.getCourse().getName());
			if(elems[1].equals("1")) {
				msg = logger.getText("activity.msg.its.1", new String[] {elems[0], elems[5]});
			} else if(elems[1].equals("2")){
				msg = logger.getText("activity.msg.its.2", new String[] {elems[0], elems[5]});
			} else{
				msg = logger.getText("activity.msg.its.3", new String[] {elems[0], elems[5]});
			}
			return new ActivityBean(act.getId(), act.getDate(), msg, act.getArchived(), act.getViewed());
		}
	}

}
