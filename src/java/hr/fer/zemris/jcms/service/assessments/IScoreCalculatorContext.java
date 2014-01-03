package hr.fer.zemris.jcms.service.assessments;

import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;

public interface IScoreCalculatorContext {
	public IAssessmentDataProvider getAssessmentDataProvider();
	public CourseInstance getCourseInstance();
	public StudentScore getStudentScore(Long userID, String assessmentShortName);
	public void setStudentScore(Long userID, String assessmentShortName, StudentScore score);
	public StudentFlag getStudentFlag(Long userID, String flagShortName);
	public void setStudentFlag(Long userID, String flagShortName, StudentFlag flag);
	public void markScoreCalculation(String assessmentShortName);
	public void unmarkScoreCalculation(String assessmentShortName);
	public void markFlagCalculation(String flagShortName);
	public void unmarkFlagCalculation(String flagShortName);
	public User getCurrentUser();
	public void setCurrentUser(User user);
	/**
	 * Pozvati prije svakog poziva izracuna bodova/zastavica kako bi se osiguralo da su sve zapamcene "blokade" izbrisane. 
	 */
	public void initCalc();
	/**
	 * Pozivom ove metode dobit ce se matrica roditelj-djeca izgradena temeljem stvarno koristenih podataka.
	 * @return matrica roditelj-djeca.
	 */
	public Map<String,Set<String>> getDependencies();
	
	public boolean existsApplication(String applShortName);
	public boolean existsAssessment(String assessmentShortName);
	public boolean existsFlag(String flagShortName);
	
	public String assessmentShortNameForTag(String tagName);
	public String flagShortNameForTag(String tagName);
	
	public StudentTask getTask(String componentShortName, int position, String taskName, User user);
	public List<StudentTask> getTasks(String componentShortName, int position, User user);
	
	/**
	 * Za zadanu provjeru vraća polje koje sadrži kratka imena provjera koje su neposredna djeca navedene
	 * provjere.
	 * 
	 * @param assessmentShortName kratko ime provjere čija se traže djeca
	 * @return polje kratkih imena djece; nikada neće biti <code>null</code>; ne smije se mijenjati izvana!
	 */
	public String[] getAssessmentChildren(String assessmentShortName);
	
}
