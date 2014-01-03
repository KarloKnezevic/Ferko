package hr.fer.zemris.jcms.parsers.mpfcs;

import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;

import java.io.Serializable;
import java.util.Set;

public class MPFCSubNode extends MPFCIntValueNode implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private MPFCIntValueNode left;
	private MPFCIntValueNode right;
	
	public MPFCSubNode(MPFCIntValueNode left, MPFCIntValueNode right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public int value(MPFormulaContext context) {
		return left.value(context) - right.value(context);
	}
	
	@Override
	public void extractGroupNames(Set<String> groupNames) {
		left.extractGroupNames(groupNames);
		right.extractGroupNames(groupNames);
	}
}
