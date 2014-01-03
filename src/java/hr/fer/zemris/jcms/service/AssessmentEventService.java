package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.beans.ext.AssessmentScheduleBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseWideEvent;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.AssessmentUtil;
import hr.fer.zemris.jcms.service.util.CourseInstanceUtil;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsEditingService;
import hr.fer.zemris.jcms.web.actions.data.AssessmentsScheduleEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;

/**
 * Razred koji se brine o dodavanju ispitnih evenata
 * @author TOMISLAV
 *
 */

public class AssessmentEventService {
	
	/**
	 * Metoda koja prima listu AssessmentScheduleBean-ova i na osnovu njih kreira CourseWideEventove za odgovarajuce kolegije
	 * @param data 
	 * @param list lista beanova
	 * @param yearSemesterID id semestra za koji se dodaju ispiti
	 * @param tagShortName tag koji opisuje o kojem se ispitu radi
	 * @param issuerUsername username osobe koja dodaje raspored u sustav
	 * 
	 */
	public static void addAssessmentEvents(final AssessmentsScheduleEditData data, final List<AssessmentScheduleBean> list, 
			final String yearSemesterID, final String tagShortName, final Long userID, final boolean check) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if (check) {
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				
				// Najprije dohvati sve kolegije na semestru, i mapiraj ih po isvuSifri
				YearSemester yearSemester = dh.getYearSemesterDAO().get(em, yearSemesterID);
				if(yearSemester==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//provjera postoji li zadani assessmentTag u bazi
				if (tagShortName == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				AssessmentTag dbTag = dh.getAssessmentTagDAO().getByShortName(em, tagShortName);
				if (dbTag == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<CourseInstance> instances = dh.getCourseInstanceDAO().findForSemester(em, yearSemesterID);
				List<Assessment> assessmentsWithTag = dh.getAssessmentDAO().findTaggedOnSemester(em, yearSemester, dbTag);
				List<Object[]> allAssessmentNames = dh.getAssessmentDAO().findShortNamesOnSemester(em, yearSemester);

				Map<String, CourseInstance> courseByIsvuCodeMap = CourseInstanceUtil.mapCourseInstanceByISVUCode(instances);
				Map<String, Assessment> assessmentByIsvuCodeMap = AssessmentUtil.mapSingleAssessmentByISVUCode(assessmentsWithTag);
				Map<String, Set<String>> assessmentShortNamesByIsvuCodeMap = AssessmentUtil.mapAssessmentShortNamesByISVUCode(allAssessmentNames);

				//provjera postoji li assessment s courseInstancom kojeg mi nemamo
				for (AssessmentScheduleBean esb : list) {
					if (courseByIsvuCodeMap.get(esb.getCourseISVUCode())==null) {
						String[] param = new String[1];
						param[0] = esb.getCourseISVUCode();
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchCourse",param));
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
				}
				
				//prolazimo kroz sve beanove i kreiramo potrebne evente/assessmente
				for (AssessmentScheduleBean esb : list) {
					
					CourseInstance ci = courseByIsvuCodeMap.get(esb.getCourseISVUCode());
					
					//stvori CourseWideEvent
					CourseWideEvent cwe = new CourseWideEvent();
					cwe.setCourseInstance(ci);
					cwe.setDuration(esb.getDuration());
					cwe.setIssuer(data.getCurrentUser());
					cwe.setRoom(null);
					cwe.setSpecifier(yearSemesterID+"/ispiti/"+dbTag.getShortName());
					cwe.setStart(esb.getStart());
					cwe.setStrength(EventStrength.STRONG);
					cwe.setTitle(ci.getCourse().getName() + " - " + dbTag.getName());
					
					//provjera postoji li vec assessment s ovim tagom
					Assessment assessment = assessmentByIsvuCodeMap.get(esb.getCourseISVUCode());
					if (assessment == null) {
						assessment = createNewDefAssessment(em, dh, dbTag, ci, cwe, assessmentShortNamesByIsvuCodeMap);
					} else {
						//gledamo da li postoji CourseWideEvent za taj Assessment
						CourseWideEvent dbCwe = assessment.getEvent();
						//ako postoji radimo update CourseWideEventa iz baze 
						if (dbCwe != null) {
							updateDbCwe(dbCwe,cwe);
							if(dbCwe.isHidden()) {
								dbCwe.setHidden(false);
								AssessmentsEditingService.propagateEventVisibilityChange(em, assessment);
							}
						}
						//inace stvaramo novi
						else
							assessment.setEvent(cwe);
					}
					
					assessment.getEvent().setContext("a:"+assessment.getId());
				}
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	private static void updateDbCwe(CourseWideEvent dbCwe, CourseWideEvent cwe) {
		dbCwe.setCourseInstance(cwe.getCourseInstance());
		dbCwe.setDuration(cwe.getDuration());
		dbCwe.setIssuer(cwe.getIssuer());
		dbCwe.setRoom(cwe.getRoom());
		dbCwe.setSpecifier(cwe.getSpecifier());
		dbCwe.setStart(cwe.getStart());
		dbCwe.setStrength(cwe.getStrength());
		dbCwe.setTitle(cwe.getTitle());
	}

	private static Assessment createNewDefAssessment(EntityManager em,
			DAOHelper dh, AssessmentTag tag, CourseInstance ci, CourseWideEvent cwe, Map<String, Set<String>> assessmentShortNamesByIsvuCodeMap) {

		String shortName = createUniqueAssessmentShortName(tag.getShortName(), assessmentShortNamesByIsvuCodeMap.get(ci.getCourse().getIsvuCode()));
		
		Assessment a = new Assessment();
		a.setName(tag.getName());
		a.setShortName(shortName);
		a.setAssessmentTag(tag);
		a.setCourseInstance(ci);
		a.setEvent(cwe);
		StringBuilder sb = new StringBuilder();
		sb.append("setPassed(rawPresent());\n");
		sb.append("setPresent(rawPresent());\n");
		sb.append("setScore(rawScore());\n");
		a.setProgram(sb.toString());
		a.setProgramType("java");
		a.setProgramVersion(0);
		dh.getAssessmentDAO().save(em, a);
		
		return a;
	}

	private static String createUniqueAssessmentShortName(String shortName,
			Set<String> set) {
		if(set==null) return shortName;
		if(!set.contains(shortName)) return shortName;
		int razlika = 'Z'-'A';
		for(int i = 0; i < razlika; i++) {
			String candidate = shortName+"_"+(char)('A'+i);
			if(!set.contains(candidate)) return candidate;
		}
		Random r = new Random();
		for(int i = 0; i < 1000; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(shortName).append('_');
			for(int j = 0; j < 5; j++) {
				sb.append(shortName).append((char)('A'+r.nextInt('Z'-'A')));
			}
			String candidate = sb.toString();
			if(!set.contains(candidate)) return candidate;
		}
		// Ako se ovo vrati, stvar ce puknuti, ali tu ne mozemo puno napraviti...
		return shortName;
	}

	public static void getAssessmentEventsData(final AssessmentsScheduleEditData data) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				List<AssessmentTag> tagList = dh.getAssessmentTagDAO().list(em);
				if (tagList == null || tagList.size()==0)
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.TagsNotFound"));
				data.setAllAssessmentTags(tagList);
				
				List<YearSemester> semesterList = dh.getYearSemesterDAO().list(em);
				if (semesterList == null || semesterList.size()==0)
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.SemestersNotFound"));
				data.setAllSemesters(semesterList);
				
				return null;
			}
		});
	}
	
	/**
	 * Dummy verzija koja služi za unos testnih podataka. NE koristiti u produkciji.
	 * @param messageLogger
	 * @param list
	 * @param yearSemesterID
	 * @param tagShortName
	 */
	public static void addAssessmentEvents(final IMessageLogger messageLogger, final List<AssessmentScheduleBean> list, 
			final String yearSemesterID, final String tagShortName) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				// Najprije dohvati sve kolegije na semestru, i mapiraj ih po isvuSifri
				YearSemester yearSemester = dh.getYearSemesterDAO().get(em, yearSemesterID);
				if(yearSemester==null) {
					messageLogger.addErrorMessage(messageLogger.getText("Error.invalidParameters"));
					return null;
				}
				
				//provjera postoji li zadani assessmentTag u bazi
				if (tagShortName == null) {
					messageLogger.addErrorMessage(messageLogger.getText("Error.invalidParameters"));
					return null;
				}
				AssessmentTag dbTag = dh.getAssessmentTagDAO().getByShortName(em, tagShortName);
				if (dbTag == null) {
					messageLogger.addErrorMessage(messageLogger.getText("Error.invalidParameters"));
					return null;
				}
				
				List<CourseInstance> instances = dh.getCourseInstanceDAO().findForSemester(em, yearSemesterID);
				List<Assessment> assessmentsWithTag = dh.getAssessmentDAO().findTaggedOnSemester(em, yearSemester, dbTag);

				Map<String, CourseInstance> courseByIsvuCodeMap = CourseInstanceUtil.mapCourseInstanceByISVUCode(instances);
				Map<String, Assessment> assessmentByIsvuCodeMap = AssessmentUtil.mapSingleAssessmentByISVUCode(assessmentsWithTag);

				//provjera postoji li assessment s courseInstancom kojeg mi nemamo
				for (AssessmentScheduleBean esb : list) {
					if (courseByIsvuCodeMap.get(esb.getCourseISVUCode())==null) {
						String[] param = new String[1];
						param[0] = esb.getCourseISVUCode();
						messageLogger.addErrorMessage(messageLogger.getText("Error.noSuchCourse",param));
						return null;
					}
				}
				
				//prolazimo kroz sve beanove i kreiramo potrebne evente/assessmente
				for (AssessmentScheduleBean esb : list) {
					
					CourseInstance ci = courseByIsvuCodeMap.get(esb.getCourseISVUCode());
					
					//stvori CourseWideEvent
					CourseWideEvent cwe = new CourseWideEvent();
					cwe.setCourseInstance(ci);
					cwe.setDuration(esb.getDuration());
					cwe.setIssuer(null);
					cwe.setRoom(null);
					cwe.setSpecifier(yearSemesterID+"/ispiti/"+dbTag.getShortName());
					cwe.setStart(esb.getStart());
					cwe.setStrength(EventStrength.STRONG);
					cwe.setTitle(ci.getCourse().getName() + " - " + dbTag.getName());
					
					//provjera postoji li vec assessment s ovim tagom
					Assessment assessment = assessmentByIsvuCodeMap.get(esb.getCourseISVUCode());
					if (assessment == null) {
						assessment = createNewDefAssessment(em, dh, dbTag, ci, cwe, new HashMap<String, Set<String>>());
					} else {
						//gledamo da li postoji CourseWideEvent za taj Assessment
						CourseWideEvent dbCwe = assessment.getEvent();
						//ako postoji radimo update CourseWideEventa iz baze 
						if (dbCwe != null)
							updateDbCwe(dbCwe,cwe);
						//inace stvaramo novi
						else
							assessment.setEvent(cwe);
					}
					
					assessment.getEvent().setContext("a:"+assessment.getId());
				}
				messageLogger.addInfoMessage(messageLogger.getText("Info.dataSuccessfullyInserted"));
				return null;
			}
		});
	}

}
