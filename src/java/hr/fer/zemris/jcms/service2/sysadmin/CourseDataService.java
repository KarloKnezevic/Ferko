package hr.fer.zemris.jcms.service2.sysadmin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseInstanceIsvuData;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.CourseInstanceUtil;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseIsvuData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import javax.persistence.EntityManager;

public class CourseDataService {

	public static void prepareImportISVUZip(EntityManager em, SynchronizeCourseIsvuData data) {
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		List<YearSemester> list = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().list(em);
		data.setAllYearSemesters(list);
		data.setCurrentSemesterID(BasicServiceSupport.getCurrentSemesterID(em));
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void importISVUZip(EntityManager em, SynchronizeCourseIsvuData data) {
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(data.getArchive()==null || !data.getArchive().canRead()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noFileAttached"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		if(synchronizeCourseIsvuData(em, data.getSemester(), data.getArchive())) {
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
		} else {
			data.getMessageLogger().addErrorMessage("Neuspjeh.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
		}
	}

	private static boolean synchronizeCourseIsvuData(EntityManager em, String ayear, File f) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(f);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		try {
			List<CourseInstance>  ciList = dh.getCourseInstanceDAO().findForSemester(em, ayear);
			Map<String,CourseInstance> ciMap = CourseInstanceUtil.mapCourseInstanceByISVUCode(ciList);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry ze = entries.nextElement();
				if(ze.isDirectory()) continue;
				String name = ze.getName();
				String id = ayear+"/"+name;
				CourseInstance ci = ciMap.get(name);
				if(ci==null) continue;
				String data = null;
				try {
					InputStream is = new BufferedInputStream(zipFile.getInputStream(ze));
					data = TextService.inputStreamToString(is, "UTF-8");
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				if(ci.getIsvuData()==null) {
					CourseInstanceIsvuData isvuData = new CourseInstanceIsvuData();
					isvuData.setId(id);
					isvuData.setData(data);
					ci.setIsvuData(isvuData);
					dh.getCourseInstanceDAO().save(em, isvuData);
				} else {
					ci.getIsvuData().setData(data);
				}
			}
		} catch(Exception ex) {
			return false;
		} finally {
			try { zipFile.close(); } catch(Exception ignorable) {}
		}
		return true;
	}

}
