package hr.fer.zemris.jcms.applications.model;

import java.util.Properties;

/**
 * Kod programski definirane prijave, ovo je razred iz kojeg
 * se izvode svi elementi koji imaju svoje ime. 
 * 
 * @author marcupic
 */
public abstract class ApplNamedElement extends ApplElement {

	private String name;

	public ApplNamedElement(int kind, String name) {
		super(kind);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public void loadState(Properties prop) {
		setEnabled(prop.getProperty("en."+name,"0").equals("1"));
	}
	
	@Override
	public void storeState(Properties prop) {
		prop.setProperty("en."+name, isEnabled() ? "1" : "0");
	}
	
	@Override
	public boolean isStateEqual(ApplElement other) {
		if(this.getClass()!=other.getClass()) return false;
		if(this.isEnabled()!=other.isEnabled()) return false;
		ApplNamedElement o = (ApplNamedElement)other;
		return this.getName().equals(o.getName());
	}
}
