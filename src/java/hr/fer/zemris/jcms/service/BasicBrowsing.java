package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.JCMSLogger;

import hr.fer.zemris.jcms.JCMSServices;
import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.ActivityBean;
import hr.fer.zemris.jcms.beans.AssessmentConfigurationSelectorBean;
import hr.fer.zemris.jcms.beans.ConfPreloadBean;
import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.beans.KeyValueBean;
import hr.fer.zemris.jcms.beans.MPRootInfoBean;
import hr.fer.zemris.jcms.beans.UserBean;
import hr.fer.zemris.jcms.beans.YearSemesterBean;
import hr.fer.zemris.jcms.beans.cached.Dependencies;
import hr.fer.zemris.jcms.beans.cached.DependencyItem;
import hr.fer.zemris.jcms.beans.ext.AdminAssessmentAppealBean;
import hr.fer.zemris.jcms.beans.ext.AdminListAppealsBean;
import hr.fer.zemris.jcms.beans.ext.AdminProcessAssessmentAppealBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentViewBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentViewChoiceBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentViewProblemsBean;
import hr.fer.zemris.jcms.beans.ext.CoarseGroupStat2;
import hr.fer.zemris.jcms.beans.ext.ConfPreloadScoreBean;
import hr.fer.zemris.jcms.beans.ext.ConfPreloadScoreEditBean;
import hr.fer.zemris.jcms.beans.ext.ConfProblemsScoreBean;
import hr.fer.zemris.jcms.beans.ext.ConfProblemsScoreEditBean;
import hr.fer.zemris.jcms.beans.ext.CoursePermissionBean;
import hr.fer.zemris.jcms.beans.ext.CourseUserPermissions;
import hr.fer.zemris.jcms.beans.ext.CourseUserPermissionsBean;
import hr.fer.zemris.jcms.beans.ext.DetailedUserScoreBean;
import hr.fer.zemris.jcms.beans.ext.ExchangeDescriptor;
import hr.fer.zemris.jcms.beans.ext.GroupMembershipExportBean;
import hr.fer.zemris.jcms.beans.ext.GroupOwnershipBean;
import hr.fer.zemris.jcms.beans.ext.GroupOwnershipsBean;
import hr.fer.zemris.jcms.beans.ext.JMBAGLoginBean;
import hr.fer.zemris.jcms.beans.ext.MPFormulaConstraints;
import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;
import hr.fer.zemris.jcms.beans.ext.MPGSVCourse;
import hr.fer.zemris.jcms.beans.ext.MPGSVGroup;
import hr.fer.zemris.jcms.beans.ext.MPGSVMarketPlace;
import hr.fer.zemris.jcms.beans.ext.MPOfferBean;
import hr.fer.zemris.jcms.beans.ext.MPSecurityConstraints;
import hr.fer.zemris.jcms.beans.ext.MPUserGroupState;
import hr.fer.zemris.jcms.beans.ext.MPUserState;
import hr.fer.zemris.jcms.beans.ext.MPViewBean;
import hr.fer.zemris.jcms.beans.ext.MarketPlaceBean;
import hr.fer.zemris.jcms.beans.ext.StudentGroupTagBean;
import hr.fer.zemris.jcms.beans.ext.UserScoreBean;
import hr.fer.zemris.jcms.beans.ext.UserText;
import hr.fer.zemris.jcms.caching.JCMSCacheFactory;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.Activity;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers;
import hr.fer.zemris.jcms.model.AssessmentConfProblems;
import hr.fer.zemris.jcms.model.AssessmentConfProblemsData;
import hr.fer.zemris.jcms.model.AssessmentConfiguration;
import hr.fer.zemris.jcms.model.AssessmentFile;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.CCIAAssignment;
import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.CourseComponentDescriptor;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.CourseComponentItemAssessment;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.KeyValue;
import hr.fer.zemris.jcms.model.MPOffer;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.SeminarInfo;
import hr.fer.zemris.jcms.model.SeminarRoot;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserActivityPrefs;
import hr.fer.zemris.jcms.model.UserDescriptor;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.appeals.AppealInstanceStatus;
import hr.fer.zemris.jcms.model.appeals.AppealProblemType;
import hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.parsers.DetailedUserScoreParser;
import hr.fer.zemris.jcms.parsers.ChoiceAnswersIterator;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.parsers.UserScoreParser;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.assessments.AssessmentStatus;
import hr.fer.zemris.jcms.service.extsystems.TestsService;
import hr.fer.zemris.jcms.service.has.HasAssessment;
import hr.fer.zemris.jcms.service.has.HasAssessmentFlag;
import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.service.has.HasCurrentUser;
import hr.fer.zemris.jcms.service.locks.MPLockFactory;
import hr.fer.zemris.jcms.service.reservations.impl.ferweb.FERWebReservationManagerFactory;
import hr.fer.zemris.jcms.service.util.CourseInstanceUtil;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service.util.RoleUtil;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.service2.ActivityService;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentAppealData;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentConfSelectData;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentRecalcData;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentViewData;
import hr.fer.zemris.jcms.web.actions.data.AdminProcessAssessmentAppealData;
import hr.fer.zemris.jcms.web.actions.data.AdminSendPSMessageData;
import hr.fer.zemris.jcms.web.actions.data.AdminSetDetailedChoiceConfData;
import hr.fer.zemris.jcms.web.actions.data.AdminSetProblemsConfData;
import hr.fer.zemris.jcms.web.actions.data.AdminListAppealsData;
import hr.fer.zemris.jcms.web.actions.data.AdminUpdatePreloadConfData;
import hr.fer.zemris.jcms.web.actions.data.AdminUploadPreloadConfData;
import hr.fer.zemris.jcms.web.actions.data.AdminUploadProblemsConfData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentChoiceInsightData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentCreateAppealData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentFileDownloadData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentPreloadInsightData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentProblemsInsightData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentSummaryViewData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentViewData;
import hr.fer.zemris.jcms.web.actions.data.CalculateConfChoiceResultsData;
import hr.fer.zemris.jcms.web.actions.data.CalculateConfProblemsResultsData;
import hr.fer.zemris.jcms.web.actions.data.ConfPreloadScoreEditData;
import hr.fer.zemris.jcms.web.actions.data.ConfProblemsScoreEditData;
import hr.fer.zemris.jcms.web.actions.data.CourseUsersListJSONData;
import hr.fer.zemris.jcms.web.actions.data.EditCoursePermissionsData;
import hr.fer.zemris.jcms.web.actions.data.ExternalGoToLabosiSSOData;
import hr.fer.zemris.jcms.web.actions.data.FetchConfExternalResultsData;
import hr.fer.zemris.jcms.web.actions.data.GoData;
import hr.fer.zemris.jcms.web.actions.data.GroupCoarseStatData;
import hr.fer.zemris.jcms.web.actions.data.GroupMembershipExportData;
import hr.fer.zemris.jcms.web.actions.data.GroupOwnershipData;
import hr.fer.zemris.jcms.web.actions.data.JMBAGUsernameImportData;
import hr.fer.zemris.jcms.web.actions.data.ListGroupEventsData;
import hr.fer.zemris.jcms.web.actions.data.MPAcceptOfferData;
import hr.fer.zemris.jcms.web.actions.data.MPDeleteOfferData;
import hr.fer.zemris.jcms.web.actions.data.MPDirectMoveData;
import hr.fer.zemris.jcms.web.actions.data.MPGroupSettingsViewData;
import hr.fer.zemris.jcms.web.actions.data.MPGroupsAdminData;
import hr.fer.zemris.jcms.web.actions.data.MPGroupsListData;
import hr.fer.zemris.jcms.web.actions.data.MPSendDirectOfferData;
import hr.fer.zemris.jcms.web.actions.data.MPSendGroupOfferData;
import hr.fer.zemris.jcms.web.actions.data.MPViewData;
import hr.fer.zemris.jcms.web.actions.data.MainData;
import hr.fer.zemris.jcms.web.actions.data.SeminarRootEditData;
import hr.fer.zemris.jcms.web.actions.data.ShowCourseEventsData;
import hr.fer.zemris.jcms.web.actions.data.StaffUsersListJSONData;
import hr.fer.zemris.jcms.web.actions.data.UploadStudentTagsData;
import hr.fer.zemris.jcms.web.actions.data.UserActionData;
import hr.fer.zemris.jcms.web.actions.data.UserImportData;
import hr.fer.zemris.jcms.web.actions.data.ViewSeminarInfoData;
import hr.fer.zemris.jcms.web.actions.data.YearSemesterEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DateUtil;
import hr.fer.zemris.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class BasicBrowsing {

	@Deprecated
	public static String getCurrentSemesterID() {
		return PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<String>() {
			@Override
			public String executeOperation(EntityManager em) {
				return getCurrentSemesterID(em);
			}
		});
	}

	@Deprecated
	public static String getCurrentSemesterID(EntityManager em) {
		KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "currentSemester");
		if(kv==null || kv.getName().length()==0) return null;
		return kv.getValue();
	}
	
	public static String getKeyValue(final String name) {
		String keyValue = PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<String>() {
			@Override
			public String executeOperation(EntityManager em) {
				return getKeyValue(em, name);
			}
		});
		return keyValue;
	}

	@Deprecated
	public static String getKeyValue(EntityManager em, String name) {
		KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, name);
		if(kv==null || kv.getName().length()==0) return null;
		return kv.getValue();
	}

	@Deprecated
	public static List<KeyValue> listKeyValues() {
		List<KeyValue> result = PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<List<KeyValue>>() {
			@Override
			public List<KeyValue> executeOperation(EntityManager em) {
				List<KeyValue> res = DAOHelperFactory.getDAOHelper().getKeyValueDAO().list(em);
				return res;
			}
		});
		return result;
	}

	@Deprecated
	public static void updateKeyValues(final List<KeyValue> repository) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				for(KeyValue kv : repository) {
					if(kv.getValue()!=null && kv.getValue().length()==0) kv.setValue(null);
					dh.getKeyValueDAO().update(em, kv);
				}
				return null;
			}
		});
	}

	@Deprecated
	public static void addKeyValue(final String newName, final String newValue) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				KeyValue kv = dh.getKeyValueDAO().get(em, newName);
				if(kv==null) {
					kv = new KeyValue(newName, newValue);
					dh.getKeyValueDAO().save(em, kv);
				} else {
					kv.setValue(newValue);
				}
				return null;
			}
		});
	}


	/**
	 * Dohvati sve potrebne podatke za redirekciju na stranicu s labosima.
	 * @param data spremište podataka
	 * @param userID ID prijavljenog korisnika
	 * @param courseInstanceID identifikator kolegija
	 */
	public static void getExternalGoToLabosiSSOData(final ExternalGoToLabosiSSOData data, final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID, "Error.invalidParameters", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				
				if(!JCMSSecurityManagerFactory.getManager().canUseExternalGoToLabosiSSO(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setUsername(data.getCurrentUser().getUsername());
				data.setLastName(data.getCurrentUser().getLastName());
				data.setFirstName(data.getCurrentUser().getFirstName());
				data.setJmbag(data.getCurrentUser().getJmbag());
				data.setEmail(data.getCurrentUser().getUserDescriptor().getEmail());
				if(data.getEmail()==null) data.setEmail("not-set");
				data.setCourseID(data.getCourseInstance().getCourse().getIsvuCode());
				data.setAcademicYear(data.getCourseInstance().getYearSemester().getAcademicYear());
				String sem = data.getCourseInstance().getYearSemester().getSemester();
				sem = Character.toUpperCase(sem.charAt(0))+sem.substring(1);
				data.setSemester(sem);
				long now = new Date().getTime() / 1000;
				data.setTimestamp(Long.toString(now));
		        data.setAuth(createAuth());
		        data.setUrl((String)JCMSSettings.getSettings().getObjects().get("jcms.external.labosi.url"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}

			private Object readExternalLabosiSSOAppendix() {
				String appendix = "";
				InputStream is =  FERWebReservationManagerFactory.class.getClassLoader().getResourceAsStream("sso-labosi.properties");
				if(is!=null) {
					Properties prop = new Properties();
					try {
						prop.load(new InputStreamReader(is,"UTF-8"));
					} catch (Exception e) {
						System.out.println("Error reading sso-labosi.properties.");
					}
					try { is.close(); } catch(Exception ignorable) {}
					appendix = prop.getProperty("key","");
				} else {
					System.out.println("sso-labosi.properties not found.");
				}
				return appendix;
			}
			
			private String createAuth() {
				try {
					StringBuilder toDigest = new StringBuilder(1000);
					toDigest.append(data.getUsername());
			        toDigest.append(data.getLastName());
			        toDigest.append(data.getFirstName());
			        toDigest.append(data.getJmbag());
			        toDigest.append(data.getEmail());
			        toDigest.append(data.getCourseID());
			        toDigest.append(data.getAcademicYear());
			        toDigest.append(data.getSemester());
			        toDigest.append(data.getTimestamp());
			        toDigest.append(readExternalLabosiSSOAppendix());
	
			        String ticket = toDigest.toString();
					
					MessageDigest algorithm = MessageDigest.getInstance("MD5");
					algorithm.reset();
					algorithm.update(ticket.getBytes("UTF-8"));
			
					byte messageDigest[] = algorithm.digest();
			
					StringBuilder hexString = new StringBuilder();
					for (int i=0;i<messageDigest.length;i++) {
						int c = (messageDigest[i] & 0xF0)>>4;
						if(c>9) {
							hexString.append((char)('a'+c-10));
						} else {
							hexString.append((char)('0'+c));
						}
						c = messageDigest[i] & 0x0F;
						if(c>9) {
							hexString.append((char)('a'+c-10));
						} else {
							hexString.append((char)('0'+c));
						}
					}
					String actualAuth = hexString.toString();
					return actualAuth;
				} catch(Exception ex) {
					return "";
				}
			}
		});
	}

	@Deprecated
	public static void getMainData(final MainData data, final Long userID, final Date dateFrom,
			final Date dateTo, final String currentYearSemester, final int calendarType) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				String semID = currentYearSemester==null || currentYearSemester.equals("") ? getCurrentSemesterID(em) : currentYearSemester;
				YearSemester ysem = null;
				if(semID!=null && !semID.equals("")) ysem = dh.getYearSemesterDAO().get(em, semID);
				// Aktivnosti prikazuj iz trenutnog semestra. Ako taj nema postavljen datom pocetka,
				// uzmi trenutni datum i odbij mjesec dana
				Date activitySince = ysem!=null && ysem.getStartsAt()!=null ? ysem.getStartsAt() : null;
				if(activitySince==null) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MONTH, -1);
					activitySince = cal.getTime();
				}
				List<Activity> activities = dh.getActivityDAO().listLastNForUser(em, activitySince, data.getCurrentUser(), 5);
				List<ActivityBean> activityBeans = ActivityService.generateActivityBeans(activities, data.getMessageLogger());
				data.setActivityBeans(activityBeans);
				boolean activityChanged = false;
				for(Activity a : activities) {
					if(!a.getViewed()) {
						activityChanged=true;
						a.setViewed(true);
					}
				}
				if(activityChanged) {
					em.flush();
				}
				List<CourseInstance> allCourses = Collections.emptyList(); 
				data.setAllCourseInstances(allCourses);
				data.setAllSemesters(dh.getYearSemesterDAO().list(em));
				if(ysem==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setSelectedSemester(ysem);
				User currentUser = data.getCurrentUser();
				if(calendarType==1 && ysem.getStartsAt()!=null && ysem.getEndsAt()!=null) {
					data.setEvents(EventsService.listForUser(em, currentUser, ysem.getStartsAt(), ysem.getEndsAt()));
				} else {
					data.setEvents(EventsService.listForUser(em, currentUser, dateFrom, dateTo));
				}
				data.setCourseInstanceWithGroups(DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findForUserAndSemester(em, ysem.getId(), currentUser));

				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				data.setRenderCourseAdministration(
						JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration()
				);

				if(data.getRenderCourseAdministration()) {
					data.setAllCourseInstances(
						JCMSSecurityManagerFactory.getManager().getCourseAdministrationList(ysem)
					);
				}
				
				data.setRenderSystemAdministration(JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration());

				if(data.getAllCourseInstances()!=null) {
					Collections.sort(data.getAllCourseInstances(),StringUtil.COURSEINSTANCE_COMPARATOR);
				}
				if(data.getAllSemesters()!=null) {
					Collections.sort(data.getAllSemesters(),StringUtil.YEARSEMESTER_COMPARATOR);
				}
				
				//List<Poll> polls = dh.getPollDAO().getPollsForUser(em, userID);
				data.setPollsForUser(new LinkedList<Poll>());
				
				//List<Poll> pollsOwn = dh.getPollDAO().getPollsForOwner(em, userID);
				data.setPollsForOwner(new LinkedList<Poll>());
				
				data.setUserKey(Long.toString(currentUser.getId(), 16)+":"+currentUser.getUserDescriptor().getExternalID());
				
				data.setPorukaAdmina(getKeyValue(em, "AdminMessage"));
				
				data.setOwnToDoList(ToDoService.getOwnList(dh,em,currentUser.getId()));
				return null;
			}
		});
		
	}

	/**
	 * Pomocni razred koji cuva podatke o odabranom seminaru od raspolozivih seminara grupe.
	 *  
	 * @author marcupic
	 */
	public static void getGoData(final GoData data, final Long userID, final String eid) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = data.getCurrentUser();
				
				Long id = null;
				try {
					id = Long.valueOf(eid);
				} catch(Exception ex) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				
				AbstractEvent ev = dh.getEventDAO().get(em, id);
				if(ev==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				String context = ev.getContext();
				if(StringUtil.isStringBlank(context)) {
					data.setResult("RES_HOME");
					return null;
				}
				
				int pos = context.indexOf(':');
				if(pos<1) {
					data.setResult("RES_HOME");
					return null;
				}
				
				String kind = context.substring(0, pos);
				String rest = context.substring(pos+1);
				
				if(kind.equals("sem")) {
					SeminarInfo si = dh.getSeminarDAO().getSeminarInfo(em, Long.valueOf(rest));
					if(si==null) {
						data.setResult("RES_HOME");
						return null;
					}
					data.setObject(si);
					data.setResult("RES_SEMINAR");
					return null;
				}
				if(kind.equals("a")) {
					Assessment a = dh.getAssessmentDAO().get(em, Long.valueOf(rest));
					if(a==null) {
						data.setResult("RES_HOME");
						return null;
					}
					if(JCMSSecurityManagerFactory.getManager().isStudentOnCourse(a.getCourseInstance())) {
						data.setObject(a);
						data.setResult("RES_ASSESSMENT");
						return null;
					}
					if(JCMSSecurityManagerFactory.getManager().canManageAssessments(a.getCourseInstance())) {
						data.setObject(a);
						data.setResult("RES_ADMINASSESSMENTVIEW");
						return null;
					}
					if(JCMSSecurityManagerFactory.getManager().isStaffOnCourse(a.getCourseInstance())) {
						data.setObject(a);
						data.setResult("RES_ASSESSMENT_ST");
						return null;
					}
					boolean guestAssistant = false;
					for(AssessmentRoom ar : a.getRooms()) {
						if(ar.getUserEvent()!=null && ar.getUserEvent().getUsers().contains(currentUser)) {
							guestAssistant = true;
							break;
						}
					}
					if(guestAssistant) {
						data.setObject(a);
						data.setResult("RES_ASSESSMENT_GU");
						return null;
					}
					data.setResult("RES_HOME");
					return null;
				}
				if(kind.equals("l")) {
					CourseInstance ci = dh.getCourseInstanceDAO().get(em, rest);
					if(ci==null) {
						data.setResult("RES_HOME");
						return null;
					}
					data.setObject(ci);
					data.setResult("RES_COURSEINSTANCE");
					return null;
				}
				if(kind.startsWith("c_")) {
					int p2 = rest.indexOf(':');
					if(p2<1 || p2>=rest.length()-1) {
						data.setResult("RES_HOME");
						return null;
					}
					String courseInstanceID = rest.substring(0, p2);
					String relativePath = rest.substring(p2+1);
					
					p2 = relativePath.indexOf('/');
					if(p2<1 || p2>=relativePath.length()-1) {
						data.setResult("RES_HOME");
						return null;
					}
					
					String root = relativePath.substring(0, p2);
					String sPosition = relativePath.substring(p2+1);
					int position = 0;
					try {
						position = Integer.parseInt(sPosition);
					} catch(Exception ex) {
						data.setResult("RES_HOME");
						return null;
					}
					
					CourseComponentItem cci = dh.getCourseComponentDAO().findItem(em, courseInstanceID, root, position);
					if(cci==null) {
						data.setResult("RES_HOME");
						return null;
					}
					
					data.setObject(cci);
					data.setResult("RES_COURSECOMPONENTITEM");
					return null;
				}
				
				data.setResult("RES_HOME");
				return null;
			}
		});
		
	}

	public static void getInitEventsData(final GoData data, final Long userID, final String semesterID) {
		
		// Popis kolegija u semestru
		final List<String> courseInstanceIDs = new ArrayList<String>(500);
		// Popis opisnika komponenti
		final List<CourseComponentDescriptor> ccDescriptors = new ArrayList<CourseComponentDescriptor>();
		// Mapa <groupRoot, shortName> komponenti; npr 1=>LAB, ...
		final Map<String, String> ccKeys = new HashMap<String, String>();
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = data.getCurrentUser();

				String semID = semesterID==null || semesterID.equals("") ? getCurrentSemesterID(em) : semesterID;
				YearSemester ysem = null;
				if(semID!=null && !semID.equals("")) ysem = dh.getYearSemesterDAO().get(em, semID);

				if(ysem==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<CourseInstance> list = dh.getCourseInstanceDAO().findForSemester(em, ysem.getId());
				for(CourseInstance ci : list) {
					courseInstanceIDs.add(ci.getId());
				}

				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				ccDescriptors.addAll(dh.getCourseComponentDAO().listDescriptors(em));

				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});

		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) return;

		final Set<String> groupRoots = new HashSet<String>();
		for(CourseComponentDescriptor ccd : ccDescriptors) {
			groupRoots.add(ccd.getGroupRoot());
			ccKeys.put(ccd.getGroupRoot(), ccd.getShortName());
		}
		
		for(final String courseInstanceID : courseInstanceIDs) {
			PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
				@Override
				public Void executeOperation(EntityManager em) {
					DAOHelper dh = DAOHelperFactory.getDAOHelper();
					CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
					for(Assessment as : ci.getAssessments()) {
						if(as.getEvent()!=null) {
							as.getEvent().setContext("a:"+as.getId());
						}
						for(AssessmentRoom r : as.getRooms()) {
							if(r.getGroup()==null) continue;
							for(GroupWideEvent gwe : r.getGroup().getEvents()) {
								gwe.setContext("a:"+as.getId());
							}
						}
					}
					
					for(Group g : ci.getPrimaryGroup().getSubgroups()) {
						// Ako je to grupa za predavanja:
						if(g.getRelativePath().equals("0")) {
							for(Group lg : g.getSubgroups()) {
								processLectureGroups(ci, lg);
							}
							continue;
						}
						// Ako je to grupa neke od komponenti:
						if(groupRoots.contains(g.getRelativePath())) {
							for(Group cg : g.getSubgroups()) {
								processComponentGroupTop(ci, cg);
							}
						}
					}
					
					return null;
				}

				private void processComponentGroupTop(CourseInstance ci, Group cg) {
					String itemID = cg.getRelativePath();
					String shortName = ccKeys.get(itemID.substring(0, itemID.indexOf('/')));
					String ctx = "c_"+shortName+":"+ci.getId()+":"+itemID;
					for(Group g : cg.getSubgroups()) {
						processComponentGroupRec(ci, g, ctx);
					}
				}

				private void processComponentGroupRec(CourseInstance ci, Group cg, String ctx) {
					for(GroupWideEvent gwe : cg.getEvents()) {
						gwe.setContext(ctx);
					}
					for(Group g : cg.getSubgroups()) {
						processComponentGroupRec(ci, g, ctx);
					}
				}

				private void processLectureGroups(CourseInstance ci, Group lg) {
					for(GroupWideEvent gwe : lg.getEvents()) {
						gwe.setContext("l:"+ci.getId());
					}
					for(Group g : lg.getSubgroups()) {
						processLectureGroups(ci, g);
					}
				}
			});
		}
	}

	public static void getSeminarRootEditData(
			final SeminarRootEditData data, final Long userID, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID)) return null;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(task.equals("listSeminarRoots")) {
					List<SeminarRoot> list = dh.getSeminarDAO().listSeminarRoots(em);
					data.setAllSeminarRoots(list);
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				SeminarRoot seminarRoot = null;
				if(data.getId()!=null) {
					try {
						seminarRoot = dh.getSeminarDAO().getSeminarRoot(em, data.getId());
					} catch(Exception ignorable) {
					}
					if(seminarRoot==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				if(task.equals("newSeminarRoot")) {
					data.setGroupName("");
					data.setSource("");
					data.setYearSemester(getCurrentSemesterID(em));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(task.equals("editSeminarRoot")) {
					if(seminarRoot==null) {
						data.setResult(AbstractActionData.RESULT_FATAL);
					} else {
						data.setGroupName(seminarRoot.getRootGroup().getName());
						data.setSource(seminarRoot.getSource());
						data.setId(seminarRoot.getId());
						data.setYearSemester(seminarRoot.getSemester().getId());
						data.setActive(seminarRoot.isActive());
						data.setResult(AbstractActionData.RESULT_INPUT);
					}
					return null;
				}
				if(!task.equals("saveSeminarRoot")) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(StringUtil.isStringBlank(data.getGroupName())) {
					data.getMessageLogger().addErrorMessage("Naziv grupe mora biti zadan!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(StringUtil.isStringBlank(data.getSource())) {
					data.getMessageLogger().addErrorMessage("Izvor podataka mora biti zadan!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(StringUtil.isStringBlank(data.getYearSemester())) {
					data.getMessageLogger().addErrorMessage("Oznaka semestra mora biti zadana!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				YearSemester ysem = dh.getYearSemesterDAO().get(em, data.getYearSemester());
				if(ysem==null) {
					data.getMessageLogger().addErrorMessage("Odabrani semestar ne postoji.");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}

				if(seminarRoot==null) {
					seminarRoot = new SeminarRoot();
				}
				seminarRoot.setActive(data.isActive());
				seminarRoot.setSemester(ysem);
				seminarRoot.setSource(data.getSource());
				if(seminarRoot.getRootGroup()==null) {
					seminarRoot.setRootGroup(new Group());
					seminarRoot.getRootGroup().setCompositeCourseID(ysem.getId()+"/@sem");
					seminarRoot.getRootGroup().setManagedRoot(false);
					seminarRoot.getRootGroup().setName(data.getGroupName());
					seminarRoot.getRootGroup().setRelativePath("");
					dh.getGroupDAO().save(em, seminarRoot.getRootGroup());
				} else {
					seminarRoot.getRootGroup().setName(data.getGroupName());
				}
				if(seminarRoot.getId()==null) {
					dh.getSeminarDAO().saveSeminarRoot(em, seminarRoot);
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
				} else {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getViewSeminarInfoData(
			final ViewSeminarInfoData data, final Long userID, final Long seminarInfoID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID)) return null;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				SeminarInfo si = seminarInfoID==null ? null : dh.getSeminarDAO().getSeminarInfo(em, seminarInfoID);
				if(si==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				boolean canProcceed = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canProcceed) {
					canProcceed = si.getStudent().equals(data.getCurrentUser());
				}
				if(!canProcceed) {
					GroupOwner go = dh.getGroupDAO().getGroupOwner(em, si.getGroup(), data.getCurrentUser());
					canProcceed = go!=null;
				}
				if(!canProcceed) {
					for(UserGroup ug : si.getGroup().getUsers()) {
						if(ug.getUser().equals(data.getCurrentUser())) {
							canProcceed = true;
							break;
						}
					}
				}
				if(!canProcceed) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				List<SeminarInfo> seminars = dh.getSeminarDAO().findSeminarInfosFor(em, si.getGroup());
				Collections.sort(seminars, new Comparator<SeminarInfo>() {
					@Override
					public int compare(SeminarInfo o1, SeminarInfo o2) {
						int r = StringUtil.HR_COLLATOR.compare(o1.getTitle(), o2.getTitle());
						if(r!=0) return r;
						return StringUtil.USER_COMPARATOR.compare(o1.getStudent(), o2.getStudent());
					}
				});
				data.setSelectedSeminar(si);
				data.setAllSeminars(seminars);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getAdminSendPSMessageData(
			final AdminSendPSMessageData data, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(StringUtil.isStringBlank(data.getName()) || StringUtil.isStringBlank(data.getKey())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(JCMSServices.periodicalPassMessage(data.getName(), data.getKey(), data.getValue())) {
					data.getMessageLogger().addInfoMessage("Poruka uspješno predana.");
				} else {
					data.getMessageLogger().addErrorMessage("Poruka nije predana.");
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getAdminAssessmentRecalcData(
			AdminAssessmentRecalcData data, Long userID, String courseInstanceID) {
		AssessmentService.updateAllAssessments(data.getMessageLogger(), userID, courseInstanceID);
	}

	@Deprecated
	public static <T extends AbstractActionData & HasCurrentUser> boolean fillCurrentUser(EntityManager em, T data, Long userID) {
		return fillCurrentUser(em, data, userID, "Error.notLoggedIn", AbstractActionData.RESULT_FATAL);
	}

	@Deprecated
	public static <T extends AbstractActionData & HasCurrentUser> boolean fillCurrentUser(EntityManager em, T data, Long userID, String message, String result) {
		User currentUser = null;
		if(userID != null) currentUser = DAOHelperFactory.getDAOHelper().getUserDAO().getUserById(em, userID);
		if(currentUser==null) {
			if(message!=null) data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(message));
			if(result!=null) data.setResult(result);
			return false;
		}
		data.setCurrentUser(currentUser);
		return true;
	}

	@Deprecated
	public static <T extends AbstractActionData & HasCourseInstance> boolean fillCourseInstance(EntityManager em, T data, String courseInstanceID) {
		return fillCourseInstance(em, data, courseInstanceID, "Error.invalidParameters", AbstractActionData.RESULT_FATAL);
	}

	@Deprecated
	public static <T extends AbstractActionData & HasCourseInstance> boolean fillCourseInstance(EntityManager em, T data, String courseInstanceID, String message, String result) {
		CourseInstance ci = null;
		if(courseInstanceID!=null && courseInstanceID.length()>0) {
			ci = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(em, courseInstanceID);
		}
		if(ci==null) {
			if(message!=null) data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(message));
			if(result!=null) data.setResult(result);
			return false;
		}
		data.setCourseInstance(ci);
		return true;
	}

	@Deprecated
	public static <T extends AbstractActionData & HasAssessment> boolean fillAssessment(EntityManager em, T data, String assessmentID) {
		if(assessmentID==null || assessmentID.length()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		Assessment assessment = null;
		try {
			assessment = DAOHelperFactory.getDAOHelper().getAssessmentDAO().get(em, Long.valueOf(assessmentID));
		} catch(Exception ignorable) {
		}
		if(assessment==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.assessmentNotFound"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setAssessment(assessment);
		return true;
	}

	@Deprecated
	public static <T extends AbstractActionData & HasAssessmentFlag> boolean fillAssessmentFlag(EntityManager em, T data, String assessmentFlagID) {
		if(assessmentFlagID==null || assessmentFlagID.length()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		AssessmentFlag assessmentFlag = null;
		try {
			assessmentFlag = DAOHelperFactory.getDAOHelper().getAssessmentDAO().getFlag(em, Long.valueOf(assessmentFlagID));
		} catch(Exception ignorable) {
		}
		if(assessmentFlag==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.assessmentFlagNotFound"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setAssessmentFlag(assessmentFlag);
		return true;
	}

	/**
	 * Package-private metoda koja opslužuje akciju AdminAssessmentView. Vraća true ako je sve u redu. Vraća false
	 * ako se je dogodila pogreška, i tada su poruke i rezultat već postavljeni.
	 *  
	 * @param em
	 * @param data
	 * @param userID
	 * @param courseInstanceID
	 * @param assessmentID
	 * @return
	 */
	@Deprecated
	static boolean getAdminAssessmentViewData(EntityManager em, AdminAssessmentViewData data,
			Long userID, String courseInstanceID, String assessmentID) {
		if(!fillCurrentUser(em, data, userID)) return false;
		if(!fillCourseInstance(em, data, courseInstanceID)) return false;
		if(!fillAssessment(em, data, assessmentID)) return false;
		
		Assessment assessment = data.getAssessment();

		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		// Inicijalizacija assessment-a
		assessment.getChildren().size();

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<AssessmentFile> myFiles = dh.getAssessmentDAO().listAssessmentFilesForUser(em, assessment, data.getCurrentUser());
		List<AssessmentFile> aFiles = dh.getAssessmentDAO().listAssessmentFilesForAssessment(em, assessment);
		Collections.sort(myFiles);
		Collections.sort(aFiles);
		List<AssessmentFile> allFiles = new ArrayList<AssessmentFile>(myFiles.size()+aFiles.size());
		allFiles.addAll(aFiles);
		allFiles.addAll(myFiles);
		data.setFiles(allFiles);
		data.setAssessmentConfigurationKey(AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration()));
		data.setConfSelectors(AssessmentService.getAllConfigurationSelectors(assessment));
		if(assessment.getAssessmentConfiguration()!=null && !data.getConfSelectors().isEmpty()) {
			data.getConfSelectors().add(0, new AssessmentConfigurationSelectorBean("-","--------"));
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return true;
	}

	public static void getAdminAssessmentConfSelectData(
			final AdminAssessmentConfSelectData data, final Long userID, final String courseInstanceID, final String assessmentID,
			final String confSelectorID, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;

				Assessment assessment = data.getAssessment();
				
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(StringUtil.isStringBlank(confSelectorID)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				if(task!=null && task.equals("askConfirm")) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(task==null || (!task.equals("doIt")&&!task.equals("changeIt"))) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(task.equals("doIt") && (assessment.getAssessmentConfiguration()!=null || confSelectorID.equals("-"))) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(confSelectorID.equals("-")) {
					if(!AssessmentService.clearAllAssessmentConfigurationData(em, assessment)) {
						data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.assessmentConfigurationNotDeleted"));
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					}
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.assessmentConfigurationChanged"));
				} else {
					AssessmentConfiguration assessmentConfiguration = AssessmentService.createAssessmentConfigurationForKey(confSelectorID);
					if(assessmentConfiguration==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					if(assessment.getAssessmentConfiguration()!=null) {
						if(!AssessmentService.clearAllAssessmentConfigurationData(em, assessment)) {
							data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Error.assessmentConfigurationNotDeleted"));
							data.setResult(AbstractActionData.RESULT_SUCCESS);
							return null;
						}
					}
					assessmentConfiguration.setAssessment(assessment);
					assessment.setAssessmentConfiguration(assessmentConfiguration);
					DAOHelperFactory.getDAOHelper().getAssessmentDAO().save(em, assessmentConfiguration);
					if(task.equals("doIt")) {
						data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.assessmentConfigurationDefined"));
					} else {
						data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.assessmentConfigurationChanged"));
					}
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getAdminProcessAssessmentAppealData(
			final AdminProcessAssessmentAppealData data, final AdminProcessAssessmentAppealBean bean,
			final Long userID, final String appealID, final String courseInstanceID,
			final String appealStatus) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Long id = 0L;
				try {
					id = Long.valueOf(appealID);
				} catch (NumberFormatException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": " + "appealID");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				AssessmentAppealInstance appeal = dh.getAssessmentDAO().getAppealForId(em, id);
				
				if (!data.getCourseInstance().equals(appeal.getAssessment().getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": " + "CourseInstance");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setAssessment(appeal.getAssessment());
				
				// Žalba je zaključana!
				if (appeal.getStatus().equals(AppealInstanceStatus.LOCKED) && !appealStatus.equals("unlock")) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("appeal.AppealLocked"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				if (appealStatus.equals("reject")) {
					appeal.setStatus(AppealInstanceStatus.REJECTED);
					appeal.setSolverUser(data.getCurrentUser());
					data.getMessageLogger().addInfoMessage(	data.getMessageLogger().getText("appeal.processed") + ". Status: "
															+ data.getMessageLogger().getText("appeal.REJECTED"));
					
				} else if (appealStatus.equals("lock")) {
					appeal.setStatus(AppealInstanceStatus.LOCKED);
					appeal.setLockerUser(data.getCurrentUser());
					data.getMessageLogger().addInfoMessage(	data.getMessageLogger().getText("appeal.processed") + ". Status: "
															+ data.getMessageLogger().getText("appeal.LOCKED"));
					
				} else if (appealStatus.equals("unlock")) {
					appeal.setStatus(AppealInstanceStatus.OPENED);
					appeal.setLockerUser(null);
					data.getMessageLogger().addInfoMessage(	data.getMessageLogger().getText("appeal.processed") + ". Status: "
															+ data.getMessageLogger().getText("appeal.OPENED"));
					
				} else if (appealStatus.equals("approve")) {
					appeal.setStatus(AppealInstanceStatus.ACCEPTED);
					appeal.setSolverUser(data.getCurrentUser());
					// Glavna obrada žalbi po tipovima.
					/*	Tipovi čija obrada ne sadrži nikakve akcije:
					 * 		WRONG_OFFICIAL_SOLUTION	// Ispravak je bolje provesti ručno
					 * 		CHECK_SCORE_FOR_PROBLEM	// Zahtijeva ponovno ispravljanje
					 * 		PROBLEM_NOT_EVALUATED	// Zahtijeva evaluaciju zadatka
					 * 		NOT_PROCESSED			// Zahtijeva evaluaciju kompletnog studentovog rada 
					 */
					
					if (appeal.getType().equals(AppealProblemType.SET_SCORE_FOR_PROBLEM)) {
						if (!setScoreForProblem(appeal, dh, em)) {
							return null;
						}
						
					} else if (appeal.getType().equals(AppealProblemType.BAD_SCAN_OFFER_SOLUTION)) {
						if (!badScanOfferSolution(appeal, dh, em)) {
							return null;
						}
						
					}
					
					data.getMessageLogger().addInfoMessage(	data.getMessageLogger().getText("appeal.processed") + ". Status: "
															+ data.getMessageLogger().getText("appeal.ACCEPTED"));
					
				} else {
					// Ne bi se smjelo dogoditi osim u slučaju lošeg poziva ove metode, ali kad se već ne koriste enumeracije, neka ovo ostane.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": appeal status");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
			
			/* Glavna obrada žalbi */
			
			/* Pogrešno skeniran obrazac na zaokruživanje. Žalba uz ponuđeni odgovor. */
			private boolean badScanOfferSolution(AssessmentAppealInstance appeal, DAOHelper dh, EntityManager em) {
				AssessmentConfiguration ac = appeal.getAssessment().getAssessmentConfiguration();
				if (!(ac instanceof AssessmentConfChoice)) {
					// Ne bi se smjelo dogoditi osim u slučaju neispravne konfiguracije žalbe.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": assessment configuration != AssessmentConfChoice");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return false;
				}
				
				AssessmentConfChoice acc = (AssessmentConfChoice) ac;
				int problemNumber = appeal.getIntProperty("problem-number");
				String answer = appeal.getStringProperty("answer-value");
				
				if (acc.getProblemsNum() < problemNumber || problemNumber < 0) {
					// Ne bi se smjelo dogoditi.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": problem-number");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return false;
					
				} else if (answer.equals("BLANK") || (answer.length() == 1 && (answer.charAt(0) - 'A' + 1) > acc.getAnswersNumber())) {
					// Ne bi se smjelo dogoditi.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": answer-value: " + answer);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return false;
				}
				
				AssessmentConfChoiceAnswers acca = dh.getAssessmentDAO().getAssessmentConfChoiceAnswersForAssessementAndStudent(em, appeal.getCreatorUser(), acc);
				if (acca == null) {
					// Ne bi se smjelo dogoditi.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": AssessmentConfChoiceAnswers: null");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return false;
				}
				
				String[] answers = StringUtil.split(acca.getAnswers(), '\t');
				if (answers.length < problemNumber) {
					// Ne bi se smjelo dogoditi.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": problem-number");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return false;
				}
				
				answers[problemNumber-1] = answer;
				StringBuilder sb = new StringBuilder(answers.length + (answers.length/2) * 5);
				int accNumberOfProblems = acc.getProblemsNum();
				for (int i = 0; i < accNumberOfProblems; i++) {
					sb.append(answers[i]);
					if (i < accNumberOfProblems - 1) {
						sb.append('\t');
					}
				}
				acca.setAnswers(sb.toString());
				acca.setAnswersStatus("");
				
				return true;
			}
			
			/* Potrebno je napraviti izmjene na bodovima za određeni zadatak. */
			private boolean setScoreForProblem(AssessmentAppealInstance appeal, DAOHelper dh, EntityManager em) {
				AssessmentConfiguration ac = appeal.getAssessment().getAssessmentConfiguration();
				if (!(ac instanceof AssessmentConfProblems)) {
					// Ne bi se smjelo dogoditi osim u slučaju neispravne konfiguracije žalbe.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": illegal assessment configuration");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return false;
				}
				
				AssessmentConfProblems acp = (AssessmentConfProblems) ac;
				int problemNumber = appeal.getIntProperty("problem-number");
				double score = appeal.getDoubleProperty("score-value");
				
				if (acp.getNumberOfProblems() < problemNumber || problemNumber < 0) {
					// Ne bi se smjelo dogoditi.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": problem-number");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return false;	
				}
				
				AssessmentConfProblemsData acpd = dh.getAssessmentDAO().getConfProblemsDataForAssessementAndUserId(em, acp, appeal.getCreatorUser().getId());
				if (acpd == null) {
					// Ne bi se smjelo dogoditi.
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": AssessmentConfProblemsData: null");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return false;
				}
				
				acpd.setScoreFor(problemNumber - 1, score);
				
				return true;
			}
		});
	}

	public static void getAdminAssessmentAppealData(
			final AdminAssessmentAppealData data, final AdminAssessmentAppealBean bean,
			final Long userID, final String appealID, final String courseInstanceID, final String assessmentID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;

				Assessment assessment = data.getAssessment();
				
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				Long id = 0L;
				try {
					id = Long.valueOf(appealID);
				} catch (NumberFormatException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": " + "appealID");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				AssessmentAppealInstance appeal = dh.getAssessmentDAO().getAppealForId(em, id);
				bean.setAppeal(appeal);
				
				List<AssessmentFile> myFiles = dh.getAssessmentDAO().listAssessmentFilesForUser(em, appeal.getAssessment(), appeal.getCreatorUser());
				List<AssessmentFile> aFiles = dh.getAssessmentDAO().listAssessmentFilesForAssessment(em, appeal.getAssessment());
				Collections.sort(myFiles);
				Collections.sort(aFiles);
				List<AssessmentFile> allFiles = new ArrayList<AssessmentFile>(myFiles.size()+aFiles.size());
				allFiles.addAll(aFiles);
				allFiles.addAll(myFiles);
				data.setFiles(allFiles);

				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getAdminUpdatePreloadConfData(
			final AdminUpdatePreloadConfData data, final ConfPreloadBean bean, final Long userID,
			final String courseInstanceID, final String assessmentID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!getAdminAssessmentViewData(em, data, userID, courseInstanceID, assessmentID)) return null;

				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				String key = AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
				if(!key.equals("PRELOAD")) {
					// Ups! Netko nam hoće podvaliti krivi tip provjere!
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rzultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				//Double maxScore = null;
				//try {
				//	maxScore = StringUtil.stringToDouble(bean.getMaxScore());
				//} catch(NumberFormatException ex) {
				//	data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.doubleNumberFormatException"));
				//	data.setResult(AbstractActionData.RESULT_INPUT);
				//	return null;
				//}
				//AssessmentConfPreload a = (AssessmentConfPreload)assessment.getAssessmentConfiguration();
				// Ova akcija se trenutno ne koristi jer je maxScore prebacen u sam assessment.
				//a.setMaxScore(maxScore);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				return null;
			}
		});
	}

	public static void getAdminUploadPreloadConfData(
			final AdminUploadPreloadConfData data, final Long userID,
			final String courseInstanceID, final String assessmentID, final String text,
			final String appendOrReplace) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!getAdminAssessmentViewData(em, data, userID, courseInstanceID, assessmentID)) return null;

				User currentUser = data.getCurrentUser();
				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				String key = AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
				if(!key.equals("PRELOAD")) {
					// Ups! Netko nam hoće podvaliti krivi tip provjere!
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(!"APPEND".equals(appendOrReplace) && !"REPLACE".equals(appendOrReplace)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				boolean append = "APPEND".equals(appendOrReplace);
				
				List<UserScoreBean> beanList = null;
				try {
					beanList = UserScoreParser.parseTabbedFormat(new StringReader(text));
				} catch(IOException ex) {
					// Imamo grešku u ulaznim podacima...
					data.getMessageLogger().addErrorMessage(ex.getMessage());
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, courseInstanceID);
				Set<String> regularJmbags = new HashSet<String>();
				Map<String,User> userMap = new HashMap<String, User>(courseUsers.size());
				for(User u : courseUsers) {
					regularJmbags.add(u.getJmbag());
					userMap.put(u.getJmbag(), u);
				}
				boolean errors = false;
				for(UserScoreBean bean : beanList) {
					if(!regularJmbags.contains(bean.getJmbag())) {
						errors = true;
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+bean.getJmbag());
					}
				}
				if(!errors) {
					Map<String, AssessmentScore> map = new HashMap<String, AssessmentScore>(courseUsers.size());
					for(AssessmentScore v : assessment.getScore()) {
						map.put(v.getUser().getJmbag(), v);
					}
					Set<String> assignedJmbags = new HashSet<String>(beanList.size());
					for(UserScoreBean bean : beanList) {
						assignedJmbags.add(bean.getJmbag());
						AssessmentScore v = map.get(bean.getJmbag());
						if(v==null) {
							v = new AssessmentScore();
							v.setAssessment(assessment);
							assessment.getScore().add(v);
							v.setError(false);
							v.setAssigner(currentUser);
							v.setRawPresent(bean.getDoubleValue()!=null);
							v.setRawScore(bean.getDoubleValue()==null ? 0.0 : bean.getDoubleValue().doubleValue());
							v.setUser(userMap.get(bean.getJmbag()));
							v.setStatus(AssessmentStatus.FAILED);
							dh.getAssessmentDAO().save(em, v);
						} else {
							v.setError(false);
							v.setRawPresent(bean.getDoubleValue()!=null);
							v.setRawScore(bean.getDoubleValue()==null ? 0.0 : bean.getDoubleValue().doubleValue());
						}
					}
					if(!append) {
						for(AssessmentScore v : assessment.getScore()) {
							if(!assignedJmbags.contains(v.getUser().getJmbag())) {
								v.setRawPresent(false);
								v.setRawScore(0);
								v.setError(false);
							}
						}
					}
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				} else {
					data.setResult(AbstractActionData.RESULT_INPUT);
				}
				return null;
			}
		});
	}

	public static void getAdminUploadProblemsConfData (
			final AdminUploadProblemsConfData data, final Long userID,
			final String courseInstanceID, final String assessmentID, final String text,
			final String appendOrReplace) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!getAdminAssessmentViewData(em, data, userID, courseInstanceID, assessmentID)) return null;

				User currentUser = data.getCurrentUser();
				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				String key = AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
				if(!key.equals("PROBLEMS")) {
					// Ups! Netko nam hoće podvaliti krivi tip provjere!
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(!"APPEND".equals(appendOrReplace) && !"REPLACE".equals(appendOrReplace)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				boolean append = "APPEND".equals(appendOrReplace);
				
				AssessmentConfProblems assessmentConf = (AssessmentConfProblems)assessment.getAssessmentConfiguration();
				if(assessmentConf.getNumberOfProblems()<1) {
					// Imamo grešku u ulaznim podacima...
					data.getMessageLogger().addErrorMessage("Niste u parametrima provjere podesili koliko zadataka provjera sadrži.");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				List<DetailedUserScoreBean> beanList = null;
				try {
					beanList = DetailedUserScoreParser.parseTabbedMultiValueFormat(new StringReader(text));
					if(beanList.isEmpty()) {
						throw new IOException("Niste predali podatke.");
					}
					if(beanList.get(0).getListOfValues().length != assessmentConf.getNumberOfProblems()) {
						throw new IOException("Ulazni podatci ne sadrže očekivani broj stupaca. Molim pogledajte u Pomoć za informacije o formatu.");
					}
				} catch(IOException ex) {
					// Imamo grešku u ulaznim podacima...
					data.getMessageLogger().addErrorMessage(ex.getMessage());
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, courseInstanceID);
				Set<String> regularJmbags = new HashSet<String>();
				Map<String,User> userMap = new HashMap<String, User>(courseUsers.size());
				for(User u : courseUsers) {
					regularJmbags.add(u.getJmbag());
					userMap.put(u.getJmbag(), u);
				}
				boolean errors = false;
				for(DetailedUserScoreBean bean : beanList) {
					if(!regularJmbags.contains(bean.getJmbag())) {
						errors = true;
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+bean.getJmbag());
					}
				}
				if(!errors) {
					Map<String, AssessmentConfProblemsData> map = new HashMap<String, AssessmentConfProblemsData>(courseUsers.size());
					List<AssessmentConfProblemsData> dataList = dh	.getAssessmentDAO()
																	.listConfProblemsDataForAssessement(em,
																	(AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration());
					for (AssessmentConfProblemsData confData : dataList) {
						map.put(confData.getUser().getJmbag(), confData);
					}
					
					Set<String> assignedJmbags = new HashSet<String>(beanList.size());
					for (DetailedUserScoreBean bean : beanList) {
						Double[] doubleScores = stringArrayToDoubleArray(bean.getListOfValues());
						assignedJmbags.add(bean.getJmbag());
						AssessmentConfProblemsData v = map.get(bean.getJmbag());
						if (v == null) {
							v = new AssessmentConfProblemsData();
							v.setAssigner(currentUser);
							v.setPresent(true);
							v.setUser(userMap.get(bean.getJmbag()));
							v.setGroup(bean.getGroup());
							v.setDscore(doubleScores);
							v.setAssessmentConfProblems((AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration());
							dh.getAssessmentDAO().save(em, v);
						} else {
							v.setPresent(true);
							v.setDscore(doubleScores);
						}
					}
					if (!append) {
						for (AssessmentConfProblemsData v : dataList) {
							if (!assignedJmbags.contains(v.getUser().getJmbag())) {
								v.setPresent(false);
							}
						}
					}
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				} else {
					data.setResult(AbstractActionData.RESULT_INPUT);
				}
				return null;
			}
		});
	}

	public static void getAdminUploadProblemsConfMaxData (
			final AdminUploadProblemsConfData data, final Long userID,
			final String courseInstanceID, final String assessmentID, final String text) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!getAdminAssessmentViewData(em, data, userID, courseInstanceID, assessmentID)) return null;

				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				String key = AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
				if(!key.equals("PROBLEMS")) {
					// Ups! Netko nam hoće podvaliti krivi tip provjere!
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				AssessmentConfProblems assessmentConf = (AssessmentConfProblems)assessment.getAssessmentConfiguration();
				if(assessmentConf.getNumberOfProblems()<1) {
					// Imamo grešku u ulaznim podacima...
					data.getMessageLogger().addErrorMessage("Niste u parametrima provjere podesili koliko zadataka provjera sadrži.");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				String[] rows = StringUtil.isStringBlank(text) ? new String[0] : StringUtil.split(text.replace('\r', '\n'), '\n');
				StringBuilder sb = new StringBuilder(text==null ? 0 :  text.length());
				Set<String> rowsSet = new HashSet<String>(rows.length);
				for(String row : rows) {
					if(StringUtil.isStringBlank(row)) continue;
					String[] elems = StringUtil.split(row, '\t');
					if(elems.length!=assessmentConf.getNumberOfProblems()+1) {
						data.getMessageLogger().addErrorMessage("Pronađen redak s neodgovarajućim brojem elemenata!");
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
					for(int i = 0; i < elems.length; i++) {
						if(StringUtil.isStringBlank(elems[i])) {
							data.getMessageLogger().addErrorMessage("Pronađen redak s praznom grupom ili maksimumom bodova za neki zadatak!");
							data.setResult(AbstractActionData.RESULT_INPUT);
							return null;
						}
						if(i>0) {
							try {
								Double d = StringUtil.stringToDouble(elems[i]);
								if(d==null || d.doubleValue()<0) {
									data.getMessageLogger().addErrorMessage("Maksimalni broj bodova nekog zadatka ne može biti negativan.");
									data.setResult(AbstractActionData.RESULT_INPUT);
									return null;
								}
							} catch(Exception ex) {
								data.getMessageLogger().addErrorMessage("Polje "+elems[i]+" se ne može interpretirati kao broj.");
								data.setResult(AbstractActionData.RESULT_INPUT);
								return null;
							}
						}
					}
					if(!rowsSet.add(elems[0])) {
						data.getMessageLogger().addErrorMessage("Pronađen duplikat grupe: "+elems[0]+"!");
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
					if(sb.length()>0) {
						sb.append('\n');
					}
					sb.append(row);
				}
				assessmentConf.setScorePerProblem(sb.toString());
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				return null;
			}
		});
	}

	@Deprecated
	private static Double[] stringArrayToDoubleArray(String[] strings) {
		if (strings == null) {
			return null;
		}
		
		Double[] doubles = new Double[strings.length];
		
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = StringUtil.stringToDouble(strings[i]);
		}
		
		return doubles;
	}

	@Deprecated
	private static Double[] stringArrayToDoubleArray(String[] strings, int startIndex) {
		if (strings == null) {
			return null;
		}

		int len = strings.length - startIndex;
		if(len<1) return new Double[0];
		
		Double[] doubles = new Double[len];
		
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = StringUtil.stringToDouble(strings[i+startIndex]);
		}
		
		return doubles;
	}

	public static void getAdminSetProblemsConfData (
			final AdminSetProblemsConfData data, final Long userID,
			final String courseInstanceID, final String assessmentID, final String parameterType,
			final String value) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!getAdminAssessmentViewData(em, data, userID, courseInstanceID, assessmentID)) return null;

				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				String key = AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
				if(!key.equals("PROBLEMS")) {
					// Ups! Netko nam hoće podvaliti krivi tip provjere!
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				// Odredi tip parametra.
				if (parameterType.equals("numberOfProblems")) {
					int numberOfProblems;
					int oldVal = ((AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration()).getNumberOfProblems();
					
					try {
						numberOfProblems = Integer.parseInt(value);
						
					} catch (NumberFormatException e) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					}
					
					((AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration()).setNumberOfProblems(numberOfProblems);
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.newNumberOfProblemsSet"));
					
					if ((oldVal != 0) && (oldVal != numberOfProblems)) {
						List<AssessmentScore> rawScoresList = dh.getAssessmentDAO().listScoresForAssessment(em, data.getAssessment());
						Map<String, AssessmentScore> rawScoresMap = new HashMap<String, AssessmentScore>(rawScoresList.size());
						for (AssessmentScore rawScore : rawScoresList) {
							rawScoresMap.put(rawScore.getUser().getJmbag(), rawScore);
						}
						List<AssessmentConfProblemsData> dataList = dh	.getAssessmentDAO()
																		.listConfProblemsDataForAssessement(em,
																		(AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration());
						for (AssessmentConfProblemsData assessmentData : dataList) {
							Double[] oldDscore = assessmentData.getDscore();
							Double[] newDscore = new Double[numberOfProblems];
							
							for (int i = 0; i < newDscore.length; i++) {
								if (i >= oldVal) {
									newDscore[i] = null;
									
								} else {
									newDscore[i] = oldDscore[i];
									
								}
							}
							
							assessmentData.setDscore(newDscore);
							
							AssessmentScore score = rawScoresMap.get(assessmentData.getUser().getJmbag());
							if (score != null) {
								score.setRawPresent(false);
								score.setRawScore(0.0);
							}
						}
					}
					
					
				} else {
					// Nepostojeći parametar.
					
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				return null;
			}
		});
	}
	
	public static void getAdminSetDetailedChoiceConfData (
			final AdminSetDetailedChoiceConfData data,
			final Long userID, final String courseInstanceID, final String assessmentID,
			final String errorColumnText, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;
				
				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				String key = AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
				if(!key.equals("CHOICE")) {
					// Ups! Netko nam hoće podvaliti krivi tip provjere!
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					// Rezultat je success, jer sada možemo van iz POST-a i možemo prikazati poruku greške
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				// TODO: Ne znam u čemu je problem, ali task je uvijek 'init'... Zato sam ovo (zasad) izbacio. :Ivan Krišto
//				if (task.equals("init")) {
//					data.setResult(AbstractActionData.RESULT_SUCCESS);
//					return null;
//				}
				
				AssessmentConfChoice assessmentConfChoice = (AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration();
				
				if (assessmentConfChoice.getErrorColumn()) {
					if (errorColumnText == null || errorColumnText.equals("")) {
						assessmentConfChoice.setErrorColumnText(data.getMessageLogger().getText("forms.defaultErrorColumnText"));
					} else {
						if (errorColumnText.length() > AssessmentConfChoice.ERROR_COLUMN_TEXT_LENGTH) {
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.inputTooLong")
																	+ ": " + data.getMessageLogger().getText("forms.errorColumnText"));
							data.setResult(AbstractActionData.RESULT_SUCCESS);
							return null;
						}
						assessmentConfChoice.setErrorColumnText(errorColumnText);
					}
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.assessmentConfigured"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void getAssessmentCreateAppealData (
			final AssessmentCreateAppealData data, final Long userID,
			final String courseInstanceID, final String assessmentID, final AppealProblemType appealType,
			final String[] values) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;
				if(!data.getAssessment().getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				
				if (userID!=null && !userID.equals(data.getCurrentUser().getId())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				AssessmentScore score = dh.getAssessmentDAO().getScore(em, data.getAssessment(), data.getCurrentUser());
				if (!score.getRawPresent()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				// Odredi tip parametra.
				if (appealType.equals(AppealProblemType.PROBLEM_NOT_EVALUATED)) {
					if (oneIntVal(dh, "problem-number", em) == null) {
						return null;
					}
					
				} else if (appealType.equals(AppealProblemType.CHECK_SCORE_FOR_PROBLEM)) {
					if (oneIntVal(dh, "problem-number", em) == null) {
						return null;
					}
				
				} else if (appealType.equals(AppealProblemType.WRONG_OFFICIAL_SOLUTION)) {
					if (oneIntVal(dh, "problem-number", em) == null) {
						return null;
					}
					
				} else if (appealType.equals(AppealProblemType.NOT_PROCESSED)) {
					AssessmentAppealInstance appeal = AssessmentAppealInstance.createAppeal(data.getAssessment(),
																							data.getCurrentUser(),
																							appealType,
																							"");
					dh.getAssessmentDAO().save(em, appeal);
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.newAppealAdded"));
									
				} else if (appealType.equals(AppealProblemType.SET_SCORE_FOR_PROBLEM)) {
					if (intDoubleVal(dh, "problem-number", "score-value", em) == null) {
						return null;
					}
					
				} else if (appealType.equals(AppealProblemType.BAD_SCAN_OFFER_SOLUTION)) {
					if (intCharVal(dh, "problem-number", "answer-value", em) == null) {
						return null;
					}
					
				} else {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}

			private Object intCharVal(DAOHelper dh, String firstProperty,
					String secondProperty, EntityManager em) {
				AssessmentAppealInstance appeal = AssessmentAppealInstance.createAppeal(data.getAssessment(),
																						data.getCurrentUser(),
																						appealType,
																						"");
				if (values.length != 2) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				int intProblem;
				try {
					intProblem = Integer.parseInt(values[0]);
				} catch (NumberFormatException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.numberFormatError") + " " + values[0]);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				appeal.addProperty(firstProperty, intProblem);
				
				String answer = null;
				if (values[1].length() == 1 && Character.isLetter(values[1].charAt(0))) {
					answer = Character.toString(Character.toUpperCase(values[1].charAt(0)));
					
				} else if (values[1].equalsIgnoreCase("BLANK")) {
					answer = new String("BLANK");
					
				} else {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + " " + values[1]);
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
					
				}
				appeal.addProperty(secondProperty, answer);
				
				dh.getAssessmentDAO().save(em, appeal);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.newAppealAdded"));
				
				return this;
			}

			private Object intDoubleVal(DAOHelper dh, String firstProperty,
					String secondProperty, EntityManager em) {
				
				AssessmentAppealInstance appeal = AssessmentAppealInstance.createAppeal(data.getAssessment(),
																						data.getCurrentUser(),
																						appealType,
																						"");
				if (values.length != 2) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				int intProblem;
				try {
					intProblem = Integer.parseInt(values[0]);
				} catch (NumberFormatException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.numberFormatError") + " " + values[0]);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				appeal.addProperty(firstProperty, intProblem);
				
				double score;
				try {
					score = Double.parseDouble(values[1]);
				} catch (NumberFormatException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.numberFormatError") + " " + values[1]);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				appeal.addProperty(secondProperty, score);
				
				dh.getAssessmentDAO().save(em, appeal);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.newAppealAdded"));
				
				return this;
			}

			private Object oneIntVal(DAOHelper dh, String property, EntityManager em) {
				AssessmentAppealInstance appeal = AssessmentAppealInstance.createAppeal(data.getAssessment(),
																						data.getCurrentUser(),
																						appealType,
																						"");
				if (values.length != 1) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				int intVal;
				try {
					intVal = Integer.parseInt(values[0]);
				} catch (NumberFormatException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.numberFormatError") + " " + values[0]);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				appeal.addProperty(property, intVal);
				dh.getAssessmentDAO().save(em, appeal);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.newAppealAdded"));
				
				return this;
			}
		});
	}

	@Deprecated
	public static void getAssessmentSummaryViewData(
			final AssessmentSummaryViewData data, final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(dh!=null) return null; // Ovo je uvijek istina pa efektivno blokiram metodu
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				List<AssessmentScore> score = dh.getAssessmentDAO().listScoresForCourseInstanceAndUser(em, data.getCourseInstance(), data.getCurrentUser());
				data.setScore(score);
				List<AssessmentFlagValue> flagValues = dh.getAssessmentDAO().listFlagValuesForCourseInstanceAndUser(em, data.getCourseInstance(), data.getCurrentUser());
				data.setFlagValues(flagValues);
				
				// ----- POCETAK: POKUSAJ HIJERARHIJSKE PRIPREME PODATKA; NAJNOVIJE IZDANJE (verzija 2) ---
				Dependencies deps = JCMSCacheFactory.getCache().getDependencies(data.getCourseInstance().getId());
				//data.setDependenciesJSON(deps.toJSONStringBuilder().toString());
				List<TreeRenderingClues> renderingClues;
				if(deps==null) {
					// Necu prikazati nista!
					renderingClues = new ArrayList<TreeRenderingClues>();
				} else {
					deps = cloneDependencies(deps, data.getCourseInstance().getAssessments(), data.getCourseInstance().getFlags());
					Map<String,Object> objectMap = new HashMap<String, Object>(100);
					Map<String,Object> valueMap = new HashMap<String, Object>(100);
					for(Assessment a : data.getCourseInstance().getAssessments()) {
						objectMap.put(a.getShortName()+":A", a);
					}
					for(AssessmentFlag a : data.getCourseInstance().getFlags()) {
						objectMap.put(a.getShortName()+":F", a);
					}
					for(AssessmentScore s : score) {
						valueMap.put(s.getAssessment().getShortName()+":A", s);
					}
					for(AssessmentFlagValue s : flagValues) {
						valueMap.put(s.getAssessmentFlag().getShortName()+":F", s);
					}
					renderingClues = new ArrayList<TreeRenderingClues>(objectMap.size()*4);
					Set<String> visibles = new HashSet<String>(50);
					for(DependencyItem di : deps.getRoots()) {
						checkAssessmentRenderingVisibility(di, objectMap, valueMap, visibles);
					}
					for(DependencyItem di : deps.getRoots()) {
						fillAssessmentRenderingClues(di, objectMap, valueMap, visibles, renderingClues);
					}
				}
				// Blokirano; metoda je prebacena 
				// data.setRenderingClues(renderingClues);
				// ----- KRAJ: POKUSAJ HIJERARHIJSKE PRIPREME PODATKA; NAJNOVIJE IZDANJE (verzija 2) ---
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}

			@Deprecated
			private void fillAssessmentRenderingClues(DependencyItem di, Map<String, Object> objectMap,
					Map<String, Object> valueMap, Set<String> visibles, List<TreeRenderingClues> renderingClues) {

				if(!visibles.contains(di.getUniqueID())) {
					return;
				}
				
				Object assessmentOrFlag = objectMap.get(di.getUniqueID());
				
				int type;
				Object object;
				Object value;
				
				if(assessmentOrFlag instanceof Assessment) {
					Assessment a = (Assessment)assessmentOrFlag;
					AssessmentScore sc = (AssessmentScore)valueMap.get(di.getUniqueID());
					type = TreeRenderingClues.TYPE_ASSESSMENT;
					object = a;
					value = sc;
				} else {
					AssessmentFlag a = (AssessmentFlag)assessmentOrFlag;
					AssessmentFlagValue sc = (AssessmentFlagValue)valueMap.get(di.getUniqueID());
					type = TreeRenderingClues.TYPE_FLAG;
					object = a;
					value = sc;
				}

				renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_ITEM_START,type,object,value));

				boolean hasVisibleChildren = false;
				
				for(DependencyItem diChild : di.getDependencies()) {
					if(visibles.contains(diChild.getUniqueID())) {
						hasVisibleChildren = true;
						break;
					}
				}

				if(hasVisibleChildren) {
					renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_CHILDREN_START,type,object,value));
					for(DependencyItem diChild : di.getDependencies()) {
						fillAssessmentRenderingClues(diChild, objectMap, valueMap, visibles, renderingClues);
					}
					renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_CHILDREN_END,type,object,value));
				}
				
				renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_ITEM_END,type,object,value));
			}
			
			@Deprecated
			private void checkAssessmentRenderingVisibility(DependencyItem di, Map<String, Object> objectMap,
					Map<String, Object> valueMap, Set<String> visibles) {
				Object assessmentOrFlag = objectMap.get(di.getUniqueID());
				if(assessmentOrFlag instanceof Assessment) {
					Assessment a = (Assessment)assessmentOrFlag;
					char visibility = a.getVisibility();
					if(visibility=='H') return; // Student ovo ne vidi
					if(visibility=='E') {
						// ovo mozda vidi...
						AssessmentScore sc = (AssessmentScore)valueMap.get(di.getUniqueID());
						// Ako nema bodova, ili ako nije bio, ne vidi!
						if(sc==null || !sc.getEffectivePresent()) {
							return;
						}
					}
					// Inace ovo vidi
				} else {
					AssessmentFlag a = (AssessmentFlag)assessmentOrFlag;
					char visibility = a.getVisibility();
					if(visibility=='H') return; // Student ovo ne vidi
					if(visibility=='E') {
						// ovo mozda vidi...
						AssessmentFlagValue sc = (AssessmentFlagValue)valueMap.get(di.getUniqueID());
						// Ako nema zastavice, ili ako je ona false, ne vidi!
						if(sc==null || !sc.getValue()) {
							return;
						}
					}
					// Inace ovo vidi
				}
				visibles.add(di.getUniqueID());
				// Ako sam tu, ovo je vidljivo; ajmo pogledati sto je s djecom...
				for(DependencyItem diChild : di.getDependencies()) {
					checkAssessmentRenderingVisibility(diChild, objectMap, valueMap, visibles);
				}
			}

			@Deprecated
			protected Dependencies cloneDependencies(Dependencies deps,	Set<Assessment> assessments, Set<AssessmentFlag> flags) {
				DependencyItem[] originalRoots = deps.getRoots();
				final Map<String,SortData> sortIndexes = new HashMap<String, SortData>(assessments.size()+flags.size());
				for(Assessment a : assessments) {
					SortData sd = new SortData(a.getShortName()+":A", a.getName(), a.getSortIndex());
					sortIndexes.put(sd.uniqueID,sd);
				}
				for(AssessmentFlag f : flags) {
					SortData sd = new SortData(f.getShortName()+":F", f.getName(), f.getSortIndex());
					sortIndexes.put(sd.uniqueID,sd);
				}
				Comparator<DependencyItem> comparator = new Comparator<DependencyItem>() {
					@Override
					public int compare(DependencyItem o1, DependencyItem o2) {
						if(o1==null) {
							if(o2!=null) return -1;
							return 0;
						} else if(o2==null) {
							return 1;
						}
						SortData sd1 = sortIndexes.get(o1.getUniqueID());
						SortData sd2 = sortIndexes.get(o2.getUniqueID());
						if(sd1==null) {
							if(sd2!=null) return -1;
							return 0;
						} else if(sd2==null) {
							return 1;
						}
						int d = sd1.sortIndex - sd2.sortIndex;
						if(d!=0) return d;
						return StringUtil.HR_COLLATOR.compare(sd1.uniqueID, sd2.uniqueID);
					}
				};
				return new Dependencies(deps.getCourseInstanceID(), cloneDependencyItemArray(originalRoots, sortIndexes, new HashMap<Object,Object>(200), comparator, new HashSet<Object>(200)));
			}
			
			@Deprecated
			private DependencyItem[] cloneDependencyItemArray(DependencyItem[] originalArray, Map<String, SortData> sortIndexes, Map<Object,Object> cache, Comparator<DependencyItem> comparator, Set<Object> cyclePrevention) {
				if(originalArray==null) return null;
				DependencyItem[] cloned = (DependencyItem[])cache.get(originalArray);
				if(cloned!=null) return cloned;
				if(!cyclePrevention.add(originalArray)) {
					// Ups! Vec je bio tamo! Vrtim se u krug!
					System.out.println("Otkriven ciklus kod kloniranja DependencyItem[].");
					return null;
				}
				cloned = new DependencyItem[originalArray.length];
				for(int i = 0; i < originalArray.length; i++) {
					DependencyItem item = originalArray[i];
					cloned[i] = cloneDependencyItem(item, sortIndexes, cache, comparator, cyclePrevention);
				}
				Arrays.sort(cloned, comparator);
				cache.put(originalArray, cloned);
				cyclePrevention.remove(originalArray);
				return cloned;
			}

			@Deprecated
			private DependencyItem cloneDependencyItem(DependencyItem item,	Map<String, SortData> sortIndexes, Map<Object, Object> cache, Comparator<DependencyItem> comparator, Set<Object> cyclePrevention) {
				if(item==null) return null;
				DependencyItem cloned = (DependencyItem)cache.get(item);
				if(cloned!=null) return cloned;
				if(!cyclePrevention.add(item)) {
					// Ups! Vec je bio tamo! Vrtim se u krug!
					System.out.println("Otkriven ciklus kod kloniranja DependencyItem.");
					return null;
				}
				cloned = new DependencyItem(item.getUniqueID(), cloneDependencyItemArray(item.getDependencies(), sortIndexes, cache, comparator, cyclePrevention));
				cache.put(item, cloned);
				cyclePrevention.remove(item);
				return cloned;
			}

			@Deprecated
			class SortData {
				String uniqueID;
				int sortIndex;
				public SortData(String uniqueID, String name, int sortIndex) {
					super();
					this.uniqueID = uniqueID;
					this.sortIndex = sortIndex;
				}
			}
		});
	}
	
	@Deprecated
	public static class TreeRenderingClues {
		public static final int EVENT_ITEM_START     = 1;
		public static final int EVENT_CHILDREN_START = 2;
		public static final int EVENT_CHILDREN_END   = 3;
		public static final int EVENT_ITEM_END       = 4;

		public static final int TYPE_ASSESSMENT     = 1;
		public static final int TYPE_FLAG           = 2;
		
		private Object object; // Ovo ce kod renderiranja bodova biti assessment ili assessmentflag
		private Object value; // Ovo ce kod renderiranja bodova biti assessmentScore ili assessmentFlagValue
		private int event; // 1...item start, 2...children group start, 3...children group end, 4...item end  
		private int objectType; // 1...assessment, 2...flag, ...
		
		public TreeRenderingClues(int event, int objectType, Object object,
				Object value) {
			super();
			this.event = event;
			this.objectType = objectType;
			this.object = object;
			this.value = value;
		}
		
		public Object getObject() {
			return object;
		}
		
		public int getObjectType() {
			return objectType;
		}
		
		public Object getValue() {
			return value;
		}
		
		public int getEvent() {
			return event;
		}
	}
	
	public static void getConfPreloadScoreEditData (
			final ConfPreloadScoreEditData data, final ConfPreloadScoreEditBean bean,
			final Long userID, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
				if(!fillAssessment(em, data, bean.getAssessmentID())) return null;
				// Pronadi sve korisnike koje ovaj smije vidjeti. Ovo je za sada sve, ali kasnije ovisno o dozvolama treba ukljuciti samo neke!
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				// Selekcija po dvoranama ili po abecedi -- pocetak
				List<User> courseUsers = null;
				if(StringUtil.isStringBlank(bean.getSelectedRoomID())) {
					bean.setSelectedRoomID("*");
				}
				if(!bean.getSelectedRoomID().equals("*")) {
					Set<User> users = getAssessmentRoomUsers(bean.getSelectedRoomID(), data.getAssessment());
					if(users==null) {
						bean.setSelectedRoomID(null);
						courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
					} else {
						courseUsers = new ArrayList<User>(users);
						bean.setLetter('*');
					}
				} else {
					courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
				}
				List<KeyValueBean> rooms = getAssessmentRooms(data.getAssessment());
				if(rooms.size()==1) {
					rooms.clear();
				}
				bean.setRooms(rooms);
				// Selekcija po dvoranama ili po abecedi -- kraj
				
				//List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
				List<AssessmentScore> list = dh.getAssessmentDAO().listScoresForAssessment(em, data.getAssessment());
				Set<Character> letters = new HashSet<Character>();
				for(User u : courseUsers) {
					letters.add(Character.valueOf(u.getLastName().charAt(0)));
				}
				List<String> lettersAsStrings = new ArrayList<String>(letters.size());
				for(Character c : letters) {
					lettersAsStrings.add(c.toString());
				}
				Collections.sort(lettersAsStrings,StringUtil.HR_COLLATOR);
				Character selLetter = bean.getLetter();
				if(selLetter==null && lettersAsStrings.size()>0) {
					selLetter = Character.valueOf(lettersAsStrings.get(0).charAt(0));
				}
				lettersAsStrings.add("*");
				data.setAvailableLetters(lettersAsStrings);
				// Ako nema slova, to znači da nema niti jednog korisnika...
				if(selLetter==null) {
					bean.setItems(new ArrayList<ConfPreloadScoreBean>());
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				bean.setLetter(selLetter);
				List<User> selectedUsers = new ArrayList<User>(selLetter.charValue()=='*' ? courseUsers.size() : courseUsers.size()/10);
				if(selLetter.charValue()=='*') {
					for(User u : courseUsers) {
						selectedUsers.add(u);
					}
				} else {
					for(User u : courseUsers) {
						if(u.getLastName().charAt(0)==selLetter.charValue()) {
							selectedUsers.add(u);
						}
					}
				}
				Collections.sort(selectedUsers,StringUtil.USER_COMPARATOR);
				List<AssessmentScore> scores = new ArrayList<AssessmentScore>(selectedUsers.size());
				Map<User,AssessmentScore> map = new HashMap<User, AssessmentScore>();
				Set<User> selectedUsersSet = new HashSet<User>(selectedUsers);
				for(AssessmentScore s : list) {
					if(selectedUsersSet.contains(s.getUser())) {
						scores.add(s);
						map.put(s.getUser(), s);
					}
				}
				if(task.equals("edit") || task.equals("pickLetter")) {
					List<ConfPreloadScoreBean> l = new ArrayList<ConfPreloadScoreBean>(selectedUsers.size());
					for(User u : selectedUsers) {
						ConfPreloadScoreBean b = new ConfPreloadScoreBean();
						l.add(b);
						b.setFirstName(u.getFirstName());
						b.setLastName(u.getLastName());
						b.setJmbag(u.getJmbag());
						b.setUserID(u.getId());
						AssessmentScore s = map.get(u);
						if(s==null) {
							b.setId(null);
							b.setScore("");
							b.setVersion(0L);
						} else {
							b.setId(s.getId());
							b.setVersion(s.getVersion());
							if(s.getAssigner()!=null) {
								b.setAssigner(s.getAssigner().getLastName()+" "+s.getAssigner().getFirstName());
							}
							if(s.getRawPresent()) {
								b.setScore(Double.toString(s.getRawScore()));
							} else {
								b.setScore("");
							}
						}
						b.setOScore(b.getScore());
					}
					bean.setItems(l);
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(!task.equals("save")) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Map<Long,AssessmentScore> mapByID = new HashMap<Long, AssessmentScore>();
				for(AssessmentScore s : scores) {
					mapByID.put(s.getId(), s);
				}

				Map<Long,User> mapByUser = new HashMap<Long, User>(selectedUsers.size());
				for(User u : selectedUsers) {
					mapByUser.put(u.getId(), u);
				}
				boolean prevara = false; boolean greska = false;
				Double[] bodovi = new Double[bean.getItems().size()];
				int i = -1;
				for(ConfPreloadScoreBean b : bean.getItems()) {
					i++;
					if(b.getId()!=null) {
						AssessmentScore s = mapByID.get(b.getId());
						if(s==null) {
							prevara = true;
						}
					} else {
						if(b.getUserID()==null) {
							prevara = true;
						} else {
							User u = mapByUser.get(b.getUserID());
							if(u==null) {
								prevara = true;
							}
						}
					}
					try {
						bodovi[i] = StringUtil.stringToDouble(b.getScore());
					} catch(Exception ex) {
						greska = true;
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.doubleNumberFormatException"));
					}
				}
				if(prevara) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(greska) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				i = -1;
				for(ConfPreloadScoreBean b : bean.getItems()) {
					i++;
					boolean change = !StringUtil.stringEqualsLoosly(b.score, b.oScore);
					if(!change) continue;
					if(b.getId()!=null) {
						AssessmentScore s = mapByID.get(b.getId());
						boolean azuriraj = false;
						// Ako je zapamcena verzija ista kao i u bazi...
						if(b.getVersion()>=s.getVersion()) {
							azuriraj = true;
						} else {
							// Inace je u bazi veca verzija...
							Double score = bodovi[i];
							// Ako zelim nesto obrisati, onemoguci to
							if(score==null) {
								azuriraj = false;
								data.getMessageLogger().addWarningMessage("Student "+s.getUser().getJmbag()+": brisanje vrijednosti je preskoceno zbog paralelne promjene u bazi.");
							} else {
								boolean difference = !areDoublesEqual(s.getRawScore(), score.doubleValue(), 1E-5);
								if(!difference) {
									azuriraj = false;
								} else if(!s.getRawPresent()) {
									azuriraj = true;
									data.getMessageLogger().addWarningMessage("Student "+s.getUser().getJmbag()+": bodovi su upisani iako je u bazi pronadeno novije stanje koje je bilo bez bodova.");
								} else {
									azuriraj = false;
									data.getMessageLogger().addWarningMessage("Student "+s.getUser().getJmbag()+": azuriranje vrijednosti je preskoceno zbog paralelne promjene u bazi.");
								}
							}
						}
						if(azuriraj) {
							Double score = bodovi[i];
							if(score==null) {
								// Ako je score==null, to znaci da nije bio na tome...
								// Ako trebam ažurirati, onda to napravi
								if(s.getRawPresent() || !areDoublesEqual(s.getRawScore(), 0, 1E-5)) {
									s.setRawPresent(false);
									s.setRawScore(0);
									s.setAssigner(data.getCurrentUser());
								}
							} else {
								s.setRawPresent(true);
								s.setRawScore(score.doubleValue());
								s.setAssigner(data.getCurrentUser());
							}
						}
					} else {
						User u = mapByUser.get(b.getUserID());
						AssessmentScore s = new AssessmentScore();
						s.setAssessment(data.getAssessment());
						s.setError(false);
						s.setUser(u);
						s.setStatus(AssessmentStatus.PASSED);
						Double score = bodovi[i];
						if(score==null) {
							s.setRawPresent(false);
							s.setRawScore(0);
						} else {
							s.setRawPresent(true);
							s.setRawScore(score.doubleValue());
							s.setAssigner(data.getCurrentUser());
						}
						dh.getAssessmentDAO().save(em, s);
						data.getAssessment().getScore().add(s);
					}
				}
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void getAdminListAppealsData (
			final AdminListAppealsData data, final AdminListAppealsBean bean,
			final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!BasicBrowsing.fillCurrentUser(em, data, userID)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<AssessmentAppealInstance> appeals = dh.getAssessmentDAO().listAppealsForCourse(em, data.getCourseInstance());
				bean.setAppeals(appeals);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	/**
	 * Za zadanu provjeru dohvaca popis dvorana koje su zauzete i koje imaju raspodijeljene studente.
	 * Key/Name je ID od AssessmentRoom objekta, value je naziv prostorije.
	 * Ako lista ima samo jedan element, treba je discardati, jer to znaci da nije napravljen
	 * raspored po dvoranama, vec lista sadrzi samo "*".
	 * @param assessment
	 * @return
	 */
	private static List<KeyValueBean> getAssessmentRooms(Assessment assessment) {
		assessment = findAssessmentWithRoomSchedule(assessment);
		List<KeyValueBean> list = new ArrayList<KeyValueBean>();
		if(assessment.getRooms()!=null) {
			for(AssessmentRoom ar : assessment.getRooms()) {
				if(ar.isTaken() && ar.getGroup()!=null && !ar.getGroup().getUsers().isEmpty()) {
					list.add(new KeyValueBean(ar.getId().toString(), ar.getRoom()==null ? "?" : ar.getRoom().getName()));
				}
			}
		}
		Collections.sort(list, new Comparator<KeyValueBean>() {
			@Override
			public int compare(KeyValueBean o1, KeyValueBean o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		list.add(new KeyValueBean("*", "*"));
		return list;
	}

	private static boolean assessmentContainsRoomSchedule(Assessment a) {
		if(a.getRooms()!=null && !a.getRooms().isEmpty()) {
			for(AssessmentRoom ar : a.getRooms()) {
				// Ako sam nasao barem jednu sobu koja ima dodijeljene studente:
				if(ar.isTaken() && ar.getGroup()!=null && !ar.getGroup().getUsers().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Gleda ima li doticna provjera raspored studenata, ili to ima njezin roditelj, pa vraca onu koja to ima.
	 * Ako nema niti jedna, vraca onu koja je predana kao argument.
	 * @param original
	 * @return provjeru koja ima raspored studenata
	 */
	private static Assessment findAssessmentWithRoomSchedule(Assessment original) {
		if(assessmentContainsRoomSchedule(original)) return original;
		if(original.getParent()!=null && assessmentContainsRoomSchedule(original.getParent())) return original.getParent();
		return original;
	}
	
	/**
	 * Za zadani assessmentRoomID vraća skup studenata koji su u toj sobi, ili null ako nema te sobe, ili nije zauzeta, ili je prazna.
	 * @param assessmentRoomID
	 * @param assessment
	 * @return
	 */
	private static Set<User> getAssessmentRoomUsers(String assessmentRoomID, Assessment assessment) {
		assessment = findAssessmentWithRoomSchedule(assessment);
		if(assessment.getRooms()!=null) {
			for(AssessmentRoom ar : assessment.getRooms()) {
				if(ar.isTaken() && ar.getGroup()!=null && !ar.getGroup().getUsers().isEmpty() && ar.getId().toString().equals(assessmentRoomID)) {
					Set<User> set = new HashSet<User>(ar.getGroup().getUsers().size());
					for(UserGroup ug : ar.getGroup().getUsers()) {
						set.add(ug.getUser());
					}
					return set;
				}
			}
		}
		return null;
	}
	
	public static void getConfProblemsScoreEditData (
			final ConfProblemsScoreEditData data, final ConfProblemsScoreEditBean bean,
			final Long userID, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
				if(!fillAssessment(em, data, bean.getAssessmentID())) return null;
				// Pronadi sve korisnike koje ovaj smije vidjeti. Ovo je za sada sve, ali kasnije ovisno o dozvolama treba ukljuciti samo neke!
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				// Provjeri je li numberOfProblems postavljen
				int numberOfProblems = ((AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration()).getNumberOfProblems();
				if (numberOfProblems == 0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.numberOfProblemsNotSet"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				// Selekcija po dvoranama ili po abecedi -- pocetak
				List<User> courseUsers = null;
				if(StringUtil.isStringBlank(bean.getSelectedRoomID())) {
					bean.setSelectedRoomID("*");
				}
				if(!bean.getSelectedRoomID().equals("*")) {
					Set<User> users = getAssessmentRoomUsers(bean.getSelectedRoomID(), data.getAssessment());
					if(users==null) {
						bean.setSelectedRoomID(null);
						courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
					} else {
						courseUsers = new ArrayList<User>(users);
						bean.setLetter('*');
					}
				} else {
					courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
				}
				List<KeyValueBean> rooms = getAssessmentRooms(data.getAssessment());
				if(rooms.size()==1) {
					rooms.clear();
				}
				bean.setRooms(rooms);
				// Selekcija po dvoranama ili po abecedi -- kraj
				
				List<AssessmentConfProblemsData> list = dh	.getAssessmentDAO()
															.listConfProblemsDataForAssessement(em, 
															(AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration());
				Set<Character> letters = new HashSet<Character>();
				for(User u : courseUsers) {
					letters.add(Character.valueOf(u.getLastName().charAt(0)));
				}
				List<String> lettersAsStrings = new ArrayList<String>(letters.size());
				for(Character c : letters) {
					lettersAsStrings.add(c.toString());
				}
				Collections.sort(lettersAsStrings,StringUtil.HR_COLLATOR);
				Character selLetter = bean.getLetter();
				if(selLetter==null && lettersAsStrings.size()>0) {
					selLetter = Character.valueOf(lettersAsStrings.get(0).charAt(0));
				}
				lettersAsStrings.add("*");
				data.setAvailableLetters(lettersAsStrings);
				// Ako nema slova, to znači da nema niti jednog korisnika...
				if(selLetter==null) {
					bean.setItems(new ArrayList<ConfProblemsScoreBean>());
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				bean.setLetter(selLetter);
				List<User> selectedUsers = new ArrayList<User>(selLetter.charValue()=='*' ? courseUsers.size() : courseUsers.size()/10);
				if(selLetter.charValue()=='*') {
					for(User u : courseUsers) {
						selectedUsers.add(u);
					}
				} else {
					for(User u : courseUsers) {
						if(u.getLastName().charAt(0)==selLetter.charValue()) {
							selectedUsers.add(u);
						}
					}
				}
				Collections.sort(selectedUsers,StringUtil.USER_COMPARATOR);
				List<AssessmentConfProblemsData> scores = new ArrayList<AssessmentConfProblemsData>(selectedUsers.size());
				Map<User,AssessmentConfProblemsData> map = new HashMap<User, AssessmentConfProblemsData>();
				Set<User> selectedUsersSet = new HashSet<User>(selectedUsers);
				for(AssessmentConfProblemsData s : list) {
					if(selectedUsersSet.contains(s.getUser())) {
						scores.add(s);
						map.put(s.getUser(), s);
					}
				}
				
				bean.setNumberOfProblems(numberOfProblems);
				if(task.equals("edit") || task.equals("pickLetter")) {
					List<ConfProblemsScoreBean> l = new ArrayList<ConfProblemsScoreBean>(selectedUsers.size());
					for(User u : selectedUsers) {
						ConfProblemsScoreBean b = new ConfProblemsScoreBean();
						l.add(b);
						b.setFirstName(u.getFirstName());
						b.setLastName(u.getLastName());
						b.setJmbag(u.getJmbag());
						b.setUserID(u.getId());
						b.setNumberOfProblems(numberOfProblems);
						AssessmentConfProblemsData s = map.get(u);
						if(s==null) {
							b.setId(null);
							b.setScore(new String[numberOfProblems]);
							b.setPresent(false);
							b.setOPresent(false);
							b.setGroup(null);
							b.setOGroup(null);
							b.setOldScore(new String[numberOfProblems]);
							b.setVersion(0);
						} else {
							b.setPresent(s.getPresent());
							b.setOPresent(s.getPresent());
							b.setId(s.getId());
							if(s.getAssigner()!=null) {
								b.setAssigner(s.getAssigner().getLastName()+" "+s.getAssigner().getFirstName());
							}
							b.setVersion(s.getVersion());
							if(s.getPresent()) {
								b.setGroup(s.getGroup());
								b.setOGroup(s.getGroup());
								String[] score = doubleArrayToStringArray(s.getDscore());
								if (score == null) {
									score = new String[numberOfProblems];
								}
								b.setScore(score);
								b.setOldScore(score);
							} else {
								b.setGroup(null);
								b.setOGroup(null);
								b.setScore(new String[numberOfProblems]);
								b.setOldScore(b.getScore());
							}
						}
					}
					bean.setItems(l);
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				if(!task.equals("save")) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Map<Long,AssessmentConfProblemsData> mapByID = new HashMap<Long, AssessmentConfProblemsData>();
				for(AssessmentConfProblemsData s : scores) {
					mapByID.put(s.getId(), s);
				}

				Map<Long,User> mapByUser = new HashMap<Long, User>(selectedUsers.size());
				for(User u : selectedUsers) {
					mapByUser.put(u.getId(), u);
				}
				boolean prevara = false;
				
				Map<Integer, Double[]> mapDoubleScores = new HashMap<Integer, Double[]>();
				Integer index = -1;
				for(ConfProblemsScoreBean b : bean.getItems()) {
					index++;
					if(b.getId()!=null) {
						AssessmentConfProblemsData s = mapByID.get(b.getId());
						if(s==null) {
							prevara = true;
							break;
						}
						if (b.getPresent()) {
							Double[] doubleScore = null;
							try {
								doubleScore = stringArrayToDoubleArray(b.getScore());
							} catch (NumberFormatException e) {
								data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.doubleNumberFormatException"));
								data.setResult(AbstractActionData.RESULT_FATAL);
								return null;
							}
							mapDoubleScores.put(index, doubleScore);
						}
					} else {
						if(b.getUserID()==null) {
							prevara = true;
							break;
						} else {
							User u = mapByUser.get(b.getUserID());
							if(u==null) {
								prevara = true;
								break;
							}
						}
						if (b.getPresent()) {
							Double[] doubleScore = null;
							try {
								doubleScore = stringArrayToDoubleArray(b.getScore());
							} catch (NumberFormatException e) {
								data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.doubleNumberFormatException"));
								data.setResult(AbstractActionData.RESULT_FATAL);
								return null;
							}
							mapDoubleScores.put(index, doubleScore);
						}
					}
				}
				
				if(prevara) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				index = -1;
				for(ConfProblemsScoreBean b : bean.getItems()) {
					index++;
					// Vidi najprije je li ista mijenjano
					boolean modified = b.getPresent()!=b.getOPresent() 
						|| !StringUtil.stringEqualsLoosly(b.getGroup(), b.getOGroup()) 
						|| !Arrays.equals(b.getScore(), b.getOldScore());
					// Ako redak nije modificiran, vozi dalje 
					if(!modified) continue;
					// Ako sam modificirao postojeci redak
					if(b.getId()!=null) {
						AssessmentConfProblemsData s = mapByID.get(b.getId());
						// I ako taj vise u bazi ne postoji:
						if(s==null) {
							data.getMessageLogger().addErrorMessage("Zapis koji ste uređivali u međuvremenu je izbrisan iz baze!");
							data.setResult(AbstractActionData.RESULT_FATAL);
							return null;
						}
						// Ako su u bazi stariji (ili bas ocitani) podaci, smijes ih izmijeniti
						if(b.getVersion() >= s.getVersion()) {
							if (b.getPresent()) {
								// Ako trebam ažurirati, onda to napravi
								Double[] doubleScore = mapDoubleScores.get(index);
								s.setDscore(doubleScore);
								s.setPresent(true);
								s.setGroup(b.getGroup());
								s.setAssigner(data.getCurrentUser());
							} else {
								s.setPresent(false);
								s.setDscore(null);
								s.setGroup(null);
							}
						} else {
							data.getMessageLogger().addWarningMessage("Podaci o studentu "+s.getUser().getFirstName()+" "+s.getUser().getLastName()+" ("+s.getUser().getJmbag()+") nisu ažurirani jer ih je netko promijenio u međuvremenu.");
						}
					} else if (b.getPresent()) {
						User u = mapByUser.get(b.getUserID());
						AssessmentConfProblemsData s = map.get(u);
						if(s!=null) {
							if(s.getPresent()) {
								data.getMessageLogger().addWarningMessage("Podaci o studentu "+s.getUser().getFirstName()+" "+s.getUser().getLastName()+" ("+s.getUser().getJmbag()+") nisu ažurirani jer ih je netko promijenio u međuvremenu.");
								continue;
							}
						} else {
							s = new AssessmentConfProblemsData();
							s.setAssessmentConfProblems((AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration());
							s.setUser(u);
						}
						Double[] score = mapDoubleScores.get(index);
						s.setPresent(true);
						s.setDscore(score);
						s.setAssigner(data.getCurrentUser());
						s.setGroup(b.getGroup());
						if(s.getId()==null) {
							dh.getAssessmentDAO().save(em, s);
						}
					}
				}
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	private static String[] doubleArrayToStringArray(Double[] doubles) {
		if (doubles == null) {
			return null;
		}
		
		String[] strings = new String[doubles.length];
		
		for (int i = 0; i < strings.length; i++) {
			if (doubles[i] == null) {
				strings[i] = "";
			} else {
				strings[i] = doubles[i].toString();
			}
		}
		
		return strings;
	}
	
	private static boolean areDoublesEqual(double d1, double d2, double tolerance) {
		double d = Math.abs(d1-d2);
		return d <= tolerance;
	}
	
	public static void getConfProblemsCalculateResultsData (
			final CalculateConfProblemsResultsData data, final Long userID,
			final String courseInstanceID, final String assessmentID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!getAdminAssessmentViewData(em, data, userID, courseInstanceID, assessmentID)) return null;

				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				// Provjeri je li numberOfProblems postavljen
				int numberOfProblems = ((AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration()).getNumberOfProblems();
				if (numberOfProblems == 0) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.numberOfProblemsNotSet"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
				Set<User> courseUsersSet = new HashSet<User>(courseUsers);
				List<AssessmentScore> rawScoresList = dh.getAssessmentDAO().listScoresForAssessment(em, data.getAssessment());
				List<AssessmentConfProblemsData> listScoreData = dh	.getAssessmentDAO()
																	.listConfProblemsDataForAssessement(em,
																	(AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration());
				if (listScoreData.isEmpty()) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.ConfProblemsScoreSumNoData"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				Map<String, AssessmentConfProblemsData> scoreDataMap = new HashMap<String, AssessmentConfProblemsData>(listScoreData.size());
				for (AssessmentConfProblemsData scoreData : listScoreData) {
					if (!courseUsersSet.contains(scoreData.getUser())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+scoreData.getUser().getJmbag());
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					} else {
						scoreDataMap.put(scoreData.getUser().getJmbag(), scoreData);
					}
				}
				
				Map<String, AssessmentScore> rawScoresMap = new HashMap<String, AssessmentScore>(rawScoresList.size());
				for (AssessmentScore rawScore : rawScoresList) {
					if (!courseUsersSet.contains(rawScore.getUser())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+rawScore.getUser().getJmbag());
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					} else {
						rawScoresMap.put(rawScore.getUser().getJmbag(), rawScore);
					}
				}
				
				for (User user : courseUsers) {
					boolean needToSave = false;
					AssessmentScore rawScore = rawScoresMap.get(user.getJmbag());
					AssessmentConfProblemsData scoreData = scoreDataMap.get(user.getJmbag());
					
					if (rawScore == null) {
						rawScore = new AssessmentScore(data.getAssessment(), user);
						needToSave = true;
					}
					
					if (scoreData == null) {
						rawScore.setRawScore(0.0);
						rawScore.setRawPresent(false);
						rawScore.setAssigner(null);
						rawScore.setError(false);
						rawScore.setStatus(AssessmentStatus.PASSED);
					} else {
						double newRawScore = getRawSum(scoreData);
						double oldRawScore = rawScore.getRawScore();
						boolean oldPresent = rawScore.getRawPresent();
						boolean newPresent = scoreData.getPresent();
						if ((oldRawScore != newRawScore) || (oldPresent != newPresent)) {
							rawScore.setRawScore(newRawScore);
							rawScore.setRawPresent(scoreData.getPresent());
							rawScore.setAssigner(scoreData.getAssigner());
							rawScore.setError(false);
							rawScore.setStatus(AssessmentStatus.PASSED);
						}
					}
					
					if (needToSave) {
						dh.getAssessmentDAO().save(em, rawScore);
					}
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.ConfProblemsScoreSumDone"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getFetchConfExternalResultsData (
			final FetchConfExternalResultsData data, final Long userID,
			final String courseInstanceID, final String assessmentID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@SuppressWarnings("unchecked")
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!getAdminAssessmentViewData(em, data, userID, courseInstanceID, assessmentID)) return null;

				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				// Dohvati CourseComponentItemAssessment koji pokazuje na ovu provjeru; takav bi morao biti samo jedan!
				List<CourseComponentItemAssessment> itemList = (List<CourseComponentItemAssessment>)em.createNamedQuery("CourseComponentItemAssessment.getForAssessment").setParameter("assessment", data.getAssessment()).getResultList();
				if(itemList==null || itemList.size()>1) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidNumberOfCourseComponentItemAssessment"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				CourseComponentItemAssessment itemAssessment = itemList.get(0);
				
				// Dohvati sve korisnike kojima je dodijeljen ovaj test, i povadi njihove jmbagove
				List<CCIAAssignment> assignments = dh.getCourseComponentDAO().getItemAssessmentUsers(em, itemAssessment);
				List<String> logins = new ArrayList<String>(assignments.size());
				Map<String,User> userMap = new HashMap<String, User>(assignments.size());
				for(CCIAAssignment a : assignments) {
					logins.add(a.getUser().getUsername());
					userMap.put(a.getUser().getUsername(), a.getUser());
				}

				// Postojece scorove dohvati i pospremi u mapu
				Map<String,AssessmentScore> scoreMap = new HashMap<String, AssessmentScore>(logins.size()>assessment.getScore().size() ? logins.size() : assessment.getScore().size());
				for(AssessmentScore s : assessment.getScore()) {
					scoreMap.put(s.getUser().getUsername(), s);
				}
				
				int offset = 0; int batchSize = 100;
				while(offset < logins.size()) {
					int end = offset+batchSize;
					if(end > logins.size()) end = logins.size();
					TestsService.fetchAndStoreExternalResults(data.getMessageLogger(), data.getCurrentUser().getUsername(), itemAssessment, logins.subList(offset, end), assessment, scoreMap);
					offset = end;
				}

				for(Map.Entry<String,AssessmentScore> entry : scoreMap.entrySet()) {
					AssessmentScore s = entry.getValue(); 
					if(s.getId()!=null) continue;
					String username = entry.getKey();
					s.setUser(userMap.get(username));
					dh.getAssessmentDAO().save(em, s);
				}

				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.RetrievalDone"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getConfChoiceCalculateResultsData (
			final CalculateConfChoiceResultsData data, final Long userID,
			final String courseInstanceID, final String assessmentID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!getAdminAssessmentViewData(em, data, userID, courseInstanceID, assessmentID)) return null;

				Assessment assessment = data.getAssessment();

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!assessment.getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
				Set<User> courseUsersSet = new HashSet<User>(courseUsers);
				List<AssessmentScore> rawScoresList = dh.getAssessmentDAO().listScoresForAssessment(em, data.getAssessment());
				List<AssessmentConfChoiceAnswers> listScoreData = dh.getAssessmentDAO()
																	.listAssessmentConfChoiceAnswersForAssessement(em,
																	(AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration());
				if (listScoreData.isEmpty()) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.ConfChoiceAnswersNoData"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				Map<String, AssessmentConfChoiceAnswers> scoreDataMap = new HashMap<String, AssessmentConfChoiceAnswers>(listScoreData.size());
				for (AssessmentConfChoiceAnswers scoreData : listScoreData) {
					if (!courseUsersSet.contains(scoreData.getUser())) {
						data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+scoreData.getUser().getJmbag());
						//data.setResult(AbstractActionData.RESULT_FATAL);
						//return null;
					} else {
						scoreDataMap.put(scoreData.getUser().getJmbag(), scoreData);
					}
				}
				
				Map<String, AssessmentScore> rawScoresMap = new HashMap<String, AssessmentScore>(rawScoresList.size());
				for (AssessmentScore rawScore : rawScoresList) {
					if (!courseUsersSet.contains(rawScore.getUser())) {
						data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+rawScore.getUser().getJmbag());
						//data.setResult(AbstractActionData.RESULT_FATAL);
						//return null;
					} else {
						rawScoresMap.put(rawScore.getUser().getJmbag(), rawScore);
					}
				}
				
				AssessmentConfChoice acc = (AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration();
				double scoreCorrect = acc.getScoreCorrect();
				double scoreIncorrect = acc.getScoreIncorrect();
				double scoreUnanswered = acc.getScoreUnanswered();
				String detailedScores = acc.getDetailTaskScores();
				boolean useDetailedScores = false;
				Map<String, Double[]> scoresMap = null;
				if (detailedScores != null && !detailedScores.equals("")) {
					useDetailedScores = true;
					scoresMap = new HashMap<String, Double[]>();
					
					String[] scoresByGroupArray = detailedScores.split("\n");
					for (int i = 0; i < scoresByGroupArray.length; i++) {
						if (scoresByGroupArray[i] == null || scoresByGroupArray[i].equals("")) continue;
						String[] scores = TextService.split(scoresByGroupArray[i], '\t');
						if (scores.length != 5) {
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": "
																	+ data.getMessageLogger().getText("forms.detailTaskScores"));
							data.setResult(AbstractActionData.RESULT_FATAL);
							return null;
						}
						Double[] scoresD = new Double[3];
						for (int j = 2; j < scores.length; j++) {
							try {
								double score;
								if (scores[j] == null || scores[j].equals("")) {
									score = 0.0;
								} else {
									score = Double.parseDouble(scores[j]);
								}
								scoresD[j-2] = score;
							} catch (NumberFormatException e) {
								data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters") + ": "
										+ data.getMessageLogger().getText("forms.detailTaskScores"));
										data.setResult(AbstractActionData.RESULT_FATAL);
										return null;
							}
						}
						scoresMap.put(scores[0].trim() + scores[1].trim(), scoresD);
					}
					
				}
				String[] correctAnswers = acc.getCorrectAnswers().split("\n");
				Map<String, String> correctAnswersByGroupMap = new HashMap<String, String>(correctAnswers.length<1 ? 16 : correctAnswers.length);
				for (int i = 0; i < correctAnswers.length; i++) {
					if(correctAnswers[i].isEmpty()) continue;
					int delimiter = correctAnswers[i].indexOf('\t');
					String group = correctAnswers[i].substring(0, delimiter);
					correctAnswersByGroupMap.put(group, correctAnswers[i]);
				}
				
				String[] manipulatorsArray = StringUtil.isStringBlank(acc.getProblemManipulators()) ? new String[0] : StringUtil.split(acc.getProblemManipulators(), '\n');
				Map<String, Map<Integer, String[]>> manipulators = new HashMap<String, Map<Integer,String[]>>();
				for(String row : manipulatorsArray) {
					String[] elems = StringUtil.split(row, '\t');
					Map<Integer, String[]> map = manipulators.get(elems[0]);
					if(map==null) {
						map = new HashMap<Integer, String[]>();
						manipulators.put(elems[0], map);
					}
					Integer i = Integer.valueOf(elems[1]);
					map.put(i, elems);
				}
				for (User user : courseUsers) {
					boolean needToSave = false;
					AssessmentScore rawScore = rawScoresMap.get(user.getJmbag());
					AssessmentConfChoiceAnswers scoreData = scoreDataMap.get(user.getJmbag());
					
					if (rawScore == null) {
						rawScore = new AssessmentScore(data.getAssessment(), user);
						needToSave = true;
					}
					
					if (scoreData == null || scoreData.getGroup() == null || scoreData.getGroup().equals("") || !scoreData.getPresent()) {
						rawScore.setRawScore(0.0);
						rawScore.setRawPresent(false);
						rawScore.setAssigner(null);
						rawScore.setError(false);
						rawScore.setStatus(AssessmentStatus.PASSED);
					} else {
						String group = scoreData.getGroup();
						String answers = scoreData.getAnswers();
						String correctAnswersForGroup = correctAnswersByGroupMap.get(group);
						
						if (correctAnswersForGroup == null) {
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.groupNotFound")+" "+group);
							data.setResult(AbstractActionData.RESULT_FATAL);
							return null;
						}
						
						String answersStatus = null;
						String next;
						
						StringBuilder sb = new StringBuilder(40);
						ChoiceAnswersIterator iteratorAnswers = new ChoiceAnswersIterator(answers, "\t");
						ChoiceAnswersIterator iteratorCorrect = new ChoiceAnswersIterator(correctAnswersForGroup, "\t");
						iteratorCorrect.next();	// Da preskočimo grupu
						
						Map<Integer, String[]> mans = manipulators.get(group);
						
						boolean answerEval; int zadID = 0;
						while ((next = iteratorAnswers.next()) != null) {
							zadID++;
							String[] manipulator = mans==null ? null : mans.get(Integer.valueOf(zadID));
							if (next.equals("BLANK") || next.equals("")) {
								if(manipulator!=null) {
									if(manipulator[2].equals("X")) {
										sb.append('X');
									} else {
										sb.append('-');
									}
								} else {
									sb.append('-');
								}
								iteratorCorrect.next();
							} else {
								answerEval = iteratorCorrect.nextAndTest(next);

								if(manipulator!=null) {
									if(manipulator[2].equals("X") || manipulator[2].equals("x")) {
										sb.append('X');
									} else {
										if (answerEval) {
											sb.append('T');
											
										} else {
											sb.append('N');
											
										}
									}
								} else {
									if (answerEval) {
										sb.append('T');
										
									} else {
										sb.append('N');
										
									}
								}
							}
							sb.append('\t');
						}
						sb.deleteCharAt(sb.lastIndexOf("\t"));
						
						answersStatus = sb.toString();
						scoreData.setAnswersStatus(answersStatus);
						
						ChoiceAnswersIterator iteratorStatus = new ChoiceAnswersIterator(answersStatus, "\t");
						double scoreSum = 0.0;
						double possibleSum = 0.0;
						double offsetSum = 0.0;
						int taskNum = 0;
						while ((next = iteratorStatus.next()) != null) {
							taskNum++;
							if (useDetailedScores) {
								Double[] val;
								val = scoresMap.get(group + taskNum);
								if (val == null) {
									scoreCorrect = 0.0;
									scoreIncorrect = 0.0;
									scoreUnanswered = 0.0;
								} else {
									scoreCorrect = val[0];
									scoreIncorrect = val[1];
									scoreUnanswered = val[2];
								}
							}
							if (next.equals("T")) {
								scoreSum += scoreCorrect;
								possibleSum += scoreCorrect;
								
							} else if (next.equals("N")) {
								scoreSum += scoreIncorrect;
								possibleSum += scoreCorrect;
								
							} else if (next.equals("-")) {
								scoreSum += scoreUnanswered;
								possibleSum += scoreCorrect;
							} else if(next.equals("X")) {
								offsetSum += scoreCorrect;
							}
						}
						
						if(offsetSum>1E-5 && possibleSum>1E-5) {
							scoreSum = (offsetSum+possibleSum)/possibleSum*scoreSum;
						}
						rawScore.setRawScore(scoreSum);
						rawScore.setRawPresent(scoreData.getPresent());
						rawScore.setAssigner(scoreData.getAssigner());
						rawScore.setError(false);
						rawScore.setStatus(AssessmentStatus.PASSED);
					}
					
					if (needToSave) {
						dh.getAssessmentDAO().save(em, rawScore);
					}
				}
				
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.ConfProblemsScoreSumDone"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	private static double getRawSum(AssessmentConfProblemsData scoreData) {
		double rawScore = 0.0;
		Double[] scores = scoreData.getDscore();
		
		if (scores != null) {
			for (Double singleScore : scores) {
				rawScore += ((singleScore != null) ? singleScore.doubleValue() : 0.0);
			}
		}
		
		return rawScore;
	}

	public static void getAssessmentFileDownloadData(
			final AssessmentFileDownloadData data, final Long userID,
			final String courseInstanceID, final String assessmentID,
			final String assessmentFileID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;
				if(!data.getAssessment().getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Long afid = null;
				try {
					afid = Long.valueOf(assessmentFileID);
				} catch(Exception ex) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				AssessmentFile file = dh.getAssessmentDAO().getAssessmentFile(em, afid);
				if(file==null) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(file.getOriginalFileName()!=null) {
					data.setNameToSend(file.getOriginalFileName());
				} else {
					data.setNameToSend(file.getId()+"."+file.getExtension());
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				
				// Dozvole:
				boolean ok = false;
				if(file.getUser()==null) {
					// Ako nema korisnika, OK je ako je to osoblje kolegija ili student kolegija, ili vanjski asistent koji ovo cuva:
					if(JCMSSecurityManagerFactory.getManager().isStaffOnCourse(data.getCourseInstance()) || JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance())) {
						ok = true;
					} else {
						User user = data.getCurrentUser();
						
						boolean guestAssistant = false;
						for(AssessmentRoom ar : data.getAssessment().getRooms()) {
							if(ar.getUserEvent()!=null && ar.getUserEvent().getUsers().contains(user)) {
								guestAssistant = true;
								break;
							}
						}

						if(guestAssistant) ok = true;
					}
				} else {
					// Ako ima korisnika, to moze vidjeti samo taj korisnik, ili pak administrativno osoblje kolegija, ili njegov nastavnik
					if(file.getUser().equals(data.getCurrentUser())) {
						ok = true;
					} else if(JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
						ok = true;
					} else if(JCMSSecurityManagerFactory.getManager().isUserStudentsLecturer(data.getCourseInstance(),file.getUser())) {
						ok = true;
					}
				}
				
				if(!ok) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setFile(file);
				File dir = JCMSSettings.getSettings().getFilesRootDir();
				dir = new File(dir, "A-"+data.getAssessment().getId());
				File filePath = new File(dir, file.getId().toString()); 
				data.setFilePath(filePath);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated /** Maknuto u AssessmentsStudentViewService */
	public static void getAssessmentViewData(final AssessmentViewData data, final AssessmentViewBean bean,
			final Long loggedInUserID, final String courseInstanceID, final String assessmentID,
			final String requestedUserID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, loggedInUserID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;
				if(!data.getAssessment().getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Long reqUserID = null;
				try {
					if(requestedUserID!=null && requestedUserID.length()>0) reqUserID = Long.valueOf(requestedUserID);
				} catch(Exception ex) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);

				User user = data.getCurrentUser();
				if(reqUserID!=null && !reqUserID.equals(data.getCurrentUser().getId())) {
					// Ako je eksplicitno zadan korisnik, to je mogao traziti samo
					// administrator ili osoblje kolegija!
					boolean canView = JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance());
					if(!canView) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					// Dakle, ako trenutni korisnik nije u toj kategoriji, vrati mu error
					// inace:
					user = dh.getUserDAO().getUserById(em, reqUserID);
					if(user==null) {
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				// Vidi je li user uopce na kolegiju
				List<UserGroup> ugroups = dh.getGroupDAO().findUserGroupsForUser(em, data.getCourseInstance().getId(), "0", user);
				if(ugroups.isEmpty()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				//AssessmentScore
				AssessmentScore score = dh.getAssessmentDAO().getScore(em, data.getAssessment(), user);
				data.setScore(score);

				List<AssessmentFile> myFiles = dh.getAssessmentDAO().listAssessmentFilesForUser(em, data.getAssessment(), user);
				List<AssessmentFile> aFiles = dh.getAssessmentDAO().listAssessmentFilesForAssessment(em, data.getAssessment());
				Collections.sort(myFiles);
				Collections.sort(aFiles);
				List<AssessmentFile> allFiles = new ArrayList<AssessmentFile>(myFiles.size()+aFiles.size());
				allFiles.addAll(aFiles);
				allFiles.addAll(myFiles);
				data.setFiles(allFiles);
				data.setAssessmentConfigurationKey(AssessmentService.getKeyForAssessmentConfiguration(data.getAssessment().getAssessmentConfiguration()));

				// Ima li postavljenu zastavicu
				if(data.getAssessment().getAssessmentFlag()!=null) {
					AssessmentFlagValue flagValue = dh.getAssessmentDAO().getFlagValue(em, data.getAssessment().getAssessmentFlag(), user);
					data.setFlagValue(flagValue);
					if(flagValue==null) {
						data.setCanTake(false);
					} else {
						data.setCanTake(flagValue.getValue());
					}
				} else {
					data.setCanTake(true);
				}

				AssessmentConfiguration ac = data.getAssessment().getAssessmentConfiguration();
				if(score==null || !score.getPresent()) {
					// Ako nije bio, nista dalje ne dohvacamo...
				} else {
					// Punjenje bean-a
					if (ac instanceof AssessmentConfProblems) {
						AssessmentConfProblems acp = (AssessmentConfProblems) ac;
						AssessmentViewProblemsBean beanData = new AssessmentViewProblemsBean();
						AssessmentConfProblemsData acpData = null;
						if(score.getRawPresent()) {
							// Ako je bio na tome, pokusaj dohvatiti zapis. Ovo ce se zbog uporabe
							// getSingleResult() raspasti ako zapisa NEMA.
							acpData = dh.getAssessmentDAO().getConfProblemsDataForAssessementAndUserId(em, acp, user.getId());
						} else {
							// Ako nije bio na tome, stvori mu defaultni prazan objekt koji se ne pohranjuje u bazu
							acpData = new AssessmentConfProblemsData();
							acpData.setAssessmentConfProblems(acp);
							acpData.setDscore(new Double[acp.getNumberOfProblems()]);
							acpData.setGroup("");
							acpData.setPresent(false);
							acpData.setUser(score.getUser());
						}
						int problemsNum = acp.getNumberOfProblems();
						beanData.setNumberOfProblems(problemsNum);
						beanData.setGroup(acpData==null ? "" : acpData.getGroup());
						beanData.setScores(acpData==null ? new Double[acp.getNumberOfProblems()] : acpData.getDscore());
						Double[] maxScores = null;
						if(!StringUtil.isStringBlank(acp.getScorePerProblem())) {
							String[] rows = StringUtil.split(acp.getScorePerProblem(), '\n');
							String group = beanData.getGroup()+"\t";
							for(String row : rows) {
								if(row.startsWith(group)) {
									maxScores = stringArrayToDoubleArray(StringUtil.split(row, '\t'), 1);
									break;
								}
							}
						}
						beanData.setMaxScores(maxScores);
						bean.setConfType("PROBLEMS");
						bean.setData(beanData);
						String[] problemsIds = new String[problemsNum];
						for (int i = 1; i <= problemsNum; i++) {
							problemsIds[i-1] = Integer.toString(i);
						}
						data.setProblemsIds(problemsIds);
						data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), data.getCurrentUser()));
						
					} else if (ac instanceof AssessmentConfChoice) {
						AssessmentConfChoice acc = (AssessmentConfChoice) ac;
						AssessmentConfChoiceAnswers accAnswers = null;
						if(score.getRawPresent()) {
							// Ako je bio na tome, pokusaj dohvatiti zapis. Ovo ce se zbog uporabe
							// getSingleResult() raspasti ako zapisa NEMA.
							accAnswers = dh	.getAssessmentDAO()
								.getAssessmentConfChoiceAnswersForAssessementAndStudent(em, user, acc);
						} else {
							// Ako nije bio na tome, stvori mu defaultni prazan objekt koji se ne pohranjuje u bazu
							accAnswers = new AssessmentConfChoiceAnswers();
							int numberOfProblems = acc.getProblemsNum();
							StringBuilder sb = new StringBuilder(6*numberOfProblems);
							for(int i = 0; i < numberOfProblems; i++) {
								if(i!=0) sb.append('\t');
								sb.append("BLANK");
							}
							accAnswers.setAnswers(sb.toString());
							sb = new StringBuilder(2*numberOfProblems);
							for(int i = 0; i < numberOfProblems; i++) {
								if(i!=0) sb.append('\t');
								sb.append("-");
							}
							accAnswers.setAnswersStatus(sb.toString());
							accAnswers.setGroup("");
							accAnswers.setPresent(false);
							accAnswers.setUser(score.getUser());
							accAnswers.setAssessmentConfChoice(acc);
						}
						AssessmentViewChoiceBean beanData = new AssessmentViewChoiceBean();
						
						int numberOfProblems = acc.getProblemsNum();
						beanData.setNumberOfProblems(numberOfProblems);
						beanData.setProblemsLabels(StringUtil.split(acc.getProblemsLabels(), '\t'));
						beanData.setAnswers(StringUtil.split(accAnswers.getAnswers().trim(), '\t'));
						data.setProblemsIds(beanData.getProblemsLabels());
						String group = accAnswers.getGroup();
						beanData.setGroup(group);
						group.concat("\t");
						String groupAnswersStr = null;
						String[] allCorrectAnswers = null;
						if(acc.getCorrectAnswers()!=null) {
							allCorrectAnswers = StringUtil.split(acc.getCorrectAnswers().trim(), '\n');
							for (int i = 0; i < allCorrectAnswers.length; i++) {
								if (allCorrectAnswers[i].startsWith(group)) {
									groupAnswersStr = allCorrectAnswers[i];
									break;
								}
							}
						} else {
							allCorrectAnswers = new String[numberOfProblems];
							for(int i = 0; i < numberOfProblems; i++) {
								allCorrectAnswers[i] = "";
							}
						}
						if (groupAnswersStr != null) {
							String[] groupAnswersWithGroup = StringUtil.split(groupAnswersStr.trim(), '\t');
							String[] studentGroupCorrectAnwers = new String[groupAnswersWithGroup.length-1];
							for (int i = 0; i < studentGroupCorrectAnwers.length; i++) {
								studentGroupCorrectAnwers[i] = groupAnswersWithGroup[i+1];
							}
							beanData.setCorrectAnswers(studentGroupCorrectAnwers);
						} else {
							beanData.setCorrectAnswers(new String[numberOfProblems]);
						}
						if (accAnswers.getAnswersStatus() != null) {
							beanData.setAnswersStatus(StringUtil.split(accAnswers.getAnswersStatus(), '\t'));
						} else {
							beanData.setAnswersStatus(new String[numberOfProblems]);
						}
						
						if (acc.getDetailTaskScores() == null || acc.getDetailTaskScores().equals("")) {
							beanData.setUsingDetailedTaskScores(false);
							beanData.setScoreIncorrect(acc.getScoreIncorrect());
							beanData.setScoreUnanswered(acc.getScoreUnanswered());
							beanData.setScoreCorrect(acc.getScoreCorrect());
						} else {
							beanData.setUsingDetailedTaskScores(true);
							String[] detailedScores = StringUtil.split(acc.getDetailTaskScores(), '\n');
							String[] detailedScoresCorrect = new String[numberOfProblems];
							String[] detailedScoresIncorrect = new String[numberOfProblems];
							String[] detailedScoresUnanswered = new String[numberOfProblems];
							
							// varijabla group završava sa '\t'!
							for (int i = 0, dpNum = 0; i < detailedScores.length; i++) {
								if (detailedScores[i].startsWith(group)) {
									String[] dScores = StringUtil.split(detailedScores[i], '\t');
									detailedScoresCorrect[dpNum] = dScores[2];
									detailedScoresIncorrect[dpNum] = dScores[3];
									detailedScoresUnanswered[dpNum] = dScores[4];
									dpNum++;
								}
							}
							beanData.setDetailedScoresCorrect(detailedScoresCorrect);
							beanData.setDetailedScoresIncorrect(detailedScoresIncorrect);
							beanData.setDetailedScoresUnanswered(detailedScoresUnanswered);
						}
						
						bean.setData(beanData);
						bean.setConfType("CHOICE");
						
						int answersNum = acc.getAnswersNumber();
						String[] answers = new String[answersNum];
						for (int i = 0; i < answersNum; i++) {
							answers[i] = Character.toString((char)('A' + i));
						}
						data.setAnswers(answers);
						
						data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), data.getCurrentUser()));
						
					} else {
						String[] problemsIds = new String[] {"1"};
						data.setProblemsIds(problemsIds);
						data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), data.getCurrentUser()));
					}
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}


	public static void getAssessmentPreloadInsightData(final AssessmentPreloadInsightData data,
			final Long loggedInUserID, final String courseInstanceID, final String assessmentID,
			final String requestedUserID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, loggedInUserID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;
				if(!data.getAssessment().getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Long reqUserID = null;
				try {
					if(requestedUserID!=null && requestedUserID.length()>0) reqUserID = Long.valueOf(requestedUserID);
				} catch(Exception ex) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);

				User user = data.getCurrentUser();
				if(reqUserID!=null && !reqUserID.equals(data.getCurrentUser().getId())) {
					// Ako je eksplicitno zadan korisnik, to je mogao traziti samo
					// administrator ili osoblje kolegija!
					boolean canView = JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance());
					if(!canView) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					// Dakle, ako trenutni korisnik nije u toj kategoriji, vrati mu error
					// inace:
					user = dh.getUserDAO().getUserById(em, reqUserID);
					if(user==null) {
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				//AssessmentScore
				AssessmentScore score = dh.getAssessmentDAO().getScore(em, data.getAssessment(), user);
				data.setScore(score);
				
				List<AssessmentFile> myFiles = dh.getAssessmentDAO().listAssessmentFilesForUser(em, data.getAssessment(), data.getCurrentUser());
				List<AssessmentFile> lista2 = new ArrayList<AssessmentFile>(myFiles);
				Collections.sort(myFiles);
				for (AssessmentFile file : myFiles) {
					// Svaka slika mora imati opisnik koji ima format scan-<broj>
					if (!file.getDescriptor().matches("scan-[0-9]++")) {
						lista2.remove(file);
					}
				}
				myFiles = lista2;
				
				data.setFiles(myFiles);
				data.setAssessmentConfigurationKey(AssessmentService.getKeyForAssessmentConfiguration(data.getAssessment().getAssessmentConfiguration()));

				// Ima li postavljenu zastavicu
				if(data.getAssessment().getAssessmentFlag()!=null) {
					AssessmentFlagValue flagValue = dh.getAssessmentDAO().getFlagValue(em, data.getAssessment().getAssessmentFlag(), user);
					data.setFlagValue(flagValue);
					if(flagValue==null) {
						data.setCanTake(false);
					} else {
						data.setCanTake(flagValue.getValue());
					}
				} else {
					data.setCanTake(true);
				}
				
				String[] problemsIds = new String[] {"1"};
				data.setProblemsIds(problemsIds);
				data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), data.getCurrentUser()));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void getAssessmentProblemsInsightData(final AssessmentProblemsInsightData data,
			final Long loggedInUserID, final String courseInstanceID, final String assessmentID,
			final String requestedUserID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, loggedInUserID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;
				if(!data.getAssessment().getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Long reqUserID = null;
				try {
					if(requestedUserID!=null && requestedUserID.length()>0) reqUserID = Long.valueOf(requestedUserID);
				} catch(Exception ex) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);

				User user = data.getCurrentUser();
				if(reqUserID!=null && !reqUserID.equals(data.getCurrentUser().getId())) {
					// Ako je eksplicitno zadan korisnik, to je mogao traziti samo
					// administrator ili osoblje kolegija!
					boolean canView = JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance());
					if(!canView) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					// Dakle, ako trenutni korisnik nije u toj kategoriji, vrati mu error
					// inace:
					user = dh.getUserDAO().getUserById(em, reqUserID);
					if(user==null) {
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				//AssessmentScore
				AssessmentScore score = dh.getAssessmentDAO().getScore(em, data.getAssessment(), user);
				data.setScore(score);

				List<AssessmentFile> myFiles = dh.getAssessmentDAO().listAssessmentFilesForUser(em, data.getAssessment(), data.getCurrentUser());
				List<AssessmentFile> lista2 = new ArrayList<AssessmentFile>(myFiles);
				Collections.sort(myFiles);
				for (AssessmentFile file : myFiles) {
					// Svaka slika mora imati opisnik koji ima format scan-<broj>
					if (!file.getDescriptor().matches("scan-[0-9]++")) {
						lista2.remove(file);
					}
				}
				myFiles = lista2;
				data.setFiles(myFiles);
				data.setAssessmentConfigurationKey(AssessmentService.getKeyForAssessmentConfiguration(data.getAssessment().getAssessmentConfiguration()));

				// Ima li postavljenu zastavicu
				if(data.getAssessment().getAssessmentFlag()!=null) {
					AssessmentFlagValue flagValue = dh.getAssessmentDAO().getFlagValue(em, data.getAssessment().getAssessmentFlag(), user);
					data.setFlagValue(flagValue);
					if(flagValue==null) {
						data.setCanTake(false);
					} else {
						data.setCanTake(flagValue.getValue());
					}
				} else {
					data.setCanTake(true);
				}
				
				int problemsNum = ((AssessmentConfProblems) data.getAssessment().getAssessmentConfiguration()).getNumberOfProblems();
				String[] problemsIds = new String[problemsNum];
				for (int i = 1; i <= problemsNum; i++) {
					problemsIds[i-1] = Integer.toString(i);
				}
				data.setProblemsIds(problemsIds);
				data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), data.getCurrentUser()));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void getAssessmentChoiceInsightData(final AssessmentChoiceInsightData data,
			final Long loggedInUserID, final String courseInstanceID, final String assessmentID,
			final String requestedUserID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, loggedInUserID)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				if(!fillAssessment(em, data, assessmentID)) return null;
				if(!data.getAssessment().getCourseInstance().equals(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Long reqUserID = null;
				try {
					if(requestedUserID!=null && requestedUserID.length()>0) reqUserID = Long.valueOf(requestedUserID);
				} catch(Exception ex) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);

				User user = data.getCurrentUser();
				if(reqUserID!=null && !reqUserID.equals(data.getCurrentUser().getId())) {
					// Ako je eksplicitno zadan korisnik, to je mogao traziti samo
					// administrator ili osoblje kolegija!
					boolean canView = JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance());
					if(!canView) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					// Dakle, ako trenutni korisnik nije u toj kategoriji, vrati mu error
					// inace:
					user = dh.getUserDAO().getUserById(em, reqUserID);
					if(user==null) {
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				//AssessmentScore
				AssessmentScore score = dh.getAssessmentDAO().getScore(em, data.getAssessment(), user);
				data.setScore(score);

				List<AssessmentFile> myFiles = dh.getAssessmentDAO().listAssessmentFilesForUser(em, data.getAssessment(), data.getCurrentUser());
				List<AssessmentFile> lista2 = new ArrayList<AssessmentFile>(myFiles);
				Collections.sort(myFiles);
				for (AssessmentFile file : myFiles) {
					// Svaka slika mora imati opisnik koji ima format scan-<broj>
					if (!file.getDescriptor().matches("scan-[0-9]++")) {
						lista2.remove(file);
					}
				}
				myFiles = lista2;
				data.setFiles(myFiles);
				data.setAssessmentConfigurationKey(AssessmentService.getKeyForAssessmentConfiguration(data.getAssessment().getAssessmentConfiguration()));

				// Ima li postavljenu zastavicu
				if(data.getAssessment().getAssessmentFlag()!=null) {
					AssessmentFlagValue flagValue = dh.getAssessmentDAO().getFlagValue(em, data.getAssessment().getAssessmentFlag(), user);
					data.setFlagValue(flagValue);
					if(flagValue==null) {
						data.setCanTake(false);
					} else {
						data.setCanTake(flagValue.getValue());
					}
				} else {
					data.setCanTake(true);
				}
				
				int problemsNum = ((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration()).getProblemsNum();
				String problemLabels = ((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration()).getProblemsLabels();
				String[] problemsIds;
				if (problemLabels != null) {
					problemsIds = StringUtil.split(problemLabels, '\t');
				} else {
					problemsIds = new String[problemsNum];
					for (int i = 1; i <= problemsNum; i++) {
						problemsIds[i-1] = Integer.toString(i);
					}
				}
				data.setProblemsIds(problemsIds);
				
				int answersNum = ((AssessmentConfChoice) data.getAssessment().getAssessmentConfiguration()).getAnswersNumber();
				String[] answers = new String[answersNum];
				for (int i = 0; i < answersNum; i++) {
					answers[i] = Character.toString((char)('A' + i));
				}
				data.setAnswers(answers);
				
				data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), data.getCurrentUser()));
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	public static void getGroupOwnershipData(final GroupOwnershipData data,
			final Long userID, final GroupOwnershipBean bean, final Set<String> usersFrom, final String parentRelativePath, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
			if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
			bean.setCourseInstanceID(data.getCourseInstance().getId());
			JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
			boolean canManage = JCMSSecurityManagerFactory.getManager().canManageLectureGroupOwners(data.getCourseInstance());
			if(!canManage) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return null;
			}
			List<GroupOwner> list = DAOHelperFactory.getDAOHelper().getGroupDAO().findForSubgroups(em, data.getCourseInstance().getId(), parentRelativePath);
			List<UserGroup> allUserGroups = DAOHelperFactory.getDAOHelper().getGroupDAO().listUserGroupsInGroupTree(em, data.getCourseInstance().getId(), "3");
			Set<User> allUsersSet = new HashSet<User>(allUserGroups.size());
			for(UserGroup ug : allUserGroups) {
				if(usersFrom.contains(ug.getGroup().getRelativePath())) {
					allUsersSet.add(ug.getUser());
				}
			}
			List<User> allUsers = new ArrayList<User>(allUsersSet);
			Collections.sort(allUsers, StringUtil.USER_COMPARATOR);
			Map<Long, User> userByIdMap = new HashMap<Long, User>(allUserGroups.size());
			for(int i = 0; i < allUsers.size(); i++) {
				userByIdMap.put(allUsers.get(i).getId(), allUsers.get(i));
			}
			List<Group> allGroups = DAOHelperFactory.getDAOHelper().getGroupDAO().findSubgroups(em, data.getCourseInstance().getId(), parentRelativePath+"/%");
			Collections.sort(allGroups,StringUtil.GROUP_COMPARATOR);
			data.setAllGroups(allGroups);
			if(task.equals("input")) {
				List<GroupOwnershipsBean> ows = new ArrayList<GroupOwnershipsBean>(allUsers.size());
				bean.setUsers(ows);
				for(User user : allUsers) {
					GroupOwnershipsBean bean = new GroupOwnershipsBean();
					bean.setFirstName(user.getFirstName());
					bean.setLastName(user.getLastName());
					bean.setJmbag(user.getJmbag());
					bean.setId(user.getId());
					Set<Long> ownedGroups = new HashSet<Long>();
					bean.setGroups(ownedGroups);
					for(GroupOwner go : list) {
						if(!go.getUser().equals(user)) continue;
						ownedGroups.add(go.getGroup().getId());
					}
					ows.add(bean);
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
			if(!task.equals("update")) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return null;
			}
			Map<Long,Group> groupMap = new HashMap<Long, Group>(allGroups.size());
			for(Group g : allGroups) {
				groupMap.put(g.getId(), g);
			}
			for(GroupOwnershipsBean b : bean.getUsers()) {
				User user = userByIdMap.get(b.getId());
				if(user==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Set<Long> groupIDs = b.getGroups()==null ? new HashSet<Long>() : new HashSet<Long>(b.getGroups());
				for(GroupOwner go : list) {
					if(!go.getUser().equals(user)) continue;
					// Ako korisnik i treba biti vlasnik ove grupe, vozi dalje...
					if(groupIDs.contains(go.getGroup().getId())) {
						groupIDs.remove(go.getGroup().getId());
						continue;
					}
					// Inace, korisnik je vlasnik ove grupe, a vise ne bi smio biti... Obrisi to!
					dh.getGroupDAO().remove(em, go);
				}
				// Sada su u groupIDs ostale samo one grupe kojima bi trebao biti vlasnik a jos nije! Dodaj ih!
				for(Long gid : groupIDs) {
					Group g = groupMap.get(gid);
					if(g==null) {
						// Ups! Netko nesto mulja? Ta grupa nije u legalnim podgrupama!
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					GroupOwner go = new GroupOwner(g, user);
					dh.getGroupDAO().save(em, go);
				}
			}
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return null;
		}
	});
	}

	public static void getUserImportData(final UserImportData data, final Long userID,
			final List<UserBean> list, final Long authTypeID, final Set<String> roles,
			final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canImport = JCMSSecurityManagerFactory.getManager().canEditAccouts();
				// Ako korisnik ne može mijenjati accounte, javi gresku.
				if(!canImport) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setAllAuthTypes(dh.getAuthTypeDAO().list(em));
				List<Role> allRoles = dh.getRoleDAO().list(em);
				Collections.sort(allRoles);
				data.setAllRoles(allRoles);
				if(task.equals("input")) {
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(!task.equals("importList")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				AuthType authType = authTypeID == null ? null : dh.getAuthTypeDAO().get(em, authTypeID);
				if(authType==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				List<Role> availableRoles = dh.getRoleDAO().list(em);
				Map<String, Role> roleMap = RoleUtil.mapRolesByName(availableRoles);
				Set<Role> targetRoles = new HashSet<Role>();
				for(String roleName : roles) {
					Role role = roleMap.get(roleName);
					if(role==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					targetRoles.add(role);
				}
				List<String> jmbags = new ArrayList<String>(list.size());
				for(UserBean bean : list) {
					jmbags.add(bean.getJmbag());
				}
				List<User> users = dh.getUserDAO().getFullForJmbagSublistBatching(em, jmbags);
				Map<String, User> map = UserUtil.mapUserByJmbag(users);
				Random r = new Random();
				char[] slova = new char[12];
				for(UserBean bean : list) {
					User user = map.get(bean.getJmbag());
					UserDescriptor udes;
					boolean isnew = false;
					if(user==null) {
						isnew = true;
						user = new User();
						udes = new UserDescriptor();
						user.setUserDescriptor(udes);
						udes.setExternalID(SynchronizerService.createExternalID(r));
					} else {
						udes = user.getUserDescriptor();
					}
					if(isnew) user.setJmbag(bean.getJmbag());
					if(isnew) user.setFirstName(bean.getFirstName());
					if(isnew) user.setLastName(bean.getLastName());
					// Ako username nije zadan, provjeri da vec ne postoji, pa ako ne, postavi defaultne vrijednosti
					boolean dataValid = false;
					if(StringUtil.isStringBlank(bean.getUsername())) {
						if(user.getUsername()==null) {
							user.setUsername(bean.getJmbag());
							udes.setAuthUsername(bean.getJmbag());
						} else if(!user.getUsername().equals(user.getJmbag())) {
							dataValid = udes.getDataValid();
						}
					} else {
						dataValid = true;
						user.setUsername(bean.getUsername());
						udes.setAuthUsername(bean.getAuthUsername());
					}
					// ako je email zadan, pregazi ga...
					String dummyMail = user.getJmbag()+"@fer.hr";
					if(!StringUtil.isStringBlank(bean.getEmail()) && dummyMail.equals(udes.getEmail())) {
						udes.setEmail(bean.getEmail());
					}
					udes.setAuthType(authType);
					udes.setDataValid(dataValid);
					udes.setLocked(false);
					if(udes.getPassword()==null) {
						for(int i = 0; i < slova.length; i++) {
							slova[i] = (char)(r.nextInt('Z'-'A')+'A');
						}
						udes.setPassword(StringUtil.encodePassword(new String(slova), "SHA"));
					}
					udes.getRoles().addAll(targetRoles);
					if(user.getId()==null) {
						dh.getUserDAO().save(em, user);
						map.put(user.getJmbag(), user);
					}
				}
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getJMBAGUsernameImportData(final JMBAGUsernameImportData data,
			final Long userID, final List<JMBAGLoginBean> list, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canImport = JCMSSecurityManagerFactory.getManager().canEditAccouts();
				// Ako korisnik ne može mijenjati accounte, javi gresku.
				if(!canImport) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(task.equals("input")) {
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(!task.equals("importList")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				List<String> jmbags = new ArrayList<String>(list.size());
				for(JMBAGLoginBean bean : list) {
					jmbags.add(bean.getJmbag());
				}
				List<User> users = dh.getUserDAO().getFullForJmbagSublistBatching(em, jmbags);
				Map<String, User> map = UserUtil.mapUserByJmbag(users);
				int skippedCounter = 0;
				int fixedCounter = 0;
				for(JMBAGLoginBean bean : list) {
					User user = map.get(bean.getJmbag());
					if(user==null) {
						data.getMessageLogger().addErrorMessage("Korisnik "+bean.getJmbag()+" nije pronađen.");
						continue;
					}
					if(user.getUsername().equals(bean.getUsername())) {
						skippedCounter++;
						continue;
					}
					user.setUsername(bean.getUsername());
					user.getUserDescriptor().setAuthUsername(bean.getUsername());
					user.getUserDescriptor().setDataValid(true);
					fixedCounter++;
				}
				data.getMessageLogger().addInfoMessage("Preskočeno "+skippedCounter+" i ažurirano "+fixedCounter+" korisnik(a).");
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getUserActionData(final UserActionData data, final Long userID, final UserBean bean, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canBrowse = JCMSSecurityManagerFactory.getManager().canBrowseAccouts();
				// Ako korisnik ne može browse-ati accounte, javi gresku.
				boolean issueError = !canBrowse;
				// Medutim, da bi korisnik mijenjao sam svoje podatke, ne treba mu dozvola. Dakle:
				boolean selfEditing = data.getCurrentUser().getId().equals(bean.getId());
				if(selfEditing) {
					issueError = false;
				}
				if(issueError) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCanViewAll(canBrowse);
				
				List<Role> roles = dh.getRoleDAO().list(em);
				Collections.sort(roles);
				data.setAvailableRoles(roles);
	
				List<AuthType> authTypes = dh.getAuthTypeDAO().list(em);
				data.setAvailableAuthTypes(authTypes);
				
				if(task.equals("fillSearch")) {
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				if(task.equals("fillNew")) {
					if(!JCMSSecurityManagerFactory.getManager().canAddAccouts()) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					bean.setRoles(new ArrayList<String>());
					bean.setPreferences(new HashMap<String, Object>());
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
				if(task.equals("find") || task.equals("resetExternalID")) {
					User user = null;
					if(bean.getId()!=null) {
						user = dh.getUserDAO().getUserById(em, bean.getId());
					} else if(bean.getUsername()!=null && bean.getUsername().length()>0) {
						user = dh.getUserDAO().getUserByUsername(em, bean.getUsername());
					} else if(bean.getJmbag()!=null && bean.getJmbag().length()>0) {
						user = dh.getUserDAO().getUserByJMBAG(em, bean.getJmbag());
					} else {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noUserSearchData"));
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
					if(user==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userSearchFailed"));
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
					
					// Ako trebamo resetirati ID
					if(task.equals("resetExternalID")) {
						Random rnd = new Random();
						user.getUserDescriptor().setExternalID(SynchronizerService.createExternalID(rnd));
						data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.externalIDResetted"));
					}
					bean.setAuthTypeID(user.getUserDescriptor().getAuthType().getId());
					bean.setAuthUsername(user.getUserDescriptor().getAuthUsername());
					bean.setDataValid(user.getUserDescriptor().getDataValid());
					bean.setEmail(user.getUserDescriptor().getEmail());
					bean.setFirstName(user.getFirstName());
					bean.setId(user.getId());
					bean.setJmbag(user.getJmbag());
					bean.setLastName(user.getLastName());
					bean.setLocked(user.getUserDescriptor().getLocked());
					List<String> uRoles = new ArrayList<String>(user.getUserDescriptor().getRoles().size());
					for(Role r : user.getUserDescriptor().getRoles()) {
						uRoles.add(r.getName());
					}
					Collections.sort(uRoles);
					bean.setRoles(uRoles);
					bean.setUsername(user.getUsername());
					fillUserPreferences(bean, user);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				if(!task.equals("update")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				boolean doesntHavePermissions = (bean.getId()==null && !JCMSSecurityManagerFactory.getManager().canAddAccouts()) 
											|| (bean.getId()!=null && !JCMSSecurityManagerFactory.getManager().canEditAccouts());
				
				if(!selfEditing && doesntHavePermissions) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				if(!checkUserData(data.getMessageLogger(), bean, doesntHavePermissions)) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				User user = null;
				UserDescriptor udes = null;
				Random rnd = new Random();
				if(bean.getId()!=null) {
					user = dh.getUserDAO().getUserById(em, bean.getId());
					if(user==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userSearchFailed"));
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
					udes = user.getUserDescriptor();
				} else {
					user = new User();
					udes = new UserDescriptor();
					udes.setExternalID(SynchronizerService.createExternalID(rnd));
					user.setUserDescriptor(udes);
				}
				if(!doesntHavePermissions) {
					boolean error = false;
					AuthType authType = dh.getAuthTypeDAO().get(em, bean.getAuthTypeID());
					if(authType==null) error = true;
					Map<String, Role> rolesMap = RoleUtil.mapRolesByName(roles);
					Set<Role> newRoleSet = new HashSet<Role>();
					if(!error && bean.getRoles()!=null) {
						for(String r : bean.getRoles()) {
							if(!rolesMap.containsKey(r)) {
								error = true;
								break;
							}
							newRoleSet.add(rolesMap.get(r));
						}
					}
					if(error) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
					udes.getRoles().retainAll(newRoleSet);
					udes.getRoles().addAll(newRoleSet);
					udes.setAuthType(authType);
				}
				udes.setEmail(bean.getEmail());
				if(!StringUtil.isStringBlank(bean.getPassword())) {
					udes.setPassword(StringUtil.encodePassword(bean.getPassword(), "SHA"));
				}
				
				UserActivityPrefs prefs = udes.getUserActivityPrefs()==null ? new UserActivityPrefs() : udes.getUserActivityPrefs();
				Properties p = StringUtil.getPropertiesFromString(prefs.getProperties());
				p.setProperty("mail", bean.getPreferences().get("mail").toString());
				prefs.setProperties(StringUtil.getStringFromProperties(p));
				if(udes.getUserActivityPrefs()==null) {
					udes.setUserActivityPrefs(prefs);
				}

				if(doesntHavePermissions) {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					data.setUser(user);
					return null;
				}
				user.setJmbag(bean.getJmbag());
				user.setFirstName(bean.getFirstName());
				user.setLastName(bean.getLastName());
				user.setUsername(bean.getUsername());
				udes.setAuthUsername(bean.getAuthUsername());
				udes.setDataValid(bean.getDataValid());
				udes.setLocked(bean.getLocked());

				if(user.getId()==null) {
					dh.getUserDAO().save(em, user);
				}
				data.setUser(user);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}

			private void fillUserPreferences(UserBean bean, User user) {
				UserActivityPrefs prefs = user.getUserDescriptor().getUserActivityPrefs();
				if(prefs==null) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("mail", Boolean.valueOf(false));
					bean.setPreferences(map);
					return;
				}
				Map<String, Object> map = new HashMap<String, Object>();
				Properties p = StringUtil.getPropertiesFromString(prefs.getProperties());
				map.put("mail", Boolean.valueOf(p.getProperty("mail", "false").equals("true")));
				bean.setPreferences(map);
			}
		});
	}

	// Osoba koja nema ovlasti (dakle, sam korisnik) može mijenjati samo svoj email te zaporku.
	protected static boolean checkUserData(IMessageLogger messageLogger, UserBean bean, boolean doesntHavePermissions) {
		boolean ok = true;
		if(!StringUtil.stringEquals(bean.getPassword(), bean.getDoublePassword())) {
			messageLogger.addErrorMessage("Zaporke se ne slažu!");
			ok = false;
		}
		if(StringUtil.isStringBlank(bean.getEmail())) {
			messageLogger.addErrorMessage("EMail je obavezno polje.");
			ok = false;
		}
		if(bean.getPreferences()==null) {
			bean.setPreferences(new HashMap<String, Object>());
		}
		Object o = bean.getPreferences().get("mail");
		if(o instanceof String) {
			String s = (String)o;
			bean.getPreferences().put("mail", Boolean.valueOf(s.equals("true") || s.equals("1")));
		} else if(o instanceof String[]) {
			String[] ss = (String[])o;
			String s = ss.length>0 ? ss[0] : "false";
			bean.getPreferences().put("mail", Boolean.valueOf(s.equals("true") || s.equals("1")));
		} else {
			bean.getPreferences().put("mail", Boolean.valueOf(false));
		}
		if(doesntHavePermissions) return ok;
		if(StringUtil.isStringBlank(bean.getFirstName())) {
			messageLogger.addErrorMessage("Ime je obavezno polje.");
			ok = false;
		}
		if(StringUtil.isStringBlank(bean.getLastName())) {
			messageLogger.addErrorMessage("Prezime je obavezno polje.");
			ok = false;
		}
		if(StringUtil.isStringBlank(bean.getUsername())) {
			messageLogger.addErrorMessage("Username je obavezno polje.");
			ok = false;
		}
		if(StringUtil.isStringBlank(bean.getAuthUsername())) {
			messageLogger.addErrorMessage("AuthUsername je obavezno polje.");
			ok = false;
		}
		if(StringUtil.isStringBlank(bean.getJmbag())) {
			messageLogger.addErrorMessage("JMBAG je obavezno polje.");
			ok = false;
		}
		if(bean.getAuthTypeID()==null) {
			messageLogger.addErrorMessage("AuthTypeID je obavezno polje.");
			ok = false;
		}
		return ok;
	}

	public static void getCourseUsersListJSONData(final CourseUsersListJSONData data,
			final Long userID, final String courseInstanceID, final String relativePath, final String userText) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) {
					data.setUsers(new ArrayList<User>());
					return null;
				}
				if(!fillCourseInstance(em, data, courseInstanceID)) {
					data.setUsers(new ArrayList<User>());
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canBrowse = JCMSSecurityManagerFactory.getManager().canObtainCourseUsersList(data.getCourseInstance(), relativePath);
				if(!canBrowse) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					data.setUsers(new ArrayList<User>());
					return null;
				}
				UserText ut = UserText.parse(userText);
				List<User> users = null;
				if(relativePath==null || relativePath.trim().length()==0) {
					users = new ArrayList<User>();
				} else {
					if(ut.getLastName() != null) {
						if(ut.getFirstName() != null) {
							if(ut.getJmbag() != null) {
								users = dh.getGroupDAO().listUsersInGroupTree(em, courseInstanceID, relativePath, ut.getLastName(), ut.getFirstName(), ut.getJmbag());
							} else {
								users = dh.getGroupDAO().listUsersInGroupTree(em, courseInstanceID, relativePath, ut.getLastName(), ut.getFirstName());
							}
						} else {
							users = dh.getGroupDAO().listUsersInGroupTree(em, courseInstanceID, relativePath, ut.getLastName());
						}
					} else {
						users = dh.getGroupDAO().listUsersInGroupTree(em, courseInstanceID, relativePath);
					}
				}
				Collections.sort(users, StringUtil.USER_COMPARATOR);
				data.setUsers(users);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getStaffUsersListJSONData(final StaffUsersListJSONData data,
			final Long userID, final String userText) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) {
					data.setUsers(new ArrayList<User>());
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canBrowse = JCMSSecurityManagerFactory.getManager().canObtainStaffList();
				if(!canBrowse) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					data.setUsers(new ArrayList<User>());
					return null;
				}
				if(StringUtil.isStringBlank(userText) || userText.length()<2) {
					data.setUsers(new ArrayList<User>());
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				UserText ut = UserText.parse(userText);
				List<User> users = null;
				if(ut.getLastName() != null) {
					if(ut.getFirstName() != null) {
						if(ut.getJmbag() != null) {
							users = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_COURSE_STAFF, ut.getLastName(), ut.getFirstName(), ut.getJmbag());
						} else {
							users = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_COURSE_STAFF, ut.getLastName(), ut.getFirstName());
						}
					} else {
						users = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_COURSE_STAFF, ut.getLastName());
					}
				} else {
					users = new ArrayList<User>();
					// Za omoguciti globalni bezkriterijski dohvat svih koristiti ovo dolje - no ne preporuca se!
					// users = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_COURSE_STAFF);
				}
				Collections.sort(users, StringUtil.USER_COMPARATOR);
				data.setUsers(users);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getEditCoursePermissionsData(final EditCoursePermissionsData data, final CourseUserPermissionsBean bean, final Long userID, final String courseInstanceID, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageCourseUsersList(data.getCourseInstance(), "3");
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				List<CoursePermissionBean> availablePerms = new ArrayList<CoursePermissionBean>();
				for(int i = 0; i < JCMSSecurityConstants.getSecurityCourseRolesCount(); i++) {
					String relPath = JCMSSecurityConstants.getSecurityCourseRole(i);
					availablePerms.add(new CoursePermissionBean(relPath, data.getMessageLogger().getText(relPath)));
				}
				Collections.sort(availablePerms, new Comparator<CoursePermissionBean>() {
					@Override
					public int compare(CoursePermissionBean o1, CoursePermissionBean o2) {
						return o1.getTitle().compareTo(o2.getTitle());
					}
				});
				data.setAvailablePermissions(availablePerms);
				//if(task.equals("main")) {
				//	data.setResult(AbstractActionData.RESULT_SUCCESS);
				//	return null;
				//}
				List<Group> secGroups = dh.getGroupDAO().findSubgroups(em, courseInstanceID, JCMSSecurityConstants.SEC_ROLE_GROUP, JCMSSecurityConstants.SEC_ROLE_GROUP+"/%");
				mapAndCreateSecurityGroups(em, data.getCourseInstance(), secGroups);
				List<User> ulist = dh.getGroupDAO().listUsersInGroupTree(em, courseInstanceID, JCMSSecurityConstants.SEC_ROLE_GROUP);
				Collections.sort(ulist, StringUtil.USER_COMPARATOR);
				
				List<CourseUserPermissions> plist = new ArrayList<CourseUserPermissions>(ulist.size());
				Map<Long, CourseUserPermissions> userMap = new HashMap<Long, CourseUserPermissions>();
				int index = -1;
				for(User u : ulist) {
					index++;
					CourseUserPermissions c = new CourseUserPermissions();
					c.setFirstName(u.getFirstName());
					c.setLastName(u.getLastName());
					c.setJmbag(u.getJmbag());
					c.setId(u.getId());
					c.setGroupRelativePaths(new HashSet<String>());
					plist.add(c);
					userMap.put(u.getId(), c);
				}
				for(Group g : secGroups) {
					for(UserGroup ug : g.getUsers()) {
						CourseUserPermissions c = userMap.get(ug.getUser().getId());
						c.getGroupRelativePaths().add(g.getRelativePath());
					}
				}

				if(task.equals("list")) {
					bean.setUserPermissions(plist);
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(task.equals("add")) {
					CourseUserPermissions cp = bean.getNewUser(); 
					if(cp.getId()==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noUserSelected"));
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					}
					if(!updateUsersCoursePermissions(data, em, dh, data.getCourseInstance(), secGroups, userMap, cp)) return null;
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				if(!task.equals("update")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				for(CourseUserPermissions cp : bean.getUserPermissions()) {
					if(!updateUsersCoursePermissions(data, em, dh, data.getCourseInstance(), secGroups, userMap, cp)) return null;
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}

			private boolean updateUsersCoursePermissions(
					EditCoursePermissionsData data, EntityManager em,
					DAOHelper dh, CourseInstance courseInstance, List<Group> secGroups,
					Map<Long, CourseUserPermissions> userMap,
					CourseUserPermissions cp) {
				User user = cp!=null && cp.getId()!= null ? dh.getUserDAO().getUserById(em, cp.getId()) : null;
				if(user==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.unknownUserEncountered"));
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return false;
				}
				CourseUserPermissions actual = userMap.get(user.getId());
				Set<String> actualPerms =  actual == null ? new HashSet<String>() : actual.getGroupRelativePaths();
				for(Group g : secGroups) {
					if(g.getRelativePath().equals(JCMSSecurityConstants.SEC_ROLE_GROUP)) continue;
					String relpath = g.getRelativePath();
					if(cp.getGroupRelativePaths().contains(relpath)) {
						if(!actualPerms.contains(relpath)) {
							// Dodaj ga u tu grupu!
							if(!JCMSSecurityManagerFactory.getManager().canModifyCoursePermission(courseInstance, relpath)) {
								data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
								data.setResult(AbstractActionData.RESULT_SUCCESS);
								return false;
							}
							UserGroup ug = new UserGroup();
							ug.setGroup(g);
							ug.setUser(user);
							dh.getUserGroupDAO().save(em, ug);
							g.getUsers().add(ug);
						}
					} else {
						if(actualPerms.contains(relpath)) {
							// Obrisi ga iz te grupe!
							Iterator<UserGroup> it = g.getUsers().iterator();
							while(it.hasNext()) {
								UserGroup ug = it.next();
								if(ug.getUser().equals(user)) {
									it.remove();
									dh.getUserGroupDAO().remove(em, ug);
									break;
								}
							}
						}
					}
				}
				return true;
			}
		});
	}

	protected static Map<String, Group> mapAndCreateSecurityGroups(EntityManager em, CourseInstance courseInstance, List<Group> secGroups) {
		Group parent = null;
		for(Group g : secGroups) {
			if(g.getRelativePath().equals(JCMSSecurityConstants.SEC_ROLE_GROUP)) {
				parent = g;
				break;
			}
		}
		if(parent == null) {
			parent = new Group();
			parent.setCompositeCourseID(courseInstance.getId());
			parent.setEnteringAllowed(false);
			parent.setLeavingAllowed(false);
			parent.setManagedRoot(false);
			parent.setName(JCMSSecurityConstants.SEC_ROLE_GROUP_NAME);
			parent.setParent(courseInstance.getPrimaryGroup());
			parent.setRelativePath(JCMSSecurityConstants.SEC_ROLE_GROUP);
			DAOHelperFactory.getDAOHelper().getGroupDAO().save(em, parent);
			courseInstance.getPrimaryGroup().getSubgroups().add(parent);
			secGroups.add(parent);
		}
		Map<String,Group> map = new HashMap<String, Group>();
		map.put(parent.getRelativePath(), parent);

		for(Group g : secGroups) {
			map.put(g.getRelativePath(), g);
		}
		for(int i = 0; i < JCMSSecurityConstants.getSecurityCourseRolesCount(); i++) {
			String relPath = JCMSSecurityConstants.getSecurityCourseRole(i);
			if(map.containsKey(relPath)) continue;
			Group g = new Group();
			g.setCompositeCourseID(courseInstance.getId());
			g.setEnteringAllowed(false);
			g.setLeavingAllowed(false);
			g.setManagedRoot(false);
			g.setName(JCMSSecurityConstants.getSecurityCourseRoleName(i));
			g.setParent(parent);
			g.setRelativePath(relPath);
			DAOHelperFactory.getDAOHelper().getGroupDAO().save(em, g);
			parent.getSubgroups().add(g);
			map.put(g.getRelativePath(), g);
			secGroups.add(g);
		}
		return map;
	}

	@Deprecated
	public static void getMPGroupsAdminData(final MPGroupsAdminData data,
			final Long userID, final String courseInstanceID, final Long parentID,
			final MarketPlaceBean bean, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				Group parent = parentID!=null ? dh.getGroupDAO().get(em, parentID) : null;
				if(parent==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setParent(parent);
				if(!parent.isManagedRoot()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.nonManagedRoot"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageCourseMarketPlace(data.getCourseInstance(), parent.getRelativePath());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				MarketPlace mp = parent.getMarketPlace(); 
				if(mp==null) {
					mp = new MarketPlace();
					mp.setFormulaConstraints(null);
					mp.setOpen(false);
					mp.setTimeBuffer(-1);
					dh.getMarketPlaceDAO().save(em, mp);
					mp.setGroup(parent);
					parent.setMarketPlace(mp);
				}

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date now = new Date();
				
				List<Group> groupList = new ArrayList<Group>(parent.getSubgroups());
				List<GroupBean> newGroups = new ArrayList<GroupBean>(groupList.size());
				Collections.sort(groupList, StringUtil.GROUP_COMPARATOR);
				for(Group g : groupList) {
					GroupBean gb = new GroupBean();
					gb.setCapacity(g.getCapacity());
					gb.setCompositeCourseID(g.getCompositeCourseID());
					gb.setEnteringAllowed(g.isEnteringAllowed());
					gb.setId(g.getId());
					gb.setLeavingAllowed(g.isLeavingAllowed());
					gb.setManagedRoot(g.isManagedRoot());
					gb.setMpSecurityTag(g.getMpSecurityTag());
					gb.setName(g.getName());
					gb.setRelativePath(g.getRelativePath());
					newGroups.add(gb);
				}
				if(task.equals("input")) {
					bean.setGroups(newGroups);
					bean.setFormulaConstraints(mp.getFormulaConstraints());
					bean.setId(mp.getId());
					bean.setOpen(mp.getOpen());
					bean.setOpenFrom(mp.getOpenFrom()==null ? null : sdf.format(mp.getOpenFrom()));
					bean.setOpenUntil(mp.getOpenUntil()==null ? null : sdf.format(mp.getOpenUntil()));
					bean.setSecurityConstraints(mp.getSecurityConstraints());
					bean.setTimeBuffer(mp.getTimeBuffer());
					data.setResult(AbstractActionData.RESULT_INPUT);
					data.setActive(mp.isActive(now));
					return null;
				}
				if(!task.equals("update")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(bean.getTimeBuffer()<0 && bean.getTimeBuffer()!=-1) {
					bean.setTimeBuffer(-1);
				}
				boolean errors = false;
				Date openFrom = null;
				if(!StringUtil.isStringBlank(bean.getOpenFrom())) {
					try {
						openFrom = sdf.parse(bean.getOpenFrom());
					} catch (ParseException e) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongDateFormat"));
						errors = true;
					}
				}
				Date openUntil = null;
				if(!StringUtil.isStringBlank(bean.getOpenUntil())) {
					try {
						openUntil = sdf.parse(bean.getOpenUntil());
					} catch (ParseException e) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongDateFormat"));
						errors = true;
					}
				}
				if(!StringUtil.isStringBlank(bean.getFormulaConstraints())) {
					try {
						new MPFormulaConstraints(bean.getFormulaConstraints());
					} catch (ParseException e) {
						data.getMessageLogger().addErrorMessage("Greska u tumacenju formule: "+e.getMessage());
						errors = true;
					}
				}
				if(!StringUtil.isStringBlank(bean.getSecurityConstraints())) {
					try {
						new MPSecurityConstraints(bean.getSecurityConstraints());
					} catch (ParseException e) {
						data.getMessageLogger().addErrorMessage("Greska u tumacenju ogranicenja: "+e.getMessage());
						errors = true;
					}
				}
				Map<Long, Group> mapByID = GroupUtil.mapGroupByID(groupList);
				if(!errors && bean.getGroups()!=null) {
					for(GroupBean gb : bean.getGroups()) {
						if(gb.getId()==null || !mapByID.containsKey(gb.getId())) {
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
							data.setResult(AbstractActionData.RESULT_FATAL);
							return null;
						}
						try {
							MPSecurityConstraints.checkSecurityTagFormat(gb.getMpSecurityTag());
						} catch(ParseException ex) {
							data.getMessageLogger().addErrorMessage(ex.getMessage());
							data.setResult(AbstractActionData.RESULT_INPUT);
							return null;
						}
					}
					for(GroupBean gb : bean.getGroups()) {
						Group g = mapByID.get(gb.getId());
						g.setEnteringAllowed(gb.isEnteringAllowed());
						g.setLeavingAllowed(gb.isLeavingAllowed());
						if(gb.getCapacity()<0 && gb.getCapacity()!=-1) {
							g.setCapacity(-1);
						} else {
							g.setCapacity(gb.getCapacity());
						}
						g.setMpSecurityTag(gb.getMpSecurityTag());
					}
				}
				if(errors) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				mp.setFormulaConstraints(bean.getFormulaConstraints());
				mp.setOpen(bean.isOpen());
				mp.setOpenFrom(openFrom);
				mp.setOpenUntil(openUntil);
				mp.setSecurityConstraints(bean.getSecurityConstraints());
				mp.setTimeBuffer(bean.getTimeBuffer());
				data.setActive(mp.isActive(now));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getMPGroupsListAdminData(final MPGroupsListData data, final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				List<MPRootInfoBean> list = JCMSSecurityManagerFactory.getManager().getMarketPlacesForUser(data.getCourseInstance());
				data.setMpRoots(list);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	private static class MPWorkContext {
		Date now;
		MarketPlace marketPlace;
		boolean marketPlaceActive;
		MPSecurityConstraints scons;
		MPFormulaConstraints fcons;
		MPFormulaContext context;
		List<Group> allGroups;
		Map<Long, Group> groupsByIDMap;
		List<UserGroup> userGroups;
		Map<Group,UserGroup> myUserGroupMap;
		Set<Group> myGroups;
		Date newLimit;
		
		public MPWorkContext(EntityManager em, CourseInstance courseInstance, Group parent, User user) {
			now = new Date();
			userGroups = DAOHelperFactory.getDAOHelper().getGroupDAO().findUserGroupsForUser(em, courseInstance.getId(), parent.getRelativePath(), user);
			Collections.sort(userGroups, StringUtil.USER_GROUP_COMPARATOR2);
			myUserGroupMap = new HashMap<Group, UserGroup>();
			myGroups = new HashSet<Group>();
			for(UserGroup ug : userGroups) {
				myUserGroupMap.put(ug.getGroup(),ug);
				myGroups.add(ug.getGroup());
			}
			marketPlace = parent.getMarketPlace();
			marketPlaceActive = checkMarketPlaceActive(em, marketPlace, now);
			try {
				if(marketPlaceActive) scons = new MPSecurityConstraints(marketPlace.getSecurityConstraints());
			} catch (ParseException e) {
				marketPlaceActive = false;
			}
			try {
				if(marketPlaceActive) fcons = new MPFormulaConstraints(marketPlace.getFormulaConstraints());
			} catch (ParseException e) {
				marketPlaceActive = false;
			}
			if(marketPlaceActive) {
				allGroups = DAOHelperFactory.getDAOHelper().getGroupDAO().findSubgroups(em, courseInstance.getId(), parent.getRelativePath()+"/%");
				Collections.sort(allGroups, StringUtil.GROUP_COMPARATOR);
				groupsByIDMap = GroupUtil.mapGroupByID(allGroups);
				if(fcons!=null && fcons.getNumberOfConstraints()!=0) {
					List<Object[]> res = DAOHelperFactory.getDAOHelper().getGroupDAO().getGroupStat(em, parent.getCompositeCourseID(), parent.getRelativePath());
					context = new SimpleMPFormulaContext(res,groupsByIDMap);
				} else {
					List<Object[]> res = DAOHelperFactory.getDAOHelper().getGroupDAO().getCoarseGroupStat(em, parent.getCompositeCourseID(), parent.getRelativePath());
					context = new BlankMPFormulaContext(res,groupsByIDMap);
				}
				if(marketPlace.getTimeBuffer()!=-1) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(now);
					cal.add(Calendar.SECOND, marketPlace.getTimeBuffer());
					newLimit = cal.getTime();
				}
			}
		}

		private boolean precalculatedCheckMoveDeny;
		private String initialUserGroupName;
		private String initialStudentTag;
		
		public void prepareCheckMove(UserGroup ug) {
			initialUserGroupName = ug.getGroup().getName();
			initialStudentTag = ug.getTag();
			context.getExchangeDescriptor().setFromGroup(ug.getGroup().getName());
			context.getExchangeDescriptor().setFromGroupTag(ug.getGroup().getMpSecurityTag());
			context.getExchangeDescriptor().setFromStudentTag(ug.getTag());
			context.getExchangeDescriptor().setToStudentTag(ug.getTag());
			context.getExchangeDescriptor().setToGroup(null);
			context.getExchangeDescriptor().setToGroupTag(null);
			// Ako se iz grupe ne moze van, ili se zbog vremenskog ogranicenja vise ne moze van, to zabiljezi:
			if(!ug.getGroup().isLeavingAllowed() || checkGroupTimeConstraintViolated(ug.getGroup(),newLimit)) {
				precalculatedCheckMoveDeny = true; 
			} else {
				precalculatedCheckMoveDeny = false; 
			}
		}

		public boolean canMoveStudentToGroup(UserGroup ug, Group g) {
			// ako vec unaprijed znamo da ne mozemo van:
			if(precalculatedCheckMoveDeny) return false;
			// nije dopušten ulazak u grupu...
			if(!g.isEnteringAllowed()) return false;
			// ako je definirano ograničenje kapaciteta, onda ne:
			if(g.getCapacity()!=-1 && context.getTotalSizeForGroup(g.getName())>=g.getCapacity()) return false;
			// ako je to grupa nerasporedenih, onda ne
			if(StringUtil.isStringBlank(g.getName())) return false;
			// ako sam vec tamo, onda ne
			if(myGroups.contains(g)) return false;
			// ako to zbog sigurnosnih pravila nije moguće
			if(scons!=null && !scons.canMove(ug.getTag(), ug.getGroup().getMpSecurityTag(), g.getMpSecurityTag())) return false;
			// ako je ciljna grupa u konfliktu s vremenskim ograničenjem...
			if(checkGroupTimeConstraintViolated(g,newLimit)) return false;
			context.getExchangeDescriptor().setToGroup(g.getName());
			context.getExchangeDescriptor().setToGroupTag(g.getMpSecurityTag());
			if(fcons!=null && fcons.getNumberOfConstraints()>0) {
				// Ovdje sada znam otkud selim i kamo selim. Izracunajmo neslaganje prije:
				fcons.canMoveStudent(context);
				int vmBefore = context.getViolationMeasure();
				// OK, ovdje umanji za jedan aktualnog studenta:
				context.decrease(initialUserGroupName, initialStudentTag);
				context.increase(g.getName(), ug.getTag());
				boolean canMove = fcons.canMoveStudent(context);
				int vmAfter = context.getViolationMeasure();
				context.increase(initialUserGroupName, initialStudentTag);
				context.decrease(g.getName(), ug.getTag());
				if(!canMove) {
					// Ako se ovim preseljenjem popravlja stanje
					// (recimo, iz prenatrpane grupe jedan student želi izaci)
					// to pak dopustimo...
					if(vmAfter < vmBefore) return true;
					return false;
				}
			}
			// Inace se cini da mogu!
			return true;
		}

		public boolean checkGroupTimeConstraintViolated(Group group, Date timeConstraint) {
			if(timeConstraint==null) return false;
			Set<GroupWideEvent> gwes = group.getEvents();
			for(GroupWideEvent gwe : gwes) {
				if(gwe.getStart().before(timeConstraint)) {
					return true;
				}
			}
			return false;
		}

		public boolean checkGroupTimeConstraintViolated(Group group) {
			return checkGroupTimeConstraintViolated(group, newLimit);
		}

		public void calcSendDestinations(MPUserGroupState s) {
			// U svim grupama blokiraj moje vlastite grupe
			s.getBlockedGroups().addAll(myGroups);
			s.getDirectBlockedGroups().addAll(myGroups);
			// Ako ja iz ove grupe ne mogu van, ili sam u grupi "nerasporedeni", tada su zamjene zabranjene
			if(StringUtil.isStringBlank(s.getMyUserGroup().getGroup().getName()) || !s.getMyUserGroup().getGroup().isLeavingAllowed()) {
				s.getBlockedGroups().addAll(allGroups);
				s.getDirectBlockedGroups().addAll(allGroups);
			} else {
				for(Group g : allGroups) {
					if(scons!=null && !scons.canMove(myUserGroupMap.get(s.getMyUserGroup().getGroup()).getTag(), s.getMyUserGroup().getGroup().getMpSecurityTag(), g.getMpSecurityTag()) || checkGroupTimeConstraintViolated(g)) {
						s.getBlockedGroups().add(g);
						s.getDirectBlockedGroups().add(g);
					} else if(StringUtil.isStringBlank(g.getName())) {
						s.getBlockedGroups().add(g);
						s.getDirectBlockedGroups().add(g);
					}
				}
			}
			s.getAvailForDirectOffers().addAll(allGroups);
			s.getAvailForDirectOffers().removeAll(s.getDirectBlockedGroups());
			s.getAvailForGroupOffers().addAll(allGroups);
			s.getAvailForGroupOffers().removeAll(s.getBlockedGroups());
			s.setActive(true);
			if(!s.getMyUserGroup().getGroup().isLeavingAllowed()) {
				s.setActive(false);
			} else if(newLimit!=null) {
				boolean violated = checkGroupTimeConstraintViolated(s.getMyUserGroup().getGroup(), newLimit);
				if(violated) s.setActive(false);
			}
		}
		
		public boolean canExchange(Group fromGroup, String fromGroupTag, String fromStudentTag, Group toGroup, String toGroupTag, String toStudentTag) {
			if(StringUtil.isStringBlank(fromGroup.getName()) || !fromGroup.isEnteringAllowed() || !fromGroup.isLeavingAllowed()) return false;
			if(StringUtil.isStringBlank(toGroup.getName()) || !toGroup.isEnteringAllowed() || !toGroup.isLeavingAllowed()) return false;
			if(newLimit!=null) {
				if(checkGroupTimeConstraintViolated(fromGroup, newLimit)) return false;
				if(checkGroupTimeConstraintViolated(toGroup, newLimit)) return false;
			}
			if(scons!=null && !scons.canExchange(fromGroupTag, fromStudentTag, toGroupTag, toStudentTag)) return false;
			return true;
		}
	}
	
	public static void getMPViewData(final MPViewData data, final Long userID,
			final String courseInstanceID, final Long parentID, final MPViewBean bean, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID)) return null;
				Group parent = parentID!=null ? dh.getGroupDAO().get(em, parentID) : null;
				if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setParent(parent);
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
				if(mpwc.userGroups.size()==0) {
					// Ups! Tog korisnika tamo nema!!! Je li on uopce na predavanjima?
					List<Group> lectureGroups = dh.getGroupDAO().findSubGroupsForUser(em, data.getCourseInstance().getId(), "0", data.getCurrentUser());
					if(lectureGroups.isEmpty()) {
						// Ovaj korisnik uopce nije na kolegiju! Van!
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					} else {
						// Korisnik je na kolegiju, ali ne sudjeluje u ovoj burzi...
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.notInMarketPlace"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				data.setAllGroups(mpwc.allGroups);
				data.setUserGroups(mpwc.userGroups);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				data.setNow(sdf.format(mpwc.now));
				if(!mpwc.marketPlaceActive) {
					data.setActive(false);
					MPUserState state = new MPUserState();
					for(UserGroup ug : mpwc.userGroups) {
						state.getOrCreateForGroup(ug);
					}
					data.setUserState(state);
				} else {
					List<MPOffer> offers = dh.getMarketPlaceDAO().listOffersRegardingUser(em, parent.getMarketPlace(), data.getCurrentUser());
					MPUserState state = MPUserState.buildFrom(offers, data.getCurrentUser(), mpwc.now, mpwc.scons, mpwc.myUserGroupMap);
					for(UserGroup ug : mpwc.userGroups) {
						MPUserGroupState s = state.getOrCreateForGroup(ug);
						// Mogu li se samo premjestiti u drugu grupu?
						mpwc.prepareCheckMove(ug);
						for(Group g : mpwc.allGroups) {
							if(mpwc.canMoveStudentToGroup(ug, g)) {
								s.getAvailForMove().add(g);
							}
						}
						mpwc.calcSendDestinations(s);
					}
					data.setUserState(state);
					data.setActive(mpwc.marketPlaceActive);
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	@Deprecated
	private static class SimpleMPFormulaContext implements MPFormulaContext {
		int violationMeasure;

		Map<String,int[]> totals = new HashMap<String, int[]>();
		Map<String,int[]> details = new HashMap<String,int[]>(64);
		boolean formulaApplies;
		ExchangeDescriptor exchangeDescriptor = new ExchangeDescriptor();

		public SimpleMPFormulaContext(List<Object[]> res, Map<Long, Group> groupsByIDMap) {
			for(Object[] o : res) {
				Long id = (Long)o[0];
				String tag = (String)o[1];
				int broj = ((Number)o[2]).intValue();
				String groupName = groupsByIDMap.get(id).getName();
				String detKey = groupName+"'''"+(tag==null ? "" : tag);
				details.put(detKey, new int[] {broj});
				int[] data = totals.get(groupName);
				if(data==null) {
					data = new int[] {broj};
					totals.put(groupName, data);
				} else {
					data[0] += broj;
				}
			}
		}

		@Override
		public void clearFormulaAppliesFlag() {
			formulaApplies = false;
		}

		@Override
		public ExchangeDescriptor getExchangeDescriptor() {
			return exchangeDescriptor;
		}

		@Override
		public boolean getFormulaAppliesFlag() {
			return formulaApplies;
		}

		@Override
		public int getNumberOfStudentsWithTag(String groupName, String tagName) {
			String detKey = groupName+"'''"+(tagName==null ? "" : tagName);
			int[] res = details.get(detKey);
			if(res==null) return 0;
			return res[0];
		}

		@Override
		public int getTotalSizeForGroup(String groupName) {
			int[] data = totals.get(groupName);
			if(data==null) return 0;
			return data[0];
		}

		@Override
		public void setFormulaAppliesFlag() {
			formulaApplies = true;
		}
		
		@Override
		public void decrease(String groupName, String studentTag) {
			String detKey = groupName+"'''"+(studentTag==null ? "" : studentTag);
			int[] res = details.get(detKey);
			if(res==null) {
				res = new int[] {0};
				details.put(detKey, res);
			}
			res[0]--;
			res = totals.get(groupName);
			if(res==null) {
				res = new int[] {0};
				totals.put(groupName, res);
			}
			res[0]--;
		}

		@Override
		public void increase(String groupName, String studentTag) {
			String detKey = groupName+"'''"+(studentTag==null ? "" : studentTag);
			int[] res = details.get(detKey);
			if(res==null) {
				res = new int[] {0};
				details.put(detKey, res);
			}
			res[0]++;
			res = totals.get(groupName);
			if(res==null) {
				res = new int[] {0};
				totals.put(groupName, res);
			}
			res[0]++;
		}
		@Override
		public void addViolationMeasure(int measure) {
			violationMeasure += measure;
		}
		@Override
		public int getViolationMeasure() {
			return violationMeasure;
		}
		@Override
		public void resetViolationMeasure() {
			violationMeasure = 0;
		}
	}

	@Deprecated
	private static class BlankMPFormulaContext implements MPFormulaContext {
		int violationMeasure;
		boolean formulaApplies;
		ExchangeDescriptor exchangeDescriptor = new ExchangeDescriptor();
		Map<String,Integer> totals = new HashMap<String, Integer>();
		public BlankMPFormulaContext(List<Object[]> res, Map<Long, Group> groupsByIDMap) {
			for(Object[] o : res) {
				totals.put(groupsByIDMap.get((Long)o[0]).getName(), Integer.valueOf(((Number)o[1]).intValue()));
			}
		}

		@Override
		public void clearFormulaAppliesFlag() {
			formulaApplies = false;
		}

		@Override
		public ExchangeDescriptor getExchangeDescriptor() {
			return exchangeDescriptor;
		}

		@Override
		public boolean getFormulaAppliesFlag() {
			return formulaApplies;
		}

		@Override
		public int getNumberOfStudentsWithTag(String groupName, String tagName) {
			return 0;
		}

		@Override
		public int getTotalSizeForGroup(String groupName) {
			if(totals==null) return 0;
			Integer i = totals.get(groupName);
			if(i==null) return 0;
			return i.intValue();
		}

		@Override
		public void setFormulaAppliesFlag() {
			formulaApplies = true;
		}
		
		@Override
		public void decrease(String groupName, String studentTag) {
		}
		
		@Override
		public void increase(String groupName, String studentTag) {
		}
		@Override
		public void addViolationMeasure(int measure) {
			violationMeasure += measure;
		}
		@Override
		public int getViolationMeasure() {
			return violationMeasure;
		}
		@Override
		public void resetViolationMeasure() {
			violationMeasure = 0;
		}
	}

	@Deprecated
	public static void getMPDirectMoveData(MPDirectMoveData data, Long userID, MPOfferBean bean, String task) {
		Long pid = bean.getParentID();
		try {
			MPLockFactory.get().writeLock(pid);
			getMPDirectMoveDataImpl(data, userID, bean, task);
		} finally {
			MPLockFactory.get().releaseLock(pid);
		}
	}
	
	@Deprecated
	private static void getMPDirectMoveDataImpl(final MPDirectMoveData data, final Long userID, final MPOfferBean bean, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
				Group parent = bean.getParentID()!=null ? dh.getGroupDAO().get(em, bean.getParentID()) : null;
				if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setParent(parent);
				data.setMarketPlace(parent.getMarketPlace());
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				Group otherGroup = bean.getGroupID()!=null ? dh.getGroupDAO().get(em, bean.getGroupID()) : null;
				UserGroup myUserGroup = dh.getUserGroupDAO().get(em, bean.getMyUserGroupID());
				if(myUserGroup==null || !myUserGroup.getUser().equals(data.getCurrentUser())
						|| !myUserGroup.getGroup().getParent().equals(parent) || otherGroup==null 
						|| !otherGroup.getParent().equals(parent)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group myGroup = myUserGroup.getGroup();
				if(!myUserGroup.getGroup().getId().equals(bean.getMyGroupID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
				if(!mpwc.marketPlaceActive) {
					data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				} else {
					mpwc.prepareCheckMove(myUserGroup);
					if(!mpwc.canMoveStudentToGroup(myUserGroup, otherGroup)) {
						// javi da nije moguce premjestiti
						data.getMessageLogger().addErrorMessage("Premještanje nije moguće obaviti.");
					} else {
						// premjesti ga...
						myUserGroup.getGroup().getUsers().remove(myUserGroup);
						myUserGroup.setGroup(otherGroup);
						otherGroup.getUsers().add(myUserGroup);
						em.flush();
						dh.getMarketPlaceDAO().clearAllOffersForUser(em, mpwc.marketPlace, myUserGroup.getUser(), myGroup);
						data.setMovedFromGroup(myGroup);
						data.setMovedToGroup(otherGroup);
						data.setMovedUser(myUserGroup.getUser());
						data.getMessageLogger().addInfoMessage("Premještanje je uspješno obavljeno.");
						JCMSLogger.getLogger().mpLogMove(mpwc.marketPlace, myUserGroup.getUser(), myGroup, otherGroup, (User)null);
					}
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	protected static boolean checkMarketPlaceActive(EntityManager em, MarketPlace marketPlace, Date now) {
		if(marketPlace == null) return false;
		String burzaAktivna = getKeyValue(em, "marketPlace");
		if(!"yes".equals(burzaAktivna)) return false;
		return marketPlace.isActive(now);
	}

	@Deprecated
	public static void getMPSendGroupOfferData(MPSendGroupOfferData data, Long userID, MPOfferBean bean) {
		Long pid = bean.getParentID();
		try {
			MPLockFactory.get().writeLock(pid);
			getMPSendGroupOfferDataImpl(data, userID, bean);
		} finally {
			MPLockFactory.get().releaseLock(pid);
		}
	}
	
	@Deprecated
	private static void getMPSendGroupOfferDataImpl(final MPSendGroupOfferData data, final Long userID, final MPOfferBean bean) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
				Group parent = bean.getParentID()!=null ? dh.getGroupDAO().get(em, bean.getParentID()) : null;
				if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setParent(parent);
				Date validUntil = null;
				if(!StringUtil.isStringBlank(bean.getValidUntil())) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						validUntil = sdf.parse(bean.getValidUntil());
					} catch(ParseException ex) {
						data.getMessageLogger().addErrorMessage("Datum je pogrešnog formata.");
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
				}
				if(validUntil!=null && new Date().after(validUntil)) {
					data.getMessageLogger().addErrorMessage("Ne može poslati ponudu koja je već u startu istekla.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				Group otherGroup = bean.getGroupID()!=null ? dh.getGroupDAO().get(em, bean.getGroupID()) : null;
				UserGroup myUserGroup = dh.getUserGroupDAO().get(em, bean.getMyUserGroupID());
				if(myUserGroup==null  || !myUserGroup.getUser().equals(data.getCurrentUser())
						|| !myUserGroup.getGroup().getParent().equals(parent) || otherGroup==null 
						|| !otherGroup.getParent().equals(parent)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group myGroup = myUserGroup.getGroup();
				if(!myUserGroup.getGroup().getId().equals(bean.getMyGroupID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
				if(!mpwc.marketPlaceActive) {
					data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				} else {

					List<MPOffer> offers = dh.getMarketPlaceDAO().listOffersRegardingUser(em, parent.getMarketPlace(), data.getCurrentUser(), myGroup);
					MPUserState state = MPUserState.buildFrom(offers, data.getCurrentUser(), mpwc.now, mpwc.scons, mpwc.myUserGroupMap);
					MPUserGroupState s = state.getOrCreateForGroup(myUserGroup);
					mpwc.calcSendDestinations(s);
					// Kada mu smijem poslati grupnu ponudu? Ako je "s" aktivan i ako odredisna grupa nije blokirana:
					if(s.isActive() && !s.getBlockedGroups().contains(otherGroup)) {
						// Sada mogu poslati ponudu...
						MPOffer offer = new MPOffer();
						offer.setFromGroup(myGroup);
						offer.setFromUser(myUserGroup.getUser());
						offer.setFromTag(myUserGroup.getTag());
						offer.setMarketPlace(parent.getMarketPlace());
						offer.setNeedsAck(bean.isRequireApr());
						offer.setToGroup(otherGroup);
						offer.setToUser(null);
						offer.setValidUntil(validUntil);
						if(!StringUtil.isStringBlank(bean.getReason())) {
							if(bean.getReason().length()>100) {
								offer.setReason(bean.getReason().substring(0, 100));
							} else {
								offer.setReason(bean.getReason());
							}
						}
						dh.getMarketPlaceDAO().save(em, offer);
						data.getMessageLogger().addInfoMessage("Ponuda je poslana.");
					} else {
						// Ne mogu poslati ponudu...
						data.getMessageLogger().addErrorMessage("Ponudu nije moguće poslati.");
					}
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getMPDeleteOfferData(final MPDeleteOfferData data, final Long userID, final MPOfferBean bean) {
		Long pid = bean.getParentID();
		try {
			MPLockFactory.get().writeLock(pid);
			getMPDeleteOfferDataImpl(data, userID, bean);
		} finally {
			MPLockFactory.get().releaseLock(pid);
		}
	}
	
	@Deprecated
	private static void getMPDeleteOfferDataImpl(final MPDeleteOfferData data, final Long userID, final MPOfferBean bean) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
				Group parent = bean.getParentID()!=null ? dh.getGroupDAO().get(em, bean.getParentID()) : null;
				if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setParent(parent);
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				MPOffer offer = bean.getOfferID()==null ? null : dh.getMarketPlaceDAO().getMPOffer(em, bean.getOfferID());
				UserGroup myUserGroup = dh.getUserGroupDAO().get(em, bean.getMyUserGroupID());
				if(myUserGroup==null || offer == null || !offer.getFromUser().equals(data.getCurrentUser()) || !offer.getFromGroup().equals(myUserGroup.getGroup())
						|| !myUserGroup.getGroup().getParent().equals(parent) || !myUserGroup.getUser().equals(data.getCurrentUser())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!myUserGroup.getGroup().getId().equals(bean.getMyGroupID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
				if(!mpwc.marketPlaceActive) {
					data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				} else {
					dh.getMarketPlaceDAO().deleteReplysTo(em, parent.getMarketPlace(), offer);
					dh.getMarketPlaceDAO().remove(em, offer);
					data.getMessageLogger().addInfoMessage("Ponuda je uspješno obrisana.");
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getMPAcceptOfferData(MPAcceptOfferData data,
			Long userID, MPOfferBean bean, String task) {
		Long pid = bean.getParentID();
		try {
			MPLockFactory.get().writeLock(pid);
			getMPAcceptOfferDataImpl(data, userID, bean, task);
		} finally {
			MPLockFactory.get().releaseLock(pid);
		}
	}

	@Deprecated
	public static void getMPAcceptOfferDataImpl(final MPAcceptOfferData data,
			final Long userID, final MPOfferBean bean, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
				Group parent = bean.getParentID()!=null ? dh.getGroupDAO().get(em, bean.getParentID()) : null;
				if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setParent(parent);
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				MPOffer offer = bean.getOfferID()==null ? null : dh.getMarketPlaceDAO().getMPOffer(em, bean.getOfferID());
				UserGroup myUserGroup = dh.getUserGroupDAO().get(em, bean.getMyUserGroupID());
				if(myUserGroup==null || offer == null || !myUserGroup.getUser().equals(data.getCurrentUser()) || !offer.getToGroup().equals(myUserGroup.getGroup())
						|| !myUserGroup.getGroup().getParent().equals(parent)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!myUserGroup.getGroup().getId().equals(bean.getMyGroupID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				// Pronadi UserGroup objekt za drugog korisnika
				UserGroup otherUserGroup = dh.getUserGroupDAO().find(em, offer.getFromUser(), offer.getFromGroup());
				if(otherUserGroup==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Date now = new Date();
				if(MPUserState.isOfferExpired(offer.getValidUntil(), now)) {
					data.getMessageLogger().addErrorMessage("Ponuda je istekla.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				if(offer.getReplyTo()!=null && MPUserState.isOfferExpired(offer.getReplyTo().getValidUntil(), now)) {
					data.getMessageLogger().addErrorMessage("Originalna ponuda je istekla.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
				if(!mpwc.marketPlaceActive) {
					data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				} else {
					if(!mpwc.canExchange(offer.getFromGroup(), offer.getFromGroup().getMpSecurityTag(), offer.getFromTag(), myUserGroup.getGroup(), myUserGroup.getGroup().getMpSecurityTag(), myUserGroup.getTag())) {
						data.getMessageLogger().addErrorMessage("Zatraženu akciju nije moguće izvesti zbog ograničenja koja je administrator postavio na burzu.");
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					if(task.equals("acceptGroupOffer") || task.equals("acceptApproval") || task.equals("acceptDirectOffer")) {
						if(offer.getToUser()!=null && !offer.getToUser().equals(myUserGroup.getUser())) {
							// Hm... Netko nesto muti...
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
							data.setResult(AbstractActionData.RESULT_FATAL);
							return null;
						}
						// obavi zamjenu
						Group myGroup = myUserGroup.getGroup();
						Group otherGroup = otherUserGroup.getGroup();
						dh.getMarketPlaceDAO().clearAllOffersForUsers(em, parent.getMarketPlace(), offer.getFromUser(), otherGroup, myUserGroup.getUser(), myGroup);
						myGroup.getUsers().remove(myUserGroup);
						otherGroup.getUsers().remove(otherUserGroup);
						myGroup.getUsers().add(otherUserGroup);
						otherGroup.getUsers().add(myUserGroup);
						myUserGroup.setGroup(otherGroup);
						otherUserGroup.setGroup(myGroup);
						data.getMessageLogger().addInfoMessage("Zamjena je uspješno obavljena.");
						JCMSLogger.getLogger().mpLogSwitch(offer.getMarketPlace(), myUserGroup.getUser(), myGroup, otherUserGroup.getUser(), otherGroup, offer.getReplyTo()!=null ? "APPROVAL" : (offer.getToUser()!=null ? "DIRECT" : "GROUP"));
					} else if(task.equals("sendApprovalRequest")) {
						if(offer.getToUser()!=null || offer.getReplyTo()!=null || !offer.getNeedsAck()) {
							// Hm... Netko nesto muti...
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
							data.setResult(AbstractActionData.RESULT_FATAL);
							return null;
						}
						List<MPOffer> offers = dh.getMarketPlaceDAO().listOffersRegardingUser(em, parent.getMarketPlace(), data.getCurrentUser(), myUserGroup.getGroup());
						MPUserState state = MPUserState.buildFrom(offers, data.getCurrentUser(), mpwc.now, mpwc.scons, mpwc.myUserGroupMap);
						MPUserGroupState s = state.getOrCreateForGroup(myUserGroup);
						mpwc.calcSendDestinations(s);
						if(!s.isActive()) {
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
							data.setResult(AbstractActionData.RESULT_FATAL);
							return null;
						}
						boolean alreadySent = false;
						for(MPOffer mpoffer : s.getMyAckReqForGroupOffers()) {
							if(mpoffer.getReplyTo()!=null && mpoffer.getReplyTo().equals(offer)) {
								alreadySent = true;
								break;
							}
						}
						if(alreadySent) {
							data.getMessageLogger().addErrorMessage("Zahtjev za zamjenom već ste poslali tom korisniku.");
							data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
							return null;
						}
						MPOffer o = new MPOffer();
						o.setFromGroup(myUserGroup.getGroup());
						o.setFromTag(myUserGroup.getTag());
						o.setFromUser(myUserGroup.getUser());
						o.setMarketPlace(parent.getMarketPlace());
						o.setNeedsAck(false);
						o.setReplyTo(offer);
						o.setToGroup(offer.getFromGroup());
						o.setToUser(offer.getFromUser());
						dh.getMarketPlaceDAO().save(em, o);
						data.getMessageLogger().addInfoMessage("Zahtjev za zamjenom poslan je korisniku.");
					} else {
						data.getMessageLogger().addErrorMessage("Interna pogreška.");
					}
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	@Deprecated
	public static void getMPSendDirectOfferData(MPSendDirectOfferData data, Long userID, MPOfferBean bean) {
		Long pid = bean.getParentID();
		try {
			MPLockFactory.get().writeLock(pid);
			getMPSendDirectOfferDataImpl(data, userID, bean);
		} finally {
			MPLockFactory.get().releaseLock(pid);
		}
	}
	
	@Deprecated
	public static void getMPSendDirectOfferDataImpl(final MPSendDirectOfferData data, final Long userID, final MPOfferBean bean) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
				Group parent = bean.getParentID()!=null ? dh.getGroupDAO().get(em, bean.getParentID()) : null;
				if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot() || StringUtil.isStringBlank(bean.getToUsername())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setParent(parent);
				Date validUntil = null;
				if(!StringUtil.isStringBlank(bean.getValidUntil())) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						validUntil = sdf.parse(bean.getValidUntil());
					} catch(ParseException ex) {
						data.getMessageLogger().addErrorMessage("Datum je pogrešnog formata.");
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
				}
				if(validUntil!=null && new Date().after(validUntil)) {
					data.getMessageLogger().addErrorMessage("Ne može poslati ponudu koja je već u startu istekla.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				User otherUser = dh.getUserDAO().getUserByUsername(em, bean.getToUsername());
				if(otherUser==null) {
					// Dajmo generalnu poruku, tako da se ne moze zakljuciti sto je tocno problem s korisnikom.
					data.getMessageLogger().addErrorMessage("Korisnik "+bean.getToUsername()+" ne postoji ili nije na kolegiju u zadanoj grupi.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				Group otherGroup = bean.getGroupID()!=null ? dh.getGroupDAO().get(em, bean.getGroupID()) : null;
				UserGroup otherUserGroup = otherGroup==null ? null : dh.getUserGroupDAO().find(em, otherUser, otherGroup);
				if(otherUserGroup==null) {
					// Dajmo namjerno istu poruku kao i maloprije, tako da se ne moze zakljuciti sto je tocno problem s korisnikom.
					data.getMessageLogger().addErrorMessage("Korisnik "+bean.getToUsername()+" ne postoji ili nije na kolegiju u zadanoj grupi.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				UserGroup myUserGroup = dh.getUserGroupDAO().get(em, bean.getMyUserGroupID());
				if(myUserGroup==null  || !myUserGroup.getUser().equals(data.getCurrentUser())
						|| !myUserGroup.getGroup().getParent().equals(parent) || otherUserGroup==null || otherGroup==null 
						|| !otherGroup.getParent().equals(parent)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(otherUser.equals(myUserGroup.getUser())) {
					data.getMessageLogger().addErrorMessage("Stvarno duhovito :-)");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				if(otherGroup.equals(myUserGroup.getGroup())) {
					data.getMessageLogger().addErrorMessage("Već ste u traženoj grupi!");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				Group myGroup = myUserGroup.getGroup();
				if(!myUserGroup.getGroup().getId().equals(bean.getMyGroupID())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
				if(!mpwc.marketPlaceActive) {
					data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				} else {
					List<MPOffer> offers = dh.getMarketPlaceDAO().listOffersRegardingUser(em, parent.getMarketPlace(), data.getCurrentUser(), myGroup);
					MPUserState state = MPUserState.buildFrom(offers, data.getCurrentUser(), mpwc.now, mpwc.scons, mpwc.myUserGroupMap);
					MPUserGroupState s = state.getOrCreateForGroup(myUserGroup);
					mpwc.calcSendDestinations(s);
					for(MPOffer o : s.getMyDirectOffers()) {
						if(o.getToGroup().equals(otherGroup) && o.getToUser().equals(otherUser)) {
							data.getMessageLogger().addErrorMessage("Tom ste korisniku već poslali direktnu ponudu.");
							data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
							return null;
						}
					}
					// Kada mu smijem poslati grupnu ponudu? Ako je "s" aktivan i ako odredisna grupa nije blokirana:
					if(s.isActive() && !s.getBlockedGroups().contains(otherGroup)) {
						// Sada mogu poslati ponudu...
						MPOffer offer = new MPOffer();
						offer.setFromGroup(myGroup);
						offer.setFromUser(myUserGroup.getUser());
						offer.setFromTag(myUserGroup.getTag());
						offer.setMarketPlace(parent.getMarketPlace());
						offer.setNeedsAck(false); // Ove ponude po definiciji ne mogu biti s potvrdom!
						offer.setToGroup(otherGroup);
						offer.setToUser(otherUser);
						offer.setValidUntil(validUntil);
						if(!StringUtil.isStringBlank(bean.getReason())) {
							if(bean.getReason().length()>100) {
								offer.setReason(bean.getReason().substring(0, 100));
							} else {
								offer.setReason(bean.getReason());
							}
						}
						dh.getMarketPlaceDAO().save(em, offer);
						data.getMessageLogger().addInfoMessage("Ponuda je poslana.");
					} else {
						// Ne mogu poslati ponudu...
						data.getMessageLogger().addErrorMessage("Ponudu nije moguće poslati.");
					}
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getUploadStudentTagsDataImpl(UploadStudentTagsData data,	Long userID, String courseInstanceID, Long parentID, List<StudentGroupTagBean> beans) {
		Long pid = parentID;
		try {
			MPLockFactory.get().writeLock(pid);
			getUploadStudentTagsDataImpl(data, userID, courseInstanceID, parentID, beans);
		} finally {
			MPLockFactory.get().releaseLock(pid);
		}
	}

	public static void getGroupCoarseStatData(final GroupCoarseStatData data, final Long userID, final String semesterID, final String parentRelativePath, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canUpdate = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canUpdate) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				data.setAllSemesters(dh.getYearSemesterDAO().list(em));
				if(task.equals("input")) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(!task.equals("view")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				String prp = StringUtil.isStringBlank(parentRelativePath) ? "0" : parentRelativePath;
				String cysemID = StringUtil.isStringBlank(semesterID) ? getCurrentSemesterID(em) : semesterID;
				YearSemester ysem = StringUtil.isStringBlank(cysemID) ? null : dh.getYearSemesterDAO().get(em, cysemID);
				if(ysem==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setYearSemester(ysem);
				data.setParentRelativePath(prp);
				List<CourseInstance> allCourses = dh.getCourseInstanceDAO().findForSemester(em, ysem.getId());
				Map<String, CourseInstance> mapByIsvu = CourseInstanceUtil.mapCourseInstanceByID(allCourses);
				List<CoarseGroupStat2> stat = dh.getGroupDAO().getCoarseGroupStat2(em, ysem.getId()+"/%", prp+"/%");
				Map<String, List<CoarseGroupStat2>> statMap = new HashMap<String, List<CoarseGroupStat2>>(100);
				for(CoarseGroupStat2 st : stat) {
					CourseInstance ci = mapByIsvu.get(st.getCompositeCourseID());
					if(ci==null) {
						continue; // Sta se je ovdje dogodilo?!?!?!
					}
					st.setCourseIsvuCode(ci.getCourse().getIsvuCode());
					st.setCourseName(ci.getCourse().getName());
					List<CoarseGroupStat2> list = statMap.get(st.getCourseIsvuCode());
					if(list==null) {
						list = new ArrayList<CoarseGroupStat2>();
						statMap.put(st.getCourseIsvuCode(), list);
					}
					list.add(st);
				}
				Collections.sort(allCourses, StringUtil.COURSEINSTANCE_COMPARATOR);
				List<List<CoarseGroupStat2>> finalList = new ArrayList<List<CoarseGroupStat2>>(statMap.size());
				for(CourseInstance ci : allCourses) {
					List<CoarseGroupStat2> list = statMap.get(ci.getCourse().getIsvuCode());
					if(list==null) {
						continue; // Hm... ovo se ne smije dogoditi!
					}
					Collections.sort(list, new Comparator<CoarseGroupStat2>() {
						@Override
						public int compare(CoarseGroupStat2 o1, CoarseGroupStat2 o2) {
							return StringUtil.HR_COLLATOR.compare(o1.getGroupName(), o2.getGroupName());
						}
					});
					finalList.add(list);
				}
				data.setStats(finalList);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getGroupMembershipExportData(final GroupMembershipExportData data, final Long userID, final GroupMembershipExportBean bean, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(StringUtil.isStringBlank(bean.getFormat())) {
					bean.setFormat("csv");
				}
				if(!bean.getFormat().equals("csv") && !bean.getFormat().equals("xls")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canView = false;
				if(!StringUtil.isStringBlank(bean.getCourseInstanceID())) {
					if(!fillCourseInstance(em, data, bean.getCourseInstanceID())) return null;
					canView = JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance());
				} else {
					canView = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				}
				if(StringUtil.isStringBlank(bean.getParentRelativePath()) && StringUtil.isStringBlank(bean.getRelativePath()) && !task.equals("input")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!canView) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				data.setAllSemesters(dh.getYearSemesterDAO().list(em));
				String semID = bean.getSemesterID();
				if(StringUtil.isStringBlank(semID)) semID = getCurrentSemesterID(em);
				if(StringUtil.isStringBlank(semID)) {
					data.getMessageLogger().addErrorMessage("Ne mogu utvrditi trenutni semestar.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				YearSemester ysem = dh.getYearSemesterDAO().get(em, semID);
				if(ysem==null) {
					data.getMessageLogger().addErrorMessage("Traženi semetar nije u bazi.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				bean.setSemesterID(semID);
				List<CourseInstance> allCourses = dh.getCourseInstanceDAO().findForSemester(em, bean.getSemesterID());
				Collections.sort(allCourses, StringUtil.COURSEINSTANCE_COMPARATOR);
				List<CourseInstance> selectionList = new ArrayList<CourseInstance>(allCourses.size()+1);
				CourseInstance emptyCourseInstance = new CourseInstance();
				Course emptyCourse = new Course();
				emptyCourse.setIsvuCode("");
				emptyCourse.setName("");
				emptyCourseInstance.setCourse(emptyCourse);
				selectionList.add(emptyCourseInstance);
				selectionList.addAll(allCourses);
				data.setAllCourses(selectionList);
				if(task.equals("input")) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(!task.equals("view")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				List<UserGroup> ugList = null;
				List<CourseInstance> courses;
				if(data.getCourseInstance()!=null) {
					// Trazi na kolegiju
					courses = new ArrayList<CourseInstance>();
					courses.add(data.getCourseInstance());
					if(!StringUtil.isStringBlank(bean.getRelativePath())) {
						ugList = dh.getGroupDAO().findUserGroup(em, data.getCourseInstance().getId(), bean.getRelativePath());
					} else {
						ugList = dh.getGroupDAO().findUserGroup(em, data.getCourseInstance().getId(), bean.getParentRelativePath()+"/%");
					}
				} else {
					// Trazi na svim kolegijima
					courses = allCourses;
					if(!StringUtil.isStringBlank(bean.getRelativePath())) {
						ugList = dh.getGroupDAO().findUserGroup(em, bean.getSemesterID()+"/%", bean.getRelativePath());
					} else {
						ugList = dh.getGroupDAO().findUserGroup(em, bean.getSemesterID()+"/%", bean.getParentRelativePath()+"/%");
					}
				}
				Map<String, CourseInstance> mapByID = CourseInstanceUtil.mapCourseInstanceByID(courses);
				ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
				if(bean.getFormat().equals("csv")) {
					if(!exportUserListToCSV(data.getMessageLogger(), ugList, bos, bean, mapByID)) {
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					data.setMimeType("text/csv");
					data.setFileName("popis.csv");
				} else if(bean.getFormat().equals("xls")) {
					if(!exportUserListToXLS(data.getMessageLogger(), ugList, bos, bean, mapByID)) {
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					data.setMimeType("application/vnd.ms-excel");
					data.setFileName("popis.xls");
				}

				byte[] bytes = bos.toByteArray();
				data.setStream(new ByteArrayInputStream(bytes));
				data.setLength(bytes.length);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	protected static boolean exportUserListToCSV(IMessageLogger messageLogger, List<UserGroup> ugList, ByteArrayOutputStream bos, GroupMembershipExportBean bean, Map<String, CourseInstance> mapByID) {
		try {
			OutputStreamWriter w = new OutputStreamWriter(bos, "UTF-8");
			for(UserGroup ug : ugList) {
				w.append(ug.getUser().getJmbag());
				if(bean.getWriteISVUCode()) {
					CourseInstance ci = mapByID.get(ug.getGroup().getCompositeCourseID());
					if(ci==null) {
						w.append("#ISVU!Not!Found");
					} else {
						w.append("#").append(ci.getCourse().getIsvuCode());
					}
				}
				if(bean.getWriteStudentName()) {
					w.append("#").append(ug.getUser().getLastName()).append(", ").append(ug.getUser().getFirstName());
				}
				w.append("#").append(ug.getGroup().getName());
				if(bean.getWriteStudentTag()) {
					w.append("#");
					if(ug.getTag()!=null) w.append(ug.getTag());
				}
				w.append("\r\n");
			}
			w.flush();
			w.close();
		} catch(Exception ex) {
			messageLogger.addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}

	protected static boolean exportUserListToXLS(IMessageLogger messageLogger, List<UserGroup> ugList, ByteArrayOutputStream bos, GroupMembershipExportBean bean, Map<String, CourseInstance> mapByID) {
		try {
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("Popis");
			sheet.setDefaultColumnWidth((short)20);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			// postavi kao stil tekst, prema http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
			cellStyle.setDataFormat((short)0x31); 
			int rowIndex = -1;
			for(UserGroup ug : ugList) {
				rowIndex++;
				int columnIndex = 0;
				HSSFRow row = sheet.createRow((short)rowIndex);
				HSSFCell cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(ug.getUser().getJmbag()));
				if(bean.getWriteISVUCode()) {
					CourseInstance ci = mapByID.get(ug.getGroup().getCompositeCourseID());
					if(ci==null) {
						cell = row.createCell((short)columnIndex); columnIndex++;
						cell.setCellStyle(cellStyle);
						cell.setCellValue(new HSSFRichTextString("#ISVU!Not!Found"));
					} else {
						cell = row.createCell((short)columnIndex); columnIndex++;
						cell.setCellStyle(cellStyle);
						cell.setCellValue(new HSSFRichTextString(ci.getCourse().getIsvuCode()));
					}
				}
				if(bean.getWriteStudentName()) {
					cell = row.createCell((short)columnIndex); columnIndex++;
					cell.setCellStyle(cellStyle);
					cell.setCellValue(new HSSFRichTextString(ug.getUser().getLastName()+", "+ug.getUser().getFirstName()));
				}
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(ug.getGroup().getName()));
				if(bean.getWriteStudentTag()) {
					cell = row.createCell((short)columnIndex); columnIndex++;
					cell.setCellStyle(cellStyle);
					cell.setCellValue(new HSSFRichTextString(ug.getTag()!=null ? ug.getTag() : ""));
				}
			}

			wb.write(bos);
			bos.flush();
			bos.close();
		} catch(Exception ex) {
			messageLogger.addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}

	public static void getYearSemesterEditData(final YearSemesterEditData data,
			final YearSemesterBean bean, final Long userID, final String task, final boolean create) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canDoIt = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canDoIt) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(task.equals("list")) {
					List<YearSemester> list = dh.getYearSemesterDAO().list(em);
					data.setAllYearSemesters(list);
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(task.equals("new")) {
					bean.setAcademicYear("");
					bean.setId("");
					bean.setSemester("");
					bean.setStartsAt("");
					bean.setEndsAt("");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(task.equals("edit")) {
					YearSemester ys = dh.getYearSemesterDAO().get(em, bean.getId());
					if(ys==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Trazeni semestar ne postoji."));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					bean.setAcademicYear(ys.getAcademicYear());
					bean.setId(ys.getId());
					bean.setSemester(ys.getSemester());
					bean.setStartsAt(DateUtil.dateTimeToString(ys.getStartsAt()));
					bean.setEndsAt(DateUtil.dateTimeToString(ys.getEndsAt()));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(!task.equals("save")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				YearSemester ys = dh.getYearSemesterDAO().get(em, bean.getId());
				if(ys==null && !create) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Trazeni semestar ne postoji."));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(ys!=null && create) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Trazeni semestar već postoji."));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				boolean foundErrors = false;
				if(StringUtil.isStringBlank(bean.getId())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("ID mora biti zadan."));
					foundErrors = true;
				} else if(!bean.getId().matches("^\\d{4}[A-Z]$")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("ID je pogrešnog formata."));
					foundErrors = true;
				}
				if(StringUtil.isStringBlank(bean.getSemester())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Semestar mora biti zadan."));
					foundErrors = true;
				}
				if(StringUtil.isStringBlank(bean.getAcademicYear())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Akademska godina mora biti zadana."));
					foundErrors = true;
				} else if(bean.getAcademicYear().length()!=9 || !bean.getAcademicYear().matches("^\\d{4}/\\d{4}$") || (Integer.parseInt(bean.getAcademicYear().substring(0,4))+1)!=Integer.parseInt(bean.getAcademicYear().substring(5,9))) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Akademska godina je pogrešnog formata."));
					foundErrors = true;
				}
				if(!StringUtil.isStringBlank(bean.getStartsAt())) {
					if(!DateUtil.checkFullDateFormat(bean.getStartsAt())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Početak semestra je pogrešnog formata."));
						foundErrors = true;
					}
				}
				if(!StringUtil.isStringBlank(bean.getEndsAt())) {
					if(!DateUtil.checkFullDateFormat(bean.getEndsAt())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Kraj semestra je pogrešnog formata."));
						foundErrors = true;
					}
				}
				if(foundErrors) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(ys==null) {
					ys = new YearSemester();
				}
				ys.setId(bean.getId());
				ys.setAcademicYear(bean.getAcademicYear());
				ys.setSemester(bean.getSemester());
				if(!StringUtil.isStringBlank(bean.getStartsAt())) {
					ys.setStartsAt(DateUtil.stringToDateTime(bean.getStartsAt()));
				} else {
					ys.setStartsAt(null);
				}
				if(!StringUtil.isStringBlank(bean.getEndsAt())) {
					ys.setEndsAt(DateUtil.stringToDateTime(bean.getEndsAt()));
				} else {
					ys.setEndsAt(null);
				}
				
				if(create) {
					dh.getYearSemesterDAO().save(em, ys);
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
				} else {
					data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getMPGroupSettingsViewData(final MPGroupSettingsViewData data,
			final Long userID, final String semesterID, final String parentRelativePath,
			final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canUpdate = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canUpdate) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				data.setAllSemesters(dh.getYearSemesterDAO().list(em));
				if(task.equals("input")) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				if(!task.equals("view")) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				String prp = StringUtil.isStringBlank(parentRelativePath) ? "0" : parentRelativePath;
				String cysemID = StringUtil.isStringBlank(semesterID) ? getCurrentSemesterID(em) : semesterID;
				YearSemester ysem = StringUtil.isStringBlank(cysemID) ? null : dh.getYearSemesterDAO().get(em, cysemID);
				if(ysem==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setYearSemester(ysem);
				data.setParentRelativePath(prp);
				List<CourseInstance> allCourses = dh.getCourseInstanceDAO().findForSemester(em, ysem.getId());
				Map<String, CourseInstance> mapByIsvu = CourseInstanceUtil.mapCourseInstanceByID(allCourses);
				List<Group> allGroups = dh.getGroupDAO().findSubgroupsLLE(em, ysem.getId()+"/%", prp, prp+"/%");
				Map<String, List<Group>> groupsByCourses = GroupUtil.mapGroupByCompositeCourseID(allGroups);
				Map<String, Group> courseMarketPlaces = GroupUtil.mapMarketPlacesByCompositeCourseID(allGroups);
				data.setCourses(new ArrayList<MPGSVCourse>(groupsByCourses.size()));
				for(String key : groupsByCourses.keySet()) {
					List<Group> groups = groupsByCourses.get(key);
					Group parentGroup = courseMarketPlaces.get(key);
					CourseInstance courseInstance = mapByIsvu.get(key);
					MPGSVCourse c = new MPGSVCourse();
					c.setCourseName(courseInstance.getCourse().getName());
					c.setIsvuCode(courseInstance.getCourse().getIsvuCode());
					c.setGroups(new ArrayList<MPGSVGroup>(groups.size()));
					MPGSVMarketPlace mp = new MPGSVMarketPlace();
					if(parentGroup.getMarketPlace()==null) {
						mp.setAbsent(true);
						mp.setTimeBuffer(-1);
					} else {
						mp.setAbsent(false);
						mp.setTimeBuffer(parentGroup.getMarketPlace().getTimeBuffer());
						mp.setFormulaConstraints(parentGroup.getMarketPlace().getFormulaConstraints());
						mp.setId(parentGroup.getMarketPlace().getId());
						mp.setOpen(parentGroup.getMarketPlace().getOpen());
						mp.setOpenFrom(parentGroup.getMarketPlace().getOpenFrom());
						mp.setOpenUntil(parentGroup.getMarketPlace().getOpenUntil());
						mp.setSecurityConstraints(parentGroup.getMarketPlace().getSecurityConstraints());
					}
					c.setMarketPlace(mp);
					for(Group g : groups) {
						if(g.isManagedRoot()) continue;
						MPGSVGroup gr = new MPGSVGroup();
						gr.setCapacity(g.getCapacity());
						gr.setCompositeCourseID(g.getCompositeCourseID());
						gr.setEnteringAllowed(g.isEnteringAllowed());
						gr.setId(g.getId());
						gr.setLeavingAllowed(g.isLeavingAllowed());
						gr.setManagedRoot(g.isManagedRoot());
						gr.setMpSecurityTag(g.getMpSecurityTag());
						gr.setName(g.getName());
						gr.setRelativePath(g.getRelativePath());
						c.getGroups().add(gr);
					}
					Collections.sort(c.getGroups());
					data.getCourses().add(c);
				}
				Collections.sort(data.getCourses());
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void changeMPGroupSettingsViewData(
			final MPGroupSettingsViewData data, final Long userID,
			final Set<Long> selectedMarketPlaces, final boolean openIt) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canUpdate = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canUpdate) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				for(Long id : selectedMarketPlaces) {
					MarketPlace mp = dh.getMarketPlaceDAO().getMarketPlace(em, id);
					if(mp!=null && mp.getOpen() != openIt) {
						mp.setOpen(openIt);
					}
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	public static void getListGroupEventsData(final ListGroupEventsData data, final Long userID, final String courseInstanceID, final String relativePath) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID, "Error.invalidParameters", AbstractActionData.RESULT_FATAL)) return null;
				String relPath = relativePath;
				if(relPath==null) relPath = "";
				User user = data.getCurrentUser();
				JCMSSecurityManagerFactory.getManager().init(user, em);
				CourseInstance ci = data.getCourseInstance();
				boolean canView = JCMSSecurityManagerFactory.getManager().canViewGroupTree(ci, relPath);
				if(!canView) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group group = dh.getGroupDAO().get(em, ci.getId(), relPath);
				if(group==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				List<GroupWideEvent> events = new ArrayList<GroupWideEvent>(group.getEvents());
				Collections.sort(events, StringUtil.GROUP_WIDE_EVENT_COMPARATOR);
				data.setEvents(events);
				data.setOwners(new ArrayList<GroupOwner>());
				data.setGroup(group);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getShowCourseEventsData(final ShowCourseEventsData data,
			final Long userID, final String courseInstanceID, final Date dateFrom,
			final Date dateTo) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!fillCourseInstance(em, data, courseInstanceID, "Error.invalidParameters", AbstractActionData.RESULT_FATAL)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setEvents(EventsService.listForCourseInstance(em, data.getCourseInstance(), dateFrom, dateTo));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

}
