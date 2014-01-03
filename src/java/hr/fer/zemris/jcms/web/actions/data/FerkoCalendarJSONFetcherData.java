package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.service2.HomePageService.DayDescriptor;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class FerkoCalendarJSONFetcherData extends AbstractActionData {

	private List<AbstractEvent> events;

	private Date dateFrom;
	private Date dateTo;
	private String command;
	private String fatalMessage;
	private String sDateFrom;
	private String sDateTo;
	private Map<String,Integer> daysMap;
	List<DayDescriptor> dayDescriptors;
	
	public FerkoCalendarJSONFetcherData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<DayDescriptor> getDayDescriptors() {
		return dayDescriptors;
	}
	public void setDayDescriptors(List<DayDescriptor> dayDescriptors) {
		this.dayDescriptors = dayDescriptors;
	}
	public Map<String, Integer> getDaysMap() {
		return daysMap;
	}
	public void setDaysMap(Map<String, Integer> daysMap) {
		this.daysMap = daysMap;
	}
	public List<AbstractEvent> getEvents() {
		return events;
	}
	public void setEvents(List<AbstractEvent> events) {
		this.events = events;
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
	
	public void setCommand(String command) {
		this.command = command;
	}
	public String getCommand() {
		return command;
	}
	
	public String getFatalMessage() {
		return fatalMessage;
	}
	public void setFatalMessage(String fatalMessage) {
		this.fatalMessage = fatalMessage;
	}
	
	public String getSDateFrom() {
		return sDateFrom;
	}
	public void setSDateFrom(String sDateFrom) {
		this.sDateFrom = sDateFrom;
	}
	public String getSDateTo() {
		return sDateTo;
	}
	public void setSDateTo(String sDateTo) {
		this.sDateTo = sDateTo;
	}
}
