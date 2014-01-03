package hr.fer.zemris.tests.rezervacije;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.service.reservations.IReservationManager;
import hr.fer.zemris.jcms.service.reservations.IReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.ReservationException;
import hr.fer.zemris.jcms.service.reservations.ReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.RoomReservation;

public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String pocetak = "2008-11-20 09:00:00";
		//String kraj = "2008-11-20 10:00:00";
		String pocetak = "2009-01-03 08:00:00";
		String kraj = "2009-01-03 10:00:00";
		String[] dvorane = new String[] {"PCLAB1","PCLAB2","PCLAB3"};
		
		IReservationManagerFactory factory = ReservationManagerFactory.getFactory("FER");
		IReservationManager manager = null;
		try {
			manager = factory.getInstance(1L, "MČ005", "marcupic");
			List<RoomReservation> rooms = new ArrayList<RoomReservation>(dvorane.length);
			for(int i = 0; i < dvorane.length; i++) {
				rooms.add(new RoomReservation(dvorane[i]));
			}
			System.out.println("Prije provjere: ");
			System.out.println(rooms);
			System.out.println("--------------------------");
			//manager.checkRoom(rooms, pocetak, kraj, "Pokusne rezervacije");
			//manager.allocateRoom("PCLAB1", pocetak, kraj, "Pokusne rezervacije");
			manager.deallocateRoom("PCLAB1", pocetak, kraj);
			System.out.println("Nakon provjere: ");
			System.out.println(rooms);
		} catch (ReservationException e) {
			e.printStackTrace();
		} finally {
			try { if(manager!=null) manager.close(); } catch(ReservationException ignorable) {}
		}
		
	}

}
