package hr.fer.zemris.jcms.beans.ext;

public interface MPFormulaContext {
	
	/**
	 * Ovom metodom formula može doći do podataka o trenutnom broju
	 * studenata unutar navedene grupe.
	 * 
	 * @param groupName
	 * @return
	 */
	public int getTotalSizeForGroup(String groupName);
	/**
	 * Ovom metodom formula može doći do podataka o trenutnom broju
	 * studenata koji imaju navedeni tag unutar navedene grupe.
	 * 
	 * @param groupName
	 * @param tagName
	 * @return
	 */
	public int getNumberOfStudentsWithTag(String groupName, String tagName);
	/**
	 * Ovaj poziv postavit će zastavicu kojom se govori da se formula
	 * odnosi na slučaj koji se trenutno izračunava.
	 */
	public void setFormulaAppliesFlag();
	/**
	 * Čisti navedenu zastavicu.
	 */
	public void clearFormulaAppliesFlag();
	/**
	 * Vraća vrijednost zastavice.
	 * @return vrijednost zastavice
	 */
	public boolean getFormulaAppliesFlag();

	/**
	 * Ovim pozivom formula može doći do opisnika trenutne izmjene.
	 * 
	 * @return
	 */
	public ExchangeDescriptor getExchangeDescriptor();
	/**
	 * Iz navedene grupe mice jednog studenta zadanog taga.
	 * @param groupName
	 * @param studentTag
	 */
	public void decrease(String groupName, String studentTag);
	/**
	 * U navedenu grupu dodaje jednog studenta zadanog taga.
	 * @param groupName
	 * @param studentTag
	 */
	public void increase(String groupName, String studentTag);
	/**
	 * Vraca u kojoj su mjeri pravila koja su primjenjiva prekrsena.
	 * @return
	 */
	public int getViolationMeasure();
	/**
	 * Dodaje mjeru krsenja. Argument bi trebao biti pozitivan.
	 * @param measure
	 */
	public void addViolationMeasure(int measure);
	/**
	 * Resetira mjeru krsenja.
	 */
	public void resetViolationMeasure();
	
}
