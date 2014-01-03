package hr.fer.zemris.jcms.web.actions2.course.market;

import hr.fer.zemris.jcms.beans.ext.MPOfferBean;

import hr.fer.zemris.jcms.service2.course.market.MarketBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.MPAcceptOfferData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=MPAcceptOfferData.class,defaultNavigBuilder=BuilderDefault.class)
public class MPAcceptOffer extends Ext2ActionSupport<MPAcceptOfferData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
		data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
		data.setResult(AbstractActionData.RESULT_FATAL);
		return null;
    }
    
	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String acceptGroupOffer() throws Exception {
    	MarketBrowserService.acceptGroupOffer(getEntityManager(), data, "acceptGroupOffer");
        return null;
    }

	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String sendApprovalRequest() throws Exception {
    	MarketBrowserService.acceptGroupOffer(getEntityManager(), data, "sendApprovalRequest");
        return null;
    }

	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String acceptApproval() throws Exception {
    	MarketBrowserService.acceptGroupOffer(getEntityManager(), data, "acceptApproval");
        return null;
    }

	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String acceptDirectOffer() throws Exception {
    	MarketBrowserService.acceptGroupOffer(getEntityManager(), data, "acceptDirectOffer");
        return null;
    }

    public MPOfferBean getBean() {
		return data.getBean();
	}
    public void setBean(MPOfferBean bean) {
		data.setBean(bean);
	}
}
