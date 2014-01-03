package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// razred kromosom. jedno rjesenje problema
public class Chromosome implements Comparable<Chromosome> {
	private GenSched scheduler;
	private GenResMan manager;
	private int number;

	private List<GenEvent> cromosomEvents = new LinkedList<GenEvent>();

	private FitnessResult fitnessResult;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	// konstruktor kromosoma
	public Chromosome(GenSched scheduler, GenResMan manager) {
		this.scheduler = scheduler;
		this.manager = manager;
	}

	// import iz rezultata
	public void importResult(ISchedulingResult result) {
		// dohvati podatke o planu i originalnim eventima
		IPlan plan = result.getPlan();
		List<IEvent> events = scheduler.getEvents();

		// za svaki event iz plana
		for (IEvent event : plan.getPlanEvents()) {
			// priprema lokalnih varijabli
			GenEvent gEvent = null;
			IEvent origEvent = null;

			// trazi originalni event
			for (IEvent temp : events) {
				if (temp.getId().equals(event.getId())) {
					origEvent = temp;
				}
			}

			// inicijalizacija eventa
			gEvent = new GenEvent(this, origEvent, manager);

			// dohvati termine
			List<ITerm> terms = origEvent.getTerms();

			// za svaki termin iz eventa
			for (ITerm term : event.getTerms()) {
				// priprema lokalnih varijabli
				GenTerm gTerm = null;
				ITerm origTerm = null;

				// trazi originalni termin
				for (ITerm temp : terms) {
					if (temp.getName().equals(term.getName())) {
						origTerm = temp;
					}
				}

				// inicijalizacija termina
				gTerm = new GenTerm(gEvent, origTerm, manager);

				// dohvacanje time parametara
				ITimeParameter time = term.getDefinition().getTimeParameters()
						.get(0);

				// postavljanje parametara
				gTerm.setDate(time.getFromDate());
				gTerm
						.setSpan(new TimeSpan(time.getFromTime(), time
								.getToTime()));

				// dohvacanje location parametara
				ILocationParameter loc = term.getDefinition()
						.getLocationParameters().get(0);
				gTerm.setRoom(new RoomData(loc.getId(), loc.getName(), loc
						.getActualCapacity()));

				// dodavanje termina
				gEvent.addTerm(gTerm);

				// dodavanje studenata u termin
				gTerm.reserveIndividuals(term.getDefinition().getIndividuals());
			}

			gEvent.updateLastDate();
			// dodavanje eventa
			cromosomEvents.add(gEvent);
		}
	}

	// stvaranje novih evenata
	public boolean generateEvents() {
		// zastaviza za ponavljanje generacije
		Boolean result = false;

		while (true) {
			for (IEvent event : scheduler.getEvents()) {
				GenEvent gEvent = new GenEvent(this, event, manager);

				// pokusaj stvoriti novi event
				result = gEvent.createEvent();

				// dodaj event
				cromosomEvents.add(gEvent);

				// ako treba ponovno generiranje, izbrisi trenutne evente
				if (result == null) {
					for (GenEvent genEvent : cromosomEvents)
						genEvent.freeTerms();

					cromosomEvents = new LinkedList<GenEvent>();

					// System.out
					// .println("\nMijenjanje zadnjih datuma i ponovno generiranje evenata...\n");

					break;
				}

				// ako ne moze nikako izgraditi raspored, vrati false
				if (!result)
					return false;
			}

			if (result == null)
				continue;

			this.optimize();
			this.calculateFitness();
			return true;
		}
	}

	// metoda krizanja
	public void crossover(Chromosome parent1, Chromosome parent2) {
		// za svaki event
		for (IEvent event : scheduler.getEvents()) {
			// stvori novi event
			GenEvent gEvent = new GenEvent(this, event, manager);
			List<GenTerm> childTerms = new ArrayList<GenTerm>();
			DateStamp firstDate = null;

			for (IPrecondition prec : event.getPreconditionEvents()) {
				IEvent precEvent = scheduler.getEventByPrecondition(prec
						.toString());
				GenEvent pEvent = this.getCromosomEvent(precEvent.getId());

				DateStamp precDate = Util.dayCalc(pEvent.getLastDate(), Integer
						.parseInt(prec.getTimeDistance().substring(0, 1)) + 1);

				if (firstDate == null) {
					firstDate = precDate;
				} else {
					if (precDate.compareTo(firstDate) > 0)
						firstDate = precDate;
				}
			}

			// dohvati termine roditelja i sortiraj ih
			List<GenTerm> terms1 = parent1.getCromosomEvent(event.getId())
					.getTerms();
			List<GenTerm> terms2 = parent2.getCromosomEvent(event.getId())
					.getTerms();

			// ako je given distribucija
			if (event.getEventDistribution().getType() != 6) {
				// uzimaju se bolji termini od para
				for (int i = 0; i < terms1.size(); i++) {
					GenTerm newTerm1 = new GenTerm(terms1.get(i), gEvent);
					GenTerm newTerm2 = new GenTerm(terms2.get(i), gEvent);

					if (firstDate == null)
						firstDate = (newTerm1.getDate().compareTo(
								newTerm2.getDate()) <= 0 ? newTerm1.getDate()
								: newTerm2.getDate());

					if (terms1.get(i).compareTo(terms2.get(i)) <= 0) {
						if (!childTerms.contains(newTerm1)
								&& firstDate.compareTo(newTerm1.getDate()) <= 0) {
							childTerms.add(newTerm1);
						} else {
							childTerms.add(newTerm2);
						}
					} else {
						if (!childTerms.contains(newTerm2)
								&& firstDate.compareTo(newTerm2.getDate()) <= 0) {
							childTerms.add(newTerm2);
						} else {
							childTerms.add(newTerm1);
						}
					}
				}

				// ako studenti nisu na razini termina
				if (!parent1.getCromosomEvent(event.getId())
						.studentsOnTermLevel()) {
					// izbrisi sve studente iz termina
					for (GenTerm term : childTerms)
						term.freeStudents();

					// oslobodi studente iz prijasnjih termina
					// for (GenTerm term : terms1)
					// term.unreserveStudentsWithoutRemoval();
					// for (GenTerm term : terms2)
					// term.unreserveStudentsWithoutRemoval();

					// dohvati sve studente za razmjestiti
					List<String> studentPool = gEvent.getIndividuals();

					// razmjesti sve studente
					gEvent
							.organizeStudents(childTerms, studentPool, true,
									true);
				}
			} else {
				// ako je random razdioba, spoji sve termine
				LinkedList<GenTerm> allTerms = new LinkedList<GenTerm>();
				allTerms.addAll(terms1);
				allTerms.addAll(terms2);
				Collections.sort(allTerms);

				List<String> studentPool = gEvent.getIndividuals();

				// oslobodi studente bez micanja iz termina
				// for (GenTerm term : allTerms)
				// term.unreserveStudentsWithoutRemoval();

				for (int i = 0; i < gEvent.getMinTerms(); i++) {
					childTerms.add(new GenTerm(allTerms.remove(0), gEvent));
				}

				// praznjenje termina
				for (GenTerm term : childTerms)
					term.freeStudents();

				// probaj smjestiti studente u minimalan broj termina
				studentPool = gEvent.organizeStudents(childTerms, studentPool,
						false, false);

				boolean allStudents = false;
				int cicle = 0;
				LinkedList<GenTerm> badTerms = new LinkedList<GenTerm>();

				// dok god je potrebno dodati novi termin i on se moze dodati
				while (cicle < 2) {
					while (studentPool.size() != 0
							&& childTerms.size() < gEvent.getMaxTerms()
							&& allTerms.size() != 0) {
						// uzmi novi termin
						GenTerm newTerm = new GenTerm(allTerms.remove(0),
								gEvent);
						newTerm.freeStudents();

						if (firstDate == null)
							firstDate = newTerm.getDate();

						if (childTerms.contains(newTerm)
								|| firstDate.compareTo(newTerm.getDate()) > 0)
							continue;

						// lista za smjestanje
						List<GenTerm> newList = new ArrayList<GenTerm>();
						newList.add(newTerm);

						// pokusaj smjestiti studente
						studentPool = gEvent.organizeStudents(newList,
								studentPool, allStudents, false);

						// ako je barem neki student smjesten
						if (newTerm.getOccupationNumber() != 0) {
							// dodaj u listu odabranih termina
							childTerms.add(newTerm);
						} else {
							if (cicle == 0) {
								if (!badTerms.contains(newTerm))
									badTerms.add(newTerm);
							}

							if (cicle == 1) {
								// inace, prebaci u smjestanje svih studenata
								allStudents = true;
								allTerms.addFirst(newTerm);
							}
						}
					}

					if (studentPool.size() != 0) {
						allTerms = badTerms;
						cicle++;
					} else {
						break;
					}
				}

				// ako jos ima nerazvrstanih studenata, forsiraj smjestanje
				if (studentPool.size() != 0)
					gEvent
							.organizeStudents(childTerms, studentPool, true,
									true);
			}

			gEvent.addTerms(childTerms);
			gEvent.updateLastDate();
			cromosomEvents.add(gEvent);
		}
	}

	// metoda dohvaca lokacijske parametre
	public List<ILocationParameter> getLocationParameters() {
		return scheduler.getPlan().getDefinition().getLocationParameters();
	}

	// metoda dohvaca vremenske parametre
	public List<ITimeParameter> getTimeParameters(IEvent event) {
		List<ITimeParameter> params = new ArrayList<ITimeParameter>();
		params.addAll(scheduler.getPlan().getDefinition().getTimeParameters());

		return timeParameterCorrection(params, event);
	}

	// metoda vrsi korekciju vremenskih parametara s obzirom na unaprijed
	// definirane prve i zadnje dane
	public List<ITimeParameter> timeParameterCorrection(
			List<ITimeParameter> params, IEvent event) {
		List<ITimeParameter> newParams = new ArrayList<ITimeParameter>();

		DateStamp latestDate = scheduler.getLatestDate(event);

		// za svaki time parametar definiran u listi
		for (ITimeParameter param : params) {
			// provjera je li datum pocetka manji ili jednak najkasnijem datumu,
			// odnosno ulazi li uopce taj parametar u daljnju obradu
			if (param.getFromDate().compareTo(latestDate) <= 0) {
				// provjera je li datum kraja veci od najkasnijeg datuma, jer
				// ako je, mora se manjiti na najkasniji datum
				if (param.getToDate().compareTo(latestDate) > 0) {
					GenTimeParam gtp = new GenTimeParam(param.getFromDate(),
							param.getFromTime(), latestDate, new TimeStamp(20,
									0));
					newParams.add(gtp);
				} else {
					// ako nema korekcija, samo dodaj takav kakav je
					newParams.add(param);
				}
			}
		}

		return newParams;
	}

	public List<String> getIndividuals() {
		return scheduler.getPlan().getDefinition().getIndividuals();
	}

	public GenEvent getCromosomEvent(String eventId) {
		for (GenEvent gEvent : cromosomEvents)
			if (gEvent.getEventId().equals(eventId))
				return gEvent;

		return null;
	}

	public GenSched getScheduler() {
		return scheduler;
	}

	public FitnessResult calculateFitness() {
		fitnessResult = new FitnessResult();

		int crowded = 0;
		int conflicted = 0;
		int terms = 0;
		float timeSum = 0;

		for (GenEvent gEvent : cromosomEvents) {
			FitnessResult eventResult = gEvent.calculateFitness();

			crowded += eventResult.getValue(0);
			conflicted += eventResult.getValue(1);
			terms += eventResult.getValue(2);
			timeSum = eventResult.getValue(3);
		}

		// broj prenapucenih dvorana
		fitnessResult.add(crowded);

		// broj konfliktnih studenata
		fitnessResult.add(conflicted);

		// broj termina
		fitnessResult.add(terms);

		// prosjecna udaljenost termina od sredine dana
		fitnessResult.add(timeSum / cromosomEvents.size());

		return fitnessResult;
	}

	public FitnessResult getFitness() {
		return fitnessResult;
	}

	// rezervira sve resurse kromosoma
	public void fillReservations() {
		for (GenEvent gEvent : cromosomEvents)
			gEvent.fillReservations();
	}

	public void mutate() {
		// ako se mutacija izvodi
		if (Util.random() < Util.mutation) {
			GenEvent gEvent = cromosomEvents.get(Util.random(cromosomEvents
					.size()));
			gEvent.mutate();
		}
	}

	public void optimize() {
		for (GenEvent gEvent : cromosomEvents)
			gEvent.optimize();
	}

	@Override
	public int compareTo(Chromosome o) {
		return this.getFitness().compareTo(o.getFitness());
	}

	public ISchedulingResult toResult() {
//		System.out.println(this.toString() + "\n");
		ISchedulingResult result = new SchedulingResult();
		result.addPlan(this.scheduler.getPlan().getName());

		for (GenEvent gEvent : cromosomEvents) {
			result.addEvent(gEvent.getEventName(), gEvent.getEventId());

			for (GenTerm term : gEvent.getTerms()) {
				result.addTerm(gEvent.getEventName(), term.getName(), term
						.getRoom().getId(), term.getRoom().getCapacity(), term
						.getDate().getStamp(), term.getSpan().getStart()
						.getAbsoluteTime(), term.getSpan().getEnd()
						.getAbsoluteTime());

				for (String student : term.getStudents()) {
					result.addStudentToTerm(gEvent.getEventName(), term
							.getName(), student);
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		String s = "";
		for (GenEvent gEvent : cromosomEvents)
			s += gEvent + "\n";
		return s;
	}
}
