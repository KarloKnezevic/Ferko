package hr.fer.zemris.jcms.parsers.mpfcs;

import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;

import java.io.Serializable;

public abstract class MPFCIntValueNode extends MPFCNode implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract int value(MPFormulaContext context);
	
}
