package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ActivityBean;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class FeedData extends AbstractActionData {

	private List<ActivityBean> activityBeans;
	private String key;
	private String which;
	private String format = "rss_2.0";
	
	public FeedData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<ActivityBean> getActivityBeans() {
		return activityBeans;
	}
	public void setActivityBeans(List<ActivityBean> activityBeans) {
		this.activityBeans = activityBeans;
	}
	
	public String getWhich() {
		return which;
	}
	public void setWhich(String which) {
		this.which = which;
	}
	
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
