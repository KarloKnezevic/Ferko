package hr.fer.zemris.jcms.web.actions2.course.market;

import hr.fer.zemris.jcms.beans.ext.MPViewBean;

import hr.fer.zemris.jcms.service2.course.market.MarketBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.MPViewData;

@WebClass(dataClass=MPViewData.class)
public class MPView extends Ext2ActionSupport<MPViewData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
    	return view();
    }
    
	@WebMethodInfo
    public String view() throws Exception {
		MarketBrowserService.viewMarketPlace(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

    public Long getParentID() {
		return data.getParentID();
	}
    public void setParentID(Long parentID) {
		data.setParentID(parentID);
	}

    public MPViewBean getBean() {
		return data.getBean();
	}
    public void setBean(MPViewBean bean) {
		data.setBean(bean);
	}
}
