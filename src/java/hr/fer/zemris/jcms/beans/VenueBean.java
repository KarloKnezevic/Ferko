package hr.fer.zemris.jcms.beans;

public class VenueBean {

	private String shortName;
	private String name;
	private String address;
	private String locator;

	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getLocator() {
		return locator;
	}
	public void setLocator(String locator) {
		this.locator = locator;
	}
}
