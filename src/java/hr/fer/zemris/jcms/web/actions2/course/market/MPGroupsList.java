package hr.fer.zemris.jcms.web.actions2.course.market;

import hr.fer.zemris.jcms.service2.course.market.MarketBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.MPGroupsListData;

@WebClass(dataClass=MPGroupsListData.class)
public class MPGroupsList extends Ext2ActionSupport<MPGroupsListData> {

	private static final long serialVersionUID = 2L;
	
	@WebMethodInfo
    public String execute() throws Exception {
		MarketBrowserService.getMarketPlacesListForCourseInstance(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
}
