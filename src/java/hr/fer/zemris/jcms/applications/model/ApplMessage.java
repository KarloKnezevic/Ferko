package hr.fer.zemris.jcms.applications.model;

import java.util.Properties;

/**
 * Kod programski definirane prijave, ovo je ne-interaktivni element 
 * koji omogućava da se korisniku na ekran ispiše poruka.
 * 
 * @author marcupic
 */
public class ApplMessage extends ApplNamedElement {

	private String text;
	
	public ApplMessage(String name, String text) {
		super(3, name);
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	@Override
	public void loadUsersData(Properties prop) {
	}
	
	@Override
	public void storeUsersData(Properties prop) {
	}
	
	@Override
	public String toString() {
		return "Elem[type=message name="+getName()+"]";
	}
	
}
