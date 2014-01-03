package hr.fer.zemris.jcms.web.actions;


import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationExportListData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class ApplicationExportList extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;


	private ApplicationExportListData data = null;
	private String format;
	private String courseInstanceID;
	private Long definitionID;

    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new ApplicationExportListData(MessageLoggerFactory.createMessageLogger(this, true));
		// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		ApplicationService.getApplicationExportListData(data, getCurrentUser().getUserID(), courseInstanceID, definitionID, format);
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

	public Long getDefinitionID() {
		return definitionID;
	}

	public void setDefinitionID(Long definitionID) {
		this.definitionID = definitionID;
	}

	public ApplicationExportListData getData() {
		return data;
	}

	public void setData(ApplicationExportListData data) {
		this.data = data;
	}
    
}
