package hr.fer.zemris.jcms.service2.course.assessments;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.ext.AssessmentFileUploadBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFile;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.parsers.AssessmentFilesUploadParser;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.AssessmentFileUtil;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.web.actions.data.AssessmentFilesUploadData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.FileUtil;

import javax.persistence.EntityManager;

public class AssessmentsUploadService {

	/* ====================================================================================
	 * 
	 * UPLOAD DATOTEKA NA PROVJERU - glavne metode
	 * 
	 * ====================================================================================
	 */
	
	public static void uploadZippedFilesOnAssessment(EntityManager em, AssessmentFilesUploadData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		// Dozvole
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		List<AssessmentFileUploadBean> beanList = extractAssessmentFileUploadBeans(data.getMessageLogger(), data.getArchive());
		if(beanList==null) {
			data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
		Map<String,User> mapUserByJMBAG = UserUtil.mapUserByJmbag(courseUsers);
		boolean errors = false;
		for(AssessmentFileUploadBean b : beanList) {
			if(b.getJmbag().length()!=0 && !mapUserByJMBAG.containsKey(b.getJmbag())) {
				data.getMessageLogger().addErrorMessage("Student "+b.getJmbag()+" ne postoji.");
				errors = true;
			}
		}
		if(errors) {
			data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
			return;
		}
		List<AssessmentFile> files = dh.getAssessmentDAO().listAssessmentFiles(em, data.getAssessment());
		Map<String, List<AssessmentFile>> fmap = AssessmentFileUtil.mapByJMBAG(files);
		if(!storeAssessmentFilesToDisk(em, dh, data.getMessageLogger(), data.getArchive(), data.getAssessment(), mapUserByJMBAG, fmap, beanList)) {
			data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
			return;
		}
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/* ====================================================================================
	 * 
	 * UPLOAD DATOTEKA NA PROVJERU - pomoćne metode
	 * 
	 * ====================================================================================
	 */
	
	static List<AssessmentFileUploadBean> extractAssessmentFileUploadBeans(
			IMessageLogger messageLogger, File archive) {
		if(archive==null) {
			messageLogger.addErrorMessage("Nije primljena datoteka!");
			return null;
		}
		List<AssessmentFileUploadBean> result = null;
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(archive);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry ze = entries.nextElement();
				if(!ze.getName().equals("opisnik.txt")) {
					continue;
				}
				result = AssessmentFilesUploadParser.parseTabbedFormat(zipFile.getInputStream(ze));
				break;
			}
			if(result==null) {
				messageLogger.addErrorMessage("Datoteka opisnik.txt nije prisutna u arhivi.");
			}
		} catch(Exception ex) {
			messageLogger.addErrorMessage(ex.getMessage());
		} finally {
			try { if(zipFile != null) zipFile.close(); } catch(Exception ignorable) {}
		}
		return result;
	}

	protected static boolean storeAssessmentFilesToDisk(
			EntityManager em, DAOHelper dh, IMessageLogger messageLogger, File archive, Assessment assessment, Map<String,User> mapUserByJMBAG,
			Map<String, List<AssessmentFile>> fmap,
			List<AssessmentFileUploadBean> beanList) {
		ZipFile zipFile = null;
		Map<String,AssessmentFileUploadBean> map = new HashMap<String, AssessmentFileUploadBean>(beanList.size());
		for(AssessmentFileUploadBean b : beanList) {
			map.put(b.getFileName(), b);
		}
		boolean anyError = false;
		try {
			File dir = JCMSSettings.getSettings().getFilesRootDir();
			dir = new File(dir, "A-"+assessment.getId());
			if(!dir.exists()) dir.mkdir();
			zipFile = new ZipFile(archive);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry ze = entries.nextElement();
				if(ze.getName().equals("opisnik.txt")) {
					continue;
				}
				if(ze.isDirectory()) continue;
				String name = ze.getName();
				AssessmentFileUploadBean b = map.get(name);
				if(b==null) {
					// Ovo nije opisano u datoteci.
					messageLogger.addWarningMessage("Preskočio sam: "+name);
					continue;
				}
				String key = b.getJmbag()!=null && b.getJmbag().length()>0 ? b.getJmbag() : null;
				AssessmentFile afile = AssessmentFileUtil.findFor(fmap, key, b.getDescriptor());
				if(afile==null) {
					afile = new AssessmentFile();
					afile.setAssessment(assessment);
					afile.setDescriptor(b.getDescriptor());
					String ext = FileUtil.findExtension(name);
					afile.setExtension(ext);
					afile.setMimeType(FileUtil.findMimeTypeForExtension(ext));
					if(b.getJmbag().length()>0) {
						afile.setUser(mapUserByJMBAG.get(b.getJmbag()));
					}
					if(b.getDescription().length()>0) {
						afile.setDescription(b.getDescription());
					}
					afile.setOriginalFileName(name);
					dh.getAssessmentDAO().save(em, afile);
				} else {
					String ext = FileUtil.findExtension(name);
					afile.setExtension(ext);
					afile.setOriginalFileName(name);
					afile.setMimeType(FileUtil.findMimeTypeForExtension(ext));
					if(b.getDescription().length()>0) {
						afile.setDescription(b.getDescription());
					} else {
						afile.setDescription(null);
					}
				}
				InputStream is = null;
				OutputStream os = null;
				try {
					is = zipFile.getInputStream(ze);
					os = new BufferedOutputStream(new FileOutputStream(new File(dir,afile.getId().toString())));
					FileUtil.copyFile(is, os);
				} catch(Exception ex) {
					anyError = true;
					messageLogger.addErrorMessage(ex.getMessage());
				} finally {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					try { if(is!=null) is.close(); } catch(Exception ignorable) {}
				}
			}
		} catch(Exception ex) {
			anyError = true;
			messageLogger.addErrorMessage(ex.getMessage());
		} finally {
			try { if(zipFile != null) zipFile.close(); } catch(Exception ignorable) {}
		}
		return !anyError;
	}

}
