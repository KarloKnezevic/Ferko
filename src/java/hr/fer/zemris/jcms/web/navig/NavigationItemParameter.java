package hr.fer.zemris.jcms.web.navig;

/**
 * Parametar koji je potreban za izradu linka navigacije.
 * 
 * @author marcupic
 */
public class NavigationItemParameter {
	private String name;
	private Object value;
	
	public NavigationItemParameter() {
	}

	public NavigationItemParameter(String name, Object value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Parameter: "+name+" = "+value;
	}
}
