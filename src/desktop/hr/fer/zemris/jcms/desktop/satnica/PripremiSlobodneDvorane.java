package hr.fer.zemris.jcms.desktop.satnica;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.DateStampCache;
import hr.fer.zemris.util.time.TemporalList;
import hr.fer.zemris.util.time.TemporalNode;
import hr.fer.zemris.util.time.TimeSpanCache;
import hr.fer.zemris.util.time.TimeStampCache;
import hr.fer.zemris.util.time.TemporalList.TL;

public class PripremiSlobodneDvorane {

	/**
	 * <p>Ovo je program koji se poziva iz komandne linije, i ne pakira se u web aplikaciju.
	 * Program kao ulaz uzima format satnice koji dobijem, i pretvori/provjeri/prilagodi
	 * ga formatu s kojim dalje mozemo raditi i obaviti ucitavanje.
	 *   
	 * <p>Primjer poziva: C:/fer/ferko/dvorane_cupic_20080916.txt various-files/room_mappings.txt C:/fer/ferko/gen
	 * 
	 * <p>Primjer ulazne datoteke sa satnicom:
	 * <pre>
	 * 17.9.2008;12:00:00;14:00:00;A101;Priprema s demosima iz DIGLOG
	 * 17.9.2008;08:00:00;10:00:00;A111;Osnove elektrotehnike (1.02)
	 * </pre>
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		if(args.length!=3) {
			System.out.println("Krivi poziv! Zadajte zauzeca_ulaz, room_mappings_datoteku te zauzeca_izlaz.");
			System.exit(0);
		}
		Map<String,List<String>> roomMap = loadRoomMappings(args[1]);
		List<String> roomsList = new ArrayList<String>();
		
		Set<String> allRooms = new HashSet<String>(50);
		
		List<RoomTermBean> all = new LinkedList<RoomTermBean>();
		Set<RoomTermBean> allSet = new HashSet<RoomTermBean>(10000);
		
		InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
		List<String> satnicaLines = TextService.inputStreamToUTF8StringList(is);
		for(String line : satnicaLines) {
			String[] elems = TextService.split(line, ';');
			for(int i = 0; i < elems.length; i++) {
				elems[i] = elems[i].trim();
			}
			String wrongDate = elems[0];
			String start = elems[1].substring(0,5);
			String end = elems[2].substring(0,5);
			String room = elems[3];
			String reason = elems[4];
			String date = readDate(wrongDate);
			List<String> eventRooms = roomMap.get(room);
			if(eventRooms==null) {
				eventRooms = roomsList;
				eventRooms.clear();
				eventRooms.add(room);
			}
			for(String currentRoom : eventRooms) {
				allRooms.add(currentRoom);
			}
			for(String currentRoom : eventRooms) {
				RoomTermBean bean = new RoomTermBean();
				bean.date = date;
				bean.room = currentRoom;
				bean.start = start;
				bean.end = end;
				bean.reason = reason;
				if(allSet.add(bean)) {
					all.add(bean);
				}
			}
		}

		RoomTermBean bb;
		bb = new RoomTermBean("2008-09-29","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-09-30","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-01","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-02","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-03","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-04","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-05","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-06","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-07","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-08","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-09","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);
		bb = new RoomTermBean("2008-10-10","PCLAB3","09:00","17:00","SAP Boot Camp");
		if(allSet.add(bb)) all.add(bb);

		for(File f : new File("C:/fer/ferko/vanjska").listFiles()) {
			System.out.println("Citam vanjska: "+f);
			is = new BufferedInputStream(new FileInputStream(f));
			List<String> lines = TextService.inputStreamToUTF8StringList(is);
			for(String line : lines) {
				String[] elems = TextService.split(line, '\t');
				for(int i = 0; i < elems.length; i++) {
					elems[i] = elems[i].trim();
				}
				String start = elems[2].substring(0,5);
				String end = elems[3].substring(0,5);
				String room = elems[6];
				String reason = f.getName();
				String date = elems[1];
				List<String> eventRooms = roomMap.get(room);
				if(eventRooms==null) {
					eventRooms = roomsList;
					eventRooms.clear();
					eventRooms.add(room);
				}
				for(String currentRoom : eventRooms) {
					allRooms.add(currentRoom);
				}
				for(String currentRoom : eventRooms) {
					RoomTermBean bean = new RoomTermBean();
					bean.date = date;
					bean.room = currentRoom;
					bean.start = start;
					bean.end = end;
					bean.reason = reason;
					if(allSet.add(bean)) {
						all.add(bean);
					}
				}
			}
		}
		
		Set<String> sveDvorane = new HashSet<String>(100);
		for(RoomTermBean bean : all) {
			sveDvorane.add(bean.room);
		}

		is = new BufferedInputStream(new FileInputStream("c:/usr/eclipse_workspaces/jcms_workspace/jcms/metadata/properties/initial-data/rooms.txt"));
		List<String> roomLines = TextService.inputStreamToUTF8StringList(is);
		BufferedWriter w4 = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(args[2],"TDvoraneIliKakoVec.csv")))));
		for(String line : roomLines) {
			String[] elems = TextService.split(line, '\t');
			for(int i = 0; i < elems.length; i++) {
				elems[i] = elems[i].trim();
			}
			sveDvorane.add(elems[1]);
			w4.write(elems[1]);
			w4.write(";");
			w4.write(elems[5]);
			w4.write("\r\n");
		}
		w4.flush(); w4.close();
		
		// Mapa<dvorana,Mapa<datum,RoomTermBean>>
		Map<String,Map<String,List<RoomTermBean>>> map = new HashMap<String, Map<String,List<RoomTermBean>>>(100);
		for(RoomTermBean rt : all) {
			Map<String,List<RoomTermBean>> m = map.get(rt.room);
			if(m==null) {
				m = new HashMap<String, List<RoomTermBean>>(100);
				map.put(rt.room, m);
			}
			List<RoomTermBean> l = m.get(rt.date);
			if(l==null) {
				l = new ArrayList<RoomTermBean>();
				m.put(rt.date, l);
			}
			l.add(rt);
		}
		String fromDate = "2008-09-15";
		String toDate = "2009-01-16";
		Set<String> ignoredDates = new HashSet<String>();
		ignoredDates.add("2008-10-08");
		ignoredDates.add("2008-11-21");
		ignoredDates.add("2009-01-06");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cal.setTime(sdf2.parse(fromDate+" 12:00:00"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		cal.setTime(sdf.parse(fromDate));
		LinkedHashSet<String> allDates = new LinkedHashSet<String>(100);
		while(true) {
			String d = sdf.format(cal.getTime());
			if(d.compareTo(toDate)>0) break;
			int dow = cal.get(Calendar.DAY_OF_WEEK); 
			if(dow!=Calendar.SATURDAY && dow!=Calendar.SUNDAY) {
				boolean preskoci = false;
				if(d.compareTo("2008-10-13")>=0 && d.compareTo("2008-10-24")<=0) preskoci = true;
				if(d.compareTo("2008-11-24")>=0 && d.compareTo("2008-12-05")<=0) preskoci = true;
				if(d.compareTo("2008-12-20")>=0 && d.compareTo("2009-01-04")<=0) preskoci = true;
				if(!preskoci) {
					allDates.add(d);
					System.out.println("Dodao sam "+d);
				}
			}
			cal.add(Calendar.HOUR_OF_DAY, 24);
		}

		TimeSpanCache timeSpanCache = new TimeSpanCache();
		TimeStampCache timeStampCache = new TimeStampCache();
		DateStampCache dateStampCache = new DateStampCache();

		BufferedWriter w1 = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(args[2],"TKadSuDvoraneZauzete.csv")))));
		BufferedWriter w2 = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(args[2],"TKadSuDvoraneSlobodne.csv")))));
		BufferedWriter w5 = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(args[2],"zauzece_dvorana_graficki.csv")))));

		//List<String> dvorane = new ArrayList<String>(map.keySet());
		List<String> dvorane = new ArrayList<String>(sveDvorane);
		Collections.sort(dvorane);
		for(String dvorana : dvorane) {
			Map<String,List<RoomTermBean>> m = map.get(dvorana);
			for(String datum : allDates) {
				TemporalList tlist = new TemporalList(timeSpanCache);
				if(m!=null) {
					List<RoomTermBean> l = m.get(datum);
					if(l!=null) {
						for(RoomTermBean rt : l) {
							tlist.addInterval(
								dateStampCache.get(datum),
								timeSpanCache.get(timeStampCache.get(rt.start), timeStampCache.get(rt.end)),
								rt.reason
							);
						}
					}
				}
				Set<DateStamp> s = new HashSet<DateStamp>();
				s.add(dateStampCache.get(datum));
				TemporalList invtl = tlist.createInversionList(s, timeStampCache.get(8,0), timeStampCache.get(20,0));
				TL t = tlist.getMap().get(dateStampCache.get(datum));
				if(t!=null && t.first!=null) {
					TemporalNode n = t.first;
					while(n!=null) {
						w1.write(dvorana);
						w1.write(";");
						w1.write(datum);
						w1.write(";");
						w1.write(n.getTimeSpan().getStart().toString());
						w1.write(";");
						w1.write(n.getTimeSpan().getEnd().toString());
						w1.write(";");
						w1.write(n.getDescriptors().toString());
						w1.write("\r\n");
						n = n.getNext();
					}
					n = t.first;
					int currPos = 8*4;
					int absEndPos = 20*4;
					w5.write(datum);
					w5.write(";");
					while(n!=null) {
						int p = n.getTimeSpan().getStart().getHour()*4+(n.getTimeSpan().getStart().getMinute()%15);
						int e = n.getTimeSpan().getEnd().getHour()*4+(n.getTimeSpan().getEnd().getMinute()%15);
						while(currPos<p && currPos<absEndPos) {
							if(currPos%4==0) w5.write("|");
							w5.write(" ");
							currPos++;
						}
						while(currPos<e && currPos<absEndPos) {
							if(currPos%4==0) w5.write("|");
							w5.write("*");
							currPos++;
						}
						n = n.getNext();
					}
					while(currPos<absEndPos) {
						if(currPos%4==0) w5.write("|");
						w5.write(" ");
						currPos++;
					}
					w5.write("; ");
					w5.write(dvorana);
					w5.write("\r\n");
				} else {
					int currPos = 8*4;
					int absEndPos = 20*4;
					w5.write(datum);
					w5.write(";");
					while(currPos<absEndPos) {
						if(currPos%4==0) w5.write("|");
						w5.write(" ");
						currPos++;
					}
					w5.write("; ");
					w5.write(dvorana);
					w5.write("\r\n");
				}
				t = invtl.getMap().get(dateStampCache.get(datum));
				if(t!=null && t.first!=null) {
					TemporalNode n = t.first;
					while(n!=null) {
						w2.write(dvorana);
						w2.write(";");
						w2.write(datum);
						w2.write(";");
						w2.write(n.getTimeSpan().getStart().toString());
						w2.write(";");
						w2.write(n.getTimeSpan().getEnd().toString());
						w2.write("\r\n");
						n = n.getNext();
					}
				}
			}
		}
		w1.flush();
		w1.close();
		w2.flush();
		w2.close();
		w5.flush();
		w5.close();
		
		BufferedWriter w3full = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(args[2],"TZauzetostStudenataFull.csv")))));
		BufferedWriter w3 = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(args[2],"TZauzetostStudenata.csv")))));
		BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream("C:/fer/ferko/tmpGen/zauzetost.csv"))));
		while(true) {
			String line = r.readLine();
			if(line==null) break;
			if(line.trim().length()==0) continue;
			String[] elems = StringUtil.split(line, ';');
			for(int i = 0; i < elems.length; i++) {
				elems[i] = elems[i].trim();
			}
			w3.write(elems[0]);
			w3.write(";");
			w3.write(elems[1]);
			w3.write(";");
			w3.write(elems[2]);
			w3.write(";");
			w3.write(elems[3]);
			w3.write("\r\n");

			w3full.write(elems[0]);
			w3full.write(";");
			w3full.write(elems[1]);
			w3full.write(";");
			w3full.write(elems[2]);
			w3full.write(";");
			w3full.write(elems[3]);
			w3full.write(";");
			w3full.write(elems[4]);
			w3full.write("\r\n");
		}
		r.close();

		for(File f : new File("C:/fer/ferko/vanjska").listFiles()) {
			r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(f))));
			while(true) {
				String line = r.readLine();
				if(line==null) break;
				if(line.trim().length()==0) continue;
				String[] elems = StringUtil.split(line, '\t');
				for(int i = 0; i < elems.length; i++) {
					elems[i] = elems[i].trim();
				}
				w3.write(elems[0]);
				w3.write(";");
				w3.write(elems[1]);
				w3.write(";");
				w3.write(elems[2]);
				w3.write(";");
				w3.write(elems[3]);
				w3.write("\r\n");

				w3full.write(elems[0]);
				w3full.write(";");
				w3full.write(elems[1]);
				w3full.write(";");
				w3full.write(elems[2]);
				w3full.write(";");
				w3full.write(elems[3]);
				w3full.write(";");
				w3full.write(elems[4]);
				w3full.write("/");
				w3full.write(elems[5]);
				w3full.write("/");
				w3full.write(elems[6]);
				w3full.write("\r\n");
			}
			r.close();
		}

		w3.flush(); w3.close();
		w3full.flush(); w3full.close();

		r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream("C:/fer/ferko/tmpGen/zauzetost.csv"))));
		Map<String,Map<String,TemporalList>> opterecenja = new HashMap<String, Map<String,TemporalList>>(5000);
		while(true) {
			String line = r.readLine();
			if(line==null) break;
			if(line.trim().length()==0) continue;
			String[] elems = StringUtil.split(line, ';');
			for(int i = 0; i < elems.length; i++) {
				elems[i] = elems[i].trim();
			}
			String jmbag = elems[0];
			String datum = elems[1];
			String pocetak = elems[2];
			String kraj = elems[3];
			String razlog = elems[4].substring(elems[4].indexOf('|')+1);
			
			Map<String,TemporalList> zaStudenta = opterecenja.get(jmbag);
			if(zaStudenta==null) {
				zaStudenta = new HashMap<String, TemporalList>(allDates.size());
				opterecenja.put(jmbag, zaStudenta);
			}
			
			TemporalList tl = zaStudenta.get(datum);
			if(tl==null) {
				tl = new TemporalList(timeSpanCache);
				zaStudenta.put(datum, tl);
			}
			tl.addInterval(dateStampCache.get(datum), timeSpanCache.get(timeStampCache.get(pocetak), timeStampCache.get(kraj)), razlog);
		}
		r.close();

		for(File f : new File("C:/fer/ferko/vanjska").listFiles()) {
			r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(f))));
			while(true) {
				String line = r.readLine();
				if(line==null) break;
				if(line.trim().length()==0) continue;
				String[] elems = StringUtil.split(line, '\t');
				for(int i = 0; i < elems.length; i++) {
					elems[i] = elems[i].trim();
				}

				String jmbag = elems[0];
				String datum = elems[1];
				String pocetak = elems[2];
				String kraj = elems[3];
				String razlog = elems[4]+" / "+elems[5]+" / "+elems[6];
				
				Map<String,TemporalList> zaStudenta = opterecenja.get(jmbag);
				if(zaStudenta==null) {
					zaStudenta = new HashMap<String, TemporalList>(allDates.size());
					opterecenja.put(jmbag, zaStudenta);
				}
				
				TemporalList tl = zaStudenta.get(datum);
				if(tl==null) {
					tl = new TemporalList(timeSpanCache);
					zaStudenta.put(datum, tl);
				}
				tl.addInterval(dateStampCache.get(datum), timeSpanCache.get(timeStampCache.get(pocetak), timeStampCache.get(kraj)), razlog);
			}
			r.close();
		}

		w3full = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(args[2],"TZauzetostStudenataFull2.csv")))));
		w3 = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(args[2],"TZauzetostStudenata2.csv")))));
		List<String> jmbags = new ArrayList<String>(opterecenja.keySet());
		Collections.sort(jmbags);
		List<String> datumi = new ArrayList<String>(allDates);
		Collections.sort(datumi);
		for(String jmbag : jmbags) {
			Map<String,TemporalList> zaStudenta = opterecenja.get(jmbag);
			if(zaStudenta==null) continue;
			for(String datum : datumi) {
				TemporalList tl = zaStudenta.get(datum);
				if(tl==null) continue;
				if(tl.getMap().isEmpty()) continue;
				// Inace je samo jedan datum unutra...
				TL t = tl.getMap().entrySet().iterator().next().getValue();
				TemporalNode n = t.first;
				while(n!=null) {
					TemporalNode last = n;
					while(last.getNext()!=null && last.getNext().getTimeSpan().getStart().equals(n.getTimeSpan().getEnd())) {
						last = last.getNext();
					}
					w3.write(jmbag);
					w3.write(";");
					w3.write(datum);
					w3.write(";");
					w3.write(n.getTimeSpan().getStart().toString());
					w3.write(";");
					w3.write(last.getTimeSpan().getEnd().toString());
					w3.write("\r\n");
					
					n = last.getNext();
				}
			}
		}
		w3.flush(); w3.close();
		w3full.flush(); w3full.close();
	}

	static class RoomTermBean {
		String date;
		String start;
		String end;
		String room;
		String reason;
		public RoomTermBean() {
		}
		
		public RoomTermBean(String date, String room, String start, String end,
				String reason) {
			super();
			this.date = date;
			this.room = room;
			this.start = start;
			this.end = end;
			this.reason = reason;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((date == null) ? 0 : date.hashCode());
			result = prime * result + ((end == null) ? 0 : end.hashCode());
			result = prime * result + ((room == null) ? 0 : room.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RoomTermBean other = (RoomTermBean) obj;
			if (date == null) {
				if (other.date != null)
					return false;
			} else if (!date.equals(other.date))
				return false;
			if (end == null) {
				if (other.end != null)
					return false;
			} else if (!end.equals(other.end))
				return false;
			if (room == null) {
				if (other.room != null)
					return false;
			} else if (!room.equals(other.room))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			return true;
		}
		
	}
	private static String readDate(String wrongDate) {
		String[] elems = wrongDate.split("\\.");
		StringBuilder sb = new StringBuilder(10);
		sb.append(elems[2]);
		sb.append('-');
		if(elems[1].length()<2) sb.append('0');
		sb.append(elems[1]);
		sb.append('-');
		if(elems[0].length()<2) sb.append('0');
		sb.append(elems[0]);
		if(sb.length()!=10) {
			System.out.println("Pogresan datum: "+wrongDate);
		}
		return sb.toString();
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
