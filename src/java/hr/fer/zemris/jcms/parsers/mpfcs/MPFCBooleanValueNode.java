package hr.fer.zemris.jcms.parsers.mpfcs;

import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;

import java.io.Serializable;

public abstract class MPFCBooleanValueNode extends MPFCNode implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract boolean value(MPFormulaContext context);
	
}
