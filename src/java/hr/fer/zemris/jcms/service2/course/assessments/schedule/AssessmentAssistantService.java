package hr.fer.zemris.jcms.service2.course.assessments.schedule;

import hr.fer.zemris.jcms.beans.AssessmentRoomBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentAssistantBean;
import hr.fer.zemris.jcms.beans.ext.AssistantRoomBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentAssistantSchedule;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.parsers.AssistantsJmbagParser;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.AssessmentUtil;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AssessmentAssistantScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

public class AssessmentAssistantService {
	
	public static void assistantsEdit(EntityManager em, AssessmentAssistantScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		Assessment a = data.getAssessment();
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		//izracun koliko je potrebno asistenata
		int aNum = 0;
		if (a.getRooms()!=null)
			for (AssessmentRoom ar : a.getRooms())
				if (ar.isTaken())
					aNum += ar.getRequiredAssistants();
		data.setAssistantsRequired(aNum);
		
		List<User> assistants = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_ASISTENT);
		if (assistants == null || assistants.size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noAssistants"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		fillAssistantBeanList(data,a,data.getAssistantBeanList(),assistants);
		
		//sortiramo listu po prezimenu
		Collections.sort(data.getAssistantBeanList(), new Comparator<AssessmentAssistantBean>() {
			@Override
			public int compare(AssessmentAssistantBean o1, AssessmentAssistantBean o2) {
				int r = StringUtil.HR_COLLATOR.compare(o1.getLastName(), o2.getLastName());
				if (r == 0)
					return StringUtil.HR_COLLATOR.compare(o1.getFirstName(), o2.getFirstName());
				return r;
			}
		});
		
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
	
	public static void assistantsUpdate(EntityManager em, AssessmentAssistantScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		Assessment a = data.getAssessment();
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		//provjera podataka koje smo dobili
		if (data.getAssistantBeanList() == null || data.getAssistantBeanList().size() == 0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		List<User> assistants = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_ASISTENT);
		if (assistants == null || assistants.size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noAssistants"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		//stvaramo mapu asistenata po Id-u
		Map<Long, User> dbAssistantsMap= UserUtil.mapUserById(assistants);
		
		//provjera jesu li podaci u beanu valjani
		for (AssessmentAssistantBean aab : data.getAssistantBeanList()) {
			User u = null;
			try {
				Boolean.valueOf(aab.getTaken());
				u = dbAssistantsMap.get(Long.valueOf(aab.getUserID()));
			} catch (Exception ignorable) {}
			if (u==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		
		//mapiramo vec uzete asistente po userID-u
		Map<Long, AssessmentAssistantSchedule> map = new HashMap<Long, AssessmentAssistantSchedule>();
		if (a.getAssistantSchedule()!=null) {
			map = AssessmentUtil.mapAssistantScheduleByUserID(a.getAssistantSchedule());
		}
		
		//kad smo se uvjerili da je sve ok idemo raditi update
		for (AssessmentAssistantBean aab : data.getAssistantBeanList()) {
			Long id = Long.valueOf(aab.getUserID());
			boolean taken = Boolean.valueOf(aab.getTaken());
			AssessmentAssistantSchedule aas = map.get(id);
			if (aas == null && taken) {
				aas = new AssessmentAssistantSchedule();
				aas.setUser(dbAssistantsMap.get(id));
				aas.setAssessment(a);
				a.getAssistantSchedule().add(aas);
				dh.getAssessmentDAO().save(em, aas);
			} else if (aas!=null && !taken){
				a.getAssistantSchedule().remove(aas);
				aas.setAssessment(null);
				dh.getAssessmentDAO().remove(em, aas);
			}
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void importAssistantsPrepare(EntityManager em, AssessmentAssistantScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
				
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
	
	public static void importAssistants(EntityManager em, AssessmentAssistantScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		Assessment a = data.getAssessment();
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
		List<User> assistants = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_ASISTENT);
		if (assistants == null || assistants.size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noAssistants"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		Map<String, User> assistantsJmbagMap = UserUtil.mapUserByJmbag(assistants);
		
		//mapiramo vec uzete asistente po userID-u
		Map<String, AssessmentAssistantSchedule> map = null;
		if (a.getAssistantSchedule()!=null) {
			map = AssessmentUtil.mapAssistantScheduleByJmbag(a.getAssistantSchedule());
		} else {
			new HashMap<Long, AssessmentAssistantSchedule>();
		}
		
		List<String> list = null;
		if (!StringUtil.isStringBlank(data.getImportData())) {
			try {
				list = AssistantsJmbagParser.parse(new StringReader(data.getImportData()));
			} catch (Exception ex) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.importParseError")+" "+ex.getMessage());
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
		} else {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		//kad smo se uvjerili da je sve ok idemo raditi update
		for (String jmbag : list) {
			
			if (assistantsJmbagMap.get(jmbag)==null) {
				String[] param = {jmbag};
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchAssistant",param));
				continue;
			}
			
			AssessmentAssistantSchedule aas = map.get(jmbag);
			if (aas == null) {
				aas = new AssessmentAssistantSchedule();
				aas.setUser(assistantsJmbagMap.get(jmbag));
				aas.setAssessment(a);
				a.getAssistantSchedule().add(aas);
				dh.getAssessmentDAO().save(em, aas);
			}
			else
				map.remove(jmbag);
		}
		
		for (AssessmentAssistantSchedule aas : map.values()) {
			a.getAssistantSchedule().remove(aas);
			aas.setAssessment(null);
			dh.getAssessmentDAO().remove(em, aas);
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void editAssistantSchedule(EntityManager em, AssessmentAssistantScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		Assessment a = data.getAssessment();
		
		if (a.getAssistantSchedule() == null || a.getAssistantSchedule().size() == 0) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.noAssistantsForAssessment"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		if (a.getRooms() == null || a.getRooms().size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRooms"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		//punimo listu sa svim sobama koje su uzete za assessment
		List<AssessmentRoomBean> roomList = new ArrayList<AssessmentRoomBean>();
		
		//dodajemo praznu sobu
		AssessmentRoomBean tmp = new AssessmentRoomBean();
		tmp.setId("-1");
		tmp.setName("");
		roomList.add(tmp);
		
		boolean roomTaken = false;
		for (AssessmentRoom ar : a.getRooms()) {
			if (ar.isTaken()) {
				AssessmentRoomBean arb = new AssessmentRoomBean();
				arb.setId(String.valueOf(ar.getId()));
				arb.setName(ar.getRoom().getShortName());
				roomList.add(arb);
				roomTaken = true;
			}
		}				
		
		//ako ne postoji ni jedna odabrana soba
		if (!roomTaken) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.noRoomsTaken"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		//sortiramo sobe po imenu
		Collections.sort(roomList, new Comparator<AssessmentRoomBean>() {
				@Override
				public int compare(AssessmentRoomBean o1, AssessmentRoomBean o2) {
					return o1.getName().compareTo(o2.getName());
				}
		});
		data.setRoomList(roomList);
		
		//punimo beanove koji uparuju asistenta i dvoranu
		for (AssessmentAssistantSchedule aas : a.getAssistantSchedule())
			data.getAssistantRoomBeanList().add(fillAssistantRoomBean(aas));
		
		//sortiramo listu po prezimenu
		Collections.sort(data.getAssistantRoomBeanList(),new Comparator<AssistantRoomBean>() {
			@Override
			public int compare(AssistantRoomBean o1, AssistantRoomBean o2) {
				int r = StringUtil.HR_COLLATOR.compare(o1.getLastName(), o2.getLastName());
				if (r == 0)
					return StringUtil.HR_COLLATOR.compare(o1.getFirstName(), o2.getFirstName());
				return r;
			}
		});
		
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
	
	public static void updateAssistantSchedule(EntityManager em, AssessmentAssistantScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		Assessment a = data.getAssessment();
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		List<AssistantRoomBean> beanList = data.getAssistantRoomBeanList();
		if (beanList == null || beanList.size() == 0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		if (a.getAssistantSchedule() == null || a.getAssistantSchedule().size() == 0) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.noAssistantsForAssessment"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if (a.getRooms() == null || a.getRooms().size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRooms"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		//punimo mapu AssessmentRoomova po id.u
		Map<Long,AssessmentRoom> roomMap = new HashMap<Long, AssessmentRoom>();
		for (AssessmentRoom ar : a.getRooms())
			roomMap.put(ar.getId(), ar);
		
		//punimo mapu AssessmentAssistantSchedula po id-u
		Map<Long,AssessmentAssistantSchedule> assistantMap = new HashMap<Long, AssessmentAssistantSchedule>();
		for (AssessmentAssistantSchedule aas : a.getAssistantSchedule())
			assistantMap.put(aas.getId(), aas);
		
		//provjeravamo podatke iz liste
		for (AssistantRoomBean arb : beanList) {
			AssessmentRoom ar = null;
			AssessmentAssistantSchedule aas = null;
			Long id = null;
			try {
				id = Long.valueOf(arb.getAssessmentRoomID());
				ar = roomMap.get(id);
				aas = assistantMap.get(Long.valueOf(arb.getAssessmentScheduleID()));
			} catch (Exception ignorable) {}
			
			if (ar == null && id != -1 || aas == null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		
		//sad idemo napraviti promjene
		for (AssistantRoomBean arb : beanList) {
			AssessmentRoom ar = null;
			if (!arb.getAssessmentRoomID().equals("-1")) 
				ar = roomMap.get(Long.valueOf(arb.getAssessmentRoomID()));
			AssessmentAssistantSchedule aas = assistantMap.get(Long.valueOf(arb.getAssessmentScheduleID()));
			aas.setRoom(ar);
		}

		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	///////////////////
	//Privatne metode//
	///////////////////
	
	private static AssistantRoomBean fillAssistantRoomBean(AssessmentAssistantSchedule aas) {
		
		AssistantRoomBean arb = new AssistantRoomBean();
		
		arb.setAssessmentScheduleID(String.valueOf(aas.getId()));
		arb.setFirstName(aas.getUser().getFirstName());
		arb.setLastName(aas.getUser().getLastName());
		arb.setJmbag(aas.getUser().getJmbag());
		arb.setAssessmentRoomID(aas.getRoom()!=null ? aas.getRoom().getId().toString() :  "");
		
		return arb;
	}

	private static void fillAssistantBeanList(AssessmentAssistantScheduleData data, Assessment a, List<AssessmentAssistantBean> beanList,
			List<User> assistants) {
		
		//smanjit cemo za broj onih asistenata koji su vec rasporedjeni
		int aNum = data.getAssistantsRequired();
		
		Map<Long, AssessmentAssistantSchedule> map = new HashMap<Long, AssessmentAssistantSchedule>();
		
		if (a.getAssistantSchedule()!=null) {
			map = AssessmentUtil.mapAssistantScheduleByUserID(a.getAssistantSchedule());
		}
		
		for (User u : assistants) {
			AssessmentAssistantBean aab = new AssessmentAssistantBean();
			aab.setFirstName(u.getFirstName());
			aab.setLastName(u.getLastName());
			aab.setUserID(String.valueOf(u.getId()));
			aab.setJmbag(u.getJmbag());
			if (map.containsKey(u.getId())) {
				aab.setTaken(String.valueOf(true));
				--aNum;
			}
			else
				aab.setTaken(String.valueOf(false));
			beanList.add(aab);
		}
		data.setAssistantsRequired(aNum);
	}
	
}
