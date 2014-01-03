package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.model.CourseInstance;

/**
 * Engine za izračun bodova odnosno zastavica studenta.
 * 
 * @author marcupic
 *
 */
public interface IScoreCalculatorEngine {

	/**
	 * Metoda stvara context koji je potreban za sva daljnja izračunavanja.
	 * 
	 * @param courseInstance primjerak kolegija nad kojim će se računati bodovi
	 * @param assessmentDataProvider objekt koji zna kako dohvaćati potrebne podatke za provođenje izračuna
	 * @return kontekst
	 */
	public IScoreCalculatorContext createContext(CourseInstance courseInstance, IAssessmentDataProvider assessmentDataProvider);

	/**
	 * Zahtjev za izračunom bodova studenta na zadanoj provjeri.
	 * 
	 * @param user korisnik
	 * @param assessmentShortName kratko ime provjere
	 * @param context kontekst
	 * @return objekt koji opisuje bodove studenta
	 */
	public StudentScore calculateScore(String assessmentShortName, IScoreCalculatorContext context);
	
	/**
	 * Zahtjev za izračunom bodova studenta na zadanoj provjeri.
	 * 
	 * @param user korisnik
	 * @param flagShortName kratko ime zastavice
	 * @param context kontekst
	 * @return objekt koji opisuje vrijednost zastavice
	 */
	public StudentFlag calculateFlag(String flagShortName, IScoreCalculatorContext context);

	/**
	 * Pozvati prije svakog poziva izracuna bodova/zastavica kako bi se osiguralo da su sve zapamcene "blokade" izbrisane. 
	 */
	public void initCalc(IScoreCalculatorContext context);

}
