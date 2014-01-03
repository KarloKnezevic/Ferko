package hr.fer.zemris.util.scheduling.algorithms.CLONALG;

import hr.fer.zemris.jcms.model.planning.Definition;
import hr.fer.zemris.util.scheduling.algorithms.MainScheduler;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.ReservationManager2;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.SchedulingResult;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEventDistribution;
import hr.fer.zemris.util.scheduling.support.algorithmview.ILocationParameter;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Antibody implements Comparable<Antibody> {
	
	
	private String planName;
	private Map<String, List<Term>> terms;
	private Map<String, List<Student>> students;
	
	private static final int numberOfHardConstraints = 0;
	private static final int penaltyStep = 10;
	private int[] fitnessVector = new int[6];
	
	public boolean studentiNaRaziniTermina=false;
	private Random randomGenerator = new Random();
	private int[] studentConflicts;
	
	public Antibody() {
		terms=new HashMap<String, List<Term>>();
		students=new HashMap<String, List<Student>>();
	}
	
	public Antibody(Map<String, String[]> availableStudentsForEvent, Map<String, ITerm[]> availableTermsForEvent, Map<String, Map<RoomData, Map<DateStamp, List<TimeSpan>>>> availableResourcesForEvent) {
		terms=new HashMap<String, List<Term>>();
		students=new HashMap<String, List<Student>>();
		setPlan(MainScheduler.plan.getName());
		int c=1;
		for(IEvent e:MainScheduler.plan.getPlanEvents()) {
			String[] eStudents = availableStudentsForEvent.get(e.getId());
			if(eStudents!=null) {
				List<String> t1 = Arrays.asList(eStudents);
				Collections.shuffle(t1, randomGenerator);
				eStudents=t1.toArray(new String[1]);
			}
			ITerm[] eTerms;
			if(e.getEventDistribution().getType()==Definition.RANDOM_DISTRIBUTION) {
				IEventDistribution d = e.getEventDistribution();
				int numberOfTerms = 0;
				if(d.getMinimumTermNumber()==d.getMaximumTermNumber())
					numberOfTerms=d.getMaximumTermNumber();
				else
					numberOfTerms=d.getMinimumTermNumber() + randomGenerator.nextInt(d.getMaximumTermNumber()-d.getMinimumTermNumber());
				eTerms=new ITerm[numberOfTerms];
				for(int j=0;j<numberOfTerms;j++) {
					eTerms[j] = new Term(e.getId()+"."+c,"Termin"+j);
					c++;
				}
			}
			else {
				eTerms=availableTermsForEvent.get(e.getId());
			}
			int fromIndex=0;
			int processedTerms=0;
			for(ITerm t:eTerms) {
				String[] termStudents = availableStudentsForEvent.get(t.getId());
				RoomData[] termRooms = null;
				Map<RoomData, Map<DateStamp,List<TimeSpan>>> m = availableResourcesForEvent.get(t.getId());
				if(m!=null) {
					termRooms = new RoomData[m.keySet().size()];
					m.keySet().toArray(termRooms);
				}
				if(termStudents==null) {
					int numberOfStudents=(eStudents.length-fromIndex)/(eTerms.length-processedTerms);
					int toIndex = Math.min(eStudents.length, fromIndex+numberOfStudents);
					termStudents = Arrays.copyOfRange(eStudents, fromIndex, toIndex);
					fromIndex+=numberOfStudents;
					processedTerms++;
				}
				if(termRooms==null) {
					m = availableResourcesForEvent.get(e.getId());
					termRooms=new RoomData[m.keySet().size()];
					m.keySet().toArray(termRooms);
				}
				RoomData selectedRoom = termRooms[randomGenerator.nextInt(termRooms.length)];
				List<DateStamp> dateStampList = new ArrayList<DateStamp>(m.get(selectedRoom).keySet());
				Collections.shuffle(dateStampList,randomGenerator);
				DateStamp selectedDate = dateStampList.get(0);
				List<TimeSpan> timeSpanList = new ArrayList<TimeSpan>(m.get(selectedRoom).get(selectedDate));
				Collections.shuffle(timeSpanList);
				TimeSpan selectedTimeSpan = timeSpanList.get(0);
				int startTime = selectedTimeSpan.getStart().getAbsoluteTime()/15;
				if(selectedTimeSpan.getEnd().getAbsoluteTime()-selectedTimeSpan.getStart().getAbsoluteTime()>e.getTermDuration()) {
					startTime += randomGenerator.nextInt((selectedTimeSpan.getEnd().getAbsoluteTime()-selectedTimeSpan.getStart().getAbsoluteTime()-e.getTermDuration())/15);
				}
				int endTime = startTime + e.getTermDuration()/15;
				Term t1 = new Term(t.getId(), t.getName());
				t1.setDateStamp(selectedDate);
				t1.setRoom(selectedRoom);
				t1.setTimeSpan(new TimeSpan(new TimeStamp(startTime*15), new TimeStamp(endTime*15)));
				t1.setDuration(e.getTermDuration());
				addTermToEvent(e.getId(),t1);
				for(String s:termStudents) {
					addStudentToTerm(t1, s);
				}
			}
		}
	}
	
	
	public void rebuildManager(ReservationManager2 manager) {
		for(IEvent e:MainScheduler.plan.getPlanEvents()) {
			for(Term t:terms.get(e.getId())) {
				manager.reserveRoom(t.getRoom().getId(), t.getDate(), t.getTimeSpan());
				for(Student s:students.get(t.getId())) {
					manager.reserveStudent(s.jmbag, t.getDate(), t.getTimeSpan());
				}
			}
		}
	}
	
	private void setTerms(Map<String, List<Term>> terms) {
		this.terms=new HashMap<String, List<Term>>();
		for(String s:terms.keySet()) {
			this.terms.put(s, new ArrayList<Term>());
			for(Term t:terms.get(s)) {
				this.terms.get(s).add((Term) t.clone());
			}
		}
	}
	
	private void setStudents(Map<String, List<Student>> students) {
		this.students=new HashMap<String, List<Student>>();
		for(String s:students.keySet()) {
			this.students.put(s, new ArrayList<Student>());
			for(Student s1:students.get(s)) {
				this.students.get(s).add(new Student(s1.jmbag));
			}
		}
	}
	
	protected Object clone() {
		Antibody ret = new Antibody();
		ret.setPlan(planName);
		ret.setTerms(terms);
		ret.setStudents(students);
		ret.setFitnessVector(fitnessVector);
		ret.setAllStudents(studentConflicts);
		return ret;
	}
	
	private void setAllStudents(int[] allStudents) {
		this.studentConflicts=Arrays.copyOf(allStudents, allStudents.length);
	}


	private void setFitnessVector(int[] fitnessVector) {
		this.fitnessVector=Arrays.copyOf(fitnessVector, fitnessVector.length);
	}

	public void setPlan(String planName) {
		this.planName=planName;
	}
	
	public ISchedulingResult toResult() {
		SchedulingResult result = new SchedulingResult();
		result.addPlan(planName);
		for(IEvent e:MainScheduler.plan.getPlanEvents()) {
			result.addEvent(e.getName(),e.getId());
			for(Term t:terms.get(e.getId())) {
				result.addTerm(e.getName(), t.getName(), t.getRoom().getId(), t.getRoom().getCapacity(), t.getDate().getStamp(), t.getTimeSpan().getStart().getAbsoluteTime(), t.getTimeSpan().getEnd().getAbsoluteTime());
				for(Student s:students.get(t.getId())) {
					result.addStudentToTerm(e.getName(), t.getName(), s.jmbag);
				}
			}
		}
		return result;
	}
	
	public void addTermToEvent(String eventId, Term term) {
		if(terms.get(eventId)==null)
			terms.put(eventId, new ArrayList<Term>());
		terms.get(eventId).add(term);
	}
	
	public void addStudentToTerm(Term term, String JMBAG) {
		if(students.get(term.getId())==null)
			students.put(term.getId(), new ArrayList<Student>());
		students.get(term.getId()).add(new Student(JMBAG));
	}
	
	@Override
	public String toString() {
		String out="";
		for(IEvent e : MainScheduler.plan.getPlanEvents()) {
			out += e.getId() + "\n\n";
			for(Term t:terms.get(e.getId())) {
				out += t.getName() + "/" + t.getRoom().getName() + "(" + t.getRoom().getCapacity() + ")/" + t.getDate().getStamp() + "/" + t.getTimeSpan().toString() + "\n";
				for(Student s:students.get(t.getId())) {
					out += s.jmbag + " (" + studentConflicts[MainScheduler.jmbagsCache.translate(s.jmbag)] + ")\n";
				}
				out += students.get(t.getId()).size() + "\n\n";
			}
			out += "-----------------------------------------";
			out += "\n\n";
		}
		return out;
	}
	
	public void evaluate(ReservationManager2 manager) {
		Arrays.fill(fitnessVector,0);
		for(IEvent e:MainScheduler.plan.getPlanEvents()) {
			fitnessVector[0] += countWrongPreconditionsForEvent(e);
		}
		fitnessVector[1]=manager.countConflictsForRooms();
		fitnessVector[2]=countConflictsForStudents(manager);
		countOvercrowdedRooms();
	}

	private int countConflictsForStudents(ReservationManager2 manager) {
		int ret=0;
		studentConflicts=new int[MainScheduler.jmbagsCache.size()];
		for(int i=0;i<MainScheduler.jmbagsCache.size();i++) {
			int conflicts=manager.countConflictsForStudent(i);
			ret+=conflicts;
			studentConflicts[i]=conflicts;
		}
		return ret;
	}
	private void countOvercrowdedRooms() {
		for(String eventId:terms.keySet()) {
			for(Term t:terms.get(eventId)) {
				fitnessVector[4]++;
				if(t.getRoom().getCapacity()<students.get(t.getId()).size())
					fitnessVector[3]++;
				else
					fitnessVector[5]+=t.getRoom().getCapacity()-students.get(t.getId()).size();
			}
		}
	}
	
	private int countWrongPreconditionsForEvent(IEvent event) {
		int count=0;
		for(IPrecondition p:event.getPreconditionEvents()) {
			for(Term t1:terms.get(event.getId())) {
				for(Term t2:terms.get(p.getEvent().getId())) {
					if(p.getTimeDistanceValue()<15) {
						if(DateStamp.dateDiff(t1.getDate(), t2.getDate())<p.getTimeDistanceValue())
							count++;
					}
					else {
						if(t1.getTimeSpan().getEnd().getAbsoluteTime()-t2.getTimeSpan().getStart().getAbsoluteTime()<p.getTimeDistanceValue())
							count++;
					}
					
				}
			}
		}
		return count;
	}
	
	public int[] getFitnessVector() {
		return fitnessVector;
	}
	
	public void mutate(Map<String, Map<RoomData, Map<DateStamp, List<TimeSpan>>>> availableResourcesForEvent) {
		List<IEvent> events = MainScheduler.plan.getPlanEvents();
		IEvent event = events.get(randomGenerator.nextInt(events.size()));
		List<Term> termini = terms.get(event.getId());
		Term term = termini.get(randomGenerator.nextInt(termini.size()));
		if(!studentiNaRaziniTermina) {
			if(randomGenerator.nextBoolean()) {
				Term term2=termini.get(randomGenerator.nextInt(termini.size()));
				List<Student> s1 = students.get(term.getId());
				List<Student> s2 = students.get(term2.getId());
				int num1 = randomGenerator.nextInt(s1.size());
				int num2 = randomGenerator.nextInt(s2.size());
				Collections.sort(s1);
				Collections.sort(s2);
				for(int i=0;i<num1;i++)
					s2.add(s1.remove(0));
				for(int i=0;i<num2;i++)
					s1.add(s2.remove(0));
			}
			else {
				changeTerm(event, term, availableResourcesForEvent);
			}
		}
		else {
			changeTerm(event, term, availableResourcesForEvent);
		}
	}
	
//	private void swapStudents(Term term1, Term term2) {
//		List<Student> students1 = students.get(term1.getId());
//		List<Student> students2 = students.get(term2.getId());
//		int size = Math.min(students1.size(), students2.size());
//		int idx1=randomGenerator.nextInt(size);
//		int idx2=randomGenerator.nextInt(size);
//		Student s1=students1.get(idx1);
//		Student s2=students2.get(idx2);
//		students1.set(idx1, s2);
//		students2.set(idx2, s1);
//	}
	
	private void changeTerm(IEvent event, Term term, Map<String, Map<RoomData, Map<DateStamp, List<TimeSpan>>>> availableResourcesForEvent) {
		Map<RoomData, Map<DateStamp, List<TimeSpan>>> termData = availableResourcesForEvent.get(term.getId());
		if(termData==null)
			termData=availableResourcesForEvent.get(event.getId());
		List<RoomData> availableRooms = new ArrayList<RoomData>(termData.keySet());
		RoomData selectedRoom = availableRooms.get(randomGenerator.nextInt(availableRooms.size()));
		List<DateStamp> availableDates = new ArrayList<DateStamp>(termData.get(selectedRoom).keySet());
		DateStamp selectedDate = availableDates.get(randomGenerator.nextInt(availableDates.size()));
		List<TimeSpan> availableTimeSpans = new ArrayList<TimeSpan>(termData.get(selectedRoom).get(selectedDate));
		if(availableTimeSpans==null) return;
		TimeSpan selectedTimeSpan = availableTimeSpans.get(randomGenerator.nextInt(availableTimeSpans.size()));
		int startTime = selectedTimeSpan.getStart().getAbsoluteTime()/15;
		if(selectedTimeSpan.getEnd().getAbsoluteTime()-selectedTimeSpan.getStart().getAbsoluteTime()>event.getTermDuration()) {
			startTime += randomGenerator.nextInt((selectedTimeSpan.getEnd().getAbsoluteTime()-selectedTimeSpan.getStart().getAbsoluteTime()-event.getTermDuration())/15);
		}
		int endTime = startTime + event.getTermDuration()/15;
		TimeSpan ts = new TimeSpan(new TimeStamp(startTime*15), new TimeStamp(endTime*15));
		term.setDateStamp(selectedDate);
		term.setRoom(selectedRoom);
		term.setTimeSpan(ts);
	}

	@Override
	public int compareTo(Antibody o) {
		int[] fitnessVector1 = this.getFitnessVector();
		int[] fitnessVector2 = o.getFitnessVector();
		for(int i=0;i<numberOfHardConstraints;i++) {
			if(fitnessVector1[i]<fitnessVector2[i])
				return -1;
			if(fitnessVector1[i]>fitnessVector2[i])
				return 1;
		}
		int penaltySum1=0;
		int penaltySum2=0;
		for(int i=numberOfHardConstraints;i<fitnessVector1.length;i++) {
			penaltySum1+=fitnessVector1[i]*Math.pow(penaltyStep, fitnessVector1.length-i);
			penaltySum2+=fitnessVector2[i]*Math.pow(penaltyStep, fitnessVector1.length-i);
		}
		if(penaltySum1<penaltySum2)
			return -1;
		if(penaltySum1>penaltySum2)
			return 1;
		return 0;
	}
	
	public int getNumberOfHardConstraints() {
		return numberOfHardConstraints;
	}
	
	public boolean isAcceptable() {
		for(int i=0;i<numberOfHardConstraints;i++)
			if(fitnessVector[i]>0)
				return false;
		return true;
	}
	
	public static Antibody convertFromResult(ISchedulingResult result) {
		Antibody ret = new Antibody();
		IPlan p = result.getPlan();
		ret.setPlan(p.getName());
		for(IEvent e:p.getPlanEvents()) {
			for(ITerm t:e.getTerms()) {
				Term t1=new Term(t.getId(),t.getName());
				ITimeParameter time = t.getDefinition().getTimeParameters().get(0);
				t1.setDateStamp(time.getFromDate());
				t1.setTimeSpan(new TimeSpan(time.getFromTime(), time.getToTime()));
				t1.setDuration(e.getTermDuration());
				ILocationParameter location = t.getDefinition().getLocationParameters().get(0);
				t1.setRoom(new RoomData(location.getId(), location.getName(), location.getActualCapacity()));
				ret.addTermToEvent(e.getId(), t1);
				for(String s:t.getDefinition().getIndividuals())
					ret.addStudentToTerm(t1, s);
			}
		}
		return ret;
	}

	private class Student implements Comparable<Student>{
		private String jmbag;
		private int id;
		public Student(String jmbag) {
			this.jmbag=jmbag;
			this.id=MainScheduler.jmbagsCache.translate(jmbag);
		}
		
		
		@Override
		public int compareTo(Student o) {
			if(studentConflicts[this.id]<studentConflicts[o.id])
				return 1;
			if(studentConflicts[this.id]>studentConflicts[o.id])
				return -1;
			return this.jmbag.compareTo(o.jmbag);
		}
		
		@Override
		public boolean equals(Object obj) {
			return this.jmbag.equals(((Student)obj).jmbag);
		}
		
	}
}
