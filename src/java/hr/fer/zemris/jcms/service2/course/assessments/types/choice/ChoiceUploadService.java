package hr.fer.zemris.jcms.service2.course.assessments.types.choice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.beans.ext.ChoiceUserScoreBean;
import hr.fer.zemris.jcms.beans.ext.UserAnswersBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.parsers.ChoiceUserScoreParser;
import hr.fer.zemris.jcms.parsers.ChoiceUserScoreRMKParser;
import hr.fer.zemris.jcms.parsers.UserAnswersParser;
import hr.fer.zemris.jcms.parsers.UserAnswersRMKParser;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.AssessmentService;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AdminUploadChoiceConfData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

/**
 * Razred sloja usluge koji nudi upload rezultata za provjeru na obrazac.
 * 
 * @author marcupic
 *
 */
public class ChoiceUploadService {

	/* ====================================================================================
	 * 
	 * UPLOAD NA PROVJERE S OBRASCOM - glavne metode
	 * 
	 * ====================================================================================
	 */
	
	/**
	 * Metoda koja priprema podatke o provjeri na obrazac za uređivanje.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void fetchChoiceData(EntityManager em, AdminUploadChoiceConfData data) {
		// Pripremi podatke
		if(!fetchChoiceDataPrepare(em, data)) return;
		
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	/**
	 * Metoda koja postavlja rezultate studenata iz datoteke.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void uploadStudentResults(EntityManager em, AdminUploadChoiceConfData data) {

		// TODO: Daj vidi ovu metodu i setStudentResults; čini se da ima hrpa paralelnog ali malo drugačijeg koda...
		
		// Pripremi podatke
		if(!fetchChoiceDataPrepare(em, data)) return;

		if(!"APPEND".equals(data.getAppendOrReplace()) && !"REPLACE".equals(data.getAppendOrReplace())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		boolean append = "APPEND".equals(data.getAppendOrReplace());
		
		if (data.getDataFile() == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noFile"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		if (data.getDataFile().getName().toLowerCase().endsWith(".txt")) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noTxtFile"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		AssessmentConfChoice assessmentConfChoice = (AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration();
		int problemsNum = assessmentConfChoice.getProblemsNum();
		int answersNum = assessmentConfChoice.getAnswersNumber();
		BufferedReader reader = null;
		List<UserAnswersBean> beanList = null;
		try {
			reader = new BufferedReader(new FileReader(data.getDataFile()));
			if("RMK".equals(data.getDataFormat())) {
				beanList = UserAnswersRMKParser.parseRMKFormat(reader, problemsNum, answersNum);
			} else {
				beanList = UserAnswersParser.parseTabbedMultiValueFormat(reader, problemsNum, answersNum);
			}
			reader.close();
			data.getDataFile().delete();
		} catch (FileNotFoundException e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
			
		} catch (IOException e) {
			e.printStackTrace();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.errorWhileReadingFile"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
			
		} catch (IllegalArgumentException e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidInputAtLine")
													+ " " + e.getMessage());
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
		Set<String> regularJmbags = new HashSet<String>();
		Map<String,User> userMap = new HashMap<String, User>(courseUsers.size());
		for(User u : courseUsers) {
			regularJmbags.add(u.getJmbag());
			userMap.put(u.getJmbag(), u);
		}
		boolean errors = false;
		for(UserAnswersBean bean : beanList) {
			if(!regularJmbags.contains(bean.getJmbag())) {
				errors = true;
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+bean.getJmbag());
			}
		}
		
		if (errors) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		Map<String, AssessmentConfChoiceAnswers> map = new HashMap<String, AssessmentConfChoiceAnswers>(courseUsers.size());
		List<AssessmentConfChoiceAnswers> dataList = dh	.getAssessmentDAO()
														.listAssessmentConfChoiceAnswersForAssessement(em,
														(AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration());
		for (AssessmentConfChoiceAnswers confData : dataList) {
			map.put(confData.getUser().getJmbag(), confData);
		}
		
		Set<String> assignedJmbags = new HashSet<String>(beanList.size());
		for (UserAnswersBean bean : beanList) {
			assignedJmbags.add(bean.getJmbag());
			AssessmentConfChoiceAnswers v = map.get(bean.getJmbag());
			if (v == null) {
				v = new AssessmentConfChoiceAnswers();
				v.setAssigner(data.getCurrentUser());
				v.setPresent(true);
				v.setUser(userMap.get(bean.getJmbag()));
				v.setGroup(bean.getGroup());
				v.setAnswers(StringUtil.joinToString(bean.getAnswers(),true));
				v.setAnswersStatus(null);
				v.setAssessmentConfChoice((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration());
				dh.getAssessmentDAO().save(em, v);
			} else {
				v.setPresent(true);
				v.setAnswers(StringUtil.joinToString(bean.getAnswers(),true));
				v.setGroup(bean.getGroup());
				v.setAnswersStatus(null);
			}
		}
		if (!append) {
			for (AssessmentConfChoiceAnswers v : dataList) {
				if (!assignedJmbags.contains(v.getUser().getJmbag())) {
					v.setPresent(false);
				}
			}
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda koja postavlja rezultate studenata.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void setStudentResults(EntityManager em, AdminUploadChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceDataPrepare(em, data)) return;
		
		if(!"APPEND".equals(data.getAppendOrReplace()) && !"REPLACE".equals(data.getAppendOrReplace())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		boolean append = "APPEND".equals(data.getAppendOrReplace());
		
		List<ChoiceUserScoreBean> beanList = null;
		try {
			if("RMK".equals(data.getDataFormat())) {
				beanList = ChoiceUserScoreRMKParser.parseRMKFormat(new StringReader(data.getText()));
			} else {
				beanList = ChoiceUserScoreParser.parseTabbedMultiValueFormat(new StringReader(data.getText()));
			}
		} catch(IOException ex) {
			// Imamo grešku u ulaznim podacima...
			data.getMessageLogger().addErrorMessage(ex.getMessage());
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
		Set<String> regularJmbags = new HashSet<String>();
		Map<String,User> userMap = new HashMap<String, User>(courseUsers.size());
		for(User u : courseUsers) {
			regularJmbags.add(u.getJmbag());
			userMap.put(u.getJmbag(), u);
		}
		boolean errors = false;
		int problemsNum = ((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration()).getProblemsNum();
		for(ChoiceUserScoreBean bean : beanList) {
			if(!regularJmbags.contains(bean.getJmbag())) {
				errors = true;
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+bean.getJmbag());
			}
			// Vidi je li broj odgovora dobar; toleriraj jedan viška za stazu do slike koju treba izignorirati
			String[] tmp = StringUtil.split(bean.getAnswers(), '\t'); 
			if (tmp.length != problemsNum && tmp.length != problemsNum+1) {
				errors = true;
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidNumberOfAnswers") + "(" + problemsNum + ") JMBAG: " + bean.getJmbag());
				continue;
			}
			// Ako je slika tu, makni je
			if (tmp.length != problemsNum) {
				int pos = bean.getAnswers().lastIndexOf('\t');
				if(pos==-1) {
					errors = true;
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidNumberOfAnswers") + "(" + problemsNum + ") JMBAG: " + bean.getJmbag());
					continue;
				}
				bean.setAnswers(bean.getAnswers().substring(0,pos));
			}
			
		}
		if(!errors) {
			Map<String, AssessmentConfChoiceAnswers> map = new HashMap<String, AssessmentConfChoiceAnswers>(courseUsers.size());
			List<AssessmentConfChoiceAnswers> dataList = dh	.getAssessmentDAO()
															.listAssessmentConfChoiceAnswersForAssessement(em,
															(AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration());
			for (AssessmentConfChoiceAnswers confData : dataList) {
				map.put(confData.getUser().getJmbag(), confData);
			}
			
			Set<String> assignedJmbags = new HashSet<String>(beanList.size());
			for (ChoiceUserScoreBean bean : beanList) {
				assignedJmbags.add(bean.getJmbag());
				AssessmentConfChoiceAnswers v = map.get(bean.getJmbag());
				if (v == null) {
					v = new AssessmentConfChoiceAnswers();
					v.setAssigner(data.getCurrentUser());
					v.setPresent(true);
					v.setUser(userMap.get(bean.getJmbag()));
					v.setGroup(bean.getGroup());
					v.setAnswers(bean.getAnswers());
					v.setAnswersStatus(null);
					v.setAssessmentConfChoice((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration());
					dh.getAssessmentDAO().save(em, v);
				} else {
					v.setPresent(true);
					v.setAnswers(bean.getAnswers());
					v.setGroup(bean.getGroup());
					v.setAnswersStatus(null);
				}
			}
			if (!append) {
				for (AssessmentConfChoiceAnswers v : dataList) {
					if (!assignedJmbags.contains(v.getUser().getJmbag())) {
						v.setPresent(false);
					}
				}
			}
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		} else {
			data.setResult(AbstractActionData.RESULT_INPUT);
		}
	}


	/* ====================================================================================
	 * 
	 * UREĐIVANJE PROVJERE S OBRASCOM - pomoćne metode
	 * 
	 * ====================================================================================
	 */
	

	/**
	 * Pomoćna metoda koja popunjava podatke o provjeri i provjerava je li to doista provjera na obrazac.
	 * U slučaju greške, u podatkovni objekt bit će upisani svi potrebni podatci.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 * @return <code>true</code> ako nema grešaka, <code>false</code> inače
	 */
	static boolean fetchChoiceDataPrepare(EntityManager em, AdminUploadChoiceConfData data) {
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return false;

		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}

		Assessment assessment = data.getAssessment();
		
		String key = AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
		if(!key.equals("CHOICE")) {
			// Ups! Netko nam hoće podvaliti krivi tip provjere!
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}

		return true;
	}
}
