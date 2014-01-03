package hr.fer.zemris.util.scheduling.support;

import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationManager implements Serializable, Cloneable {
	

	private static final long serialVersionUID = 8930711433353368120L;
	private ReservationManager fixedReservationManager;
	//      Map<JMBAG, Map<DateStamp, int[]>>
	private Map<String, Map<DateStamp, int[]>> studentReservation = new HashMap<String, Map<DateStamp, int[]>>();
	//      Map<RoomID, Map<DateStamp, int[]>>
	private Map<String, Map<DateStamp, int[]>> roomReservation = new HashMap<String, Map<DateStamp, int[]>>();
	
	public ReservationManager() {
		super();
	}
	public ReservationManager(Map<String, ISchedulingData> data) {
		for(String eventId:data.keySet()) {
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
		ReservationManager ret = new ReservationManager(fixedReservationManager);
		ret.setStudentReservation(studentReservation);
		ret.setRoomReservation(roomReservation);
		return ret;
	}
	
	private void setStudentReservation(Map<String, Map<DateStamp, int[]>> studentReservation) {
		this.studentReservation = new HashMap<String, Map<DateStamp,int[]>>();
		for(String s:studentReservation.keySet()) {
			this.studentReservation.put(s, new HashMap<DateStamp, int[]>());
			Map<DateStamp,int[]> s1 = studentReservation.get(s);
			Map<DateStamp,int[]> s2 = this.studentReservation.get(s);
			for(DateStamp d:s1.keySet()) {
				int[] t = s1.get(d);
				s2.put(d, Arrays.copyOf(t, t.length));
			}
		}
	}
	
	private void setRoomReservation(Map<String, Map<DateStamp, int[]>> roomReservation) {
		this.roomReservation = new HashMap<String, Map<DateStamp,int[]>>();
		for(String s:roomReservation.keySet()) {
			this.roomReservation.put(s, new HashMap<DateStamp, int[]>());
			Map<DateStamp,int[]> s1 = roomReservation.get(s);
			Map<DateStamp,int[]> s2 = this.roomReservation.get(s);
			for(DateStamp d:s1.keySet()) {
				int[] t = s1.get(d);
				s2.put(d, Arrays.copyOf(t, t.length));
			}
		}
	}
	
	public ReservationManager(ReservationManager fixedReservationManager) {
		this.fixedReservationManager=fixedReservationManager;
	}
	
	private void reserveStudentFixed(String JMBAG, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, int[]> s1 = studentReservation.get(JMBAG);
		if(s1==null) {
			s1=new HashMap<DateStamp, int[]>();
			studentReservation.put(JMBAG, s1);
		}
		int[] s2 = s1.get(dateStamp);
		if(s2==null) {
			s2=new int[24*4];
			s1.put(dateStamp, s2);
		}
		Arrays.fill(s2, getStartIdx(timeSpan), getEndIdx(timeSpan), 1);
	}
	
	public void reserveStudent(String JMBAG, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, int[]> s1 = studentReservation.get(JMBAG);
		if(s1==null) {
			s1=new HashMap<DateStamp, int[]>();
			studentReservation.put(JMBAG, s1);
		}
		int[] s2 = s1.get(dateStamp);
		if(s2==null) {
			int[] occupancesForDateFixed=fixedReservationManager.studentReservation.get(JMBAG).get(dateStamp);
			if(occupancesForDateFixed!=null)
				s2 = Arrays.copyOf(occupancesForDateFixed, occupancesForDateFixed.length);
			else
				s2=new int[24*4];
			s1.put(dateStamp, s2);
		}
		int startIdx=getStartIdx(timeSpan);
		int endIdx=getEndIdx(timeSpan);
		for(int i=startIdx;i<endIdx;i++) {
			s2[i]+=1;
		}
	}

	private int getStartIdx(TimeSpan timeSpan) {
		return (int)Math.floor(timeSpan.getStart().getAbsoluteTime()/15);
	}
	
	private int getEndIdx(TimeSpan timeSpan) {
		return (int)Math.ceil(timeSpan.getEnd().getAbsoluteTime()/15);
	}
	
	public boolean isStudentReserved(String JMBAG, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, int[]> studentReservationRecord = studentReservation.get(JMBAG);
		if(studentReservationRecord==null) {
			studentReservationRecord = fixedReservationManager.studentReservation.get(JMBAG);
			if(studentReservationRecord==null)
				throw new IllegalArgumentException("Ne postoji zapis za trazenog studenta!");
		}
		int[] occupancesForDate = studentReservationRecord.get(dateStamp);
		if(occupancesForDate==null) {
			occupancesForDate = fixedReservationManager.studentReservation.get(JMBAG).get(dateStamp);
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
		if(!isStudentReserved(JMBAG, dateStamp, timeSpan))
			return;
		else {
			Map<DateStamp, int[]> studentReservationRecord = studentReservation.get(JMBAG);
			if(studentReservationRecord==null)
				return;
			int[] occupancesForDate = studentReservationRecord.get(dateStamp);
			if(occupancesForDate==null)
				return;
			int[] occupancesForDateFixed=fixedReservationManager.studentReservation.get(JMBAG).get(dateStamp);
			if(occupancesForDateFixed!=null) {
				int startIdx=getStartIdx(timeSpan);
				int endIdx=getEndIdx(timeSpan);
				for(int i=startIdx;i<endIdx;i++)
					occupancesForDate[i]=occupancesForDateFixed[i];
			}
			else {
				Arrays.fill(occupancesForDate, getStartIdx(timeSpan), getEndIdx(timeSpan), 0);
			}
			studentReservationRecord.put(dateStamp, occupancesForDate);
		}
	}
	
	public void clearStudentReservationForDateStamp(String JMBAG, DateStamp dateStamp) {
		Map<DateStamp, int[]> studentReservationRecord = studentReservation.get(JMBAG);
		if(studentReservationRecord==null)
			return;
		studentReservationRecord.remove(dateStamp);
	}
	
	public void clearStudentReservation(String JMBAG) {
		studentReservation.remove(JMBAG);
	}
	

	public void reserveRoom(String roomId, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, int[]> s1 = roomReservation.get(roomId);
		if(s1==null) {
			s1=new HashMap<DateStamp, int[]>();
			roomReservation.put(roomId, s1);
		}
		int[] s2 = s1.get(dateStamp);
		if(s2==null) {
			int[] occupancesForDateFixed=fixedReservationManager.roomReservation.get(roomId).get(dateStamp);
			if(occupancesForDateFixed!=null)
				s2= Arrays.copyOf(occupancesForDateFixed, occupancesForDateFixed.length);
			else
				s2=new int[24*4];
			s1.put(dateStamp, s2);
			
		}
		int startIdx=getStartIdx(timeSpan);
		int endIdx=getEndIdx(timeSpan);
		for(int i=startIdx;i<endIdx;i++) {
			s2[i]+=1;
		}
		//System.out.println("Room " + roomId + " reserved on " + dateStamp.getStamp() + " for: " + timeSpan.toString());
	}


	private void reserveRoomFixed(String roomId, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, int[]> s1 = roomReservation.get(roomId);
		if(s1==null) {
			s1=new HashMap<DateStamp, int[]>();
			roomReservation.put(roomId, s1);
		}
		int[] s2 = s1.get(dateStamp);
		if(s2==null) {
			s2=new int[24*4];
			Arrays.fill(s2, 1);
			s1.put(dateStamp, s2);	
		}
		Arrays.fill(s2, getStartIdx(timeSpan), getEndIdx(timeSpan), 0);
	}
	
	public boolean isRoomReserved(String JMBAG, DateStamp dateStamp, TimeSpan timeSpan) {
		Map<DateStamp, int[]> roomReservationRecord = roomReservation.get(JMBAG);
		if(roomReservationRecord==null) {
			roomReservationRecord = fixedReservationManager.roomReservation.get(JMBAG);
			if(roomReservationRecord==null)
				throw new IllegalArgumentException("Ne postoji zapis za trazenu dvoranu!");
		}
		int[] occupancesForDate = roomReservationRecord.get(dateStamp);
		if(occupancesForDate==null) {
			occupancesForDate = fixedReservationManager.roomReservation.get(JMBAG).get(dateStamp);
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
		if(!isRoomReserved(roomId, dateStamp, timeSpan))
			return;
		else {
			Map<DateStamp, int[]> roomReservationRecord = roomReservation.get(roomId);
			if(roomReservationRecord==null)
				return;
			int[] occupancesForDate = roomReservationRecord.get(dateStamp);
			if(occupancesForDate==null)
				return;
			int[] occupancesForDateFixed=fixedReservationManager.roomReservation.get(roomId).get(dateStamp);
			if(occupancesForDateFixed!=null) {
				int startIdx=getStartIdx(timeSpan);
				int endIdx=getEndIdx(timeSpan);
				for(int i=startIdx;i<endIdx;i++)
					occupancesForDate[i]=occupancesForDateFixed[i];
			}
			else {
				Arrays.fill(occupancesForDate, getStartIdx(timeSpan), getEndIdx(timeSpan), 0);
			}
			roomReservationRecord.put(dateStamp, occupancesForDate);
		}
	}
	
	public void clearRoomReservationForDateStamp(String roomId, DateStamp dateStamp) {
		Map<DateStamp, int[]> roomReservationRecord = roomReservation.get(roomId);
		if(roomReservationRecord==null)
			return;
		roomReservationRecord.remove(dateStamp);
	}
	
	public void clearRoomReservation(String roomId) {
		roomReservation.remove(roomId);
	}
	
	public int countConflictsForStudent(String JMBAG) {
		int count = 0;
		Map<DateStamp, int[]> s1 = studentReservation.get(JMBAG);
		for(DateStamp d : s1.keySet()) {
			int[] s2 = s1.get(d);
			for(int i:s2)
				if(i>0)
					count+=i-1;
		}
		return count;
	}
	
	public int countConflictsForStudents() {
		int count = 0;
		for(String JMBAG:studentReservation.keySet()) {
			count+=countConflictsForStudent(JMBAG);
		}
		return count;
	}
	
	public int countConflictsForRoom(String roomId) {
		int count = 0;
		Map<DateStamp, int[]> s1 = roomReservation.get(roomId);
		for(DateStamp d : s1.keySet()) {
			int[] s2 = s1.get(d);
			for(int i:s2)
				if(i>0)
					count+=i-1;
		}
		return count;
	}
	
	public int countConflictsForRooms() {
		int count = 0;
		for(String roomId:roomReservation.keySet()) {
			count+=countConflictsForRoom(roomId);
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
