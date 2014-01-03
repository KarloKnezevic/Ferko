package hr.fer.zemris.jcms.service.has;

import hr.fer.zemris.jcms.model.Group;

public interface HasParent {

	public void setParent(Group parent);
	public Group getParent();

}
