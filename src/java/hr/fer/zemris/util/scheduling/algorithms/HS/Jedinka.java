package hr.fer.zemris.util.scheduling.algorithms.HS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.SchedulingResult;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.ILocationParameter;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;

/**
 * @author Kusalic Domagoj
 */
public class Jedinka {
	public final double PARAMETAR_A = 0.85;
	public static final double PARAMETAR_B = 0.2;
	public String planName;
	public boolean uspio = false; // uspjesnost stvaranja objekta
	public double dobrota;
	HashMap<String, JEvent> events = new HashMap<String, JEvent>(); // eventName_JEvent
	ArrayList<Zauzeto> zauzeto = new ArrayList<Zauzeto>(); // popis_zauzetih_prostorija

	// konstruktor za stvaranje potpuno nove jedinke iz nicega
	// friendly konstruktor, ukoliko ne uspije stvoriti jedinku postavlja uspio
	// na false;
	Jedinka(MojPlan mojPlan, Map<String, ISchedulingData> data) {
		planName = mojPlan.name;
		for (MojEvent mojEvent : mojPlan.events) {
			JEvent je = null;
			for (int i = 0; i < 5 && je == null; i++)
				je = JEvent.stvoriJEvent(mojEvent, zauzeto, data);
			if (je == null)
				return; // uspio je false po defaultu
			else
				events.put(mojEvent.name, je);
		}
		uspio = true;
		ubaciLjude();

//		for (String trenutniEvent: events.keySet()) {
//			System.out.println("     -     -     trenutniEvent: "+trenutniEvent);
//			//HashSet<String> svi=new HashSet<String>();
//			for (JTerm term :events.get(trenutniEvent).termini) {
//				System.out.println(" - - - termName, capacyty, popunjenost, moguci: "+term.termName+" "+term.capacity+" "+term.popunjenost+" "+term.moguci.size());
//				System.out.println(new TreeSet<String>(term.studenti));
//				System.out.println(new TreeSet<String>(term.moguci));
//				//svi.addAll(term.studenti);
//			}
//			//System.out.println(svi.size());
//		}
		dobrota=izracunajDobrotu();
	}

	public Jedinka(MojPlan mojPlan, Map<String, ISchedulingData> data, ISchedulingResult ISR) { // ocekujem da event zadovoljava hard constraints
		planName = mojPlan.name;
		
		for (MojEvent mojEvent : mojPlan.events) {
			JEvent je = null;
			for (IEvent ievent: ISR.getPlan().getPlanEvents()){
				if (ievent.getName().equals(mojEvent.name)) {
					je = JEvent.stvoriJEvent(mojEvent, zauzeto, data,ievent);
					break;
				}
			}
			events.put(mojEvent.name, je);
		}
		uspio = true;
		ubaciLjude();
		dobrota=izracunajDobrotu();
		// dodatak dobroti:
		boolean greska=false;
		for (String ev1: events.keySet())
			for (JTerm te1  : events.get(ev1).termini)
				for (String ev2: events.keySet())
					for (JTerm te2  : events.get(ev1).termini)
						if (!ev1.equals(ev2) && !te1.equals(te2) &&
							te1.datum.equals(te2.datum) && te1.roomId.equals(te2.roomId) &&
							te1.startOffset < te2.endOffset && te1.endOffset > te2.startOffset)
							greska=true;
		if (greska) dobrota-=1000;
	}

	public Jedinka(MojPlan mojPlan, Map<String, ISchedulingData> data, TreeMap<Double, Jedinka> populacija) {
		if (Math.random()<=PARAMETAR_A) {
			if (populacija==null) return; 
			Double ttt=populacija.keySet().toArray(new Double[1])[(int) (populacija.size()*Math.random())];
			if (ttt==null) return;
			ISchedulingResult ISR = populacija.get(populacija.floorKey(ttt)).toISD();
//---------copy-begin
			planName = mojPlan.name;
			
			for (MojEvent mojEvent : mojPlan.events) {
				JEvent je = null;
				for (IEvent ievent: ISR.getPlan().getPlanEvents()){
					if (ievent.getName().equals(mojEvent.name)) {
						je = JEvent.stvoriJEvent2(mojEvent, zauzeto, data,ievent);
						break;
					}
				}
				events.put(mojEvent.name, je);
			}
			uspio = true;
			ubaciLjude();
			dobrota=izracunajDobrotu();
			// dodatak dobroti:
			boolean greska=false;
			for (String ev1: events.keySet())
				for (JTerm te1  : events.get(ev1).termini)
					for (String ev2: events.keySet())
						for (JTerm te2  : events.get(ev1).termini)
							if (!ev1.equals(ev2) && !te1.equals(te2) &&
								te1.datum.equals(te2.datum) && te1.roomId.equals(te2.roomId) &&
								te1.startOffset < te2.endOffset && te1.endOffset > te2.startOffset)
								greska=true;
			if (greska) dobrota-=500;
//---------copy-end
		}
		else {
			planName = mojPlan.name;
			for (MojEvent mojEvent : mojPlan.events) {
				JEvent je = null;
				for (int i = 0; i < 5 && je == null; i++)
					je = JEvent.stvoriJEvent(mojEvent, zauzeto, data);
				if (je == null)
					return; // uspio je false po defaultu
				else
					events.put(mojEvent.name, je);
			}
			uspio = true;
			ubaciLjude();
			dobrota=izracunajDobrotu();
		}		
	}

	private double izracunajDobrotu() {
		double ret=0;
		for (String ev :events.keySet()){
			ret += events.get(ev).brojTermina*300; // broj termina
			for (JTerm term:events.get(ev).termini){
				if (term.capacity < term.popunjenost) // prepopunjena ucionica
					ret+=200;
				if (term.capacity > term.popunjenost) // nepopunjenoa ucionica
					ret+= term.capacity-term.popunjenost;
				for (String student: term.studenti) { // kriva ucionica
					if (!term.moguci.contains(student))
						ret+=2;
				}
			}
		}
		ret+=Math.random();
		ret=1000000-ret;
		return ret;
	}

	private void ubaciLjude() { // TopoloskoSortiranje+MaximumMatchingBFS
		ArrayList<String> poredak = topoloskoSortiranje();
		//System.out.println("Poredak je: " + poredak);
		for (int xi = 0; xi < poredak.size(); xi++) {
			JEvent trenutniEvent = events.get(poredak.get(xi));
			List<String> lju = trenutniEvent.mojEvent.people;
			ArrayList<String> ljudi = new ArrayList<String>();
			for (String s : lju)
				ljudi.add(new String(s)); // cista_kopija
			Collections.shuffle(ljudi);

			for (JTerm jterm : trenutniEvent.termini) {
				// pocisti gdje ljudi ne mogu biti zbog prijasnjih eventa
				String[] ljudiZaBrisati = new String[jterm.moguci.size()];
				int stao = 0;
				for (String covjek : jterm.moguci)
					ljudiZaBrisati[stao++] = new String(covjek);
				for (String covjek : ljudiZaBrisati) {
					boolean mozeOstat = true;
					for (IPrecondition precon : trenutniEvent.mojEvent.preconditions) {
						String preEvent = precon.getEvent().getName();
						int distance = precon.getTimeDistanceValue(); // koliko.dana.razmaka.0=sljedeci.dan,1=onaj.iza
						for (JTerm ter : events.get(preEvent).termini)
							if (ter.studenti.contains(covjek)
									&& JEvent.povecajDatumZa(ter.datum,
											distance).compareTo(jterm.datum) >= 0) { // studenti,ne.moguci
								mozeOstat = false;
							}
					}
					if (!mozeOstat)
						jterm.moguci.remove(covjek);
				}
				// System.out.println(trenutniEvent.mojEvent.name +" "+jterm.termName + " obrisao: "+obrisaoIhSam);
			}
			//--------------------------
			for (int ij = 0; ij < ljudi.size(); ij++) {
				String covjek = ljudi.get(ij);
				int brojTermina = trenutniEvent.termini.size();
				LinkedList<LinkedList<Par>> kju = new LinkedList<LinkedList<Par>>();
				for (int i = 0; i < brojTermina; i++)//inicijalizacija queue-a
					if (trenutniEvent.termini.get(i).moguci.contains(covjek)) {
						kju.addLast(new LinkedList<Par>());
						kju.getLast().addLast(new Par(covjek, i));
					}
				LinkedList<Par> rjesenje=null;
				while (kju.size()>0) {
					LinkedList<Par> llp = kju.getFirst();
					kju.removeFirst();
					if (llp.size()>4) break; // pretrazuje.do.dubine.4
					Par par = llp.getLast();
					if (trenutniEvent.termini.get(par.in).popunjenost < trenutniEvent.termini.get(par.in).capacity) {
						rjesenje = llp;
						break;
					}
					for (int i=0;i<brojTermina;i++) {
						if (i==par.in) continue; 
						for (String stu: trenutniEvent.termini.get(par.in).studenti)
							if (trenutniEvent.termini.get(i).moguci.contains(stu)) {
								LinkedList<Par> nova=new LinkedList<Par>(llp);
								nova.addLast(new Par(stu,i));
								kju.addLast(nova);
								break;
							}
					}
				}

				//obrada rjesenje, moze biti null ukoliko se ne moze ubaciti;
				if (rjesenje!=null) { // ako.postoji.rjesenje
					for (int i=0;i<brojTermina;i++) // izbaci.te.koje.ces.premjestati
						for (Par p: rjesenje)
							if (trenutniEvent.termini.get(i).studenti.contains(p.st)) {
								trenutniEvent.termini.get(i).studenti.remove(p.st);
								trenutniEvent.termini.get(i).popunjenost--;
							}
					for (Par p: rjesenje) {
						trenutniEvent.termini.get(p.in).studenti.add(p.st);
						trenutniEvent.termini.get(p.in).popunjenost++;
					}
				} else { // ako.se.student.ne.moze.ubaciti
					int odabran=(int) (brojTermina*Math.random());
					trenutniEvent.termini.get(odabran).popunjenost++;
					trenutniEvent.termini.get(odabran).studenti.add(covjek);
				}
				//System.out.println(covjek+ " "+rjesenje+"    " +kju);
				kju.clear();
			}
		}
	}

	private ArrayList<String> topoloskoSortiranje() {
		int velicina = events.size();
		ArrayList<String> ret = new ArrayList<String>();
		while (ret.size() < velicina)
			for (String s : events.keySet())
				if (!ret.contains(s)) {
					boolean moze = true;
					for (IPrecondition x : events.get(s).mojEvent.preconditions)
						if (!ret.contains(x.getEvent().getName()))
							moze = false;
					if (moze)
						ret.add(s);
				}
		return ret;
	}

	public ISchedulingResult toISD() {
		SchedulingResult result=new SchedulingResult();
		result.addPlan(planName);
		for (String ename: events.keySet()) {
			result.addEvent(ename, events.get(ename).mojEvent.id);
			for (JTerm jterm :events.get(ename).termini) {
				result.addTerm(ename, jterm.termName, jterm.roomId, jterm.capacity, jterm.datum.toString(), jterm.startOffset, jterm.endOffset);
				for (String student:jterm.studenti) {
					result.addStudentToTerm(ename, jterm.termName, student);
				}
			}
		}
		return result;
	}
}

class JEvent {
	MojEvent mojEvent;
	ArrayList<JTerm> termini;
	int brojTermina;

	private JEvent(MojEvent mojEvent) {
		this.mojEvent = mojEvent;
		termini = new ArrayList<JTerm>();

	}
	
	static JEvent stvoriJEvent(MojEvent mojEvent, ArrayList<Zauzeto> zauzeto,
			Map<String, ISchedulingData> data, IEvent ev) {
		JEvent event = new JEvent(mojEvent);
		event.brojTermina = ev.getTerms().size();

		for (ITerm it: ev.getTerms()){

			DateStamp datum = it.getDefinition().getTimeParameters().get(0).getFromDate();
			int sOffset=it.getDefinition().getTimeParameters().get(0).getFromTime().getAbsoluteTime();
			int eOffset=it.getDefinition().getTimeParameters().get(0).getToTime().getAbsoluteTime();

			JTerm jTerm = new JTerm(it.getName(), it.getDefinition().getLocationParameters().get(0).getId(),
					it.getDefinition().getLocationParameters().get(0).getActualCapacity(),
					datum, sOffset, eOffset);
			event.termini.add(jTerm); // zauzet termin

			Map<String, Map<DateStamp, List<TimeSpan>>> pData = data.get(event.mojEvent.id).getPeopleData();
			for (String covjek : event.mojEvent.people) {
				boolean moze = false;
				if (pData.containsKey(covjek)) {
					if (pData.get(covjek).get(datum) == null)
						moze = true;
					else {
						moze = true;
						for (TimeSpan ts : pData.get(covjek).get(datum)) {
							if (ts.getStart().compareTo(new TimeStamp(eOffset)) < 0
									&& ts.getEnd().compareTo(
											new TimeStamp(sOffset)) > 0)
								moze = false;
						}
					}
				}
				if (moze)
					jTerm.moguci.add(covjek);
			}
		}
		return event;
	}

	static JEvent stvoriJEvent2(MojEvent mojEvent, ArrayList<Zauzeto> zauzeto,
			Map<String, ISchedulingData> data, IEvent ev) {
		JEvent event = new JEvent(mojEvent);
		event.brojTermina = ev.getTerms().size();

		for (ITerm it: ev.getTerms()){

			DateStamp datum = it.getDefinition().getTimeParameters().get(0).getFromDate();
			int sOffset=it.getDefinition().getTimeParameters().get(0).getFromTime().getAbsoluteTime();
			int eOffset=it.getDefinition().getTimeParameters().get(0).getToTime().getAbsoluteTime();

			if (Math.random()<Jedinka.PARAMETAR_B) {
				int pomak=((int)(Math.random()*3)-1)*15;
				sOffset+=pomak;
				eOffset+=pomak;
			}
			
			JTerm jTerm = new JTerm(it.getName(), it.getDefinition().getLocationParameters().get(0).getId(),
					it.getDefinition().getLocationParameters().get(0).getActualCapacity(),
					datum, sOffset, eOffset);
			event.termini.add(jTerm); // zauzet termin

			Map<String, Map<DateStamp, List<TimeSpan>>> pData = data.get(event.mojEvent.id).getPeopleData();
			for (String covjek : event.mojEvent.people) {
				boolean moze = false;
				if (pData.containsKey(covjek)) {
					if (pData.get(covjek).get(datum) == null)
						moze = true;
					else {
						moze = true;
						for (TimeSpan ts : pData.get(covjek).get(datum)) {
							if (ts.getStart().compareTo(new TimeStamp(eOffset)) < 0
									&& ts.getEnd().compareTo(
											new TimeStamp(sOffset)) > 0)
								moze = false;
						}
					}
				}
				if (moze)
					jTerm.moguci.add(covjek);
			}
		}
		return event;
	}
	
	static JEvent stvoriJEvent(MojEvent mojEvent, ArrayList<Zauzeto> zauzeto,
			Map<String, ISchedulingData> data) { // moze_vratiti_nullspije
		JEvent event = new JEvent(mojEvent);
		event.brojTermina = (int) (mojEvent.minTerms + (mojEvent.maxTerms + 1 - mojEvent.minTerms)
				* Math.random());

		//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + mojEvent);
		for (int iX = 0; iX < event.brojTermina; iX++) {
			TreeSet<DateStamp> dat = new TreeSet<DateStamp>();
			for (ITimeParameter t : event.mojEvent.time) {
				dat.add(t.getFromDate());
				dat.add(t.getToDate());
			}
			DateStamp[] datumi = dajDatumeIzIntervala(dat.first(), dat.last());
			int[][] slobodno = new int[datumi.length][48];
			for (int j = 0; j < slobodno.length; j++)
				for (int k = 0; k < slobodno[j].length; k++) {
					slobodno[j][k] = 0;
					for (ITimeParameter t : event.mojEvent.time) {
						if (datumi[j].compareTo(t.getFromDate()) >= 0
								&& new TimeStamp(k * 15 + 8 * 60).compareTo(t
										.getFromTime()) >= 0
								&& datumi[j].compareTo(t.getToDate()) <= 0
								&& new TimeStamp(k * 15 + 8 * 60
										+ mojEvent.duration).compareTo(t
										.getToTime()) <= 0)
							slobodno[j][k] = 1;
					}
				}
			// System.out.println(Arrays.asList(datumi));
			// for (int i1=0;i1<datumi.length;i1++)
			// {System.out.println(datumi[i1]); for (int
			// j=0;j<slobodno[i1].length;j++) System.out.print(slobodno[i1][j]);
			// System.out.println(); }
			int ukupno = 0;
			for (int j = 0; j < slobodno.length; j++)
				for (int k = 0; k < slobodno[j].length; k++)
					if (slobodno[j][k] == 1)
						ukupno++;
			DateStamp datum = null;
			int sOffset = 0, eOffset = 0;
			String roomID = null;
			for (int j = 0; j < 10; j++) {
				int odabran = (int) ((ukupno + 1) * (Math.random() * 0.6 + 0.2 * (event.mojEvent.type - 1))); // odaberi_neki_1-indeksiran
				int ukupno2 = 0;
				kPetlja: for (int k = 0; k < slobodno.length; k++)
					// pronadji kad pocinje i zavrsava
					for (int l = 0; l < slobodno[k].length; l++) {
						if (slobodno[k][l] == 1)
							ukupno2++;
						if (ukupno2 == odabran) {
							datum = datumi[k];
							sOffset = l * 15 + 8 * 60;
							eOffset = sOffset + event.mojEvent.duration;
							break kPetlja;
						}
					}
				// if (datum==null) { for (int qqq=0;qqq<1000;qqq++)
				// System.out.println("ERRRRRRRRRRRRRRRRRR -  - - - datum je null!!!");
				// System.out.flush(); }
				Map<RoomData, Map<DateStamp, List<TimeSpan>>> rData = data.get(
						event.mojEvent.id).getTermData();
				petljaRD: for (RoomData rd : rData.keySet()) { // provjeri je li
																// slobodan
					if (rData.get(rd).containsKey(datum)) { // ako uopce ima tog
															// dana
						for (TimeSpan roomTS : rData.get(rd).get(datum)) {
							if (roomTS.getStart().compareTo(
									new TimeStamp(sOffset)) <= 0
									&& roomTS.getEnd().compareTo(
											new TimeStamp(eOffset)) >= 0) {
								roomID = rd.getId();
								break petljaRD;
							}
						}
					}
				}
				if (roomID == null)
					continue;
				for (Zauzeto zaz : zauzeto) {
					if (zaz.datum.equals(datum)
							&& roomID.equals(zaz.roomId)
							&& new TimeStamp(zaz.endOffset)
									.compareTo(new TimeStamp(sOffset)) > 0
							&& new TimeStamp(zaz.startOffset)
									.compareTo(new TimeStamp(eOffset)) < 0) {
						roomID = null;
						break;
					}
				}
				if (roomID != null)
					break; // slodoban je!
			}
			if (roomID == null)
				return null; // neuspijesno odredjivanje termina, vrati null

			zauzeto.add(new Zauzeto(roomID, datum, sOffset, eOffset)); // dodaj_da_je_zauzeto
			int kapacitet = 0;
			for (ILocationParameter ilp : event.mojEvent.location) { // pronadji_kapacitet
				if (ilp.getId().equals(roomID)) {
					kapacitet = ilp.getActualCapacity();
					break;
				}
				// System.out.println("                      "+roomID+" "+ilp.getId()+" "+ilp.getName()+ilp.getActualCapacity());
			}
			JTerm jTerm = new JTerm(event.mojEvent.termNames.get(iX), roomID,
					kapacitet, datum, sOffset, eOffset);
			event.termini.add(jTerm); // zauzet termin

			Map<String, Map<DateStamp, List<TimeSpan>>> pData = data.get(
					event.mojEvent.id).getPeopleData();
			for (String covjek : event.mojEvent.people) {
				boolean moze = false;
				if (pData.containsKey(covjek)) {
					if (pData.get(covjek).get(datum) == null)
						moze = true;
					else {
						moze = true;
						for (TimeSpan ts : pData.get(covjek).get(datum)) {
							if (ts.getStart().compareTo(new TimeStamp(eOffset)) < 0
									&& ts.getEnd().compareTo(
											new TimeStamp(sOffset)) > 0)
								moze = false;
						}
					}
				}
				if (moze)
					jTerm.moguci.add(covjek);
			}
			// System.out.println("-*/-*/-*/-/-*/-*/-*/-*/ "+jTerm.moguci);
		}
		return event;
	}

	static DateStamp[] dajDatumeIzIntervala(DateStamp najmanji,
			DateStamp najveci) {
		int najmanjiGodina = Integer.parseInt(najmanji.toString().substring(0,
				4));
		int najmanjiMjesec = Integer.parseInt(najmanji.toString().substring(5,
				7));
		int najmanjiDan = Integer
				.parseInt(najmanji.toString().substring(8, 10));
		int najveciGodina = Integer
				.parseInt(najveci.toString().substring(0, 4));
		int najveciMjesec = Integer
				.parseInt(najveci.toString().substring(5, 7));
		int najveciDan = Integer.parseInt(najveci.toString().substring(8, 10));
		int[] mjeseci = new int[] { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31,
				30, 31 };
		if (najmanjiGodina % 400 == 0
				|| (najmanjiGodina % 4 == 0 && najmanjiGodina % 100 != 0)) // akoJePrijestupna
			mjeseci[2] = 29;
		else
			mjeseci[2] = 28;
		ArrayList<DateStamp> vrati = new ArrayList<DateStamp>();
		vrati.add(najmanji);
		while (najmanjiGodina < najveciGodina || najmanjiMjesec < najveciMjesec
				|| najmanjiDan < najveciDan) {
			najmanjiDan++;
			if (mjeseci[najmanjiMjesec] < najmanjiDan) {
				najmanjiDan = 0;
				najmanjiMjesec++;
			}
			if (najmanjiMjesec > 12) {
				najmanjiMjesec = 1;
				najmanjiGodina++;
				if (najmanjiGodina % 400 == 0
						|| (najmanjiGodina % 4 == 0 && najmanjiGodina % 100 != 0))
					mjeseci[2] = 29;
				else
					mjeseci[2] = 28;
			}
			vrati
					.add(new DateStamp(najmanjiGodina, najmanjiMjesec,
							najmanjiDan));
		}
		return vrati.toArray(new DateStamp[0]);
	}

	static DateStamp povecajDatumZa(DateStamp datum, int distance) {
		int godina = Integer.parseInt(datum.toString().substring(0, 4));
		int mjesec = Integer.parseInt(datum.toString().substring(5, 7));
		int dan = Integer.parseInt(datum.toString().substring(8, 10));
		int[] mjeseci = new int[] { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31,
				30, 31 };
		if (godina % 400 == 0 || (godina % 4 == 0 && godina % 100 != 0)) // akoJePrijestupna
			mjeseci[2] = 29;
		else
			mjeseci[2] = 28;
		for (int i = 0; i < distance; i++) {
			dan++;
			if (mjeseci[mjesec] < dan) {
				dan = 0;
				mjesec++;
			}
			if (mjesec > 12) {
				mjesec = 1;
				godina++;
				if (godina % 400 == 0 || (godina % 4 == 0 && godina % 100 != 0)) // akoJePrijestupna
					mjeseci[2] = 29;
				else
					mjeseci[2] = 28;
			}
		}
		return new DateStamp(godina, mjesec, dan);
	}

}

class JTerm {
	// definira odmah:
	String termName;
	String roomId;
	int capacity;
	DateStamp datum;
	int startOffset;
	int endOffset;
	HashSet<String> moguci = new HashSet<String>();
	// definira kasnije:
	int popunjenost = 0;
	HashSet<String> studenti = new HashSet<String>();

	JTerm(String termName, String roomId, int capacity, DateStamp datum,
			int startoffset, int endOffset) {
		this.termName = termName;
		this.roomId = roomId;
		this.capacity = capacity;
		this.datum = datum;
		this.startOffset = startoffset;
		this.endOffset = endOffset;
		// System.out.println("---Dodan je--- "+this.toString());
	}

	@Override
	public String toString() {
		return "JTerm [capacity=" + capacity + ", datum=" + datum
				+ ", endOffset=" + endOffset + ", roomId=" + roomId
				+ ", startOffset=" + startOffset + ", termName=" + termName
				+ "]";
	}
}

class Zauzeto {
	String roomId;
	DateStamp datum;
	int startOffset;
	int endOffset;

	Zauzeto(String roomID2, DateStamp datum2, int sOffset, int eOffset) {
		roomId = roomID2;
		datum = datum2;
		startOffset = sOffset;
		endOffset = eOffset;
	}
}

class Par {
	String st;
	int in;

	Par(String st, int in) {
		this.st = new String(st);
		this.in = in;
	}

	Par(Par p) {
		this(p.st, p.in);
	}
	public String toString() {
		return st+ "/"+in;
	}
}