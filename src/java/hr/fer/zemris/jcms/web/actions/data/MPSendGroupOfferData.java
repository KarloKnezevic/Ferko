package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.MPOfferBean;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.service.has.HasParent;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class MPSendGroupOfferData extends BaseCourseInstance implements HasParent {

	private Group parent;
	private	MPOfferBean bean = new MPOfferBean();
	
	public MPSendGroupOfferData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public Group getParent() {
		return parent;
	}
	public void setParent(Group parent) {
		this.parent = parent;
	}

    public MPOfferBean getBean() {
		return bean;
	}
    public void setBean(MPOfferBean bean) {
		this.bean = bean;
	}
}
