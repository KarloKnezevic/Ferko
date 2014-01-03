package hr.fer.zemris.util.scheduling.support;

import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationManager2 implements Serializable, Cloneable {
	

	private static final long serialVersionUID = 8930711433353368120L;
	private ReservationManager2 fixedReservationManager;
	//      Map<JMBAG, Map<DateStamp, int[]>>
	private Map<DateStamp, short[]>[] studentReservation;
	//      Map<RoomID, Map<DateStamp, int[]>>
	private Map<DateStamp, byte[]>[] roomReservation;
	
	private ItemCache jmbagsCache;
	private ItemCache termsCache;
	public ReservationManager2() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public ReservationManager2(Map<String, ISchedulingData> data) {
		for(String eventId:data.keySet()) {
			jmbagsCache = data.get(eventId).getJmbagsCache();
			termsCache = data.get(eventId).getTermsCache();
			if(studentReservation==null) {
				studentReservation = new Map[jmbagsCache.size()];
			}
			if(roomReservation==null) {
				roomReservation = new Map[termsCache.size()];
			}
			Map<String, Map<DateStamp, List<TimeSpan>>> peopleData = data.get(eventId).getPeopleData();
			for(String studentJMBAG:peopleData.keySet()) {
				for(DateStamp dateStamp:peopleData.get(studentJMBAG).keySet()) {
					for(TimeSpan timeSpan:peopleData.get(studentJMBAG).get(dateStamp)) {
						reserveStudentFixed(studentJMBAG, dateStamp, timeSpan);
					}
				}
			}
			Map<RoomData, Map<DateStamp, List<TimeSpan>>> termData = data.get(eventId).getTermData();
			for(RoomData roomData:termData.keySet()) {
				for(DateStamp dateStamp:termData.get(roomData).keySet()) {
					for(TimeSpan timeSpan:termData.get(roomData).get(dateStamp)) {
						reserveRoomFixed(roomData.getId(), dateStamp, timeSpan);
					}
				}
			}
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		ReservationManager2 ret = new ReservationManager2(fixedReservationManager);
		ret.setStudentReservation(studentReservation);
		ret.setRoomReservation(roomReservation);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private void setStudentReservation(Map<DateStamp, short[]>[] studentReservation2) {
		this.studentReservation = new Map[studentReservation2.length];
		for(int i = 0; i < studentReservation2.length; i++) {
			Map<DateStamp, short[]> s2 = new HashMap<DateStamp, short[]>();
			this.studentReservation[i] = s2;
			Map<DateStamp, short[]> s1 = studentReservation2[i];
			if(s1!=null) {
				for(DateStamp d:s1.keySet()) {
					short[] t = s1.get(d);
					s2.put(d, Arrays.copyOf(t, t.length));
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setRoomReservation(Map<DateStamp, byte[]>[] roomReservation2) {
		this.roomReservation = new Map[roomReservation2.length];
		for(int i = 0; i < roomReservation2.length; i++) {
			Map<DateStamp, byte[]> s2 = new HashMap<DateStamp, byte[]>();
			this.roomReservation[i] = s2;
			Map<DateStamp, byte[]> s1 = roomReservation2[i];
			if(s1!=null) {
				for(DateStamp d:s1.keySet()) {
					byte[] t = s1.get(d);
					s2.put(d, Arrays.copyOf(t, t.length));
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public ReservationManager2(ReservationManager2 fixedReservationManager) {
		this.fixedReservationManager=fixedReservationManager;
		jmbagsCache = fixedReservationManager.jmbagsCache;
		termsCache = fixedReservationManager.termsCache;
		studentReservation = new Map[jmbagsCache.size()];
		roomReservation = new Map[termsCache.size()];
	}
	
	private void reserveStudentFixed(int studentIndex, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, short[]> s1 = studentReservation[studentIndex];
		if(s1==null) {
			s1=new HashMap<DateStamp, short[]>();
			studentReservation[studentIndex] = s1;
		}
		short[] s2 = s1.get(dateStamp);
		if(s2==null) {
			s2=new short[24*4];
			s1.put(dateStamp, s2);
		}
		Arrays.fill(s2, getStartIdx(timeSpan), getEndIdx(timeSpan), (short)1);
	}

	private void reserveStudentFixed(String JMBAG, DateStamp dateStamp, TimeSpan timeSpan) {
		reserveStudentFixed(jmbagsCache.translate(JMBAG), dateStamp, timeSpan);
	}
	
	public void reserveStudent(int studentIndex, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, short[]> s1 = studentReservation[studentIndex];
		if(s1==null) {
			s1=new HashMap<DateStamp, short[]>();
			studentReservation[studentIndex] = s1;
		}
		short[] s2 = s1.get(dateStamp);
		if(s2==null) {
			short[] occupancesForDateFixed=fixedReservationManager.studentReservation[studentIndex].get(dateStamp);
			if(occupancesForDateFixed!=null)
				s2 = Arrays.copyOf(occupancesForDateFixed, occupancesForDateFixed.length);
			else
				s2=new short[24*4];
			s1.put(dateStamp, s2);
		}
		int startIdx=getStartIdx(timeSpan);
		int endIdx=getEndIdx(timeSpan);
		for(int i=startIdx;i<endIdx;i++) {
			s2[i]+=1;
		}
	}

	public void reserveStudent(String JMBAG, DateStamp dateStamp, TimeSpan timeSpan) {
		if(jmbagsCache==null) {
			System.out.println("null!!");
		}
		reserveStudent(jmbagsCache.translate(JMBAG), dateStamp, timeSpan);
	}

	private int getStartIdx(TimeSpan timeSpan) {
		return (int)Math.floor(timeSpan.getStart().getAbsoluteTime()/15);
	}
	
	private int getEndIdx(TimeSpan timeSpan) {
		return (int)Math.ceil(timeSpan.getEnd().getAbsoluteTime()/15);
	}
	
	public boolean isStudentReserved(String JMBAG, DateStamp dateStamp, TimeSpan timeSpan) {
		return isStudentReserved(jmbagsCache.translate(JMBAG), dateStamp, timeSpan);
	}
	
	public boolean isStudentReserved(int studentIndex, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, short[]> studentReservationRecord = studentReservation[studentIndex];
		if(studentReservationRecord==null) {
			studentReservationRecord = fixedReservationManager.studentReservation[studentIndex];
		}
		short[] occupancesForDate = studentReservationRecord.get(dateStamp);
		if(occupancesForDate==null) {
			occupancesForDate = fixedReservationManager.studentReservation[studentIndex].get(dateStamp);
			if(occupancesForDate==null)
				return false;
		}
		for(int i=getStartIdx(timeSpan);i<getEndIdx(timeSpan);i++) {
			if(occupancesForDate[i]!=0)
				return true;
		}
		return false;
	}
	
	public void clearStudentReservationForTimeSpan(String JMBAG, DateStamp dateStamp, TimeSpan timeSpan) {
		clearStudentReservationForTimeSpan(jmbagsCache.translate(JMBAG), dateStamp, timeSpan);
	}
	
	public void clearStudentReservationForTimeSpan(int studentIndex, DateStamp dateStamp, TimeSpan timeSpan) {
		if(!isStudentReserved(studentIndex, dateStamp, timeSpan))
			return;
		else {
			Map<DateStamp, short[]> studentReservationRecord = studentReservation[studentIndex];
			if(studentReservationRecord==null)
				return;
			short[] occupancesForDate = studentReservationRecord.get(dateStamp);
			if(occupancesForDate==null)
				return;
			if(fixedReservationManager.studentReservation[studentIndex]!=null) {
				short[] occupancesForDateFixed=fixedReservationManager.studentReservation[studentIndex].get(dateStamp);
				if(occupancesForDateFixed!=null) {
					int startIdx=getStartIdx(timeSpan);
					int endIdx=getEndIdx(timeSpan);
					for(int i=startIdx;i<endIdx;i++)
						occupancesForDate[i]=occupancesForDateFixed[i];
				}
				else {
					int startIdx=getStartIdx(timeSpan);
					int endIdx=getEndIdx(timeSpan);
					for(int i=startIdx; i<endIdx; i++)
						occupancesForDate[i]--;
				}
			}
			else {
				int startIdx=getStartIdx(timeSpan);
				int endIdx=getEndIdx(timeSpan);
				for(int i=startIdx; i<endIdx; i++)
					occupancesForDate[i]--;
			}
			studentReservationRecord.put(dateStamp, occupancesForDate);
		}
	}
	
	public void clearStudentReservationForDateStamp(String JMBAG, DateStamp dateStamp) {
		clearStudentReservationForDateStamp(jmbagsCache.translate(JMBAG), dateStamp);
	}
	
	public void clearStudentReservationForDateStamp(int studentIndex, DateStamp dateStamp) {
		Map<DateStamp, short[]> studentReservationRecord = studentReservation[studentIndex];
		if(studentReservationRecord==null)
			return;
		studentReservationRecord.remove(dateStamp);
	}
	
	public void clearStudentReservation(String JMBAG) {
		studentReservation[jmbagsCache.translate(JMBAG)]=null;
	}
	
	public void clearStudentReservation(int studentIndex) {
		studentReservation[studentIndex] = null;
	}
	

	public void reserveRoom(int roomIndex, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, byte[]> s1 = roomReservation[roomIndex];
		if(s1==null) {
			s1=new HashMap<DateStamp, byte[]>();
			roomReservation[roomIndex] = s1;
		}
		byte[] s2 = s1.get(dateStamp);
		if(s2==null) {
			byte[] occupancesForDateFixed=fixedReservationManager.roomReservation[roomIndex].get(dateStamp);
			if(occupancesForDateFixed!=null)
				s2= Arrays.copyOf(occupancesForDateFixed, occupancesForDateFixed.length);
			else
				s2=new byte[24*4];
			s1.put(dateStamp, s2);
			
		}
		int startIdx=getStartIdx(timeSpan);
		int endIdx=getEndIdx(timeSpan);
		for(int i=startIdx;i<endIdx;i++) {
			s2[i]+=1;
		}
	}
	
	public void reserveRoom(String roomId, DateStamp dateStamp, TimeSpan timeSpan) {
		reserveRoom(termsCache.translate(roomId), dateStamp, timeSpan);
	}


	private void reserveRoomFixed(int roomIndex, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, byte[]> s1 = roomReservation[roomIndex];
		if(s1==null) {
			s1=new HashMap<DateStamp, byte[]>();
			roomReservation[roomIndex] = s1;
		}
		byte[] s2 = s1.get(dateStamp);
		if(s2==null) {
			s2=new byte[24*4];
			Arrays.fill(s2, (byte)1);
			s1.put(dateStamp, s2);	
		}
		Arrays.fill(s2, getStartIdx(timeSpan), getEndIdx(timeSpan), (byte)0);
	}
	
	private void reserveRoomFixed(String roomId, DateStamp dateStamp, TimeSpan timeSpan) {
		reserveRoomFixed(termsCache.translate(roomId), dateStamp, timeSpan);
	}
	
	public boolean isRoomReserved(String roomId, DateStamp dateStamp, TimeSpan timeSpan) {
		return isRoomReserved(termsCache.translate(roomId), dateStamp, timeSpan);
	}
	
	public boolean isRoomReserved(int roomIndex, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, byte[]> roomReservationRecord = roomReservation[roomIndex];
		if(roomReservationRecord==null) {
			roomReservationRecord = fixedReservationManager.roomReservation[roomIndex];
			if(roomReservationRecord==null)
				throw new IllegalArgumentException("Ne postoji zapis za trazenu dvoranu!");
		}
		byte[] occupancesForDate = roomReservationRecord.get(dateStamp);
		if(occupancesForDate==null) {
			occupancesForDate = fixedReservationManager.roomReservation[roomIndex].get(dateStamp);
			if(occupancesForDate==null)
				return false;
		}
		for(int i=getStartIdx(timeSpan);i<getEndIdx(timeSpan);i++) {
			if(occupancesForDate[i]!=0)
				return true;
		}
		return false;
	}
	
	public void clearRoomReservationForTimeSpan(String roomId, DateStamp dateStamp, TimeSpan timeSpan) {
		clearRoomReservationForTimeSpan(termsCache.translate(roomId), dateStamp, timeSpan);
	}
	
	public void clearRoomReservationForTimeSpan(int roomIndex, DateStamp dateStamp, TimeSpan timeSpan) {
		if(!isRoomReserved(roomIndex, dateStamp, timeSpan))
			return;
		else {
			Map<DateStamp, byte[]> roomReservationRecord = roomReservation[roomIndex];
			if(roomReservationRecord==null)
				return;
			byte[] occupancesForDate = roomReservationRecord.get(dateStamp);
			if(occupancesForDate==null)
				return;
			if(fixedReservationManager.roomReservation[roomIndex]!=null) {
				byte[] occupancesForDateFixed=fixedReservationManager.roomReservation[roomIndex].get(dateStamp);
				if(occupancesForDateFixed!=null) {
					int startIdx=getStartIdx(timeSpan);
					int endIdx=getEndIdx(timeSpan);
					for(int i=startIdx;i<endIdx;i++)
						occupancesForDate[i]=occupancesForDateFixed[i];
				}
				else {
					int startIdx=getStartIdx(timeSpan);
					int endIdx=getEndIdx(timeSpan);
					for(int i=startIdx; i<endIdx; i++)
						occupancesForDate[i]--;
				}
			}
			else {
				int startIdx=getStartIdx(timeSpan);
				int endIdx=getEndIdx(timeSpan);
				for(int i=startIdx; i<endIdx; i++)
					occupancesForDate[i]--;
			}
			roomReservationRecord.put(dateStamp, occupancesForDate);
		}
	}
	
	public void clearRoomReservationForDateStamp(String roomId, DateStamp dateStamp) {
		clearRoomReservationForDateStamp(termsCache.translate(roomId), dateStamp);
	}
	
	public void clearRoomReservationForDateStamp(int roomIndex, DateStamp dateStamp) {
		Map<DateStamp, byte[]> roomReservationRecord = roomReservation[roomIndex];
		if(roomReservationRecord==null)
			return;
		roomReservationRecord.remove(dateStamp);
	}
	
	public void clearRoomReservation(String roomId) {
		roomReservation[jmbagsCache.translate(roomId)] = null;
	}
	
	public void clearRoomReservation(int roomIndex) {
		roomReservation[roomIndex] = null;
	}
	
	public int countConflictsForStudent(int studentIndex) {
		int count = 0;
		Map<DateStamp, short[]> s1 = studentReservation[studentIndex];
		if(s1!=null) {
			for(DateStamp d : s1.keySet()) {
				short[] s2 = s1.get(d);
				for(short i:s2)
					if(i>0)
						count+=i-1;
			}
		}
		return count;
	}
	
	public int countConflictsForStudent(String jmbag) {
		return countConflictsForStudent(jmbagsCache.translate(jmbag));
	}
	
	public int countConflictsForStudents() {
		int count = 0;
		for(int i=0;i<studentReservation.length;i++) {
			count+=countConflictsForStudent(i);
		}
		return count;
	}
	
	public int countConflictsForRoom(String roomId) {
		return countConflictsForRoom(termsCache.translate(roomId));
	}
	
	public int countConflictsForRoom(int roomIndex) {
		int count = 0;
		Map<DateStamp, byte[]> s1 = roomReservation[roomIndex];
		if(s1!=null) {
			for(DateStamp d : s1.keySet()) {
				byte[] s2 = s1.get(d);
				for(int i:s2)
					if(i>0)
						count+=i-1;
			}
		}
		return count;
	}
	
	public int countConflictsForRooms() {
		int count = 0;
		for(int i=0; i<roomReservation.length; i++) {
			count+=countConflictsForRoom(i);
		}
		return count;
	}

//	public Map<String, Map<DateStamp, int[]>> getStudentReservation() {
//		return studentReservation;
//	}
//
//	public Map<String, Map<DateStamp, int[]>> getRoomReservation() {
//		return roomReservation;
//	}
}
