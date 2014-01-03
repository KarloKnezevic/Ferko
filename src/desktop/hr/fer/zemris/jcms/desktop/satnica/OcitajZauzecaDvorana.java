package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

public class OcitajZauzecaDvorana {

	private static final String XMLRPC_URL_AUTH = "https://www.fer.hr/xmlrpc/xr_auth.php";
	private static final String XMLRPC_URL_ROOMS = "https://www.fer.hr/xmlrpc/xr_dvorane.php";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		if(args.length != 6) {
			System.err.println("Ocekivao sam popis_soba popis_datuma popisSvihSoba izlazZauzetost izlazSlobodno folderVanjskaZauzeca");
			System.exit(0);
		}
		
		String[] up = readFerkoAccount();
		// Ove sobe gledamo u ferwebu
		Set<String> onlyRooms = readRoomsFilter(args[0]);
		// Ovo su sve sobe za koje generiramo zauzeca
		Set<String> allRooms = readRoomsFilter(args[2]);
		String username = up[0];
		String password = up[1];

		// Za svaki slucaj nadodaj sve iz onlyRooms
		allRooms.addAll(onlyRooms);
		
		Set<String> dates = readDates(args[1]);
		List<String> datesList = new ArrayList<String>(dates);
		Collections.sort(datesList);
		
		Integer login_id = login(XMLRPC_URL_AUTH, username, password);
		if(login_id==null) {
			System.out.println("Prijava neuspješna.");
			System.exit(0);
		}

		// Map<Soba, Map<Datum,List<Zauzece>>>
		Map<String, Map<String,List<Zauzece>>> mapaZauzeca = new HashMap<String, Map<String,List<Zauzece>>>();
		
		String sFromTime = "08:00";
		String sToTime = "20:00";
		int fromTime = extractMinutesFromTimeOnly(sFromTime);
		int toTime = extractMinutesFromTimeOnly(sToTime);
		
		String[] rooms = new String[onlyRooms.size()];
		onlyRooms.toArray(rooms);
		Arrays.sort(rooms);
		for(String date : datesList) {
			List<Zauzece> zauzeca = checkRooms(XMLRPC_URL_ROOMS, login_id, rooms, date+" "+sFromTime, date+" "+sToTime);
			for(Zauzece z : zauzeca) {
				Map<String,List<Zauzece>> zauzeceSobe = mapaZauzeca.get(z.room);
				if(zauzeceSobe==null) {
					zauzeceSobe = new HashMap<String, List<Zauzece>>();
					mapaZauzeca.put(z.room, zauzeceSobe);
				}
				List<Zauzece> zauzecaUDanu = zauzeceSobe.get(z.date);
				if(zauzecaUDanu==null) {
					zauzecaUDanu = new ArrayList<Zauzece>();
					zauzeceSobe.put(z.date, zauzecaUDanu);
				}
				zauzecaUDanu.add(z);
			}
		}

		// mapaZauzeca.get("A101").get("2009-03-10").add(new Zauzece("A101", "2009-03-10", extractMinutesFromTimeOnly("10:00"), extractMinutesFromTimeOnly("12:00")));
		
		logout(XMLRPC_URL_AUTH, login_id);
		System.out.println(mapaZauzeca);
		
		String[] allRoomsArray = new String[allRooms.size()];
		allRooms.toArray(allRoomsArray);
		Arrays.sort(allRoomsArray);

		nadodajFiksnaZauzeca(mapaZauzeca, args[5], dates, fromTime, toTime, allRooms);

		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(args[3]))));
		for(String room : allRoomsArray) {
			dumpajZauzeceSobe(w, mapaZauzeca, datesList, room, fromTime, toTime);
		}
		w.flush();
		w.close();

		w = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(args[4]))));
		for(String room : allRoomsArray) {
			dumpajSlobodnostSobe(w, mapaZauzeca, datesList, room, fromTime, toTime);
		}
		w.flush();
		w.close();
		System.exit(0);
	}

	private static void nadodajFiksnaZauzeca(Map<String, Map<String, List<Zauzece>>> mapaZauzeca, String dirName, Set<String> dates, int fromTime, int toTime, Set<String> allRooms) throws IOException {
		for(File f : new File(dirName).listFiles()) {
			BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(f))));
			while(true) {
				String line = r.readLine();
				if(line==null) break;
				line = line.trim();
				if(line.length()==0) continue;
				if(line.charAt(0)=='#') continue;
				String[] elems = StringUtil.split(line, ';');
				for(int i = 0; i < elems.length; i++) {
					elems[i] = elems[i].trim();
				}
				String room = elems[0];
				String date = elems[1];
				String sFrom = elems[2];
				String sTo = elems[3];
				if(!dates.contains(date)) {
					System.out.println("Ignoriram vanjsko zauzece: "+line);
					continue;
				}
				if(!allRooms.contains(room)) {
					System.out.println("Ignoriram vanjsko zauzece: "+line);
					continue;
				}
				int zFrom = extractMinutesFromTimeOnly(sFrom);
				int zTo = extractMinutesFromTimeOnly(sTo);

				if(zFrom>=toTime || zTo<=fromTime) {
					System.out.println("Ignoriram vanjsko zauzece: "+line);
					continue;
				}
				Zauzece z = new Zauzece();
				z.date = date;
				z.fromTime = zFrom;
				if(z.fromTime<fromTime) z.fromTime = fromTime;
				z.toTime = zTo;
				if(z.toTime>toTime) z.toTime = toTime;
				z.room = room;

				Map<String,List<Zauzece>> zauzeceSobe = mapaZauzeca.get(z.room);
				if(zauzeceSobe==null) {
					zauzeceSobe = new HashMap<String, List<Zauzece>>();
					mapaZauzeca.put(z.room, zauzeceSobe);
				}
				List<Zauzece> zauzecaUDanu = zauzeceSobe.get(z.date);
				if(zauzecaUDanu==null) {
					zauzecaUDanu = new ArrayList<Zauzece>();
					zauzeceSobe.put(z.date, zauzecaUDanu);
				}
				zauzecaUDanu.add(z);
			}
			r.close();
		}
	}

	private static void dumpajZauzeceSobe(BufferedWriter w, Map<String, Map<String, List<Zauzece>>> mapaZauzeca, List<String> datesList, String room, int fromTime, int toTime) throws IOException {
		Map<String, List<Zauzece>> mapaPoDatumima = mapaZauzeca.get(room);
		if(mapaPoDatumima==null) {
			// Svih je datuma slobodna
			// writeAllDatesNotTaken(w, datesList, room, fromTime, toTime);
			return;
		}
		int dif = toTime - fromTime +1;
		boolean[] lookup = new boolean[dif];
		for(String date : datesList) {
			List<Zauzece> zauzecaDana = mapaPoDatumima.get(date);
			if(zauzecaDana==null) {
				// Taj je dan slobodna
				continue;
			}
			Arrays.fill(lookup, false);
			for(Zauzece z : zauzecaDana) {
				for(int j = z.fromTime; j < z.toTime; j++) {
					lookup[j-fromTime] = true;
				}
			}
			int p = 0;
			while(true) {
				// Nadi pocetak zauzeca
				while(p<lookup.length && lookup[p]==false) p++;
				if(p>=lookup.length) break;
				int k = p+1;
				while(k<lookup.length && lookup[k]==true) k++;
				w.append(room).append(";").append(date).append(";").append(toHourMinute(p+fromTime)).append(";").append(toHourMinute(k+fromTime)).append(";\r\n");
				p = k+1;
			}
		}
		
	}

	private static void dumpajSlobodnostSobe(BufferedWriter w, Map<String, Map<String, List<Zauzece>>> mapaZauzeca, List<String> datesList, String room, int fromTime, int toTime) throws IOException {
		Map<String, List<Zauzece>> mapaPoDatumima = mapaZauzeca.get(room);
		if(mapaPoDatumima==null) {
			// Svih je datuma slobodna
			writeAllDatesFree(w, datesList, room, fromTime, toTime);
			return;
		}
		int dif = toTime - fromTime;
		boolean[] lookup = new boolean[dif];
		for(String date : datesList) {
			List<Zauzece> zauzecaDana = mapaPoDatumima.get(date);
			if(zauzecaDana==null) {
				// Taj je dan slobodna
				writeSingleDayFree(w, date, room, fromTime, toTime);
				continue;
			}
			Arrays.fill(lookup, false);
			for(Zauzece z : zauzecaDana) {
				for(int j = z.fromTime; j < z.toTime; j++) {
					lookup[j-fromTime] = true;
				}
			}
			int p = 0;
			while(true) {
				// Nadi pocetak slobodnog
				while(p<lookup.length && lookup[p]==true) p++;
				if(p>=lookup.length) break;
				int k = p+1;
				while(k<lookup.length && lookup[k]==false) k++;
				w.append(room).append(";").append(date).append(";").append(toHourMinute(p+fromTime)).append(";").append(toHourMinute(k+fromTime)).append(";\r\n");
				p = k+1;
			}
		}
	}

	private static void writeAllDatesFree(BufferedWriter w, List<String> datesList, String room, int fromTime, int toTime) throws IOException {
		for(String date : datesList) {
			writeSingleDayFree(w, date, room, fromTime, toTime);
		}
	}

	private static void writeSingleDayFree(BufferedWriter w, String date, String room, int fromTime, int toTime) throws IOException {
		w.append(room).append(";").append(date).append(";").append(toHourMinute(fromTime)).append(";").append(toHourMinute(toTime)).append(";\r\n");
	}

	public static Integer login(String url, String username, String password) {
		XmlRpcClient client = null;
		
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(url));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}
		client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		client.setConfig(config);
		Object[] params = new Object[] {username,password};
		Integer login_id = null;
		try {
			login_id = (Integer)client.execute("auth.rlogin",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
		return login_id;
	}

	static class Zauzece {
		String date;
		String room;
		int fromTime;
		int toTime;
		
		public Zauzece() {
		}
		
		public Zauzece(String room, String date, int fromTime, int toTime) {
			super();
			this.room = room;
			this.date = date;
			this.fromTime = fromTime;
			this.toTime = toTime;
		}

		@Override
		public String toString() {
			return room+";"+date+";"+toHourMinute(fromTime)+";"+toHourMinute(toTime)+";?";
		}

	}
	
	private static String toHourMinute(int t) {
		int h = t / 60;
		int m = t - h*60;
		StringBuilder sb = new StringBuilder(5);
		if(h<10) sb.append('0');
		sb.append(h);
		sb.append(':');
		if(m<10) sb.append('0');
		sb.append(m);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public static List<Zauzece> checkRooms(String url, Integer login_id, String[] rooms, String dateTimeFrom, String dateTimeTo) {
		
		// Lista zauzeca koja se gradi
		List<Zauzece> zauzeca = new ArrayList<Zauzece>();
		
		// Minuta u danu pocetka i kraja intervala koji gledamo
		int iFromTime = extractMinutes(dateTimeFrom);
		int iToTime = extractMinutes(dateTimeTo);
		
		XmlRpcClient client = null;
		Integer from;
		Integer to;
		try {
			from = unixTimeStamp(dateTimeFrom);
			to = unixTimeStamp(dateTimeTo);
		} catch (ParseException e2) {
			e2.printStackTrace();
			return null;
		}

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(url));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}
		client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		client.setConfig(config);
		Object[] params = new Object[] {login_id, from, to, rooms};
		Object[] result = null;
		try {
			result = (Object[])client.execute("dvorane.rezervirano_interval",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
		if(result==null || result.length==0) {
			return zauzeca; // slobodno zauzmi
		}
		for(Object o : result) {
			String dvorana = ((Map<String,String>)o).get("dvorana");
			Integer od = ((Map<String,Integer>)o).get("od");
			Integer d = ((Map<String,Integer>)o).get("do");
			//System.out.println("od="+od+", from="+from);
			if(od.intValue()>=to.intValue() || d.intValue()<=from.intValue()) {
				//System.out.println("Ne smeta...");
				continue;
			}
			Zauzece z = new Zauzece();
			z.date = dateTimeFrom.substring(0,10);
			String ftime = fromUnixTimeStamp(od.longValue());
			String ttime = fromUnixTimeStamp(d.longValue());
			if(!ftime.substring(0,10).equals(z.date)) {
				System.err.println("Pogresan datum (1)!");
				return null;
			}
			if(!ttime.substring(0,10).equals(z.date)) {
				System.err.println("Pogresan datum (2)!");
				return null;
			}
			z.fromTime = extractMinutes(ftime);
			if(z.fromTime<iFromTime) z.fromTime = iFromTime;
			z.toTime = extractMinutes(ttime);
			if(z.toTime>iToTime) z.toTime = iToTime;
			z.room = dvorana;
			zauzeca.add(z);
		}
		System.out.println(""+Arrays.toString(result));
		return zauzeca;
	}

	/**
	 * Ulaz "2009-03-01 17:35"
	 * @param ftime
	 * @return
	 */
	private static int extractMinutes(String ftime) {
		return Integer.parseInt(ftime.substring(11,13))*60+Integer.parseInt(ftime.substring(14,16));
	}

	/**
	 * Ulaz "17:35"
	 * @param timeOnly
	 * @return
	 */
	private static int extractMinutesFromTimeOnly(String timeOnly) {
		return Integer.parseInt(timeOnly.substring(0,2))*60+Integer.parseInt(timeOnly.substring(3,5));
	}

	public static Object[] getAllocatedRoom(String url, Integer login_id, String dateTimeFrom, String dateTimeTo) {
		XmlRpcClient client = null;
		Integer from;
		Integer to;
		try {
			from = unixTimeStamp(dateTimeFrom);
			to = unixTimeStamp(dateTimeTo);
		} catch (ParseException e2) {
			e2.printStackTrace();
			return null;
		}

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(url));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return null;
		}
		client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		client.setConfig(config);
		Object[] params = new Object[] {login_id, from, to};
		Object[] result = null;
		try {
			result = (Object[])client.execute("dvorane.rezervirano_interval",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	public static void logout(String url, Integer login_id) {
		XmlRpcClient client = null;
		
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(url));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return;
		}
		client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		client.setConfig(config);
		Object[] params = new Object[] {login_id};
		try {
			client.execute("auth.rlogout",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return;
		}
		return;
	}
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public static Integer unixTimeStamp(String time) throws ParseException {
		Date d;
		synchronized(sdf) {
			d = sdf.parse(time);
		}
		int stamp = (int)(d.getTime()/1000);
		return new Integer(stamp);
	}

	public static String fromUnixTimeStamp(long time) {
		Date d = new Date(time*1000);
		return sdf.format(d);
	}

	public static String[] readFerkoAccount() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("../account.properties"));
		String line = br.readLine();
		line = line.trim();
		String[] res = line.split(" ");
		br.close();
		return res;
	}

	private static Set<String> readRoomsFilter(String sobeFileName) throws IOException {
		Set<String> rooms = new HashSet<String>(50);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sobeFileName),"UTF-8"));
		while(true) {
			String line = br.readLine();
			if(line==null) break;
			line = line.trim();
			if(line.length()==0) continue;
			if(line.startsWith("#")) continue;
			rooms.add(line);
		}
		br.close();
		return rooms;
	}

	private static Set<String> readDates(String datesFileName) throws IOException {
		Set<String> rooms = new HashSet<String>(500);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(datesFileName),"UTF-8"));
		while(true) {
			String line = br.readLine();
			if(line==null) break;
			line = line.trim();
			if(line.length()==0) continue;
			if(line.startsWith("#")) continue;
			rooms.add(line);
		}
		br.close();
		return rooms;
	}
}
