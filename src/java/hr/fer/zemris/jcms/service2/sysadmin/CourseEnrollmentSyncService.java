package hr.fer.zemris.jcms.service2.sysadmin;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import hr.fer.zemris.jcms.beans.ext.CourseInstanceBeanExt;
import hr.fer.zemris.jcms.beans.ext.ISVUFileItemBean;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.parsers.CourseInstanceParser;
import hr.fer.zemris.jcms.parsers.ISVUFileParser;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseStudentsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class CourseEnrollmentSyncService {

	public static void prepareCourseEnrollmentSync(EntityManager em, SynchronizeCourseStudentsData data) {
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(BasicServiceSupport.getCurrentSemesterID(em));
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void syncCourseEnrollment(EntityManager em, SynchronizeCourseStudentsData data) {
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(data.getSemester());

		List<ISVUFileItemBean> items = null;
		try {
			items = ISVUFileParser.parseTabbedFormat(new StringReader(data.getText()==null ? "" : data.getText()));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Format podataka je neispravan!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		if(StringUtil.isStringBlank(data.getSemester())) {
			data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, data.getSemester());
		if(ys==null) {
			data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		AuthType authType = DAOHelperFactory.getDAOHelper().getAuthTypeDAO().getByName(em, "ferweb://https://www.fer.hr/xmlrpc/xr_auth.php");
		if(authType==null) {
			data.getMessageLogger().addErrorMessage("Ne postoji trazeni autentifikacijski tip!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		SynchronizerService.synchronizeISVUFile(em, ys, authType, items);
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return;
	}
	
	public static void prepareOpenCourseInstances(EntityManager em, SynchronizeCourseStudentsData data) {
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(BasicServiceSupport.getCurrentSemesterID(em));
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void openCourseInstances(EntityManager em, SynchronizeCourseStudentsData data) {
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(data.getSemester());

		List<CourseInstanceBeanExt> items = null;
		try {
			items = CourseInstanceParser.parseTabbedFormat(new StringReader(data.getText()==null ? "" : data.getText()));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Format podataka je neispravan!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		if(StringUtil.isStringBlank(data.getSemester())) {
			data.getMessageLogger().addErrorMessage("Semestar nije zadan!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, data.getSemester());
		if(ys==null) {
			data.getMessageLogger().addErrorMessage("Semestar ne postoji!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		SynchronizerService.synchronizeCourseInstances(em, ys.getId(), items, false);
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return;
	}
}
