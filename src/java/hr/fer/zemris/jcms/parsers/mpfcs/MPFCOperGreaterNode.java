package hr.fer.zemris.jcms.parsers.mpfcs;

import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;

import java.io.Serializable;
import java.util.Set;

public class MPFCOperGreaterNode extends MPFCBooleanValueNode implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private MPFCIntValueNode left;
	private MPFCIntValueNode right;
	
	public MPFCOperGreaterNode(MPFCIntValueNode left, MPFCIntValueNode right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean value(MPFormulaContext context) {
		int delta = left.value(context) - right.value(context);
		if(delta>0) return true;
		delta = -delta +1;
		context.addViolationMeasure(delta);
		return false;
	}
	
	@Override
	public void extractGroupNames(Set<String> groupNames) {
		left.extractGroupNames(groupNames);
		right.extractGroupNames(groupNames);
	}
}
