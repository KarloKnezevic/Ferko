package hr.fer.zemris.jcms.locking;

/**
 * Iznimka koja će biti izazvana ako ne bude moguće
 * napraviti traženo otključavanje. Primjerice, to će
 * se dogoditi ako se pokuša otključati nešto što dretva 
 * nije zaključala već je netko drugi zaključao, ili
 * ako se pokuša zaključati nešto što uopće nije zaključano.
 *  
 * @author marcupic
 *
 */
public class UnlockException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnlockException() {
	}

	public UnlockException(String message) {
		super(message);
	}

	public UnlockException(Throwable cause) {
		super(cause);
	}

	public UnlockException(String message, Throwable cause) {
		super(message, cause);
	}

}
