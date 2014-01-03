/*
 * Created on Oct 6, 2004
 */
package hr.fer.zemris.auth;

/**
 * @author marcupic
 */
public interface IAuthenticator {
	public void setProperty(String name, Object value);
	public AuthenticationResult authenticate();
	public AuthenticationResult authenticate(String username, String password);
}
