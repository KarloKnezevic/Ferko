package hr.fer.zemris.jcms.service.reservations.impl.dummy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import hr.fer.zemris.jcms.service.reservations.IReservationManager;
import hr.fer.zemris.jcms.service.reservations.IReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.ReservationException;
import hr.fer.zemris.jcms.service.reservations.RoomReservation;
import hr.fer.zemris.jcms.service.reservations.RoomReservationEntry;
import hr.fer.zemris.jcms.service.reservations.RoomReservationPeriod;
import hr.fer.zemris.jcms.service.reservations.RoomReservationStatus;
import hr.fer.zemris.jcms.service.reservations.RoomReservationTask;

/**
 * Lažni upravitelj rezervacija
 * @author Ivan
 *
 */
public class DummyReservationManagerFactory implements IReservationManagerFactory {

	private Set<String> controlledRooms = new HashSet<String>();
	
	public DummyReservationManagerFactory() {

	}
	
	@Override
	public IReservationManager getInstance(Long userID, String jmbag, String username) throws ReservationException {
		return new ReservationManager(userID, jmbag, username);
	}

	private class ReservationManager implements IReservationManager {

		public ReservationManager(Long userID, String jmbag, String username) throws ReservationException {
			super();
		}

		@Override
		public boolean updateReservationRoom(String room,
				String oldDateTimeFrom, String oldDateTimeTo,
				String newDateTimeFrom, String newDateTimeTo, String reason,
				String context, boolean justCheck) throws ReservationException {
			return true;
		}
		
		@Override
		public List<RoomReservationEntry> listRoomReservations(
				List<String> rooms, String dateTimeFrom, String dateTimeTo)
				throws ReservationException {
			return new ArrayList<RoomReservationEntry>();
		}
		
		@Override
		public Boolean isUnderControl(String roomShortName) {
			synchronized (controlledRooms) {
				return Boolean.valueOf(controlledRooms.contains(roomShortName));
			}
		}

		@Override
		public void close() throws ReservationException {
		}

		@Override
		public boolean allocateRoom(String room, String dateTimeFrom,
				String dateTimeTo, String reason, String context)
				throws ReservationException {
			return false;
		}
		public boolean allocateRoom(String room, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			return true;
		}

		public boolean allocateRooms(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			return true;
		}

		public RoomReservation checkRoom(String room, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			return new RoomReservation(room, RoomReservationStatus.FREE); 
		}

		@Override
		public RoomReservation checkRoom(String room, String dateTimeFrom,
				String dateTimeTo, String reason, String context)
				throws ReservationException {
			return new RoomReservation(room, RoomReservationStatus.FREE); 
		}
		
		public void checkRoom(List<RoomReservation> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			for(RoomReservation rr : rooms) rr.setStatus(RoomReservationStatus.FREE);
		}
		
		@Override
		public void checkRoom(List<RoomReservation> rooms, String dateTimeFrom,
				String dateTimeTo, String reason, String context)
				throws ReservationException {
			for(RoomReservation rr : rooms) rr.setStatus(RoomReservationStatus.FREE);
		}
		
		public boolean deallocateRoom(String room, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			return true;
		}
		
		public boolean deallocateRooms(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			return true;
		}


		@Override
		public List<RoomReservationPeriod> findAvailablePeriodsForRooms(List<String> rooms, String dateTimeFrom, String dateTimeTo)	throws ReservationException {
			List<RoomReservationPeriod> results = new ArrayList<RoomReservationPeriod>();
			for(String roomShortName : rooms){
				results.addAll(findAvailableRoomPeriods(roomShortName, dateTimeFrom, dateTimeTo));
			}
			return null;
		}

		@Override
		public List<RoomReservationPeriod> findAvailableRoomPeriods(String room, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			List<RoomReservationPeriod> results = new ArrayList<RoomReservationPeriod>();
			try {
				Calendar c1 = Calendar.getInstance();
				c1.setTime(sdf.parse(dateTimeFrom));
				Calendar c2 = Calendar.getInstance();
				c2.setTime(sdf.parse(dateTimeTo));
				
				while(c1.before(c2)){
					String date = sdfDateOnly.format(c1.getTime());
					String start = sdfTimeOnly.format(c1.getTime()); 
					c1.add(Calendar.HOUR, +2);
					String end = sdfTimeOnly.format(c1.getTime());
					results.add(new RoomReservationPeriod(room, RoomReservationStatus.FREE, date, start, end));
					c1.add(Calendar.HOUR, +1);
				}
			
			} catch (ParseException ignored) {}
			return results;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfTimeOnly = new SimpleDateFormat("HH:mm");
		
	}
	
	

}
