package hr.fer.zemris.jcms.web.actions;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageContainer;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.LoggerMessage;
import hr.fer.zemris.jcms.web.actions.data.support.LoggerMessageType;
import hr.fer.zemris.util.StringUtil;

import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.TextProviderSupport;

public class Struts2TextProvider extends TextProviderSupport implements IMessageLogger {
	
	private static class MyLocaleProvider implements LocaleProvider {
	@Override
		public Locale getLocale() {
			return StringUtil.HR_LOCALE;
		}
	}
	
	public Struts2TextProvider() {
		super(Struts2TextProvider.class, new MyLocaleProvider());
	}

	@Override
	public void addErrorMessage(String messageText) {
		// Do nothing. This class is for localization purposes only!
	}

	@Override
	public void addFieldErrorMessage(String fieldName, String messageText) {
		// Do nothing. This class is for localization purposes only!
	}

	@Override
	public void addInfoMessage(String messageText) {
		// Do nothing. This class is for localization purposes only!
	}

	@Override
	public void addMessage(LoggerMessageType messageType, String messageText) {
		// Do nothing. This class is for localization purposes only!
	}

	@Override
	public void addWarningMessage(String messageText) {
		// Do nothing. This class is for localization purposes only!
	}

	@Override
	public void clearMessages() {
		// Do nothing. This class is for localization purposes only!
	}

	@Override
	public String formatDate(Date date) {
		// Do nothing. This class is for localization purposes only!
		return null;
	}

	@Override
	public String formatDateTime(Date date) {
		// Do nothing. This class is for localization purposes only!
		return null;
	}

	@Override
	public IMessageContainer getMessageContainer() {
		// Do nothing. This class is for localization purposes only!
		return null;
	}

	@Override
	public List<LoggerMessage> getMessages() {
		// Do nothing. This class is for localization purposes only!
		return null;
	}

	@Override
	public boolean hasErrorMessages() {
		// Do nothing. This class is for localization purposes only!
		return false;
	}

	@Override
	public boolean hasInfoMessages() {
		// Do nothing. This class is for localization purposes only!
		return false;
	}

	@Override
	public boolean hasMessages() {
		// Do nothing. This class is for localization purposes only!
		return false;
	}

	@Override
	public boolean hasWarningMessages() {
		// Do nothing. This class is for localization purposes only!
		return false;
	}

	@Override
	public void registerAsDelayed() {
		// Do nothing. This class is for localization purposes only!
	}
	
}
