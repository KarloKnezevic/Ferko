package hr.fer.zemris.jcms.web.interceptors.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageContainer;

public interface DelayedMessagesAware {
	public void setDelayedMessagesContainer(IMessageContainer delayedMessagesContainer);
	public IMessageContainer getDelayedMessageContainer();
}
