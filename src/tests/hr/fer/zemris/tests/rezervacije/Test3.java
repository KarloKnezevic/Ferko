package hr.fer.zemris.tests.rezervacije;

import hr.fer.zemris.jcms.service.reservations.IReservationManager;
import hr.fer.zemris.jcms.service.reservations.IReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.ReservationException;
import hr.fer.zemris.jcms.service.reservations.ReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.RoomReservationEntry;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Test3 {

	public static void main(String[] args) throws ReservationException {
		
		String[] roomsArray = new String[] {"A101", "A102", "A109", "A110", "A111", "A201", "A202", "A209", "A210", "A211", "A301", "A302", "B1", "B2", "B3", "B4", "B5",
				"D1", "D152", "D2", "D258", "D260", "D270", "D272", "D273", "D346", "PCLAB1", "PCLAB2", "PCLAB3"};
		String[][] rasponiDatuma = new String[][] {
				{"2009-09-07 00:00", "2009-09-15 23:59"},
				{"2009-09-16 00:00", "2009-09-30 23:59"},
				{"2009-10-01 00:00", "2009-10-15 23:59"},
				{"2009-10-16 00:00", "2009-10-31 23:59"},
				{"2009-11-01 00:00", "2009-11-15 23:59"},
				{"2009-11-16 00:00", "2009-11-30 23:59"},
				{"2009-12-01 00:00", "2009-12-15 23:59"},
				{"2009-12-16 00:00", "2009-12-31 23:59"},
				{"2010-01-01 00:00", "2010-01-15 23:59"},
				{"2010-01-16 00:00", "2010-01-31 23:59"},
		};
		IReservationManagerFactory manFact2 = ReservationManagerFactory.getFactory("FER2");
		IReservationManager man2 = manFact2.getInstance(Long.valueOf(1), "MČ005", "marcupic");
		
		IReservationManagerFactory manFact = ReservationManagerFactory.getFactory("FER");
		IReservationManager man = manFact.getInstance(Long.valueOf(1), "MČ005", "marcupic");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		for(String[] dates : rasponiDatuma) {
			System.out.println("Cekam za datume: "+dates[0]+" - "+dates[1]);
			try { Thread.sleep(5000); } catch(Exception ex) {}
			List<RoomReservationEntry> list = man.listRoomReservations(Arrays.asList(roomsArray), dates[0], dates[1]);
			System.out.println("   " + list.size()+" zapisa");
			for(RoomReservationEntry p : list) {
				//System.out.println(p);
				//man2.allocateRoom(p.getRoom(), sdf.format(new Date(p.getFromtime())), sdf.format(new Date(p.getTotime())), p.getReason());
				man2.allocateRoom(p.getRoom(), sdf.format(new Date(p.getFromtime())), sdf.format(new Date(p.getTotime())), "Nastavno zauzeće");
			}
		}
		man.close();
		man2.close();
	}

}
