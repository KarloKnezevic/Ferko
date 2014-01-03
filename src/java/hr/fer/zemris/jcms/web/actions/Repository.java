package hr.fer.zemris.jcms.web.actions;


//import hr.fer.zemris.jcms.beans.RepositoryCategoryBean;
import hr.fer.zemris.jcms.beans.RepositoryFileBean;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.service.RepositoryService;
import hr.fer.zemris.jcms.web.actions.data.RepositoryData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.persistence.EntityManager;

import com.opensymphony.xwork2.Preparable;


public class Repository extends ExtendedActionSupport implements Preparable {
  
	private static final long serialVersionUID = 1L;
	
	private RepositoryData data = null;
	private RepositoryFileBean fileBean = null;
	private String courseInstanceID;
	private File upload; //datoteka koja se uploada
    private String uploadContentType;  //tip datoteke
    private String uploadFileName; //ime uploadane datoteke
    private String fileComment;  //komentar uz datoteku
    private Long previousFileID; // id datoteke koja se postavlja kao prethodna verzija datoteke
    private Long categoryID;
    private String categoryName;   //ime koje ce imati kategorija
    private String message;
    private Long fileID; //id odabrane datoteke
    private long length;
    private InputStream inputStream;
    
	public void prepare() throws Exception {
		data = new RepositoryData(MessageLoggerFactory.createMessageLogger(this, true));
	}
    
    public String execute() throws Exception {
    	//pokušaj varanja
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	RepositoryService.showRepository(data,getCurrentUser().getUserID(),courseInstanceID);
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			return SUCCESS;
		}
		return NO_PERMISSION;
    }
    public String newCategoryPrep() throws Exception {
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	
    	RepositoryService.fillDataWithCourseInstance(data,getCurrentUser().getUserID(), courseInstanceID);
    	
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			return SUCCESS;
		}
    	return NO_PERMISSION;
    }
    
    public String newCategory() throws Exception {
    	//pokušaj varanja
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	
    	RepositoryService.setRepositoryNewCategory(data, getCurrentUser().getUserID(), categoryName, courseInstanceID, categoryID);
    	if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			data.setActionMessage("ACTION OK!");
			return SUCCESS;
		}
		return NO_PERMISSION;
    }
    
    public String uploadFilePrep() throws Exception {
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	
    	RepositoryService.fillDataWithCourseInstance(data,getCurrentUser().getUserID(), courseInstanceID);
    	
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			return SUCCESS;
		}
    	return NO_PERMISSION;
    }
    
    public String uploadFile() throws Exception {
     
    	if(!hasCurrentUser()) {
 		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
 		return NO_PERMISSION;
    	}
    	RepositoryService.setRepositoryUpload(data, upload, getCurrentUser().getUserID(), courseInstanceID, categoryID, fileComment, uploadFileName, -1L, uploadContentType);
    	if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			data.setActionMessage("ACTION OK!");
			return SUCCESS;
		}
		return NO_PERMISSION;
    } 
    
    public String uploadVersionPrep() throws Exception {
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	
    	RepositoryService.fillDataWithCourseInstance(data,getCurrentUser().getUserID(), courseInstanceID);
    	
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			return SUCCESS;
		}
    	return NO_PERMISSION;
    }
    
    public String uploadVersion() throws Exception {
    	if(!hasCurrentUser()) {
     		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
     		return NO_PERMISSION;
        	}
        	RepositoryService.setRepositoryUpload(data, upload, getCurrentUser().getUserID(), courseInstanceID, -1L, fileComment, uploadFileName, previousFileID, uploadContentType);
        	if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
    		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
    			data.getMessageLogger().registerAsDelayed();
    			data.setActionMessage("ACTION OK!");
    			return SUCCESS;
    		}
    		return NO_PERMISSION;
    }
     
    public String deleteCategory() throws Exception {
    	//pokušaj varanja
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	
    	RepositoryService.deleteRepositoryCategory(data, getCurrentUser().getUserID(), courseInstanceID, categoryID);
    	if(data.getResult().equals(AbstractActionData.RESULT_REVIEW_ACTION)) {
    		data.setActionMessage("ACTION ERROR!");
    		return SUCCESS;
    	}
    	if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			data.setActionMessage("ACTION OK!");
			return SUCCESS;
		}
		return NO_PERMISSION;
    }
    
    public String deleteFile() throws Exception {
    	//pokušaj varanja
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	
    	RepositoryService.deleteRepositoryFile(data, getCurrentUser().getUserID(), courseInstanceID, previousFileID, "file");
    	if(data.getResult().equals(AbstractActionData.RESULT_REVIEW_ACTION)) {
    		data.setActionMessage("ACTION ERROR!");
    		return SUCCESS;
    	}
    	if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			data.setActionMessage("ACTION OK!");
			return SUCCESS;
		}
		return NO_PERMISSION;
    }   
    
    public String deleteFileAndVersions() throws Exception {
    	//pokušaj varanja
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	
    	RepositoryService.deleteRepositoryFile(data, getCurrentUser().getUserID(), courseInstanceID, previousFileID,"fileAndVersions");
    	if(data.getResult().equals(AbstractActionData.RESULT_REVIEW_ACTION)) {
    		data.setActionMessage("ACTION ERROR!");
    		return SUCCESS;
    	}
    	if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			data.setActionMessage("ACTION OK!");
			return SUCCESS;
		}
		return NO_PERMISSION;
    }   
    
    public String downloadFile() throws Exception {
    	
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	RepositoryService.downloadRepositoryFile(data, getCurrentUser().getUserID(), courseInstanceID, fileID);
    
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
    		
    		try {
				length = data.getFilePath().length();
				data.getFilePath().length();
				inputStream = new BufferedInputStream(new FileInputStream(data.getFilePath()),32*1024);
			} catch(Exception ex) {
				return NO_PERMISSION;
			}
    		
    		return SUCCESS;
    	}
    	return NO_PERMISSION;	
    }
    
    public String hideFile() throws Exception {
    	
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	RepositoryService.hideRepositoryFile(data, getCurrentUser().getUserID(), courseInstanceID, fileID);
    
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
    		data.getMessageLogger().registerAsDelayed();
    		return SUCCESS;
    	}
    	return NO_PERMISSION;	
    }
    
public String showFile() throws Exception {
    	
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	RepositoryService.showRepositoryFile(data, getCurrentUser().getUserID(), courseInstanceID, fileID);
    
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
    		data.getMessageLogger().registerAsDelayed();
    		return SUCCESS;
    	}
    	return NO_PERMISSION;	
    }
    
    public String viewAllVersions() throws Exception {
    	
    	//pokušaj varanja
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	RepositoryService.viewAllVersions(data, getCurrentUser().getUserID(), courseInstanceID, fileID );
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			return SUCCESS;
		}
		return NO_PERMISSION;
    }
    
    public String viewFileInfo() throws Exception {
    	
    	//pokušaj varanja
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}
    	fileBean = new RepositoryFileBean(); 
    	
    	RepositoryService.viewFileInfo(data, fileBean, getCurrentUser().getUserID(), courseInstanceID, fileID );
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			return SUCCESS;
		}
		return NO_PERMISSION;
    }
    
    
  public Long getPreviousFileID() {
	return previousFileID;
  }
  public void setPreviousFileID(Long previousFileID) {
	this.previousFileID = previousFileID;
  }
  public Long getCategoryID() {
	return categoryID;
  }
  public void setCategoryID(Long categoryID) {
	this.categoryID = categoryID;
  }
  public String getFileComment() {
    return fileComment;
  }
  public void setFileComment(String fileComment) {
    this.fileComment = fileComment;
  }
  public File getUpload() {
    return upload;
  }
  public void setUpload(File upload) {
    this.upload = upload;
  }
  public String getUploadContentType() {
    return uploadContentType;
  }
  public void setUploadContentType(String uploadContentType) {
    this.uploadContentType = uploadContentType;
  }
  public String getUploadFileName() {
    return uploadFileName;
  }
  public void setUploadFileName(String uploadFileName) {
    this.uploadFileName = uploadFileName;
  }
  public String getCourseInstanceID() {
	return courseInstanceID;
  }
  public void setCourseInstanceID(String courseInstanceID) {
	this.courseInstanceID = courseInstanceID;
  }
  public RepositoryData getData() {
	return data;
  }
  public void setData(RepositoryData data) {
	this.data = data;	
  }
  public String getCategoryName() {
	return categoryName;
  }
  public void setCategoryName(String categoryName) {
	this.categoryName = categoryName;
  }
  
  public String getMessage() {
	return message;
  }

  public void setMessage(String message) {
	this.message = message;
  }

  public Long getFileID() {
	return fileID;
  }

  public void setFileID(Long fileID) {
	this.fileID = fileID;
  }

 public long getLength() {
	return length;
 }

 public void setLength(long length) {
	this.length = length;
 }

 public InputStream getInputStream() {
	return inputStream;
 }

 public void setInputStream(InputStream inputStream) {
	this.inputStream = inputStream;
 }

public RepositoryFileBean getFileBean() {
	return fileBean;
}

public void setFileBean(RepositoryFileBean fileBean) {
	this.fileBean = fileBean;
}

  

}

