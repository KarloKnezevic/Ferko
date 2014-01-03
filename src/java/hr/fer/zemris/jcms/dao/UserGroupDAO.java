package hr.fer.zemris.jcms.dao;

import java.util.List;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;

import javax.persistence.EntityManager;

public interface UserGroupDAO {

	public UserGroup get(EntityManager em, Long id);
	public void save(EntityManager em, UserGroup userGroup);
	public void remove(EntityManager em, UserGroup userGroup);
	public UserGroup find(EntityManager em, User user, Group group);
	public List<UserGroup> search(EntityManager em, String term);
}
