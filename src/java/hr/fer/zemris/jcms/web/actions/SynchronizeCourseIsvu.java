package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseIsvuData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.File;

@Deprecated
public class SynchronizeCourseIsvu extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private SynchronizeCourseIsvuData data = null;
	private File archive;
	private String archiveContentType;
	private String archiveFileName;
	private String semester;
	
    public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		data = new SynchronizeCourseIsvuData(MessageLoggerFactory.createMessageLogger(this));
		SynchronizerService.synchronizeCourseIsvuData(data, getCurrentUser().getUserID(), getSemester(), archive, "upload");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
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
		data = new SynchronizeCourseIsvuData(MessageLoggerFactory.createMessageLogger(this));
		SynchronizerService.synchronizeCourseIsvuData(data, getCurrentUser().getUserID(), getSemester(), archive, "input");
        return INPUT;
    }

    public String getSemester() {
		return semester;
	}
    public void setSemester(String semester) {
		this.semester = semester;
	}
    
    public SynchronizeCourseIsvuData getData() {
		return data;
	}
    public void setData(SynchronizeCourseIsvuData data) {
		this.data = data;
	}
    
    public File getArchive() {
		return archive;
	}
    public void setArchive(File archive) {
		this.archive = archive;
	}

    public String getArchiveContentType() {
		return archiveContentType;
	}
    public void setArchiveContentType(String archiveContentType) {
		this.archiveContentType = archiveContentType;
	}

    public String getArchiveFileName() {
		return archiveFileName;
	}
    public void setArchiveFileName(String archiveFileName) {
		this.archiveFileName = archiveFileName;
	}

}
