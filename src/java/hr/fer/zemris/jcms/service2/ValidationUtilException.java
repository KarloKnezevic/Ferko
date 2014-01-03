package hr.fer.zemris.jcms.service2;

/**
 * Ovu iznimku bacit ce {@link ValidationUtil} ne uspije napraviti
 * traženu konverziju. Pažnja: ovisno o pozvanoj metodi, moguće je
 * da je poruka pogreške već negdje i zapisana.
 * 
 * @author marcupic
 */
public class ValidationUtilException extends Exception {

	private static final long serialVersionUID = 1L;

	public ValidationUtilException() {
	}

	public ValidationUtilException(String message) {
		super(message);
	}

	public ValidationUtilException(Throwable cause) {
		super(cause);
	}

	public ValidationUtilException(String message, Throwable cause) {
		super(message, cause);
	}

}
