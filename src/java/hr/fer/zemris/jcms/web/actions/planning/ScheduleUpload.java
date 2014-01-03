package hr.fer.zemris.jcms.web.actions.planning;

import hr.fer.zemris.jcms.service.PlanningService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.PlanningData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import java.io.File;
import com.opensymphony.xwork2.Preparable;

public class ScheduleUpload extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private PlanningData data = null;
	private File schedule;
	private String archiveContentType;
	private String archiveFileName;
	private String courseInstanceID;
	private String planID;
	
	@Override
	public void prepare() throws Exception {
		data = new PlanningData(MessageLoggerFactory.createMessageLogger(this));
	}

    public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	PlanningService.uploadSchedule(data, schedule, getCurrentUser().getUserID(), courseInstanceID, planID);
    	data.getMessageLogger().registerAsDelayed();
    	return "listPlans";
    }

    public String execute() throws Exception {
    	return upload();
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

	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public PlanningData getData() {
		return data;
	}

	public void setData(PlanningData data) {
		this.data = data;
	}

	public File getSchedule() {
		return schedule;
	}

	public void setSchedule(File schedule) {
		this.schedule = schedule;
	}

	public String getPlanID() {
		return planID;
	}

	public void setPlanID(String planID) {
		this.planID = planID;
	}

    
}
