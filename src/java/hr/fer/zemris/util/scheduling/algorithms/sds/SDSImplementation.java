package hr.fer.zemris.util.scheduling.algorithms.sds;

import hr.fer.zemris.jcms.model.planning.Definition;
import hr.fer.zemris.util.scheduling.support.ISchedulingAlgorithm;
import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingMonitor;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.ReservationManager2;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.SchedulingAlgorithmStatus;
import hr.fer.zemris.util.scheduling.support.SchedulingException;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import javax.swing.JPanel;

public class SDSImplementation implements ISchedulingAlgorithm {

	private SDSAgent[] agents = new SDSAgent[brojAgenata];
	private static boolean equalDistribution;
	private static boolean equalSequence;
	private int termNumberInEachEvent;
	private int minTermNumber;
	private static SchedulingAlgorithmStatus status = SchedulingAlgorithmStatus.SUCCESS;
	private static ISchedulingMonitor monitor;
	private static Map<IEvent, SDSTerm> currentTerm = new HashMap<IEvent, SDSTerm>();
	private static Map<IEvent, SDSTerm> comparingTerm = new HashMap<IEvent, SDSTerm>();;
	private static Map<IEvent, SDSTerm> studentsComparingTerms = new HashMap<IEvent, SDSTerm>();;
	private static Map<IEvent, String> student1 = new HashMap<IEvent, String>();
	private static Map<IEvent, String> student2 = new HashMap<IEvent, String>();
	private final static int brojAgenata = 100;
	private boolean stop = false;
	private IPlan plan;
	private Map<String, ISchedulingData> eventsSchedulingData;
	private ReservationManager2 fixedManager;
	private ReservationManager2 tempManager;
	private int[] fitnessVector = new int[6];
	private int acceptableSch = 0;
	private int repeat = 0;
	private Queue<Integer> red = new LinkedList<Integer>();
	
	/**
	 * Metoda koja "prepisuje" podatak u trenutnom agentu. Ako je agent od kojeg
	 * je izgubio usporedbu aktivan, onda prepisuje od njega, ako nije aktivan,
	 * nasumicno izmjenjuje termin.
	 * 
	 * @param agent
	 * @param comAgent
	 */
	private void diffuse(int agent, int comAgent) {

		boolean currentStudActivity = true, comparingStudActivity = true;
		boolean term1Students = true, term2Students = true;
		if (equalDistribution)
			for (IEvent event : agents[agent].getEvents().keySet()) {
				currentStudActivity = currentStudActivity
						&& agents[agent].getStudentActivity().get(
								studentsComparingTerms.get(event));
				comparingStudActivity = comparingStudActivity
						&& agents[agent].getStudentActivity().get(
								studentsComparingTerms.get(event));
				term1Students = term1Students
						&& (currentTerm.get(event).getStudents().size() > currentTerm
								.get(event).getRoom().getCapacity());
				term2Students = term2Students
						&& studentsComparingTerms.get(event).getStudents()
								.size() < studentsComparingTerms.get(event)
								.getRoom().getCapacity();
			}

		for (IEvent event : agents[agent].getEvents().keySet()) {

			if (currentTerm.get(event).isStudentsChangable()) {
				if (agents[agent].getStudentActivity() != null)
					if (!agents[agent].getStudentActivity().get(
							currentTerm.get(event))
							&& !equalDistribution) {
						if (agents[comAgent].getStudentActivity().get(
								studentsComparingTerms.get(event)) != null)
							if (agents[agent].getStudentActivity().get(
									studentsComparingTerms.get(event))) {
								agents[agent].transferStudent(student1
										.get(event), event, currentTerm
										.get(event), studentsComparingTerms
										.get(event));
								agents[agent].transferStudent(student2
										.get(event), event,
										studentsComparingTerms.get(event),
										currentTerm.get(event));
							} else if (currentTerm.get(event).getRoom()
									.getCapacity() < currentTerm.get(event)
									.getStudents().size()
									&& studentsComparingTerms.get(event)
											.getRoom().getCapacity() > studentsComparingTerms
											.get(event).getStudents().size())
								agents[agent].transferStudent(student1
										.get(event), event, currentTerm
										.get(event), studentsComparingTerms
										.get(event));
							else if (currentTerm.get(event).getRoom()
									.getCapacity() > currentTerm.get(event)
									.getStudents().size())
								agents[agent].transferStudent(student2
										.get(event), event,
										studentsComparingTerms.get(event),
										currentTerm.get(event));
							else {
								agents[agent].transferStudent(student1
										.get(event), event, currentTerm
										.get(event), studentsComparingTerms
										.get(event));
								agents[agent].transferStudent(student2
										.get(event), event,
										studentsComparingTerms.get(event),
										currentTerm.get(event));
							}
					}
				if (equalDistribution) {
					if (!currentStudActivity && comparingStudActivity) {
						agents[agent].transferStudent(student1.get(event),
								event, currentTerm.get(event),
								studentsComparingTerms.get(event));
						agents[agent].transferStudent(student2.get(event),
								event, studentsComparingTerms.get(event),
								currentTerm.get(event));
					}
					if (term1Students && term2Students)
						agents[agent].transferStudent(student1.get(event),
								event, currentTerm.get(event),
								studentsComparingTerms.get(event));
				}
			}

			if (!agents[agent].getTermActivity().get(event)) {
				if (agents[comAgent].getTermActivity().get(event))
					agents[agent].changeTerms(event, currentTerm.get(event),
							comparingTerm.get(event));
				else {
					agents[agent].changeTerms(event, currentTerm.get(event),
							null);
				}
			}

			if (event.getEventDistribution().getType() != Definition.GIVEN_DISTRIBUTION
					&& termNumberInEachEvent == -1
					&& agents[agent].getTermActivity().get(event)
					&& agents[agent].getEvents().get(event).size() > minTermNumber)
				if (!agents[agent].getTermNumberActivity().get(event)) {
					agents[agent].removeTerm(event, currentTerm.get(event));
				}
		}
	}

	/**
	 * Metoda koja uspoređuje event trenutnog agenta i nasumično odabranog i
	 * koja uspoređuje dva studenta u jednom eventu
	 * 
	 * @param agent
	 *            id agenta kojeg uspoređujemo
	 */
	private int test(int agent) {
		Random randomGenerator = new Random();

		int comAgent = 0;
		// odabir agenta s kojim usporedujemo
		do {
			comAgent = randomGenerator.nextInt(brojAgenata);
		} while (comAgent == agent);
		int term = -1;
		int termInStudentChanges = -1;
		int student1num = -1, student2num = -1;
		for (IEvent event : agents[agent].getEvents().keySet()) {
			List<SDSTerm> agentTerms = agents[agent].getEvents().get(event);
			List<SDSTerm> comparingAgentTerms = agents[comAgent].getEvents()
					.get(event);
			// odabir termina za usporedbu
			// ako equalSequence, usporeduju se isti termini u svakom eventu
			// kako bi se u svakom terminu iste zamjene obavljale
			if (term == -1 || equalSequence == false)
				term = randomGenerator.nextInt(agentTerms.size());
			SDSTerm currentTermTemp = agentTerms.get(term);
			currentTerm.put(event, currentTermTemp);
			SDSTerm comparingTermTemp = null;
			if (comparingAgentTerms.size() <= term)
				comparingTermTemp = comparingAgentTerms.get(randomGenerator
						.nextInt(comparingAgentTerms.size()));
			else
				comparingTermTemp = comparingAgentTerms.get(term);
			comparingTerm.put(event, comparingTermTemp);
			if (fixedManager.isRoomReserved(currentTermTemp.getRoom().getId(),
					currentTermTemp.getDate(), currentTermTemp.getTermSpan())
					&& !fixedManager.isRoomReserved(comparingTermTemp.getRoom()
							.getId(), comparingTermTemp.getDate(),
							comparingTermTemp.getTermSpan())) {
				agents[agent].getTermActivity().put(event, false);
				agents[comAgent].getTermActivity().put(event, true);
			} else if (agents[agent].evaluateTerm(event, currentTermTemp,
					equalSequence, tempManager) <= agents[comAgent]
					.evaluateTerm(event, comparingTermTemp, equalSequence,
							tempManager))
				agents[agent].getTermActivity().put(event, true);
			else {
				agents[agent].getTermActivity().put(event, false);

			}
			SDSTerm studentsComparingTermTemp;
			// odabir studenata za usporedbu
			if (currentTermTemp.isStudentsChangable() && agentTerms.size() > 1) {
				do {
					if (termInStudentChanges == -1
							|| equalDistribution == false)
						termInStudentChanges = randomGenerator
								.nextInt(agentTerms.size());
					studentsComparingTermTemp = agentTerms
							.get(termInStudentChanges);
				} while (studentsComparingTermTemp.getId().equals(
						currentTermTemp.getId())
						&& !studentsComparingTermTemp.isStudentsChangable());
				List<String> studentsInTerm = currentTermTemp.getStudents();
				List<String> studentsInComparingTerm = studentsComparingTermTemp
						.getStudents();
				studentsComparingTerms.put(event, studentsComparingTermTemp);
				int pom = 0;
				if (studentsInTerm.size() > 0
						&& studentsInComparingTerm.size() > 0) {
					if (student1num == -1 || equalDistribution == false)
						student1num = randomGenerator.nextInt(studentsInTerm
								.size());
					String student1Temp = studentsInTerm.get(student1num);
					student1.put(event, student1Temp);
					if (studentsInComparingTerm.size() != 0) {
						if (student2num == -1 || equalDistribution == false)
							student2num = randomGenerator
									.nextInt(studentsInComparingTerm.size());
						String student2Temp = studentsInComparingTerm
								.get(student2num);
						student2.put(event, student2Temp);

						boolean b1 = fixedManager.isStudentReserved(
								student1Temp, currentTermTemp.getDate(),
								currentTermTemp.getTermSpan());
						boolean b2 = fixedManager.isStudentReserved(
								student2Temp, currentTermTemp.getDate(),
								currentTermTemp.getTermSpan());

						boolean b3 = fixedManager.isStudentReserved(
								student1Temp, comparingTermTemp.getDate(),
								comparingTermTemp.getTermSpan());
						boolean b4 = fixedManager.isStudentReserved(
								student2Temp, comparingTermTemp.getDate(),
								comparingTermTemp.getTermSpan());

						if (!b1)
							pom--;
						if (!b2)
							pom--;
						if (b3)
							pom++;
						if (b4)
							pom++;
					}
					if (pom > 0) {
						agents[agent].getStudentActivity().put(currentTermTemp,
								false);
						if (pom == 2)
							agents[agent].getStudentActivity().put(
									studentsComparingTermTemp, true);
						else
							agents[agent].getStudentActivity().put(
									studentsComparingTermTemp, false);
					} else {
						agents[agent].getStudentActivity().put(currentTermTemp,
								true);
					}
				} else {
					agents[agent].getStudentActivity().put(currentTermTemp,
							true);
				}
			}
			// provjarava treba li izbaciti termin
			if (event.getEventDistribution().getType() != Definition.GIVEN_DISTRIBUTION
					&& termNumberInEachEvent == -1 ) {
				int freeSpace = 0;
				for (SDSTerm temp : agents[agent].getEvents().get(event)) {
					freeSpace += temp.getRoom().getCapacity()
							- temp.getStudents().size();
				}
				if (freeSpace > currentTermTemp.getRoom().getCapacity())
					agents[agent].getTermNumberActivity().put(event, false);
				else
					agents[agent].getTermNumberActivity().put(event, true);

			} else {
				agents[agent].getTermNumberActivity().put(event, true);
			}

		}
		return comAgent;
	}

	/**
	 * U ovoj metodi se inicijaliziraju pocetna stanja. Nasumicni odabir
	 * prostorije i vremena odrzavanja termina, te nasumicno popunjavanje
	 * termina s studentima TODO : popravit
	 * 
	 * @param agent
	 *            redni broj agenta
	 */
	private void initialize(int agent) {
		System.out.println(agents[agent]);
		Random randomGenerator = new Random();
		for (IEvent e : plan.getPlanEvents()) {
			// provjera raspodjele studenata, random ili given
			List<String> eventStudents = new ArrayList<String>();
			List<RoomData> eventRooms = new ArrayList<RoomData>();

			ISchedulingData eventData = eventsSchedulingData.get(e.getId());
			if (eventData != null) {
				if (eventData.getPeopleData() != null)
					eventStudents.addAll(eventData.getPeopleData().keySet());
				if (eventData.getTermData() != null)
					eventRooms.addAll(eventData.getTermData().keySet());
			}
			agents[agent].addEventTerm(e, new ArrayList<SDSTerm>());
			agents[agent].getTermActivity().put(e, false);
			List<ITerm> eventTerms = new ArrayList<ITerm>();
			if (e.getEventDistribution().getType() == Definition.RANDOM_DISTRIBUTION) {
				minTermNumber = e.getEventDistribution().getMinimumTermNumber();
				int maxTermNumber = e.getEventDistribution()
						.getMaximumTermNumber();
				int termNumber = 0;
				if (minTermNumber == maxTermNumber)
					termNumber = minTermNumber;
				if (termNumberInEachEvent != -1)
					termNumber = termNumberInEachEvent;
				if (minTermNumber != maxTermNumber)
					termNumber = minTermNumber
							+ randomGenerator.nextInt(maxTermNumber
									- minTermNumber + 1);
				for (int i = 0; i < termNumber; i++) {
					eventTerms.add(new SDSTerm(e.getId() + i, e.getName()
							+ "-term" + i));
				}
			}
			if (e.getEventDistribution().getType() == Definition.GIVEN_DISTRIBUTION) {
				eventTerms.addAll(e.getTerms());
			}
			// liste u koje se spremaju podaci o terminu
			List<String> termStudents = new ArrayList<String>();
			List<RoomData> termRooms = new ArrayList<RoomData>();

			int from = 0, order = 0;
			for (ITerm term : eventTerms) {
				termStudents.clear();
				termRooms.clear();
				ISchedulingData data = eventsSchedulingData.get(term.getId());
				if (e.getEventDistribution().getType() == Definition.GIVEN_DISTRIBUTION) {

					if (data.getPeopleData() != null)
						termStudents.addAll(data.getPeopleData().keySet());
					if (data.getTermData() != null)
						termRooms.addAll(data.getTermData().keySet());
				}
				boolean b = true;

				if (termStudents.isEmpty()) {
					termStudents.addAll(eventStudents.subList(from, from
							+ eventStudents.size() / eventTerms.size()));
					from += termStudents.size();
				}
				if (term.getDefinition() != null)
					if (term.getDefinition().getIndividuals() != null)
						if (term.getDefinition().getIndividuals().size() > 0)
							b = false;
				// ako su studenti definirani na terminu, onemogućujemo
				// promjenu studenata;

				if (termRooms.isEmpty())
					termRooms.addAll(eventRooms);
				RoomData room = termRooms.get(randomGenerator.nextInt(termRooms
						.size()));
				List<DateStamp> dates = new ArrayList<DateStamp>();
				try {
					dates.addAll(data.getTermData().get(room).keySet());
				} catch (NullPointerException e1) {
					dates.addAll(eventData.getTermData().get(room).keySet());
				}
				DateStamp date = dates.get(randomGenerator
						.nextInt(dates.size()));
				List<TimeSpan> timeSpans = new ArrayList<TimeSpan>();
				try {
					timeSpans.addAll(data.getTermData().get(room).get(date));
				} catch (Exception e1) {
					timeSpans.addAll(eventData.getTermData().get(room)
							.get(date));
				}
				TimeSpan termTime;
				do {
					TimeSpan time = timeSpans.get(randomGenerator
							.nextInt(timeSpans.size()));

					if (time.getEnd().getAbsoluteTime()
							- time.getStart().getAbsoluteTime() == e
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
								+ termNum * 15 + e.getTermDuration()));
					}
				} while (termTime.getEnd().getAbsoluteTime() > new TimeStamp(
						20, 0).getAbsoluteTime());
				SDSTerm pTerm = new SDSTerm(term.getId(), term.getName());
				pTerm.setStudentsChangable(b);
				pTerm.setOrder(order);
				order++;
				pTerm.setDate(date);
				pTerm.setRoom(room);
				pTerm.setTermSpan(termTime);
				pTerm.getStudents().addAll(termStudents);
				pTerm.getTermRooms().addAll(termRooms);
				pTerm.setEventData(eventData);
				pTerm.setTermData(data);
				// changeable definiran u terminu
				agents[agent].getEvents().get(e).add(pTerm);
			}
		}

	}

	@Override
	public String getClassName() {
		return "StochasticDiffusionSearch";
	}

	@Override
	public JPanel getExecutionFeedback() throws SchedulingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISchedulingResult getResult() throws SchedulingException {
		ISchedulingResult[] res = getResults();
		return res[0];
	}

	@Override
	public SchedulingAlgorithmStatus getStatus() {
		return status;
	}

	@Override
	public void prepare(IPlan plan,
			Map<String, ISchedulingData> eventsSchedulingData)
			throws SchedulingException {
		// pregledaj definiciju
		this.plan = plan;
		this.eventsSchedulingData = eventsSchedulingData;
		for (int i = 0; i < brojAgenata; i++) {
			agents[i] = new SDSAgent(plan, i, eventsSchedulingData);
		}
		fixedManager = new ReservationManager2(new ReservationManager2(
				eventsSchedulingData));
		this.termNumberInEachEvent = plan.getTermNumberInEachEvent();
		// jednaki broj termina s istom raspodjelom studenata
		equalDistribution = plan.isEqualStudentDistributionInEachEvent();
		// jednak redosljed
		equalSequence = plan.isEqualTermSequenceInEachEvent();
		status = SchedulingAlgorithmStatus.PREPARED;
		for (int i = 0; i < agents.length; i++)
			initialize(i);
	}

	@Override
	public void registerSchedulingMonitor(ISchedulingMonitor sm)
			throws SchedulingException {
		monitor = sm;

	}

	@Override
	public void start() throws SchedulingException {
		boolean b = false;
		while (true && repeat < Integer.MAX_VALUE) {
			step();
			if (b && acceptableSch * 4 > brojAgenata)
				break;
			b = true;

		}
		ISchedulingResult[] r = getResults();
		for (int i = 0; i < r.length; i++)
			System.out.println(r[i]);

	}

	@Override
	public void stop() throws SchedulingException {
		status = SchedulingAlgorithmStatus.INTERRUPTED;
		stop = true;

	}

	@Override
	public ISchedulingResult[] getResults() throws SchedulingException {
		List<ISchedulingResult> res = new ArrayList<ISchedulingResult>();
		for (int i = 0; i < agents.length; i++) {
			boolean acceptable = true;
			int[] fitnessVector = agents[i].getFitnessVector();
			for (int k = 0; k < 4; k++)
				if (fitnessVector[k] != 0)
					acceptable = false;
			if (acceptable) {
				res.add(agents[i].toResult());
			}
		}
		ISchedulingResult[] results = new ISchedulingResult[res.size()];
		for (int i = 0; i < results.length; i++)
			results[i] = res.get(i);
		return results;
	}

	@Override
	public void step() throws SchedulingException {
		status = SchedulingAlgorithmStatus.RUNNING;
		acceptableSch = 0;

		for (int i = 0; i < brojAgenata; i++) {
			tempManager = null;
			tempManager = new ReservationManager2(fixedManager);
			fillTempManager(i);
			int comAgent = test(i);
			diffuse(i, comAgent);
			evaluateResult(i);
			agents[i].setFitnessVector(fitnessVector);
		}
	}

	private void fillTempManager(int agent) {
		for (IEvent e : agents[agent].getEvents().keySet())
			for (SDSTerm t : agents[agent].getEvents().get(e)) {
				reservations(t);
			}
	}

	@Override
	public void use(ISchedulingResult result) throws SchedulingException {
		Random randomGenerator = new Random();
		int pom = randomGenerator.nextInt(agents.length);
		agents[pom] = new SDSAgent(result.getPlan(), pom, eventsSchedulingData);
	}

	/**
	 * rezervacija prostorije i studenata u reservationManageru.
	 * 
	 * @param pTerm
	 */
	public void reservations(SDSTerm pTerm) {
		tempManager.reserveRoom(pTerm.getRoom().getId(), pTerm.getDate(), pTerm
				.getTermSpan());
		for (String student : pTerm.getStudents()) {
			tempManager.reserveStudent(student, pTerm.getDate(), pTerm
					.getTermSpan());
		}
	}

	public void evaluateResult(int agent) {
		Arrays.fill(fitnessVector, 0);
		for (IEvent e : agents[agent].getEvents().keySet()) {
			fitnessVector[0] += countWrongPreconditionsForEvent(e, agent);
		}
		fitnessVector[1] = tempManager.countConflictsForRooms();
		fitnessVector[2] = tempManager.countConflictsForStudents();
		countOvercrowdedRooms(agent);
		boolean acceptable = true;
		for (int k = 0; k < 4; k++)
			if (fitnessVector[k] != 0)
				acceptable = false;
		if (acceptable) {
			acceptableSch++;
		}
	}

	private void countOvercrowdedRooms(int agent) {
		for (IEvent event : agents[agent].getEvents().keySet()) {
			for (SDSTerm t : agents[agent].getEvents().get(event)) {
				fitnessVector[4]++;
				if (t.getRoom().getCapacity() < t.getStudents().size())
					fitnessVector[3]++;
				else
					fitnessVector[5] += t.getRoom().getCapacity()
							- t.getStudents().size();
			}
		}

	}

	private int countWrongPreconditionsForEvent(IEvent e, int agent) {
		int count = 0;
		for (IPrecondition p : e.getPreconditionEvents()) {
			for (SDSTerm t1 : agents[agent].getEvents().get(e)) {
				for (SDSTerm t2 : agents[agent].getEvents().get(p.getEvent())) {
					if (DateStamp.dateDiff(t1.getDate(), t2.getDate()) < p
							.getTimeDistanceValue())
						count++;
				}
			}
		}
		return count;
	}
}
