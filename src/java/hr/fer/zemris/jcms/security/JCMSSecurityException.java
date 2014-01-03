package hr.fer.zemris.jcms.security;

public class JCMSSecurityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JCMSSecurityException() {
		super();
	}

	public JCMSSecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public JCMSSecurityException(String message) {
		super(message);
	}

	public JCMSSecurityException(Throwable cause) {
		super(cause);
	}

}
