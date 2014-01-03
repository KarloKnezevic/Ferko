package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class ZauzmiDvorane {

	private static final String XMLRPC_URL_AUTH = "https://www.fer.hr/xmlrpc/xr_auth.php";
	private static final String XMLRPC_URL_ROOMS = "https://www.fer.hr/xmlrpc/xr_dvorane.php";

	public static boolean alokacija = true;
	public static boolean dealokacija = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		String[] up = readFerkoAccount();
		Set<String> onlyRooms = readRoomsFilter("D:\\fer\\ferko\\0910L\\effective\\in\\popisDvoranaZaFerWeb.txt");
		String user_code = "MČ005";
		String username = up[0];
		String password = up[1];
		
		String ciklus = "03";
		String fileName = "D:/fer/ferko/0910L/labosi/c"+ciklus+"/rasporedC"+ciklus+"_tl.txt";
		String fileName2 = "D:/fer/ferko/0910L/effective/in/isvuUTF8.txt";
		
		List<ScheduleEntry> schedule = readSchedule(fileName, onlyRooms);
		List<ScheduleEntry> allocSchedule = new ArrayList<ScheduleEntry>(schedule.size());

		String datesFrom = "2010-03-08";
		String datesTo = "2010-03-12";

		nadodajFiksnaZauzeca(allocSchedule, "D:\\fer\\ferko\\0910L\\effective\\in\\vanjskaDvorane", datesFrom, datesTo, onlyRooms);
		
		Map<String,String> nameByIsvuCode = readISVU(fileName2);
		Set<String> allocated = new HashSet<String>(schedule.size());
		for(ScheduleEntry entry : schedule) {
			String key = entry.room+"/"+entry.date+"/"+entry.start;
			if(allocated.contains(key)) {
				System.out.println(">>>>>>>>>> Odbacujem "+entry);
				continue;
			}
			//System.out.println(entry.toString());
			String kolegij = nameByIsvuCode.get(entry.isvuCode);
			if(kolegij==null) {
				entry.courseName = "?";
				System.out.println("Nemam ime za kolegij: "+kolegij);
			} else {
				entry.courseName = kolegij;
			}
			allocSchedule.add(entry);
		}

		Integer login_id = login(XMLRPC_URL_AUTH, username, password);
		if(login_id==null) {
			System.out.println("Prijava neuspješna.");
			System.exit(0);
		}

		for(ScheduleEntry entry : allocSchedule) {
			try { Thread.sleep(500); } catch(Exception ignorable) {}
			String opis = entry.courseName + " / " + entry.lab + " (FERKO)";
			int iStatus = checkRoom(XMLRPC_URL_ROOMS, 
					login_id, 
					entry.room, 
					user_code, 
					entry.date+" "+entry.start, 
					entry.date+" "+entry.end, 
					opis);
			System.out.println("Termin: "+entry+", "+opis);
			switch(iStatus) {
			case 0: // zauzmi
				Boolean bResult = allocateRoom(XMLRPC_URL_ROOMS, 
						login_id, 
						entry.room, 
						user_code, 
						entry.date+" "+entry.start, 
						entry.date+" "+entry.end, 
						opis);
				System.out.println("   >>> "+bResult+" "+entry+", "+opis);
				break;
			case 1: // vec imamo
				System.out.println("   >>> imamo "+entry+", "+opis);
				break;
			case 2: // konflikt
				System.out.println("   >>> konflikt "+entry+", "+opis);
				break;
			default:
				System.out.println("   >>> nepoznat status "+iStatus+" "+entry+", "+opis);
				break;
			}
			//System.out.println("Rezultat je: "+bResult);
			//System.out.println(opis);
		}

		logout(XMLRPC_URL_AUTH, login_id);

/*
		Integer login_id = login(XMLRPC_URL_AUTH, username, password);
		if(login_id==null) {
			System.out.println("Prijava neuspješna.");
			System.exit(0);
		}
		Object[] res = getAllocatedRoom(XMLRPC_URL_ROOMS, login_id, "2008-10-07 08:00", "2008-10-07 10:00");
		System.out.println(""+Arrays.toString(res));
		
		//Boolean bResult = deallocateRoom(XMLRPC_URL_ROOMS, login_id, "A101", user_code, "2008-10-08 13:00", "2008-10-08 15:00");
		//System.out.println("Rezultat je: "+bResult);
		//Boolean bResult = allocateRoom(XMLRPC_URL_ROOMS, login_id, "A101", user_code, "2008-10-08 13:00", "2008-10-08 15:00", "Ispitivanje sustava");
		//System.out.println("Rezultat je: "+bResult);
		logout(XMLRPC_URL_AUTH, login_id);
*/
	}

	private static void nadodajFiksnaZauzeca(List<ScheduleEntry> allocSchedule, String dirName, String fromDate, String toDate, Set<String> allRooms) throws IOException {
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
				String isvu = elems[5];
				String name = elems[6];
				if(date.compareTo(fromDate)<0 || date.compareTo(toDate)>0) {
					System.out.println("Ignoriram vanjsko zauzece: "+line);
					continue;
				}
				if(!allRooms.contains(room)) {
					System.out.println("Ignoriram vanjsko zauzece: "+line);
					continue;
				}
				System.out.println("Uzimam u obzir vanjsko zauzece: "+line);
				
				ScheduleEntry se = new ScheduleEntry();
				se.courseName = name;
				se.date =date;
				se.start = sFrom;
				se.end = sTo;
				se.isvuCode = isvu;
				se.lab = "LAB";
				se.room = room;
				allocSchedule.add(se);
			}
			r.close();
		}
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

	public static Boolean allocateRoom(String url, Integer login_id, String room, String userCode, String dateTimeFrom, String dateTimeTo, String reason) {
		if(!alokacija) {
			System.out.println("Preskacem alokaciju.");
			return Boolean.TRUE;
		}
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
		Object[] params = new Object[] {login_id,room,userCode, from, to, reason};
		Boolean ok = Boolean.FALSE;
		try {
			ok = (Boolean)client.execute("dvorane.rezerviraj",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
		return ok;
	}

	@SuppressWarnings("unchecked")
	public static int checkRoom(String url, Integer login_id, String room, String userCode, String dateTimeFrom, String dateTimeTo, String reason) {
		XmlRpcClient client = null;
		Integer from;
		Integer to;
		try {
			from = unixTimeStamp(dateTimeFrom);
			to = unixTimeStamp(dateTimeTo);
		} catch (ParseException e2) {
			e2.printStackTrace();
			return -1;
		}

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(url));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return -1;
		}
		client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		client.setConfig(config);
		Object[] params = new Object[] {login_id, from, to, new String[] {room}};
		Object[] result = null;
		try {
			result = (Object[])client.execute("dvorane.rezervirano_interval",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return -2;
		}
		if(result==null || result.length==0) {
			return 0; // slobodno zauzmi
		}
		boolean konflikt = false;
		boolean imamo = false;
		for(Object o : result) {
			Integer od = ((Map<String,Integer>)o).get("od");
			Integer d = ((Map<String,Integer>)o).get("do");
			//System.out.println("od="+od+", from="+from);
			if(od.intValue()>=to.intValue() || d.intValue()<=from.intValue()) {
				//System.out.println("Ne smeta...");
				continue;
			}
			if(from.equals(od) && to.equals(d) && reason.equals(((Map<String,String>)o).get("rez_zasto")) && room.equals(((Map<String,String>)o).get("dvorana"))) {
				imamo = true;
				if(dealokacija) {
					if(userCode.equals(((Map<String,String>)o).get("user_code"))) {
						Boolean res = deallocateRoom(url, login_id, room, userCode, from, to);
						System.out.println("   Dealokacija: "+res);
						return 1;
					}
				}
				continue;
			}
			konflikt = true;
			break;
		}
		if(!konflikt) {
			if(imamo) return 1;
			return 0;
		}
		System.out.println(""+Arrays.toString(result));
		return 2; // konflikt!!!
	}

	public static Boolean deallocateRoom(String url, Integer login_id, String room, String userCode, String dateTimeFrom, String dateTimeTo) {
		if(!dealokacija) {
			System.out.println("Preskacem dealokaciju.");
			return Boolean.TRUE;
		}
		XmlRpcClient client = null;
		Integer from;
		Integer to;
		try {
			from = unixTimeStamp(dateTimeFrom);
			to = unixTimeStamp(dateTimeTo);
		} catch (ParseException e2) {
			e2.printStackTrace();
			return Boolean.FALSE;
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
		Object[] params = new Object[] {login_id, room, userCode, from, to};
		Boolean ok = Boolean.FALSE;
		try {
			ok = (Boolean)client.execute("dvorane.odrezerviraj",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
		return ok;
	}

	public static Boolean deallocateRoom(String url, Integer login_id, String room, String userCode, Integer from, Integer to) {
		if(!dealokacija) {
			System.out.println("Preskacem dealokaciju.");
			return Boolean.FALSE;
		}
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
		Object[] params = new Object[] {login_id, room, userCode, from, to};
		Boolean ok = Boolean.FALSE;
		try {
			ok = (Boolean)client.execute("dvorane.odrezerviraj",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
		return ok;
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

	public static String[] readFerkoAccount() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("../account.properties"));
		String line = br.readLine();
		line = line.trim();
		String[] res = line.split(" ");
		br.close();
		return res;
	}

	private static List<ScheduleEntry> readSchedule(String fileName, Set<String> onlyRooms) throws IOException {
		List<ScheduleEntry> list = new ArrayList<ScheduleEntry>(500);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"UTF-8"));
		while(true) {
			String line = br.readLine();
			if(line==null) break;
			line = line.trim();
			if(line.startsWith("#")) continue;
			String[] elems = line.split("\\|");
			if(!onlyRooms.contains(elems[5])) {
				continue;
			}
			ScheduleEntry e = new ScheduleEntry();
			e.date = elems[2];
			e.start = elems[3];
			e.end = elems[4];
			e.isvuCode = elems[0];
			e.lab = elems[1];
			e.room = elems[5];
			list.add(e);
		}
		br.close();
		Collections.sort(list);
		return list;
	}

	private static Map<String, String> readISVU(String fileName2) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName2),"UTF-8"));
		while(true) {
			String line = br.readLine();
			if(line==null) break;
			line = line.trim();
			if(line.startsWith("#")) continue;
			String[] elems = hr.fer.zemris.util.StringUtil.split(line, '#');
			map.put(elems[2], elems[6]);
		}
		br.close();
		return map;
	}

	static class ScheduleEntry implements Comparable<ScheduleEntry> {
		String isvuCode;
		String lab;
		String date;
		String start;
		String end;
		String room;
		String courseName;
		
		@Override
		public int compareTo(ScheduleEntry o) {
			int r;
			r = isvuCode.compareTo(o.isvuCode);
			if(r!=0) return r;
			r = room.compareTo(o.room);
			if(r!=0) return r;
			r = date.compareTo(o.date);
			if(r!=0) return r;
			r = start.compareTo(o.start);
			if(r!=0) return r;
			r = end.compareTo(o.end);
			if(r!=0) return r;
			return 0;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(50);
			sb.append(date); sb.append(" ");
			sb.append(start); sb.append(" ");
			sb.append(end); sb.append(" ");
			sb.append(room); sb.append(" ");
			sb.append(isvuCode);
			return sb.toString();
		}

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


}
