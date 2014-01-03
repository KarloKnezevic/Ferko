package hr.fer.zemris.jcms.service.reservations.impl.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.service.reservations.IReservationManager;
import hr.fer.zemris.jcms.service.reservations.IReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.ReservationException;
import hr.fer.zemris.jcms.service.reservations.RoomReservation;
import hr.fer.zemris.jcms.service.reservations.RoomReservationEntry;
import hr.fer.zemris.jcms.service.reservations.RoomReservationPeriod;
import hr.fer.zemris.jcms.service.reservations.RoomReservationStatus;
import hr.fer.zemris.jcms.service.reservations.RoomReservationTask;
import hr.fer.zemris.jcms.service.reservations.impl.ferweb.FERWebReservationManagerFactory;
import hr.fer.zemris.util.DateUtil;

/**
 * Jednostavan upravitelj rezervacijama koji sve sprema na disk. Nije za produkcijsku uporabu.
 * 
 * @author marcupic
 */
public class FileReservationManagerFactory implements IReservationManagerFactory {

	private static final Logger logger = Logger.getLogger(FileReservationManagerFactory.class.getCanonicalName());
	private Set<String> controlledRooms = new HashSet<String>();
	private File reservationsFile;
	private ReservationDatabase rdb;
	
	public FileReservationManagerFactory() {
		InputStream is =  FERWebReservationManagerFactory.class.getClassLoader().getResourceAsStream("reservations-file-dummy.properties");
		if(is!=null) {
			Properties prop = new Properties();
			try {
				prop.load(new InputStreamReader(is,"UTF-8"));
			} catch (Exception e) {
				logger.error("Error reading reservations-ferweb.properties.");
				e.printStackTrace();
			}
			try { is.close(); } catch(Exception ignorable) {}
			String resFileName = prop.getProperty("fileName",null);
			if(resFileName==null) {
				throw new RuntimeException("U datoteci reservations-file-dummy.properties nema vrijednosti fileName. Kako je odabrana dummy implementacija reservation managera, to je obavezno.");
			}
			reservationsFile = new File(resFileName);
			if(!reservationsFile.canWrite()) {
				throw new RuntimeException("Datoteka "+reservationsFile.getAbsolutePath()+" ne postoji. Ona je potrebna za rad dummy reservation managera. Molim stvorite je. Ako nemate podataka, ona može biti i prazna (veličine 0 okteta).");
			}
		} else {
			throw new RuntimeException("Datoteka reservations-file-dummy.properties ne postoji. Kako je odabrana dummy implementacija reservation managera, ona je obavezna.");
		}
		
		is =  FERWebReservationManagerFactory.class.getClassLoader().getResourceAsStream("reservations-ferweb-rooms.txt");
		if(is!=null) {
			try {
				List<String> rooms = TextService.inputStreamToUTF8StringList(is);
				controlledRooms.addAll(rooms);
			} catch (Exception e) {
				logger.error("Error reading reservations-ferweb-rooms.txt.");
				e.printStackTrace();
			}
			try { is.close(); } catch(Exception ignorable) {}
		} else {
			logger.warn("reservations-ferweb-rooms.txt not found.");
		}
	}

	public ReservationDatabase getReservationDatabase() throws ReservationException {
		if(rdb==null) {
			FileInputStream fis = null;
			if(reservationsFile.length()==0) {
				rdb = new ReservationDatabase();
			} else {
				try {
					fis = new FileInputStream(reservationsFile);
					ObjectInput ois = new ObjectInputStream(new BufferedInputStream(fis));
					rdb = (ReservationDatabase)ois.readObject();
					ois.close();
					fis = null;
				} catch(Exception ex) {
					throw new ReservationException("Pogreška prilikom učitavanja baze.", ex);
				} finally {
					if(fis!=null) {
						try { fis.close(); } catch(Exception ignorable) {}
					}
				}
			}
		}
		return rdb;
	}
	
	public void storeReservationDatabase() throws ReservationException {
		if(rdb == null) {
			// Ovo se ne smije dogodili, ali sta je tu je...
			return;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(reservationsFile);
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(fos));
			oos.writeObject(rdb);
			oos.close();
			fos = null;
		} catch(Exception ex) {
			throw new ReservationException("Pogreška prilikom spremanja baze.", ex);
		} finally {
			if(fos!=null) {
				try { fos.close(); } catch(Exception ignorable) {}
			}
		}
	}
	
	@Override
	public IReservationManager getInstance(Long userID, String jmbag, String username) throws ReservationException {
		return new ReservationManager(userID, jmbag, username);
	}

	private class ReservationManager implements IReservationManager {

		private String jmbag;
		
		public ReservationManager(Long userID, String jmbag, String username) throws ReservationException {
			super();
			this.jmbag = jmbag;
		}

		@Override
		public Boolean isUnderControl(String roomShortName) {
			synchronized (FileReservationManagerFactory.this) {
				return Boolean.valueOf(controlledRooms.contains(roomShortName));
			}
		}

		@Override
		public void close() throws ReservationException {
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				if(r.isModified()) {
					r.resetModified();
					storeReservationDatabase();
				}
			}
		}

		@Override
		public boolean updateReservationRoom(String room,
				String oldDateTimeFrom, String oldDateTimeTo,
				String newDateTimeFrom, String newDateTimeTo, String reason,
				String context, boolean justCheck) throws ReservationException {
			throw new ReservationException("update method is not supported in this reservation manager.");
		}
		
		@Override
		public boolean allocateRoom(String room, String dateTimeFrom,
				String dateTimeTo, String reason, String context)
				throws ReservationException {
			return allocateRoom(room, dateTimeFrom, dateTimeTo, reason);
		}
		
		public boolean allocateRoom(String room, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				long fromtime = semiFullDateTimeToLong(dateTimeFrom);
				long totime = semiFullDateTimeToLong(dateTimeTo);
				// Ako je soba tada vec zauzeta, nemoj rezervirati!
				if(r.queryReservations(room, fromtime, totime).isEmpty()) {
					r.addReservation(room, fromtime, totime, reason, jmbag);
					return true;
				} else {
					return false;
				}
			}
		}

		public boolean allocateRooms(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			boolean anyProblem = false;
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				long fromtime = semiFullDateTimeToLong(dateTimeFrom);
				long totime = semiFullDateTimeToLong(dateTimeTo);
				for(RoomReservationTask task : rooms) {
					// Ako je soba tada vec zauzeta, nemoj rezervirati!
					if(r.queryReservations(task.getRoomShortName(), fromtime, totime).isEmpty()) {
						r.addReservation(task.getRoomShortName(), fromtime, totime, reason, jmbag);
						task.setSuccess(true);
					} else {
						anyProblem = true;
						task.setSuccess(false);
					}
				}
			}
			return !anyProblem;
		}

		@Override
		public RoomReservation checkRoom(String room, String dateTimeFrom,
				String dateTimeTo, String reason, String context)
				throws ReservationException {
			return checkRoom(room, dateTimeFrom, dateTimeTo, reason);
		}
		
		public RoomReservation checkRoom(String room, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				long fromtime = semiFullDateTimeToLong(dateTimeFrom);
				long totime = semiFullDateTimeToLong(dateTimeTo);
				List<ReservationEntry> list = r.queryReservations(room, fromtime, totime);
				if(list.isEmpty()) {
					return new RoomReservation(room, RoomReservationStatus.FREE);
				}
				if(list.size()==1) {
					ReservationEntry e = list.get(0);
					if(e.getFromtime()==fromtime && e.getTotime()==totime && e.getJmbag().equals(jmbag) && e.getReason().equals(reason)) {
						return new RoomReservation(room, RoomReservationStatus.RESERVED_FOR_US);
					}
				}
				return new RoomReservation(room, RoomReservationStatus.RESERVED_FOR_OTHER);
			}
		}

		@Override
		public void checkRoom(List<RoomReservation> rooms, String dateTimeFrom,
				String dateTimeTo, String reason, String context)
				throws ReservationException {
			checkRoom(rooms, dateTimeFrom, dateTimeTo, reason);
		}
		
		public void checkRoom(List<RoomReservation> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				long fromtime = semiFullDateTimeToLong(dateTimeFrom);
				long totime = semiFullDateTimeToLong(dateTimeTo);
				for(RoomReservation rr : rooms) {
					List<ReservationEntry> list = r.queryReservations(rr.getRoomShortName(), fromtime, totime);
					if(list.isEmpty()) {
						rr.setStatus(RoomReservationStatus.FREE);
						continue;
					}
					if(list.size()==1) {
						ReservationEntry e = list.get(0);
						if(e.getFromtime()==fromtime && e.getTotime()==totime && e.getJmbag().equals(jmbag) && e.getReason().equals(reason)) {
							rr.setStatus(RoomReservationStatus.RESERVED_FOR_US);
							continue;
						}
					}
					rr.setStatus(RoomReservationStatus.RESERVED_FOR_OTHER);
				}
			}
		}
		
		public boolean deallocateRoom(String room, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				long fromtime = semiFullDateTimeToLong(dateTimeFrom);
				long totime = semiFullDateTimeToLong(dateTimeTo);
				boolean res = r.deallocateReservations(room, fromtime, totime, jmbag);
				return res;
			}
		}
		
		public boolean deallocateRooms(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			boolean anyProblem = false;
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				long fromtime = semiFullDateTimeToLong(dateTimeFrom);
				long totime = semiFullDateTimeToLong(dateTimeTo);
				for(RoomReservationTask task : rooms) {
					boolean res = r.deallocateReservations(task.getRoomShortName(), fromtime, totime, jmbag);
					if(res) {
						task.setSuccess(true);
					} else {
						task.setSuccess(false);
						anyProblem = true;
					}
				}
			}
			return !anyProblem;
		}


		@Override
		public List<RoomReservationPeriod> findAvailablePeriodsForRooms(List<String> rooms, String dateTimeFrom, String dateTimeTo)	throws ReservationException {
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				if(!DateUtil.checkSemiFullDateFormat(dateTimeFrom)) {
					throw new ReservationException("Could not find available periods for rooms - wrong start time format.");
				}
				if(!DateUtil.checkSemiFullDateFormat(dateTimeTo)) {
					throw new ReservationException("Could not find available periods for rooms - wrong end time format.");
				}
				String[] dateRange = DateUtil.generateDateRange(dateTimeFrom.substring(0,10), dateTimeTo.substring(0,10), true);
				String[] middleDateRange = null;
				if(dateRange.length>2) {
					middleDateRange = new String[dateRange.length-2];
					System.arraycopy(dateRange, 1, middleDateRange, 0, middleDateRange.length);
				}
				String prviDanOdTime = dateTimeFrom.substring(11);
				String zadnjiDanDoTime = dateTimeTo.substring(11);
				String prviDan = dateTimeFrom.substring(0,10);
				String zadnjiDan = dateTimeTo.substring(0,10);
				List<String> checkList = new ArrayList<String>(rooms.size());
				List<String> notUnderControl = new ArrayList<String>(rooms.size());
				for(int i = 0; i < rooms.size(); i++) {
					String roomName = rooms.get(i);
					if(!isUnderControl(roomName)) {
						notUnderControl.add(roomName);
					} else {
						checkList.add(roomName);
					}
				}
				String[] roomsArray = new String[checkList.size()];
				for(int i = 0; i < checkList.size(); i++) {
					roomsArray[i] = checkList.get(i);
				}
				Map<String, List<ReservationEntry>> results = new HashMap<String, List<ReservationEntry>>();
				long fromtime = semiFullDateTimeToLong(dateTimeFrom);
				long totime = semiFullDateTimeToLong(dateTimeTo);
				for(String roomName : roomsArray) {
					results.put(roomName, r.queryReservations(roomName, fromtime, totime));
				}
				
				List<RoomReservationPeriod> availabilityList = new ArrayList<RoomReservationPeriod>(checkList.size()*dateRange.length*3+notUnderControl.size()*dateRange.length);
				
				// Idemo za svaku sobu iz soba koje nisu pod nasom kontrolom:
				for(String roomShortName : notUnderControl) {
					if(dateRange.length==1) {
						availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[0], prviDanOdTime, zadnjiDanDoTime));
					} else if(dateRange.length==2) {
						availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[0], prviDanOdTime, "20:00"));
						availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[dateRange.length-1], "08:00", zadnjiDanDoTime));
					} else if(dateRange.length>2) {
						availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[0], prviDanOdTime, "20:00"));
						availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[dateRange.length-1], "08:00", zadnjiDanDoTime));
						List<RoomReservationPeriod> res = generateAlwaysFree(roomShortName, middleDateRange, "08:00", "20:00");
						availabilityList.addAll(res);
					}
				}
				
				// Idemo za svaku sobu iz soba koje su pod nasom kontrolom:
				for(String roomShortName : roomsArray) {
					Map<String, boolean[]> mapaZauzeca = new HashMap<String, boolean[]>(dateRange.length);
					for(String date : dateRange) {
						boolean[] dan = new boolean[24*60];
						Arrays.fill(dan, false);
						mapaZauzeca.put(date, dan);
					}
					List<ReservationEntry> list = results.get(roomShortName);
					// Ako za tu sobu nemam zauzeca:
					if(list==null || list.isEmpty()) {
						if(dateRange.length==1) {
							availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[0], prviDanOdTime, zadnjiDanDoTime));
						} else if(dateRange.length==2) {
							availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[0], prviDanOdTime, "20:00"));
							availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[dateRange.length-1], "08:00", zadnjiDanDoTime));
						} else if(dateRange.length>2) {
							availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[0], prviDanOdTime, "20:00"));
							availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, dateRange[dateRange.length-1], "08:00", zadnjiDanDoTime));
							List<RoomReservationPeriod> res = generateAlwaysFree(roomShortName, middleDateRange, "08:00", "20:00");
							availabilityList.addAll(res);
						}
						continue;
					}
					// Inace idemo vidjeti kada su ta zauzeca:
					for(ReservationEntry o : list) {
						Date pocetak = new Date(o.getFromtime());
						Date kraj = new Date(o.getTotime());
						String odStamp = sdf.format(pocetak);
						String doStamp = sdf.format(kraj);
						String odDate = odStamp.substring(0,10);
						String doDate = doStamp.substring(0,10);
						String odTime = odStamp.substring(11);
						String doTime = doStamp.substring(11);
						// Ako su to zauzeca unutar istog dana:
						if(odDate.equals(doDate)) {
							markAsTaken(mapaZauzeca, odDate, odTime, doTime);
							continue;
						}
						// Inace se interval proteze kroz nekoliko dana:
						String[] dani = DateUtil.generateDateRange(odDate, doDate, false);
						// prvi i zadnji dan imam granice; sve u sredini ide od jutra do navecer
						if(odTime.compareTo("20:00")<0) {
							markAsTaken(mapaZauzeca, dani[0], odTime, "20:00");
						}
						if(doTime.compareTo("08:00")>0) {
							markAsTaken(mapaZauzeca, dani[dani.length-1], "08:00", doTime);
						}
						for(int k = 1; k < dani.length-1; k++) {
							markAsTaken(mapaZauzeca, dani[k], "08:00", "20:00");
						}
					}
					for(String date : dateRange) {
						boolean[] dan = mapaZauzeca.get(date);
						int poc = DateUtil.shortTimeToMinutes(date.equals(prviDan) ? prviDanOdTime : "08:00");
						int kra = DateUtil.shortTimeToMinutes(date.endsWith(zadnjiDan) ? zadnjiDanDoTime : "20:00");
						int startOfPeriod = poc;
						while(startOfPeriod<dan.length) {
							int curr = startOfPeriod;
							// Vidi do kada je dvorana slobodna
							while(curr<dan.length && !dan[curr]) curr++;
							if(curr!=startOfPeriod) {
								// Imam jedan slobodan period
								if(curr>=kra) {
									curr=kra;
									if(startOfPeriod < kra) {
										availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, date, DateUtil.minutesToShortTime(startOfPeriod), DateUtil.minutesToShortTime(curr)));
									}
									// Gotov sam s aktualnim danom
									break;
								}
								if(startOfPeriod < kra) {
									availabilityList.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, date, DateUtil.minutesToShortTime(startOfPeriod), DateUtil.minutesToShortTime(curr)));
								}
							}
							while(curr<dan.length && dan[curr]) curr++;
							startOfPeriod = curr;
						}
					}
				}
				return availabilityList;
			}
		}

		@Override
		public List<RoomReservationEntry> listRoomReservations(List<String> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			synchronized (FileReservationManagerFactory.this) {
				ReservationDatabase r = getReservationDatabase();
				long fromtime = semiFullDateTimeToLong(dateTimeFrom);
				long totime = semiFullDateTimeToLong(dateTimeTo);
				List<ReservationEntry> totalList = new ArrayList<ReservationEntry>();
				for(String roomName : rooms) {
					List<ReservationEntry> list = r.queryReservations(roomName, fromtime, totime);
					totalList.addAll(list);
				}
				List<RoomReservationEntry> result = new ArrayList<RoomReservationEntry>(totalList.size());
				for(ReservationEntry e : totalList) {
					result.add(new RoomReservationEntry(e.getRoom(), e.getJmbag(), e.getFromtime(), e.getTotime(), e.getReason()));
				}
				return result;
			}
		}

		/**
		 * Pomoćna metoda koja označava zauzeti interval u mapi dvorane. Ključevi mape su datumi, vrijednosti polja booleana,
		 * pri čemu i-ti element odgovara zauzetosti i-te minute u danu. Ako mapa nema podatke za zadani datum, zauzeće se
		 * ignorira.
		 * @param mapaZauzeca mapa zauzeća
		 * @param odDate datum zauzeća
		 * @param odTime početak zauzeća
		 * @param doTime kraj zauzeća
		 */
		private void markAsTaken(Map<String, boolean[]> mapaZauzeca, String date, String odTime, String doTime) {
			int odMin = DateUtil.shortTimeToMinutes(odTime);
			int doMin = DateUtil.shortTimeToMinutes(doTime);
			boolean[] polje = mapaZauzeca.get(date);
			if(polje==null) return;
			for(int i = odMin; i < doMin; i++) {
				polje[i] = true;
			}
		}

		/**
		 * Pomoćna metoda koja za zadanu prostoriju i raspon dana generira status koji odgovara uvijek-slobodnim terminima.
		 * 
		 * @param roomShortName prostorija
		 * @param dateRange raspon datuma (svaki element je formata yyyy-MM-dd)
		 * @param fromTime pocetak vremena u danu (formata HH:mm)
		 * @param toTime kraj vremena u danu (formata HH:mm)
		 * @return listu statusa
		 */
		private List<RoomReservationPeriod> generateAlwaysFree(String roomShortName, String[] dateRange, String fromTime, String toTime) {
			List<RoomReservationPeriod> list = new ArrayList<RoomReservationPeriod>(dateRange.length);
			for(String date : dateRange) {
				list.add(new RoomReservationPeriod(roomShortName, RoomReservationStatus.FREE, date, fromTime, toTime));
			}
			return list;
		}
		
		@Override
		public List<RoomReservationPeriod> findAvailableRoomPeriods(String room, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			synchronized (FileReservationManagerFactory.this) {
				return findAvailablePeriodsForRooms(Arrays.asList(new String[] {room}), dateTimeFrom, dateTimeTo);
			}
		}
		
		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		private long semiFullDateTimeToLong(String dateTime) throws ReservationException {
			try {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateTime).getTime();
			} catch (ParseException e) {
				throw new ReservationException("Pogrešan format vremena: "+dateTime+". Očekivao sam yyyy-MM-dd HH:mm.");
			}
		}
	}

	public static class ReservationDatabase implements Serializable, ModificationListener {
		
		private static final long serialVersionUID = 1L;
		private boolean modifiedFlag = false;
		
		// Mapa roomName, rezervacije
		private Map<String, RoomReservations> map = new HashMap<String, RoomReservations>();
		
		public void addReservation(String roomName, long fromtime, long totime, String reason, String jmbag) {
			RoomReservations r = map.get(roomName);
			if(r==null) {
				r = new RoomReservations(roomName, this);
				map.put(roomName, r);
			}
			r.addReservation(new ReservationEntry(r.getRoomName(), jmbag, fromtime, totime, reason));
		}

		public boolean deallocateReservations(String roomName, long fromtime, long totime, String jmbag) {
			RoomReservations r = map.get(roomName);
			if(r==null) {
				return false;
			}
			return r.deallocateReservations(fromtime, totime, jmbag);
		}

		public List<ReservationEntry> queryReservations(String roomName, long fromtime, long totime) {
			RoomReservations r = map.get(roomName);
			if(r==null) {
				return new ArrayList<ReservationEntry>();
			}
			return r.queryReservations(fromtime, totime);
		}
		
		@Override
		public void modified() {
			modifiedFlag = true;
		}
		
		public boolean isModified() {
			return modifiedFlag;
		}
		
		public void resetModified() {
			modifiedFlag = false;
		}
	}

	private static interface ModificationListener {
		public void modified();
	}
	
	public static class RoomReservations implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		// Mapa datum, lista rezervacije
		private Map<String, List<ReservationEntry>> entries = new HashMap<String, List<ReservationEntry>>();
		private ModificationListener listener;
		private String roomName;
		
		public RoomReservations(String roomName, ModificationListener listener) {
			if(listener==null) throw new IllegalArgumentException("listener can not be null.");
			if(roomName==null) throw new IllegalArgumentException("roomName can not be null.");
			this.listener = listener;
			this.roomName = roomName;
		}
		
		public boolean deallocateReservations(long fromtime, long totime, String jmbag) {
			Date d = new Date(fromtime);
			String key = getDate(d);
			List<ReservationEntry> list = entries.get(key);
			if(list==null) {
				return false;
			}
			boolean uspjeh = false;
			Iterator<ReservationEntry> it = list.iterator();
			while(it.hasNext()) {
				ReservationEntry e = it.next();
				if(e.fromtime==fromtime && e.totime==totime && e.jmbag.equals(jmbag)) {
					it.remove();
					uspjeh = true;
					listener.modified();
					break;
				}
			}
			if(uspjeh) {
				if(list.isEmpty()) {
					entries.remove(key);
				}
			}
			return uspjeh;
		}

		public String getRoomName() {
			return roomName;
		}
		
		public void addReservation(ReservationEntry entry) {
			if(!roomName.equals(entry.getRoom())) {
				throw new IllegalArgumentException("Can not add reservation for room "+entry.getRoom()+" in manager for room "+roomName);
			}
			Date d = new Date(entry.getFromtime());
			String key = getDate(d);
			List<ReservationEntry> list = entries.get(key);
			if(list==null) {
				list = new ArrayList<ReservationEntry>();
				entries.put(key, list);
			}
			list.add(entry);
			listener.modified();
		}

		public List<ReservationEntry> queryReservations(long fromtime, long totime) {
			List<ReservationEntry> result = new ArrayList<ReservationEntry>();
			String startDate = getDate(new Date(fromtime));
			String endDate = getDate(new Date(totime));
			String[] range = startDate.equals(endDate) ? new String[] {startDate} : DateUtil.generateDateRange(startDate, endDate, false);
			for(String date : range) {
				List<ReservationEntry> list = entries.get(date);
				if(list == null || list.isEmpty()) continue;
				for(ReservationEntry e : list) {
					// Ako dogadaj pocinje nakon perioda koji me zanima:
					if(e.getFromtime() >= totime) continue;
					// Ako dogadaj zavrsava prije perioda koji me zanima:
					if(e.getTotime() <= fromtime) continue;
					result.add(e);
				}
			}
			return result;
		}

		private String getDate(Date date) {
			return new SimpleDateFormat("yyyy-MM-dd").format(date);
		}
	}
	
	public static class ReservationEntry implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private long fromtime;
		private long totime;
		private String reason;
		private String jmbag;
		private String room;
		
		public ReservationEntry(String room, String jmbag, long fromtime,
				long totime, String reason) {
			super();
			this.room = room;
			this.jmbag = jmbag;
			this.fromtime = fromtime;
			this.totime = totime;
			this.reason = reason;
		}
		
		public long getFromtime() {
			return fromtime;
		}
		
		public long getTotime() {
			return totime;
		}
		
		public String getReason() {
			return reason;
		}
		
		public String getJmbag() {
			return jmbag;
		}
		
		public String getRoom() {
			return room;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (fromtime ^ (fromtime >>> 32));
			result = prime * result + ((jmbag == null) ? 0 : jmbag.hashCode());
			result = prime * result
					+ ((reason == null) ? 0 : reason.hashCode());
			result = prime * result + ((room == null) ? 0 : room.hashCode());
			result = prime * result + (int) (totime ^ (totime >>> 32));
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
			ReservationEntry other = (ReservationEntry) obj;
			if (fromtime != other.fromtime)
				return false;
			if (jmbag == null) {
				if (other.jmbag != null)
					return false;
			} else if (!jmbag.equals(other.jmbag))
				return false;
			if (reason == null) {
				if (other.reason != null)
					return false;
			} else if (!reason.equals(other.reason))
				return false;
			if (room == null) {
				if (other.room != null)
					return false;
			} else if (!room.equals(other.room))
				return false;
			if (totime != other.totime)
				return false;
			return true;
		}
	}
}
