/*
 * Created on Oct 6, 2004
 */
package hr.fer.zemris.auth.ferwebauth;

import hr.fer.zemris.auth.AuthenticationResult;
import hr.fer.zemris.auth.AuthenticatorConsts;
import hr.fer.zemris.auth.IAuthenticator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

/**
 * @author marcupic
 */
public class FerWebAuthenticator implements IAuthenticator {

	public static final String AC_HOST = "ac_host";

	private String username;

	private String password;

	private String hostname;


	public FerWebAuthenticator() {
	}

	public FerWebAuthenticator(String host) {
		this.hostname = host;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hr.fer.zemris.auth.IAuthenticator#authenticate()
	 */
	public AuthenticationResult authenticate() {
		// Do logic here...
		AuthenticationResult ar = null;
		
		XmlRpcClient client = null;
		
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(hostname));
			config.setConnectionTimeout(60*1000);
			config.setReplyTimeout(60*1000);
		} catch (MalformedURLException e1) {
			username=null; password = null;
			ar = new AuthenticationResult(false, "Invalid authentication URL.");
			e1.printStackTrace();
			return ar;
		}
		client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		client.setConfig(config);
		Object[] params = new Object[] {username,password};
		Boolean ok = Boolean.valueOf(false);
		try {
			ok = (Boolean)client.execute("auth.auth",params);
		} catch (XmlRpcException e) {
			username=null; password = null;
			ar = new AuthenticationResult(false, "Exception while performing XmlRpc authentication action.");
			e.printStackTrace();
			return ar;
		}
		if(ok.booleanValue()) {
			ar = new AuthenticationResult(true, null);
		} else {
			ar = new AuthenticationResult(false, "Username or password is invalid.");
		}
		username = null;
		password = null;
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
		} else if (name.equals(AC_HOST)) {
			hostname = (String) value;
		}
	}

	public static void main(String[] args) throws Exception {
		String u, p;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Username: ");
		u = br.readLine();
		System.out.print("Password: ");
		p = br.readLine();

		FerWebAuthenticator fwa = new FerWebAuthenticator();
		fwa.setProperty(FerWebAuthenticator.AC_HOST, "https://www.fer.hr/xmlrpc/xr_auth.php");
		AuthenticationResult ar = fwa.authenticate(u, p);
		if (ar.isSuccess()) {
			System.out.println("Successfull!");
		} else {
			System.out.println("Authentication failure: " + ar.getMessage());
		}
	}
}