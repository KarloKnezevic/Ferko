package hr.fer.zemris.jcms.security;

public class JCMSSecurityConstants {
	public static final String NASTAVNIK = "3/1";
	public static final String ASISTENT = "3/2";
	public static final String ADMIN_KOLEGIJA = "3/3";
	public static final String NOSITELJ = "3/4";
	public static final String ASISTENT_ORG = "3/5";
	
	public static final String ROLE_ADMIN = "admin";
	public static final String ROLE_ASISTENT = "asistent";
	public static final String ROLE_LECTURER = "lecturer";
	public static final String ROLE_COURSE_STAFF = "n_osoblje";
	public static final String ROLE_STUDENT = "student";
	
	static final String[] ALL_COURSE_ROLES = new String[] {ADMIN_KOLEGIJA, NOSITELJ, NASTAVNIK, ASISTENT_ORG, ASISTENT};
	
	private static final String[] securityCourseRoles = new String[] {"3/1", "3/2", "3/3", "3/4", "3/5"};
	private static final String[] securityCourseRoleNames = new String[] {"lecturers","assistants","courseAdmin", "mainLecturers", "mainAssistants"};

	public static String getSecurityCourseRole(int index) {
		return securityCourseRoles[index];
	}

	public static String getSecurityCourseRoleName(int index) {
		return securityCourseRoleNames[index];
	}

	public static int getSecurityCourseRolesCount() {
		return securityCourseRoles.length;
	}
	
	public static final String SEC_ROLE_GROUP = "3";
	public static final String SEC_ROLE_GROUP_NAME = "sec-groups";
	
}
