package hr.fer.zemris.jcms.security;

public class JCMSSManNotInitializedException extends JCMSSecurityException {

	private static final long serialVersionUID = 1L;

	public JCMSSManNotInitializedException() {
		super();
	}

	public JCMSSManNotInitializedException(String message, Throwable cause) {
		super(message, cause);
	}

	public JCMSSManNotInitializedException(String message) {
		super(message);
	}

	public JCMSSManNotInitializedException(Throwable cause) {
		super(cause);
	}
}
