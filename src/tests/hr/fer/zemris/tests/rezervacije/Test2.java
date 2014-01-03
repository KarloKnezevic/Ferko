package hr.fer.zemris.tests.rezervacije;

import java.util.List;

import hr.fer.zemris.jcms.service.reservations.IReservationManager;
import hr.fer.zemris.jcms.service.reservations.IReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.ReservationException;
import hr.fer.zemris.jcms.service.reservations.ReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.RoomReservationPeriod;

public class Test2 {

	// new String[] {"A101", "A102", "A109", "A110", "A111", "A201", "A202", "A209", "A210", "A211", "A301", "A302", "B1", "B2", "B3", "B4", "B5",
	// "D1", "D152", "D2", "D258", "D260", "D270", "D272", "D273", "D346", "PCLAB1", "PCLAB2", "PCLAB3"};
	public static void main(String[] args) throws ReservationException {
		IReservationManagerFactory manFact = ReservationManagerFactory.getFactory("FER");
		IReservationManager man = manFact.getInstance(Long.valueOf(1), "MČ005", "marcupic");
		List<RoomReservationPeriod> list = man.findAvailableRoomPeriods("A101", "2009-11-09 08:00", "2009-11-13 20:00");
		man.close();
		for(RoomReservationPeriod p : list) {
			System.out.println(p.getFullTimeSpanString());
		}
	}
	
}
