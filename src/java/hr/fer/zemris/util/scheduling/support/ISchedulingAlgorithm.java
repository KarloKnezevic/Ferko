package hr.fer.zemris.util.scheduling.support;

import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import java.awt.Component;
import java.util.Map;

public interface ISchedulingAlgorithm {


	/**
	 * Podaci potrebi za izradu rasporeda
	 * @param plan 	Plan za koji se izrađuje raspored
	 * @param preparedSchedulingData 	Pripremljeni podaci. Key=entityID(eventID ili termID), Value=SchedulingData.
	 */
	public void prepare(IPlan plan, Map<String, ISchedulingData> eventsSchedulingData) throws SchedulingException;
	
	/**
	 * Pokrece algoritam.
	 */
	public void start() throws SchedulingException;
	
	/**
	 * Zaustavlja algoritam.
	 */
	public void stop() throws SchedulingException;
	
	/**
	 * Pokrece jednu iteraciju algoritma.
	 */
	public void step() throws SchedulingException;
	
	/**
	 * Koristi rezultat nekog drugog algoritma.
	 */
	public void use(ISchedulingResult result) throws SchedulingException;
	
	/**
	 * Vraca panel za custom prikaz tijeka izvodenja.
	 */
	public Component getExecutionFeedback() throws SchedulingException;
	
	/**
	 * Vraca najbolji rezultat izvodenja.
	 */
	public ISchedulingResult getResult() throws SchedulingException;
			
	/**
	 * Vraca citavu populaciju rjesenja.
	 */
	public ISchedulingResult[] getResults() throws SchedulingException;
	
	/**
	 * Registrira monitor kojem algoritam dojavljuje promjenu statusa
	 */
	public void registerSchedulingMonitor(ISchedulingMonitor sm) throws SchedulingException;


	public SchedulingAlgorithmStatus getStatus();
	
	public String getClassName();

}
