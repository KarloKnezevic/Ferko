package hr.fer.zemris.jcms.locking;

/**
 * Iznimka koja opisuje problem nastao kod izgradnje
 * objekta {@link LockPath}.
 * 
 * @author marcupic
 */
public class LockPathException extends Exception {

	private static final long serialVersionUID = 1L;

	public LockPathException(String message) {
		super(message);
	}

	public LockPathException(String message, Throwable cause) {
		super(message, cause);
	}

	
}
