package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.ISVUFileItemBean;
import hr.fer.zemris.jcms.parsers.ISVUFileParser;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseStudentsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

@Deprecated
public class SynchronizeCourseStudents extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private SynchronizeCourseStudentsData data = null;
	private String text;
	private String semester;
	
    public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		data = new SynchronizeCourseStudentsData(MessageLoggerFactory.createMessageLogger(this));

		List<ISVUFileItemBean> items = null;
		try {
			items = ISVUFileParser.parseTabbedFormat(new StringReader(text==null ? "" : text));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Format podataka je neispravan!");
			return INPUT;
		}
		SynchronizerService.SynchronizeCourseStudentsData(data, getCurrentUser().getUserID(), getSemester(), items, "upload");
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
		data = new SynchronizeCourseStudentsData(MessageLoggerFactory.createMessageLogger(this));
		SynchronizerService.SynchronizeCourseStudentsData(data, getCurrentUser().getUserID(), getSemester(), null, "input");
        return INPUT;
    }

    public String getSemester() {
		return semester;
	}
    public void setSemester(String semester) {
		this.semester = semester;
	}
    
    public SynchronizeCourseStudentsData getData() {
		return data;
	}
    public void setData(SynchronizeCourseStudentsData data) {
		this.data = data;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
