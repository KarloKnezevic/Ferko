package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.MainData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;

@Deprecated
public class Main extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String sDateFrom;
	private String sDateTo;
	private Date dateFrom;
	private Date dateTo;
	private String currentYearSemesterID;
	// Vrsta kalendara:
	// 1 - semestar
	// 2 - mjesec
	// 3 - tjedan
	// 4 - dan
	// 5 - sljedećih 7 dana
	// 6 - sve što postoji
	private int calendarType = 1;
	
	private MainData data;
	
    public String execute() throws Exception {
    	if(!hasCurrentUser()) return NOT_LOGGED_IN;
    	checkDatesFilter();
    	data = new MainData(MessageLoggerFactory.createMessageLogger(this,true));
		BasicBrowsing.getMainData(data, getCurrentUser().getUserID(), dateFrom, dateTo, currentYearSemesterID, calendarType);
		if(data.getSelectedSemester()!=null) {
			currentYearSemesterID = data.getSelectedSemester().getId();
		}
        return SUCCESS;
    }

    private void checkDatesFilter() {
    	Cookie[] cookies = ServletActionContext.getRequest().getCookies();
    	if(cookies==null) {
			calendarType = 3;
			Calendar cal = Calendar.getInstance();
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			switch(dow) {
			case Calendar.MONDAY: break;
			case Calendar.TUESDAY: cal.add(Calendar.DAY_OF_WEEK, -1); break;
			case Calendar.WEDNESDAY: cal.add(Calendar.DAY_OF_WEEK, -2); break;
			case Calendar.THURSDAY: cal.add(Calendar.DAY_OF_WEEK, -3); break;
			case Calendar.FRIDAY: cal.add(Calendar.DAY_OF_WEEK, -4); break;
			case Calendar.SATURDAY: cal.add(Calendar.DAY_OF_WEEK, -5); break;
			case Calendar.SUNDAY: cal.add(Calendar.DAY_OF_WEEK, -6); break;
			}
			int dom = cal.get(Calendar.DAY_OF_MONTH); // od 1
			int moy = cal.get(Calendar.MONTH); // od 0
			int y = cal.get(Calendar.YEAR);
			String start = datum(y,moy+1,dom,0,0,0);
			cal.add(Calendar.DAY_OF_WEEK, 7);
			dom = cal.get(Calendar.DAY_OF_MONTH); // od 1
			moy = cal.get(Calendar.MONTH); // od 0
			y = cal.get(Calendar.YEAR);
			String end = datum(y,moy+1,dom,0,0,0);
			setSDateFrom(start);
			setSDateTo(end);
    		// calendarType = 1;
    		return;
    	}
    	if(this.dateFrom!=null || this.dateTo!=null) {
			calendarType = 0; // custom!
    		return;
    	}
    	for(Cookie c : cookies) {
    		if(!c.getName().equals("ferko_cal_date_filter")) continue;
    		String val = c.getValue();
    		if(val.equals("1")) {
    			calendarType = 1;
    			return;
    		} else if(val.equals("6")) {
    			calendarType = 6;
    			return;
    		} else if (val.equals("2")) {
    			calendarType = 2;
    			Calendar cal = Calendar.getInstance();
    			int moy = cal.get(Calendar.MONTH); // od 0
    			int y = cal.get(Calendar.YEAR);
    			String start = datum(y,moy+1,1,0,0,0);
    			moy++;
    			if(moy>Calendar.DECEMBER) {
    				moy = Calendar.JANUARY;
    				y++;
    			}
    			String end = datum(y,moy+1,1,0,0,0);
    			setSDateFrom(start);
    			setSDateTo(end);
    			return;
    			
    		} else if (val.equals("4")) {
    			calendarType = 4;
    			Calendar cal = Calendar.getInstance();
    			int dom = cal.get(Calendar.DAY_OF_MONTH); // od 1
    			int moy = cal.get(Calendar.MONTH); // od 0
    			int y = cal.get(Calendar.YEAR);
    			String start = datum(y,moy+1,dom,0,0,0);
    			String end = datum(y,moy+1,dom,23,59,59);
    			setSDateFrom(start);
    			setSDateTo(end);
    			return;
    			
    		} else if (val.equals("5")) {
    			// Sljedećih 7 dana
    			calendarType = 5;
    			Calendar cal = Calendar.getInstance();
    			int moy = cal.get(Calendar.MONTH); // od 0
    			int y = cal.get(Calendar.YEAR);
    			int dom = cal.get(Calendar.DAY_OF_MONTH); // od 1
    			String start = datum(y,moy+1,dom,0,0,0);
    			cal.add(Calendar.DAY_OF_WEEK, 7);
    			dom = cal.get(Calendar.DAY_OF_MONTH); // od 1
    			moy = cal.get(Calendar.MONTH); // od 0
    			y = cal.get(Calendar.YEAR);
    			String end = datum(y,moy+1,dom,0,0,0);
    			setSDateFrom(start);
    			setSDateTo(end);
    			return;
    		} 
    		break;
    	}
		calendarType = 3;
		Calendar cal = Calendar.getInstance();
		int dow = cal.get(Calendar.DAY_OF_WEEK);
		switch(dow) {
		case Calendar.MONDAY: break;
		case Calendar.TUESDAY: cal.add(Calendar.DAY_OF_WEEK, -1); break;
		case Calendar.WEDNESDAY: cal.add(Calendar.DAY_OF_WEEK, -2); break;
		case Calendar.THURSDAY: cal.add(Calendar.DAY_OF_WEEK, -3); break;
		case Calendar.FRIDAY: cal.add(Calendar.DAY_OF_WEEK, -4); break;
		case Calendar.SATURDAY: cal.add(Calendar.DAY_OF_WEEK, -5); break;
		case Calendar.SUNDAY: cal.add(Calendar.DAY_OF_WEEK, -6); break;
		}
		int dom = cal.get(Calendar.DAY_OF_MONTH); // od 1
		int moy = cal.get(Calendar.MONTH); // od 0
		int y = cal.get(Calendar.YEAR);
		String start = datum(y,moy+1,dom,0,0,0);
		cal.add(Calendar.DAY_OF_WEEK, 7);
		dom = cal.get(Calendar.DAY_OF_MONTH); // od 1
		moy = cal.get(Calendar.MONTH); // od 0
		y = cal.get(Calendar.YEAR);
		String end = datum(y,moy+1,dom,0,0,0);
		setSDateFrom(start);
		setSDateTo(end);
		return;
	}

	private String datum(int y, int mon, int day, int h, int m, int s) {
		StringBuilder sb = new StringBuilder(20);
		sb.append(y).append("-");
		if(mon<10) sb.append('0');
		sb.append(mon).append("-");
		if(day<10) sb.append('0');
		sb.append(day).append(" ");
		if(h<10) sb.append('0');
		sb.append(h).append(":");
		if(m<10) sb.append('0');
		sb.append(m).append(":");
		if(s<10) sb.append('0');
		sb.append(s);
		return sb.toString();
	}

	public int getCalendarType() {
		return calendarType;
	}
	
	public MainData getData() {
		return data;
	}
    public void setData(MainData data) {
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
				sDateFrom = null;
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
				sDateTo = null;
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

	public String getCurrentYearSemesterID() {
		return currentYearSemesterID;
	}

	public void setCurrentYearSemesterID(String currentYearSemesterID) {
		this.currentYearSemesterID = currentYearSemesterID;
		if(this.currentYearSemesterID!=null && this.currentYearSemesterID.equals("")) this.currentYearSemesterID = null;
	}
}
