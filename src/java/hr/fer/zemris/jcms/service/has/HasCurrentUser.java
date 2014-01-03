package hr.fer.zemris.jcms.service.has;

import hr.fer.zemris.jcms.model.User;

public interface HasCurrentUser {

	public void setCurrentUser(User currentUser);
	public User getCurrentUser();
	
}
