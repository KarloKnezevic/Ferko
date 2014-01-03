package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.LabScheduleBean;
import hr.fer.zemris.jcms.parsers.LabScheduleTextListParser;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeLabScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

@Deprecated
public class SynchronizeLabSchedule extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private SynchronizeLabScheduleData data = null;
	private String text;
	private String semester;
	
    public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		data = new SynchronizeLabScheduleData(MessageLoggerFactory.createMessageLogger(this));
		List<LabScheduleBean> items = null;
		try {
			items = LabScheduleTextListParser.parseTabbedFormat(new StringReader(text==null ? "" : text));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Format podataka je neispravan!");
			return INPUT;
		}
		SynchronizerService.getSynchronizeLabScheduleData(data, getCurrentUser().getUserID(), getSemester(), items, "upload");
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
		data = new SynchronizeLabScheduleData(MessageLoggerFactory.createMessageLogger(this));
		SynchronizerService.getSynchronizeLabScheduleData(data, getCurrentUser().getUserID(), getSemester(), null, "input");
        return INPUT;
    }

    public String getSemester() {
		return semester;
	}
    public void setSemester(String semester) {
		this.semester = semester;
	}
    
    public SynchronizeLabScheduleData getData() {
		return data;
	}
    public void setData(SynchronizeLabScheduleData data) {
		this.data = data;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
