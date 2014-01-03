package hr.fer.zemris.jcms.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.CCIAMatrix;
import hr.fer.zemris.jcms.beans.CCIAMatrixColumn;
import hr.fer.zemris.jcms.beans.CCIAMatrixRow;
import hr.fer.zemris.jcms.beans.CCTAMatrix;
import hr.fer.zemris.jcms.beans.CCTAMatrixColumn;
import hr.fer.zemris.jcms.beans.CCTAMatrixRow;
import hr.fer.zemris.jcms.beans.CourseComponentBean;
import hr.fer.zemris.jcms.beans.CourseComponentItemBean;
import hr.fer.zemris.jcms.beans.CourseComponentTaskBean;
import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.beans.ManualGroupsCreateBean;
import hr.fer.zemris.jcms.beans.TestDataBean;
import hr.fer.zemris.jcms.beans.TestInstanceDataBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentAssistantBean;
import hr.fer.zemris.jcms.beans.ext.BaseUserBean;
import hr.fer.zemris.jcms.beans.ext.ComponentDefBean;
import hr.fer.zemris.jcms.beans.ext.ComponentItemAssessmentBean;
import hr.fer.zemris.jcms.beans.ext.ComponentTaskAssignmentBean;
import hr.fer.zemris.jcms.beans.ext.ComponentUserTaskBean;
import hr.fer.zemris.jcms.beans.ext.EditItemScoresBean;
import hr.fer.zemris.jcms.beans.ext.FileBean;
import hr.fer.zemris.jcms.beans.ext.FileDownloadBean;
import hr.fer.zemris.jcms.beans.ext.FileUploadBean;
import hr.fer.zemris.jcms.beans.ext.GroupOwnerFlat;
import hr.fer.zemris.jcms.beans.ext.ItemScoreBean;
import hr.fer.zemris.jcms.beans.ext.ReviewersUserTaskBean;
import hr.fer.zemris.jcms.beans.ext.TaskFileBean;
import hr.fer.zemris.jcms.beans.ext.TaskFileUploadBean;
import hr.fer.zemris.jcms.beans.ext.TaskReviewBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AbstractCourseComponentDef;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfEnum;
import hr.fer.zemris.jcms.model.AssessmentConfRange;
import hr.fer.zemris.jcms.model.AssessmentConfiguration;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CCIAAssignment;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseComponentADef;
import hr.fer.zemris.jcms.model.CourseComponentDescriptor;
import hr.fer.zemris.jcms.model.CourseComponentFDef;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.CourseComponentItemAssessment;
import hr.fer.zemris.jcms.model.CourseComponentTask;
import hr.fer.zemris.jcms.model.CourseComponentTaskAssignment;
import hr.fer.zemris.jcms.model.CourseComponentTaskUpload;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.ItemDescriptionFile;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.UserSpecificEvent2;
import hr.fer.zemris.jcms.model.Venue;
import hr.fer.zemris.jcms.model.extra.EventStrength;
import hr.fer.zemris.jcms.parsers.ManualGroupsCreateParser;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.assessments.AssessmentStatus;
import hr.fer.zemris.jcms.service.extsystems.TestsService;
import hr.fer.zemris.jcms.service.util.AssessmentUtil;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.web.actions.data.CourseComponentData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.FileUtil;
import hr.fer.zemris.util.InputStreamWrapper;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;

/**
 * Razred koji se brine o komponentama jednog primjerka kolegija
 * @author TOMISLAV
 *
 */
public class CourseComponentService {
	
	//TODO: position u tasku
	public static void getComponentsTree(final CourseComponentData data, final String courseInstanceID, 
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {

				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if (!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID))
					return null;
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//dohvacamo sve postojece komponente na courseInstance.u te sve deskriptore koji postoje u sustavu
				Set<CourseComponent> dbComponentSet= data.getCourseInstance().getCourseComponents();
				Set<CourseComponentDescriptor> dbDescriptorSet = new HashSet<CourseComponentDescriptor>(dh.getCourseComponentDAO().listDescriptors(em));
				 
				//TODO: popraviti jednom da to izgleda bolje i efikasnije
				//radimo tree
				for (CourseComponent cc : dbComponentSet) {
					for (CourseComponentItem cci : cc.getItems()) {
						cci.getName();
					}
					dbDescriptorSet.remove(cc.getDescriptor());
				}
				
				//punimo bean listu s deskriptorima za koje se jos mogu napraviti komponente na ovom courseInstanceu
				List<CourseComponentBean> descriptorList = new ArrayList<CourseComponentBean>(dbDescriptorSet.size());
				for (CourseComponentDescriptor ccd : dbDescriptorSet) {
					CourseComponentBean ccb = new CourseComponentBean();
					ccb.setShortName(ccd.getShortName());
					ccb.setName(ccd.getName());
					
					descriptorList.add(ccb);
				}
				
				
				//da li korisnik moze administrirati
				data.setAdmin(
						JCMSSecurityManagerFactory.getManager()
						.canPerformCourseAdministration(data.getCourseInstance())
						);
				
				data.setComponentSet(dbComponentSet);
				data.setDescriptorList(descriptorList);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				
				return null;
			}
			
		});
	}
	
	public static void viewComponentItem(final CourseComponentData data,
			final String courseComponentItemID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			//TODO: dovrsiti da prikazuje sve informacije
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				prepareItemView(data,dh,em,cci);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
		
		// Ovaj dio radimo izvan transakcije baze podataka jer nam baza vise ne treba
		// a moze potrajati neko vrijeme...
		List<ComponentItemAssessmentBean> assesmentList = data.getItemAssessmentsList();
		if(assesmentList!=null) {
			String username = data.getCurrentUser().getUsername();
			for(ComponentItemAssessmentBean bean : assesmentList) {
				TestDataBean tdb = TestsService.retrieveTestData(bean.getAssessmentIdentifier(), username, true);
				bean.setTestDataBean(tdb);
				// E sad, s ovime nisam zadovoljan, jer se sloj usluge bavi razrjesavanjem URL-ova...
				// no, za sada neka bude ovako.
				if(tdb.getTestInstanceData()!=null) {
					for(TestInstanceDataBean ti : tdb.getTestInstanceData()) {
						ti.setUrl(TestsService.getTestPath(tdb, ti));
					}
				}
			}
		}
	}

	public static void createComponentItemGroups(final CourseComponentData data,
			final String courseComponentItemID, final Long userID, final String text) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			//TODO: dovrsiti da prikazuje sve informacije
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;

				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);

				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				List<ManualGroupsCreateBean> entries = null;
				try {
					entries = ManualGroupsCreateParser.parseTabbedFormat(new StringReader(text));
				} catch (Exception e) {
					data.getMessageLogger().addErrorMessage(e.getMessage());
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				if(entries.isEmpty()) {
					data.getMessageLogger().addErrorMessage("Ništa niste zadali.");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				Map<String,Venue> venues = new HashMap<String, Venue>();
				Map<String,Room> rooms = new HashMap<String, Room>();
				for(ManualGroupsCreateBean bean : entries) {
					if(!venues.containsKey(bean.getVenue())) {
						Venue v = dh.getVenueDAO().get(em, bean.getVenue());
						if(v==null) {
							data.getMessageLogger().addErrorMessage("Lokacija "+bean.getVenue()+" nije registrirana u sustavu!");
							data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
							return null;
						}
						venues.put(bean.getVenue(), v);
					}
					String roomKey = bean.getVenue()+"@@@"+bean.getRoomName();
					if(rooms.containsKey(roomKey)) continue;
					Room r = dh.getRoomDAO().get(em, bean.getVenue(), bean.getRoomName());
					if(r==null) {
						data.getMessageLogger().addErrorMessage("Prostorija "+bean.getRoomName()+" na lokaciji "+bean.getVenue()+" nije registrirana u sustavu!");
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					rooms.put(roomKey, r);
				}
				
				String groupRootPath = cci.getCourseComponent().getDescriptor().getGroupRoot();
				String groupRootName = cci.getCourseComponent().getDescriptor().getName();
				String componentRootGroupPath = groupRootPath + "/" + cci.getPosition();
				String componentRootGroupName = cci.getCourseComponent().getDescriptor().getShortName().substring(0,1)+cci.getCourseComponent().getDescriptor().getShortName().substring(1).toLowerCase()+" "+cci.getPosition();

				CourseInstance ci = cci.getCourseComponent().getCourseInstance();

				Group gRoot = findSubGroupByRelativePath(ci.getPrimaryGroup(), groupRootPath);
				if(gRoot==null) {
					gRoot = new Group();
					gRoot.setCapacity(-1);
					gRoot.setCompositeCourseID(ci.getId());
					gRoot.setEnteringAllowed(false);
					gRoot.setLeavingAllowed(false);
					gRoot.setManagedRoot(false);
					gRoot.setMarketPlace(null);
					gRoot.setMpSecurityTag(null);
					gRoot.setName(groupRootName);
					gRoot.setParent(ci.getPrimaryGroup());
					gRoot.setRelativePath(groupRootPath);
					dh.getGroupDAO().save(em, gRoot);
					ci.getPrimaryGroup().getSubgroups().add(gRoot);
				}

				Group gcciRoot = findSubGroupByRelativePath(gRoot, componentRootGroupPath);
				if(gcciRoot==null) {
					gcciRoot = new Group();
					gcciRoot.setCapacity(-1);
					gcciRoot.setCompositeCourseID(ci.getId());
					gcciRoot.setEnteringAllowed(false);
					gcciRoot.setLeavingAllowed(false);
					gcciRoot.setManagedRoot(true);
					gcciRoot.setMpSecurityTag(null);
					gcciRoot.setName(componentRootGroupName);
					gcciRoot.setParent(gRoot);
					gcciRoot.setRelativePath(componentRootGroupPath);
					MarketPlace mp = new MarketPlace();
					mp.setTimeBuffer(-1);
					mp.setGroup(gcciRoot);
					gcciRoot.setMarketPlace(mp);
					dh.getGroupDAO().save(em, gcciRoot);
					gRoot.getSubgroups().add(gcciRoot);
				}

				if(gcciRoot.getMarketPlace()!=null && gcciRoot.getMarketPlace().getOpen()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.marketPlaceStillOpen"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				for(ManualGroupsCreateBean bean : entries) {
					String groupName = bean.getDate() + " " + bean.getStartTime() + " " + bean.getEndTime() + " " + bean.getRoomName();
					Group sg = findSubGroupByName(gcciRoot, groupName);
					if(sg==null) {
						sg = new Group();
						sg.setCapacity(-1);
						sg.setCompositeCourseID(ci.getId());
						sg.setEnteringAllowed(true);
						sg.setLeavingAllowed(true);
						sg.setManagedRoot(false);
						sg.setMpSecurityTag(null);
						sg.setName(groupName);
						sg.setParent(gcciRoot);
						sg.setRelativePath(gcciRoot.getRelativePath()+"/"+findNextRelativePath(gcciRoot.getSubgroups()));
						sg.setMarketPlace(null);
						dh.getGroupDAO().save(em, sg);
						gcciRoot.getSubgroups().add(sg);
						
						GroupWideEvent gwe = new GroupWideEvent();
						gwe.setDuration(bean.getDuration());
						gwe.setIssuer(data.getCurrentUser());
						gwe.setRoom(rooms.get(bean.getVenue()+"@@@"+bean.getRoomName()));
						gwe.setSpecifier(ci.getYearSemester().getId()+"/rucno/L");
						gwe.setStart(bean.getStartDate());
						gwe.setStrength(EventStrength.STRONG);
						gwe.setTitle(bean.getTitle());
						dh.getEventDAO().save(em, gwe);

						gwe.getGroups().add(sg);
						sg.getEvents().add(gwe);
					}
				}

				Group sg = findSubGroupByRelativePath(gcciRoot, gcciRoot.getRelativePath()+"/0");
				if(sg==null) {
					sg = new Group();
					sg.setCapacity(-1);
					sg.setCompositeCourseID(ci.getId());
					sg.setEnteringAllowed(true);
					sg.setLeavingAllowed(true);
					sg.setManagedRoot(false);
					sg.setMpSecurityTag(null);
					sg.setName("Neraspoređeni");
					sg.setParent(gcciRoot);
					sg.setRelativePath(gcciRoot.getRelativePath()+"/"+findNextRelativePath(gcciRoot.getSubgroups()));
					sg.setMarketPlace(null);
					dh.getGroupDAO().save(em, sg);
					gcciRoot.getSubgroups().add(sg);
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	private static String findNextRelativePath(Set<Group> subgroups) {
		if(subgroups.isEmpty()) return "1";
		List<Integer> list = new ArrayList<Integer>();
		for(Group g : subgroups) {
			String rp = g.getRelativePath();
			int pos = rp.lastIndexOf('/');
			if(pos==-1) {
				list.add(Integer.valueOf(rp));
			} else {
				list.add(Integer.valueOf(rp.substring(pos+1)));
			}
		}
		if(list.isEmpty()) return "1";
		Collections.sort(list);
		int curr = list.get(0).intValue();
		if(curr>1) return "1";
		for(Integer i : list) {
			if(curr != i.intValue()) return String.valueOf(curr);
			curr++;
		}
		return String.valueOf(curr);
	}

	private static Group findSubGroupByRelativePath(Group parent, String subGroupRelativePath) {
		for(Group g : parent.getSubgroups()) {
			if(g.getRelativePath().equals(subGroupRelativePath)) return g;
		}
		return null;
	}

	private static Group findSubGroupByName(Group parent, String subGroupName) {
		for(Group g : parent.getSubgroups()) {
			if(g.getName().equals(subGroupName)) return g;
		}
		return null;
	}
	
	public static void addCourseComponent(final CourseComponentData data,final String courseInstanceID, 
			final Long userID, final String shortName) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {

				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//punimo podatke
				if (!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID))
					return null;
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//provjera je li zadano pravilno ime
				if (shortName == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				CourseComponentDescriptor ccd = dh.getCourseComponentDAO().getByShortName(em, shortName);
				
				if (ccd == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//provjera postoji li vec komponenta s tim imenom
				
				for (CourseComponent cc : data.getCourseInstance().getCourseComponents()) {
					if (cc.getDescriptor().getShortName().equals(shortName)) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.componentExists"));
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
				}
				
				//sad kad je sve u redu stvaramo novu komponentu
				createCourseComponent(dh,em,ccd,data.getCourseInstance());
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				
				return null;
			}
			
		});
	}
	
	public static void newComponentItem(final CourseComponentData data, final String courseComponentID, 
			 final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				//punimo podatke za prikaz i izlazimo van
				if (!fillCourseComponent(em, data, courseComponentID))
					return null;
				
				CourseComponent cc = data.getCourseComponent();
				data.setCourseInstance(cc.getCourseInstance());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_INPUT);
				return null;
			}
		});
	}
	
	public static void editComponentItem(final CourseComponentData data, final String courseComponentItemID, 
			final CourseComponentItemBean itemBean, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//ako je sve ok idemo pripremiti bean
				itemBean.setName(cci.getName());
				itemBean.setId(String.valueOf(cci.getId()));
				itemBean.setPosition(String.valueOf(cci.getPosition()));
				
				data.setResult(AbstractActionData.RESULT_INPUT);
				return null;
			}
		});
	}
	
	public static void saveComponentItem(final CourseComponentData data, final String courseComponentID, 
			final CourseComponentItemBean itemBean, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				if (!fillCourseComponent(em, data, courseComponentID))
					return null;
				CourseComponent cc = data.getCourseComponent();
				data.setCourseInstance(cc.getCourseInstance());
				data.setCourseComponent(cc);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//provjera je li to novi item ili se uredjuje vec postojeci
				CourseComponentItem cci=null;
				boolean isNew = true;
				if (!StringUtil.isStringBlank(itemBean.getId()) && !"-1".equals(itemBean.getId())) {
					try {
						cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(itemBean.getId()));
					}
					catch (Exception ignorable) {}
					if (cci==null || !cc.equals(cci.getCourseComponent())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					isNew = false;
				}
				else {
					cci= new CourseComponentItem();
					itemBean.setId("-1");
				}
				
				//provjera podataka
				try {
					int position = Integer.valueOf(itemBean.getPosition());
					for (CourseComponentItem tmp : cc.getItems()) {
						//ako su pozicije iste ali se ne radi o istom itemu
						if (tmp.getPosition()==position && !tmp.getId().equals(Long.valueOf(itemBean.getId()))) {
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.componentItemExists"));
							data.setResult(AbstractActionData.RESULT_INPUT);
							return null;
						}
					}
				}catch (Exception ignorable) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongPositionFormat"));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				if (StringUtil.isStringBlank(itemBean.getName())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.nameMustBeGiven"));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				//Dodavanje nove ili update postojeće teme u ITS-u
				IssueTrackingService.updateMessageTopic(em, cci.getName(), itemBean.getName().trim(), cc, "CCI");
				
				//ako je sve uredu stvaramo novi item ili radimo update postojeceg
				cci.setName(itemBean.getName().trim());
				cci.setPosition(Integer.valueOf(itemBean.getPosition()));
				
				
				if (isNew) {
					//TODO: dodati jos grupu
					cci.setCourseComponent(cc);
					cc.getItems().add(cci);
					dh.getCourseComponentDAO().save(em, cci);
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
			
		});
	}
	
	public static void viewItemScore(final CourseComponentData data,
			final String courseComponentItemID, final EditItemScoresBean bean,
			final Long userID) {
		//TODO: kontrola
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setAdmin(true);

				String groupRootRelativePath = cci.getCourseComponent().getDescriptor().getGroupRoot()+"/"+cci.getPosition();
	
				List<UserGroup> uglist = dh.getGroupDAO().findUserGroup(em, data.getCourseInstance().getId(), groupRootRelativePath+"/%");

				List<User> courseUsers = dh.getCourseInstanceDAO().findCourseUsers(em, data.getCourseInstance().getId());
				
				Map<Long, UserGroup> userGroupMapByUserID = new HashMap<Long, UserGroup>(uglist.size());
				for (UserGroup ug : uglist) {
					userGroupMapByUserID.put(ug.getUser().getId(), ug);
				}

				fillItemUserScores(dh,em,data,cci,bean,courseUsers,true,userGroupMapByUserID,true);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void viewItemScore(final CourseComponentData data,
			final String courseComponentItemID, final String groupID, 
			final EditItemScoresBean bean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				Group g = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
					g = dh.getGroupDAO().get(em, Long.valueOf(groupID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if (g==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				data.setGroup(g);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				Set<Group> groupSet = new HashSet<Group>(
						dh.getGroupDAO().findGroupsOwnedBy(em, data.getCourseInstance().getId(), data.getCurrentUser())
					);
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if((!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !groupSet.contains(g)) || !data.getCourseInstance().getId().equals(g.getCompositeCourseID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Map<Long, UserGroup> userGroupMapByUserID = new HashMap<Long, UserGroup>(g.getUsers().size());
				List<User> userList = new ArrayList<User>(g.getUsers().size());
				for (UserGroup ug : g.getUsers()) {
					userList.add(ug.getUser());
					userGroupMapByUserID.put(ug.getUser().getId(), ug);
				}
				
				fillItemUserScores(dh,em,data,cci,bean,userList,false,userGroupMapByUserID,true);
				
				data.setAdmin(false);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void saveItemScore(final CourseComponentData data,
			final String courseComponentItemID, final EditItemScoresBean bean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		//TODO: kontrola
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setAdmin(true);
				
				List<User> courseUsers = dh.getCourseInstanceDAO().findCourseUsers(em, data.getCourseInstance().getId());
				saveItemUserScore(dh,em,data,cci,bean,courseUsers,true);
				return null;
			}
		});
	}
	
	public static void saveItemScore(final CourseComponentData data,
			final String courseComponentItemID, final String groupID, 
			final EditItemScoresBean bean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				Group g = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
					g = dh.getGroupDAO().get(em, Long.valueOf(groupID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if (g==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				data.setGroup(g);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				Set<Group> groupSet = new HashSet<Group>(
						dh.getGroupDAO().findGroupsOwnedBy(em, data.getCourseInstance().getId(), data.getCurrentUser())
					);
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if((!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !groupSet.contains(g)) || !data.getCourseInstance().getId().equals(g.getCompositeCourseID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<User> userList = new ArrayList<User>(g.getUsers().size());
				for (UserGroup ug : g.getUsers()) {
					userList.add(ug.getUser());
				}
				
				data.setAdmin(false);
				saveItemUserScore(dh,em,data,cci,bean,userList,false);
				return null;
			}
		});
	}
	
	public static void editGroupOwners(final CourseComponentData data,
			final String courseComponentItemID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<Group> groupList = 
					dh.getGroupDAO().findSubgroups(em, data.getCourseInstance().getId(), 
							cci.getCourseComponent().getDescriptor().getGroupRoot()+"/"+String.valueOf(cci.getPosition())+"/%");
				if (groupList == null || groupList.size()==0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noGroups"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				List<User> ownerList = 
					dh.getGroupDAO().listUsersInGroupTree(em, data.getCourseInstance().getId(), JCMSSecurityConstants.SEC_ROLE_GROUP);
				if (ownerList == null || ownerList.size()==0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noOwners"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Collections.sort(ownerList, StringUtil.USER_COMPARATOR);
				Collections.sort(groupList, StringUtil.GROUP_COMPARATOR);
				
				List<GroupBean> groupBeanList = new ArrayList<GroupBean>(groupList.size());
				List<GroupOwner> goList = null;
				List<BaseUserBean> owners = null;
				for (Group g : groupList) { 
					GroupBean bean = new GroupBean();
					bean.setId(g.getId());
					bean.setName(g.getName());
					
					goList = dh.getGroupDAO().findForGroup(em, g);
					owners = new ArrayList<BaseUserBean>(goList.size());
					for (GroupOwner go : goList) {
						BaseUserBean b = new BaseUserBean();
						b.setFirstName(go.getUser().getFirstName());
						b.setLastName(go.getUser().getLastName());
						b.setJmbag(go.getUser().getJmbag());
						b.setUserID(go.getId());
						
						owners.add(b);
					}
					
					bean.setOwnerList(owners);
					groupBeanList.add(bean);
				}
				data.setGroupList(groupBeanList);
				data.setUserList(ownerList);
				
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}
	
	public static void addGroupOwner(final CourseComponentData data,
			final String courseComponentItemID,
			final GroupOwnerFlat goBean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				Group g = null;
				User u = null;
				try {
					g = dh.getGroupDAO().get(em, Long.valueOf(goBean.getGroupID()));
					u = dh.getUserDAO().getUserById(em, Long.valueOf(goBean.getUserID()));
				} catch (Exception e) {
				}
				if (g == null || u == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						|| !data.getCourseInstance().getId().equals(g.getCompositeCourseID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (dh.getGroupDAO().getGroupOwner(em, g, u)!=null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.alreadyGroupOwner"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				GroupOwner go = new GroupOwner();
				go.setGroup(g);
				go.setUser(u);
				dh.getGroupDAO().save(em, go);
				
				data.setOwner(go);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}
	
	public static void removeGroupOwner(final CourseComponentData data,
			final String courseComponentItemID,
			final String goID,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				GroupOwner go = null;
				try {
					go = dh.getGroupDAO().getGroupOwner(em, Long.valueOf(goID));
				} catch (Exception e) {
				}
				if (go == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						|| !data.getCourseInstance().getId().equals(go.getGroup().getCompositeCourseID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				go.setGroup(null);
				go.setUser(null);
				dh.getGroupDAO().remove(em, go);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}

	public static void newItemDef(final CourseComponentData data,
			final String courseComponentItemID, final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void saveItemDef(final CourseComponentData data,
			final String courseComponentItemID, final List<ComponentDefBean> beanList,
			final Long userID) {
		//TODO:kontrola
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//mapiramo postojece flagove i assessmente na predmetu
				
				List<AssessmentFlag> tmpFlagList = dh.getAssessmentDAO().listFlagsForCourseInstance(em, data.getCourseInstance().getId());
				List<Assessment> tmpAssessmentList = dh.getAssessmentDAO().listForCourseInstance(em, data.getCourseInstance().getId());
				
				Map<String, Assessment> assessmentByName = new HashMap<String, Assessment>(tmpFlagList.size());
				Map<String, AssessmentFlag> flagByName = new HashMap<String, AssessmentFlag>(tmpAssessmentList.size());
				
				for (AssessmentFlag flag : tmpFlagList) {
					flagByName.put(flag.getShortName(),flag);
				}
				for (Assessment a : tmpAssessmentList) {
					assessmentByName.put(a.getShortName(),a);
				}
				
				//provjera podataka
				String rootName = cci.getCourseComponent().getDescriptor().getShortName();
				String itemName = rootName+cci.getPosition();
				boolean ok = true;
				for (ComponentDefBean bean : beanList) {
					if (bean.getType().equals("boolean")) {
						if (flagByName.get(itemName+bean.getShortName())!=null) {
							String param[] = {bean.getShortName()};
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.componentDefExists",param));
							ok = false;
						}
					}
					else if (bean.getType().equals("enum") || bean.getType().equals("range")) {
						if (assessmentByName.get(itemName+bean.getShortName())!=null) {
							String param[] = {bean.getShortName()};
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.componentDefExists",param));
							ok = false;
						}
					}
					else {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				if (!ok) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				//spremamo to u bazu
				
				//pozicija item definitiona u prikazu
				int position = 0;
				
				for (ComponentDefBean bean : beanList) {
					++position;
					if (bean.getType().equals("boolean")) {
						AssessmentFlag flag = new AssessmentFlag();
						flag.setCourseInstance(data.getCourseInstance());
						flag.setShortName(itemName+bean.getShortName());
						flag.setName(bean.getName());
						StringBuilder sb = new StringBuilder();
						sb.append("if(overrideSet()) {\n");
						sb.append("setValue(overrideValue());\n");
						sb.append("} else {\n");
						sb.append("setValue(false);\n}\n");
						flag.setProgram(sb.toString());
						flag.setProgramType("java");
						flag.setProgramVersion(0);

						dh.getAssessmentDAO().save(em, flag);
						
						CourseComponentFDef ccfd = new CourseComponentFDef();
						ccfd.setAssessmentFlag(flag);
						ccfd.setCourseComponentItem(cci);
						ccfd.setPosition(position);
						
						dh.getCourseComponentDAO().save(em, ccfd);
						
						cci.getCourseComponentDefs().add(ccfd);
					}
					else if (bean.getType().equals("enum") || bean.getType().equals("range")) {
						Assessment a = new Assessment();
						a.setCourseInstance(data.getCourseInstance());
						a.setShortName(itemName+bean.getShortName());
						a.setName(bean.getName());
						StringBuilder sb = new StringBuilder();
						sb.append("setPassed(rawPresent());\n");
						sb.append("setPresent(rawPresent());\n");
						sb.append("setScore(rawScore());\n");
						a.setProgram(sb.toString());
						a.setProgramType("java");
						a.setProgramVersion(0);
						
						dh.getAssessmentDAO().save(em, a);
						assessmentByName.put(a.getShortName(), a);
						
						if (bean.getType().equals("enum")) {
							AssessmentConfEnum conf = new AssessmentConfEnum();
							conf.setIntervalStart(Double.valueOf(bean.getStart()));
							conf.setIntervalEnd(Double.valueOf(bean.getEnd()));
							conf.setStep(Double.valueOf(bean.getStep()));
							conf.setAssessment(a);
							
							dh.getAssessmentDAO().save(em, conf);
							a.setAssessmentConfiguration(conf);
						}
						else {
							AssessmentConfRange conf = new AssessmentConfRange();
							conf.setRangeStart(Double.valueOf(bean.getStart()));
							conf.setRangeEnd(Double.valueOf(bean.getEnd()));
							conf.setAssessment(a);
							
							dh.getAssessmentDAO().save(em, conf);
							a.setAssessmentConfiguration(conf);
						}
						
						//gledamo postoji li parent root
						Assessment root = assessmentByName.get(rootName);
						boolean isNew = false;
						if (root==null) {
							root = new Assessment();
							root.setCourseInstance(data.getCourseInstance());
							root.setShortName(rootName);
							root.setName(rootName);
							root.setProgram("sumChildren();");
							root.setProgramType("java");
							dh.getAssessmentDAO().save(em, root);
							assessmentByName.put(root.getShortName(), root);
							isNew = true;
						}
						
						//gledamo postoji li parent item
						Assessment item = assessmentByName.get(itemName);
						if (item==null) {
							item = new Assessment();
							item.setCourseInstance(data.getCourseInstance());
							item.setShortName(itemName);
							item.setName(itemName);
							item.setParent(root);
							item.setProgram("sumChildren();");
							item.setProgramType("java");
							
							dh.getAssessmentDAO().save(em, item);
							assessmentByName.put(item.getShortName(), item);
							root.getChildren().add(item);
						}
						if (isNew) {
							item.setParent(root);
							root.getChildren().add(item);
						}
						
						a.setParent(item);
						item.getChildren().add(a);
						
						CourseComponentADef ccad = new CourseComponentADef();
						ccad.setAssessment(a);
						ccad.setCourseComponentItem(cci);
						ccad.setPosition(position);
						
						dh.getCourseComponentDAO().save(em, ccad);
						
						cci.getCourseComponentDefs().add(ccad);
					}
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void changeDefPosition(final CourseComponentData data,
			final String componentDefID, final Long userID, final String direction) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			//TODO: dovrsiti da prikazuje sve informacije
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				AbstractCourseComponentDef def = null;
				try {
					def = dh.getCourseComponentDAO().getDef(em, Long.valueOf(componentDefID));
				}
				catch (Exception ignorable) {}
				if (def==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseInstance(def.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponent(def.getCourseComponentItem().getCourseComponent());
				data.setCourseComponentItem(def.getCourseComponentItem());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				int dir = 0;
				if ("up".equals(direction))
					dir = -1;
				else if ("down".equals(direction))
					dir = 1;
				else {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<AbstractCourseComponentDef> defList = new ArrayList<AbstractCourseComponentDef>(
						data.getCourseComponentItem().getCourseComponentDefs()
					);
				
				Collections.sort(defList, new Comparator<AbstractCourseComponentDef> () {
					@Override
					public int compare(AbstractCourseComponentDef o1, AbstractCourseComponentDef o2) {
						if (o1.getPosition() != o2.getPosition())
							return o1.getPosition()-o2.getPosition();
						String o1name = "", o2name = "";
						if (o1 instanceof CourseComponentFDef)
							o1name = ((CourseComponentFDef)o1).getAssessmentFlag().getName();
						else if (o1 instanceof CourseComponentADef)
							o1name = ((CourseComponentADef)o1).getAssessment().getName();
						if (o2 instanceof CourseComponentFDef)
							o2name = ((CourseComponentFDef)o2).getAssessmentFlag().getName();
						else if (o2 instanceof CourseComponentADef)
							o2name = ((CourseComponentADef)o2).getAssessment().getName();
						return StringUtil.HR_COLLATOR.compare(o1name, o2name);
					}
				});
				
				for (int i=0;i<defList.size();++i)
					if (defList.get(i).getPosition() != i+1)
						defList.get(i).setPosition(i+1);
						
				
				for (int i=0;i<defList.size();++i) {
					if (defList.get(i).equals(def) && i+dir >= 0 && i+dir < defList.size()) {
						int tmp = defList.get(i).getPosition();
						defList.get(i).setPosition(defList.get(i+dir).getPosition());
						defList.get(i+dir).setPosition(tmp);
					}
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void removeItemDef(final CourseComponentData data,
			final String componentDefID, final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				AbstractCourseComponentDef def = null;
				try {
					def = dh.getCourseComponentDAO().getDef(em, Long.valueOf(componentDefID));
				}
				catch (Exception ignorable) {}
				if (def==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseInstance(def.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(def.getCourseComponentItem());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (def instanceof CourseComponentADef) {
					CourseComponentADef aDef = (CourseComponentADef)def;
					
					Assessment a = aDef.getAssessment();
					
					def.getCourseComponentItem().getCourseComponentDefs().remove(def);
					def.setCourseComponentItem(null);
					
					dh.getCourseComponentDAO().remove(em, def);
					
					AssessmentConfiguration conf = a.getAssessmentConfiguration();
					conf.setAssessment(null);
					a.setAssessmentConfiguration(null);
					dh.getAssessmentDAO().remove(em, conf);
					
					if (a.getParent()!=null)
						a.getParent().getChildren().remove(a);
					
					Iterator<AssessmentScore> it = a.getScore().iterator();
					while (it.hasNext()) {
						AssessmentScore s = it.next();
						s.setUser(null);
						s.setAssessment(null);
						s.setAssigner(null);
						dh.getAssessmentDAO().remove(em, s);

						it.remove();
					}
					a.setCourseInstance(null);
					
					dh.getAssessmentDAO().remove(em, a);
				}
				if (def instanceof CourseComponentFDef) {
					CourseComponentFDef fDef = (CourseComponentFDef)def;
					
					AssessmentFlag f = fDef.getAssessmentFlag();
					
					def.getCourseComponentItem().getCourseComponentDefs().remove(def);
					def.setCourseComponentItem(null);
					
					dh.getCourseComponentDAO().remove(em, def);
					
					Iterator<AssessmentFlagValue> it = f.getValues().iterator();
					while (it.hasNext()) {
						AssessmentFlagValue afv = it.next();
						afv.setUser(null);
						afv.setAssessmentFlag(null);
						dh.getAssessmentDAO().remove(em, afv);
						
						it.remove();
					}
					f.setCourseInstance(null);
					
					dh.getAssessmentDAO().remove(em, f);
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void uploadItemDescriptionFile(final CourseComponentData data, 
			final String courseComponentItemID, final FileUploadBean bean, final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				//TODO: dodati charset u cijelu pricu
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//dohvacamo item
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponentItem(cci);
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//provjeravamo ima li bean gresaka
				if (StringUtil.isStringBlank(bean.getUploadFileName())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
					
				for (ItemDescriptionFile file : cci.getItemDescriptionFiles()) {
					if (file.getFileName().equals(bean.getUploadFileName().trim())) {
						prepareItemView(data, dh, em, cci);
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.fileNameExists"));
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
				}
				
				//dohvacamo potreban direktorij i usput stvaramo strukturu datoteka na disku
				try {
					
					//kopiramo datoteku
					File resultFile = new File(getItemDir(cci),bean.getUploadFileName());
					resultFile.createNewFile();
					FileUtils.copyFile(bean.getUpload(), resultFile);
					
					//stvaramo ItemDescriptionFile
					ItemDescriptionFile idf = new ItemDescriptionFile();
					idf.setCourseComponentItem(cci);
					idf.setFileName(bean.getUploadFileName());
					idf.setMimeType(bean.getUploadContentType());
					
					cci.getItemDescriptionFiles().add(idf);
					dh.getCourseComponentDAO().save(em, idf);
					
				} catch (Exception e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.internalError"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
			
		});
	}
	
	public static void removeItemDescriptionFile(final CourseComponentData data, 
			final String fileID,final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//dohvacamo taskupload
				ItemDescriptionFile idf = null;
				try {
					idf = dh.getCourseComponentDAO().getItemDescriptionFile(em, Long.valueOf(fileID));
				}catch (Exception ingorable) {
				}
				if (idf==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				data.setCourseInstance(idf.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(idf.getCourseComponentItem());
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//datoteka koju cemo obrisati
				try {
					removeIDF(dh,em,idf);
				} catch (Exception e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.internalError"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}

	public static void viewItemDescriptionFile(final CourseComponentData data, 
			final String fileID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//dohvacamo taskupload
				ItemDescriptionFile idf = null;
				try {
					idf = dh.getCourseComponentDAO().getItemDescriptionFile(em, Long.valueOf(fileID));
				}catch (Exception ingorable) {
				}
				if (idf==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				data.setCourseInstance(idf.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(idf.getCourseComponentItem());
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				File file = new File(getItemDir(idf.getCourseComponentItem()),idf.getFileName());
				
				FileDownloadBean fileBean = new FileDownloadBean();
				fileBean.setFile(file);
				fileBean.setFileName(idf.getFileName());
				fileBean.setMimeType(idf.getMimeType());
				try {
					fileBean.setStream(new BufferedInputStream(new FileInputStream(file),32*1024));
				} catch (Exception e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.internalError"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				fileBean.setLength(file.length());
				
				data.setDownloadBean(fileBean);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}

	public static void newItemAssessment(final CourseComponentData data,
			final String courseComponentItemID, final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_INPUT);
				return null;
			}
		});
		
	}
	
	public static void editItemAssessment(final CourseComponentData data,
			final String componentItemAssessmentID, final ComponentItemAssessmentBean bean, 
			final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItemAssessment ccia = null;
				
				try {
					ccia = dh.getCourseComponentDAO().getItemAssessment(em, Long.valueOf(componentItemAssessmentID));
				}catch (Exception ingorable) {
				}
				if (ccia==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(ccia.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ccia.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(ccia.getCourseComponentItem());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				bean.setId(String.valueOf(ccia.getId()));
				bean.setAssessmentIdentifier(ccia.getAssessmentIdentifier());
				
				data.setItemAssessmentBean(bean);
				
				data.setResult(AbstractActionData.RESULT_INPUT);
				return null;
			}
		});
		
	}
	
	public static void saveItemAssessment(final CourseComponentData data,
			final String courseComponentItemID, final ComponentItemAssessmentBean bean, 
			final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//provjera je li to novi item ili se uredjuje vec postojeci
				CourseComponentItemAssessment ccia = null;
				boolean isNew = true;
				if (!StringUtil.isStringBlank(bean.getId())) {
					try {
						ccia = dh.getCourseComponentDAO().getItemAssessment(em, Long.valueOf(bean.getId()));
					}
					catch (Exception ignorable) {}
					if (ccia==null || !cci.equals(ccia.getCourseComponentItem())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					isNew = false;
				}
				else 
					ccia = new CourseComponentItemAssessment();
				
				if (StringUtil.isStringBlank(bean.getAssessmentIdentifier())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongAssessmentIdentifier"));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				//TODO:
//				//provjera postoji li vec ovaj identifier
//				for (CourseComponentItemAssessment itemAssessment : cci.getItemAssessments()) {
//					if (itemAssessment.getAssessmentIdentifier().equals(bean.getAssessmentIdentifier().trim())) {
//						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.assessmentIdentifierExists"));
//						data.setResult(AbstractActionData.RESULT_INPUT);
//					}
//				}
				
				ccia.setAssessmentIdentifier(bean.getAssessmentIdentifier().trim());
				
				//TODO: dovrsiti
				if (isNew) {
					ccia.setCourseComponentItem(cci);
					cci.getItemAssessments().add(ccia);
					
					Assessment a = new Assessment();
					a.setCourseInstance(data.getCourseInstance());
					a.setName(bean.getAssessmentIdentifier().trim());
					a.setShortName("IA-"+getNextItemAssessmentID(dh,em,data.getCourseInstance()));
					a.setProgram("setPassed(rawPresent());\r\nsetPresent(rawPresent());\r\nsetScore(rawScore());\r\n");
					a.setProgramType("java");
					dh.getAssessmentDAO().save(em, a);
					ccia.setAssessment(a);
					
					dh.getCourseComponentDAO().save(em, ccia);
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
		
	}
	

	public static void autoAssignItemAssessment(final CourseComponentData data, 
			final String componentItemAssessmentID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItemAssessment ccia = null;
				try {
					ccia = dh.getCourseComponentDAO().getItemAssessment(em, Long.valueOf(componentItemAssessmentID));
				}catch (Exception ingorable) {
				}
				if (ccia==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				CourseInstance ci = ccia.getCourseComponentItem().getCourseComponent().getCourseInstance();
				data.setCourseComponent(ccia.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ci);
				data.setCourseComponentItem(ccia.getCourseComponentItem());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
				List<CCIAAssignment> currentUsers = dh.getCourseComponentDAO().getItemAssessmentUsers(em, ccia);
				
				//radimo set usera koji vec postoje na tasku
				Set<User> userSet = new HashSet<User>();
				Map<User,CCIAAssignment> assignmentMap = new HashMap<User, CCIAAssignment>();
				
				for (CCIAAssignment cciaa : currentUsers) {
					userSet.add(cciaa.getUser());
					assignmentMap.put(cciaa.getUser(), cciaa);
				}
				
				//stvaramo one assignmente kojih nema
				for (User u : users) {
					if (!userSet.contains(u)) {
						CCIAAssignment cciaa = new CCIAAssignment();
						
						cciaa.setCourseComponentItemAssessment(ccia);
						cciaa.setUser(u);
						
						dh.getCourseComponentDAO().save(em, cciaa);
					}
					else
						userSet.remove(u);
				}
				
				//brisemo visak korisnika
				CCIAAssignment cciaa;
				for (User u : userSet) {
					cciaa = assignmentMap.get(u);
					cciaa.setUser(null);
					cciaa.setCourseComponentItemAssessment(null);
					
					dh.getCourseComponentDAO().remove(em, cciaa);
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void newTask(final CourseComponentData data, final String courseComponentItemID, 
			 final CourseComponentTaskBean taskBean, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				taskBean.setMaxFilesCount("10");
				taskBean.setMaxFileSize("2");
				
				data.setResult(AbstractActionData.RESULT_INPUT);
				return null;
			}
		});
	}
	
	public static void editTask(final CourseComponentData data, final String courseComponentTaskID,
			final CourseComponentTaskBean taskBean, final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(cct.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cct.getCourseComponentItem());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				fillTaskBean(cct,taskBean);
				
				data.setResult(AbstractActionData.RESULT_INPUT);
				return null;
			}
		});
	}
	

	public static void saveTask(final CourseComponentData data, final String courseComponentItemID,
			 final CourseComponentTaskBean taskBean, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//provjera je li to novi item ili se uredjuje vec postojeci
				CourseComponentTask cct = null;
				boolean isNew = true;
				if (!StringUtil.isStringBlank(taskBean.getId())) {
					try {
						cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(taskBean.getId()));
					}
					catch (Exception ignorable) {}
					if (cct==null || !cci.equals(cct.getCourseComponentItem())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					isNew = false;
				}
				else 
					cct= new CourseComponentTask();
				
				//provjeravamo podatke
				if (!checkTaskBean(dh,em,data,taskBean,cct.getId())) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				//sada spremamo podatke
				dbSaveTask(dh,em,cct,taskBean,isNew,cci,data.getCurrentUser());
				
				em.flush();
				cct.getId();
				data.setCourseComponentTask(cct);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void removeTask(final CourseComponentData data, 
			final String courseComponentTaskID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseInstance(cct.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cct.getCourseComponentItem());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//TODO: taskDescriptionFile
				cct.getReviewers().clear();
				List<CourseComponentTaskAssignment> assignmentList = dh.getCourseComponentDAO().getTaskUsers(em, cct);
				for (CourseComponentTaskAssignment ccta : assignmentList) {
					removeTaskAssignment(em, dh, ccta);
				}
				UserSpecificEvent2 use2 = cct.getDeadline();
				cct.setDeadline(null);
				dh.getEventDAO().remove(em, use2);
				//brisemo direktorij
				getTaskDir(cct).delete();
				
				cct.getCourseComponentItem().getTasks().remove(cct);
				cct.setCourseComponentItem(null);
				dh.getCourseComponentDAO().remove(em, cct);
				
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
		
	}
	
	public static void editReviewers(final CourseComponentData data,
			final String courseComponentTaskID, final List<AssessmentAssistantBean> beanList,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(cct.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cct.getCourseComponentItem());
				data.setCourseComponentTask(cct);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Set<User> currentReviewers = cct.getReviewers();
				List<User> reviewersList = 
					dh.getGroupDAO().listUsersInGroupTree(em, data.getCourseInstance().getId(), JCMSSecurityConstants.SEC_ROLE_GROUP);
				if (reviewersList == null || reviewersList.size()==0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noReviewers"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				for (User u : reviewersList) {
					AssessmentAssistantBean bean = new AssessmentAssistantBean();
					bean.setUserID(String.valueOf(u.getId()));
					bean.setFirstName(u.getFirstName());
					bean.setLastName(u.getLastName());
					if (currentReviewers.contains(u))
						bean.setTaken(Boolean.toString(true));
					else
						bean.setTaken(Boolean.toString(false));
					
					beanList.add(bean);
				}
				
				final Collator myCollator = Collator.getInstance(new Locale("hr"));
				Collections.sort(beanList, new Comparator<AssessmentAssistantBean>() {
					@Override
					public int compare(AssessmentAssistantBean o1, AssessmentAssistantBean o2) {
						int r = myCollator.compare(o1.getLastName(), o2.getFirstName());
						if (r == 0)
							return myCollator.compare(o1.getFirstName(), o2.getFirstName());
						return r;
					}
				});
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		}); 
	}
	
	public static void saveReviewers(final CourseComponentData data,
			final String courseComponentTaskID, final List<AssessmentAssistantBean> beanList,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(cct.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cct.getCourseComponentItem());
				data.setCourseComponentTask(cct);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Set<User> currentReviewers = cct.getReviewers();
				List<User> reviewersList = 
					dh.getGroupDAO().listUsersInGroupTree(em, data.getCourseInstance().getId(), JCMSSecurityConstants.SEC_ROLE_GROUP);
				if (reviewersList == null || reviewersList.size()==0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noReviewers"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//stvaramo mapu asistenata po Id-u
				Map<Long, User> dbReviwersMap= UserUtil.mapUserById(reviewersList);
				
				//provjera jesu li podaci u beanu valjani
				for (AssessmentAssistantBean aab : beanList) {
					User u = null;
					try {
						Boolean.valueOf(aab.getTaken());
						u = dbReviwersMap.get(Long.valueOf(aab.getUserID()));
					} catch (Exception ignorable) {}
					if (u==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				
				//kad smo se uvjerili da je sve ok idemo raditi update
				for (AssessmentAssistantBean aab : beanList) {
					Long id = Long.valueOf(aab.getUserID());
					boolean taken = Boolean.valueOf(aab.getTaken());
					User u = dbReviwersMap.get(id);
					if (!currentReviewers.contains(u) && taken) {
						currentReviewers.add(u);
					}
					else if (currentReviewers.contains(u) && !taken){
						currentReviewers.remove(u);
					}
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				
				return null;
			}
		}); 
	}
	
	public static void viewTaskUsers(final CourseComponentData data,
			final String courseComponentTaskID,
			final List<ReviewersUserTaskBean> userList,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(cct.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cct.getCourseComponentItem());
				data.setCourseComponentTask(cct);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !cct.getReviewers().contains(data.getCurrentUser())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<CourseComponentTaskAssignment> assignments = 
					dh.getCourseComponentDAO().listTaskAssignments(em, cct);

				Set<String> validJMBAGs = getFilteredJmbags(data, em);
				for (CourseComponentTaskAssignment ccta : assignments) {
					if(validJMBAGs!=null && !validJMBAGs.contains(ccta.getUser().getJmbag())) continue;
					ReviewersUserTaskBean bean = new ReviewersUserTaskBean();
					bean.setJmbag(ccta.getUser().getJmbag());
					bean.setFirstName(ccta.getUser().getFirstName());
					bean.setLastName(ccta.getUser().getLastName());
					bean.setAssignmentID(String.valueOf(ccta.getId()));
					bean.setReviewed(String.valueOf(ccta.isReviewed()));
					bean.setLocked(String.valueOf(ccta.isLocked()));
					
					userList.add(bean);
				}
				
				Collections.sort(userList,new Comparator<ReviewersUserTaskBean>() {
					@Override
					public int compare(ReviewersUserTaskBean o1, ReviewersUserTaskBean o2) {
						int r = StringUtil.HR_COLLATOR.compare(o1.getLastName(),o2.getLastName());
						if (r == 0) {
							r = StringUtil.HR_COLLATOR.compare(o1.getFirstName(), o2.getFirstName());
							if (r==0)
								return o1.getJmbag().compareTo(o2.getJmbag());
						}
						return r;
					}
				});
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
			
		});
	}

	private static Set<String> getFilteredJmbags(
			final CourseComponentData data, EntityManager em) {
		Set<String> validJMBAGs = null;
		if(!StringUtil.isStringBlank(data.getFilterGroupID())) {
			Long gid = null;
			try {
				gid = Long.valueOf(data.getFilterGroupID());
				Group g = DAOHelperFactory.getDAOHelper().getGroupDAO().get(em, gid);
				if(g!=null) {
					validJMBAGs = new HashSet<String>(g.getUsers().size()*2);
					for(UserGroup ug : g.getUsers()) {
						validJMBAGs.add(ug.getUser().getJmbag());
					}
				}
			} catch(Exception ex) {
			}
		}
		return validJMBAGs;
	}

	public static void viewAssignmentStatus(final CourseComponentData data,
			final String assignmentID, final TaskReviewBean reviewBean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTaskAssignment ccta = null;
				
				try {
					ccta = dh.getCourseComponentDAO().getTaskAssignment(em, Long.valueOf(assignmentID));
				}catch (Exception ingorable) {
				}
				if (ccta==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(ccta.getCourseComponentTask().getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ccta.getCourseComponentTask().getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(ccta.getCourseComponentTask().getCourseComponentItem());
				data.setCourseComponentTask(ccta.getCourseComponentTask());
				data.setCourseComponentTaskAssignment(ccta);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !ccta.getCourseComponentTask().getReviewers().contains(data.getCurrentUser())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setAssignmentBean(fillAssignmentBean(ccta));
				fillReviewBean(ccta,reviewBean);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}

	public static void reviewAssignment(final CourseComponentData data,
			final String assignmentID, final TaskReviewBean bean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTaskAssignment ccta = null;
				
				try {
					ccta = dh.getCourseComponentDAO().getTaskAssignment(em, Long.valueOf(assignmentID));
				}catch (Exception ingorable) {
				}
				if (ccta==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(ccta.getCourseComponentTask().getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ccta.getCourseComponentTask().getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(ccta.getCourseComponentTask().getCourseComponentItem());
				data.setCourseComponentTask(ccta.getCourseComponentTask());
				data.setCourseComponentTaskAssignment(ccta);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !ccta.getCourseComponentTask().getReviewers().contains(data.getCurrentUser())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (!checkAssignmentReviewBean(data,bean)) {
					data.setAssignmentBean(fillAssignmentBean(ccta));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				if (!StringUtil.isStringBlank(bean.getExtension())) {
					try {
						ccta.setExtensionDate(df.parse(bean.getExtension()));
					} catch (Exception ignorable) {
					}
				}
				ccta.setScore(StringUtil.stringToDouble(bean.getScore()));
				ccta.setPassed(Boolean.valueOf(bean.getPassed()));
				ccta.setComment(bean.getComment());
				boolean reviewed = Boolean.valueOf(bean.getReviewed());
				if (reviewed && !ccta.isReviewed()) {
					ccta.setReviewed(true);
					ccta.setReviewedBy(data.getCurrentUser());
				}
				else if (!reviewed && ccta.isReviewed()){
					ccta.setReviewed(false);
					ccta.setReviewedBy(null);
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}
	
	public static void unlockAssignment(final CourseComponentData data,
			final String assignmentID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTaskAssignment ccta = null;
				
				try {
					ccta = dh.getCourseComponentDAO().getTaskAssignment(em, Long.valueOf(assignmentID));
				}catch (Exception ingorable) {
				}
				if (ccta==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				data.setCourseInstance(ccta.getCourseComponentTask().getCourseComponentItem().getCourseComponent().getCourseInstance());
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !ccta.getCourseComponentTask().getReviewers().contains(data.getCurrentUser())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponentTaskAssignment(ccta);
				
				ccta.setLocked(false);
				ccta.setLockingDate(null);
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}
	
	public static void lockAssignment(final CourseComponentData data,
			final String assignmentID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTaskAssignment ccta = null;
				
				try {
					ccta = dh.getCourseComponentDAO().getTaskAssignment(em, Long.valueOf(assignmentID));
				}catch (Exception ingorable) {
				}
				if (ccta==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(ccta.getCourseComponentTask().getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ccta.getCourseComponentTask().getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(ccta.getCourseComponentTask().getCourseComponentItem());
				data.setCourseComponentTask(ccta.getCourseComponentTask());
				data.setCourseComponentTaskAssignment(ccta);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(ccta.getUser() != data.getCurrentUser()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (!canUserLockAssignment(data,ccta)) {
					ComponentUserTaskBean taskBean = new ComponentUserTaskBean();
					fillUserTaskBean(taskBean,ccta.getCourseComponentTask(),ccta);
					data.setUserTask(taskBean);
					data.setLocked(!canUserModifyUpload(ccta));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				ccta.setLocked(true);
				ccta.setLockingDate(new Date());
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}

	public static void getAllTaskFiles(final CourseComponentData data,
			final String courseComponentTaskID,
			final FileDownloadBean fileBean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(cct.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cct.getCourseComponentItem());
				data.setCourseComponentTask(cct);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !cct.getReviewers().contains(data.getCurrentUser())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}		
				
				Set<String> validJMBAGs = getFilteredJmbags(data, em);
				try {
					File out = FileUtil.zipFolder(getTaskDir(cct), validJMBAGs);
					if (out==null)
						throw new Exception();
					String name = StringUtil.getSafeFileName(cct.getCourseComponentItem().getName() +" - "+ cct.getCourseComponentItem().getName()+".zip");
					fileBean.setFile(out);
					fileBean.setFileName(name);
					fileBean.setLength(out.length());
					fileBean.setMimeType("application/x-zip-compressed");
					fileBean.setStream(new DeleteOnCloseFileInputStream(out));
				} catch (Exception e) {
					e.printStackTrace();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.internalError"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}
	
	public static void autoAssignTask(final CourseComponentData data, 
			final String courseComponentTaskID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				CourseInstance ci = cct.getCourseComponentItem().getCourseComponent().getCourseInstance();
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ci);
				data.setCourseComponentItem(cct.getCourseComponentItem());
				data.setCourseComponentTask(cct);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
				List<CourseComponentTaskAssignment> currentUsers = dh.getCourseComponentDAO().getTaskUsers(em, cct);
				
				//radimo set usera koji vec postoje na tasku
				Set<User> userSet = new HashSet<User>();
				Map<User,CourseComponentTaskAssignment> assignmentMap = new HashMap<User, CourseComponentTaskAssignment>();
				
				for (CourseComponentTaskAssignment ccta : currentUsers) {
					userSet.add(ccta.getUser());
					assignmentMap.put(ccta.getUser(), ccta);
				}
				
				//stvaramo one assignmente kojih nema
				for (User u : users) {
					if (!userSet.contains(u)) {
						CourseComponentTaskAssignment ccta = new CourseComponentTaskAssignment();
						
						ccta.setCourseComponentTask(cct);
						ccta.setEvent(cct.getDeadline());
						ccta.setEventUser(u);
						ccta.setUser(u);
						ccta.setVisible(true);
						
						dh.getCourseComponentDAO().save(em, ccta);
					}
					else
						userSet.remove(u);
				}
				
				//brisemo visak korisnika
				CourseComponentTaskAssignment ccta;
				for (User u : userSet) {
					ccta = assignmentMap.get(u);
					removeTaskAssignment(em, dh, ccta);
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void assignTaskUsers(final CourseComponentData data, 
			final String courseComponentTaskID,
			final List<String> jmbagList,
			final Long userID, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				CourseInstance ci = cct.getCourseComponentItem().getCourseComponent().getCourseInstance();
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ci);
				data.setCourseComponentItem(cct.getCourseComponentItem());
				data.setCourseComponentTask(cct);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (task.equals("new")) {
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				Map<String, User> dbUserMap = UserUtil.mapUserByJmbag(
						dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId())
					);
				List<User> userList = new ArrayList<User>();
				
				//provjera jesu li beanovi ispravani
				for (String jmbag : jmbagList) {
					User u = dbUserMap.get(jmbag);
					if (u==null) {
						String[] param = new String[1];
						param[0] = jmbag;
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchStudent",param));
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
					userList.add(u);
				}
				
				List<CourseComponentTaskAssignment> currentUsers = dh.getCourseComponentDAO().getTaskUsers(em, cct);
				
				//radimo set usera koji vec postoje na tasku
				Set<User> userSet = new HashSet<User>();
				Map<User,CourseComponentTaskAssignment> assignmentMap = new HashMap<User, CourseComponentTaskAssignment>();
				
				for (CourseComponentTaskAssignment ccta : currentUsers) {
					userSet.add(ccta.getUser());
					assignmentMap.put(ccta.getUser(), ccta);
				}
				
				//stvaramo one assignmente kojih nema
				for (User u : userList) {
					if (!userSet.contains(u)) {
						CourseComponentTaskAssignment ccta = new CourseComponentTaskAssignment();
						
						ccta.setCourseComponentTask(cct);
						ccta.setEvent(cct.getDeadline());
						ccta.setEventUser(u);
						ccta.setUser(u);
						ccta.setVisible(true);
						
						dh.getCourseComponentDAO().save(em, ccta);
					}
					else
						userSet.remove(u);
				}
				
				//brisemo visak korisnika
				CourseComponentTaskAssignment ccta;
				for (User u : userSet) {
					ccta = assignmentMap.get(u);
					removeTaskAssignment(em, dh, ccta);
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void viewTaskInfo(final CourseComponentData data,
			final String courseComponentTaskID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(cct.getCourseComponentItem().getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cct.getCourseComponentItem());
				data.setCourseComponentTask(cct);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !cct.getReviewers().contains(data.getCurrentUser())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setAdmin(JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance()));
				data.setStaffMember(cct.getReviewers().contains(data.getCurrentUser()));
				
				CourseComponentTaskBean bean = new CourseComponentTaskBean();
				fillTaskBean(cct, bean);
				data.setTaskBean(bean);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
			
		});
	}
	
	public static void viewTask(final CourseComponentData data, 
			final String courseComponentTaskID, final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//dohvacamo task
				CourseComponentTask cct = null;
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				//punimo data objekt s osnovnim podacima
				CourseInstance ci = cct.getCourseComponentItem().getCourseComponent().getCourseInstance();
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ci);
				data.setCourseComponentItem(cct.getCourseComponentItem());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);

				CourseComponentTaskAssignment ccta = null;
				ccta = dh.getCourseComponentDAO().getAssignmentOnTask(em, cct, data.getCurrentUser());
				
				if(ccta==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//punimo task bean
				ComponentUserTaskBean taskBean = new ComponentUserTaskBean();
				
				fillUserTaskBean(taskBean,cct,ccta);
				
				//stavimo bean u data objekt
				data.setUserTask(taskBean);
				
				//da li trebamo prikazati formu za upload?
				
				data.setLocked(!canUserModifyUpload(ccta));
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	

	public static void uploadTaskFile(final CourseComponentData data, 
			final String taskAssignmentID, final TaskFileUploadBean bean, final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				//TODO: dodati charset u cijelu pricu
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//dohvacamo task 
				CourseComponentTaskAssignment ccta = null;
				try {
					ccta = dh.getCourseComponentDAO().getTaskAssignment(em, Long.valueOf(taskAssignmentID));
				}catch (Exception ingorable) {
				}
				if (ccta==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponentTask(ccta.getCourseComponentTask());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				
				if(ccta.getUser() != data.getCurrentUser()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//provjeravamo ima li bean gresaka
				if (!checkTaskFileUploadBean(data,bean,ccta)) {
					//ako ima, punimo data objekt potrebnim info i vracamo se natrag
					
					CourseComponentItem cci = ccta.getCourseComponentTask().getCourseComponentItem();
					data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
					data.setCourseComponent(cci.getCourseComponent());
					data.setCourseComponentItem(cci);
					ComponentUserTaskBean taskBean = new ComponentUserTaskBean();
					fillUserTaskBean(taskBean,ccta.getCourseComponentTask(),ccta);
					data.setUserTask(taskBean);
					data.setLocked(!canUserModifyUpload(ccta));
					
					return null;
				}
				
				//dohvacamo potreban direktorij i usput stvaramo strukturu datoteka na disku
				try {
					File userTaskDir = getUserTaskDir(ccta);
				
					//kopiramo datoteku
					File resultFile = new File(userTaskDir,bean.getUploadFileName());
					resultFile.createNewFile();
					FileUtils.copyFile(bean.getUpload(), resultFile);
					
					//stvaramo taskUpload
					CourseComponentTaskUpload cctu = new CourseComponentTaskUpload();
					cctu.setCourseComponentTaskAssignment(ccta);
					cctu.setFileName(bean.getUploadFileName());
					cctu.setMimeType(bean.getUploadContentType());
					cctu.setTag(bean.getFileTag());
					cctu.setUploadedOn(new Date());
					
					ccta.getUploads().add(cctu);
					dh.getCourseComponentDAO().save(em, cctu);
					
				} catch (Exception e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.internalError"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
			
		});
	}

	public static void removeTaskFile(final CourseComponentData data, 
			final String taskUploadID,final Long userID) {
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//dohvacamo taskupload
				CourseComponentTaskUpload cctu = null;
				try {
					cctu = dh.getCourseComponentDAO().getTaskUpload(em, Long.valueOf(taskUploadID));
				}catch (Exception ingorable) {
				}
				if (cctu==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponentTask(cctu.getCourseComponentTaskAssignment().getCourseComponentTask());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				
				if(cctu.getCourseComponentTaskAssignment().getUser() != data.getCurrentUser()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if (!canUserModifyUpload(cctu.getCourseComponentTaskAssignment())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.cannotRemove"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//datoteka koju cemo obrisati
				try {
					removeTaskUpload(em,dh,cctu);
				} catch (Exception e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.internalError"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}
	
	
	public static void viewTaskFile(final CourseComponentData data,
			final String taskUploadID, final FileDownloadBean fileBean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//dohvacamo taskupload
				CourseComponentTaskUpload cctu = null;
				try {
					cctu = dh.getCourseComponentDAO().getTaskUpload(em, Long.valueOf(taskUploadID));
				}catch (Exception ingorable) {
				}
				if (cctu==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseComponentTask(cctu.getCourseComponentTaskAssignment().getCourseComponentTask());
				data.setCourseInstance(data.getCourseComponentTask().getCourseComponentItem().getCourseComponent().getCourseInstance());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				
				if(cctu.getCourseComponentTaskAssignment().getUser() != data.getCurrentUser()
						&& !JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !cctu.getCourseComponentTaskAssignment().getCourseComponentTask().getReviewers().contains(data.getCurrentUser())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				File file = new File(getUserTaskDir(cctu.getCourseComponentTaskAssignment()),cctu.getFileName());
				
				fileBean.setFile(file);
				fileBean.setFileName(cctu.getFileName());
				fileBean.setMimeType(cctu.getMimeType());
				try {
					fileBean.setStream(new BufferedInputStream(new FileInputStream(file),32*1024));
				} catch (Exception e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.internalError"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				fileBean.setLength(file.length());
				
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
	}
	
	public static void getUserScore(final CourseComponentData data,
			final String courseComponentItemID, final String groupID, 
			final String jmbag, final EditItemScoresBean bean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				Group g = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
					g = dh.getGroupDAO().get(em, Long.valueOf(groupID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if (g==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				data.setGroup(g);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				Set<Group> groupSet = new HashSet<Group>(
						dh.getGroupDAO().findGroupsOwnedBy(em, data.getCourseInstance().getId(), data.getCurrentUser())
					);
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if((!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !groupSet.contains(g)) || !data.getCourseInstance().getId().equals(g.getCompositeCourseID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Map<String, User> userMap = UserUtil.mapUserByJmbag(
						dh.getCourseInstanceDAO().findCourseUsers(em, data.getCourseInstance().getId())
					);

				data.setOk(false);

				boolean noNewStudent = false;
				
				if (StringUtil.isStringBlank(jmbag)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchStudent",""));
					noNewStudent = true;
					//data.setResult(AbstractActionData.RESULT_SUCCESS);
					//return null;
				}
				
				if (!noNewStudent && userMap.get(jmbag)==null) {
					String param[] = {jmbag};
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchStudent",param));
					noNewStudent = true;
					//data.setResult(AbstractActionData.RESULT_SUCCESS);
					//return null;
				}
				
				data.setOk(true);
				
				Group parent = g.getParent();

				String[] history = StringUtil.isStringBlank(data.getUserSelection()) ? new String[] {} : data.getUserSelection().split(",");
				List<String> list = new ArrayList<String>(history.length+1);
				for(String j : history) {
					j = j.trim();
					if(j.isEmpty()) continue;
					if(userMap.get(j)!=null) {
						list.add(j);
					} else {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchStudent",j));
					}
				}
				if(!noNewStudent && !list.contains(jmbag)) {
					list.add(jmbag);
				}
				List<UserGroup> uglist = new ArrayList<UserGroup>(list.size());
				Map<Long, UserGroup> userGroupMapByUserID = new HashMap<Long, UserGroup>(uglist.size()*2);
				List<User> userList = new ArrayList<User>(list.size());
				StringBuilder sb = new StringBuilder(list.size()*11);
				for(String currentJmbag : list) {
					List<UserGroup> uglist2 = dh.getGroupDAO().findUserGroupsForUser(em, data.getCourseInstance().getId(), parent.getRelativePath(), userMap.get(currentJmbag));
					if(uglist2!=null) {
						uglist.addAll(uglist2);
						for (UserGroup ug : uglist2) {
							userGroupMapByUserID.put(ug.getUser().getId(), ug);
						}
					}
					userList.add(userMap.get(currentJmbag));
					if(sb.length()!=0) {
						sb.append(',');
					}
					sb.append(currentJmbag);
				}
				data.setUserSelection(sb.toString());
				sb = null;
				
				fillItemUserScores(dh,em,data,cci,bean,userList,false,userGroupMapByUserID,false);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		
		});
		
	}
	
	public static void saveUserScore(final CourseComponentData data,
			final String courseComponentItemID, final String groupID, 
			final EditItemScoresBean bean,
			final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		//TODO: kontrola
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentItem cci = null;
				Group g = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
					g = dh.getGroupDAO().get(em, Long.valueOf(groupID));
				}
				catch (Exception ignorable) {}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if (g==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(cci.getCourseComponent().getCourseInstance());
				data.setCourseComponentItem(cci);
				data.setGroup(g);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				Set<Group> groupSet = new HashSet<Group>(
						dh.getGroupDAO().findGroupsOwnedBy(em, data.getCourseInstance().getId(), data.getCurrentUser())
					);
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if((!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())
						&& !groupSet.contains(g)) || !data.getCourseInstance().getId().equals(g.getCompositeCourseID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setOk(true);
				
				List<User> userList = 
					dh.getCourseInstanceDAO().findCourseUsers(em, data.getCourseInstance().getId());
				
				saveItemUserScore(dh,em,data,cci,bean,userList,false);
				return null;
			}
		});
	}

	//////////////////
	//Pomocne metode//
	//////////////////	

	private static void fillItemUserScores(DAOHelper dh, EntityManager em,
			CourseComponentData data, CourseComponentItem cci,
			EditItemScoresBean scoreBean, List<User> users, boolean alphabet, Map<Long, UserGroup> userGroupMapByUserID, boolean sortUsers) {
		
		//svi componentDefovi za ovaj item i odgovarajuci scoreovi za svakog usera
		List<ComponentDefBean> defList = new ArrayList<ComponentDefBean>(cci.getCourseComponentDefs().size());
		List<Map<Long, String>> userScore = new ArrayList<Map<Long,String>>(cci.getCourseComponentDefs().size());
		List<Map<Long, Long>> userScoreVersions = new ArrayList<Map<Long,Long>>(cci.getCourseComponentDefs().size());
		Map<Long, String> assignersMap = new HashMap<Long, String>();
		
		//sortiramo courseComponentDefove
		
		List<AbstractCourseComponentDef> list = new ArrayList<AbstractCourseComponentDef>(cci.getCourseComponentDefs());
		
		//sortiramo po positionu pa po imenu
		Collections.sort(list, new Comparator<AbstractCourseComponentDef> () {
			@Override
			public int compare(AbstractCourseComponentDef o1, AbstractCourseComponentDef o2) {
				if (o1.getPosition() != o2.getPosition())
					return o1.getPosition()-o2.getPosition();
				String o1name = "", o2name = "";
				if (o1 instanceof CourseComponentFDef)
					o1name = ((CourseComponentFDef)o1).getAssessmentFlag().getName();
				else if (o1 instanceof CourseComponentADef)
					o1name = ((CourseComponentADef)o1).getAssessment().getName();
				if (o2 instanceof CourseComponentFDef)
					o2name = ((CourseComponentFDef)o2).getAssessmentFlag().getName();
				else if (o2 instanceof CourseComponentADef)
					o2name = ((CourseComponentADef)o2).getAssessment().getName();
				return StringUtil.HR_COLLATOR.compare(o1name, o2name);
			}
		});
		
		for (AbstractCourseComponentDef accd : list) {
			
			ComponentDefBean bean = new ComponentDefBean();
			bean.setPosition(accd.getPosition());
			bean.setId(String.valueOf(accd.getId()));
			if (accd instanceof CourseComponentFDef) {
				CourseComponentFDef def = (CourseComponentFDef) accd;
				
				bean.setName(def.getAssessmentFlag().getName());
				bean.setType("boolean");
				//bean.setId(String.valueOf(def.getAssessmentFlag().getId()));
				
				List<AssessmentFlagValue> tmpFlags = dh.getAssessmentDAO().listFlagValuesForAssessmentFlag(em, def.getAssessmentFlag());
				Map<Long,String> result = new HashMap<Long, String>();
				Map<Long,Long> versions = new HashMap<Long, Long>();
				for (AssessmentFlagValue afv : tmpFlags) {
					versions.put(afv.getUser().getId(), Long.valueOf(afv.getVersion()));
					result.put(afv.getUser().getId(), String.valueOf(afv.getValue()));
					if (afv.getAssigner()!= null)
						assignersMap.put(afv.getUser().getId(), afv.getAssigner().getLastName()+" "+afv.getAssigner().getFirstName());
				}
				userScore.add(result);
				userScoreVersions.add(versions);
			}
			if (accd instanceof CourseComponentADef) {
				CourseComponentADef def = (CourseComponentADef) accd;
				bean.setName(def.getAssessment().getName());
				if (def.getAssessment().getAssessmentConfiguration() instanceof AssessmentConfEnum) {
					AssessmentConfEnum conf = (AssessmentConfEnum)def.getAssessment().getAssessmentConfiguration();
					bean.setName(def.getAssessment().getName());
					bean.setType("enum");
					//bean.setId(String.valueOf(def.getAssessment().getAssessmentConfiguration()));
					bean.setStart(conf.getIntervalStart());
					bean.setEnd(conf.getIntervalEnd());
					bean.setStep(conf.getStep());
				}
				if (def.getAssessment().getAssessmentConfiguration() instanceof AssessmentConfRange) {
					AssessmentConfRange conf = (AssessmentConfRange)def.getAssessment().getAssessmentConfiguration();
					bean.setName(def.getAssessment().getName());
					bean.setType("range");
					//bean.setId(String.valueOf(def.getAssessment().getAssessmentConfiguration()));
					bean.setStart(conf.getRangeStart());
					bean.setEnd(conf.getRangeEnd());
				}
				
				List<AssessmentScore> tmpScores = dh.getAssessmentDAO().listScoresForAssessment(em, def.getAssessment());
				Map<Long,String> result = new HashMap<Long, String>();
				Map<Long,Long> versions = new HashMap<Long, Long>();
				for (AssessmentScore as : tmpScores) {
					versions.put(as.getUser().getId(), Long.valueOf(as.getVersion()));
					if (as.getRawPresent()) {
						result.put(as.getUser().getId(),String.valueOf(as.getRawScore()));
						if (as.getAssigner()!= null)
							assignersMap.put(as.getUser().getId(), as.getAssigner().getLastName()+" "+as.getAssigner().getFirstName());
					}
				}
				userScore.add(result);
				userScoreVersions.add(versions);
			}
			
			defList.add(bean);
		}
		
		scoreBean.setScoreTypeList(defList);
		
		//svi useri koji su na tom itemu
		if(sortUsers) Collections.sort(users,StringUtil.USER_COMPARATOR);
		
		if (alphabet) {
			Set<Character> letterSet = new HashSet<Character>();
			
			//gledamo koja su sva slova prisutna
			for (User u : users)
				letterSet.add(u.getLastName().charAt(0));
			
			List<String> stringLetters = new ArrayList<String>(letterSet.size());
			for (Character c : letterSet) {
				stringLetters.add(c.toString());
			}
			Collections.sort(stringLetters, StringUtil.HR_COLLATOR);
			data.setLetters(stringLetters);
			
			//gledamo izabrano slovo
			String letter = scoreBean.getLetter();			
			if(StringUtil.isStringBlank(letter) && stringLetters.size()> 0 || letter.length()>1) {
				letter = String.valueOf(stringLetters.get(0).charAt(0));
			}
			
			// Ako nema slova, to znači da nema niti jednog korisnika...
			if(StringUtil.isStringBlank(letter)) {
				scoreBean.setScoreList(new ArrayList<ItemScoreBean>());
				return ;
			}
			
			scoreBean.setLetter(letter);
		}
		
		//pripremamo usere s trazenim slovom
		List<ItemScoreBean> userList = new ArrayList<ItemScoreBean>(); 
		
		for (User u : users) {
			if (!alphabet || u.getLastName().charAt(0)==scoreBean.getLetter().charAt(0)) {
				ItemScoreBean bean = new ItemScoreBean();
				bean.setFirstName(u.getFirstName());
				bean.setLastName(u.getLastName());
				bean.setJmbag(u.getJmbag());
				bean.setId(String.valueOf(u.getId()));
				UserGroup ug = userGroupMapByUserID==null ? null : userGroupMapByUserID.get(u.getId());
				if(ug==null) {
					bean.setTag("");
				} else {
					bean.setTag(ug.getTag());
				}
				String assigner = null;
				assigner = assignersMap.get(u.getId());
				if (assigner==null)
					bean.setAssignedBy("");
				else
					bean.setAssignedBy(assigner);
				
				List<Long> versions = new ArrayList<Long>(userScoreVersions.size());
				List<String> scores = new ArrayList<String>(userScore.size());
				for (Map<Long, String> map: userScore) {
					
					String r = map.get(u.getId());
					if (r == null)
						scores.add("");
					else
						scores.add(r);
				}
				for (Map<Long, Long> map: userScoreVersions) {
					
					Long r = map.get(u.getId());
					if (r == null)
						versions.add(Long.valueOf(0));
					else
						versions.add(r);
				}
				bean.setScores(scores);
				bean.setOScores(new ArrayList<String>(scores));
				bean.setVersions(versions);
				
				userList.add(bean);
			}
		}
		scoreBean.setScoreList(userList);
	}
	
	private static void saveItemUserScore(DAOHelper dh, EntityManager em,
			CourseComponentData data, CourseComponentItem cci,
			EditItemScoresBean bean, List<User> users, boolean alphabet) {
		
		//dohvacamo sve usere
		List<User> selUsers = new ArrayList<User>();
		Map<Long, User> userMap = UserUtil.mapUserById(users);
		Map<Long, Map<Long, String>> userOScoresMap = new HashMap<Long, Map<Long,String>>(bean.getScoreList().size());
		Map<Long, Map<Long, String>> userScoresMap = new HashMap<Long, Map<Long,String>>(bean.getScoreList().size());
		Map<Long, Map<Long, Long>> userScoreVersionsMap = new HashMap<Long, Map<Long,Long>>(bean.getScoreList().size());
		Map<String, AbstractCourseComponentDef> defMap = new HashMap<String, AbstractCourseComponentDef>(cci.getCourseComponentDefs().size());
		
		//napravimo mapu
		for (AbstractCourseComponentDef accd : cci.getCourseComponentDefs()) {
			defMap.put(String.valueOf(accd.getId()), accd);
		}
		Set<String> validUserFilter = new HashSet<String>(bean.getScoreList().size()*2);
		
		//provjera podataka
		boolean ok = true;
		for (ItemScoreBean isb : bean.getScoreList()) {
			User u = null;
			try {
				u = userMap.get(Long.valueOf(isb.getId()));
			} catch (Exception ignorable) {
			}
			if (u==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return ;
			}
			validUserFilter.add(u.getJmbag());
			//ako nismo primili dobar broj rezultata
			if (isb.getScores().size() != bean.getScoreTypeList().size() || isb.getScores().size() != defMap.size()) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return ;
			}
			//punimo bean za prikaz
			isb.setFirstName(u.getFirstName());
			isb.setLastName(u.getLastName());
			isb.setJmbag(u.getJmbag());
			
			Iterator<Long> versionIterator = isb.getVersions().iterator();
			Iterator<String> scoreIterator = isb.getScores().iterator();
			Iterator<String> oscoreIterator = isb.getOScores().iterator();
			Iterator<ComponentDefBean> compIterator = bean.getScoreTypeList().iterator();
			
			Map<Long, String> oscores = new HashMap<Long, String>(isb.getOScores().size());
			Map<Long, String> scores = new HashMap<Long, String>(isb.getScores().size());
			Map<Long, Long> versions = new HashMap<Long, Long>(isb.getVersions().size());
			while (scoreIterator.hasNext()) {
				Long v = versionIterator.next();
				String s = scoreIterator.next();
				String os = oscoreIterator.next();
				String id = compIterator.next().getId(); 
				
				if (StringUtil.isStringBlank(id) || defMap.get(id) == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return ;
				}
				
				AbstractCourseComponentDef def = defMap.get(id);
				
				if (!isScoreValid(data,s,def)) {
					ok = false;
					isb.setError(true);
				}
				else {
					oscores.put(def.getId(),os);
					scores.put(def.getId(),s);
					versions.put(def.getId(),v);
				}
			}
			selUsers.add(u);
			userOScoresMap.put(u.getId(), oscores);
			userScoresMap.put(u.getId(), scores);
			userScoreVersionsMap.put(u.getId(), versions);
		}

		// Dohvat i provjera history-ja, ako se koristi - pocetak
		String[] history = StringUtil.isStringBlank(data.getUserSelection()) ? new String[] {} : data.getUserSelection().split(",");
		List<String> list = new ArrayList<String>(history.length);
		for(String j : history) {
			j = j.trim();
			if(j.isEmpty()) continue;
			if(validUserFilter.contains(j)) {
				list.add(j);
			} else {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noSuchStudent",j));
			}
		}
		StringBuilder sb = new StringBuilder(list.size()*11);
		for(String currentJmbag : list) {
			if(sb.length()!=0) {
				sb.append(',');
			}
			sb.append(currentJmbag);
		}
		data.setUserSelection(sb.toString());
		sb = null;
		// Dohvat i provjera history-ja, ako se koristi - kraj

		data.setJmbag(selUsers.get(0).getJmbag());
		
		if (!ok) {
			
			if (alphabet) {
				Set<Character> letterSet = new HashSet<Character>();
				
				//gledamo koja su sva slova prisutna
				for (User u : users)
					letterSet.add(u.getLastName().charAt(0));
				
				List<String> stringLetters = new ArrayList<String>(letterSet.size());
				for (Character c : letterSet) {
					stringLetters.add(c.toString());
				}
				Collections.sort(stringLetters, StringUtil.HR_COLLATOR);
				data.setLetters(stringLetters);
				
				//gledamo izabrano slovo
				String letter = bean.getLetter();			
				if(StringUtil.isStringBlank(letter) && stringLetters.size()> 0 || letter.length()>1) {
					letter = String.valueOf(stringLetters.get(0).charAt(0));
				}
				
				// Ako nema slova, to znači da nema niti jednog korisnika...
				if(StringUtil.isStringBlank(letter)) {
					bean.setScoreList(new ArrayList<ItemScoreBean>());
					return ;
				}
				
				bean.setLetter(letter);
			}
			
			List<ComponentDefBean> defList = new ArrayList<ComponentDefBean>(cci.getCourseComponentDefs().size());
			for (ComponentDefBean cdf : bean.getScoreTypeList()) {
				defList.add(getComponentDefBean(defMap.get(cdf.getId())));
			}
			bean.setScoreTypeList(defList);
			data.setResult(AbstractActionData.RESULT_INPUT);
			return ;
		}
		
		//punimo rezultate
		for (ComponentDefBean cdf : bean.getScoreTypeList()) {
			AbstractCourseComponentDef accd = defMap.get(cdf.getId());
			
			if (accd instanceof CourseComponentFDef) {
				CourseComponentFDef def = (CourseComponentFDef) accd;
				Map<Long, AssessmentFlagValue> fvMap = 
					AssessmentUtil.mapAssessmentFlagValueByUserID(def.getAssessmentFlag().getValues());
				for (User u : selUsers) {
					//ako je korisnik nesto unio
					String scoreString = userScoresMap.get(u.getId()).get(def.getId());
					String oscoreString = userOScoresMap.get(u.getId()).get(def.getId());
					if (!StringUtil.isStringBlank(scoreString)) {
						AssessmentFlagValue value = fvMap.get(u.getId());
						boolean fVal = Boolean.valueOf(scoreString);
						if (value==null && fVal) {
							value = new AssessmentFlagValue();
							value.setAssessmentFlag(def.getAssessmentFlag());
							value.setValue(fVal);
							value.setManualValue(fVal);
							value.setManuallySet(true);
							value.setUser(u);
							value.setAssigner(data.getCurrentUser());
							
							dh.getAssessmentDAO().save(em, value);
							def.getAssessmentFlag().getValues().add(value);
						}
						else if (value != null) {
							// Jesam li JA napravio promjenu u podacima koje sam dohvatio iz baze?
							boolean change = !StringUtil.stringEqualsLoosly(scoreString, oscoreString);
							if(change) {
								Long scoreVersion = userScoreVersionsMap.get(u.getId()).get(def.getId());
								long lScoreVersion = scoreVersion==null ? 0L : scoreVersion.longValue();
								boolean azuriraj = false;
								// Ako je moja zapamcena verzija veca li jednaka pohranjenoj u bazi, tada pohrani ako ima razlike
								if(lScoreVersion>=value.getVersion()) {
									azuriraj = fVal != value.getValue();
								} else {
									// Inace je u bazi novija verzija; ako su podaci razliciti, azuriraj
									azuriraj = fVal != value.getValue();
									// ali samo ako time ne brises zastavicu:
									if(azuriraj) {
										if(!fVal) {
											azuriraj = false;
											// Dodaj poruku da preskaces
											data.getMessageLogger().addWarningMessage("Korisnik "+u.getJmbag()+": preskoceno brisanje zastavice zbog paralelne promjene u bazi.");
										}
									}
								}
								if(azuriraj) {
									value.setValue(fVal);
									value.setManualValue(fVal);
									value.setManuallySet(true);
									value.setAssigner(data.getCurrentUser());
								}
							}
						}
					}
				}
			}
			if (accd instanceof CourseComponentADef) {
				CourseComponentADef def = (CourseComponentADef) accd;
				Map<Long, AssessmentScore> avMap = AssessmentUtil.mapAssessmentScoreByUserID(def.getAssessment().getScore());
				for (User u : selUsers) {
					//ako je korisnik nesto unio
					String scoreString = userScoresMap.get(u.getId()).get(def.getId());
					String oscoreString = userOScoresMap.get(u.getId()).get(def.getId());
					if (!StringUtil.isStringBlank(scoreString)) {
						AssessmentScore value = avMap.get(u.getId());
						double sVal = StringUtil.stringToDouble(scoreString);
						if (value==null) {
							value = new AssessmentScore();
							value.setAssessment(def.getAssessment());
							value.setAssigner(data.getCurrentUser());
							value.setRawPresent(true);
							value.setRawScore(sVal);
							value.setStatus(AssessmentStatus.PASSED);
							value.setUser(u);
							
							dh.getAssessmentDAO().save(em, value);
							def.getAssessment().getScore().add(value);
						}
						else {
							// Jesam li JA napravio promjenu u podacima koje sam dohvatio iz baze?
							boolean change = !StringUtil.stringEqualsLoosly(scoreString, oscoreString);
							if(change) {
								Long scoreVersion = userScoreVersionsMap.get(u.getId()).get(def.getId());
								long lScoreVersion = scoreVersion==null ? 0L : scoreVersion.longValue();
								boolean azuriraj = false;
								boolean difference = Math.abs(sVal-value.getRawScore()) > 1E-6;
								// Ako je moja zapamcena verzija veca li jednaka pohranjenoj u bazi, tada pohrani ako ima razlike
								if(lScoreVersion>=value.getVersion()) {
									azuriraj = difference || !value.getRawPresent();
								} else {
									// Moja je verzija manja, pa je u bazi novije stanje
									azuriraj = difference || !value.getRawPresent();
									if(!value.getRawPresent()) {
										data.getMessageLogger().addWarningMessage("Korisnik "+u.getJmbag()+": bodovi su upisani iako je u bazi zateceno novije stanje koje je bez bodova.");
									} else {
										azuriraj = false;
										data.getMessageLogger().addWarningMessage("Korisnik "+u.getJmbag()+": bodovi nisu upisani jer je u bazi zateceno novije stanje.");
									}
								}
								if (azuriraj){
									value.setAssessment(def.getAssessment());
									value.setAssigner(data.getCurrentUser());
									value.setRawPresent(true);
									value.setRawScore(sVal);
									value.setStatus(AssessmentStatus.PASSED);
								}
							}
						}
					}
					else {
						AssessmentScore value = avMap.get(u.getId());
						if (value!=null) {
							Long scoreVersion = userScoreVersionsMap.get(u.getId()).get(def.getId());
							long lScoreVersion = scoreVersion==null ? 0L : scoreVersion.longValue();
							boolean azuriraj = false;
							// Ako je moja zapamcena verzija veca li jednaka pohranjenoj u bazi, tada pohrani ako ima razlike
							if(lScoreVersion>=value.getVersion()) {
								azuriraj = true;
							} else {
								// Moja je verzija manja, pa je u bazi novije stanje
								azuriraj = false;
								data.getMessageLogger().addWarningMessage("Korisnik "+u.getJmbag()+": preskoceno brisanje bodova zbog promjene u bazi.");
							}
							if(azuriraj) {
								value.setRawPresent(false);
								value.setAssigner(null);
								value.setRawScore(0);
							}
						}
					}
				}
			}
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		if(!StringUtil.isStringBlank(data.getUserSelection())) {
			data.getMessageLogger().getMessageContainer().addPrivateData("userHistory", data.getUserSelection());
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	private static ComponentDefBean getComponentDefBean(
			AbstractCourseComponentDef accd) {
		
		ComponentDefBean defBean = new ComponentDefBean();
		
		defBean.setId(String.valueOf(accd.getId()));
		if (accd instanceof CourseComponentFDef) {
			CourseComponentFDef def = (CourseComponentFDef) accd;
			
			defBean.setName(def.getAssessmentFlag().getName());
			defBean.setType("boolean");
			//defBean.setId(String.valueOf(def.getAssessmentFlag().getId()));
		}
		if (accd instanceof CourseComponentADef) {
			CourseComponentADef def = (CourseComponentADef) accd;
			defBean.setName(def.getAssessment().getName());
			if (def.getAssessment().getAssessmentConfiguration() instanceof AssessmentConfEnum) {
				AssessmentConfEnum conf = (AssessmentConfEnum)def.getAssessment().getAssessmentConfiguration();
				defBean.setName(def.getAssessment().getName());
				defBean.setType("enum");
				//defBean.setId(String.valueOf(def.getAssessment().getAssessmentConfiguration()));
				defBean.setStart(conf.getIntervalStart());
				defBean.setEnd(conf.getIntervalEnd());
				defBean.setStep(conf.getStep());
			}
			if (def.getAssessment().getAssessmentConfiguration() instanceof AssessmentConfRange) {
				AssessmentConfRange conf = (AssessmentConfRange)def.getAssessment().getAssessmentConfiguration();
				defBean.setName(def.getAssessment().getName());
				defBean.setType("range");
				//defBean.setId(String.valueOf(def.getAssessment().getAssessmentConfiguration()));
				defBean.setStart(conf.getRangeStart());
				defBean.setEnd(conf.getRangeEnd());
			}
		}
		return defBean;
	}
	
	private static void removeTaskAssignment(EntityManager em,
			DAOHelper dh, CourseComponentTaskAssignment ccta) {
		
		ccta.getEvent().getUsers().remove(ccta);
		ccta.setEvent(null);
		ccta.setEventUser(null);
		ccta.setReviewedBy(null);
		
		//brisemo upload
		List<CourseComponentTaskUpload> list = new ArrayList<CourseComponentTaskUpload>(ccta.getUploads());
		for (CourseComponentTaskUpload cctu : list)
			removeTaskUpload(em, dh, cctu);

		//brisemo folder
		File rmFile = getUserTaskDir(ccta);
		rmFile.delete();
		
		ccta.setCourseComponentTask(null);
		dh.getCourseComponentDAO().remove(em, ccta);
	}

	private static void removeTaskUpload(EntityManager em,
			DAOHelper dh, CourseComponentTaskUpload cctu) {
		
		File rmFile = new File(getUserTaskDir(cctu.getCourseComponentTaskAssignment()),cctu.getFileName());
		//brisemo prvo datoteku
		rmFile.delete();
		//pa onda i objekt iz baze
		cctu.getCourseComponentTaskAssignment().getUploads().remove(cctu);
		cctu.setCourseComponentTaskAssignment(null);
		dh.getCourseComponentDAO().remove(em, cctu);
		
	}
	
	private static boolean isScoreValid(CourseComponentData data,
			String score, AbstractCourseComponentDef def) {
		
		if (StringUtil.isStringBlank(score))
			return true;
		if (def instanceof CourseComponentFDef) {
			try {
				Boolean.valueOf(score);
			} catch (Exception e) {
				String[] param = {score};
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongFlagFormat",param));
				return false;
			}
		}
		if (def instanceof CourseComponentADef) {
			double value;
			try {
				value = StringUtil.stringToDouble(score);
			} catch (Exception e) {
				String[] param = {score};
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongScoreFormat",param));
				return false;
			}
			
			AssessmentConfiguration conf = 
				((CourseComponentADef)def).getAssessment().getAssessmentConfiguration();
			if (conf instanceof AssessmentConfRange) {
				AssessmentConfRange currConf = (AssessmentConfRange)conf;
				if (value < currConf.getRangeStart() || value > currConf.getRangeEnd()) {
					String[] param = {score};
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.outOfRange",param));
					return false;
				}
			}
			if (conf instanceof AssessmentConfEnum) {
				AssessmentConfEnum currConf = (AssessmentConfEnum)conf;
				if (value < currConf.getIntervalStart() || value > currConf.getIntervalEnd()) {
					String[] param = {score};
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.outOfRange",param));
					return false;
				}
			}
		}
		return true;
	}
	
	private static String getNextItemAssessmentID(DAOHelper dh, EntityManager em, CourseInstance courseInstance) {
		
		List<Assessment> aList = dh.getAssessmentDAO().listForCourseInstance(em,courseInstance.getId());
		int num = 0;
		for (Assessment a : aList) {
			if (a.getShortName().startsWith("IA-")) {
				String tmp = a.getShortName().split("-")[1];
				int cur = 0;
				try {
					cur = Integer.valueOf(tmp);
				} catch (Exception e) {
				}
				if (cur >= num)
					num = cur+1;
			}
		}
		
		return String.valueOf(num);
	}
	
	private static ComponentTaskAssignmentBean fillAssignmentBean(CourseComponentTaskAssignment ccta) {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		ComponentTaskAssignmentBean bean = new ComponentTaskAssignmentBean();
		
		bean.setFirstName(ccta.getUser().getFirstName());
		bean.setLastName(ccta.getUser().getLastName());
		bean.setJmbag(ccta.getUser().getJmbag());
		bean.setId(String.valueOf(ccta.getId()));
		
		bean.setLocked(ccta.isLocked());
		if (ccta.getLockingDate()!=null && ccta.isLocked()) {
			bean.setLockingDate(df.format(ccta.getLockingDate()));
		}
		
		List <TaskFileBean> beanList = new ArrayList<TaskFileBean>(ccta.getUploads().size());
		for (CourseComponentTaskUpload cctu : ccta.getUploads()) {
			TaskFileBean fileBean = new TaskFileBean();
			fileBean.setId(String.valueOf(cctu.getId()));
			fileBean.setFileName(cctu.getFileName());
			fileBean.setTag(cctu.getTag());
			fileBean.setUploadDate(df.format(cctu.getUploadedOn()));
			
			beanList.add(fileBean);
		}
		
		bean.setFileList(beanList);
		
		return bean;
	}
	
	private static void fillReviewBean(CourseComponentTaskAssignment ccta,
			TaskReviewBean reviewBean) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		reviewBean.setReviewed(String.valueOf(ccta.isReviewed()));
		reviewBean.setComment(ccta.getComment());
		if (ccta.getExtensionDate()!=null) {
			reviewBean.setExtension(df.format(ccta.getExtensionDate()));
		}
		reviewBean.setPassed(String.valueOf(ccta.isPassed()));
		DecimalFormat formatter = new DecimalFormat("0.00");
		reviewBean.setScore(formatter.format(ccta.getScore()));
	}
	
	private static void prepareItemView(CourseComponentData data,
			DAOHelper dh, EntityManager em, CourseComponentItem cci) {
		
		//da li je to administrator
		data.setAdmin(
				JCMSSecurityManagerFactory.getManager()
				.canPerformCourseAdministration(data.getCourseInstance())
				);
		data.setStaffMember(dh.getCourseInstanceDAO().isCourseStaffMember(em, data.getCourseInstance(), data.getCurrentUser()));
		data.setCourseComponentItem(cci);

		Collection<CourseComponentTask> tasks = null;
		Collection<CourseComponentItemAssessment> itemAssessments = null;
		
		//ako je admin onda dohvacamo sve taskove/assessmente na itemu)
		if (data.isAdmin()) {
			tasks = cci.getTasks();
			itemAssessments = cci.getItemAssessments();
		}
		else if (data.isStaffMember()) {
			//ako nije admin ali pripada osoblju kolegija
			tasks = dh.getCourseComponentDAO().findTasksForReviewer(em, cci, data.getCurrentUser());
			itemAssessments = new ArrayList<CourseComponentItemAssessment>();
			//TODO: reorganizirati
		}
		else {
			//inace dohvacamo listu taskova/assessmenta na zadanom itemu za korisnika
			tasks = dh.getCourseComponentDAO().findUserTasksOnItem(em,cci,data.getCurrentUser());
			itemAssessments = dh.getCourseComponentDAO().findUserAssessmentsOnItem(em,cci,data.getCurrentUser());
		}
		
		
		List<CourseComponentTaskBean> beanList = 
			new ArrayList<CourseComponentTaskBean>(tasks.size());
		List<ComponentItemAssessmentBean> assessmentList =
			new ArrayList<ComponentItemAssessmentBean>(itemAssessments.size());
		List<FileBean> fileList = new ArrayList<FileBean>(cci.getItemDescriptionFiles().size());
		List<ComponentDefBean> defList = new ArrayList<ComponentDefBean>(cci.getCourseComponentDefs().size());
		
		//pripremamo listu svih taskova
		for (CourseComponentTask cct : tasks) {
			CourseComponentTaskBean bean = new CourseComponentTaskBean();
			bean.setId(String.valueOf(cct.getId()));
			bean.setTitle(cct.getTitle());
			
			beanList.add(bean);
		}
		
		Collections.sort(beanList, new Comparator<CourseComponentTaskBean>() {
			@Override
			public int compare(CourseComponentTaskBean o1, CourseComponentTaskBean o2) {
				if(o1==null) {
					if(o2==null) return 0;
					return -1;
				}
				if(o2==null) return 1;
				if(o1.getTitle()==null) {
					if(o2.getTitle()==null) return 0;
					return -1;
				}
				if(o2.getTitle()==null) return 1;
				return StringUtil.HR_COLLATOR.compare(o1.getTitle(), o2.getTitle());
			}
		});

		if(JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance())) {
			// Student je. Pronadi u kojim je sve podgrupama...
			List<Group> studentsGroups = dh.getGroupDAO().findSubGroupsForUser(em, data.getCourseInstance().getId(),
						cci.getCourseComponent().getDescriptor().getGroupRoot()+"/"+String.valueOf(cci.getPosition()),data.getCurrentUser());
			Date now = new Date();
			// Sortiraj grupe po imenima:
			Collections.sort(studentsGroups, StringUtil.GROUP_COMPARATOR);
			List<CourseComponentData.TermAssisstantData> termAssistants = new ArrayList<CourseComponentData.TermAssisstantData>();
			// Idemo za svaku grupu:
			for(Group g : studentsGroups) {
				// 1. pronadi najkasniji događaj te grupe:
				List<GroupWideEvent> ev = new ArrayList<GroupWideEvent>(g.getEvents());
				if(!ev.isEmpty()) Collections.sort(ev, eventsByStartComparator);
				// Ako nema događaja, ili ako je now>=last.start
				List<User> assistants = new ArrayList<User>();
				if(ev.isEmpty() || !now.before(ev.get(ev.size()-1).getStart())) {
					List<GroupOwner> groupOwners = dh.getGroupDAO().findForGroup(em, g);
					for(GroupOwner go : groupOwners) {
						assistants.add(go.getUser());
					}
					Collections.sort(assistants, StringUtil.USER_COMPARATOR);
				}
				CourseComponentData.TermAssisstantData ta = new CourseComponentData.TermAssisstantData(g,ev,assistants);
				termAssistants.add(ta);
			}
			data.setTermAssistants(termAssistants);
		}
		if (data.isStaffMember()) {
			List<GroupOwner> list = 
				dh.getGroupDAO().findForSubgroupsAndUser(em, data.getCourseInstance().getId(),
						cci.getCourseComponent().getDescriptor().getGroupRoot()+"/"+String.valueOf(cci.getPosition()),data.getCurrentUser());
			List<GroupBean> groupList = new ArrayList<GroupBean>(list.size());
			for (GroupOwner o : list) {
				GroupBean bean = new GroupBean();
				bean.setName(o.getGroup().getName());
				bean.setId(o.getGroup().getId());
				
				groupList.add(bean);
			}
			Collections.sort(groupList,new Comparator<GroupBean>() {
				@Override
				public int compare(GroupBean o1, GroupBean o2) {
					return StringUtil.HR_COLLATOR.compare(o1.getName(), o2.getName());
				}
			});
			data.setGroupList(groupList);
		}
		//pripremamo listu svih itemAssessmenta
		for (CourseComponentItemAssessment ccia : itemAssessments) {
			ComponentItemAssessmentBean bean = new ComponentItemAssessmentBean();
			bean.setId(String.valueOf(ccia.getId()));
			bean.setAssessmentIdentifier(ccia.getAssessmentIdentifier());
			
			assessmentList.add(bean);
		}
		
		//pripremamo listu svih fajlova
		for (ItemDescriptionFile idf : cci.getItemDescriptionFiles()) {
			FileBean bean = new FileBean();
			bean.setId(String.valueOf(idf.getId()));
			bean.setFileName(idf.getFileName());
			
			fileList.add(bean);
		}
		
		Collections.sort(fileList, new Comparator<FileBean>() {
			@Override
			public int compare(FileBean o1, FileBean o2) {
				if(o1==null) {
					if(o2==null) return 0;
					return -1;
				}
				if(o2==null) return 1;
				if(o1.getFileName()==null) {
					if(o2.getFileName()==null) return 0;
					return -1;
				}
				if(o2.getFileName()==null) return 1;
				return StringUtil.HR_COLLATOR.compare(o1.getFileName(), o2.getFileName());
			}
		});
		//TODO: nije genericko
		//pripremamo listu componentDefova
		for (AbstractCourseComponentDef accd : cci.getCourseComponentDefs()) {
			ComponentDefBean bean = new ComponentDefBean();
			bean.setId(String.valueOf(accd.getId()));
			bean.setPosition(accd.getPosition());
			if (accd instanceof CourseComponentFDef) {
				CourseComponentFDef def = (CourseComponentFDef)accd;
				bean.setName(def.getAssessmentFlag().getName());
			}
			if (accd instanceof CourseComponentADef) {
				CourseComponentADef def = (CourseComponentADef)accd;
				bean.setName(def.getAssessment().getName());
			}
			
			defList.add(bean);
		}
		//sortiramo listu componentDefova po positionu pa po imenu
		Collections.sort(defList, new Comparator<ComponentDefBean> () {
			@Override
			public int compare(ComponentDefBean o1, ComponentDefBean o2) {
				if (o1.getPosition()!=o2.getPosition())
					return o1.getPosition()-o2.getPosition();
				return StringUtil.HR_COLLATOR.compare(o1.getName(), o2.getName());
			}
		});
		
		data.setDefList(defList);
		data.setTaskList(beanList);
		data.setItemAssessmentsList(assessmentList);
		data.setItemFiles(fileList);
	}
	
	private static void fillUserTaskBean(ComponentUserTaskBean taskBean,
			CourseComponentTask cct, CourseComponentTaskAssignment ccta) {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		taskBean.setComment(ccta.getComment());
		if (cct.getDeadline()!=null) {
			if (cct.getDeadline().isHidden())
				taskBean.setDeadline(null);
			else
				taskBean.setDeadline(df.format(cct.getDeadline().getStart()));
		}
		taskBean.setDescription(cct.getDescription());
		if (ccta.getExtensionDate()!=null) {
			taskBean.setExtensionDate(df.format(ccta.getExtensionDate()));
		}
		Map<String, String> tagMap = createTagMap(cct.getFileTags(), true);
		if (cct.getFilesRequiredCount()==tagMap.size() || StringUtil.isStringBlank(cct.getFileTags()))
			taskBean.setFileTags(tagMap);
		else
			taskBean.setFileTags(createTagMap(cct.getFileTags(), false));
		taskBean.setLocked(ccta.isLocked());
		if (ccta.getLockingDate()!=null && ccta.isLocked()) {
			taskBean.setLockingDate(df.format(ccta.getLockingDate()));
		}
		taskBean.setMaxFilesCount(String.valueOf(cct.getMaxFilesCount()));
		taskBean.setMaxFileSize(String.valueOf(cct.getMaxFileSize()/(1024L*1024)));
		taskBean.setFilesRequiredCount(String.valueOf(cct.getFilesRequiredCount()));
		taskBean.setPassed(ccta.isPassed());
		taskBean.setReviewed(ccta.isReviewed());
		if (ccta.getReviewedBy()!=null)
			taskBean.setReviewedBy(ccta.getReviewedBy().getFirstName()+" "+ccta.getReviewedBy().getLastName());
		DecimalFormat formatter = new DecimalFormat("0.00");
		taskBean.setScore(formatter.format(ccta.getScore()));
		taskBean.setTaskId(String.valueOf(cct.getId()));
		taskBean.setTitle(cct.getTitle());
		taskBean.setAssignmentID(String.valueOf(ccta.getId()));
		
		//settiramo listu fajlova
		List <TaskFileBean> beanList = new ArrayList<TaskFileBean>(ccta.getUploads().size());
		for (CourseComponentTaskUpload cctu : ccta.getUploads()) {
			TaskFileBean bean = new TaskFileBean();
			bean.setId(String.valueOf(cctu.getId()));
			bean.setFileName(cctu.getFileName());
			bean.setTag(cctu.getTag());
			bean.setUploadDate(df.format(cctu.getUploadedOn()));
			
			beanList.add(bean);
		}
		taskBean.setTaskUploadList(beanList);
		
	}
	
	private static boolean checkTaskFileUploadBean(CourseComponentData data, TaskFileUploadBean bean,
			CourseComponentTaskAssignment ccta) {
		
		//provjera je li upload zakljucan
		if (ccta.isLocked()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.taskLocked"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return false;
		}
		
		if (bean.getUpload()==null 
				|| StringUtil.isStringBlank(bean.getUploadFileName())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.fileNotUploaded"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return false;
		}
		if (StringUtil.isStringBlank(bean.getUploadContentType())) {
			bean.setUploadContentType("application/octet-stream");
		}
		
		//provjera postoji li vec fajl s istim imenom ili istim tagom
		for (CourseComponentTaskUpload cctu : ccta.getUploads()) {
			if (cctu.getFileName().equals(bean.getUploadFileName())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.fileExists"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
			if (!StringUtil.isStringBlank(bean.getFileTag()) && cctu.getTag().equals(bean.getFileTag())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.fileWithTagExists"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return false;
			}
		}
		//provjera je li proslo vrijeme za upload
		Date taskDate = getAssignmentDate(ccta);
		if (taskDate != null && !taskDate.after(new Date())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.timeLimit"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return false;
		}
		
		//provjera je li korisnik uploadao previse fajli
		if (ccta.getUploads().size()>=ccta.getCourseComponentTask().getMaxFilesCount()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.fileLimit"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return false;
		}
		//provjera je li fajl prevelik
		if (bean.getUpload().length()>ccta.getCourseComponentTask().getMaxFileSize()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.fileTooLarge"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return false;
		}
		
		return true;
	}
	
//	private static boolean canUserUpload(CourseComponentTaskAssignment ccta) {
//		
//		if (ccta.isLocked())
//			return false;
//		
//		Date taskDate = getAssignmentDate(ccta);
//		if (taskDate==null)
//			return false;
//		if (!taskDate.after(new Date()))
//			return false;		
//		
//		if (ccta.getUploads().size()>=ccta.getCourseComponentTask().getMaxFilesCount()) 
//			return false;
//		if (ccta.getCourseComponentTask().getFilesRequiredCount()!=-1 && 
//				ccta.getUploads().size()>=ccta.getCourseComponentTask().getFilesRequiredCount())
//			return false;		
//		
//		return true;
//	}
	
	private static boolean canUserLockAssignment(CourseComponentData data, CourseComponentTaskAssignment ccta) {
		
		if (!canUserModifyUpload(ccta)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.timeLimit"));
			return false;
		}
		Set<String> tagSet = new HashSet<String>();
		for (CourseComponentTaskUpload upload : ccta.getUploads()) {
			tagSet.add(upload.getTag());
		}
		Map<String, String> map = createTagMap(ccta.getCourseComponentTask().getFileTags(),true);
		for (String tag : map.keySet()) {
			if (!tagSet.contains(tag)) {
				String param[] = new String[1];
				param[0] = map.get(tag);
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.tagMissing",param));
				return false;
			}
		}
		if (ccta.getCourseComponentTask().getFilesRequiredCount()!=-1 &&
				ccta.getUploads().size()<ccta.getCourseComponentTask().getFilesRequiredCount()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.notEnoughFiles"));
			return false;
		}
		
		return true;
	}
	
	private static boolean canUserModifyUpload(CourseComponentTaskAssignment ccta) {
		
		if (ccta.isLocked())
			return false;
		
		Date taskDate = getAssignmentDate(ccta);
		if (taskDate==null)
			return true;
		if (!taskDate.after(new Date()))
			return false;
		return true;
	}
	
	private static Date getAssignmentDate(CourseComponentTaskAssignment ccta) {
		
		Date taskDate = ccta.getExtensionDate();
		if (taskDate == null) {
			if (ccta.getEvent() == null)
				return null;
			if (ccta.getEvent().isHidden())
				return null;
			taskDate = ccta.getEvent().getStart();
		}
		return taskDate;
	}
	
	private static void removeIDF(DAOHelper dh, EntityManager em,
			ItemDescriptionFile idf) {
		File rmFile = new File(getItemDir(idf.getCourseComponentItem()),idf.getFileName());
		//brisemo prvo datoteku
		rmFile.delete();
		//pa onda i objekt iz baze
		idf.getCourseComponentItem().getItemDescriptionFiles().remove(idf);
		idf.setCourseComponentItem(null);
		dh.getCourseComponentDAO().remove(em, idf);
	}
	
	private static File getItemDir(CourseComponentItem cci) {
		String compID = String.valueOf(cci.getCourseComponent().getId());
		String itemID = String.valueOf(cci.getId());
		
		String[] tmpList= {compID,itemID};
		File itemDir = JCMSSettings.getSettings().getCompRootDir();;
		
		for (String dir : tmpList) {
			itemDir = new File(itemDir,dir);
			if (!itemDir.exists())
				itemDir.mkdir();
		}
		
		return itemDir;
	}
	
	private static File getTaskDir(CourseComponentTask cct) {
		String compID = String.valueOf(cct.getCourseComponentItem().getCourseComponent().getId());
		String itemID = String.valueOf(cct.getCourseComponentItem().getId());
		String taskID = String.valueOf(cct.getId());
		
		String[] tmpList= {compID,itemID,taskID};
		File taskDir = JCMSSettings.getSettings().getCompRootDir();;
		
		for (String dir : tmpList) {
			taskDir = new File(taskDir,dir);
			if (!taskDir.exists())
				taskDir.mkdir();
		}
		
		return taskDir;
	}
	
	private static File getUserTaskDir(CourseComponentTaskAssignment ccta) {
		String compID = String.valueOf(
				ccta.getCourseComponentTask()
				.getCourseComponentItem()
				.getCourseComponent()
				.getId()
			);
		String itemID = String.valueOf(
				ccta.getCourseComponentTask()
				.getCourseComponentItem()
				.getId()
			);
		String taskID = String.valueOf(ccta.getCourseComponentTask().getId());
		String userJmbag = ccta.getUser().getJmbag();
		
		String[] tmpList= {compID,itemID,taskID,userJmbag};
		
		File tmpFile = JCMSSettings.getSettings().getCompRootDir();;
		
		for (String dir : tmpList) {
			tmpFile = new File(tmpFile,dir);
			if (!tmpFile.exists())
				tmpFile.mkdir();
		}
		
		return tmpFile;
	}

	private static void dbSaveTask(DAOHelper dh, EntityManager em,
			CourseComponentTask cct, CourseComponentTaskBean taskBean,
			boolean isNew, CourseComponentItem cci, User user) {
		
		cct.setTitle(taskBean.getTitle().trim());
		cct.setDescription(taskBean.getDescription());
		cct.setFileTags(taskBean.getFileTags().trim());
		//setiramo filesRequiredCount i maxFilesCount u skladu s brojem tagova 
		int tagCount = createTagMap(taskBean.getFileTags().trim(),true).size();
		int filesCount = Integer.valueOf(taskBean.getFilesRequiredCount());
		if (filesCount != -1)
			filesCount = max(tagCount,filesCount);
		cct.setFilesRequiredCount(filesCount);
		cct.setMaxFilesCount(max(tagCount,Integer.valueOf(taskBean.getMaxFilesCount())));
		cct.setMaxFileSize(Long.valueOf(taskBean.getMaxFileSize())*1024*1024L);
		//cct.setNeedsReviewers(Boolean.valueOf(taskBean.getNeedsReviewers()));
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date deadline = null;
		boolean blank = false;
		if (!StringUtil.isStringBlank(taskBean.getDeadline())) {
			try { deadline = df.parse(taskBean.getDeadline());
			} catch (Exception ignorable) {}
		}
		else {
			deadline = new Date();
			blank = true;
		}
		
		if (cct.getDeadline() != null) {
			cct.getDeadline().setStart(deadline);
			if (blank)
				cct.getDeadline().setHidden(true);
			else
				cct.getDeadline().setHidden(false);
		}
		
		//podaci koji se pune ako je to novi task
		if (isNew) {
			cct.setCourseComponentItem(cci);
			cci.getTasks().add(cct);
			
			//novi event
			UserSpecificEvent2 use = new UserSpecificEvent2();
			use.setDeadline(true);
			use.setStart(deadline);
			use.setIssuer(user);
			use.setStrength(EventStrength.STRONG);
			StringBuilder sb = new StringBuilder();
			sb.append("ROK: ");
			sb.append(cci.getPosition());
			sb.append(". ");
			sb.append(cci.getCourseComponent().getDescriptor().getPositionalName());
			sb.append(", ");
			sb.append(cci.getName());
			sb.append(" - ");
			sb.append(cct.getTitle());
			use.setTitle(sb.toString());
			
			if (blank)
				use.setHidden(true);
			else
				use.setHidden(false);
			
			dh.getEventDAO().save(em, use);
			
			cct.setDeadline(use);
			dh.getCourseComponentDAO().save(em, cct);
		}

		
	}
	
	private static int max(int tagCount, int filesCount) {
		if (tagCount > filesCount)
			return tagCount;
		return filesCount;
	}

	private static void fillTaskBean(CourseComponentTask cct,
			CourseComponentTaskBean taskBean) {
		
		if (cct.getDeadline()!=null && !cct.getDeadline().isHidden())
			taskBean.setDeadline(cct.getDeadline().getStartAsText());
		else
			taskBean.setDeadline(null);
		
		taskBean.setDescription(cct.getDescription());
		taskBean.setFilesRequiredCount(String.valueOf(cct.getFilesRequiredCount()));
		taskBean.setFileTags(cct.getFileTags());
		taskBean.setId(String.valueOf(cct.getId()));
		taskBean.setMaxFilesCount(String.valueOf(cct.getMaxFilesCount()));
		//taskBean.setNeedsReviewers(String.valueOf(cct.isNeedsReviewers()));
		taskBean.setMaxFileSize(String.valueOf(cct.getMaxFileSize()/(1024L*1024)));
		taskBean.setTitle(cct.getTitle());
		
	}

	private static boolean checkAssignmentReviewBean(CourseComponentData data, 
			TaskReviewBean bean) {
		boolean ok = true;
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (!StringUtil.isStringBlank(bean.getExtension())) {
			try {
				df.parse(bean.getExtension());
			} catch (Exception e) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongDateFormat"));
				ok = false;
			}
		}
		try {
			StringUtil.stringToDouble(bean.getScore());
		} catch (Exception e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongScoreFormat"));
			ok = false;
		}
		try {
			Boolean.valueOf(bean.getPassed());
		} catch (Exception e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongPassedFormat"));
			ok = false;
		}
		try {
			Boolean.valueOf(bean.getReviewed());
		} catch (Exception e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongReviewedFormat"));
			ok = false;
		}
		
		return ok;
	}
	
	private static boolean checkTaskBean(DAOHelper dh, EntityManager em,
			CourseComponentData data, CourseComponentTaskBean taskBean, Long id) {
		//TODO: popravit error poruke, popraviti ovisnost filesRequired, filesCount, fileTags
		boolean ok = true;
		
		if (StringUtil.isStringBlank(taskBean.getTitle())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.titleMustBeGiven"));
			ok = false;
		}
		//da li vec postoji task s ovim imenom
		try {
			CourseComponentTask dbCct = null;
			dbCct = dh.getCourseComponentDAO().findByTitleOnItem(em, taskBean.getTitle(), data.getCourseComponentItem());
			if (dbCct != null && (id!=dbCct.getId())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.titleExists"));
				ok = false;
			}
		} catch (Exception e) {
		}
		
		if (StringUtil.isStringBlank(taskBean.getDescription())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.descriptionMustBeGiven"));
			ok = false;
		}
		
		if (!StringUtil.isStringBlank(taskBean.getDeadline())) {
			try {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				df.parse(taskBean.getDeadline());
			} catch (Exception e) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongDateFormat2"));
				ok = false;
			}
		}
		try {
			int filesRequiredCount = Integer.valueOf(taskBean.getFilesRequiredCount());
			if (filesRequiredCount!=-1 && filesRequiredCount < 1)
				throw new Exception();
		} catch (Exception e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongFilesRequiredCount"));
			ok = false;
		}
		String tags = taskBean.getFileTags();
		if (!StringUtil.isStringBlank(tags) && !checkTags(tags)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongTagFormat"));
			ok = false;
		}
		try {
			int maxFilesCount = Integer.valueOf(taskBean.getMaxFilesCount());
			if (maxFilesCount<1) 
				throw new Exception();
		} catch (Exception e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongMaxFilesCount"));
			ok = false;
		}
		try {
			int maxFileSize = Integer.valueOf(taskBean.getMaxFileSize());
			if (maxFileSize<=0)
				throw new Exception();
		} catch (Exception e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongMaxFileSize"));
			ok = false;
		}
//		try {
//			Boolean.valueOf(taskBean.getNeedsReviewers());
//		} catch (Exception e) {
//			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongFormat"));
//			ok = false;
//		}
		return ok;
	}

	private static boolean checkTags(String tags) {
		
		if (StringUtil.isStringBlank(tags))
			return true;
		
		if (createTagMap(tags,true).size()==0) return false;
		return true;
	}

	private static Map<String, String> createTagMap(String tags,boolean empty) {
		Map<String, String> tagMap = new HashMap<String, String>();
		
		if (!empty)
			tagMap.put("", "");
		if (tags==null)
			return tagMap;
		
		String[] tagArray = tags.split("#");
		
		for (String tag : tagArray) {
			String[] currTag = tag.split(":");
			if (currTag.length!=2)
				return tagMap;
			tagMap.put(currTag[0], currTag[1]);
		}
		return tagMap;
	}

	private static boolean fillCourseComponent(EntityManager em,
			CourseComponentData data, String courseComponentID) {
		
		if(courseComponentID==null || courseComponentID.length()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		
		CourseComponent cc = null;
		try {
			cc = DAOHelperFactory.getDAOHelper().getCourseComponentDAO().getCourseComponent(em, Long.valueOf(courseComponentID));
		} catch(Exception ignorable) {
		}
		if(cc==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setCourseComponent(cc);
		return true;
	}
	
	private static void createCourseComponent(DAOHelper dh, EntityManager em,
			CourseComponentDescriptor ccd, CourseInstance courseInstance) {
		CourseComponent cc = new CourseComponent();
		
		cc.setCourseInstance(courseInstance);
		cc.setDescriptor(ccd);
		courseInstance.getCourseComponents().add(cc);
		
		dh.getCourseComponentDAO().save(em, cc);
		
		//dodaje se novi topic u ITS
		IssueTrackingService.updateMessageTopic(em, null, cc.getDescriptor().getName(), cc, "CC");
	}
	
	public static void showMatrix(final CourseComponentData data, 
			final String courseComponentItemID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				CourseInstance ci = cci.getCourseComponent().getCourseInstance();
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(ci);
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
				Collections.sort(users, StringUtil.USER_COMPARATOR);
				List<CourseComponentItemAssessment> assessments = new ArrayList<CourseComponentItemAssessment>(cci.getItemAssessments());
				Collections.sort(assessments, new Comparator<CourseComponentItemAssessment>() {
					@Override
					public int compare(CourseComponentItemAssessment o1, CourseComponentItemAssessment o2) {
						return o1.getAssessmentIdentifier().compareTo(o2.getAssessmentIdentifier());
					}
				});
				String[] assessmentNames = new String[assessments.size()];
				for(int i = 0; i < assessments.size(); i++) {
					assessmentNames[i] = assessments.get(i).getAssessmentIdentifier();
				}
				List<CCIAMatrixRow> rows = new ArrayList<CCIAMatrixRow>(users.size());
				CCIAMatrix matrix = new CCIAMatrix(assessmentNames, rows);
				Map<User,CCIAMatrixRow> userToRowMap = new HashMap<User, CCIAMatrixRow>(users.size());
				for(int i = 0; i < users.size(); i++) {
					User u = users.get(i);
					CCIAMatrixRow row = new CCIAMatrixRow(u.getId(), u.getJmbag(), u.getFirstName(), u.getLastName(), new CCIAMatrixColumn[assessments.size()]);
					rows.add(row);
					userToRowMap.put(u, row);
				}
				for(int i = 0; i < assessments.size(); i++) {
					CourseComponentItemAssessment ccia = assessments.get(i);
					List<CCIAAssignment> currentUsers = dh.getCourseComponentDAO().getItemAssessmentUsers(em, ccia);
					for (CCIAAssignment cciaa : currentUsers) {
						CCIAMatrixRow row = userToRowMap.get(cciaa.getUser());
						if(row==null) continue;
						CCIAMatrixColumn col = new CCIAMatrixColumn(cciaa.getUser().getId(), ccia.getId(), true);
						row.getColumns()[i] = col;
					}
					for(int uid = 0; uid < rows.size(); uid++) {
						CCIAMatrixRow row = rows.get(uid);
						if(row.getColumns()[i] != null) continue;
						CCIAMatrixColumn col = new CCIAMatrixColumn(row.getUserID(), ccia.getId(), false);
						row.getColumns()[i] = col;
					}
				}
				data.setCciaMatrix(matrix);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void matrixManipulateItem(final CourseComponentData data, 
			final String courseComponentItemAssessmentID, final String componentUserID, final Long userID, final String task, final InputStreamWrapper[] wrapper) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				CourseComponentItemAssessment ccia = null;
				try {
					ccia = dh.getCourseComponentDAO().getItemAssessment(em, Long.valueOf(courseComponentItemAssessmentID));
				}catch (Exception ingorable) {
				}
				if (ccia==null) {
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				CourseInstance ci = ccia.getCourseComponentItem().getCourseComponent().getCourseInstance();
				data.setCourseComponent(ccia.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ci);
				data.setCourseComponentItem(ccia.getCourseComponentItem());
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID)) {
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
				Set<User> userSet = new HashSet<User>(users);
				User user = dh.getUserDAO().getUserById(em, Long.valueOf(componentUserID));
				if(user==null || !userSet.contains(user)) {
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(!task.equals("add") && !task.equals("remove")) {
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				CCIAAssignment assignment = dh.getCourseComponentDAO().findUserAssessmentAssignment(em, ccia, user);
				if(assignment==null) {
					if(task.equals("add")) {
						// Treba ga dodati!
						assignment = new CCIAAssignment();
						assignment.setCourseComponentItemAssessment(ccia);
						assignment.setUser(user);
						dh.getCourseComponentDAO().save(em, assignment);
						wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>1</code><present>1</present></result>");
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					} else {
						// Ne treba ga dodati jer je vec tu!
						wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>0</code><present>1</present></result>");
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					}
				} else {
					if(task.equals("remove")) {
						// Treba ga maknuti!
						dh.getCourseComponentDAO().remove(em, assignment);
						wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>1</code><present>0</present></result>");
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					} else {
						// Ne treba ga brisati jer ga niti nema!
						wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>0</code><present>0</present></result>");
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					}
				}
			}
		});
	}

	public static void showTaskMatrix(final CourseComponentData data,
			final String courseComponentItemID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
		
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				CourseComponentItem cci = null;
				try {
					cci = dh.getCourseComponentDAO().getItem(em, Long.valueOf(courseComponentItemID));
				}catch (Exception ingorable) {
				}
				if (cci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				CourseInstance ci = cci.getCourseComponent().getCourseInstance();
				data.setCourseComponent(cci.getCourseComponent());
				data.setCourseInstance(ci);
				data.setCourseComponentItem(cci);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID))
					return null;
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
				Collections.sort(users, StringUtil.USER_COMPARATOR);
				List<CourseComponentTask> tasks = new ArrayList<CourseComponentTask>(cci.getTasks());
				Collections.sort(tasks, new Comparator<CourseComponentTask>() {
					@Override
					public int compare(CourseComponentTask o1, CourseComponentTask o2) {
						return o1.getTitle().compareTo(o2.getTitle());
					}
				});
				String[] taskNames = new String[tasks.size()];
				for(int i = 0; i < tasks.size(); i++) {
					taskNames[i] = tasks.get(i).getTitle();
				}
				List<CCTAMatrixRow> rows = new ArrayList<CCTAMatrixRow>(users.size());
				CCTAMatrix matrix = new CCTAMatrix(taskNames, rows);
				Map<User,CCTAMatrixRow> userToRowMap = new HashMap<User, CCTAMatrixRow>(users.size());
				for(int i = 0; i < users.size(); i++) {
					User u = users.get(i);
					CCTAMatrixRow row = new CCTAMatrixRow(u.getId(), u.getJmbag(), u.getFirstName(), u.getLastName(), new CCTAMatrixColumn[tasks.size()]);
					rows.add(row);
					userToRowMap.put(u, row);
				}
				for(int i = 0; i < tasks.size(); i++) {
					CourseComponentTask task = tasks.get(i);
					List<CourseComponentTaskAssignment> currentUsers = dh.getCourseComponentDAO().getTaskUsers(em, task);
					for (CourseComponentTaskAssignment ccta : currentUsers) {
						CCTAMatrixRow row = userToRowMap.get(ccta.getUser());
						if(row==null) continue;
						CCTAMatrixColumn col = new CCTAMatrixColumn(ccta.getUser().getId(), task.getId(), true);
						row.getColumns()[i] = col;
					}
					for(int uid = 0; uid < rows.size(); uid++) {
						CCTAMatrixRow row = rows.get(uid);
						if(row.getColumns()[i] != null) continue;
						CCTAMatrixColumn col = new CCTAMatrixColumn(row.getUserID(), task.getId(), false);
						row.getColumns()[i] = col;
					}
				}
				data.setCctaMatrix(matrix);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	protected static InputStreamWrapper createInputStreamWrapperFromText(String text) {
		try {
			byte[] buf = text.getBytes("UTF-8");
			return new InputStreamWrapper(new ByteArrayInputStream(buf), "result", buf.length, "text/xml; charset=utf-8");
		} catch(Exception ex) {
			byte[] buf = "Encoding error. Could not generate original message.".getBytes();
			return new InputStreamWrapper(new ByteArrayInputStream(buf), "result", buf.length, "text/plain; charset=utf-8");
		}
	}

	public static void cctMatrixManipulateItem(final CourseComponentData data,
			final String courseComponentTaskID, final String taskUserID, final Long userID, final String task,
			final String sure, final InputStreamWrapper[] wrapper) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				CourseComponentTask cct = null;
				try {
					cct = dh.getCourseComponentDAO().getTask(em, Long.valueOf(courseComponentTaskID));
				}catch (Exception ingorable) {
				}
				if (cct==null) {
					// code=2 znaci pogreska.
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				CourseInstance ci = cct.getCourseComponentItem().getCourseComponent().getCourseInstance();
				data.setCourseComponent(cct.getCourseComponentItem().getCourseComponent());
				data.setCourseInstance(ci);
				data.setCourseComponentItem(cct.getCourseComponentItem());
				data.setCourseComponentTask(cct);
				
				if (!BasicBrowsing.fillCurrentUser(em, data, userID)) {
					// code=2 znaci pogreska.
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				//dozvole
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance())) {
					// code=2 znaci pogreska.
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				if ((!task.equals("add") && !task.equals("remove")) || StringUtil.isStringBlank(taskUserID)) {
					// code=2 znaci pogreska.
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				User taskUser = dh.getUserDAO().getUserById(em, Long.valueOf(taskUserID));
				if(taskUser==null) {
					// code=2 znaci pogreska.
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				Map<String, User> dbUserMap = UserUtil.mapUserByJmbag(
						dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId())
					);
				
				if(dbUserMap.get(taskUser.getJmbag())==null) {
					// code=2 znaci pogreska.
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>2</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				CourseComponentTaskAssignment taskAssignment = dh.getCourseComponentDAO().getAssignmentOnTask(em, cct, taskUser);
				if(task.equals("add")) {
					if(taskAssignment!=null) {
						// code=0 nije bilo promjene
						wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>0</code><present>1</present></result>");
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					}
					// Inace ga idemo dodati...
					CourseComponentTaskAssignment ccta = new CourseComponentTaskAssignment();
					ccta.setCourseComponentTask(cct);
					ccta.setEvent(cct.getDeadline());
					ccta.setEventUser(taskUser);
					ccta.setUser(taskUser);
					ccta.setVisible(true);
					dh.getCourseComponentDAO().save(em, ccta);
					// code=1 bilo je promjene
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>1</code><present>1</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				} else {
					// Znaci, akcija je remove
					// Da vidimo - ako ovog assignmenta uopce nema:
					if(taskAssignment==null) {
						// code=0 nije bilo promjene
						wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>0</code><present>0</present></result>");
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					}
					// Inace je tu; ajmo vidjeti je li vec nesto uploadano. 
					if(!taskAssignment.getUploads().isEmpty()) {
						if(!"yes".equals(sure)) {
							// code=3 trazi potvrdu za brisanje jer ima uploadanih datoteka:
							wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>3</code><present>1</present></result>");
							data.setResult(AbstractActionData.RESULT_SUCCESS);
							return null;
						}
						// inace imamo potvrdu, pa ovo obrisi:
						List<File> filesToDelete = new ArrayList<File>(taskAssignment.getUploads().size());
						File userTaskDir = getUserTaskDir(taskAssignment);
						for(CourseComponentTaskUpload u : taskAssignment.getUploads()) {
							File taskFile = new File(userTaskDir, u.getFileName());
							filesToDelete.add(taskFile);
							dh.getCourseComponentDAO().remove(em, u);
						}
						taskAssignment.getUploads().clear();
						em.flush();
						// Ako je flush uspio, u bazi je brisanje obavljeno bez greske, pa je mala sansa da se dogodi rollback...
						// Stoga idemo brisati i s diska datoteke
						for(File taskFile : filesToDelete) {
							taskFile.delete();
						}
					}
					dh.getCourseComponentDAO().remove(em, taskAssignment);
					// code=1 bilo je promjene
					wrapper[0] = createInputStreamWrapperFromText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result><code>1</code><present>0</present></result>");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
			}
		});
	}
	
	private static final Comparator<AbstractEvent> eventsByStartComparator = new Comparator<AbstractEvent>() {
		@Override
		public int compare(AbstractEvent e1, AbstractEvent e2) {
			int r = e1.getStart().compareTo(e2.getStart());
			if(r!=0) return r;
			return StringUtil.HR_COLLATOR.compare(e1.getTitle(), e2.getTitle());
		}
	};

}
