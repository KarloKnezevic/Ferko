package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.parsers.mpfcs.MPFCBooleanValueNode;

import java.io.Serializable;
import java.util.Set;

public class MPFormulaConstraint implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private MPFCBooleanValueNode node;
	
	public MPFormulaConstraint(MPFCBooleanValueNode node) {
		this.node = node;
	}

	public boolean isSatisfied(MPFormulaContext context) {
		return node.value(context);
	}
	
	public void extractGroupNames(Set<String> groupNames) {
		node.extractGroupNames(groupNames);
	}
}
