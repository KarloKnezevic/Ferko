package hr.fer.zemris.jcms.web.actions2.course.repositories;


//import hr.fer.zemris.jcms.beans.RepositoryCategoryBean;
import hr.fer.zemris.jcms.beans.RepositoryFileBean;

import hr.fer.zemris.jcms.service.RepositoryService;
import hr.fer.zemris.jcms.service2.course.repositories.RepositoriesService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.RepositoryData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.repositories.RepositoryBuilder;

import java.io.File;
import java.io.InputStream;

@WebClass(dataClass=RepositoryData.class)
public class Repository extends Ext2ActionSupport<RepositoryData> {
  
	private static final long serialVersionUID = 1L;

	private RepositoryFileBean fileBean = null;

    private String message;

    private long length;
    private InputStream inputStream;
    
	
	@WebMethodInfo
    public String execute() throws Exception {
    	RepositoriesService.fetchMain(getEntityManager(), data);
		return null;
    }
	
	@WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=RepositoryBuilder.class,navigBuilderIsRoot=false,additionalMenuItems = {"m2","Repository.newRootCategory"})})
    public String newCategoryPrep() throws Exception {
    	RepositoriesService.prepareNewCategory(getEntityManager(),data);
    	return null;
    }
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true)},
					struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=BuilderDefault.class)})
    public String newCategory() throws Exception {
    	RepositoriesService.addNewCategory(getEntityManager(), data);
    	return null;
    }
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=BuilderDefault.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))})
    public String downloadFile() throws Exception {
    	RepositoriesService.getRepositoryFile(getEntityManager(), data);
    	return null;
    }
    
	@WebMethodInfo(
		dataResultMappings={
				@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true),
				@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=SHOW_FATAL_MESSAGE,registerDelayedMessages=true),
				@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=INPUT,registerDelayedMessages=true),
				@DataResultMapping(dataResult=AbstractActionData.RESULT_REVIEW_ACTION,struts2Result=SUCCESS,registerDelayedMessages=true)
				
		},
		struts2ResultMappings={
				@Struts2ResultMapping(struts2Result="SUCCESS", navigBuilder=BuilderDefault.class),
				@Struts2ResultMapping(struts2Result="SHOW_FATAL_MESSAGE", navigBuilder=BuilderDefault.class),
				@Struts2ResultMapping(struts2Result="INPUT", navigBuilder=BuilderDefault.class)
		}
	)
	public String deleteCategory() throws Exception {
    	RepositoriesService.deleteRepositoryCategory(getEntityManager(), data);//, getCurrentUser().getUserID(), getCourseInstanceID(), getCategoryID());
    	return null;
    	
    	/*
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
		return NO_PERMISSION;*/
    }
	
	
	@WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=RepositoryBuilder.class,navigBuilderIsRoot=false,additionalMenuItems = {"m2","Repository.newFile"})})
    public String uploadFilePrep() throws Exception {
    	RepositoriesService.prepareNewCategory(getEntityManager(),data);
    	//RepositoryService.fillDataWithCourseInstance(data,getCurrentUser().getUserID(), getCourseInstanceID());
    	return null;
    }
    
	@WebMethodInfo(
			dataResultMappings={
					@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true),
					@DataResultMapping(dataResult=AbstractActionData.RESULT_REVIEW_ACTION,struts2Result=SUCCESS,registerDelayedMessages=true)
					
			},
			struts2ResultMappings={
					@Struts2ResultMapping(struts2Result="SUCCESS", navigBuilder=BuilderDefault.class)
			}
		)
    public String uploadFile() throws Exception {
    	setPreviousFileID(-1L);
		RepositoriesService.setRepositoryUpload(getEntityManager(), data);
    	return null;
    } 
    
    @WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=RepositoryBuilder.class,navigBuilderIsRoot=false,additionalMenuItems = {"m2","Repository.newVersion"})})
    public String uploadVersionPrep() throws Exception {
        RepositoriesService.prepareNewCategory(getEntityManager(),data);
        return null;
    }
    
    @WebMethodInfo(
			dataResultMappings={
					@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true),
					@DataResultMapping(dataResult=AbstractActionData.RESULT_REVIEW_ACTION,struts2Result=SUCCESS,registerDelayedMessages=true)		
			},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result="SUCCESS", navigBuilder=BuilderDefault.class)
			}
		)
    public String uploadVersion() throws Exception {
    	setCategoryID(-1L);
    	RepositoriesService.setRepositoryUpload(getEntityManager(), data);
    	return null;
    } 
   
    
    @WebMethodInfo(
    		dataResultMappings={
    				@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true),
    				@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=SHOW_FATAL_MESSAGE,registerDelayedMessages=true),
    				@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=INPUT,registerDelayedMessages=true),
    				@DataResultMapping(dataResult=AbstractActionData.RESULT_REVIEW_ACTION,struts2Result=SUCCESS,registerDelayedMessages=true)
    				
    		},
    		struts2ResultMappings={
    				@Struts2ResultMapping(struts2Result="SUCCESS", navigBuilder=BuilderDefault.class),
    				@Struts2ResultMapping(struts2Result="SHOW_FATAL_MESSAGE", navigBuilder=BuilderDefault.class),
    				@Struts2ResultMapping(struts2Result="INPUT", navigBuilder=BuilderDefault.class)
    		}
    )
    public String deleteFile() throws Exception {	
    	RepositoriesService.deleteRepositoryFile(getEntityManager(), data, "file");
    	/*if(data.getResult().equals(AbstractActionData.RESULT_REVIEW_ACTION)) {
    		data.setActionMessage("ACTION ERROR!");
    		return SUCCESS;
    	}*/
    	//if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
    	/*if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			data.setActionMessage("ACTION OK!");
			return SUCCESS;
		}
		return NO_PERMISSION;
		*/
    	return null;
    }   
    
    @WebMethodInfo(
    		dataResultMappings={
    				@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true),
    				@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=SHOW_FATAL_MESSAGE,registerDelayedMessages=true),
    				@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=INPUT,registerDelayedMessages=true),
    				@DataResultMapping(dataResult=AbstractActionData.RESULT_REVIEW_ACTION,struts2Result=SUCCESS,registerDelayedMessages=true)
    				
    		},
    		struts2ResultMappings={
    				@Struts2ResultMapping(struts2Result="SUCCESS", navigBuilder=BuilderDefault.class),
    				@Struts2ResultMapping(struts2Result="SHOW_FATAL_MESSAGE", navigBuilder=BuilderDefault.class),
    				@Struts2ResultMapping(struts2Result="INPUT", navigBuilder=BuilderDefault.class)
    		}
    )
    public String deleteFileAndVersions() throws Exception {
    	RepositoriesService.deleteRepositoryFile(getEntityManager(), data, "fileAndVersions");
    	return null;
    }   
    
    
    @WebMethodInfo(
    	dataResultMappings={
    		@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true)
    	},
    	struts2ResultMappings={
    		@Struts2ResultMapping(struts2Result="SUCCESS", navigBuilder=BuilderDefault.class)
    	}
    )
    public String hideFile() throws Exception {
    	RepositoriesService.hideRepositoryFile(getEntityManager(), data);
    	return null;	
    }

    @WebMethodInfo(
        dataResultMappings={
        	@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true)
        },
        struts2ResultMappings={
        	@Struts2ResultMapping(struts2Result="SUCCESS", navigBuilder=BuilderDefault.class)
        }
     )
     public String showFile() throws Exception {
    	RepositoriesService.showRepositoryFile(getEntityManager(), data);
    	return null;
    }
    
    @WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=RepositoryBuilder.class,navigBuilderIsRoot=false,additionalMenuItems = {"m2","Repository.allVersions"})})
    public String viewAllVersions() throws Exception {
    	RepositoriesService.viewAllVersions(getEntityManager(), data);
		return null;
    }
    
    @WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=RepositoryBuilder.class,navigBuilderIsRoot=false,additionalMenuItems = {"m2","Repository.viewFileInfo"})})
    public String viewFileInfo() throws Exception {
    	fileBean = new RepositoryFileBean(); 
    	RepositoriesService.viewFileInfo(getEntityManager(), data, fileBean);
		return null;
    }
    
    
  public Long getPreviousFileID() {
	return data.getPreviousFileID();
  }
  public void setPreviousFileID(Long previousFileID) {
	data.setPreviousFileID(previousFileID);
  }
  public Long getCategoryID() {
	return data.getCategoryID();
  }
  public void setCategoryID(Long categoryID) {
	data.setCategoryID(categoryID);
  }
  public String getFileComment() {
    return data.getFileComment();
  }
  public void setFileComment(String fileComment) {
    data.setFileComment(fileComment);
  }
  public File getUpload() {
    return data.getUpload();
  }
  public void setUpload(File upload) {
    data.setUpload(upload);
  }
  public String getUploadContentType() {
    return data.getUploadContentType();
  }
  public void setUploadContentType(String uploadContentType) {
    data.setUploadContentType(uploadContentType);
  }
  public String getUploadFileName() {
    return data.getUploadFileName();
  }
  public void setUploadFileName(String uploadFileName) {
    data.setUploadFileName(uploadFileName);
  }
  public String getCourseInstanceID() {
	return data.getCourseInstanceID();
  }
  public void setCourseInstanceID(String courseInstanceID) {
	data.setCourseInstanceID(courseInstanceID);
  }
 
  public String getCategoryName() {
	return data.getCategoryName();
  }
  public void setCategoryName(String categoryName) {
	data.setCategoryName(categoryName);
  }
  
  public String getMessage() {
	return message;
  }

  public void setMessage(String message) {
	this.message = message;
  }

  public Long getFileID() {
	return getFileID();
  }

  public void setFileID(Long fileID) {
	data.setFileID(fileID);
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

