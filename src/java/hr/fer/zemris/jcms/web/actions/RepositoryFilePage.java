package hr.fer.zemris.jcms.web.actions;


import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.beans.RepositoryFileBean;
import hr.fer.zemris.jcms.model.RepositoryFilePageComment;
import hr.fer.zemris.jcms.service.RepositoryFilePageService;
import hr.fer.zemris.jcms.service.RepositoryService;
//import hr.fer.zemris.jcms.service.imageprovider.ImageProvider;
import hr.fer.zemris.jcms.web.actions.data.RepositoryFilePageData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;


public class RepositoryFilePage extends ExtendedActionSupport implements Preparable {
  
	private static final long serialVersionUID = 1L;
	
	private RepositoryFilePageData data = null;
	private RepositoryFileBean fileBean = null;
	private String courseInstanceID; 
	private String comment;
	private List<String> commentsList; 
	private long fileID;
	private String image;
	private Long pageNumber; // (redni)broj stranice
	private int resId; // rezolucija
	private int commentType; //tip komentara
	
    
	public void prepare() throws Exception {
		data = new RepositoryFilePageData(MessageLoggerFactory.createMessageLogger(this, true));
	}
    
    public String execute() throws Exception {
    	//pokušaj varanja
    	if(!hasCurrentUser()){
    		return NO_PERMISSION;
    	}

    	RepositoryFilePageService.showFilePage(data, getCurrentUser().getUserID(), courseInstanceID, fileID, pageNumber);
    	
    	commentsList = new ArrayList<String>();
   
    	if(data.getComments().isEmpty()){
    		commentsList.add("Nema komentara");
    	}
    	
    	for (RepositoryFilePageComment comment : data.getComments()) {
    		commentsList.add(comment.getComment());
		}
    	
    	
    	
    	//commentsList = data.getComments();
    	
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			return SUCCESS;
		}
    	return NO_PERMISSION;
    }
    

	public String addFilePageCommentPrep() throws Exception {
    	//data.setFileID(fileID);
    	return SUCCESS;
    }
    
    public String addFilePageComment() throws Exception {
    	if(!hasCurrentUser()) {
     		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
     		return NO_PERMISSION;
        	}
    		commentType = 0;
    		//pageNumber = new Long(0);
    		//comment = "Ovo je prvi komentar";
        	RepositoryFilePageService.setRepositoryFilePageNewComment(data, getCurrentUser().getUserID(), courseInstanceID, comment, pageNumber, fileID, commentType);
        	
        	if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
    		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
    			data.getMessageLogger().registerAsDelayed();
    			//data.setActionMessage("ACTION OK!");
    			return SUCCESS;
    		}
    		return NO_PERMISSION;
    }
    
    public String showPicture() throws Exception{
    	if(!hasCurrentUser()) {
     		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
     		return NO_PERMISSION;
        	}
   
    	return NO_PERMISSION;
    }

	public RepositoryFilePageData getData() {
		return data;
	}

	public void setData(RepositoryFilePageData data) {
		this.data = data;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public long getFileID() {
		return fileID;
	}

	public void setFileID(long fileID) {
		this.fileID = fileID;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Long getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Long pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public int getCommentType() {
		return commentType;
	}

	public void setCommentType(int commentType) {
		this.commentType = commentType;
	}

	public void setCommentsList(List<String> commentsList) {
		this.commentsList = commentsList;
	}

	public List<String> getCommentsList() {
		return commentsList;
	}
    
	
	

}

