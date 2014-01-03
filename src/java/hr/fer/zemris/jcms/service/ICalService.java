package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.dao.DAOHelperImpl;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserDescriptor;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Geo;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

public class ICalService {
	
	@SuppressWarnings("unchecked")
	public static Calendar getCalendarForKey(String key) {
		
		final Calendar cal = new Calendar();
		cal.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
		cal.getProperties().add(Version.VERSION_2_0);
		cal.getProperties().add(CalScale.GREGORIAN);
		cal.getProperties().add(new Method("PUBLISH")); // ?? RFC 2446
		cal.getProperties().add(new net.fortuna.ical4j.model.property.XProperty("X-WR-CALNAME","Ferko kalendar"));
		cal.getProperties().add(new net.fortuna.ical4j.model.property.XProperty("X-WR-TIMEZONE","Europe/Warsaw"));
		cal.getProperties().add(new net.fortuna.ical4j.model.property.XProperty("X-WR-CALDESC","Kalendar sustava Ferko"));
		
		final List<VEvent> vevents = new LinkedList<VEvent>();
		final Long userID = Long.parseLong(key.split(":")[0],16);
		final String externalID = key.split(":")[1];
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				User user = new DAOHelperImpl().getUserDAO().getUserById(em, userID); 
				if(user==null) throw new SecurityException();
				UserDescriptor userd = user.getUserDescriptor();
				if(!userd.getExternalID().equals(externalID)) throw new SecurityException();
				java.util.Calendar dateTo = java.util.Calendar.getInstance();
				dateTo.add(java.util.Calendar.YEAR, 1);
				java.util.Calendar dateFrom = java.util.Calendar.getInstance();
				dateFrom.add(java.util.Calendar.YEAR, -2);
				List<AbstractEvent> events = EventsService.listForUser(em, user, dateFrom.getTime(), dateTo.getTime());
				for(AbstractEvent event : events) {
					if(event==null) continue;
					VEvent vevent = new VEvent(new DateTime(event.getStart()), event.getTitle());
					java.util.Calendar end = new java.util.GregorianCalendar();
					end.setTime(event.getStart());
					end.add(java.util.Calendar.MINUTE, event.getDuration());
					vevent.getProperties().add(new DtEnd(new DateTime(end.getTime())));
					end.add(java.util.Calendar.HOUR, -48);
					//vevent.getProperties().getProperty(Property.DTSTAMP).getParameters().add(Value.DATE);
					//vevent.getProperties().getProperty(Property.DTSTAMP).getParameters().add(new DateTime(end.getTime()));
					StringBuilder builder = new StringBuilder();
					if(event.getRoom()==null) {
						builder.append("N/A");
					} else {
						builder.append(event.getRoom().getShortName()).append(" (").append(event.getRoom().getVenue().getName()).append(")");
					}
					vevent.getProperties().add(new Location(builder.toString()));
					
					if(event.getRoom()!=null) {
						String locator = event.getRoom().getVenue().getLocator();
						if(locator!=null && locator.startsWith("geo:")) {
							vevent.getProperties().add(new Geo(locator.split(":")[1]));
						}
					}
					vevent.getProperties().add(new Uid(event.getId().toString())); // ?
					vevents.add(vevent);
				}
				return null;
			}});

		cal.getComponents().addAll(vevents);
		return cal;
	}
}
