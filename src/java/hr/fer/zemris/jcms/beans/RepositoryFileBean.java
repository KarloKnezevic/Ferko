package hr.fer.zemris.jcms.beans;

//import java.util.List;

public class RepositoryFileBean {

	private Long id;
	private String realName;
	private String comment;
	private String uploadDate;
	private String status;
	private String mimeType;
	private String owner;
	private String category;
	private int fileVersion;
	private double size;
	private boolean hidden;
	
	/*private List<String> roles;
	
	private RepositoryCourse repositoryCourse; 
	private RepositoryFile nextVersion;
	private RepositoryFile previousVersion;
	*/
		
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int getFileVersion() {
		return fileVersion;
	}
	public void setFileVersion(int fileVersion) {
		this.fileVersion = fileVersion;
	}
	public double getSize() {
		return size;
	}
	public void setSize(double size) {
		this.size = size;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public boolean isHidden() {
		return hidden;
	}
	
}