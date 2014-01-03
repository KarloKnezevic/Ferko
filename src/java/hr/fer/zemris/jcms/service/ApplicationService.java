package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.beans.ApplicationDefinitionBean;
import hr.fer.zemris.jcms.beans.StudentApplicationBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.ApplicationStatus;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminAproveData;
import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminEditData;
import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminTableData;
import hr.fer.zemris.jcms.web.actions.data.ApplicationExportListData;
import hr.fer.zemris.jcms.web.actions.data.ApplicationExportTableData;
import hr.fer.zemris.jcms.web.actions.data.ApplicationListStudentsData;
import hr.fer.zemris.jcms.web.actions.data.ApplicationMainData;
import hr.fer.zemris.jcms.web.actions.data.ApplicationStudentSubmitData;
import hr.fer.zemris.jcms.web.actions.data.ApplicationStudentViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

@Deprecated
public class ApplicationService {
	
	@Deprecated
	public static void getApplicationMainData(final ApplicationMainData data,
			final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				if (currentUser == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				if (ci == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("ShowCourse.noCourse"));
					return null;
				}
				data.setCourseInstance(ci);
				
				if (!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(ci)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				boolean isAdmin = JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci);
				data.setRenderCourseAdministration(isAdmin);
				data.setDefinitions(dh.getApplicationDAO().listDefinitions(em, ci.getId()));
				
				if(!isAdmin){
				List<StudentApplication> l = dh.getApplicationDAO().listForUser(em, currentUser, ci.getId());
				Map<Long, StudentApplication> map = new HashMap<Long, StudentApplication>();
				for (StudentApplication st : l) {
					map.put(st.getApplicationDefinition().getId(), st);
				}
				data.setFilledApplications(map);
				}
				return null;
			}
		});
	}

	@Deprecated
	public static void getApplicationStudentViewData(
			final ApplicationStudentViewData data, final Long userID,
			final String courseInstanceID, final Long applicationID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				if (currentUser == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				if (ci == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("ShowCourse.noCourse"));
					return null;
				}
				data.setCourseInstance(ci);
				
				if (!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(ci)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				data.setApplication(dh.getApplicationDAO().get(em, applicationID));
				return null;
			}
		});
	}

	@Deprecated
	public static void getApplicationAdminEditData(
			final ApplicationAdminEditData data,
			final ApplicationDefinitionBean bean, final Long userID,
			final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				if (currentUser == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				CourseInstance ci = dh.getCourseInstanceDAO().get(em, bean.getCourseInstanceID());
				if (ci == null) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"ShowCourse.noCourse"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseInstance(ci);
			
				if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				ApplicationDefinition definition = null;
				if (bean.getId() != null && !bean.getId().equals("")) {
					try {
						definition = dh.getApplicationDAO()
						.getDefinition(em, Long.valueOf(bean.getId()));
					} catch (Exception ignorable) {}
					if (definition == null) {
						data.getMessageLogger().addErrorMessage(
								data.getMessageLogger().getText(
								"Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				if (definition != null) {
					data.setDefinition(definition);
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if (task.equals("new")) {
					bean.setCourseInstanceID(ci.getId().toString());
					bean.setId(null);
					bean.setName("");
					bean.setShortName("");
					bean.setOpenFrom(sdf.format(new Date()));
					bean.setOpenUntil(sdf.format(new Date()));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				if (task.equals("edit")) {
					if (definition == null) {
						data.setResult(AbstractActionData.RESULT_FATAL);
					} else {
						bean.setId(definition.getId().toString());
						bean.setCourseInstanceID(definition.getCourse()
								.getId().toString());
						bean.setName(definition.getName());
						bean.setShortName(definition.getShortName());
						bean.setOpenFrom(sdf.format(definition.getOpenFrom()));
						bean.setOpenUntil(sdf.format(definition.getOpenUntil()));
						data.setResult(AbstractActionData.RESULT_INPUT);
					}
					return null;
				}
				if (!task.equals("save")) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				boolean willDoIt = true;
				if (bean.getName() == null
						|| bean.getName().trim().equals("")) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.nameMustBeGiven"));
					willDoIt = false;
				} else if (bean.getName().length() > 100) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.nameTooLong"));
				}
				if (bean.getShortName() == null
						|| bean.getShortName().trim().equals("")) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.shortNameMustBeGiven"));
					willDoIt = false;
				} else if (bean.getShortName().length() > 10) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.shortNameTooLong"));
				}
				
				

				Date openFrom = null;
				Date openUntil = null;
				try {
					openFrom = sdf.parse(bean.getOpenFrom());
					openUntil = sdf.parse(bean.getOpenUntil());

				} catch (ParseException ignore) {
				}

				if (openFrom == null || openUntil == null) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.wrongDateFormat"));
					willDoIt = false;
				} else if (!openFrom.before(openUntil)) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.datesMustBeInOrder"));
					willDoIt = false;
				}
				if (!willDoIt) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}

				boolean isNew = (definition == null);
				if (definition == null) {
					definition = new ApplicationDefinition();
				}
				definition.setName(bean.getName());
				definition.setShortName(bean.getShortName());
				definition.setCourse(ci);
				definition.setOpenFrom(openFrom);
				definition.setOpenUntil(openUntil);

				if (isNew) {
					dh.getApplicationDAO().save(em, definition);
					data.getMessageLogger().addInfoMessage(
							data.getMessageLogger().getText(
									"Info.dataSuccessfullyInserted"));
				} else {
					data.getMessageLogger().addInfoMessage(
							data.getMessageLogger().getText(
									"Info.dataSuccessfullyUpdated"));
				}
				data.setDefinition(definition);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});

	}

	@Deprecated
	public static void getApplicationStudentSubmitData(
			final ApplicationStudentSubmitData data,
			final StudentApplicationBean bean, final Long userID,
			final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				if (currentUser == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				CourseInstance ci = dh.getCourseInstanceDAO().get(em, data.getCourseInstanceID());
				if (ci == null) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"ShowCourse.noCourse"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseInstance(ci);

				if (!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(ci)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				ApplicationDefinition definition = null;
				try {
					definition = dh.getApplicationDAO().getDefinition(em, 
							Long.parseLong(data.getApplicationID()));
				} catch (Exception ignorable) {}
				
				if (definition == null) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				data.setDefinition(definition);
				try {
					if (dh.getApplicationDAO().getApplicationForUser(
							em, currentUser,
							definition.getId()) != null) {
						data.getMessageLogger().addErrorMessage(
								data.getMessageLogger().getText(
										"Error.alreadySubmited"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				} catch (Exception ignorable) {}

				StudentApplication application = null;

				if (task.equals("new")) {
					bean.setUserID(userID);
					bean.setId(null);
					bean.setReason("");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}

				if (!task.equals("save")) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				boolean willDoIt = true;
				if (bean.getReason() == null || bean.getReason().trim().equals("")) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.reasonMustBeGiven"));
					willDoIt = false;
				}
				Date now = new Date();
				// Uzmi trenutno vrijeme

				if (now.after(definition.getOpenUntil())
						|| now.before(definition.getOpenFrom())) {
					data.getMessageLogger().addErrorMessage(
							data.getMessageLogger().getText(
									"Error.applicationClosed"));
					willDoIt = false;
				}
				if (!willDoIt) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}

				application = new StudentApplication();
				application.setApplicationDefinition(definition);
				application.setDate(now);
				application.setReason(bean.getReason());
				application.setStatus(ApplicationStatus.NEW);
				application.setStatusReason(null);
				application.setUser(currentUser);

				dh.getApplicationDAO().save(em, application);
				data.getMessageLogger().addInfoMessage(
						data.getMessageLogger().getText(
								"Info.dataSuccessfullyInserted"));

				data.setApplication(application);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});

	}

	@Deprecated
	public static void getApplicationAdminTableData(
			final ApplicationAdminTableData data, final Long userID,
			final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em,
						userID);
				if (currentUser == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				if (ci == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("ShowCourse.noCourse"));
					return null;
				}
				data.setCourseInstance(ci);
				
				if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				data.setDefinitions(dh.getApplicationDAO().listDefinitions(em, ci.getId()));
				List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
				Collections.sort(users, new Comparator<User>(){

					public int compare(User u1, User u2) {
					int ret = u1.getLastName().compareTo(u2.getLastName());
					if(ret != 0)
						return ret;
					ret = u1.getFirstName().compareTo(u2.getLastName());
					if(ret != 0)
						return ret;
					ret = u1.getJmbag().compareTo(u2.getJmbag());
					return ret;
					}
				});
				data.setUsers(users);
				// Ideja: ako je data.isFullList()=true, tada prikazujemo sve korisnike, imali oni prijava ili ne
				//        ako je data.isFullList()=false, treba prikazati samo one studente koji imaju barem jednu prijavu
				//        (studente koji se trebaju prikazati pamte se u shortUserList) 
				List<User> shortUserList = new ArrayList<User>(users.size());
				Map<Long, Map<Long, StudentApplication>> map = new HashMap<Long, Map<Long, StudentApplication>>();
				for (User u : users) {
					// TODO: preraditi - vrlo lose rjesenje
					// Komentar: trebamo sve prijave ionako; zasto ovdje za digitalnu saljemo 850 sql upita sto traje i traje, umjesto da posaljemo
					//           upit: daj mi sve na ovom kolegiju, pa si sami to razvrstamo? Nemojmo zaboraviti da u ovom prvom slucaju upit dolazi
					//           850 pita optimizatoru na obradu, a i podaci putuju amo-tamo mrezom sto sve bitno usporava izvrsavanje!
					List<StudentApplication> l = dh.getApplicationDAO().listForUser(em, u, ci.getId());
					if(data.isFullList() || (l!=null && !l.isEmpty())) {
						shortUserList.add(u);
						Map<Long, StudentApplication> m = new HashMap<Long, StudentApplication>();
						for (StudentApplication sa : l) {
							m.put(sa.getApplicationDefinition().getId(), sa);
						}
						map.put(u.getId(), m);
					}
				}
				data.setUsers(shortUserList);
				data.setApplications(map);
				return null;
			}
		});
	}

	@Deprecated
	public static void getApplicationAdminAproveData(
			final ApplicationAdminAproveData data, final Long userID,
			final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				if(currentUser==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				CourseInstance ci = dh.getCourseInstanceDAO().get(em, data.getCourseInstanceID());
				if(ci==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("ShowCourse.noCourse"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCourseInstance(ci);

				if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
					data.getMessageLogger(). addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
						
				User user = dh.getUserDAO().getUserById(em, data.getStudentID());
				if(user==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noUser"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setStudent(user);

				if(task.equals("view")) {
					List<StudentApplication> list = null;
					try {
						list = dh.getApplicationDAO().listForUser(em, user, data.getCourseInstanceID());
					} catch(Exception ignorable) {
					}
					boolean foundFromDefinitionID = false;
					for(StudentApplication sa : list){
						if(data.getFromDefinitionID()!=null && sa.getApplicationDefinition().getId().equals(data.getFromDefinitionID())) {
							foundFromDefinitionID = true;
						}
						StudentApplicationBean bean = new StudentApplicationBean();
						bean.setId(sa.getId());
						bean.setDate(sa.getDate());
						bean.setReason(sa.getReason());
						bean.setStatus(sa.getStatus().name());
						bean.setDefinition(sa.getApplicationDefinition().getName());
						String sr = sa.getStatusReason();
						if(sr==null)
							bean.setStatusReason("");
						else
							bean.setStatusReason(sr);
						data.getBeans().add(bean);
					}
					if(data.getFromDefinitionID()!=null && !foundFromDefinitionID) {
						data.setFromDefinitionID(null);
					}
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;

				}
				if(!task.equals("save")) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				boolean willDoIt = checkApplicationBeans(data.getBeans());
				if(!willDoIt) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.reasonMustBeGiven"));
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				
				List<StudentApplication> list = null;
				try {
					list = dh.getApplicationDAO().listForUser(em, user, data.getCourseInstanceID());
				} catch(Exception ignorable) {}
				
				Map<Long, StudentApplication> applicationMap = new HashMap<Long, StudentApplication>();
				for (StudentApplication apl : list) {
					applicationMap.put(apl.getId(), apl);
				}

				for(StudentApplicationBean bean : data.getBeans()){
					StudentApplication sa = applicationMap.get(bean.getId());
					if(sa.getStatus().name() != bean.getStatus())
						sa.setStatus(ApplicationStatus.valueOf(bean.getStatus()));
					if(!bean.getStatusReason().trim().equals(sa.getStatusReason()))
						sa.setStatusReason(bean.getStatusReason().trim());
					if(sa.getStatus()==ApplicationStatus.NEW)
						sa.setStatusReason(null);
					
				}
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});

	}

	@Deprecated
	protected static boolean checkApplicationBeans(
			List<StudentApplicationBean> beans) {
		for(StudentApplicationBean b : beans){
			if(!ApplicationStatus.NEW.name().equals(b.getStatus()))
			   if(b.getStatusReason()==null || b.getStatusReason().trim().equals("")){
				return false;
			}
		}
		return true;
	}

	@Deprecated
	public static void getApplicationListStudentsData(
			final ApplicationListStudentsData data, final Long userID,
			final String courseInstanceID, final Long definitionID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em,
						userID);
				if (currentUser == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				if (ci == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("ShowCourse.noCourse"));
					return null;
				}
				data.setCourseInstance(ci);
				
				ApplicationDefinition definition = dh.getApplicationDAO().getDefinition(em, definitionID);
				if (definition == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					return null;
				}
				data.setDefinition(definition);
				
				if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				List<StudentApplication> applications = dh.getApplicationDAO()
					.listForDefinition(em, courseInstanceID, definitionID);
				
				List<User> users = new ArrayList<User>();
				Map<Long, StudentApplication> map = new HashMap<Long, StudentApplication>();

				for(StudentApplication sa : applications){
					users.add(sa.getUser());
					map.put(sa.getUser().getId(), sa);
				}
				data.setUsers(users);
				data.setApplications(map);
				return null;
			}
		});
		
	}

	@Deprecated
	public static void getApplicationExportListData(
			final ApplicationExportListData data, final Long userID, 
			final String courseInstanceID, final Long definitionID, final String format) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em,
						userID);
				if (currentUser == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				if( !"csv".equals(format) && !"xls".equals(format)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				if (ci == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("ShowCourse.noCourse"));
					return null;
				}
				data.setCourseInstance(ci);
				
				ApplicationDefinition definition = dh.getApplicationDAO().getDefinition(em, definitionID);
				if (definition == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					return null;
				}
				data.setDefinition(definition);
				
				if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				List<StudentApplication> applications = dh.getApplicationDAO()
					.listForDefinition(em, courseInstanceID, definitionID);
				
				List<User> users = new ArrayList<User>();
				Map<Long, StudentApplication> map = new HashMap<Long, StudentApplication>();

				for(StudentApplication sa : applications){
					users.add(sa.getUser());
					map.put(sa.getUser().getId(), sa);
				}
				data.setUsers(users);
				data.setApplications(map);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
				if(format.equals("csv")) {
					if(!exportListToCSV(data, bos)) {
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					data.setMimeType("text/csv");
					data.setFileName("prijave_"+data.getDefinition().getShortName()+".csv");
				} else if(format.equals("xls")) {
					if(!exportListToXLS(data, bos)) {
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					data.setMimeType("application/vnd.ms-excel");
					data.setFileName("prijave_"+data.getDefinition().getShortName()+".xls");
				}

				byte[] bytes = bos.toByteArray();
				data.setStream(new ByteArrayInputStream(bytes));
				data.setLength(bytes.length);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
		
	}
	
	@Deprecated
	public static void getApplicationExportTableData(
			final ApplicationExportTableData data, final Long userID,
			final String courseInstanceID, final String format) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em,
						userID);
				if (currentUser == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.invalidParameters"));
					return null;
				}
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);

				if( !"csv".equals(format) && !"xls".equals(format)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				
				CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				if (ci == null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("ShowCourse.noCourse"));
					return null;
				}
				data.setCourseInstance(ci);
				
				if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger()
							.getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
					return null;
				}
				
				data.setDefinitions(dh.getApplicationDAO().listDefinitions(em, ci.getId()));
				List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
				Collections.sort(users, new Comparator<User>(){

					public int compare(User u1, User u2) {
					int ret = u1.getLastName().compareTo(u2.getLastName());
					if(ret != 0)
						return ret;
					ret = u1.getFirstName().compareTo(u2.getLastName());
					if(ret != 0)
						return ret;
					ret = u1.getJmbag().compareTo(u2.getJmbag());
					return ret;
					}
				});
				data.setUsers(users);
				Map<Long, Map<Long, StudentApplication>> map = new HashMap<Long, Map<Long, StudentApplication>>();
				for (User u : users) {
					List<StudentApplication> l = dh.getApplicationDAO().listForUser(em, u, ci.getId());
					Map<Long, StudentApplication> m = new HashMap<Long, StudentApplication>();
					for (StudentApplication sa : l) {
						m.put(sa.getApplicationDefinition().getId(), sa);
					}
					map.put(u.getId(), m);
				}
				data.setApplications(map);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
				if(format.equals("csv")) {
					if(!exportTableToCSV(data, bos)) {
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					data.setMimeType("text/csv");
					data.setFileName("prijave.csv");
				} else if(format.equals("xls")) {
					if(!exportTableToXLS(data, bos)) {
						data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
						return null;
					}
					data.setMimeType("application/vnd.ms-excel");
					data.setFileName("prijave.xls");
				}

				byte[] bytes = bos.toByteArray();
				data.setStream(new ByteArrayInputStream(bytes));
				data.setLength(bytes.length);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});		
	}
	
	@Deprecated
	protected static boolean exportListToCSV(ApplicationExportListData data, ByteArrayOutputStream bos) {
		try {
			OutputStreamWriter w = new OutputStreamWriter(bos, "UTF-8");
			w.append("\"").append(data.getMessageLogger().getText("forms.jmbag"))
			 .append("\",\"").append(data.getMessageLogger().getText("forms.lastName"))
			 .append(", ").append(data.getMessageLogger().getText("forms.firstName"))
			 .append("\",\"").append(data.getDefinition().getShortName())
			 .append("\"").append("\r\n");;
			
			for(User u : data.getUsers()) {
				w.append("\"").append(u.getJmbag())
				 .append("\",\"").append(u.getLastName())
				 .append(", ").append(u.getFirstName())
				 .append("\",\"");
				w.append(statusToString(data.getApplications().get(u.getId())));
				w.append("\"").append("\r\n");
			}
			w.flush();
			w.close();
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}
	
	@Deprecated
	protected static boolean exportListToXLS(ApplicationExportListData data, ByteArrayOutputStream bos) {
	try {
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("Popis");
			sheet.setDefaultColumnWidth((short)20);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			// postavi kao stil tekst, prema http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
			cellStyle.setDataFormat((short)0x31); 
			int rowIndex = -1;
			for(User u : data.getUsers()) {
				rowIndex++;
				int columnIndex = 0;
				HSSFRow row = sheet.createRow((short)rowIndex);
				
				HSSFCell cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(u.getJmbag()));
				
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(u.getLastName() + ", " + u.getFirstName()));

				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(data.getApplications().get(u.getId()).getReason()));
			
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(statusToString(data.getApplications().get(u.getId()))));
			
			}

			wb.write(bos);
			bos.flush();
			bos.close();
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}

	@Deprecated
	protected static boolean exportTableToCSV(ApplicationExportTableData data, ByteArrayOutputStream bos) {
		try {
			OutputStreamWriter w = new OutputStreamWriter(bos, "UTF-8");
			w.append("\"").append(data.getMessageLogger().getText("forms.jmbag"))
			 .append("\",\"").append(data.getMessageLogger().getText("forms.lastName"))
			 .append(", ").append(data.getMessageLogger().getText("forms.firstName"));
			for(ApplicationDefinition ad : data.getDefinitions()){
				w.append("\",\"").append(ad.getShortName());
			}
			
			w.append("\"").append("\r\n");;
			
			for(User u : data.getUsers()) {
				w.append("\"").append(u.getJmbag())
				 .append("\",\"").append(u.getLastName())
				 .append(", ").append(u.getFirstName());
				for(ApplicationDefinition ad : data.getDefinitions()){
					w.append("\",\"")
					 .append(statusToString(data.getApplications().get(u.getId()).get(ad.getId())));
				}
				w.append("\"").append("\r\n");
			}
			w.flush();
			w.close();
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}
	
	@Deprecated
	protected static boolean exportTableToXLS(ApplicationExportTableData data, ByteArrayOutputStream bos) {
		try {
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("Popis");
			sheet.setDefaultColumnWidth((short)20);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			// postavi kao stil tekst, prema http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
			cellStyle.setDataFormat((short)0x31); 
			int rowIndex = 0;
			int columnIndex = 0;
			HSSFRow row = sheet.createRow((short)rowIndex);
			
			HSSFCell cell = row.createCell((short)columnIndex); columnIndex++;
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString("JMBAG"));

			cell = row.createCell((short)columnIndex); columnIndex++;
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString("Prezime, Ime"));

			for(ApplicationDefinition ad : data.getDefinitions()){
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(ad.getShortName()));
			}
			
			for(User u : data.getUsers()) {
				rowIndex++;
				columnIndex = 0;
				row = sheet.createRow((short)rowIndex);

				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(u.getJmbag()));

				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(u.getLastName() + ", " + u.getFirstName()));

				for(ApplicationDefinition ad : data.getDefinitions()){
					cell = row.createCell((short)columnIndex); columnIndex++;
					cell.setCellStyle(cellStyle);
					cell.setCellValue(new HSSFRichTextString(statusToString(data.getApplications().get(u.getId()).get(ad.getId()))));
				}
			}
			wb.write(bos);
			bos.flush();
			bos.close();
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}

	@Deprecated
	protected static String statusToString(StudentApplication application){
		if(application == null)
			return "-";
		switch(application.getStatus()){
		case ACCEPTED:
			return "A";
		case REJECTED:
			return "R";
		case NEW:
			return "N";
		default:
			return "-" ;
		}
	}
	
}
