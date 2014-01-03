package hr.fer.zemris.jcms.web.actions.data.support;


public class LoggerMessage {
	
	private String messageText;
	private LoggerMessageType messageType;
	private String fieldName;
	
	public LoggerMessage(LoggerMessageType messageType, String messageText, String fieldName) {
		super();
		this.messageText = messageText;
		this.messageType = messageType;
		this.fieldName = fieldName;
	}

	public LoggerMessage(LoggerMessageType messageType, String messageText) {
		this(messageType, messageText, null);
	}

	public String getMessageText() {
		return messageText;
	}
	
	public LoggerMessageType getMessageType() {
		return messageType;
	}
	
	public String getFieldName() {
		return fieldName;
	}
}
