package hr.fer.zemris.jcms.web.actions.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link ShowCourse}.
 *  
 * @author marcupic
 *
 */
public class ShowCourseEventsData extends BaseCourseInstance {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String sDateFrom;
	private String sDateTo;
	private Date dateFrom;
	private Date dateTo;
	private String courseInstanceID;

	private List<AbstractEvent> events = new ArrayList<AbstractEvent>();
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ShowCourseEventsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<AbstractEvent> getEvents() {
		return events;
	}
	
	public void setEvents(List<AbstractEvent> events) {
		this.events = events;
	}
	
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
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
