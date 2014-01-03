package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.GroupScheduleBean;
import hr.fer.zemris.jcms.parsers.GroupScheduleParser;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseLectureScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class SynchronizeCourseLectureSchedule extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private SynchronizeCourseLectureScheduleData data = null;
	private String text;
	private String semester;
	
    public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		data = new SynchronizeCourseLectureScheduleData(MessageLoggerFactory.createMessageLogger(this));
		List<GroupScheduleBean> items = null;
		try {
			items = GroupScheduleParser.parseTabbedFormat(new StringReader(text==null ? "" : text));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Format podataka je neispravan!");
			return INPUT;
		}
		SynchronizerService.SynchronizeCourseLectureScheduleData(data, getCurrentUser().getUserID(), getSemester(), items, "upload");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
			return INPUT;
		}
		data.getMessageLogger().registerAsDelayed();
        return SUCCESS;
    }

    public String execute() throws Exception {
    	return input();
    }

    public String input() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		data = new SynchronizeCourseLectureScheduleData(MessageLoggerFactory.createMessageLogger(this));
		SynchronizerService.SynchronizeCourseLectureScheduleData(data, getCurrentUser().getUserID(), getSemester(), null, "input");
        return INPUT;
    }

    public String getSemester() {
		return semester;
	}
    public void setSemester(String semester) {
		this.semester = semester;
	}
    
    public SynchronizeCourseLectureScheduleData getData() {
		return data;
	}
    public void setData(SynchronizeCourseLectureScheduleData data) {
		this.data = data;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
