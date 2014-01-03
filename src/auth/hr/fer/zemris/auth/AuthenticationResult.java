/*
 * Created on Oct 6, 2004
 */
package hr.fer.zemris.auth;

/**
 * @author marcupic
 */
public final class AuthenticationResult {
	private boolean success;
	private String message;
	
	public AuthenticationResult(boolean success, String message) {
		this.success = success;
		this.message = message;
		}
	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @return Returns the success.
	 */
	public boolean isSuccess() {
		return success;
	}
}
