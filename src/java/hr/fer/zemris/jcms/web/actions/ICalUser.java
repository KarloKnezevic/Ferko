package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.ICalService;

import java.io.Serializable;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletResponseAware;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;

import com.opensymphony.xwork2.ActionSupport;

public class ICalUser extends ActionSupport implements Serializable, ServletResponseAware {

	private static final long serialVersionUID = 1L;
	
	private String key;
	private HttpServletResponse response;

	@Override
	public String execute() throws Exception {
		if(key==null) return NONE;
		Calendar cal = null;
		try {
			cal = ICalService.getCalendarForKey(key);
		} catch (SecurityException e) {
			return NONE;
		}
		response.setContentType("text/calendar");
		response.setCharacterEncoding("UTF-8");
		final CalendarOutputter output = new CalendarOutputter(false);
		output.output(cal, response.getOutputStream());
		return NONE;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setServletResponse(HttpServletResponse arg0) {
		response = arg0;
	}
	
}
