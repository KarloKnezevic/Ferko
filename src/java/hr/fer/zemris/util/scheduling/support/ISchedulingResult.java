package hr.fer.zemris.util.scheduling.support;

import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;

public interface ISchedulingResult {

	/**
	 * Dodaje plan u rezultat.
	 * @param planName
	 */
	public void addPlan(String planName);
	/**
	 * Dodaje dogadaj u plan.
	 * @param eventName
	 */
	public void addEvent(String eventId, String eventName);
	/**
	 * Dodaje termin u dogadaj sa svim potrebnim podacima.
	 * @param eventName
	 * @param termName
	 * @param roomID
	 * @param capacity
	 * @param termDate
	 * @param startTime
	 * @param endTime
	 */
	public void addTerm(String eventName, String termName, String roomID, int capacity, 
			String termDate, int startTimeOffset, int endTimeOffset);
	/**
	 * Dodaje studenta u termin.
	 * @param eventName
	 * @param termName
	 * @param jmbag
	 */
	public void addStudentToTerm(String eventName, String termName, String jmbag);
	
	/**
	 * Rezultati izrade rasporeda. Ne koristi algoritam nego lokalna aplikacija (LocalStarter)
	 * @return
	 */
	public String getResultXML();
	
	/**
	 * Dohvaca vec generirani plan.
	 * @return
	 */
	public IPlan getPlan();	
}
