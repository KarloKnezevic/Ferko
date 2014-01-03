package hr.fer.zemris.jcms.web.actions2;

import java.io.IOException;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.parsers.json.JSONWriter;
import hr.fer.zemris.jcms.service2.HomePageService;
import hr.fer.zemris.jcms.service2.HomePageService.DayDescriptor;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.FerkoCalendarJSONFetcherData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.util.InputStreamWrapper;

@WebClass(dataClass=FerkoCalendarJSONFetcherData.class, defaultNavigBuilder=BuilderDefault.class)
public class FerkoCalendarJSONFetcher extends Ext2ActionSupport<FerkoCalendarJSONFetcherData> {

	private static final long serialVersionUID = 1L;
	private InputStreamWrapper streamWrapper;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@WebMethodInfo(
			dataResultMappings={
				@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false),
				@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false)
			},
			struts2ResultMappings={
				@Struts2ResultMapping(struts2Result=WRAPPED_STREAM, navigBuilder=BuilderDefault.class)
			}
		)
	public String execute() throws IOException, JSONException {
		HomePageService.getFerkoWeekCalendar(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("ERR");
			jw.key("message").value(data.getFatalMessage());
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
    		return null;
		}
		StringWriter writer = new StringWriter();
		JSONWriter jw = new JSONWriter(writer).object();
		jw.key("status").value("OK");
		jw.key("startDate").value(data.getSDateFrom().substring(0, 10));
		jw.key("endDate").value(data.getSDateTo().substring(0, 10));
		jw.key("dateMap").object();
		for(Map.Entry<String,Integer> e : data.getDaysMap().entrySet()) {
			jw.key(e.getKey()).value(e.getValue().intValue());
		}
		jw.endObject();
		jw.key("dateDesc").array();
		for(DayDescriptor dd : data.getDayDescriptors()) {
			jw.object();
			jw.key("i").value(dd.getIndex());
			jw.key("t").value(dd.getTitle());
			jw.key("d").value(dd.getDate());
			jw.endObject();
		}
		jw.endArray();
		jw.key("calEvents").array();
		for(AbstractEvent ev : data.getEvents()) {
			jw.object();
			jw.key("i").value(ev.getId());
			jw.key("t").value(ev.getTitle());
			jw.key("r").value(ev.getRoom()==null ? "" : ev.getRoom().getShortName());
			jw.key("c").value(ev.getContext()==null ? "" : ev.getContext());
			jw.key("s").value(data.formatDateTime(ev.getStart()));
			jw.key("d").value(ev.getDuration());
			jw.endObject();
		}
		jw.endArray();
		jw.endObject();
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
				writer.toString(), 
				"application/json");
		return null;
	}
	
	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}
	
	public String getSDateFrom() {
		return data.getSDateFrom();
	}

	public void setSDateFrom(String dateFrom) {
		data.setSDateFrom(dateFrom);
		if(data.getSDateFrom()!=null && !data.getSDateFrom().equals("")) {
			try {
				data.setDateFrom(sdf.parse(data.getSDateFrom()));
			} catch(ParseException ignorable) {
				data.setSDateFrom(null);
			}
		}
	}

	public String getSDateTo() {
		return data.getSDateTo();
	}

	public void setSDateTo(String dateTo) {
		data.setSDateTo(dateTo);
		if(data.getSDateTo()!=null && !data.getSDateTo().equals("")) {
			try {
				data.setDateTo(sdf.parse(data.getSDateTo()));
			} catch(ParseException ignorable) {
				data.setSDateTo(null);
			}
		}
	}

	public Date getDateFrom() {
		return data.getDateFrom();
	}

	public void setDateFrom(Date dateFrom) {
		data.setDateFrom(dateFrom);
	}

	public Date getDateTo() {
		return data.getDateTo();
	}

	public void setDateTo(Date dateTo) {
		data.setDateTo(dateTo);
	}

	public void setCommand(String command) {
		data.setCommand(command);
	}
	public String getCommand() {
		return data.getCommand();
	}

	/**
	 * Namjerno nema nicega; ideja ovog polja jest da se
	 * osigura da zahtjev svakog korisnika bude jedinstven
	 * tako da eventualni mehanizmi pohrane odgovora u cache
	 * budu lokalizirani za svakog korisnika. Idealno, tekst
	 * koji se ovdje salje morao bi biti sastavljen od identifikatora
	 * korisnika, te nekakvog timestamp-a.
	 * 
	 * @param cachePrevention nekakav tekst
	 */
	public void setCachePrevention(String cachePrevention) {
	}
}
