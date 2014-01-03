package hr.fer.zemris.jcms.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.RepositoryCategory;
import hr.fer.zemris.jcms.model.RepositoryCourse;
import hr.fer.zemris.jcms.model.RepositoryFile;
import hr.fer.zemris.jcms.model.RepositoryFilePage;
import hr.fer.zemris.jcms.model.RepositoryFilePageComment;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.RepositoryData;
import hr.fer.zemris.jcms.web.actions.data.RepositoryFilePageData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import javax.persistence.EntityManager;



	public class RepositoryFilePageService {

		
		
		
		public static void showFilePage(final RepositoryFilePageData data, final Long userID, 
				final String courseInstanceID, final Long fileID, final Long pageNumber) {
			PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
				@Override
				public Void executeOperation(EntityManager em) {
					if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
					if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID, "Error.invalidParameters", AbstractActionData.RESULT_FATAL)) return null;
					JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
					if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					
					//da li je to administrator
					data.setAdmin(JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance()));
					
					//da li je to nastavno osoblje
					DAOHelper dh = DAOHelperFactory.getDAOHelper();
					data.setStaffMember(dh.getCourseInstanceDAO().isCourseStaffMember(em, data.getCourseInstance(), data.getCurrentUser()));
					
					RepositoryFile repFile = dh.getRepositoryFileDAO().getFile(em, fileID);
				
					if(repFile == null){
						
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					
	
					//provjeravamo da li postoje već izgenerirane sličice, ako ne, generiraj ih. 
					//Ako se dogodi pogreška u generiranju slika, prikaži stranicu ali bez slike
					
					if(!imagesExist(repFile)){ 
						if(!imagesCreate(repFile, dh, em)){
							data.setResult(AbstractActionData.RESULT_NONFATAL_ERROR);
							return null;
						}
					}
					
					data.setFileID(repFile.getId());
					
					Long filePageID = null;
					
					for (RepositoryFilePage repositoryFilePage : repFile.getFilePages()) {
						if(repositoryFilePage.getPage() == pageNumber){
							filePageID = repositoryFilePage.getId();
							break;
						}
					}
					
					
					RepositoryFilePage filePage = dh.getRepositoryFilePageDAO().getFilePage(em, filePageID);
					//komentari na pripadajućoj stranici
					List<RepositoryFilePageComment> comments = new ArrayList<RepositoryFilePageComment>();
					
					data.setComments(comments);
					
					for (RepositoryFilePageComment pageComments : filePage.getComments()) {
						comments.add(pageComments);
					}
								
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				
			});
			
		}
		
		private static boolean imagesExist(RepositoryFile repFile) {
			
			if(repFile.getFilePages().isEmpty()){
				return false;
			}
			return true;
		}
		
		
		
		
		protected static boolean imagesCreate(RepositoryFile repFile, DAOHelper dh, EntityManager em) {
			
			//RepositoryFilePage filePage = new RepositoryFilePage();
			//filePage.setRepositoryFile(repFile);
			
			
			RepositoryFilePage filePageAdd = new RepositoryFilePage();
			filePageAdd.setPage(0);
			filePageAdd.setRepositoryFile(repFile);
			
			repFile.getFilePages().add(filePageAdd);
			
			dh.getRepositoryFilePageDAO().save(em, filePageAdd);
			
			return true;
		}

		
		
		
		public static void setRepositoryFilePageNewComment(
				final RepositoryFilePageData data, final Long userID, 
				 final String courseInstanceID, final String comment, 
				  final Long pageNumber, final Long fileID,
				  	final int commentType ){
			
			PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
				public Void executeOperation(EntityManager em) {

					if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
					if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID, "Error.invalidParameters", AbstractActionData.RESULT_FATAL)) return null;
					JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
					if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())|| (!JCMSSecurityManagerFactory.getManager().canUserManageRepository(data.getCourseInstance()))) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					
					DAOHelper dh = DAOHelperFactory.getDAOHelper();
					
					RepositoryFile repFile = dh.getRepositoryFileDAO().getFile(em, fileID);
					List<RepositoryFilePage> filePages = repFile.getFilePages();
					
					//String h = Long.toString(pageNumber);
					//int a = Integer.getInteger(h);
					
					
					//RepositoryFilePage filePage = dh.getRepositoryFilePageDAO().getFilePage(em, fileID);
					
					RepositoryFilePage filePage = null;
					
					for (RepositoryFilePage repositoryFilePage : filePages) {
						if(repositoryFilePage.getPage() == pageNumber){
							filePage = repositoryFilePage;
							break;
						}
					}
					
					if(filePage == null){
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
					
					
					RepositoryFilePageComment pageComment = new RepositoryFilePageComment();
					pageComment.setComment(comment);
					pageComment.setCommentType(commentType);
					pageComment.setDate(new Date());
					pageComment.setRepositoryFilePage(filePage);
					pageComment.setUser(data.getCurrentUser());
					
					filePage.getComments().add(pageComment);
					
					dh.getRepositoryFilePageDAO().save(em, pageComment);

					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}//kraj executeOperation
			});
		} //kraj metode

}//kraj razreda

	
