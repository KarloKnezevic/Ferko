package hr.fer.zemris.jcms.web.actions2.course.market;

import hr.fer.zemris.jcms.beans.ext.MPOfferBean;

import hr.fer.zemris.jcms.service2.course.market.MarketBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.MPDirectMoveData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

import java.text.SimpleDateFormat;
import java.util.Date;

@WebClass(dataClass=MPDirectMoveData.class,defaultNavigBuilder=BuilderDefault.class)
public class MPDirectMove extends Ext2ActionSupport<MPDirectMoveData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String execute() throws Exception {
    	return view();
    }
    
	@WebMethodInfo(lockPath="ml\\ci${bean.courseInstanceID}\\g\\g${bean.parentID}")
    public String view() throws Exception {
    	MarketBrowserService.directMove(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return null;
		}
		if(data.getMovedFromGroup()!=null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println("[BURZA]["+sdf.format(new Date())+"] Preselio korisnika "+data.getMovedUser().getJmbag()+" iz "+data.getMovedFromGroup().getName()+" u "+data.getMovedToGroup().getName());
		}
        return null;
    }

    public MPOfferBean getBean() {
		return data.getBean();
	}
    public void setBean(MPOfferBean bean) {
		data.setBean(bean);
	}
}
