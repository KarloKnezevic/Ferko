package hr.fer.zemris.tests;

import hr.fer.zemris.jcms.jaas.module.JCMSLoginModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

public class JAASTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws LoginException {
		System.console().writer().write("Your username: ");
		System.console().writer().flush();
		final String username = System.console().readLine();
		System.console().writer().write("Your password: ");
		System.console().writer().flush();
		final String password = new String(System.console().readPassword());
		JCMSLoginModule module = new JCMSLoginModule();
		Subject s = new Subject();
		Map<String,String> options = new HashMap<String, String>();
		options.put("debug", "true");
		options.put("jcmsHostName", "localhost");
		options.put("jcmsPort", "12845");
		module.initialize(s, new CallbackHandler() {
			@Override
			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				for(Callback c : callbacks) {
					if(c instanceof NameCallback) {
						((NameCallback)c).setName(username);
						continue;
					}
					if(c instanceof PasswordCallback) {
						((PasswordCallback)c).setPassword(password.toCharArray());
						continue;
					}
					System.out.println("Unknown callback: "+c);
				}
			}
		}, null, options);
		boolean mlogin = module.login();
		System.out.println("Login returned "+mlogin);
		if(!mlogin) return;
		boolean mcommit = module.commit();
		System.out.println("Commit returned "+mcommit);
		if(!mcommit) return;
		System.out.println("Roles are:");
		System.out.println(s.getPrincipals());
	}

}
