package hr.fer.zemris.jcms.parsers.mpfcs;

import java.io.Serializable;
import java.util.Set;

public abstract class MPFCNode implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Metoda zadužena za popunjavanje predanog skupa svim
	 * grupama o kojima pravilo ovisi.
	 * 
	 * @param groupNames
	 */
	public abstract void extractGroupNames(Set<String> groupNames);
	
}
