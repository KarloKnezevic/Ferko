package hr.fer.zemris.jcms.web.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.ShowCourseEventsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class ShowCourseEvents extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String sDateFrom;
	private String sDateTo;
	private Date dateFrom;
	private Date dateTo;
	private String courseInstanceID;
	private ShowCourseEventsData data = null;
	
    public String execute() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new ShowCourseEventsData(MessageLoggerFactory.createMessageLogger(this));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getShowCourseEventsData(data, getCurrentUser()==null ? null : getCurrentUser().getUserID(), getCourseInstanceID(), getDateFrom(), getDateTo());
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		setTitle(data.getCourseInstance().getCourse().getName());
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public ShowCourseEventsData getData() {
		return data;
	}
    
    public void setData(ShowCourseEventsData data) {
		this.data = data;
	}

	public String getSDateFrom() {
		return sDateFrom;
	}

	public void setSDateFrom(String dateFrom) {
		sDateFrom = dateFrom;
		if(sDateFrom!=null && !sDateFrom.equals("")) {
			try {
				this.dateFrom = sdf.parse(sDateFrom);
			} catch(ParseException ignorable) {
			}
		}
	}

	public String getSDateTo() {
		return sDateTo;
	}

	public void setSDateTo(String dateTo) {
		sDateTo = dateTo;
		if(sDateTo!=null && !sDateTo.equals("")) {
			try {
				this.dateTo = sdf.parse(sDateTo);
			} catch(ParseException ignorable) {
			}
		}
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}
}
