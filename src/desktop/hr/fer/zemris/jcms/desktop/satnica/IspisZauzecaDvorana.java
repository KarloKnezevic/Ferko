package hr.fer.zemris.jcms.desktop.satnica;

import java.io.BufferedReader;
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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

public class IspisZauzecaDvorana {

	private static final String XMLRPC_URL_AUTH = "https://www.fer.hr/xmlrpc/xr_auth.php";
	private static final String XMLRPC_URL_ROOMS = "https://www.fer.hr/xmlrpc/xr_dvorane.php";

	public static boolean alokacija = true;
	public static boolean dealokacija = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		String[] brisemRezervacije = new String[] {
		};
		
		Set<String> zaBrisanje = new HashSet<String>(brisemRezervacije.length);
		for(String r : brisemRezervacije) {
			zaBrisanje.add(r);
		}
		
		String[] up = readFerkoAccount();
		//Set<String> onlyRooms = readRoomsFilter("D:\\fer\\ferko\\0910L\\effective\\in\\popisDvoranaZaIspiteFERWeb.txt");
		Set<String> onlyRooms = new HashSet<String>(); onlyRooms.add("B5");
		String user_code = "MČ005";
		String username = up[0];
		String password = up[1];
		
		String datesFrom = "2010-03-29 03:00";
		String datesTo = "2010-04-09 21:00";

		Integer login_id = login(XMLRPC_URL_AUTH, username, password);
		if(login_id==null) {
			System.out.println("Prijava neuspješna.");
			System.exit(0);
		}

		List<ReservationEntry> reservations = getReservedRooms(XMLRPC_URL_ROOMS, login_id, new ArrayList<String>(onlyRooms), user_code, datesFrom, datesTo);

		// Kada treba stvarno pokrenuti dealokaciju, ovaj parametar postaviti na true!
		dealokacija = false;
		
		for(ReservationEntry re : reservations) {
			String r = re.toString();
			boolean brisi = zaBrisanje.contains(r);
			if(brisi) {
				System.out.print("- ");
			} else {
				System.out.print(" ");
			}
			System.out.println(re);
			System.out.flush();
			if(brisi) {
				// deallocateRoom(XMLRPC_URL_ROOMS, login_id, re.getRoom(), re.getUserCode(), re.getOdUnixTime(), re.getDoUnixTime());
			}
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
		Object[] params = new Object[] {login_id, room, from, to};
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
		Object[] params = new Object[] {login_id, room, from, to};
		Boolean ok = Boolean.FALSE;
		try {
			ok = (Boolean)client.execute("dvorane.odrezerviraj",params);
		} catch (XmlRpcException e) {
			e.printStackTrace(System.out);
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

	public static String unixTimeStampToString(Integer time) {
		Date d = new Date(time.longValue()*1000);
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

	@SuppressWarnings("unchecked")
	public static List<ReservationEntry> getReservedRooms(String url, Integer login_id, List<String> rooms, String userCode, String dateTimeFrom, String dateTimeTo) {
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
		String[] roomsArray = new String[rooms.size()];
		rooms.toArray(roomsArray);
		Object[] params = new Object[] {login_id, from, to, roomsArray};
		Object[] result = null;
		try {
			result = (Object[])client.execute("dvorane.rezervirano_interval",params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			return null;
		}
		if(result==null || result.length==0) {
			return new ArrayList<ReservationEntry>(); // slobodno zauzmi
		}
		List<ReservationEntry> res = new ArrayList<ReservationEntry>(result.length);
		for(Object o : result) {
			Integer od = ((Map<String,Integer>)o).get("od");
			Integer d = ((Map<String,Integer>)o).get("do");
			String user_code = (String)(((Map<String,String>)o).get("user_code"));
			String user_public_name = (String)(((Map<String,String>)o).get("user_public_name"));
			String reason = (String)(((Map<String,String>)o).get("rez_zasto"));
			String room = (String)(((Map<String,String>)o).get("dvorana"));
			res.add(new ReservationEntry(unixTimeStampToString(od),unixTimeStampToString(d), user_code, user_public_name, reason, room, od, d));
		}
		return res;
	}
	
	static class ReservationEntry implements Comparable<ReservationEntry> {
		String startTime;
		String endTime;
		String userCode;
		String userPublicName;
		String reason;
		String room;
		Integer odUnixTime;
		Integer doUnixTime;
		
		public ReservationEntry(String startTime, String endTime,
				String userCode, String userPublicName, String reason, String room, Integer odUnixTime, Integer doUnixTime) {
			super();
			this.startTime = startTime;
			this.endTime = endTime;
			this.userCode = userCode;
			this.userPublicName = userPublicName;
			this.reason = reason;
			this.room = room;
			this.odUnixTime = odUnixTime;
			this.doUnixTime = doUnixTime;
		}
		public Integer getOdUnixTime() {
			return odUnixTime;
		}
		public Integer getDoUnixTime() {
			return doUnixTime;
		}
		public String getUserPublicName() {
			return userPublicName;
		}
		public String getStartTime() {
			return startTime;
		}
		public String getEndTime() {
			return endTime;
		}
		public String getUserCode() {
			return userCode;
		}
		public String getReason() {
			return reason;
		}
		public String getRoom() {
			return room;
		}
		@Override
		public String toString() {
			return room+" "+startTime+"-"+endTime+" "+userCode+" "+userPublicName+" "+reason + "("+odUnixTime+"-"+doUnixTime+")";
		}
		@Override
		public int compareTo(ReservationEntry o) {
			int r = startTime.compareTo(o.startTime);
			if(r!=0) return r;
			return room.compareTo(o.room);
		}
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
		Set<String> rooms = new LinkedHashSet<String>(50);
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
