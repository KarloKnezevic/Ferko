package hr.fer.zemris.jcms.service.sso;

public final class FalseSSOChecker implements ISSOChecker {

	@Override
	public boolean check(String code, String courseID, String time, String auth) {
		return false;
	}

}
