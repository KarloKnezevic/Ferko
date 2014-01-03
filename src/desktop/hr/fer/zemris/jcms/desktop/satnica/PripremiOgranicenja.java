package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.parsers.TextService;

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

public class PripremiOgranicenja {

	/**
	 * <p>Ovo je program koji se poziva iz komandne linije, i ne pakira se u web aplikaciju.
	 * Program kao ulaz uzima format ogranicenja koji dobijem, i pretvori/provjeri/prilagodi
	 * ga formatu s kojim dalje mozemo raditi i obaviti ucitavanje.
	 *   
	 * <p>Primjer poziva: various-files/ogranicenja_ulaz.txt various-files/room_mappings.txt various-files/ogranicenja_izlaz.txt
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
		
		if(args.length!=3) {
			System.out.println("Krivi poziv! Zadajte ogranicenja_ulaz, room_mappings_datoteku te ogranicenja_izlaz.");
			System.exit(0);
		}
		Map<String,List<String>> roomMap = loadRoomMappings(args[1]);
		List<String> roomsList = new ArrayList<String>();
		
		Set<String> knownCourses = new HashSet<String>(50);
		Set<String> unknownCourses = new HashSet<String>(50);
		List<Ogranicenje> ogranicenja = new ArrayList<Ogranicenje>(500);
		Set<String> allRooms = new HashSet<String>(50);
		
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
			String grupe = elems[1].trim().toUpperCase();
			int count = Integer.parseInt(elems[2].trim());
			String nazivKolegija = elems[3].trim();
			String room = elems[4];
			String[] sveGrupe = TextService.split(grupe, ',');
			for(int i = 0; i < sveGrupe.length; i++) {
				sveGrupe[i] = sveGrupe[i].trim();
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
		// Garbage collect lines
		satnicaLines = null;
		
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(args[2]))));
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
		public String getCourseName() {
			return courseName;
		}
		public void setCourseName(String courseName) {
			this.courseName = courseName;
		}
		public String getRoom() {
			return room;
		}
		public void setRoom(String room) {
			this.room = room;
		}
	}

}
