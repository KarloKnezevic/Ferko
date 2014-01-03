package hr.fer.zemris.jcms.service2.course.assessments.types.choice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hr.fer.zemris.jcms.beans.ext.ChoiceProblemMappingBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.parsers.ChoiceProblemMappingParser;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.AssessmentService;
import hr.fer.zemris.jcms.service2.ValidationUtil;
import hr.fer.zemris.jcms.service2.ValidationUtilException;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AdminSetDetailedChoiceConfData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

/**
 * Razred sloja usluge koji nudi kompletno podešavanje postavki provjera na obrazac.
 * 
 * @author marcupic
 *
 */
public class ChoiceEditingService {

	/* ====================================================================================
	 * 
	 * UREĐIVANJE PROVJERE S OBRASCOM - glavne metode
	 * 
	 * ====================================================================================
	 */
	
	/**
	 * Metoda koja priprema podatke o provjeri na obrazac za uređivanje.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void fetchChoiceEditingData(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;
		
		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		if("default".equals(data.getSelectedView())) {
			data.setGroupsNum(ac.getGroupsNum()==0 ? "" : String.valueOf(ac.getGroupsNum()));
			data.setPersonalizedGroups(ac.getPersonalizedGroups());
			data.setProblemsNum(ac.getProblemsNum()==0 ? "" : String.valueOf(ac.getProblemsNum()));
			data.setAnswersNumber(ac.getAnswersNumber()==0 ? "" : String.valueOf(ac.getAnswersNumber()));
			data.setErrorColumn(ac.getErrorColumn());
			data.setErrorColumnText(ac.getErrorColumnText());
		} else if("forms".equals(data.getSelectedView())) {
			List<Assessment> availableAssessments = new ArrayList<Assessment>(data.getCourseInstance().getAssessments());
			Collections.sort(availableAssessments, new Comparator<Assessment>() {
				@Override
				public int compare(Assessment o1, Assessment o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			data.setAvailableAssessments(availableAssessments);
		} else if("scoring".equals(data.getSelectedView())) {
			data.setScoreCorrect(Math.abs(ac.getScoreCorrect())< 1E-6 ? "" : Double.toString(ac.getScoreCorrect()));
			data.setScoreIncorrect(Math.abs(ac.getScoreIncorrect())< 1E-6 ? "" : Double.toString(ac.getScoreIncorrect()));
			data.setScoreUnanswered(Math.abs(ac.getScoreUnanswered())< 1E-6 ? "" : Double.toString(ac.getScoreUnanswered()));
			data.setDetailTaskScores(ac.getDetailTaskScores());
		} else if("answers".equals(data.getSelectedView())) {
			data.setCorrectAnswers(ac.getCorrectAnswers());
		} else if("groups".equals(data.getSelectedView())) {
			data.setGroupsLabels(ac.getGroupsLabels());
		} else if("plabels".equals(data.getSelectedView())) {
			data.setProblemsLabels(ac.getProblemsLabels());
		} else if("pmapping".equals(data.getSelectedView())) {
			data.setMapping(ac.getProblemMapping());
		} else if("manip".equals(data.getSelectedView())) {
			data.setProblemManipulators(ac.getProblemManipulators());
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda koja postavlja osnovne parametre provjere.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void setBasicProperties(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;
		
		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		int answersNumberInt = 0;
		int problemsNumInt = 0;
		int groupsNumInt = 0;
		try {
			answersNumberInt = ValidationUtil.tryConvertToInteger(data.getAnswersNumber(), data, "forms.answersNumber", 1, 0, ValidationUtil.NumberCheck.MIN);
			problemsNumInt = ValidationUtil.tryConvertToInteger(data.getProblemsNum(), data, "forms.problemsNum", 1, 0, ValidationUtil.NumberCheck.MIN);
			groupsNumInt = ValidationUtil.tryConvertToInteger(data.getGroupsNum(), data, "forms.groupsNum", 1, 0, ValidationUtil.NumberCheck.MIN);
		} catch(ValidationUtilException ex) {
			// Ne radi ništa jer je već sve postavljeno
			return;
		}
		
		// Postavljanje novih podataka.
		ac.setAnswersNumber(answersNumberInt);
		ac.setErrorColumn(data.getErrorColumn());
		ac.setErrorColumnText(data.getErrorColumnText());
		ac.setPersonalizedGroups(data.getPersonalizedGroups());
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		int oldProblemsNum = ac.getProblemsNum(); 
		if (oldProblemsNum != problemsNumInt) {
			List<AssessmentScore> listScores = dh.getAssessmentDAO().listScoresForAssessment(em, data.getAssessment());
			for (AssessmentScore score : listScores) {
				score.setRawPresent(false);
			}
			List<AssessmentConfChoiceAnswers> listAnswers = dh	.getAssessmentDAO()
																.listAssessmentConfChoiceAnswersForAssessement(em, ac);
			for (AssessmentConfChoiceAnswers answers : listAnswers) {
				answers.setAnswersStatus(null);
				String resizedAnswers = resizeDelimitedString(answers.getAnswers(), problemsNumInt, '\t', "");
				answers.setAnswers(resizedAnswers);
			}
			ac.setProblemsNum(problemsNumInt);
			
			if (StringUtil.isStringBlank(ac.getProblemsLabels())) {
				StringBuilder sb = new StringBuilder();
				for (int i = 1; i <= problemsNumInt; i++) {
					sb.append(i);
					if (i < problemsNumInt) {
						sb.append('\t');
					}
				}
				ac.setProblemsLabels(sb.toString());
			}
		}
		
		int oldGroupsNum = ac.getGroupsNum(); 
		if (oldGroupsNum != groupsNumInt) {
			ac.setGroupsNum(groupsNumInt);
			
			if (!ac.getPersonalizedGroups() && StringUtil.isStringBlank(ac.getGroupsLabels())) {
				StringBuilder sb = new StringBuilder();
				for (char ch = 'A'; ch <= groupsNumInt + 'A' - 1; ch++) {
					sb.append(ch);
					if (ch < groupsNumInt + 'A' - 1) {
						sb.append('\t');
					}
				}
				ac.setGroupsLabels(sb.toString());
			}
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja bodovnu politiku.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void setDetailedScore(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();

		if(!updateDetailTaskScore(data, data.getDetailTaskScores())) return;
		
		double scoreCorrectDbl = 0.0;
		double scoreIncorrectDbl = 0.0;
		double scoreUnansweredDbl = 0.0;
		try {
			scoreCorrectDbl = ValidationUtil.tryConvertToDouble(data.getScoreCorrect(), data, "forms.scoreCorrect", 0, 0, ValidationUtil.NumberCheck.NONE, 0);
			scoreIncorrectDbl = ValidationUtil.tryConvertToDouble(data.getScoreIncorrect(), data, "forms.scoreIncorrect", 0, 0, ValidationUtil.NumberCheck.NONE, 0);
			scoreUnansweredDbl = ValidationUtil.tryConvertToDouble(data.getScoreUnanswered(), data, "forms.scoreUnanswered", 0, 0, ValidationUtil.NumberCheck.NONE, 0);
		} catch(ValidationUtilException ex) {
			// Ne radi ništa jer je već sve postavljeno
			return;
		}
		
		// Postavljanje novih podataka.		
		ac.setScoreCorrect(scoreCorrectDbl);
		ac.setScoreIncorrect(scoreIncorrectDbl);
		ac.setScoreUnanswered(scoreUnansweredDbl);
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja bodovnu politiku iz uploadane datoteke koju potom briše.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void uploadDetailedScore(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		FileProcessor fp = new FileProcessor() {
			@Override
			protected boolean process(EntityManager em, AdminSetDetailedChoiceConfData data, String content) {
				return updateDetailTaskScore(data, content);
			}
		};
		if(!fp.run(em, data)) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja točne odgovore.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void setCorrectAnswers(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		if(!updateCorrectAnswers(data, data.getCorrectAnswers())) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda koja postavlja točne odgovore iz uploadane datoteke koju potom briše.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void uploadCorrectAnswers(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		FileProcessor fp = new FileProcessor() {
			@Override
			protected boolean process(EntityManager em, AdminSetDetailedChoiceConfData data, String content) {
				return updateCorrectAnswers(data, content);
			}
		};
		if(!fp.run(em, data)) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja labele grupa.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void setGroupLabels(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		// Interval ima viši prioritet. Provjeri ako je postavljen, pa automatski generiraj slijed...
		if (!StringUtil.isStringBlank(data.getIntervalStart()) && !StringUtil.isStringBlank(data.getIntervalEnd())) {
			String labels = generateLabels(data.getIntervalStart(), data.getIntervalEnd(), '\t', ac.getGroupsNum());
			if (labels == null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
														+ ": " + data.getIntervalStart() + ", " + data.getIntervalEnd());
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.dataNotSet"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
			ac.setGroupsLabels(labels);
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		if(!updateGroupLabels(data, data.getGroupsLabels())) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda koja postavlja labele grupa iz uploadane datoteke koju potom briše.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void uploadGroupLabels(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		FileProcessor fp = new FileProcessor() {
			@Override
			protected boolean process(EntityManager em, AdminSetDetailedChoiceConfData data, String content) {
				return updateGroupLabels(data, content);
			}
		};
		if(!fp.run(em, data)) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja labele zadataka.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void setProblemLabels(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		// Interval ima viši prioritet. Provjeri ako je postavljen, pa automatski generiraj slijed...
		if (!StringUtil.isStringBlank(data.getIntervalStart()) && !StringUtil.isStringBlank(data.getIntervalEnd())) {
			String labels = generateLabels(data.getIntervalStart(), data.getIntervalEnd(), '\t', ac.getProblemsNum());
			if (labels == null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
														+ ": " + data.getIntervalStart() + ", " + data.getIntervalEnd());
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.dataNotSet"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
			ac.setProblemsLabels(labels);
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		if(!updateProblemsLabels(data, data.getProblemsLabels())) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda koja postavlja labele grupa iz uploadane datoteke koju potom briše.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void uploadProblemLabels(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		FileProcessor fp = new FileProcessor() {
			@Override
			protected boolean process(EntityManager em, AdminSetDetailedChoiceConfData data, String content) {
				return updateProblemsLabels(data, content);
			}
		};
		if(!fp.run(em, data)) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja mapiranje zadataka.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void setProblemMapping(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		if(!updateProblemMapping(data, data.getMapping())) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja mapiranje zadataka iz uploadane datoteke koju potom briše.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void uploadProblemMapping(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		FileProcessor fp = new FileProcessor() {
			@Override
			protected boolean process(EntityManager em, AdminSetDetailedChoiceConfData data, String content) {
				return updateProblemMapping(data, content);
			}
		};
		if(!fp.run(em, data)) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja manipulatore zadataka.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void setProblemManipulators(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		if(!updateProblemManipulators(data, data.getProblemManipulators())) return;
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja postavlja manipulatore zadataka iz uploadane datoteke koju potom briše.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void uploadProblemManipulators(EntityManager em, AdminSetDetailedChoiceConfData data) {

		// Pripremi podatke
		if(!fetchChoiceEditingPrepare(em, data)) return;

		FileProcessor fp = new FileProcessor() {
			@Override
			protected boolean process(EntityManager em, AdminSetDetailedChoiceConfData data, String content) {
				return updateProblemManipulators(data, content);
			}
		};
		if(!fp.run(em, data)) return;
		
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
	 * Pomoćna metoda koja obavlja ažuriranje bodovne politike.
	 * @param data podatkovni objekt
	 * @param content bodovna politika
	 * @return <code>true</code> ako je sve OK, false inače
	 */
	private static boolean updateDetailTaskScore(AdminSetDetailedChoiceConfData data, String content) {
		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		int problemsNum = ac.getProblemsNum();
		int groupsNum = ac.getGroupsNum();
		
		// Provjera
		if (!StringUtil.isStringBlank(content)) {
			String oldDetailTaskScores = ac.getDetailTaskScores();
			if (oldDetailTaskScores == null || !oldDetailTaskScores.equals(content)) {
				if (!checkDetailTaskScores(data, content, problemsNum, groupsNum)) {
					return false;
				}
				ac.setDetailTaskScores(content);
			}
		}
		return true;
	}
	
	/**
	 * Pomoćna metoda koja obavlja ažuriranje točnih odgovora.
	 * @param data podatci
	 * @param content točni odgovori
	 * @return <code>true</code> ako je sve OK, false inače
	 */
	private static boolean updateCorrectAnswers(AdminSetDetailedChoiceConfData data, String content) {
		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		int problemsNum = ac.getProblemsNum();
		int groupsNum = ac.getGroupsNum();
		
		// Provjera
		if (!StringUtil.isStringBlank(content)) {
			String oldCorrectAnswers = ac.getCorrectAnswers();
			if (oldCorrectAnswers == null || !oldCorrectAnswers.equals(content)) {
				String answ = adjustCorrectAnswers(data, content, problemsNum, groupsNum, ac.getPersonalizedGroups());
				if (answ == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return false;
				}
				ac.setCorrectAnswers(answ);
			}
		}
		return true;
	}

	/**
	 * Pomoćna metoda koja obavlja ažuriranje labela grupa.
	 * @param data podatci
	 * @param content labele grupa
	 * @return <code>true</code> ako je sve OK, false inače
	 */
	private static boolean updateGroupLabels(AdminSetDetailedChoiceConfData data, String content) {
		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		// Provjera
		if (!StringUtil.isStringBlank(content)) {
			if (checkGroupLabels(content, ac, data)) {
				ac.setGroupsLabels(content);
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Pomoćna metoda koja obavlja ažuriranje labela zadataka.
	 * @param data podatci
	 * @param content labele zadataka
	 * @return <code>true</code> ako je sve OK, false inače
	 */
	private static boolean updateProblemsLabels(AdminSetDetailedChoiceConfData data, String content) {
		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		// Provjera
		if (!StringUtil.isStringBlank(content)) {
			if (checkProblemsLabels(content, ac, data)) {
				ac.setProblemsLabels(content);
			} else {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Pomoćna metoda koja obavlja ažuriranje mapiranja zadataka.
	 * @param data podatci
	 * @param content mapiranje zadataka
	 * @return <code>true</code> ako je sve OK, false inače
	 */
	private static boolean updateProblemMapping(AdminSetDetailedChoiceConfData data, String content) {
		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
		
		// Provjera
		if (!StringUtil.isStringBlank(content)) {
			if (checkProblemMapping(content, ac, data)) {
				ac.setProblemMapping(content);
			} else {
				return false;
			}
		} else {
			ac.setProblemMapping(null);
		}
		return true;
	}
	
	/**
	 * Pomoćna metoda koja obavlja ažuriranje manipulatora zadataka.
	 * @param data podatci
	 * @param content manipulatori zadataka
	 * @return <code>true</code> ako je sve OK, false inače
	 */
	private static boolean updateProblemManipulators(AdminSetDetailedChoiceConfData data, String content) {
		AssessmentConfChoice ac = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();

		int problemsNum = ac.getProblemsNum();
		if(problemsNum<1) {
			data.getMessageLogger().addErrorMessage("Broj zadataka na provjeri nije definiran. Molim najprije definirajte taj podatak.");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return false;
		}
		String[] rows = StringUtil.isStringBlank(data.getProblemManipulators()) ? new String[0] : StringUtil.split(data.getProblemManipulators().replace('\r', '\n'), '\n');
		StringBuilder sb = new StringBuilder(StringUtil.isStringBlank(data.getProblemManipulators()) ? 0 : data.getProblemManipulators().length());
		Set<String> keys = new HashSet<String>();
		for(String row : rows) {
			if(StringUtil.isStringBlank(row)) continue;
			String[] elems = StringUtil.split(row, '\t');
			if(elems.length!=3) {
				data.getMessageLogger().addErrorMessage("Pronađen redak s pogrešnim brojem elemenata.");
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			if(!(elems[2].equals("X") || elems[2].equals("x"))) {
				data.getMessageLogger().addErrorMessage("Pronađen redak s nepoznatim modifikatorom.");
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			for(String x : elems) {
				if(StringUtil.isStringBlank(x)) {
					data.getMessageLogger().addErrorMessage("Pronađen redak s praznim poljem.");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return false;
				}
			}
			try {
				int x = Integer.parseInt(elems[1]);
				if(x<1 || x>problemsNum) throw new IllegalArgumentException();
			} catch(Exception ex) {
				data.getMessageLogger().addErrorMessage("\""+elems[1]+"\" nije ispravna oznaka zadatka.");
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			String entryKey = elems[0]+"\t"+elems[1];
			if(!keys.add(entryKey)) {
				data.getMessageLogger().addErrorMessage("Zadano više modifikatora za grupu "+elems[0]+", zadatak "+elems[1]+". To nije podržano.");
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			if(sb.length()!=0) {
				sb.append('\n');
			}
			sb.append(row);
		}
		
		ac.setProblemManipulators(sb.toString());
		return true;
	}
	
	/**
	 * Pomoćni apstraktni razred koji čita predanu datoteku s diska i potom izvršava obradu nad njom.
	 * 
	 * @author marcupic
	 *
	 */
	abstract static class FileProcessor {
		public boolean run(EntityManager em, AdminSetDetailedChoiceConfData data) {
			if (data.getDataFile() == null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noFile"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			
			if (data.getDataFile().getName().toLowerCase().endsWith(".txt")) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noTxtFile"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			
			String content = null;
			BufferedReader reader = null;
			try {
				StringBuilder sb = new StringBuilder(200);
				reader = new BufferedReader(new FileReader(data.getDataFile()));
				String line = null;
				while (true) {
					line = reader.readLine();
					if (line == null) {
						break;
					}
					sb.append(line).append('\n');
				}
				content = sb.toString();
				reader.close();
				data.getDataFile().delete();
				
			} catch (FileNotFoundException e) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.errorWhileReadingFile"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}

			return process(em, data, content);
		}
		
		protected abstract boolean process(EntityManager em, AdminSetDetailedChoiceConfData data, String content);
	}
	/**
	 * Pomoćna metoda koja popunjava podatke o provjeri i provjerava je li to doista provjera na obrazac.
	 * U slučaju greške, u podatkovni objekt bit će upisani svi potrebni podatci.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 * @return <code>true</code> ako nema grešaka, <code>false</code> inače
	 */
	static boolean fetchChoiceEditingPrepare(EntityManager em, AdminSetDetailedChoiceConfData data) {
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

	/**
	 * Mijenja veličinu niza spremljenog u string pri čemu su elementi odvojeni delimiterom.
	 *  
	 * @param str String u koji je spremljen niz podataka odvojenih delimiterom.
	 * @param newLength Nova dužina niza.
	 * @param delimiter Delimiter kojim su odvojeni podatci u stringu.
	 * @param emptyPlace Oznaka praznog mjesta u nizu (npr. "BLANK", "", ...).
	 * @return Početni niz (u stringu) podataka odvojen jednakim delimiterima, ali nove duljine.
	 */
	static String resizeDelimitedString(String str, int newLength, char delimiter, String emptyPlace) {
		if ((str == null) || (str.length() == 0)) {
			return null;
		}
		
		String[] arr = StringUtil.split(str, delimiter);
		StringBuilder sb = new StringBuilder(arr.length*3);
		
		if (arr.length == newLength) {
			return str;
		}
		
		for (int i = 0; i < newLength; i++) {
			if (i < arr.length) {
				sb.append(arr[i]);
			} else {
				sb.append(emptyPlace);
			}
			if (i != newLength - 1) {
				sb.append(delimiter);
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Provjera ispravnosti parametra <code>detailTaskScores</code>.
	 * 
	 * @param data Objekt za pohranu povratnog rezultata i spremanje poruka.
	 * @param contents Sadržaj detailTaskScores-a.
	 * @param problemsNum Broj zadataka.
	 * @param groupsNum Broj grupa.
	 * @return True ako su parametri ispravni, inače false.
	 */
	static boolean checkDetailTaskScores(
			AbstractActionData data, String contents,
			int problemsNum, int groupsNum) {
		
		if (contents.equals("")) {
			return true;
		}
		
		if (contents.length() > AssessmentConfChoice.DETAILED_TASK_SCORES_LENGTH) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.inputTooLong")
													+ ": " + data.getMessageLogger().getText("forms.detailTaskScores"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return false;
		}
		
		String[] detailScores = StringUtil.split(contents, '\n');
		
		int length = 0;
		for (int i = 0; i < detailScores.length; i++) {
			if (!(detailScores[i] == null || detailScores[i].trim().equals(""))) {
				length++;
			}
		}
		
		if (length != groupsNum*problemsNum) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.scoresNotDefinedForAllGroupsAndProblems"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return false;
		}
		
		for (int i = 0; i < length; i++) {
			String[] singleScores = StringUtil.split(detailScores[i], '\t');
			if (singleScores.length != 5) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidScoreDataAtLine")
														+ ": " + (i+1));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Prilagodba ispravnih odgovora (<code>correctAnswers</code>), kako bi se stvar napravila malo robusnijom (ignoriranje ascii 13 pod windowsima
	 * koje u metodi checkCorrectAnswers rusi jednakost, prazni retci koji ruse ocekivani broj linija i sl).
	 * 
	 * @param data Objekt za pohranu povratnog rezultata i spremanje poruka.
	 * @param contents Sadržaj correctAnswers-a.
	 * @param problemsNum Broj zadataka.
	 * @param groupsNum Broj grupa.
	 * @param personalizedGroups 
	 * @return null ako nesto ne valja; inace dobro formatirani correctAnswers
	 */
	private static String adjustCorrectAnswers(
			AbstractActionData data,
			String correctAnswers, int problemsNum, int groupsNum, boolean personalizedGroups) {
		
		if (correctAnswers.equals("")) {
			return correctAnswers;
		}

		List<String> elems = new ArrayList<String>();
		String[] byGroup = TextService.split(correctAnswers, '\n');
		for(String s : byGroup) {
			s = s.trim();
			if(s.length()==0) continue;
			elems.add(s);
		}
		
		if ((groupsNum != 0 && elems.size() != groupsNum) || (!personalizedGroups && groupsNum==0)) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder(correctAnswers.length());
		for (int i = 0; i < elems.size(); i++) {
			String[] byTask = TextService.split(elems.get(i), '\t');
			if (byTask.length != problemsNum + 1) {
				return null;
			}
			if(i>0) sb.append('\n');
			sb.append(elems.get(i));
		}
		return sb.toString();
	}
	
	/**
	 * Provjera ispravnosti parametra <code>groupsLabels</code>.
	 * 
	 * @param groupsLabels Parametar koji postavljano, tj. oznake grupa odvojene tabom (\t).
	 * @param assessmentConfChoice Opisnik provjere kojoj postavljamo parametar.
	 * @param data Objekt za pohranu povratnog rezultata i spremanje poruka.
	 * @return True ako su parametri ispravni, inače false.
	 */
	private static boolean checkGroupLabels(String groupsLabels,
			AssessmentConfChoice assessmentConfChoice,
			AbstractActionData data) {
		
		String oldGroupsLabels = assessmentConfChoice.getGroupsLabels();
		int groupsNum = assessmentConfChoice.getGroupsNum();
		
		if (oldGroupsLabels == null || !oldGroupsLabels.equals(groupsLabels)) {
			if (groupsLabels.length() > AssessmentConfChoice.GROUPS_LABELS_LENGTH) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.inputTooLong")
														+ ": " + data.getMessageLogger().getText("forms.groupsLabels"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			String[] labels = StringUtil.splitTabbedMultiline(groupsLabels);
			if (labels.length != groupsNum) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
														+ ": " + data.getMessageLogger().getText("forms.groupsLabels")
														+ ": " + groupsLabels + ".");
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
		}
		return true;
	}

	/**
	 * Generiranje niza oznaka odvojenih delimiterom <code>delimiter</code>.
	 * 
	 * @param intervalStart Početak niza.
	 * @param intervalEnd Kraj niza.
	 * @param delimiter Delimiter.
	 * @param elementsNum Broj elemenata niza.
	 * @return Generirani niz ili null ako su parametri neispravni
	 * 			(intervalStart > intervalEnd;
	 * 			graničnici intervala nisu broj-broj, malo slovo-malo slovo ili veliko slovo-veliko slovo;
	 * 			intervalEnd - intervalStart != elementsNum).
	 */
	private static String generateLabels(String intervalStart, String intervalEnd, char delimiter, int elementsNum) {
		StringBuilder sb = new StringBuilder();
		
		try {
			int start = -1, end = -1;
			start = Integer.parseInt(intervalStart);
			end = Integer.parseInt(intervalEnd);
			
			if (!(start <= end && start >= 0 && end >= 0)
					|| ((end + 1) - start != elementsNum)) {
				
				return null;
			}
			for (int i = start; i <= end; i++) {
				sb.append(i);
				if (i != end) {
					sb.append(delimiter);
				}
			}
			
			return sb.toString();
		} catch (NumberFormatException e) {
		}
		
		if (intervalStart.length() == 1 && intervalEnd.length() == 1) {
			char start = intervalStart.charAt(0);
			char end = intervalEnd.charAt(0);
			
			if (!(Character.isLowerCase(start) && Character.isLowerCase(end))
					&& !(Character.isUpperCase(start) && Character.isUpperCase(end))
					|| ((end + 1) - start != elementsNum)) {
				
				return null;
			}
			
			if (start > end) {
				return null;
			}
			
			for (char i = start; i <= end; i++) {
				sb.append(i);
				if (i != end) {
					sb.append(delimiter);
				}
			}
			return sb.toString();
			
		} else {
			return null;
		}
	}

	/**
	 * Provjera ispravnosti parametra <code>problemsLabels</code>.
	 * 
	 * @param problemsLabels Parametar koji postavljano, tj. oznake zadataka odvojene tabom (\t).
	 * @param assessmentConfChoice Opisnik provjere kojoj postavljamo parametar.
	 * @param data Objekt za pohranu povratnog rezultata i spremanje poruka.
	 * @return True ako su parametri ispravni, inače false.
	 */
	private static boolean checkProblemsLabels(String problemsLabels,
			AssessmentConfChoice assessmentConfChoice,
			AbstractActionData data) {
		
		int problemsNum = assessmentConfChoice.getProblemsNum();
		String oldProblemLabels = assessmentConfChoice.getProblemsLabels();
		
		if (oldProblemLabels == null || !oldProblemLabels.equals(problemsLabels)) {
			String[] labels = StringUtil.split(problemsLabels, '\t');
			if (labels.length != problemsNum) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
														+ ": " + data.getMessageLogger().getText("forms.problemsLabels")
														+ ": " + problemsLabels + ".");
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
		}
		return true;
	}

	/**
	 * Provjera ispravnosti parametra <code>problemMapping</code>.
	 * 
	 * @param mapping Parametar koji postavljano, tj. mapiranje zadataka.
	 * @param assessmentConfChoice Opisnik provjere kojoj postavljamo parametar.
	 * @param data Objekt za pohranu povratnog rezultata i spremanje poruka.
	 * @return True ako su parametri ispravni, inače false.
	 */
	private static boolean checkProblemMapping(String mapping,
			AssessmentConfChoice assessmentConfChoice,
			AbstractActionData data) {
		
		String oldMapping = assessmentConfChoice.getProblemMapping();
		
		if (oldMapping == null || !oldMapping.equals(mapping)) {
			if (mapping.length() > AssessmentConfChoice.PROBLEM_MAPPING_LENGTH) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.inputTooLong")
														+ ": " + data.getMessageLogger().getText("forms.problemMapping"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			if (StringUtil.isStringBlank(assessmentConfChoice.getGroupsLabels())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.groupLabelsNotSpecified"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			if (StringUtil.isStringBlank(assessmentConfChoice.getProblemsLabels())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.problemLabelsNotSpecified"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			
			boolean error = false;
			String[] groupsLabels = StringUtil.splitTabbedMultiline(assessmentConfChoice.getGroupsLabels());
			String[] problemLabels = StringUtil.split(assessmentConfChoice.getProblemsLabels(), '\t');
			int groupsNum = groupsLabels.length;
			int problemsNum = problemLabels.length;
			if (groupsLabels[groupsNum-1] == null || groupsLabels[groupsNum-1].equals("")) {
				groupsNum--;
			}
			if (problemLabels[problemsNum-1] == null || problemLabels[problemsNum-1].equals("")) {
				problemsNum--;
			}
			
			List<ChoiceProblemMappingBean> mappingList = null;
			try {
				mappingList = ChoiceProblemMappingParser.parseTabbedMultiValueFormat(new StringReader(mapping));
			} catch (IOException ignorable) { ignorable.printStackTrace(); }
			
			int currentProblem = 0;
			int currentGroup = 0;
			
			for (ChoiceProblemMappingBean pmBean : mappingList) {
				if (currentGroup == -1) {
					// Podataka je više nego što bi trebalo biti!
					error = true;
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
							+ ": " + data.getMessageLogger().getText("forms.problemMapping")
							+ ": Podataka je više nego što bi trebalo biti!");
				}
				if (!pmBean.getGroupLabel().equals(groupsLabels[currentGroup])) {
					error = true;
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
							+ ": " + data.getMessageLogger().getText("forms.problemMapping")
							+ ": Linija " + (currentGroup*problemsNum + currentProblem + 1)
							+ ": Postavljena je grupa: " + pmBean.getGroupLabel() + ", a treba biti: " + groupsLabels[currentGroup]);
					break;
				}
				if (!pmBean.getProblemLabel().equals(problemLabels[currentProblem])) {
					error = true;
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
							+ ": " + data.getMessageLogger().getText("forms.problemMapping")
							+ ": Linija " + (currentGroup*problemsNum + currentProblem + 1)
							+ ": Postavljen je zadatak: " + pmBean.getProblemLabel() + ", a treba biti: " + problemLabels[currentProblem]);
					break;
				}
				currentProblem++;
				if (currentProblem == problemsNum) {
					currentGroup++;
					currentProblem = 0;
					if (currentGroup == groupsNum) {
						currentGroup = -1;
					}
				}
			}
			
			if (error) {
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			
		}
		return true;
	}
}
