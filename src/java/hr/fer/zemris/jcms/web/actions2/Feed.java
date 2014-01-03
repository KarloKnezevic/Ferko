package hr.fer.zemris.jcms.web.actions2;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

import hr.fer.zemris.jcms.beans.ActivityBean;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.service2.ActivityService;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.FeedData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.util.InputStreamWrapper;
import hr.fer.zemris.util.StringUtil;

@WebClass(dataClass=FeedData.class, defaultNavigBuilder=BuilderDefault.class)
public class Feed extends Ext2ActionSupport<FeedData> {

	private static final long serialVersionUID = 1L;
	
	private InputStreamWrapper streamWrapper;

	@WebMethodInfo(loginCheck=false,
		dataResultMappings={
			@DataResultMapping(dataResult="not-found",struts2Result="not-found",registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="not-found", navigBuilder=BuilderDefault.class,transactionalMethod=@TransactionalMethod(closeImmediately=true)),
			@Struts2ResultMapping(struts2Result=WRAPPED_STREAM, navigBuilder=BuilderDefault.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))
		}
	)
	public String execute() throws IOException, JSONException {
		if(data.getCurrentUser()==null) {
			BasicServiceSupport.setUserFromExternalID(getEntityManager(), data.getKey(), data);
		} else {
			User user = BasicServiceSupport.retrieveUserFromExternalID(getEntityManager(), data.getKey());
			if(user==null || !user.equals(data.getCurrentUser())) {
				data.setResult("not-found");
				return null;
			}
		}
		if(data.getCurrentUser()==null) {
			data.setResult("not-found");
			return null;
		}

		String content_type = "application/xml; charset=UTF-8";
		if(!StringUtil.isStringBlank(data.getFormat()) && !data.getFormat().equals("rss_2.0") && !data.getFormat().equals("atom_1.0")) {
			data.setFormat("rss_2.0");
		}
		if(data.getFormat().equals("rss_2.0")) {
			// content_type = "application/xml; charset=utf-8";
			content_type = "application/rss+xml; charset=utf-8";
			// content_type = "text/xml";
		} else if(data.getFormat().equals("atom_1.0")) {
			// content_type = "application/xml; charset=utf-8";
			content_type = "application/atom+xml; charset=utf-8";
			// content_type = "text/xml";
		}
		if(StringUtil.isStringBlank(data.getWhich())) data.setWhich("activities");
		String[] elems = StringUtil.split(data.getWhich(), ',');
		Set<String> requests = new LinkedHashSet<String>();
		for(String s : elems) {
			requests.add(s.toString());
		}
		
		SyndFeed feed = new SyndFeedImpl();
		feed.setTitle("Ferko RSS Feed");
		feed.setLink("https://ferko.fer.hr/ferko");
		feed.setDescription("Dostava informacija uživo s Ferka!");
		feed.setFeedType(data.getFormat());
		
		List<SyndEntry> entires = new ArrayList<SyndEntry>();
		for(String req : requests) {
			if(req.equals("activities")) {
				List<ActivityBean> beans = ActivityService.fetchForCurrentSemestar(getEntityManager(), getData().getCurrentUser(), getData().getMessageLogger());
				for(ActivityBean b : beans) {
					SyndEntry entry = new SyndEntryImpl();
					entry.setTitle(b.getMessage());
					entry.setPublishedDate(b.getDate());
					SyndContent content = new SyndContentImpl();
					content.setType("html");
					content.setValue("Detalje možete pogledati na <a href=\"https://ferko.fer.hr/ferko\">Ferku</a>.");
					entry.setDescription(content);
					entires.add(entry);
				}
			}
		}
		feed.setEntries(entires);
		
		SyndFeedOutput output = new SyndFeedOutput();
		StringWriter w = new StringWriter(2048);
		try {
			output.output(feed, w);
		} catch (FeedException e) {
			e.printStackTrace();
			data.setResult("not-found");
			return null;
		}
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
				w.toString(),
				content_type);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return null;
	}

	public String getKey() {
		return data.getKey();
	}
	public void setKey(String key) {
		data.setKey(key);
	}

	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
	
	public String getWhich() {
		return data.getWhich();
	}
	public void setWhich(String which) {
		data.setWhich(which);
	}
	
	public String getFormat() {
		return data.getFormat();
	}
	public void setFormat(String format) {
		data.setFormat(format);
	}

}
