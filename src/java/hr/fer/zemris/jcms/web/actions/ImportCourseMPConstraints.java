package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.ConstraintsImportBean;
import hr.fer.zemris.jcms.parsers.ConstraintsImportParser;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.web.actions.data.ImportCourseMPConstraintsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class ImportCourseMPConstraints extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private ImportCourseMPConstraintsData data = null;
	private String text;
	private String semester;
	private String parentGroupRelativePath;
	private boolean resetCapacities;
	private boolean resetConstraints;
	
    public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		data = new ImportCourseMPConstraintsData(MessageLoggerFactory.createMessageLogger(this));
		List<ConstraintsImportBean> items = null;
		try {
			items = ConstraintsImportParser.parseTabbedFormat(new StringReader(text==null ? "" : text));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Format podataka je neispravan!");
			return INPUT;
		}
		SynchronizerService.getImportCourseMPConstraintsData(data, getCurrentUser().getUserID(), getSemester(), items, parentGroupRelativePath, resetCapacities, resetConstraints, "upload");
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
		data = new ImportCourseMPConstraintsData(MessageLoggerFactory.createMessageLogger(this));
		SynchronizerService.getImportCourseMPConstraintsData(data, getCurrentUser().getUserID(), getSemester(), null, null, false, false, "input");
        return INPUT;
    }

    public String getSemester() {
		return semester;
	}
    public void setSemester(String semester) {
		this.semester = semester;
	}
    
    public ImportCourseMPConstraintsData getData() {
		return data;
	}
    public void setData(ImportCourseMPConstraintsData data) {
		this.data = data;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public String getParentGroupRelativePath() {
		return parentGroupRelativePath;
	}
	public void setParentGroupRelativePath(String parentGroupRelativePath) {
		this.parentGroupRelativePath = parentGroupRelativePath;
	}

	public boolean isResetCapacities() {
		return resetCapacities;
	}

	public void setResetCapacities(boolean resetCapacities) {
		this.resetCapacities = resetCapacities;
	}

	public boolean isResetConstraints() {
		return resetConstraints;
	}

	public void setResetConstraints(boolean resetConstraints) {
		this.resetConstraints = resetConstraints;
	}
	
}
