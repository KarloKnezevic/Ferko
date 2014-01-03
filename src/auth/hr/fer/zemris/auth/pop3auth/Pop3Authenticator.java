/*
 * Created on Oct 6, 2004
 */
package hr.fer.zemris.auth.pop3auth;

import hr.fer.zemris.auth.AuthenticationResult;
import hr.fer.zemris.auth.AuthenticatorConsts;
import hr.fer.zemris.auth.IAuthenticator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * @author marcupic
 */
public class Pop3Authenticator implements IAuthenticator {

	public static final String AC_HOST = "ac_host";

	public static final String AC_PORT = "ac_port";

	private String username;

	private String password;

	private String hostname;

	private short port = (short) 110;

	public Pop3Authenticator() {
	}

	public Pop3Authenticator(String host, short port) {
		this.hostname = host;
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hr.fer.zemris.auth.IAuthenticator#authenticate()
	 */
	public AuthenticationResult authenticate() {
		// Do logic here...
		AuthenticationResult ar = null;
		Socket sock = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		String l = null;
		try {
			sock = new Socket(hostname, port);
			br = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(sock
					.getOutputStream()));
			while (true) {
				// read hello message
				l = br.readLine();
				//System.out.println(">"+l);
				//System.out.println(new StringBuffer("USER ").append(username).append(
				//"\r\n").toString());
				// send user
				bw.write(new StringBuffer("USER ").append(username).append(
						"\r\n").toString());
				bw.flush();
				// read response message
				l = br.readLine();
				//System.out.println(">"+l);
				if (!l.toUpperCase().startsWith("+OK")) {
					ar = new AuthenticationResult(false,
							"User not recognized (" + l + ").");
					break;
				}
				// send password
				//System.out.println(new StringBuffer("PASS ").append(password).append(
				//"\r\n").toString());
				bw.write(new StringBuffer("PASS ").append(password).append(
						"\r\n").toString());
				bw.flush();
				// read response message
				l = br.readLine();
				//System.out.println(">"+l);
				if (!l.toUpperCase().startsWith("+OK")) {
					ar = new AuthenticationResult(false, "Invalid password ("
							+ l + ").");
					break;
				}
				// send quit
				bw.write("QUIT\r\n");
				ar = new AuthenticationResult(true, null);
				break;
			}
		} catch (java.io.IOException ex) {
			ar = new AuthenticationResult(false, ex.getMessage());
		}
		username = null;
		password = null;
		if (bw != null)
			try {
				bw.close();
			} catch (Exception ex) {
			}
		if (br != null)
			try {
				br.close();
			} catch (Exception ex) {
			}
		if (sock != null)
			try {
				sock.close();
			} catch (Exception ex) {
			}
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
		} else if (name.equals(AC_PORT)) {
			if (value instanceof Short)
				port = ((Short) value).shortValue();
			else if (value instanceof Integer)
				port = ((Integer) value).shortValue();
			else
				port = Integer.valueOf(value.toString()).shortValue();
		}
	}

	public static void main(String[] args) throws Exception {
		String u, p;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Username: ");
		u = br.readLine();
		System.out.print("Password: ");
		p = br.readLine();

		Pop3Authenticator p3a = new Pop3Authenticator();
		p3a.setProperty(Pop3Authenticator.AC_HOST, "pinus.cc.fer.hr");
		p3a.setProperty(Pop3Authenticator.AC_PORT, new Short((short) 110));
		AuthenticationResult ar = p3a.authenticate(u, p);
		if (ar.isSuccess()) {
			System.out.println("Successfull!");
		} else {
			System.out.println("Authentication failure: " + ar.getMessage());
		}

	}
}