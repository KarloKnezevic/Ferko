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
public class ApplStudents extends ApplNamedElement {
	
	private String text;
	private int min;
	private int max;
	
	public ApplStudents(String name, int min, int max, String text) {
		super(4, name);
		this.text = text;
		this.min = min;
		this.max = max;
	}

	public String getText() {
		return text;
	}

	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
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
	public boolean validate(IMessageLogger messageLogger) {
		String val = (String)getUserData();
		int cnt = 0;
		if(val!=null) {
			val = val.trim();
			if(!val.isEmpty()) {
				String[] elems = StringUtil.split(val, '\n');
				cnt = elems.length;
			}
		}
		if(cnt>max) {
			messageLogger.addErrorMessage(messageLogger.getText("Error.applTooManyStudents",new String[] {getName(),String.valueOf(cnt),String.valueOf(max)}));
			return false;
		}
		if(cnt<min) {
			messageLogger.addErrorMessage(messageLogger.getText("Error.applTooFewStudents",new String[] {getName(),String.valueOf(cnt),String.valueOf(min)}));
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "Elem[type=students name="+getName()+" min="+min+" max="+max+"]";
	}

	@Override
	public boolean isUserInput() {
		return true;
	}
}
