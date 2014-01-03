package hr.fer.zemris.jcms.applications.model;

/**
 * Kod programski definirane prijave, ovo je opcija koja
 * se koristi kod odabira jedne od ponuđenih opcija ili
 * više od ponuđenih opcija. 
 * 
 * @author marcupic
 */
public class ApplOption {
	private String key;
	private String value;
	private boolean other;
	private boolean enabled = true;
	private int index;
	
	public ApplOption(String key, String value, boolean other) {
		super();
		this.key = key;
		this.value = value;
		this.other = other;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isOther() {
		return other;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public int getIndex() {
		return index;
	}
	void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplOption other = (ApplOption) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return key+"("+(enabled?"E":"e")+(other?",o":"")+"): "+value;
	}
}
