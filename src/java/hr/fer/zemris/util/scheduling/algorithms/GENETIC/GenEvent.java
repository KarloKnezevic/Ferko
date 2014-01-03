package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import hr.fer.zemris.jcms.model.planning.PlanEvent.Precondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IGroup;
import hr.fer.zemris.util.scheduling.support.algorithmview.ILocationParameter;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeStamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.event.ListSelectionEvent;

//razred GenEvent. jedan generirani event unutar kromosoma
public class GenEvent {
	private Chromosome cromosom;
	private IEvent event;
	private GenResMan manager;

	private List<GenTerm> eventTerms = new ArrayList<GenTerm>();

	private int minTerms;
	private int maxTerms;

	private DateStamp lastDate;

	private List<String> individuals;
	private List<String> studentPool;

	private Map<DateStamp, List<Integer>> except;
	private List<GenTerm> badTerms;

	private FitnessResult fitnessResult;
	private boolean randomDistribution = false;
	private boolean studentsOnTermLevel = false;
	
	private int termId = 0;

	// konstruktor. postavljaju se privatne varijable i ispituje distribucija
	public GenEvent(Chromosome cromosom, IEvent event, GenResMan manager) {
		this.cromosom = cromosom;
		this.event = event;
		this.manager = manager;

		individuals = getIndividuals();
		studentPool = new ArrayList<String>();

		this.except = new HashMap<DateStamp, List<Integer>>();
		this.badTerms = new ArrayList<GenTerm>();

		// provjera distribucije za zadani event
		if (event.getEventDistribution().getType() == 6) {
			this.randomDistribution = true;
			this.minTerms = event.getEventDistribution().getMinimumTermNumber();
			this.maxTerms = event.getEventDistribution().getMaximumTermNumber();
		} else {
			this.randomDistribution = false;
		}
	}

	public Boolean createEvent() {
		// Util.printFeedback("Event: " + this.event.getId());
		// provjera o kakvoj se distribuciji radi i pokretanje odgovarajuceg
		// nacina stvaranja termina
		if (randomDistribution) {
			// random distribucija
			// ako ne moze generirati termine kako treba
			if (!generateRandomTerms())
				return configurePreconditions();
		} else {
			// given distribucija
			// ako ne moze generirati termine kako treba
			if (!generateFixdTerms())
				return configurePreconditions();
		}

		// azuriranje zadnjeg datuma
		updateLastDate();

		// for (GenTerm term : eventTerms)
		// Util.printFeedback(term.getRoom().getId() + " " + term.getDate()
		// + " " + term.getSpan() + " " + term.getReservedStudents()
		// + " " + term.getConflictedStudents() + " "
		// + term.getOccupation() + " " + term.getCapacity());
		//
		// Util.printFeedback("");
		return true;
	}

	// rezervira sve resurse eventa
	public void fillReservations() {
		for (GenTerm term : eventTerms)
			term.fillReservations();
	}

	// metoda koja smanjuje zadnji datum preduvjeta za 1 ako se trenutni event
	// ne moze generirati
	private Boolean configurePreconditions() {
		// ako nema preduvjeta, vrati false, ne moze generirati
		if (event.getPreconditionEvents().size() == 0)
			return false;
		else {
			// inace, smanji zadnje datume svakog preduvjeta za 1 i
			// spremi u listu zadnjih datuma
			for (IPrecondition prec : event.getPreconditionEvents()) {
				IEvent event = cromosom.getScheduler().getEventByPrecondition(
						prec.toString());
				DateStamp lastDate = cromosom.getScheduler().getLatestDate(
						event);
				lastDate = Util.dayCalc(lastDate, -1);
				cromosom.getScheduler().setLatestDate(event, lastDate);
			}

			// vrati null, znak da se rjesenje odbaci i generira novo
			return null;
		}
	}

	// generiranje termina ako je raspodijela 'given'
	private boolean generateFixdTerms() {
		// pokusaj 'naivnog' odabira svih termina
		for (ITerm term : event.getTerms()) {
			GenTerm newTerm = generateNewTerm(term);

			// ispitaj je li termin nadjen
			if (newTerm != null) {
				// ako je, rezerviraj ga i dodaj medju termine
				newTerm.reserveTerm();
				eventTerms.add(newTerm);
			} else {
				// ako nije, vrati false, ne moze se generirati raspored
				return false;
			}
		}

		// rasporedi studente ako su definirani na razini eventa ili plana
		if (individuals.size() != 0)
			organizeStudents(eventTerms, individuals, true, true);

		return true;
	}

	private boolean generateRandomTerms() {
		// stvaranje minimalnog broja termina
		for (int i = 0; i < minTerms; i++) {
			GenTerm newTerm = generateNewTerm(null);

			// ispitaj je li termin nadjen
			if (newTerm != null) {
				// ako je, rezerviraj ga i dodaj medju termine
				newTerm.reserveTerm();
				eventTerms.add(newTerm);
			} else {
				// ako nije, vrati false, raspored nije moguce izraditi
				return false;
			}
		}

		// smjestanje studenata u minimalan broj termina
		studentPool = individuals;
		studentPool = organizeStudents(eventTerms, studentPool, false, false);

		// zastavica za smjestanje svih studenata
		boolean allStudents = false;

		// dok god je potreban termin (a moguce ga je dodati), dodaje se novi
		// termin i smjestaju se studenti u njega
		while (studentPool.size() != 0 && eventTerms.size() < this.maxTerms) {
			GenTerm newTerm = generateNewTerm(null);

			// ispitaj novi termin
			if (newTerm != null) {
				// pokusaj smjestiti studente unutra
				List<GenTerm> list = new ArrayList<GenTerm>();
				list.add(newTerm);

				studentPool = organizeStudents(list, studentPool, allStudents,
						false);

				// ako je smjesten barem jedan student
				if (newTerm.getOccupationNumber() != 0) {
					// rezerviraj termin
					newTerm.reserveTerm();
					eventTerms.add(newTerm);
				} else {
					// inace, vrati termin u lose termine, oznaci zastavicu i
					// probaj rasporediti medju konfliktna mjesta preostale
					// studente
					badTerms.add(newTerm);
					allStudents = true;
					studentPool = organizeStudents(eventTerms, studentPool,
							true, false);
				}
			} else {
				// ako nema novog termina, izadji
				break;
			}
		}

		// ako vise nema termina ili je postignut maksimalan broj termina,
		// forsiraj raspored studenata
		if (studentPool.size() != 0)
			organizeStudents(eventTerms, studentPool, true, true);

		// rasporedjivanje gotovo
		return true;
	}

	// realizacija mutacije za event
	public void mutate() {
		if (studentsOnTermLevel || eventTerms.size() < 2) {
			mutate1();
		} else {
			int choice = Util.random(3);

			if (choice == 0)
				mutate1();

			if (choice == 1)
				mutate2();

			if (choice == 2)
				mutate3();
		}
	}

	// nalazak novog spana za jedan termin i preseljenje svih studenata - jedina
	// opcija za studente definirane na razini termina
	private void mutate1() {
		// random odabir termina koji se uklanja
		GenTerm oldTerm = eventTerms.remove(Util.random(eventTerms.size()));

		// stvaranje novog termina
		GenTerm newTerm = generateNewTerm(oldTerm.getTerm());

		// ako je novi termin nadjen
		if (newTerm != null) {
			// oslobadjanje starog termina i spremanje studenata
			List<String> students = oldTerm.getStudents();
			oldTerm.freeReservations();

			// provjera nalazi li se stari termin u except
			List<Integer> exceptList = except.get(oldTerm.getDate());
			if (exceptList != null
					&& exceptList.contains(oldTerm.getSpan().getStart()
							.getAbsoluteTime())) {
				badTerms.add(oldTerm);
			}

			// rezervacija i dodavanje novog termina
			newTerm.reserveTerm();
			eventTerms.add(newTerm);

			// ako su studenti definirani vise od termina
			if (!studentsOnTermLevel) {
				// smjesti koliko ih stane u novi termin
				List<GenTerm> list = new ArrayList<GenTerm>();
				list.add(newTerm);
				students = organizeStudents(list, students, false, false);

				// ako nisu svi smjesteni (nova dvorana je manja), smjesti u
				// druge termine
				if (students.size() != 0)
					organizeStudents(eventTerms, students, true, true);
			} else {
				// ako su definirani na terminu, dodaj u novi termin
				newTerm.reserveIndividuals(students);
			}
		} else {
			// ako novi termin nije nadjen, nema promjene, mutacija se ne moze
			// izvrsiti, vrati stari termin
			eventTerms.add(oldTerm);
		}

		// azuriranje zadnjeg datuma
		updateLastDate();
	}

	// potpuna medjusobna zamjena studenata dvaju termina
	private void mutate2() {
		// dohvati nasumicno dva termina
		GenTerm term1 = eventTerms.remove(Util.random(eventTerms.size()));
		GenTerm term2 = eventTerms.remove(Util.random(eventTerms.size()));
		List<GenTerm> list1 = new ArrayList<GenTerm>();
		List<GenTerm> list2 = new ArrayList<GenTerm>();

		// dohvati studente
		List<String> stud1 = term1.getStudents();
		List<String> stud2 = term2.getStudents();

		// oslobodi studente
		term1.freeStudents();
		term2.freeStudents();

		// pripremi liste
		list1.add(term1);
		list2.add(term2);

		// pokusaj normalno raporediti studente
		stud1 = organizeStudents(list2, stud1, true, false);
		stud2 = organizeStudents(list1, stud2, true, false);

		// rasporedi ostale koji nisu dosad rasporedjeni
		if (stud1.size() != 0)
			organizeStudents(eventTerms, stud1, true, true);
		if (stud2.size() != 0)
			organizeStudents(eventTerms, stud2, true, false);

		// vrati termine u listu
		eventTerms.add(term1);
		eventTerms.add(term2);
	}

	// preseljenje studenata izmedju termina - neovisno (x% od svakog termina u
	// neki drugi)
	private void mutate3() {
		// za svaki termin
		for (int count = 0; count < eventTerms.size(); count++) {
			// uzima se prvi termin
			GenTerm src = eventTerms.remove(0);

			// dohvacaju se studenti tog termina
			List<String> stud = src.getStudents();

			// radi se x% iteracija
			for (int i = 0; i < Math.round(Util.mutStudCount * stud.size()); i++) {
				// uzima se random student iz liste
				String jmbag = stud.remove(Util.random(stud.size()));
				src.freeindividual(jmbag);

				// odabire se random termin za odrediste
				GenTerm dest = eventTerms.get(Util.random(eventTerms.size()));

				// ako nema mjesta u odredisnom terminu
				if (!dest.isAvailable()) {
					// zamjena studenata, odaberi drugog
					List<String> stud2 = dest.getStudents();
					String jmbag2 = stud2.remove(Util.random(stud2.size()));
					dest.freeindividual(jmbag2);

					// dodaj drugog u prvi termin
					src.reserveIndividual(jmbag2);
				}

				// prvi student se smjesta u odredisni termin
				dest.reserveIndividual(jmbag);
			}

			// termin se vraca na kraj liste
			eventTerms.add(src);
		}
	}

	public String getEventId() {
		return this.event.getId();
	}
	
	public String getEventName() {
		return this.event.getName();
	}
	
	public int getTermNumber() {
		return this.eventTerms.size();
	}

	public void freeTerms() {
		for (GenTerm term : eventTerms)
			term.freeReservations();
	}

	public FitnessResult calculateFitness() {
		fitnessResult = new FitnessResult();

		int crowded = 0;
		int conflicted = 0;
		int terms = eventTerms.size();
		int timeSum = 0;

		for (GenTerm term : eventTerms) {
			FitnessResult termResult = term.calculateFitness();

			if (termResult.getValue(0) > 1)
				crowded++;

			conflicted += termResult.getValue(1);

			timeSum += termResult.getValue(2);
		}

		// broj prenapucenih dvorana
		fitnessResult.add(crowded);

		// broj konfliktnih studenata
		fitnessResult.add(conflicted);

		// broj termina
		fitnessResult.add(terms);

		// prosjecna udaljenost termina od sredine dana
		fitnessResult.add(((float) timeSum) / terms);

		return fitnessResult;
	}

	// dohvaca random dobar termin. dobar termin je termin koji u potpunosti
	// odgovara barem jednom studentu. ako nije moguce dohvatiti dobar termin,
	// dohvacaju se svi ostali sve dok je moguce dohvatiti bilo kakav termin
	private GenTerm generateNewTerm(ITerm term) {
		while (true) {
			// stvaranje novog termina
			GenTerm newTerm = new GenTerm(this, term, manager);

			// pokusaj stvaranja novog termina
			Boolean status = newTerm.findNewTerm(except);

			// ako je uredno nadjen novi termin
			if (status != null) {
				// ako zasad nema problema sa studentima
				if (newTerm.status()) {
					// ako studenti nisu dodijeljeni na razini termina, potrebno
					// je ispitati je li termin dobar na razini eventa
					if (newTerm.getReservedStudentsNumber() == 0) {
						// ako je termin dobar, vrati ga
						if (isTermUsable(newTerm)) {
							return newTerm;
						} else {
							// ak nije dobar na razini eventa, dodaj u except i
							// badTerms i trazi novi termin
							addToExcept(newTerm);
							badTerms.add(newTerm);
							newTerm.setStatus(false);
							continue;
						}
					} else {
						// ako su studenti dodijeljeni na razini termina, samo
						// vrati termin
						return newTerm;
					}
				} else {
					// ako nije dodan ni jedan student na razini termina, dodaj
					// u except i badTerms i trazi novi termin
					addToExcept(newTerm);
					badTerms.add(newTerm);
					continue;
				}
			} else {
				// ako nije nadjen termin, provjeri postoje li neki zapisi u
				// badTerms
				if (badTerms.size() != 0) {
					// ako postoje, odaberi random los termin
					GenTerm badTerm = badTerms.remove(Util.random(badTerms
							.size()));
					return badTerm;
				} else {
					// ako ne postoje, vrati null
					return null;
				}
			}
		}
	}

	// metoda ispituje moze li se navedeni termin koristiti. termin se moze
	// koristiti ako barem jednom studentu odgovara
	private boolean isTermUsable(GenTerm term) {
		// bira se po kojoj se listi provjerava
		List<String> list = (studentPool.size() != 0 ? studentPool
				: individuals);

		// provjera
		if (list.size() != 0) {
			for (String student : list) {
				if (!manager.isStudentReserved(student, term.getDate(), term
						.getSpan()))
					return true;
			}
			return false;
		} else {
			return true;
		}
	}

	// metoda koja dodaje novu vrijednost u except mapu
	private void addToExcept(GenTerm term) {
		List<Integer> list = except.get(term.getDate());
		if (list == null) {
			list = new ArrayList<Integer>();
			except.put(term.getDate(), list);
		}

		list.add(term.getSpan().getStart().getAbsoluteTime());
	}

	public void updateLastDate() {
		DateStamp lastDate = null;

		for (GenTerm term : eventTerms) {
			// ako vec imamo zapisan najkasniji datum termina
			if (lastDate != null) {
				// ispitaj je li novi termin kasniji i zamijeni
				if (lastDate.compareTo(term.getDate()) < 0)
					lastDate = term.getDate();
			} else {
				// ako nije zapisan, zapisi kao zadnji
				lastDate = term.getDate();
			}
		}

		this.lastDate = lastDate;
	}

	// metoda koja obavlja rasporedjivanje studenata po dvoranama za fiksnu
	// raspodjelu
	public List<String> organizeStudents(List<GenTerm> terms,
			List<String> individuals, boolean organizeAll,
			boolean forceOrganization) {
		int i = 0;
		int k = 0;
		boolean flag, flag2;
		List<String> leftStudents = new ArrayList<String>();

		// ciklicko rasporedjivanje studenata u termine
		for (String jmbag : individuals) {
			flag = false;

			// iteriraj po svim terminima
			for (int j = i; j < i + terms.size(); j++) {
				int pos = j % terms.size();

				// ako je student slobodan u to vrijeme i dvorana moze primiti
				// jos studenata
				if (!manager.isStudentReserved(jmbag, terms.get(pos).getDate(),
						terms.get(pos).getSpan())
						&& terms.get(pos).isAvailable()) {
					// spremi ga u odabrani termin
					terms.get(pos).addReservedIndividual(jmbag);

					// promijeni defaultni termin
					i = (i + 1) % terms.size();
					flag = true;

					break;
				}
			}

			// ako student nije dodan
			if (!flag) {
				// dodaj konfliktnog studenta ili ga dodaj u listu nerazvrstanih
				if (organizeAll) {
					flag2 = false;
					// ako se dodaju konfliktni studenti, iteriraj ponovno po
					// svim terminima
					for (int j = k; j < k + terms.size(); j++) {
						int pos = j % terms.size();

						// ako je dvorana raspoloziva
						if (terms.get(pos).isAvailable()) {
							terms.get(pos).addConflictedIndividual(jmbag);
							flag2 = true;
							break;
						}
					}

					// ako su sve dovorane popunjene, ovisi o zastavici
					// forceOrganization hoce li se smjestati ili ne
					if (!flag2) {
						if (forceOrganization) {
							terms.get(k).addConflictedIndividual(jmbag);
						} else {
							leftStudents.add(jmbag);
						}
					}

					// na kraju, promijeni pocetak za dodavanje konflikata
					k = (k + 1) % terms.size();
				} else {
					// ako se vracaju nesmjesteni studenti, dodaj ga u listu
					leftStudents.add(jmbag);
				}
			}
		}

		// vrati listu studenata koji nisu smjesteni
		return leftStudents;
	}

	// metoda dohvaca lokacijske parametre. ako nisu definirani za zadani
	// event, gleda se nadredjeni kromosom
	public List<ILocationParameter> getLocationParameters() {
		List<ILocationParameter> params = event.getDefinition()
				.getLocationParameters();
		if (params.size() == 0)
			params = cromosom.getLocationParameters();

		return params;
	}

	// metoda dohvaca vremenske parametre. uzimaju se u obzir eventi preduvjeti
	// i njihovi zadnji datumi. ako vremenski parametri nisu definirani za
	// zadani event, gleda se nadredjeni kromosom
	public List<ITimeParameter> getTimeParameters() {
		List<ITimeParameter> params = event.getDefinition().getTimeParameters();
		if (params.size() == 0) {
			params = cromosom.getTimeParameters(event);
		} else {
			params = cromosom.timeParameterCorrection(params, event);
		}

		// ako ima preduvjeta, trazi najkasniji datum zavrsetka preduvjeta
		if (event.getPreconditionEvents().size() != 0) {
			DateStamp maxDate = null;
			for (IPrecondition prec : event.getPreconditionEvents()) {
				// biljezenje zadnjeg datuma eventa preduvjeta iz kromosoma
				DateStamp lastDate = cromosom.getCromosomEvent(cromosom
						.getScheduler().getEventByPrecondition(prec.toString())
						.getId()).lastDate;

				// dohvacanje udaljenosti u danima
				int intDist = Util.getDistance(prec.getTimeDistance());

				// ako je maxDate definiran
				if (maxDate != null) {
					// azuriraj maxDate ako je manji od novoga
					if (maxDate.compareTo(lastDate) < 0)
						maxDate = Util.dayCalc(lastDate, intDist + 1);
				} else {
					// inace azuriraj maxDate
					maxDate = Util.dayCalc(lastDate, intDist + 1);
				}
			}

			// prijedji po listi vremenskih parametara
			List<ITimeParameter> newList = new ArrayList<ITimeParameter>();
			for (ITimeParameter param : params) {
				// ako je zavrsni datum veci ili jednak maksimalnom datumu (ako
				// se
				// raspon uopce moze uzeti u obzir)
				if (param.getToDate().compareTo(maxDate) >= 0) {
					// ako je pocetni datum manji od maksimalnog datuma
					// preduvjeta
					if (param.getFromDate().compareTo(maxDate) < 0) {
						// pomkni pocetni datum na maksimalni za preduvjete
						GenTimeParam gtp = new GenTimeParam(maxDate,
								new TimeStamp(8, 0), param.getToDate(), param
										.getToTime());

						// dodaj u listu novih vremenskih parametara
						newList.add(gtp);
					} else {
						// inace, samo dodaj parametar kakav je
						newList.add(param);
					}
				}
			}

			// vrati listu novih parametara
			return newList;
		} else {
			// ako nema preduvjeta, vrati osnovnu listu parametara
			return params;
		}
	}

	public void setStudentsOnTermLevel(boolean flag) {
		studentsOnTermLevel = flag;
	}

	public boolean studentsOnTermLevel() {
		return this.studentsOnTermLevel;
	}

	public List<String> getIndividuals() {
		List<String> params = event.getDefinition().getIndividuals();

		if (params.size() == 0)
			params = cromosom.getIndividuals();

		if (params.size() == 0)
			params = cromosom.getScheduler().getPeople(event.getId());

		return params;
	}

	public int getTermDuration() {
		return event.getTermDuration();
	}

	public List<GenTerm> getTerms() {
		return new ArrayList<GenTerm>(eventTerms);
	}

	public void addTerms(List<GenTerm> terms) {
		eventTerms = terms;
	}
	
	public void addTerm(GenTerm term) {
		eventTerms.add(term);
	}

	public int getMinTerms() {
		return this.minTerms;
	}

	public int getMaxTerms() {
		return this.maxTerms;
	}

	public void optimize() {
		int freeSpace = 0;
		for (GenTerm term : eventTerms) {
			freeSpace += term.getFreeSpace();
		}

		while (true) {
			int chosen = 0;
			for (int i = 0; i < eventTerms.size(); i++)
				if (eventTerms.get(i).getOccupationNumber() <= eventTerms.get(
						chosen).getOccupationNumber())
					chosen = i;

			if (freeSpace - eventTerms.get(chosen).getFreeSpace() >= eventTerms
					.get(chosen).getOccupationNumber()) {
				GenTerm term = eventTerms.remove(chosen);
				freeSpace -= term.getCapacity();

				List<String> list = term.getStudents();
				term.freeReservations();

				organizeStudents(eventTerms, list, true, false);
			} else {
				break;
			}
		}
	}
	
	public int getUniqueTermId() {
		termId++;
		return termId;
	}
	
	public DateStamp getLastDate() {
		return lastDate;
	}

	@Override
	public String toString() {
		String s = event.getId() + "\n";

		for (GenTerm term : eventTerms)
			s += term.toString() + "\n";

		return s;
	}
}