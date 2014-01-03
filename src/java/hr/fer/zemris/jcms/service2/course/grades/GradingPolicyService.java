package hr.fer.zemris.jcms.service2.course.grades;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.activities.types.ActivityEventKind;
import hr.fer.zemris.jcms.activities.types.GradeActivity;
import hr.fer.zemris.jcms.beans.GradingPolicyBean;
import hr.fer.zemris.jcms.beans.GroupGraderBean;
import hr.fer.zemris.jcms.beans.StringNameStringValue;
import hr.fer.zemris.jcms.dao.CourseInstanceDAO;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.locking.LockPath;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Grade;
import hr.fer.zemris.jcms.model.GradesVisibility;
import hr.fer.zemris.jcms.model.GradingPolicy;
import hr.fer.zemris.jcms.model.GradingStat;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.assessments.AssessmentStatus;
import hr.fer.zemris.jcms.service.util.AssessmentUtil;
import hr.fer.zemris.jcms.service.util.GradesUtil;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.GradingPolicyData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DateUtil;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class GradingPolicyService {

	public static void show(EntityManager em, GradingPolicyData data) {
		
		// Dohvati podatke
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if(!JCMSSecurityManagerFactory.getManager().canViewGradingPolicy(ci)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setEditable(JCMSSecurityManagerFactory.getManager().canEditGradingPolicy(ci));
		
		// Postoji li grading policy objekt?
		GradingPolicy gp = ci.getGradingPolicy(); 
		if(gp==null) {
			// Objekt treba biti zakljucan da bih mogao napraviti stvaranje. Je li?
			LockPath lp = data.getLockPath();
			if(lp==null) {
				data.setResult("locking-redirect");
				return;
			}
			if(lp.size()!=3 || !lp.getPart(0).equals("ml") || !lp.getPart(1).equals("ci"+ci.getId()) || !lp.getPart(2).equals("a")) {
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			gp = new GradingPolicy();
			ci.setGradingPolicy(gp);
		}

		convertGPToBean(gp, data.getBean(), data.getMessageLogger());
		
		if(!StringUtil.isStringBlank(data.getBean().getPolicyImplementation())) {
			if(data.getBean().getPolicyImplementation().equals("SC")) {
				// Bodovi za 3, 4 i 5 + provjera za cupanje bodova
				deserializeRulesSC(gp.getRules(), data.getRules());
				fillAssessmentOffering(ci, data.getRules(), "aOffering");
				gp.setRules(serializeRulesSC(ci, data.getRules(), data.getMessageLogger(), true, true));
			} else if(data.getBean().getPolicyImplementation().equals("SP")) {
				// Postotak studenata koji dobiva 2, 3, 4 i 5 + provjera za cupanje prolaza
				deserializeRulesSP(gp.getRules(), data.getRules());
				fillAssessmentOffering(ci, data.getRules(), "aOffering");
				gp.setRules(serializeRulesSP(ci, data.getRules(), data.getMessageLogger(), true, true));
			} else {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unknownPolicyImplementation"));
			}
		} else {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.noPolicyImplementation"));
		}

		if(gp.getGradesValid()) {
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			List<Group> grupe = dh.getGroupDAO().findLectureSubgroups(em, data.getCourseInstance().getId());
			if(grupe!=null && !grupe.isEmpty()) {
				Collections.sort(grupe, StringUtil.GROUP_COMPARATOR);
				Group parent = grupe.get(0).getParent();
				// Dohvati sve registrirane nastavnike za pojedine grupe za predavanja
				List<GroupOwner> grOwners = dh.getGroupDAO().findForSubgroups(em, data.getCourseInstance().getId(), parent.getRelativePath());
				// Pronadi sve korisnike koji mogu biti ocjenjivaci
				Set<User> userSet = new HashSet<User>();
				Group g1 = dh.getGroupDAO().get(em, data.getCourseInstance().getId(), "3/1");
				Group g2 = dh.getGroupDAO().get(em, data.getCourseInstance().getId(), "3/4");
				if(g1!=null) {
					for(UserGroup ug : g1.getUsers()) {
						userSet.add(ug.getUser());
					}
				}
				if(g2!=null) {
					for(UserGroup ug : g2.getUsers()) {
						userSet.add(ug.getUser());
					}
				}
				if(!userSet.isEmpty()) {
					List<User> userList = new ArrayList<User>(userSet);
					Collections.sort(userList, StringUtil.USER_COMPARATOR);
					for(User user : userList) {
						data.getBean().getGraderUsers().add(new StringNameStringValue(user.getId().toString(), user.getLastName()+", "+user.getFirstName()));
					}
					for(Group g : grupe) {
						GroupGraderBean ggb = new GroupGraderBean();
						GroupOwner owner = null;
						for(GroupOwner go : grOwners) {
							// Ako sam pronasao korisnika zadane grupe, i taj ima pravo dati ocjenu:
							if(go.getGroup().getId().equals(g.getId()) && userSet.contains(go.getUser())) {
								owner = go;
								break;
							}
						}
						User user = owner!=null ? owner.getUser() : userList.get(0);
						if(g.getName().trim().isEmpty()) {
							ggb.setGroup("[bez imena]");
						} else {
							ggb.setGroup(g.getName());
						}
						ggb.setGroupID(g.getId().toString());
						ggb.setUserID(user.getId().toString());
						data.getBean().getGraders().add(ggb);
					}
				}
			}
		}
		
		if(data.getMessageLogger().hasErrorMessages() || data.getMessageLogger().hasWarningMessages()) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.gradeRulesIncomplete"));
		}
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void exportISVUXML(EntityManager em, GradingPolicyData data) {
		// Dohvati podatke
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessments(ci)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		if(!ci.getGradingPolicy().getGradesValid()) {
			data.getMessageLogger().addErrorMessage("Ocjene još nisu podijeljene!");
			data.setResult("redirect-show");
			return;
		}

		// Map<GroupID,Nastavnik_JMBAG>
		Map<Long,String> ocjenjivaciGrupa = new HashMap<Long, String>();
		for(GroupGraderBean ggb : data.getBean().getGraders()) {
			try {
				Long groupID = Long.valueOf(ggb.getGroupID());
				Long userID = Long.valueOf(ggb.getUserID());
				Group g = DAOHelperFactory.getDAOHelper().getGroupDAO().get(em, groupID);
				User u = DAOHelperFactory.getDAOHelper().getUserDAO().getUserById(em, userID);
				if(g==null || u==null || !g.getCompositeCourseID().equals(ci.getId())) {
					data.getMessageLogger().addErrorMessage("Pogreška u obradi ulaznih podataka!");
					data.setResult("redirect-show");
					return;
				}
				ocjenjivaciGrupa.put(g.getId(), u.getJmbag());
			} catch(Exception ex) {
				data.getMessageLogger().addErrorMessage("Pogreška u obradi ulaznih podataka!");
				data.setResult("redirect-show");
				return;
			}
		}
		
		List<User> courseUsers = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findCourseUsers(em, ci.getId());
		Set<User> courseUserSet = new HashSet<User>(courseUsers);
		List<Grade> allGrades = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().listGradesFor(em, ci);
		Map<Long,Grade> gradeMap = GradesUtil.mapGradeByUserID(allGrades);
		List<UserGroup> lectureUGs = DAOHelperFactory.getDAOHelper().getGroupDAO().findAllLectureUserGroups(em, data.getCourseInstance().getId());
		Map<Long,UserGroup> lectureGroups = GroupUtil.mapUserGroupByUserID(lectureUGs);
		for(User u : courseUserSet) {
			UserGroup ug = lectureGroups.get(u.getId());
			if(ug == null) {
				data.getMessageLogger().addErrorMessage("Pogreška u obradi ulaznih podataka!");
				data.setResult("redirect-show");
				return;
			}
			String ocj = ocjenjivaciGrupa.get(ug.getGroup().getId());
			if(ocj==null || ocj.isEmpty()) {
				data.getMessageLogger().addErrorMessage("Pogreška u obradi ulaznih podataka!");
				data.setResult("redirect-show");
				return;
			}
			Grade g = gradeMap.get(u.getId());
			if(g == null) {
				data.getMessageLogger().addErrorMessage("Pogreška u obradi ulaznih podataka!");
				data.setResult("redirect-show");
				return;
			}
		}
		
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("SCH", null);
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(tmpFile)), "UTF-8"));
				bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><!DOCTYPE ispitniRok SYSTEM \"http://www.isvu.hr/dtds/zavrsniIspit.dtd\"><ispitniRok><sifVU>36</sifVU>");
				bw.write("<sifPred>");
				bw.write(ci.getCourse().getIsvuCode());
				bw.write("</sifPred>");
				bw.write("<datumRok>");
				String datum = new SimpleDateFormat("dd.MM.yyyy").format(ci.getGradingPolicy().getTermDate());
				bw.write(datum);
				bw.write("</datumRok>");
				bw.write("<oznVrstaRok>B</oznVrstaRok>");
				bw.write("<ispitiNaRoku>");
				for(User u : courseUserSet) {
					UserGroup ug = lectureGroups.get(u.getId());
					String ocj = ocjenjivaciGrupa.get(ug.getGroup().getId());
					Grade g = gradeMap.get(u.getId());
					bw.write("<ispit><JMBAG>");
					bw.write(u.getJmbag());
					bw.write("</JMBAG><kratOcjenaUsmeni>");
					bw.write((char)(g.getGrade()+'0'));
					bw.write("</kratOcjenaUsmeni><datumIspit>");
					bw.write(datum);
					bw.write("</datumIspit><oznDjelUsmeni>");
					bw.write(ocj);
					bw.write("</oznDjelUsmeni></ispit>");
				}
				bw.write("</ispitiNaRoku>");
				bw.write("</ispitniRok>");
				bw.flush();
			} catch(Exception ex) {
				tmpFile.delete();
				data.getMessageLogger().addErrorMessage("Greška prilikom izrade datoteke.");
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			} finally {
				if(bw != null) try { bw.close(); } catch(Exception ignorable) {}
			}
			DeleteOnCloseFileInputStream docis = new DeleteOnCloseFileInputStream(tmpFile);
			docis.setFileName("OcjeneZaISVU.xml");
			docis.setMimeType("application/octet-stream");
			data.setStream(docis);
			data.setResult(AbstractActionData.RESULT_SUCCESS);
		} catch (IOException e) {
			e.printStackTrace();
			data.getMessageLogger().addErrorMessage("Greška prilikom izrade datoteke.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
	}
	
	public static void update(EntityManager em, GradingPolicyData data) {
		
		// Dohvati podatke
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if(!JCMSSecurityManagerFactory.getManager().canViewGradingPolicy(ci) || !JCMSSecurityManagerFactory.getManager().canEditGradingPolicy(ci)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setEditable(true);
		
		// Objekt treba biti zakljucan da bih mogao napraviti editiranje. Je li?
		LockPath lp = data.getLockPath();
		if(lp==null) {
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(lp.size()!=3 || !lp.getPart(0).equals("ml") || !lp.getPart(1).equals("ci"+ci.getId()) || !lp.getPart(2).equals("a")) {
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		// Postoji li grading policy objekt? Ako ne, imamo fatalnu pogrešku; on je već morao biti stvoren!
		GradingPolicy gp = ci.getGradingPolicy(); 
		if(gp==null) {
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		boolean newPolicy = false;
		
		if(data.getBean().getPolicyImplementation() != null && !data.getBean().getPolicyImplementation().equals(gp.getPolicyImplementation())) {
			// Politika je promjenjena. Flushaj sve podatke iz pravila
			data.getRules().clear();
			
			newPolicy = true;
		}
		
		boolean errors = !checkGPBean(data.getBean(), data.getMessageLogger());
		data.getBean().setGradesVisibilities(constructGradesVisibilitiesList(data.getMessageLogger()));
		data.getBean().setPolicyImplementations(constructPolicyImplementations(data.getMessageLogger()));
		
		if(!StringUtil.isStringBlank(data.getBean().getPolicyImplementation())) {
			if(data.getBean().getPolicyImplementation().equals("SC")) {
				// Bodovi za 3, 4 i 5 + provjera za cupanje bodova
				if(newPolicy) deserializeRulesSC(null, data.getRules());
				fillAssessmentOffering(ci, data.getRules(), "aOffering");
				if(!errors) gp.setRules(serializeRulesSC(ci, data.getRules(), data.getMessageLogger(), true, false));
			} else if(data.getBean().getPolicyImplementation().equals("SP")) {
				// Postotak studenata koji dobiva 2, 3, 4 i 5 + provjera za cupanje prolaza
				if(newPolicy) deserializeRulesSP(null, data.getRules());
				fillAssessmentOffering(ci, data.getRules(), "aOffering");
				if(!errors) gp.setRules(serializeRulesSP(ci, data.getRules(), data.getMessageLogger(), true, false));
			} else {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unknownPolicyImplementation"));
			}
		} else {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.noPolicyImplementation"));
		}

		if(errors || data.getMessageLogger().hasErrorMessages()) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.gradeRulesIncomplete"));
			data.setResult(AbstractActionData.RESULT_INPUT);
		} else {
			if(data.getMessageLogger().hasWarningMessages()) {
				data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.gradeRulesIncomplete"));
			}
			convertBeanToGP(gp, data.getBean(), data.getMessageLogger());
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
		}
	}

	public static void runGrading(EntityManager em, GradingPolicyData data) {
		
		// Dohvati podatke
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if(!JCMSSecurityManagerFactory.getManager().canViewGradingPolicy(ci) || !JCMSSecurityManagerFactory.getManager().canEditGradingPolicy(ci)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setEditable(true);
		
		// Objekt treba biti zakljucan da bih mogao napraviti editiranje. Je li?
		LockPath lp = data.getLockPath();
		if(lp==null) {
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(lp.size()!=3 || !lp.getPart(0).equals("ml") || !lp.getPart(1).equals("ci"+ci.getId()) || !lp.getPart(2).equals("a")) {
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		// Postoji li grading policy objekt? Ako ne, imamo fatalnu pogrešku; on je već morao biti stvoren!
		GradingPolicy gp = ci.getGradingPolicy(); 
		if(gp==null) {
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		if(gp.getGradesLocked()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.courseNotGraded1"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if(!StringUtil.isStringBlank(gp.getPolicyImplementation())) {
			if(gp.getPolicyImplementation().equals("SC")) {
				// Bodovi za 3, 4 i 5 + provjera za cupanje bodova
				runGradingSC(em, ci, data);
			} else if(gp.getPolicyImplementation().equals("SP")) {
				// Postotak studenata koji dobiva 2, 3, 4 i 5 + provjera za cupanje prolaza
				runGradingSP(em, ci, data);
			} else {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unknownPolicyImplementation"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
			}
		} else {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.noPolicyImplementation"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
		}
	}

	public static void showGrades(EntityManager em, GradingPolicyData data) {
		
		// Dohvati podatke
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if(!JCMSSecurityManagerFactory.getManager().canViewGradingPolicy(ci)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		if(ci.getGradingPolicy()==null || !ci.getGradingPolicy().getGradesValid() || ci.getGradingPolicy().getGradesVisibility()==GradesVisibility.NOT_VISIBLE) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.gradesUnavailable"));
			data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
			return;
		}
		
		List<Grade> allGrades = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().listGradesFor(em, ci);
		Collections.sort(allGrades, new Comparator<Grade>() {
			@Override
			public int compare(Grade g1, Grade g2) {
				int r = (int)g1.getGrade() - (int)g2.getGrade();
				if(r!=0) return -r; // Veće ocjene prije
				return StringUtil.USER_COMPARATOR.compare(g1.getUser(), g2.getUser());
			}
		});
		data.setGrades(allGrades);
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	
	private static void convertBeanToGP(GradingPolicy gp, GradingPolicyBean bean, IMessageLogger logger) {
		gp.setGradesLocked(bean.getGradesLocked());
		gp.setGradesVisibility(GradesVisibility.valueOf(bean.getGradesVisibility()));
		gp.setPolicyImplementation(bean.getPolicyImplementation());
		if(bean.getPolicyImplementation()==null) {
			gp.setRules(null);
		}
		gp.setTermDate(StringUtil.isStringBlank(bean.getTermDate())? null : DateUtil.stringToDate(bean.getTermDate()));
	}

	private static boolean checkGPBean(GradingPolicyBean bean, IMessageLogger logger) {
		boolean errors = false;
		if(!StringUtil.isStringBlank(bean.getPolicyImplementation())) {
			if(!isValueIn(bean.getPolicyImplementation(), new String[] {"SC", "SP"})) {
				logger.addErrorMessage(logger.getText("Error.unknownPolicyImplementation"));
				errors = true;
			}
		} else {
			bean.setPolicyImplementation(null);
		}
		if(!isValueIn(bean.getGradesVisibility(), new String[] {GradesVisibility.NOT_VISIBLE.name(), GradesVisibility.STAFF_ONLY.name(), GradesVisibility.VISIBLE.name()})) {
			logger.addErrorMessage(logger.getText("Error.invalidGradesVisibility"));
			errors = true;
		}
		if(!StringUtil.isStringBlank(bean.getTermDate())) {
			if(!DateUtil.checkDateFormat(bean.getTermDate())) {
				logger.addErrorMessage(logger.getText("Error.dateSyntaxError"));
				errors = true;
			}
		} else {
			bean.setTermDate(null);
		}
		return !errors;
	}

	private static boolean isValueIn(String value, String[] values) {
		for(String s : values) {
			if(s.equals(value)) return true;
		}
		return false;
	}
	
	private static void convertGPToBean(GradingPolicy gp, GradingPolicyBean bean, IMessageLogger logger) {
		bean.setGradesLocked(gp.getGradesLocked());
		bean.setGradesValid(gp.getGradesValid());
		bean.setGradesVisibility(gp.getGradesVisibility().name());
		bean.setGradesVisibilities(constructGradesVisibilitiesList(logger));
		bean.setId(gp.getId());
		bean.setPolicyImplementation(gp.getPolicyImplementation());
		bean.setPolicyImplementations(constructPolicyImplementations(logger));
		bean.setTermDate(gp.getTermDate()!=null ? DateUtil.dateToString(gp.getTermDate()) : "");
	}

	private static List<StringNameStringValue> constructPolicyImplementations(IMessageLogger logger) {
		List<StringNameStringValue> list = new ArrayList<StringNameStringValue>();
		list.add(new StringNameStringValue("SC", logger.getText("forms.grades.policyImpl.SC")));
		list.add(new StringNameStringValue("SP", logger.getText("forms.grades.policyImpl.SP")));
		return list;
	}

	private static List<StringNameStringValue> constructGradesVisibilitiesList(IMessageLogger logger) {
		List<StringNameStringValue> list = new ArrayList<StringNameStringValue>();
		list.add(new StringNameStringValue(GradesVisibility.NOT_VISIBLE.name(), logger.getText("forms.grades.visibility."+GradesVisibility.NOT_VISIBLE)));
		list.add(new StringNameStringValue(GradesVisibility.STAFF_ONLY.name(), logger.getText("forms.grades.visibility."+GradesVisibility.STAFF_ONLY)));
		list.add(new StringNameStringValue(GradesVisibility.VISIBLE.name(), logger.getText("forms.grades.visibility."+GradesVisibility.VISIBLE)));
		return list;
	}

	private static String serializeRulesSC(CourseInstance ci, Map<String, Object> rulesMap, IMessageLogger logger, boolean useErrLog, boolean useWarnLog) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("1\n");
		String v = getString(rulesMap,"as");
		if(!StringUtil.isStringBlank(v)) {
			try {
				Long l = Long.valueOf(v);
				if(null == findAssessmentForID(ci.getAssessments(), l)) {
					if(useErrLog) logger.addErrorMessage(logger.getText("Error.problemWithAssessmentSelection"));
					l = null;
				}
				if(l!=null) sb.append(l.toString());
			} catch(Exception ex) {
				if(useErrLog) logger.addErrorMessage(logger.getText("Error.problemWithAssessmentSelection"));
			}
		} else {
			if(useWarnLog) logger.addWarningMessage(logger.getText("Warning.assessmentNotSelected"));
		}
		double lastTreshold = 0;
		for(int i = 3; i<= 5; i++) {
			sb.append('\n');
			v = getString(rulesMap,"s"+i);
			if(!StringUtil.isStringBlank(v)) {
				try {
					Double l = StringUtil.stringToDouble(v);
					if(l.doubleValue()<lastTreshold) {
						if(useErrLog) logger.addErrorMessage(logger.getText("Error.gradeTresholdInvalidValue")+i+".");
						l = null;
					} else {
						lastTreshold = l.doubleValue();
					}
					if(l!=null) sb.append(l.toString());
				} catch(Exception ex) {
					if(useErrLog) logger.addErrorMessage(logger.getText("Error.gradeTresholdInvalidValue")+i+".");
				}
			} else {
				if(useWarnLog) logger.addWarningMessage(logger.getText("Warning.gradeTresholdNotSelected")+i+".");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Pomoćna metoda koja iz mape dohvaća string koji pohranjen ili kao string, ili kao polje stringova.
	 * Naime, kada se struts-u kaže da u mapu čije su vrijednosti objekti pohrani string iz parametara,
	 * on unutra umjesto stringa gurne polje stringova (koje je, btw. veličine 1). Ukoliko je u mapi string
	 * ili polje stringova, dohvaća se taj string (ili prvi string polja). U svim ostalim slučajevima
	 * vraća se <code>null</code> (neće se izazvati nikakva iznimka).
	 *  
	 * @param rulesMap mapa
	 * @param key ključ koji se traži
	 * @return vrijednost ili null
	 */
	private static String getString(Map<String, Object> rulesMap, String key) {
		Object o = rulesMap.get(key);
		if(o==null) return null;
		if(o instanceof String) {
			return (String)o;
		}
		if(o instanceof String[]) {
			String[] arr = (String[])o;
			if(arr.length<1) return null;
			return arr[0];
		}
		return null;
	}

	private static Object findAssessmentForID(Set<Assessment> assessments, Long l) {
		for(Assessment a : assessments) {
			if(a.getId().equals(l)) return a;
		}
		return null;
	}

	private static void deserializeRulesSC(String rules, Map<String, Object> rulesMap) {
		if(StringUtil.isStringBlank(rules)) {
			defaultRulesSC(rulesMap);
			return;
		}
		String[] elems = TextService.split(rules, '\n');
		// Ako je verzija 1
		if(elems[0].equals("1")) {
			String provjera = elems[1];
			String sBodovi3 = elems[2];
			String sBodovi4 = elems[3];
			String sBodovi5 = elems[4];
			rulesMap.put("s3", sBodovi3);
			rulesMap.put("s4", sBodovi4);
			rulesMap.put("s5", sBodovi5);
			rulesMap.put("as", provjera.isEmpty() ? null : provjera);
		} else {
			defaultRulesSC(rulesMap);
			return;
		}
	}

	private static void defaultRulesSC(Map<String, Object> rulesMap) {
		rulesMap.put("s3", "62.5");
		rulesMap.put("s4", "75");
		rulesMap.put("s5", "87.5");
		rulesMap.put("as", null); // Identifikator provjere, zapisan kao string
	}

	private static String serializeRulesSP(CourseInstance ci, Map<String, Object> rulesMap, IMessageLogger logger, boolean useErrLog, boolean useWarnLog) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("1\n");
		String v = getString(rulesMap,"as");
		if(!StringUtil.isStringBlank(v)) {
			try {
				Long l = Long.valueOf(v);
				if(null == findAssessmentForID(ci.getAssessments(), l)) {
					if(useErrLog) logger.addErrorMessage(logger.getText("Error.problemWithAssessmentSelection"));
					l = null;
				}
				if(l!=null) sb.append(l.toString());
			} catch(Exception ex) {
				if(useErrLog) logger.addErrorMessage(logger.getText("Error.problemWithAssessmentSelection"));
			}
		} else {
			if(useWarnLog) logger.addWarningMessage(logger.getText("Warning.assessmentNotSelected"));
		}
		double sum = 0;
		for(int i = 2; i<= 5; i++) {
			sb.append('\n');
			v = getString(rulesMap,"p"+i);
			if(!StringUtil.isStringBlank(v)) {
				try {
					Double l = StringUtil.stringToDouble(v);
					if(l.doubleValue()>100 || l.doubleValue()<0) {
						if(useErrLog) logger.addErrorMessage(logger.getText("Error.gradePercentageInvalidValue")+i+".");
						l = null;
					} else {
						sum += l.doubleValue();
					}
					if(l!=null) sb.append(l.toString());
				} catch(Exception ex) {
					if(useErrLog) logger.addErrorMessage(logger.getText("Error.gradePercentageInvalidValue")+i+".");
				}
			} else {
				if(useWarnLog) logger.addWarningMessage(logger.getText("Warning.gradePercentageNotSelected")+i+".");
			}
		}
		if(Math.abs(sum-100.0)>1E-3) {
			// Ako je nešto popunjavao, onda javi grešku:
			if(Math.abs(sum)>1E-3) {
				if(useErrLog) logger.addErrorMessage(logger.getText("Error.gradePercentagesSum"));
			} else { // Inače javi upozorenje
				if(useWarnLog) logger.addWarningMessage(logger.getText("Warning.gradePercentagesSum"));
			}
		}
		return sb.toString();
	}
	
	private static void deserializeRulesSP(String rules, Map<String, Object> rulesMap) {
		if(StringUtil.isStringBlank(rules)) {
			defaultRulesSP(rulesMap);
			return;
		}
		String[] elems = TextService.split(rules, '\n');
		// Ako je verzija 1
		if(elems[0].equals("1")) {
			String provjera = elems[1];
			String sPostotak2 = elems[2];
			String sPostotak3 = elems[3];
			String sPostotak4 = elems[4];
			String sPostotak5 = elems[5];
			rulesMap.put("p2", sPostotak2);
			rulesMap.put("p3", sPostotak3);
			rulesMap.put("p4", sPostotak4);
			rulesMap.put("p5", sPostotak5);
			rulesMap.put("as", provjera.isEmpty() ? null : provjera);
		} else {
			defaultRulesSP(rulesMap);
			return;
		}
	}

	private static void defaultRulesSP(Map<String, Object> rulesMap) {
		rulesMap.put("p2", "15");
		rulesMap.put("p3", "35");
		rulesMap.put("p4", "35");
		rulesMap.put("p5", "15");
		rulesMap.put("as", null); // Identifikator provjere, zapisan kao string
	}

	private static void runGradingSC(EntityManager em, CourseInstance ci, GradingPolicyData data) {
		if(StringUtil.isStringBlank(ci.getGradingPolicy().getRules())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unableToRunGrading"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Long assessmentID = null;
		double score3 = 0;
		double score4 = 0;
		double score5 = 0;
		try {
			String[] elems = TextService.split(ci.getGradingPolicy().getRules(), '\n');
			// Ako je verzija 1
			if(elems[0].equals("1")) {
				assessmentID = Long.valueOf(elems[1]);
				score3 = StringUtil.stringToDouble(elems[2]).doubleValue();
				score4 = StringUtil.stringToDouble(elems[3]).doubleValue();
				score5 = StringUtil.stringToDouble(elems[4]).doubleValue();
			} else {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unableToRunGrading"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unableToRunGrading"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Assessment as = AssessmentUtil.getAssessmentWithID(ci.getAssessments(), assessmentID);
		if(as == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.gradingAssessmentNotExists"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		List<User> courseUsers = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findCourseUsers(em, ci.getId());
		Set<User> courseUserSet = new HashSet<User>(courseUsers);
		Map<Long,AssessmentScore> scoreMap = AssessmentUtil.mapAssessmentScoreByUserID(as.getScore());
		List<Grade> allGrades = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().listGradesFor(em, ci);
		Map<Long,Grade> gradeMap = GradesUtil.mapGradeByUserID(allGrades);
		List<AssessmentScore> scoreList = new ArrayList<AssessmentScore>(courseUsers.size());

		List<Grade> gradesToPersist = new ArrayList<Grade>(courseUsers.size());
		
		// Provjerimo najprije da za svakog studenta postoji assessment score
		int brojPrijava = 0;
		for(User u : courseUsers) {
			AssessmentScore asc = scoreMap.get(u.getId());
			if(asc==null || asc.isError()) {
				brojPrijava++;
				if(brojPrijava<=5) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.studentHasNoScore")+" "+u.getLastName()+", "+u.getFirstName()+" ("+u.getJmbag()+")");
				}
			} else {
				scoreList.add(asc);
			}
			Grade g = gradeMap.get(u.getId());
			if(g==null) {
				g = new Grade();
				g.setCourseInstance(ci);
				g.setUser(u);
				gradeMap.put(u.getId(), g);
				gradesToPersist.add(g);
			}
		}
		if(brojPrijava>5) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.studentsHaveNoScore")+" "+(brojPrijava-5));
		}
		if(brojPrijava>0) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.runAssessmentScoring"));
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.gradingAborted"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		// Imam li višak ocjena?
		List<Grade> gradesToRemove = new ArrayList<Grade>();
		for(Grade g : allGrades) {
			if(!courseUserSet.contains(g.getUser())) {
				gradesToRemove.add(g);
			}
		}
		
		// Sortiram prvo sve koji prošli, a onda one koji su pali; u tim kategorijama opet prvo one s više bodova pa onda s manje.
		// Razlikujem bodove do na šestu decimalu.
		Collections.sort(scoreList, new Comparator<AssessmentScore>() {
			@Override
			public int compare(AssessmentScore o1, AssessmentScore o2) {
				if(o1.getEffectiveStatus()==AssessmentStatus.PASSED) {
					if(o2.getEffectiveStatus()==AssessmentStatus.FAILED) {
						return -1;
					}
				} else {
					if(o2.getEffectiveStatus()==AssessmentStatus.PASSED) {
						return 1;
					}
				}
				double razlika = o1.getEffectiveScore() - o2.getEffectiveScore();
				if(razlika>1E-6) return -1;
				if(razlika<-1E-6) return  1;
				return 0;
			}
		});
		
		int brojProslih = 0;
		for(AssessmentScore s : scoreList) {
			if(s.getEffectiveStatus()==AssessmentStatus.PASSED) {
				brojProslih++;
			} else {
				break;
			}
		}
		
		int brojPalih = courseUsers.size() - brojProslih;
		// Na prvom mjestu je broj jedinica; na zadnjem broj petica
		int[] gradeCounters = new int[] {0,0,0,0,0};
		// Na prvom mjestu je prag za 2; na zadnjem za 5
		double[] gradeTresholds = new double[] {0,0,0,0};
		Date now = new Date();

		if(brojProslih==0) {
			for(int i = 0; i < brojPalih; i++) {
				AssessmentScore s = scoreList.get(i);
				Grade g = gradeMap.get(s.getUser().getId());
				ActivityEventKind kind = setGrade(g, (byte)1);
				g.setGivenBy(data.getCurrentUser());
				g.setGivenAt(now);
				gradeCounters[0]++;
				if(ci.getGradingPolicy().getGradesVisibility()==GradesVisibility.VISIBLE && kind!=ActivityEventKind.SILENT) {
					JCMSSettings.getSettings().getActivityReporter().addActivity(
						new GradeActivity(now, ci.getId(), g.getUser().getId(), g.getGrade(), s.getEffectiveRank(), kind)
					);
				}
			}
			// I tu smo gotovi
		} else {
			for(AssessmentScore s : scoreList) {
				Grade g = gradeMap.get(s.getUser().getId());
				// Uvećaj score na 6. decimali da izbjegnemo probleme oko zaokruzivanja
				double efsc = s.getEffectiveScore() + 1E-6;
				byte newGrade; 
				if(s.getEffectiveStatus()==AssessmentStatus.FAILED) {
					newGrade = (byte)1;
				} else {
					if(efsc>=score5) {
						newGrade = (byte)5;
					} else if(efsc>=score4) {
						newGrade = (byte)4;
					} else if(efsc>=score3) {
						newGrade = (byte)3;
					} else {
						newGrade = (byte)2;
					}
					// Ovo ce ispravno postaviti pragove jer idem po popisu studenta koji je sortiran prema
					// padajucem broju bodova!
					gradeTresholds[newGrade-2] = s.getEffectiveScore();
				}
				gradeCounters[newGrade-1]++;
				ActivityEventKind kind = setGrade(g, newGrade);
				g.setGivenBy(data.getCurrentUser());
				g.setGivenAt(now);
				if(ci.getGradingPolicy().getGradesVisibility()==GradesVisibility.VISIBLE && kind!=ActivityEventKind.SILENT) {
					JCMSSettings.getSettings().getActivityReporter().addActivity(
							new GradeActivity(now, ci.getId(), g.getUser().getId(), g.getGrade(), s.getEffectiveRank(), kind)
					);
				}
			}
			// I tu smo gotovi
		}
		
		// Još malo kućanskih poslova...
		CourseInstanceDAO cid = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO(); 
		for(Grade g : gradesToRemove) {
			cid.remove(em, g);
		}
		for(Grade g : gradesToPersist) {
			cid.save(em, g);
		}

		// I podesi rudimentarnu statistiku...
		GradingStat gs = ci.getGradingPolicy().getGradingStat();
		if(gs==null) {
			gs = new GradingStat();
			ci.getGradingPolicy().setGradingStat(gs);
		}
		gs.setFailed(brojPalih);
		gs.setPassed(brojProslih);
		gs.setGradeCounts(gradeCounters);
		gs.setGradeTresholds(gradeTresholds);
		ci.getGradingPolicy().serializeGS();

		ci.getGradingPolicy().setGradesValid(true);
		if(ci.getGradingPolicy().getGradesVisibility()==GradesVisibility.VISIBLE) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.courseGraded1"));
		} else {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.courseGraded2"));
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	private static void runGradingSP(EntityManager em, CourseInstance ci, GradingPolicyData data) {
		if(StringUtil.isStringBlank(ci.getGradingPolicy().getRules())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unableToRunGrading"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Long assessmentID = null;
		double percentage2 = 0;
		double percentage3 = 0;
		double percentage4 = 0;
		double percentage5 = 0;
		try {
			String[] elems = TextService.split(ci.getGradingPolicy().getRules(), '\n');
			// Ako je verzija 1
			if(elems[0].equals("1")) {
				assessmentID = Long.valueOf(elems[1]);
				percentage2 = StringUtil.stringToDouble(elems[2]).doubleValue() / 100.0;
				percentage3 = StringUtil.stringToDouble(elems[3]).doubleValue() / 100.0;
				percentage4 = StringUtil.stringToDouble(elems[4]).doubleValue() / 100.0;
				percentage5 = StringUtil.stringToDouble(elems[5]).doubleValue() / 100.0;
			} else {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unableToRunGrading"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unableToRunGrading"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Assessment as = AssessmentUtil.getAssessmentWithID(ci.getAssessments(), assessmentID);
		if(as == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.gradingAssessmentNotExists"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		List<User> courseUsers = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findCourseUsers(em, ci.getId());
		Set<User> courseUserSet = new HashSet<User>(courseUsers);
		Map<Long,AssessmentScore> scoreMap = AssessmentUtil.mapAssessmentScoreByUserID(as.getScore());
		List<Grade> allGrades = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().listGradesFor(em, ci);
		Map<Long,Grade> gradeMap = GradesUtil.mapGradeByUserID(allGrades);
		List<AssessmentScore> scoreList = new ArrayList<AssessmentScore>(courseUsers.size());

		List<Grade> gradesToPersist = new ArrayList<Grade>(courseUsers.size());
		
		// Provjerimo najprije da za svakog studenta postoji assessment score
		int brojPrijava = 0;
		for(User u : courseUsers) {
			AssessmentScore asc = scoreMap.get(u.getId());
			if(asc==null || asc.isError()) {
				brojPrijava++;
				if(brojPrijava<=5) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.studentHasNoScore")+" "+u.getLastName()+", "+u.getFirstName()+" ("+u.getJmbag()+")");
				}
			} else {
				scoreList.add(asc);
			}
			Grade g = gradeMap.get(u.getId());
			if(g==null) {
				g = new Grade();
				g.setCourseInstance(ci);
				g.setUser(u);
				gradeMap.put(u.getId(), g);
				gradesToPersist.add(g);
			}
		}
		if(brojPrijava>5) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.studentsHaveNoScore")+" "+(brojPrijava-5));
		}
		if(brojPrijava>0) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.runAssessmentScoring"));
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.gradingAborted"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		// Imam li višak ocjena?
		List<Grade> gradesToRemove = new ArrayList<Grade>();
		for(Grade g : allGrades) {
			if(!courseUserSet.contains(g.getUser())) {
				gradesToRemove.add(g);
			}
		}
		
		// Sortiram prvo sve koji prošli, a onda one koji su pali; u tim kategorijama opet prvo one s više bodova pa onda s manje.
		// Razlikujem bodove do na šestu decimalu.
		Collections.sort(scoreList, new Comparator<AssessmentScore>() {
			@Override
			public int compare(AssessmentScore o1, AssessmentScore o2) {
				if(o1.getEffectiveStatus()==AssessmentStatus.PASSED) {
					if(o2.getEffectiveStatus()==AssessmentStatus.FAILED) {
						return -1;
					}
				} else {
					if(o2.getEffectiveStatus()==AssessmentStatus.PASSED) {
						return 1;
					}
				}
				double razlika = o1.getEffectiveScore() - o2.getEffectiveScore();
				if(razlika>1E-6) return -1;
				if(razlika<-1E-6) return  1;
				return 0;
			}
		});
		
		int brojProslih = 0;
		for(AssessmentScore s : scoreList) {
			if(s.getEffectiveStatus()==AssessmentStatus.PASSED) {
				brojProslih++;
			} else {
				break;
			}
		}
		
		int brojPalih = courseUsers.size() - brojProslih;
		// Na prvom mjestu je broj jedinica; na zadnjem broj petica
		int[] gradeCounters = new int[] {0,0,0,0,0};
		// Na prvom mjestu je prag za 2; na zadnjem za 5
		double[] gradeTresholds = new double[] {0,0,0,0};
		Date now = new Date();

		if(brojProslih==0) {
			for(int i = 0; i < brojPalih; i++) {
				AssessmentScore s = scoreList.get(i);
				Grade g = gradeMap.get(s.getUser().getId());
				ActivityEventKind kind = setGrade(g, (byte)1);
				g.setGivenBy(data.getCurrentUser());
				g.setGivenAt(now);
				gradeCounters[0]++;
				if(ci.getGradingPolicy().getGradesVisibility()==GradesVisibility.VISIBLE && kind!=ActivityEventKind.SILENT) {
					JCMSSettings.getSettings().getActivityReporter().addActivity(
						new GradeActivity(now, ci.getId(), g.getUser().getId(), g.getGrade(), s.getEffectiveRank(), kind)
					);
				}
			}
			// I tu smo gotovi
		} else {
			double dIndex5 = brojProslih * percentage5;
			double dIndex4 = brojProslih * (percentage5+percentage4);
			double dIndex3 = brojProslih * (percentage5+percentage4+percentage3);
			//double dIndex2 = brojProslih;
			byte[] grades = new byte[brojProslih];
			Arrays.fill(grades, (byte)0);
			
			// Ocjena 5
			// --------------------------------------------
			int curr = 0;
			int kraj = (int)(dIndex5+0.5);
			if(kraj-curr==0 && percentage5>1E-3) kraj++;
			if(kraj>grades.length) kraj = grades.length;
			for(int i = curr; i < kraj; i++) {
				if(grades[i]==(byte)0) {
					grades[i] = (byte)5;
					gradeCounters[4]++;
				}
			}
			// Sada vidi je li ovo stalo negdje gdje se bodovi ne mijenjaju, pa ako je, uzmi u obzir jos i sve one
			// ispod koji imaju isti broj bodova
			double granica5 = scoreList.get(kraj-1).getEffectiveScore();
			curr = kraj;
			while(curr<grades.length && Math.abs(granica5-scoreList.get(curr).getEffectiveScore())<1E-3) {
				if(grades[curr]==(byte)0) {
					grades[curr] = (byte)5;
					gradeCounters[4]++;
				}
				curr++;
			}

			// Ocjena 2 - idemo popunjavati odozdo...
			// --------------------------------------------
			curr = (int)(dIndex3+0.5); // Ovo je gornja granica; OK je da piše dIndex3
			if(curr==grades.length && percentage2>1E-3) curr--;
			if(curr>grades.length) curr = grades.length;
			kraj = grades.length;
			for(int i = curr; i < kraj; i++) {
				if(grades[i]==(byte)0) {
					grades[i] = (byte)2;
					gradeCounters[1]++;
				}
			}
			// Sada vidi je li ovo pocelo negdje gdje se bodovi ne mijenjaju, pa ako je, uzmi u obzir jos i sve one
			// iznad koji imaju isti broj bodova
			double granica2 = scoreList.get(curr).getEffectiveScore();
			while(curr>=0 && Math.abs(granica2-scoreList.get(curr).getEffectiveScore())<1E-3) {
				if(grades[curr]==(byte)0) {
					grades[curr] = (byte)2;
					gradeCounters[1]++;
				}
				curr--;
			}

			// Ocjena 4
			// --------------------------------------------
			curr = (int)(dIndex5+0.5);
			kraj = (int)(dIndex4+0.5);
			if(kraj-curr==0 && percentage4>1E-3) kraj++;
			if(kraj>grades.length) kraj = grades.length;
			for(int i = curr; i < kraj; i++) {
				if(grades[i]==(byte)0) {
					grades[i] = (byte)4;
					gradeCounters[3]++;
				}
			}
			// Sada vidi je li ovo stalo negdje gdje se bodovi ne mijenjaju, pa ako je, uzmi u obzir jos i sve one
			// ispod koji imaju isti broj bodova
			double granica4 = scoreList.get(kraj-1).getEffectiveScore();
			curr = kraj;
			while(curr<grades.length && Math.abs(granica4-scoreList.get(curr).getEffectiveScore())<1E-3) {
				if(grades[curr]==(byte)0) {
					grades[curr] = (byte)4;
					gradeCounters[3]++;
				}
				curr++;
			}

			// Ocjena 3
			// --------------------------------------------
			curr = (int)(dIndex4+0.5);
			kraj = (int)(dIndex3+0.5);
			if(kraj-curr==0 && percentage3>1E-3) kraj++;
			if(kraj>grades.length) kraj = grades.length;
			for(int i = curr; i < kraj; i++) {
				if(grades[i]==(byte)0) {
					grades[i] = (byte)3;
					gradeCounters[2]++;
				}
			}
			// Sada vidi je li ovo pocelo negdje gdje se bodovi ne mijenjaju, pa ako je, uzmi u obzir jos i sve one
			// iznad koji imaju isti broj bodova
			double granica3 = scoreList.get(curr).getEffectiveScore();
			while(curr>=0 && Math.abs(granica3-scoreList.get(curr).getEffectiveScore())<1E-3) {
				if(grades[curr]==(byte)0) {
					grades[curr] = (byte)3;
					gradeCounters[2]++;
				}
				curr--;
			}
			
			for(int i = 0; i < brojProslih; i++) {
				AssessmentScore s = scoreList.get(i);
				Grade g = gradeMap.get(s.getUser().getId());
				ActivityEventKind kind = setGrade(g, grades[i]);
				g.setGivenBy(data.getCurrentUser());
				g.setGivenAt(now);
				// Ovo ce ispravno postaviti pragove jer idem po popisu studenta koji je sortiran prema
				// padajucem broju bodova!
				gradeTresholds[grades[i]-2] = s.getEffectiveScore();
				if(ci.getGradingPolicy().getGradesVisibility()==GradesVisibility.VISIBLE && kind!=ActivityEventKind.SILENT) {
					JCMSSettings.getSettings().getActivityReporter().addActivity(
						new GradeActivity(now, ci.getId(), g.getUser().getId(), g.getGrade(), s.getEffectiveRank(), kind)
					);
				}
			}
			for(int i = brojProslih; i < brojProslih+brojPalih; i++) {
				AssessmentScore s = scoreList.get(i);
				Grade g = gradeMap.get(s.getUser().getId());
				ActivityEventKind kind = setGrade(g, (byte)1);
				g.setGivenBy(data.getCurrentUser());
				g.setGivenAt(now);
				gradeCounters[0]++;
				if(ci.getGradingPolicy().getGradesVisibility()==GradesVisibility.VISIBLE && kind!=ActivityEventKind.SILENT) {
					JCMSSettings.getSettings().getActivityReporter().addActivity(
						new GradeActivity(now, ci.getId(), g.getUser().getId(), g.getGrade(), s.getEffectiveRank(), kind)
					);
				}
			}
			// I tu smo gotovi
		}
		
		// Još malo kućanskih poslova...
		CourseInstanceDAO cid = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO(); 
		for(Grade g : gradesToRemove) {
			cid.remove(em, g);
		}
		for(Grade g : gradesToPersist) {
			cid.save(em, g);
		}

		// I podesi rudimentarnu statistiku...
		GradingStat gs = ci.getGradingPolicy().getGradingStat();
		if(gs==null) {
			gs = new GradingStat();
			ci.getGradingPolicy().setGradingStat(gs);
		}
		gs.setFailed(brojPalih);
		gs.setPassed(brojProslih);
		gs.setGradeCounts(gradeCounters);
		gs.setGradeTresholds(gradeTresholds);
		ci.getGradingPolicy().serializeGS();

		ci.getGradingPolicy().setGradesValid(true);
		if(ci.getGradingPolicy().getGradesVisibility()==GradesVisibility.VISIBLE) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.courseGraded1"));
		} else {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.courseGraded2"));
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	private static ActivityEventKind setGrade(Grade g, byte newGrade) {
		ActivityEventKind kind = ActivityEventKind.SILENT;
		byte currentGrade = g.getGrade();
		// Nista se nije promijenilo!
		if(currentGrade==newGrade) return kind;
		if(currentGrade==(byte)0) {
			kind = ActivityEventKind.CREATED;
		} else {
			if(newGrade==(byte)0) {
				kind = ActivityEventKind.DELETED;
			} else{
				kind = ActivityEventKind.MODIFIED;
			}
		}
		g.setGrade(newGrade);
		return kind;
	}

	/**
	 * Metoda u mapu pod zadanim imenom dodaje listu {@link StringNameStringValue} objekata koji 
	 * predstavljaju sve provjere na kolegiju. Pri tome je ključ identifikator provjere (kao string)
	 * a vrijednost naziv provjere.
	 * 
	 * @param ci kolegij
	 * @param rulesMap mapa
	 * @param name ključ pod kojim treba spremiti listu
	 */
	private static void fillAssessmentOffering(CourseInstance ci, Map<String, Object> rulesMap, String name) {
		List<Assessment> allAssessments = new ArrayList<Assessment>(ci.getAssessments());
		Collections.sort(allAssessments, new Comparator<Assessment>() {
			@Override
			public int compare(Assessment o1, Assessment o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		List<StringNameStringValue> list = new ArrayList<StringNameStringValue>(allAssessments.size());
		for(Assessment a : allAssessments) {
			list.add(new StringNameStringValue(a.getId().toString(), a.getName()+ " ("+a.getShortName()+")"));
		}
		rulesMap.put(name, list);
	}
}
