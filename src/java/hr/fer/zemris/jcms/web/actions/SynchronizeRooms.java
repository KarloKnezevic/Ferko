package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.RoomBean;
import hr.fer.zemris.jcms.parsers.RoomParser;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeRoomsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class SynchronizeRooms extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private SynchronizeRoomsData data = null;
	private String text;
	
    public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		data = new SynchronizeRoomsData(MessageLoggerFactory.createMessageLogger(this));
		List<RoomBean> items = null;
		try {
			items = RoomParser.parseTabbedFormat(new StringReader(text==null ? "" : text));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Format podataka je neispravan!");
			return INPUT;
		}
		SynchronizerService.getSynchronizeRoomsData(data, getCurrentUser().getUserID(), items);
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
		data = new SynchronizeRoomsData(MessageLoggerFactory.createMessageLogger(this));
		SynchronizerService.getSynchronizeRoomsData(data, getCurrentUser().getUserID(), null);
        return INPUT;
    }

    public SynchronizeRoomsData getData() {
		return data;
	}
    public void setData(SynchronizeRoomsData data) {
		this.data = data;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
