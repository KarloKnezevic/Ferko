package hr.fer.zemris.util.scheduling.algorithms.CLONALG;

import hr.fer.zemris.jcms.model.planning.Definition;
import hr.fer.zemris.util.scheduling.algorithms.MainScheduler;
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
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Clonalg implements ISchedulingAlgorithm {


	private static final int initialPopulationSize=50;
	private static final double clonesMultiplier=0.3;
	private static final int multi = 50;
	private static final int newToAdd=7;
	private Antibody[] population = new Antibody[initialPopulationSize];
	private Antibody[] clonesPopulation;
	private Map<String, ISchedulingData> eventsSchedulingData;
	
	private ReservationManager2 fixedReservationManager;
	private ReservationManager2 manager;
	
	private Map<String, String[]> availableStudentsForEvent = new HashMap<String, String[]>();
	private Map<String, ITerm[]> availableTermsForEvent = new HashMap<String, ITerm[]>();
	
	private Map<String, Map<RoomData, Map<DateStamp, List<TimeSpan>>>> availableResourcesForEvent = new HashMap<String, Map<RoomData,Map<DateStamp,List<TimeSpan>>>>();
	
	@Override
	public void prepare(IPlan plan, Map<String, ISchedulingData> eventsSchedulingData) throws SchedulingException {
		this.eventsSchedulingData=eventsSchedulingData;
		this.fixedReservationManager = new ReservationManager2(eventsSchedulingData);
		prepareLists();
	}
	
	private void prepareLists() {
		for(IEvent e:MainScheduler.plan.getPlanEvents()) {
			ISchedulingData eventData = eventsSchedulingData.get(e.getId());
			if(eventData!=null) {
				if(eventData.getPeopleData()!=null) {
					String[] es = new String[eventData.getPeopleData().keySet().size()];
					eventData.getPeopleData().keySet().toArray(es);
					availableStudentsForEvent.put(e.getId(),es);
				}
				if(eventData.getTermData()!=null) {
					Map<RoomData, Map<DateStamp, List<TimeSpan>>> dataForEvent = availableResourcesForEvent.get(e);
					if(dataForEvent==null)
						dataForEvent=new HashMap<RoomData, Map<DateStamp,List<TimeSpan>>>();
					for(RoomData rd:eventData.getTermData().keySet()) {
						Map<DateStamp, List<TimeSpan>> dataForRoom = dataForEvent.get(rd);
						if(dataForRoom==null)
							dataForRoom = new HashMap<DateStamp, List<TimeSpan>>();
						for(DateStamp d:eventData.getTermData().get(rd).keySet()) {
							List<TimeSpan> timesForDate = dataForRoom.get(d);
							if(timesForDate==null)
								timesForDate=new ArrayList<TimeSpan>();
							for(TimeSpan t:eventData.getTermData().get(rd).get(d)) {
								if(t.getDuration()>=e.getTermDuration())
									timesForDate.add(t);
							}
							if(timesForDate.size()>0)
								dataForRoom.put(d,timesForDate);
						}
						dataForEvent.put(rd, dataForRoom);
					}
					availableResourcesForEvent.put(e.getId(), dataForEvent);
				}
			}
			if(e.getEventDistribution().getType()==Definition.GIVEN_DISTRIBUTION) {
				ITerm[] et = new ITerm[e.getTerms().size()];
				e.getTerms().toArray(et);
				availableTermsForEvent.put(e.getId(),et);
				for(ITerm t:et) {
					ISchedulingData termData = eventsSchedulingData.get(t.getId());
					if(termData!=null) {
						if(termData.getPeopleData()!=null) {
							String[] es = new String[termData.getPeopleData().keySet().size()];
							termData.getPeopleData().keySet().toArray(es);
							availableStudentsForEvent.put(t.getId(),es);
						}
						if(termData.getTermData()!=null) {
							Map<RoomData, Map<DateStamp, List<TimeSpan>>> dataForEvent = availableResourcesForEvent.get(t);
							if(dataForEvent==null)
								dataForEvent=new HashMap<RoomData, Map<DateStamp,List<TimeSpan>>>();
							for(RoomData rd:termData.getTermData().keySet()) {
								Map<DateStamp, List<TimeSpan>> dataForRoom = dataForEvent.get(rd);
								if(dataForRoom==null)
									dataForRoom = new HashMap<DateStamp, List<TimeSpan>>();
								for(DateStamp d:termData.getTermData().get(rd).keySet()) {
									List<TimeSpan> timesForDate = dataForRoom.get(d);
									if(timesForDate==null)
										timesForDate=new ArrayList<TimeSpan>();
									for(TimeSpan ts:termData.getTermData().get(rd).get(d)) {
										if(ts.getDuration()>=e.getTermDuration())
											timesForDate.add(ts);
									}
									if(timesForDate.size()>0)
										dataForRoom.put(d,timesForDate);
								}
								dataForEvent.put(rd, dataForRoom);
							}
							availableResourcesForEvent.put(t.getId(), dataForEvent);
						}
					}
				}
			}
		}
	}


	@Override
	public void start() throws SchedulingException {
		createRandomMembers(initialPopulationSize);
		Arrays.sort(population);
	}

	private void createNewPopulationMember(int i) {
		Antibody result = new Antibody(availableStudentsForEvent,availableTermsForEvent,availableResourcesForEvent);
		manager = new ReservationManager2(fixedReservationManager);
		result.rebuildManager(manager);
		result.evaluate(manager);
		manager=null;
//		System.out.println(i);
		population[i]=result;
	}

	@Override
	public void step() throws SchedulingException {
		final double tau = 3.476 * (initialPopulationSize-1);
		int numberOfClones;
		clonesPopulation = new Antibody[initialPopulationSize*3];
		int k=0;
		for(int i=0;i<initialPopulationSize;i++) {
			numberOfClones = (int) Math.round(clonesMultiplier*initialPopulationSize/(i+1));
			for(int j=0;j<numberOfClones;j++) {
				clonesPopulation[k+j]=(Antibody) population[i].clone();
				if(i>0 || j>0) {
					int mutations = (int)(1+multi*0.25* (1-Math.exp(-(k+j)/tau)));
					for(int l=0;l<mutations;l++) {
						clonesPopulation[k+j].mutate(availableResourcesForEvent);
					}
					ReservationManager2 man=new ReservationManager2(fixedReservationManager);
					clonesPopulation[k+j].rebuildManager(man);
					clonesPopulation[k+j].evaluate(man);
				}
			}
			k+=numberOfClones;
		}
		clonesPopulation=Arrays.copyOf(clonesPopulation, k);
		clonesPopulation=selectPopulation(initialPopulationSize-newToAdd);
		int i=0;
		for(Antibody p:clonesPopulation) {
			population[i]=(Antibody) p.clone();
			i++;
		}
		createRandomMembers(newToAdd);
		Arrays.sort(population);
		System.out.println(Arrays.toString(population[0].getFitnessVector()));
	}

	private Antibody[] selectPopulation(int i) {
		Arrays.sort(clonesPopulation);
		return Arrays.copyOfRange(clonesPopulation, 0, i);
	}
	
	private void createRandomMembers(int n) {
		int k=population.length-1;
		for(int i=0;i<n;i++) {
			createNewPopulationMember(k);
			k--;
		}
	}


	@Override
	public void stop() throws SchedulingException {
	}

	@Override
	public void use(ISchedulingResult result) throws SchedulingException {
		Antibody a=Antibody.convertFromResult(result);
		ReservationManager2 man=new ReservationManager2(fixedReservationManager);
		a.rebuildManager(man);
		a.evaluate(man);
		population[a.compareTo(population[0])+1]=a;

	}
	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public Component getExecutionFeedback() throws SchedulingException {
		return null;
	}
	
	
	@Override
	public ISchedulingResult getResult() throws SchedulingException {
		return population[0].toResult();
	}

	@Override
	public ISchedulingResult[] getResults() throws SchedulingException {
		ISchedulingResult[] results = new ISchedulingResult[population.length];
		for(int i=0;i<population.length;i++)
			results[i]=population[i].toResult();
		return results;
	}

	@Override
	public SchedulingAlgorithmStatus getStatus() { return null; }
	
	@Override
	public void registerSchedulingMonitor(ISchedulingMonitor sm) throws SchedulingException {}
	

}
