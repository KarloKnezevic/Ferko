package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.AssessmentService;
import hr.fer.zemris.jcms.web.actions.data.AssessmentSummaryExportData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

import com.opensymphony.xwork2.Preparable;

public class AdminAssessmentSummaryExport extends ExtendedActionSupport implements Preparable {
	
private static final long serialVersionUID = 2L;
	
	String courseInstanceID;
	AssessmentSummaryExportData data;
	DeleteOnCloseFileInputStream stream;
	String format;
	Long selectedGroup;
	
	@Override
	public void prepare() throws Exception {
		data = new AssessmentSummaryExportData(MessageLoggerFactory.createMessageLogger(this,true));
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
		AssessmentService.assessmentSummaryExport(data, getCurrentUser().getUserID(), getCourseInstanceID(), reference, getFormat(), getSelectedGroup());
		stream = reference[0];
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		return "stream";
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public AssessmentSummaryExportData getData() {
		return data;
	}

	public void setData(AssessmentSummaryExportData data) {
		this.data = data;
	}
	
	public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}
	
	public Long getSelectedGroup() {
		return selectedGroup;
	}
	
	public void setSelectedGroup(Long selectedGroup) {
		this.selectedGroup = selectedGroup;
	}
}
