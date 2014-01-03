package hr.fer.zemris.jcms.beans;

public class MPRootInfoBean {

	private Long id;
	private String compositeCourseID;
	private String relativePath;
	private String name;
	private boolean active;
	private boolean canManage;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCompositeCourseID() {
		return compositeCourseID;
	}
	public void setCompositeCourseID(String compositeCourseID) {
		this.compositeCourseID = compositeCourseID;
	}
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Moze li aktivni korisnik upravljati burzom?
	 * @return
	 */
	public boolean getCanManage() {
		return canManage;
	}
	public void setCanManage(boolean canManage) {
		this.canManage = canManage;
	}
}
