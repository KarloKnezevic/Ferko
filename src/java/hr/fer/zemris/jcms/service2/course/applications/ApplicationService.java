package hr.fer.zemris.jcms.service2.course.applications;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.activities.types.ApplicationActivity;
import hr.fer.zemris.jcms.applications.ApplContainer;
import hr.fer.zemris.jcms.applications.ApplSourceCodeProducer;
import hr.fer.zemris.jcms.applications.IApplBuilderRunner;
import hr.fer.zemris.jcms.applications.model.ApplElement;
import hr.fer.zemris.jcms.applications.model.ApplNamedElement;
import hr.fer.zemris.jcms.applications.model.ApplOption;
import hr.fer.zemris.jcms.applications.model.ApplOptionSelection;
import hr.fer.zemris.jcms.applications.model.ApplSingleSelect;
import hr.fer.zemris.jcms.applications.parser.ApplCodeParser;
import hr.fer.zemris.jcms.applications.parser.ApplCodeSection;
import hr.fer.zemris.jcms.beans.ApplicationDefinitionBean;
import hr.fer.zemris.jcms.beans.StudentApplicationBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.ApplicationStatus;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.assessments.DynaCodeEngineFactory;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
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
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DateUtil;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ApplicationService {

	public static void retrieveApplicationDefinitions(EntityManager em, ApplicationMainData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
			return;
		}
		
		boolean isAdmin = JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci);
		boolean isStudent = JCMSSecurityManagerFactory.getManager().isStudentOnCourse(ci);
		data.setRenderCourseAdministration(isAdmin);
		List<ApplicationDefinition> applDefs = dh.getApplicationDAO().listDefinitions(em, ci.getId());
		data.setDefinitions(applDefs);
		
		List<StudentApplication> filledApplications = null;
		Map<Long, StudentApplication> filledApplicationsMap = new HashMap<Long, StudentApplication>();
		if(isStudent){
			filledApplications = dh.getApplicationDAO().listForUser(em, data.getCurrentUser(), ci.getId());
			for (StudentApplication st : filledApplications) {
				filledApplicationsMap.put(st.getApplicationDefinition().getId(), st);
			}
		}
		data.setFilledApplications(filledApplicationsMap);
		filterApplicationDefinitions(em, isAdmin, isStudent, applDefs, filledApplicationsMap, data.getMessageLogger(), ci, data.getCurrentUser());
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return;
	}

	public static void newApplicationDefinition(EntityManager em, ApplicationAdminEditData data) {
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return;
		
		CourseInstance ci = data.getCourseInstance();
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		ApplicationDefinitionBean bean = data.getBean();
		
		bean.setCourseInstanceID(ci.getId());
		bean.setId(null);
		bean.setName("");
		bean.setShortName("");
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);
		Date now = new Date();
		bean.setOpenFrom(sdf.format(now));
		bean.setOpenUntil(sdf.format(now));
		bean.setProgram("");
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void editApplicationDefinition(EntityManager em, ApplicationAdminEditData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return;
		
		CourseInstance ci = data.getCourseInstance();
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		ApplicationDefinition definition = null;
		ApplicationDefinitionBean bean = data.getBean();

		if(StringUtil.isStringBlank(bean.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		try {
			definition = dh.getApplicationDAO().getDefinition(em, Long.valueOf(bean.getId()));
		} catch (Exception ignorable) {}
		if (definition == null) {
			data.getMessageLogger().addErrorMessage(
					data.getMessageLogger().getText(
					"Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setDefinition(definition);
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);
		bean.setId(definition.getId().toString());
		bean.setCourseInstanceID(definition.getCourse()
				.getId().toString());
		bean.setName(definition.getName());
		bean.setShortName(definition.getShortName());
		bean.setOpenFrom(sdf.format(definition.getOpenFrom()));
		bean.setOpenUntil(sdf.format(definition.getOpenUntil()));
		bean.setProgram(definition.getProgram());
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void saveOrUpdateApplicationDefinition(EntityManager em, ApplicationAdminEditData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return;
		
		CourseInstance ci = data.getCourseInstance();
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		// ApplicationDefinition definition = null;

		ApplicationDefinitionBean bean = data.getBean();
		boolean willDoIt = true;
		if (bean.getName() == null || bean.getName().trim().equals("")) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.nameMustBeGiven"));
			willDoIt = false;
		} else if (bean.getName().length() > 100) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.nameTooLong"));
			willDoIt = false;
		}
		if (bean.getShortName() == null || bean.getShortName().trim().equals("")) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.shortNameMustBeGiven"));
			willDoIt = false;
		} else if (bean.getShortName().length() > 10) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.shortNameTooLong"));
			willDoIt = false;
		}

		Date openFrom = null;
		Date openUntil = null;
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);
		try {
			if(!StringUtil.isStringBlank(bean.getOpenFrom())) openFrom = sdf.parse(bean.getOpenFrom());
			if(!StringUtil.isStringBlank(bean.getOpenUntil())) openUntil = sdf.parse(bean.getOpenUntil());
		} catch (ParseException ignore) {
		}

		if (openFrom == null || openUntil == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongDateFormat"));
			willDoIt = false;
		} else if (!openFrom.before(openUntil)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.datesMustBeInOrder"));
			willDoIt = false;
		} else if(new Date().after(openUntil)) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Error.openUntilInPast"));
		}

		ApplicationDefinition definition = null;
		if(!StringUtil.isStringBlank(bean.getId())) {
			try {
				definition = dh.getApplicationDAO().getDefinition(em, Long.valueOf(bean.getId()));
			} catch (Exception ignorable) {}
			if (definition == null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		
		// Ovo sa sourcem kreni provjeravati samo ako je sve ostalo bilo OK; inače preskupo
		if(willDoIt) {
			if(StringUtil.isStringBlank(bean.getProgram())) {
				bean.setProgram(null);
			} else {
				if(definition==null || !StringUtil.stringEquals(bean.getProgram(), definition.getProgram())) {
					if(!checkApplicationProgram(data.getMessageLogger(), bean.getProgram())) {
						willDoIt = false;
					}
				}
			}
		}
		
		if (!willDoIt) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
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
		if(!StringUtil.stringEqualsLoosly(bean.getProgram(), definition.getProgram())) {
			definition.setProgram(bean.getProgram());
			definition.setProgramVersion(definition.getProgramVersion()+1);
		}
		if (isNew) {
			dh.getApplicationDAO().save(em, definition);
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
		} else {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		}
		data.setDefinition(definition);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void prepareStudentApplication(EntityManager em, ApplicationStudentSubmitData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		ApplicationDefinition definition = null;
		try {
			definition = dh.getApplicationDAO().getDefinition(em, Long.parseLong(data.getApplicationID()));
		} catch (Exception ignorable) {}
		
		if (definition == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setDefinition(definition);
		
		try {
			if (dh.getApplicationDAO().getApplicationForUser(em, data.getCurrentUser(), definition.getId()) != null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.alreadySubmited"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		} catch (Exception ignorable) {}

		StudentApplicationBean bean = data.getBean();

		Date now = new Date();
		if((definition.getOpenFrom()!=null && now.before(definition.getOpenFrom())) || (definition.getOpenUntil()!=null && now.after(definition.getOpenUntil()))) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.applicationNotAccessible"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if(!StringUtil.isStringBlank(definition.getProgram())) {
			ApplContainer cont = new ApplContainer();
			IApplStudentDataProvider prov = new ApplStudentDataProviderImpl(em, ci, data.getCurrentUser());
			if(!buildApplicationForUser(cont, prov, definition, data.getMessageLogger())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.applicationNotAccessible"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			bean.setElements(cont.getElements());
			int index = -1;
			for(ApplElement e : cont.getElements()) {
				index++;
				switch(e.getKind()) {
				case 4:
					bean.getMap().put("tel"+index, "");
					e.setRenderingData("tel"+index);
					break;
				case 2:
					bean.getMap().put("mel"+index, "");
					e.setRenderingData("mel"+index);
					break;
				case 1:
					ApplOptionSelection optSel = new ApplOptionSelection();
					bean.getMap().put("sel"+index, optSel);
					bean.getMap().put("sel"+index+".1", "");
					e.setRenderingData("sel"+index);
					break;
				}
			}
			Properties stateProp = new Properties();
			cont.storeState(stateProp);
			bean.setState(StringUtil.encodeString(StringUtil.getStringFromProperties(stateProp)));
		}
		bean.setUserID(data.getCurrentUser().getId());
		bean.setId(null);
		bean.setReason("");
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
	
	public static void saveStudentApplication(EntityManager em, ApplicationStudentSubmitData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		ApplicationDefinition definition = null;
		try {
			definition = dh.getApplicationDAO().getDefinition(em, Long.parseLong(data.getApplicationID()));
		} catch (Exception ignorable) {}
		
		if (definition == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setDefinition(definition);
		
		try {
			if (dh.getApplicationDAO().getApplicationForUser(em, data.getCurrentUser(), definition.getId()) != null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.alreadySubmited"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		} catch (Exception ignorable) {}

		StudentApplicationBean bean = data.getBean();

		Date now = new Date();
		if((definition.getOpenFrom()!=null && now.before(definition.getOpenFrom())) || (definition.getOpenUntil()!=null && now.after(definition.getOpenUntil()))) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.applicationNotAccessible"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		boolean errors = false;
		StudentApplication application = new StudentApplication();
		if(!StringUtil.isStringBlank(definition.getProgram())) {
			ApplContainer cont = new ApplContainer();
			IApplStudentDataProvider prov = new ApplStudentDataProviderImpl(em, ci, data.getCurrentUser());
			if(!buildAndCheckApplicationForUser(cont, prov, definition, data.getMessageLogger(), bean.getState())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.applicationNotAccessible"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			bean.setElements(cont.getElements());
			int index = -1;
			
			for(ApplElement e : cont.getElements()) {
				index++;
				switch(e.getKind()) {
				case 4: {
					String val = StringUtil.getString(bean.getMap().get("tel"+index));
					if(val==null) val="";
					val = adjustStudentJMBAGs(val);
					bean.getMap().put("tel"+index, val);
					e.setRenderingData("tel"+index);
					e.setUserData(val);
					if(!e.validate(data.getMessageLogger())) errors = true;
					break;
				}
				case 2: {
					String val = StringUtil.getString(bean.getMap().get("mel"+index));
					if(val==null) val="";
					bean.getMap().put("mel"+index, val);
					e.setRenderingData("mel"+index);
					e.setUserData(val);
					if(!e.validate(data.getMessageLogger())) errors = true;
					break;
				}
				case 1: {
					e.setRenderingData("sel"+index);
					ApplSingleSelect singleSel = (ApplSingleSelect)e;
					ApplOptionSelection optSel = new ApplOptionSelection();
					String val = StringUtil.getString(bean.getMap().get("sel"+index));
					String val1 = StringUtil.getString(bean.getMap().get("sel"+index+".1"));
					ApplOption option = singleSel.getOption(val);
					if(val!=null && option!=null) optSel.setKey(val);
					if(val1!=null && option!=null && option.isOther()) optSel.setText(val1);
					bean.getMap().put("sel"+index, optSel);
					bean.getMap().put("sel"+index+".1", "");
					e.setUserData(optSel);
					if(!e.validate(data.getMessageLogger())) errors = true;
					break;
				}
				}
			}
			if(errors) {
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
			Properties allData = new Properties();
			cont.storeState(allData);
			cont.storeUserData(allData);
			application.setDetailedData(StringUtil.getStringFromProperties(allData));
			application.setReason("");
		} else {
			if (StringUtil.isStringBlank(bean.getReason())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.reasonMustBeGiven"));
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
			application.setReason(bean.getReason());
		}

		application.setApplicationDefinition(definition);
		application.setDate(now);
		application.setStatus(ApplicationStatus.NEW);
		application.setStatusReason(null);
		application.setUser(data.getCurrentUser());

		dh.getApplicationDAO().save(em, application);
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));

		data.setApplication(application);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void getStudentApplication(EntityManager em, ApplicationStudentViewData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		StudentApplication application = dh.getApplicationDAO().get(em, data.getApplicationID());
		data.setApplication(application);
		if(application==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(!application.getUser().equals(data.getCurrentUser()) || !application.getApplicationDefinition().getCourse().equals(ci)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		ApplicationDefinition definition = application.getApplicationDefinition();
		StudentApplicationBean bean = data.getBean();

		if(!StringUtil.isStringBlank(definition.getProgram())) {
			ApplContainer cont = new ApplContainer();
			IApplStudentDataProvider prov = new ApplStudentDataProviderImpl(em, ci, data.getCurrentUser());
			if(!buildAndRestoreApplicationForUser(cont, prov, definition, application, data.getMessageLogger())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.applicationNotAccessible"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			bean.setElements(cont.getElements());
			int index = -1;
			for(ApplElement e : cont.getElements()) {
				index++;
				switch(e.getKind()) {
				case 4: {
					String val = (String)e.getUserData();
					if(val==null) val = "";
					bean.getMap().put("tel"+index, val);
					e.setRenderingData("tel"+index);
					break;
				}
				case 2: {
					String val = (String)e.getUserData();
					if(val==null) val = "";
					bean.getMap().put("mel"+index, val);
					e.setRenderingData("mel"+index);
					break;
				}
				case 1: {
					ApplOptionSelection optSel = (ApplOptionSelection)e.getUserData();
					if(optSel==null) optSel = new ApplOptionSelection();
					bean.getMap().put("sel"+index, optSel);
					bean.getMap().put("sel"+index+".1", "");
					e.setRenderingData("sel"+index);
					break;
				}
				}
			}
		} else {
			bean.setReason(application.getReason());
		}
		bean.setUserID(application.getUser().getId());
		bean.setId(application.getId());
		bean.setDate(application.getDate());
		bean.setDefinition(definition.getName());
		bean.setStatus(application.getStatus().name());
		bean.setStatusReason(application.getStatusReason());
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void viewStudentApplicationForApproval(EntityManager em, ApplicationAdminAproveData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		User user = dh.getUserDAO().getUserById(em, data.getStudentID());
		if(user==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noUser"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setStudent(user);

		List<StudentApplication> list = null;
		try {
			list = dh.getApplicationDAO().listForUser(em, user, data.getCourseInstanceID());
		} catch(Exception ignorable) {
		}
		boolean foundFromDefinitionID = false;
		IApplStudentDataProvider prov = null;
		StudentApplicationBean moveAsFirstBean = null;
		for(StudentApplication sa : list){
			StudentApplicationBean bean = new StudentApplicationBean();
			if(data.getFromDefinitionID()!=null && sa.getApplicationDefinition().getId().equals(data.getFromDefinitionID())) {
				foundFromDefinitionID = true;
				moveAsFirstBean = bean;
			}
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
			if(!StringUtil.isStringBlank(sa.getApplicationDefinition().getProgram())) {
				ApplContainer cont = new ApplContainer();
				if(prov==null) {
					prov = new ApplStudentDataProviderImpl(em, ci, user);
				}
				if(!buildAndRestoreApplicationForUser(cont, prov, sa.getApplicationDefinition(), sa, data.getMessageLogger())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.applicationNotAccessible"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				bean.setElements(cont.getElements());
				int index = -1;
				for(ApplElement e : cont.getElements()) {
					index++;
					switch(e.getKind()) {
					case 4: {
						String val = (String)e.getUserData();
						if(val==null) val = "";
						bean.getMap().put("tel"+index, val);
						e.setRenderingData("tel"+index);
						break;
					}
					case 2: {
						String val = (String)e.getUserData();
						if(val==null) val = "";
						bean.getMap().put("mel"+index, val);
						e.setRenderingData("mel"+index);
						break;
					}
					case 1: {
						ApplOptionSelection optSel = (ApplOptionSelection)e.getUserData();
						if(optSel==null) optSel = new ApplOptionSelection();
						bean.getMap().put("sel"+index, optSel);
						bean.getMap().put("sel"+index+".1", "");
						e.setRenderingData("sel"+index);
						break;
					}
					}
				}
			}
			if(moveAsFirstBean==bean) {
				data.getBeans().add(0, bean);
			} else {
				data.getBeans().add(bean);
			}
		}
		if(data.getFromDefinitionID()!=null && !foundFromDefinitionID) {
			data.setFromDefinitionID(null);
		}
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
	
	public static void saveStudentApplicationApproval(EntityManager em, ApplicationAdminAproveData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		User user = dh.getUserDAO().getUserById(em, data.getStudentID());
		if(user==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noUser"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setStudent(user);

		List<StudentApplication> list = null;
		try {
			list = dh.getApplicationDAO().listForUser(em, user, data.getCourseInstanceID());
		} catch(Exception ignorable) {}
		
		Map<Long, StudentApplication> applicationMap = new HashMap<Long, StudentApplication>();
		for (StudentApplication apl : list) {
			applicationMap.put(apl.getId(), apl);
		}

		Date now = new Date();
		List<ApplicationActivity> activities = null;
		for(StudentApplicationBean bean : data.getBeans()){
			boolean updated = false;
			StudentApplication sa = applicationMap.get(bean.getId());
			if(!sa.getStatus().name().equals(bean.getStatus())) {
				sa.setStatus(ApplicationStatus.valueOf(bean.getStatus()));
				updated = true;
			}
			String trimmedStatusReason = bean.getStatusReason()==null ? "" : bean.getStatusReason().trim();
			if(!StringUtil.stringEqualsLoosly(trimmedStatusReason, sa.getStatusReason())) {
				sa.setStatusReason(trimmedStatusReason);
				updated = true;
			}
			if(sa.getStatus()==ApplicationStatus.NEW) {
				sa.setStatusReason(null);
			}
			if(updated) {
				if(activities==null) activities = new ArrayList<ApplicationActivity>();
				activities.add(new ApplicationActivity(now, ci.getId(), user.getId(), sa.getId()));
			}
		}
		
		if(activities!=null) {
			for(ApplicationActivity activity : activities) {
				JCMSSettings.getSettings().getActivityReporter().addActivity(activity);
			}
		}
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void listStudentsOnApplication(EntityManager em, ApplicationListStudentsData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		ApplicationDefinition definition = dh.getApplicationDAO().getDefinition(em, data.getDefinitionID());
		if (definition == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.invalidParameters"));
			return;
		}
		data.setDefinition(definition);
		
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
			return;
		}
		
		List<StudentApplication> applications = dh.getApplicationDAO()
			.listForDefinition(em, data.getCourseInstanceID(), data.getDefinitionID());
		
		List<User> users = new ArrayList<User>();
		Map<Long, StudentApplication> map = new HashMap<Long, StudentApplication>();

		for(StudentApplication sa : applications){
			users.add(sa.getUser());
			map.put(sa.getUser().getId(), sa);
		}

		Collections.sort(users, StringUtil.USER_COMPARATOR);
		
		data.setUsers(users);
		data.setApplications(map);
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void fetchApplicationsMatrix(EntityManager em, ApplicationAdminTableData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		data.setDefinitions(dh.getApplicationDAO().listDefinitions(em, ci.getId()));
		List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
		Collections.sort(users, StringUtil.USER_COMPARATOR);
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

		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void exportStudentApplicationsForDefinition(EntityManager em, ApplicationExportListData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if( !"csv".equals(data.getFormat()) && !"xls".equals(data.getFormat())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		ApplicationDefinition definition = dh.getApplicationDAO().getDefinition(em, data.getDefinitionID());
		if (definition == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger()
					.getText("Error.invalidParameters"));
			return;
		}
		data.setDefinition(definition);
		
		List<StudentApplication> applications = dh.getApplicationDAO()
		.listForDefinition(em, data.getCourseInstanceID(), data.getDefinitionID());
	
		List<User> users = new ArrayList<User>();
		Map<Long, StudentApplication> map = new HashMap<Long, StudentApplication>();
	
		for(StudentApplication sa : applications){
			users.add(sa.getUser());
			map.put(sa.getUser().getId(), sa);
		}
		data.setUsers(users);
		data.setApplications(map);

		ApplContainer cont = null;
		if(!StringUtil.isStringBlank(definition.getProgram())) {
			cont = new ApplContainer();
			Class<?> c = DynaCodeEngineFactory.getEngine().classForProgram("P", definition.getId(), definition.getProgram(), definition.getProgramVersion());
			if(c==null) {
				data.getMessageLogger().addErrorMessage("Postoji problem s prijavom \""+definition.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta.");
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			try {
				cont.setDefinable(false); cont.setExecutable(false);
				Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
				IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, new EmptyStudentDataProviderImpl());
				cont.setDefinable(true);
				builderRunner.buildApplication();
			} catch(Throwable ex) {
				data.getMessageLogger().addErrorMessage("Postoji problem s prijavom \""+definition.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta. Poruka je: "+ex.getMessage());
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
		if(data.getFormat().equals("csv")) {
			if(!exportListToCSV(data, bos, cont)) {
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			data.setMimeType("text/csv");
			data.setFileName("prijave_"+data.getDefinition().getShortName()+".csv");
		} else if(data.getFormat().equals("xls")) {
			if(!exportListToXLS(data, bos, cont)) {
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			data.setMimeType("application/vnd.ms-excel");
			data.setFileName("prijave_"+data.getDefinition().getShortName()+".xls");
		}
	
		byte[] bytes = bos.toByteArray();
		data.setStream(new ByteArrayInputStream(bytes));
		data.setLength(bytes.length);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void exportApplicationsMatrixOnCourse(EntityManager em, ApplicationExportTableData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		CourseInstance ci = data.getCourseInstance();
		
		if (!JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(ci)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if( !"csv".equals(data.getFormat()) && !"xls".equals(data.getFormat())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		data.setDefinitions(dh.getApplicationDAO().listDefinitions(em, ci.getId()));
		List<User> users = dh.getUserDAO().listUsersOnCourseInstance(em, ci.getId());
		Collections.sort(users, StringUtil.USER_COMPARATOR);
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
		if(data.getFormat().equals("csv")) {
			if(!exportTableToCSV(data, bos)) {
				data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
				return;
			}
			data.setMimeType("text/csv");
			data.setFileName("prijave.csv");
		} else if(data.getFormat().equals("xls")) {
			if(!exportTableToXLS(data, bos)) {
				data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
				return;
			}
			data.setMimeType("application/vnd.ms-excel");
			data.setFileName("prijave.xls");
		}

		byte[] bytes = bos.toByteArray();
		data.setStream(new ByteArrayInputStream(bytes));
		data.setLength(bytes.length);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	protected static boolean exportListToCSV(ApplicationExportListData data, ByteArrayOutputStream bos, ApplContainer cont) {
		try {
			OutputStreamWriter w = new OutputStreamWriter(bos, "UTF-8");
			w.append("\"").append(data.getMessageLogger().getText("forms.jmbag"))
			 .append("\",\"").append(data.getMessageLogger().getText("forms.lastName"))
			 .append(", ").append(data.getMessageLogger().getText("forms.firstName"))
			 .append("\"");
			if(cont==null) {
				w.append(",\"Upisani razlog\"");
			} else {
				for(ApplElement e : cont.getElements()) {
					switch(e.getKind()) {
					case 1:
						w.append(",\"").append(((ApplNamedElement)e).getName()).append("\"");
						break;
					case 2:
						w.append(",\"").append(((ApplNamedElement)e).getName()).append("\"");
						break;
					case 4:
						w.append(",\"").append(((ApplNamedElement)e).getName()).append("\"");
						break;
					}
				}
			}
			w.append(",\"Status\"");
			w.append("\r\n");
			
			for(User u : data.getUsers()) {
				w.append("\"").append(u.getJmbag())
				 .append("\",\"").append(u.getLastName())
				 .append(", ").append(u.getFirstName())
				 .append("\"");
				
				if(cont==null) {
					w.append(",\"").append(csvSafeField(data.getApplications().get(u.getId()).getReason())).append("\"");
				} else {
					StudentApplication sa = data.getApplications().get(u.getId());
					cont.loadUserData(StringUtil.getPropertiesFromString(sa.getDetailedData()));
					for(ApplElement e : cont.getElements()) {
						switch(e.getKind()) {
						case 1: {
							ApplOptionSelection sel = (ApplOptionSelection)e.getUserData();
							if(sel==null) sel = new ApplOptionSelection();
							ApplOption opt = ((ApplSingleSelect)e).getOption(sel.getKey());
							String s;
							if(opt==null) {
								s = "";
							} else if(opt.isOther()) {
								s = opt.getKey()+": "+StringUtil.denullify(sel.getText());
							} else {
								s = opt.getKey();
							}
							w.append(",\"").append(csvSafeField(s)).append("\"");
							break;
						}
						case 2: {
							String text = (String)e.getUserData();
							if(text==null) text = "";
							w.append(",\"").append(csvSafeField(text)).append("\"");
							break;
						}
						case 4: {
							String text = (String)e.getUserData();
							if(text==null) text = "";
							w.append(",\"").append(csvSafeField(text)).append("\"");
							break;
						}
						}
					}
				}
				w.append(",\"").append(statusToString(data.getApplications().get(u.getId()))).append("\"");
				w.append("\r\n");
			}
			w.flush();
			w.close();
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}
	
	private static String csvSafeField(String reason) {
		if(reason==null || reason.isEmpty()) return "";
		char[] chs = reason.toCharArray();
		StringBuilder sb = null;
		int nextToCopy = 0;
		for(int i = 0; i < chs.length; i++) {
			char c = chs[i];
			if(c=='\"') {
				if(i>nextToCopy) {
					if(sb==null) sb = new StringBuilder((int)(chs.length*1.1));
					sb.append(chs, nextToCopy, i-nextToCopy);
				} else {
					if(sb==null) sb = new StringBuilder((int)(chs.length*1.1));
				}
				nextToCopy = i+1;
				sb.append("\"\"");
			}
		}
		if(nextToCopy==0) return reason;
		if(nextToCopy < chs.length-1) {
			if(sb==null) sb = new StringBuilder((int)(chs.length*1.1));
			sb.append(chs, nextToCopy, chs.length-nextToCopy);
		}
		return sb.toString();
	}

	protected static boolean exportListToXLS(ApplicationExportListData data, ByteArrayOutputStream bos, ApplContainer cont) {
	try {
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("Popis");
			sheet.setDefaultColumnWidth((short)20);
			HSSFCellStyle cellStyle = wb.createCellStyle();
			// postavi kao stil tekst, prema http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
			cellStyle.setDataFormat((short)0x31); 
			int rowIndex = -1;
			// Zaglavlje:
			{
				rowIndex++;
				int columnIndex = 0;
				HSSFRow row = sheet.createRow((short)rowIndex);
				
				HSSFCell cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString("JMBAG"));
				
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString("Prezime, ime"));
				if(cont==null) {
					cell = row.createCell((short)columnIndex); columnIndex++;
					cell.setCellStyle(cellStyle);
					cell.setCellValue(new HSSFRichTextString("Upisani razlog"));
				} else {
					for(ApplElement e : cont.getElements()) {
						switch(e.getKind()) {
						case 1:
							cell = row.createCell((short)columnIndex); columnIndex++;
							cell.setCellStyle(cellStyle);
							cell.setCellValue(new HSSFRichTextString(((ApplNamedElement)e).getName()));
							break;
						case 2:
							cell = row.createCell((short)columnIndex); columnIndex++;
							cell.setCellStyle(cellStyle);
							cell.setCellValue(new HSSFRichTextString(((ApplNamedElement)e).getName()));
							break;
						case 4:
							cell = row.createCell((short)columnIndex); columnIndex++;
							cell.setCellStyle(cellStyle);
							cell.setCellValue(new HSSFRichTextString(((ApplNamedElement)e).getName()));
							break;
						}
					}
				}
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString("Status"));
			}
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
				if(cont==null) {
					cell = row.createCell((short)columnIndex); columnIndex++;
					cell.setCellStyle(cellStyle);
					cell.setCellValue(new HSSFRichTextString(data.getApplications().get(u.getId()).getReason()));
				} else {
					StudentApplication sa = data.getApplications().get(u.getId());
					cont.loadUserData(StringUtil.getPropertiesFromString(sa.getDetailedData()));
					for(ApplElement e : cont.getElements()) {
						switch(e.getKind()) {
						case 1: {
							ApplOptionSelection sel = (ApplOptionSelection)e.getUserData();
							if(sel==null) sel = new ApplOptionSelection();
							cell = row.createCell((short)columnIndex); columnIndex++;
							cell.setCellStyle(cellStyle);
							ApplOption opt = ((ApplSingleSelect)e).getOption(sel.getKey());
							String s;
							if(opt==null) {
								s = "";
							} else if(opt.isOther()) {
								s = opt.getKey()+": "+StringUtil.denullify(sel.getText());
							} else {
								s = opt.getKey();
							}
							cell.setCellValue(new HSSFRichTextString(s));
							break;
						}
						case 2: {
							String text = (String)e.getUserData();
							if(text==null) text = "";
							cell = row.createCell((short)columnIndex); columnIndex++;
							cell.setCellStyle(cellStyle);
							cell.setCellValue(new HSSFRichTextString(text));
							break;
						}
						case 4: {
							String text = (String)e.getUserData();
							if(text==null) text = "";
							cell = row.createCell((short)columnIndex); columnIndex++;
							cell.setCellStyle(cellStyle);
							cell.setCellValue(new HSSFRichTextString(text));
							break;
						}
						}
					}
				}
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

	private static String statusToString(StudentApplication application){
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

	private static boolean exportTableToCSV(ApplicationExportTableData data, ByteArrayOutputStream bos) {
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
	
	private static boolean exportTableToXLS(ApplicationExportTableData data, ByteArrayOutputStream bos) {
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
	
	private static String adjustStudentJMBAGs(String val) {
		if(val==null) return "";
		StringBuilder sb = new StringBuilder(val.length());
		StringTokenizer stok = new StringTokenizer(val, " \r\n\t,#:", false);
		while(stok.hasMoreTokens()) {
			String el = stok.nextToken();
			el = el.trim();
			if(el.isEmpty()) continue;
			if(sb.length()!=0) sb.append("\n");
			sb.append(el);
		}
		return sb.toString();
	}

	private static boolean checkApplicationProgram(IMessageLogger messageLogger, String program) {
		try {
			ApplCodeParser parser = new ApplCodeParser(program);
			List<ApplCodeSection> sections = parser.getSections();
			String source = ApplSourceCodeProducer.getSource("DynaClassTmpP_tmp", "studtest2.dynamic", sections);
			Class<?> c = DynaCodeEngineFactory.getEngine().oneTimeCompile(messageLogger, "studtest2.dynamic", "DynaClassTmpP_tmp", source);
			if(c==null) {
				return false;
			}
			ApplContainer cont = new ApplContainer();
			cont.setDefinable(false); cont.setExecutable(false);
			IApplStudentDataProvider prov = new ApplStudentDataProviderImpl(null, null, null);
			Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
			IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, prov);
			cont.setDefinable(true);
			builderRunner.buildApplication();
			// Sada provjeri koje ima elemente a koje filter sekcije
			Set<String> elemNames = new HashSet<String>();
			for(ApplElement e : cont.getElements()) {
				if(e instanceof ApplNamedElement) {
					elemNames.add(((ApplNamedElement)e).getName());
				}
			}
			boolean errors = false;
			for(ApplCodeSection s : sections) {
				if(!s.getSectionName().equals("filter")) continue;
				if(s.getArguments().isEmpty()) continue;
				if(!elemNames.contains(s.getArguments().get(0))) {
					messageLogger.addErrorMessage("Definiran je filter za element "+s.getArguments().get(0)+", međutim, tog elementa nema u prijavi!");
					errors = true;
				}
			}
			return !errors;
		} catch(Throwable t) {
			messageLogger.addErrorMessage(t.getMessage());
			return false;
		}
	}

	private static void filterApplicationDefinitions(EntityManager em, boolean isAdmin, boolean isStudent, List<ApplicationDefinition> applDefs, Map<Long, StudentApplication> filledApplicationsMap, IMessageLogger messageLogger, CourseInstance ci, User user) {
		// Koje su prijave vidljive?
		// Ako je korisnik admin: sve
		if(isAdmin) return;
		
		Date now = new Date();
		
		Iterator<ApplicationDefinition> it = applDefs.iterator();
		while(it.hasNext()) {
			ApplicationDefinition def = it.next();
			// Ako je korisnik popunio prijavu, vidi je...
			if(filledApplicationsMap.containsKey(def.getId())) continue;
			// Ako vrijeme za prikaz još nije nastupilo, makni je:
			if(def.getOpenFrom()!=null && now.before(def.getOpenFrom())) {
				it.remove();
				continue;
			}
			// Ako je vrijeme za prikaz prošlo, makni je:
			if(def.getOpenUntil()!=null && now.after(def.getOpenUntil())) {
				it.remove();
				continue;
			}
			// Inače smo za sada OK. Ovdje sada napraviti ono "složeno" filtriranje
			// TODO: "složeno" filtriranje prijave
			// pa izbaci ako treba izbaciti
			if(!StringUtil.isStringBlank(def.getProgram())) {
				Class<?> c = DynaCodeEngineFactory.getEngine().classForProgram("P", def.getId(), def.getProgram(), def.getProgramVersion());
				if(c==null) {
					messageLogger.addErrorMessage("Postoji problem s prijavom \""+def.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta.");
					it.remove();
					continue;
				}
				try {
					ApplContainer cont = new ApplContainer();
					cont.setDefinable(false); cont.setExecutable(false);
					IApplStudentDataProvider prov = new ApplStudentDataProviderImpl(em, ci, user);
					Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
					IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, prov);
					cont.setDefinable(true);
					builderRunner.buildApplication();
					cont.setExecutable(true);
					builderRunner.applyGlobalFilter();
					if(!builderRunner.isEnabled()) {
						it.remove();
						continue;
					}
					builderRunner.applyFilters();
					if(!builderRunner.isEnabled()) {
						it.remove();
						continue;
					}
				} catch(Throwable ex) {
					messageLogger.addErrorMessage("Postoji problem s prijavom \""+def.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta. Poruka je: "+ex.getMessage());
					it.remove();
					continue;
				}
			}
		}
	}

	private static boolean buildApplicationForUser(ApplContainer cont, IApplStudentDataProvider prov, ApplicationDefinition def, IMessageLogger messageLogger) {
		Class<?> c = DynaCodeEngineFactory.getEngine().classForProgram("P", def.getId(), def.getProgram(), def.getProgramVersion());
		if(c==null) {
			messageLogger.addErrorMessage("Postoji problem s prijavom \""+def.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta.");
			return false;
		}
		try {
			cont.setDefinable(false); cont.setExecutable(false);
			Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
			IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, prov);
			cont.setDefinable(true);
			builderRunner.buildApplication();
			cont.setExecutable(true);
			builderRunner.applyGlobalFilter();
			if(!builderRunner.isEnabled()) {
				return false;
			}
			builderRunner.applyFilters();
			if(!builderRunner.isEnabled()) {
				return false;
			}
		} catch(Throwable ex) {
			messageLogger.addErrorMessage("Postoji problem s prijavom \""+def.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta. Poruka je: "+ex.getMessage());
			return false;
		}
		return true;
	}
	
	private static boolean buildAndCheckApplicationForUser(ApplContainer cont, IApplStudentDataProvider prov, ApplicationDefinition def, IMessageLogger messageLogger, String encodedState) {
		Properties stateProps = null;
		try {
			stateProps = StringUtil.getPropertiesFromString(StringUtil.decodeString(encodedState));
		} catch(Exception ex) {
			messageLogger.addErrorMessage(messageLogger.getText("Error.invalidParameters"));
			return false;
		}
		Class<?> c = DynaCodeEngineFactory.getEngine().classForProgram("P", def.getId(), def.getProgram(), def.getProgramVersion());
		if(c==null) {
			messageLogger.addErrorMessage("Postoji problem s prijavom \""+def.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta.");
			return false;
		}
		try {
			cont.setDefinable(false); cont.setExecutable(false);
			Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
			IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, prov);
			cont.setDefinable(true);
			builderRunner.buildApplication();
			cont.setExecutable(true);
			builderRunner.applyGlobalFilter();
			if(!builderRunner.isEnabled()) {
				return false;
			}
			builderRunner.applyFilters();
			if(!builderRunner.isEnabled()) {
				return false;
			}
			
			ApplContainer cont2 = new ApplContainer();
			cont2.setDefinable(false); cont2.setExecutable(false);
			IApplBuilderRunner builderRunner2 = (IApplBuilderRunner)constr.newInstance(cont2, prov);
			cont2.setDefinable(true);
			builderRunner2.buildApplication();
			cont2.loadState(stateProps);
			
			if(!cont2.isStructurallyEquals(cont)) {
				messageLogger.addErrorMessage(messageLogger.getText("Error.applicationStateChanged"));
				return false;
			}
		} catch(Throwable ex) {
			messageLogger.addErrorMessage("Postoji problem s prijavom \""+def.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta. Poruka je: "+ex.getMessage());
			return false;
		}
		return true;
	}

	private static boolean buildAndRestoreApplicationForUser(ApplContainer cont, IApplStudentDataProvider prov, ApplicationDefinition def, StudentApplication application, IMessageLogger messageLogger) {
		Properties stateProps = null;
		try {
			stateProps = StringUtil.getPropertiesFromString(application.getDetailedData());
		} catch(Exception ex) {
			messageLogger.addErrorMessage(messageLogger.getText("Error.invalidParameters"));
			return false;
		}
		Class<?> c = DynaCodeEngineFactory.getEngine().classForProgram("P", def.getId(), def.getProgram(), def.getProgramVersion());
		if(c==null) {
			messageLogger.addErrorMessage("Postoji problem s prijavom \""+def.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta.");
			return false;
		}
		try {
			cont.setDefinable(false); cont.setExecutable(false);
			Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
			IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, prov);
			cont.setDefinable(true);
			builderRunner.buildApplication();
			cont.setExecutable(true);
			cont.loadState(stateProps);
			cont.loadUserData(stateProps);
		} catch(Throwable ex) {
			messageLogger.addErrorMessage("Postoji problem s prijavom \""+def.getName()+"\". Ona je onemogućena dok se problem ne ispravi. Molim upozorite nadležnog asistenta. Poruka je: "+ex.getMessage());
			return false;
		}
		return true;
	}

}
