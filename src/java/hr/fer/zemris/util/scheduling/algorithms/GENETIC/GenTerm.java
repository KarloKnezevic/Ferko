package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.algorithmview.ILocationParameter;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;

//razred GenTerm. jedan generirani termin unutar kromosoma
public class GenTerm implements Comparable<GenTerm> {
	private RoomData room;
	private DateStamp date;
	private TimeSpan span;
	private String name;

	private GenEvent gEvent;
	private ITerm term;
	private GenResMan manager;

	private List<ILocationParameter> locations;
	private List<ITimeParameter> times;
	private int duration;

	private Set<String> reservedIndividuals = new HashSet<String>();
	private Set<String> conflictedIndividuals = new HashSet<String>();

	private SchedPoint schedPoint;
	private FitnessResult fitnessResult;

	private boolean status;

	// izrada kopije
	public GenTerm(GenTerm original, GenEvent gEvent) {
		this.room = original.room;
		this.date = original.date;
		this.span = original.span;

		this.gEvent = gEvent;

		this.term = original.term;
		if (term != null)
			this.name = term.getName();
		else {
			this.name = gEvent.getEventId() + "T" + gEvent.getUniqueTermId();
		}

		this.manager = original.manager;

		this.locations = original.locations;
		this.times = original.times;
		this.duration = original.duration;

		this.reservedIndividuals = new HashSet<String>(
				original.reservedIndividuals);
		this.conflictedIndividuals = new HashSet<String>(
				original.conflictedIndividuals);

		this.schedPoint = new SchedPoint(true, false, this);
		this.status = original.status;
	}

	public GenTerm(GenEvent gEvent, ITerm term, GenResMan manager) {
		// inicijalizacija lokalnih varijabli
		this.gEvent = gEvent;

		this.term = term;
		if (term != null)
			this.name = term.getName();
		else {
			this.name = gEvent.getEventId() + "T" + gEvent.getUniqueTermId();
		}

		this.manager = manager;

		// dohvacanje parametara
		locations = getLocationParameters();
		times = getTimeParameters();
		duration = gEvent.getTermDuration();

		this.schedPoint = new SchedPoint(true, false, this);
		this.status = false;
	}

	// dohvaca datum odrzavanja termina
	public DateStamp getDate() {
		return date;
	}

	public void setDate(DateStamp date) {
		this.date = date;
	}

	// dohvaca span odrzavanja termina
	public TimeSpan getSpan() {
		return span;
	}

	public void setSpan(TimeSpan span) {
		this.span = span;
	}

	public boolean status() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public ITerm getTerm() {
		return term;
	}

	public Boolean findNewTerm(Map<DateStamp, List<Integer>> except) {
		// priprema radne liste lokacija
		List<ILocationParameter> workLocs = new LinkedList<ILocationParameter>(
				locations);

		// dok god ima dvorana na izbor
		while (workLocs.size() != 0) {
			// odabir dvorane
			ILocationParameter room = locations.get(Util.random(locations
					.size()));

			// odabir vremenskog raspona - ako postoji
			ITimeParameter timeParameter;
			if (times.size() != 0) {
				timeParameter = times.get(Util.random(times.size()));
			} else {
				return null;
			}

			// dohvat random termina koji odgovara zahtjevima
			DatedTimeSpan param = getRandomFreeSpan(room.getId(),
					timeParameter, duration, except);

			// ako je parametar nadjen
			if (param != null) {
				// spremanje lokalnih varijabli
				this.date = param.getDate();
				this.span = param.getSpan();
				this.room = manager.getRoomData(room.getId());

				// info o terminu
				// Util.printFeedback((this.term != null ? this.term.getId() :
				// "")
				// + " " + this.room.getId() + " " + this.date + " "
				// + this.span);

				// ako su definirani studenti na razini termina, dodaj ih sve u
				// ovaj
				if (term != null) {
					List<String> termIndividuals = term.getDefinition()
							.getIndividuals();
					if (termIndividuals.size() != 0) {
						reserveIndividuals(termIndividuals);
						gEvent.setStudentsOnTermLevel(true);

						// ako nema niti jednog studenta kojem ovaj termin
						// odgovara, vrati false
						if (this.getReservedStudentsNumber() == 0) {
							status = false;
							return status;
						}
					}
				}

				status = true;
				return status;
			} else {
				// ako nije nadjen parametar, izbrisi sobu i trazi dalje
				workLocs.remove(room);
			}
		}

		// ako uopce ne nadje termin, vraca null
		return null;
	}

	// rezervira trenutni termin
	public void reserveTerm() {
		// rezervacija termina
		manager.reserveRoom(this.room.getId(), this.date, this.span,
				this.schedPoint);
	}

	// oslobadja sve resurse termina
	public void freeReservations() {
		// oslobadja studente
		freeStudents();

		// oslobadja dvoranu
		manager.clearRoomReservationForTimeSpan(this.room.getId(), this
				.getDate(), this.getSpan());
	}

	// oslobadja sve studente
	public void freeStudents() {
		// oslobadja sve studente
		for (String jmbag : this.reservedIndividuals)
			manager.clearStudentReservationForTimeSpan(jmbag, this.getDate(),
					this.getSpan());

		reservedIndividuals = new HashSet<String>();
		conflictedIndividuals = new HashSet<String>();
	}

	// oslobadja studente ali ih zadrzava u terminu
	public void unreserveStudentsWithoutRemoval() {
		// oslobadja sve studente
		for (String jmbag : this.reservedIndividuals)
			manager.clearStudentReservationForTimeSpan(jmbag, this.getDate(),
					this.getSpan());
	}

	// rezervira sve resurse termina
	public void fillReservations() {
		// rezervira sve studente
		for (String jmbag : this.reservedIndividuals)
			manager
					.reserveStudent(jmbag, this.date, this.span,
							this.schedPoint);

		// rezervira dvoranu
		manager.reserveRoom(this.room.getId(), this.getDate(), this.getSpan(),
				this.schedPoint);
	}

	// metoda dohvaca lokacijske parametre. ako nisu definirani za zadani
	// termin, gleda se nadredjeni event
	public List<ILocationParameter> getLocationParameters() {
		List<ILocationParameter> params = null;

		// ako postoji definirani termin
		if (term != null) {
			params = term.getDefinition().getLocationParameters();

			// ako nisu definirani lokacijski parametri
			if (params.size() == 0)
				params = gEvent.getLocationParameters();
		} else {
			// ako nije definiran termin, odmah ide prema gore
			params = gEvent.getLocationParameters();
		}

		return params;
	}

	// metoda dohvaca vremenske parametre. ako nisu definirani za zadani termin,
	// gleda se nadredjeni event
	private List<ITimeParameter> getTimeParameters() {
		List<ITimeParameter> params = null;

		// ako postoji definiran term
		if (term != null) {
			params = term.getDefinition().getTimeParameters();

			// ako nisu definirani vremenski parametri
			if (params.size() == 0)
				params = gEvent.getTimeParameters();
		} else {
			// ako nije definiran termin, odmah ide prema gore
			params = gEvent.getTimeParameters();
		}

		return params;
	}

	// metoda vraca nasumicno odabrani time span zeljene duljine u zeljenoj sobi
	// sa zeljenim timeParametrima. ako takav span ne postoji, vraca se null
	private DatedTimeSpan getRandomFreeSpan(String roomId,
			ITimeParameter timeParameter, int duration,
			Map<DateStamp, List<Integer>> except) {
		// dohvacanje svih slobodnih termina
		Map<DateStamp, List<TimeSpan>> freeSpans = manager
				.getFreeSpansForRoomInSpan(roomId, timeParameter, duration,
						except);

		// ako termina nema, vrati null
		if (freeSpans == null)
			return null;

		// racunanje koji ce se dateStamp uzeti
		int pos = Util.random(freeSpans.keySet().size());
		int i = 0;
		TimeSpan chosen = null;
		DateStamp chosenStamp = null;

		for (DateStamp dateStamp : freeSpans.keySet()) {
			if (i == pos) {
				// racunanje koji se se span uzeti
				int pos2 = Util.random(freeSpans.get(dateStamp).size());

				// izabrani span se biljezi
				chosen = freeSpans.get(dateStamp).get(pos2);
				chosenStamp = dateStamp;
				break;
			}
			i++;
		}

		// izracun trajanja odabranog spana
		int chosenDuration = (chosen.getEnd().getHour() - chosen.getStart()
				.getHour())
				* 60
				+ (chosen.getEnd().getMinute() - chosen.getStart().getMinute());

		// racunanje koliko se razlicitih termina moze napraviti od ovog
		// (najmanje 1)
		int times = chosenDuration / duration;

		// racunanje koji ce se tocno raspon uzeti
		int chosenStep = Util.random(times);

		// vracanje odgovarajuceg raspona
		TimeStamp start = new TimeStamp(chosen.getStart().getAbsoluteTime()
				+ chosenStep * duration);
		TimeStamp end = new TimeStamp(chosen.getStart().getAbsoluteTime()
				+ chosenStep * duration + duration);

		return new DatedTimeSpan(chosenStamp, new TimeSpan(start, end));
	}

	public FitnessResult calculateFitness() {
		fitnessResult = new FitnessResult();

		// racunanje prve tocke - (pre)napucenost dvorane
		// 0 - 1 je dvorana unuar kapaciteta, 0 je popunjena, 1 je prazna
		// 1 na dalje je prepunjena dvorana, postotak prepunjenja
		float first = (reservedIndividuals.size() + conflictedIndividuals
				.size())
				/ room.getCapacity();
		if (first > 1)
			fitnessResult.add(first);
		else
			fitnessResult.add(1 - first);

		// racunanje druge tocke - broj konfliktnih studenata
		fitnessResult.add(conflictedIndividuals.size());

		// racunanje trece tocke - blizina termina sredini dana
		// broj oznacava udaljenost pocetka termina od 14h
		int length = Math.abs(14 * 60 - this.span.getStart().getAbsoluteTime());

		fitnessResult.add(length);

		return fitnessResult;
	}

	public FitnessResult getFitness() {
		return fitnessResult;
	}

	public void reserveIndividuals(Collection<String> termIndividuals) {
		for (String jmbag : termIndividuals) {
			reserveIndividual(jmbag);
		}
	}

	public void reserveIndividual(String jmbag) {
		if (!manager.isStudentReserved(jmbag, this.date, this.span)) {
			addReservedIndividual(jmbag);
		} else {
			addConflictedIndividual(jmbag);
		}
	}

	public void addReservedIndividual(String jmbag) {
		manager.reserveStudent(jmbag, this.date, this.span, this.schedPoint);
		this.reservedIndividuals.add(jmbag);
	}

	public boolean freeindividual(String jmbag) {
		if (reservedIndividuals.contains(jmbag)) {
			reservedIndividuals.remove(jmbag);
			manager.clearStudentReservationForTimeSpan(jmbag, this.date,
					this.span);
			return true;
		}

		if (conflictedIndividuals.contains(jmbag)) {
			conflictedIndividuals.remove(jmbag);
			return true;
		}

		return false;
	}

	public void addConflictedIndividual(String jmbag) {
		conflictedIndividuals.add(jmbag);
	}

	public int getReservedStudentsNumber() {
		return reservedIndividuals.size();
	}

	public int getConflictedStudentsNumber() {
		return conflictedIndividuals.size();
	}

	public int getCapacity() {
		return room.getCapacity();
	}

	public int getOccupationNumber() {
		return (reservedIndividuals.size() + conflictedIndividuals.size());
	}

	public int getFreeSpace() {
		return (this.getCapacity() - this.getOccupationNumber());
	}

	public boolean isAvailable() {
		if (this.getOccupationNumber() < this.getCapacity())
			return true;
		else
			return false;
	}

	public RoomData getRoom() {
		return room;
	}

	public void setRoom(RoomData room) {
		this.room = room;
	}

	@Override
	public String toString() {
		String termId = (this.term == null ? "" : term.getId());
		return (this.name + " " + termId + " " + this.date + " " + this.span
				+ " " + this.room.getId() + " " + this.status + " "
				+ this.getReservedStudentsNumber() + " "
				+ this.getConflictedStudentsNumber() + " " + this.getCapacity());
	}

	@Override
	public int compareTo(GenTerm o) {
		if (!this.name.equals(o.name))
			return this.fitnessResult.compareTo(o.fitnessResult);
		else
			return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		GenTerm other = (GenTerm) obj;

		if (!this.name.equals(other.name)) {
			if (date == null) {
				if (other.date != null)
					return false;
			} else if (!date.equals(other.date))
				return false;

			if (span == null) {
				if (other.span != null)
					return false;
			} else if (!span.equals(other.span))
				return false;

			if (room == null) {
				if (other.room != null)
					return false;
			} else if (!room.equals(other.room))
				return false;

			return true;
		} else
			return true;
	}

	// dohvaca sve studente
	public List<String> getStudents() {
		List<String> list = new LinkedList<String>();
		list.addAll(this.reservedIndividuals);
		list.addAll(this.conflictedIndividuals);
		return list;
	}

	public String getName() {
		return this.name;
	}
}
