package hr.fer.zemris.util.scheduling.support;

import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchedulingDataImpl implements ISchedulingData {

	/**
	 * Used to restrict access to the add methods from anywhere except the data loader in the LocalStarter.
	 */
	private boolean accessLockActivated = false;
	
	private Map<String, Map<DateStamp, List<TimeSpan>>> peopleData = new HashMap<String, Map<DateStamp,List<TimeSpan>>>();
	private Map<RoomData, Map<DateStamp, List<TimeSpan>>> termData = new HashMap<RoomData, Map<DateStamp,List<TimeSpan>>>();
	private Map<DateStamp, List<TimeSpan>>[] peopleDataFast;
	private Map<DateStamp, List<TimeSpan>>[] termDataFast;
	
	private int[] students;
	private int[] terms;
	
	private ItemCache jmbagsCache;
	private ItemCache termsCache;

	public int[] getStudents() {
		if(peopleDataFast==null) {
			buildPeopleDataFast();
		}
		return students;
	}
	
	public int[] getTerms() {
		if(termDataFast==null) {
			buildTermDataFast();
		}
		return terms;
	}
	
	@Override
	public ItemCache getJmbagsCache() {
		return jmbagsCache;
	}
	
	@Override
	public ItemCache getTermsCache() {
		return termsCache;
	}
	
	public SchedulingDataImpl(ItemCache jmbagsCache, ItemCache termsCache){
		this.jmbagsCache = jmbagsCache;
		this.termsCache = termsCache;
	}

	@Override
	public Map<DateStamp, List<TimeSpan>>[] getPeopleDataFast() {
		if(peopleDataFast==null) {
			buildPeopleDataFast();
		}
		return peopleDataFast;
	}
	
	@Override
	public Map<DateStamp, List<TimeSpan>>[] getTermDataFast() {
		if(termDataFast==null) {
			buildTermDataFast();
		}
		return termDataFast;
	}
	
	@SuppressWarnings("unchecked")
	private void buildPeopleDataFast() {
		peopleDataFast = new Map[jmbagsCache.size()];
		for(int si = peopleDataFast.length-1; si >= 0; si--) {
			peopleDataFast[si] = peopleData.get(jmbagsCache.items[si]);
		}
		Set<String> studenti = peopleData.keySet();
		this.students = new int[studenti.size()];
		int i = 0;
		for(String jmbag : studenti) {
			this.students[i] = jmbagsCache.translate(jmbag);
			i++;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void buildTermDataFast() {
		termDataFast = new Map[termsCache.size()];
		for(int si = termDataFast.length-1; si >= 0; si--) {
			termDataFast[si] = termData.get(termsCache.items[si]);
		}
		Set<RoomData> sobe = termData.keySet();
		this.terms = new int[sobe.size()];
		int i = 0;
		for(RoomData soba : sobe) {
			this.terms[i] = termsCache.translate(soba.getId());
			i++;
		}
	}

	public void addPeopleDataItem(String item) {
		String jmbag=null;
		Map<DateStamp, List<TimeSpan>> dateStampMap = new HashMap<DateStamp, List<TimeSpan>>();
		List<TimeSpan> timeSpanList = null;

		String[] elements = item.split("#");
		jmbag = elements[0];
		if(jmbag.length() != 10) throw new IllegalParameterException("JMBAG mora imati 10 znakova (" + jmbag + ")");
		for(int i=1; i<elements.length; i++){
			String dateGroup = elements[i];
			String[] elems = dateGroup.split("\\$");
			DateStamp dateStamp = new DateStamp(elems[0]);
			timeSpanList = new ArrayList<TimeSpan>();
			for(int j=1; j<elems.length; j++){
				String[] timeSpanString = elems[j].split("-");
				TimeStamp ts1 = new TimeStamp(timeSpanString[0]);
				TimeStamp ts2 = new TimeStamp(timeSpanString[1]);
				timeSpanList.add(new TimeSpan(ts1, ts2));
			}
			dateStampMap.put(dateStamp, timeSpanList);
		}
		peopleData.put(jmbag, dateStampMap);
	}
	
	public void addTermDataItem(String item){
		if(accessLockActivated) throw new SchedulingException("Access to this method is forbidden.");
		
		Map<DateStamp, List<TimeSpan>> dateStampMap = new HashMap<DateStamp, List<TimeSpan>>();
		List<TimeSpan> timeSpanList = null;
		
		String[] elems = item.split("#"); 
		String[] roomInfo = elems[0].split("\\$");
		RoomData rd = new RoomData(roomInfo[1], roomInfo[0], Integer.parseInt(roomInfo[2]));
		
		for(int i = 1; i< elems.length; i++){
			String[] stamps = elems[i].split("\\$");  
			String dateStampString = stamps[0]; 
			DateStamp dateStamp = new DateStamp(dateStampString);
			timeSpanList = new ArrayList<TimeSpan>();
			for(int j=1; j<stamps.length; j++){
				String[] timeSpanString = stamps[j].split("-"); 
				TimeStamp ts1 = new TimeStamp(timeSpanString[0]);
				TimeStamp ts2 = new TimeStamp(timeSpanString[1]);
				timeSpanList.add(new TimeSpan(ts1, ts2));
			}
			dateStampMap.put(dateStamp, timeSpanList);
		}
		termData.put(rd, dateStampMap);
	}
	
	@Override
	public Map<String, Map<DateStamp, List<TimeSpan>>> getPeopleData() {
		return peopleData;
	}

	@Override
	public Map<RoomData, Map<DateStamp, List<TimeSpan>>> getTermData() {
		return termData;
	}
	
	@Override
	public void dataLoadingCompleted() {
		this.accessLockActivated=true;
	}

}
