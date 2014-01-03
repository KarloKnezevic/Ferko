package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.ApplicationDefinition;

import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ApplicationExportListData extends BaseCourseInstance {
	
	private List<User> users;
	private Map<Long, StudentApplication> applications; 

	private ApplicationDefinition definition;
	private String mimeType;
	private String fileName;
	private InputStream stream;
	private long length;

	private String format;
	private String courseInstanceID;
	private Long definitionID;

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ApplicationExportListData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Map<Long, StudentApplication> getApplications() {
		return applications;
	}

	public void setApplications(Map<Long, StudentApplication> applications) {
		this.applications = applications;
	}
	
	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}
    
    public long getLength() {
		return length;
	}
    public void setLength(long length) {
		this.length = length;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ApplicationDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ApplicationDefinition definition) {
		this.definition = definition;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public Long getDefinitionID() {
		return definitionID;
	}

	public void setDefinitionID(Long definitionID) {
		this.definitionID = definitionID;
	}

}
