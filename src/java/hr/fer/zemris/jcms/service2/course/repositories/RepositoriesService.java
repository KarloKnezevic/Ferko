package hr.fer.zemris.jcms.service2.course.repositories;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.RepositoryFileBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.RepositoryCategory;
import hr.fer.zemris.jcms.model.RepositoryCourse;
import hr.fer.zemris.jcms.model.RepositoryFile;
import hr.fer.zemris.jcms.model.RepositoryFilePage;
import hr.fer.zemris.jcms.model.RepositoryFilePageComment;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.RepositoryFileStatus;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.RepositoryData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.InputStreamWrapper;
import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;

public class RepositoriesService {

	
	public static void fetchMain(EntityManager em, RepositoryData data){
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		
		//da li je to administrator
		data.setAdmin(JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance()));
		
		//da li je to nastavno osoblje
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		data.setStaffMember(dh.getCourseInstanceDAO().isCourseStaffMember(em, data.getCourseInstance(), data.getCurrentUser()));
		
		checkRepositoryRootCourse(em, data.getCourseInstance().getCourse());
		RepositoryCourse repository = data.getCourseInstance().getCourse().getRepository();

		List<RepositoryCategory> allCategories = new ArrayList<RepositoryCategory>();		
		List<RepositoryFile> allFiles = new ArrayList<RepositoryFile>();
		
		data.setCategories(allCategories);
		data.setFiles(allFiles);
		
		for(RepositoryCategory cat : repository.getRootCategories()){
			
			recursiveList(cat, allCategories, allFiles);
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	
	public static void prepareNewCategory(EntityManager em, RepositoryData data){
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserManageRepository(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	
	public static void addNewCategory(EntityManager em, RepositoryData data){
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserManageRepository(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		RepositoryCategory parentCategory = data.getCategoryID()==null ? null : dh.getRepositoryFileDAO().getRepositoryCategory(em, data.getCategoryID());
		if (parentCategory==null && data.getCategoryID()!=null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		if(!saveRepositoryCategory(em, dh, data.getMessageLogger(),data.getCourseInstance().getCourse().getRepository(),parentCategory,data.getCategoryName())) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	
	public static void getRepositoryFile(EntityManager em, RepositoryData data){
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();	
		
		RepositoryFile fileToDownload = dh.getRepositoryFileDAO().getFile(em, data.getFileID() );
		
		String isvuCode = fileToDownload.getRepositoryCourse().getCourse().getIsvuCode();
		File root = getRepositoryCourseFile(isvuCode, data.getFileID());
		
		try {
			InputStreamWrapper wrapper = new InputStreamWrapper(new BufferedInputStream(new FileInputStream(root)),fileToDownload.getRealName(),root.length(),fileToDownload.getMimeType());
			data.setStream(wrapper);
		} catch (FileNotFoundException e) {
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	
	public static void deleteRepositoryCategory(EntityManager em, RepositoryData data){
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		

		if(!JCMSSecurityManagerFactory.getManager().canUserManageRepository(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();	
				
				//izvuci kategoriju koju misliš obrisati
				RepositoryCategory categoryToDelete = data.getCategoryID()==null ? null : dh.getRepositoryFileDAO().getRepositoryCategory(em, data.getCategoryID());
				
				if (categoryToDelete==null && data.getCategoryID()!=null) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				
				//provjeri postoje li podkategorije ove kategorije i/ili ima li datoteka u ovoj kategoriji
				if(!(categoryToDelete.getSubCategories().isEmpty()) || !(categoryToDelete.getFiles().isEmpty())){
					data.setResult(AbstractActionData.RESULT_REVIEW_ACTION);
					return;
				}
				
				//ovdje se odvija samo brisanje kategorije ili datoteke.
				if(!removeRepositoryCategory(em, dh, data.getMessageLogger(), data.getCurrentUser(), data.getCourseInstance().getCourse().getRepository(), categoryToDelete)) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
		
	
	public static void deleteRepositoryFile(EntityManager em, RepositoryData data, final String deleteAction){
			// Dohvat kolegija
			if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
			
			// Dozvole
			if(!JCMSSecurityManagerFactory.getManager().canUserManageRepository(data.getCourseInstance())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}

				DAOHelper dh = DAOHelperFactory.getDAOHelper();		
				
				/*
				 * Osim brisanja datoteke, potrebno je obrisati i vezu između datoteke
				 * i kategorije u RepositoryCategory. Tu kategoriju možemo dohvatiti direktno
				 * iz RepositoryFile one datoteke koju želimo obrisati. Zatim potrebno je 
				 * postaviti atribut nextVersion prethodne verzije datoteke na null. Također, potrebno
				 * je u RepositoryCategory odgovarajuće kategorije izbrisati vezu na ovu datoteku.
				 */
				
				
				if(deleteAction.matches("file")){
					//ovdje se odvija samo brisanje datoteke.
					if(!removeRepositoryFile(em, dh, data.getMessageLogger(), data.getCurrentUser(), data.getPreviousFileID())) {
						data.setResult(AbstractActionData.RESULT_INPUT);
						return;
					}
				} else {
					//ovdje se brisu fajl i sve njegove (prethodne) verzije
					if(!removeRepositoryFileAndVersions(em, dh, data.getMessageLogger(), data.getCurrentUser(), data.getPreviousFileID())) {
						data.setResult(AbstractActionData.RESULT_INPUT);
						return;
					}
				}
				
					data.setResult(AbstractActionData.RESULT_SUCCESS);
		}

	
	public static void hideRepositoryFile(EntityManager em, RepositoryData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
	
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserManageRepository(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
			
			DAOHelper dh = DAOHelperFactory.getDAOHelper();	
			
			RepositoryFile fileToHide = dh.getRepositoryFileDAO().getFile(em, data.getFileID() );
			
			fileToHide.setStatus(RepositoryFileStatus.HIDDEN);
			
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
	
	
	public static void setRepositoryUpload(EntityManager em, RepositoryData data){
		
		//PROVJERITI (?OVDJE ILI U storeRepositoryFileOnDisk?) DA LI POSTOJI -TODO
		//VEĆ DATOTEKA S ISTIM IMENOM u željenoj datoteci. Ako da, vrati pogrešku(? no permission?) 
		//PAZITI NA VERZIJE!! AKO JE ODABRANA DATOTEKA S ISTIM IMENOM I ODABRANO JE DA BUDE NOVA
		//VERZIJA, ONDA ŠTO? (DODATI AUTOMATSKI ver...) ILI JAVITI POGREŠKU SVEJEDNO? 
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
	
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserManageRepository(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
			
				if(!storeRepositoryFileOnDisk(em, dh, data.getMessageLogger(), data.getUpload(), data.getCurrentUser(), data.getCourseInstance().getCourse(), data.getCourseInstance().getCourse().getRepository(), data.getCategoryID(), data.getFileComment(), data.getUploadFileName(), data.getUploadContentType(), data.getPreviousFileID())) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
		}

	public static void showRepositoryFile(EntityManager em, RepositoryData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
	
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserManageRepository(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
				DAOHelper dh = DAOHelperFactory.getDAOHelper();	
				
				RepositoryFile fileToHide = dh.getRepositoryFileDAO().getFile(em, data.getFileID() );		
				fileToHide.setStatus(RepositoryFileStatus.VISIBLE);
		
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
	}
	
	public static void viewAllVersions(EntityManager em, RepositoryData data){
				
			// Dohvat kolegija
			if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
	
			// Dozvole
			if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				//da li je to administrator
				data.setAdmin(JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance()));
				
				//da li je to nastavno osoblje
				
				data.setStaffMember(dh.getCourseInstanceDAO().isCourseStaffMember(em, data.getCourseInstance(), data.getCurrentUser()));
				
				RepositoryFile fileMoreVersions = dh.getRepositoryFileDAO().getFile(em, data.getFileID());
						
				List<RepositoryFile> allFiles = new ArrayList<RepositoryFile>();
				
				data.setFiles(allFiles);
				
				versionRecursiveList(allFiles, fileMoreVersions);
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
	
	
	public static void viewFileInfo(EntityManager em, RepositoryData data, RepositoryFileBean fileBean) {
				
			// Dohvat kolegija
			if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

			// Dozvole
			if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		
				//da li je to administrator
				data.setAdmin(JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance()));
				
				//da li je to nastavno osoblje
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				data.setStaffMember(dh.getCourseInstanceDAO().isCourseStaffMember(em, data.getCourseInstance(), data.getCurrentUser()));
				
				RepositoryFile fileInfo = dh.getRepositoryFileDAO().getFile(em, data.getFileID() );
							
				fileBean.setRealName(fileInfo.getRealName());
				fileBean.setCategory(fileInfo.getCategory().getCategoryName());
				fileBean.setComment(fileInfo.getComment());
				fileBean.setFileVersion(fileInfo.getFileVersion());
				fileBean.setMimeType(fileInfo.getMimeType());
				fileBean.setOwner(fileInfo.getOwner().getFirstName()+" "+fileInfo.getOwner().getLastName());
				fileBean.setStatus(fileInfo.getStatus().toString());
				fileBean.setUploadDate(fileInfo.getUploadDate().toString());
				
				String isvuCode = fileInfo.getRepositoryCourse().getCourse().getIsvuCode();
				File root = getRepositoryCourseFile(isvuCode, data.getFileID());
				fileBean.setSize((root.length()/1000000.));
				
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static void versionRecursiveList(List<RepositoryFile> allFiles, RepositoryFile fileOldVersion) {
		
		allFiles.add(fileOldVersion);
		
		if(fileOldVersion.getFileVersion()==1){
			return;
		}
		versionRecursiveList(allFiles, fileOldVersion.getPreviousVersion());
	}
	
	
	private static boolean storeRepositoryFileOnDisk(
			EntityManager em, DAOHelper dh, IMessageLogger messageLogger, File upload, User user, Course course, RepositoryCourse repositoryCourse, Long categoryID, String fileComment, String uploadFileName, String uploadContentType, Long previousFileID){
	
		try {
			
			/*
			 * Slijed spremanja na disk i u bazu 
			 *
			 * 1. korak: Dohvati poveznicu na glavni repozitorij, pa zatim na repozitorij predmeta. 
			 *           Ako repozitorij predmeta ne postoji, stvori ga. Ime je ISVU SIFRA (?). Ovo 
			 *           se inace automatski stvara također kod stvaranja Coursa u SynchronizerService.
			 * 2. korak: Stvori novi RepositoryFile, napuni ga potrebnim podacima i spremi ga u bazu. 
			 *           Baza ce joj dodijeliti id!
			 * 3. korak: Pogledaj odabranu kategoriju u repozitoriju, ako željena kategorija ne postoji, 
			 * 			 stvori ju. Ako nije odabrana niti jedna kategorija,ok,sprema se u osnovnu mapu. 
			 * 4. korak: Dohvati dodijeljeni id iz baze i spremi uploadanu datoteku na disk. Treba biti spremljena
			 *           na poziciji: \Repository\Course\Category\ID
			 *   
			 * ---warning--- Ne zaboraviti provjeravati greške (da li se pojedina akcija(korak) uspješno obavila!)
			 * ---update---- Stvoriti temp datoteku, i ako uspije, napraviti samo rename dolje.
			 */		
			// 1. korak:
			File rootDir = checkRepositoryRootCourse(em,course);
			/*trenutno se sve datoteke spremaju za taj predmet u jedan direktorij, cije je ime 
			* ime isvu sifre ( radi lakseg snalazenja, mozda da bude pravo ime?? ). Razmisliti o
			* spremanju u svaki direktorij zasebno ovisno o kategoriji. 
			*/
			
			File targetFile = File.createTempFile("tmp", "bla", rootDir);
			try {
				FileUtils.copyFile(upload, targetFile);
			} catch (Exception e) {
				messageLogger.addErrorMessage(e.getMessage());
				return false;
			}
		
			// 2.korak:
			RepositoryFile repFile = new RepositoryFile();
			repFile.setOwner(user);
			repFile.setRealName(uploadFileName); 
			repFile.setStatus(RepositoryFileStatus.VISIBLE);
			repFile.setComment(fileComment);
			repFile.setUploadDate(new Date());
			repFile.setRepositoryCourse(repositoryCourse);
			//repFile.setMimeType(findMimeTypeForExtension(uploadContentType));
			repFile.setMimeType(uploadContentType);
			//repFile.setFilePages(null);
			
			// VERZIJE i KATEGORIJE
			if(previousFileID == -1L){
				RepositoryCategory category = categoryID == null ? null : dh.getRepositoryFileDAO().getRepositoryCategory(em, categoryID);
				category.getFiles().add(repFile);
				repFile.setFileVersion(1);
				repFile.setCategory(category);
				repFile.setNextVersion(null);
				repFile.setPreviousVersion(null);
			
			}else{
				RepositoryFile previousFile=dh.getRepositoryFileDAO().getFile(em, previousFileID);
				RepositoryCategory previousCategory = previousFile.getCategory();
			    previousCategory.getFiles().add(repFile);
				repFile.setFileVersion(previousFile.getFileVersion()+1); 
				repFile.setCategory(previousCategory);
				repFile.setPreviousVersion(previousFile);
				previousFile.setNextVersion(repFile);	
				
			}
			
			dh.getRepositoryFileDAO().save(em, repFile);  //spremanje u bazu
			
			// 3.korak: --- not needed
			
			// 4.korak:
			String positionOnDisk=rootDir.getCanonicalPath();
			File theFile = new File(positionOnDisk,Long.toString(repFile.getId()));
			try {
				targetFile.renameTo(theFile);
			} catch (Exception e) {
				messageLogger.addErrorMessage(e.getMessage());
				return false;
			}
	
		} catch (Exception e){
			messageLogger.addErrorMessage(e.getMessage());
			return false;
		}
		return true;
	}
	
	private static boolean removeRepositoryFileAndVersions(
			EntityManager em, DAOHelper dh, IMessageLogger messageLogger,
			User currentUser, Long thisFileID) {
		
		RepositoryFile thisFile = dh.getRepositoryFileDAO().getFile(em, thisFileID);
		
		List<Long> allFileID = new ArrayList<Long>();
			
		RepositoryCategory categoryWithVersionsToDelete = thisFile.getCategory();
		//punimo listu sa ID-ovima svih datoteka koje se brišu (sve verzije)
		listFileID(thisFile, allFileID);
		
		for(Long fileIDToDelete : allFileID){
			
			RepositoryFile fileToDelete = dh.getRepositoryFileDAO().getFile(em, fileIDToDelete);
			
			//dodano nakon dodavanja repositoryFilePage-ova u RepositoryFile  -- kod se ponavlja!!! refactor -TODO
			List<RepositoryFilePage> filePagesToDelete = fileToDelete.getFilePages();
		
			//za sve filePagove koji su vezani uz taj fajl		
			for (RepositoryFilePage repositoryFilePage : filePagesToDelete) {
				
				List<RepositoryFilePageComment> commentsToDelete = repositoryFilePage.getComments();
				
				for (RepositoryFilePageComment repositoryFilePageComment : commentsToDelete) {
					dh.getRepositoryFilePageDAO().remove(em, repositoryFilePageComment);
				}
				
				//brišemo sve komentare unutar pojedinog filePage-a
				for (int i = 0; i < commentsToDelete.size(); i++) {
					repositoryFilePage.getComments().remove(i);
				}		
			}
			
			for (RepositoryFilePage repositoryFilePage : filePagesToDelete) {
				dh.getRepositoryFilePageDAO().remove(em, repositoryFilePage);
			}
			
			for (int j = 0; j < filePagesToDelete.size(); j++) {
				fileToDelete.getFilePages().remove(j);
			}

			//kraj novoga dodanog dijela
			
			categoryWithVersionsToDelete.getFiles().remove(fileToDelete);
			
			String isvuCode = fileToDelete.getRepositoryCourse().getCourse().getIsvuCode();
			File fileDeletefromDisk = getRepositoryCourseFile(isvuCode,fileIDToDelete);
			
			if(fileDeletefromDisk.delete()){
				dh.getRepositoryFileDAO().remove(em, fileToDelete);
			}
		}
		return true;
	}
	
	
	
	
	
	private static void listFileID(RepositoryFile thisFile, List<Long> allFileID) {
		
		allFileID.add(thisFile.getId());
		if(thisFile.getPreviousVersion()==null){
			return;
		}
		listFileID(thisFile.getPreviousVersion(), allFileID);
		return;
	}
	
	
	private static boolean removeRepositoryFile(EntityManager em,
			DAOHelper dh, IMessageLogger messageLogger, User currentUser, Long thisFileID) {
		
		RepositoryFile thisFileToDelete = dh.getRepositoryFileDAO().getFile(em,thisFileID);
		//postavljam kod prošle verzije datoteke null na sljedeću verziju
		if(thisFileToDelete.getPreviousVersion()!=null){
			thisFileToDelete.getPreviousVersion().setNextVersion(null);
		}
		
		//dodano nakon dodavanja repositoryFilePage-ova u RepositoryFile
		List<RepositoryFilePage> filePagesToDelete = thisFileToDelete.getFilePages();
	
		//za sve filePagove koji su vezani uz taj fajl		
		for (RepositoryFilePage repositoryFilePage : filePagesToDelete) {
			
			List<RepositoryFilePageComment> commentsToDelete = repositoryFilePage.getComments();
			
			for (RepositoryFilePageComment repositoryFilePageComment : commentsToDelete) {
				dh.getRepositoryFilePageDAO().remove(em, repositoryFilePageComment);
			}
			
			//brišemo sve komentare unutar pojedinog filePage-a
			for (int i = 0; i < commentsToDelete.size(); i++) {
				repositoryFilePage.getComments().remove(i);
			}	
			
		}
		
		for (RepositoryFilePage repositoryFilePage : filePagesToDelete) {
			dh.getRepositoryFilePageDAO().remove(em, repositoryFilePage);
		}
		
		for (int j = 0; j < filePagesToDelete.size(); j++) {
			thisFileToDelete.getFilePages().remove(j);
		}

		//brisanje iz liste fajlova u kategoriji
		thisFileToDelete.getCategory().getFiles().remove(thisFileToDelete);
		
		//brisati sa harda
		String isvuCode = thisFileToDelete.getRepositoryCourse().getCourse().getIsvuCode();
		File fileDeletefromDisk = getRepositoryCourseFile(isvuCode,thisFileToDelete.getId());
		
		if(fileDeletefromDisk.delete()){
			//brisanje iz baze
			dh.getRepositoryFileDAO().remove(em,dh.getRepositoryFileDAO().getFile(em, thisFileID));
		}

		return true;
	}
	
	
	
	
	private static boolean removeRepositoryCategory(EntityManager em, DAOHelper dh, IMessageLogger messageLogger,
			User currentUser, RepositoryCourse repository, RepositoryCategory categoryToDelete) {
		
		/* 
		 *  1. obriši kategoriju iz RepositoryCourse liste ako se radi o root kategoriji, 
		 *  	inače pozovi parent kategory, i tamo obriši tu podkategoriju.
		 *  2. obriši kategoriju (repositoryCategory) po njegovom id-u
		 *  3. -.-
		 */
		
		//1.
		if(categoryToDelete.getParentCategory()== null ){
			repository.getRootCategories().remove(categoryToDelete);
		} else {
			 categoryToDelete.getParentCategory().getSubCategories().remove(categoryToDelete);
		}
		
		//2. 
		dh.getRepositoryFileDAO().remove(em, categoryToDelete);
	
		return true;
	}
	
	
	
	private static File checkRepositoryRootCourse(EntityManager em, Course course){
		File rootDir = JCMSSettings.getSettings().getRepositoriesRootDir();
		rootDir = new File(rootDir,course.getIsvuCode());
		if(!rootDir.exists()){ 
			rootDir.mkdir();
		}
		if(course.getRepository()==null){
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			RepositoryCourse repCourse = new RepositoryCourse();
			repCourse.setCourse(course);
			course.setRepository(repCourse);
			dh.getRepositoryFileDAO().save(em, repCourse);
		}
		return rootDir;
	}
	
	private static void recursiveList(RepositoryCategory cat,
			List<RepositoryCategory> allCategories, List<RepositoryFile> allFiles) {
		
		if(cat==null)return;
		allCategories.add(cat);
		for(RepositoryFile rFile : cat.getFiles()){
			allFiles.add(rFile);
		}
		
		for(RepositoryCategory child : cat.getSubCategories()) {
			recursiveList(child,allCategories,allFiles);
		}
	}
	
	private static boolean saveRepositoryCategory(
			EntityManager em, DAOHelper dh, IMessageLogger messageLogger, RepositoryCourse repositoryCourse, RepositoryCategory parentCategory, String categoryName){
		
		try	{
			RepositoryCategory repCategory = new RepositoryCategory();
			repCategory.setParentCategory(parentCategory);
			repCategory.setRepositoryCourse(repositoryCourse);
			repCategory.setCategoryName(categoryName);
		
			if(parentCategory==null) {
				int numberOfRootCategories = repositoryCourse.getRootCategories().size();
				repCategory.setPosition(numberOfRootCategories+1);
			} else {
				if(parentCategory.getSubCategories().isEmpty()){
					repCategory.setPosition(0);
				} else {
					RepositoryCategory lastChild = parentCategory.getSubCategories().get(parentCategory.getSubCategories().size()-1);	
					repCategory.setPosition(lastChild.getPosition()+1);
				}
				parentCategory.getSubCategories().add(repCategory);	
			}
			
			dh.getRepositoryFileDAO().save(em, repCategory);	
			
		} catch(Exception e) {
			messageLogger.addErrorMessage(e.getMessage());
			return false;
		}
		return true;
	}
	
	private static File getRepositoryCourseFile(String isvuCode, Long fileID){
		File rootDir = JCMSSettings.getSettings().getRepositoriesRootDir(); 
		File pathOnDiskCourse = new File(rootDir, isvuCode);
		File pathOnDiskFile = new File(pathOnDiskCourse, Long.toString(fileID));
		return pathOnDiskFile;	
	}
	
	
}
