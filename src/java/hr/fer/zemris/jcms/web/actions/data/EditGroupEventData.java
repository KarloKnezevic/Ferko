package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.beans.RoomBean;
import hr.fer.zemris.jcms.beans.ext.GroupEventBean;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class EditGroupEventData extends BaseGroup {

	private GroupEventBean bean = new GroupEventBean();
	private List<RoomBean> rooms;
	private Long groupID;
	
	public EditGroupEventData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<RoomBean> getRooms() {
		return rooms;
	}
	public void setRooms(List<RoomBean> rooms) {
		this.rooms = rooms;
	}
	
	public GroupEventBean getBean() {
		return bean;
	}
	public void setBean(GroupEventBean bean) {
		this.bean = bean;
	}
	
	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}
}
