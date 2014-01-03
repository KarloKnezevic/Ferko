package hr.fer.zemris.jcms.service.sso;

import java.util.HashMap;
import java.util.Map;

public class SSOCheckerFactory {

	private static final String defaultChecker = "ferweb";
	private static final Map<String,ISSOChecker> checkersMap = new HashMap<String, ISSOChecker>();
	private static final ISSOChecker falseChecker = new FalseSSOChecker();
	
	static {
		checkersMap.put(defaultChecker, new FERWebSSOChecker());
	}
	public static ISSOChecker getInstance(String system) {
		if(system==null || system.length()==0) {
			system = defaultChecker;
		}
		ISSOChecker c = checkersMap.get(system);
		if(c == null) {
			return falseChecker;
		}
		return c;
	}
}
