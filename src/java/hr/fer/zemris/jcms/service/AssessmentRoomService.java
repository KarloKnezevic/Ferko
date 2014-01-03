package hr.fer.zemris.jcms.service;


import hr.fer.zemris.jcms.beans.AssessmentRoomBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentRoomArrangedBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentAssistantSchedule;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomStatus;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomTag;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.assessments.ListsGenerator;
import hr.fer.zemris.jcms.service.assessments.ScheduleToMailMergeGenerator;
import hr.fer.zemris.jcms.service.assessments.ScheduleToPDFGenerator;
import hr.fer.zemris.jcms.service.reservations.IReservationManager;
import hr.fer.zemris.jcms.service.reservations.IReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.ReservationException;
import hr.fer.zemris.jcms.service.reservations.ReservationManagerFactory;
import hr.fer.zemris.jcms.service.reservations.RoomReservation;
import hr.fer.zemris.jcms.service.reservations.RoomReservationStatus;
import hr.fer.zemris.jcms.web.actions.data.AssessmentRoomScheduleData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentScheduleData;
import hr.fer.zemris.jcms.web.actions.data.BaseAssessment;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

public class AssessmentRoomService {
	
	/**
	 * Metoda koja se bavi listanjem i updateom soba
	 * @param data
	 * @param assessmentID
	 * @param venueShortName
	 * @param beanList
	 * @param method
	 */
	public static void getRoomsForAssessment(final AssessmentRoomScheduleData data, final String assessmentID, 
			final String venueShortName,final List<AssessmentRoomBean> beanList, final String sort, 
			final String type, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {

			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//napunimo s podacima
				if (!getRequiredData(em, data, assessmentID))
					return null;
				Assessment a = data.getAssessment();
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//sinkroniziramo sobe 
				if (a.getRooms()==null || a.getRooms().size()==0)
					synchronizeRooms(dh,em,a,venueShortName);

				//napunimo beanListu s vrijednostima i pobrojimo zauzeta mjesta
				for (AssessmentRoom ar : a.getRooms()) {
					beanList.add(fillAssessmentBean(ar));
				}
				
				//na kraju sortiramo beanListu po parametru
				if ("desc".equals(type))
					sort(beanList,sort,-1);
				else
					sort(beanList,sort,1);
				
				//postavimo podatke o userima i kapacitetu
				setCapacityAndUsers(dh,em,data, a);
				
				// inace dodajemo poruke
				if (data.getUserNumber() == 0)
					data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.noStudents"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				
				return null;
			}
		});
	}

	

	public static void updateRoomsForAssessment(final AssessmentRoomScheduleData data, final String assessmentID, 
			final List<AssessmentRoomBean> beanList, final Long userID, final String sure) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {

			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//napunimo s podacima
				if (!getRequiredData(em, data, assessmentID))
					return null;
				Assessment a = data.getAssessment();
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//koristit cemo rooms umjesto a.getRooms()
				Collection<AssessmentRoom> rooms = a.getRooms();
				
				if (beanList == null || beanList.size() == 0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if (rooms == null || rooms.size()==0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRooms"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				// stavi assesmentRoomove u mapu tako da ih mozemo lagano dohvatiti
				Map<Long, AssessmentRoom> roomMap = new HashMap<Long, AssessmentRoom>(rooms.size());
				for (AssessmentRoom ar : rooms) {
					roomMap.put(ar.getId(), ar);
				}
				
				boolean hasErrors = false;
				
				//provjera da li su beanovi dobro ispunjeni
				for (AssessmentRoomBean bean : beanList) {
					
					//provjera postoji li assessmentRoom sa zadanim id.em
					AssessmentRoom ar = null;
					try { 
						ar = roomMap.get(Long.valueOf(bean.getId())); 
					}
					catch (Exception ignorable) { }
					
					if (ar == null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
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
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				//TODO: ako vec postoji raspored pitamo korisnika zeli li unistiti raspored
				
				boolean arranged = false;
				if (a.getGroup()!=null && data.getUserNumber() > a.getGroup().getUsers().size())
					arranged = true;
				
				boolean doit = false;
				try {
					doit = Boolean.valueOf(sure);
				} catch (Exception ignorable) {}
				
				if (arranged && !doit) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if (arranged)
					AssessmentStudentService.clearAssessmentSchedule(dh, em, a);
				
				//inace radimo update
				for (AssessmentRoomBean bean : beanList) {
					AssessmentRoom ar = roomMap.get(Long.valueOf(bean.getId()));
					updateAssessmentRoom(ar, bean);
				}

				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	
	
	public static void autoChooseRooms(final AssessmentRoomScheduleData data, final String assessmentID,
			final String sure, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {

			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				//napunimo s podacima
				
				if (!getRequiredData(em, data, assessmentID))
					return null;
				Assessment a = data.getAssessment();
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//koristit cemo rooms umjesto a.getRooms()
				Collection<AssessmentRoom> rooms = a.getRooms();
				
				if (rooms == null || rooms.size()==0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noRooms"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//postavljamo podatke
				setCapacityAndUsers(dh,em,data, a);
				
				if (data.getUserNumber() ==0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noStudents"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
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
					return null;
				}
				
				//provjera postoji li raspored prije nego napravimo autochoose
				boolean arranged = false;
				if (data.getUserNumber() != a.getGroup().getUsers().size()) 
					arranged = true;
				
				boolean doit = false;
				try {
					doit = Boolean.valueOf(sure);
				} catch (Exception ignorable) {}
				
				if (arranged && !doit) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				else if (arranged)
					AssessmentStudentService.clearAssessmentSchedule(dh, em, a);
				
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
				return null;		
			}

		});
	}

	public static void getAvailableStatus(final AssessmentRoomScheduleData data, final String assessmentID,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				
				if (!BasicBrowsing.fillAssessment(em, data, assessmentID))
					return null;
				Assessment a = data.getAssessment();
				data.setCourseInstance(a.getCourseInstance());
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if(a.getEvent()==null) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDateSet"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(a.getEvent().getDuration()<1) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDurationSet"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
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
						manager.checkRoom(checkRooms, pocetak, kraj, reason);
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
				return null;
			}
		});
	}
	
	public static void syncReservations(final AssessmentRoomScheduleData data, final String assessmentID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				
				if (!BasicBrowsing.fillAssessment(em, data, assessmentID))
					return null;
				Assessment a = data.getAssessment();
				data.setCourseInstance(a.getCourseInstance());
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if(a.getEvent()==null) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDateSet"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(a.getEvent().getDuration()<1) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.noAssessmentDurationSet"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
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
					return null;
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
										boolean uspjeh = false;
										try {
											// Za svaki slučaj provjeri još jednom dotičnu dvoranu. Tek ako je slobodna kreni u rezerviranje...
											RoomReservation rr = manager.checkRoom(r.getRoom().getShortName(), pocetak, kraj, reason);
											if(rr!=null && rr.getStatus().equals(RoomReservationStatus.FREE)) {
												uspjeh = manager.allocateRoom(r.getRoom().getShortName(), pocetak, kraj, reason);
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
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.availableStatusUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	@Deprecated
	public static void viewRoomList(final AssessmentScheduleData data, final String assessmentID,
			final String venueShortName, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				if (!getRequiredData(em, data, assessmentID))
					return null;
				Assessment a = data.getAssessment();
				data.setCourseInstance(a.getCourseInstance());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Collection<AssessmentRoom> rooms = a.getRooms();
				
				if (rooms == null || rooms.size()==0) {
					synchronizeRooms(DAOHelperFactory.getDAOHelper(), em, a, venueShortName);
				}
				List<AssessmentRoomArrangedBean> beanList = new ArrayList<AssessmentRoomArrangedBean>(rooms.size());
				
				//TODO: napraviti upit koji ce vratiti broj studenata po dvorani
				
				//punimo mapu asistenata mapiranih po dvorani
				Map<AssessmentRoom, Integer> assistantPerRoomMap = new HashMap<AssessmentRoom, Integer>();
				if (a.getAssistantSchedule() != null)
					for (AssessmentAssistantSchedule aas : a.getAssistantSchedule()) {
						AssessmentRoom ar = aas.getRoom();
						if (ar != null) {
							Integer count = assistantPerRoomMap.get(ar);
							if (count != null)
								assistantPerRoomMap.put(ar, count+1);
							else
								assistantPerRoomMap.put(ar, new Integer(1));
						}
					}
				
				for (AssessmentRoom ar : a.getRooms()) {
					if (ar.isTaken()) {
						AssessmentRoomArrangedBean agb = new AssessmentRoomArrangedBean();
						agb.setAssessmentRoomID(ar.getId());
						agb.setRoomName(ar.getRoom().getShortName());
						agb.setCapacity(ar.getCapacity());
						agb.setAssistantRequired(ar.getRequiredAssistants());
						if (ar.getGroup() != null && ar.getGroup().getUsers() != null)
							agb.setUserNum(ar.getGroup().getUsers().size());
						else
							agb.setUserNum(0);
						
						if (assistantPerRoomMap.get(ar) == null)
							agb.setAssistantNum(0);
						else
							agb.setAssistantNum(assistantPerRoomMap.get(ar));
						beanList.add(agb);
					}
				}
				
				data.setRoomList(beanList);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}
	
	@Deprecated
	public static void viewRoomInfo(final AssessmentScheduleData data, final String assessmentRoomID,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				if (StringUtil.isStringBlank(assessmentRoomID)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
				}
				AssessmentRoom ar = null;
				try {
					ar = dh.getAssessmentDAO().getAssessmentRoom(em, Long.valueOf(assessmentRoomID));
				} catch(Exception ignorable) {}
				
				if(ar==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.assessmentRoomNotFound"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setAssessment(ar.getAssessment());
				data.setCourseInstance(ar.getAssessment().getCourseInstance());
				data.setRoomName(ar.getRoom().getShortName());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				
				//punimo info s asistentima
				List<User> assistantList = new ArrayList<User>();
				if (ar.getAssessment().getAssistantSchedule() != null)
					for (AssessmentAssistantSchedule aas : ar.getAssessment().getAssistantSchedule())
						if (ar.equals(aas.getRoom()))
							assistantList.add(aas.getUser());
				
				data.setAssistantList(assistantList);
				
				//provjeravamo ima li studenata
				Group g = ar.getGroup();
				if (g == null || g.getUsers() == null || 
						g.getUsers().size()==0) {
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//punimo info sa studentima
				List<User> userList = new ArrayList<User>(g.getUsers().size());
				List<UserGroup> tmpList = new ArrayList<UserGroup>(g.getUsers());
				//sortiramo po positionu
				Collections.sort(tmpList, new Comparator<UserGroup>() {
					@Override
					public int compare(UserGroup o1, UserGroup o2) {
						return o1.getPosition()-o2.getPosition();
					}
				});
				for (UserGroup ug : tmpList)
					userList.add(ug.getUser());
				
				data.setUserList(userList);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				
				return null;
			}
		});
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
	
	private static boolean getRequiredData(EntityManager em, BaseAssessment data, String assessmentID) {
		
		if (!BasicBrowsing.fillAssessment(em, data, assessmentID))
			return false;
		data.setCourseInstance(data.getAssessment().getCourseInstance());
		
		return true;
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


	/**
	 * Metoda koja sinkronizira skup AssessmentRoomova nekog Assessmenta sa skupom dvorana iz baze za zadani venue.
	 * (stvara nove AssessmentRoomove ukoliko odgovarajuci ne postoje za odredjenu dvoranu iz baze)
	 * @param dh
	 * @param em
	 * @param a
	 * @param venueShortName
	 * @return
	 */
	@Deprecated
	public static boolean synchronizeRooms(DAOHelper dh, EntityManager em, Assessment a, String venueShortName) {
		
		Collection<AssessmentRoom> rooms = a.getRooms();
		boolean result = false;
		
		//popuni set s roomovima koji trenutno postoje za assessment
		Set<Room> assessmentRoomSet = new HashSet<Room>(rooms.size());
				
		for (AssessmentRoom ar : rooms) {
			assessmentRoomSet.add(ar.getRoom());
		}

		// sinkroniziraj sobe (stvori nove assessmentRoomove ako ih nema)
		List<Room> roomList = dh.getRoomDAO().listByVenue(em,venueShortName);
		for (Room r : roomList) {
			if (!assessmentRoomSet.contains(r) && r.getAssessmentPlaces() > 0 && r.getPublicRoom()) {
				createAssessmentRoom(em, a, r);
				result = true;
			}
		}
		
		return result;
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
	
	private static void createAssessmentRoom(EntityManager em, Assessment a, Room r) {
		AssessmentRoom ar = new AssessmentRoom();
		ar.setAssessment(a);
		ar.setAvailable(true);
		ar.setCapacity(r.getAssessmentPlaces());
		ar.setGroup(null);
		ar.setRequiredAssistants(r.getAssessmentAssistants());
		ar.setRoom(r);
		ar.setRoomTag(AssessmentRoomTag.MANDATORY);
		ar.setTaken(false);
		ar.setUserEvent(null);

		a.getRooms().add(ar);
		em.persist(ar);
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



	/**
	 * Metoda generira sve potrebne popise za provjeru, u obliku PDF datoteke.
	 * 
	 * @param data
	 * @param userID
	 * @param assessmentID
	 * @param reference
	 */
	@Deprecated
	public static void prepareDownloadListings(final AssessmentScheduleData data, final Long userID, final String assessmentID, final DeleteOnCloseFileInputStream[] reference) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID)) return null;
				if(!BasicBrowsing.fillAssessment(em, data, assessmentID)) return null;
				data.setCourseInstance(data.getAssessment().getCourseInstance());

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				File f = null;
				try {
					f = File.createTempFile("JCMS_", ".pdf");
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				BufferedOutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(f));
					ListsGenerator lgen = new ListsGenerator(os);
					lgen.generateLists(data.getAssessment());
					lgen.close();
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				stream.setFileName("popisi.pdf");
				stream.setMimeType("application/pdf");
				reference[0] = stream;
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}



	/**
	 * Metoda generira samo raspored studenata za provjeru, u obliku PDF datoteke (i to samo po JMBAG-ovima).
	 * @param data
	 * @param userID
	 * @param assessmentID
	 * @param reference
	 */
	@Deprecated
	public static void prepareDownloadSchedule(final AssessmentScheduleData data, final Long userID, final String assessmentID, final DeleteOnCloseFileInputStream[] reference) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID)) return null;
				if(!BasicBrowsing.fillAssessment(em, data, assessmentID)) return null;
				data.setCourseInstance(data.getAssessment().getCourseInstance());

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				File f = null;
				try {
					f = File.createTempFile("JCMS_", ".pdf");
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				BufferedOutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(f));
					ScheduleToPDFGenerator lgen = new ScheduleToPDFGenerator(os);
					lgen.generateLists(data.getAssessment());
					lgen.close();
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				stream.setFileName("raspored.pdf");
				stream.setMimeType("application/pdf");
				reference[0] = stream;
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	/**
	 * Metoda generira samo raspored studenata za provjeru, u obliku PDF datoteke (i to samo po JMBAG-ovima).
	 * @param data
	 * @param userID
	 * @param assessmentID
	 * @param reference
	 */
	@Deprecated
	public static void prepareDownloadMailMerge(final AssessmentScheduleData data, final Long userID, final String assessmentID, final DeleteOnCloseFileInputStream[] reference) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID)) return null;
				if(!BasicBrowsing.fillAssessment(em, data, assessmentID)) return null;
				data.setCourseInstance(data.getAssessment().getCourseInstance());

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessmentSchedule(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				File f = null;
				try {
					f = File.createTempFile("JCMS_", ".txt");
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				Writer os = null;
				try {
					os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f)),"UTF-8");
					ScheduleToMailMergeGenerator lgen = new ScheduleToMailMergeGenerator(os);
					lgen.generateLists(data.getAssessment());
					lgen.close();
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGenerateMailMerge"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGenerateMailMerge"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				stream.setFileName("MailMerge.txt");
				stream.setMimeType("application/octet-stream");
				reference[0] = stream;
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
}
