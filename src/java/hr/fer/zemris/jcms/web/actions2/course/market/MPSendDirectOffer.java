package hr.fer.zemris.jcms.web.actions2.course.market;

import hr.fer.zemris.jcms.beans.ext.MPOfferBean;

import hr.fer.zemris.jcms.service2.course.market.MarketBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.MPSendDirectOfferData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=MPSendDirectOfferData.class,defaultNavigBuilder=BuilderDefault.class)
public class MPSendDirectOffer extends Ext2ActionSupport<MPSendDirectOfferData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String execute() throws Exception {
    	return view();
    }
    
	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String view() throws Exception {
		MarketBrowserService.sendDirectOffer(getEntityManager(), data);
        return null;
    }

    public MPOfferBean getBean() {
		return data.getBean();
	}
    public void setBean(MPOfferBean bean) {
		data.setBean(bean);
	}
}
