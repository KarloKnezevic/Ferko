package hr.fer.zemris.jcms.service2.course.groups;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import hr.fer.zemris.jcms.beans.RoomBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.EditGroupEventData;
import hr.fer.zemris.jcms.web.actions.data.ListGroupEventsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class GroupEventsService {

	public static void listGroupsEvents(EntityManager em, ListGroupEventsData data) {
		
		// Dohvati grupu i ucitaj kolegij
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;

		GroupSupportedPermission perm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		data.setPerm(perm);
		
		if(!perm.getCanViewEvents()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		List<GroupWideEvent> events = new ArrayList<GroupWideEvent>(data.getGroup().getEvents());
		Collections.sort(events, StringUtil.GROUP_WIDE_EVENT_COMPARATOR);
		data.setEvents(events);
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		data.setOwners(dh.getGroupDAO().findForGroup(em, data.getGroup()));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void newGroupsEvents(EntityManager em, EditGroupEventData data) {
	
		if(!groupsEventsEditingPrepare(em, data)) return;
		
		data.getBean().setDuration(0);
		data.getBean().setId(null);
		data.getBean().setRoomID(null);
		data.getBean().setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		data.getBean().setTitle("");
		data.setResult(AbstractActionData.RESULT_INPUT);
		
	}

	public static void editGroupsEvents(EntityManager em, EditGroupEventData data) {
		
		if(!groupsEventsEditingPrepare(em, data)) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		GroupWideEvent event = null;
		try {
			if(data.getBean().getId()!=null) event = dh.getEventDAO().getGroupWideEvent(em, data.getBean().getId());
		} catch(Exception ignorable) {
		}
		if(event==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(!event.getGroups().contains(data.getGroup())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		data.getBean().setId(event.getId());
		data.getBean().setDuration(event.getDuration());
		data.getBean().setRoomID(event.getRoom()==null ? null : event.getRoom().getId());
		data.getBean().setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getStart()));
		data.getBean().setTitle(event.getTitle());
		data.setResult(AbstractActionData.RESULT_INPUT);

	}

	public static void saveOrUpdateGroupsEvents(EntityManager em, EditGroupEventData data) {
		
		if(!groupsEventsEditingPrepare(em, data)) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		GroupWideEvent event = null;
		if(data.getBean().getId()!=null) {
			try {
				event = dh.getEventDAO().getGroupWideEvent(em, data.getBean().getId());
			} catch(Exception ignorable) {
			}
			if(event==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		if(event!=null && !event.getGroups().contains(data.getGroup())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		Date startAt = null;
		boolean errors = false;
		if(StringUtil.isStringBlank(data.getBean().getStart())) {
			data.getMessageLogger().addErrorMessage("Datum je obavezan.");
			errors = true;
		} else {
			try {
				startAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data.getBean().getStart());
			} catch (ParseException e) {
				data.getMessageLogger().addErrorMessage("Datum se ne može protumačiti.");
				errors = true;
			}
		}
		if(StringUtil.isStringBlank(data.getBean().getTitle())) {
			data.getMessageLogger().addErrorMessage("Naziv je obavezan.");
			errors = true;
		}
		if(data.getBean().getDuration()<1) {
			data.getMessageLogger().addErrorMessage("Trajanje mora biti pozitivan broj.");
			errors = true;
		}
		if(errors) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		if(event==null) {
			event = new GroupWideEvent();
		}
		event.setDuration(data.getBean().getDuration());
		if(event.getIssuer()==null) {
			event.setIssuer(data.getCurrentUser());
		}
		if(!event.getGroups().contains(data.getGroup())) {
			event.getGroups().add(data.getGroup());
		}
		if(!startAt.equals(event.getStart())) event.setStart(startAt);
		event.setTitle(data.getBean().getTitle());
		event.setStrength(EventStrength.MEDIUM);
		
		Room beanRoom = StringUtil.isStringBlank(data.getBean().getRoomID()) ? null : dh.getRoomDAO().get(em, data.getBean().getRoomID());
		if(beanRoom!=null) {
			if(!beanRoom.equals(event.getRoom())) {
				event.setRoom(beanRoom);
			}
		} else {
			event.setRoom(beanRoom);
		}
		if(event.getId()==null) {
			dh.getEventDAO().save(em, event);
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
		} else {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		
	}
	
	private static boolean groupsEventsEditingPrepare(EntityManager em, EditGroupEventData data) {
		
		// Dohvati grupu i ucitaj kolegij
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return false;
		
		GroupSupportedPermission perm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(!perm.getCanManageEvents()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<Room> roomList = dh.getRoomDAO().list(em);
		List<RoomBean> roomBeanList = new ArrayList<RoomBean>(roomList.size());
		for(Room room : roomList) {
			RoomBean roomBean = new RoomBean();
			roomBean.setId(room.getId());
			roomBean.setName(room.getName());
			roomBeanList.add(roomBean);
		}
		Collections.sort(roomBeanList,new Comparator<RoomBean>() {
			@Override
			public int compare(RoomBean o1, RoomBean o2) {
				return StringUtil.HR_COLLATOR.compare(o1.getName(), o2.getName());
			}
		});
		data.setRooms(roomBeanList);

		return true;
	}

}
