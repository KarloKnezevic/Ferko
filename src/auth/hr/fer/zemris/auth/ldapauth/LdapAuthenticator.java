/*
 * Created on Oct 6, 2004
 */
package hr.fer.zemris.auth.ldapauth;

import hr.fer.zemris.auth.AuthenticationResult;
import hr.fer.zemris.auth.AuthenticatorConsts;
import hr.fer.zemris.auth.IAuthenticator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author marcupic
 */
public class LdapAuthenticator implements IAuthenticator {

	public static final String AC_PROVIDER_URL = "ac_provider_url";
	public static final String AC_LDAP_SPECIFIC = "ac_ldap_specific://";

	private String username;

	private String password;

	private String provider_url;

	private Hashtable<Object,Object> ldspec = new Hashtable<Object,Object>();
	
	public LdapAuthenticator() {
	}

	public LdapAuthenticator(String provider_url) {
		this.provider_url = provider_url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hr.fer.zemris.auth.IAuthenticator#authenticate()
	 */
	public AuthenticationResult authenticate() {
		// Do logic here...
		AuthenticationResult ar = null;
		Hashtable<Object,Object> hashtableEnvironment = new Hashtable<Object,Object>(ldspec);
		hashtableEnvironment.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		hashtableEnvironment
				.put(Context.PROVIDER_URL, provider_url);
		hashtableEnvironment.put(Context.SECURITY_PRINCIPAL, username);
		hashtableEnvironment.put(Context.SECURITY_CREDENTIALS, password);
		if(!hashtableEnvironment.containsKey("java.naming.ldap.version")) {
			hashtableEnvironment.put("java.naming.ldap.version", "3");
		}
		try {
			Context context = new InitialContext(hashtableEnvironment);
			context.close();
			ar = new AuthenticationResult(true, null);
		} catch (AuthenticationException ae) {
			ar = new AuthenticationResult(false, ae.getMessage());
		} catch (NamingException e) {
			ar = new AuthenticationResult(false, e.getMessage());
		} catch (Exception e) {
			ar = new AuthenticationResult(false, e.getMessage());
		}
		username = null;
		password = null;
		hashtableEnvironment.clear();
		return ar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hr.fer.zemris.auth.IAuthenticator#authenticate(java.lang.String,
	 *      java.lang.String)
	 */
	public AuthenticationResult authenticate(String username, String password) {
		this.username = username;
		this.password = password;
		return authenticate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hr.fer.zemris.auth.IAuthenticator#setProperty(java.lang.String,
	 *      java.lang.Object)
	 */
	public void setProperty(String name, Object value) {
		if (name == null)
			return;
		if (name.equals(AuthenticatorConsts.AC_USERNAME)) {
			username = (String) value;
		} else if (name.equals(AuthenticatorConsts.AC_PASSWORD)) {
			password = (String) value;
		} else if (name.equals(AC_PROVIDER_URL)) {
			provider_url = (String) value;
		} else if(name.startsWith(AC_LDAP_SPECIFIC)) {
			ldspec.put(name.substring(AC_LDAP_SPECIFIC.length()),value);
		}
	}

	public static void main(String[] args) throws Exception {
		String u, p;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Username: ");
		u = br.readLine();
		System.out.print("Password: ");
		p = br.readLine();
		IAuthenticator ldapa = new LdapAuthenticator();
		ldapa.setProperty(LdapAuthenticator.AC_PROVIDER_URL, "ldap://gazda.cc.fer.hr");
		ldapa.setProperty(LdapAuthenticator.AC_LDAP_SPECIFIC+"java.naming.ldap.version","3");
		AuthenticationResult ar = ldapa.authenticate(u, p);
		if (ar.isSuccess()) {
			System.out.println("Successfull!");
		} else {
			System.out.println("Authentication failure: " + ar.getMessage());
		}
	}
}