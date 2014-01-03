package hr.fer.zemris.jcms.web.actions;


import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationExportTableData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class ApplicationExportTable extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;


	private ApplicationExportTableData data = null;
	private String format;
	private String courseInstanceID;

    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new ApplicationExportTableData(MessageLoggerFactory.createMessageLogger(this, true));
		// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		ApplicationService.getApplicationExportTableData(data, getCurrentUser().getUserID(), courseInstanceID, format);
    	} else {
    		return NO_PERMISSION;
    	}
        return SUCCESS;
   
    }

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public ApplicationExportTableData getData() {
		return data;
	}

	public void setData(ApplicationExportTableData data) {
		this.data = data;
	}
    
}
