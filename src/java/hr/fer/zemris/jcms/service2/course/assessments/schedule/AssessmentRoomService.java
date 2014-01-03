package hr.fer.zemris.jcms.service2.course.assessments.schedule;

import hr.fer.zemris.jcms.beans.AssessmentRoomBean;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.Venue;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomStatus;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomTag;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.reservations.IReservationManager;
import hr.fer.zemris.jcms.service.reservations.IReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.ReservationException;
import hr.fer.zemris.jcms.service.reservations.ReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.RoomReservation;
import hr.fer.zemris.jcms.service.reservations.RoomReservationStatus;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentsReserveRoomsData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentRoomScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

public class AssessmentRoomService {
	
	/**
	 * Metoda priprema podatke za inicijalni prikaz na kojem se odabire semestar, provjera i unosi raspored
	 * po dvoranama.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void prepareAssessmentsReserveRooms(EntityManager em, AdminAssessmentsReserveRoomsData data) {
		
		boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(BasicServiceSupport.getCurrentSemesterID(em));
		
		List<AssessmentTag> tagList = DAOHelperFactory.getDAOHelper().getAssessmentTagDAO().list(em);
		if (tagList == null || tagList.size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.TagsNotFound"));
			data.setAllAssessmentTags(new ArrayList<AssessmentTag>());
		} else {
			data.setAllAssessmentTags(tagList);
		}
		data.setResult(AbstractActionData.RESULT_INPUT);
		return;
	}

	/**
	 * Metoda temeljem odabranog semestra, vrste provjere i rasporeda
	 * po dvoranama preslikava to na kolegije i obavlja rezervacije
	 * preko ReservationManagera.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void importAssessmentsReserveRooms(EntityManager em, AdminAssessmentsReserveRoomsData data) {
		
		boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(BasicServiceSupport.getCurrentSemesterID(em));
		List<AssessmentTag> tagList = DAOHelperFactory.getDAOHelper().getAssessmentTagDAO().list(em);
		if (tagList == null || tagList.size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.TagsNotFound"));
			data.setAllAssessmentTags(new ArrayList<AssessmentTag>());
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		data.setAllAssessmentTags(tagList);

		if(StringUtil.isStringBlank(data.getSemester())) {
			data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		if(StringUtil.isStringBlank(data.getText())) {
			data.getMessageLogger().addErrorMessage("Niste zadali niti jedan ispit.");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, data.getSemester());
		if(ys==null) {
			data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		//provjera postoji li zadani assessmentTag u bazi
		if (StringUtil.isStringBlank(data.getAssessmentTag())) {
			data.getMessageLogger().addErrorMessage("Niste odabrali vrstu provjere!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		AssessmentTag dbTag = DAOHelperFactory.getDAOHelper().getAssessmentTagDAO().getByShortName(em, data.getAssessmentTag());
		if (dbTag == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		Map<String,Assessment> assessmentsByCourseMap = new HashMap<String, Assessment>();
		Map<String,Set<RoomWrapper>> courseRoomsMap = new LinkedHashMap<String, Set<RoomWrapper>>(); // Da ocuvamo redosljed dodavanja

		List<Assessment> allAssessments = DAOHelperFactory.getDAOHelper().getAssessmentDAO().findTaggedOnSemester(em, ys, dbTag);
		for(Assessment a : allAssessments) {
			assessmentsByCourseMap.put(a.getCourseInstance().getCourse().getIsvuCode(), a);
		}
		
		try {
			Map<String,Map<String,Room>> roomsByVenuesMap = new HashMap<String, Map<String,Room>>();
			List<String> lines = TextService.readerToStringList(new StringReader(data.getText()));
			for(String line : lines) {
				String[] elems = StringUtil.split(line, '\t');
				if(elems.length<4) {
					data.getMessageLogger().addErrorMessage("Pronađen redak pogrešnog formata (možda ispit bez dvorane?).");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
				String courseIsvuCode = elems[2];
				if(courseRoomsMap.containsKey(courseIsvuCode)) {
					data.getMessageLogger().addErrorMessage("Pronađen duplikat kolegija: "+courseIsvuCode+".");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
				if(!assessmentsByCourseMap.containsKey(courseIsvuCode)) {
					data.getMessageLogger().addErrorMessage("Kolegij "+courseIsvuCode+" nema provjere označene tagom "+dbTag.getShortName()+".");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
				
				Set<RoomWrapper> rooms = new LinkedHashSet<RoomWrapper>(); // Da ocuvamo redosljed...
				courseRoomsMap.put(courseIsvuCode, rooms);
				for(int i = 3; i < elems.length; i++) {
					String[] el2 = elems[i].split("#");
					if(el2.length!=3) {
						data.getMessageLogger().addErrorMessage("Format dvorane je pogrešan!");
						data.setResult(AbstractActionData.RESULT_INPUT);
						return;
					}
					String venue = el2[0];
					String roomName = el2[1];
					Integer capacity = Integer.parseInt(el2[2]);
					Map<String,Room> roomsMap = roomsByVenuesMap.get(venue);
					if(roomsMap==null) {
						roomsMap = fillRoomsMap(em, venue);
						roomsByVenuesMap.put(venue, roomsMap);
					}
					Room r = roomsMap.get(roomName);
					if(r==null) {
						data.getMessageLogger().addErrorMessage("Pronađena nepostojeća dvorana: "+venue+"#"+roomName+".");
						data.setResult(AbstractActionData.RESULT_INPUT);
						return;
					}
					RoomWrapper rw = new RoomWrapper(r, capacity);
					if(!rooms.add(rw)) {
						data.getMessageLogger().addErrorMessage("Pronađen duplikat dvorane: "+venue+"#"+roomName+" kod "+courseIsvuCode+".");
						data.setResult(AbstractActionData.RESULT_INPUT);
						return;
					}
				}
			}
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage("Dogodila se je pogreška. Detalji su zapisani u logu.");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		List<List<AssessmentRoom>> toReserve = new LinkedList<List<AssessmentRoom>>();
		
		// Sada mogu krenuti u postupak rezervacija
		for(Map.Entry<String, Set<RoomWrapper>> entry : courseRoomsMap.entrySet()) {
			String isvuCode = entry.getKey();
			Set<RoomWrapper> rooms = entry.getValue();
			Assessment a = assessmentsByCourseMap.get(isvuCode);
			boolean shouldReserve = false;
			if(a.getRooms()==null || a.getRooms().isEmpty()) {
				shouldReserve = true;
				AssessmentScheduleService.synchronizeRooms(DAOHelperFactory.getDAOHelper(), em, a, "FER");
			} else {
				boolean anyTaken = false;
				for(AssessmentRoom ar : a.getRooms()) {
					if(ar.isTaken()) {
						anyTaken = true;
						break;
					}
				}
				if(!anyTaken) {
					shouldReserve = true;
				}
			}
			if(!shouldReserve) {
				data.getMessageLogger().addInfoMessage("Preskačem kolegij "+a.getCourseInstance().getCourse().getIsvuCode()+" - "+a.getCourseInstance().getCourse().getName()+" jer je detektiran napravljeni raspored.");
				continue;
			}
			// Idemo mapirati sve sobe koje postoje na kolegiju...
			Map<Room, AssessmentRoom> arMap = new HashMap<Room, AssessmentRoom>();
			for(AssessmentRoom ar : a.getRooms()) {
				arMap.put(ar.getRoom(), ar);
			}
			List<AssessmentRoom> arList = new ArrayList<AssessmentRoom>();
			// Idemo sada po nama dodijeljenim sobama...
			for(RoomWrapper rw : rooms) {
				AssessmentRoom ar = arMap.get(rw.room);
				if(ar==null) {
					ar = AssessmentScheduleService.createAssessmentRoomEx(em, a, rw.room);
					arMap.put(rw.room, ar);
				}
				ar.setTaken(true);
				ar.setCapacity(rw.capacity);
				arList.add(ar);
			}
			
			toReserve.add(arList);
		}

		em.flush();
		
		// I sada idemo u rezervacije...
		for(List<AssessmentRoom> arList : toReserve) {
			if(arList.isEmpty()) continue;
			syncReservationsEx(em, data, arList.get(0).getAssessment());
		}
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return;
	}

	/**
	 * Pomoćni razred koji čuva vezu soba-broj_studenata iz parsiranog formata.
	 */
	private static class RoomWrapper {
		Room room;
		int capacity;
		
		public RoomWrapper(Room room, int capacity) {
			super();
			this.room = room;
			this.capacity = capacity;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((room == null) ? 0 : room.hashCode());
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
			RoomWrapper other = (RoomWrapper) obj;
			if (room == null) {
				if (other.room != null)
					return false;
			} else if (!room.equals(other.room))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return room.getId()+" ("+capacity+")";
		}
	}
	
	private static Map<String,Room> fillRoomsMap(EntityManager em, String venue) {
		List<Room> list = DAOHelperFactory.getDAOHelper().getRoomDAO().listByVenue(em, venue);
		Map<String, Room> map = new HashMap<String, Room>(list.size()*2);
		for(Room r : list) {
			map.put(r.getShortName(), r);
		}
		return map;
	}

	/**
	 * Metoda koja se bavi listanjem i updateom soba
	 * @param data
	 * @param assessmentID
	 * @param venueShortName
	 * @param beanList
	 * @param method
	 */
	public static void getRoomsForAssessment(EntityManager em, AssessmentRoomScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Assessment a = data.getAssessment();
		
		if(StringUtil.isStringBlank(data.getRoomVenue())) {
			data.setRoomVenue("FER");
		}
		
		//sinkroniziramo sobe 
		if (a.getRooms()==null || a.getRooms().size()==0)
			AssessmentScheduleService.synchronizeRooms(dh,em,a,"FER");

		//napunimo beanListu s vrijednostima i pobrojimo zauzeta mjesta
		for (AssessmentRoom ar : a.getRooms()) {
			data.getRoomList().add(fillAssessmentBean(ar));
		}
		
		//na kraju sortiramo beanListu po parametru
		if ("desc".equals(data.getType()))
			sort(data.getRoomList(),data.getSort(),-1);
		else
			sort(data.getRoomList(),data.getSort(),1);
		
		//postavimo podatke o userima i kapacitetu
		setCapacityAndUsers(dh,em,data, a);
		
		// inace dodajemo poruke
		if (data.getUserNumber() == 0)
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.noStudents"));
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void addRoomToList(EntityManager em, AssessmentRoomScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		if(StringUtil.isStringBlank(data.getRoomVenue())) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.noVenueSpecified"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if(StringUtil.isStringBlank(data.getRoomName())) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.noRoomSpecified"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Venue venue = dh.getVenueDAO().get(em, data.getRoomVenue());
		if(venue==null) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.venueDoesNotExists"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}

		Room room = dh.getRoomDAO().get(em, venue.getShortName(), data.getRoomName());
		if(room == null) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.roomDoesNotExists"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		Assessment a = data.getAssessment();
		boolean present = false;
		for(AssessmentRoom ar : a.getRooms()) {
			if(ar.getRoom().getVenue().getShortName().equals(venue.getShortName()) && ar.getRoom().getShortName().equals(room.getShortName())) {
				present = true;
			}
		}

		if(present) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.roomAlreadyPresent"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		AssessmentRoom ar = new AssessmentRoom();
		ar.setAssessment(a);
		ar.setAvailable(true);
		ar.setCapacity(room.getAssessmentPlaces());
		ar.setRequiredAssistants(room.getAssessmentAssistants());
		ar.setReserved(false);
		ar.setRoom(room);
		ar.setRoomStatus(AssessmentRoomStatus.UNCHECKED);
		ar.setTaken(false);
		ar.setRoomTag(AssessmentRoomTag.MANDATORY);
		em.persist(ar);
		a.getRooms().add(ar);
		
		data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Info.roomAdded"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return;
	}
	

	public static void updateRoomsForAssessment(EntityManager em, AssessmentRoomScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		if(StringUtil.isStringBlank(data.getRoomVenue())) {
			data.setRoomVenue("FER");
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Assessment a = data.getAssessment();
				
		//koristit cemo rooms umjesto a.getRooms()
		Collection<AssessmentRoom> rooms = a.getRooms();
		
		if (data.getRoomList() == null || data.getRoomList().size() == 0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if (rooms == null || rooms.size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRooms"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		// stavi assesmentRoomove u mapu tako da ih mozemo lagano dohvatiti
		Map<Long, AssessmentRoom> roomMap = new HashMap<Long, AssessmentRoom>(rooms.size());
		for (AssessmentRoom ar : rooms) {
			roomMap.put(ar.getId(), ar);
		}
		
		boolean hasErrors = false;
		
		//provjera da li su beanovi dobro ispunjeni
		for (AssessmentRoomBean bean : data.getRoomList()) {
			
			//provjera postoji li assessmentRoom sa zadanim id.em
			AssessmentRoom ar = null;
			try { 
				ar = roomMap.get(Long.valueOf(bean.getId())); 
			}
			catch (Exception ignorable) { }
			
			if (ar == null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			//popunimo u beanu one informacije koje nistu stigle zahtjevom a trebaju nam
			bean.setName(ar.getRoom().getName());
			bean.setAvailable(ar.isAvailable());
			//provjerimo podatke u beanu
			if (!isBeanDataValid(data.getMessageLogger(),bean))
				hasErrors = true;
		}
		
		//postavimo podatke o userima i kapacitetu
		setCapacityAndUsers(dh, em, data, a);
		
		//ako ima errora vracamo se na input
		if (hasErrors) {
			
			//dodajemo warning message
			if (data.getUserNumber() == 0)
				data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.noStudents"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		//TODO: ako vec postoji raspored pitamo korisnika zeli li unistiti raspored
		boolean arranged = false;
		if (a.getGroup()!=null && data.getUserNumber() > a.getGroup().getUsers().size())
			arranged = true;
		
		boolean doit = false;
		try {
			doit = Boolean.valueOf(data.getDoit());
		} catch (Exception ignorable) {}
		
		if (arranged && !doit) {
			data.setResult(AbstractActionData.RESULT_CONFIRM);
			return;
		}
		if (arranged)
			AssessmentScheduleService.clearAssessmentSchedule(dh, em, a);
		
		//inace radimo update
		for (AssessmentRoomBean bean : data.getRoomList()) {
			AssessmentRoom ar = roomMap.get(Long.valueOf(bean.getId()));
			updateAssessmentRoom(ar, bean);
		}

		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	
	
	public static void autoChooseRooms(EntityManager em, AssessmentRoomScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Assessment a = data.getAssessment();
		
		//koristit cemo rooms umjesto a.getRooms()
		Collection<AssessmentRoom> rooms = a.getRooms();
		
		if (rooms == null || rooms.size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRooms"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		//postavljamo podatke
		setCapacityAndUsers(dh,em,data, a);
		
		if (data.getUserNumber() ==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noStudents"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		//sortiramo po prioritetima pa po kapacitetu dvorana
		List<AssessmentRoom> roomList = new ArrayList<AssessmentRoom>(rooms);
		Collections.sort(roomList, new Comparator<AssessmentRoom>() {
			@Override
			public int compare(AssessmentRoom o1, AssessmentRoom o2) {

				int r = o2.getRoomTag().compareTo(o1.getRoomTag());
				if (r == 0)
					return o2.getCapacity() - o1.getCapacity();
				return r;
			}
		});
		
		//idemo vidjet da li ima dovoljno mjesta za trenutnu konfiguraciju soba
		int capacity = 0;
		for (AssessmentRoom ar : roomList) {
			if (data.getUserNumber()<=capacity)
				break;
			if (ar.getCapacity()>0 && ar.isAvailable() && ar.getRoomTag()!=AssessmentRoomTag.FORBIDDEN) {
				capacity += ar.getCapacity();
			}
		}
		
		if (capacity<data.getUserNumber()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.notEnoughCapacity"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		//provjera postoji li raspored prije nego napravimo autochoose
		boolean arranged = false;
		if (data.getUserNumber() != a.getGroup().getUsers().size()) 
			arranged = true;
		
		boolean doit = false;
		try {
			doit = Boolean.valueOf(data.getDoit());
		} catch (Exception ignorable) {}
		
		if (arranged && !doit) {
			data.setResult(AbstractActionData.RESULT_CONFIRM);
			return;
		}
		else if (arranged)
			AssessmentScheduleService.clearAssessmentSchedule(dh, em, a);
		
		//sad idemo to i uzeti
		capacity = 0;
		for (AssessmentRoom ar : roomList) {
			if (ar.getCapacity()>0 && ar.isAvailable() && ar.getRoomTag()!=AssessmentRoomTag.FORBIDDEN 
					& data.getUserNumber() > capacity) {
				ar.setTaken(true);
				capacity += ar.getCapacity();
			}
			else if (ar.isTaken()) { 
					ar.setTaken(false);
			}
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.autoChooseSuccessful"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void getAvailableStatus(EntityManager em, AssessmentRoomScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		Assessment a = data.getAssessment();
				
		if(a.getEvent()==null) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDateSet"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if(a.getEvent().getDuration()<1) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDurationSet"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}

		// Mapiraj sve sobe po Venue-ima, jer po njima idu IReservationManager-i.
		Set<AssessmentRoom> rooms = data.getAssessment().getRooms();
		Map<String,List<AssessmentRoom>> roomsByVenues = new HashMap<String, List<AssessmentRoom>>();
		for(AssessmentRoom room : rooms) {
			String venue = room.getRoom().getVenue().getShortName();  
			List<AssessmentRoom> list = roomsByVenues.get(venue);
			if(list==null) {
				list = new ArrayList<AssessmentRoom>();
				roomsByVenues.put(venue, list);
			}
			list.add(room);
		}
		
		// Definiraj datum pocetka, kraja i razlog
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String pocetak = sdf.format(a.getEvent().getStart());
		Calendar cal = Calendar.getInstance();
		cal.setTime(a.getEvent().getStart());
		cal.add(Calendar.MINUTE, a.getEvent().getDuration());
		String kraj = sdf.format(cal.getTime());
		String reason = a.getName() + " iz kolegija " + a.getCourseInstance().getCourse().getName();

		// Idemo za svaki venue posebno...
		for(String venue : roomsByVenues.keySet()) {
			IReservationManagerFactory factory = ReservationManagerFactory.getFactory(venue);
			IReservationManager manager = null;
			try {
				manager = factory.getInstance(data.getCurrentUser().getId(), data.getCurrentUser().getJmbag(), data.getCurrentUser().getUsername());
				
				List<AssessmentRoom> list = roomsByVenues.get(venue);
				List<RoomReservation> checkRooms = new ArrayList<RoomReservation>(list.size());
				Map<String,AssessmentRoom> roomByShortName = new HashMap<String, AssessmentRoom>(list.size());
				for(int i = 0; i < list.size(); i++) {
					AssessmentRoom r = list.get(i);
					roomByShortName.put(r.getRoom().getShortName(), r);
					if(!manager.isUnderControl(r.getRoom().getShortName())) {
						if(r.getRoomStatus()==null) {
							r.setRoomStatus(AssessmentRoomStatus.NOT_UNDER_CONTROL);
						} else switch(r.getRoomStatus()) {
							case AVAILABLE:
							case RESERVED:
							case UNAVAILABLE:
							case UNCHECKED:
								r.setRoomStatus(AssessmentRoomStatus.NOT_UNDER_CONTROL);
								break;
						}
					} else {
						// Ako je pod kontrolom sustava za rezervacije
						checkRooms.add(new RoomReservation(r.getRoom().getShortName()));
					}
				}
				String description = "";
				if(a.getAssessmentFlag()!=null) description = a.getAssessmentFlag().getShortName();
				String context = a.getCourseInstance().getId()+"#asid#"+a.getId()+"#"+description;
				manager.checkRoom(checkRooms, pocetak, kraj, reason, context);
				for(RoomReservation rr : checkRooms) {
					AssessmentRoom r = roomByShortName.get(rr.getRoomShortName());
					switch(rr.getStatus()) {
					case FREE: 
						r.setRoomStatus(AssessmentRoomStatus.AVAILABLE);
						break;
					case NOT_UNDER_CONTROL:
						if(!AssessmentRoomStatus.MANUALLY_RESERVED.equals(r.getRoomStatus())) {
							r.setRoomStatus(AssessmentRoomStatus.NOT_UNDER_CONTROL);
						}
						break;
					case RESERVED_FOR_OTHER: 
						if(!AssessmentRoomStatus.MANUALLY_RESERVED.equals(r.getRoomStatus())) {
							r.setRoomStatus(AssessmentRoomStatus.UNAVAILABLE);
						}
						break;
					case RESERVED_FOR_US: 
						r.setRoomStatus(AssessmentRoomStatus.RESERVED);
						break;
					}
				}
			} catch(ReservationException ex) {
				data.getMessageLogger().addInfoMessage("Pogreška prilikom komunikacije sa sustavom rezervacija za "+venue+": "+ex.getMessage());
			} finally {
				try { if(manager!=null) manager.close(); } catch(ReservationException ignorable) {}
			}
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.availableStatusUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void syncReservations(EntityManager em, AssessmentRoomScheduleData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		Assessment a = data.getAssessment();
				
		if(a.getEvent()==null) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDateSet"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if(a.getEvent().getDuration()<1) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDurationSet"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}

		boolean giveUp = false;
		// Mapiraj sve sobe po Venue-ima, jer po njima idu IReservationManager-i.
		Set<AssessmentRoom> rooms = data.getAssessment().getRooms();
		Map<String,List<AssessmentRoom>> roomsByVenues = new HashMap<String, List<AssessmentRoom>>();
		for(AssessmentRoom room : rooms) {
			String venue = room.getRoom().getVenue().getShortName();  
			List<AssessmentRoom> list = roomsByVenues.get(venue);
			if(list==null) {
				list = new ArrayList<AssessmentRoom>();
				roomsByVenues.put(venue, list);
			}
			list.add(room);
			if(room.getRoomStatus()==null || room.getRoomStatus().equals(AssessmentRoomStatus.UNCHECKED)) {
				giveUp = true;
			}
		}

		if(giveUp) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.uncheckedRoomsFound"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}

		// Definiraj datum pocetka, kraja i razlog
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String pocetak = sdf.format(a.getEvent().getStart());
		Calendar cal = Calendar.getInstance();
		cal.setTime(a.getEvent().getStart());
		cal.add(Calendar.MINUTE, a.getEvent().getDuration());
		String kraj = sdf.format(cal.getTime());
		String reason = a.getName() + " iz kolegija " + a.getCourseInstance().getCourse().getName();

		// Idemo za svaki venue posebno...
		for(String venue : roomsByVenues.keySet()) {
			IReservationManagerFactory factory = ReservationManagerFactory.getFactory(venue);
			IReservationManager manager = null;
			try {
				manager = factory.getInstance(data.getCurrentUser().getId(), data.getCurrentUser().getJmbag(), data.getCurrentUser().getUsername());
				
				// Idemo vidjeti za svaku sobu...
				List<AssessmentRoom> list = roomsByVenues.get(venue);
				for(int i = 0; i < list.size(); i++) {
					AssessmentRoom r = list.get(i);
					// Ako ova dvorana nije oznacena kao odabrana...
					if(!r.isTaken()) {
						// Ako smo je mi zauzeli kroz rezervacije, oslobodi je...
						if(AssessmentRoomStatus.RESERVED.equals(r.getRoomStatus())) {
							manager.deallocateRoom(r.getRoom().getShortName(), pocetak, kraj);
							r.setRoomStatus(AssessmentRoomStatus.AVAILABLE);
						}
					} else { // Ako je dvorana oznacena kao odabrana...
						// Ako je pod kontrolom managera:
						if(manager.isUnderControl(r.getRoom().getShortName())) {
							// Ako vec nije rezervirana za mene i ako nije nedostupna:
							if(!AssessmentRoomStatus.MANUALLY_RESERVED.equals(r.getRoomStatus()) && !AssessmentRoomStatus.RESERVED.equals(r.getRoomStatus()) && !AssessmentRoomStatus.UNAVAILABLE.equals(r.getRoomStatus())) {
								String description = "";
								if(r.getAssessment().getAssessmentFlag()!=null) description = r.getAssessment().getAssessmentFlag().getShortName();
								String context = r.getAssessment().getCourseInstance().getId()+"#asid#"+r.getAssessment().getId()+"#"+description;
								boolean uspjeh = false;
								try {
									// Za svaki slučaj provjeri još jednom dotičnu dvoranu. Tek ako je slobodna kreni u rezerviranje...
									RoomReservation rr = manager.checkRoom(r.getRoom().getShortName(), pocetak, kraj, reason, context);
									if(rr!=null && rr.getStatus().equals(RoomReservationStatus.FREE)) {
										uspjeh = manager.allocateRoom(r.getRoom().getShortName(), pocetak, kraj, reason, context);
									}
								} catch(ReservationException ignorable) {
								}
								if(uspjeh) {
									r.setRoomStatus(AssessmentRoomStatus.RESERVED);
								} else {
									data.getMessageLogger().addInfoMessage("Pokušaj rezervacij dvorane "+r.getRoom().getShortName()+" nije uspio.");
								}
							}
						} else { // Inace ne mogu napraviti nista...
						}
					}
				}
			} catch(ReservationException ex) {
				data.getMessageLogger().addInfoMessage("Pogreška prilikom komunikacije sa sustavom rezervacija za "+venue+": "+ex.getMessage());
			} finally {
				try { if(manager!=null) manager.close(); } catch(ReservationException ignorable) {}
			}
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.reservationSyncCompleted"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Privremena verzija API-ja za rezervaciju dvorana za meduispite.
	 * 
	 * @param em
	 * @param a
	 */
	public static void syncReservationsEx(EntityManager em, AbstractActionData data, Assessment a) {
		
		if(a.getEvent()==null) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDateSet"));
			return;
		}
		if(a.getEvent().getDuration()<1) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDurationSet"));
			return;
		}

		// boolean giveUp = false;
		// Mapiraj sve sobe po Venue-ima, jer po njima idu IReservationManager-i.
		Set<AssessmentRoom> rooms = a.getRooms();
		Map<String,List<AssessmentRoom>> roomsByVenues = new HashMap<String, List<AssessmentRoom>>();
		for(AssessmentRoom room : rooms) {
			String venue = room.getRoom().getVenue().getShortName();  
			List<AssessmentRoom> list = roomsByVenues.get(venue);
			if(list==null) {
				list = new ArrayList<AssessmentRoom>();
				roomsByVenues.put(venue, list);
			}
			list.add(room);
			//if(room.getRoomStatus()==null || room.getRoomStatus().equals(AssessmentRoomStatus.UNCHECKED)) {
			//	giveUp = true;
			//}
		}

		// Ovo sada necemo raditi...
//		if(giveUp) {
//			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.uncheckedRoomsFound"));
//			data.setResult(AbstractActionData.RESULT_SUCCESS);
//			return;
//		}

		// Definiraj datum pocetka, kraja i razlog
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String pocetak = sdf.format(a.getEvent().getStart());
		Calendar cal = Calendar.getInstance();
		cal.setTime(a.getEvent().getStart());
		cal.add(Calendar.MINUTE, a.getEvent().getDuration());
		String kraj = sdf.format(cal.getTime());
		String reason = a.getName() + " iz kolegija " + a.getCourseInstance().getCourse().getName();

		// Idemo za svaki venue posebno...
		for(String venue : roomsByVenues.keySet()) {
			IReservationManagerFactory factory = ReservationManagerFactory.getFactory(venue);
			IReservationManager manager = null;
			try {
				manager = factory.getInstance(data.getCurrentUser().getId(), data.getCurrentUser().getJmbag(), data.getCurrentUser().getUsername());
				
				// Idemo vidjeti za svaku sobu...
				List<AssessmentRoom> list = roomsByVenues.get(venue);
				for(int i = 0; i < list.size(); i++) {
					AssessmentRoom r = list.get(i);
					// Ako ova dvorana nije oznacena kao odabrana...
					if(!r.isTaken()) {
						// Ako smo je mi zauzeli kroz rezervacije, oslobodi je...
						if(AssessmentRoomStatus.RESERVED.equals(r.getRoomStatus())) {
							manager.deallocateRoom(r.getRoom().getShortName(), pocetak, kraj);
							if(manager.isUnderControl(r.getRoom().getShortName())) {
								r.setRoomStatus(AssessmentRoomStatus.AVAILABLE);
							} else {
								r.setRoomStatus(AssessmentRoomStatus.NOT_UNDER_CONTROL);
							}
						}
					} else { // Ako je dvorana oznacena kao odabrana...
						// Ako je pod kontrolom managera:
						if(manager.isUnderControl(r.getRoom().getShortName())) {
							String description = "";
							if(r.getAssessment().getAssessmentFlag()!=null) description = r.getAssessment().getAssessmentFlag().getShortName();
							String context = r.getAssessment().getCourseInstance().getId()+"#asid#"+r.getAssessment().getId()+"#"+description;
							// Ako vec nije rezervirana za mene i ako nije nedostupna:
							if(!AssessmentRoomStatus.MANUALLY_RESERVED.equals(r.getRoomStatus()) && !AssessmentRoomStatus.RESERVED.equals(r.getRoomStatus())) {
								boolean uspjeh = false;
								boolean prethodnoNasa = false;
								try {
									// Za svaki slučaj provjeri još jednom dotičnu dvoranu. Tek ako je slobodna kreni u rezerviranje...
									RoomReservation rr = manager.checkRoom(r.getRoom().getShortName(), pocetak, kraj, reason, context);
									if(rr!=null && rr.getStatus().equals(RoomReservationStatus.FREE)) {
										uspjeh = manager.allocateRoom(r.getRoom().getShortName(), pocetak, kraj, reason, context);
									} else if(rr!=null && rr.getStatus().equals(RoomReservationStatus.RESERVED_FOR_US)) {
										uspjeh = true;
										prethodnoNasa = true;
									}
								} catch(ReservationException ignorable) {
								}
								if(uspjeh) {
									if(prethodnoNasa) {
										data.getMessageLogger().addInfoMessage("Dvorana "+r.getRoom().getShortName()+" je već rezervirana za kolegij "+a.getCourseInstance().getCourse().getIsvuCode()+" "+a.getCourseInstance().getCourse().getName()+".");
									} else {
										r.setRoomStatus(AssessmentRoomStatus.RESERVED);
									}
								} else {
									data.getMessageLogger().addInfoMessage("Pokušaj rezervacije dvorane "+r.getRoom().getShortName()+" za kolegij "+a.getCourseInstance().getCourse().getIsvuCode()+" "+a.getCourseInstance().getCourse().getName()+" nije uspio.");
								}
							}
						} else { // Inace ne mogu napraviti nista...
							r.setRoomStatus(AssessmentRoomStatus.MANUALLY_RESERVED);
						}
					}
				}
			} catch(ReservationException ex) {
				data.getMessageLogger().addInfoMessage("Pogreška prilikom komunikacije sa sustavom rezervacija za "+venue+": "+ex.getMessage());
			} finally {
				try { if(manager!=null) manager.close(); } catch(ReservationException ignorable) {}
			}
		}
	}

	/**
	 * Privremena verzija API-ja za rezervaciju dvorana za meduispite, koja za rezervirane termine mijenja trajanje.
	 * Pri tome je skraćivanje uvijek moguće, a produživanje samo ako ima slobodnog mjesta iza.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 * @param a provjera
	 * @param oldDuration staro trajanje u minutama
	 * @param newDuration novo trajanje u minutama
	 * @return <code>true</code> ako je uspjelo, <code>false</code> inače
	 */
	public static boolean syncReservationsDurationEx(EntityManager em, AbstractActionData data, Assessment a, int oldDuration, int newDuration, boolean justCheck) {
		
		if(a.getEvent()==null) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDateSet"));
			return false;
		}
		if(a.getEvent().getDuration()<1 || oldDuration<1) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDurationSet"));
			return false;
		}

		Set<AssessmentRoom> rooms = a.getRooms();
		Map<String,List<AssessmentRoom>> roomsByVenues = new HashMap<String, List<AssessmentRoom>>();
		for(AssessmentRoom room : rooms) {
			String venue = room.getRoom().getVenue().getShortName();  
			List<AssessmentRoom> list = roomsByVenues.get(venue);
			if(list==null) {
				list = new ArrayList<AssessmentRoom>();
				roomsByVenues.put(venue, list);
			}
			list.add(room);
		}

		// Definiraj datum pocetka, kraja i razlog
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String pocetak = sdf.format(a.getEvent().getStart());
		Calendar cal = Calendar.getInstance();
		cal.setTime(a.getEvent().getStart());
		cal.add(Calendar.MINUTE, oldDuration);
		String stariKraj = sdf.format(cal.getTime());
		cal.setTime(a.getEvent().getStart());
		cal.add(Calendar.MINUTE, newDuration);
		String noviKraj = sdf.format(cal.getTime());
		
		String reason = a.getName() + " iz kolegija " + a.getCourseInstance().getCourse().getName();

		boolean safeToProlong = true;
		
		// Faza 1: oslobadanje nekoristenih dvorana
		// Idemo za svaki venue posebno...
		for(String venue : roomsByVenues.keySet()) {
			IReservationManagerFactory factory = ReservationManagerFactory.getFactory(venue);
			IReservationManager manager = null;
			try {
				manager = factory.getInstance(data.getCurrentUser().getId(), data.getCurrentUser().getJmbag(), data.getCurrentUser().getUsername());
				
				// Idemo vidjeti za svaku sobu...
				List<AssessmentRoom> list = roomsByVenues.get(venue);
				for(int i = 0; i < list.size(); i++) {
					AssessmentRoom r = list.get(i);
					// Ako ova dvorana nije oznacena kao odabrana...
					if(!r.isTaken()) {
						// Ako smo je mi zauzeli kroz rezervacije, oslobodi je...
						if(AssessmentRoomStatus.RESERVED.equals(r.getRoomStatus())) {
							manager.deallocateRoom(r.getRoom().getShortName(), pocetak, stariKraj);
							if(manager.isUnderControl(r.getRoom().getShortName())) {
								r.setRoomStatus(AssessmentRoomStatus.AVAILABLE);
							} else {
								r.setRoomStatus(AssessmentRoomStatus.NOT_UNDER_CONTROL);
							}
						}
					} else { // Ako je dvorana oznacena kao odabrana...
						// Ako je pod kontrolom managera i ako je rezervirana, vidi mogu li produziti zauzece...
						if(newDuration>oldDuration && manager.isUnderControl(r.getRoom().getShortName()) && AssessmentRoomStatus.RESERVED.equals(r.getRoomStatus())) {
							String description = "";
							if(r.getAssessment().getAssessmentFlag()!=null) description = r.getAssessment().getAssessmentFlag().getShortName();
							String context = r.getAssessment().getCourseInstance().getId()+"#asid#"+r.getAssessment().getId()+"#"+description;
							try {
								// Za svaki slučaj provjeri još jednom dotičnu dvoranu. Tek ako je slobodna kreni u rezerviranje...
								RoomReservation rr = manager.checkRoom(r.getRoom().getShortName(), stariKraj, noviKraj, reason, context);
								if(rr==null || !rr.getStatus().equals(RoomReservationStatus.FREE)) {
									data.getMessageLogger().addInfoMessage("Ne mogu produžiti trajanje dvorane "+venue+": "+r.getRoom().getShortName());
									safeToProlong = false;
								}
							} catch(ReservationException ignorable) {
								safeToProlong = false;
								data.getMessageLogger().addInfoMessage("Ne mogu produžiti trajanje dvorane "+venue+": "+r.getRoom().getShortName());
							}
						}
					}
				}
			} catch(ReservationException ex) {
				data.getMessageLogger().addInfoMessage("Pogreška prilikom komunikacije sa sustavom rezervacija za "+venue+": "+ex.getMessage());
			} finally {
				try { if(manager!=null) manager.close(); } catch(ReservationException ignorable) {}
			}
		}

		if(!safeToProlong) {
			return false;
		}
		
		// Faza 2: stvarne promjene
		// Idemo za svaki venue posebno...
		for(String venue : roomsByVenues.keySet()) {
			IReservationManagerFactory factory = ReservationManagerFactory.getFactory(venue);
			IReservationManager manager = null;
			try {
				manager = factory.getInstance(data.getCurrentUser().getId(), data.getCurrentUser().getJmbag(), data.getCurrentUser().getUsername());
				
				// Idemo vidjeti za svaku sobu...
				List<AssessmentRoom> list = roomsByVenues.get(venue);
				for(int i = 0; i < list.size(); i++) {
					AssessmentRoom r = list.get(i);
					// Ako ova dvorana nije oznacena kao odabrana...
					if(r.isTaken()) {
						// Ako je pod kontrolom managera:
						if(manager.isUnderControl(r.getRoom().getShortName())) {
							String description = "";
							if(r.getAssessment().getAssessmentFlag()!=null) description = r.getAssessment().getAssessmentFlag().getShortName();
							String context = r.getAssessment().getCourseInstance().getId()+"#asid#"+r.getAssessment().getId()+"#"+description;
							// Ako vec nije rezervirana za mene i ako nije nedostupna:
							if(AssessmentRoomStatus.RESERVED.equals(r.getRoomStatus())) {
								boolean uspjeh = false;
								try {
									uspjeh = manager.updateReservationRoom(r.getRoom().getShortName(), pocetak, stariKraj, pocetak, noviKraj, reason, context, justCheck);
								} catch(ReservationException ignorable) {
								}
								if(!uspjeh) {
									data.getMessageLogger().addInfoMessage("Pokušaj promjene rezervacije dvorane "+r.getRoom().getShortName()+" za kolegij "+a.getCourseInstance().getCourse().getName()+" nije uspio.");
									return false;
								}
							}
						}
					}
				}
			} catch(ReservationException ex) {
				data.getMessageLogger().addInfoMessage("Pogreška prilikom komunikacije sa sustavom rezervacija za "+venue+": "+ex.getMessage());
			} finally {
				try { if(manager!=null) manager.close(); } catch(ReservationException ignorable) {}
			}
		}
		
		return true;
	}

	///////////////////////////////////
	//Slijede pomocne privatne metode//
	///////////////////////////////////
	
	/**
	 * Metoda koja sortira AssessmentRoomBeanove po razlictim kriterijima
	 * @param beanList
	 * @param param kriterij po kojem se sortira, moze biti: "name", "capacity", "assistants", "roomTag", "taken", "available"  
	 * @param mod 1 ako je sortiranje uzlazno -1 ako je silazno
	 */
	private static void sort(List<AssessmentRoomBean> beanList, String param, final int mod) {
		
		
		if ("capacity".equals(param)) {
			Collections.sort(beanList, new Comparator<AssessmentRoomBean>() {
				@Override
				public int compare(AssessmentRoomBean o1, AssessmentRoomBean o2) {
					int r = mod*Integer.valueOf(o1.getCapacity()).compareTo(Integer.valueOf(o2.getCapacity()));
					if (r==0)
						return o1.getName().compareTo(o2.getName());
					return r;
				}
			});
			return ;
		}
		
		if ("assistants".equals(param)) {
			Collections.sort(beanList, new Comparator<AssessmentRoomBean>() {
				@Override
				public int compare(AssessmentRoomBean o1, AssessmentRoomBean o2) {
					int r = mod*Integer.valueOf(o1.getRequiredAssistants()).compareTo(Integer.valueOf(o2.getRequiredAssistants()));
					if (r==0)
						return o1.getName().compareTo(o2.getName());
					return r;
				}
			});
			return ;
		}
		
		if ("roomTag".equals(param)) {
			Collections.sort(beanList, new Comparator<AssessmentRoomBean>() {
				@Override
				public int compare(AssessmentRoomBean o1, AssessmentRoomBean o2) {
					int r = mod*AssessmentRoomTag.valueOf(o1.getRoomTag()).compareTo(AssessmentRoomTag.valueOf(o2.getRoomTag()));
					if (r==0)
						return o1.getName().compareTo(o2.getName());
					return r;
				}
			});
			return ;
		}
		
		if ("taken".equals(param)) {
			Collections.sort(beanList, new Comparator<AssessmentRoomBean>() {
				@Override
				public int compare(AssessmentRoomBean o1, AssessmentRoomBean o2) {
					int r = Boolean.valueOf(o1.getTaken()).compareTo(Boolean.valueOf(o2.getTaken()));
					if (r==0)
						return o1.getName().compareTo(o2.getName());
					return mod*r;
				}
			});
			return ;
		}
		
		if ("available".equals(param)) {
			Collections.sort(beanList, new Comparator<AssessmentRoomBean>() {
				@Override
				public int compare(AssessmentRoomBean o1, AssessmentRoomBean o2) {
					int r = Boolean.valueOf(o1.getAvailable()).compareTo(Boolean.valueOf(o2.getAvailable()));
					if (r==0)
						return o1.getName().compareTo(o2.getName());
					return mod*r;
				}
			});
			return ;
		}
		
		//default je ime
		Collections.sort(beanList, new Comparator<AssessmentRoomBean>() {
				@Override
				public int compare(AssessmentRoomBean o1, AssessmentRoomBean o2) {
					return mod*o1.getName().compareTo(o2.getName());
				}
		});
	}
	
	/**
	 * Metoda koja postavlja ukupan broj studenata i kapacitet svih zauzetih dvorana zadanog Assessmenta
	 * @param em 
	 * @param dh 
	 * @param data
	 * @param a
	 */
	private static void setCapacityAndUsers(DAOHelper dh, EntityManager em, AssessmentRoomScheduleData data, Assessment a) {
		
		int currCapacity = 0, userNumber = 0;
		if (a.getGroup() != null) {
			Number num = dh.getUserDAO().getUserNumber(em, a.getCourseInstance().getId(),
					a.getGroup().getRelativePath()+"/%", a.getGroup().getRelativePath());
			userNumber = num.intValue();
		}
		data.setUserNumber(userNumber);
		for (AssessmentRoom ar : a.getRooms()) {
			if (ar.isTaken()) currCapacity += ar.getCapacity();
		}
		data.setCurrCapacity(currCapacity);
		if (currCapacity == 0)
			data.setPercent("-");
		else {
			DecimalFormat formatter = new DecimalFormat("0.00%");
			data.setPercent(formatter.format((double)userNumber/currCapacity));
		}
	}

	private static AssessmentRoomBean fillAssessmentBean(AssessmentRoom ar) {

		AssessmentRoomBean bean = new AssessmentRoomBean();

		bean.setAvailable(ar.isAvailable());
		bean.setCapacity(String.valueOf(ar.getCapacity()));
		bean.setName(ar.getRoom().getShortName());
		bean.setId(String.valueOf(ar.getId()));
		bean.setRequiredAssistants(String.valueOf(ar.getRequiredAssistants()));
		bean.setRoomTag(String.valueOf(ar.getRoomTag()));
		bean.setTaken(String.valueOf(ar.isTaken()));
		bean.setRoomStatus(ar.getRoomStatus()==null ? null : ar.getRoomStatus().toString());
		return bean;
	}
	
	private static void updateAssessmentRoom(AssessmentRoom ar, AssessmentRoomBean bean) {
		ar.setCapacity(Integer.valueOf(bean.getCapacity()));
		ar.setRequiredAssistants(Integer.valueOf(bean.getRequiredAssistants()));
		ar.setTaken(Boolean.valueOf(bean.getTaken()));
		ar.setRoomTag(AssessmentRoomTag.valueOf(bean.getRoomTag()));
	}
	
	private static boolean isBeanDataValid(IMessageLogger logger,  AssessmentRoomBean bean) {
		boolean ok = true;
		int x = 0;
		try {
			x = Integer.valueOf(bean.getCapacity());
			if (x<0) {
				logger.addErrorMessage(logger.getText("Error.capacityMustBePositive"));
				ok = false;
			}
			x = Integer.valueOf(bean.getRequiredAssistants());
			if (x<0) {
				logger.addErrorMessage(logger.getText("Error.assistantsMustBePositive"));
				ok = false;
			}
			Boolean.valueOf(bean.getTaken());
			AssessmentRoomTag.valueOf(bean.getRoomTag());
		} catch (Exception ex) {
			logger.addErrorMessage(logger.getText("Error.wrongType"));
			ok = false;
		}
		return ok;
	}

}
