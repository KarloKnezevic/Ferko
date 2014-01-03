package hr.fer.zemris.util.scheduling.algorithms.filip_boltuzic;

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
import hr.fer.zemris.util.scheduling.support.algorithmview.IEventDistribution;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BCOScheduler implements ISchedulingAlgorithm {

	private final static int populationSize = 50;
	//private final static int iterations = 1000;
	private static int stageNumber;
	private static int stepNum = 0;
	private static long averageProfitability[];
	private static double follow;
	private static List <PopulationMemberBCO> list;
	private IPlan plan;
	private Random randomGenerator = new Random();
	private ReservationManager2 fixedReservations;
	private ReservationManager2 manager;

	private static PopulationMemberBCO[] startPopulation = new PopulationMemberBCO[populationSize];
	private static PopulationMemberBCO forager;
	private Map<String, ISchedulingData> eventsSchedulingData;
	
	private Map<String, String[]> availableStudentsForEvent = new HashMap<String, String[]>();
	private Map<String, ITerm[]> availableTermsForEvent = new HashMap<String, ITerm[]>();
	private Map<String, Map<RoomData, Map<DateStamp, List<TimeSpan>>>> availableResourcesForEvent = new HashMap<String, Map<RoomData, Map<DateStamp, List<TimeSpan>>>>();
	public static Map<PopulationMemberBCO, List<PopulationMemberBCO>> stageMap = new HashMap<PopulationMemberBCO, List<PopulationMemberBCO>>();

	@Override
	public void prepare(IPlan plan,
			Map<String, ISchedulingData> eventsSchedulingData)
			throws SchedulingException {

		this.eventsSchedulingData = eventsSchedulingData;
		this.plan = plan;
		this.fixedReservations = new ReservationManager2(eventsSchedulingData);
		this.randomGenerator = new Random();
		fillLists();
	}

	private void fillLists() {

		for (IEvent e : plan.getPlanEvents()) {
			ISchedulingData eventData = eventsSchedulingData.get(e.getId());
			if (eventData != null) {
				if (eventData.getPeopleData() != null) {
					String[] es = new String[eventData.getPeopleData().keySet()
							.size()];
					eventData.getPeopleData().keySet().toArray(es);
					availableStudentsForEvent.put(e.getId(), es);
				}
				if (eventData.getTermData() != null) {
					Map<RoomData, Map<DateStamp, List<TimeSpan>>> dataForEvent = availableResourcesForEvent
							.get(e);
					if (dataForEvent == null)
						dataForEvent = new HashMap<RoomData, Map<DateStamp, List<TimeSpan>>>();
					for (RoomData rd : eventData.getTermData().keySet()) {
						Map<DateStamp, List<TimeSpan>> dataForRoom = dataForEvent
								.get(rd);
						if (dataForRoom == null)
							dataForRoom = new HashMap<DateStamp, List<TimeSpan>>();
						for (DateStamp d : eventData.getTermData().get(rd)
								.keySet()) {
							List<TimeSpan> timesForDate = dataForRoom.get(d);
							if (timesForDate == null)
								timesForDate = new ArrayList<TimeSpan>();
							for (TimeSpan t : eventData.getTermData().get(rd)
									.get(d)) {
								if (t.getDuration() >= e.getTermDuration())
									timesForDate.add(t);
							}
							if (timesForDate.size() > 0)
								dataForRoom.put(d, timesForDate);
						}
						dataForEvent.put(rd, dataForRoom);
					}
					availableResourcesForEvent.put(e.getId(), dataForEvent);
				}
			}
			if (e.getEventDistribution().getType() == Definition.GIVEN_DISTRIBUTION) {
				ITerm[] et = new ITerm[e.getTerms().size()];
				e.getTerms().toArray(et);
				availableTermsForEvent.put(e.getId(), et);
				for (ITerm t : et) {
					ISchedulingData termData = eventsSchedulingData.get(t
							.getId());
					if (termData != null) {
						if (termData.getPeopleData() != null) {
							String[] es = new String[termData.getPeopleData()
									.keySet().size()];
							termData.getPeopleData().keySet().toArray(es);
							availableStudentsForEvent.put(t.getId(), es);
						}
						Map<RoomData, Map<DateStamp, List<TimeSpan>>> dataForEvent = availableResourcesForEvent
								.get(t);
						if (dataForEvent == null)
							dataForEvent = new HashMap<RoomData, Map<DateStamp, List<TimeSpan>>>();
						for (RoomData rd : termData.getTermData().keySet()) {
							Map<DateStamp, List<TimeSpan>> dataForRoom = dataForEvent
									.get(rd);
							if (dataForRoom == null)
								dataForRoom = new HashMap<DateStamp, List<TimeSpan>>();
							for (DateStamp d : termData.getTermData().get(rd)
									.keySet()) {
								List<TimeSpan> timesForDate = dataForRoom
										.get(d);
								if (timesForDate == null)
									timesForDate = new ArrayList<TimeSpan>();
								for (TimeSpan ts : termData.getTermData().get(
										rd).get(d)) {
									if (ts.getDuration() >= e.getTermDuration())
										timesForDate.add(ts);
								}
								if (timesForDate.size() > 0)
									dataForRoom.put(d, timesForDate);
							}
							dataForEvent.put(rd, dataForRoom);
						}
						availableResourcesForEvent.put(t.getId(), dataForEvent);
						}
				}
			}
		}
	}

	@Override
	public void start() throws SchedulingException {
		for (int i = 0; i < populationSize; i++) {
			createRandomPopulationMember(i);
		}
		Arrays.sort(startPopulation);
		
		System.out.println("pocetno rjesenje: " + Arrays.toString(startPopulation[0].getFitnessVector()));
		stageNumber = stageMap.get(startPopulation[0]).size();
		calculateColonyProfitabilityRating();
	}

	private void makeOneRandomStep() {
		boolean eventLevel = false;
		boolean kraj = false;
		for (IEvent e : plan.getPlanEvents()) {
			if (kraj)
				break;
			if (!forager.getEvents().contains(e)){
				forager.addEvent(e);
			}
			if (forager.getTerms().get(e.getId())==null){
				addTermToForager(e).getId();
			}
			
				for (TermBCO t : forager.getTerms().get(e.getId())) {
					if (kraj)
						break;
					
					if (availableStudentsForEvent.get(e.getId()) != null){
						eventLevel = true;
					}
					if (eventLevel){
					for (String s : availableStudentsForEvent.get(e.getId())){
						if (kraj)
							break;
						
							List<Student> list = forager.getStudents().get(
									t.getId());
							if (list == null){
								forager.addStudentToTerm(t, s);
								kraj = true;
								break;
							}
							if (!list.contains(new Student(s))) {
								forager.addStudentToTerm(t, s);
								kraj = true;
								break;
							} 
					}
					}else{
						for (String s : availableStudentsForEvent.get(t.getId())) {
							if (kraj)
								break;
							
							List<Student> list = forager.getStudents().get(
									t.getId());
							if (list == null){
								forager.addStudentToTerm(t, s);
								kraj = true;
								break;
							}
							if (!list.contains(new Student(s))) {
								forager.addStudentToTerm(t, s);
								kraj = true;
								break;
							} 						
						}					
					}
				}
				if (!kraj){
					if (e.getTerms().size() > forager.getTerms().get(e.getId()).size()){
						addTermToForager(e);
						makeOneRandomStep();
					}
				}
		}
	}

	private TermBCO addTermToForager(IEvent e) {
		ITerm[] eTerms;
		if (e.getEventDistribution().getType() == Definition.RANDOM_DISTRIBUTION) {
			IEventDistribution d = e.getEventDistribution();
			int numberOfTerms = 0;
			if (d.getMinimumTermNumber() == d.getMaximumTermNumber())
				numberOfTerms = d.getMaximumTermNumber();
			else
				numberOfTerms = d.getMinimumTermNumber()
						+ randomGenerator.nextInt(d.getMaximumTermNumber()
								- d.getMinimumTermNumber());
			eTerms = new ITerm[numberOfTerms];
			for (int j = 0; j < numberOfTerms; j++) {
				eTerms[j] = new TermBCO(e.getId() + "/" + j, e.getId()
						+ "/Term_" + j);
			}
		} else {
			eTerms = availableTermsForEvent.get(e.getId());
		}
		int fromIndex = 0;
		int processedTerms = 0;
		TermBCO ter1 = null;
		for (ITerm t : eTerms) {
			if (forager.getStudents().keySet().contains(t.getId())){
				continue;
			}
			String[] termStudents = availableStudentsForEvent
					.get(t.getId());
			RoomData[] termRooms = null;
			Map<RoomData, Map<DateStamp, List<TimeSpan>>> m = availableResourcesForEvent
					.get(t.getId());
			if (m != null) {
				termRooms = new RoomData[m.keySet().size()];
				m.keySet().toArray(termRooms);
			}
			if (termStudents == null) {
				String[] eStudents = availableStudentsForEvent.get(e
						.getId());
				List<String> t1 = Arrays.asList(eStudents);
				Collections.shuffle(t1, randomGenerator);
				eStudents = t1.toArray(new String[1]);
				int numberOfStudents = (eStudents.length - fromIndex)
						/ (eTerms.length - processedTerms);
				int toIndex = Math.min(eStudents.length, fromIndex
						+ numberOfStudents);
				termStudents = Arrays.copyOfRange(eStudents, fromIndex,
						toIndex);
				fromIndex += numberOfStudents;
				processedTerms++;
			}
			if (termRooms == null) {
				m = availableResourcesForEvent.get(e.getId());
				termRooms = new RoomData[m.keySet().size()];
				m.keySet().toArray(termRooms);
			}
			RoomData selectedRoom = termRooms[randomGenerator
					.nextInt(termRooms.length)];
			
			List<DateStamp> dateStampList = new ArrayList<DateStamp>(m.get(
					selectedRoom).keySet());
			Collections.shuffle(dateStampList, randomGenerator);
			DateStamp selectedDate = dateStampList.get(0);
			List<TimeSpan> timeSpanList = new ArrayList<TimeSpan>(m.get(
					selectedRoom).get(selectedDate));
			Collections.shuffle(timeSpanList);
			TimeSpan selectedTimeSpan = timeSpanList.get(0);
			int startTime = selectedTimeSpan.getStart().getAbsoluteTime() / 15;
			
			if (selectedTimeSpan.getEnd().getAbsoluteTime()
					- selectedTimeSpan.getStart().getAbsoluteTime() > e
					.getTermDuration()) {
				startTime += randomGenerator.nextInt((selectedTimeSpan
						.getEnd().getAbsoluteTime()
						- selectedTimeSpan.getStart().getAbsoluteTime() - e
						.getTermDuration()) / 15);
			}
			int endTime = startTime + e.getTermDuration() / 15;
			ter1 = new TermBCO(t.getId(), t.getName());
			ter1.setDateStamp(selectedDate);
			ter1.setRoom(selectedRoom);
			ter1.setTimeSpan(new TimeSpan(new TimeStamp(startTime * 15),
					new TimeStamp(endTime * 15)));
			ter1.setDuration(e.getTermDuration());
			forager.addTermToEvent(e.getId(), ter1);
			break;
		}
		return ter1;
		
	}

	private void evaluateFitness() {
		manager = new ReservationManager2(fixedReservations);
		forager.rebuildManager(manager);
		forager.evaluate(manager);
		manager = null;
	}

	private void evaluateFitness(PopulationMemberBCO pop) {
		manager = new ReservationManager2(fixedReservations);
		pop.rebuildManager(manager);
		pop.evaluate(manager);
		manager = null;
	}

	private void setFollow(float div) {
		if (div < 0.75) {
			follow = 0.0;
		} else if (div >= 0.75 && div < 0.85) {
			follow = 0.02;
		} else if (div >= 0.85 && div < 1) {
			follow = 0.2;
		} else if (div >= 1) {
			follow = 0.8;
		}

	}

	private void calculateColonyProfitabilityRating() {
		averageProfitability = new long [stageNumber];
		Arrays.fill(averageProfitability, 0);
		for (int i = 0; i < stageNumber; i++) {
			for (int j = 0; j < populationSize;j++){
				PopulationMemberBCO pop = stageMap.get(startPopulation[j]).get(i);
				evaluateFitness(pop);
				averageProfitability [i]+= pop.calculateProfitability();
			}
			averageProfitability[i] /= populationSize;
		}
	}

		@Override
	public void step() throws SchedulingException {
			
			forager = new PopulationMemberBCO();
			makeOneRandomStep();
			list = new ArrayList <PopulationMemberBCO> ();
			
			for (stepNum = 1; stepNum < stageNumber; stepNum++) {
				follow = -1;
				evaluateFitness();
				int pF = forager.calculateProfitability();
				float div = pF / averageProfitability[stepNum];
				setFollow(div);
			
				if (follow > randomGenerator.nextFloat()) {
					int index = randomGenerator.nextInt(startPopulation.length/10);
					PopulationMemberBCO getter = startPopulation[index];
					forager = (PopulationMemberBCO) stageMap.get(getter).get(stepNum)
						.clone();
				} else {
					makeOneRandomStep();
				}
				PopulationMemberBCO part = (PopulationMemberBCO) forager.clone();
				list.add(part);
			}
			int index = randomGenerator.nextInt(populationSize/2) + populationSize/2;
			stageMap.remove(startPopulation[index]);
			startPopulation[index] = forager;
			evaluateFitness();
			stageMap.put(forager, list);
			Arrays.sort(startPopulation);
			//ispis generiranog rješenja
			//System.out.println(Arrays.toString(forager.getFitnessVector()));
			System.out.println("Najbolje rješenje: " + Arrays.toString(startPopulation[0].getFitnessVector()));
	}

	private void createRandomPopulationMember(int i) {
		PopulationMemberBCO result = new PopulationMemberBCO();
		result.setPlan(plan.getName());
		List<PopulationMemberBCO> stageList = new ArrayList<PopulationMemberBCO>();
		for (IEvent e : plan.getPlanEvents()) {
			result.addEvent(e);

			ITerm[] eTerms;
			if (e.getEventDistribution().getType() == Definition.RANDOM_DISTRIBUTION) {
				IEventDistribution d = e.getEventDistribution();
				int numberOfTerms = 0;
				if (d.getMinimumTermNumber() == d.getMaximumTermNumber())
					numberOfTerms = d.getMaximumTermNumber();
				else
					numberOfTerms = d.getMinimumTermNumber()
							+ randomGenerator.nextInt(d.getMaximumTermNumber()
									- d.getMinimumTermNumber());
				eTerms = new ITerm[numberOfTerms];
				for (int j = 0; j < numberOfTerms; j++) {
					eTerms[j] = new TermBCO(e.getId() + "/" + j, e.getId()
							+ "/Term_" + j);
				}
			} else {
				eTerms = availableTermsForEvent.get(e.getId());
			}
			int fromIndex = 0;
			int processedTerms = 0;
			for (ITerm t : eTerms) {
				String[] termStudents = availableStudentsForEvent
						.get(t.getId());
				RoomData[] termRooms = null;
				
				Map<RoomData, Map<DateStamp, List<TimeSpan>>> m = availableResourcesForEvent
						.get(t.getId());
				if (m != null) {
					
					termRooms = new RoomData[m.keySet().size()];
					m.keySet().toArray(termRooms);
				}
				if (termStudents == null) {
					
					String[] eStudents = availableStudentsForEvent.get(e
							.getId());
					List<String> t1 = Arrays.asList(eStudents);
					Collections.shuffle(t1, randomGenerator);
					
					eStudents = t1.toArray(new String[1]);
					
					int numberOfStudents = (eStudents.length - fromIndex)
							/ (eTerms.length - processedTerms);
					int toIndex = Math.min(eStudents.length, fromIndex
							+ numberOfStudents);
					
					termStudents = Arrays.copyOfRange(eStudents, fromIndex,
							toIndex);
					fromIndex += numberOfStudents;
					processedTerms++;
				}
				if (termRooms == null) {
					m = availableResourcesForEvent.get(e.getId());
					termRooms = new RoomData[m.keySet().size()];
					m.keySet().toArray(termRooms);
				}
				
				RoomData selectedRoom = termRooms[randomGenerator
						.nextInt(termRooms.length)];
				
				List<DateStamp> dateStampList = new ArrayList<DateStamp>(m.get(
						selectedRoom).keySet());
				Collections.shuffle(dateStampList, randomGenerator);
				DateStamp selectedDate = dateStampList.get(0);
				List<TimeSpan> timeSpanList = new ArrayList<TimeSpan>(m.get(
						selectedRoom).get(selectedDate));
				Collections.shuffle(timeSpanList);
				TimeSpan selectedTimeSpan = timeSpanList.get(0);
				int startTime = selectedTimeSpan.getStart().getAbsoluteTime() / 15;
				
				if (selectedTimeSpan.getEnd().getAbsoluteTime()
						- selectedTimeSpan.getStart().getAbsoluteTime() > e
						.getTermDuration()) {
					startTime += randomGenerator.nextInt((selectedTimeSpan
							.getEnd().getAbsoluteTime()
							- selectedTimeSpan.getStart().getAbsoluteTime() - e
							.getTermDuration()) / 15);
				}
				int endTime = startTime + e.getTermDuration() / 15;
				TermBCO t1 = new TermBCO(t.getId(), t.getName());
				t1.setDateStamp(selectedDate);
				t1.setRoom(selectedRoom);
				t1.setTimeSpan(new TimeSpan(new TimeStamp(startTime * 15),
						new TimeStamp(endTime * 15)));
				t1.setDuration(e.getTermDuration());
				result.addTermToEvent(e.getId(), t1);

				for (String s : termStudents) {
					result.addStudentToTerm(t1, s);
					// List<IEvent> events = new ArrayList <IEvent> ();
					// events.addAll(result.getEvents());
					// Map<String, List<TermBCO>> terms = new HashMap<String,
					// List<TermBCO>> ();
					// terms.putAll(result.getTerms());
					// Map<String, List<Student>> students = new HashMap
					// <String, List <Student>> ();
					// students.putAll(result.getStudents());
					// PopulationMemberBCO stage = new PopulationMemberBCO
					// (events, terms, students);
					// //System.out.println(sta.toString());
					PopulationMemberBCO stage = (PopulationMemberBCO) result
							.clone();
					stageList.add(stage);
				}
			}
		}

		stageMap.put(result, stageList);
	
		manager = new ReservationManager2(fixedReservations);
		result.rebuildManager(manager);
		result.evaluate(manager);
		manager = null;
		startPopulation[i] = result;
	}

	@Override
	public void stop() throws SchedulingException {
		// TODO Auto-generated method stub

	}

	@Override
	public void use(ISchedulingResult result) throws SchedulingException {
		PopulationMemberBCO a=PopulationMemberBCO.convertFromResult(result);
		ReservationManager2 man=new ReservationManager2(fixedReservations);
		a.rebuildManager(man);
		a.evaluate(man);
		startPopulation[a.compareTo(startPopulation[0])+1]=a;
	}

	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public Component getExecutionFeedback() throws SchedulingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISchedulingResult getResult() throws SchedulingException {
		return startPopulation[0].toResult();
	}

	@Override
	public ISchedulingResult[] getResults() throws SchedulingException {
		ISchedulingResult[] results = new ISchedulingResult[startPopulation.length];
		for(int i=0;i<startPopulation.length;i++)
			results[i]=startPopulation[i].toResult();
		return results;
	}

	@Override
	public SchedulingAlgorithmStatus getStatus() {
		return SchedulingAlgorithmStatus.SUCCESS;
	}

	@Override
	public void registerSchedulingMonitor(ISchedulingMonitor sm)
			throws SchedulingException {
		// TODO Auto-generated method stub

	}

}
