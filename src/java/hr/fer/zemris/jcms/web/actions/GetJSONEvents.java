package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperImpl;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.EventsService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

public class GetJSONEvents extends ExtendedActionSupport {

	private static final long serialVersionUID = 1L;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	List<AbstractEvent> events;
	Date dateFrom = new Date();
	Date dateTo = new Date();
	

    public String execute() {
    	if(!hasCurrentUser()) return SUCCESS;
    	try {
			dateTo=sdf.parse("2009-10-10 00:00:00");
		} catch (ParseException e) {
			dateTo=new Date();
		}
        events = PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<List<AbstractEvent>>() {
			@Override
			public List<AbstractEvent> executeOperation(EntityManager em) {
				DAOHelper dh = new DAOHelperImpl();
				User user = dh.getUserDAO().getUserById(em, getCurrentUser().getUserID()); // TODO: bedasto
				return EventsService.listForUser(em, user, dateFrom, dateTo);
			}
		});
        return SUCCESS;
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


	public List<AbstractEvent> getEvents() {
		return events;
	}



}
