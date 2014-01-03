package hr.fer.zemris.jcms.web.actions.data;

import java.io.File;
import java.util.List;

import hr.fer.zemris.jcms.model.RepositoryFilePage;
import hr.fer.zemris.jcms.model.RepositoryFilePageComment;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link RepositoryPage}.
 *  
 */
public class RepositoryFilePageData extends BaseCourseInstance {
	
	private List<RepositoryFilePageComment> comments; 
	private List<RepositoryFilePage> filePages;
	private String actionMessage;
	private File picturePath;
	private String pictureName;
	private boolean admin;
	private boolean staffMember;
	private int file;
	private Long fileID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public RepositoryFilePageData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	public List<RepositoryFilePageComment> getComments() {
		return comments;
	}
	public void setComments(List<RepositoryFilePageComment> comments) {
		this.comments = comments;
	}
	public File getPicturePath() {
		return picturePath;
	}
	public void setPicturePath(File picturePath) {
		this.picturePath = picturePath;
	}
	public String getPictureName() {
		return pictureName;
	}
	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public boolean isStaffMember() {
		return staffMember;
	}
	public void setStaffMember(boolean staffMember) {
		this.staffMember = staffMember;
	}
	public List<RepositoryFilePage> getFilePage() {
		return filePages;
	}
	public void setFilePage(List<RepositoryFilePage> filePages) {
		this.filePages = filePages;
	}
	public void setActionMessage(String actionMessage) {
		this.actionMessage = actionMessage;
	}
	public String getActionMessage() {
		return actionMessage;
	}
	public void setId(int id) {
		this.file = id;
	}
	public int getId() {
		return file;
	}
	public void setFileID(Long fileID) {
		this.fileID = fileID;
	}
	public Long getFileID() {
		return fileID;
	}


   
	

}

