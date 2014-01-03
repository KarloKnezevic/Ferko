package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// reservation manager za genetski algoritam
public class GenResMan {
	private Map<String, Map<DateStamp, SchedPoint[]>> studentReservation = new HashMap<String, Map<DateStamp, SchedPoint[]>>();
	private Map<String, Map<DateStamp, SchedPoint[]>> roomReservation = new HashMap<String, Map<DateStamp, SchedPoint[]>>();
	private Map<String, RoomData> roomDetails = new HashMap<String, RoomData>();

	private SchedPoint fixedOccupiedPoint = new SchedPoint(true, true, null);
	private SchedPoint unoccupiedPoint = new SchedPoint(false, false, null);

	// konstruktor. Pune se fiksni podaci
	public GenResMan(Map<String, ISchedulingData> data) {
		// iteriranje po svim eventima
		for (String eventId : data.keySet()) {
			// dohvacanje podataka o ljudima
			Map<String, Map<DateStamp, List<TimeSpan>>> peopleData = data.get(
					eventId).getPeopleData();
			// iteriranje po ljudima
			for (String studentJMBAG : peopleData.keySet()) {
				// iteriranje po datumima
				for (DateStamp dateStamp : peopleData.get(studentJMBAG)
						.keySet()) {
					// iteriranje po timeSpanovima
					for (TimeSpan timeSpan : peopleData.get(studentJMBAG).get(
							dateStamp)) {
						// fiksno rezerviranje studenta
						reserveStudent(studentJMBAG, dateStamp, timeSpan,
								fixedOccupiedPoint);
					}
				}
			}

			// dohvacanje podataka o dvoranama
			Map<RoomData, Map<DateStamp, List<TimeSpan>>> termData = data.get(
					eventId).getTermData();
			// iteriranje po dvoranama
			for (RoomData roomData : termData.keySet()) {
				// spremanje podataka o dvorani
				roomDetails.put(roomData.getId(), roomData);

				// iteriranje po datumima
				for (DateStamp dateStamp : termData.get(roomData).keySet()) {
					// iteriranje po timeSpanovima
					for (TimeSpan timeSpan : termData.get(roomData).get(
							dateStamp)) {
						// fiksno rezerviranje dvorane
						reserveRoomFixed(roomData.getId(), dateStamp, timeSpan);
					}
				}
			}
		}
	}

	// standardno rezerviranje studenta
	public void reserveStudent(String JMBAG, DateStamp dateStamp,
			TimeSpan timeSpan, SchedPoint point) {
		// ako student ne postoji
		if (!studentReservation.containsKey(JMBAG)) {
			// dodaj studenta u mapu
			studentReservation.put(JMBAG,
					new HashMap<DateStamp, SchedPoint[]>());
		}

		// ako ne postoji datum za studenta
		if (!studentReservation.get(JMBAG).containsKey(dateStamp)) {
			// dodaj trazeni datum
			studentReservation.get(JMBAG)
					.put(dateStamp, new SchedPoint[24 * 4]);

			// popuni sve spanove slobodnim vremenom
			Arrays.fill(studentReservation.get(JMBAG).get(dateStamp),
					unoccupiedPoint);
		}

		// rezerviranje za zadani span
		for (int i = timeSpan.getStart().getAbsoluteTime() / 15; i < timeSpan
				.getEnd().getAbsoluteTime() / 15; i++) {
			studentReservation.get(JMBAG).get(dateStamp)[i] = point;
		}
	}

	// provjera je li student rezerviran za dani span
	public boolean isStudentReserved(String JMBAG, DateStamp dateStamp,
			TimeSpan timeSpan) {
		// dohvacanje podataka o navedenom studentu
		Map<DateStamp, SchedPoint[]> studentReservationRecord = studentReservation
				.get(JMBAG);

		// ako podaci nisu dohvaceni
		if (studentReservationRecord == null) {
			// vrati poruku o gresci
			throw new IllegalArgumentException(
					"Ne postoji zapis za trazenog studenta!");
		}

		// dohvacanje zauzeca za trazeni dan
		SchedPoint[] occupancesForDate = studentReservationRecord
				.get(dateStamp);

		// ako zauzeca nisu dohvacena
		if (occupancesForDate == null) {
			return false;
		}

		// iteriranje po zauzecima, ispitivanje tocaka
		for (int i = timeSpan.getStart().getAbsoluteTime() / 15; i < timeSpan
				.getEnd().getAbsoluteTime() / 15; i++) {
			// ako je neka tocka zauzeta, vrati da je student zauzet
			if (occupancesForDate[i].isOccupied())
				return true;
		}

		// ako se iteriranje zavrsilo, student je slobodan
		return false;
	}

	// brisanje rezervacije studenta za zadani TimeSpan
	public void clearStudentReservationForTimeSpan(String JMBAG,
			DateStamp dateStamp, TimeSpan timeSpan) {
		// ako student nije zauzet u zadanom terminu, vrati
		if (!isStudentReserved(JMBAG, dateStamp, timeSpan))
			return;
		else {
			// inace, dohvati podatke o studentu
			Map<DateStamp, SchedPoint[]> studentReservationRecord = studentReservation
					.get(JMBAG);

			// ako podaci nisu dohvaceni, vrati
			if (studentReservationRecord == null)
				return;

			// dohvati zauzeca za zadani dan
			SchedPoint[] occupancesForDate = studentReservationRecord
					.get(dateStamp);

			// ako nema dohvacenih zauzeca, vrati
			if (occupancesForDate == null)
				return;

			// iteriraj po svim tockama, ako nisu fiksne, stavi
			// unoccupiedPoint
			for (int i = timeSpan.getStart().getAbsoluteTime() / 15; i < timeSpan
					.getEnd().getAbsoluteTime() / 15; i++)
				if (!occupancesForDate[i].isFixed())
					occupancesForDate[i] = unoccupiedPoint;

			// natrag zapisi zapis za taj dan
			studentReservationRecord.put(dateStamp, occupancesForDate);
		}
	}

	// brisanje za cijeli datum, svodi se na brisanje preko cijelog dana
	public void clearStudentReservationForDateStamp(String JMBAG,
			DateStamp dateStamp) {
		clearStudentReservationForTimeSpan(JMBAG, dateStamp, new TimeSpan(
				new TimeStamp(0, 0), new TimeStamp(24, 0)));
	}

	// brisanje podataka o studentu, svodi se na brisanje preko svih
	// datuma
	public void clearStudentReservation(String JMBAG) {
		for (DateStamp dateStamp : studentReservation.get(JMBAG).keySet())
			clearStudentReservationForDateStamp(JMBAG, dateStamp);
	}

	// standardno rezerviranje dvorane
	public void reserveRoom(String roomId, DateStamp dateStamp,
			TimeSpan timeSpan, SchedPoint point) {
		// ako dvorana ne postoji
		if (!roomReservation.containsKey(roomId)) {
			// dodaj dvoranu u mapu
			roomReservation.put(roomId, new HashMap<DateStamp, SchedPoint[]>());
		}

		// ako ne postoji datum za dvoranu
		if (!roomReservation.get(roomId).containsKey(dateStamp)) {
			// dodaj trazeni datum
			roomReservation.get(roomId).put(dateStamp, new SchedPoint[24 * 4]);

			// popuni sve spanove slobodnim vremenom
			Arrays.fill(roomReservation.get(roomId).get(dateStamp),
					unoccupiedPoint);
		}

		// rezerviranje za zadani span
		for (int i = timeSpan.getStart().getAbsoluteTime() / 15; i < timeSpan
				.getEnd().getAbsoluteTime() / 15; i++) {
			roomReservation.get(roomId).get(dateStamp)[i] = point;
		}
	}

	// fiksno rezerviranje dvorane
	private void reserveRoomFixed(String roomId, DateStamp dateStamp,
			TimeSpan timeSpan) {
		// ako dvorana ne postoji
		if (!roomReservation.containsKey(roomId)) {
			// dodaj dvoranu u mapu
			roomReservation.put(roomId, new HashMap<DateStamp, SchedPoint[]>());
		}

		// ako ne postoji datum za dvoranu
		if (!roomReservation.get(roomId).containsKey(dateStamp)) {
			// dodaj trazeni datum
			roomReservation.get(roomId).put(dateStamp, new SchedPoint[24 * 4]);

			// popuni sve spanove zauzetim vremenom
			Arrays.fill(roomReservation.get(roomId).get(dateStamp),
					fixedOccupiedPoint);
		}

		// popunjavanje zadanog spana slobodnim vremenom
		for (int i = timeSpan.getStart().getAbsoluteTime() / 15; i < timeSpan
				.getEnd().getAbsoluteTime() / 15; i++) {
			roomReservation.get(roomId).get(dateStamp)[i] = unoccupiedPoint;
		}
	}

	// provjera je li dvorana rezerviran za dani span
	public boolean isRoomReserved(String roomId, DateStamp dateStamp,
			TimeSpan timeSpan) {
		// dohvacanje podataka o navedenoj dvorani
		Map<DateStamp, SchedPoint[]> roomReservationRecord = roomReservation
				.get(roomId);

		// ako podaci nisu dohvaceni
		if (roomReservationRecord == null) {
			// vrati poruku o gresci
			throw new IllegalArgumentException(
					"Ne postoji zapis za trazenu dvoranu!");
		}

		// dohvacanje zauzeca za trazeni dan
		SchedPoint[] occupancesForDate = roomReservationRecord.get(dateStamp);

		// ako zauzeca nisu dohvacena
		if (occupancesForDate == null) {
			return false;
		}

		// iteriranje po zauzecima, ispitivanje tocaka
		for (int i = timeSpan.getStart().getAbsoluteTime() / 15; i < timeSpan
				.getEnd().getAbsoluteTime() / 15; i++) {
			// ako je neka tocka zauzeta, vrati da je dvorana zauzeta
			if (occupancesForDate[i].isOccupied())
				return true;
		}

		// ako se iteriranje zavrsilo, dvorana je slobodna
		return false;
	}

	// brisanje rezervacije dvorane za zadani TimeSpan
	public void clearRoomReservationForTimeSpan(String roomId,
			DateStamp dateStamp, TimeSpan timeSpan) {
		// ako dvorana nije zauzeta u zadanom terminu, vrati
		if (!isRoomReserved(roomId, dateStamp, timeSpan))
			return;
		else {
			// inace, dohvati podatke o dvorani
			Map<DateStamp, SchedPoint[]> roomReservationRecord = roomReservation
					.get(roomId);

			// ako podaci nisu dohvaceni, vrati
			if (roomReservationRecord == null)
				return;

			// dohvati zauzeca za zadani dan
			SchedPoint[] occupancesForDate = roomReservationRecord
					.get(dateStamp);

			// ako nema dohvacenih zauzeca, vrati
			if (occupancesForDate == null)
				return;

			// iteriraj po svim tockama, ako nisu fiksne, stavi
			// unoccupiedPoint
			for (int i = timeSpan.getStart().getAbsoluteTime() / 15; i < timeSpan
					.getEnd().getAbsoluteTime() / 15; i++)
				if (!occupancesForDate[i].isFixed())
					occupancesForDate[i] = unoccupiedPoint;

			// natrag zapisi zapis za taj dan
			roomReservationRecord.put(dateStamp, occupancesForDate);
		}
	}

	// brisanje za cijeli datum, svodi se na brisanje preko cijelog dana
	public void clearRoomReservationForDateStamp(String roomId,
			DateStamp dateStamp) {
		clearRoomReservationForTimeSpan(roomId, dateStamp, new TimeSpan(
				new TimeStamp(0, 0), new TimeStamp(24, 0)));
	}

	// brisanje podataka o dvorani, svodi se na brisanje preko svih
	// datuma
	public void clearRoomReservation(String roomId) {
		for (DateStamp dateStamp : roomReservation.get(roomId).keySet())
			clearRoomReservationForDateStamp(roomId, dateStamp);
	}

	// vraca popis raspolozivih termina zadane duljine za trazenu dvoranu unutar
	// zadanog timeSpana
	public LinkedList<TimeSpan> getFreeTimeSpansForRoomInTimeSpan(
			String roomId, DateStamp dateStamp, TimeSpan timeSpan,
			int duration, List<Integer> except) {
		// priprema liste iznimaka, ako postoji
		// iznimke su zapisane kao apsolutne vrijednosti vremena u kojima
		// pocinju termini koji se izbacuju
		if (except != null) {
			// sortiranje liste iznimaka
			Collections.sort(except);
		}
		// inicijalizacija brojaca za iznimku
		int excPos = 0;

		// trazenje postoji li navedena dvorana
		Map<DateStamp, SchedPoint[]> roomReservationRecord = roomReservation
				.get(roomId);
		if (roomReservationRecord == null)
			throw new IllegalArgumentException(
					"Ne postoji zapis za trazenu dvoranu!");

		// priprema liste
		LinkedList<TimeSpan> list = new LinkedList<TimeSpan>();

		// trazenje postoji li navedeni datum
		SchedPoint[] occupancesForDate = roomReservationRecord.get(dateStamp);

		// ako datum ne postoji, dodaje se novi raspolozivi dan
		if (occupancesForDate == null) {
			reserveRoom(roomId, dateStamp, new TimeSpan(new TimeStamp(8, 0),
					new TimeStamp(20, 0)), unoccupiedPoint);

			occupancesForDate = roomReservationRecord.get(dateStamp);

			// ako postoje termini koji se izbacuju
			if (except != null) {
				// traze se ti termini
				int start = timeSpan.getStart().getAbsoluteTime() / 15;
				int end;

				// prelazi se po listi pocetaka i traze se raspolozivi
				while (excPos < except.size()) {
					end = except.get(excPos) / 15;

					// ako se nadje, dodaje se u listu
					if (start != end && (end - start) * 15 >= duration)
						list.add(new TimeSpan(new TimeStamp(start * 15),
								new TimeStamp(end * 15)));

					start = end + (duration / 15);
					excPos++;
				}

				// naposlijetku, dodavanje zadnjeg termina
				end = timeSpan.getEnd().getAbsoluteTime() / 15;

				// ako zadnji termin odgovara kriterijima
				if (start != end && (end - start) * 15 >= duration)
					list.add(new TimeSpan(new TimeStamp(start * 15),
							new TimeStamp(end * 15)));
			} else {
				// ako nema iznimaka, vraca se odgovarajuci TimeSpan (cijeli je
				// slobodan)
				list.add(new TimeSpan(timeSpan.getStart(), timeSpan.getEnd()));
			}
			return list;
		}

		// priprema varijabli za pretragu
		int start = timeSpan.getStart().getAbsoluteTime() / 15;
		int end = timeSpan.getEnd().getAbsoluteTime() / 15;
		int freeStart = 0;
		boolean onFreeSpan = false;
		int i;
		int excVal = -1;

		// ako iznimke postoje, inicijalizacija vrijednosti
		if (except != null) {
			excVal = except.get(excPos) / 15;
		}

		// prolaz preko zadanog spana
		for (i = start; i < end; i++) {
			// ako trenutno mjeri slobodan span
			if (onFreeSpan) {
				// ako dodje na zauzeto podrucje ili na iznimku
				if (occupancesForDate[i].isOccupied() || i == excVal) {
					// ponisti zastavicu
					onFreeSpan = false;

					// ako je span duzi ili jednak zadanoj duljini
					if ((i - freeStart) * 15 >= duration) {
						// dodaj novi span
						list.add(new TimeSpan(new TimeStamp(freeStart * 15),
								new TimeStamp(i * 15)));
					}
				}
			} else {
				// inace, ako naidje na slobodan termin, i taj termin nije na
				// iznimci (ako je uopce definirana) pocinje brojat
				if (!occupancesForDate[i].isOccupied()
						&& ((excVal != -1 && (i < excVal || i >= excVal
								+ (duration / 15))) || excVal == -1)) {
					freeStart = i;
					onFreeSpan = true;
				}
			}

			// ako je kraju iznimke, pomakni na slijedecu ili zavrsi s iznimkama
			if (excVal != -1 && i + 1 == excVal + (duration / 15)) {
				excPos++;
				if (excPos < except.size())
					excVal = except.get(excPos) / 15;
				else
					excVal = -1;
			}
		}

		// na kraju, ponovno provjera za span ako je na slobodnom spanu
		if (onFreeSpan && (i - freeStart) * 15 >= duration) {
			// dodaj novi span
			list.add(new TimeSpan(new TimeStamp(freeStart * 15), new TimeStamp(
					i * 15)));
		}

		// ako ima sto vratiti, vrati, inace null
		if (list.size() != 0)
			return list;
		else
			return null;
	}

	// metoda vraca sve spanove baram zadane duljine za zadanu dvoranu u
	// trazenom razdoblju
	public Map<DateStamp, List<TimeSpan>> getFreeSpansForRoomInSpan(
			String roomId, ITimeParameter timeParameter, int duration,
			Map<DateStamp, List<Integer>> except) {
		// priprema mape s povratnim podacima
		Map<DateStamp, List<TimeSpan>> map = new TreeMap<DateStamp, List<TimeSpan>>();

		// priprema sortiranog seta, da dani idu po redu
		TreeSet<DateStamp> roomDates = new TreeSet<DateStamp>();
		roomDates.addAll(roomReservation.get(roomId).keySet());

		// prolaze se svi raspolozivi datumi za dvoranu
		for (DateStamp dateStamp : roomDates) {
			// ako je datum unutar zahtjevanih
			if (dateStamp.compareTo(timeParameter.getFromDate()) >= 0
					&& dateStamp.compareTo(timeParameter.getToDate()) <= 0) {
				// stvaraju se opceniti TimeStampovi
				TimeStamp start;
				TimeStamp end;

				// ako je datum pocetka, uzima se u obzir vrijeme pocetka
				if (dateStamp.equals(timeParameter.getFromDate())) {
					start = timeParameter.getFromTime();
				} else {
					start = new TimeStamp(8, 0);
				}

				// ako je datum kraja, uzima se u obzir vrijeme kraja
				if (dateStamp.equals(timeParameter.getToDate())) {
					end = timeParameter.getToTime();
				} else {
					end = new TimeStamp(20, 0);
				}

				// dodaj slobodne termine za taj dan - ako ih ima
				List<TimeSpan> entry = getFreeTimeSpansForRoomInTimeSpan(
						roomId, dateStamp, new TimeSpan(start, end), duration,
						(except == null ? null : except.get(dateStamp)));

				// ako ima rezultata, dodaj ih u mapu za taj datum
				if (entry != null)
					map.put(dateStamp, entry);
			}
		}

		// ako se ima sto vratiti, vrati
		if (map.size() != 0)
			return map;
		else
			return null;
	}

	public RoomData getRoomData(String roomId) {
		return roomDetails.get(roomId);
	}
	
	public void clearReservations() {
		for(String roomId : roomReservation.keySet())
			this.clearRoomReservation(roomId);
		
		for(String jmbag : studentReservation.keySet())
			this.clearStudentReservation(jmbag);
	}
}
