package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.MPGroupSettingsViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Deprecated
public class MPGroupSettingsView extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String semesterID;
	private String parentRelativePath;
	private MPGroupSettingsViewData data = null;
	private Set<Long> selectedMarketPlaces = new HashSet<Long>();
	
    public String execute() throws Exception {
    	return input();
    }
    
    public String view() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new MPGroupSettingsViewData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getMPGroupSettingsViewData(data, getCurrentUser().getUserID(), getSemesterID(), getParentRelativePath(), "view");
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
		data = new MPGroupSettingsViewData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getMPGroupSettingsViewData(data, getCurrentUser().getUserID(), getSemesterID(), getParentRelativePath(), "input");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return INPUT;
    }

    public String openMPs() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new MPGroupSettingsViewData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.changeMPGroupSettingsViewData(data, getCurrentUser().getUserID(), getSelectedMarketPlaces(), true);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		BasicBrowsing.getMPGroupSettingsViewData(data, getCurrentUser().getUserID(), getSemesterID(), getParentRelativePath(), "view");
		return SUCCESS;
    }

    public String closeMPs() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new MPGroupSettingsViewData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.changeMPGroupSettingsViewData(data, getCurrentUser().getUserID(), getSelectedMarketPlaces(), false);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		BasicBrowsing.getMPGroupSettingsViewData(data, getCurrentUser().getUserID(), getSemesterID(), getParentRelativePath(), "view");
		return SUCCESS;
    }

    public MPGroupSettingsViewData getData() {
		return data;
	}
    public void setData(MPGroupSettingsViewData data) {
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
    
    public Set<Long> getSelectedMarketPlaces() {
		return selectedMarketPlaces;
	}
    public void setSelectedMarketPlaces(Set<Long> selectedMarketPlaces) {
		this.selectedMarketPlaces = selectedMarketPlaces;
	}
}
