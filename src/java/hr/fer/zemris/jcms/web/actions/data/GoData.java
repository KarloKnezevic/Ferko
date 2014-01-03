package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class GoData extends AbstractActionData {

	private AbstractEvent event;
	private Object object;

	public GoData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public AbstractEvent getEvent() {
		return event;
	}
	
	public void setEvent(AbstractEvent event) {
		this.event = event;
	}
	
	public Object getObject() {
		return object;
	}
	
	public void setObject(Object object) {
		this.object = object;
	}
}
