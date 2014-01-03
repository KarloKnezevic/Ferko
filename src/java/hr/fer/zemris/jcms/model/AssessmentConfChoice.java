package hr.fer.zemris.jcms.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Vrsta provjere kod koje se studentovi odgovori očitavaju s obrasca.
 * 
 * @author Ivan Krišto
 */
@Entity
@DiscriminatorValue("C")
@Table(name="ac_choice") 
public class AssessmentConfChoice extends AssessmentConfiguration {
	
	private static final long serialVersionUID = 1L;
	
	/** Broj zadataka koje ima provjera. */
	private int problemsNum;
	
	/** Labele zadataka. Format je lbl1\tlbl2\tlbl3... */
	private String problemsLabels;
	
	/** Broj grupa provjere. */
	private int groupsNum;
	
	/** Labele grupa provjere. Format je lbl1\tlbl2\tlbl3... */
	private String groupsLabels;
	
	/** Radi li se o personaliziranim grupama (s bar-kodom) ili standardnim. */
	private boolean personalizedGroups;
	
	/** Broj ponuđenih odgovora. */
	private int answersNumber;
	
	/** Ima li provjera dodatni stupac pogreške i tablicu za korekcije. */ 
	private boolean errorColumn;
	
	/** Tekst iznad stupca greške. */
	private String errorColumnText;
	
	/** Broj bodova za točan odgovor. */
	private double scoreCorrect;
	
	/** Broj bodova za netočan odgovor. */
	private double scoreIncorrect;
	
	/** Broj bodova za neodgovoreno pitanje. */
	private double scoreUnanswered;
	
	/** Raspodijela bodova po zadatcima (za nejednoliku raspodijelu). */
	private String detailTaskScores;
	
	/**
	 * Točni odgovori.<br />
	 * Format:<br />
	 * grupa1\tOdgovor1\tOdgovor2\tOdgovor3\tOdgovor4...<br />
	 * grupa2\tOdgovor1\tOdgovor2\tOdgovor3\tOdgovor4...<br />
	 * ... <br />
	 * Dopušteno je umjesto: <code>OdgovorN</code> navesti više odgovora odvojenih zarezom (više točnih odgovora po pitanju),
	 * npr. A,C,D
	 */
	private String correctAnswers;
	
	/** Mapiranje tipova i verzija zadataka po grupama. */
	private String problemMapping;
	
	/** Manipulatori zadataka po grupama. */
	private String problemManipulators;
	
	/**
	 * Konstruktor.
	 */
	public AssessmentConfChoice() {
	}

	/**
	 * @return the answersNumber
	 */
	public int getAnswersNumber() {
		return this.answersNumber;
	}

	/**
	 * @param answersNumber the answersNumber to set
	 */
	public void setAnswersNumber(int answersNumber) {
		this.answersNumber = answersNumber;
	}

	/**
	 * @return Točni odgovori po grupama. Svaka skupina odgovorora odvojena je sa "\n".
	 */
	@Column(length=64000)
	public String getCorrectAnswers() {
		return this.correctAnswers;
	}
	public static final int CORRECT_ANSWERS_LENGTH = 64000;

	/**
	 * @param correctAnswers the correctAnswers to set
	 */
	public void setCorrectAnswers(String correctAnswers) {
		this.correctAnswers = correctAnswers;
	}

	/**
	 * @return the errorColumn
	 */
	public boolean getErrorColumn() {
		return this.errorColumn;
	}

	/**
	 * @param errorColumn the errorColumn to set
	 */
	public void setErrorColumn(boolean errorColumn) {
		this.errorColumn = errorColumn;
	}
	
	@Column(length=20, nullable=true)
	public String getErrorColumnText() {
		return errorColumnText;
	}
	public static final int ERROR_COLUMN_TEXT_LENGTH = 20;

	public void setErrorColumnText(String errorColumnText) {
		this.errorColumnText = errorColumnText;
	}

	/**
	 * @return the groupsLabels
	 */
	@Column(length=32000)
	public String getGroupsLabels() {
		return this.groupsLabels;
	}
	public static final int GROUPS_LABELS_LENGTH = 32000;

	/**
	 * @param groupsLabels the groupsLabels to set
	 */
	public void setGroupsLabels(String groupsLabels) {
		this.groupsLabels = groupsLabels;
	}

	@Column(length=32000)
	public String getProblemManipulators() {
		return problemManipulators;
	}
	public void setProblemManipulators(String problemManipulators) {
		this.problemManipulators = problemManipulators;
	}
	
	/**
	 * @return the groupsNum
	 */
	public int getGroupsNum() {
		return this.groupsNum;
	}

	/**
	 * @param groupsNum the groupsNum to set
	 */
	public void setGroupsNum(int groupsNum) {
		this.groupsNum = groupsNum;
	}

	/**
	 * @return the problemsLabels
	 */
	public String getProblemsLabels() {
		return this.problemsLabels;
	}

	/**
	 * @param problemsLabels the problemsLabels to set
	 */
	public void setProblemsLabels(String problemsLabels) {
		this.problemsLabels = problemsLabels;
	}

	/**
	 * @return the problemsNum
	 */
	public int getProblemsNum() {
		return this.problemsNum;
	}

	/**
	 * @param problemsNum the problemsNum to set
	 */
	public void setProblemsNum(int problemsNum) {
		this.problemsNum = problemsNum;
	}

	/**
	 * @return the scoreCorrect
	 */
	public double getScoreCorrect() {
		return this.scoreCorrect;
	}

	/**
	 * @param scoreCorrect the scoreCorrect to set
	 */
	public void setScoreCorrect(double scoreCorrect) {
		this.scoreCorrect = scoreCorrect;
	}

	/**
	 * @return the scoreIncorrect
	 */
	public double getScoreIncorrect() {
		return this.scoreIncorrect;
	}

	/**
	 * @param scoreIncorrect the scoreIncorrect to set
	 */
	public void setScoreIncorrect(double scoreIncorrect) {
		this.scoreIncorrect = scoreIncorrect;
	}

	/**
	 * @return the scoreUnanswered
	 */
	public double getScoreUnanswered() {
		return this.scoreUnanswered;
	}

	/**
	 * @param scoreUnanswered the scoreUnanswered to set
	 */
	public void setScoreUnanswered(double scoreUnanswered) {
		this.scoreUnanswered = scoreUnanswered;
	}

	/**
	 * @return the personalizedGroups
	 */
	public boolean getPersonalizedGroups() {
		return this.personalizedGroups;
	}

	/**
	 * @param personalizedGroups the personalizedGroups to set
	 */
	public void setPersonalizedGroups(boolean personalizedGroups) {
		this.personalizedGroups = personalizedGroups;
	}
	
	/**
	 * Detaljna raspodijela bodova po zadatcima. Format:
	 * <table border="1">
	 *   <tr>
	 *     <td>grupa</td>
	 *     <td>zadatak</td>
	 *     <td>tocno</td>
	 *     <td>netocno</td>
	 *     <td>neodgovoreno</td>
	 *   </tr>
	 * </table>
	 * Elementi su odvojeni tabovima.<br />
	 * Primjer:
	 * <table border="1">
	 *   <tr>
	 *     <td>A tab 1 tab 2 tab -1 tab 0</td>
	 *   </tr>
	 *   <tr>
	 *     <td>A tab 2 tab 1 tab -0.5 tab 0</td>
	 *   </tr>  
	 *   <tr>
	 *     <td>...</td>
	 *   </tr>
	 *   <tr>
	 *     <td>B tab 1 tab 2 tab -1 tab 0</td>
	 *   </tr>
	 *   <tr>
	 *     <td>...</td>
	 *   </tr>
	 * </table>
	 * 
	 * @return
	 */
	@Column(length=128000, nullable=true)
	public String getDetailTaskScores() {
		return detailTaskScores;
	}
	public static final int DETAILED_TASK_SCORES_LENGTH = 128000;

	public void setDetailTaskScores(String detailTaskScores) {
		this.detailTaskScores = detailTaskScores;
	}
	
	/**
	 * Mapiranje tipova i verzija zadataka po grupama.
	 * Format:
	 * <table border="1">
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka 1. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka 2. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka zadnjeg zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka 1. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka 2. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka zadnjeg zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *	</table>
	 *	Mapiranje je potrebno definirati za sve zadatke i sve grupe!<br />
	 *	Ako mapiranje nije definirano, svaka grupa na istom mjestu ima isti tip zadatka.<br /> 
	 *	Podatci o mapiranju za pojedini zadatak pojedine grupe su odvojeni tabom.<br /> 
	 */
	@Column(length=2000000, nullable=true)
	public String getProblemMapping() {
		return problemMapping;
	}
	public static final int PROBLEM_MAPPING_LENGTH = 2000000;

	public void setProblemMapping(String problemMapping) {
		this.problemMapping = problemMapping;
	}

}
