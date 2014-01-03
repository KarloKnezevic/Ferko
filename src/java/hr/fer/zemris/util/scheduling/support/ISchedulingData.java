package hr.fer.zemris.util.scheduling.support;

import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import java.util.List;
import java.util.Map;

public interface ISchedulingData {

	/**
	 * Podaci o zauzecima studenata. Key=JMBAG, Value=Zauzeti periodi po danima
	 */
	public Map<String,Map<DateStamp, List<TimeSpan>>> getPeopleData();
	// sljedece tri metode sluze pretvaranju JMBAG-ova u redne brojeve kako bi se
	// moglo raditi s poljima.
	public Map<DateStamp, List<TimeSpan>>[] getPeopleDataFast();
	public Map<DateStamp, List<TimeSpan>>[] getTermDataFast();
	public int[] getStudents();
	public int[] getTerms();
	public ItemCache getJmbagsCache();
	public ItemCache getTermsCache();
	
	/**
	 * Podaci o terminima/dvoranama. Key=RoomData, Value=Slobodni periodi po danima
	 */
	public Map<RoomData,Map<DateStamp, List<TimeSpan>>> getTermData();
	
	/**
	 * Used for filling the scheduling data object with prepared student data.
	 * Disabled after the data is loaded. Any attempt to use this method after the initial
	 * loading is completed will result in a SchedulingException.
	 */
	public void addPeopleDataItem(String item);
	
	/**
	 * Used for filling the scheduling data object with prepared term data.
	 * Disabled after the data is loaded. Any attempt to use this method after the initial
	 * loading is completed will result in a SchedulingException.
	 */
	public void addTermDataItem(String item);
	
	/**
	 * Must be called after all the data has been loaded.
	 * Activates the access lock to restrict access to the add methods.
	 */
	public void dataLoadingCompleted();
}
