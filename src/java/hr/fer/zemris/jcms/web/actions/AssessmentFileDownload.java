package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AssessmentFileDownloadData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class AssessmentFileDownload extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private String assessmentID;
	private String assessmentFileID;
	private AssessmentFileDownloadData data = null;
	private InputStream stream;
	private long length;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentFileDownloadData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		BasicBrowsing.getAssessmentFileDownloadData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID(), getAssessmentFileID());
    	} else {
    		return NO_PERMISSION;
    	}
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			try {
				length = data.getFilePath().length();
				stream = new BufferedInputStream(new FileInputStream(data.getFilePath()),32*1024);
			} catch(Exception ex) {
				data.getMessageLogger().addErrorMessage("Datoteka nije dostupna.");
				return SHOW_FATAL_MESSAGE;
			}
	        return SUCCESS;
		}
        return NO_PERMISSION;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public String getAssessmentID() {
		return assessmentID;
	}
    public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}
    
    public AssessmentFileDownloadData getData() {
		return data;
	}
    public void setData(AssessmentFileDownloadData data) {
		this.data = data;
	}
    
    public String getAssessmentFileID() {
		return assessmentFileID;
	}
    public void setAssessmentFileID(String assessmentFileID) {
		this.assessmentFileID = assessmentFileID;
	}
    
    public InputStream getStream() {
		return stream;
	}
    
    public long getLength() {
		return length;
	}
    public void setLength(long length) {
		this.length = length;
	}
}
