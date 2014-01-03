package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.beans.ext.GroupScheduleBean;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PripremiSatnicuNoviFormat2 {

	/**
	 * <p>Ovo je program koji se poziva iz komandne linije, i ne pakira se u web aplikaciju.
	 * Program kao ulaz uzima format satnice koji dobijem, i pretvori/provjeri/prilagodi
	 * ga formatu s kojim dalje mozemo raditi i obaviti ucitavanje.
	 *   
	 * <p>Primjer poziva: c:/fer/ferko/satnica20080903.txt various-files/room_mappings.txt c:/fer/ferko/gen/satnica_izlaz2.txt c:/fer/ferko/prilagodbaSatnicaIsvu.txt c:/fer/ferko/valjaneGrupe.txt c:/fer/ferko/prilagodbaSatnicaIsvu2.txt predmetiSaSamoJednimTerminom.txt kolegiji.txt kolegijiKojiSeNecePredavati.txt
	 * 
	 * <p>Primjer ulazne datoteke sa satnicom:
	 * <pre>
	 * 2008-08-09#D1#11#3#Arhitektura računala 2#3.OIMTa1, 3.PI2, 3.RI2, 3.RZa1, 3.TIa1#34277
	 * 2008-08-23#D1#11#3#Arhitektura računala 2#3.OIMTa1, 3.PI2, 3.RI2, 3.RZa1, 3.TIa1#34277
	 * 2008-09-06#D1#11#3#Arhitektura računala 2#3.OIMTa1, 3.PI2, 3.RI2, 3.RZa1, 3.TIa1#34277
	 * </pre>
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		if(args.length!=9) {
			System.out.println("Krivi poziv! Zadajte satnicu_ulaz, room_mappings_datoteku, satnicu_izlaz, prilagodbaSatnicaUlaz, valjaneGrupe prilagodbaSatnicaUlaz2 predmetiSaSamoJednimTerminom.txt kolegiji.txt kolegijiKojiSeNecePredavati.txt.");
			System.exit(0);
		}
		Map<String,List<String>> roomMap = loadRoomMappings(args[1]);
		List<String> roomsList = new ArrayList<String>();

		Map<String,List<String[]>> prilagodbaMap = loadPrilagodba(args[3]);
		Map<String,Set<String>> valjaneGrupe = loadValjaneGrupe(args[4]);

		Map<String,List<String>> nadodavanjeGrupa = loadNadodavanjeGrupa(args[5]);
		Map<String,String> naziviKolegija = ucitajNaziveKolegija(args[7]);
		Set<String> dozvoljeneKorekcijeGrupa = citajSifre(args[6]);

		Set<String> neceSePredavati = citajSifre2(args[8]);
		
		Set<String> knownCourses = new HashSet<String>(50);
		Set<String> unknownCourses = new HashSet<String>(50);
		List<GroupScheduleBean> schedule = new ArrayList<GroupScheduleBean>(5000);
		Set<String> allRooms = new HashSet<String>(50);
		Set<String> knownGroups = new HashSet<String>(50);
		Set<String> unknownGroups = new HashSet<String>(50);
		Set<String> kolegijiUSatnici = new HashSet<String>(200);
		Map<String,Set<String>> grupeKojeImajuSatnicu = new HashMap<String, Set<String>>(200);
		
		InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
		List<String> satnicaLines = TextService.inputStreamToUTF8StringList(is);
		for(String line : satnicaLines) {
			String[] elems = TextService.split(line, '#');
			if(elems.length<7) {
				System.out.println("Pogresan redak u satnici: "+line);
				continue;
			}
			String date = elems[0];
			String room = elems[1];
			String start = elems[2];
			if(start.length()==1) {
				start = "0"+start+":00";
			} else {
				start = start+":00";
			}
			int duration = Integer.parseInt(elems[3])*60;
			String nazivKolegija = elems[4].trim();
			String grupa = elems[5].trim().toUpperCase();
			String sifraKolegija = elems[6].trim();
			kolegijiUSatnici.add(sifraKolegija);
			Set<String> validGrupe = valjaneGrupe.get(sifraKolegija);
			if(validGrupe==null) {
				unknownCourses.add(sifraKolegija);
			}
			String[] sveGrupe = TextService.split(grupa, ',');
			for(int i = 0; i < sveGrupe.length; i++) {
				sveGrupe[i] = sveGrupe[i].trim();
			}
			sveGrupe = nadodajGrupe(nadodavanjeGrupa, sifraKolegija, sveGrupe);
			sveGrupe = prilagodiGrupe(prilagodbaMap, sifraKolegija, sveGrupe);
			if(sveGrupe.length==0) {
				System.out.println("Uklanjam ogranicenje: "+line);
				continue;
			}
			for(int i = 0; i < sveGrupe.length; i++) {
				if(validGrupe!=null) {
					if(!validGrupe.contains(sveGrupe[i])) {
						unknownGroups.add(sifraKolegija+"/"+sveGrupe[i]);
					} else {
						knownGroups.add(sifraKolegija+"/"+sveGrupe[i]);
					}
				}
			}
			
			List<String> eventRooms = roomMap.get(room);
			if(eventRooms==null) {
				eventRooms = roomsList;
				eventRooms.clear();
				eventRooms.add(room);
			}
			for(String currentRoom : eventRooms) {
				allRooms.add(currentRoom);
			}
			
			knownCourses.add(nazivKolegija);

			Set<String> grupeSaSatnicom = grupeKojeImajuSatnicu.get(sifraKolegija);
			if(grupeSaSatnicom==null) {
				grupeSaSatnicom = new HashSet<String>();
				grupeKojeImajuSatnicu.put(sifraKolegija, grupeSaSatnicom);
			}
			for(int i = 0; i < sveGrupe.length; i++) {
				grupeSaSatnicom.add(sveGrupe[i]);
			}
			for(String currentRoom : eventRooms) {
				GroupScheduleBean bean = new GroupScheduleBean();
				bean.setDate(date);
				bean.setStart(start);
				bean.setDuration(duration);
				bean.setIsvuCode(sifraKolegija);
				bean.setRoom(currentRoom);
				bean.setVenue("FER");
				
				Set<String> cache = new HashSet<String>();
				for(int i = 0; i < sveGrupe.length; i++) {
					if(cache.add(sveGrupe[i])) {
						bean.getGroups().add(sveGrupe[i]);
					}
				}

				schedule.add(bean);
			}
		}

		// Garbage collect lines
		satnicaLines = null;

		if(!unknownCourses.isEmpty()) {
			System.out.println("Postoje nepoznati kolegiji u satnici ("+unknownCourses.size()+")");
			System.out.println("==========================================================================");
			List<String> list = new ArrayList<String>(unknownCourses);
			Collections.sort(list);
			for(String ime : list) {
				System.out.println(ime);
			}
			System.out.println();
		}
		if(!unknownGroups.isEmpty()) {
			System.out.println("Postoje nepoznate grupe u satnici ("+unknownGroups.size()+")");
			System.out.println("==========================================================================");
			List<String> list = new ArrayList<String>(unknownGroups);
			Collections.sort(list);
			for(String ime : list) {
				System.out.println(ime);
			}
			System.out.println();
		}
		// Ovo su sve grupe koje postoje ali nemaju satnice
		List<String> unscheduledGroups = new ArrayList<String>();
		// Ovo su samo one grupe na kolegijima koji imaju satnicu, a koje nemaju satnice
		List<String> unscheduledGroups2 = new ArrayList<String>();
		for(Map.Entry<String, Set<String>> entry : valjaneGrupe.entrySet()) {
			String isvu = entry.getKey();
			Set<String> groups = entry.getValue();
			for(String grupa : groups) {
				String x = isvu+"/"+grupa;
				if(knownGroups.contains(x)) continue;
				unscheduledGroups.add(x);
				if(kolegijiUSatnici.contains(isvu)) {
					unscheduledGroups2.add(x);
				}
			}
		}
		if(!unscheduledGroups.isEmpty()) {
			System.out.println("Postoje grupe u ISVU-u koje nemaju satnicu ("+unscheduledGroups.size()+")");
			System.out.println("==========================================================================");
			List<String> list = new ArrayList<String>(unscheduledGroups);
			Collections.sort(list);
			int broj = 0;
			List<String> korekcije = new ArrayList<String>();
			for(String ime : list) {
				if(ime.endsWith("/")) continue;
				String[] el = StringUtil.split(ime, '/');
				String isvu = el[0];
				if(neceSePredavati.contains(isvu)) continue;
				broj++;
				System.out.println(ime+"\t"+courseName(naziviKolegija, isvu));
				if(!dozvoljeneKorekcijeGrupa.contains(isvu)) continue;
				Set<String> izborGrupa = grupeKojeImajuSatnicu.get(isvu);
				if(izborGrupa==null || izborGrupa.isEmpty()) continue;
				korekcije.add(isvu+"\t"+el[1]+"\t"+izborGrupa.iterator().next());
			}
			System.out.println("Efektivni broj: "+broj);
			if(!korekcije.isEmpty()) {
				System.out.println("Predložio sam "+korekcije.size()+" korekcija u ../satnica-korekcije.txt.");
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("../satnica-korekcije.txt")),"UTF-8"));
				for(String k : korekcije) {
					bw.write(k); bw.write("\r\n");
				}
				bw.close();
			}
			System.out.println();
		}
		if(!unscheduledGroups2.isEmpty()) {
			System.out.println("Postoje grupe u ISVU-u koje nemaju satnicu dok druge grupe istog kolegija imaju ("+unscheduledGroups2.size()+")");
			System.out.println("========================================================================================");
			List<String> list = new ArrayList<String>(unscheduledGroups2);
			Collections.sort(list);
			int broj = 0;
			for(String ime : list) {
				if(ime.endsWith("/")) continue;
				String[] el = StringUtil.split(ime, '/');
				if(neceSePredavati.contains(el[0])) {
					System.out.println(ime+"    !\t"+courseName(naziviKolegija, el[0]));
				} else {
					System.out.println(ime+"\t"+courseName(naziviKolegija, el[0]));
				}
				broj++;
			}
			System.out.println("Efektivni broj: "+broj);
			System.out.println();
		}
		Set<String> nerasporedeniKolegiji = new HashSet<String>(valjaneGrupe.keySet());
		nerasporedeniKolegiji.removeAll(kolegijiUSatnici);
		if(!nerasporedeniKolegiji.isEmpty()) {
			System.out.println("Postoje neraspoređeni kolegiji ("+nerasporedeniKolegiji.size()+")");
			System.out.println("========================================");
			List<String> list = new ArrayList<String>(nerasporedeniKolegiji);
			Collections.sort(list);
			int broj = 0;
			for(String ime : list) {
				if(neceSePredavati.contains(ime)) continue;
				broj++;
				System.out.println(ime+"\t"+courseName(naziviKolegija, ime));
			}
			System.out.println("Efektivni broj: "+broj);
			System.out.println();
		}
		
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(args[2])),"utf-8"));
		for(GroupScheduleBean bean : schedule) {
			w.write(bean.getDate());
			w.write('#');
			w.write(bean.getStart());
			w.write('#');
			w.write(String.valueOf(bean.getDuration()));
			w.write('#');
			w.write(bean.getVenue());
			w.write('#');
			w.write(bean.getRoom());
			w.write('#');
			w.write(bean.getIsvuCode());
			w.write('#');
			w.write(bean.getGroups().get(0));
			for(int i = 1; i < bean.getGroups().size(); i++) {
				w.write(',');
				w.write(bean.getGroups().get(i));
			}
			w.write("\r\n");
		}
		w.close();
		
		System.out.println("Sve sobe ("+allRooms.size()+")");
		System.out.println("==========================================================================");
		List<String> allRoomsSorted = new ArrayList<String>(allRooms);
		Collections.sort(allRoomsSorted);
		for(String r : allRoomsSorted) {
			System.out.println("Room: "+r);
		}
		System.out.println();
	}

	// Ako ovdje u popisu grupa vidim 
	private static String[] nadodajGrupe(Map<String, List<String>> nadodavanjeGrupa, String sifraKolegija, String[] sveGrupe) {
		List<String> novo = null;
		for(int i = 0; i < sveGrupe.length; i++) {
			List<String> dodati = nadodavanjeGrupa.get(sifraKolegija+"\t"+sveGrupe[i]);
			if(dodati==null) {
				// Za ovo nemam nista novoga
				if(novo!=null) {
					if(!novo.contains(sveGrupe[i])) novo.add(sveGrupe[i]);
				}
				continue;
			}
			// Inace nesto trebam dodati... Ako je ovo prvi puta:
			if(novo==null) {
				novo = new ArrayList<String>(16);
				for(int j = 0; j <= i; j++) {
					if(!novo.contains(sveGrupe[j])) {
						novo.add(sveGrupe[j]);
					}
				}
			}
			for(int j = dodati.size()-1; j >= 0; j--) {
				String g = dodati.get(j);
				if(!novo.contains(g)) {
					novo.add(g);
				}
			}
		}
		if(novo!=null) {
			String[] n = new String[novo.size()];
			novo.toArray(n);
			return n;
		}
		return sveGrupe;
	}

	private static Map<String, Set<String>> loadValjaneGrupe(String fileName) throws IOException {
		Map<String,Set<String>> pMap = new HashMap<String, Set<String>>();
		InputStream is = new BufferedInputStream(new FileInputStream(fileName));
		List<String> mappingLines = TextService.inputStreamToUTF8StringList(is);
		for(String line : mappingLines) {
			String[] elems = TextService.split(line, '\t');
			Set<String> l = pMap.get(elems[0]);
			if(l==null) {
				l = new HashSet<String>();
				pMap.put(elems[0], l);
			}
			for(int i = 1; i < elems.length; i++) {
				l.add(elems[i]);
			}
		}
		return pMap;
	}

	private static String[] prilagodiGrupe(Map<String, List<String[]>> prilagodbaMap, String sifraKolegija, String[] sveGrupe) {
		List<String[]> p = prilagodbaMap.get(sifraKolegija);
		if(p==null) {
			return sveGrupe;
		}
		ArrayList<String> list = new ArrayList<String>(sveGrupe.length);
outer:	for(String g : sveGrupe) {
			for(String[] e : p) {
				if(g.equals(e[0])) {
					if(e[1].equals("-")) continue outer;
					list.add(e[1]);
					continue outer;
				}
			}
			list.add(g);
		}
		sveGrupe = new String[list.size()];
		sveGrupe = list.toArray(sveGrupe);
		return sveGrupe;
	}

	private static Map<String, List<String[]>> loadPrilagodba(String fileName) throws IOException {
		Map<String,List<String[]>> pMap = new HashMap<String, List<String[]>>();
		InputStream is = new BufferedInputStream(new FileInputStream(fileName));
		List<String> mappingLines = TextService.inputStreamToUTF8StringList(is);
		for(String line : mappingLines) {
			String[] elems = TextService.split(line, '\t');
			List<String[]> l = pMap.get(elems[0]);
			if(l==null) {
				l = new ArrayList<String[]>();
				pMap.put(elems[0], l);
			}
			l.add(new String[] {elems[1],elems[2]});
		}
		return pMap;
	}

	private static Map<String, List<String>> loadNadodavanjeGrupa(String fileName) throws IOException {
		Map<String,List<String>> pMap = new HashMap<String, List<String>>();
		InputStream is = new BufferedInputStream(new FileInputStream(fileName));
		List<String> mappingLines = TextService.inputStreamToUTF8StringList(is);
		for(String line : mappingLines) {
			if(line.length()==0)continue;
			if(line.charAt(0)=='#') continue;
			String[] elems = TextService.split(line, '\t');
			String key = elems[0]+"\t"+(elems[2].toUpperCase());
			List<String> l = pMap.get(key);
			if(l==null) {
				l = new ArrayList<String>();
				pMap.put(key, l);
			}
			String g = elems[1].toUpperCase();
			if(!l.contains(g)) l.add(g);
		}
		return pMap;
	}

	private static Map<String, List<String>> loadRoomMappings(String fileName) throws IOException {
		Map<String,List<String>> roomMap = new HashMap<String, List<String>>();
		InputStream is = new BufferedInputStream(new FileInputStream(fileName));
		List<String> mappingLines = TextService.inputStreamToUTF8StringList(is);
		for(String line : mappingLines) {
			String[] elems = TextService.split(line, '\t');
			List<String> l = new ArrayList<String>();
			for(int i = 1; i < elems.length; i++) {
				l.add(elems[i]);
			}
			roomMap.put(elems[0], l);
		}
		return roomMap;
	}

	private static Set<String> citajSifre(String file) throws IOException {
		Set<String> set = new HashSet<String>(200);
		List<String> lines = TextService.inputStreamToUTF8StringList(new BufferedInputStream(new FileInputStream(file)));
		for(String line : lines) {
			line = line.trim();
			if(line.isEmpty()) continue;
			set.add(line);
		}
		return set;
	}

	private static Set<String> citajSifre2(String file) throws IOException {
		Set<String> set = new HashSet<String>(200);
		List<String> lines = TextService.inputStreamToUTF8StringList(new BufferedInputStream(new FileInputStream(file)));
		for(String line : lines) {
			line = line.trim();
			if(line.isEmpty()) continue;
			String[] el = StringUtil.split(line, '\t');
			set.add(el[0]);
		}
		return set;
	}

	private static Map<String, String> ucitajNaziveKolegija(String fileName) throws IOException {
		Map<String, String> m = new HashMap<String, String>(200);
		List<String> lines = TextService.inputStreamToUTF8StringList(new BufferedInputStream(new FileInputStream(fileName)));
		for(String line : lines) {
			line = line.trim();
			if(line.isEmpty()) continue;
			String[] el = StringUtil.split(line, '\t');
			m.put(el[0], el[1]);
		}
		return m;
	}
	
	private static String courseName(Map<String, String> courseNames, String isvu) {
		String n = courseNames.get(isvu);
		if(n==null) return "?";
		return n;
	}
}
