package hr.fer.zemris.jcms.desktop.satnica;

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

public class PostProcesOgranicenja {

	/**
	 * <p>Ovo je program koji se poziva iz komandne linije, i ne pakira se u web aplikaciju.
	 * Program kao ulaz uzima format ogranicenja koji dobijem, i pretvori/provjeri/prilagodi
	 * ga formatu s kojim dalje mozemo raditi i obaviti ucitavanje.
	 *   
	 * <p>Primjer poziva: c:/fer/ferko/ogranicenja_grupa20080903.txt various-files/room_mappings.txt c:/fer/ferko/gen/ogranicenja_grupa_izlaz2.txt  c:/fer/ferko/prilagodbaSatnicaIsvu.txt grupe.txt kolegijiZaBurzu.txt
	 * 
	 * <p>Primjer ulazne datoteke sa satnicom:
	 * <pre>
	 * 34313#3.AUT1,3.EE1,3.ERI1#220#Automatsko upravljanje#B4
	 * 34313#3.EE2,3.ELE2,3.RK2#220#Automatsko upravljanje#B4
	 * 19674#1.01#82#Digitalna logika#A111
	 * 19674#1.02#82#Digitalna logika#A111
	 * </pre>
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		if(args.length!=6) {
			System.out.println("Krivi poziv! Zadajte ogranicenja_ulaz, room_mappings_datoteku, ogranicenja_izlaz prilagodba, valjane_grupe  kolegijiZaBurzu.txt.");
			System.exit(0);
		}
		Set<String> kolegijiZaBurzu = args.length<3 ? null : citajSifre(args[5]);
		
		Map<String,List<String>> roomMap = loadRoomMappings(args[1]);
		List<String> roomsList = new ArrayList<String>();
		
		Map<String,List<String[]>> prilagodbaMap = loadPrilagodba(args[3]);
		Map<String,Set<String>> valjaneGrupe = loadValjaneGrupe(args[4]);
		
		Set<String> knownCourses = new HashSet<String>(50);
		Set<String> unknownCourses = new HashSet<String>(50);
		List<Ogranicenje> ogranicenja = new ArrayList<Ogranicenje>(500);
		Set<String> allRooms = new HashSet<String>(50);
		Set<String> knownGroups = new HashSet<String>(50);
		Set<String> unknownGroups = new HashSet<String>(50);
		
		InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
		List<String> satnicaLines = TextService.inputStreamToUTF8StringList(is);
		char separator = 'X';
		for(String line : satnicaLines) {
			if(separator=='X') {
				if(line.indexOf('\t')!=-1) {
					separator = '\t';
				} else {
					separator = '#';
				}
			}
			String[] elems = TextService.split(line, separator);
			String sifraKolegija = elems[0].trim();
			Set<String> validGrupe = valjaneGrupe.get(sifraKolegija);
			if(validGrupe==null) {
				unknownCourses.add(sifraKolegija);
			}
			String grupe = elems[1].trim().toUpperCase();
			int count = Integer.parseInt(elems[2].trim());
			String nazivKolegija = elems[3].trim();
			String room = elems[4];
			String[] sveGrupe = TextService.split(grupe, ',');
			for(int i = 0; i < sveGrupe.length; i++) {
				sveGrupe[i] = sveGrupe[i].trim();
			}
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

			if(eventRooms.size()>1) {
				System.out.println("Pronadeno ogranicenje na vise prostorija. To ne podržavamo.");
				System.out.println(" > "+line);
				System.exit(1);
			}
			for(String currentRoom : eventRooms) {
				allRooms.add(currentRoom);
			}
			
			knownCourses.add(nazivKolegija);

			for(String currentRoom : eventRooms) {
				Ogranicenje bean = new Ogranicenje();
				bean.setCount(count);
				bean.setCourseName(nazivKolegija);
				bean.setIsvuCode(sifraKolegija);
				bean.setRoom(currentRoom);
				bean.setGroups(new ArrayList<String>());
				
				Set<String> cache = new HashSet<String>();
				for(int i = 0; i < sveGrupe.length; i++) {
					if(cache.add(sveGrupe[i])) {
						bean.getGroups().add(sveGrupe[i]);
					}
				}

				ogranicenja.add(bean);
			}
		}

		// Garbage collect lines
		satnicaLines = null;

		if(!unknownCourses.isEmpty()) {
			System.out.println("Postoje nepoznati kolegiji u zadanim ogranicenjima ("+unknownCourses.size()+")");
			System.out.println("==========================================================================");
			List<String> list = new ArrayList<String>(unknownCourses);
			Collections.sort(list);
			for(String ime : list) {
				System.out.println(ime);
			}
			System.out.println();
		}
		if(!unknownGroups.isEmpty()) {
			System.out.println("Postoje nepoznate grupe u zadanim ogranicenjima ("+unknownGroups.size()+")");
			System.out.println("==========================================================================");
			List<String> list = new ArrayList<String>(unknownGroups);
			Collections.sort(list);
			for(String ime : list) {
				System.out.println(ime);
			}
			System.out.println();
		}
		List<String> unconstrainedGroups = new ArrayList<String>();
		for(Map.Entry<String, Set<String>> entry : valjaneGrupe.entrySet()) {
			String isvu = entry.getKey();
			Set<String> groups = entry.getValue();
			for(String grupa : groups) {
				String x = isvu+"/"+grupa;
				if(knownGroups.contains(x)) continue;
				unconstrainedGroups.add(x);
			}
		}
		if(!unconstrainedGroups.isEmpty()) {
			System.out.println("Postoje grupe u ISVU-u koje nemaju zadana ogranicenja ("+unconstrainedGroups.size()+")");
			System.out.println("==========================================================================");
			List<String> list = new ArrayList<String>(unconstrainedGroups);
			Collections.sort(list);
			for(String ime : list) {
				if(ime.endsWith("/")) continue;
				String[] el = StringUtil.split(ime, '\t');
				if(kolegijiZaBurzu!=null && !kolegijiZaBurzu.contains(el[0])) continue;
				System.out.println(ime);
			}
			System.out.println();
		}
		
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(args[2])),"utf-8"));
		for(Ogranicenje bean : ogranicenja) {
			w.write(bean.getIsvuCode());
			w.write('\t');
			// Ako je to ograničenje jedne grupe, tagiraj ga s ograničenjem tipa 1
			if(bean.getGroups().size()==1) {
				w.write('1');
				w.write('\t');
				w.write(String.valueOf(bean.getCount()));
				w.write('\t');
				w.write(bean.getGroups().get(0));
				w.write("\r\n");
				continue;
			}
			// Inače je kumulativno ograničenje; tagiraj ga kao ograničenje tipa 2
			w.write('2');
			w.write('\t');
			w.write(String.valueOf(bean.getCount()));
			w.write('\t');
			w.write('"');
			w.write(bean.getGroups().get(0));
			w.write('"');
			for(int i = 1; i < bean.getGroups().size(); i++) {
				w.write('+');
				w.write('"');
				w.write(bean.getGroups().get(i));
				w.write('"');
			}
			w.write(" <= ");
			w.write(String.valueOf(bean.getCount()));
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

	private static class Ogranicenje {
		private String isvuCode;
		private List<String> groups;
		private int count;
		private String courseName;
		private String room;
		public String getIsvuCode() {
			return isvuCode;
		}
		public void setIsvuCode(String isvuCode) {
			this.isvuCode = isvuCode;
		}
		public List<String> getGroups() {
			return groups;
		}
		public void setGroups(List<String> groups) {
			this.groups = groups;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		//public String getCourseName() {
		//	return courseName;
		//}
		public void setCourseName(String courseName) {
			this.courseName = courseName;
		}
		//public String getRoom() {
		//	return room;
		//}
		public void setRoom(String room) {
			this.room = room;
		}
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

}
