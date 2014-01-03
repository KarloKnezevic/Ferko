package hr.fer.zemris.jcms.service2.course.assessments.types.choice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.beans.ext.ConfChoiceScoreBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.AssessmentService;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.ConfChoiceScoreEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

/**
 * Razred sloja usluge koji nudi upload rezultata za provjeru na obrazac.
 * 
 * @author marcupic
 *
 */
public class ChoiceResultsEditingService {

	/* ====================================================================================
	 * 
	 * UREĐIVANJE BODOVA NA PROVJERI S OBRASCOM - glavne metode
	 * 
	 * ====================================================================================
	 */
	
	/**
	 * Metoda koja dohvaća podatke za prikaz.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void fetchStudentResults(EntityManager em, ConfChoiceScoreEditData data) {

		// Pripremi podatke
		if(!fetchChoiceDataPrepare(em, data)) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
		List<AssessmentConfChoiceAnswers> list = dh	.getAssessmentDAO()
													.listAssessmentConfChoiceAnswersForAssessement(em, 
													(AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration());
		Set<Character> letters = new HashSet<Character>();
		for(User u : courseUsers) {
			letters.add(Character.valueOf(u.getLastName().charAt(0)));
		}
		List<String> lettersAsStrings = new ArrayList<String>(letters.size());
		for(Character c : letters) {
			lettersAsStrings.add(c.toString());
		}
		Collections.sort(lettersAsStrings,StringUtil.HR_COLLATOR);
		lettersAsStrings.add("*");
		Character selLetter = data.getBean().getLetter();
		if(selLetter==null && lettersAsStrings.size()>0) {
			selLetter = Character.valueOf(lettersAsStrings.get(0).charAt(0));
		}
		data.setAvailableLetters(lettersAsStrings);
		// Ako nema korisnika, gotovi smo
		if(courseUsers.isEmpty()) {
			data.getBean().setItems(new ArrayList<ConfChoiceScoreBean>());
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		data.getBean().setLetter(selLetter);
		// Inače isfiltriraj tražene korisnike
		List<User> selectedUsers = new ArrayList<User>(courseUsers.size()/10);
		if(selLetter.charValue()=='*') {
			selectedUsers.addAll(courseUsers);
		} else {
			for(User u : courseUsers) {
				if(u.getLastName().charAt(0)==selLetter.charValue()) {
					selectedUsers.add(u);
				}
			}
		}
		Collections.sort(selectedUsers,StringUtil.USER_COMPARATOR);
		List<AssessmentConfChoiceAnswers> scores = new ArrayList<AssessmentConfChoiceAnswers>(selectedUsers.size());
		Map<User,AssessmentConfChoiceAnswers> map = new HashMap<User, AssessmentConfChoiceAnswers>();
		Set<User> selectedUsersSet = new HashSet<User>(selectedUsers);
		for(AssessmentConfChoiceAnswers s : list) {
			if(selectedUsersSet.contains(s.getUser())) {
				scores.add(s);
				map.put(s.getUser(), s);
			}
		}
		
		int problemsNum = ((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration()).getProblemsNum();
		data.getBean().setProblemsNum(problemsNum);
		List<ConfChoiceScoreBean> l = new ArrayList<ConfChoiceScoreBean>(selectedUsers.size());
		for(User u : selectedUsers) {
			ConfChoiceScoreBean b = new ConfChoiceScoreBean();
			l.add(b);
			b.setFirstName(u.getFirstName());
			b.setLastName(u.getLastName());
			b.setJmbag(u.getJmbag());
			b.setUserID(u.getId());
			b.setProblemsNum(problemsNum);
			AssessmentConfChoiceAnswers s = map.get(u);
			if(s==null) {
				b.setId(null);
				b.setAnswers(new String[problemsNum]);
				b.setPresent(false);
				b.setGroup("");
			} else {
				b.setPresent(s.getPresent());
				b.setId(s.getId());
				b.setGroup(s.getGroup());
				if(s.getAssigner()!=null) {
					b.setAssigner(s.getAssigner().getLastName()+" "+s.getAssigner().getFirstName());
				}
				if(s.getPresent()) {
					String[] answers = null;
					if (s.getAnswers() != null) {
						answers = StringUtil.split(s.getAnswers(), '\t');
					}
					if (answers == null) {
						answers = new String[problemsNum];
					}
					b.setAnswers(answers);
				} else {
					b.setAnswers(new String[problemsNum]);
				}
			}
		}
		data.getBean().setItems(l);
		data.setResult(AbstractActionData.RESULT_INPUT);
		return;
		
		//data.setResult(AbstractActionData.RESULT_SUCCESS);
		//data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
	}

	/**
	 * Metoda koja ažurira prethodno dohvaćene rezultate studenata.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void updateStudentResults(EntityManager em, ConfChoiceScoreEditData data) {

		// Pripremi podatke
		if(!fetchChoiceDataPrepare(em, data)) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
		List<AssessmentConfChoiceAnswers> list = dh	.getAssessmentDAO()
													.listAssessmentConfChoiceAnswersForAssessement(em, 
													(AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration());
		Set<Character> letters = new HashSet<Character>();
		for(User u : courseUsers) {
			letters.add(Character.valueOf(u.getLastName().charAt(0)));
		}
		List<String> lettersAsStrings = new ArrayList<String>(letters.size());
		for(Character c : letters) {
			lettersAsStrings.add(c.toString());
		}
		Collections.sort(lettersAsStrings,StringUtil.HR_COLLATOR);
		lettersAsStrings.add("*");
		Character selLetter = data.getBean().getLetter();
		if(selLetter==null && lettersAsStrings.size()>0) {
			selLetter = Character.valueOf(lettersAsStrings.get(0).charAt(0));
		}
		data.setAvailableLetters(lettersAsStrings);
		// Ako nema korisnika, gotovi smo
		if(courseUsers.isEmpty()) {
			data.getBean().setItems(new ArrayList<ConfChoiceScoreBean>());
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		data.getBean().setLetter(selLetter);
		// Inače isfiltriraj tražene korisnike
		List<User> selectedUsers = new ArrayList<User>(courseUsers.size()/10);
		if(selLetter.charValue()=='*') {
			selectedUsers.addAll(courseUsers);
		} else {
			for(User u : courseUsers) {
				if(u.getLastName().charAt(0)==selLetter.charValue()) {
					selectedUsers.add(u);
				}
			}
		}
		Collections.sort(selectedUsers,StringUtil.USER_COMPARATOR);
		List<AssessmentConfChoiceAnswers> scores = new ArrayList<AssessmentConfChoiceAnswers>(selectedUsers.size());
		Map<User,AssessmentConfChoiceAnswers> map = new HashMap<User, AssessmentConfChoiceAnswers>();
		Set<User> selectedUsersSet = new HashSet<User>(selectedUsers);
		for(AssessmentConfChoiceAnswers s : list) {
			if(selectedUsersSet.contains(s.getUser())) {
				scores.add(s);
				map.put(s.getUser(), s);
			}
		}
		
		int problemsNum = ((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration()).getProblemsNum();
		data.getBean().setProblemsNum(problemsNum);

		// Tu počinje snimanje:
		Map<Long,AssessmentConfChoiceAnswers> mapByID = new HashMap<Long, AssessmentConfChoiceAnswers>();
		for (AssessmentConfChoiceAnswers s : scores) {
			mapByID.put(s.getId(), s);
		}

		Map<Long,User> mapByUser = new HashMap<Long, User>(selectedUsers.size());
		for(User u : selectedUsers) {
			mapByUser.put(u.getId(), u);
		}
		boolean prevara = false;
		
		Map<Integer, String[]> mapAnswers = new HashMap<Integer, String[]>();
		Integer index = -1;
		for(ConfChoiceScoreBean b : data.getBean().getItems()) {
			index++;
			if(b.getId()!=null) {
				AssessmentConfChoiceAnswers s = mapByID.get(b.getId());
				if(s==null) {
					prevara = true;
					break;
				}
				if (b.getPresent()) {
					String[] answers = b.getAnswers();
					mapAnswers.put(index, answers);
				}
			} else {
				if(b.getUserID()==null) {
					prevara = true;
					break;
				} else {
					User u = mapByUser.get(b.getUserID());
					if(u==null) {
						prevara = true;
						break;
					}
				}
				if (b.getPresent()) {
					String[] answers = b.getAnswers();
					mapAnswers.put(index, answers);
				}
			}
		}
		
		if(prevara) {
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		index = -1;
		for(ConfChoiceScoreBean b : data.getBean().getItems()) {
			index++;
			if(b.getId()!=null) {
				AssessmentConfChoiceAnswers s = mapByID.get(b.getId());
				if (!b.getPresent() && s.getPresent()) {
					s.setAnswers(null);
					s.setAnswersStatus(null);
					s.setAssigner(null);
					s.setPresent(false);
					s.setGroup(null);
					continue;
				}
				
				if (b.getPresent()) {
					// Ako trebam ažurirati, onda to napravi
					s.setGroup(b.getGroup());
					String[] answers = mapAnswers.get(index);
					String combinedAns = StringUtil.joinToString(answers, true);
					if (s.getAnswers()==null || !s.getAnswers().equals(combinedAns)) {
						s.setAnswers(combinedAns);
						s.setAnswersStatus(null);
						s.setAssigner(data.getCurrentUser());
					}
					if(!s.getPresent()) {
						s.setPresent(true);
					}
				}
			} else if (b.getPresent()) {
				User u = mapByUser.get(b.getUserID());
				AssessmentConfChoiceAnswers s = new AssessmentConfChoiceAnswers();
				s.setAssessmentConfChoice((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration());
				s.setUser(u);
				s.setGroup(b.getGroup());
				String[] answers = mapAnswers.get(index);
				String combinedAns = StringUtil.joinToString(answers, true);
				if(b.getPresent()) {
					s.setPresent(true);
					s.setAnswers(combinedAns);
					s.setAnswersStatus(null);
					s.setAssigner(data.getCurrentUser());
				} else {
					s.setPresent(false);
					s.setAnswers(null);
					s.setAnswersStatus(null);
				}
				dh.getAssessmentDAO().save(em, s);
			}
		}
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
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
	static boolean fetchChoiceDataPrepare(EntityManager em, ConfChoiceScoreEditData data) {
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getBean().getAssessmentID())) return false;

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

		// Provjeri je li numberOfProblems postavljen
		int problemsNum = ((AssessmentConfChoice) assessment.getAssessmentConfiguration()).getProblemsNum();
		if (problemsNum == 0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.numberOfProblemsNotSet"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}

		return true;
	}
}
