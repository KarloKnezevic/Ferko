package hr.fer.zemris.jcms.service.sso;

public interface ISSOChecker {
	public boolean check(String code, String courseID, String time, String auth);
}
