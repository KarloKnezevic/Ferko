package hr.fer.zemris.jcms.service;
 
import java.io.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.CourseComponentBean;
import hr.fer.zemris.jcms.beans.PlanDescriptorBean;
import hr.fer.zemris.jcms.beans.ScheduleBean;
import hr.fer.zemris.jcms.beans.ScheduleEventBean;
import hr.fer.zemris.jcms.beans.ScheduleTermBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.dao.PlanningDAO;
import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseComponentDescriptor;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.model.extra.PlanStatus;
import hr.fer.zemris.jcms.model.planning.*;
import hr.fer.zemris.jcms.model.planning.PlanningEntity.DateSpan;
import hr.fer.zemris.jcms.model.planning.Definition.IDefinitionParameter;
import hr.fer.zemris.jcms.model.planning.Definition.PeopleParameter;
import hr.fer.zemris.jcms.model.planning.Definition.RoomParameter;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.ScheduleAnalyzerService.BasicResult;
import hr.fer.zemris.jcms.service.reservations.*; 
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service2.course.groups.GroupTreeBrowserService;
import hr.fer.zemris.jcms.service2.sysadmin.ComponentScheduleSyncService;
import hr.fer.zemris.jcms.web.actions.data.PlanningData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.InputStreamWrapper;
import hr.fer.zemris.util.scheduling.support.*;
import hr.fer.zemris.util.time.*;
import hr.fer.zemris.util.time.TemporalList.TL;
import javax.persistence.EntityManager;
import javax.xml.parsers.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Komponenta sloja usluge za podršku Planiranju
 * 
 * @author ivanfer
 */
public class PlanningService {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	
	public static String getPackageLocationOnDisk(String classPackage){
		String fileSeparator = System.getProperty("file.separator");
		String currentDir = JCMSSettings.getSettings().getApplRealPath();
		StringBuilder classFileBuilder = new StringBuilder();
		classFileBuilder.append(currentDir);
		classFileBuilder.append(fileSeparator);
		classFileBuilder.append("applet");
		classFileBuilder.append(fileSeparator);
		String tmp = classPackage.replaceAll("\\.", "\\"+fileSeparator);
		classFileBuilder.append(tmp);
		classFileBuilder.append(fileSeparator);
		return classFileBuilder.toString();
	}
	
	/**
	 * Dohvat planova i rasporeda za trenutnog korisnika na trenutnom kolegiju
	 * @param courseInstanceID
	 * @param userID
	 */
	public static void getPlans(final PlanningData data, final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User user = dh.getUserDAO().getUserById(em, userID);
				
				JCMSSecurityManagerFactory.getManager().init(user, em);
				CourseInstance courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(courseInstance);
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(courseInstance)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<PlanDescriptor> plans = dh.getPlanningDAO().listPlans(em, courseInstanceID, userID);
				List<PlanDescriptorBean> planBeans = new ArrayList<PlanDescriptorBean>();
				for(PlanDescriptor p : plans) planBeans.add(new PlanDescriptorBean(p, data));
				
				SimpleDateFormat sdf = new SimpleDateFormat(PlanningService.DATE_FORMAT);
				
				for(PlanDescriptorBean pb: planBeans) {
					List<ScheduleBean> schedules = new ArrayList<ScheduleBean>();
					for(ScheduleDescriptor schedule : dh.getPlanningDAO().listSchedulesForPlan(em, pb.getID())) {
						ScheduleBean bean = new ScheduleBean(Long.toString(schedule.getId()));
						bean.setCreationDate(sdf.format(schedule.getCreationDate()));
						String[] params = schedule.getParameters().split(" ");
						if(params!=null && params.length>1){
							String[] publicationParams = params[1].split("=");
							if(publicationParams!=null && publicationParams.length>1) bean.setPublicationDate(publicationParams[1]);
						}
						schedules.add(bean);
					}
					Collections.sort(schedules);
					pb.setSchedules(schedules);
				}
				Collections.sort(planBeans);
				data.setPlanBeans(planBeans);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	/**
	 * Priprema podataka za izradu novog plana
	 * @param data
	 * @param userID
	 * @param courseInstanceID
	 */
	public static void prepareForNewPlan(final PlanningData data, final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User user = dh.getUserDAO().getUserById(em, userID);
				JCMSSecurityManagerFactory.getManager().init(user, em);
				CourseInstance courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(courseInstance);
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(courseInstance)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	/**
	 * Dohvat grupa na trenutnom kolegiju
	 * @param courseInstanceID
	 * @param userID
	 * @param wrapper
	 */
	public static void getCourseInstanceGroups(final PlanningData data, final String courseInstanceID, final Long userID, final InputStreamWrapper[] wrapper) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User user = dh.getUserDAO().getUserById(em, userID);
				
				JCMSSecurityManagerFactory.getManager().init(user, em);
				CourseInstance courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(courseInstance);
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(courseInstance)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//Dohvat grupa na kolegiju
				List<Group> allGroups = new ArrayList<Group>();
				//grupe za predavanja
				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				List<Group> lectureGroups = JCMSSecurityManagerFactory.getManager().listAccessibleGroups(ci, "0");
				//izbacivanje 0/0 grupe za predavanja
				Iterator<Group> i = lectureGroups.iterator();
				while(i.hasNext()){
					Group g = i.next();
					if(g.getRelativePath().equals("0/0")){
						i.remove(); 
						break;
					}
				}
				allGroups.addAll(lectureGroups);   

				int[] groupsToLoad = {1,2,4,5,6};
				for(int k=0; k<groupsToLoad.length; k++){
					addAccessibleGroupsForRelativePath(allGroups, Integer.toString(groupsToLoad[k]), data.getCourseInstance());
				}
				
				//Oblikovanje XML zapisa
				StringBuilder groups = new StringBuilder();
				groups.append("<groups>");
				StringBuilder group = new StringBuilder();
				for(Group g : allGroups){
					group = new StringBuilder();
					group.append("<group>");
					group.append("<name>");group.append(g.getName());group.append("</name>");
					group.append("<id>");group.append(g.getId());group.append("</id>");
					group.append("<path>");group.append(g.getRelativePath());group.append("</path>");
					group.append("</group>");
					groups.append(group.toString());
				}
				groups.append("</groups>");
				wrapper[0] = createInputStreamWrapperFromText(groups.toString());
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	private static void addAccessibleGroupsForRelativePath(List<Group> result, String relativePath, CourseInstance ci){
		List<Group> tmp = JCMSSecurityManagerFactory.getManager().listAccessibleGroups(ci, relativePath);
		result.addAll(tmp);  
	}
	
	/**
	 * Dohvat dostupnih prostorija na faksu
	 * @param courseInstanceID
	 * @param wrapper
	 */
	public static void getRooms(final String courseInstanceID, final Long userID, final InputStreamWrapper[] wrapper){
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				List<Room> roomList = dh.getRoomDAO().list(em);
				
				//Oblikovanje XML zapisa
				StringBuilder rooms = new StringBuilder();
				rooms.append("<rooms>");
				StringBuilder room;
				for(Room r : roomList){
					room = new StringBuilder();
					room.append("<room>");
					room.append("<name>");
					room.append(r.getName());
					room.append("</name>");
					room.append("<id>");
					room.append(r.getId());
					room.append("</id>");
					room.append("<cap>");
					room.append(r.getLecturePlaces()+"-"+r.getExercisePlaces()+"-"+r.getAssessmentPlaces());
					room.append("</cap>");
					room.append("</room>");
					rooms.append(room.toString());
				}
				rooms.append("</rooms>");
				wrapper[0] = createInputStreamWrapperFromText(rooms.toString());
				return null;
			}
		});
	}
	
	/**
	 * Dohvat zapisa plana
	 * @param courseInstanceID
	 * @param wrapper
	 */
	public static void getPlan(final PlanningData data, final String courseInstanceID, final Long planID, final Long userID, final InputStreamWrapper[] wrapper){
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User user = dh.getUserDAO().getUserById(em, userID);
				JCMSSecurityManagerFactory.getManager().init(user, em);
				CourseInstance courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(courseInstance)){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "Permission denied."));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				PlanDescriptor descriptor = dh.getPlanningDAO().get(em, planID);
				PlanningStorage storage = null;
				if(descriptor != null) storage = descriptor.getPlanData();
				
				if(descriptor==null || storage==null){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "Unable to get requested plan. (Error 1)"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				if(storage.getData()==null || storage.getData().length()==0){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "No data for requested plan. (Error 2)"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//Ako ID plana ne odgovara zadanom kolegiju
				if(courseInstanceID != descriptor.getCourseInstance().getId()){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "Requested plan doesn't belong to the provided course instance."));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//Finally return the requested data
				wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("SUCCESS", storage.getData()));
				return null;
			}
		});
	}
	
	protected static InputStreamWrapper createInputStreamWrapperFromText(String param) {
		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + param + "</result>";
		try {
			byte[] buf = text.getBytes("UTF-8");
			return new InputStreamWrapper(new ByteArrayInputStream(buf), "result", buf.length, "text/xml; charset=utf-8");
		} catch(Exception ex) {
			byte[] buf = "Encoding error. Could not generate original message.".getBytes();
			return new InputStreamWrapper(new ByteArrayInputStream(buf), "result", buf.length, "text/plain; charset=utf-8");
		}
	}
	
	/**
	 * Dohvat statusa plana
	 * @param courseInstanceID
	 * @param userID
	 * @param planID
	 * @param wrapper
	 */
	public static void getPlanStatus(final Long planID, final InputStreamWrapper[] wrapper) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				PlanDescriptor desc = dh.getPlanningDAO().get(em, planID);
				String result ="INVALID_ID";
				if(desc!=null) result = desc.getStatus().toString();
				//Korisniku se vraca ID plana
				wrapper[0] = createInputStreamWrapperFromText(result);
				return null;
			}
		});
	}

	/**
	 * Pohrana plana i priprema podataka za izradu
	 * @param courseInstanceID
	 * @param userID
	 * @param wrapper
	 * 
	 * Korisniku se vraća XML koji sadrži sljedeće tagove:
	 * 1.) result>SUCCESS</result> ili <result>FAILURE</result>
	 * 2.) <message>_content_</message>
	 */
	public static void savePlan(final PlanningData data, final String courseInstanceID, final Long userID, final String planData, 
			final InputStreamWrapper[] wrapper) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				PlanningDAO pdao = dh.getPlanningDAO();
				User u = dh.getUserDAO().getUserById(em, userID);
				data.setCurrentUser(u);
				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(ci);
				JCMSSecurityManagerFactory.getManager().init(u, em);
				
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(ci)){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "Security exception. You are not allowed to use Planning service on this course."));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				//Priprema za XML
				final Node planNode = loadXML(planData);
				if(planNode==null){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "Cannot parse plan."));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//Pohrana podataka osnovnog plana
				PlanningStorage basicPlan = new PlanningStorage(planData);
				pdao.savePlanningData(em, basicPlan);

				//Pohrana osnovnog plana
				final PlanDescriptor newPlan = new PlanDescriptor();
				
				Element planElement = (Element)planNode;
				newPlan.setName(planElement.getAttribute("name"));
				newPlan.setPlanData(basicPlan);
				newPlan.setOwner(u);
				newPlan.setStatus(PlanStatus.NEW);
				newPlan.setCreationDate(new Date());
				newPlan.setCourseInstance(ci);
				newPlan.setParameters("");
				pdao.savePlan(em, newPlan);
				
				//Priprema plana
				boolean isUpdate = false;
				preparePlan(em, courseInstanceID, newPlan, data, isUpdate);
				
				if(newPlan.getStatus()!=PlanStatus.PREPARED){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", Long.toString(newPlan.getId())));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//Korisniku se vraca ID plana
				wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("SUCCESS", Long.toString(newPlan.getId())));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	/**
	 * Pohrana izmjena plana
	 * @param courseInstanceID
	 * @param userID
	 * @param planID
	 * @param wrapper
	 * 
	 * Korisniku se vraća XML koji sadrži sljedeće tagove:
	 * 1.) result>SUCCESS</result> ili <result>FAILURE</result>
	 * 2.) <message>_content_</message>
	 */
	public static void updatePlan(final PlanningData data, final String courseInstanceID, final Long userID, final String planData, 
			final Long planID, final InputStreamWrapper[] wrapper) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				PlanningDAO pdao = dh.getPlanningDAO();
				User u = dh.getUserDAO().getUserById(em, userID);
				data.setCurrentUser(u);
				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(ci);
				JCMSSecurityManagerFactory.getManager().init(u, em);
				
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(ci)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				//Priprema za XML
				final Node planNode = loadXML(planData);
				if(planNode==null){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "Cannot parse plan."));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				PlanDescriptor descriptor = pdao.get(em, planID);
				if(descriptor==null){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "No plan descriptor for given ID."));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				PlanningStorage storedPlanData = descriptor.getPlanData();
				if(storedPlanData==null){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "No plan data for given ID."));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//Jednostavna usporedba
				if(storedPlanData.getData().equals(planData)){
					System.out.println("[Planning] Updated plan data is equal to old plan data.");
				}else{
					System.out.println("[Planning] Updated plan data is different from old plan data.");
				}
				
				Plan p = new Plan(planNode);
				
				//Pohrana plana
				storedPlanData.setData(planData);
				
				//Pohrana naziva plana
				descriptor.setName(p.getName());
				
				//Priprema plana
				boolean isUpdate = true;
				preparePlan(em, courseInstanceID, descriptor, data, isUpdate);
				
				if(descriptor.getStatus()!=PlanStatus.PREPARED){
					wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("FAILURE", "Error preparing plan."));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//Korisniku se vraca ID plana
				wrapper[0] = createInputStreamWrapperFromText(prepareResultXML("SUCCESS", Long.toString(descriptor.getId())));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	private static String prepareResultXML(String result, String message){
		StringBuilder sb = new StringBuilder();
		sb.append("<planresult>");
		sb.append(result);
		sb.append("</planresult>");
		sb.append("<message>");
		sb.append(message);
		sb.append("</message>");
		return sb.toString();
	}
	
	/**
	 * Priprema plana za izradu - eksplicitno pokretanje s weba
	 * @param courseInstanceID
	 * @param planID
	 */
	public static void preparePlanExt(final PlanningData data, final String courseInstanceID, final Long userID, 
			final Long planID, final InputStreamWrapper[] wrapper){
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>(){
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				data.setCurrentUser(currentUser);
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				CourseInstance courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(courseInstance);
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(courseInstance)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				boolean isUpdate = false;

				PlanDescriptor planDesc = dh.getPlanningDAO().get(em, planID);
				
				preparePlan(em, courseInstanceID, planDesc, data, isUpdate);
				
				//Korisniku se vraca status plana 
				String result;
				if(planDesc.getStatus().equals(PlanStatus.PREPARED)) result = "SUCCESS";
				else result = "FAILURE";
				
				wrapper[0] = createInputStreamWrapperFromText(prepareResultXML(result, planDesc.getStatus().toString()));
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	
	/**
	 * Priprema plana za izradu - automatsko pokretanje nakon pohrane
	 * @param plan
	 * @throws ReservationException 
	 */
	public static void preparePlan(final EntityManager em, final String courseInstanceID, final PlanDescriptor planDesc, final PlanningData data, boolean isUpdate){

		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		//Pokretanje pripreme
		planDesc.setStatus(PlanStatus.PREPARING);
		dh.getPlanningDAO().savePlan(em, planDesc);
		
		try{
			PlanningStorage planData = planDesc.getPlanData();
			Node planNode = loadXML(planData.getData());
			
			//Rekreiranje plana iz XML oblika
			Plan plan = new Plan(planNode);
			
			//Postavljanje ID-a plana
			plan.setId(Long.toString(planDesc.getId()));

			//Kreiranje identifikatora evenata i termina(segmenata)
			int eventNumber = 1;
			for(PlanEvent pe : plan.getEvents()){
				pe.setId("E"+Long.toString(planDesc.getId()) + "." + eventNumber);
				int termNumber = 1;
				for(PlanEventSegment pes : pe.getSegments()){
					pes.setId("T"+Long.toString(planDesc.getId()) + "." + eventNumber + "." + termNumber);
					termNumber++;
				}
				eventNumber++;
			}
			planData.setData(plan.toXMLString());
			
			preparePlanDefinitions(plan);
			
			StringBuilder sbPeople = new StringBuilder();
			sbPeople.append("<people>");
			StringBuilder sbTerms = new StringBuilder();
			sbTerms.append("<terms>");
			
			for(PlanEvent event : plan.getEvents()){
				
				if(event.getDistribution().getType()==Definition.RANDOM_DISTRIBUTION){
					System.out.println("\n[Planning] Preparing event, name:"+event.getName() + " id:" + event.getId() + " (random_distribution)");	
					//1. Priprema podataka o zauzecima studenata
					prepareEntityData("STUDENT", event, em, courseInstanceID, sbPeople, data);
	
					//2. Priprema podataka o slobodnim terminima dvorana
					prepareEntityData("TERM", event, em, courseInstanceID, sbTerms, data);
					
				}else if(event.getDistribution().getType()==Definition.GIVEN_DISTRIBUTION){
					System.out.println("\n[Planning] Preparing event, name:"+event.getName() + " id:" + event.getId() + " (given_distribution)");
					
					//1. Priprema podataka o zauzecima studenata

					if(event.hasDefinedParameters(Definition.PEOPLE_DEF)){
						prepareEntityData("STUDENT", event, em, courseInstanceID, sbPeople, data);
					}
					else
					{
						sbPeople.append("<"+event.getId()+">");
						for(PlanEventSegment pes : event.getSegments()){
							System.out.println("\n[Planning] Preparing term, name:"+pes.getName() + " id:" + pes.getId());
							prepareEntityData("STUDENT", pes, em, courseInstanceID, sbPeople, data);
						}
						sbPeople.append("</"+event.getId()+">");
					}
					
					//2. Priprema podataka o slobodnim terminima dvorana
					
					if(event.hasDefinedParameters(Definition.LOCATION_DEF)){
						prepareEntityData("TERM", event, em, courseInstanceID, sbTerms, data);
					}
					else
					{
						sbTerms.append("<"+event.getId()+">");
						for(PlanEventSegment pes : event.getSegments()){
							System.out.println("\n[Planning] Preparing term, name:"+pes.getName() + " id:" + pes.getId());
							prepareEntityData("TERM", pes, em, courseInstanceID, sbTerms, data);
						}
						sbTerms.append("</"+event.getId()+">");
					}
				}
			}
			
			sbPeople.append("</people>");
			PlanningStorage peopleData = planDesc.getPeopleData();
			if(peopleData==null) peopleData = new PlanningStorage(sbPeople.toString());
			else peopleData.setData(sbPeople.toString());
			planDesc.setPeopleData(peopleData);
			
			sbTerms.append("</terms>");
			PlanningStorage termData = planDesc.getTermData();
			if(termData==null) termData = new PlanningStorage(sbTerms.toString());
			else termData.setData(sbTerms.toString());
			planDesc.setTermData(termData);
			
			planDesc.setStatus(PlanStatus.PREPARED);
			
		}catch(ReservationException e){
			e.printStackTrace();
			planDesc.setStatus(PlanStatus.PREPARATION_ERROR);
		}catch(Exception e){
			e.printStackTrace();
			planDesc.setStatus(PlanStatus.PREPARATION_ERROR);
		}finally{
			dh.getPlanningDAO().savePlan(em, planDesc);
		}
	}
	
	private static void preparePlanDefinitions(Plan plan){
		//Kontrolni ispis definicija prije podesavanja
		plan.printDefinitionSummary();
		
		//Spustanje definicija iz plana (ako ih ima) u dogadaje
		for(int i : Definition.paramTypes){
			for(PlanEvent pe : plan.getEvents()) {
				plan.getDefinition().copyDefinitionsToEntity(i, pe);
			}
		}
		
		//Kontrolni ispis definicija
		System.out.println(" ");
		plan.printDefinitionSummary();
		
		//Priprema definicija za svaki dogadaj zasebno
		for(PlanEvent pe : plan.getEvents()) {
			prepareEventDefinitions(pe);
		}
		
		//Kontrolni ispis definicija nakon podesavanja
		System.out.println(" ");
		plan.printDefinitionSummary();
	}
	
	private static void prepareEventDefinitions(PlanEvent planEvent){
		//Nema posebnih priprema za RANDOM raspodjelu
		if(planEvent.getDistribution().getType()==Definition.RANDOM_DISTRIBUTION) return;
		
		//Ako je vrijeme u dogadaju, rasprsuje ga se po terminima
		if(planEvent.hasDefinedParameters(Definition.TIME_DEF)){
			for(PlanEventSegment pes : planEvent.getSegments()){
				planEvent.getDefinition().copyDefinitionsToEntity(Definition.TIME_DEF, pes);
			}
		} 
		else {  //Ako je vrijeme u terminima, vraca ga se u dogadaj
			for(PlanEventSegment pes : planEvent.getSegments()){
				pes.getDefinition().copyDefinitionsToEntity(Definition.TIME_DEF, planEvent);
			}
		}
		
	}

	
	private static void prepareEntityData(String param, PlanningEntity entity, EntityManager em, String courseInstanceID, StringBuilder sb, final PlanningData data ) throws ReservationException{
		String id="";
		if(entity instanceof PlanEvent) id = ((PlanEvent)entity).getId();
		else id = ((PlanEventSegment)entity).getId();
		System.out.println("[Planning] Preparing "+param+" DATA");
		sb.append("<"+id+">");
		if(param.equals("STUDENT")) prepareStudentData(em, courseInstanceID, entity, sb);
		else if(param.equals("TERM")) prepareTermData(em, courseInstanceID, entity, sb, data);
		sb.append("</"+id+">");
	}
	
	
	private static void prepareStudentData(EntityManager em, String courseInstanceID, PlanningEntity entity, StringBuilder sb) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Set<User> userSet = new HashSet<User>();
		List<User> userList = new ArrayList<User>();
		
		//Priprema liste jmbagova svih odabranih studenata
		List<IDefinitionParameter> peopleParams = entity.getParameters(Definition.PEOPLE_DEF);
		for(IDefinitionParameter s : peopleParams){
			if(s instanceof PeopleParameter){
				PeopleParameter pp = (PeopleParameter)s;
				if(pp.isGroup()){
					List<User> groupUsers = dh.getGroupDAO().listUsersInGroupTree(em, courseInstanceID, pp.getGroupRelativePath());
					for(User u : groupUsers) userSet.add(u);
				}
				else {
					User u = dh.getUserDAO().getUserByJMBAG(em, pp.getJmbag());
					//Ako student ne postoji. Moze se desiti kod dodavanja studenata preko liste jmbagova.
					if(u==null) throw new IllegalParameterException("Student with JMBAG " + pp.getJmbag() + " does not exist.");
					userSet.add(u);
				}
			}
		}
		userList.addAll(userSet);
		
		
		System.out.println("[Planning] Student list created.");
		
		//Prikupljanje podataka o zauzecima za sve periode iz definicije
		Map<User, TemporalList> finalResults = new HashMap<User, TemporalList>();
		List<DateSpan> periods = entity.getTimeParameters();
		for(DateSpan span : periods){
			System.out.println("[Planning] Fetching data for date span: "  + span.toString());
			//Dohvat zauzeca
			BasicResult res = new BasicResult();
			ScheduleAnalyzerService.analyze(em, res, userList, span.getStartDate(), span.getEndDate());
			
			System.out.println("[Planning] Updating data with analysis results.");
			for(Map.Entry<User, TemporalList> entry : res.busyMap.entrySet()){
				
				TemporalList existingList = finalResults.get(entry.getKey());
				if(existingList==null) existingList = new TemporalList(res.timeSpanCache);

				//Ekstrakcija novih intervala
				for(Map.Entry<DateStamp, TL> entry2 : entry.getValue().getMap().entrySet()){
					DateStamp dateStamp = entry2.getKey();
					TemporalNode node = entry2.getValue().first;
					while(node!=null){
						existingList.addInterval(dateStamp, node.getTimeSpan(), null);
						node=node.getNext();
					}
				}
				
				finalResults.put(entry.getKey(), existingList);
				// System.out.println(existingList.toString());
			}
		}

		//Priprema zapisa
		String jmbagCache="";
		String dateCache="";
		
		for(Map.Entry<User, TemporalList> entry : finalResults.entrySet()) {
			User user = entry.getKey(); 
			String newJmbag = user.getJmbag();
			if(!newJmbag.equals(jmbagCache)){
				if(!jmbagCache.equals("")) sb.append("\n");
				sb.append(newJmbag);
				jmbagCache=newJmbag;
				dateCache="";
			}
			TemporalList tl = entry.getValue();
			Map<DateStamp,TemporalList.TL> byDays = tl.getMap();
			List<Entry<DateStamp,TemporalList.TL>> podaci = new ArrayList<Entry<DateStamp,TemporalList.TL>>(byDays.entrySet());
			Collections.sort(podaci, new Comparator<Entry<DateStamp,TemporalList.TL>>() {
				@Override
				public int compare(Entry<DateStamp, TL> o1, Entry<DateStamp, TL> o2) {
					return o1.getKey().compareTo(o2.getKey());
				}
			});
		
			for(Map.Entry<DateStamp,TemporalList.TL> en2 : podaci) {
				TemporalNode n = en2.getValue().first;
				while(n != null) {

					String newDate = en2.getKey().getStamp();
					if(!newDate.equals(dateCache)){
						sb.append("#");
						sb.append(newDate);
						dateCache=newDate;
					}
					sb.append("$");
					sb.append(n.getTimeSpan().getStart().toString());
					sb.append("-");
					sb.append(n.getTimeSpan().getEnd().toString());
					n = n.getNext();
				}
			}						
		}
	}
	
	private static void prepareTermData(EntityManager em, String courseInstanceID, PlanningEntity entity, StringBuilder sb, final PlanningData data) throws ReservationException {
		
		SimpleDateFormat sdf = new SimpleDateFormat(Definition.DATE_FORMAT);
//		DummyReservationManagerFactory factory = new DummyReservationManagerFactory();
		IReservationManagerFactory factory = ReservationManagerFactory.getFactory("FER");
		
		User cu = data.getCurrentUser();
		IReservationManager manager = factory.getInstance(cu.getId(), cu.getJmbag(), cu.getUsername());
		
		List<RoomParameter> rooms = entity.getRoomParameters();
		List<DateSpan> periods = entity.getTimeParameters();
		Map<RoomParameter, List<RoomReservationPeriod>> finalResults = new HashMap<RoomParameter, List<RoomReservationPeriod>>();
	
		Iterator<RoomParameter> i = rooms.iterator();
		while(i.hasNext()){
			RoomParameter room = i.next();
			Room r = DAOHelperFactory.getDAOHelper().getRoomDAO().get(em, room.getId());
			for(DateSpan ds : periods){
				List<RoomReservationPeriod> terms = manager.findAvailableRoomPeriods(r.getShortName(), sdf.format(ds.getStartDate()) ,  sdf.format(ds.getEndDate()));
				List<RoomReservationPeriod> existingTerms = finalResults.get(room);
				if(existingTerms==null) existingTerms=new ArrayList<RoomReservationPeriod>();
				existingTerms.addAll(terms);
				finalResults.put(room, existingTerms);
			}
		}
		
		RoomParameter roomCache = null;
		String dateCache = "";
		for(Map.Entry<RoomParameter, List<RoomReservationPeriod>> entry : finalResults.entrySet()){
			
			if(!entry.getKey().equals(roomCache)){
				if(roomCache!=null) sb.append("\n");
				sb.append(entry.getKey().toString());
				roomCache=entry.getKey();
				dateCache="";
			}
			
			for(RoomReservationPeriod reservationPeriod : entry.getValue()){
				if(!reservationPeriod.getDate().equals(dateCache)){
					sb.append("#");
					sb.append(reservationPeriod.getDate());
					dateCache=reservationPeriod.getDate();
				}
				sb.append("$");
				sb.append(reservationPeriod.getTimeSpanString());
			}
		}
	}
	
	private static Node loadXML(String planData){
		Node result = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(planData.getBytes("UTF-8"));
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document doc;
			doc = builder.parse(bis);
			Node rootNode = doc.getFirstChild(); 
			result = rootNode;
		} catch (UnsupportedEncodingException ignored) {
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			result=null;
		} catch (SAXException e) {
			e.printStackTrace();
			result=null;
		} catch (IOException e) {
			e.printStackTrace();
			result=null;
		} catch(Exception e){
			e.printStackTrace();
			result=null;
		}
		return result;
	}


	public static void getLocalScheduler(final PlanningData data, final String courseInstanceID, final Long userID, final Long planID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>(){
			public Void executeOperation(EntityManager em) {

				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				User u = dh.getUserDAO().getUserById(em, userID);
				
				JCMSSecurityManagerFactory.getManager().init(u, em);
				CourseInstance courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(courseInstance);
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(courseInstance)){
		    		data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Planning.noServiceUsagePermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				PlanDescriptor planDesc = dh.getPlanningDAO().get(em, planID);
				
				if(!planDesc.getStatus().equals(PlanStatus.PREPARED)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Planning.localSchedulerUnavailableForUnPreparedPlans"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				JarOutputStream jar = null;
				File tmpFile = null;
				Writer w = null;
				
				try {
					tmpFile = File.createTempFile("SCH", null);
					jar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)));
					
					//Podaci o planu - potrebni zbog identifikacije distribucije događaja
					PlanningStorage ps = planDesc.getPlanData();
					Node pn = loadXML(ps.getData());
					Plan p = new Plan(pn); 
					
					
					//Podaci o studentima -> PlanDescriptor.peopleData
					PlanningStorage peopleDataStorage = planDesc.getPeopleData();
					Node peopleDataNode = loadXML(peopleDataStorage.getData());
					NodeList eventNodes = peopleDataNode.getChildNodes();
					for(int i = 0; i<eventNodes.getLength(); i++){
						Node n = eventNodes.item(i);
						if(p.getEventByID(n.getNodeName()).getDistribution().getType()==Definition.RANDOM_DISTRIBUTION){
							saveEntry(n.getNodeName(), n.getTextContent(), "_peopleData.csv", jar);
						}else{
							PlanEvent pe = p.getEventByID(n.getNodeName());
							boolean peopleDefinitionAtEventLevel = true;
							
							//Cilj: ustanoviti gdje su definirani studenti, na razini događaja ili termina
							//Metoda: ide se gledati postoje li termini te jesu li u prvom dohvacenom terminu defirani studenti
							//		  Ako jesu tada su studenti sigurno definirani na razini termina. (Dovoljno je gledati samo jedan termin)
							for(PlanEventSegment pes : pe.getSegments()){
								if(pes.hasDefinedParameters(Definition.PEOPLE_DEF)){
									peopleDefinitionAtEventLevel = false;
								}
								break;
							}
							
							//Ako su studenti definirani na razini dogadaja
							if(peopleDefinitionAtEventLevel){
								saveEntry(n.getNodeName(), n.getTextContent(), "_peopleData.csv", jar);
							}
							//Ako su studenti definirani na razini termina
							else{
								NodeList children = n.getChildNodes();
								for(int j =0; j<children.getLength();j++){
									Node termNode = children.item(j);
									saveEntry(termNode.getNodeName(), termNode.getTextContent(), "_peopleData.csv", jar);
								}
							}
						}
					}
					
					//Podaci o slobodnim periodima prostorija/termini -> PlanDescriptor.termData
					PlanningStorage termDataStorage = planDesc.getTermData();
					Node termDataNode = loadXML(termDataStorage.getData());
					NodeList termEventNodes = termDataNode.getChildNodes();
					for(int i = 0; i<termEventNodes.getLength(); i++){
						Node n = termEventNodes.item(i);
						if(p.getEventByID(n.getNodeName()).getDistribution().getType()==Definition.RANDOM_DISTRIBUTION){
							saveEntry(n.getNodeName(), n.getTextContent(), "_termData.csv", jar);
						}else{
							PlanEvent pe = p.getEventByID(n.getNodeName());
							boolean roomDefinitionAtEventLevel = true;
							
							//Cilj: ustanoviti gdje su definirane prostorije, na razini događaja ili termina
							//Metoda: ide se gledati postoje li termini te jesu li u prvom dohvacenom terminu defirane prostorije.
							//		  Ako jesu tada su prostorije sigurno definirane na razini termina. (Dovoljno je gledati samo jedan termin)
							for(PlanEventSegment pes : pe.getSegments()){
								if(pes.hasDefinedParameters(Definition.LOCATION_DEF)){
									roomDefinitionAtEventLevel = false;
								}
								break;
							}
							
							//Ako su prostorije definirane na razini dogadaja
							if(roomDefinitionAtEventLevel){
								saveEntry(n.getNodeName(), n.getTextContent(), "_termData.csv", jar);
							}
							//Ako su prostorije definirane na razini termina
							else
							{
								NodeList children = n.getChildNodes();
								for(int j =0; j<children.getLength();j++){
									Node termNode = children.item(j);
									saveEntry(termNode.getNodeName(), termNode.getTextContent(), "_termData.csv", jar);
								}
							}
						}
					}
					
					//Podaci o rasporedu
					PlanningStorage planDataStorage = planDesc.getPlanData();
					String planData = planDataStorage.getData();
					jar.putNextEntry(new ZipEntry("planData.xml"));
					w = new OutputStreamWriter(jar, "UTF-8");
					w.write(planData);
					w.flush();
					jar.closeEntry();	
					
					
					//Manifest
					jar.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
					w = new OutputStreamWriter(jar, "UTF-8");
					w.write("Manifest-Version: 1.0\n");
					w.write("Created-By: 1.6.0_14 (Sun Microsystems Inc.)\n");
					w.write("Main-Class: hr.fer.zemris.util.scheduling.LocalStarter\n\n");
					w.flush();
					jar.closeEntry();	
					
					//Lista algoritama
					jar.putNextEntry(new JarEntry("algorithms.data"));
					w = new OutputStreamWriter(jar, "UTF-8");
					File classFolder = new File(getPackageLocationOnDisk("hr.fer.zemris.util.scheduling.algorithms"));
					
					String[] files = getAllAlgorithms(classFolder);
					// String[] files = classFolder.list();
					for(String fileName : files) {
						w.write("hr.fer.zemris.util.scheduling.algorithms."+fileName+"\n");
					}
					w.flush();
					jar.closeEntry();	
					
					//Model podataka
					saveClassFiles("hr.fer.zemris.jcms.model.planning", jar);
					
					//Local starter + algoritamske datoteke + ostala podrska
					saveClassFiles("hr.fer.zemris.util.scheduling", jar);
					
					//Time utility
					saveClassFiles("hr.fer.zemris.util.time", jar);
					
					//Zbog IllegalParameterException
					saveClassFiles("hr.fer.zemris.jcms.exceptions", jar);
					
					//jfreechart library
					saveClassFiles("org.jfree", jar);
					
					jar.close();
					
					DeleteOnCloseFileInputStream docis = new DeleteOnCloseFileInputStream(tmpFile);
					
					String jarName = "GeneratorRasporeda_" + planDesc.getName().replaceAll(" ", "_") + ".jar";
					
					docis.setFileName(jarName);
					docis.setMimeType("application/java-archive");
					data.setStream(docis);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					
					
				} catch (IOException e) {
					e.printStackTrace();
					data.getMessageLogger().addErrorMessage("Greška prilikom izrade JAR datoteke.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				} finally {
					if(jar!=null) {
						try { jar.close(); } catch(Exception ignorable) {}
					}
				}
				return null;
			}


		});
	}
	
	/**
	 * 
	 * Pomoćna metoda koja u navedenom direktoriju i svim poddirektorijima traži valjana
	 * imena algoritama raspoređivanja. Vraća listu relativnih imena razreda. Primjerice,
	 * ako je u poddirektoriju <code>abc</code> pronađen algoritam <code>FastAlgorithm</code>,
	 * u <code>cde</code> pronađen <code>TurboAlgorithm</code> te u vršnom direktoriju 
	 * pronađen <code>BasicScheduler</code>, rezultat će biti polje:<br>
	 * <code>{"abc.FastAlgorithm","cde.TurboAlgorithm","BasicScheduler"}</code>.<br>
	 * Napomena: prihvatljiva imena algoritama definirana su metodom {@link #isAcceptableAlgorithmName(String)}.
	 * 
	 * @param classFolder direktorij iz kojeg kreće pretraga, i koji je u tom smislu vršni
	 * @return polje pronađenih algoritama; može biti veličine 0, ali sigurno neće biti <code>null</code>
	 */
	private static String[] getAllAlgorithms(File classFolder) {
		Set<String> algoNames = new HashSet<String>();
		getAllAlgorithmsRecursive(classFolder, "", algoNames);
		List<String> list = new ArrayList<String>(algoNames);
		Collections.sort(list);
		String[] result = new String[list.size()];
		list.toArray(result);
		return result;
	}

	/**
	 * Pomoćna metoda koja obavlja rekurzivnu pretragu i poziva se od drugih metoda.
	 * Ne pozivati ovu metodu direktno!
	 * 
	 * @param classFolder trenutni vršni direktorij
	 * @param parent aktualni relativni roditelj, ili prazan string ako nema roditelja; ne smije biti <code>null</code>
	 * @param algoNames aktualni skup do sada pronađenih relativnih imena algoritama; novopronađeni algoritmi također se tu dodaju
	 */
	private static void getAllAlgorithmsRecursive(File classFolder, String parent, Set<String> algoNames) {
		if(classFolder==null) return;
		File[] list = classFolder.listFiles();
		if(list==null || list.length==0) return;
		for(File file : list) {
			String fileName = file.getName();
			// Ako je direktorij, obradi ga rekurzivno
			if(file.isDirectory()) {
				String newParent = parent.isEmpty() ? fileName : parent + "." + fileName;
				getAllAlgorithmsRecursive(file, newParent, algoNames);
				continue;
			}
			// Ako nije obicna datoteka, preskoci
			if(!file.isFile()) continue;
			// Ako je to unutarnji razred, preskoci; algoritam mora biti vrsni razred
			if(fileName.indexOf('$')!=-1) continue;
			// Inače imamo kandidata za algoritam.
			int dot = fileName.lastIndexOf(".");
			if(dot == -1) continue;
			fileName = fileName.substring(0, dot);
			if(!isAcceptableAlgorithmName(fileName)) continue;
			if(!parent.isEmpty()) {
				fileName = parent + "." + fileName;
			}
			algoNames.add(fileName);
		}
	}

	/**
	 * Testira je li ime razreda prihvatljivo kao ime algoritma rasporedivanja. Algoritmi rasporedivanja
	 * moraju biti nazvani po formatu <code>XYZScheduler</code> ili <code>XYZAlgorithm</code>, pa se to 
	 * provjerava (prefiks <code>XYZ</code> može biti proizvoljne duljine, uključivo i 0).
	 * 
	 * @param algoName ime razreda koji se provjerava
	 * @return <code>true</code> ako je ime prihvatljivo, <code>false</code> ako nije
	 */
	private static boolean isAcceptableAlgorithmName(String algoName) {
		if(algoName==null || algoName.isEmpty()) return false;
		return (algoName.endsWith("Scheduler") || algoName.endsWith("Algorithm"));
	}
	
	private static void saveEntry(String name, String content, String fileName, JarOutputStream jar) throws IOException{
		jar.putNextEntry(new JarEntry(name + fileName));
		Writer w = new OutputStreamWriter(jar, "UTF-8");
		w.write(content);
		w.flush();
		jar.closeEntry();
	}
	private static void saveClassFiles(String classPackage, JarOutputStream jar) throws IOException{
		File classFolder = new File(getPackageLocationOnDisk(classPackage));
		String packageForJar = classPackage.replaceAll("\\.", "/");
		File[] files = classFolder.listFiles();
		for(File f : files) {
			if(f.isFile()){
				jar.putNextEntry(new JarEntry(packageForJar + "/" + f.getName()));
				InputStream is = new BufferedInputStream(new FileInputStream(f));
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf, 0, buf.length)) != -1) {
				    jar.write(buf, 0, len);
				}
				is.close();
				jar.closeEntry();
			}else if(f.isDirectory()){
				saveClassFiles(classPackage + "." + f.getName(), jar);
			}
		}
	}

	public static void uploadSchedule(final PlanningData data, final File schedule, final Long userID, final String courseInstanceID, final String planID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>(){
			public Void executeOperation(EntityManager em) {

				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(schedule)));
					String line="";
					StringBuilder scheduleXML = new StringBuilder();
					while(line!=null){
							scheduleXML.append(line);
							line = br.readLine();
					}
					
					Node resultRootNode = loadXML(scheduleXML.toString());
					Element algorithmElement = (Element)resultRootNode.getFirstChild();
					String algorithmClass=algorithmElement.getAttribute("className");
					Node scheduleRoot = resultRootNode.getLastChild();
					Element rootElement = (Element)resultRootNode;
					Long originatedID = null;
					if(rootElement!=null) {
						String s = rootElement.getAttribute("planid");
						if(s!=null) originatedID = new Long(s);
					}
					Plan schedule = new Plan(scheduleRoot);
					
					DAOHelper dh = DAOHelperFactory.getDAOHelper();
					PlanningDAO pdao = dh.getPlanningDAO();
					
					PlanDescriptor planDesc = pdao.get(em, Long.parseLong(planID));
					
					//Identifikator plana kojem pripada raspored (pročita se iz izlazne XML datoteke iz generatora)
					//mora odgovarati identifikatoru plana kojem se raspored pridodaje
					if(!planDesc.getId().equals(originatedID)){
						data.getMessageLogger().addErrorMessage("Raspored koji pokušavate pohraniti (" + schedule.getName() + ") ne pripada odabranom planu (" + planDesc.getName() + ") !");                       
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					
					//Pohrana rasporeda
					PlanningStorage scheduleData = new PlanningStorage(schedule.toXMLString());
					pdao.savePlanningData(em, scheduleData);
					
					ScheduleDescriptor scheduleDesc = new ScheduleDescriptor();
					scheduleDesc.setData(scheduleData);
					scheduleDesc.setCreationDate(new Date());
					scheduleDesc.setParameters("algorithm="+algorithmClass);
					scheduleDesc.setParent(planDesc);
					pdao.saveSchedule(em, scheduleDesc);
					
					List<ScheduleDescriptor> schedules = planDesc.getSchedules();
					if(schedules==null) {
						schedules = new ArrayList<ScheduleDescriptor>();
						planDesc.setSchedules(schedules);
						pdao.savePlan(em, planDesc);
					}
					schedules.add(scheduleDesc);
					
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					data.getMessageLogger().addInfoMessage("Raspored je uspješno pohranjen!");
					return null;
					
				} catch (FileNotFoundException e) {
					data.getMessageLogger().addErrorMessage("Greška kod učitavanja rasporeda.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage("Greška kod učitavanja rasporeda.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
			}
		});
	}
	
	public static void getSchedule(final PlanningData data, 
			final Long userID, final String courseInstanceID, final String scheduleID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>(){
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				PlanningDAO pdao = dh.getPlanningDAO();
				User u = dh.getUserDAO().getUserById(em, userID);
				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(ci);
				
				JCMSSecurityManagerFactory.getManager().init(u, em);
				data.setCourseInstance(ci);
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(ci)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				ScheduleDescriptor scheduleDesc = pdao.getSchedule(em, Long.parseLong(scheduleID));
				if(scheduleDesc==null){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
								
 				PlanningStorage scheduleData = scheduleDesc.getData();
 				ISchedule schedule = new Plan(loadXML(scheduleData.getData()));
				
 				//Priprema dogadaja i termina
				ScheduleBean sbean = new ScheduleBean();
				data.setScheduleBean(sbean);  
				
				sbean.setName(schedule.getName());
				List<ScheduleEventBean> ebeans = new ArrayList<ScheduleEventBean>();
				for(IScheduleEvent ise : schedule.getScheduleEvents()){
					ScheduleEventBean ebean = new ScheduleEventBean();
					ebean.setName(ise.getName());
					ebean.setId(ise.getId());
					List<ScheduleTermBean> tbeans = new ArrayList<ScheduleTermBean>();
					for(IScheduleTerm ist : ise.getScheduleEventTerms()){
						ScheduleTermBean tbean = new ScheduleTermBean();
						tbean.setId(ist.getId());
						tbean.setName(ist.getTermName());
						tbean.setNumberOfStudents(ist.getStudents().size());
						tbean.setRoomName(ist.getRoom().getName() + " (" + ist.getRoom().getCapacity()+")");
						tbean.setTermStart(ist.getDate().toString()+" "+ist.getTermSpan().getStart().toString());
						tbean.setTermEnd(ist.getDate().toString()+" "+ist.getTermSpan().getEnd().toString());
						tbean.setJmbags(ist.getStudents());
						StringBuilder sb = new StringBuilder();
						for(String jmbag : ist.getStudents()) sb.append(jmbag+", ");
						sb.deleteCharAt(sb.length()-1);
						tbean.setStudents(sb.toString());
						
						tbeans.add(tbean);
					}
					ebean.setTermBeans(tbeans);
					ebeans.add(ebean);
				}
				sbean.setEventBeans(ebeans);
				
				List<CourseComponentBean> componentBeans = new ArrayList<CourseComponentBean>();
//				Priprema opisnika komponenti
				
				//1- dohvat svih descriptora u sustavu
//				List<CourseComponentDescriptor> componentDescriptors = dh.getCourseComponentDAO().listDescriptors(em);
//				for(CourseComponentDescriptor ccd : componentDescriptors) componentBeans.add(new CourseComponentBean(ccd.getId(), ccd.getPositionalName()));
				
				//2- Dohvat samo komponenti koje postoje na kolegiju
				for(CourseComponent cc : dh.getCourseComponentDAO().listComponentsOnCourse(em, ci)){
					componentBeans.add(new CourseComponentBean(cc.getDescriptor().getId(), cc.getDescriptor().getPositionalName()));
				}
				data.setComponents(componentBeans);
				
				List<String> preparedNumbers = new ArrayList<String>();
				for(Integer i = 1; i<=100; i++) preparedNumbers.add(i.toString());
				data.setNumbers(preparedNumbers);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void checkScheduleValidity(final PlanningData data, final Long userID, 
			final String courseInstanceID, final String scheduleID, final boolean publishImmediately) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>(){
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				PlanningDAO pdao = dh.getPlanningDAO();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				data.setCurrentUser(currentUser);
				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				data.setCourseInstance(ci);
				
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				data.setCourseInstance(ci);
				if(!JCMSSecurityManagerFactory.getManager().canUsePlanningService(ci)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				ScheduleDescriptor scheduleDesc = pdao.getSchedule(em, Long.parseLong(scheduleID));
				if(scheduleDesc==null){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
								
				System.out.println("\n[SchedulePublication] Began checking schedule validity. Immediate publication="+publishImmediately);
				PlanningStorage scheduleData = scheduleDesc.getData();
				Node scheduleXML = loadXML(scheduleData.getData());
				ISchedule schedule = new Plan(scheduleXML);
				
				//Ovi podatke se gleda samo u prvom koraku kada može doći do izmjene naziva termina
				
				if(!publishImmediately){
					//U raspored pohraniti eventualne izmjene naziva termina i naziva pridjeljenih dogadaja
					ScheduleBean sbean = data.getScheduleBean();
					for(ScheduleEventBean ebean : sbean.getEventBeans()){
						IScheduleEvent event = schedule.getEventForId(ebean.getId());
						event.setPublicationGroupType(ebean.getGroupType());
						event.setComponentDescriptorId(ebean.getComponentID());
						event.setEventComponentNumber(ebean.getSelectedNumber());
						event.setPrivateGroupName(ebean.getPrivateGroupName());
						for(ScheduleTermBean tbean : ebean.getTermBeans()){
							IScheduleTerm term = event.getTermForId(tbean.getId());
							term.setOverridenEventName(tbean.getEventName());
							term.overrideTermName(tbean.getName());
						}
					}
					scheduleData.setData(schedule.toXMLString());
					System.out.println("[SchedulePublication] Schedule with collected data saved.");
					
				}
				
				IReservationManagerFactory factory = ReservationManagerFactory.getFactory("FER");
				User cu = data.getCurrentUser();
				IReservationManager reservationManager;
			
				try {
					reservationManager = factory.getInstance(cu.getId(), cu.getJmbag(), cu.getUsername());
				} catch (ReservationException re) {
					re.printStackTrace();
					data.getMessageLogger().addErrorMessage("Došlo je do pogreške u komunikaciji sa sustavom za rezervaciju dvorana!");
					data.setScheduleValidationResult(false);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				System.out.println("[SchedulePublication] Reservation manager acquired.");
				System.out.println("[SchedulePublication] Starting student and room availability check.");
				//Provjera jesu li dvorane i studenti još uvijek slobodni
				for(IScheduleEvent event : schedule.getScheduleEvents()){
					
					System.out.println("[SchedulePublication] Checking for event: " + event.getName() + " (" + event.getId() +")");
					for(IScheduleTerm term : event.getScheduleEventTerms()){ 
						//System.out.println("Termin " + term.getTermName() + " " + term.getId());
						//System.out.println("Period termina " + term.getStartDateTime() + " " + term.getEndDateTime());
						
						//Provjera prostorije
						Room r = dh.getRoomDAO().get(em, term.getRoom().getId());
						if(r==null){
							data.getMessageLogger().addErrorMessage("Došlo je do pogreške u komunikaciji s bazom. Nemoguće dohvatiti sobu s ID="+term.getRoom().getId());
							data.setScheduleValidationResult(false);
							data.setResult(AbstractActionData.RESULT_SUCCESS);
							return null;
						}
						RoomReservation roomReservationResult;
						boolean roomReservationFailure = false;
						try {
							roomReservationResult = reservationManager.checkRoom(r.getShortName(), term.getStartDateTime(), term.getEndDateTime(), "schedule check");
						} catch (ReservationException e) {
							data.getScheduleValidationMessages().add("Pogreška kod provjere stanja rezervacije dvorane " + r.getName());
							data.setScheduleValidationResult(false);
							data.setResult(AbstractActionData.RESULT_SUCCESS);
							return null;
						}
						if(!roomReservationResult.getStatus().equals(RoomReservationStatus.FREE)){
							roomReservationFailure=true;
						}
						
						//Provjera studenata
						List<User> userList = new ArrayList<User>();
						for(String jmbag : term.getStudents()) userList.add(dh.getUserDAO().getUserByJMBAG(em, jmbag));
						SimpleDateFormat sdf = new SimpleDateFormat(Definition.DATE_FORMAT);
						Date fromDate=null;
						Date toDate = null;
						try {
							fromDate = sdf.parse(term.getStartDateTime());
							toDate = sdf.parse(term.getEndDateTime());
						} catch (ParseException ignored) {}
						
						BasicResult res = new BasicResult();
						ScheduleAnalyzerService.analyze(em, res, userList, fromDate, toDate);
						int numberOfStudentFailures = 0;
						for(Map.Entry<User, TemporalList> entry : res.busyMap.entrySet()){
							if(entry.getValue()!=null && entry.getValue().getMap().size()>0){
								numberOfStudentFailures++;
							}
						}
						if(numberOfStudentFailures > 0 || roomReservationFailure){
							if(numberOfStudentFailures > 0){
								System.out.println("[SchedulePublication] Availability check aborted. Students no longer available: " + numberOfStudentFailures);
								data.getScheduleValidationMessages().add("Broj studenata koji više nije dostupan: " + numberOfStudentFailures);
							}else{
								System.out.println("[SchedulePublication] All students still available.");
								data.getScheduleValidationMessages().add("Svi studenti su još uvijek dostupni.");
							}
							if(roomReservationFailure){
								System.out.println("[SchedulePublication] Availability check aborted. Room no longer available: " + r.getName());
								data.getScheduleValidationMessages().add("Prostorija " + r.getName() + " više nije slobodna u terminu: " 
										+ term.getStartDateTime() + " do " + term.getEndDateTime());
							}else{
								System.out.println("[SchedulePublication] Room " + r.getName() + " still available from " + term.getStartDateTime() + " till " + term.getEndDateTime());
								data.getScheduleValidationMessages().add("Prostorija " + r.getName() + " je još uvijek slobodna u terminu " 
										+ term.getStartDateTime() + " do " + term.getEndDateTime());
							}
							data.setScheduleValidationResult(false);
							data.setResult(AbstractActionData.RESULT_SUCCESS);
							return null;
						}
					}
				} //Gotova provjera po eventima
				System.out.println("[SchedulePublication] Availability check completed. Students and rooms available.");
				
				if(publishImmediately) {
					data.setScheduleValidationResult(false);
					try{
						publishSchedule(em, schedule, reservationManager, ci, currentUser, scheduleDesc);
						data.getScheduleValidationMessages().add("Raspored uspješno objavljen!");
						
					}catch(SchedulingException e){
						if(e.isPropagate()) throw e; //rollback transakcije
						data.getScheduleValidationMessages().add(e.getMessage());
					}
				}
				else{
					data.getScheduleValidationMessages().add("Provjerene su zauzetosti dvorana i studenata u Vašem rasporedu. Raspored je moguće objaviti! ");
					data.setScheduleValidationResult(true);
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	} 

	private static void publishSchedule(EntityManager em, ISchedule schedule, IReservationManager reservationManager, CourseInstance ci, User currentUser, ScheduleDescriptor scheduleDesc) {
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Map<Room, RoomReservationPeriod> reservedRooms = new HashMap<Room, RoomReservationPeriod>();
		System.out.println("[SchedulePublication] Starting final publication!");
		//TODO: za evente poglewdati dal te root grupe postoje
		//ako ih ima a nema dozvole za brisanje onda prekid
		//ako ih nema ok
		//dodatno: ako market place != null && marketplace.getOpen  -> kraj, otvorena je burza. nema dalje.
		//         eg.getMarketPlace().getOpen();
		System.out.println("[SchedulePublication] Checking marketplace dependencies.");
		for(IScheduleEvent event : schedule.getScheduleEvents()){
			if(event.getPublicationGroupType().equals(IScheduleEvent.COMPONENT_GROUP)){
				Group g = dh.getGroupDAO().get(em, ci.getId(), event.getComponentDescriptorId()+"/"+event.getEventComponentNumber());
				if(g!=null && g.getMarketPlace()!=null && g.getMarketPlace().getOpen()){
					System.out.println("[SchedulePublication] Aborting final publication. Marketplace open for group " + g.getFullPath());
					throw new SchedulingException("Nad grupom " + g.getFullPath() +" je otvorena burza!", true);
				}
			}else{
				Group rootPrivateGroup = GroupUtil.findPrivateGroup(ci);
				for(Group g : rootPrivateGroup.getSubgroups()){
					if(g.getName().equals(event.getPrivateGroupName()) && g.getMarketPlace()!=null && g.getMarketPlace().getOpen()){
						System.out.println("[SchedulePublication] Aborting final publication. Marketplace open for group " + g.getFullPath());
						throw new SchedulingException("Nad grupom " + g.getFullPath() +" je otvorena burza!", true);
					}
				}
			}
		}

		
		//Stvaranje grupa i evenata
		for(IScheduleEvent event : schedule.getScheduleEvents()){
			System.out.println("[SchedulePublication] Publishing event: " + event.getName() + "   publication group: " +event.getPublicationGroupType());
			//Root grupa
			String componentRoot="";
			CourseComponentDescriptor ccd=null;
			Group eventGroup=null;
			
			if(event.getPublicationGroupType().equals(IScheduleEvent.COMPONENT_GROUP)){
				
				for(CourseComponent cc : ci.getCourseComponents()){
					if(cc.getDescriptor().getId()==Long.parseLong(event.getComponentDescriptorId())){
						componentRoot = cc.getDescriptor().getGroupRoot();
						ccd = cc.getDescriptor();
					}
				}
				if(ccd==null || componentRoot.equals("")) {
					System.out.println("[SchedulePublication] Aborting publication. Unable to get component descriptor.");
					throw new SchedulingException("Unable to get course component descriptor "+event.getComponentDescriptorId()+ " for event: " +event.getName());
				}
				String rootRelativePath = componentRoot+ "/" + event.getEventComponentNumber();
				eventGroup = dh.getGroupDAO().get(em, ci.getId(), rootRelativePath); 
				if(eventGroup==null) {
					eventGroup = new Group();
					eventGroup.setCapacity(-1);
					eventGroup.setCompositeCourseID(ci.getId());
					eventGroup.setEnteringAllowed(false);
					eventGroup.setLeavingAllowed(false);
					eventGroup.setManagedRoot(true);
					eventGroup.setRelativePath(rootRelativePath);
					eventGroup.setName(event.getName());
					dh.getGroupDAO().save(em, eventGroup);
					Group componentGroup = dh.getGroupDAO().get(em, ci.getId(), componentRoot);
					
					if(componentGroup!=null){
						//Provjera postoji li već dijete component grupe koje
						//ima jednak naziv kao naš event
						for(Group g : componentGroup.getSubgroups()){
							if(g.getName().equals(event.getName())){
								throw new SchedulingException("U odabranoj komponenti već postoji objavljeni događaj naziva jednakog vašem događaju (" + event.getName() + "). Preporuka: izmijenite naziv događaja u planu i generirajte novi raspored.");
							}
						}
					}
					
					if(componentGroup==null){
						componentGroup = new Group();
						componentGroup.setCapacity(-1);
						componentGroup.setCompositeCourseID(ci.getId());
						componentGroup.setEnteringAllowed(false);
						componentGroup.setLeavingAllowed(false);
						componentGroup.setManagedRoot(false);
						componentGroup.setRelativePath(componentRoot);
						componentGroup.setName(ComponentScheduleSyncService.Helper.getComponentRootGroupName(ccd));
						componentGroup.setParent(ci.getPrimaryGroup());
						dh.getGroupDAO().save(em, componentGroup);
						ci.getPrimaryGroup().getSubgroups().add(componentGroup);
					}
					componentGroup.getSubgroups().add(eventGroup);
					eventGroup.setParent(componentGroup);
				}
				eventGroup.setName(event.getName());
				
			}else if(event.getPublicationGroupType().equals(IScheduleEvent.PRIVATE_GROUP)){
				
				//Get the root private group on course
				Group rootPrivateGroup = GroupUtil.findPrivateGroup(ci);
				if(rootPrivateGroup==null){
					rootPrivateGroup = new Group();
					rootPrivateGroup.setCapacity(-1);
					rootPrivateGroup.setCompositeCourseID(ci.getId());
					rootPrivateGroup.setEnteringAllowed(false);
					rootPrivateGroup.setLeavingAllowed(false);
					rootPrivateGroup.setManagedRoot(false);
					rootPrivateGroup.setRelativePath("6");
					rootPrivateGroup.setParent(ci.getPrimaryGroup());
					dh.getGroupDAO().save(em, rootPrivateGroup);
					ci.getPrimaryGroup().getSubgroups().add(rootPrivateGroup);
				}
				dh.getGroupDAO().save(em, new GroupOwner(rootPrivateGroup, currentUser));
				//Is there a private room with the name user wants to use?
				eventGroup=null;
				for(Group g : rootPrivateGroup.getSubgroups()){
					if(g.getName().equals(event.getPrivateGroupName())){
						eventGroup = g;
					}
				}
				if(eventGroup==null){ //There is no such group
					eventGroup = new Group();
					eventGroup.setCapacity(-1);
					eventGroup.setCompositeCourseID(ci.getId());
					eventGroup.setEnteringAllowed(false);
					eventGroup.setLeavingAllowed(false);
					eventGroup.setManagedRoot(true);
					eventGroup.setRelativePath(GroupUtil.findNextRelativePath(rootPrivateGroup));
					eventGroup.setParent(rootPrivateGroup);
					eventGroup.setName(event.getName());
					dh.getGroupDAO().save(em, eventGroup);
					rootPrivateGroup.getSubgroups().add(eventGroup);
				}
				dh.getGroupDAO().save(em, new GroupOwner(eventGroup, currentUser));
				eventGroup.setName(event.getName());
			}else{
				throw new SchedulingException("Unknown publication group type", true);
			}
			System.out.println("[SchedulePublication] Publication group for event prepared. Relative path: " + eventGroup.getRelativePath());
			//Brisanje podgrupa ako ih ima
			if(!eventGroup.getSubgroups().isEmpty()){ 
				List<Group> gList = new ArrayList<Group>(eventGroup.getSubgroups());
				for(Group g : gList) {
					GroupTreeBrowserService.fullyDeleteGroups(em, g.getId());
				}
				eventGroup.getSubgroups().clear();
				em.flush();
			}
			
			for(IScheduleTerm term : event.getScheduleEventTerms()){
				
				Group termGroup = new Group();
				termGroup.setCapacity(term.getStudents().size());
				termGroup.setCompositeCourseID(ci.getId());
				termGroup.setEnteringAllowed(false);
				termGroup.setLeavingAllowed(false);
				termGroup.setManagedRoot(false);
				termGroup.setName(term.getTermName());
				termGroup.setParent(eventGroup);
				termGroup.setRelativePath(eventGroup.getRelativePath() + "/" + term.getSerialNumber());
				dh.getGroupDAO().save(em, termGroup);
				eventGroup.getSubgroups().add(termGroup);
				
				for(String jmbag : term.getStudents()) {
					UserGroup ug = new UserGroup();
					ug.setUser(dh.getUserDAO().getUserByJMBAG(em, jmbag));
					ug.setGroup(termGroup);
					dh.getUserGroupDAO().save(em, ug);
					termGroup.getUsers().add(ug);
				}
				
				GroupWideEvent termEvent = new GroupWideEvent(); 
				termEvent.setDuration(event.getTermDuration());
				termEvent.setStart(term.getStart());
				termEvent.getGroups().add(termGroup);
				termEvent.setTitle(term.getTermName());
				termEvent.setStrength(EventStrength.STRONG);
				termEvent.setIssuer(currentUser);
				termEvent.setRoom(dh.getRoomDAO().get(em, term.getRoom().getId()));
				termEvent.setContext("c_"+ccd.getShortName()+":"+ci.getId()+":"+componentRoot);
				termGroup.getEvents().add(termEvent);
				dh.getEventDAO().save(em, termEvent);
				System.out.println("[SchedulePublication] Generated group and event for term " + term.getTermName());
				
			}
		}

		
		
		//Priprema prostorija
		for(IScheduleEvent event : schedule.getScheduleEvents()){
			for(IScheduleTerm term : event.getScheduleEventTerms()){
				Room r = dh.getRoomDAO().get(em, term.getRoom().getId());
				RoomReservationPeriod rrp = new RoomReservationPeriod(r.getShortName(), RoomReservationStatus.FREE, term.getStartDateTime(), term.getEndDateTime());
				rrp.setReason(ci.getCourse().getName() + " - " + term.getOverridenEventName());
				reservedRooms.put(r, rrp);
			}
		}
		
		//Rezervacija prostorija
		try{	
			for(Map.Entry<Room, RoomReservationPeriod> entry : reservedRooms.entrySet()){
				Room r = entry.getKey();
				RoomReservationPeriod rrp = entry.getValue();
				if(!reservationManager.allocateRoom(r.getShortName(), rrp.getDateTimeFrom(), rrp.getDateTimeTo(), rrp.getReason())){
					rollbackRoomReservations(reservedRooms, reservationManager);
					throw new SchedulingException("Rezervacija dvorane " + r.getName() + " nije uspjela.", true);
				}
			}
		}catch(ReservationException e){
			rollbackRoomReservations(reservedRooms, reservationManager);
			throw new SchedulingException(e.getMessage(), true);
		}
		
		//U parametre deskriptora se zapisuje datum objave - nije predvidjen u entitetu
		String previousParameters = scheduleDesc.getParameters();
		SimpleDateFormat sdf = new SimpleDateFormat(PlanningService.DATE_FORMAT);
		String date = sdf.format(new Date());
		scheduleDesc.setParameters(previousParameters + " publicationDate="+ date);
		
		System.out.println("[SchedulePublication] Room reservations completed successfuly.");
	}

	private static void rollbackRoomReservations(Map<Room, RoomReservationPeriod> reservedRooms , IReservationManager reservationManager){
		System.out.println("[SchedulePublication] Room reservations failed. Performing rollback.");
		for(Map.Entry<Room, RoomReservationPeriod> entry : reservedRooms.entrySet()){
			RoomReservationPeriod rrp = entry.getValue();
			Room r = entry.getKey();
			try {
				reservationManager.deallocateRoom(r.getShortName(), rrp.getDateTimeFrom(), rrp.getDateTimeTo());
			} catch (ReservationException ignored) {}
		}
	}
	
	

	/**
	 * Provjera postoje li grupe u koje se zeli objaviti novi raspored.
	 * @param userID
	 * @param courseInstanceID
	 * @param publicationGroups
	 * @param wrapper
	 */
	public static void validatePublicationGroups(final Long userID, final String courseInstanceID, 
			final String publicationGroups, final InputStreamWrapper[] wrapper){
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>(){
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				CourseInstance courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				Group rootPrivateGroup = GroupUtil.findPrivateGroup(courseInstance);
				System.out.println("[ScheduleGroupValidation] New request: " + publicationGroups);
				String result="VALID";
				String message="";
				try{
					Node n = loadXML(publicationGroups);
					NodeList groups = n.getChildNodes();
					for(int i=0; i<groups.getLength(); i++){
						Element group = (Element)groups.item(i);
						if(group.getNodeName().equals("cg")){
							String componentDescriptorID = group.getAttribute("cid");
							String groupNumber = group.getAttribute("cn");
							String serial = group.getAttribute("serial");
							String groupRoot="";
							for(CourseComponent cc : courseInstance.getCourseComponents()){
								if(cc.getDescriptor().getId()==Long.parseLong(componentDescriptorID)){
									groupRoot = cc.getDescriptor().getGroupRoot();
								}
							}
							Group g = dh.getGroupDAO().get(em, courseInstance.getId(), groupRoot+"/"+groupNumber);
							if(g!=null) message += serial + " ";
						}else if(group.getNodeName().equals("pg")){
							String privateGroupName = group.getAttribute("name");
							String serial = group.getAttribute("serial");
							for(Group g : rootPrivateGroup.getSubgroups()){
								if(g.getName().equals(privateGroupName)) message += serial + " ";
							}
						}
					}
					if(!message.equals("")) result="INVALID";
				}catch(Exception e){
					result="ERROR";
				}
				System.out.println("[ScheduleGroupValidation] Result: "+result + "   Message: " + message);
				wrapper[0] = createInputStreamWrapperFromText(prepareResultXML(result, message));
				return null;
			}
		});
	}

	

}
