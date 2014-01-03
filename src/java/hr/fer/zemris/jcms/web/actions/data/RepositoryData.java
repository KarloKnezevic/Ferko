package hr.fer.zemris.jcms.web.actions.data;

import java.io.File;
import java.util.List;

import com.lowagie.text.pdf.codec.Base64.InputStream;

import hr.fer.zemris.jcms.model.RepositoryCategory;
import hr.fer.zemris.jcms.model.RepositoryFile;
import hr.fer.zemris.jcms.web.actions.Repository;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.InputStreamWrapper;

/**
 * Podatkovna struktura za akciju {@link Repository}.
 *  
 */
public class RepositoryData extends BaseCourseInstance {
	
	private List<RepositoryCategory> categories;
	private List<RepositoryFile> files; 
	private String actionMessage;
	private File filePath;
	private String fileName;
	private boolean admin;
	private boolean staffMember; 
	private String courseInstanceID;
	private Long categoryID;
	private String categoryName;      //ime koje ce imati kategorija
    private Long fileID; //id odabrane datoteke
	private InputStreamWrapper stream;
	private Long previousFileID; // id datoteke koja se postavlja kao prethodna verzija datoteke
	private File upload; //datoteka koja se uploada
    private String fileComment;  //komentar uz datoteku
    private String uploadFileName; //ime uploadane datoteke
    private String uploadContentType;  //tip datoteke
	
    
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

	public Long getPreviousFileID() {
		return previousFileID;
	}
    
    public void setPreviousFileID(Long previousFileID) {
		this.previousFileID = previousFileID;
	}
	
	public InputStreamWrapper getStream() {
		return stream;
	}
	
	public void setStream(InputStreamWrapper stream) {
		this.stream = stream;
	}
	
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public RepositoryData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public void setCategories(List<RepositoryCategory> categories) {
		this.categories = categories;
	}

	public List<RepositoryCategory> getCategories() {
		return categories;
	}
	
	public void setFiles(List<RepositoryFile> files) {
		this.files = files;
	}

	public List<RepositoryFile> getFiles() {
		return files;
	}

	public String getActionMessage() {
		return actionMessage;
	}

	public void setActionMessage(String actionMessage) {
		this.actionMessage = actionMessage;
	}

	public File getFilePath() {
		return filePath;
	}

	public void setFilePath(File filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCategoryID(Long categoryID) {
		this.categoryID = categoryID;
	}

	public Long getCategoryID() {
		return categoryID;
	}

	public void setFileID(Long fileID) {
		this.fileID = fileID;
	}

	public Long getFileID() {
		return fileID;
	}
	
	

}

