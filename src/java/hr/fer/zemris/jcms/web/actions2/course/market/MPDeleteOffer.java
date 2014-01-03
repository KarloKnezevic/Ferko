package hr.fer.zemris.jcms.web.actions2.course.market;

import hr.fer.zemris.jcms.beans.ext.MPOfferBean;

import hr.fer.zemris.jcms.service2.course.market.MarketBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.MPDeleteOfferData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=MPDeleteOfferData.class,defaultNavigBuilder=BuilderDefault.class)
public class MPDeleteOffer extends Ext2ActionSupport<MPDeleteOfferData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String execute() throws Exception {
		MarketBrowserService.deleteOffer(getEntityManager(), data);
        return null;
    }

    public MPOfferBean getBean() {
		return data.getBean();
	}
    public void setBean(MPOfferBean bean) {
		data.setBean(bean);
	}
}
