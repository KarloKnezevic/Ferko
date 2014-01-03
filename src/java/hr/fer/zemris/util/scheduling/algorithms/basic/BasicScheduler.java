package hr.fer.zemris.util.scheduling.algorithms.basic;

import hr.fer.zemris.jcms.model.planning.Definition;
import hr.fer.zemris.util.scheduling.support.ISchedulingAlgorithm;
import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingMonitor;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.SchedulingAlgorithmStatus;
import hr.fer.zemris.util.scheduling.support.SchedulingException;
import hr.fer.zemris.util.scheduling.support.SchedulingResult;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class BasicScheduler implements ISchedulingAlgorithm {
 
	/**
	 * Pripremljene kombinacije termina koje treba isprobati. Key=eventID
	 */
	private static Map<String, List<String>> termCombinations = new HashMap<String, List<String>>();	
	/**
	 * Popisi studenata. Key=eventID
	 */
	private static Map<String, List<String>> studentLists = new HashMap<String, List<String>>();	
	/**
	 * Zauzeca studenata. Key=eventID
	 */
	private static Map<String, List<String>> occupancyLists = new HashMap<String, List<String>>();
	
	private static SchedulingAlgorithmStatus status;
	private static ISchedulingMonitor monitor;
	
	private static StringBuilder feedbackBuilder = new StringBuilder();
	private static JTextArea feedbackArea = null;
	
	private static Thread executingThread; 

	private IPlan plan;
	
	private static ISchedulingResult result = new SchedulingResult();
	
	private static void log(String msg){
		feedbackBuilder.append(msg+"\n");
		feedbackArea.setText(feedbackBuilder.toString());
		feedbackArea.setCaretPosition(feedbackArea.getDocument().getLength());
	}
	
	public JPanel getExecutionFeedback(){
		feedbackBuilder = new StringBuilder();
		JPanel feedbackPanel = new JPanel();
		feedbackArea = new JTextArea("", 10,30);
		JScrollPane scrollPane = new JScrollPane(feedbackArea);
		feedbackPanel.add(scrollPane);
		return feedbackPanel;
	}
	
	public ISchedulingResult getResult(){
		return result;
	}
	
	public String getClassName(){
		return this.getClass().getCanonicalName();
	}
	
	@Override
	public void registerSchedulingMonitor(ISchedulingMonitor sm) throws SchedulingException {
		monitor=sm;
		
	}
	
	@Override
	public SchedulingAlgorithmStatus getStatus() {
		return status;
	}

	
	public void start(){
		
		executingThread = new Thread(new Runnable(){

			@Override
			public void run() {
				result.addPlan(plan.getName());
				//Pokušaj izrade
				try {
					for(IEvent event : plan.getPlanEvents()){
						if(event.getEventDistribution().getType()==Definition.RANDOM_DISTRIBUTION){
							result.addEvent(event.getName(), event.getId());
							log("\nEvent: " + event.getName());
							BasicScheduler.execute(termCombinations.get(event.getId()), studentLists.get(event.getId()), occupancyLists.get(event.getId()), event.getName());
							if(status.equals(SchedulingAlgorithmStatus.FAILURE)) {
								monitor.algorithmStatusChangeNotification();
								return;
							}
						}
					}
					monitor.algorithmStatusChangeNotification();
				} catch (IOException e) {
					throw new SchedulingException(e);
				}
			}
		});
		executingThread.start();
		status = SchedulingAlgorithmStatus.RUNNING;
	}
	
	public void stop(){
		executingThread.interrupt();
		status = SchedulingAlgorithmStatus.INTERRUPTED;
	}
	
	/**
	 * Priprema podataka za algoritam, za svaki dogadaj individualno.
	 */
	public void prepare(IPlan plan, Map<String, ISchedulingData> schedulingData) {

		this.plan=plan;
		for(IEvent event : plan.getPlanEvents()){
			if(event.getEventDistribution().getType()==Definition.RANDOM_DISTRIBUTION){
				List<TermRecord> rawTermList = prepareTermData(schedulingData.get(event.getId()).getTermData());
				System.out.println("rawtermlist size " + rawTermList.size());
				studentLists.put(event.getId(), prepareJmbagData(schedulingData.get(event.getId()).getPeopleData()));
				occupancyLists.put(event.getId(), prepareOccupancyData(schedulingData.get(event.getId()).getPeopleData()));
				
				//Pripremljene kombinacije za minimalni broj termina
				Set<Set<TermRecord>> combinations = prepareTermRecordCombinations(event.getEventDistribution().getMinimumTermNumber(), rawTermList);
				System.out.println("combinations size " + combinations.size());
				for(Set<TermRecord> combination : combinations){
					termCombinations.put(event.getId(), prepareListFromSet(combination));
					break;
				}		
			}
		}	
		status=SchedulingAlgorithmStatus.PREPARED;
		
		result = new SchedulingResult();
	}
	
	private List<TermRecord> prepareTermData(Map<RoomData,Map<DateStamp, List<TimeSpan>>> termData){
		List<TermRecord> results = new ArrayList<TermRecord>();
		for(Map.Entry<RoomData, Map<DateStamp, List<TimeSpan>>> entry : termData.entrySet()){
			RoomData rd = entry.getKey();
			for(Map.Entry<DateStamp, List<TimeSpan>> dateStampEntry : entry.getValue().entrySet()){
				DateStamp ds = dateStampEntry.getKey();
				for(TimeSpan ts : dateStampEntry.getValue()){
					TermRecord tr = new TermRecord(ds.getStamp(), ts.getStart().toString(), ts.getEnd().toString(), rd.getId(), rd.getCapacity());
					results.add(tr);
				}
			}
		}
		return results;
	}
	
	private List<String> prepareJmbagData(Map<String,Map<DateStamp, List<TimeSpan>>> peopleData){
		return new ArrayList<String>(peopleData.keySet());
	}
	
	private List<String> prepareOccupancyData(Map<String,Map<DateStamp, List<TimeSpan>>> peopleData){
		List<String> results = new ArrayList<String>();
		for(Map.Entry<String, Map<DateStamp, List<TimeSpan>>> entry : peopleData.entrySet()){
			String jmbag = entry.getKey();
			for(Map.Entry<DateStamp, List<TimeSpan>> dateStampEntry : entry.getValue().entrySet()){
				DateStamp ds = dateStampEntry.getKey();
				for(TimeSpan ts : dateStampEntry.getValue()){
					results.add(jmbag+";"+ds.getStamp()+";"+ts.getStart().toString()+";"+ts.getEnd().toString()+";1");
				}
			}
		}
		return results;
	}
	
	public static Set<Set<TermRecord>> prepareTermRecordCombinations(int TermRecordNumber, List<TermRecord> TermRecordData ){
		
		Set<Set<TermRecord>> initialSet = new HashSet<Set<TermRecord>>();
		for(TermRecord t : TermRecordData) {
			Set<TermRecord> tmp = new HashSet<TermRecord>();
			tmp.add(t); 
			initialSet.add(tmp);
		}
		
		if(TermRecordNumber==1) return initialSet;
		
		Set<Set<TermRecord>> result = initialSet;
		for(int i = 0; i<TermRecordNumber-1; i++){
			result = cartesian(TermRecordData,result);
		}
		
//		printTermRecordCombinations(result);
		
		return result;
	}
	
	public static Set<Set<TermRecord>> cartesian(List<TermRecord> set1, Set<Set<TermRecord>> set2){
		Set<Set<TermRecord>> result = new HashSet<Set<TermRecord>>();
		for(Set<TermRecord> s2: set2) {
			for(TermRecord s1 : set1){
				Set<TermRecord> tmp = new HashSet<TermRecord>(s2);
				if(tmp.add(s1)) result.add(tmp);
			}
		}
		return result;
	}
	
	public static List<String> prepareListFromSet(Set<TermRecord> comb){
		List<String> result = new ArrayList<String>();
		for(TermRecord t : comb) result.add(t.toString());
		return result;
	}

	
	
	public static void execute(List<String> rawTermList, List<String> rawJmbagList, List<String> rawOccupancyData, String eventName) throws IOException {
		
		log("execute()  - " + Thread.currentThread().getName());
		System.out.println("execute()  - " + Thread.currentThread().getName());
		
		List<Term> termList = loadTermList(rawTermList);
		List<String> jmbags = rawJmbagList;
		Map<String,List<Occupance>> occupance = loadOccupance(rawOccupancyData);
		
		Term[] terms = new Term[termList.size()];
		termList.toArray(terms);
		int[] sizes = new int[termList.size()];
		Arrays.fill(sizes, 0);

		Allocation[] allocations = new Allocation[jmbags.size()];
		int totalSize = 0;
		
		for(Term t : termList) {
			totalSize += t.capacity;
		}
		
		Random r = new Random();
		Collections.shuffle(jmbags, r);

		double factor = (double)jmbags.size()/(double)totalSize;
		int allocationIndex = 0;
		int studentIndex = 0;
		int termIndex = -1;
		for(Term t : termList) {
			termIndex++;
			int n = (int)(factor * t.capacity);
			while(studentIndex<jmbags.size() && n>0) {
				String jmbag = jmbags.get(studentIndex);
				allocations[allocationIndex] = new Allocation(jmbag,occupance.get(jmbag),termIndex);
				studentIndex++;
				allocationIndex++;
				sizes[termIndex]++; // tu sam dodao jednog studenta
				n--;
			}
		}

		while(studentIndex<jmbags.size()) {
			boolean anyChange = false;
			for(termIndex=0; termIndex<terms.length; termIndex++) {
				Term t = terms[termIndex];
				if(sizes[termIndex]<t.capacity-1) {
					// mogu uzeti jos jednoga
					String jmbag = jmbags.get(studentIndex);
					allocations[allocationIndex] = new Allocation(jmbag,occupance.get(jmbag),termIndex);
					studentIndex++;
					allocationIndex++;
					sizes[termIndex]++; // tu sam dodao jednog studenta
					anyChange = true;
					if(studentIndex>=jmbags.size()) break;
				}
			}
			if(!anyChange) {
				log("Nije moguce napraviti niti inicijalnu raspodjelu.");
				return;
			}
		}
		
		int[] histogramTermini = new int[terms.length];
		int[] histogram = new int[terms.length+1];
		Arrays.fill(histogram, 0);
		Arrays.fill(histogramTermini, 0);
		boolean[][] possibleTerms = new boolean[allocations.length][];
		for(int i = 0; i < possibleTerms.length; i++) {
			possibleTerms[i] = new boolean[terms.length];
			int brojMogucih = 0;
			for(int t = 0; t < terms.length; t++) {
				possibleTerms[i][t] = canOccupyTerm(allocations[i],terms[t]);
				if(possibleTerms[i][t]) brojMogucih++;
				if(possibleTerms[i][t]) histogramTermini[t]++;
			}
			histogram[brojMogucih]++;
			if(brojMogucih==0 && histogram[brojMogucih]<=10) {
				log("Primjer studenta: "+allocations[i].jmbag);
			}
		}
		
		for(int k=0; k<histogram.length; k++) {
			log("Broj mogucih "+k+" - broj studenata "+histogram[k]);
		}
		log("=====================================");
		for(int k=0; k<histogramTermini.length; k++) {
			log("Termin "+k+" - broj mogucih studenata "+histogramTermini[k]);
		}
		log("=====================================");
		try { Thread.sleep(2000); } catch(Exception ex) {}
		//System.exit(0);

		int conflictedCount = 0;
		for(int i = 0; i < allocations.length; i++) {
			allocations[i].studentIndex = i;
			allocations[i].conflicted = !possibleTerms[i][allocations[i].termIndex];
			if(allocations[i].conflicted) conflictedCount++;
		}
		
		Integer[] icache = new Integer[allocations.length];
		for(int i = 0; i < icache.length; i++) {
			icache[i] = Integer.valueOf(i);
		}

		List<Integer> conflictedList = new ArrayList<Integer>(allocations.length);
		List<Integer> nonconflictedList = new ArrayList<Integer>(allocations.length);
		for(int i = 0; i < allocations.length; i++) {
			if(allocations[i].conflicted) {
				conflictedList.add(icache[i]);
			} else {
				nonconflictedList.add(icache[i]);
			}
		}
		
		int cconfcount = conflictedList.size();
		log("Inicijalni broj konflikata: "+conflictedCount);
		List<Integer> tmpList = new ArrayList<Integer>(allocations.length);
		List<Integer> availableTerms = new ArrayList<Integer>(terms.length);
		
		int iterationNumber=0;
		
outer:	while(conflictedList.size()>0) {		  
	
			iterationNumber++;
			if(iterationNumber>100000){
				status=SchedulingAlgorithmStatus.FAILURE;
				return;
			}
			
			if(Thread.interrupted()) return; 
			
			if(cconfcount!=conflictedList.size()) {
				cconfcount=conflictedList.size();
				StringBuilder sb = new StringBuilder(200);
				sb.append("Novi broj konflikata: ").append(cconfcount).append(" (").append(conflictedList.size()).append("/").append(nonconflictedList.size()).append("=").append((conflictedList.size()+nonconflictedList.size())).append(")");
				for(int k=0; k<sizes.length; k++) {
					sb.append(" ").append(sizes[k]);
				}
				log(sb.toString());
//				System.out.flush();
				try { Thread.sleep(10); } catch(Exception ex) {}
			}																				
			int x = r.nextInt(100);
			boolean useConflicted = x > -30;
			if(useConflicted) {																
				int index = r.nextInt(conflictedList.size());
				Integer conIndex = conflictedList.get(index);
				conflictedList.set(index,conflictedList.get(conflictedList.size()-1));
				conflictedList.remove(conflictedList.size()-1);
				// Izvadio sam konfliktnog van
				index = conIndex.intValue();
				Allocation s1 = allocations[index];

				if(r.nextInt(100)<=20) {
					// pokusaj ga preseliti u neki termin, ako tamo ima mjesta
					availableTerms.clear();													
					for(int j = 0; j < terms.length; j++) {
						if(sizes[j]<terms[j].capacity) {
							availableTerms.add(icache[j]);											
						}
					}
					Collections.shuffle(availableTerms);
					for(int j = 0; j < availableTerms.size(); j++) {
						int termInd = availableTerms.get(j).intValue();											
						// moze li student tamo?
						if(possibleTerms[s1.studentIndex][termInd]) {
							// moze!
							sizes[s1.termIndex]--;
							sizes[termInd]++;
							s1.termIndex = termInd;
							s1.conflicted = false;
							nonconflictedList.add(icache[s1.studentIndex]);
							continue outer;
						}
					}
				}

				tmpList.clear();
				tmpList.addAll(conflictedList);
				tmpList.addAll(nonconflictedList);
				Collections.shuffle(tmpList, r);
				boolean rijesio = false;
				for(int i = 0; i < tmpList.size(); i++) {
					int aindex = tmpList.get(i).intValue();
					Allocation s2 = allocations[aindex];
					if(s1.termIndex==s2.termIndex) continue;
					if(possibleTerms[s1.studentIndex][s2.termIndex]) {
						// s1 moze u s2
						if(!possibleTerms[s2.studentIndex][s1.termIndex]) {
							// ako s2 ne bi mogao u s1
							int y = r.nextInt(100);
							if(y>20) continue; // tada u 80% slucajeva ipak preskoci ovakvu zamjenu...
							// inace ih ipak zamijeni
							int s1tindex = s1.termIndex;
							int s2tindex = s2.termIndex;
							s1.termIndex = s2tindex;
							s2.termIndex = s1tindex;
							s1.conflicted = false;
							if(!s2.conflicted) {
								int pos = nonconflictedList.indexOf(icache[s2.studentIndex]);
								nonconflictedList.set(pos,nonconflictedList.get(nonconflictedList.size()-1));
								nonconflictedList.remove(nonconflictedList.size()-1);
								conflictedList.add(icache[s2.studentIndex]);
							}
							s2.conflicted = true;
							nonconflictedList.add(icache[s1.studentIndex]);
							rijesio = true;
							break;
						} else {
							// s2 isto moze u s1
							int s1tindex = s1.termIndex;
							int s2tindex = s2.termIndex;
							s1.termIndex = s2tindex;
							s2.termIndex = s1tindex;
							s1.conflicted = false;
							if(s2.conflicted) {
								int pos = conflictedList.indexOf(icache[s2.studentIndex]);
								conflictedList.set(pos,conflictedList.get(conflictedList.size()-1));
								conflictedList.remove(conflictedList.size()-1);
								nonconflictedList.add(icache[s2.studentIndex]);
							}
							s2.conflicted = false;
							nonconflictedList.add(icache[s1.studentIndex]);
							rijesio = true;
							break;
						}
					} else {
						//conflictedList.add(icache[s1.studentIndex]);
					}
				}
				if(!rijesio) {
					conflictedList.add(icache[s1.studentIndex]);
				}
			} else {
				//int index = r.nextInt(nonconflictedList.size());
			}
		}
		cconfcount=conflictedList.size();
		log("Konacni broj konflikata: "+cconfcount);
		
//		System.out.println();
		log("Zavrsna provjera:");
		
		conflictedCount = 0;
		for(int i = 0; i < allocations.length; i++) {
			allocations[i].conflicted = !possibleTerms[i][allocations[i].termIndex];
			if(allocations[i].conflicted) conflictedCount++;
		}

		log("Broj konflikata: "+conflictedCount);
		log("\nRaspored:");

//		BufferedWriter bw = new BufferedWriter(new FileWriter(Thread.currentThread().getName()+".txt"));

		for(int ti = 0; ti < terms.length; ti++) {
			Term t = terms[ti];
			
			log("\nTermin: "+t);
			result.addTerm(eventName, t.toString(), t.getRoom(), t.getCapacity(), t.getDate(), t.getFromOffset(), t.getToOffset());
//			bw.write("Termin: "+t+"\r\n");
			
			int index = 0; 
			for(int i = 0; i < allocations.length; i++) {
				Allocation s = allocations[i];
				if(s.termIndex!=ti) continue;
				index++;
				
				log(index+". "+s.jmbag);
				result.addStudentToTerm(eventName, t.toString(), s.jmbag);
//				bw.write(s.jmbag+"\r\n");
				
			}			
//			bw.write("\r\n");		
		}
//		bw.close();
		
		status=SchedulingAlgorithmStatus.SUCCESS;
	}

	private static boolean canOccupyTerm(Allocation allocation, Term term) {
		if(allocation.occupance==null || allocation.occupance.isEmpty()) return true;
		for(Occupance o : allocation.occupance) {
			if(!o.date.equals(term.date)) continue;
			if(o.fromOffset >= term.getToOffset()) continue;
			if(o.toOffset <= term.getFromOffset()) continue;
			return false; // Nasao sam preklapanje
		}
		return true;
	}

	static class Allocation {
		String jmbag;
		int termIndex;
		List<Occupance> occupance;
		int studentIndex;
		boolean conflicted;
		
		public Allocation(String jmbag, List<Occupance> occupance, int termIndex) {
			super();
			this.jmbag = jmbag;
			this.occupance = occupance;
			this.termIndex = termIndex;
		}
	}
	
	private static Map<String, List<Occupance>> loadOccupance(List<String> rawOccupancyData) throws IOException {
		System.out.println("loadOccupance()  - " + Thread.currentThread().getName());
		Map<String,List<Occupance>> result = new HashMap<String,List<Occupance>>();
		for(String line : rawOccupancyData){
			String[] elems = line.split(";");
			String jmbag = elems[0];
			String date = elems[1];
			String from = elems[2];
			String to = elems[3];
			List<Occupance> list = result.get(jmbag);
			if(list==null) {
				list = new ArrayList<Occupance>();
				result.put(jmbag, list);
			}
			list.add(new Occupance(date,calcOffset(from),calcOffset(to)));
		}
		return result;
	}

	private static List<Term> loadTermList(List<String> rawTermList) throws IOException {
		System.out.println("loadTermList()  - " + Thread.currentThread().getName());
		List<Term> result = new ArrayList<Term>();
		for(String line : rawTermList){
			String[] elems = line.split(";");
			result.add(new Term(elems[0],calcOffset(elems[1]),calcOffset(elems[2]),elems[3],Integer.parseInt(elems[4])));
		}
		return result;
	}

	protected static int calcOffset(String time) {
		return Integer.parseInt(time.substring(0,2))*60+Integer.parseInt(time.substring(3,5));
	}
	
	static class Term {
		String date; // date in format 2008-10-23
		int fromOffset; // minute offset
		int toOffset; // minute offset
		String room; // room name
		int capacity; // capacity
		
		@Override
		public String toString() {
			return date+" "+intToTime(fromOffset)+"-"+intToTime(toOffset)+" "+room+" ("+capacity+")";
		}
		
		public Term(String date, int fromOffset, int toOffset, String room, int capacity) {
			super();
			this.date = date;
			this.fromOffset = fromOffset;
			this.toOffset = toOffset;
			this.room = room;
			this.capacity = capacity;
		}
		public String getDate() {
			return date;
		}
		public int getFromOffset() {
			return fromOffset;
		}
		public int getToOffset() {
			return toOffset;
		}
		public String getRoom() {
			return room;
		}
		public int getCapacity() {
			return capacity;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((date == null) ? 0 : date.hashCode());
			result = prime * result + fromOffset;
			result = prime * result + ((room == null) ? 0 : room.hashCode());
			result = prime * result + toOffset;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Term other = (Term) obj;
			if (date == null) {
				if (other.date != null)
					return false;
			} else if (!date.equals(other.date))
				return false;
			if (fromOffset != other.fromOffset)
				return false;
			if (room == null) {
				if (other.room != null)
					return false;
			} else if (!room.equals(other.room))
				return false;
			if (toOffset != other.toOffset)
				return false;
			return true;
		}
	}

	static class Occupance {
		String date; // date in format 2008-10-23
		int fromOffset; // minute offset
		int toOffset; // minute offset
		
		public Occupance(String date, int fromOffset, int toOffset) {
			super();
			this.date = date;
			this.fromOffset = fromOffset;
			this.toOffset = toOffset;
		}
		public String getDate() {
			return date;
		}
		public int getFromOffset() {
			return fromOffset;
		}
		public int getToOffset() {
			return toOffset;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((date == null) ? 0 : date.hashCode());
			result = prime * result + fromOffset;
			result = prime * result + toOffset;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Term other = (Term) obj;
			if (date == null) {
				if (other.date != null)
					return false;
			} else if (!date.equals(other.date))
				return false;
			if (fromOffset != other.fromOffset)
				return false;
			if (toOffset != other.toOffset)
				return false;
			return true;
		}
	}

	private static String intToTime(int time) {
		int hours = time / 60;
		int minutes = time - hours*60;
		StringBuilder sb = new StringBuilder();
		if(hours<10) sb.append('0');
		sb.append(hours);
		sb.append(':');
		if(minutes<10) sb.append('0');
		sb.append(minutes);
		return sb.toString();
	}

//	static class TermRecordCombination{
//	
//		List<TermRecord> dataSet;
//		int periodDistanceSum = 0;
//		
//		@SuppressWarnings("unchecked")
//		public TermRecordCombination(Set<TermRecord> set){
//			this.dataSet= new ArrayList(set);
//			Collections.sort(dataSet);
//			calculateSum();
//		}
//		
//		private void calculateSum(){
//			Iterator<TermRecord> i = dataSet.iterator();
//			while(i.hasNext()){
//				TermRecord tr = i.next();
//				
//			}
//		}
//		
//	}
	
	
	
	static class TermRecord implements Comparable<TermRecord>{
		DateStamp dateStamp;
		TimeStamp timeFromStamp;
		TimeStamp timeToStamp;
		String room;
		int capacity;

		public TermRecord(String date, String timeFrom, String timeTo, String room, int capacity){
			this.dateStamp=new DateStamp(date);
			this.timeFromStamp=new TimeStamp(timeFrom);
			this.timeToStamp=new TimeStamp(timeTo);
			this.room=room;
			this.capacity = capacity;
		}

		public String toString(){
			return dateStamp.toString()+";"+timeFromStamp.toString()+";"+timeToStamp.toString()+";"+room+";"+capacity;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dateStamp == null) ? 0 : dateStamp.hashCode());
			result = prime * result + ((timeFromStamp == null) ? 0 : timeFromStamp.hashCode());
			result = prime * result + ((timeToStamp == null) ? 0 : timeToStamp.hashCode());
			result = prime * result + ((room == null) ? 0 : room.hashCode());
			result = prime * result + capacity;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TermRecord other = (TermRecord) obj;
			
			if (dateStamp == null) {
				if (other.dateStamp != null)
					return false;
			} else if (!dateStamp.equals(other.dateStamp))
				return false;
			
			if (timeFromStamp == null) {
				if (other.timeFromStamp != null)
					return false;
			} else if (!timeFromStamp.equals(other.timeFromStamp))
				return false;
			
			if (timeToStamp == null) {
				if (other.timeToStamp != null)
					return false;
			} else if (!timeToStamp.equals(other.timeToStamp))
				return false;
			
			if (room == null) {
				if (other.room != null)
					return false;
			} else if (!room.equals(other.room))
				return false;
			
			if (capacity != other.capacity)
				return false;
			
			return true;
		}

		@Override
		public int compareTo(TermRecord o) {
			if (dateStamp.compareTo(o.dateStamp)!=0) 
				return dateStamp.compareTo(o.dateStamp);
			else if(timeFromStamp.compareTo(o.timeFromStamp)!=0)
				return timeFromStamp.compareTo(o.timeFromStamp);
			else if(timeToStamp.compareTo(o.timeToStamp)!=0)
				return timeToStamp.compareTo(o.timeToStamp);
			return 0;
		}
	}

	@Override
	public ISchedulingResult[] getResults() throws SchedulingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void step() throws SchedulingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void use(ISchedulingResult result) throws SchedulingException {
		throw new UnsupportedOperationException();
	}



	
//	public static void printTermRecordCombinations(Set<Set<TermRecord>> l){
//	System.out.println("printing collection size " + l.size());
//	for(Set<TermRecord> set : l) {
////		System.out.println("printing subcollection size " + set.size());
//		System.out.println(printSet(set));
//	}
//}

//public static String printSet(Set<TermRecord> s1){
//	String result ="";
//	for(TermRecord s : s1) result+=s.toString()+"  ";
//	return result;
//}

}
