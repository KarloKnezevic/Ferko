package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.MPOfferBean;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.has.HasParent;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class MPDirectMoveData extends BaseCourseInstance implements HasParent {

	private Group parent;
	private MarketPlace marketPlace;
	private Group movedFromGroup;
	private Group movedToGroup;
	private User movedUser;
	private	MPOfferBean bean = new MPOfferBean();

	public MPDirectMoveData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

    public MPOfferBean getBean() {
		return bean;
	}
    public void setBean(MPOfferBean bean) {
		this.bean = bean;
	}

	public Group getParent() {
		return parent;
	}
	public void setParent(Group parent) {
		this.parent = parent;
	}

	public MarketPlace getMarketPlace() {
		return marketPlace;
	}

	public void setMarketPlace(MarketPlace marketPlace) {
		this.marketPlace = marketPlace;
	}

	public Group getMovedFromGroup() {
		return movedFromGroup;
	}

	public void setMovedFromGroup(Group movedFromGroup) {
		this.movedFromGroup = movedFromGroup;
	}

	public Group getMovedToGroup() {
		return movedToGroup;
	}

	public void setMovedToGroup(Group movedToGroup) {
		this.movedToGroup = movedToGroup;
	}
	
	public User getMovedUser() {
		return movedUser;
	}
	public void setMovedUser(User movedUser) {
		this.movedUser = movedUser;
	}
}
