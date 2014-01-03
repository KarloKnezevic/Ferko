package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.AdminSendPSMessage;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminSendPSMessage}.
 *  
 * @author marcupic
 *
 */
public class AdminSendPSMessageData extends AbstractActionData {

	private String name;
	private String key;
	private String value;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminSendPSMessageData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
