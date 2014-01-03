package hr.fer.zemris.jcms.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Vrsta provjere kod koje se za svakog studenta može pohraniti broj
 * bodova ostvaren na svakom zadatku, te njegovu grupu. Sustav sam na
 * temelju tih podataka računa ukupno ostvarene bodove. 
 * 
 * @author Ivan Krišto
 */
@Entity
@DiscriminatorValue("R")
@Table(name="ac_problems")
public class AssessmentConfProblems extends AssessmentConfiguration {
	
	private static final long serialVersionUID = 1L;
	
	/** Broj zadataka koje ima provjera. */
	private int numberOfProblems;
	private String scorePerProblem;
	
	/**
	 * Konstruktor.
	 */
	public AssessmentConfProblems() {
	}
	
	/**
	 * @return Broja zadataka koje ima provjera.
	 */
	public int getNumberOfProblems() {
		return this.numberOfProblems;
	}

	public void setNumberOfProblems(int numberOfProblems) {
		this.numberOfProblems = numberOfProblems;
	}
	
	/**
	 * Čuva maksimalni broj bodova za svako pitanje po grupama. Format je niz redaka, gdje svaki redak sadrži:<br>
	 * GRUPA TAB MAX_ZAD1 TAB MAX_ZAD2 TAB ...
	 * @return
	 */
	@Column(nullable=true, length=64000)
	public String getScorePerProblem() {
		return scorePerProblem;
	}
	public void setScorePerProblem(String scorePerProblem) {
		this.scorePerProblem = scorePerProblem;
	}
}
