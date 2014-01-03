package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ActivityBean;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class ShowActivitiesData extends AbstractActionData {

	private List<ActivityBean> activityBeans;
	
	public ShowActivitiesData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<ActivityBean> getActivityBeans() {
		return activityBeans;
	}
	public void setActivityBeans(List<ActivityBean> activityBeans) {
		this.activityBeans = activityBeans;
	}
}
