package hr.fer.zemris.util.scheduling.algorithms.sds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.ReservationManager2;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.SchedulingResult;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;

public class SDSAgent implements Comparable<SDSAgent> {

	private static final int numberOfHardConstraints = 4;
	private static final int penaltyStep = 10;
	// private ReservationManager2 manager;
	private Map<String, Integer> type = new HashMap<String, Integer>();
	private int termNumberInEachEvent;
	private int id;
	private String planName;
	private Map<IEvent, List<SDSTerm>> events = new HashMap<IEvent, List<SDSTerm>>();
	private Map<ITerm, Boolean> studentActivity = new HashMap<ITerm, Boolean>();
	private Map<IEvent, Boolean> termActivity = new HashMap<IEvent, Boolean>();
	private Map<IEvent, Boolean> termNumberActivity = new HashMap<IEvent, Boolean>();
	private int[] fitnessVector = new int[6];

	private String[] fitnessLabels = new String[] {
			"Unsatisfied preconditions", "Room conflicts", "Student conflicts",
			"Overcrowded rooms", "Number of terms", "Vacant places" };

	public SDSAgent(IPlan plan, int id,
			Map<String, ISchedulingData> eventsSchedulingData) {
		for (IEvent e : plan.getPlanEvents())
			type.put(e.getId(), e.getEventDistribution().getType());
		termNumberInEachEvent = plan.getTermNumberInEachEvent();
		planName = plan.getName();
		this.id = id;
	}

	public Map<ITerm, Boolean> getStudentActivity() {
		return studentActivity;
	}

	public Map<IEvent, Boolean> getTermActivity() {
		return termActivity;
	}

	public int getType(String eventID) {
		return type.get(eventID);
	}

	public Map<IEvent, Boolean> getTermNumberActivity() {
		return termNumberActivity;
	}

	public int getTermNumberInEachEvent() {
		return termNumberInEachEvent;
	}

	public void addEventTerm(IEvent event, List<SDSTerm> list) {
		events.put(event, list);
	}

	public Map<IEvent, List<SDSTerm>> getEvents() {
		return events;
	}

	public int[] getFitnessVector() {
		return fitnessVector;
	}

	public void setFitnessVector(int[] fitnessVector) {
		if(fitnessVector.length!=this.fitnessVector.length)
			return;
		for(int i=0;i<fitnessVector.length;i++)
			this.fitnessVector[i]=fitnessVector[i];
	}

	public int evaluateTerm(IEvent event, SDSTerm currentTerm,
			boolean equalSequence, ReservationManager2 tempManager) {
		// 2 treba promjena vremena, 1 promijena dvorane,0 bez promijene
		if (!event.getPreconditionEvents().isEmpty()) {
			for (IPrecondition ip : event.getPreconditionEvents()) {
				for (SDSTerm term : events.get(ip.getEvent())) {
					if (term.getTermSpan().getStart().before(
							currentTerm.getTermSpan().getEnd()))
						return 2;
				}
			}
		}
		if (equalSequence) {
			for (SDSTerm term : events.get(event)) {
				if (currentTerm.getOrder() < term.getOrder()
						&& currentTerm.getTermSpan().getStart().after(
								term.getTermSpan().getEnd()))
					return 2;
				if (currentTerm.getOrder() > term.getOrder()
						&& currentTerm.getTermSpan().getStart().before(
								term.getTermSpan().getEnd()))
					return 2;
			}
		}
		if (tempManager.countConflictsForRoom(currentTerm.getRoom().getId()) > 0)
			return 2;
		if (currentTerm.getStudents().size() > currentTerm.getRoom()
				.getCapacity())
			return 1;
		return 0;
	}

	/**
	 * zamjena termina ili random promjena postavki ukoliko comparingTerm==null
	 * 
	 * @param event
	 * @param currentTerm
	 * @param comparingTerm
	 */
	public void changeTerms(IEvent event, SDSTerm currentTerm,
			SDSTerm comparingTerm) {
		if (!currentTerm.equals2(comparingTerm) && comparingTerm != null)
			return;
		System.out.println("Term switch");
		System.out.print(currentTerm + "  ");
		Random randomGenerator = new Random();
		if (comparingTerm == null) {
			List<DateStamp> dates = new ArrayList<DateStamp>();

			RoomData room = currentTerm.getTermRooms().get(
					randomGenerator.nextInt(currentTerm.getTermRooms().size()));

			ISchedulingData data = currentTerm.getTermData();
			if (data != null)
				dates.addAll(data.getTermData().get(room).keySet());
			if (dates.isEmpty() && currentTerm.getEventData() != null)
				dates.addAll(currentTerm.getEventData().getTermData().get(room)
						.keySet());
			DateStamp date = dates.get(randomGenerator.nextInt(dates.size()));
			List<TimeSpan> timeSpans = new ArrayList<TimeSpan>();
			if (data != null)
				timeSpans.addAll(data.getTermData().get(room).get(date));
			if (timeSpans.isEmpty())
				timeSpans.addAll(currentTerm.getEventData().getTermData().get(
						room).get(date));
			TimeSpan time = timeSpans.get(randomGenerator.nextInt(timeSpans
					.size()));
			TimeSpan termTime;
			if (time.getEnd().getAbsoluteTime()
					- time.getStart().getAbsoluteTime() == event
					.getTermDuration())
				termTime = time;
			else {
				int absTime = time.getEnd().getAbsoluteTime()
						- time.getStart().getAbsoluteTime();
				// - e.getTermDuration();
				int termNum = randomGenerator.nextInt(absTime / 15);
				termTime = new TimeSpan(new TimeStamp(time.getStart()
						.getAbsoluteTime()
						+ termNum * 15), new TimeStamp(time.getStart()
						.getAbsoluteTime()
						+ termNum * 15 + event.getTermDuration()));
			}

			int index = events.get(event).indexOf(currentTerm);
			events.get(event).get(index).setDate(date);
			events.get(event).get(index).setRoom(room);
			events.get(event).get(index).setTermSpan(termTime);
			System.out.println(" random " + room.getName() + " " + date + " "
					+ termTime);
		} else {
			int index = events.get(event).indexOf(currentTerm);
			events.get(event).get(index).setDate(comparingTerm.getDate());
			events.get(event).get(index).setRoom(comparingTerm.getRoom());
			events.get(event).get(index).setTermSpan(
					comparingTerm.getTermSpan());
			System.out.println("comparingTerm " + comparingTerm);
		}

	}

	/**
	 * prebaci studenta iz currentTerm u studentsComparingTerm
	 * 
	 * @param student1
	 * @param event
	 * @param currentTerm
	 * @param studentsComparingTerm
	 */
	public void transferStudent(String student1, IEvent event,
			SDSTerm currentTerm, SDSTerm studentsComparingTerm) {
	//	System.out.println("Student switch");
	//	System.out.println("TransferStudent " + student1 + " " + currentTerm
	//			+ " " + studentsComparingTerm);
		for (SDSTerm term : events.get(event)) {
			if (term.getId().equals(currentTerm.getId())) {
				term.getStudents().remove(student1);
			}
			if (term.getId().equals(studentsComparingTerm.getId())) {
				term.getStudents().add(student1);
			}

		}

	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof SDSAgent))
			return false;
		SDSAgent pom = (SDSAgent) obj;
		return this.id == pom.getId();
	}

	@Override
	public int hashCode() {
		return new Integer(id).hashCode();
	}

	private int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "agent-" + id;
	}

	/**
	 * metoda koja izbacuje termin u slucaju da je razlika broja izmedu
	 * kapaciteta i studenata veca od kapaciteta prostorije sdsTerma
	 * 
	 * @param event
	 * @param sdsTerm
	 */
	public void removeTerm(IEvent event, SDSTerm sdsTerm) {
		List<String> students = sdsTerm.getStudents();
		int i = 0;

		if (events.get(event).remove(sdsTerm)) {
			for (SDSTerm term : events.get(event)) {
				while (term.getRoom().getCapacity() < term.getStudents().size()) {
					if (i >= students.size())
						return;
					term.getStudents().add(students.get(i));
					i++;
				}
			}
		}

	}

	@Override
	public int compareTo(SDSAgent o) {
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

	public ISchedulingResult toResult() {
		SchedulingResult result = new SchedulingResult();
		result.addPlan(planName);
		for (IEvent e : events.keySet()) {
			result.addEvent(e.getName(), e.getId());
			for (SDSTerm t : events.get(e)) {
				result.addTerm(e.getName(), t.getName(), t.getRoom().getId(), t
						.getRoom().getCapacity(), t.getDate().getStamp(), t
						.getTermSpan().getStart().getAbsoluteTime(), t
						.getTermSpan().getEnd().getAbsoluteTime());
				for (String s : t.getStudents()) {
					result.addStudentToTerm(e.getName(), t.getName(), s);
				}
			}
		}
		return result;
	}

}
