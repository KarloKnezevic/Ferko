package hr.fer.zemris.jcms.web.actions.data;

import java.io.InputStream;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.StudentScoreBrowserSettings;

/**
 * Podatkovna struktura za akciju {@link StudentScoreBrowserSettings}.
 *  
 * @author marcupic
 *
 */
public class StudentScoreBrowserSettingsData extends BaseCourseInstance {

	private InputStream stream;
	private String what;
	private String reqdata;
	private String courseInstanceID;

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public StudentScoreBrowserSettingsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
	public String getWhat() {
		return what;
	}
	public void setWhat(String what) {
		this.what = what;
	}

	public String getReqdata() {
		return reqdata;
	}
	public void setReqdata(String reqdata) {
		this.reqdata = reqdata;
	}

	public InputStream getStream() {
		return stream;
	}
	public void setStream(InputStream stream) {
		this.stream = stream;
	}
}
