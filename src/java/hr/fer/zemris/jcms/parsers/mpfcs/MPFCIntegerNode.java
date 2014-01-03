package hr.fer.zemris.jcms.parsers.mpfcs;

import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;

import java.io.Serializable;
import java.util.Set;

public class MPFCIntegerNode extends MPFCIntValueNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int number;
	
	public MPFCIntegerNode(int number) {
		super();
		this.number = number;
	}

	@Override
	public int value(MPFormulaContext context) {
		return number;
	}
	
	@Override
	public void extractGroupNames(Set<String> groupNames) {
	}
}
