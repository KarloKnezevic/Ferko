package hr.fer.zemris.jcms.web.actions.data.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MessageContainerImpl implements IMessageContainer {

	private boolean fieldErrorMessages = false;
	private boolean nonFieldErrorMessages = false;
	private boolean infoMessages = false;
	private boolean warningMessages = false;
	private List<LoggerMessage> messages = new ArrayList<LoggerMessage>();
	private Map<String, Object> privateMessages;
	
	public void addFieldErrorMessage(String fieldName, String messageText) {
		messages.add(new LoggerMessage(LoggerMessageType.ERROR, messageText, fieldName));
		fieldErrorMessages = true;
	}

	public void addErrorMessage(String messageText) {
		messages.add(new LoggerMessage(LoggerMessageType.ERROR, messageText));
		nonFieldErrorMessages = true;
	}

	public void addInfoMessage(String messageText) {
		messages.add(new LoggerMessage(LoggerMessageType.INFO, messageText));
		infoMessages = true;
	}

	public void addMessage(LoggerMessageType messageType, String messageText) {
		switch(messageType) {
		case ERROR:
			addErrorMessage(messageText);
			return;
		case INFO:
			addInfoMessage(messageText);
			return;
		case WARNING:
			addWarningMessage(messageText);
			return;
		}
		messages.add(new LoggerMessage(messageType, messageText));
	}

	public void addWarningMessage(String messageText) {
		messages.add(new LoggerMessage(LoggerMessageType.WARNING, messageText));
		warningMessages = true;
	}

	@Override
	public void clearMessages() {
		messages.clear();
		fieldErrorMessages = false;
		nonFieldErrorMessages = false;
		warningMessages = false;
		infoMessages = false;
	}

	@Override
	public List<LoggerMessage> getMessages() {
		return messages;
	}

	@Override
	public boolean hasErrorMessages() {
		return fieldErrorMessages || nonFieldErrorMessages;
	}
	
	@Override
	public boolean hasInfoMessages() {
		return infoMessages;
	}
	
	@Override
	public boolean hasMessages() {
		return !messages.isEmpty();
	}
	
	@Override
	public boolean hasWarningMessages() {
		return warningMessages;
	}

	@Override
	public boolean hasFieldErrorMessages() {
		return fieldErrorMessages;
	}
	
	@Override
	public boolean hasNonFieldErrorMessages() {
		return nonFieldErrorMessages;
	}
	
	public void addAll(IMessageContainer c) {
		if(c==null) return;
		messages.addAll(c.getMessages());
		fieldErrorMessages |= c.hasFieldErrorMessages();
		nonFieldErrorMessages |= c.hasNonFieldErrorMessages();
		warningMessages |= c.hasWarningMessages();
		infoMessages |= c.hasInfoMessages();
		Map<String, Object> pm = c.getPrivateMessages();
		if(pm!=null && !pm.isEmpty()) {
			privateMessages = new HashMap<String, Object>(pm);
		}
	}
	
	@Override
	public Map<String, Object> getPrivateMessages() {
		return privateMessages;
	}
	
	@Override
	public void addPrivateData(String key, String value) {
		if(privateMessages==null) {
			privateMessages = new HashMap<String, Object>();
		}
		privateMessages.put(key, value);
	}
	
	@Override
	public String getPrivateMessage(String key) {
		if(privateMessages==null) return null;
		return (String)privateMessages.get(key);
	}
	
	@Override
	public boolean hasPrivateMessages() {
		if(privateMessages==null) return false;
		return !privateMessages.isEmpty();
	}
}
