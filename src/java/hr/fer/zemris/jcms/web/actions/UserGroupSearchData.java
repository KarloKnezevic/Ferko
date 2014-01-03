package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.UGBean;

import java.util.LinkedList;
import java.util.List;

public class UserGroupSearchData {

	private List<UGBean> users = new LinkedList<UGBean>();
	
	public List<UGBean> getUsers() {
		return users;
	}
	public void setUsers(List<UGBean> users) {
		this.users = users;
	}
}
