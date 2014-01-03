package hr.fer.zemris.jcms.service2.course.assessments.schedule;

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
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

import hr.fer.zemris.jcms.beans.ext.AssessmentRoomArrangedBean;
import hr.fer.zemris.jcms.beans.ext.UserRoomBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentAssistantSchedule;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.UserSpecificEvent;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomTag;
import hr.fer.zemris.jcms.parsers.UserRoomParser;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.assessments.AssessmentInfoGenerator;
import hr.fer.zemris.jcms.service.assessments.FormGenerator;
import hr.fer.zemris.jcms.service.assessments.ListsGenerator;
import hr.fer.zemris.jcms.service.assessments.ScheduleToMailMergeGenerator;
import hr.fer.zemris.jcms.service.assessments.ScheduleToPDFGenerator;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AssessmentScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class AssessmentScheduleService {

	/* ====================================================================================
	 * 
	 * RAD S RASPOREDOM PROVJERA - glavne metode
	 * 
	 * ====================================================================================
	 */

	/**
	 * Metoda dohvaća podatke potrebne da bi se nacrtala početna stranica za izradu rasporeda.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void fetchScheduleMenu(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		Assessment a = data.getAssessment();
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		Collection<AssessmentRoom> rooms = a.getRooms();
		
		if (rooms == null || rooms.size()==0) {
			synchronizeRooms(DAOHelperFactory.getDAOHelper(), em, a, "FER");
		}
		List<AssessmentRoomArrangedBean> beanList = new ArrayList<AssessmentRoomArrangedBean>(rooms.size());
		
		//TODO: napraviti upit koji ce vratiti broj studenata po dvorani
		
		//punimo mapu asistenata mapiranih po dvorani
		int brojAsistenata = 0;
		Map<AssessmentRoom, Integer> assistantPerRoomMap = new HashMap<AssessmentRoom, Integer>();
		if (a.getAssistantSchedule() != null)
			for (AssessmentAssistantSchedule aas : a.getAssistantSchedule()) {
				brojAsistenata++;
				AssessmentRoom ar = aas.getRoom();
				if (ar != null) {
					Integer count = assistantPerRoomMap.get(ar);
					if (count != null)
						assistantPerRoomMap.put(ar, Integer.valueOf(count.intValue()+1));
					else
						assistantPerRoomMap.put(ar, Integer.valueOf(1));
				}
			}

		int brojRasporedenihStudenata = 0;
		int brojZauzetihDvorana = 0;
		int brojPotrebnihAsistenata = 0;
		int raspolozivKapacitet = 0;
		for (AssessmentRoom ar : a.getRooms()) {
			if (ar.isTaken()) {
				brojZauzetihDvorana++;
				brojPotrebnihAsistenata+=ar.getRequiredAssistants();
				Integer uzetoAsistenata = assistantPerRoomMap.get(ar);
				if(uzetoAsistenata==null || uzetoAsistenata.intValue()<ar.getRequiredAssistants()) {
					data.setAssistantScheduleCreated(1);
				}
				raspolozivKapacitet+=ar.getCapacity();
				AssessmentRoomArrangedBean agb = new AssessmentRoomArrangedBean();
				agb.setAssessmentRoomID(ar.getId());
				agb.setRoomName(ar.getRoom().getShortName());
				agb.setCapacity(ar.getCapacity());
				agb.setAssistantRequired(ar.getRequiredAssistants());
				if (ar.getGroup() != null && ar.getGroup().getUsers() != null) {
					agb.setUserNum(ar.getGroup().getUsers().size());
					brojRasporedenihStudenata += ar.getGroup().getUsers().size();
				} else {
					agb.setUserNum(0);
				}
				if (assistantPerRoomMap.get(ar) == null) {
					agb.setAssistantNum(0);
				} else {
					agb.setAssistantNum(assistantPerRoomMap.get(ar));
				}
				int gs = ar.getGroup()==null ? 0 : ar.getGroup().getUsers().size();
				if(gs==0) {
					agb.setFirstUser(null);
					agb.setLastUser(null);
				} else if(gs==1) {
					List<UserGroup> list = new ArrayList<UserGroup>(ar.getGroup().getUsers());
					agb.setFirstUser(list.get(0).getUser());
					agb.setLastUser(list.get(0).getUser());
				} else {
					List<UserGroup> list = new ArrayList<UserGroup>(ar.getGroup().getUsers());
					UserGroup ug1 = Collections.min(list, StringUtil.USER_GROUP_COMPARATOR1);
					UserGroup ug2 = Collections.max(list, StringUtil.USER_GROUP_COMPARATOR1);
					agb.setFirstUser(ug1.getUser());
					agb.setLastUser(ug2.getUser());
				}
				
				beanList.add(agb);
			}
		}

		int ukupniBrojStudenata = 0;
		int objavljeno = 0; // 0 nije; 1 indicira neki problem; 2 sve OK
		if(a.getGroup()!=null) {
			ukupniBrojStudenata += a.getGroup().getUsers().size();
			for(Group g : a.getGroup().getSubgroups()) {
				ukupniBrojStudenata += g.getUsers().size();
				if(g.getEvents().isEmpty()) {
					objavljeno = 1;
				} else {
					for(AbstractEvent e : g.getEvents()) {
						if(e.isHidden()) {
							objavljeno = 1;
						}
					}
				}
			}
		}

		// Jesam li dohvatio studente? Ili nisam (0) ili jesam (2)
		if(ukupniBrojStudenata==0) {
			data.setStudentsFetched(0);
		} else {
			data.setStudentsFetched(2);
		}

		// Jesam li zauzeo dovoljno dvorana?
		if(brojZauzetihDvorana==0) {
			data.setRoomsFetched(0);
		} else if(raspolozivKapacitet<ukupniBrojStudenata) {
			data.setRoomsFetched(1);
		} else {
			data.setRoomsFetched(2);
		}

		// Jesam li rasporedio sve studente? Nisam uopce (0), neke jesam (1) ili sam sve (2)
		if(data.getStudentsFetched()!=0) {
			if(brojRasporedenihStudenata==0) {
				data.setStudentScheduleCreated(0);
			} else if(brojRasporedenihStudenata < ukupniBrojStudenata) {
				data.setStudentScheduleCreated(1);
			} else {
				data.setStudentScheduleCreated(2);
			}
		}

		if(brojAsistenata>0) {
			if(brojAsistenata>=brojPotrebnihAsistenata) {
				data.setAssistantsFetched(2);
			} else {
				data.setAssistantsFetched(1);
			}
		}
		
		// Ako sam uzeo nesto asistenata i dvorana, i nisam otkrio da ista fali, imam dobar raspored:
		if(brojAsistenata>0 && brojPotrebnihAsistenata>0 && brojZauzetihDvorana>0 && data.getAssistantScheduleCreated()==0) {
			data.setAssistantScheduleCreated(2);
		}
		
		if(brojZauzetihDvorana>0) {
			if(objavljeno==0 && brojRasporedenihStudenata>=ukupniBrojStudenata) objavljeno = 2;
			data.setSchedulePublished(objavljeno);
		}
		
		Collections.sort(beanList, new Comparator<AssessmentRoomArrangedBean>() {
			@Override
			public int compare(AssessmentRoomArrangedBean o1,
					AssessmentRoomArrangedBean o2) {
				return StringUtil.HR_COLLATOR.compare(o1.getRoomName(), o2.getRoomName());
			}
		});
		data.setRoomList(beanList);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda dohvaća podatke o jednoj konkretnoj dvorani.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void fetchRoomInfo(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		if (StringUtil.isStringBlank(data.getAssessmentRoomID())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		AssessmentRoom ar = null;
		try {
			ar = dh.getAssessmentDAO().getAssessmentRoom(em, Long.valueOf(data.getAssessmentRoomID()));
		} catch(Exception ignorable) {}
		
		if(ar==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.assessmentRoomNotFound"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		Assessment a = ar.getAssessment();

		data.setAssessment(a);
		data.setCourseInstance(a.getCourseInstance());
		data.setRoomName(ar.getRoom().getShortName());
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		//punimo info s asistentima
		List<User> assistantList = new ArrayList<User>();
		if (ar.getAssessment().getAssistantSchedule() != null)
			for (AssessmentAssistantSchedule aas : ar.getAssessment().getAssistantSchedule())
				if (ar.equals(aas.getRoom()))
					assistantList.add(aas.getUser());
		
		data.setAssistantList(assistantList);
		
		//provjeravamo ima li studenata
		Group g = ar.getGroup();
		if (g == null || g.getUsers() == null || 
				g.getUsers().size()==0) {
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		//punimo info sa studentima
		List<User> userList = new ArrayList<User>(g.getUsers().size());
		List<UserGroup> tmpList = new ArrayList<UserGroup>(g.getUsers());
		//sortiramo po positionu
		Collections.sort(tmpList, new Comparator<UserGroup>() {
			@Override
			public int compare(UserGroup o1, UserGroup o2) {
				return o1.getPosition()-o2.getPosition();
			}
		});
		for (UserGroup ug : tmpList)
			userList.add(ug.getUser());
		
		data.setUserList(userList);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda priprema PDF s ispitnom papirologijom (popisi studenata, asistenata i sl.).
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void prepareListingsPDF(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		File f = null;
		try {
			f = File.createTempFile("JCMS_", ".pdf");
		} catch (IOException e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		BufferedOutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(f));
			ListsGenerator lgen = new ListsGenerator(os);
			lgen.generateLists(data.getAssessment());
			lgen.close();
		} catch (IOException e) {
			try { if(os!=null) os.close(); } catch(Exception ignorable) {}
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		try { if(os!=null) os.close(); } catch(Exception ignorable) {}
		DeleteOnCloseFileInputStream stream = null;
		try {
			stream = new DeleteOnCloseFileInputStream(f);
		} catch (IOException e) {
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		stream.setFileName("popisi.pdf");
		stream.setMimeType("application/pdf");
		data.setStream(stream);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda priprema PDF s anonimiziranim rasporedom studenata na provjeri.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void prepareSchedulePDF(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		File f = null;
		try {
			f = File.createTempFile("JCMS_", ".pdf");
		} catch (IOException e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		BufferedOutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(f));
			ScheduleToPDFGenerator lgen = new ScheduleToPDFGenerator(os);
			lgen.generateLists(data.getAssessment());
			lgen.close();
		} catch (IOException e) {
			try { if(os!=null) os.close(); } catch(Exception ignorable) {}
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		try { if(os!=null) os.close(); } catch(Exception ignorable) {}
		DeleteOnCloseFileInputStream stream = null;
		try {
			stream = new DeleteOnCloseFileInputStream(f);
		} catch (IOException e) {
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		stream.setFileName("raspored.pdf");
		stream.setMimeType("application/pdf");
		data.setStream(stream);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda priprema MailMerge datoteku s rasporedom studenata.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void prepareMailMerge(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		File f = null;
		try {
			f = File.createTempFile("JCMS_", ".txt");
		} catch (IOException e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		String encoding = "UTF-8";
		if("1".equals(data.getCp())) {
			encoding = "Windows-1250";
		}
		Writer os = null;
		try {
			os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f)),encoding);
			ScheduleToMailMergeGenerator lgen = new ScheduleToMailMergeGenerator(os);
			lgen.generateLists(data.getAssessment());
			lgen.close();
		} catch (IOException e) {
			try { if(os!=null) os.close(); } catch(Exception ignorable) {}
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGenerateMailMerge"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		try { if(os!=null) os.close(); } catch(Exception ignorable) {}
		DeleteOnCloseFileInputStream stream = null;
		try {
			stream = new DeleteOnCloseFileInputStream(f);
		} catch (IOException e) {
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGenerateMailMerge"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		stream.setFileName("MailMerge.txt");
		stream.setMimeType("text/plain; charset="+encoding);
		data.setStream(stream);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda priprema MailMerge datoteku s rasporedom studenata.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void prepareAssessmentInfo(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		File f = null;
		try {
			f = File.createTempFile("JCMS_", ".txt");
		} catch (IOException e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		String encoding = "UTF-8";
		if("1".equals(data.getCp())) {
			encoding = "Windows-1250";
		}
		Writer os = null;
		try {
			os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f)),encoding);
			AssessmentInfoGenerator lgen = new AssessmentInfoGenerator(os);
			lgen.generateLists(data.getAssessment());
			lgen.close();
		} catch (IOException e) {
			try { if(os!=null) os.close(); } catch(Exception ignorable) {}
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGenerateAssessmentInfo"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		try { if(os!=null) os.close(); } catch(Exception ignorable) {}
		DeleteOnCloseFileInputStream stream = null;
		try {
			stream = new DeleteOnCloseFileInputStream(f);
		} catch (IOException e) {
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGenerateAssessmentInfo"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		stream.setFileName("AssessmentInfo.txt");
		stream.setMimeType("text/plain; charset="+encoding);
		data.setStream(stream);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda priprema PDF s obrascima za studente.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void prepareAnswerSheetsPDF(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		Assessment scheduleAssessment = data.getAssessment();

		// Ako postoji posebno zadana provjera iz koje dohvaćamo raspored, dohvatimo je
		if(!StringUtil.isStringBlank(data.getScheduleAssessmentID())) {
			Assessment a = null;
			try {
				a = DAOHelperFactory.getDAOHelper().getAssessmentDAO().get(em, Long.valueOf(data.getScheduleAssessmentID()));
			} catch(Exception ex) {
			}
			if(a==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			scheduleAssessment = a;
		}
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance()) || !data.getAssessment().getCourseInstance().equals(scheduleAssessment.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		File f = null;
		try {
			f = File.createTempFile("JCMS_", ".pdf");
		} catch (IOException e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		BufferedOutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(f));
			FormGenerator lgen = new FormGenerator(os);
			lgen.generateForms(data.getAssessment(), scheduleAssessment);
			lgen.close();
		} catch (IOException e) {
			try { if(os!=null) os.close(); } catch(Exception ignorable) {}
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		try { if(os!=null) os.close(); } catch(Exception ignorable) {}
		DeleteOnCloseFileInputStream stream = null;
		try {
			stream = new DeleteOnCloseFileInputStream(f);
		} catch (IOException e) {
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		stream.setFileName("obrasci.pdf");
		stream.setMimeType("application/pdf");
		data.setStream(stream);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda dohvaća sve studente koji trebaju pisati provjeru (prema postavljenoj zastavici ili sve s kolegija)
	 * i povezuje ih s provjerom.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void retrieveAssessmentStudents(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		boolean doit = false;
		try {
			doit = Boolean.valueOf(data.getDoit());
		} catch (Exception ignorable) {}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
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
			return;
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.studentsSuccessfullySynchronized"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda raspoređuje studente po dvoranama.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void makeStudentSchedule(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		boolean arranged = false;				

		Assessment a = data.getAssessment();
		
		int userNum = getUserNum(dh, em, a);
		
		if (userNum==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noStudents"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		if (userNum>getRoomCapacity(a)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.notEnoughCapacity"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		if (userNum != a.getGroup().getUsers().size()) 
			arranged = true;
		
		boolean doit = false;
		try {
			doit = Boolean.valueOf(data.getDoit());
		} catch (Exception ignorable) {}
		
		if (arranged && !doit) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
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
		
		if ("random".equals(data.getType())) {
			//radimo random raspored
			Collections.shuffle(userList);
		}
		else {
			//abecedno sortiranje usera po prezimenu, pa po imenu
			Collections.sort(userList, myComparator);
		}

		List<AssessmentRoom> scheduledRooms = new ArrayList<AssessmentRoom>();
		int totalCapacity = 0;
		for (AssessmentRoom ar : roomList) {
			if (ar.isTaken() && ar.getCapacity()>0) {
				scheduledRooms.add(ar);
				totalCapacity += ar.getCapacity();
			}
		}
		
		int[] brojStudenata = new int[scheduledRooms.size()];
		if("true".equals(data.getProportional())) {
			double percentage = (double)userList.size()/(double)totalCapacity;
			int left = userList.size();
			for(int i = 0; i < scheduledRooms.size(); i++) {
				AssessmentRoom ar = scheduledRooms.get(i);
				int zaSobu = (int)(ar.getCapacity() * percentage + 0.5);
				if(zaSobu > left) zaSobu = left;
				brojStudenata[i] = zaSobu;
				left -= zaSobu;
			}
			while(left>0) {
				boolean anyChange = false;
				for(int i = 0; left>0 && i < scheduledRooms.size(); i++) {
					AssessmentRoom ar = scheduledRooms.get(i);
					if(brojStudenata[i] >= ar.getCapacity()) {
						continue;
					}
					brojStudenata[i]++;
					anyChange=true;
					left--;
				}
				if(!anyChange) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateSchedule"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return;
				}
			}
		} else {
			int left = userList.size();
			for(int i = 0; i < scheduledRooms.size(); i++) {
				AssessmentRoom ar = scheduledRooms.get(i);
				int zaSobu = ar.getCapacity();
				if(zaSobu > left) zaSobu = left;
				brojStudenata[i] = zaSobu;
				left -= zaSobu;
			}
		}

		//idemo napunit sobe
		Iterator<UserGroup> it = userList.iterator();
		for(int ri = 0; ri < scheduledRooms.size(); ri++) {
			AssessmentRoom ar = scheduledRooms.get(ri);
			int studenata = brojStudenata[ri];
			if (ar.getGroup() == null)
				createNewRoomGroup(dh,em,ar,a.getGroup());
			Group g = ar.getGroup();
			int i=0, capacity = studenata;
			
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

		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.studentsScheduled"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda objavljuje događaje o pisanju provjere studentima i asistentima.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void publishEvents(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Assessment a = data.getAssessment();
		
		int userNum = getUserNum(dh, em, a);
		
		if (userNum == a.getGroup().getUsers().size() || userNum == 0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noStudentSchedule"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
						
		if (a.getEvent() == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRootEvent"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}

		// Eventi za studente:
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
		
		// Eventi za asistente:
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
					use.setContext("a:"+a.getId());
					
					aas.getRoom().setUserEvent(use);
					dh.getEventDAO().save(em, use);
				} else {
					if(StringUtil.isStringBlank(use.getContext())) {
						use.setContext("a:"+a.getId());
					}
				}
				
				use.getUsers().add(aas.getUser());
			}
		}
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.eventsBroadcastSuccessful"));
	}		

	/**
	 * Metoda dohvaća parametre potrebne za prikaz formulara za unos gotovog rasporeda.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void importSchedulePrepare(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	/**
	 * Metoda importa prethodno napravljeni raspored za provjeru u sustav.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void importSchedule(EntityManager em, AssessmentScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		if(StringUtil.isStringBlank(data.getScheduleImport())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		List<UserRoomBean> beanList = null;
		try { 
			if ("1".equals(data.getType())) {
				beanList = UserRoomParser.parseMailMerge(new StringReader(data.getScheduleImport()));
			} else if ("2".equals(data.getType())) {
				beanList = UserRoomParser.parseTabbedFormat(new StringReader(data.getScheduleImport()));
			} else {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.importParseError")+" "+ex.getMessage());
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		boolean doit = false;
		try {
			doit = Boolean.valueOf(data.getDoit());
		} catch (Exception ignorable) {}
		
		Assessment a = data.getAssessment();
		if (a.getRooms() == null || a.getRooms().size() == 0) {
			if (!synchronizeRooms(dh, em, a, "FER")) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRooms"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
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
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
			userSet.add(userMap.get(urb.getJmbag()));
			
			if (roomMap.get(urb.getShortRoomName())==null) {
				String[] param = new String[1];
				param[0] = urb.getShortRoomName();
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchRoom",param));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
		}
		
		//dohvacamo grupu assessmenta, ako je nema stvaramo je
		if (a.getGroup() == null)
			createNewRootGroup(dh,em,a);
		 
		//idemo sinkronizirati popis korisnika koji smo dobili
		if (!synchronizeGroupUsers(dh,em,a,userSet,doit)) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
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
	}

	
	/* ====================================================================================
	 * 
	 * RAD S RASPOREDOM PROVJERA - pomoćne metode
	 * 
	 * ====================================================================================
	 */
	
	/**
	 * Metoda koja sinkronizira skup AssessmentRoomova nekog Assessmenta sa skupom dvorana iz baze za zadani venue.
	 * (stvara nove AssessmentRoomove ukoliko odgovarajuci ne postoje za odredjenu dvoranu iz baze)
	 * @param dh
	 * @param em
	 * @param a
	 * @param venueShortName
	 * @return
	 */
	public static boolean synchronizeRooms(DAOHelper dh, EntityManager em, Assessment a, String venueShortName) {
		
		Collection<AssessmentRoom> rooms = a.getRooms();
		boolean result = false;
		
		//popuni set s roomovima koji trenutno postoje za assessment
		Set<Room> assessmentRoomSet = new HashSet<Room>(rooms.size());
				
		for (AssessmentRoom ar : rooms) {
			assessmentRoomSet.add(ar.getRoom());
		}

		// sinkroniziraj sobe (stvori nove assessmentRoomove ako ih nema)
		List<Room> roomList = dh.getRoomDAO().listByVenue(em,venueShortName);
		for (Room r : roomList) {
			if (!assessmentRoomSet.contains(r) && r.getAssessmentPlaces() > 0 && r.getPublicRoom()) {
				createAssessmentRoom(em, a, r);
				result = true;
			}
		}
		
		return result;
	}

	/**
	 * Metoda na provjeru dodaje prostoriju.
	 * 
	 * @param em entity manager
	 * @param a provjera
	 * @param r soba
	 */
	public static void createAssessmentRoom(EntityManager em, Assessment a, Room r) {
		createAssessmentRoomEx(em, a, r);
	}

	public static AssessmentRoom createAssessmentRoomEx(EntityManager em, Assessment a, Room r) {
		AssessmentRoom ar = new AssessmentRoom();
		ar.setAssessment(a);
		ar.setAvailable(true);
		ar.setCapacity(r.getAssessmentPlaces());
		ar.setGroup(null);
		ar.setRequiredAssistants(r.getAssessmentAssistants());
		ar.setRoom(r);
		ar.setRoomTag(AssessmentRoomTag.MANDATORY);
		ar.setTaken(false);
		ar.setUserEvent(null);

		a.getRooms().add(ar);
		em.persist(ar);
		return ar;
	}

	/**
	 * Metoda koja stvara jednu vrsnu grupu Assessmenta
	 * @param dh
	 * @param em
	 * @param a
	 */
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
	 * Metoda koja vraca id prve sljedece slobodne grupe unutar grupa za Assessmente
	 * 
	 * @param dh
	 * @param em
	 * @param compositeCourseID id predmeta
	 * @param relativePath path unutar kojeg se trazi prvi slobodni id (oblika "4/%", 4/1/%", ... uvijek pocinje s "4/") 
	 * @return
	 */
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
	 * Metoda koja brise sve groupe, sve groupWideEvente i userSpecificEvente iz AssessmentRoomova.
	 * Pri tome se ne diraju statusi dvorana (rezervirane dvorane i dalje ostaju rezervirane),
	 * i ne diraju se uneseni asistenti na provjeru (iako im se brise raspored).
	 * @param dh
	 * @param em
	 * @param a
	 */
	static void clearAssessmentSchedule(DAOHelper dh, EntityManager em, Assessment a) {
		
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
			
			// Korekcija implementiranog ponasanja:
			//  - iteriramo kroz asistente i brisemo im samo dodijeljene dvorane,
			//    jer nema smisla ponovno dohvacati i upisivati jednom dohvacene asistente
			for (AssessmentAssistantSchedule aas : a.getAssistantSchedule()) {
				aas.setRoom(null);
				//aas.setUser(null);
				//aas.setAssessment(null);
				//dh.getAssessmentDAO().remove(em, aas);
			}
			// a.getAssistantSchedule().clear();
		}
	}

	
	/**
	 * Metoda dohvaća iz baze broj studenata smještenih u podgrupe zadane grupe.
	 * 
	 * @param dh dao helper
	 * @param em entity manager
	 * @param a provjera
	 * @return ukupni broj studenata u podgrupama
	 */
	private static int getUserNum(DAOHelper dh, EntityManager em, Assessment a) {
		
		int userNumber = 0;
		if (a.getGroup() != null) {
			Number num = dh.getUserDAO().getUserNumber(em, a.getCourseInstance().getId(),
					a.getGroup().getRelativePath()+"/%", a.getGroup().getRelativePath());
			userNumber = num.intValue();
		}
		return userNumber;
	}

	/**
	 * Metoda računa ukupni kapacitet svih zauzetih dvorana za određenu provjeru znanja.
	 * 
	 * @param a provjera
	 * @return ukupni kapacitet
	 */
	private static int getRoomCapacity(Assessment a) {
		
		int currCapacity = 0;
		for (AssessmentRoom ar : a.getRooms()) {
			if (ar.isTaken()) currCapacity += ar.getCapacity();
		}
		return currCapacity;
	}

}
