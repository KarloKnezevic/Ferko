package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.GroupCoarseStatData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class GroupCoarseStat extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String semesterID;
	private String parentRelativePath;
	private GroupCoarseStatData data = null;

    public String execute() throws Exception {
    	return input();
    }
    
    public String view() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new GroupCoarseStatData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getGroupCoarseStatData(data, getCurrentUser().getUserID(), getSemesterID(), getParentRelativePath(), "view");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return SUCCESS;
    }

    public String input() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new GroupCoarseStatData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getGroupCoarseStatData(data, getCurrentUser().getUserID(), getSemesterID(), getParentRelativePath(), "input");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return INPUT;
    }

    public GroupCoarseStatData getData() {
		return data;
	}
    public void setData(GroupCoarseStatData data) {
		this.data = data;
	}

    public String getParentRelativePath() {
		return parentRelativePath;
	}
    public void setParentRelativePath(String parentRelativePath) {
		this.parentRelativePath = parentRelativePath;
	}
    
    public String getSemesterID() {
		return semesterID;
	}
    public void setSemesterID(String semesterID) {
		this.semesterID = semesterID;
	}
}
