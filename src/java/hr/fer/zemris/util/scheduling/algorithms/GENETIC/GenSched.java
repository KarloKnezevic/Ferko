package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import hr.fer.zemris.util.scheduling.support.ISchedulingAlgorithm;
import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingMonitor;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.SchedulingAlgorithmStatus;
import hr.fer.zemris.util.scheduling.support.SchedulingException;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;

public class GenSched implements ISchedulingAlgorithm {
	private IPlan plan;
	private Map<String, ISchedulingData> eventsSchedulingData;
	private List<IEvent> events;
	private GenResMan manager;

	private boolean studentDistribution;
	private boolean termSequence;

	private Map<String, DateStamp> lastDates = new HashMap<String, DateStamp>();
	private Map<String, DateStamp> firstDates = new HashMap<String, DateStamp>();

	private Map<String, Integer> preconditionTimes = new HashMap<String, Integer>();

	private static SchedulingAlgorithmStatus status;
	private static ISchedulingMonitor monitor;

	private List<Chromosome> population = new ArrayList<Chromosome>();
	
	private boolean working = false;

	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public JPanel getExecutionFeedback() throws SchedulingException {
		return Util.getFeedBackPanel();
	}

	@Override
	public ISchedulingResult getResult() throws SchedulingException {
		return population.get(0).toResult();
	}

	@Override
	public ISchedulingResult[] getResults() throws SchedulingException {
		ISchedulingResult[] results = new ISchedulingResult[population.size()];
		
		for(int i = 0; i < population.size(); i++) {
			results[i] = population.get(i).toResult();
		}
		
		return results;
	}

	@Override
	public SchedulingAlgorithmStatus getStatus() {
		return status;
	}

	@Override
	public void registerSchedulingMonitor(ISchedulingMonitor sm)
			throws SchedulingException {
		monitor = sm;
	}

	@Override
	public void start() throws SchedulingException {
		this.working = true;
		
//		Thread workingThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (working) {
//					GenSched.this.step();
//				}
//			}
//		});
//		
//		workingThread.run();
	}

	@Override
	public void step() throws SchedulingException {
		// nova populacija
		List<Chromosome> newPopulation = new ArrayList<Chromosome>();

		// elitizam
		for (int i = 0; i < Util.elitismNumber; i++) {
			newPopulation.add(population.get(i));
		}

		// nove jedinke
		Chromosome chromosome;
		int newIndividuals = 0;
		for (newIndividuals = 0; newIndividuals < Util.newIndividuals; newIndividuals++) {
			chromosome = new Chromosome(GenSched.this, manager);

			if (!chromosome.generateEvents()) {
				break;
			}

			newPopulation.add(chromosome);
			manager.clearReservations();
		}

		// krizanje
		int crossoverNumber = Util.populationSize - Util.elitismNumber
				- newIndividuals;
		for (int i = 0; i < crossoverNumber; i++) {
			Chromosome parent1 = population.get(Util.randomPopElem());

			if (Util.random() < Util.crossover) {
				Chromosome parent2 = population.get(Util.randomPopElem());
				Chromosome child = new Chromosome(this, manager);

				child.crossover(parent1, parent2);
				child.optimize();

				newPopulation.add(child);
				manager.clearReservations();
			} else {
				newPopulation.add(parent1);
			}
		}

		// mutacija
		for (int i = Util.elitismNumber; i < newPopulation.size(); i++) {
			Chromosome chr = newPopulation.get(i);
			chr.fillReservations();

			chr.mutate();
			chr.optimize();
			chr.calculateFitness();

			manager.clearReservations();
		}

		// evaluacija
		Collections.sort(newPopulation);
		
		// nova populacija
		population = newPopulation;
	}

	@Override
	public void stop() throws SchedulingException {
		this.working = false;
	}

	@Override
	public void use(ISchedulingResult result) throws SchedulingException {
		population.remove(population.size() - 1);
		
		Chromosome newIndividual = new Chromosome(this, manager);
		newIndividual.importResult(result);
		newIndividual.calculateFitness();
		
		population.add(newIndividual);
		manager.clearReservations();
		Collections.sort(population);
	}

	@Override
	public void prepare(IPlan localPlan,
			Map<String, ISchedulingData> localEventsSchedulingData)
			throws SchedulingException {
		// inicijalizacija globalnih varijabli
		this.plan = localPlan;
		this.eventsSchedulingData = localEventsSchedulingData;
		this.events = plan.getPlanEvents();

		// inicijalizacija zastavica
		this.studentDistribution = plan.isEqualStudentDistributionInEachEvent();
		this.termSequence = plan.isEqualTermSequenceInEachEvent();
		
		//priprema managera
		this.manager = new GenResMan(eventsSchedulingData);

		// trazenje prvih i zadnjih datuma za evente
		findFirstLastDates(plan, events, firstDates, lastDates);

		// uredjivanje preduvjeta
		updatePreconditions(events, preconditionTimes, lastDates);

		// stvaranje populacije kromosoma i update statusa
		Chromosome cromosom;
//		Util.printFeedback(Util.currentTime());
//		Util.printFeedback("---------------");

		for (int i = 0; i < Util.populationSize; i++) {
			cromosom = new Chromosome(this, manager);
			if (!cromosom.generateEvents()) {
				
				i--;
				manager.clearReservations();
				
				continue;
			}

			cromosom.setNumber(i);
			population.add(cromosom);
			manager.clearReservations();
		}

		Collections.sort(population);

//		for (int i = 0; i < 10; i++) {
//			Chromosome crom = population.get(i);
//			Util.printFeedback(crom.getNumber() + " = " + crom.getFitness());
//		}

//		Util.printFeedback("---------------");
//		Util.printFeedback(Util.currentTime());

//		if (status == SchedulingAlgorithmStatus.RUNNING)
//			status = SchedulingAlgorithmStatus.PREPARED;
//		else
//			status = SchedulingAlgorithmStatus.FAILURE;
//
//		status = SchedulingAlgorithmStatus.RUNNING;
	}

	public List<IEvent> getEvents() {
		return this.events;
	}

	public IPlan getPlan() {
		return plan;
	}

	public List<String> getPeople(String eventId) {
		List<String> list = new ArrayList<String>();
		list.addAll(eventsSchedulingData.get(eventId).getPeopleData().keySet());
		return list;
	}

	public DateStamp getLatestDate(IEvent event) {
		return lastDates.get(event.getId());
	}

	public void setLatestDate(IEvent event, DateStamp latestDate) {
		lastDates.put(event.getId(), latestDate);
	}

	// pronalazak prvih i zadnjih datuma za evente
	private void findFirstLastDates(IPlan plan, List<IEvent> events,
			Map<String, DateStamp> firstDates, Map<String, DateStamp> lastDates) {
		// za svaki event
		for (IEvent event : events) {
			// dohvacanje time parametara od svakog eventa
			List<ITimeParameter> params = event.getDefinition()
					.getTimeParameters();
			if (params.size() == 0)
				params = plan.getDefinition().getTimeParameters();

			DateStamp min = params.get(0).getFromDate();
			DateStamp max = params.get(0).getToDate();

			// pronalazak najmanjeg i najveceg DateStampa
			for (ITimeParameter param : params) {
				if (param.getFromDate().compareTo(min) < 0)
					min = param.getFromDate();
				if (param.getToDate().compareTo(max) > 0)
					max = param.getToDate();
			}

			// zapisivanje vrijednosti
			firstDates.put(event.getId(), min);
			lastDates.put(event.getId(), max);
		}
	}

	// uredjuje se baza preduvjeta
	private void updatePreconditions(List<IEvent> events,
			Map<String, Integer> preconditionTimes,
			Map<String, DateStamp> lastDates) {
		// za svaki event
		for (IEvent event : events) {
			// za svaki preduvjet (ako ga ima)
			for (IPrecondition pre : event.getPreconditionEvents()) {
				// pokreni rekurzivno azuriranje vremena
				updatePreconditionsRec(getEventByPrecondition(pre.toString()),
						Util.getDistance(pre.getTimeDistance()), lastDates);
			}
		}
	}

	// uredjuje se baza preduvjeta za dane rekurzivno
	private void updatePreconditionsRec(IEvent event, int days,
			Map<String, DateStamp> lastDates) {
		// biljezenje zadnjeg datuma i racunanje novog
		DateStamp dateStamp = lastDates.get(event.getId());
		DateStamp newStamp = Util.dayCalc(dateStamp, -days - 1);

		// zapisivanje umanjenog datuma
		lastDates.put(event.getId(), newStamp);

		// rekurzivno dalje pomicanje datuma
		for (IPrecondition precondition : event.getPreconditionEvents()) {
			updatePreconditionsRec(getEventByPrecondition(precondition
					.toString()), days, lastDates);
		}
	}

	// metoda na temelju stringa iz toString vraca IEvent o kojem se radi
	public IEvent getEventByPrecondition(String prec) {
		String name = prec.substring(0, prec.indexOf(" "));
		for (IEvent event : events) {
			if (event.getName().equals(name))
				return event;
		}
		return null;
	}
}
