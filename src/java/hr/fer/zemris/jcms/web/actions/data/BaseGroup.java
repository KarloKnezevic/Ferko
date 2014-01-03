package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.service.has.HasGroup;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class BaseGroup extends BaseCourseInstance implements HasGroup {

	public Group group;
	
	public BaseGroup(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	@Override
	public Group getGroup() {
		return group;
	}

	@Override
	public void setGroup(Group group) {
		this.group = group;
	}

}
