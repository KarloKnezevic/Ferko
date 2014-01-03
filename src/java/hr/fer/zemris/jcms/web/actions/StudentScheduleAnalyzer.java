package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.ScheduleAnalyzerService;
import hr.fer.zemris.jcms.web.actions.data.StudentScheduleAnalyzerData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class StudentScheduleAnalyzer extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String semesterID;
	private String dateFrom;
	private String dateTo;
	private StudentScheduleAnalyzerData data = null;
	private String jmbagsList;
	private String courseInstanceID;
	
	private boolean createOccupancyMap;
	
    public String execute() throws Exception {
    	return input();
    }
    
    public String viewForSemester() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new StudentScheduleAnalyzerData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		ScheduleAnalyzerService.getStudentScheduleAnalyzerData(data, getCurrentUser().getUserID(), getSemesterID(), getDateFrom(), getDateTo(), null, isCreateOccupancyMap(), getCourseInstanceID(), "viewForSemester");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
	        return "inputSemester";
		}
        return "viewForSemester";
    }

    public String showApplet() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new StudentScheduleAnalyzerData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		ScheduleAnalyzerService.getStudentScheduleAnalyzerAppletData(data, getCurrentUser().getUserID(), getSemesterID(), getDateFrom(), getDateTo(), getJmbagsList(), isCreateOccupancyMap(), getCourseInstanceID());
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
	        return "inputSemesterAndUsers";
		}
        return "showApplet";
    }

    public String viewForSemesterAndUsers() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new StudentScheduleAnalyzerData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		ScheduleAnalyzerService.getStudentScheduleAnalyzerData(data, getCurrentUser().getUserID(), getSemesterID(), getDateFrom(), getDateTo(), getJmbagsList(), isCreateOccupancyMap(), getCourseInstanceID(), "viewForSemesterAndUsers");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
	        return "inputSemesterAndUsers";
		}
        return "viewForSemesterAndUsers";
    }

    public String inputSemester() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new StudentScheduleAnalyzerData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		ScheduleAnalyzerService.getStudentScheduleAnalyzerData(data, getCurrentUser().getUserID(), getSemesterID(), getDateFrom(), getDateTo(), null, isCreateOccupancyMap(), getCourseInstanceID(), "inputSemester");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return "inputSemester";
    }

    public String inputSemesterAndUsers() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new StudentScheduleAnalyzerData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		ScheduleAnalyzerService.getStudentScheduleAnalyzerData(data, getCurrentUser().getUserID(), getSemesterID(), getDateFrom(), getDateTo(), getJmbagsList(), isCreateOccupancyMap(), getCourseInstanceID(), "inputSemesterAndUsers");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return "inputSemesterAndUsers";
    }

    public StudentScheduleAnalyzerData getData() {
		return data;
	}
    public void setData(StudentScheduleAnalyzerData data) {
		this.data = data;
	}

    public String getSemesterID() {
		return semesterID;
	}
    public void setSemesterID(String semesterID) {
		this.semesterID = semesterID;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}
    
	public String getJmbagsList() {
		return jmbagsList;
	}
	public void setJmbagsList(String jmbagsList) {
		this.jmbagsList = jmbagsList;
	}
	
	public boolean isCreateOccupancyMap() {
		return createOccupancyMap;
	}
	public void setCreateOccupancyMap(boolean createOccupancyMap) {
		this.createOccupancyMap = createOccupancyMap;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
}
