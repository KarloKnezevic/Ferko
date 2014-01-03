package hr.fer.zemris.jcms.web.actions;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;

public class SetCalendarType extends ExtendedActionSupport {

	private static final long serialVersionUID = 3L;

	private String calendarType;
	
	// Vrsta kalendara:
	// 1 - semestar
	// 2 - mjesec
	// 3 - tjedan
	// 4 - dan
	// 5 - sljedećih 7 dana
	// 6 - sve
	
	public String execute() throws Exception {
    	updateCalendarType();
        return SUCCESS;
    }

    public String getCalendarType() {
		return calendarType;
	}

	public void setCalendarType(String calendarType) {
		this.calendarType = calendarType;
	}

	private void updateCalendarType() {
		Cookie c = null;
		if("day".equals(calendarType)) {
	    	c = new Cookie("ferko_cal_date_filter","4");
		} else if("semester".equals(calendarType)) {
	    	c = new Cookie("ferko_cal_date_filter","1");
		} else if("month".equals(calendarType)) {
	    	c = new Cookie("ferko_cal_date_filter","2");
		} else if("week".equals(calendarType)) {
	    	c = new Cookie("ferko_cal_date_filter","3");
		} else if("next7".equals(calendarType)) {
	    	c = new Cookie("ferko_cal_date_filter","5");
		} else if("all".equals(calendarType)) {
	    	c = new Cookie("ferko_cal_date_filter","6");
		}
		if(c!=null) {
			c.setMaxAge(60*60*24*30);
			c.setPath("/");
	    	ServletActionContext.getResponse().addCookie(c);
		}
	}
}
