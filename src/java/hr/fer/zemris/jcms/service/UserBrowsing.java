package hr.fer.zemris.jcms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.beans.UGBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.web.actions.UserGroupSearchData;

public class UserBrowsing {

	public static UserGroupSearchData getUserGroupSearchData(final String term) {
		UserGroupSearchData data = null;
		data = PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<UserGroupSearchData>() {
			@Override
			public UserGroupSearchData executeOperation(EntityManager em) {
				UserGroupSearchData d = new UserGroupSearchData();
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				Map<String,String> courseNames = new HashMap<String, String>(); 
				List<UserGroup> userGroups = dh.getUserGroupDAO().search(em, term);
				for(UserGroup ug : userGroups) {
					UGBean ugb = new UGBean(ug.getUser());
					String ISVU = ug.getGroup().getCompositeCourseID().split("/")[1];
					if(!courseNames.containsKey(ISVU)) {
						Course c = dh.getCourseDAO().get(em, ISVU);
						String courseName = c.getName();
						courseNames.put(ISVU, courseName);
						ugb.setCourseName(courseName);
					} else {
						ugb.setCourseName(courseNames.get(ISVU));
					}
					ugb.setGroupName(ug.getGroup().getName());
					ugb.setGroupId(ug.getGroup().getId());
					d.getUsers().add(ugb);
				}
				return d;
			}
		});
		return data;
	}
}
