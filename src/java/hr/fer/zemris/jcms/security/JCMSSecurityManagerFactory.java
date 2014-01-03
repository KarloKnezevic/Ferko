package hr.fer.zemris.jcms.security;

public class JCMSSecurityManagerFactory {

	private static IJCMSSecurityManager manager;
	
	public static IJCMSSecurityManager getManager() {
		return manager;
	}
	
	public static void init() {
		manager = new JCMSSecurityManager();
	}
}
