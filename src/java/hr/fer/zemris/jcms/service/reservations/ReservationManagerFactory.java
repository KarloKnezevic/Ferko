package hr.fer.zemris.jcms.service.reservations;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ReservationManagerFactory {

	private static final Logger logger = Logger.getLogger(ReservationManagerFactory.class.getCanonicalName());
	private static Map<String,IReservationManagerFactory> factories;
	
	static {
		factories = new HashMap<String, IReservationManagerFactory>();
		InputStream is =  ReservationManagerFactory.class.getClassLoader().getResourceAsStream("reservation-managers.properties");
		if(is!=null) {
			Properties prop = new Properties();
			try {
				prop.load(new InputStreamReader(is,"UTF-8"));
			} catch (Exception e) {
				logger.error("Error reading reservation-managers.properties.");
				e.printStackTrace();
			}
			try { is.close(); } catch(Exception ignorable) {}
			for(Object key : prop.keySet()) {
				Object value = prop.get(key);
				if(key==null || value==null) {
					continue;
				}
				String venue = (String)key;
				String mngFctrClass = (String)value;
				try {
					Object mngFctr = ReservationManagerFactory.class.getClassLoader().loadClass(mngFctrClass).newInstance();
					factories.put(venue, (IReservationManagerFactory)mngFctr);
				} catch(Exception ex) {
					logger.error("Error creating IReservationManagerFactory "+mngFctrClass, ex);
				}
			}
			if(factories.isEmpty()) {
				logger.warn("No IReservationManagerFactory-s found. Reservation system is effectively disabled.");
			}
		} else {
			logger.warn("reservation-managers.properties not found.");
		}
	}
	
	public static IReservationManagerFactory getFactory(String venueShortName) {
		return factories.get(venueShortName);
	}
	
}
