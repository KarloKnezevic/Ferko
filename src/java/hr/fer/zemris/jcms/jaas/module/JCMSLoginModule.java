package hr.fer.zemris.jcms.jaas.module;

import hr.fer.zemris.jcms.bcon.BConMessageReader;
import hr.fer.zemris.jcms.bcon.BConMessageWriterSupport;
import hr.fer.zemris.jcms.bcon.BConMsgCheckAuth;
import hr.fer.zemris.jcms.bcon.BConMsgCheckAuthStatus;
import hr.fer.zemris.jcms.bcon.BConMsgQuit;
import hr.fer.zemris.jcms.jaas.principals.RolePrincipal;
import hr.fer.zemris.jcms.jaas.principals.UserPrincipal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * <p>This is JAAS JCMS Login Module.</p>
 * <p>Configuration:</p>
 * <ol>
 * <li>
 * <p>in tomcat conf directory create file <code>tomcat-jaas.config</code>:</p>
 * <pre>
 * JCMS {
 *   hr.fer.zemris.jcms.jaas.module.JCMSLoginModule REQUIRED option1=value1 ... optioni=valuei;
 * };
 * </pre>
 * Currently, recognized options are:
 * <table>
 * <tr><td><b>Option</b></td><td><b>Mandatory</b></td><td><b>Description</b></td></tr>
 * <tr><td>debug</td><td>optional</td><td>Should debug messages be written? Defaults to false.</td></tr>
 * <tr><td>jcmsHostName</td><td>required</td><td>JCMS host.</td></tr>
 * <tr><td>jcmsPort</td><td>required</td><td>JCMS port.</td></tr>
 * </table>
 * </li>
 * <li>
 * <p>Option1, ... Optioni are option names (and appropriate values) that this module understands.
 * This options are stored in map options and can be used from there.</p>
 * </li>
 * <li>
 * <p>Place jcms-jaas-version.jar into <code>$CATALINA_HOME/server/lib</code>.</p>
 * </li>
 * <li>
 * <p>Add environment variable <code>JAVA_OPTS=-Djava.security.auth.login.config==$CATALINA_HOME/conf/tomcat-jaas.config</code></p>
 * </li>
 * <li>
 * <p>In your web application, configure security restrictions as deemed appropriate. Also, configure context file. For example,
 *    if your application name is app1, then:</p>
 *    <ul>
 *    <li>You will have application residing in $CATALINA_HOME/webapps/app1, and there will be $CATALINA_HOME/webapps/app1/WEB-INF/web.xml with
 *        security restrictions.</li>
 *    <li>You will have to create web application context file containing server-side configuration for your application. This file will be
 *        $CATALINA_HOME/conf/Catalina/localhost/app1.xml with following content:
 *        <pre>
 *        &lt;Context path="/app1" reloadable="true"&gt;
 *        
 *        &lt;Realm
 *               className="org.apache.catalina.realm.JAASRealm"
 *               appName="JCMS"
 *               userClassNames="hr.fer.zemris.jcms.jaas.principals.UserPrincipal"
 *               roleClassNames="hr.fer.zemris.jcms.jaas.principals.RolePrincipal"
 *               debug="99"/&gt;
 *        
 *        &lt;/Context&gt; 
 *        </pre>
 *        </li>
 *    </ul>
 * </li>
 * </ol>
 * 
 * @author marcupic
 *
 */
public class JCMSLoginModule implements LoginModule {

	private BConMessageWriterSupport wSupport = new BConMessageWriterSupport();
	private BConMessageReader msgReader = new BConMsgCheckAuthStatus.Reader();
	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map<String, ?> options;

	// recognized options
	private boolean debug;
	private String jcmsHostName;
	private int jcmsPort;

	private boolean succeeded = false;
	private boolean commitSucceeded = false;
	
	// username and password
	private String username;
	private char[] password;
	private String[] roleNames;

	UserPrincipal userPrincipal;
	RolePrincipal[] rolePrincipal;

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.options = options;
		debug = "true".equals(this.options.get("debug"));
		jcmsHostName = (String)this.options.get("jcmsHostName");
		jcmsPort = Integer.valueOf((String)this.options.get("jcmsPort"));
		if(debug) {
			System.out.println("[JCMSLoginModule] debug = "+debug);
			System.out.println("[JCMSLoginModule] jcmsHostName = "+jcmsHostName);
			System.out.println("[JCMSLoginModule] jcmsPort = "+jcmsPort);
		}
	}

	@Override
	public boolean login() throws LoginException {
		// prompt for a user name and password
		if (callbackHandler == null)
			throw new LoginException("Error: no CallbackHandler available "
					+ "to garner authentication information from the user");

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("user name: ");
		callbacks[1] = new PasswordCallback("password: ", false);

		if(debug) {
			System.out.println("NescumeLoginModule.login called.");
		}

		try {
			callbackHandler.handle(callbacks);
			username = ((NameCallback) callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback) callbacks[1])
					.getPassword();
			if (tmpPassword == null) {
				// treat a NULL password as an empty password
				tmpPassword = new char[0];
			}
			password = new char[tmpPassword.length];
			System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
			((PasswordCallback) callbacks[1]).clearPassword();
		} catch (java.io.IOException ioe) {
			throw new LoginException(ioe.toString());
		} catch (UnsupportedCallbackException uce) {
			throw new LoginException("Error: " + uce.getCallback().toString()
					+ " not available to garner authentication information "
					+ "from the user");
		}
		try {
			succeeded = authenticate();
		} catch(LoginException ex) {
			ex.printStackTrace();
			succeeded = false;
			username=null;
			clearPassword();
			throw ex;
		} catch(RuntimeException t) {
			t.printStackTrace();
			succeeded = false;
			username=null;
			clearPassword();
			throw t;
		}
		clearPassword();
		if(!succeeded) {
			username=null;
			throw new FailedLoginException("Username and/or password incorrect.");
		}
		return true;
	}

	@Override
	public boolean commit() throws LoginException {
		if(!succeeded) return false;
		userPrincipal = new UserPrincipal(username);
		rolePrincipal = new RolePrincipal[roleNames.length];
		for(int i = 0; i < roleNames.length; i++) {
			rolePrincipal[i] = new RolePrincipal(roleNames[i]);
		}
		if(!subject.getPrincipals().contains(userPrincipal)) {
			subject.getPrincipals().add(userPrincipal);
		}
		for(int i = 0; i < rolePrincipal.length; i++) {
			if(!subject.getPrincipals().contains(rolePrincipal[i])) {
				subject.getPrincipals().add(rolePrincipal[i]);
			}
		}
		commitSucceeded = true;
		return true;
	}

	@Override
	public boolean abort() throws LoginException {
		if(!succeeded) return false;
		if(!commitSucceeded) {
			clearAll();
		} else {
			logout();
		}
		return true;
	}

	@Override
	public boolean logout() throws LoginException {
		subject.getPrincipals().remove(userPrincipal);
		for(int i = 0; i < rolePrincipal.length; i++) {
			subject.getPrincipals().remove(rolePrincipal[i]);
		}
		clearAll();
		return true;
	}

	private void clearPassword() {
		if(password==null) return;
		for (int i = 0; i < password.length; i++)
			password[i] = ' ';
		password = null;
	}

	private void clearAll() {
		username = null;
		userPrincipal = null;
		rolePrincipal = null;
		clearPassword();
	}


	private boolean authenticate() throws LoginException {
		Socket sock = null;
		try {
			sock = new Socket(jcmsHostName, jcmsPort);
			sock.setSoTimeout(30000);
			DataInputStream dis = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
			new BConMsgCheckAuth(username, password==null ? "" : new String(password)).write(dos, wSupport);
			dos.flush();
			short id = dis.readShort();
			if(id!=BConMsgCheckAuthStatus.ID) {
				throw new LoginException("JCMS returned unexpected response.");
			}
			BConMsgCheckAuthStatus stat = (BConMsgCheckAuthStatus)msgReader.read(dis);
			new BConMsgQuit().write(dos, wSupport);
			dos.flush();
			if(!stat.isAccepted()) {
				return false;
			}
			if(stat.getRoles()!=null) {
				roleNames = new String[stat.getRoles().size()];
				stat.getRoles().toArray(roleNames);
			} else {
				roleNames = new String[0];
			}
			return true;
		} catch(IOException ex) {
			throw new LoginException("Exception occured: "+ex.getMessage());
		} finally {
			try { sock.close(); } catch(Exception ignorable) {}
		}
	}
}
