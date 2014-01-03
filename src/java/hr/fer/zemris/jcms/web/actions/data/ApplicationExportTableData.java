package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApplicationExportTableData extends BaseCourseInstance {
	
	private List<ApplicationDefinition> definitions;
	private List<User> users;
	private Map <Long, Map<Long, StudentApplication>> applications; 
	
	private CourseInstance courseInstance;
	private String mimeType;
	private String fileName;
	private InputStream stream;
	private long length;

	private String format;
	private String courseInstanceID;

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ApplicationExportTableData(IMessageLogger messageLogger) {
		super(messageLogger);
		List<ApplicationDefinition> l = Collections.emptyList();
		List<User> l2 = Collections.emptyList();
		Map <Long, Map<Long, StudentApplication>> m = Collections.emptyMap();
		setDefinitions(l);
		setUsers(l2);
		setApplications(m);
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
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

	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	public List<ApplicationDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<ApplicationDefinition> definitions) {
		this.definitions = definitions;
	}

	public Map<Long, Map<Long, StudentApplication>> getApplications() {
		return applications;
	}

	public void setApplications(
			Map<Long, Map<Long, StudentApplication>> applications) {
		this.applications = applications;
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
}
