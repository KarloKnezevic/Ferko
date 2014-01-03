package hr.fer.zemris.jcms.service2.course.assessments;

import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.jcms.beans.AssessmentFlagValueBean;
import hr.fer.zemris.jcms.beans.StringNameStringValue;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagTag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.CourseWideEvent;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomStatus;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.assessments.DynaCodeEngineFactory;
import hr.fer.zemris.jcms.service.assessments.defimpl.SourceCodeUtils;
import hr.fer.zemris.jcms.service.has.HasAssessment;
import hr.fer.zemris.jcms.service.has.HasAssessmentFlag;
import hr.fer.zemris.jcms.service.util.AssessmentUtil;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.service2.course.assessments.schedule.AssessmentRoomService;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentEditData;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentFlagDataData;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentFlagEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;
import hr.fer.zemris.jcms.locking.LockPath;

import javax.persistence.EntityManager;

/**
 * Sloj usluge koji nudi uređivanje provjera i zastavica.
 * 
 * @author marcupic
 *
 */
public class AssessmentsEditingService {

	public static void adminAssessmentFlagDataSave(EntityManager em, AdminAssessmentFlagDataData data) {
		// Priprema podataka
		if(!adminAssessmentFlagDataPrepare(em, data)) return;

		LockPath lp = data.getLockPath();
		if(lp==null || !lp.getPart(0).equals("ml") || !lp.getPart(1).startsWith("ci") || !data.getCourseInstance().getId().equals(lp.getPart(1).substring(2)) || !lp.getPart(2).equals("a") || !lp.getPart(3).startsWith("f") || !data.getAssessmentFlag().getId().toString().equals(lp.getPart(3).substring(1)) || !data.getCourseInstanceID().equals(data.getCourseInstance().getId())) {
			data.getMessageLogger().addWarningMessage("Pogrešan poziv.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(data.getFlagValues()==null || data.getFlagValues().isEmpty()) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("AssessmentFlags.info.values.notUpdated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<User> users = dh.getCourseInstanceDAO().findCourseUsers(em, data.getCourseInstance().getId());
		Map<Long, User> userByIDMap = UserUtil.mapUserById(users);
		
		List<AssessmentFlagValue> flagValues = dh.getAssessmentDAO().listFlagValuesForAssessmentFlag(em, data.getAssessmentFlag());
		Map<Long, AssessmentFlagValue> valueByUserIDMap = AssessmentUtil.mapAssessmentFlagValueByUserID(flagValues);

		boolean error = false;
		for(AssessmentFlagValueBean b : data.getFlagValues()) {
			if(!userByIDMap.containsKey(b.getStudentId())) {
				data.getMessageLogger().addErrorMessage("Pronađen student "+b.getStudentJMBAG()+" kojeg nema na kolegiju.");
				error = true;
			}
		}
		if(error) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("AssessmentFlags.info.values.notUpdated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		int updates = 0;
		int inserts = 0;
		for(AssessmentFlagValueBean b : data.getFlagValues()) {
			// Ako ja nisam nista dirao, vozi dalje...
			if(b.isManuallySet()==b.isOriginalManuallySet() && b.isManualValue()==b.isOriginalManualValue()) {
				continue;
			}
			User u = userByIDMap.get(b.getStudentId());
			if(!b.isManuallySet() && b.isManualValue()) {
				b.setManualValue(false);
				data.getMessageLogger().addWarningMessage("Preskočeno ažuriranje podataka o studentu JMBAG="+u.getJmbag()+". Tražena nedozvoljena kombinacija: fiksiraj=NE a fiksirana vrijednost=DA.");
				continue;
			}
			AssessmentFlagValue v = valueByUserIDMap.get(b.getStudentId());
			// Mogucnosti:
			// Ako ga prije nije bilo:
			if(b.getId()==null) {
				// I ako ga jos uvijek nema:
				if(v==null) {
					// mogu ga dodati, jer ga prije nije bilo
					v = addNewFlagValue(em, data.getAssessmentFlag(), u, data.getCurrentUser(), b);
					valueByUserIDMap.put(u.getId(), v);
					inserts++;
					continue;
				} else {
					if(v.getManuallySet()!=b.isManuallySet() || v.getManualValue()!=b.isManualValue()) {
						// preskoci jer imamo nekompatibilnu promjenu; to javi; netko ga je u meduvremenu dodao...
						data.getMessageLogger().addWarningMessage("Podatke o studentu JMBAG="+u.getJmbag()+" je netko paralelno promijenio - vaša promjena stoga nije provedena.");
						continue;
					} else {
						// preskoci jer je sve OK; netko ga je u meduvremenu dodao, ali tocno onako kako smo i mi htjeli...
						continue;
					}
				}
			}
			// Ako vise ne postoji:
			if(v==null) {
				// preskoci jer ga je netko ocito obrisao s razlogom; dojavi to...
				data.getMessageLogger().addWarningMessage("Podatke o studentu JMBAG="+u.getJmbag()+" je netko paralelno izbrisao - vaša promjena stoga nije provedena.");
				continue;
			}
			if(v.getVersion()>b.getVersion()) {
				// Ako je u meduvremenu verzija zapisa u bazi porasla, netko je nesto mijenjao:
				if(v.getManuallySet()!=b.isManuallySet() || v.getManualValue()!=b.isManualValue()) {
					// preskoci jer imamo nekompatibilnu promjenu; to javi; netko ga je u meduvremenu dodao...
					data.getMessageLogger().addWarningMessage("Podatke o studentu JMBAG="+u.getJmbag()+" je netko paralelno promijenio - vaša promjena stoga nije provedena.");
					continue;
				} else {
					// preskoci jer je sve OK; netko ga je u meduvremenu dodao, ali tocno onako kako smo i mi htjeli...
					continue;
				}
			}
			if(v.getManuallySet()!=b.isManuallySet() || v.getManualValue()!=b.isManualValue()) {
				v.setManuallySet(b.isManuallySet());
				v.setManualValue(b.isManualValue());
				v.setAssigner(data.getCurrentUser());
				updates++;
				continue;
			}
		}
		if(inserts==0 && updates==0) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("AssessmentFlags.info.values.notUpdated"));
		} else {
			data.getMessageLogger().addInfoMessage("Broj ažuriranih zapisa: "+updates+", broj stvorenih zapisa "+inserts+".");
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void adminAssessmentFlagDataResetManual(EntityManager em, AdminAssessmentFlagDataData data) {
		// Priprema podataka
		if(!adminAssessmentFlagDataPrepare(em, data)) return;

		if(!data.isConfirmed()) {
			data.setResult(AbstractActionData.RESULT_CONFIRM);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		List<AssessmentFlagValue> flagValues = dh.getAssessmentDAO().listFlagValuesForAssessmentFlag(em, data.getAssessmentFlag());
		for(AssessmentFlagValue v : flagValues) {
			v.setManuallySet(false);
			v.setManualValue(false);
			v.setAssigner(null);
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("AssessmentFlags.info.values.reseted"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	private static AssessmentFlagValue addNewFlagValue(EntityManager em,
			AssessmentFlag assessmentFlag, User u, User assigner, AssessmentFlagValueBean b) {
		AssessmentFlagValue afv = new AssessmentFlagValue();
		afv.setAssessmentFlag(assessmentFlag);
		afv.setAssigner(assigner);
		afv.setError(false);
		afv.setManuallySet(b.isManuallySet());
		afv.setManualValue(b.isManuallySet() && b.isManualValue());
		afv.setUser(u);
		afv.setValue(false);
		DAOHelperFactory.getDAOHelper().getAssessmentDAO().save(em, afv);
		assessmentFlag.getValues().add(afv);
		return afv;
	}

	public static void adminAssessmentFlagDataShow(EntityManager em, AdminAssessmentFlagDataData data) {

		// Priprema podataka
		if(!adminAssessmentFlagDataPrepare(em, data)) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<User> users = dh.getCourseInstanceDAO().findCourseUsers(em, data.getCourseInstance().getId());

		HashSet<String> lettersSet = new LinkedHashSet<String>(40);
		Collections.sort(users, StringUtil.USER_COMPARATOR);
		for(User u : users) {
			if(u.getLastName().length()>0) {
				lettersSet.add(u.getLastName().substring(0, 1));
			}
		}
		List<String> letters = new ArrayList<String>(lettersSet);

		if(letters.isEmpty()) {
			data.setFlagValues(new ArrayList<AssessmentFlagValueBean>());
			data.setLetter("*");
			data.setLetters(new ArrayList<String>());
			data.getLetters().add("*");
			// Gotovi smo
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		data.setLetters(new ArrayList<String>(letters));
		data.getLetters().add("*");

		if(StringUtil.isStringBlank(data.getLetter())) {
			data.setLetter(letters.get(0));
		}
		
		if(data.getLetter().length()>1) {
			data.setLetter(data.getLetter().substring(0,1));
		}
		if(!data.getLetter().equals("*")) {
			if(!lettersSet.contains(data.getLetter())) {
				data.setLetter(letters.get(0));
			}
		}
		
		char letter = data.getLetter().charAt(0);

		List<AssessmentFlagValue> flagValues = dh.getAssessmentDAO().listFlagValuesForAssessmentFlag(em, data.getAssessmentFlag());
		
		data.setFlagValues(new ArrayList<AssessmentFlagValueBean>(users.size()));
		
		Map<Long, AssessmentFlagValue> valueByUserIDMap = AssessmentUtil.mapAssessmentFlagValueByUserID(flagValues);
		for(User u : users) {
			if(letter != '*' && (u.getLastName().length()==0 || (u.getLastName().charAt(0)!=letter))) {
				continue;
			}
			AssessmentFlagValueBean b = new AssessmentFlagValueBean();
			b.setStudentFirstName(u.getFirstName());
			b.setStudentLastName(u.getLastName());
			b.setStudentId(u.getId());
			b.setStudentJMBAG(u.getJmbag());

			AssessmentFlagValue v = valueByUserIDMap.get(u.getId());
			if(v==null) {
				b.setAssignerFirstName(null);
				b.setAssignerLastName(null);
				b.setAssignerId(null);
				b.setAssignerJMBAG(null);
				
				b.setError(false);
				b.setId(null);
				b.setManuallySet(false);
				b.setOriginalManualValue(false);
				b.setOriginalManuallySet(false);
				b.setManualValue(false);
				
				b.setValue(false);
				b.setVersion(-1);
			} else {
				b.setAssignerFirstName(v.getAssigner()==null ? null : v.getAssigner().getFirstName());
				b.setAssignerLastName(v.getAssigner()==null ? null : v.getAssigner().getLastName());
				b.setAssignerId(v.getAssigner()==null ? null : v.getAssigner().getId());
				b.setAssignerJMBAG(v.getAssigner()==null ? null : v.getAssigner().getJmbag());
				
				b.setError(v.getError());
				b.setId(v.getId());
				b.setManuallySet(v.getManuallySet());
				b.setOriginalManuallySet(v.getManuallySet());
				b.setManualValue(v.getManualValue());
				b.setOriginalManualValue(v.getManualValue());
				
				b.setValue(v.getValue());
				b.setVersion(v.getVersion());
			}
			data.getFlagValues().add(b);
		}
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	/* ====================================================================================
	 * 
	 * RAD S PROVJERAMA - glavne metode
	 * 
	 * ====================================================================================
	 */
	
	/**
	 * Metoda koja priprema stvaranje nove provjere znanja.
	 * Kada je popunjavanje potrebnih parametara gotovo, parametri se snimaju pozivom metode 
	 * {@link #adminAssessmentSaveOrUpdate(EntityManager, AdminAssessmentEditData)}.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentNew(EntityManager em, AdminAssessmentEditData data) {

		// Priprema podataka
		if(!adminAssessmentEditPrepare(em, data)) return;
		
		// Priprema praznog beana
		data.getBean().setAssesmentTagID("");
		data.getBean().setCourseInstanceID(data.getCourseInstance().getId().toString());
		data.getBean().setId(null);
		data.getBean().setName("");
		data.getBean().setShortName("");
		data.getBean().setMaxScore("");
		data.getBean().setStartsAt("");
		data.getBean().setDuration("");
		StringBuilder sb = new StringBuilder();
		sb.append("setPassed(rawScore()>0);\n");
		sb.append("setPresent(rawPresent());\n");
		sb.append("setScore(rawScore());\n");
		data.getBean().setProgram(sb.toString());
		data.getBean().setProgramType("java");
		data.getBean().setProgramVersion(0);
		data.getBean().setAssesmentFlagID(null);
		data.getBean().setChainedParentID(null);
		data.getBean().setParentID(null);
		data.getBean().setVisibility("V");
		data.getBean().setSortIndex(0);
		data.getBean().setLocked(false);
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	/**
	 * Metoda koja iz baze dohvaća postojeću provjeru i priprema je za editiranje.
	 * Kada je editiranje gotovo, parametri se snimaju pozivom metode {@link #adminAssessmentSaveOrUpdate(EntityManager, AdminAssessmentEditData)}.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentEdit(EntityManager em, AdminAssessmentEditData data) {

		// Priprema podataka
		if(!adminAssessmentEditPrepare(em, data)) return;

		// Učitavanje provjere
		Assessment assessment = loadAssessment(em, data, data.getBean().getId());
		if(assessment==null) {
			return;
		}
		if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		provjeriZauzeteDvorane(data, assessment);
		
		Iterator<Assessment> it = data.getPossibleScoreSources().iterator();
		while(it.hasNext()) {
			Assessment a = it.next();
			if(a.getId().equals(assessment.getId())) {
				it.remove();
				break;
			}
		}
		// Priprema beana iz podataka
		data.getBean().setId(assessment.getId().toString());
		data.getBean().setAssesmentTagID(assessment.getAssessmentTag()!=null ? assessment.getAssessmentTag().getId().toString() : "");
		data.getBean().setCourseInstanceID(assessment.getCourseInstance().getId().toString());
		data.getBean().setName(assessment.getName());
		data.getBean().setShortName(assessment.getShortName());
		data.getBean().setProgram(assessment.getProgram());
		data.getBean().setProgramType(assessment.getProgramType());
		data.getBean().setProgramVersion(assessment.getProgramVersion());
		data.getBean().setAssesmentFlagID(assessment.getAssessmentFlag()!=null ? assessment.getAssessmentFlag().getId().toString() : "");
		data.getBean().setChainedParentID(assessment.getChainedParent()!=null ? assessment.getChainedParent().getId().toString() : "");
		data.getBean().setParentID(assessment.getParent()!=null ? assessment.getParent().getId().toString() : "");
		data.getBean().setMaxScore(assessment.getMaxScore()==null ? "" : assessment.getMaxScore().toString());
		if(assessment.getEvent()!=null) {
			data.getBean().setDuration(String.valueOf(assessment.getEvent().getDuration()));
			data.getBean().setStartsAt(assessment.getEvent().getStartAsText());
			data.getBean().setEventHidden(assessment.getEvent().isHidden());
		}
		data.getBean().setVisibility(String.valueOf(assessment.getVisibility()));
		data.getBean().setSortIndex(assessment.getSortIndex());
		data.getBean().setLocked(assessment.getAssessmentLocked());
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	/**
	 * Pomocna metoda koja provjerava ima li na provjeri dvorana rezerviranih u sustavu rezervacija i ako ima,
	 * javlja upozorenje.
	 * @param data podatkovni objekt
	 * @param assessment provjera
	 * @return <code>true</code> ako ima, <code>false</code> ako nema
	 */
	private static boolean provjeriZauzeteDvorane(AdminAssessmentEditData data, Assessment assessment) {
		if(assessment.getRooms()==null || assessment.getRooms().isEmpty()) return false;
		boolean anyTaken = false;
		for(AssessmentRoom ar : assessment.getRooms()) {
			if(AssessmentRoomStatus.MANUALLY_RESERVED.equals(ar.getRoomStatus()) || AssessmentRoomStatus.RESERVED.equals(ar.getRoomStatus())) {
				anyTaken = true;
				break;
			}
		}
		if(anyTaken) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("warning.liveReservationRequired"));
		}
		return anyTaken;
	}

	/**
	 * Metoda koja u bazu zapisuje novu provjeru znanja ili ažurira postojeću. Radi li se o novoj ili
	 * postojećoj provjeri znanja, utvrđuje se uvidom u identifikator provjere (ako je <code>null</code>
	 * ili prazan, provjera je nova).
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentSaveOrUpdate(EntityManager em, AdminAssessmentEditData data) {

		// Priprema podataka
		if(!adminAssessmentEditPrepare(em, data)) return;

		// Radimo li update postojeće provjere, ili save nove?
		boolean update = !StringUtil.isStringBlank(data.getBean().getId());
		
		// Dohvati provjeru ako radimo update
		Assessment assessment = null;
		
		Date stariDatum = null;
		int staroTrajanje = 0;
		boolean trebaSinkroniziratiRezervacije = false;
		
		if(update) {
			assessment = loadAssessment(em, data, data.getBean().getId());
			if(assessment==null) {
				return;
			}
			if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			trebaSinkroniziratiRezervacije = provjeriZauzeteDvorane(data, assessment);
			Iterator<Assessment> it = data.getPossibleScoreSources().iterator();
			while(it.hasNext()) {
				Assessment a = it.next();
				if(a.getId().equals(assessment.getId())) {
					it.remove();
					break;
				}
			}
			if(assessment.getEvent()!=null) {
				stariDatum = new Date(assessment.getEvent().getStart().getTime());
				staroTrajanje = assessment.getEvent().getDuration();
			}
		}
		
		// Učitavanje provjere
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		Date startAt = null;
		int duration = 0;
		if(!StringUtil.isStringBlank(data.getBean().getStartsAt())) {
			try {
				startAt = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(data.getBean().getStartsAt());
			} catch (ParseException e) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.dateSyntaxError"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
			if(!StringUtil.isStringBlank(data.getBean().getDuration())) {
				try {
					duration = Integer.parseInt(data.getBean().getDuration());
				} catch (Exception e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.dateDurationSyntaxError"));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
				if(duration<1) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.dateDurationNonPositive"));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
			} else {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.dateDurationNotSpecified"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
		}
		
		if(trebaSinkroniziratiRezervacije && stariDatum!=null && !stariDatum.equals(startAt)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.dateChangeNotPossibleDueReservations"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		if(!"E".equals(data.getBean().getVisibility()) && !"V".equals(data.getBean().getVisibility()) && !"H".equals(data.getBean().getVisibility())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidVisibility"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		AssessmentTag tag = null;
		if(data.getBean().getAssesmentTagID()!=null && !data.getBean().getAssesmentTagID().equals("") && !data.getBean().getAssesmentTagID().equals("-1")) {
			try {
				tag = dh.getAssessmentTagDAO().get(em, Long.valueOf(data.getBean().getAssesmentTagID()));
			} catch(Exception ignorable) {
			}
			if(tag==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		AssessmentFlag flag = null;
		if(data.getBean().getAssesmentFlagID()!=null && !data.getBean().getAssesmentFlagID().equals("") && !data.getBean().getAssesmentFlagID().equals("-1")) {
			try {
				flag = dh.getAssessmentDAO().getFlag(em, Long.valueOf(data.getBean().getAssesmentFlagID()));
			} catch(Exception ignorable) {
			}
			if(flag==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		Assessment parent = null;
		if(data.getBean().getParentID()!=null && !data.getBean().getParentID().equals("") && !data.getBean().getParentID().equals("-1")) {
			try {
				parent = dh.getAssessmentDAO().get(em, Long.valueOf(data.getBean().getParentID()));
			} catch(Exception ignorable) {
			}
			if(parent==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		Assessment chainedParent = null;
		if(data.getBean().getChainedParentID()!=null && !data.getBean().getChainedParentID().equals("") && !data.getBean().getChainedParentID().equals("-1")) {
			try {
				chainedParent = dh.getAssessmentDAO().get(em, Long.valueOf(data.getBean().getChainedParentID()));
			} catch(Exception ignorable) {
			}
			if(chainedParent==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		
		boolean willDoIt = true;
		
		if(data.getBean().getName()==null || data.getBean().getName().trim().equals("")) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.nameMustBeGiven"));
			willDoIt = false;
		}
		if(data.getBean().getShortName()==null || data.getBean().getShortName().trim().equals("")) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.shortNameMustBeGiven"));
			willDoIt = false;
		}
		if(!checkAssessmentProgram(data)) {
			willDoIt = false;
		}
		Double maxScore = null;
		try {
			maxScore = StringUtil.stringToDouble(data.getBean().getMaxScore());
		} catch(NumberFormatException ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.doubleNumberFormatException"));
			willDoIt = false;
		}
		if(startAt==null && assessment!=null && assessment.getEvent()!=null) {
			if(StringUtil.isStringBlank(data.getBean().getDuration())) data.getBean().setDuration(String.valueOf(assessment.getEvent().getDuration()));
			if(StringUtil.isStringBlank(data.getBean().getStartsAt())) data.getBean().setStartsAt(assessment.getEvent().getStartAsText());
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.canNotRemoveDate"));
			willDoIt = false;
		}
		if(assessment!=null && parent!=null && assessment.getId().equals(parent.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParentDetected"));
			willDoIt = false;
		}
		if(parent!=null && chainedParent!=null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.tooManyParents"));
			willDoIt = false;
		}
		if(chainedParent!=null) {
			Assessment other = dh.getAssessmentDAO().findForChainedParent(em, chainedParent);
			// Ako ova projera jos ne postoji, a postoji druga koja ima istog ulancanog roditelja, ili
			// ako ova provjera postoji, postoji i neka koja ima odabranog ulancanog roditelja, i ta NIJE trenutna:
			if((assessment == null && other != null) || (assessment!=null && other!=null && !assessment.getId().equals(other.getId()))) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.tooManyChainedChildren"));
				willDoIt = false;
			}
		}
		if(!willDoIt) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		if(assessment==null) {
			assessment = new Assessment();
		}
		updateAssessmentProgram(data);
		boolean programChanged = !update || !StringUtil.stringEquals(assessment.getProgram(), data.getBean().getProgram()) || !StringUtil.stringEquals(assessment.getProgramType(),data.getBean().getProgramType());
		if(programChanged && !StringUtil.isStringBlank(data.getBean().getProgram())) {
			if(!SourceCodeUtils.checkForIllegalConstructs(data.getBean().getProgram())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.illegalProgramConstructs"));
				willDoIt = false;
			}
			if(willDoIt && !DynaCodeEngineFactory.getEngine().tryCompile(data.getMessageLogger(), "A", data.getBean().getProgram())) {
				willDoIt = false;
			}
		}
		
		if(trebaSinkroniziratiRezervacije && stariDatum!=null && assessment.getEvent()!=null && staroTrajanje!=duration) {
			boolean uspjeh = AssessmentRoomService.syncReservationsDurationEx(em, data, assessment, staroTrajanje, duration, true);
			if(!uspjeh) {
				willDoIt = false;
			}
		}

		if(!willDoIt) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		assessment.setAssessmentTag(tag);
		assessment.setName(data.getBean().getName());
		assessment.setShortName(data.getBean().getShortName());
		assessment.setCourseInstance(data.getCourseInstance());
		assessment.setAssessmentFlag(flag);
		assessment.setParent(parent);
		assessment.setChainedParent(chainedParent);
		assessment.setMaxScore(maxScore);
		assessment.setVisibility(data.getBean().getVisibility().charAt(0));
		assessment.setAssessmentLocked(data.getBean().isLocked());
		assessment.setSortIndex(data.getBean().getSortIndex());
		if(programChanged) {
			assessment.setProgram(data.getBean().getProgram());
			assessment.setProgramType(data.getBean().getProgramType());
			if(update) {
				assessment.setProgramVersion(assessment.getProgramVersion()+1);
			} else {
				assessment.setProgramVersion(0);
			}
		}
		boolean eventVisibilityChanged = false;
		if(assessment.getEvent()!=null) {
			assessment.getEvent().setDuration(duration);
			assessment.getEvent().setStart(startAt);
			assessment.getEvent().setContext("a:"+assessment.getId());
			eventVisibilityChanged = assessment.getEvent().isHidden()!=data.getBean().getEventHidden();
			assessment.getEvent().setHidden(data.getBean().getEventHidden());
		} else if(startAt!=null) {
			CourseWideEvent cwe = new CourseWideEvent();
			cwe.setCourseInstance(data.getCourseInstance());
			cwe.setDuration(duration);
			cwe.setIssuer(data.getCurrentUser());
			cwe.setSpecifier(data.getCourseInstance().getYearSemester().getId()+"/ispiti");
			cwe.setStart(startAt);
			cwe.setStrength(EventStrength.STRONG);
			cwe.setTitle(assessment.getName());
			cwe.setContext("a:"+assessment.getId());
			eventVisibilityChanged = cwe.isHidden()!=data.getBean().getEventHidden();
			cwe.setHidden(data.getBean().getEventHidden());
			dh.getEventDAO().save(em, cwe);
			assessment.setEvent(cwe);
		}

		if(eventVisibilityChanged) {
			propagateEventVisibilityChange(em, assessment);
		}
		
		if(trebaSinkroniziratiRezervacije && stariDatum!=null && assessment.getEvent()!=null && staroTrajanje!=assessment.getEvent().getDuration()) {
			boolean uspjeh = AssessmentRoomService.syncReservationsDurationEx(em, data, assessment, staroTrajanje, assessment.getEvent().getDuration(), false);
			if(!uspjeh) {
				data.getMessageLogger().addInfoMessage("Pogreška u komuniciranju sa sustavom rezervacija.");
			}
		}
		
		if(update) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Assessments.info.updated"));
		} else {
			dh.getAssessmentDAO().save(em, assessment);
			assessment.getCourseInstance().getAssessments().add(assessment);
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Assessments.info.added"));
		}
		data.setAssessment(assessment);
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	private static void updateAssessmentProgram(AdminAssessmentEditData data) {
		if(StringUtil.isStringBlank(data.getBean().getProgramType())) {
			return;
		}
		if(data.getBean().getProgramType().equals("java")) {
			// Za java programe ne radimo posebno procesiranje...
			return;
		}
		if(data.getBean().getProgramType().equals("gui1")) {
			AssessmentProgramGUI1 gui = parseAssessmentProgramGui1(data);
			data.getBean().setProgram(gui.generate());
		}
		if(data.getBean().getProgramType().equals("gui2")) {
			AssessmentProgramGUI2 gui = parseAssessmentProgramGui2(data);
			data.getBean().setProgram(gui.generate());
		}
		return;
	}

	private static boolean checkAssessmentProgram(AdminAssessmentEditData data) {
		if(StringUtil.isStringBlank(data.getBean().getProgramType())) {
			return true;
		}
		if(data.getBean().getProgramType().equals("java")) {
			// Za java programe ne radimo posebno procesiranje...
			return true;
		}
		if(data.getBean().getProgramType().equals("gui1")) {
			return checkAssessmentProgramGui1(data);
		}
		if(data.getBean().getProgramType().equals("gui2")) {
			return checkAssessmentProgramGui2(data);
		}
		return false;
	}

	private static boolean checkAssessmentProgramGui2(AdminAssessmentEditData data) {
		return null!=parseAssessmentProgramGui2(data);
	}

	private static boolean checkAssessmentProgramGui1(AdminAssessmentEditData data) {
		return null!=parseAssessmentProgramGui1(data);
	}

	private static AssessmentProgramGUI1 parseAssessmentProgramGui1(AdminAssessmentEditData data) {
		AssessmentProgramGUI1 gui = new AssessmentProgramGUI1();
		String guiStr = data.getGuiConfig();
		if(StringUtil.isStringBlank(guiStr)) {
			data.getMessageLogger().addErrorMessage("Pogreška u primljenim podatcima (v1). Imate li uključen JavaScript?");
			return null;
		}
		if(!guiStr.startsWith("//@@1")) {
			data.getMessageLogger().addErrorMessage("Pogreška u primljenim podatcima (v2). Imate li uključen JavaScript?");
			return null;
		}
		String podatci = guiStr.substring(5);
		String[] elems = podatci.split("\t");
		gui.guiConfig = data.getGuiConfig();
		try {
			int i = 0;
			while(i<elems.length) {
				if(elems[i].startsWith("@A")) {
					AssessmentProgramGUI1Constraint constr = new AssessmentProgramGUI1Constraint();
					constr.sname = elems[i].substring(2);
					try {
						if(!elems[i+1].equals("null")) constr.factor = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					try {
						if(!elems[i+2].equals("null")) constr.limitMin = StringUtil.stringToDouble(elems[i+2]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+2]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					try {
						if(!elems[i+3].equals("null")) constr.limitMax = StringUtil.stringToDouble(elems[i+3]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+3]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					if(elems[i+4].equals("1")) {
						constr.mustBePresent = true;
					} else if(elems[i+4].equals("0")) {
						constr.mustBePresent = false;
					} else {
						data.getMessageLogger().addErrorMessage(""+elems[i+4]+" se ne može protumačiti kao zastavica (problem s JavaScript-om?).");
						return null;
					}
					if(elems[i+5].equals("1")) {
						constr.mustPass = true;
					} else if(elems[i+5].equals("0")) {
						constr.mustPass = false;
					} else {
						data.getMessageLogger().addErrorMessage(""+elems[i+5]+" se ne može protumačiti kao zastavica (problem s JavaScript-om?).");
						return null;
					}
					gui.constraints.add(constr);
					i+=6;
					continue;
				}
				if(elems[i].equals("@ClimitMin")) {
					try {
						if(!elems[i+1].equals("null")) gui.limitMin = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					i+=2;
					continue;
				}
				if(elems[i].equals("@ClimitMax")) {
					try {
						if(!elems[i+1].equals("null")) gui.limitMax = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					i+=2;
					continue;
				}
				if(elems[i].equals("@CminSum")) {
					try {
						if(!elems[i+1].equals("null")) gui.requiredMinimum = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					i+=2;
					continue;
				}
				if(elems[i].equals("@CscalingFactor")) {
					try {
						if(!elems[i+1].equals("null")) gui.scalingFactor = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					i+=2;
					continue;
				}
				if(elems[i].equals("@CatLeastPresent")) {
					try {
						if(!elems[i+1].equals("null")) gui.atLeastPresent = Integer.valueOf(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao cijeli broj.");
						return null;
					}
					i+=2;
					continue;
				}
				if(elems[i].equals("@CatLeastPassed")) {
					try {
						if(!elems[i+1].equals("null")) gui.atLeastPassed = Integer.valueOf(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao cijeli broj.");
						return null;
					}
					i+=2;
					continue;
				}
				data.getMessageLogger().addErrorMessage("Pogreška u primljenim podatcima (v4). Imate li uključen JavaScript?");
				return null;
			}
		} catch(Exception ex) { // U slučaju indexOutOfBoundsException-a i sl.
			data.getMessageLogger().addErrorMessage("Pogreška u primljenim podatcima (v3). Imate li uključen JavaScript?");
			return null;
		}
		return gui;
	}

	private static AssessmentProgramGUI2 parseAssessmentProgramGui2(AdminAssessmentEditData data) {
		AssessmentProgramGUI2 gui = new AssessmentProgramGUI2();
		String guiStr = data.getGuiConfig();
		if(StringUtil.isStringBlank(guiStr)) {
			data.getMessageLogger().addErrorMessage("Pogreška u primljenim podatcima (v1). Imate li uključen JavaScript?");
			return null;
		}
		if(!guiStr.startsWith("//@@2")) {
			data.getMessageLogger().addErrorMessage("Pogreška u primljenim podatcima (v2). Imate li uključen JavaScript?");
			return null;
		}
		String podatci = guiStr.substring(5);
		String[] elems = podatci.split("\t");
		gui.guiConfig = data.getGuiConfig();
		try {
			int i = 0;
			while(i<elems.length) {
				if(elems[i].equals("@ClimitMin")) {
					try {
						if(!elems[i+1].equals("null")) gui.limitMin = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					i+=2;
					continue;
				}
				if(elems[i].equals("@ClimitMax")) {
					try {
						if(!elems[i+1].equals("null")) gui.limitMax = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					i+=2;
					continue;
				}
				if(elems[i].equals("@CminSum")) {
					try {
						if(!elems[i+1].equals("null")) gui.requiredMinimum = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					i+=2;
					continue;
				}
				if(elems[i].equals("@CscalingFactor")) {
					try {
						if(!elems[i+1].equals("null")) gui.scalingFactor = StringUtil.stringToDouble(elems[i+1]);
					} catch(NumberFormatException ex) {
						data.getMessageLogger().addErrorMessage(""+elems[i+1]+" se ne može protumačiti kao decimalni broj.");
						return null;
					}
					i+=2;
					continue;
				}
				data.getMessageLogger().addErrorMessage("Pogreška u primljenim podatcima (v4). Imate li uključen JavaScript?");
				return null;
			}
		} catch(Exception ex) { // U slučaju indexOutOfBoundsException-a i sl.
			data.getMessageLogger().addErrorMessage("Pogreška u primljenim podatcima (v3). Imate li uključen JavaScript?");
			return null;
		}
		return gui;
	}

	private static class AssessmentProgramGUI1Constraint {
		String sname;
		Double factor;
		Double limitMin;
		Double limitMax;
		boolean mustPass;
		boolean mustBePresent;
	}
	
	private static class AssessmentProgramGUI1 {
		List<AssessmentProgramGUI1Constraint> constraints = new ArrayList<AssessmentProgramGUI1Constraint>();
		Double limitMin;
		Double limitMax;
		Double scalingFactor;
		Double requiredMinimum;
		String guiConfig;
		Integer atLeastPresent;
		Integer atLeastPassed;
		
		public String generate() {
			StringBuilder sb = new StringBuilder();
			sb.append(guiConfig).append("\n");
			sb.append("double sc = 0;\n");
			sb.append("int bioNa = 0;\n");
			sb.append("int prosaoProvjera = 0;\n");
			sb.append("boolean prosao = true;\n");
			sb.append("\n");
			for(AssessmentProgramGUI1Constraint constr : constraints) {
				sb.append("if(present(\"").append(constr.sname).append("\")) {\n");
				sb.append("  bioNa++;\n");
				if(constr.factor!=null && Math.abs(constr.factor.doubleValue()-1.0)>0.0001) {
					sb.append("  double sc2 = score(\"").append(constr.sname).append("\") * ").append(constr.factor.doubleValue()).append(";\n");
				} else {
					sb.append("  double sc2 = score(\"").append(constr.sname).append("\");\n");
				}
				if(constr.limitMin!=null) {
					sb.append("  if(sc2<").append(constr.limitMin.doubleValue()).append(") sc2 = ").append(constr.limitMin.doubleValue()).append(";\n");
				}
				if(constr.limitMax!=null) {
					sb.append("  if(sc2>").append(constr.limitMax.doubleValue()).append(") sc2 = ").append(constr.limitMax.doubleValue()).append(";\n");
				}
				sb.append("  sc += sc2;\n");
				sb.append("  if(passed(\"").append(constr.sname).append("\")) prosaoProvjera++;\n");
				if(constr.mustPass) {
					sb.append("  if(!passed(\"").append(constr.sname).append("\")) prosao = false;\n");
				}
				if(constr.mustBePresent || constr.mustPass) {
					sb.append("} else {\n");
					sb.append("  prosao = false;\n");
					sb.append("}\n");
				} else {
					sb.append("}\n");
				}
			}
			sb.append("\n");
			if(scalingFactor!=null && Math.abs(scalingFactor.doubleValue()-1.0)>0.0001) {
				sb.append("sc = sc * ").append(scalingFactor.doubleValue()).append(";\n");
			}
			if(limitMin!=null) {
				sb.append("if(sc<").append(limitMin.doubleValue()).append(") sc=").append(limitMin.doubleValue()).append(";\n");
			}
			if(limitMax!=null) {
				sb.append("if(sc>").append(limitMax.doubleValue()).append(") sc=").append(limitMax.doubleValue()).append(";\n");
			}
			sb.append("\n");
			sb.append("if(bioNa<1) {\n");
			sb.append("  setPresent(false);\n");
			sb.append("  setPassed(false);\n");
			sb.append("  setScore(0);\n");
			sb.append("} else {\n");
			sb.append("  setPresent(true);\n");
			if(requiredMinimum!=null) {
				sb.append("  if(sc<").append(requiredMinimum.doubleValue()).append(") prosao=false;\n");
			}
			if(atLeastPresent!=null) {
				sb.append("  if(bioNa<").append(atLeastPresent.intValue()).append(") prosao=false;\n");
			}
			if(atLeastPassed!=null) {
				sb.append("  if(prosaoProvjera<").append(atLeastPassed.intValue()).append(") prosao=false;\n");
			}
			sb.append("  setPassed(prosao);\n");
			sb.append("  setScore(sc);\n");
			sb.append("}\n");
			return sb.toString();
		}
	}

	private static class AssessmentProgramGUI2 {
		Double limitMin;
		Double limitMax;
		Double scalingFactor;
		Double requiredMinimum;
		String guiConfig;
		
		public String generate() {
			StringBuilder sb = new StringBuilder();
			sb.append(guiConfig).append("\n");
			sb.append("setPresent(rawPresent());\n");
			sb.append("\n");
			sb.append("if(!rawPresent()) {\n");
			sb.append("  setPassed(false);\n");
			sb.append("  setScore(0);\n");
			sb.append("} else {\n");
			if(scalingFactor!=null && Math.abs(scalingFactor.doubleValue()-1.0)>0.0001) {
				sb.append("  double sc = rawScore() * ").append(scalingFactor.doubleValue()).append(";\n");
			} else {
				sb.append("  double sc = rawScore();\n");
			}
			if(limitMin!=null) {
				sb.append("  if(sc<").append(limitMin.doubleValue()).append(") sc=").append(limitMin.doubleValue()).append(";\n");
			}
			if(limitMax!=null) {
				sb.append("  if(sc>").append(limitMax.doubleValue()).append(") sc=").append(limitMax.doubleValue()).append(";\n");
			}
			if(requiredMinimum!=null) {
				sb.append("  setPassed(sc>=").append(requiredMinimum.doubleValue()).append(");\n");
			} else {
				sb.append("  setPassed(true);\n");
			}
			sb.append("  setScore(sc);\n");
			sb.append("}\n");
			return sb.toString();
		}
	}
	
	public static void propagateEventVisibilityChange(EntityManager em, Assessment assessment) {
		// Pogledaj je li na provjeri napravljen raspored;
		// Ako je, pronađi evente i potom im modificiraj vidljivost u skladu s vidljivosti predanog eventa
		for(AssessmentRoom r : assessment.getRooms()) {
			if(r.getUserEvent()!=null) {
				r.getUserEvent().setHidden(assessment.getEvent().isHidden());
			}
			if(r.getGroup()!=null && !r.getGroup().getEvents().isEmpty()) {
				for(GroupWideEvent gwe : r.getGroup().getEvents()) {
					gwe.setHidden(assessment.getEvent().isHidden());
				}
			}
		}
	}

	/* ====================================================================================
	 * 
	 * RAD S PROVJERAMA - pomoćne metode
	 * 
	 * ====================================================================================
	 */

	/**
	 * Pomoćna metoda koja priprema podatke za akcije stvaranja/uređivanja provjera. U slučaju
	 * nastupanja pogreške, tekst pogreške i potreban status automatski će biti postavljeni
	 * u predani podatkovni objekt.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 * @return <code>false</code> ako je došlo do greške, <code>true</code> inače
	 */
	static boolean adminAssessmentEditPrepare(EntityManager em, AdminAssessmentEditData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return false;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		
		data.setTags(dh.getAssessmentTagDAO().list(em));
		data.setFlags(dh.getAssessmentDAO().listFlagsForCourseInstance(em, data.getCourseInstance().getId()));
		data.setPossibleChainedParents(dh.getAssessmentDAO().listForCourseInstance(em, data.getCourseInstance().getId()));
		data.setPossibleParents(new ArrayList<Assessment>(data.getPossibleChainedParents()));
		data.setPossibleScoreSources(new ArrayList<Assessment>(data.getPossibleChainedParents()));
		Collections.sort(data.getPossibleScoreSources(), new Comparator<Assessment>() {
			@Override
			public int compare(Assessment o1, Assessment o2) {
				return o1.getShortName().compareTo(o2.getShortName());
			}
		});
		// TODO: Ovdje izbaci one za koje bi ovo bilo nedopušteno...
		// ...
		// I potom ubaci na početak prazan placeholder
		data.getTags().add(0, new AssessmentTag(false, Long.valueOf(-1), "", ""));
		data.getFlags().add(0, new AssessmentFlag(Long.valueOf(-1)));
		data.getPossibleChainedParents().add(0, new Assessment(Long.valueOf(-1)));
		data.getPossibleParents().add(0, new Assessment(Long.valueOf(-1)));
		
		data.setVisibilities(new ArrayList<StringNameStringValue>());
		data.getVisibilities().add(new StringNameStringValue("V",data.getMessageLogger().getText("visibility.V", "Always visible")));
		data.getVisibilities().add(new StringNameStringValue("E",data.getMessageLogger().getText("visibility.E", "Visible is present")));
		data.getVisibilities().add(new StringNameStringValue("H",data.getMessageLogger().getText("visibility.H", "Always hidden")));
		
		return true;
	}

	/**
	 * Pomoćna metoda koja učitava provjeru temeljem predanog identifikatora. Ako nastupi pogreška,
	 * sve potrebno postavit će se u predani podatkovni objekt.
	 *  
	 * @param <T> tip podatkovnog objekta
	 * @param em entity manager
	 * @param data podatkovni objekt
	 * @param id identifikator provjere
	 * @return tražena provjera ili <code>null</code> u slučaju pogreške 
	 */
	static <T extends AbstractActionData & HasAssessment> Assessment loadAssessment(EntityManager em, T data, String id) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Assessment assessment = null;
		if(StringUtil.isStringBlank(id)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return null;
		}
		try {
			assessment = dh.getAssessmentDAO().get(em, Long.valueOf(id));
		} catch(Exception ignorable) {
		}
		if(assessment==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return null;
		}
		data.setAssessment(assessment);
		return assessment;
	}

	/* ====================================================================================
	 * 
	 * RAD S ZASTAVICAMA - glavne metode
	 * 
	 * ====================================================================================
	 */

	/**
	 * Metoda koja priprema stvaranje nove zastavice.
	 * Kada je popunjavanje potrebnih parametara gotovo, parametri se snimaju pozivom metode 
	 * {@link #adminAssessmentFlagSaveOrUpdate(EntityManager, AdminAssessmentFlagEditData)}.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentFlagNew(EntityManager em, AdminAssessmentFlagEditData data) {

		// Priprema podataka
		adminAssessmentFlagEditPrepare(em, data);
		
		// Popunjavanje beana defaultnim podatcima
		data.getBean().setAssesmentFlagTagID("");
		data.getBean().setCourseInstanceID(data.getCourseInstance().getId().toString());
		data.getBean().setId(null);
		data.getBean().setName("");
		data.getBean().setShortName("");
		data.getBean().setProgram("if(overrideSet()) {\r\n  setValue(overrideValue());\r\n} else {\r\n  setValue(false);\r\n}\r\n");
		data.getBean().setProgramType("java");
		data.getBean().setProgramVersion(0);
		data.getBean().setVisibility("V");
		data.getBean().setSortIndex(0);
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
	
	/**
	 * Metoda koja iz baze dohvaća postojeću zastavicu i priprema je za editiranje.
	 * Kada je editiranje gotovo, parametri se snimaju pozivom metode {@link #adminAssessmentFlagSaveOrUpdate(EntityManager, AdminAssessmentFlagEditData)}.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentFlagEdit(EntityManager em, AdminAssessmentFlagEditData data) {

		// Priprema podataka
		adminAssessmentFlagEditPrepare(em, data);
		
		// Dohvat zastavice
		AssessmentFlag flag = loadAssessmentFlag(em, data, data.getBean().getId());
		if(flag==null) {
			return;
		}
		if(!flag.getCourseInstance().equals(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		// Popunjavanje beana podatcima iz zastavice
		data.getBean().setId(flag.getId().toString());
		data.getBean().setAssesmentFlagTagID(flag.getAssesmentFlagTag()!=null ? flag.getAssesmentFlagTag().getId().toString() : "");
		data.getBean().setCourseInstanceID(flag.getCourseInstance().getId().toString());
		data.getBean().setName(flag.getName());
		data.getBean().setShortName(flag.getShortName());
		data.getBean().setProgram(flag.getProgram());
		data.getBean().setProgramType(flag.getProgramType());
		data.getBean().setProgramVersion(flag.getProgramVersion());
		String vis = "V";
		switch(flag.getVisibility()) {
			case 'H': vis = "H"; break;
			case 'E': vis = "E"; break;
		}
		data.getBean().setVisibility(vis);
		data.getBean().setSortIndex(flag.getSortIndex());
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
				
	/**
	 * Metoda koja u bazu zapisuje novu zastavicu ili ažurira postojeću. Radi li se o novoj ili
	 * postojećoj zastavici, utvrđuje se uvidom u identifikator zastavice (ako je <code>null</code>
	 * ili prazan, provjera je nova).
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentFlagSaveOrUpdate(EntityManager em, AdminAssessmentFlagEditData data) {
		
		// Priprema podataka
		adminAssessmentFlagEditPrepare(em, data);
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		// dohvat podataka o zastavici ako radimo update
		boolean update = !StringUtil.isStringBlank(data.getBean().getId());
		AssessmentFlag flag = null;
		if(update) {
			flag = loadAssessmentFlag(em, data, data.getBean().getId());
			if(flag==null) {
				return;
			}
			if(!flag.getCourseInstance().equals(data.getCourseInstance())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		
		AssessmentFlagTag tag = null;
		if(!StringUtil.isStringBlank(data.getBean().getAssesmentFlagTagID()) && !data.getBean().getAssesmentFlagTagID().equals("-1")) {
			try {
				tag = dh.getAssessmentFlagTagDAO().get(em, Long.valueOf(data.getBean().getAssesmentFlagTagID()));
			} catch(Exception ignorable) {
			}
			if(tag==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}

		// Ako trebam napraviti insert/update, provjeri je li sve u redu?
		boolean willDoIt = true;
		if(StringUtil.isStringBlank(data.getBean().getName())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.nameMustBeGiven"));
			willDoIt = false;
		}
		if(StringUtil.isStringBlank(data.getBean().getShortName())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.shortNameMustBeGiven"));
			willDoIt = false;
		}
		boolean isNew = (flag == null);
		if(flag==null) {
			flag = new AssessmentFlag();
		}
		boolean programChanged = isNew || !StringUtil.stringEquals(flag.getProgram(), data.getBean().getProgram()) || !StringUtil.stringEquals(flag.getProgramType(), data.getBean().getProgramType());

		if(programChanged && !StringUtil.isStringBlank(data.getBean().getProgram())) {
			if(!SourceCodeUtils.checkForIllegalConstructs(data.getBean().getProgram())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.illegalProgramConstructs"));
				willDoIt = false;
			}
			if(willDoIt && !DynaCodeEngineFactory.getEngine().tryCompile(data.getMessageLogger(), "F", data.getBean().getProgram())) {
				willDoIt = false;
			}
		}
		if(!"E".equals(data.getBean().getVisibility()) && !"V".equals(data.getBean().getVisibility()) && !"H".equals(data.getBean().getVisibility())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidVisibility"));
			willDoIt = false;
		}

		if(!willDoIt) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		flag.setVisibility(data.getBean().getVisibility().charAt(0));
		flag.setAssesmentFlagTag(tag);
		flag.setName(data.getBean().getName());
		flag.setShortName(data.getBean().getShortName());
		flag.setCourseInstance(data.getCourseInstance());
		if(programChanged) {
			flag.setProgram(data.getBean().getProgram());
			flag.setProgramType(data.getBean().getProgramType());
			if(isNew) {
				flag.setProgramVersion(0);
			} else {
				flag.setProgramVersion(flag.getProgramVersion()+1);
			}
		}
		if(isNew) {
			dh.getAssessmentDAO().save(em, flag);
			flag.getCourseInstance().getFlags().add(flag);
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("AssessmentFlags.info.added"));
		} else {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("AssessmentFlags.info.updated"));
		}
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/* ====================================================================================
	 * 
	 * RAD S ZASTAVICAMA - pomoćne metode
	 * 
	 * ====================================================================================
	 */

	/**
	 * Pomoćna metoda koja priprema podatke za akcije stvaranja/uređivanja zastavica. U slučaju
	 * nastupanja pogreške, tekst pogreške i potreban status automatski će biti postavljeni
	 * u predani podatkovni objekt.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 * @return <code>false</code> ako je došlo do greške, <code>true</code> inače
	 */
	static boolean adminAssessmentFlagEditPrepare(EntityManager em, AdminAssessmentFlagEditData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return false;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setVisibilities(new ArrayList<StringNameStringValue>());
		data.getVisibilities().add(new StringNameStringValue("V",data.getMessageLogger().getText("visibility.V", "Always visible")));
		data.getVisibilities().add(new StringNameStringValue("E",data.getMessageLogger().getText("visibility.E", "Visible is present")));
		data.getVisibilities().add(new StringNameStringValue("H",data.getMessageLogger().getText("visibility.H", "Always hidden")));
		data.setTags(dh.getAssessmentFlagTagDAO().list(em));
		data.getTags().add(0, new AssessmentFlagTag(false, Long.valueOf(-1), "", ""));
		
		return true;
	}

	/**
	 * Pomoćna metoda koja učitava zastavicu temeljem predanog identifikatora. Ako nastupi pogreška,
	 * sve potrebno postavit će se u predani podatkovni objekt.
	 *  
	 * @param <T> tip podatkovnog objekta
	 * @param em entity manager
	 * @param data podatkovni objekt
	 * @param id identifikator zastavice
	 * @return tražena zastavica ili <code>null</code> u slučaju pogreške
	 */
	static <T extends AbstractActionData & HasAssessmentFlag> AssessmentFlag loadAssessmentFlag(EntityManager em, T data, String id) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		AssessmentFlag flag = null;
		if(StringUtil.isStringBlank(id)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return null;
		}
		try {
			flag = dh.getAssessmentDAO().getFlag(em, Long.valueOf(id));
		} catch(Exception ignorable) {
		}
		if(flag==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return null;
		}
		data.setAssessmentFlag(flag);
		return flag;
	}

	static boolean adminAssessmentFlagDataPrepare(EntityManager em, AdminAssessmentFlagDataData data) {
		
		// Dohvat i postavljanje zastavice
		if(null==loadAssessmentFlag(em, data, data.getAssessmentFlagID())) {
			return false;
		}
		data.setCourseInstance(data.getAssessmentFlag().getCourseInstance());
		
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}

		return true;
	}

}
