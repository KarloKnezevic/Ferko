package hr.fer.zemris.jcms.service.reservations.impl.ferweb;

import java.io.InputStream;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.service.reservations.IReservationManager;
import hr.fer.zemris.jcms.service.reservations.IReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.ReservationBackendException;
import hr.fer.zemris.jcms.service.reservations.ReservationException;
import hr.fer.zemris.jcms.service.reservations.RoomReservation;
import hr.fer.zemris.jcms.service.reservations.RoomReservationEntry;
import hr.fer.zemris.jcms.service.reservations.RoomReservationPeriod;
import hr.fer.zemris.jcms.service.reservations.RoomReservationStatus;
import hr.fer.zemris.jcms.service.reservations.RoomReservationTask;
import hr.fer.zemris.util.DateUtil;

public class FERWebReservationManagerFactory implements IReservationManagerFactory {

	private static final Logger logger = Logger.getLogger(FERWebReservationManagerFactory.class.getCanonicalName());
	private static final String XMLRPC_URL_AUTH = "https://www.fer.hr/xmlrpc/xr_auth.php";
	private static final String XMLRPC_URL_ROOMS = "https://www.fer.hr/xmlrpc/xr_dvorane.php";

	private String ferkoUsername;
	private String ferkoPassword;
	private Set<String> controlledRooms = new HashSet<String>();
	
	public FERWebReservationManagerFactory() {
		InputStream is =  FERWebReservationManagerFactory.class.getClassLoader().getResourceAsStream("reservations-ferweb.properties");
		if(is!=null) {
			Properties prop = new Properties();
			try {
				prop.load(new InputStreamReader(is,"UTF-8"));
			} catch (Exception e) {
				logger.error("Error reading reservations-ferweb.properties.");
				e.printStackTrace();
			}
			try { is.close(); } catch(Exception ignorable) {}
			ferkoUsername = prop.getProperty("username","invalid-user");
			ferkoPassword = prop.getProperty("password","invalid-password");
		} else {
			logger.warn("reservations-ferweb.properties not found.");
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
	
	@Override
	public IReservationManager getInstance(Long userID, String jmbag, String username) throws ReservationException {
		return new ReservationManager(userID, jmbag, username);
	}

	/**
	 * @author marcupic
	 *
	 */
	private class ReservationManager implements IReservationManager {

		private String jmbag;
		private Integer login_id;
		
		public ReservationManager(Long userID, String jmbag, String username) throws ReservationException {
			super();
			this.jmbag = jmbag;
			login_id = login(ferkoUsername, ferkoPassword);
		}

		@Override
		public Boolean isUnderControl(String roomShortName) {
			synchronized (controlledRooms) {
				return Boolean.valueOf(controlledRooms.contains(roomShortName));
			}
		}

		@Override
		public void close() throws ReservationException {
			if(login_id==null) return;
			try {
				logout(login_id);
			} finally {
				login_id = null;
			}
		}

		public Integer login(String username, String password) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					return loginInternal(username, password);
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}

		/**
		 * Prijava na FerWeb. Uvijek vraća broj koji je različit on <code>null</code>.
		 * 
		 * @param url url za rezervacije
		 * @param username korisničko ime
		 * @param password zaporka
		 * @return identifikator prijave korisnika
		 * @throws ReservationException ako se prijava ne moze obaviti
		 */
		public Integer loginInternal(String username, String password) throws ReservationException {
			XmlRpcClient client = null;
			
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_AUTH));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not login to FerWeb. Nested message:"+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			Object[] params = new Object[] {username,password};
			Integer login_id = null;
			try {
				login_id = (Integer)client.execute("auth.rlogin", params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not login to FerWeb. Nested message: "+e.getMessage(), e);
			} catch (XmlRpcException e) {
				e.printStackTrace();
				throw new ReservationException("Could not login to FerWeb. Nested message: "+e.getMessage(), e);
			}
			if(login_id==null) {
				throw new ReservationException("Could not login to FerWeb. FerWeb returned invalid user handle.");
			}
			return login_id;
		}

		public void logout(Integer login_id) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					logoutInternal(login_id);
					return;
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}

		public void logoutInternal(Integer login_id) throws ReservationException {
			XmlRpcClient client = null;
			
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_AUTH));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not logout from FerWeb. Nested message:"+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			Object[] params = new Object[] {login_id};
			try {
				client.execute("auth.rlogout",params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not logout from FerWeb. Nested message:"+e.getMessage(), e);
			} catch (XmlRpcException e) {
				throw new ReservationException("Could not logout from FerWeb. Nested message:"+e.getMessage(), e);
			}
			return;
		}

		@Override
		public boolean allocateRoom(String room, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					return allocateRoomInternal(room, dateTimeFrom, dateTimeTo, reason, context);
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}
		
		public boolean allocateRoom(String room, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			return allocateRoom(room, dateTimeFrom, dateTimeTo, reason, null);
		}
		
		public boolean allocateRoomInternal(String room, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException {
			XmlRpcClient client = null;
			Integer from;
			Integer to;
			try {
				from = unixTimeStamp(dateTimeFrom);
				to = unixTimeStamp(dateTimeTo);
			} catch (ParseException e2) {
				throw new ReservationException("Could not allocate room - wrong time format. Nested message: "+e2.getMessage(), e2);
			}

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_ROOMS));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not allocate room. Nested message: "+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			Object[] cookies = updateContextCookie(null, context);
			Object[] params = new Object[] {login_id, room, jmbag, from, to, reason, cookies};
			Boolean ok = Boolean.FALSE;
			try {
				ok = (Boolean)client.execute("dvorane.rezerviraj",params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not allocate room. Nested message: "+e.getMessage(), e);
			} catch (XmlRpcException e) {
				throw new ReservationException("Could not allocate room. Nested message: "+e.getMessage(), e);
			}
			return ok.booleanValue();
		}

		public boolean allocateRooms(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					return allocateRoomsInternal(rooms, dateTimeFrom, dateTimeTo, reason);
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}

		public boolean allocateRoomsInternal(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			boolean allSuccessfull = true;
			for(RoomReservationTask task : rooms) {
				try {
					boolean success = allocateRoom(task.getRoomShortName(), dateTimeFrom, dateTimeTo, reason);
					task.setSuccess(success);
					if(!success) {
						allSuccessfull = false;
						task.setMessage("Dvoranu nije moguće rezervirati.");
					}
				} catch(ReservationException ex) {
					task.setSuccess(false);
					task.setMessage(ex.getMessage());
				}
			}
			return allSuccessfull;
		}

		@Override
		public boolean updateReservationRoom(String room,
				String oldDateTimeFrom, String oldDateTimeTo,
				String newDateTimeFrom, String newDateTimeTo, String reason,
				String context, boolean justCheck) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					return updateReservationRoomInternal(room, oldDateTimeFrom, oldDateTimeTo, newDateTimeFrom, newDateTimeTo, reason, context, justCheck);
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public boolean updateReservationRoomInternal(String room,
				String oldDateTimeFrom, String oldDateTimeTo,
				String newDateTimeFrom, String newDateTimeTo, String reason,
				String context, boolean justCheck) throws ReservationException {
			XmlRpcClient client = null;
			Integer oldFrom;
			Integer oldTo;
			Integer newFrom;
			Integer newTo;
			try {
				oldFrom = unixTimeStamp(oldDateTimeFrom);
				oldTo = unixTimeStamp(oldDateTimeTo);
				newFrom = unixTimeStamp(newDateTimeFrom);
				newTo = unixTimeStamp(newDateTimeTo);
			} catch (ParseException e2) {
				throw new ReservationException("Could not update room - wrong time format. Nested message: "+e2.getMessage(), e2);
			}

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_ROOMS));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not check room. Nested message: "+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			Object[] params = new Object[] {login_id, oldFrom, oldTo, new String[] {room}};
			Object[] result = null;
			try {
				result = (Object[])client.execute("dvorane.rezervirano_interval",params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not update room. Nested message: "+e.getMessage(), e);
			} catch (XmlRpcException e) {
				throw new ReservationException("Could not update room. Nested message: "+e.getMessage(), e);
			}
			if(result==null || result.length==0) {
				return false; // te rezervacije uopce nema!!!
			}
			for(Object o : result) {
				Integer od = ((Map<String,Integer>)o).get("od");
				Integer d = ((Map<String,Integer>)o).get("do");
				// Ako nam ne smeta...
				if(od.intValue()!=oldFrom.intValue() || d.intValue()!=oldTo.intValue()) {
					continue;
				}
				// Ako je mi vec imamo...
				if(room.equals(((Map<String,String>)o).get("dvorana"))) {
					boolean nasa = false;
					if(reason.equals(((Map<String,String>)o).get("rez_zasto")) && jmbag.equals(((Map<String,String>)o).get("user_code"))) {
						nasa = true;
					} else if(context!=null) {
						if(contextCookiePresent((Object[])((Map<String,Object>)o).get("cookies"), context)) {
							nasa = true;
						}
					}
					if(nasa) {
						if(justCheck) return true;
						// Azuriraj trajanje!
						Map<String,Object> izvorniPodatci = new HashMap<String, Object>();
						izvorniPodatci.put("room", room);
						izvorniPodatci.put("time_from", oldFrom);
						izvorniPodatci.put("time_to", oldTo);
						Object[] newCookies = updateContextCookie((Object[])((Map<String,Object>)o).get("cookies"), context);
						Map<String,Object> noviPodatci = new HashMap<String, Object>();
						noviPodatci.put("room", room);
						noviPodatci.put("time_from", newFrom);
						noviPodatci.put("time_to", newTo);
						noviPodatci.put("description", reason);
						noviPodatci.put("cookies", newCookies);
						Object[] params2 = new Object[] {login_id, jmbag, izvorniPodatci, noviPodatci};
						Boolean res = null;
						try {
							res = (Boolean)client.execute("dvorane.promjena",params2);
						} catch(XmlRpcClientException e) {
							e.printStackTrace();
							throw new ReservationBackendException("Could not update room. Nested message: "+e.getMessage(), e);
						} catch (XmlRpcException e) {
							e.printStackTrace();
							throw new ReservationException("Could not update room. Nested message: "+e.getMessage(), e);
						}
						if(res==null) return false;
						return res.booleanValue();
					} else {
						// Nije nasa!!!
						return false;
					}
				}
			}
			// Nismo pronasli dvoranu!!!
			return false;
		}

		private Object[] updateContextCookie(Object[] cookies, String context) {
			if(context==null) {
				if(cookies!=null) return cookies;
				return new Object[0];
			}
			String toFind = "context="+context;
			if(cookies==null || cookies.length==0) return new Object[] {toFind};
			for(int i=0; i < cookies.length; i++) {
				Object o = cookies[i];
				if(!(o instanceof String)) continue;
				String s = (String)o;
				if(s.startsWith("context=")) {
					cookies[i] = toFind;
					return cookies;
				}
			}
			Object[] newCookies = new Object[cookies.length+1];
			System.arraycopy(cookies, 0, newCookies, 0, cookies.length);
			newCookies[newCookies.length-1] = toFind;
			return newCookies;
		}

		public boolean contextCookiePresent(Object[] cookies, String context) {
			if(context==null) return false;
			String toFind = "context="+context;
			if(cookies==null) return false;
			if(cookies.length==0) return false;
			for(int i=0; i < cookies.length; i++) {
				Object o = cookies[i];
				if(!(o instanceof String)) continue;
				String s = (String)o;
				if(s.equals(toFind)) return true;
			}
			return false;
		}

		public RoomReservation checkRoom(String room, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					return checkRoomInternal(room, dateTimeFrom, dateTimeTo, reason, context);
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}

		public RoomReservation checkRoom(String room, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			return checkRoom(room, dateTimeFrom, dateTimeTo, reason, null);
		}


		@SuppressWarnings("unchecked")
		public RoomReservation checkRoomInternal(String room, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException {
			XmlRpcClient client = null;
			Integer from;
			Integer to;
			try {
				from = unixTimeStamp(dateTimeFrom);
				to = unixTimeStamp(dateTimeTo);
			} catch (ParseException e2) {
				throw new ReservationException("Could not check room - wrong time format. Nested message: "+e2.getMessage(), e2);
			}

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_ROOMS));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not check room. Nested message: "+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			Object[] params = new Object[] {login_id, from, to, new String[] {room}};
			Object[] result = null;
			try {
				result = (Object[])client.execute("dvorane.rezervirano_interval",params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not check room. Nested message: "+e.getMessage(), e);
			} catch (XmlRpcException e) {
				throw new ReservationException("Could not check room. Nested message: "+e.getMessage(), e);
			}
			if(result==null || result.length==0) {
				return new RoomReservation(room, RoomReservationStatus.FREE); // slobodno zauzmi
			}
			boolean konflikt = false;
			for(Object o : result) {
				Integer od = ((Map<String,Integer>)o).get("od");
				Integer d = ((Map<String,Integer>)o).get("do");
				// Ako nam ne smeta...
				if(od.intValue()>=to.intValue() || d.intValue()<=from.intValue()) {
					continue;
				}
				// Ako je mi vec imamo...
				if(from.equals(od) && to.equals(d) && room.equals(((Map<String,String>)o).get("dvorana"))) {
					boolean nasa = false;
					if(reason.equals(((Map<String,String>)o).get("rez_zasto")) && jmbag.equals(((Map<String,String>)o).get("user_code"))) {
						nasa = true;
					} else if(context!=null) {
						if(contextCookiePresent((Object[])((Map<String,Object>)o).get("cookies"), context)) {
							nasa = true;
						}
					}
					if(nasa) {
						return new RoomReservation(room, RoomReservationStatus.RESERVED_FOR_US);
					} else {
						konflikt = true;
						break;
					}
				}
				konflikt = true;
				break;
			}
			// Ako nije konflikt:
			if(!konflikt) {
				// Inace je mozemo zauzeti...
				return new RoomReservation(room, RoomReservationStatus.FREE);
			}
			return new RoomReservation(room, RoomReservationStatus.RESERVED_FOR_OTHER); // konflikt!!!
		}

		public List<RoomReservationEntry> listRoomReservations(List<String> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					return listRoomReservationsInternal(rooms, dateTimeFrom, dateTimeTo);
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public List<RoomReservationEntry> listRoomReservationsInternal(List<String> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			XmlRpcClient client = null;
			Integer from;
			Integer to;
			try {
				from = unixTimeStamp(dateTimeFrom);
				to = unixTimeStamp(dateTimeTo);
			} catch (ParseException e2) {
				throw new ReservationException("Could not check room - wrong time format. Nested message: "+e2.getMessage(), e2);
			}

			List<String> checkList = new ArrayList<String>(rooms.size());
			for(int i = 0; i < rooms.size(); i++) {
				String roomName = rooms.get(i);
				if(isUnderControl(roomName)) {
					checkList.add(roomName);
				}
			}
			String[] roomsArray = new String[checkList.size()];
			for(int i = 0; i < checkList.size(); i++) {
				roomsArray[i] = checkList.get(i);
			}

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_ROOMS));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not check room. Nested message: "+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			Object[] params = new Object[] {login_id, from, to, roomsArray};
			Object[] result = null;
			try {
				result = (Object[])client.execute("dvorane.rezervirano_interval",params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not obtain reservations list. Nested message: "+e.getMessage(), e);
			} catch (XmlRpcException e) {
				throw new ReservationException("Could not obtain reservations list. Nested message: "+e.getMessage(), e);
			}
			if(result==null || result.length==0) {
				return new ArrayList<RoomReservationEntry>();
			}
			List<RoomReservationEntry> resultList = new ArrayList<RoomReservationEntry>(result.length);
			for(Object o : result) {
				Integer od = ((Map<String,Integer>)o).get("od");
				Integer d = ((Map<String,Integer>)o).get("do");
				// Ako nam ne smeta...
				if(od.intValue()>=to.intValue() || d.intValue()<=from.intValue()) {
					continue;
				}
				resultList.add(new RoomReservationEntry(
						((Map<String,String>)o).get("dvorana"),
						((Map<String,String>)o).get("user_code"),
						od.longValue()*1000L,
						d.longValue()*1000L,
						((Map<String,String>)o).get("rez_zasto")
				));
			}
			return resultList;
		}
		
		public void checkRoom(List<RoomReservation> rooms, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					checkRoomInternal(rooms, dateTimeFrom, dateTimeTo, reason, context);
					return;
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}

		public void checkRoom(List<RoomReservation> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException {
			checkRoom(rooms, dateTimeFrom, dateTimeTo, reason, null);
		}

		@SuppressWarnings("unchecked")
		public void checkRoomInternal(List<RoomReservation> rooms, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException {
			XmlRpcClient client = null;
			Integer from;
			Integer to;
			try {
				from = unixTimeStamp(dateTimeFrom);
				to = unixTimeStamp(dateTimeTo);
			} catch (ParseException e2) {
				throw new ReservationException("Could not check room - wrong time format. Nested message: "+e2.getMessage(), e2);
			}

			// System.out.println("from = "+from+", to = "+to);
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_ROOMS));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not check room. Nested message: "+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			List<RoomReservation> dupList = new ArrayList<RoomReservation>(rooms.size());
			for(int i = 0; i < rooms.size(); i++) {
				RoomReservation roomRes = rooms.get(i);
				if(!isUnderControl(roomRes.getRoomShortName())) {
					roomRes.setStatus(RoomReservationStatus.NOT_UNDER_CONTROL);
				} else {
					dupList.add(roomRes);
				}
			}
			String[] roomsArray = new String[dupList.size()];
			Map<String,RoomReservation> roomMap = new HashMap<String, RoomReservation>();
			for(int i = 0; i < dupList.size(); i++) {
				roomsArray[i] = dupList.get(i).getRoomShortName();
				roomMap.put(dupList.get(i).getRoomShortName(), dupList.get(i));
			}
			Object[] params = new Object[] {login_id, from, to, roomsArray};

			Object[] result = null;
			try {
				result = (Object[])client.execute("dvorane.rezervirano_interval",params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not check room. Nested message: "+e.getMessage(), e);
			} catch (XmlRpcException e) {
				throw new ReservationException("Could not check room. Nested message: "+e.getMessage(), e);
			}
			Map<String, List<Object>> results;
			results = new HashMap<String, List<Object>>();
			if(result!=null && result.length!=0) {
				for(Object o : result) {
					String dvorana = ((Map<String,String>)o).get("dvorana");
					List<Object> list = results.get(dvorana);
					if(list==null) {
						list = new ArrayList<Object>();
						results.put(dvorana, list);
					}
					list.add(o);
				}
			}
outer:		for(String roomShortName : roomsArray) {
				// System.out.println("Dvorana:" + roomShortName);
				List<Object> list = results.get(roomShortName);
				RoomReservation roomRes = roomMap.get(roomShortName);
				// Ako nemam sobu koju je vratio FerWeb...
				if(roomRes==null) continue;
				if(list==null || list.isEmpty()) {
					roomRes.setStatus(RoomReservationStatus.FREE);
					continue;
				}
				boolean konflikt = false;
				for(Object o : list) {
					// System.out.println("Gledam: "+o);
					Integer od = ((Map<String,Integer>)o).get("od");
					Integer d = ((Map<String,Integer>)o).get("do");
					// Ako nam ne smeta...
					if(od.intValue()>=to.intValue() || d.intValue()<=from.intValue()) {
						continue;
					}
					// Ako je mi vec imamo...
					if(from.equals(od) && to.equals(d) && roomRes.getRoomShortName().equals(((Map<String,String>)o).get("dvorana"))) {
						boolean nasa = false;
						if(jmbag.equals(((Map<String,String>)o).get("user_code")) && reason.equals(((Map<String,String>)o).get("rez_zasto"))) {
							nasa = true;
						} else if(context!=null) {
							if(contextCookiePresent((Object[])((Map<String,Object>)o).get("cookies"), context)) {
								nasa = true;
							}
						}
						if(nasa) {
							roomRes.setStatus(RoomReservationStatus.RESERVED_FOR_US);
							continue outer;
						} else {
							roomRes.setStatus(RoomReservationStatus.RESERVED_FOR_OTHER);
							continue outer;
						}
					}
					konflikt = true;
					break;
				}
				// Ako nije konflikt:
				if(!konflikt) {
					roomRes.setStatus(RoomReservationStatus.FREE);
				} else {
					roomRes.setStatus(RoomReservationStatus.RESERVED_FOR_OTHER);
				}
			}
		}
		
		public boolean deallocateRoom(String room, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					return deallocateRoomInternal(room, dateTimeFrom, dateTimeTo);
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}
		
		public boolean deallocateRoomInternal(String room, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			XmlRpcClient client = null;
			Integer from;
			Integer to;
			try {
				from = unixTimeStamp(dateTimeFrom);
				to = unixTimeStamp(dateTimeTo);
			} catch (ParseException e2) {
				throw new ReservationException("Could not deallocate room - wrong time format. Nested message: "+e2.getMessage(), e2);
			}

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_ROOMS));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not deallocate room. Nested message: "+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
			// Ups: je li ovo FERWeb promijenio API? Odrezerviraj vise ne trazi jmbag korisnika!
			// Object[] params = new Object[] {login_id, room, jmbag, from, to};
			Object[] params = new Object[] {login_id, room, from, to};
			Boolean ok = Boolean.FALSE;
			try {
				ok = (Boolean)client.execute("dvorane.odrezerviraj",params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not deallocate room. Nested message: "+e.getMessage(), e);
			} catch (XmlRpcException e) {
				throw new ReservationException("Could not deallocate room. Nested message: "+e.getMessage(), e);
			}
			return ok.booleanValue();
		}
		
		public boolean deallocateRooms(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			boolean allSuccessfull = true;
			for(RoomReservationTask task : rooms) {
				try {
					boolean success = deallocateRoom(task.getRoomShortName(), dateTimeFrom, dateTimeTo);
					task.setSuccess(success);
					if(!success) {
						allSuccessfull = false;
						task.setMessage("Dvoranu nije moguće odrezervirati.");
					}
				} catch(ReservationException ex) {
					task.setSuccess(false);
					task.setMessage(ex.getMessage());
				}
			}
			return allSuccessfull;
		}

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		private Integer unixTimeStamp(String time) throws ParseException {
			Date d = sdf.parse(time);
			int stamp = (int)(d.getTime()/1000);
			return new Integer(stamp);
		}

		public List<RoomReservationPeriod> findAvailablePeriodsForRooms(List<String> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			int i = 0;
			while(true) {
				i++;
				try {
					return findAvailablePeriodsForRoomsInternal(rooms, dateTimeFrom, dateTimeTo);
				} catch(ReservationBackendException ex) {
					if(i<3) {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+".");
						try { Thread.sleep(1000*(2*i)); } catch(Exception ignorable) {}
						continue;
					} else {
						logger.warn("Exception while communicating with FERWeb reservations for attempt "+i+" Giving up.");
						throw ex;
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public List<RoomReservationPeriod> findAvailablePeriodsForRoomsInternal(List<String> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException {
			XmlRpcClient client = null;
			if(!DateUtil.checkSemiFullDateFormat(dateTimeFrom)) {
				throw new ReservationException("Could not find available periods for rooms - wrong start time format.");
			}
			if(!DateUtil.checkSemiFullDateFormat(dateTimeTo)) {
				throw new ReservationException("Could not find available periods for rooms - wrong end time format.");
			}
			Integer from;
			Integer to;
			try {
				from = unixTimeStamp(dateTimeFrom);
				to = unixTimeStamp(dateTimeTo);
			} catch (ParseException e2) {
				throw new ReservationException("Could not find available periods for rooms - wrong time format. Nested message: "+e2.getMessage(), e2);
			}
			if(from>to) {
				throw new ReservationException("Could not find available periods for rooms - start time ("+dateTimeFrom+") is after end time ("+dateTimeTo+").");
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
			
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			try {
				config.setServerURL(new URL(XMLRPC_URL_ROOMS));
				config.setConnectionTimeout(60*1000);
				config.setReplyTimeout(60*1000);
			} catch (MalformedURLException e1) {
				throw new ReservationException("Could not check room. Nested message: "+e1.getMessage(), e1);
			}
			client = new XmlRpcClient();
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			client.setConfig(config);
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
			Object[] params = new Object[] {login_id, from, to, roomsArray};

			Object[] result = null;
			try {
				result = (Object[])client.execute("dvorane.rezervirano_interval",params);
			} catch(XmlRpcClientException e) {
				e.printStackTrace();
				throw new ReservationBackendException("Could not find available periods for rooms. Nested message: "+e.getMessage(), e);
			} catch (XmlRpcException e) {
				throw new ReservationException("Could not find available periods for rooms. Nested message: "+e.getMessage(), e);
			}
			Map<String, List<Object>> results;
			results = new HashMap<String, List<Object>>();
			if(result!=null && result.length!=0) {
				for(Object o : result) {
					String dvorana = ((Map<String,String>)o).get("dvorana");
					List<Object> list = results.get(dvorana);
					if(list==null) {
						list = new ArrayList<Object>();
						results.put(dvorana, list);
					}
					list.add(o);
				}
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
				List<Object> list = results.get(roomShortName);
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
				for(Object o : list) {
					Integer odTrenutka = ((Map<String,Integer>)o).get("od");
					Integer doTrenutka = ((Map<String,Integer>)o).get("do");
					if(odTrenutka.intValue()>doTrenutka.intValue()) {
						Integer tmp = odTrenutka;
						odTrenutka = doTrenutka;
						doTrenutka = tmp;
					}
					Date pocetak = new Date(odTrenutka.intValue()*1000L);
					Date kraj = new Date(doTrenutka.intValue()*1000L);
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
			return findAvailablePeriodsForRooms(Arrays.asList(new String[] {room}), dateTimeFrom, dateTimeTo);
		}

	}

}
