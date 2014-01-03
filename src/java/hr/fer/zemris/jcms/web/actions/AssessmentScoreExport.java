package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.AssessmentService;
import hr.fer.zemris.jcms.web.actions.data.AssessmentScoreExportData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

import com.opensymphony.xwork2.Preparable;

public class AssessmentScoreExport extends ExtendedActionSupport implements Preparable {
	
private static final long serialVersionUID = 2L;
	
	String assessmentID;
	AssessmentScoreExportData data;
	DeleteOnCloseFileInputStream stream;
	String format;
	
	@Override
	public void prepare() throws Exception {
		data = new AssessmentScoreExportData(MessageLoggerFactory.createMessageLogger(this,true));
	}
	
	@Override
	public String execute() throws Exception {
		return download();
	}

	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String download() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		DeleteOnCloseFileInputStream[] reference = new DeleteOnCloseFileInputStream[] {null};
		AssessmentService.prepareScoreExport(data, getCurrentUser().getUserID(), getAssessmentID(), reference, getFormat());
		stream = reference[0];
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		return "stream";
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public AssessmentScoreExportData getData() {
		return data;
	}

	public void setData(AssessmentScoreExportData data) {
		this.data = data;
	}
	
	public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}
}
