package hr.fer.zemris.jcms.applications.model;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

import java.util.Properties;

/**
 * Kod programski definirane prijave, ovo je interaktivni element 
 * koji omogućava korisniku da unese neki tekst.
 * 
 * @author marcupic
 */
public class ApplText extends ApplNamedElement {
	
	private String text;
	
	public ApplText(String name, String text) {
		super(2, name);
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	@Override
	public void loadUsersData(Properties prop) {
		setUserData(prop.getProperty("d."+getName()));
	}
	
	@Override
	public void storeUsersData(Properties prop) {
		String data = (String)getUserData();
		if(data==null) data = "";
		prop.setProperty("d."+getName(), data);
	}
	
	@Override
	public String toString() {
		return "Elem[type=text name="+getName()+"]";
	}
	
	@Override
	public boolean validate(IMessageLogger messageLogger) {
		String val = (String)getUserData();
		if(StringUtil.isStringBlank(val)) {
			messageLogger.addErrorMessage(messageLogger.getText("Error.applicationFieldProblem", new String[] {getName()}));
			return false;
		}
		return true;
	}

	@Override
	public boolean isUserInput() {
		return true;
	}
}
