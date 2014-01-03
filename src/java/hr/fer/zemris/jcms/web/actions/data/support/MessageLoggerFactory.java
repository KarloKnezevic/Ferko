package hr.fer.zemris.jcms.web.actions.data.support;


import com.opensymphony.xwork2.ActionSupport;

/**
 * Objekt zadužen za stvaranje lokaliziranih poruka.
 * 
 * @author marcupic
 *
 */
public class MessageLoggerFactory {

	/**
	 * Vraća objekt koji se može koristiti za lokalizaciju poruka.
	 * 
	 * @param action struts2 akcija za koju se ovaj objekt stvara 
	 * @return objekt za lokalizaciju poruka
	 */
	public static IMessageLogger createMessageLogger(ActionSupport action) {
		return new MessageLoggerImpl(action, false);
	}

	public static IMessageLogger createMessageLogger(ActionSupport action, boolean importDelayedMessages) {
		return new MessageLoggerImpl(action, importDelayedMessages);
	}
	
	public static IMessageLogger createDummyMessageLogger() {
		return new DummyMessageLoggerImpl();
	}
}
