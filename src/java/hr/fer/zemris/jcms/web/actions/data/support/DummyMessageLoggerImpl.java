package hr.fer.zemris.jcms.web.actions.data.support;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Implementacija sučelje {@link IMessageLogger} koja se za lokalizaciju oslanja
 * na mogučnosti Struts2 frameworka.
 * 
 * @author marcupic
 *
 */
public class DummyMessageLoggerImpl implements IMessageLogger {
	private MessageContainerImpl msgContainer;
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
	 */
	public DummyMessageLoggerImpl() {
		super();
		this.msgContainer = new MessageContainerImpl();
	}

	@Override
	public void addFieldErrorMessage(String fieldName, String messageText) {
		msgContainer.addFieldErrorMessage(fieldName, messageText);
	}
	
	@Override
	public IMessageContainer getMessageContainer() {
		return msgContainer;
	}
	
	@Override
	public void addErrorMessage(String messageText) {
		msgContainer.addErrorMessage(messageText);
	}

	@Override
	public void addInfoMessage(String messageText) {
		msgContainer.addInfoMessage(messageText);
	}

	@Override
	public void addMessage(LoggerMessageType messageType, String messageText) {
		msgContainer.addMessage(messageType, messageText);
	}

	@Override
	public void addWarningMessage(String messageText) {
		msgContainer.addWarningMessage(messageText);
	}

	@Override
	public void clearMessages() {
		msgContainer.clearMessages();
	}

	@Override
	public List<LoggerMessage> getMessages() {
		return msgContainer.getMessages();
	}

	@Override
	public String getText(String key) {
		return key;
	}

	@Override
	public String getText(String key, String defaultValue) {
		return key;
	}

	@Override
	public String getText(String key, String[] args) {
		return key;
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
		throw new IllegalStateException("Dummy implementation does not provide this functionality.");
	}
}
