package hr.fer.zemris.jcms.web.actions.data.support;

import hr.fer.zemris.jcms.web.interceptors.data.DelayedMessageProducer;
import hr.fer.zemris.jcms.web.interceptors.data.DelayedMessagesAware;
import hr.fer.zemris.jcms.web.interceptors.data.DelayedMessagesStore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Implementacija sučelje {@link IMessageLogger} koja se za lokalizaciju oslanja
 * na mogučnosti Struts2 frameworka.
 * 
 * @author marcupic
 *
 */
public class MessageLoggerImpl implements IMessageLogger {
	private MessageContainerImpl msgContainer;
	private ActionSupport action;
	private String registrationKey;
	private SimpleDateFormat shortDateFormat;
	private SimpleDateFormat fullDateFormat;
	
	private SimpleDateFormat getShortDateFormat() {
		if(shortDateFormat==null) {
			shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		}
		return shortDateFormat;
	}
	private SimpleDateFormat getFullDateFormat() {
		if(fullDateFormat==null) {
			fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		return fullDateFormat;
	}
	@Override
	public String formatDate(Date date) {
		return date==null ? "" : getShortDateFormat().format(date);
	}
	@Override
	public String formatDateTime(Date date) {
		return date==null ? "" : getFullDateFormat().format(date);
	}

	/**
	 * Konstruktor.
	 * 
	 * @param action objekt koji se koristi za lokalizaciju poruka.
	 * @param importDelayedMessages 
	 */
	public MessageLoggerImpl(ActionSupport action, boolean importDelayedMessages) {
		super();
		if(action==null) {
			throw new IllegalArgumentException("action object must not be null!");
		}
		this.action = action;
		this.msgContainer = new MessageContainerImpl();
		if(importDelayedMessages) {
			if(!(action instanceof DelayedMessagesAware)) {
				throw new IllegalStateException("Action does not implement DelayedMessagesAware interface.");
			}
			IMessageContainer c = ((DelayedMessagesAware)action).getDelayedMessageContainer();
			this.msgContainer.addAll(c);
		}
	}

	@Override
	public IMessageContainer getMessageContainer() {
		return msgContainer;
	}
	
	@Override
	public void addFieldErrorMessage(String fieldName, String messageText) {
		msgContainer.addFieldErrorMessage(fieldName, messageText);
		action.addFieldError(fieldName, messageText);
	}

	@Override
	public void addErrorMessage(String messageText) {
		msgContainer.addErrorMessage(messageText);
		action.addActionError(messageText);
	}

	@Override
	public void addInfoMessage(String messageText) {
		msgContainer.addInfoMessage(messageText);
	}

	@Override
	public void addMessage(LoggerMessageType messageType, String messageText) {
		msgContainer.addMessage(messageType, messageText);
		if(messageType==LoggerMessageType.ERROR) {
			action.addActionError(messageText);
		} else {
			action.addActionMessage(messageText);
		}
	}

	@Override
	public void addWarningMessage(String messageText) {
		msgContainer.addWarningMessage(messageText);
		action.addActionMessage(messageText);
	}

	@Override
	public void clearMessages() {
		msgContainer.clearMessages();
		action.clearErrorsAndMessages();
	}

	@Override
	public List<LoggerMessage> getMessages() {
		return msgContainer.getMessages();
	}

	@Override
	public String getText(String key) {
		return action.getText(key);
	}

	@Override
	public String getText(String key, String defaultValue) {
		return action.getText(key,defaultValue);
	}

	@Override
	public String getText(String key, String[] args) {
		return action.getText(key,args);
	}

	@Override
	public boolean hasErrorMessages() {
		return msgContainer.hasErrorMessages();
	}
	
	@Override
	public boolean hasInfoMessages() {
		return msgContainer.hasInfoMessages();
	}
	
	@Override
	public boolean hasMessages() {
		return msgContainer.hasMessages();
	}
	
	@Override
	public boolean hasWarningMessages() {
		return msgContainer.hasWarningMessages();
	}
	
	@Override
	public void registerAsDelayed() {
		if(registrationKey!=null) return;
		if(!(action instanceof DelayedMessageProducer)) {
			throw new IllegalStateException("Provided action does not implement DelayedMessageProducer.");
		}
		if(msgContainer.hasMessages() || msgContainer.hasPrivateMessages()) {
			ActionContext actionContext = ActionContext.getContext();
			Map<?,?> context = actionContext.getApplication();
			DelayedMessagesStore messageStore = (DelayedMessagesStore)context.get("jcms.messageStore");
			if(messageStore==null) {
				throw new IllegalStateException("No message store found.");
			}
			registrationKey = messageStore.put(msgContainer);
			((DelayedMessageProducer)action).setDmsgid(registrationKey);
		}
	}
}
