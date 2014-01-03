package hr.fer.zemris.jcms.web.actions2.course.market;

import hr.fer.zemris.jcms.beans.ext.MarketPlaceBean;

import hr.fer.zemris.jcms.service2.course.market.MarketBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.MPGroupsAdminData;

@WebClass(dataClass=MPGroupsAdminData.class)
public class MPGroupsAdmin extends Ext2ActionSupport<MPGroupsAdminData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
    	return input();
    }
    
	@WebMethodInfo
    public String input() throws Exception {
		MarketBrowserService.adminMarketPlace(getEntityManager(), data, "input");
		return null;
    }

	@WebMethodInfo
    public String update() throws Exception {
		MarketBrowserService.adminMarketPlace(getEntityManager(), data, "update");
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

    public MarketPlaceBean getBean() {
		return data.getBean();
	}
    public void setBean(MarketPlaceBean bean) {
		data.setBean(bean);
	}
}
