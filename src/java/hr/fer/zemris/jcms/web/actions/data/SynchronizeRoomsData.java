package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class SynchronizeRoomsData extends AbstractActionData {

	private String currentSemesterID;
	
	public SynchronizeRoomsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getCurrentSemesterID() {
		return currentSemesterID;
	}
	public void setCurrentSemesterID(String currentSemesterID) {
		this.currentSemesterID = currentSemesterID;
	}
}
