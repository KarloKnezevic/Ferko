package hr.fer.zemris.util.scheduling.algorithms.filip_boltuzic;

import hr.fer.zemris.util.scheduling.algorithms.MainScheduler;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.ReservationManager2;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.SchedulingResult;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
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

public class PopulationMemberBCO implements Comparable<PopulationMemberBCO> {

	private int level;
	private int profitability;
	private String planName;
	private List<IEvent> events;
	private Map<String, List<TermBCO>> terms;
	private Map<String, List<Student>> students;
	
	private static final int numberOfHardConstraints = 4;
	private static final int penaltyStep = 10;
	private int[] fitnessVector = new int[6];

	public boolean studentiNaRaziniTermina = false;
	private Random randomGenerator = new Random();

	public PopulationMemberBCO() {
		events = new ArrayList<IEvent>();
		terms = new HashMap<String, List<TermBCO>>();
		students = new HashMap<String, List<Student>>();
		
	}

	public PopulationMemberBCO(ReservationManager2 fixedReservationManager) {
		events = new ArrayList<IEvent>();
		terms = new HashMap<String, List<TermBCO>>();
		students = new HashMap<String, List<Student>>();
	}

	public PopulationMemberBCO(int id) {
		this.level = id;
		events = new ArrayList<IEvent>();
		terms = new HashMap<String, List<TermBCO>>();
		students = new HashMap<String, List<Student>>();
	}

	public PopulationMemberBCO(int id,
			ReservationManager2 fixedReservationManager) {
		this.level = id;
		events = new ArrayList<IEvent>();
		terms = new HashMap<String, List<TermBCO>>();
		students = new HashMap<String, List<Student>>();
	}

	public PopulationMemberBCO(List<IEvent> events, Map<String, List<TermBCO>> terms,
			Map<String, List<Student>> students 
			) {
		this.events = events;
		this.terms = terms;
		this.students = students;
		completeData();
	}
	public void completeData(){
		List<IEvent> events = this.events;
		Map<String, List<TermBCO>> terms = this.terms;
		Map<String, List<Student>> students = this.students;
		setEvents(events);
		setTerms(terms);
		setStudents(students);
	}

	public void rebuildManager(ReservationManager2 manager) {
		for (IEvent e : events) {
			for (TermBCO t : terms.get(e.getId())) {
				manager.reserveRoom(t.getRoom().getId(), t.getDate(), t
						.getTimeSpan());
				for (Student s : students.get(t.getId())) {
					manager.reserveStudent(s.jmbag, t.getDate(), t
							.getTimeSpan());
				}
			}
		}
	}

	private void setEvents(List<IEvent> events) {
		this.events = new ArrayList<IEvent>();
		for (IEvent e : events) {
			this.events.add(e);
		}
	}

	private void setTerms(Map<String, List<TermBCO>> terms) {
		this.terms = new HashMap<String, List<TermBCO>>();
		for (String s : terms.keySet()) {
			this.terms.put(s, new ArrayList<TermBCO>());
			for (TermBCO t : terms.get(s)) {
				this.terms.get(s).add((TermBCO) t.clone());
			}
		}
	}

	private void setStudents(Map<String, List<Student>> students) {
		this.students = new HashMap<String, List<Student>>();
		for (String s : students.keySet()) {
			this.students.put(s, new ArrayList<Student>());
			for (Student s1 : students.get(s)) {
				this.students.get(s).add(new Student(s1.jmbag));
			}
		}
	}

	public Map<String, List<TermBCO>> getTerms() {
		return this.terms;
	}

	public Map<String, List<Student>> getStudents() {
		return this.students;
	}

	public void setLevel(int id) {
		this.level = id;
	}

	public int getLevel() {
		return this.level;
	}

	protected Object clone() {
		PopulationMemberBCO ret = new PopulationMemberBCO(-1);
		ret.setPlan(planName);
		ret.setEvents(events);
		ret.setTerms(terms);
		ret.setStudents(students);
		ret.setFitnessVector(fitnessVector);
		return ret;
	}

	private void setFitnessVector(int[] fitnessVector) {
		this.fitnessVector = Arrays.copyOf(fitnessVector, fitnessVector.length);
	}

	public void setPlan(String planName) {
		this.planName = planName;
	}

	public ISchedulingResult toResult() {
		SchedulingResult result = new SchedulingResult();
		result.addPlan(MainScheduler.plan.getName());
		for(IEvent e:MainScheduler.plan.getPlanEvents()) {
			result.addEvent(e.getName(),e.getId());
			for(TermBCO t:terms.get(e.getId())) {
				result.addTerm(e.getName(), t.getName(), t.getRoom().getId(), t.getRoom().getCapacity(), t.getDate().getStamp(), t.getTimeSpan().getStart().getAbsoluteTime(), t.getTimeSpan().getEnd().getAbsoluteTime());
				for(Student s:students.get(t.getId())) {
					result.addStudentToTerm(e.getName(), t.getName(), s.jmbag);
				}
			}
		}
		return result;
	}

	public void addEvent(IEvent event) {
		events.add(event);
	}

	public List<IEvent> getEvents() {
		return events;
	}

	public void addTermToEvent(String eventId, TermBCO term) {
		if (this.terms.get(eventId) == null)
			this.terms.put(eventId, new ArrayList<TermBCO>());
		this.terms.get(eventId).add(term);
	}

	public void addStudentToTerm(TermBCO term, String JMBAG) {
		if (this.students.get(term.getId()) == null)
			this.students.put(term.getId(), new ArrayList<Student>());
		this.students.get(term.getId()).add(new Student(JMBAG));
	}

	@Override
	public String toString() {
		String out = "";
		for (IEvent e : events) {
			out += e.getId() + "\n\n";
			for (TermBCO t : terms.get(e.getId())) {
				out += t.getName() + "/" + t.getRoom().getName() + "("
						+ t.getRoom().getCapacity() + ")/"
						+ t.getDate().getStamp() + "/"
						+ t.getTimeSpan().toString() + "\n";
				for (Student s : students.get(t.getId())) {
					out += s.jmbag + "\n";
				}
				out += students.get(t.getId()).size() + "\n\n";
			}
			out += "-----------------------------------------";
			out += "\n\n";
		}
		return out;
	}

	public void evaluate(ReservationManager2 manager) {
		Arrays.fill(fitnessVector, 0);
		for (IEvent e : events) {
			fitnessVector[0] += countWrongPreconditionsForEvent(e);
		}
		fitnessVector[1] = manager.countConflictsForRooms();
		fitnessVector[2] = manager.countConflictsForStudents();
		countOvercrowdedRooms();
	}

	private void countOvercrowdedRooms() {
		for (String eventId : terms.keySet()) {
			for (TermBCO t : terms.get(eventId)) {
				fitnessVector[4]++;
				if (t.getRoom().getCapacity() < students.get(t.getId()).size())
					fitnessVector[3]++;
				else
					fitnessVector[5] += t.getRoom().getCapacity()
							- students.get(t.getId()).size();
			}
		}
	}

	private int countWrongPreconditionsForEvent(IEvent event) {
		int count = 0;
		for (IPrecondition p : event.getPreconditionEvents()) {
			for (TermBCO t1 : terms.get(event.getId())) {
				for (TermBCO t2 : terms.get(p.getEvent().getId())) {
					if (DateStamp.dateDiff(t1.getDate(), t2.getDate()) < p
							.getTimeDistanceValue())
						count++;
				}
			}
		}
		return count;
	}

	public int[] getFitnessVector() {
		return fitnessVector;
	}

	public void mutate(
			Map<String, Map<RoomData, Map<DateStamp, List<TimeSpan>>>> availableResourcesForEvent) {
		IEvent event = events.get(randomGenerator.nextInt(events.size()));
		List<TermBCO> termini = terms.get(event.getId());
		TermBCO term = termini.get(randomGenerator.nextInt(termini.size()));
		if (!studentiNaRaziniTermina) {
			TermBCO term2 = termini
					.get(randomGenerator.nextInt(termini.size()));
			List<Student> s1 = students.get(term.getId());
			List<Student> s2 = students.get(term2.getId());
			int num1 = randomGenerator.nextInt(s1.size());
			int num2 = randomGenerator.nextInt(s2.size());
			Collections.shuffle(s1, randomGenerator);
			Collections.shuffle(s2, randomGenerator);
			for (int i = 0; i < num1; i++)
				s2.add(s1.remove(0));
			for (int i = 0; i < num2; i++)
				s1.add(s2.remove(0));
		}
		// if(fitnessVector[0]!=0 || fitnessVector[1]!=0)
		changeTerm(event, term, availableResourcesForEvent);
	}

	public void swapStudents(TermBCO term1, TermBCO term2) {
		List<Student> students1 = students.get(term1.getId());
		List<Student> students2 = students.get(term2.getId());
		int size = Math.min(students1.size(), students2.size());
		int idx1 = randomGenerator.nextInt(size);
		int idx2 = randomGenerator.nextInt(size);
		Student s1 = students1.get(idx1);
		Student s2 = students2.get(idx2);
		students1.set(idx1, s2);
		students2.set(idx2, s1);
	}

	public void changeTerm(
			IEvent event,
			TermBCO term,
			Map<String, Map<RoomData, Map<DateStamp, List<TimeSpan>>>> availableResourcesForEvent) {
		Map<RoomData, Map<DateStamp, List<TimeSpan>>> termData = availableResourcesForEvent
				.get(term.getId());
		if (termData == null)
			termData = availableResourcesForEvent.get(event.getId());
		List<RoomData> availableRooms = new ArrayList<RoomData>(termData
				.keySet());
		RoomData selectedRoom = availableRooms.get(randomGenerator
				.nextInt(availableRooms.size()));
		List<DateStamp> availableDates = new ArrayList<DateStamp>(termData.get(
				selectedRoom).keySet());
		DateStamp selectedDate = availableDates.get(randomGenerator
				.nextInt(availableDates.size()));
		List<TimeSpan> availableTimeSpans = new ArrayList<TimeSpan>(termData
				.get(selectedRoom).get(selectedDate));
		if (availableTimeSpans == null)
			return;
		TimeSpan selectedTimeSpan = availableTimeSpans.get(randomGenerator
				.nextInt(availableTimeSpans.size()));
		int startTime = selectedTimeSpan.getStart().getAbsoluteTime() / 15;
		if (selectedTimeSpan.getEnd().getAbsoluteTime()
				- selectedTimeSpan.getStart().getAbsoluteTime() > term
				.getDuration()) {
			startTime += randomGenerator.nextInt((selectedTimeSpan.getEnd()
					.getAbsoluteTime()
					- selectedTimeSpan.getStart().getAbsoluteTime() - term
					.getDuration()) / 15);
		}
		int endTime = startTime + term.getDuration() / 15;
		TimeSpan ts = new TimeSpan(new TimeStamp(startTime * 15),
				new TimeStamp(endTime * 15));
		term.setDateStamp(selectedDate);
		term.setRoom(selectedRoom);
		term.setTimeSpan(ts);
	}

	@Override
	public int compareTo(PopulationMemberBCO o) {
		int[] fitnessVector1 = this.getFitnessVector();
		int[] fitnessVector2 = o.getFitnessVector();
		for (int i = 0; i < numberOfHardConstraints; i++) {
			if (fitnessVector1[i] < fitnessVector2[i])
				return -1;
			if (fitnessVector1[i] > fitnessVector2[i])
				return 1;
		}
		int penaltySum1 = 0;
		int penaltySum2 = 0;
		for (int i = numberOfHardConstraints; i < fitnessVector1.length; i++) {
			penaltySum1 += fitnessVector1[i]
					* Math.pow(penaltyStep, fitnessVector1.length - i);
			penaltySum2 += fitnessVector2[i]
					* Math.pow(penaltyStep, fitnessVector1.length - i);
		}
		if (penaltySum1 < penaltySum2)
			return -1;
		if (penaltySum1 > penaltySum2)
			return 1;
		return 0;
	}

	public int getNumberOfHardConstraints() {
		return numberOfHardConstraints;
	}

	public boolean isAcceptable() {
		for (int i = 0; i < numberOfHardConstraints; i++)
			if (fitnessVector[i] > 0)
				return false;
		return true;
	}
	public int calculateProfitability() {
		int field[] = getFitnessVector();
		int base = 37;
		int ret = 0;
		for (int i = 0; i < numberOfHardConstraints; i++) {
			ret += field[i] * Math.pow(base, numberOfHardConstraints - i);
		}
		for (int i = numberOfHardConstraints; i < field.length; i++) {
			ret += field[i] * base * (i - numberOfHardConstraints);
		}
		this.profitability = ret;
		return ret;
	}
	public int getProfitability (){
		return this.profitability;
	}

	public static PopulationMemberBCO convertFromResult(ISchedulingResult result) {
		PopulationMemberBCO ret = new PopulationMemberBCO();
		List <PopulationMemberBCO> list = new ArrayList <PopulationMemberBCO> ();
		BCOScheduler.stageMap.put(ret, list);
		IPlan p = result.getPlan();
		ret.setPlan(p.getName());
		for(IEvent e:p.getPlanEvents()) {
			for(ITerm t:e.getTerms()) {
				TermBCO t1=new TermBCO(t.getId(),t.getName());
				ITimeParameter time = t.getDefinition().getTimeParameters().get(0);
				t1.setDateStamp(time.getFromDate());
				t1.setTimeSpan(new TimeSpan(time.getFromTime(), time.getToTime()));
				t1.setDuration(e.getTermDuration());
				ILocationParameter location = t.getDefinition().getLocationParameters().get(0);
				t1.setRoom(new RoomData(location.getId(), location.getName(), location.getActualCapacity()));
				ret.addTermToEvent(e.getId(), t1);
				for(String s:t.getDefinition().getIndividuals()){
					ret.addStudentToTerm(t1, s);
					PopulationMemberBCO mem = new PopulationMemberBCO();
					mem = (PopulationMemberBCO)ret.clone();
					list.add(mem);					
				}
			}
		}
		return ret;
	}
	 

}
