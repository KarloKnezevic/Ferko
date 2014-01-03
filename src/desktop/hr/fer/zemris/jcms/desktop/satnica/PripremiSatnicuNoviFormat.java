package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.beans.ext.GroupScheduleBean;
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

public class PripremiSatnicuNoviFormat {

	/**
	 * <p>Ovo je program koji se poziva iz komandne linije, i ne pakira se u web aplikaciju.
	 * Program kao ulaz uzima format satnice koji dobijem, i pretvori/provjeri/prilagodi
	 * ga formatu s kojim dalje mozemo raditi i obaviti ucitavanje.
	 *   
	 * <p>Primjer poziva: various-files/satnica_ulaz.txt various-files/room_mappings.txt various-files/satnica_izlaz.txt
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
		
		if(args.length!=3) {
			System.out.println("Krivi poziv! Zadajte satnicu_ulaz, room_mappings_datoteku te satnicu_izlaz.");
			System.exit(0);
		}
		Map<String,List<String>> roomMap = loadRoomMappings(args[1]);
		List<String> roomsList = new ArrayList<String>();
		
		Set<String> knownCourses = new HashSet<String>(50);
		Set<String> unknownCourses = new HashSet<String>(50);
		List<GroupScheduleBean> schedule = new ArrayList<GroupScheduleBean>(5000);
		Set<String> allRooms = new HashSet<String>(50);
		
		InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
		List<String> satnicaLines = TextService.inputStreamToUTF8StringList(is);
		for(String line : satnicaLines) {
			String[] elems = TextService.split(line, '#');
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
			String[] sveGrupe = TextService.split(grupa, ',');
			for(int i = 0; i < sveGrupe.length; i++) {
				sveGrupe[i] = sveGrupe[i].trim();
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

}
