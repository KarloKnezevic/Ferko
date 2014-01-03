package hr.fer.zemris.tests;

import hr.fer.zemris.jcms.service.sso.ISSOChecker;
import hr.fer.zemris.jcms.service.sso.SSOCheckerFactory;

public class SSOTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String code = "MČ005";
		String courseID = "31498";
		String time = "1226521022";
		String auth = "05355e4ab4eb52c9e4b61b97bff1318d";
		
		ISSOChecker checker = SSOCheckerFactory.getInstance(null);
		boolean result = checker.check(code, courseID, time, auth);
		System.out.println("Result = "+result);
	}

}
